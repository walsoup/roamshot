package app.polaroam.ai

import app.polaroam.ai.canned.CannedQuestGenerator
import app.polaroam.ai.llm.LlmEngine
import app.polaroam.ai.llm.LlmEngineFactory
import app.polaroam.ai.llm.LlmQuestGenerator
import app.polaroam.ai.llm.QuestJsonParser
import app.polaroam.ai.llm.QuestPromptBuilder
import app.polaroam.ai.model.ModelFiles
import app.polaroam.domain.ai.QuestGenerator
import app.polaroam.domain.model.GenerationResult
import app.polaroam.domain.model.Quest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Uses the on-device LLM when a downloaded model is present, the built-in pool otherwise.
 * The engine is created lazily and rebuilt whenever the model file changes or disappears.
 */
class OnDeviceQuestGenerator(
    private val modelFiles: ModelFiles,
    private val engineFactory: LlmEngineFactory,
    private val fallback: CannedQuestGenerator,
    private val promptBuilder: QuestPromptBuilder,
    private val parser: QuestJsonParser,
) : QuestGenerator {

    private val mutex = Mutex()
    private var engine: LlmEngine? = null
    private var engineStamp: ModelStamp? = null

    override suspend fun generate(walkType: String, count: Int): List<Quest> =
        generateDetailed(walkType, count).quests

    override suspend fun generateDetailed(walkType: String, count: Int): GenerationResult {
        val current = currentEngine() ?: return fallback.generateDetailed(walkType, count)
        return LlmQuestGenerator(current, fallback, promptBuilder, parser).generateDetailed(walkType, count)
    }

    private suspend fun currentEngine(): LlmEngine? = mutex.withLock {
        val model = modelFiles.readyModel()
        val stamp = model?.let { ModelStamp(it.absolutePath, it.length(), it.lastModified()) }
        if (stamp != engineStamp) {
            engine?.close()
            engine = model?.let(engineFactory::create)
            engineStamp = stamp
        }
        engine
    }

    private data class ModelStamp(val path: String, val size: Long, val modified: Long)
}
