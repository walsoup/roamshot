package app.polaroam.di

import android.content.Context
import androidx.room.Room
import app.polaroam.ai.model.WorkManagerModelManager
import app.polaroam.data.db.PolaroamDatabase
import app.polaroam.data.db.dao.ProgressDao
import app.polaroam.data.db.dao.WalkDao
import app.polaroam.data.photo.FilePhotoStore
import app.polaroam.data.repository.RoomProgressRepository
import app.polaroam.data.repository.RoomWalkRepository
import app.polaroam.domain.repository.ModelManager
import app.polaroam.domain.repository.PhotoStore
import app.polaroam.domain.repository.ProgressRepository
import app.polaroam.domain.repository.WalkRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PolaroamDatabase =
        Room.databaseBuilder(context, PolaroamDatabase::class.java, PolaroamDatabase.NAME).build()

    @Provides
    fun provideWalkDao(db: PolaroamDatabase): WalkDao = db.walkDao()

    @Provides
    fun provideProgressDao(db: PolaroamDatabase): ProgressDao = db.progressDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWalkRepository(impl: RoomWalkRepository): WalkRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: RoomProgressRepository): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindPhotoStore(impl: FilePhotoStore): PhotoStore

    @Binds
    abstract fun bindModelManager(impl: WorkManagerModelManager): ModelManager
}
