package com.minhafazendinha.game

/**
 * Reusable visual pack for care games.
 * A new game only needs to provide an asset contract plus scene layers;
 * runtime screens can consume the same structure without game-specific names.
 */
data class CareGameAssetPack(
    val gameId: String,
    val contract: CareAssetContract,
    val background: String,
    val foreground: String? = null,
    val actionCharacters: Map<String, String> = emptyMap()
) {
    fun characterFor(actionId: String?): String =
        actionId?.lowercase()?.let(actionCharacters::get) ?: contract.characterIdle

    fun propFor(actionId: String): String? = contract.assetFor(actionId)

    fun effectFor(actionId: String): String? = contract.effectFor(actionId)

    fun requiredDrawables(): Set<String> = buildSet {
        add(background)
        addAll(contract.requiredAssets())
        foreground?.let(::add)
        addAll(actionCharacters.values)
    }

    fun missingDrawables(available: Set<String>): Set<String> =
        requiredDrawables().filterNot(available::contains).toSet()

    fun readiness(available: Set<String>): CareGameAssetPackReadiness {
        val missing = missingDrawables(available)
        return CareGameAssetPackReadiness(
            gameId = gameId,
            ready = missing.isEmpty(),
            required = requiredDrawables().size,
            available = requiredDrawables().count(available::contains),
            missing = missing
        )
    }

    companion object {
        val MIMOSA = CareGameAssetPack(
            gameId = "mimosa",
            contract = CareAssetContract.MIMOSA,
            background = "farm_care_scene",
            foreground = "farm_care_foreground",
            actionCharacters = mapOf(
                "feed" to "farm_care_feed",
                "bath" to "farm_care_bath",
                "brush" to "farm_care_brush",
                "play" to "farm_care_play"
            )
        )
    }
}

data class CareGameAssetPackReadiness(
    val gameId: String,
    val ready: Boolean,
    val required: Int,
    val available: Int,
    val missing: Set<String>
) {
    val progress: Int
        get() = if (required == 0) 100 else ((available * 100f) / required).toInt()
}
