package app.polaroam.ai.vision

import android.graphics.Bitmap
import app.polaroam.ai.model.ModelFiles
import app.polaroam.ai.model.ModelSpec
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.ai.PhotoVerifierUnavailableException
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.Verdict
import app.polaroam.testutil.testQuest
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito

class OnDevicePhotoVerifierTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val spec = ModelSpec("https://example.com/v.onnx", "", "vision.onnx", 10L)
    private val bitmap: Bitmap = Mockito.mock(Bitmap::class.java)
    private val created = mutableListOf<RecordingVerifier>()
    private lateinit var files: ModelFiles

    @Before
    fun setUp() {
        files = ModelFiles(File(tmp.root, "models"))
    }

    private fun verifier(dispatcher: CoroutineDispatcher) = OnDevicePhotoVerifier(
        modelFiles = files,
        visionModel = spec,
        factory = PhotoVerifierFactory { RecordingVerifier().also(created::add) },
        io = dispatcher,
    )

    @Test
    fun `without the model every check is unavailable`() = runTest {
        val subject = verifier(StandardTestDispatcher(testScheduler))

        val error = runCatching { subject.verify(bitmap, testQuest("q1")) }.exceptionOrNull()

        assertThat(error).isInstanceOf(PhotoVerifierUnavailableException::class.java)
        assertThat(created).isEmpty()
    }

    @Test
    fun `the model is loaded once and reused`() = runTest {
        installModel("v1")
        val subject = verifier(StandardTestDispatcher(testScheduler))

        subject.verify(bitmap, testQuest("q1"))
        subject.verify(bitmap, testQuest("q2"))

        assertThat(created).hasSize(1)
        assertThat(created.single().calls).isEqualTo(2)
    }

    @Test
    fun `a replaced model rebuilds and closes the old verifier`() = runTest {
        installModel("v1")
        val subject = verifier(StandardTestDispatcher(testScheduler))
        subject.verify(bitmap, testQuest("q1"))

        installModel("version two")
        subject.verify(bitmap, testQuest("q1"))

        assertThat(created).hasSize(2)
        assertThat(created.first().closed).isTrue()
        assertThat(created.last().closed).isFalse()
    }

    @Test
    fun `a deleted model closes the verifier and becomes unavailable`() = runTest {
        installModel("v1")
        val subject = verifier(StandardTestDispatcher(testScheduler))
        subject.verify(bitmap, testQuest("q1"))

        files.deleteAll()
        val error = runCatching { subject.verify(bitmap, testQuest("q1")) }.exceptionOrNull()

        assertThat(error).isInstanceOf(PhotoVerifierUnavailableException::class.java)
        assertThat(created.single().closed).isTrue()
    }

    private fun installModel(contents: String) {
        files.preparePartFile(spec).writeText(contents)
        files.promotePart(spec)
    }

    private class RecordingVerifier : PhotoVerifier, AutoCloseable {
        var calls = 0
        var closed = false

        override suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict {
            calls++
            return Verdict(match = true, confidence = 0.5f, label = quest.targetLabels.first())
        }

        override fun close() {
            closed = true
        }
    }
}
