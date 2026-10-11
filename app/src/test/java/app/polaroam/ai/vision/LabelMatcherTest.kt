package app.polaroam.ai.vision

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LabelMatcherTest {

    private val matcher = LabelMatcher(minTargetScore = 0.2f)

    @Test
    fun `matches when the target is the most likely label`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("cat", 0.55f), DetectedLabel("pet", 0.09f), DetectedLabel("chair", 0.03f)),
            targetLabels = listOf("cat"),
        )

        assertThat(verdict.match).isTrue()
        assertThat(verdict.label).isEqualTo("cat")
        assertThat(verdict.confidence).isEqualTo(0.55f)
    }

    @Test
    fun `alternative targets pool their probability`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("water", 0.14f), DetectedLabel("lake", 0.12f), DetectedLabel("sky", 0.2f)),
            targetLabels = listOf("water", "lake", "river"),
        )

        assertThat(verdict.match).isTrue()
        assertThat(verdict.label).isEqualTo("water")
        assertThat(verdict.confidence).isWithin(1e-6f).of(0.26f)
    }

    @Test
    fun `plurals and synonyms count`() {
        assertThat(matcher.match(listOf(DetectedLabel("Dogs", 0.9f)), listOf("dog", "cat")).match).isTrue()
        assertThat(matcher.match(listOf(DetectedLabel("Puppy", 0.9f)), listOf("dog", "cat")).match).isTrue()
        assertThat(matcher.match(listOf(DetectedLabel("Flowers", 0.9f)), listOf("blossom", "petal")).match).isTrue()
    }

    @Test
    fun `a runner-up target loses to a more likely other label`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("food", 0.53f), DetectedLabel("mushroom", 0.26f)),
            targetLabels = listOf("mushroom"),
        )

        assertThat(verdict.match).isFalse()
        assertThat(verdict.label).isEqualTo("food")
        assertThat(verdict.confidence).isEqualTo(0.53f)
    }

    @Test
    fun `a weak top target is not enough`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("bird", 0.18f), DetectedLabel("bicycle", 0.11f)),
            targetLabels = listOf("bird"),
        )

        assertThat(verdict.match).isFalse()
        assertThat(verdict.label).isEqualTo("bird")
    }

    @Test
    fun `without any target it reports the most likely label`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("Wall", 0.8f), DetectedLabel("Brick", 0.1f)),
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
    fun `names the most likely of several matched targets`() {
        val verdict = matcher.match(
            detected = listOf(DetectedLabel("Cat", 0.2f), DetectedLabel("Dog", 0.6f)),
            targetLabels = listOf("dog", "cat"),
        )

        assertThat(verdict.label).isEqualTo("dog")
        assertThat(verdict.confidence).isWithin(1e-6f).of(0.8f)
    }
}
