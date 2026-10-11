package app.polaroam.ai.llm

import app.polaroam.ai.safety.QuestSafetyFilter
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.QuestCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuestJsonParserTest {

    private val parser = QuestJsonParser(QuestSafetyFilter())
    private var ids = 0
    private val idFactory = { "id-${ids++}" }

    private fun item(
        title: String = "Bench Story",
        objective: String = "Find a bench with old wood or a great view.",
        category: String = "OBJECTS",
        difficulty: Int = 1,
        labels: List<String> = listOf("bench", "wood"),
        extra: String = "",
    ): String {
        val labelJson = labels.joinToString(",") { "\"$it\"" }
        return """{"title":"$title","objective":"$objective","category":"$category","difficulty":$difficulty,"targetLabels":[$labelJson]$extra}"""
    }

    private fun envelope(vararg items: String) = """{"quests":[${items.joinToString(",")}]}"""

    private fun threeValid() = arrayOf(
        item(title = "Bench Story"),
        item(title = "Duck Patrol", category = "ANIMALS", difficulty = 2, labels = listOf("duck", "bird")),
        item(title = "Cloud Shapes", category = "SKY", difficulty = 3, labels = listOf("cloud", "sky")),
    )

    @Test
    fun `valid output is parsed into quests`() {
        val result = parser.parse(envelope(*threeValid()), expectedCount = 3, idFactory = idFactory)

        result as QuestParseResult.Valid
        assertThat(result.quests).hasSize(3)
        val second = result.quests[1]
        assertThat(second.title).isEqualTo("Duck Patrol")
        assertThat(second.category).isEqualTo(QuestCategory.ANIMALS)
        assertThat(second.difficulty).isEqualTo(Difficulty.MEDIUM)
        assertThat(second.targetLabels).containsExactly("duck", "bird").inOrder()
        assertThat(result.quests.map { it.id }.toSet()).hasSize(3)
    }

    @Test
    fun `a surrounding markdown fence is tolerated`() {
        val fenced = "```json\n${envelope(*threeValid())}\n```"

        assertThat(parser.parse(fenced, 3, idFactory)).isInstanceOf(QuestParseResult.Valid::class.java)
    }

    @Test
    fun `a bare json array is tolerated`() {
        val arrayJson = "[${threeValid().joinToString(",")}]"

        assertThat(parser.parse(arrayJson, 3, idFactory)).isInstanceOf(QuestParseResult.Valid::class.java)
    }

    @Test
    fun `prose around the json is rejected`() {
        val chatty = "Sure! Here are your quests: ${envelope(*threeValid())}"

        assertThat(parser.parse(chatty, 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }

    @Test
    fun `malformed json is rejected`() {
        val broken = envelope(*threeValid()).dropLast(2)

        assertThat(parser.parse(broken, 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
        assertThat(parser.parse("", 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }

    @Test
    fun `unknown keys are rejected`() {
        val items = threeValid()
        items[0] = item(extra = ""","mood":"happy"""")

        assertThat(parser.parse(envelope(*items), 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }

    @Test
    fun `missing fields are rejected`() {
        val missing = """{"quests":[{"title":"Only a title"}]}"""

        assertThat(parser.parse(missing, 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }

    @Test
    fun `too few quests are rejected`() {
        val items = threeValid().take(2).toTypedArray()

        val result = parser.parse(envelope(*items), 3, idFactory)

        result as QuestParseResult.Invalid
        assertThat(result.reason).contains("expected 3")
    }

    @Test
    fun `extra quests are trimmed to the requested count`() {
        val items = threeValid() + item(title = "Extra Quest")

        val result = parser.parse(envelope(*items), 3, idFactory)

        result as QuestParseResult.Valid
        assertThat(result.quests.map { it.title }).doesNotContain("Extra Quest")
    }

    @Test
    fun `out of range difficulty invalidates the quest`() {
        val items = threeValid()
        items[2] = item(title = "Too Hard", difficulty = 4)

        val result = parser.parse(envelope(*items), 3, idFactory)

        result as QuestParseResult.Invalid
        assertThat(result.reason).contains("difficulty")
    }

    @Test
    fun `label count must be two to five`() {
        val one = threeValid().also { it[0] = item(labels = listOf("bench")) }
        val six = threeValid().also {
            it[0] = item(labels = listOf("bench", "wood", "tree", "leaf", "grass", "moss"))
        }

        assertThat(parser.parse(envelope(*one), 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
        assertThat(parser.parse(envelope(*six), 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }

    @Test
    fun `labels outside the vocabulary are dropped`() {
        val items = threeValid()
        items[0] = item(labels = listOf("bench", "spaceship", "wood"))

        val result = parser.parse(envelope(*items), 3, idFactory)

        result as QuestParseResult.Valid
        assertThat(result.quests[0].targetLabels).containsExactly("bench", "wood").inOrder()
    }

    @Test
    fun `quest with too few known labels is rejected`() {
        val items = threeValid()
        items[0] = item(labels = listOf("spaceship", "unicorn"))

        assertThat(parser.parse(envelope(*items), 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }

    @Test
    fun `synonyms and plurals are canonicalized`() {
        val items = threeValid()
        items[1] = item(title = "Pet Parade", category = "ANIMALS", labels = listOf("Puppies", "kittens"))

        val result = parser.parse(envelope(*items), 3, idFactory)

        result as QuestParseResult.Valid
        assertThat(result.quests[1].targetLabels).containsExactly("dog", "cat").inOrder()
    }

    @Test
    fun `unsafe quests are rejected`() {
        val items = threeValid()
        items[0] = item(title = "Stranger Snap", objective = "Photograph a stranger's face near the bench.")

        val result = parser.parse(envelope(*items), 3, idFactory)

        result as QuestParseResult.Invalid
        assertThat(result.reason).contains("safety")
    }

    @Test
    fun `category is case insensitive but must be known`() {
        val lower = threeValid().also { it[0] = item(category = "objects") }
        val unknown = threeValid().also { it[0] = item(category = "VIBES") }

        assertThat(parser.parse(envelope(*lower), 3, idFactory)).isInstanceOf(QuestParseResult.Valid::class.java)
        assertThat(parser.parse(envelope(*unknown), 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }

    @Test
    fun `duplicate titles are rejected`() {
        val items = threeValid()
        items[2] = item(title = "bench story", category = "SKY", labels = listOf("cloud", "sky"))

        assertThat(parser.parse(envelope(*items), 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }

    @Test
    fun `title and objective lengths are enforced`() {
        val shortTitle = threeValid().also { it[0] = item(title = "Hi") }
        val shortObjective = threeValid().also { it[0] = item(objective = "Bench.") }

        assertThat(parser.parse(envelope(*shortTitle), 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
        assertThat(parser.parse(envelope(*shortObjective), 3, idFactory)).isInstanceOf(QuestParseResult.Invalid::class.java)
    }
}
