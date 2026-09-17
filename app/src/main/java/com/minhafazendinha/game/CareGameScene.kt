package com.minhafazendinha.game

/**
 * Reusable scene layer for care games.
 * Couples render plans with accessibility/motion preferences so screens only
 * consume a ready-to-render scene and future games inherit the same polish rules.
 */
data class CareGameScenePreferences(
    val reduceMotion: Boolean = false,
    val largeTouchTargets: Boolean = true
)

data class CareGameScene(
    val gameId: String,
    val state: String,
    val commands: List<CareGameRenderCommand>,
    val touchTargetDp: Int,
    val pressedScale: Float,
    val animated: Boolean
) {
    fun validate(): List<String> = buildList {
        if (gameId.isBlank()) add("scene.game_id")
        if (state.isBlank()) add("scene.state")
        if (commands.isEmpty()) add("scene.commands")
        if (CareVisualLayer.CHARACTER !in commands.map { it.layer }) add("scene.character_layer")
        if (touchTargetDp < 48) add("scene.touch_target")
    }
}

class CareGameSceneFactory(
    private val planner: CareGameRenderPlanner,
    private val preferences: CareGameScenePreferences = CareGameScenePreferences()
) {
    fun idle(): CareGameScene = from(planner.idle())

    fun forAction(actionId: String): CareGameScene = from(planner.forAction(actionId))

    fun forState(state: String): CareGameScene = from(planner.forState(state))

    fun from(plan: CareGameRenderPlan): CareGameScene {
        val commands = if (preferences.reduceMotion) {
            plan.commands.map { it.copy(transitionMs = 0) }
        } else {
            plan.commands
        }
        val scene = CareGameScene(
            gameId = plan.gameId,
            state = plan.state,
            commands = commands,
            touchTargetDp = if (preferences.largeTouchTargets) plan.touchTargetDp.coerceAtLeast(56) else plan.touchTargetDp,
            pressedScale = plan.pressedScale,
            animated = !preferences.reduceMotion && commands.any { it.transitionMs > 0 }
        )
        require(scene.validate().isEmpty()) {
            "Invalid scene for ${plan.gameId}/${plan.state}: ${scene.validate().joinToString()}"
        }
        return scene
    }
}

object CareGameSceneCatalog {
    fun create(
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences()
    ): CareGameSceneFactory = CareGameSceneFactory(
        planner = CareGameRenderPlannerFactory.create(gameId),
        preferences = preferences
    )

    fun catalog(
        preferences: CareGameScenePreferences = CareGameScenePreferences()
    ): Map<String, CareGameSceneFactory> = CareGameRenderPlannerFactory.catalog()
        .mapValues { (_, planner) -> CareGameSceneFactory(planner, preferences) }
}
