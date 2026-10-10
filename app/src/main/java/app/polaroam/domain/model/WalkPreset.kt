package app.polaroam.domain.model

enum class WalkPreset(val label: String, val keywords: Set<String>) {
    NEIGHBORHOOD(
        "Neighborhood",
        setOf("neighborhood", "neighbourhood", "block", "town", "suburb", "village", "home", "downtown", "city"),
    ),
    CITY_PARK(
        "City park",
        setOf("park", "garden", "gardens", "playground", "plaza", "square", "pond", "lawn", "botanical"),
    ),
    FOREST_TRAIL(
        "Forest trail",
        setOf("forest", "trail", "woods", "wood", "woodland", "hike", "hiking", "mountain", "creek", "nature"),
    ),
    NIGHT_WALK(
        "Night walk",
        setOf("night", "evening", "dusk", "dark", "moon", "moonlit", "late", "midnight", "twilight", "nocturnal"),
    );

    companion object {
        private val nonLetters = Regex("[^\\p{L}]+")

        /** Night always wins so mixed inputs like "forest at night" get night-safe quests. */
        fun match(walkType: String): WalkPreset? {
            val normalized = walkType.trim().lowercase()
            entries.firstOrNull { it.label.lowercase() == normalized }?.let { return it }

            val tokens = normalized.split(nonLetters).filter { it.isNotEmpty() }.toSet()
            if (tokens.any { it in NIGHT_WALK.keywords }) return NIGHT_WALK

            return entries
                .filter { it != NIGHT_WALK }
                .map { preset -> preset to tokens.count { it in preset.keywords } }
                .filter { (_, hits) -> hits > 0 }
                .maxByOrNull { (_, hits) -> hits }
                ?.first
        }
    }
}
