package app.polaroam.ai.vision

/**
 * Plain nouns quests may use as target labels.
 * Every entry must be a lowercase label the ML Kit base image labeler can emit; check additions
 * against https://developers.google.com/ml-kit/vision/image-labeling/label-map before adding.
 */
object LabelVocabulary {

    val labels: Set<String> = setOf(
        // animals
        "bird", "cat", "dog", "duck", "butterfly", "insect", "squirrel", "pet",
        // plants and ground
        "flower", "petal", "plant", "tree", "leaf", "grass", "moss", "mushroom", "fungus",
        "rock", "sand", "snow", "wood", "branch", "forest",
        // water and sky
        "water", "lake", "river", "waterfall", "sky", "cloud", "sunset", "moon", "rainbow",
        // built things
        "building", "bridge", "tower", "statue", "sculpture", "monument", "fountain",
        "stairs", "door", "window", "wall", "brick", "bench",
        // objects
        "lamp", "lantern", "light", "flag", "umbrella", "clock", "chair",
    )

    val synonyms: Map<String, String> = mapOf(
        "puppy" to "dog",
        "kitten" to "cat",
        "kitty" to "cat",
        "pigeon" to "bird",
        "sparrow" to "bird",
        "crow" to "bird",
        "goose" to "duck",
        "bug" to "insect",
        "beetle" to "insect",
        "bee" to "insect",
        "blossom" to "flower",
        "bloom" to "flower",
        "leaves" to "leaf",
        "toadstool" to "mushroom",
        "fungi" to "fungus",
        "stone" to "rock",
        "pebble" to "rock",
        "boulder" to "rock",
        "log" to "wood",
        "timber" to "wood",
        "trunk" to "tree",
        "stream" to "river",
        "creek" to "river",
        "brook" to "river",
        "pond" to "lake",
        "staircase" to "stairs",
        "stairway" to "stairs",
        "steps" to "stairs",
        "lamppost" to "lamp",
        "lamp post" to "lamp",
        "banner" to "flag",
        "doorway" to "door",
    )

    private val whitespace = Regex("\\s+")

    /** Maps a free-form label to its vocabulary form; returns the cleaned input if unknown. */
    fun canonical(raw: String): String {
        val cleaned = raw.trim().lowercase().replace(whitespace, " ")
        resolve(cleaned)?.let { return it }
        for (candidate in singularCandidates(cleaned)) {
            resolve(candidate)?.let { return it }
        }
        return cleaned
    }

    fun isKnown(raw: String): Boolean = canonical(raw) in labels

    private fun resolve(word: String): String? = when {
        word in labels -> word
        else -> synonyms[word]
    }

    private fun singularCandidates(word: String): List<String> = buildList {
        if (word.endsWith("ies") && word.length > 3) add(word.dropLast(3) + "y")
        if (word.endsWith("es") && word.length > 2) add(word.dropLast(2))
        if (word.endsWith("s") && word.length > 1) add(word.dropLast(1))
    }
}
