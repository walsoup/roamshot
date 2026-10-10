package app.polaroam.domain.scoring

import app.polaroam.domain.model.Difficulty
import javax.inject.Inject

class ScoreCalculator @Inject constructor() {

    fun basePoints(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY -> 10
        Difficulty.MEDIUM -> 20
        Difficulty.HARD -> 30
    }

    /** [attemptNumber] is 1-based: the first photo submitted for a quest is attempt 1. */
    fun pointsFor(difficulty: Difficulty, attemptNumber: Int): Int {
        require(attemptNumber >= 1) { "attemptNumber must be >= 1, was $attemptNumber" }
        val bonus = if (attemptNumber == 1) FIRST_TRY_BONUS else 0
        return basePoints(difficulty) + bonus
    }

    companion object {
        const val FIRST_TRY_BONUS = 5
    }
}
