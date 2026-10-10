package app.polaroam.ai.model

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class Sha256Test {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `known vectors`() {
        assertThat(Sha256.hex("".byteInputStream()))
            .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
        assertThat(Sha256.hex("abc".byteInputStream()))
            .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
    }

    @Test
    fun `file hash matches case-insensitively`() {
        val file = tmp.newFile("model.task").apply { writeText("abc") }

        assertThat(Sha256.matches(file, "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD")).isTrue()
        assertThat(Sha256.matches(file, "deadbeef")).isFalse()
    }
}
