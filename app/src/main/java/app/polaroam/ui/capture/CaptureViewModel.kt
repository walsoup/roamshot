package app.polaroam.ui.capture

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.polaroam.R
import app.polaroam.core.result.runCatchingCancellable
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestStatus
import app.polaroam.domain.repository.PhotoStore
import app.polaroam.domain.repository.WalkRepository
import app.polaroam.domain.usecase.SkipQuestUseCase
import app.polaroam.domain.usecase.SubmitPhotoUseCase
import app.polaroam.domain.usecase.SubmitResult
import app.polaroam.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CapturePhase {
    data object Loading : CapturePhase
    data object Ready : CapturePhase
    data object Capturing : CapturePhase
    data object Scanning : CapturePhase
    data class Match(
        val points: Int,
        val firstTry: Boolean,
        val label: String,
        val confidence: Float,
        val photoPath: String,
        val streak: Int,
    ) : CapturePhase
    data class NoMatch(val label: String, val confidence: Float) : CapturePhase
    data object AlreadyResolved : CapturePhase
    data class Error(@StringRes val messageRes: Int, val canRetry: Boolean) : CapturePhase
}

data class CaptureUiState(
    val quest: Quest? = null,
    val attempts: Int = 0,
    val phase: CapturePhase = CapturePhase.Loading,
) {
    val nextAttempt: Int get() = attempts + 1
    val canShoot: Boolean get() = quest != null && phase == CapturePhase.Ready
}

sealed interface CaptureEvent {
    data object Finished : CaptureEvent
}

@HiltViewModel
class CaptureViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val walks: WalkRepository,
    private val photoStore: PhotoStore,
    private val submitPhoto: SubmitPhotoUseCase,
    private val skipQuest: SkipQuestUseCase,
) : ViewModel() {

    private val questId: String = checkNotNull(savedStateHandle[Route.Capture.ARG_QUEST_ID]) {
        "Capture opened without a quest id"
    }

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private val _events = Channel<CaptureEvent>(Channel.BUFFERED)
    val events: Flow<CaptureEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        _uiState.update { it.copy(phase = CapturePhase.Loading) }
        viewModelScope.launch {
            val walkQuest = runCatchingCancellable { walks.getWalkQuest(questId) }.getOrElse {
                setPhase(CapturePhase.Error(R.string.capture_error_load, canRetry = true))
                return@launch
            }
            _uiState.update { state ->
                when {
                    walkQuest == null -> state.copy(
                        phase = CapturePhase.Error(R.string.capture_error_quest_missing, canRetry = false),
                    )
                    walkQuest.status != QuestStatus.OPEN -> state.copy(
                        quest = walkQuest.quest,
                        attempts = walkQuest.attempts,
                        phase = CapturePhase.AlreadyResolved,
                    )
                    else -> state.copy(
                        quest = walkQuest.quest,
                        attempts = walkQuest.attempts,
                        phase = CapturePhase.Ready,
                    )
                }
            }
        }
    }

    /**
     * Claims the shutter. Returns a file for the camera to write into, or null if a capture
     * can't start right now (e.g. double tap while scanning).
     */
    fun beginCapture(): File? {
        if (!_uiState.value.canShoot) return null
        return runCatchingCancellable { photoStore.newCaptureFile() }
            .onSuccess { setPhase(CapturePhase.Capturing) }
            .onFailure { setPhase(CapturePhase.Error(R.string.capture_error_camera, canRetry = true)) }
            .getOrNull()
    }

    fun onPhotoCaptured(file: File) {
        setPhase(CapturePhase.Scanning)
        viewModelScope.launch {
            when (val result = submitPhoto(questId, file)) {
                is SubmitResult.Match -> _uiState.update {
                    it.copy(
                        attempts = it.attempts + 1,
                        phase = CapturePhase.Match(
                            points = result.points,
                            firstTry = result.firstTry,
                            label = result.verdict.label,
                            confidence = result.verdict.confidence,
                            photoPath = result.photoPath,
                            streak = result.streak,
                        ),
                    )
                }
                is SubmitResult.NoMatch -> _uiState.update {
                    it.copy(
                        attempts = result.attempt,
                        phase = CapturePhase.NoMatch(result.verdict.label, result.verdict.confidence),
                    )
                }
                SubmitResult.QuestUnavailable -> setPhase(CapturePhase.AlreadyResolved)
                is SubmitResult.Failed -> setPhase(CapturePhase.Error(R.string.capture_error_verify, canRetry = true))
            }
        }
    }

    fun onCaptureFailed(file: File?) {
        if (file != null) viewModelScope.launch { photoStore.discard(file) }
        setPhase(CapturePhase.Error(R.string.capture_error_camera, canRetry = true))
    }

    fun onRetry() {
        if (_uiState.value.quest == null) load() else setPhase(CapturePhase.Ready)
    }

    fun onSkip() {
        viewModelScope.launch {
            runCatchingCancellable { skipQuest(questId) }
                .onSuccess { _events.send(CaptureEvent.Finished) }
                .onFailure { setPhase(CapturePhase.Error(R.string.capture_error_skip, canRetry = true)) }
        }
    }

    fun onDone() {
        viewModelScope.launch { _events.send(CaptureEvent.Finished) }
    }

    private fun setPhase(phase: CapturePhase) {
        _uiState.update { it.copy(phase = phase) }
    }
}
