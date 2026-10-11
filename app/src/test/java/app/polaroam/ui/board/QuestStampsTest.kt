package app.polaroam.ui.board

import app.polaroam.R
import app.polaroam.ai.canned.CannedQuestPool
import app.polaroam.domain.model.QuestCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuestStampsTest {

    private fun drawable(name: String): Int = R.drawable::class.java.getField(name).getInt(null)

    @Test
    fun everyPoolQuestUsesTheStampNamedAfterItsKey() {
        for (poolQuest in CannedQuestPool.all) {
            val expected = drawable("quest_" + poolQuest.key.replace('-', '_'))
            assertThat(questStamp(poolQuest.key, poolQuest.title, poolQuest.category))
                .isEqualTo(expected)
        }
    }

    @Test
    fun poolKeyWinsOverTheTitle() {
        assertThat(questStamp("hood-door", "Feline patrol", QuestCategory.ANIMALS))
            .isEqualTo(R.drawable.quest_hood_door)
    }

    @Test
    fun unknownPoolKeyFallsBackToKeywords() {
        assertThat(questStamp("retired-quest", "Find a wooden bench", QuestCategory.OBJECTS))
            .isEqualTo(R.drawable.quest_park_bench)
    }

    @Test
    fun keywordMatchingWorksForGeneratedQuests() {
        assertThat(questStamp(null, "Find a wooden bench", QuestCategory.OBJECTS)).isEqualTo(R.drawable.quest_park_bench)
        assertThat(questStamp(null, "Photograph a sleeping cat", QuestCategory.ANIMALS)).isEqualTo(R.drawable.quest_hood_cat)
        assertThat(questStamp(null, "Giant mushroom cluster", QuestCategory.NATURE)).isEqualTo(R.drawable.quest_forest_fungi)
        assertThat(questStamp(null, "Ancient stone bridge", QuestCategory.ARCHITECTURE)).isEqualTo(R.drawable.quest_any_bridge)
    }

    @Test
    fun keywordMatchingIsCaseInsensitive() {
        assertThat(questStamp(null, "PAINTED DOOR", QuestCategory.ARCHITECTURE)).isEqualTo(R.drawable.quest_hood_door)
    }

    @Test
    fun keywordMatchingPreventsSubstringFalsePositives() {
        // "street" must not match "tree"
        assertThat(questStamp(null, "Street art on a brick wall", QuestCategory.TEXTURES))
            .isEqualTo(R.drawable.quest_hood_brick)
        // "catching" must not match "cat"
        assertThat(questStamp(null, "Catching autumn leaves", QuestCategory.NATURE))
            .isEqualTo(R.drawable.quest_forest_leaf)
        // "outdoor" must not match "door"
        assertThat(questStamp(null, "Outdoor flower pot", QuestCategory.NATURE))
            .isEqualTo(R.drawable.quest_park_flower)
        // "scattered" must not match "cat"
        assertThat(questStamp(null, "Scattered pebbles", QuestCategory.TEXTURES))
            .isEqualTo(R.drawable.quest_any_rock)
    }

    @Test
    fun keywordMatchingHandlesPluralsAndMultiWordPhrases() {
        assertThat(questStamp(null, "Two sleeping cats", QuestCategory.ANIMALS)).isEqualTo(R.drawable.quest_hood_cat)
        assertThat(questStamp(null, "Tall oak trees", QuestCategory.NATURE)).isEqualTo(R.drawable.quest_forest_giant)
        assertThat(questStamp(null, "Painted front doors", QuestCategory.ARCHITECTURE)).isEqualTo(R.drawable.quest_hood_door)
        assertThat(questStamp(null, "A warm light post", QuestCategory.OBJECTS)).isEqualTo(R.drawable.quest_night_lamp)
        assertThat(questStamp(null, "Rest on wooden benches", QuestCategory.OBJECTS)).isEqualTo(R.drawable.quest_park_bench)
    }

    @Test
    fun titlesWithoutKeywordsFallBackToTheCategoryStamp() {
        assertThat(questStamp(null, "Something odd", QuestCategory.SKY)).isEqualTo(R.drawable.quest_any_cloud)
    }

    @Test
    fun colorsHasNoStampSoCallersShowItsIcon() {
        assertThat(categoryStamp(QuestCategory.COLORS)).isNull()
        assertThat(questStamp(null, "Find something teal", QuestCategory.COLORS)).isNull()
    }

    @Test
    fun everyCategoryButColorsHasAStamp() {
        for (category in QuestCategory.entries - QuestCategory.COLORS) {
            assertThat(categoryStamp(category)).isNotNull()
        }
    }

    @Test
    fun allCategoryIconsAreDistinct() {
        val icons = QuestCategory.entries.map { categoryIcon(it) }
        assertThat(icons.toSet()).hasSize(QuestCategory.entries.size)
    }
}
