package app.polaroam.ui.board

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.polaroam.R
import app.polaroam.domain.model.QuestCategory
import app.polaroam.ui.components.PixelArt
import app.polaroam.ui.theme.PolaroamTheme

/**
 * The 32px stamp for a quest: built-in quests by their pool key, quests the model wrote by
 * keywords in the title, then by category. Null when nothing fits (Colors has no stamp).
 */
@DrawableRes
fun questStamp(poolKey: String?, title: String, category: QuestCategory): Int? {
    poolKey?.let { poolStamps[it] }?.let { return it }

    val normalized = title.trim().lowercase()
    val tokens = normalized.split(nonWordRegex).filter { it.isNotEmpty() }
    for ((keywords, drawable) in keywordStamps) {
        if (keywords.any { kw -> matchesKeyword(normalized, tokens, kw) }) {
            return drawable
        }
    }

    return categoryStamp(category)
}

/** Draws [questStamp] at 64dp, or the 16px category icon at the same size when there is no stamp. */
@Composable
fun QuestStamp(poolKey: String?, title: String, category: QuestCategory, modifier: Modifier = Modifier) {
    val pixel = PolaroamTheme.dimens.pixel
    val stamp = questStamp(poolKey, title, category)
    if (stamp != null) {
        PixelArt(id = stamp, contentDescription = null, modifier = modifier, pixelSize = pixel)
    } else {
        PixelArt(id = categoryIcon(category), contentDescription = null, modifier = modifier, pixelSize = pixel * 2)
    }
}

private fun matchesKeyword(normalized: String, tokens: List<String>, keyword: String): Boolean {
    if (keyword.contains(' ')) {
        return Regex("\\b${Regex.escape(keyword)}(?:s|es)?\\b").containsMatchIn(normalized)
    }
    return tokens.any { token ->
        token == keyword || token == "${keyword}s" || token == "${keyword}es"
    }
}

/** The 16px icon of [category]. */
@DrawableRes
fun categoryIcon(category: QuestCategory): Int = categoryArt(category).icon

/** The fallback 32px stamp of [category]; null for Colors, which has none. */
@DrawableRes
fun categoryStamp(category: QuestCategory): Int? = categoryArt(category).stamp

private class CategoryArt(@DrawableRes val icon: Int, @DrawableRes val stamp: Int?)

private fun categoryArt(category: QuestCategory): CategoryArt = when (category) {
    QuestCategory.NATURE -> CategoryArt(R.drawable.icon_cat_nature, R.drawable.quest_any_tree)
    QuestCategory.ANIMALS -> CategoryArt(R.drawable.icon_cat_animals, R.drawable.quest_any_bird)
    QuestCategory.ARCHITECTURE -> CategoryArt(R.drawable.icon_cat_architecture, R.drawable.quest_any_bridge)
    QuestCategory.TEXTURES -> CategoryArt(R.drawable.icon_cat_textures, R.drawable.quest_any_rock)
    QuestCategory.COLORS -> CategoryArt(R.drawable.icon_cat_colors, stamp = null)
    QuestCategory.OBJECTS -> CategoryArt(R.drawable.icon_cat_objects, R.drawable.quest_any_flag)
    QuestCategory.SKY -> CategoryArt(R.drawable.icon_cat_sky, R.drawable.quest_any_cloud)
}

private val nonWordRegex = Regex("[^\\p{L}]+")

