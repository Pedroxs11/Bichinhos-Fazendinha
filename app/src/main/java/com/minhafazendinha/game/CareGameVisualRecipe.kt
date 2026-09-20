package com.minhafazendinha.game

/**
 * Visual-polish recipe consumed by every care game.
 * Keeps animation/timing decisions out of game-specific screens so a new
 * animal can inherit a production-ready feel by only supplying an asset pack.
 */
data class CareGameVisualRecipe(
    val gameId: String,
    val enterDurationMs: Long = 260,
    val actionPulseScale: Float = 1.035f,
    val actionPulseInMs: Long = 120,
    val actionPulseOutMs: Long = 150,
    val propHoldMs: Long = 420,
    val effectHoldMs: Long = 520,
    val idleReturnDelayMs: Long = 120,
    val characterIdleAsset: String,
    val backgroundAsset: String,
    val foregroundAsset: String?,
    val actions: Map<String, CareActionVisualRecipe>
) {
    fun action(actionId: String): CareActionVisualRecipe? = actions[actionId.lowercase()]

    companion object {
        fun from(pack: CareGameAssetPack): CareGameVisualRecipe {
            val ids = listOf("feed", "bathe", "brush", "play")
            return CareGameVisualRecipe(
                gameId = pack.gameId,
                characterIdleAsset = pack.contract.characterIdle,
                backgroundAsset = pack.background,
                foregroundAsset = pack.foreground,
                actions = ids.associateWith { id ->
                    CareActionVisualRecipe(
                        actionId = id,
                        characterAsset = pack.characterFor(id),
                        propAsset = pack.propFor(id),
                        effectAsset = pack.effectFor(id)
                    )
                }
            )
        }
    }
}

data class CareActionVisualRecipe(
    val actionId: String,
    val characterAsset: String,
    val propAsset: String?,
    val effectAsset: String?
)

/** Single factory entry point for runtime and future visual tooling. */
object CareGameVisualFactory {
    fun recipe(gameId: String): CareGameVisualRecipe? =
        CareGameRegistry.assetPack(gameId)?.let(CareGameVisualRecipe::from)

    fun mimosa(): CareGameVisualRecipe = CareGameVisualRecipe.from(CareGameAssetPack.MIMOSA)
}
