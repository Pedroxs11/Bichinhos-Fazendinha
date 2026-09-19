package com.minhafazendinha.game

import android.content.Context

/**
 * Reusable launch decision for every care game.
 * Centralizes asset readiness + screen wiring so future animal games can move
 * from preview artwork to the shared production renderer without custom glue.
 */
data class CareGameLaunchKit(
    val gameId: String,
    val readiness: CareGameAssetReadiness,
    val screenKit: CareGameScreenKit?
) {
    val productionReady: Boolean get() = readiness.ready && screenKit != null
    val missingAssets: Set<String> get() = readiness.missing
    val progress: Float get() = readiness.progress
}

object CareGameLaunchKitFactory {
    fun create(
        context: Context,
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences()
    ): CareGameLaunchKit {
        require(CareGamePackFactory.catalog().containsKey(gameId)) {
            "Unknown care game: $gameId"
        }
        val readiness = CareGameProductionGate.readiness(context, gameId)
        return CareGameLaunchKit(
            gameId = gameId,
            readiness = readiness,
            screenKit = if (readiness.ready) {
                CareGameScreenKitFactory.create(context, gameId, preferences)
            } else null
        )
    }

    /** Useful for dashboards/build checks: one call reports every game's art status. */
    fun catalog(context: Context): Map<String, CareGameLaunchKit> =
        CareGamePackFactory.catalog().keys.associateWith { gameId ->
            create(context, gameId)
        }
}
