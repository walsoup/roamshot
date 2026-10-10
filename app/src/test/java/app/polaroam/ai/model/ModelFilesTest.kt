package app.polaroam.ai.model

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.io.IOException
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ModelFilesTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var dir: File
    private lateinit var files: ModelFiles

    @Before
    fun setUp() {
        dir = File(tmp.root, "models")
        files = ModelFiles(dir, "model.task")
    }

    @Test
    fun `no model until a non-empty file exists`() {
        assertThat(files.readyModel()).isNull()

        files.preparePartFile()
        files.modelFile.writeText("")
        assertThat(files.readyModel()).isNull()

        files.modelFile.writeText("weights")
        assertThat(files.readyModel()).isEqualTo(files.modelFile)
    }

    @Test
    fun `promote moves the part file into place`() {
        files.preparePartFile().writeText("weights")

        val promoted = files.promotePart()

        assertThat(promoted.readText()).isEqualTo("weights")
        assertThat(files.partFile.exists()).isFalse()
        assertThat(files.readyModel()).isNotNull()
    }

    @Test
    fun `promote replaces an older model`() {
        files.preparePartFile()
        files.modelFile.writeText("old")
        files.partFile.writeText("new")

        files.promotePart()

        assertThat(files.modelFile.readText()).isEqualTo("new")
    }

    @Test
    fun `promote without a download fails`() {
        files.preparePartFile()

        assertThrows(IOException::class.java) { files.promotePart() }
    }

    @Test
    fun `delete removes model and partial download`() {
        files.preparePartFile().writeText("partial")
        files.modelFile.writeText("weights")

        assertThat(files.deleteAll()).isTrue()
        assertThat(files.modelFile.exists()).isFalse()
        assertThat(files.partFile.exists()).isFalse()
    }

    @Test
    fun `only https urls count as configured`() {
        assertThat(ModelConfig("https://example.com/m.task", "").isConfigured).isTrue()
        assertThat(ModelConfig("http://example.com/m.task", "").isConfigured).isFalse()
        assertThat(ModelConfig("", "").isConfigured).isFalse()
    }

    @Test
    fun `checksum is normalized and optional`() {
        assertThat(ModelConfig("https://x", " ABCD ").expectedSha256).isEqualTo("abcd")
        assertThat(ModelConfig("https://x", "  ").expectedSha256).isNull()
    }
}
