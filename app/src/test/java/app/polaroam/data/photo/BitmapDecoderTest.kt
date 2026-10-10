package app.polaroam.data.photo

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class BitmapDecoderTest {

    @Test
    fun `sample size keeps both sides at or above the target`() {
        assertThat(BitmapDecoder.calculateInSampleSize(4000, 3000, 1024)).isEqualTo(2)
        assertThat(BitmapDecoder.calculateInSampleSize(8192, 8192, 1024)).isEqualTo(8)
    }

    @Test
    fun `small images are not downsampled`() {
        assertThat(BitmapDecoder.calculateInSampleSize(1024, 1024, 1024)).isEqualTo(1)
        assertThat(BitmapDecoder.calculateInSampleSize(640, 480, 1024)).isEqualTo(1)
    }

    @Test
    fun `invalid sizes are handled`() {
        assertThat(BitmapDecoder.calculateInSampleSize(0, 0, 1024)).isEqualTo(1)
        assertThrows(IllegalArgumentException::class.java) {
            BitmapDecoder.calculateInSampleSize(100, 100, 0)
        }
    }
}
