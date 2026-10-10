package app.polaroam.ai.canned

import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.Difficulty.EASY
import app.polaroam.domain.model.Difficulty.HARD
import app.polaroam.domain.model.Difficulty.MEDIUM
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestCategory
import app.polaroam.domain.model.QuestCategory.ANIMALS
import app.polaroam.domain.model.QuestCategory.ARCHITECTURE
import app.polaroam.domain.model.QuestCategory.NATURE
import app.polaroam.domain.model.QuestCategory.OBJECTS
import app.polaroam.domain.model.QuestCategory.SKY
import app.polaroam.domain.model.QuestCategory.TEXTURES
import app.polaroam.domain.model.WalkPreset
import app.polaroam.domain.model.WalkPreset.CITY_PARK
import app.polaroam.domain.model.WalkPreset.FOREST_TRAIL
import app.polaroam.domain.model.WalkPreset.NEIGHBORHOOD
import app.polaroam.domain.model.WalkPreset.NIGHT_WALK

/** A built-in quest. Empty [presets] means it suits any daytime walk. */
data class PoolQuest(
    val key: String,
    val title: String,
    val objective: String,
    val category: QuestCategory,
    val difficulty: Difficulty,
    val targetLabels: List<String>,
    val presets: Set<WalkPreset> = emptySet(),
) {
    fun toQuest(id: String) = Quest(
        id = id,
        title = title,
        objective = objective,
        category = category,
        difficulty = difficulty,
        targetLabels = targetLabels,
    )
}

object CannedQuestPool {

