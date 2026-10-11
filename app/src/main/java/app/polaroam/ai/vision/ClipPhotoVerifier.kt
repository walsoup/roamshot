package app.polaroam.ai.vision

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.Verdict
import java.io.File
import java.nio.FloatBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Zero-shot photo checks with OpenAI's open CLIP ViT-B/32 image encoder, run by the open-source
 * ONNX Runtime. The photo's embedding is compared with precomputed text embeddings for every label.
 */
class ClipPhotoVerifier(
    modelFile: File,
    private val labels: ClipLabelEmbeddings,
    private val matcher: LabelMatcher,
) : PhotoVerifier, AutoCloseable {

    private val environment = OrtEnvironment.getEnvironment()
    private val session: OrtSession = environment.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
    private val inputName: String = session.inputNames.first()

    override suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict = withContext(Dispatchers.Default) {
        val embedding = embed(ClipImagePreprocessor.tensorData(bitmap))
        matcher.match(labels.classify(embedding), quest.targetLabels)
    }

    override fun close() {
        session.close()
    }

    private fun embed(pixels: FloatArray): FloatArray {
        val size = ClipImagePreprocessor.SIZE.toLong()
        return OnnxTensor.createTensor(environment, FloatBuffer.wrap(pixels), longArrayOf(1, 3, size, size)).use { input ->
            session.run(mapOf(inputName to input)).use { result ->
                val output = result.get(OUTPUT_NAME).orElse(result.get(0))
                @Suppress("UNCHECKED_CAST")
                (output.value as Array<FloatArray>)[0]
            }
        }
    }

    private companion object {
        const val OUTPUT_NAME = "image_embeds"
    }
}
