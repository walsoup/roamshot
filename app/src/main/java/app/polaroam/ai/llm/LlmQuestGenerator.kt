package app.polaroam.ai.llm

import android.util.Log
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

        for (attempt in 0 until MAX_ATTEMPTS) {
            Log.d(TAG, "Starting LLM quest generation attempt ${attempt + 1}/$MAX_ATTEMPTS for walk '$walkType'")
            if (attempt > 0) prompt = promptBuilder.buildRetry(basePrompt, lastProblem)

            val reply = try {
                withTimeout(timeoutMillis) { engine.complete(prompt) }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "LLM timed out after ${timeoutMillis}ms", e)
                lastProblem = "the model took too long to answer"
                break
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "LLM generation threw exception", e)
                lastProblem = "the model failed: ${e.message ?: e::class.java.simpleName}"
                continue
            }

            Log.d(TAG, "LLM raw reply (length=${reply.length}): $reply")
            when (val parsed = parser.parse(reply, wanted, idFactory)) {
                is QuestParseResult.Valid -> {
                    Log.d(TAG, "Successfully parsed ${parsed.quests.size} valid quests from LLM")
                    return GenerationResult(parsed.quests, QuestSource.MODEL)
                }
                is QuestParseResult.Invalid -> {
                    Log.w(TAG, "Parsing LLM response failed: ${parsed.reason}")
                    lastProblem = parsed.reason
                    val partial = parser.parse(reply, 1, idFactory)
                    if (partial is QuestParseResult.Valid && partial.quests.isNotEmpty()) {
                        Log.d(TAG, "Parsed ${partial.quests.size} valid model quests; padding ${wanted - partial.quests.size} with fallback")
                        val needed = wanted - partial.quests.size
                        val padded = if (needed > 0) fallback.generate(walkType, needed) else emptyList()
                        return GenerationResult(partial.quests + padded, QuestSource.MODEL)
                    }
                }
            }
        }

        Log.w(TAG, "Falling back to canned quests. Reason: $lastProblem")
        return GenerationResult(
            quests = fallback.generate(walkType, wanted),
            source = QuestSource.BUILT_IN,
            fallbackReason = lastProblem,
        )
    }

    companion object {
        private const val TAG = "LlmQuestGenerator"
        /** First try plus exactly one retry. */
        const val MAX_ATTEMPTS = 2
        const val DEFAULT_TIMEOUT_MILLIS = 300_000L
    }
}
