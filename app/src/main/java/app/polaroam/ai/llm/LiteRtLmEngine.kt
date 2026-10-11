package app.polaroam.ai.llm

import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.ResponseFormat
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ThinkingConfig
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs an open `.litertlm` model (Qwen3-0.6B by default) with Google's open-source LiteRT-LM
 * runtime. All native calls run on one dedicated thread. Inference itself is blocking, so callers
 * awaiting a result can time out and move on while the native call finishes in the background.
 */
class LiteRtLmEngine(
    private val modelFile: File,
    private val cacheDir: File,
    private val maxNumTokens: Int = DEFAULT_MAX_TOKENS,
) : LlmEngine {

    private val executor = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "polaroam-llm") }
    private val scope = CoroutineScope(SupervisorJob() + executor.asCoroutineDispatcher())
    private val closed = AtomicBoolean(false)

    // Only touched on the executor thread.
    private var engine: Engine? = null
    // Constrained decoding with ResponseFormat.json on LiteRT-LM CPU takes minutes per token
    // on mobile CPUs due to per-token grammar DFA state evaluation across 152k vocab tokens.
    // Qwen3 outputs clean JSON directly from the system prompt instructions.
    private var constrainedDecodingWorks = false

    init {
        warmup()
    }

    fun warmup() {
        if (closed.get()) return
        executor.execute {
            if (engine == null && !closed.get()) {
                try {
                    engine = createEngine()
                } catch (e: Exception) {
                    Log.e(TAG, "Engine warmup failed", e)
                }
            }
        }
    }

    override suspend fun complete(prompt: LlmPrompt): String {
        check(!closed.get()) { "LLM engine is closed" }
        val deferred = scope.async {
            val llm = engine ?: createEngine().also { engine = it }
            val schema = prompt.jsonSchema?.takeIf { constrainedDecodingWorks }
            val reply = if (schema == null) {
                Log.d(TAG, "Running send without schema constraint")
                send(llm, prompt, schema = null)
            } else {
                try {
                    Log.d(TAG, "Running send with schema constraint")
                    send(llm, prompt, schema)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Constrained decoding failed; retrying without schema constraint", e)
                    constrainedDecodingWorks = false
                    send(llm, prompt, schema = null)
                }
            }
            QwenOutput.clean(reply)
        }
        return try {
            deferred.await()
        } catch (e: CancellationException) {
            currentCoroutineContext().ensureActive()
            throw IllegalStateException("LLM engine was closed during inference", e)
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        scope.cancel()
        executor.execute {
            runCatching { engine?.close() }
            engine = null
        }
        executor.shutdown()
    }

    private fun send(llm: Engine, prompt: LlmPrompt, schema: String?): String {
        Log.d(TAG, "send() starting conversation. systemInstruction length=${prompt.system.length}, user length=${prompt.user.length}")
        val config = ConversationConfig(
            systemInstruction = Contents.of(prompt.system.trim()),
            samplerConfig = SamplerConfig(topK = TOP_K, topP = TOP_P, temperature = TEMPERATURE),
            maxOutputToken = MAX_OUTPUT_TOKENS,
            thinkingConfig = ThinkingConfig(enableThinking = false, thinkingTokenBudget = 0),
            enableResponseFormat = schema != null,
        )
        return llm.createConversation(config).use { conversation ->
            Log.d(TAG, "Conversation created, sending message async...")
            val sb = StringBuilder()
            val latch = java.util.concurrent.CountDownLatch(1)
            var error: Throwable? = null

            conversation.sendMessageAsync(
                prompt.user.trim(),
                object : com.google.ai.edge.litertlm.MessageCallback {
                    override fun onMessage(message: Message) {
                        val text = message.text()
                        Log.d(TAG, "onMessage chunk (${text.length} chars): ${text.take(80)}")
                        sb.append(text)
                    }

                    override fun onDone() {
                        Log.d(TAG, "onDone() total chars=${sb.length}")
                        latch.countDown()
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, "onError()", throwable)
                        error = throwable
                        latch.countDown()
                    }
                },
            )
            latch.await()
            error?.let { throw it }
            sb.toString()
        }
    }

    private fun createEngine(): Engine {
        Log.d(TAG, "createEngine() for model: ${modelFile.absolutePath} (size=${modelFile.length()})")
        require(modelFile.isFile) { "Model file missing: ${modelFile.absolutePath}" }
        val gpuConfig = EngineConfig(
            modelPath = modelFile.absolutePath,
            backend = Backend.GPU(),
            maxNumTokens = maxNumTokens,
            cacheDir = cacheDir.absolutePath,
        )
        return try {
            Log.d(TAG, "Attempting GPU Engine initialization...")
            Engine(gpuConfig).also { it.initialize() }.also { Log.d(TAG, "GPU Engine initialized successfully!") }
        } catch (e: Throwable) {
            Log.w(TAG, "GPU Engine initialization failed; falling back to CPU", e)
            val cpuConfig = EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = Backend.CPU(threadCount = 4, numOfThreads = 4),
                maxNumTokens = maxNumTokens,
                cacheDir = cacheDir.absolutePath,
            )
            Log.d(TAG, "Initializing CPU Engine...")
            Engine(cpuConfig).also { it.initialize() }.also { Log.d(TAG, "CPU Engine initialized successfully!") }
        }
    }

    private fun Message.text(): String =
        contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }

    companion object {
        private const val TAG = "LiteRtLmEngine"
        const val DEFAULT_MAX_TOKENS = 640
        private const val MAX_OUTPUT_TOKENS = 250

        // Qwen3's recommended non-thinking sampler settings.
        private const val TOP_K = 20
        private const val TOP_P = 0.8
        private const val TEMPERATURE = 0.7
    }
}

/** Qwen3 can still emit an empty or partial `<think>` block with thinking disabled. */
internal object QwenOutput {
    private val thinkBlock = Regex("<think>[\\s\\S]*?</think>")

    fun clean(raw: String): String = raw.replace(thinkBlock, "").substringAfter("</think>").trim()
}
