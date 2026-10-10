package app.polaroam.domain.repository

import android.graphics.Bitmap
import java.io.File

interface PhotoStore {
    /** A fresh temporary file for the camera to write into. */
    fun newCaptureFile(): File

    /** Decodes a downsampled, upright bitmap suitable for the vision model. */
    suspend fun decodeForVerification(capture: File): Bitmap

    /** Moves a verified capture into permanent storage and returns the new file. */
    suspend fun persist(capture: File, questId: String): File

    suspend fun discard(file: File)
}
