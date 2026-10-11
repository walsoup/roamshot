package app.polaroam.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.polaroam.ai.model.QuestModelChoice
import app.polaroam.core.result.runCatchingCancellable
import app.polaroam.core.time.AppClock
import app.polaroam.domain.model.ModelState
import app.polaroam.domain.model.PlayerProgress
import app.polaroam.domain.model.ProgressState
import app.polaroam.domain.repository.ModelManager
import app.polaroam.domain.repository.ProgressRepository
import app.polaroam.domain.streak.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatsUiState(
    val isLoading: Boolean = true,
    val totalScore: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val questsCompleted: Int = 0,
    val selectedModel: QuestModelChoice = QuestModelChoice.QWEN3,
    val modelState: ModelState = ModelState.NotConfigured,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    progress: ProgressRepository,
    private val modelManager: ModelManager,
    private val streaks: StreakCalculator,
    private val clock: AppClock,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        progress.observeProgress().catch { emit(PlayerProgress(ProgressState.EMPTY, 0)) },
        modelManager.selectedModel.catch { emit(QuestModelChoice.QWEN3) },
        modelManager.state.catch { emit(ModelState.NotConfigured) },
    ) { player, selectedChoice, model ->
        StatsUiState(
            isLoading = false,
            totalScore = player.state.totalScore,
            currentStreak = streaks.currentStreak(player.state.streak, clock.today()),
            longestStreak = player.state.streak.longest,
            questsCompleted = player.questsCompleted,
            selectedModel = selectedChoice,
            modelState = model,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun onSelectModel(choice: QuestModelChoice) = modelManager.selectModel(choice)

    fun onDownloadModel() = modelManager.download()

    fun onCancelDownload() = modelManager.cancel()

    fun onDeleteModel() {
        viewModelScope.launch { runCatchingCancellable { modelManager.delete() } }
    }
}
