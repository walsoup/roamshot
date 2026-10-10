package app.polaroam.di

import android.content.Context
import app.polaroam.BuildConfig
import app.polaroam.ai.OnDeviceQuestGenerator
import app.polaroam.ai.canned.CannedQuestGenerator
import app.polaroam.ai.llm.LlmEngineFactory
import app.polaroam.ai.llm.MediaPipeLlmEngine
import app.polaroam.ai.llm.QuestJsonParser
import app.polaroam.ai.llm.QuestPromptBuilder
import app.polaroam.ai.model.ModelFiles
import app.polaroam.ai.safety.QuestSafetyFilter
import app.polaroam.ai.vision.LabelMatcher
import app.polaroam.ai.vision.MlKitPhotoVerifier
import app.polaroam.ai.vision.StubPhotoVerifier
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.ai.QuestGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** The only place that decides which AI implementations the app runs with. */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideSafetyFilter(): QuestSafetyFilter = QuestSafetyFilter()

    @Provides
    @Singleton
    fun provideCannedGenerator(): CannedQuestGenerator = CannedQuestGenerator()

    @Provides
    @Singleton
    fun providePromptBuilder(): QuestPromptBuilder = QuestPromptBuilder()

    @Provides
    @Singleton
    fun provideParser(safety: QuestSafetyFilter): QuestJsonParser = QuestJsonParser(safety)

    @Provides
    @Singleton
    fun provideLabelMatcher(): LabelMatcher = LabelMatcher()

    @Provides
    @Singleton
    fun provideEngineFactory(@ApplicationContext context: Context): LlmEngineFactory =
        LlmEngineFactory { modelFile -> MediaPipeLlmEngine(context, modelFile) }

    @Provides
    @Singleton
    fun provideQuestGenerator(
        canned: CannedQuestGenerator,
        modelFiles: ModelFiles,
        engineFactory: LlmEngineFactory,
        promptBuilder: QuestPromptBuilder,
        parser: QuestJsonParser,
    ): QuestGenerator =
        if (BuildConfig.USE_STUB_AI) {
            canned
        } else {
            OnDeviceQuestGenerator(modelFiles, engineFactory, canned, promptBuilder, parser)
        }

    @Provides
    @Singleton
    fun providePhotoVerifier(matcher: LabelMatcher): PhotoVerifier =
        if (BuildConfig.USE_STUB_AI) {
            StubPhotoVerifier.alwaysMatch(delayMillis = STUB_VERIFY_DELAY_MS)
        } else {
            MlKitPhotoVerifier(matcher)
        }

    private const val STUB_VERIFY_DELAY_MS = 800L
}
