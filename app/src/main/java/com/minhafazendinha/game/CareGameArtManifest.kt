package com.minhafazendinha.game

/**
 * Art-production manifest shared by every factory-made care game.
 * Turns the visual target into a deterministic export checklist so art can be
 * produced/imported without changing gameplay code.
 */
data class CareArtSlot(
    val key: String,
    val role: String,
    val transparent: Boolean,
    val required: Boolean = true
)

data class CareGameArtManifest(
    val gameId: String,
    val slots: List<CareArtSlot>,
    val referenceStyle: String,
    val qualityChecks: List<String>
) {
    init {
        require(slots.map { it.key }.distinct().size == slots.size) { "Art slot keys must be unique" }
    }

    fun requiredKeys(): Set<String> = slots.filter { it.required }.mapTo(linkedSetOf()) { it.key }
}

object CareGameArtManifestFactory {
    fun create(gameId: String): CareGameArtManifest {
        val pack = CareGamePackFactory.catalog()[gameId]
            ?: error("Unknown care game: $gameId")
        val slots = buildList {
            add(CareArtSlot(pack.assets.scene, "background_scene", transparent = false))
            add(CareArtSlot(pack.assets.idle, "character_idle", transparent = true))
            pack.template.actions.forEach { action ->
                add(CareArtSlot(
                    key = requireNotNull(pack.assets.actionStates[action.id]),
                    role = "character_${action.visualState}",
                    transparent = true
                ))
            }
        }
        return CareGameArtManifest(
            gameId = gameId,
            slots = slots,
            referenceStyle = "polished_3d_cartoon_kids_game",
            qualityChecks = pack.polish.qualityChecks + listOf(
                "transparent_character_edges_clean",
                "character_light_matches_scene",
                "no_text_or_buttons_baked_into_art"
            )
        )
    }

    fun catalog(): Map<String, CareGameArtManifest> =
        CareGamePackFactory.catalog().keys.associateWith(::create)
}

/** Readiness bridge: the exact same manifest that guides exports validates imports. */
fun CareGameAssetReadiness.matches(manifest: CareGameArtManifest): Boolean =
    gameId == manifest.gameId && missing.intersect(manifest.requiredKeys()).isEmpty()
