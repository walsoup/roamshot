package app.polaroam.domain.streak

import app.polaroam.domain.model.StreakState
import java.time.LocalDate
import javax.inject.Inject

/**
 * Daily streak rules, on local calendar days:
 * - the first verified quest of a day extends the streak if the previous active day was yesterday,
 *   otherwise starts a new streak of 1;
 * - further verified quests on the same day change nothing;
 * - a streak whose last active day is older than yesterday is broken.
 */
class StreakCalculator @Inject constructor() {

    fun onQuestVerified(state: StreakState, day: LocalDate): StreakState {
        val last = state.lastActiveDay
        val current = when {
            last == null -> 1
            !day.isAfter(last) -> return state
            day == last.plusDays(1) -> state.current + 1
            else -> 1
        }
        return StreakState(
            current = current,
            longest = maxOf(state.longest, current),
            lastActiveDay = day,
        )
    }

    /** The streak to display on [today]; 0 once a day has been missed. */
    fun currentStreak(state: StreakState, today: LocalDate): Int {
        val last = state.lastActiveDay ?: return 0
        return if (today.isAfter(last.plusDays(1))) 0 else state.current
    }
}
