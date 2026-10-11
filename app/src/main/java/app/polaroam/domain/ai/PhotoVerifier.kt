package app.polaroam.domain.ai

import android.graphics.Bitmap
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.Verdict

interface PhotoVerifier {
    /** @throws PhotoVerifierUnavailableException when the vision model is not on the device. */
    suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict
}

class PhotoVerifierUnavailableException(message: String) : IllegalStateException(message)
