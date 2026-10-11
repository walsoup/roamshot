package app.polaroam.ai.vision

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ClipImagePreprocessorTest {

    @Test
    fun `pixels become normalized channel planes`() {
        val red = 0xFFFF0000.toInt()
        val black = 0xFF000000.toInt()

        val data = ClipImagePreprocessor.toChw(intArrayOf(red, black, black, black), size = 2)

        assertThat(data).hasLength(12)
        // Red plane: full red in the first pixel, zero in the rest.
        assertThat(data[0]).isWithin(1e-4f).of((1f - 0.48145466f) / 0.26862954f)
        assertThat(data[1]).isWithin(1e-4f).of(-0.48145466f / 0.26862954f)
        // Green and blue planes start at offsets 4 and 8.
        assertThat(data[4]).isWithin(1e-4f).of(-0.4578275f / 0.26130258f)
        assertThat(data[8]).isWithin(1e-4f).of(-0.40821073f / 0.27577711f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `pixel count must match the size`() {
        ClipImagePreprocessor.toChw(IntArray(3), size = 2)
    }
}
