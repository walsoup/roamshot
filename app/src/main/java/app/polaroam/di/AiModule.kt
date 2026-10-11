package app.polaroam.di

import android.content.Context
import app.polaroam.BuildConfig
import app.polaroam.ai.OnDeviceQuestGenerator
import app.polaroam.ai.canned.CannedQuestGenerator
import app.polaroam.ai.llm.LiteRtLmEngine
import app.polaroam.ai.llm.LlmEngineFactory
import app.polaroam.ai.llm.QuestJsonParser
import app.polaroam.ai.llm.QuestPromptBuilder
import app.polaroam.ai.model.ModelConfig
import app.polaroam.ai.model.ModelFiles
import app.polaroam.ai.safety.QuestSafetyFilter
import app.polaroam.ai.vision.ClipLabelEmbeddings
import app.polaroam.ai.vision.ClipPhotoVerifier
import app.polaroam.ai.vision.LabelMatcher
import app.polaroam.ai.vision.OnDevicePhotoVerifier
import app.polaroam.ai.vision.PhotoVerifierFactory
import app.polaroam.ai.vision.StubPhotoVerifier
import app.polaroam.core.coroutines.IoDispatcher
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.ai.QuestGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

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
        LlmEngineFactory { modelFile -> LiteRtLmEngine(modelFile, context.cacheDir) }

    @Provides
    @Singleton
    fun provideQuestGenerator(
        canned: CannedQuestGenerator,
        modelFiles: ModelFiles,
        config: ModelConfig,
        preferences: app.polaroam.data.repository.ModelPreferencesRepository,
        engineFactory: LlmEngineFactory,
        promptBuilder: QuestPromptBuilder,
        parser: QuestJsonParser,
    ): QuestGenerator =
        if (BuildConfig.USE_STUB_AI) {
            canned
        } else {
            OnDeviceQuestGenerator(
                modelFiles = modelFiles,
                activeQuestModel = { config.specFor(preferences.getSelectedModel()) },
                engineFactory = engineFactory,
                fallback = canned,
                promptBuilder = promptBuilder,
                parser = parser,
            )
        }

    @Provides
    @Singleton
    fun providePhotoVerifier(
        @ApplicationContext context: Context,
        modelFiles: ModelFiles,
        config: ModelConfig,
        matcher: LabelMatcher,
        @IoDispatcher io: CoroutineDispatcher,
    ): PhotoVerifier =
        if (BuildConfig.USE_STUB_AI) {
            StubPhotoVerifier.alwaysMatch(delayMillis = STUB_VERIFY_DELAY_MS)
        } else {
            val factory = PhotoVerifierFactory { modelFile ->
                val labels = ClipLabelEmbeddings.load(context.assets.open(ClipLabelEmbeddings.ASSET_PATH))
                ClipPhotoVerifier(modelFile, labels, matcher)
            }
            OnDevicePhotoVerifier(modelFiles, config.vision, factory, io)
        }

    private const val STUB_VERIFY_DELAY_MS = 800L
}
