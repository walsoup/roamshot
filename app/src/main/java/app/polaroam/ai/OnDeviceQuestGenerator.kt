package app.polaroam.ai

import android.util.Log
import app.polaroam.ai.canned.CannedQuestGenerator
import app.polaroam.ai.llm.LlmEngine
import app.polaroam.ai.llm.LlmEngineFactory
import app.polaroam.ai.llm.LlmQuestGenerator
import app.polaroam.ai.llm.QuestJsonParser
import app.polaroam.ai.llm.QuestPromptBuilder
import app.polaroam.ai.model.ModelFiles
import app.polaroam.ai.model.ModelSpec
import app.polaroam.ai.model.ModelStamp
import app.polaroam.domain.ai.QuestGenerator
import app.polaroam.domain.model.GenerationResult
import app.polaroam.domain.model.Quest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Uses the on-device LLM when a downloaded model is present, the built-in pool otherwise.
 * The engine is created lazily and rebuilt whenever the model file changes or disappears.
 */
class OnDeviceQuestGenerator(
    private val modelFiles: ModelFiles,
    private val activeQuestModel: () -> ModelSpec,
    private val engineFactory: LlmEngineFactory,
    private val fallback: CannedQuestGenerator,
    private val promptBuilder: QuestPromptBuilder,
    private val parser: QuestJsonParser,
) : QuestGenerator {

    constructor(
        modelFiles: ModelFiles,
        questModel: ModelSpec,
        engineFactory: LlmEngineFactory,
        fallback: CannedQuestGenerator,
        promptBuilder: QuestPromptBuilder,
        parser: QuestJsonParser,
    ) : this(
        modelFiles = modelFiles,
        activeQuestModel = { questModel },
        engineFactory = engineFactory,
        fallback = fallback,
        promptBuilder = promptBuilder,
        parser = parser,
    )

    private val mutex = Mutex()
    private var engine: LlmEngine? = null
    private var engineStamp: ModelStamp? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                currentEngine()
            } catch (e: Exception) {
                Log.w(TAG, "Background engine warmup skipped or failed", e)
            }
        }
    }

    override suspend fun generate(walkType: String, count: Int): List<Quest> =
        generateDetailed(walkType, count).quests

    override suspend fun generateDetailed(walkType: String, count: Int): GenerationResult {
        val questModel = activeQuestModel()
        val model = modelFiles.readyModel(questModel)
        Log.d(TAG, "readyModel: ${model?.absolutePath} (exists=${model?.isFile}, size=${model?.length()})")
        val current = currentEngine()
        if (current == null) {
            Log.w(TAG, "No LLM engine ready, falling back to canned pool")
            return fallback.generateDetailed(walkType, count)
        }
        return LlmQuestGenerator(current, fallback, promptBuilder, parser).generateDetailed(walkType, count)
    }

    private suspend fun currentEngine(): LlmEngine? = mutex.withLock {
        val questModel = activeQuestModel()
        val model = modelFiles.readyModel(questModel)
        val stamp = ModelStamp.of(model)
        if (stamp != engineStamp) {
            engine?.close()
            engine = model?.let {
                Log.d(TAG, "Creating engine from model ${it.absolutePath}")
                engineFactory.create(it)
            }
            engineStamp = stamp
        }
        engine
    }

    private companion object {
        const val TAG = "OnDeviceQuestGen"
    }
}

