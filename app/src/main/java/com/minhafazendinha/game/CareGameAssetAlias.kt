package com.minhafazendinha.game

/**
 * Lets a factory game adopt approved preview artwork immediately while final
 * production exports are delivered slot-by-slot. This keeps the generic
 * renderer usable without renaming legacy assets or coupling it to Mimosa.
 */
data class CareGameAssetAlias(
    val gameId: String,
    val aliases: Map<String, List<String>>
) {
    fun candidates(key: String): List<String> =
        (listOf(key) + aliases[key].orEmpty()).distinct()
}

object CareGameAssetAliasCatalog {
    private val farm = CareGameAssetAlias(
        gameId = "farm_care",
        aliases = mapOf(
            "farm_care_idle" to listOf("mimosa_character_idle"),
            "farm_care_feed" to listOf("mimosa_character_feed", "mimosa_character_layer_feed"),
            "farm_care_bath" to listOf("mimosa_character_bath", "mimosa_character_layer_bath"),
            "farm_care_brush" to listOf("mimosa_character_brush", "mimosa_character_layer_brush"),
            "farm_care_play" to listOf("mimosa_character_play", "mimosa_character_layer_play"),
            "farm_care_scene" to listOf("mimosa_farm_background")
        )
    )

    private val catalog = mapOf(farm.gameId to farm)

    fun candidates(gameId: String, key: String): List<String> =
        catalog[gameId]?.candidates(key) ?: listOf(key)
}
