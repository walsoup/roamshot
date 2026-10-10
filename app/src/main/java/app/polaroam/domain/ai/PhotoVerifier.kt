package app.polaroam.domain.ai

import android.graphics.Bitmap
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.Verdict

interface PhotoVerifier {
    suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict
}
