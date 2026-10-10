package app.polaroam.domain.model

/**
 * Result of checking a photo against a quest.
 * [label] is the matched target label on success, or the most confident thing seen otherwise.
 */
data class Verdict(
    val match: Boolean,
    val confidence: Float,
    val label: String,
)
