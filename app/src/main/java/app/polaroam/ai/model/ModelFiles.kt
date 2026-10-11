package app.polaroam.ai.model

import java.io.File
import java.io.IOException

/**
 * On-disk layout for the downloaded models. Downloads land in a `.part` file and are only
 * promoted to the real name once complete and verified, so a half-written model is never loaded.
 */
class ModelFiles(private val directory: File) {

    fun modelFile(spec: ModelSpec): File = File(directory, spec.fileName)
    fun partFile(spec: ModelSpec): File = File(directory, "${spec.fileName}.part")

    fun readyModel(spec: ModelSpec): File? = modelFile(spec).takeIf { it.isFile && it.length() > 0 }

    fun allReady(specs: List<ModelSpec>): Boolean = specs.all { readyModel(it) != null }

    @Throws(IOException::class)
    fun preparePartFile(spec: ModelSpec): File {
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Cannot create model directory ${directory.absolutePath}")
        }
        return partFile(spec)
    }

    @Throws(IOException::class)
    fun promotePart(spec: ModelSpec): File {
        val part = partFile(spec)
        val target = modelFile(spec)
        if (!part.isFile || part.length() == 0L) throw IOException("No downloaded model to promote")
        if (target.exists() && !target.delete()) throw IOException("Cannot replace existing model")
        if (!part.renameTo(target)) throw IOException("Cannot move downloaded model into place")
        return target
    }

    fun discardPart(spec: ModelSpec) {
        partFile(spec).delete()
    }

    fun deleteModel(spec: ModelSpec): Boolean {
        discardPart(spec)
        val file = modelFile(spec)
        return !file.exists() || file.delete()
    }

    /** Deletes every file in the model directory, including models from older app versions. Returns true when none remain. */
    fun deleteAll(): Boolean {
        directory.listFiles()?.forEach { it.delete() }
        return directory.listFiles().isNullOrEmpty()
    }
}
