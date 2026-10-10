package app.polaroam.domain.repository

import app.polaroam.domain.model.CompletedQuest
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.Walk
import app.polaroam.domain.model.WalkQuest
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface WalkRepository {
    /** Persists a new walk with its quests in order and returns the walk id. */
    suspend fun createWalk(walkType: String, source: QuestSource, quests: List<Quest>, startedAt: Instant): String

    fun observeWalk(walkId: String): Flow<Walk?>

    fun observeLatestWalk(): Flow<Walk?>

    suspend fun getWalkQuest(questId: String): WalkQuest?

    /** Returns false if the quest is no longer open. */
    suspend fun recordFailedAttempt(questId: String): Boolean

    /** Returns false if the quest is no longer open. */
    suspend fun skipQuest(questId: String): Boolean

    fun observeCompletedQuests(): Flow<List<CompletedQuest>>
}
