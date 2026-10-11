package app.polaroam.ai.vision

import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlin.math.sqrt
import org.junit.Test

class ClipLabelEmbeddingsTest {

    private val asset by lazy {
        ClipLabelEmbeddings.load(File("src/main/assets/${ClipLabelEmbeddings.ASSET_PATH}").inputStream())
    }

    @Test
    fun `asset covers every vocabulary label`() {
        // Re-run tools/clip/build_label_embeddings.py after changing LabelVocabulary.
        assertThat(asset.labels).containsAtLeastElementsIn(LabelVocabulary.labels)
        assertThat(asset.labels).containsNoDuplicates()
        assertThat(asset.dimension).isEqualTo(512)
    }

    @Test
    fun `extra labels never count as a quest target`() {
        asset.labels.filterNot { it in LabelVocabulary.labels }.forEach { extra ->
            assertThat(LabelVocabulary.isKnown(extra)).isFalse()
        }
    }

    @Test
    fun `a label's own text embedding classifies as that label`() {
        val vectors = ClipLabelEmbeddings(
            labels = listOf("cat", "dog", "tree"),
            vectors = listOf(unit(1f, 0f, 0f), unit(0f, 1f, 0f), unit(0f, 0f, 1f)),
            logitScale = 100f,
        )

        val scores = vectors.classify(floatArrayOf(0.2f, 3f, 0.1f))

        assertThat(scores.maxBy { it.confidence }.text).isEqualTo("dog")
        assertThat(scores.sumOf { it.confidence.toDouble() }).isWithin(1e-5).of(1.0)
    }

    @Test
    fun `image embedding scale does not change the result`() {
        val vectors = ClipLabelEmbeddings(listOf("a", "b"), listOf(unit(1f, 1f), unit(1f, -1f)), logitScale = 10f)

        val small = vectors.classify(floatArrayOf(0.3f, 0.1f)).map { it.confidence }
        val large = vectors.classify(floatArrayOf(30f, 10f)).map { it.confidence }

        small.zip(large).forEach { (s, l) -> assertThat(s).isWithin(1e-6f).of(l) }
    }

    private fun unit(vararg values: Float): FloatArray {
        val norm = sqrt(values.fold(0f) { acc, x -> acc + x * x })
        return FloatArray(values.size) { values[it] / norm }
    }
}
