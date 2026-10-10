package app.polaroam.ai.safety

import app.polaroam.domain.model.Quest

/**
 * Last line of defense behind the prompt: rejects quests that mention people, trespassing,
 * roads and traffic, unsafe night situations, or spending money.
 */
class QuestSafetyFilter {

    /** Returns the first offending term, or null if the text is clean. */
    fun violation(vararg texts: String): String? {
        for (text in texts) {
            denyPattern.find(text.lowercase())?.let { return it.value }
        }
        return null
    }

    fun violation(title: String, objective: String, labels: List<String>): String? =
        violation(title, objective, *labels.toTypedArray())

    fun isSafe(quest: Quest): Boolean = violation(quest.title, quest.objective, quest.targetLabels) == null

    companion object {
        val PEOPLE = listOf(
            "person", "people", "face", "selfie", "portrait", "stranger", "crowd", "child", "children",
            "kid", "baby", "babies", "man", "men", "woman", "women", "boy", "girl", "someone", "anyone",
            "tourist", "jogger", "runner", "passerby", "passersby", "neighbor", "neighbour",
        )
        val TRESPASS = listOf(
            "trespass", "trespassing", "private", "backyard", "yard", "climb", "climbing", "sneak",
            "break in", "rooftop", "abandoned", "construction site", "restricted", "no entry", "gated",
        )
        val TRAFFIC = listOf(
            "road", "street", "traffic", "highway", "freeway", "motorway", "crosswalk", "intersection",
            "car", "truck", "bus", "vehicle", "parking", "railway", "railroad", "train", "tracks",
        )
        val NIGHT_DANGER = listOf(
            "alley", "darkness", "unlit", "pitch black", "cliff", "ledge", "swim", "swimming",
            "tunnel", "underpass", "isolated", "alone",
        )
        val MONEY = listOf(
            "buy", "buying", "purchase", "shop", "shopping", "store", "paid", "payment", "money", "cash",
            "coin", "ticket", "receipt", "price", "spend", "cafe", "restaurant",
        )

        val DENIED_TERMS: List<String> = PEOPLE + TRESPASS + TRAFFIC + NIGHT_DANGER + MONEY

        private val denyPattern = Regex(
            DENIED_TERMS.joinToString(prefix = "\\b(?:", separator = "|", postfix = ")(?:s|es)?\\b") {
                Regex.escape(it)
            },
        )
    }
}
