package app.polaroam.ai.safety

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuestSafetyFilterTest {

    private val filter = QuestSafetyFilter()

    @Test
    fun `flags people and faces`() {
        assertThat(filter.violation("Snap a selfie by the fountain")).isEqualTo("selfie")
        assertThat(filter.violation("Find two faces in the crowd")).isNotNull()
        assertThat(filter.violation("Photograph people having a picnic")).isEqualTo("people")
    }

    @Test
    fun `flags trespassing and private property`() {
        assertThat(filter.violation("Climb the fence into the backyard")).isNotNull()
        assertThat(filter.violation("Find a flower in a private garden")).isEqualTo("private")
    }

    @Test
    fun `flags roads and traffic`() {
        assertThat(filter.violation("Catch a red car at the crosswalk")).isNotNull()
        assertThat(filter.violation("Photograph the busiest street")).isEqualTo("street")
        assertThat(filter.violation("Count the buses")).isEqualTo("buses")
    }

    @Test
    fun `flags unsafe night situations`() {
        assertThat(filter.violation("Explore a dark alley")).isEqualTo("alley")
        assertThat(filter.violation("Walk alone into the darkness")).isNotNull()
    }

    @Test
    fun `flags money and purchases`() {
        assertThat(filter.violation("Buy an ice cream and photograph it")).isEqualTo("buy")
        assertThat(filter.violation("Find the cheapest shop")).isEqualTo("shop")
    }

    @Test
    fun `does not flag words that merely contain a denied term`() {
        assertThat(filter.violation("Find moss on a rough surface")).isNull()
        assertThat(filter.violation("A carpet of leaves")).isNull()
        assertThat(filter.violation("A busy bee on a flower")).isNull()
        assertThat(filter.violation("Spot a manhole-sized mushroom")).isNull()
        assertThat(filter.violation("Pay attention to the clouds")).isNull()
    }

    @Test
    fun `checks labels too`() {
        assertThat(filter.violation("Nice title", "A fine objective here.", listOf("dog", "person"))).isEqualTo("person")
    }
}
