package com.myplanetservices.stillwords.data.model

/**
 * Single source of truth for Background Theme to Category mapping.
 * Easy to edit and extend in one place.
 *
 * Mappings:
 * - sunrise-roads: Motivation, Success, Discipline, Fitness, Work
 * - mountains: Courage, Confidence, Leadership, Growth
 * - still-water: Peace, Mindfulness, Healing
 * - forest-botanical: Life, Gratitude, Self-Love
 * - golden-hour: Love, Relationships, Friendship, Happiness
 * - night-sky: Wisdom, Creativity
 * - quiet-spaces: Study
 * Any category not listed falls back to the existing gradient styles.
 */
enum class BackgroundTheme(
    val id: String,
    val displayName: String,
    val categories: List<String>,
    val isPremium: Boolean = false
) {
    SUNRISE_ROADS(
        id = "sunrise-roads",
        displayName = "Sunrise Roads",
        categories = listOf("Motivation", "Success", "Discipline", "Fitness", "Work"),
        isPremium = false
    ),
    MOUNTAINS(
        id = "mountains",
        displayName = "Mountains",
        categories = listOf("Courage", "Confidence", "Leadership", "Growth"),
        isPremium = false
    ),
    STILL_WATER(
        id = "still-water",
        displayName = "Still Water",
        categories = listOf("Peace", "Mindfulness", "Healing"),
        isPremium = false
    ),
    FOREST_BOTANICAL(
        id = "forest-botanical",
        displayName = "Forest & Botanical",
        categories = listOf("Life", "Gratitude", "Self-Love"),
        isPremium = true
    ),
    GOLDEN_HOUR(
        id = "golden-hour",
        displayName = "Golden Hour",
        categories = listOf("Love", "Relationships", "Friendship", "Happiness"),
        isPremium = false
    ),
    NIGHT_SKY(
        id = "night-sky",
        displayName = "Night Sky",
        categories = listOf("Wisdom", "Creativity"),
        isPremium = false
    ),
    QUIET_SPACES(
        id = "quiet-spaces",
        displayName = "Quiet Spaces",
        categories = listOf("Study"),
        isPremium = true
    );

    companion object {
        private val categoryMap: Map<String, BackgroundTheme> = entries.flatMap { theme ->
            theme.categories.map { it.trim().lowercase() to theme }
        }.toMap()

        /**
         * Resolves the BackgroundTheme for a given category name (case-insensitive).
         * Returns null if category is unmapped, triggering fallback to gradient styles.
         */
        fun fromCategory(category: String): BackgroundTheme? {
            return categoryMap[category.trim().lowercase()]
        }

        /**
         * Resolves by theme directory id (e.g. "sunrise-roads").
         */
        fun fromId(id: String): BackgroundTheme? {
            return entries.firstOrNull { it.id.equals(id.trim(), ignoreCase = true) }
        }
    }
}
