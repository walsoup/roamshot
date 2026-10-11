package app.polaroam.ai.llm

import app.polaroam.ai.vision.LabelVocabulary
import app.polaroam.domain.model.QuestCategory
import app.polaroam.domain.model.QuestRules
import app.polaroam.domain.model.WalkTypeInput
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class QuestPromptBuilder(
    private val vocabulary: Set<String> = LabelVocabulary.labels,
) {

    fun build(walkType: String, count: Int): LlmPrompt {
        val n = QuestRules.clampCount(count)
        val categories = QuestCategory.entries.joinToString(", ") { "\"${it.name}\"" }
        val labels = vocabulary.sorted().joinToString(", ")

        val system = buildString {
            appendLine("/no_think")
            appendLine("You design short photo side quests for someone on a walk.")
            appendLine("Reply with ONLY one JSON object. No prose, no markdown, no comments.")
            appendLine()
            appendLine("JSON schema:")
            appendLine(
                """{"quests":[{"title":string,"objective":string,"category":string,"difficulty":integer,"targetLabels":[string]}]}""",
            )
            appendLine()
            appendLine("Field rules:")
            appendLine("- \"quests\" has exactly $n items.")
            appendLine("- \"title\": ${QuestRules.TITLE_MIN}-${QuestRules.TITLE_MAX} characters, playful.")
            appendLine("- \"objective\": one sentence, ${QuestRules.OBJECTIVE_MIN}-${QuestRules.OBJECTIVE_MAX} characters, says what to photograph.")
            appendLine("- \"category\": one of $categories.")
            appendLine("- \"difficulty\": 1 (easy), 2 (medium) or 3 (hard).")
            appendLine("- \"targetLabels\": ${QuestRules.MIN_LABELS}-${QuestRules.MAX_LABELS} lowercase nouns, any of which counts as a match, chosen ONLY from: $labels.")
            appendLine("- No other keys.")
            appendLine()
            appendLine("Safety rules (never break these):")
            SAFETY_RULES.forEach { appendLine("- $it") }
            appendLine()
            appendLine("Example JSON output:")
            append("""{"quests":[$EXAMPLE_ITEM]}""")
        }

        val user = "/no_think Walk type: \"${sanitizeWalkType(walkType)}\". Create $n quests that fit this walk."
        return LlmPrompt(system = system, user = user, jsonSchema = jsonSchema(n))
    }

    /** JSON Schema for the reply, so constrained decoding can only produce allowed categories and labels. */
    fun jsonSchema(count: Int): String {
        val n = QuestRules.clampCount(count)
        fun stringEnum(values: Iterable<String>) = buildJsonObject {
            put("type", "string")
            putJsonArray("enum") { values.forEach { add(it) } }
        }
        val quest = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("title") { put("type", "string") }
                putJsonObject("objective") { put("type", "string") }
                put("category", stringEnum(QuestCategory.entries.map { it.name }))
                putJsonObject("difficulty") {
                    put("type", "integer")
                    putJsonArray("enum") { (1..3).forEach { add(it) } }
                }
                putJsonObject("targetLabels") {
                    put("type", "array")
                    put("minItems", QuestRules.MIN_LABELS)
                    put("maxItems", QuestRules.MAX_LABELS)
                    put("items", stringEnum(vocabulary.sorted()))
                }
            }
            putJsonArray("required") { QUEST_KEYS.forEach { add(it) } }
            put("additionalProperties", false)
        }
        return buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("quests") {
                    put("type", "array")
                    put("minItems", n)
                    put("maxItems", n)
                    put("items", quest)
                }
            }
            putJsonArray("required") { add("quests") }
            put("additionalProperties", false)
        }.toString()
    }

    fun buildRetry(original: LlmPrompt, problem: String): LlmPrompt {
        val reason = problem.replace(controlChars, " ").take(MAX_REASON_LENGTH)
        return original.copy(
            user = original.user +
                "\n\nYour previous reply was rejected: $reason. " +
                "Reply again with ONLY the JSON object that follows every rule.",
        )
    }

    fun sanitizeWalkType(raw: String): String =
        WalkTypeInput.normalize(raw.replace(unsafeChars, " ")).ifEmpty { DEFAULT_WALK_TYPE }

    companion object {
        val SAFETY_RULES = listOf(
            "Never ask to photograph people, faces, or anyone's body, not even from far away.",
            "Never require trespassing, entering private property, or reaching into private gardens or yards.",
            "Never involve roads, streets, traffic or vehicles, and never ask anyone to stand in or near a roadway.",
            "Nothing unsafe at night: keep to well-lit, open, public paths; no dark, isolated, high or watery places.",
            "Never require money, purchases, tickets, or going into shops or cafes.",
            "Never ask to touch, pick, feed, chase or disturb animals or plants.",
        )

        private val QUEST_KEYS = listOf("title", "objective", "category", "difficulty", "targetLabels")
        private const val DEFAULT_WALK_TYPE = "a relaxed walk"
        private const val MAX_REASON_LENGTH = 300
        private val unsafeChars = Regex("[\\r\\n\\t\"`{}<>\\\\]")
        private val controlChars = Regex("[\\r\\n\\t]")

        private const val EXAMPLE_ITEM =
            """{"title":"Bench Story","objective":"Find a bench with old wood or a great view.","category":"OBJECTS","difficulty":1,"targetLabels":["bench","wood"]}"""
    }
}
