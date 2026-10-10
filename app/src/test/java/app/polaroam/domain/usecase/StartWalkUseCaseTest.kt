package app.polaroam.domain.usecase

import app.polaroam.ai.canned.CannedQuestGenerator
import app.polaroam.domain.ai.QuestGenerator
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestSource
import app.polaroam.testutil.FakeWalkRepository
import app.polaroam.testutil.FixedClock
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StartWalkUseCaseTest {

    private val walks = FakeWalkRepository()
    private val clock = FixedClock()

    @Test
    fun `blank walk type is rejected without generating`() = runTest {
        val generator = RecordingGenerator()

        val result = StartWalkUseCase(generator, walks, clock)("   ")

        assertThat(result).isEqualTo(StartWalkResult.InvalidWalkType)
        assertThat(generator.requests).isEmpty()
        assertThat(walks.walks.value).isEmpty()
    }

    @Test
    fun `starting a walk persists generated quests in order`() = runTest {
        val result = StartWalkUseCase(CannedQuestGenerator(), walks, clock)("  Forest   trail ")

        result as StartWalkResult.Started
        assertThat(result.source).isEqualTo(QuestSource.BUILT_IN)
        val walk = walks.observeWalk(result.walkId).first()!!
        assertThat(walk.walkType).isEqualTo("Forest trail")
        assertThat(walk.quests).hasSize(4)
        assertThat(walk.quests.map { it.position }).containsExactly(0, 1, 2, 3).inOrder()
    }

    @Test
    fun `quest count is clamped to the allowed range`() = runTest {
        val generator = RecordingGenerator()

        StartWalkUseCase(generator, walks, clock)("Night walk", count = 12)

        assertThat(generator.requests.single().second).isEqualTo(5)
    }

    @Test
    fun `generator crash is reported as failure`() = runTest {
        val crashing = object : QuestGenerator {
            override suspend fun generate(walkType: String, count: Int): List<Quest> = error("boom")
        }

        val result = StartWalkUseCase(crashing, walks, clock)("City park")

        assertThat(result).isInstanceOf(StartWalkResult.Failed::class.java)
        assertThat(walks.walks.value).isEmpty()
    }

    @Test
    fun `empty generator output is reported as failure`() = runTest {
        val empty = object : QuestGenerator {
            override suspend fun generate(walkType: String, count: Int): List<Quest> = emptyList()
        }

        val result = StartWalkUseCase(empty, walks, clock)("City park")

        assertThat(result).isInstanceOf(StartWalkResult.Failed::class.java)
    }

    private class RecordingGenerator : QuestGenerator {
        val requests = mutableListOf<Pair<String, Int>>()
        private val canned = CannedQuestGenerator()

        override suspend fun generate(walkType: String, count: Int): List<Quest> {
            requests += walkType to count
            return canned.generate(walkType, count)
        }
    }
}
