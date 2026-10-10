package app.polaroam.data.photo

import android.content.Context
import android.graphics.Bitmap
import app.polaroam.core.coroutines.IoDispatcher
import app.polaroam.domain.repository.PhotoStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Captures go to the cache; verified photos are moved into app-private files storage. */
class FilePhotoStore @Inject constructor(
    @ApplicationContext context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PhotoStore {

    private val captureDir = File(context.cacheDir, "capture")
    private val photoDir = File(context.filesDir, "photos")

    override fun newCaptureFile(): File {
        if (!captureDir.isDirectory && !captureDir.mkdirs()) {
            throw IOException("Cannot create capture directory")
        }
        return File(captureDir, "capture-${UUID.randomUUID()}.jpg")
    }

    override suspend fun decodeForVerification(capture: File): Bitmap = withContext(io) {
        BitmapDecoder.decodeUpright(capture, VERIFY_MAX_DIMENSION)
    }

    override suspend fun persist(capture: File, questId: String): File = withContext(io) {
        if (!photoDir.isDirectory && !photoDir.mkdirs()) throw IOException("Cannot create photo directory")
        val target = File(photoDir, "${sanitize(questId)}.jpg")
        if (!capture.renameTo(target)) {
            capture.copyTo(target, overwrite = true)
            capture.delete()
        }
        target
    }

    override suspend fun discard(file: File) {
        withContext(io) { file.delete() }
    }

    private fun sanitize(name: String): String = name.replace(Regex("[^A-Za-z0-9_-]"), "_")

    private companion object {
        const val VERIFY_MAX_DIMENSION = 1024
    }
}
