package app.polaroam.domain.ai

import app.polaroam.domain.model.GenerationResult
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestSource

interface QuestGenerator {
    suspend fun generate(walkType: String, count: Int): List<Quest>

    /** Same as [generate] but also reports where the quests came from. */
    suspend fun generateDetailed(walkType: String, count: Int): GenerationResult =
        GenerationResult(generate(walkType, count), QuestSource.BUILT_IN)
}
