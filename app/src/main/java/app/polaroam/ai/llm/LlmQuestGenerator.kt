package app.polaroam.ai.llm

import app.polaroam.domain.ai.QuestGenerator
import app.polaroam.domain.model.GenerationResult
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestRules
import app.polaroam.domain.model.QuestSource
import java.util.UUID
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException

/**
 * Asks the LLM for quests, validates the strict JSON reply, retries once with the validation
 * error, and falls back to [fallback] if both attempts fail. Never throws except on cancellation.
 */
class LlmQuestGenerator(
    private val engine: LlmEngine,
    private val fallback: QuestGenerator,
    private val promptBuilder: QuestPromptBuilder,
    private val parser: QuestJsonParser,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) : QuestGenerator {

    override suspend fun generate(walkType: String, count: Int): List<Quest> =
        generateDetailed(walkType, count).quests

    override suspend fun generateDetailed(walkType: String, count: Int): GenerationResult {
        val wanted = QuestRules.clampCount(count)
        val basePrompt = promptBuilder.build(walkType, wanted)
        var prompt = basePrompt
        var lastProblem = "no attempt made"

        repeat(MAX_ATTEMPTS) { attempt ->
            if (attempt > 0) prompt = promptBuilder.buildRetry(basePrompt, lastProblem)

            val reply = try {
                withTimeout(timeoutMillis) { engine.complete(prompt) }
            } catch (e: TimeoutCancellationException) {
                lastProblem = "the model took too long to answer"
                return@repeat
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastProblem = "the model failed: ${e.message ?: e::class.java.simpleName}"
                return@repeat
            }

            when (val parsed = parser.parse(reply, wanted, idFactory)) {
                is QuestParseResult.Valid -> return GenerationResult(parsed.quests, QuestSource.MODEL)
                is QuestParseResult.Invalid -> lastProblem = parsed.reason
            }
        }

        return GenerationResult(
            quests = fallback.generate(walkType, wanted),
            source = QuestSource.BUILT_IN,
            fallbackReason = lastProblem,
        )
    }

    companion object {
        /** First try plus exactly one retry. */
        const val MAX_ATTEMPTS = 2
        const val DEFAULT_TIMEOUT_MILLIS = 60_000L
    }
}
