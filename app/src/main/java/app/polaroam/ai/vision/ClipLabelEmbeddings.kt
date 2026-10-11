package app.polaroam.ai.vision

import java.io.InputStream
import kotlin.math.exp
import kotlin.math.sqrt
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class LabelEmbeddingsFile(
    val model: String,
    val logitScale: Float,
    val labels: List<LabelEmbeddingDto>,
)

@Serializable
private data class LabelEmbeddingDto(val label: String, val embedding: List<Float>)

/**
 * CLIP text embeddings for every label the verifier can name, precomputed by
 * `tools/clip/build_label_embeddings.py`. Turns an image embedding into zero-shot label
 * probabilities the same way CLIP does: scaled cosine similarity, then softmax.
 */
class ClipLabelEmbeddings(
    val labels: List<String>,
    private val vectors: List<FloatArray>,
    private val logitScale: Float,
) {
    val dimension: Int = vectors.firstOrNull()?.size ?: 0

    init {
        require(labels.isNotEmpty() && labels.size == vectors.size) { "Need one embedding per label" }
        require(vectors.all { it.size == dimension }) { "Embeddings must share one dimension" }
    }

    fun classify(imageEmbedding: FloatArray): List<DetectedLabel> {
        require(imageEmbedding.size == dimension) {
            "Image embedding has ${imageEmbedding.size} values, expected $dimension"
        }
        val norm = sqrt(imageEmbedding.fold(0f) { acc, x -> acc + x * x })
        require(norm > 0f) { "Image embedding is all zeros" }

        val logits = vectors.map { vector ->
            var dot = 0f
            for (i in vector.indices) dot += vector[i] * imageEmbedding[i]
            logitScale * dot / norm
        }
        val max = logits.max()
        val weights = logits.map { exp(it - max) }
        val total = weights.sum()
        return labels.mapIndexed { i, label -> DetectedLabel(label, weights[i] / total) }
    }

    companion object {
        const val ASSET_PATH = "clip/label_embeddings.json"

        private val json = Json { ignoreUnknownKeys = true }

        fun parse(text: String): ClipLabelEmbeddings {
            val file = json.decodeFromString(LabelEmbeddingsFile.serializer(), text)
            return ClipLabelEmbeddings(
                labels = file.labels.map { it.label },
                vectors = file.labels.map { it.embedding.toFloatArray() },
                logitScale = file.logitScale,
            )
        }

        fun load(input: InputStream): ClipLabelEmbeddings = input.bufferedReader().use { parse(it.readText()) }
    }
}
