package app.polaroam.ai.llm

import java.io.File

/** [jsonSchema], when set, asks engines that support constrained decoding to emit only matching JSON. */
data class LlmPrompt(
    val system: String,
    val user: String,
    val jsonSchema: String? = null,
)

/** Minimal text-completion contract so the generator can be tested without a real model. */
interface LlmEngine : AutoCloseable {
    suspend fun complete(prompt: LlmPrompt): String

    override fun close() = Unit
}

fun interface LlmEngineFactory {
    fun create(modelFile: File): LlmEngine
}
