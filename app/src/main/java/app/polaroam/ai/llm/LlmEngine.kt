package app.polaroam.ai.llm

import java.io.File

data class LlmPrompt(
    val system: String,
    val user: String,
)

/** Minimal text-completion contract so the generator can be tested without a real model. */
interface LlmEngine : AutoCloseable {
    suspend fun complete(prompt: LlmPrompt): String

    override fun close() = Unit
}

fun interface LlmEngineFactory {
    fun create(modelFile: File): LlmEngine
}
