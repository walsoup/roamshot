package app.polaroam.ai.vision

import app.polaroam.domain.model.Verdict

data class DetectedLabel(val text: String, val confidence: Float)

/** Decides whether what the vision model saw satisfies a quest's target labels. */
class LabelMatcher(
    private val threshold: Float = DEFAULT_THRESHOLD,
) {
    fun match(detected: List<DetectedLabel>, targetLabels: List<String>): Verdict {
        val targets = targetLabels.map(LabelVocabulary::canonical).toSet()
        val best = detected
            .filter { LabelVocabulary.canonical(it.text) in targets }
            .maxByOrNull { it.confidence }

        if (best != null && best.confidence >= threshold) {
            return Verdict(match = true, confidence = best.confidence, label = LabelVocabulary.canonical(best.text))
        }

        val shown = best ?: detected.maxByOrNull { it.confidence }
        return Verdict(
            match = false,
            confidence = shown?.confidence ?: 0f,
            label = shown?.let { LabelVocabulary.canonical(it.text) } ?: NOTHING_RECOGNIZED,
        )
    }

    companion object {
        const val DEFAULT_THRESHOLD = 0.6f
        const val NOTHING_RECOGNIZED = ""
    }
}
