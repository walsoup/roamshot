package app.polaroam.domain.streak

import app.polaroam.domain.model.StreakState
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class StreakCalculatorTest {

    private val calculator = StreakCalculator()
    private val day = LocalDate.of(2026, 10, 10)

    @Test
    fun `first verified quest starts a streak of one`() {
        val result = calculator.onQuestVerified(StreakState(), day)

        assertThat(result).isEqualTo(StreakState(current = 1, longest = 1, lastActiveDay = day))
    }

    @Test
    fun `more quests on the same day do not change the streak`() {
        val once = calculator.onQuestVerified(StreakState(), day)
        val twice = calculator.onQuestVerified(once, day)

        assertThat(twice).isEqualTo(once)
    }

    @Test
    fun `consecutive days extend the streak and the record`() {
        var state = StreakState()
        repeat(4) { offset -> state = calculator.onQuestVerified(state, day.plusDays(offset.toLong())) }

        assertThat(state.current).isEqualTo(4)
        assertThat(state.longest).isEqualTo(4)
        assertThat(state.lastActiveDay).isEqualTo(day.plusDays(3))
    }

    @Test
    fun `missing a day resets the streak but keeps the record`() {
        val state = StreakState(current = 5, longest = 5, lastActiveDay = day)

        val result = calculator.onQuestVerified(state, day.plusDays(2))

        assertThat(result.current).isEqualTo(1)
        assertThat(result.longest).isEqualTo(5)
        assertThat(result.lastActiveDay).isEqualTo(day.plusDays(2))
    }

    @Test
    fun `a day earlier than the last active day is ignored`() {
        val state = StreakState(current = 3, longest = 4, lastActiveDay = day)

        assertThat(calculator.onQuestVerified(state, day.minusDays(1))).isEqualTo(state)
    }

    @Test
    fun `streak continues across a year boundary`() {
        val state = StreakState(current = 2, longest = 2, lastActiveDay = LocalDate.of(2026, 12, 31))

        val result = calculator.onQuestVerified(state, LocalDate.of(2027, 1, 1))

        assertThat(result.current).isEqualTo(3)
    }

    @Test
    fun `leap day counts as a real day`() {
        val beforeLeap = StreakState(current = 1, longest = 1, lastActiveDay = LocalDate.of(2028, 2, 28))

        assertThat(calculator.onQuestVerified(beforeLeap, LocalDate.of(2028, 2, 29)).current).isEqualTo(2)
        assertThat(calculator.onQuestVerified(beforeLeap, LocalDate.of(2028, 3, 1)).current).isEqualTo(1)
    }

    @Test
    fun `displayed streak survives until the end of the next day`() {
        val state = StreakState(current = 3, longest = 3, lastActiveDay = day)

        assertThat(calculator.currentStreak(state, day)).isEqualTo(3)
        assertThat(calculator.currentStreak(state, day.plusDays(1))).isEqualTo(3)
    }

    @Test
    fun `displayed streak drops to zero once a day is missed`() {
        val state = StreakState(current = 3, longest = 3, lastActiveDay = day)

        assertThat(calculator.currentStreak(state, day.plusDays(2))).isEqualTo(0)
    }

    @Test
    fun `no activity means no streak`() {
        assertThat(calculator.currentStreak(StreakState(), day)).isEqualTo(0)
    }
}
