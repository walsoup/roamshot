package app.polaroam.ui.capture

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import app.polaroam.R
import app.polaroam.ai.vision.StubPhotoVerifier
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.ai.PhotoVerifierUnavailableException
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.QuestStatus
import app.polaroam.domain.model.Verdict
import app.polaroam.domain.scoring.ScoreCalculator
import app.polaroam.domain.streak.StreakCalculator
import app.polaroam.domain.usecase.SkipQuestUseCase
import app.polaroam.domain.usecase.SubmitPhotoUseCase
import app.polaroam.testutil.FakePhotoStore
import app.polaroam.testutil.FakeProgressRepository
import app.polaroam.testutil.FakeWalkRepository
import app.polaroam.testutil.FixedClock
import app.polaroam.testutil.MainDispatcherRule
import app.polaroam.testutil.testQuest
import app.polaroam.ui.navigation.Route
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var walks: FakeWalkRepository
    private lateinit var progress: FakeProgressRepository
    private lateinit var photos: FakePhotoStore
    private val clock = FixedClock()

    @Before
    fun setUp() {
        walks = FakeWalkRepository()
        progress = FakeProgressRepository(walks)
        photos = FakePhotoStore(tmp.root)
    }

    private suspend fun seedQuest(id: String = "q1") {
        walks.createWalk("City park", QuestSource.BUILT_IN, listOf(testQuest(id, Difficulty.EASY)), clock.now())
    }

    private fun viewModel(verifier: PhotoVerifier, questId: String = "q1") = CaptureViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Route.Capture.ARG_QUEST_ID to questId)),
        walks = walks,
        photoStore = photos,
        submitPhoto = SubmitPhotoUseCase(
            verifier = verifier,
            walks = walks,
            progress = progress,
            photos = photos,
            scoring = ScoreCalculator(),
            streaks = StreakCalculator(),
            clock = clock,
        ),
        skipQuest = SkipQuestUseCase(walks),
    )

    @Test
    fun `loads the quest and becomes ready`() = runTest {
        seedQuest()

        val vm = viewModel(StubPhotoVerifier.alwaysMatch())

        val state = vm.uiState.value
        assertThat(state.phase).isEqualTo(CapturePhase.Ready)
        assertThat(state.quest?.id).isEqualTo("q1")
        assertThat(state.canShoot).isTrue()
    }

    @Test
    fun `successful capture ends in a match with points`() = runTest {
        seedQuest()
        val vm = viewModel(StubPhotoVerifier.alwaysMatch())

        val file = vm.beginCapture()!!
        assertThat(vm.uiState.value.phase).isEqualTo(CapturePhase.Capturing)
        vm.onPhotoCaptured(file)

        val phase = vm.uiState.value.phase
        assertThat(phase).isInstanceOf(CapturePhase.Match::class.java)
        phase as CapturePhase.Match
        assertThat(phase.points).isEqualTo(15)
        assertThat(phase.firstTry).isTrue()
        assertThat(phase.streak).isEqualTo(1)
    }

    @Test
    fun `no match allows a retry`() = runTest {
        seedQuest()
        val vm = viewModel(StubPhotoVerifier(listOf(false, true)))

        vm.onPhotoCaptured(vm.beginCapture()!!)
        assertThat(vm.uiState.value.phase).isInstanceOf(CapturePhase.NoMatch::class.java)
        assertThat(vm.uiState.value.attempts).isEqualTo(1)

        vm.onRetry()
        assertThat(vm.uiState.value.phase).isEqualTo(CapturePhase.Ready)

        vm.onPhotoCaptured(vm.beginCapture()!!)
        val phase = vm.uiState.value.phase as CapturePhase.Match
        assertThat(phase.firstTry).isFalse()
        assertThat(phase.points).isEqualTo(10)
    }

    @Test
    fun `shutter is ignored while scanning`() = runTest(mainDispatcher.dispatcher) {
        seedQuest()
        val vm = viewModel(StubPhotoVerifier.alwaysMatch(delayMillis = 1_000))

        vm.onPhotoCaptured(vm.beginCapture()!!)
        assertThat(vm.uiState.value.phase).isEqualTo(CapturePhase.Scanning)
        assertThat(vm.beginCapture()).isNull()

        advanceUntilIdle()
        assertThat(vm.uiState.value.phase).isInstanceOf(CapturePhase.Match::class.java)
    }

    @Test
    fun `skip marks the quest skipped and finishes`() = runTest {
        seedQuest()
        val vm = viewModel(StubPhotoVerifier.neverMatch())

        vm.onSkip()

        assertThat(vm.events.first()).isEqualTo(CaptureEvent.Finished)
        assertThat(walks.findQuest("q1")?.status).isEqualTo(QuestStatus.SKIPPED)
    }

    @Test
    fun `missing quest shows a non-retryable error`() = runTest {
        val vm = viewModel(StubPhotoVerifier.alwaysMatch(), questId = "nope")

        assertThat(vm.uiState.value.phase)
            .isEqualTo(CapturePhase.Error(R.string.capture_error_quest_missing, canRetry = false))
        assertThat(vm.beginCapture()).isNull()
    }

    @Test
    fun `already resolved quest cannot be captured`() = runTest {
        seedQuest()
        walks.skipQuest("q1")

        val vm = viewModel(StubPhotoVerifier.alwaysMatch())

        assertThat(vm.uiState.value.phase).isEqualTo(CapturePhase.AlreadyResolved)
    }

    @Test
    fun `camera failure discards the file and offers a retry`() = runTest {
        seedQuest()
        val vm = viewModel(StubPhotoVerifier.alwaysMatch())
        val file = vm.beginCapture()!!

        vm.onCaptureFailed(file)

        assertThat(vm.uiState.value.phase).isEqualTo(CapturePhase.Error(R.string.capture_error_camera, canRetry = true))
        assertThat(photos.discarded).containsExactly(file)
        vm.onRetry()
        assertThat(vm.uiState.value.phase).isEqualTo(CapturePhase.Ready)
    }

    @Test
    fun `missing vision model points at the model download`() = runTest {
        seedQuest()
        val vm = viewModel(
            object : PhotoVerifier {
                override suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict =
                    throw PhotoVerifierUnavailableException("no model")
            },
        )

        vm.onPhotoCaptured(vm.beginCapture()!!)

        assertThat(vm.uiState.value.phase).isEqualTo(
            CapturePhase.Error(
                R.string.capture_error_models_missing,
                canRetry = true,
                illustration = R.drawable.illo_model_missing,
            ),
        )
    }
}
