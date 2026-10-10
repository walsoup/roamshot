package app.polaroam.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.polaroam.domain.model.CompletedQuest
import app.polaroam.domain.repository.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class GalleryItemUi(
    val questId: String,
    val title: String,
    val walkType: String,
    val photoPath: String,
    val points: Int,
    val day: LocalDate,
    val tiltDegrees: Float,
)

sealed interface GalleryUiState {
    data object Loading : GalleryUiState
    data object Error : GalleryUiState
    data class Loaded(val items: List<GalleryItemUi>) : GalleryUiState
}

@HiltViewModel
class GalleryViewModel @Inject constructor(
    walks: WalkRepository,
) : ViewModel() {

    val uiState: StateFlow<GalleryUiState> = walks.observeCompletedQuests()
        .map<List<CompletedQuest>, GalleryUiState> { list -> GalleryUiState.Loaded(list.map { it.toUi() }) }
        .catch { emit(GalleryUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GalleryUiState.Loading)

    private fun CompletedQuest.toUi() = GalleryItemUi(
        questId = quest.id,
        title = quest.title,
        walkType = walkType,
        photoPath = completion.photoPath,
        points = completion.points,
        day = completion.day,
        tiltDegrees = tiltFor(quest.id),
    )

    /** Stable per photo so the grid doesn't reshuffle its tilt on every recomposition. */
    private fun tiltFor(id: String): Float = (abs(id.hashCode()) % 5 - 2) * 1.5f
}
