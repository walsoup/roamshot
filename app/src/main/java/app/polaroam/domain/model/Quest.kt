package app.polaroam.domain.model

object QuestRules {
    const val MIN_QUESTS = 3
    const val MAX_QUESTS = 5
    const val DEFAULT_QUESTS = 4
    const val MIN_LABELS = 2
    const val MAX_LABELS = 5
    const val TITLE_MIN = 3
    const val TITLE_MAX = 40
    const val OBJECTIVE_MIN = 10
    const val OBJECTIVE_MAX = 160

    fun clampCount(count: Int): Int = count.coerceIn(MIN_QUESTS, MAX_QUESTS)
}

enum class Difficulty(val level: Int) {
    EASY(1),
    MEDIUM(2),
    HARD(3);

    companion object {
        fun fromLevel(level: Int): Difficulty? = entries.firstOrNull { it.level == level }
    }
}

enum class QuestCategory {
    NATURE,
    ANIMALS,
    ARCHITECTURE,
    TEXTURES,
    COLORS,
    OBJECTS,
    SKY;

    companion object {
        fun fromWire(value: String): QuestCategory? {
            val key = value.trim().replace(' ', '_')
            return entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
        }
    }
}

/**
 * A single photo side quest. [targetLabels] are alternatives: a photo matches when the
 * vision model recognizes any one of them.
 */
data class Quest(
    val id: String,
    val title: String,
    val objective: String,
    val category: QuestCategory,
    val difficulty: Difficulty,
    val targetLabels: List<String>,
) {
    init {
        require(targetLabels.size in QuestRules.MIN_LABELS..QuestRules.MAX_LABELS) {
            "Quest needs ${QuestRules.MIN_LABELS}-${QuestRules.MAX_LABELS} target labels, got ${targetLabels.size}"
        }
    }
}