    val all: List<PoolQuest> = listOf(
        // Neighborhood
        PoolQuest(
            "hood-door", "Painted Door",
            "Photograph a colorful front door you can see from the public path.",
            ARCHITECTURE, EASY, listOf("door", "building"), setOf(NEIGHBORHOOD),
        ),
        PoolQuest(
            "hood-cat", "Feline Patrol",
            "Spot a cat lounging somewhere sunny. Keep your distance and let it nap.",
            ANIMALS, MEDIUM, listOf("cat", "pet"), setOf(NEIGHBORHOOD),
        ),
        PoolQuest(
            "hood-brick", "Brick Pattern",
            "Find a wall with an interesting brick or stone pattern.",
            TEXTURES, EASY, listOf("brick", "wall"), setOf(NEIGHBORHOOD),
        ),
        PoolQuest(
            "hood-sky", "Sky Slice",
            "Frame a patch of sky squeezed between two buildings.",
            SKY, EASY, listOf("sky", "cloud", "building"), setOf(NEIGHBORHOOD),
        ),
        PoolQuest(
            "hood-jungle", "Tiny Jungle",
            "Find a plant growing out of a crack in the pavement or a wall.",
            NATURE, MEDIUM, listOf("plant", "grass", "moss", "leaf"), setOf(NEIGHBORHOOD),
        ),

        // City park
        PoolQuest(
            "park-duck", "Duck Diplomacy",
            "Photograph a duck or another water bird from the shore. No feeding needed.",
            ANIMALS, MEDIUM, listOf("duck", "bird"), setOf(CITY_PARK),
        ),
        PoolQuest(
            "park-bench", "Bench Story",
            "Find a park bench with character: old wood, a plaque, or a great view.",
            OBJECTS, EASY, listOf("bench", "wood"), setOf(CITY_PARK),
        ),
        PoolQuest(
            "park-fountain", "Fountain Hunt",
            "Track down a fountain or any moving water in the park.",
            ARCHITECTURE, MEDIUM, listOf("fountain", "water"), setOf(CITY_PARK),
        ),
        PoolQuest(
            "park-statue", "Statue Stare",
            "Find a statue or sculpture and capture its best side.",
            ARCHITECTURE, MEDIUM, listOf("statue", "sculpture", "monument"), setOf(CITY_PARK),
        ),
        PoolQuest(
            "park-flower", "Flower Bed",
            "Photograph a flower in full bloom. Bonus style points for visiting bees.",
            NATURE, EASY, listOf("flower", "petal"), setOf(CITY_PARK),
        ),
        PoolQuest(
            "park-critter", "Critter Watch",
            "Catch a squirrel or bird foraging. Patience wins this one.",
            ANIMALS, HARD, listOf("squirrel", "bird"), setOf(CITY_PARK, FOREST_TRAIL),
        ),

        // Forest trail
        PoolQuest(
            "forest-fungi", "Fungi Finder",
            "Spot a mushroom or bracket fungus. Look and photograph, never pick or taste.",
            NATURE, HARD, listOf("mushroom", "fungus"), setOf(FOREST_TRAIL),
        ),
        PoolQuest(
            "forest-moss", "Moss Carpet",
            "Find a rock or fallen log wrapped in bright green moss.",
            TEXTURES, MEDIUM, listOf("moss", "rock", "wood"), setOf(FOREST_TRAIL),
        ),
        PoolQuest(
            "forest-giant", "Old Giant",
            "Photograph the widest tree trunk you can find from the trail.",
            NATURE, EASY, listOf("tree", "wood", "forest"), setOf(FOREST_TRAIL),
        ),
        PoolQuest(
            "forest-leaf", "Leaf Peeper",
            "Find a single leaf with a striking color or shape.",
            NATURE, EASY, listOf("leaf", "plant"), setOf(FOREST_TRAIL),
        ),
        PoolQuest(
            "forest-water", "Trail Water",
            "Capture a stream or waterfall seen safely from the trail.",
            NATURE, MEDIUM, listOf("river", "waterfall", "water"), setOf(FOREST_TRAIL),
        ),
        PoolQuest(
            "forest-wing", "Forest Wings",
            "Spot a bird or butterfly among the trees.",
            ANIMALS, HARD, listOf("bird", "butterfly", "insect"), setOf(FOREST_TRAIL),
        ),

        // Night walk: only well-lit, open, public spots
        PoolQuest(
            "night-moon", "Moon Check",
            "Find the moon from a well-lit, open spot and frame it.",
            SKY, MEDIUM, listOf("moon", "sky"), setOf(NIGHT_WALK),
        ),
        PoolQuest(
            "night-lamp", "Lamp Glow",
            "Photograph a glowing lamp or lantern along a well-lit path.",
            OBJECTS, EASY, listOf("lamp", "lantern", "light"), setOf(NIGHT_WALK),
        ),
        PoolQuest(
            "night-bloom", "Night Bloom",
            "Find a flower or plant lit up by a nearby light.",
            NATURE, MEDIUM, listOf("flower", "plant"), setOf(NIGHT_WALK),
        ),
        PoolQuest(
            "night-silhouette", "Tree Silhouette",
            "Capture a tree outline against the night sky from a lit path.",
            NATURE, MEDIUM, listOf("tree", "sky"), setOf(NIGHT_WALK),
        ),
        PoolQuest(
            "night-building", "Glowing Facade",
            "Photograph a building with lit-up windows from a public square.",
            ARCHITECTURE, EASY, listOf("building", "window", "tower"), setOf(NIGHT_WALK),
        ),
        PoolQuest(
            "night-cat", "Night Prowler",
            "Spot a cat on a doorstep or wall, seen from a well-lit path.",
            ANIMALS, HARD, listOf("cat", "pet"), setOf(NIGHT_WALK),
        ),

        // Any daytime walk
        PoolQuest(
            "any-cloud", "Cloud Shapes",
            "Photograph a cloud that looks like something else.",
            SKY, EASY, listOf("cloud", "sky"),
        ),
        PoolQuest(
            "any-rock", "Rock Collector",
            "Find a rock with an interesting shape, color or pattern.",
            TEXTURES, EASY, listOf("rock", "sand"),
        ),
        PoolQuest(
            "any-bird", "Feathered Friend",
            "Spot any bird, perched or in flight.",
            ANIMALS, MEDIUM, listOf("bird", "duck"),
        ),
        PoolQuest(
            "any-tree", "Green Giant",
            "Find a tree taller than the buildings around it.",
            NATURE, EASY, listOf("tree", "plant"),
        ),
        PoolQuest(
            "any-flower", "Petal Power",
            "Find a flower you have never noticed before.",
            NATURE, EASY, listOf("flower", "petal"),
        ),
        PoolQuest(
            "any-bridge", "Bridge Spotter",
            "Find a footbridge or stone bridge and capture its shape.",
            ARCHITECTURE, MEDIUM, listOf("bridge", "water"),
        ),
        PoolQuest(
            "any-stairs", "Up the Stairs",
            "Photograph a set of outdoor stairs with an interesting shape.",
            ARCHITECTURE, MEDIUM, listOf("stairs", "building"),
        ),
        PoolQuest(
            "any-flag", "Flag Flutter",
            "Find a flag catching the wind.",
            OBJECTS, MEDIUM, listOf("flag", "sky"),
        ),
        PoolQuest(
            "any-rainbow", "Rainbow Chaser",
            "Catch a rainbow, or any splash of rainbow light, after the rain.",
            SKY, HARD, listOf("rainbow", "sky"),
        ),
    )
}
