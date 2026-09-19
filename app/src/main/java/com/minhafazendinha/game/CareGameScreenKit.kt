package com.minhafazendinha.game

import android.content.Context

/**
 * One-call screen kit for future care games.
 * Bundles the production scene host and its controller so a new animal screen
 * only needs a gameId and can immediately render idle/action/state visuals.
 */
data class CareGameScreenKit(
    val host: CareGameSceneHost,
    val controller: CareGameSceneHostController,
    val gameId: String
) {
    fun idle() = controller.idle()

    fun perform(actionId: String) = controller.perform(actionId)

    fun restore(state: String?) = controller.restore(state)

    fun visualSpec(): CareGameVisualSpec = host.visualSpec()
}

object CareGameScreenKitFactory {
    fun create(
        context: Context,
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences()
    ): CareGameScreenKit {
        require(CareGamePackFactory.catalog().containsKey(gameId)) {
            "Unknown care game: $gameId"
        }
        val host = CareGameSceneHostFactory.create(context, gameId, preferences)
        val controller = CareGameSceneHostControllerFactory.create(host, gameId)
        return CareGameScreenKit(
            host = host,
            controller = controller,
            gameId = gameId
        )
    }
}
