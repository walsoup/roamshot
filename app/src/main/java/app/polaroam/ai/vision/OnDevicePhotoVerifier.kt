package app.polaroam.ai.vision

import android.graphics.Bitmap
import app.polaroam.ai.model.ModelFiles
import app.polaroam.ai.model.ModelSpec
import app.polaroam.ai.model.ModelStamp
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.ai.PhotoVerifierUnavailableException
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.Verdict
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

fun interface PhotoVerifierFactory {
    fun create(modelFile: File): PhotoVerifier
}

/**
 * Checks photos with the downloaded vision model. The verifier is created lazily and rebuilt
 * whenever the model file changes or disappears; without a model every check throws
 * [PhotoVerifierUnavailableException].
 */
class OnDevicePhotoVerifier(
    private val modelFiles: ModelFiles,
    private val visionModel: ModelSpec,
    private val factory: PhotoVerifierFactory,
    private val io: CoroutineDispatcher,
) : PhotoVerifier {

    private val mutex = Mutex()
    private var verifier: PhotoVerifier? = null
    private var verifierStamp: ModelStamp? = null

    override suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict = mutex.withLock {
        val current = currentVerifier()
            ?: throw PhotoVerifierUnavailableException("The vision model has not been downloaded")
        current.verify(bitmap, quest)
    }

    private suspend fun currentVerifier(): PhotoVerifier? {
        val model = withContext(io) { modelFiles.readyModel(visionModel) }
        val stamp = ModelStamp.of(model)
        if (stamp != verifierStamp) {
            (verifier as? AutoCloseable)?.close()
            verifier = null
            verifierStamp = null
            verifier = model?.let { withContext(io) { factory.create(it) } }
            verifierStamp = stamp
        }
        return verifier
    }
}
