package app.polaroam.ai.vision

import app.polaroam.domain.model.Verdict

/** One zero-shot label probability. Across a photo's labels the confidences add up to about 1. */
data class DetectedLabel(val text: String, val confidence: Float)

/**
 * Decides whether what the vision model saw satisfies a quest's target labels.
 * The targets pool their probability, which must reach [minTargetScore] and beat every other
 * single label, so a sandwich doesn't pass as a mushroom just because mushroom came second.
 */
class LabelMatcher(
    private val minTargetScore: Float = DEFAULT_MIN_TARGET_SCORE,
) {
    fun match(detected: List<DetectedLabel>, targetLabels: List<String>): Verdict {
        val targets = targetLabels.map(LabelVocabulary::canonical).toSet()
        val (onTarget, offTarget) = detected.partition { LabelVocabulary.canonical(it.text) in targets }
        val targetScore = onTarget.sumOf { it.confidence.toDouble() }.toFloat()
        val bestTarget = onTarget.maxByOrNull { it.confidence }
        val bestOther = offTarget.maxOfOrNull { it.confidence } ?: 0f

        if (bestTarget != null && targetScore >= minTargetScore && targetScore >= bestOther) {
            return Verdict(match = true, confidence = targetScore, label = LabelVocabulary.canonical(bestTarget.text))
        }

        val shown = detected.maxByOrNull { it.confidence }
        return Verdict(
            match = false,
            confidence = shown?.confidence ?: 0f,
            label = shown?.let { LabelVocabulary.canonical(it.text) } ?: NOTHING_RECOGNIZED,
        )
    }

    companion object {
        /** Calibrated for CLIP ViT-B/32 over the label set in assets/clip/label_embeddings.json. */
        const val DEFAULT_MIN_TARGET_SCORE = 0.2f
        const val NOTHING_RECOGNIZED = ""
    }
}
