package app.polaroam.ai.model

import java.io.File
import java.io.IOException

/**
 * On-disk layout for the downloaded model. Downloads land in a `.part` file and are only
 * promoted to the real name once complete and verified, so a half-written model is never loaded.
 */
class ModelFiles(
    private val directory: File,
    private val fileName: String,
) {
    val modelFile: File get() = File(directory, fileName)
    val partFile: File get() = File(directory, "$fileName.part")

    fun readyModel(): File? = modelFile.takeIf { it.isFile && it.length() > 0 }

    @Throws(IOException::class)
    fun preparePartFile(): File {
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Cannot create model directory ${directory.absolutePath}")
        }
        return partFile
    }

    @Throws(IOException::class)
    fun promotePart(): File {
        val part = partFile
        val target = modelFile
        if (!part.isFile || part.length() == 0L) throw IOException("No downloaded model to promote")
        if (target.exists() && !target.delete()) throw IOException("Cannot replace existing model")
        if (!part.renameTo(target)) throw IOException("Cannot move downloaded model into place")
        return target
    }

    fun discardPart() {
        partFile.delete()
    }

    /** Returns true when no model files remain. */
    fun deleteAll(): Boolean {
        partFile.delete()
        return !modelFile.exists() || modelFile.delete()
    }
}
