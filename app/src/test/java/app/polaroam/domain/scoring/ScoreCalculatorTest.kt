package app.polaroam.domain.scoring

import app.polaroam.domain.model.Difficulty
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ScoreCalculatorTest {

    private val calculator = ScoreCalculator()

    @Test
    fun `base points scale with difficulty`() {
        assertThat(calculator.basePoints(Difficulty.EASY)).isEqualTo(10)
        assertThat(calculator.basePoints(Difficulty.MEDIUM)).isEqualTo(20)
        assertThat(calculator.basePoints(Difficulty.HARD)).isEqualTo(30)
    }

    @Test
    fun `first try adds five bonus points`() {
        assertThat(calculator.pointsFor(Difficulty.EASY, attemptNumber = 1)).isEqualTo(15)
        assertThat(calculator.pointsFor(Difficulty.MEDIUM, attemptNumber = 1)).isEqualTo(25)
        assertThat(calculator.pointsFor(Difficulty.HARD, attemptNumber = 1)).isEqualTo(35)
    }

    @Test
    fun `later attempts earn only base points`() {
        assertThat(calculator.pointsFor(Difficulty.EASY, attemptNumber = 2)).isEqualTo(10)
        assertThat(calculator.pointsFor(Difficulty.HARD, attemptNumber = 5)).isEqualTo(30)
    }

    @Test
    fun `attempt numbers start at one`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.pointsFor(Difficulty.EASY, attemptNumber = 0)
        }
    }
}
