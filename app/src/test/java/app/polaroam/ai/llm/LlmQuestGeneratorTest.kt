package app.polaroam.ai.llm

import app.polaroam.ai.canned.CannedQuestGenerator
import app.polaroam.ai.safety.QuestSafetyFilter
import app.polaroam.domain.model.QuestSource
import app.polaroam.testutil.FakeLlmEngine
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LlmQuestGeneratorTest {

    private val promptBuilder = QuestPromptBuilder()
    private val parser = QuestJsonParser(QuestSafetyFilter())
    private val fallback = CannedQuestGenerator(random = Random(1))

    private fun generator(engine: LlmEngine, timeoutMillis: Long = 1_000) =
        LlmQuestGenerator(engine, fallback, promptBuilder, parser, timeoutMillis)

    private val validReply = """
        {"quests":[
          {"title":"Bench Story","objective":"Find a bench with old wood or a great view.","category":"OBJECTS","difficulty":1,"targetLabels":["bench","wood"]},
          {"title":"Duck Patrol","objective":"Photograph a duck gliding on the pond.","category":"ANIMALS","difficulty":2,"targetLabels":["duck","bird"]},
          {"title":"Cloud Shapes","objective":"Catch a cloud that looks like an animal.","category":"SKY","difficulty":3,"targetLabels":["cloud","sky"]}
        ]}
    """.trimIndent()

    @Test
    fun `valid first reply is used directly`() = runTest {
        val engine = FakeLlmEngine({ validReply })

        val result = generator(engine).generateDetailed("City park", 3)

        assertThat(result.source).isEqualTo(QuestSource.MODEL)
        assertThat(result.quests.map { it.title }).containsExactly("Bench Story", "Duck Patrol", "Cloud Shapes").inOrder()
        assertThat(engine.prompts).hasSize(1)
    }

    @Test
    fun `invalid first reply triggers exactly one retry with the reason`() = runTest {
        val engine = FakeLlmEngine({ "not json at all" }, { validReply })

        val result = generator(engine).generateDetailed("City park", 3)

        assertThat(result.source).isEqualTo(QuestSource.MODEL)
        assertThat(engine.prompts).hasSize(2)
        assertThat(engine.prompts[1].user).contains("rejected")
        assertThat(engine.prompts[1].system).isEqualTo(engine.prompts[0].system)
    }

    @Test
    fun `two invalid replies fall back to the built-in pool`() = runTest {
        val engine = FakeLlmEngine({ "nope" }, { "{\"quests\":[]}" })

        val result = generator(engine).generateDetailed("Forest trail", 4)

        assertThat(result.source).isEqualTo(QuestSource.BUILT_IN)
        assertThat(result.quests).hasSize(4)
        assertThat(result.fallbackReason).isNotNull()
        assertThat(engine.prompts).hasSize(2)
    }

    @Test
    fun `engine errors fall back to the built-in pool`() = runTest {
        val engine = FakeLlmEngine({ throw IOException("native crash") }, { throw IllegalStateException("still broken") })

        val result = generator(engine).generateDetailed("Night walk", 3)

        assertThat(result.source).isEqualTo(QuestSource.BUILT_IN)
        assertThat(result.fallbackReason).contains("still broken")
    }

    @Test
    fun `a hung engine times out and falls back`() = runTest {
        val engine = FakeLlmEngine(
            { delay(10_000); validReply },
            { delay(10_000); validReply },
        )

        val result = generator(engine, timeoutMillis = 1_000).generateDetailed("City park", 3)

        assertThat(result.source).isEqualTo(QuestSource.BUILT_IN)
        assertThat(result.fallbackReason).contains("too long")
    }

    @Test
    fun `error then valid reply succeeds on retry`() = runTest {
        val engine = FakeLlmEngine({ throw IOException("hiccup") }, { validReply })

        val result = generator(engine).generateDetailed("City park", 3)

        assertThat(result.source).isEqualTo(QuestSource.MODEL)
    }

    @Test
    fun `generate returns just the quests`() = runTest {
        val engine = FakeLlmEngine({ validReply })

        assertThat(generator(engine).generate("City park", 3)).hasSize(3)
    }
}
