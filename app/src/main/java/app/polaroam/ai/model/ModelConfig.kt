package app.polaroam.ai.model

data class ModelConfig(
    val url: String,
    val sha256: String,
    val fileName: String = DEFAULT_FILE_NAME,
) {
    val isConfigured: Boolean get() = url.startsWith("https://", ignoreCase = true)

    val expectedSha256: String? get() = sha256.trim().lowercase().takeIf { it.isNotEmpty() }

    companion object {
        const val DEFAULT_FILE_NAME = "quest-model.task"
    }
}
