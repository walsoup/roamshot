package app.polaroam.domain.model

import java.time.Instant
import java.time.LocalDate

enum class QuestSource { MODEL, BUILT_IN }

enum class QuestStatus { OPEN, COMPLETED, SKIPPED }

data class GenerationResult(
    val quests: List<Quest>,
    val source: QuestSource,
    val fallbackReason: String? = null,
)

data class Completion(
    val questId: String,
    val photoPath: String,
    val points: Int,
    val firstTry: Boolean,
    val label: String,
    val confidence: Float,
    val completedAt: Instant,
    val day: LocalDate,
)

data class WalkQuest(
    val quest: Quest,
    val position: Int,
    val status: QuestStatus,
    val attempts: Int,
    val completion: Completion?,
)

data class Walk(
    val id: String,
    val walkType: String,
    val startedAt: Instant,
    val source: QuestSource,
    val quests: List<WalkQuest>,
) {
    val completedCount: Int get() = quests.count { it.status == QuestStatus.COMPLETED }
    val resolvedCount: Int get() = quests.count { it.status != QuestStatus.OPEN }
    val isFinished: Boolean get() = quests.isNotEmpty() && quests.none { it.status == QuestStatus.OPEN }
    val earnedPoints: Int get() = quests.sumOf { it.completion?.points ?: 0 }
}

data class CompletedQuest(
    val quest: Quest,
    val walkType: String,
    val completion: Completion,
)

object WalkTypeInput {
    const val MAX_LENGTH = 60
    private val whitespace = Regex("\\s+")

    fun normalize(raw: String): String = raw.trim().replace(whitespace, " ").take(MAX_LENGTH).trim()

    fun isValid(raw: String): Boolean = normalize(raw).isNotEmpty()
}
