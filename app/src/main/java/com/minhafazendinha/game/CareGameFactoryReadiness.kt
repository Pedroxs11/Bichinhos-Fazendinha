package com.minhafazendinha.game

import android.content.Context

/**
 * One reusable production report for the whole care-game factory.
 * New games automatically participate as soon as they are added to the catalog,
 * giving art/QA a deterministic queue instead of game-specific readiness glue.
 */
data class CareGameFactoryReadiness(
    val games: List<CareGameLaunchKit>
) {
    val readyGames: List<CareGameLaunchKit> get() = games.filter { it.productionReady }
    val blockedGames: List<CareGameLaunchKit> get() = games.filterNot { it.productionReady }
    val productionReady: Boolean get() = blockedGames.isEmpty()
    val progress: Float get() = if (games.isEmpty()) 1f else games.map { it.progress }.average().toFloat()

    /** Prioritizes the games closest to production so an art pass can unlock playable content faster. */
    val artQueue: List<CareGameLaunchKit>
        get() = blockedGames.sortedWith(compareByDescending<CareGameLaunchKit> { it.progress }.thenBy { it.gameId })

    val missingAssets: Set<String>
        get() = games.flatMapTo(linkedSetOf()) { it.missingAssets }
}

object CareGameFactoryReadinessFactory {
    fun create(
        context: Context,
        preferences: CareGameScenePreferences = CareGameScenePreferences()
    ): CareGameFactoryReadiness {
        val games = CareGamePackFactory.catalog().keys
            .sorted()
            .map { gameId -> CareGameLaunchKitFactory.create(context, gameId, preferences) }
        return CareGameFactoryReadiness(games)
    }
}
