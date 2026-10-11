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

    private val quest = spec("quest.litertlm")
    private val vision = spec("vision.onnx")

    @Before
    fun setUp() {
        dir = File(tmp.root, "models")
        files = ModelFiles(dir)
    }

    @Test
    fun `no model until a non-empty file exists`() {
        assertThat(files.readyModel(quest)).isNull()

        files.preparePartFile(quest)
        files.modelFile(quest).writeText("")
        assertThat(files.readyModel(quest)).isNull()

        files.modelFile(quest).writeText("weights")
        assertThat(files.readyModel(quest)).isEqualTo(files.modelFile(quest))
    }

    @Test
    fun `the pack is ready only when every model is`() {
        files.preparePartFile(quest)
        files.modelFile(quest).writeText("weights")
        assertThat(files.allReady(listOf(quest, vision))).isFalse()

        files.modelFile(vision).writeText("weights")
        assertThat(files.allReady(listOf(quest, vision))).isTrue()
    }

    @Test
    fun `promote moves the part file into place`() {
        files.preparePartFile(quest).writeText("weights")

        val promoted = files.promotePart(quest)

        assertThat(promoted.readText()).isEqualTo("weights")
        assertThat(files.partFile(quest).exists()).isFalse()
        assertThat(files.readyModel(quest)).isNotNull()
        assertThat(files.readyModel(vision)).isNull()
    }

    @Test
    fun `promote replaces an older model`() {
        files.preparePartFile(quest)
        files.modelFile(quest).writeText("old")
        files.partFile(quest).writeText("new")

        files.promotePart(quest)

        assertThat(files.modelFile(quest).readText()).isEqualTo("new")
    }

    @Test
    fun `promote without a download fails`() {
        files.preparePartFile(quest)

        assertThrows(IOException::class.java) { files.promotePart(quest) }
    }

    @Test
    fun `delete removes every model, partial download and leftover`() {
        files.preparePartFile(quest).writeText("partial")
        files.modelFile(vision).writeText("weights")
        File(dir, "quest-model.task").writeText("old app version")

        assertThat(files.deleteAll()).isTrue()
        assertThat(dir.listFiles()).isEmpty()
    }

    @Test
    fun `only https urls count as configured`() {
        assertThat(spec("m", url = "https://example.com/m").isConfigured).isTrue()
        assertThat(spec("m", url = "http://example.com/m").isConfigured).isFalse()
        assertThat(spec("m", url = "").isConfigured).isFalse()
        assertThat(ModelConfig(quest, spec("v", url = "")).isConfigured).isFalse()
        assertThat(ModelConfig().isConfigured).isTrue()
    }

    @Test
    fun `checksum is normalized and optional`() {
        assertThat(spec("m", sha256 = " ABCD ").expectedSha256).isEqualTo("abcd")
        assertThat(spec("m", sha256 = "  ").expectedSha256).isNull()
    }

    @Test
    fun `blank overrides keep the pinned model`() {
        val pinned = ModelConfig.QWEN3_0_6B

        assertThat(pinned.overriddenBy(" ", "abc")).isEqualTo(pinned)
        assertThat(pinned.overriddenBy("https://example.com/m", "abc").url).isEqualTo("https://example.com/m")
        assertThat(pinned.overriddenBy("https://example.com/m", "").expectedSha256).isNull()
    }

    private fun spec(fileName: String, url: String = "https://example.com/$fileName", sha256: String = "") =
        ModelSpec(url = url, sha256 = sha256, fileName = fileName, sizeBytes = 10L)
}