/** Keyed by `PoolQuest.key`; each stamp was drawn for that quest. */
private val poolStamps: Map<String, Int> = mapOf(
    "hood-door" to R.drawable.quest_hood_door,
    "hood-cat" to R.drawable.quest_hood_cat,
    "hood-brick" to R.drawable.quest_hood_brick,
    "hood-sky" to R.drawable.quest_hood_sky,
    "hood-jungle" to R.drawable.quest_hood_jungle,
    "park-duck" to R.drawable.quest_park_duck,
    "park-bench" to R.drawable.quest_park_bench,
    "park-fountain" to R.drawable.quest_park_fountain,
    "park-statue" to R.drawable.quest_park_statue,
    "park-flower" to R.drawable.quest_park_flower,
    "park-critter" to R.drawable.quest_park_critter,
    "forest-fungi" to R.drawable.quest_forest_fungi,
    "forest-moss" to R.drawable.quest_forest_moss,
    "forest-giant" to R.drawable.quest_forest_giant,
    "forest-leaf" to R.drawable.quest_forest_leaf,
    "forest-water" to R.drawable.quest_forest_water,
    "forest-wing" to R.drawable.quest_forest_wing,
    "night-moon" to R.drawable.quest_night_moon,
    "night-lamp" to R.drawable.quest_night_lamp,
    "night-bloom" to R.drawable.quest_night_bloom,
    "night-silhouette" to R.drawable.quest_night_silhouette,
    "night-building" to R.drawable.quest_night_building,
    "night-cat" to R.drawable.quest_night_cat,
    "any-cloud" to R.drawable.quest_any_cloud,
    "any-rock" to R.drawable.quest_any_rock,
    "any-bird" to R.drawable.quest_any_bird,
    "any-tree" to R.drawable.quest_any_tree,
    "any-flower" to R.drawable.quest_any_flower,
    "any-bridge" to R.drawable.quest_any_bridge,
    "any-stairs" to R.drawable.quest_any_stairs,
    "any-flag" to R.drawable.quest_any_flag,
    "any-rainbow" to R.drawable.quest_any_rainbow,
    "coast-wave" to R.drawable.quest_coast_wave,
    "coast-shell" to R.drawable.quest_coast_shell,
    "rain-umbrella" to R.drawable.quest_rain_umbrella,
    "mountain-peak" to R.drawable.quest_mountain_peak,
)

/** Checked in order, so specific subjects win over materials like brick or stone. */
private val keywordStamps: List<Pair<List<String>, Int>> = listOf(
    listOf("wave", "ocean", "surf", "beach", "sea", "tide") to R.drawable.quest_coast_wave,
    listOf("shell", "seashell", "clam") to R.drawable.quest_coast_shell,
    listOf("umbrella", "rain", "puddle", "shower", "storm") to R.drawable.quest_rain_umbrella,
    listOf("mountain", "peak", "summit", "ridge", "cliff", "alpine") to R.drawable.quest_mountain_peak,
    listOf("bridge") to R.drawable.quest_any_bridge,
    listOf("stairs", "steps", "stairway") to R.drawable.quest_any_stairs,
    listOf("statue", "sculpture", "monument") to R.drawable.quest_park_statue,
    listOf("fountain") to R.drawable.quest_park_fountain,
    listOf("bench", "seat") to R.drawable.quest_park_bench,
    listOf("door") to R.drawable.quest_hood_door,
    listOf("lamp", "lantern", "streetlight", "light post") to R.drawable.quest_night_lamp,
    listOf("flag", "banner") to R.drawable.quest_any_flag,
    listOf("rainbow") to R.drawable.quest_any_rainbow,

    listOf("duck", "goose", "waterfowl") to R.drawable.quest_park_duck,
    listOf("cat", "feline", "kitten") to R.drawable.quest_hood_cat,
    listOf("critter", "squirrel", "rodent") to R.drawable.quest_park_critter,
    listOf("bird", "feather", "avian", "pigeon", "crow", "sparrow") to R.drawable.quest_any_bird,

    listOf("fungi", "mushroom", "toadstool") to R.drawable.quest_forest_fungi,
    listOf("moss", "lichen") to R.drawable.quest_forest_moss,
    listOf("flower", "bloom", "petal", "blossom") to R.drawable.quest_park_flower,
    listOf("leaf", "leaves", "foliage") to R.drawable.quest_forest_leaf,
    listOf("stream", "creek", "waterfall", "river", "pond", "water") to R.drawable.quest_forest_water,
    listOf("tree", "trunk", "branch") to R.drawable.quest_forest_giant,

    listOf("moon", "lunar") to R.drawable.quest_night_moon,
    listOf("cloud") to R.drawable.quest_any_cloud,
    listOf("sky") to R.drawable.quest_hood_sky,

    listOf("building", "facade", "window", "architecture") to R.drawable.quest_night_building,

    listOf("brick", "masonry") to R.drawable.quest_hood_brick,
    listOf("rock", "stone", "pebble", "boulder") to R.drawable.quest_any_rock,
)
