package app.polaroam.ai.canned

import app.polaroam.ai.safety.QuestSafetyFilter
import app.polaroam.ai.vision.LabelVocabulary
import app.polaroam.domain.model.QuestRules
import app.polaroam.domain.model.WalkPreset
import com.google.common.truth.Truth.assertThat
import kotlin.random.Random
import org.junit.Test

class CannedQuestGeneratorTest {

    private val pool = CannedQuestPool.all
    private val titlesFor = { preset: WalkPreset -> pool.filter { preset in it.presets }.map { it.title }.toSet() }
    private val genericTitles = pool.filter { it.presets.isEmpty() }.map { it.title }.toSet()

    @Test
    fun `count is clamped to three to five`() {
        val generator = CannedQuestGenerator(random = Random(3))

        assertThat(generator.pick("City park", 1)).hasSize(3)
        assertThat(generator.pick("City park", 4)).hasSize(4)
        assertThat(generator.pick("City park", 9)).hasSize(5)
    }

    @Test
    fun `quests are unique with fresh ids`() {
        val quests = CannedQuestGenerator(random = Random(5)).pick("Neighborhood", 5)

        assertThat(quests.map { it.title }.toSet()).hasSize(5)
        assertThat(quests.map { it.id }.toSet()).hasSize(5)
    }

    @Test
    fun `night walks only use night quests`() {
        repeat(20) { seed ->
            val quests = CannedQuestGenerator(random = Random(seed)).pick("late night stroll", 5)
            assertThat(titlesFor(WalkPreset.NIGHT_WALK)).containsAtLeastElementsIn(quests.map { it.title })
        }
    }

    @Test
    fun `presets prefer their own quests`() {
        val quests = CannedQuestGenerator(random = Random(11)).pick("Forest trail", 4)

        assertThat(titlesFor(WalkPreset.FOREST_TRAIL)).containsAtLeastElementsIn(quests.map { it.title })
    }

    @Test
    fun `unknown walk types use generic daytime quests`() {
        val quests = CannedQuestGenerator(random = Random(2)).pick("art museum loop", 5)

        assertThat(genericTitles).containsAtLeastElementsIn(quests.map { it.title })
    }

    @Test
    fun `same seed gives the same picks`() {
        val a = CannedQuestGenerator(random = Random(42)).pick("City park", 4).map { it.title }
        val b = CannedQuestGenerator(random = Random(42)).pick("City park", 4).map { it.title }

        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `quests are sorted easiest first`() {
        val quests = CannedQuestGenerator(random = Random(9)).pick("City park", 5)

        assertThat(quests.map { it.difficulty.level }).isInOrder()
    }

    @Test
    fun `every pool quest follows the quest rules`() {
        val safety = QuestSafetyFilter()
        assertThat(pool.map { it.key }.toSet()).hasSize(pool.size)

        pool.forEach { quest ->
            assertThat(quest.title.length).isIn(QuestRules.TITLE_MIN..QuestRules.TITLE_MAX)
            assertThat(quest.objective.length).isIn(QuestRules.OBJECTIVE_MIN..QuestRules.OBJECTIVE_MAX)
            assertThat(quest.targetLabels.size).isIn(QuestRules.MIN_LABELS..QuestRules.MAX_LABELS)
            assertThat(LabelVocabulary.labels).containsAtLeastElementsIn(quest.targetLabels)
            assertThat(quest.targetLabels.map(LabelVocabulary::canonical).toSet()).hasSize(quest.targetLabels.size)
            assertThat(safety.violation(quest.title, quest.objective, quest.targetLabels)).isNull()
        }
    }

    @Test
    fun `every preset has enough quests for a full walk`() {
        WalkPreset.entries.forEach { preset ->
            val available = pool.count { preset in it.presets } +
                if (preset == WalkPreset.NIGHT_WALK) 0 else genericTitles.size
            assertThat(available).isAtLeast(QuestRules.MAX_QUESTS)
        }
        assertThat(pool.count { WalkPreset.NIGHT_WALK in it.presets }).isAtLeast(QuestRules.MAX_QUESTS)
    }
}
