package app.polaroam.domain.model

import java.time.LocalDate

data class StreakState(
    val current: Int = 0,
    val longest: Int = 0,
    val lastActiveDay: LocalDate? = null,
)

data class ProgressState(
    val totalScore: Int = 0,
    val streak: StreakState = StreakState(),
) {
    companion object {
        val EMPTY = ProgressState()
    }
}

data class PlayerProgress(
    val state: ProgressState,
    val questsCompleted: Int,
)

sealed interface ModelState {
    data object NotConfigured : ModelState
    data object NotDownloaded : ModelState
    data class Downloading(val progress: Float?) : ModelState
    data object Ready : ModelState
    data class Failed(val reason: String) : ModelState
}
