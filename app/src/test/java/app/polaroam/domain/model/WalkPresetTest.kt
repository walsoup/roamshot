package app.polaroam.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WalkPresetTest {

    @Test
    fun `preset labels match exactly regardless of case`() {
        assertThat(WalkPreset.match("City park")).isEqualTo(WalkPreset.CITY_PARK)
        assertThat(WalkPreset.match("  forest TRAIL ")).isEqualTo(WalkPreset.FOREST_TRAIL)
    }

    @Test
    fun `free text maps by keyword`() {
        assertThat(WalkPreset.match("a stroll in the woods")).isEqualTo(WalkPreset.FOREST_TRAIL)
        assertThat(WalkPreset.match("botanical garden loop")).isEqualTo(WalkPreset.CITY_PARK)
    }

    @Test
    fun `night keywords always win`() {
        assertThat(WalkPreset.match("forest trail at night")).isEqualTo(WalkPreset.NIGHT_WALK)
        assertThat(WalkPreset.match("evening park lap")).isEqualTo(WalkPreset.NIGHT_WALK)
    }

    @Test
    fun `unrelated text has no preset`() {
        assertThat(WalkPreset.match("museum of spaceships")).isNull()
    }

    @Test
    fun `walk type input is trimmed collapsed and capped`() {
        assertThat(WalkTypeInput.normalize("  rainy   river\twalk ")).isEqualTo("rainy river walk")
        assertThat(WalkTypeInput.normalize("x".repeat(100))).hasLength(WalkTypeInput.MAX_LENGTH)
        assertThat(WalkTypeInput.isValid(" \n ")).isFalse()
    }
}
