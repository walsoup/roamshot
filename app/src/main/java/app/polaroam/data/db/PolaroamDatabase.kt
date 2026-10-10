package app.polaroam.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.polaroam.data.db.dao.ProgressDao
import app.polaroam.data.db.dao.WalkDao
import app.polaroam.data.db.entity.CompletionEntity
import app.polaroam.data.db.entity.ProgressEntity
import app.polaroam.data.db.entity.QuestEntity
import app.polaroam.data.db.entity.WalkEntity

@Database(
    entities = [WalkEntity::class, QuestEntity::class, CompletionEntity::class, ProgressEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PolaroamDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao
    abstract fun progressDao(): ProgressDao

    companion object {
        const val NAME = "polaroam.db"
    }
}
