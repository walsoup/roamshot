package app.polaroam.ai.llm

import app.polaroam.ai.safety.QuestSafetyFilter
import app.polaroam.ai.vision.LabelVocabulary
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestCategory
import app.polaroam.domain.model.QuestRules
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class QuestEnvelopeDto(val quests: List<QuestDto>)

@Serializable
internal data class QuestDto(
    val title: String,
    val objective: String,
    val category: String,
    val difficulty: Int,
    val targetLabels: List<String>,
)

sealed interface QuestParseResult {
    data class Valid(val quests: List<Quest>) : QuestParseResult
    data class Invalid(val reason: String) : QuestParseResult
}

/** Strict parser and validator for LLM quest output. */
class QuestJsonParser(
    private val safety: QuestSafetyFilter,
) {
    private val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
    }

    fun parse(raw: String, expectedCount: Int, idFactory: () -> String): QuestParseResult {
        val body = stripCodeFence(raw.trim())
        if (body.isEmpty()) return QuestParseResult.Invalid("the reply was empty")

        val envelope = try {
            val trimmed = body.trim()
            if (trimmed.startsWith("[")) {
                QuestEnvelopeDto(json.decodeFromString<List<QuestDto>>(trimmed))
            } else {
                json.decodeFromString(QuestEnvelopeDto.serializer(), trimmed)
            }
        } catch (e: Exception) {
            try {
                QuestEnvelopeDto(json.decodeFromString<List<QuestDto>>(body.trim()))
            } catch (_: Exception) {
                return QuestParseResult.Invalid("the reply was not valid JSON for the schema (${e.message?.take(120)})")
            }
        }

        if (envelope.quests.size < expectedCount) {
            return QuestParseResult.Invalid("expected $expectedCount quests but got ${envelope.quests.size}")
        }

        val problems = mutableListOf<String>()
        val seenTitles = mutableSetOf<String>()
        val valid = envelope.quests.mapIndexedNotNull { index, dto ->
            when (val checked = validate(dto, idFactory)) {
                is Checked.Ok -> {
                    if (seenTitles.add(checked.quest.title.lowercase())) {
                        checked.quest
                    } else {
                        problems += "quest ${index + 1}: duplicate title"
                        null
                    }
                }
                is Checked.Rejected -> {
                    problems += "quest ${index + 1}: ${checked.reason}"
                    null
                }
            }
        }

        return if (valid.size >= expectedCount) {
            QuestParseResult.Valid(valid.take(expectedCount))
        } else {
            QuestParseResult.Invalid(problems.joinToString("; ").ifEmpty { "not enough valid quests" })
        }
    }

    private sealed interface Checked {
        data class Ok(val quest: Quest) : Checked
        data class Rejected(val reason: String) : Checked
    }

    private fun validate(dto: QuestDto, idFactory: () -> String): Checked {
        val title = dto.title.trim()
        val objective = dto.objective.trim()

        if (title.length !in QuestRules.TITLE_MIN..QuestRules.TITLE_MAX) {
            return Checked.Rejected("title must be ${QuestRules.TITLE_MIN}-${QuestRules.TITLE_MAX} characters")
        }
        if (objective.length !in QuestRules.OBJECTIVE_MIN..QuestRules.OBJECTIVE_MAX) {
            return Checked.Rejected("objective must be ${QuestRules.OBJECTIVE_MIN}-${QuestRules.OBJECTIVE_MAX} characters")
        }
        val category = QuestCategory.fromWire(dto.category)
            ?: return Checked.Rejected("unknown category \"${dto.category.take(30)}\"")
        val difficulty = Difficulty.fromLevel(dto.difficulty)
            ?: return Checked.Rejected("difficulty must be 1, 2 or 3")
        if (dto.targetLabels.size !in QuestRules.MIN_LABELS..QuestRules.MAX_LABELS) {
            return Checked.Rejected("targetLabels must have ${QuestRules.MIN_LABELS}-${QuestRules.MAX_LABELS} items")
        }
        val labels = dto.targetLabels
            .map(LabelVocabulary::canonical)
            .filter { it in LabelVocabulary.labels }
            .distinct()
        if (labels.size < QuestRules.MIN_LABELS) {
            return Checked.Rejected("targetLabels must use at least ${QuestRules.MIN_LABELS} words from the allowed list")
        }
        safety.violation(title, objective, labels)?.let { term ->
            return Checked.Rejected("breaks a safety rule (\"$term\")")
        }

        return Checked.Ok(
            Quest(
                id = idFactory(),
                title = title,
                objective = objective,
                category = category,
                difficulty = difficulty,
                targetLabels = labels,
            ),
        )
    }

    companion object {
        private val fence = Regex("^```[a-zA-Z]*\\s*([\\s\\S]*?)\\s*```$")

        internal fun stripCodeFence(text: String): String {
            val trimmed = text.trim()
            fence.matchEntire(trimmed)?.let { return it.groupValues[1].trim() }
            if (trimmed.startsWith("```")) {
                val afterFirstLine = trimmed.substringAfter("\n").trim()
                return afterFirstLine.removeSuffix("```").trim()
            }
            return trimmed
        }
    }
}
