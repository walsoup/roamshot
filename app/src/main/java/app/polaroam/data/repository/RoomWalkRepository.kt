package app.polaroam.data.repository

import app.polaroam.data.db.dao.WalkDao
import app.polaroam.data.db.entity.WalkEntity
import app.polaroam.data.db.toDomain
import app.polaroam.data.db.toEntity
import app.polaroam.data.db.toWalkQuest
import app.polaroam.domain.model.CompletedQuest
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.Walk
import app.polaroam.domain.model.WalkQuest
import app.polaroam.domain.repository.WalkRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class RoomWalkRepository @Inject constructor(
    private val walkDao: WalkDao,
) : WalkRepository {

    override suspend fun createWalk(
        walkType: String,
        source: QuestSource,
        quests: List<Quest>,
        startedAt: Instant,
    ): String {
        require(quests.isNotEmpty()) { "A walk needs at least one quest" }
        val walkId = UUID.randomUUID().toString()
        walkDao.insertWalkWithQuests(
            walk = WalkEntity(
                id = walkId,
                walkType = walkType,
                startedAtEpochMs = startedAt.toEpochMilli(),
                source = source.name,
            ),
            quests = quests.mapIndexed { index, quest -> quest.toEntity(walkId, index) },
        )
        return walkId
    }

    override fun observeWalk(walkId: String): Flow<Walk?> = combine(
        walkDao.observeWalk(walkId),
        walkDao.observeQuests(walkId),
        walkDao.observeCompletionsForWalk(walkId),
    ) { walk, quests, completions ->
        walk?.let {
            val byQuest = completions.associateBy { c -> c.questId }
            it.toDomain(quests.map { q -> q.toWalkQuest(byQuest[q.id]) })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeLatestWalk(): Flow<Walk?> =
        walkDao.observeLatestWalk().flatMapLatest { latest ->
            if (latest == null) flowOf(null) else observeWalk(latest.id)
        }

    override suspend fun getWalkQuest(questId: String): WalkQuest? {
        val quest = walkDao.getQuest(questId) ?: return null
        return quest.toWalkQuest(walkDao.getCompletion(questId))
    }

    override suspend fun recordFailedAttempt(questId: String): Boolean = walkDao.incrementAttempts(questId) > 0

    override suspend fun skipQuest(questId: String): Boolean = walkDao.skipQuest(questId) > 0

    override suspend fun abandonWalk(walkId: String): Boolean = walkDao.skipOpenQuests(walkId) > 0

    override fun observeCompletedQuests(): Flow<List<CompletedQuest>> =
        walkDao.observeGallery().map { rows -> rows.map { it.toDomain() } }
}
