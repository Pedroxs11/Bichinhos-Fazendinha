package com.minhafazendinha.game

/**
 * Small reusable controller that turns care-game actions into scene-host updates.
 * Screens can now bind buttons/input to action ids without knowing about scene
 * factories, render plans or visual state names. Future games inherit this flow.
 */
class CareGameSceneHostController(
    private val host: CareGameSceneHost,
    private val actionIds: Set<String>
) {
    private var activeAction: String? = null

    fun idle() {
        activeAction = null
        host.showIdle()
    }

    fun perform(actionId: String) {
        require(actionId in actionIds) { "Unknown care action: $actionId" }
        activeAction = actionId
        host.showAction(actionId)
    }

    fun restore(state: String?) {
        if (state.isNullOrBlank() || state == "idle") {
            idle()
        } else {
            activeAction = null
            host.showState(state)
        }
    }

    fun activeAction(): String? = activeAction
}

object CareGameSceneHostControllerFactory {
    fun create(host: CareGameSceneHost, gameId: String): CareGameSceneHostController {
        val pack = requireNotNull(CareGamePackFactory.catalog()[gameId]) {
            "Unknown care game: $gameId"
        }
        return CareGameSceneHostController(
            host = host,
            actionIds = pack.template.actions.map { it.id }.toSet()
        )
    }
}
