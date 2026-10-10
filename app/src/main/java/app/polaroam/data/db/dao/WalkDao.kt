package app.polaroam.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import app.polaroam.data.db.entity.CompletionEntity
import app.polaroam.data.db.entity.GalleryRow
import app.polaroam.data.db.entity.QuestEntity
import app.polaroam.data.db.entity.WalkEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WalkDao {

    @Insert
    abstract suspend fun insertWalk(walk: WalkEntity)

    @Insert
    abstract suspend fun insertQuests(quests: List<QuestEntity>)

    @Transaction
    open suspend fun insertWalkWithQuests(walk: WalkEntity, quests: List<QuestEntity>) {
        insertWalk(walk)
        insertQuests(quests)
    }

    @Query("SELECT * FROM walks WHERE id = :walkId")
    abstract fun observeWalk(walkId: String): Flow<WalkEntity?>

    @Query("SELECT * FROM walks ORDER BY startedAtEpochMs DESC LIMIT 1")
    abstract fun observeLatestWalk(): Flow<WalkEntity?>

    @Query("SELECT * FROM quests WHERE walkId = :walkId ORDER BY position")
    abstract fun observeQuests(walkId: String): Flow<List<QuestEntity>>

    @Query(
        """
        SELECT completions.* FROM completions
        INNER JOIN quests ON quests.id = completions.questId
        WHERE quests.walkId = :walkId
        """,
    )
    abstract fun observeCompletionsForWalk(walkId: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM quests WHERE id = :questId")
    abstract suspend fun getQuest(questId: String): QuestEntity?

    @Query("SELECT * FROM completions WHERE questId = :questId")
    abstract suspend fun getCompletion(questId: String): CompletionEntity?

    @Query("UPDATE quests SET attempts = attempts + 1 WHERE id = :questId AND status = 'OPEN'")
    abstract suspend fun incrementAttempts(questId: String): Int

    @Query("UPDATE quests SET status = 'SKIPPED' WHERE id = :questId AND status = 'OPEN'")
    abstract suspend fun skipQuest(questId: String): Int

    @Query("UPDATE quests SET status = 'COMPLETED', attempts = :attempts WHERE id = :questId AND status = 'OPEN'")
    abstract suspend fun markCompleted(questId: String, attempts: Int): Int

    @Query(
        """
        SELECT
            q.id AS questId,
            q.title AS title,
            q.objective AS objective,
            q.category AS category,
            q.difficulty AS difficulty,
            q.targetLabels AS targetLabels,
            w.walkType AS walkType,
            c.photoPath AS photoPath,
            c.points AS points,
            c.firstTry AS firstTry,
            c.label AS label,
            c.confidence AS confidence,
            c.completedAtEpochMs AS completedAtEpochMs,
            c.localDay AS localDay
        FROM completions c
        INNER JOIN quests q ON q.id = c.questId
        INNER JOIN walks w ON w.id = q.walkId
        ORDER BY c.completedAtEpochMs DESC
        """,
    )
    abstract fun observeGallery(): Flow<List<GalleryRow>>
}
