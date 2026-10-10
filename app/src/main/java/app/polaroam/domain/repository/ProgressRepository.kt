package app.polaroam.domain.repository

import app.polaroam.domain.model.Completion
import app.polaroam.domain.model.PlayerProgress
import app.polaroam.domain.model.ProgressState
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun observeProgress(): Flow<PlayerProgress>

    /**
     * Atomically marks the quest completed with [attempts], stores [completion] and replaces the
     * progress row with the result of [update]. Throws if the quest is not open anymore.
     */
    suspend fun recordCompletion(
        completion: Completion,
        attempts: Int,
        update: (ProgressState) -> ProgressState,
    ): ProgressState
}
