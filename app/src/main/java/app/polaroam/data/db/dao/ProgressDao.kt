package app.polaroam.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import app.polaroam.data.db.entity.CompletionEntity
import app.polaroam.data.db.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM progress WHERE id = 0")
    fun observe(): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE id = 0")
    suspend fun get(): ProgressEntity?

    @Upsert
    suspend fun upsert(progress: ProgressEntity)

    @Insert
    suspend fun insertCompletion(completion: CompletionEntity): Long

    @Query("SELECT COUNT(*) FROM completions")
    fun observeCompletedCount(): Flow<Int>
}
