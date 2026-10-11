package app.polaroam.di

import android.content.Context
import androidx.work.WorkManager
import app.polaroam.BuildConfig
import app.polaroam.ai.model.ModelConfig
import app.polaroam.ai.model.ModelFiles
import app.polaroam.core.coroutines.DefaultDispatcher
import app.polaroam.core.coroutines.IoDispatcher
import app.polaroam.core.time.AppClock
import app.polaroam.core.time.SystemAppClock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    abstract fun bindClock(clock: SystemAppClock): AppClock
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideModelConfig(): ModelConfig = ModelConfig(
        quest = ModelConfig.QWEN3_0_6B.overriddenBy(BuildConfig.MODEL_URL, BuildConfig.MODEL_SHA256),
        smollm2 = ModelConfig.SMOLLM2_360M,
        vision = ModelConfig.CLIP_VIT_B32_VISION.overriddenBy(
            BuildConfig.VISION_MODEL_URL,
            BuildConfig.VISION_MODEL_SHA256,
        ),
    )

    @Provides
    @Singleton
    fun provideModelFiles(@ApplicationContext context: Context): ModelFiles =
        ModelFiles(File(context.filesDir, "models"))
}
