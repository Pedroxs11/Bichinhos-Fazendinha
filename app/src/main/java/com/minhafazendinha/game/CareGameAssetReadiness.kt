package com.minhafazendinha.game

import android.content.Context

/**
 * Runtime production gate for factory-made games.
 * Prevents a screen from switching to the generic renderer before its real
 * layered artwork exists, while reporting exactly what the art pass still needs.
 */
data class CareGameAssetReadiness(
    val gameId: String,
    val available: Set<String>,
    val missing: Set<String>
) {
    val ready: Boolean get() = missing.isEmpty()
    val progress: Float get() {
        val total = available.size + missing.size
        return if (total == 0) 1f else available.size.toFloat() / total
    }
}

class CareGameAssetReadinessChecker(private val context: Context) {
    fun check(gameId: String): CareGameAssetReadiness {
        val pack = CareGamePackFactory.catalog()[gameId]
            ?: error("Unknown care game: $gameId")
        val required = buildSet {
            add(pack.assets.scene)
            add(pack.assets.idle)
            addAll(pack.assets.actionStates.values)
        }
        val available = required.filterTo(linkedSetOf(), ::exists)
        return CareGameAssetReadiness(
            gameId = gameId,
            available = available,
            missing = required - available
        )
    }

    private fun exists(key: String): Boolean =
        context.resources.getIdentifier(key, "drawable", context.packageName) != 0
}

/**
 * Production-screen factory with a safe readiness gate. Existing previews can
 * remain live until all final layers/states are delivered; then the same screen
 * automatically becomes eligible for the shared renderer.
 */
object CareGameProductionGate {
    fun readiness(context: Context, gameId: String): CareGameAssetReadiness =
        CareGameAssetReadinessChecker(context).check(gameId)

    fun createIfReady(
        context: Context,
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming(),
        onSnapshot: (CareGameInteractionSnapshot) -> Unit = {}
    ): CareGameProductionScreen? =
        if (readiness(context, gameId).ready) {
            CareGameProductionScreenFactory.create(
                context = context,
                gameId = gameId,
                preferences = preferences,
                timing = timing,
                onSnapshot = onSnapshot
            )
        } else null
}
