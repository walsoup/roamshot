package app.polaroam.ai.model

import java.io.File

/** Identifies one version of a model file, so engines can be rebuilt when the file changes. */
data class ModelStamp(val path: String, val size: Long, val modified: Long) {
    companion object {
        fun of(file: File?): ModelStamp? = file?.let { ModelStamp(it.absolutePath, it.length(), it.lastModified()) }
    }
}
