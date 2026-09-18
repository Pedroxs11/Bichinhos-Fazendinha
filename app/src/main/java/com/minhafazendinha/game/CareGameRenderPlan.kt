package com.minhafazendinha.game

/** Stable render commands shared by every care-game screen. */
data class CareGameRenderCommand(
    val layer: CareVisualLayer,
    val drawableKey: String?,
    val anchorX: Float,
    val anchorY: Float,
    val scale: Float,
    val transitionMs: Int
)

data class CareGameRenderPlan(
    val gameId: String,
    val state: String,
    val commands: List<CareGameRenderCommand>,
    val touchTargetDp: Int,
    val pressedScale: Float
) {
    fun validate(): List<String> = buildList {
        if (gameId.isBlank()) add("render.game_id")
        if (state.isBlank()) add("render.state")
        if (commands.isEmpty()) add("render.commands")
        if (commands.map { it.layer }.distinct().size != commands.size) add("render.duplicate_layers")
        if (CareVisualLayer.CHARACTER !in commands.map { it.layer }) add("render.character_layer")
        if (touchTargetDp < 48) add("render.touch_target")
    }
}

class CareGameRenderPlanner(private val runtime: CareGameVisualRuntime) {
    fun idle(): CareGameRenderPlan = from(runtime.idle())
    fun forAction(actionId: String): CareGameRenderPlan = from(runtime.forAction(actionId))
    fun forState(state: String): CareGameRenderPlan = from(runtime.resolve(state))

    fun from(visual: CareGameVisualState): CareGameRenderPlan {
        val commands = visual.layers.map { layer ->
            val drawable = when (layer) {
                CareVisualLayer.BACKGROUND -> visual.sceneDrawableKey
                CareVisualLayer.CHARACTER -> visual.drawableKey
                else -> null
            }
            CareGameRenderCommand(
                layer = layer,
                drawableKey = drawable,
                anchorX = if (layer == CareVisualLayer.CHARACTER) visual.anchorX else .5f,
                anchorY = if (layer == CareVisualLayer.CHARACTER) visual.anchorY else .5f,
                scale = if (layer == CareVisualLayer.CHARACTER) visual.scale else 1f,
                transitionMs = if (layer == CareVisualLayer.CHARACTER) visual.transitionMs else 0
            )
        }
        return CareGameRenderPlan(
            gameId = visual.gameId,
            state = visual.state,
            commands = commands,
            touchTargetDp = visual.touchTargetDp,
            pressedScale = visual.pressedScale
        ).also { plan ->
            require(plan.validate().isEmpty()) {
                "Invalid render plan for ${visual.gameId}/${visual.state}: ${plan.validate().joinToString()}"
            }
        }
    }
}

object CareGameRenderPlannerFactory {
    fun create(gameId: String): CareGameRenderPlanner = CareGameRenderPlanner(CareGameVisualRuntimeFactory.create(gameId))
    fun catalog(): Map<String, CareGameRenderPlanner> =
        CareGameVisualRuntimeFactory.catalog().mapValues { (_, runtime) -> CareGameRenderPlanner(runtime) }
}
