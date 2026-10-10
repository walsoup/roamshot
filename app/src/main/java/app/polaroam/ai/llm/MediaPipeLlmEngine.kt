package app.polaroam.ai.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
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
 * Gemma via the MediaPipe LLM Inference API. All native calls run on one dedicated thread.
 * Inference itself is blocking, so callers awaiting a result can time out and move on while the
 * native call finishes in the background.
 */
class MediaPipeLlmEngine(
    context: Context,
    private val modelFile: File,
    private val maxTokens: Int = DEFAULT_MAX_TOKENS,
) : LlmEngine {

    private val appContext = context.applicationContext
    private val executor = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "polaroam-llm") }
    private val scope = CoroutineScope(SupervisorJob() + executor.asCoroutineDispatcher())
    private val closed = AtomicBoolean(false)

    // Only touched on the executor thread.
    private var inference: LlmInference? = null

    override suspend fun complete(prompt: LlmPrompt): String {
        check(!closed.get()) { "LLM engine is closed" }
        val deferred = scope.async {
            val llm = inference ?: createInference().also { inference = it }
            llm.generateResponse(GemmaPromptFormat.format(prompt))
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
            runCatching { inference?.close() }
            inference = null
        }
        executor.shutdown()
    }

    private fun createInference(): LlmInference {
        require(modelFile.isFile) { "Model file missing: ${modelFile.absolutePath}" }
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath)
            .setMaxTokens(maxTokens)
            .build()
        return LlmInference.createFromOptions(appContext, options)
    }

    companion object {
        const val DEFAULT_MAX_TOKENS = 1280
    }
}

/** Gemma has no system role; the system text is folded into the first user turn. */
internal object GemmaPromptFormat {
    fun format(prompt: LlmPrompt): String = buildString {
        append("<start_of_turn>user\n")
        append(prompt.system.trim())
        append("\n\n")
        append(prompt.user.trim())
        append("<end_of_turn>\n")
        append("<start_of_turn>model\n")
    }
}
