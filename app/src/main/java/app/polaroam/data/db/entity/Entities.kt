package app.polaroam.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey val id: String,
    val walkType: String,
    val startedAtEpochMs: Long,
    val source: String,
)

@Entity(
    tableName = "quests",
    foreignKeys = [
        ForeignKey(
            entity = WalkEntity::class,
            parentColumns = ["id"],
            childColumns = ["walkId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("walkId")],
)
data class QuestEntity(
    @PrimaryKey val id: String,
    val walkId: String,
    val position: Int,
    val title: String,
    val objective: String,
    val category: String,
    val difficulty: Int,
    val targetLabels: List<String>,
    val status: String,
    val attempts: Int,
)

@Entity(
    tableName = "completions",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["questId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["questId"], unique = true), Index("localDay")],
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questId: String,
    val photoPath: String,
    val points: Int,
    val firstTry: Boolean,
    val label: String,
    val confidence: Float,
    val completedAtEpochMs: Long,
    /** ISO-8601 local date, e.g. 2026-10-10. */
    val localDay: String,
)

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val totalScore: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDay: String?,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}

/** Flat projection used by the gallery query. */
data class GalleryRow(
    val questId: String,
    val title: String,
    val objective: String,
    val category: String,
    val difficulty: Int,
    val targetLabels: List<String>,
    val walkType: String,
    val photoPath: String,
    val points: Int,
    val firstTry: Boolean,
    val label: String,
    val confidence: Float,
    val completedAtEpochMs: Long,
    val localDay: String,
)
