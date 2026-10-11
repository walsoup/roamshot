package app.polaroam.ui.board

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.QuestCategory
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.QuestStatus
import app.polaroam.domain.model.Walk
import app.polaroam.domain.repository.WalkRepository
import app.polaroam.domain.scoring.ScoreCalculator
import app.polaroam.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuestCardUi(
    val questId: String,
    val poolKey: String?,
    val title: String,
    val objective: String,
    val category: QuestCategory,
    val difficulty: Difficulty,
    val basePoints: Int,
    val firstTryBonus: Int,
    val status: QuestStatus,
    val earnedPoints: Int?,
) {
    val isOpen: Boolean get() = status == QuestStatus.OPEN
}

sealed interface QuestBoardUiState {
    data object Loading : QuestBoardUiState
    data object NotFound : QuestBoardUiState
    data object Error : QuestBoardUiState
    data class Loaded(
        val walkType: String,
        val source: QuestSource,
        val cards: List<QuestCardUi>,
        val completedCount: Int,
        val totalCount: Int,
        val earnedPoints: Int,
        val isFinished: Boolean,
    ) : QuestBoardUiState
}

@HiltViewModel
class QuestBoardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val walks: WalkRepository,
    private val scoring: ScoreCalculator,
) : ViewModel() {

    private val walkId: String = checkNotNull(savedStateHandle[Route.QuestBoard.ARG_WALK_ID]) {
        "QuestBoard opened without a walk id"
    }

    val uiState: StateFlow<QuestBoardUiState> = walks.observeWalk(walkId)
        .map<Walk?, QuestBoardUiState> { walk -> walk?.toUi() ?: QuestBoardUiState.NotFound }
        .catch { emit(QuestBoardUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuestBoardUiState.Loading)

    fun onAbandonWalk() {
        viewModelScope.launch {
            walks.abandonWalk(walkId)
        }
    }

    private fun Walk.toUi() = QuestBoardUiState.Loaded(
        walkType = walkType,
        source = source,
        cards = quests.map { wq ->
            QuestCardUi(
                questId = wq.quest.id,
                poolKey = wq.quest.poolKey,
                title = wq.quest.title,
                objective = wq.quest.objective,
                category = wq.quest.category,
                difficulty = wq.quest.difficulty,
                basePoints = scoring.basePoints(wq.quest.difficulty),
                firstTryBonus = ScoreCalculator.FIRST_TRY_BONUS,
                status = wq.status,
                earnedPoints = wq.completion?.points,
            )
        },
        completedCount = completedCount,
        totalCount = quests.size,
        earnedPoints = earnedPoints,
        isFinished = isFinished,
    )
}
