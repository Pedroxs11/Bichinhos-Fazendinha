package com.minhafazendinha.game

/**
 * Small DSL used by the care-game factory to define a complete visual pack
 * without duplicating asset naming/wiring in every game.
 */
class CareGameAssetPackBuilder(
    private val gameId: String,
    private val contract: CareAssetContract
) {
    private var background: String = "${gameId}_care_scene"
    private var foreground: String? = "${gameId}_care_foreground"
    private val actionCharacters = linkedMapOf<String, String>()

    fun scene(background: String, foreground: String? = null) = apply {
        this.background = background
        this.foreground = foreground
    }

    fun action(id: String, characterAsset: String) = apply {
        actionCharacters[id.lowercase()] = characterAsset
    }

    fun standardActionCharacters(prefix: String = "${gameId}_care") = apply {
        listOf("feed", "bath", "brush", "play").forEach { action ->
            actionCharacters[action] = "${prefix}_${action}"
        }
    }

    fun build(): CareGameAssetPack = CareGameAssetPack(
        gameId = gameId,
        contract = contract,
        background = background,
        foreground = foreground,
        actionCharacters = actionCharacters.toMap()
    )
}

fun careGameAssetPack(
    gameId: String,
    contract: CareAssetContract,
    configure: CareGameAssetPackBuilder.() -> Unit
): CareGameAssetPack = CareGameAssetPackBuilder(gameId, contract).apply(configure).build()
