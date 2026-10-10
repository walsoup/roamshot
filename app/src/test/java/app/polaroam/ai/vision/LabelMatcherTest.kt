package app.polaroam.ai.vision

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LabelMatcherTest {

    private val matcher = LabelMatcher(threshold = 0.6f)

    @Test
    fun `matches a target label above the threshold`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("Bench", 0.71f), DetectedLabel("Dog", 0.88f)),
            targetLabels = listOf("dog", "cat"),
        )

        assertThat(verdict.match).isTrue()
        assertThat(verdict.label).isEqualTo("dog")
        assertThat(verdict.confidence).isEqualTo(0.88f)
    }

    @Test
    fun `plurals and synonyms count`() {
        assertThat(matcher.match(listOf(DetectedLabel("Dogs", 0.9f)), listOf("dog", "cat")).match).isTrue()
        assertThat(matcher.match(listOf(DetectedLabel("Puppy", 0.9f)), listOf("dog", "cat")).match).isTrue()
        assertThat(matcher.match(listOf(DetectedLabel("Flowers", 0.9f)), listOf("blossom", "petal")).match).isTrue()
    }

    @Test
    fun `target below the threshold is not a match`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("Cat", 0.45f), DetectedLabel("Wall", 0.8f)),
            targetLabels = listOf("dog", "cat"),
        )

        assertThat(verdict.match).isFalse()
        assertThat(verdict.label).isEqualTo("cat")
        assertThat(verdict.confidence).isEqualTo(0.45f)
    }

    @Test
    fun `without any target it reports the most confident label`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("Wall", 0.8f), DetectedLabel("Brick", 0.6f)),
            targetLabels = listOf("dog", "cat"),
        )

        assertThat(verdict.match).isFalse()
        assertThat(verdict.label).isEqualTo("wall")
    }

    @Test
    fun `nothing detected yields an empty label`() {
        val verdict = matcher.match(emptyList(), listOf("dog", "cat"))

        assertThat(verdict.match).isFalse()
        assertThat(verdict.label).isEqualTo(LabelMatcher.NOTHING_RECOGNIZED)
        assertThat(verdict.confidence).isEqualTo(0f)
    }

    @Test
    fun `picks the most confident of several targets`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("Cat", 0.65f), DetectedLabel("Dog", 0.95f)),
            targetLabels = listOf("dog", "cat"),
        )

        assertThat(verdict.label).isEqualTo("dog")
    }
}
