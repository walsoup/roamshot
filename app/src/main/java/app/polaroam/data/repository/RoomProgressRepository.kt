package app.polaroam.data.repository

import androidx.room.withTransaction
import app.polaroam.data.db.PolaroamDatabase
import app.polaroam.data.db.toDomain
import app.polaroam.data.db.toEntity
import app.polaroam.domain.model.Completion
import app.polaroam.domain.model.PlayerProgress
import app.polaroam.domain.model.ProgressState
import app.polaroam.domain.repository.ProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomProgressRepository @Inject constructor(
    private val db: PolaroamDatabase,
) : ProgressRepository {

    private val walkDao = db.walkDao()
    private val progressDao = db.progressDao()

    override fun observeProgress(): Flow<PlayerProgress> = combine(
        progressDao.observe(),
        progressDao.observeCompletedCount(),
    ) { progress, completed ->
        PlayerProgress(
            state = progress?.toDomain() ?: ProgressState.EMPTY,
            questsCompleted = completed,
        )
    }

    override suspend fun recordCompletion(
        completion: Completion,
        attempts: Int,
        update: (ProgressState) -> ProgressState,
    ): ProgressState = db.withTransaction {
        val updated = walkDao.markCompleted(completion.questId, attempts)
        check(updated == 1) { "Quest ${completion.questId} is not open" }
        progressDao.insertCompletion(completion.toEntity())
        val current = progressDao.get()?.toDomain() ?: ProgressState.EMPTY
        val next = update(current)
        progressDao.upsert(next.toEntity())
        next
    }
}
