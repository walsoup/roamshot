package app.polaroam.ai.vision

import android.graphics.Bitmap
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.Verdict
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

/** Verifies photos with the bundled ML Kit image labeling model (fully on-device). */
class MlKitPhotoVerifier(
    private val matcher: LabelMatcher,
) : PhotoVerifier, AutoCloseable {

    private val labeler: ImageLabeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(REPORTING_THRESHOLD)
                .build(),
        )
    }

    override suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labels = labeler.process(image).await()
        return matcher.match(
            detected = labels.map { DetectedLabel(it.text, it.confidence) },
            targetLabels = quest.targetLabels,
        )
    }

    override fun close() {
        labeler.close()
    }

    private companion object {
        /** Low on purpose so near misses can be reported; the matcher applies the real threshold. */
        const val REPORTING_THRESHOLD = 0.3f
    }
}
