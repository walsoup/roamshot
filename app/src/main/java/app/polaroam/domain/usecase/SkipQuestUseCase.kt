package app.polaroam.domain.usecase

import app.polaroam.domain.repository.WalkRepository
import javax.inject.Inject

class SkipQuestUseCase @Inject constructor(
    private val walks: WalkRepository,
) {
    /** Returns false when the quest was already resolved. */
    suspend operator fun invoke(questId: String): Boolean = walks.skipQuest(questId)
}
