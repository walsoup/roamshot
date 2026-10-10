package app.polaroam.ui.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.polaroam.R
import app.polaroam.domain.model.Walk
import app.polaroam.domain.model.WalkPreset
import app.polaroam.domain.model.WalkTypeInput
import app.polaroam.domain.repository.WalkRepository
import app.polaroam.domain.usecase.StartWalkResult
import app.polaroam.domain.usecase.StartWalkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResumableWalk(
    val walkId: String,
    val walkType: String,
    val resolved: Int,
    val total: Int,
)

data class HomeUiState(
    val walkType: String = "",
    val presets: List<WalkPreset> = WalkPreset.entries,
    val isStarting: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val resumable: ResumableWalk? = null,
) {
    val canStart: Boolean get() = !isStarting && WalkTypeInput.isValid(walkType)
    val maxLength: Int get() = WalkTypeInput.MAX_LENGTH
}

sealed interface HomeEvent {
    data class OpenWalk(val walkId: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startWalk: StartWalkUseCase,
    walks: WalkRepository,
) : ViewModel() {

    private data class Form(
        val walkType: String = "",
        val isStarting: Boolean = false,
        @StringRes val errorRes: Int? = null,
    )

    private val form = MutableStateFlow(Form())
    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    private val resumable: Flow<ResumableWalk?> = walks.observeLatestWalk()
        .map { it?.toResumable() }
        .catch { emit(null) }

    val uiState: StateFlow<HomeUiState> = combine(form, resumable) { f, resume ->
        HomeUiState(
            walkType = f.walkType,
            isStarting = f.isStarting,
            errorRes = f.errorRes,
            resumable = resume,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    fun onWalkTypeChange(text: String) {
        form.update { it.copy(walkType = text.take(WalkTypeInput.MAX_LENGTH), errorRes = null) }
    }

    fun onPresetSelected(preset: WalkPreset) {
        form.update { it.copy(walkType = preset.label, errorRes = null) }
    }

    fun onStartWalk() {
        val current = form.value
        if (current.isStarting) return
        form.update { it.copy(isStarting = true, errorRes = null) }

        viewModelScope.launch {
            when (val result = startWalk(current.walkType)) {
                is StartWalkResult.Started -> {
                    form.update { it.copy(isStarting = false) }
                    _events.send(HomeEvent.OpenWalk(result.walkId))
                }
                StartWalkResult.InvalidWalkType ->
                    form.update { it.copy(isStarting = false, errorRes = R.string.error_walk_type_blank) }
                is StartWalkResult.Failed ->
                    form.update { it.copy(isStarting = false, errorRes = R.string.error_start_walk) }
            }
        }
    }

    private fun Walk.toResumable(): ResumableWalk? =
        if (isFinished) null else ResumableWalk(id, walkType, resolvedCount, quests.size)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
