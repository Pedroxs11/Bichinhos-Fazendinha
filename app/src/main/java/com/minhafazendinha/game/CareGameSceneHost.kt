package com.minhafazendinha.game

import android.content.Context
import android.widget.FrameLayout

/**
 * One-call host for every care-game scene.
 * New games only provide a gameId; the host wires the visual spec, scene factory
 * and shared renderer so screens do not repeat production/polish plumbing.
 */
class CareGameSceneHost(
    context: Context,
    private val gameId: String,
    preferences: CareGameScenePreferences = CareGameScenePreferences()
) : FrameLayout(context) {
    private val pack = requireNotNull(CareGamePackFactory.catalog()[gameId]) {
        "Unknown care game: $gameId"
    }
    private val visualSpec = CareGameVisualSpecFactory.build(pack)
    private val scenes = CareGameSceneCatalog.create(gameId, preferences)
    private val renderer = CareGameSceneRenderer(context, this, visualSpec)

    init {
        clipChildren = false
        clipToPadding = false
        showIdle()
    }

    fun showIdle() = render(scenes.idle())

    fun showAction(actionId: String) = render(scenes.forAction(actionId))

    fun showState(state: String) = render(scenes.forState(state))

    fun clearScene() = renderer.clear()

    fun visualSpec(): CareGameVisualSpec = visualSpec

    private fun render(scene: CareGameScene) {
        require(scene.gameId == gameId) {
            "Scene ${scene.gameId} cannot be rendered by host $gameId"
        }
        renderer.render(scene)
    }
}

/** Factory used by future games/screens to inherit the complete visual pipeline. */
object CareGameSceneHostFactory {
    fun create(
        context: Context,
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences()
    ): CareGameSceneHost = CareGameSceneHost(context, gameId, preferences)
}
