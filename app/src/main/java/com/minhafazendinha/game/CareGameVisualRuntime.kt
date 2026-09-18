package com.minhafazendinha.game

/** Render-ready state shared by every factory-made care game. */
data class CareGameVisualState(
    val gameId: String,
    val state: String,
    val sceneDrawableKey: String,
    val drawableKey: String,
    val anchorX: Float,
    val anchorY: Float,
    val scale: Float,
    val layers: List<CareVisualLayer>,
    val transitionMs: Int,
    val touchTargetDp: Int,
    val pressedScale: Float
)

class CareGameVisualRuntime(
    private val pack: CareGamePack,
    private val spec: CareGameVisualSpec = CareGameVisualSpecFactory.build(pack)
) {
    init {
        require(spec.validate().isEmpty()) {
            "Invalid visual spec for ${pack.id}: ${spec.validate().joinToString()}"
        }
    }

    fun idle(): CareGameVisualState = resolve("idle")

    fun forAction(actionId: String): CareGameVisualState {
        val action = pack.template.actions.firstOrNull { it.id == actionId }
            ?: error("Unknown action '$actionId' for ${pack.id}")
        return resolve(action.visualState)
    }

    fun resolve(state: String): CareGameVisualState {
        require(state in spec.requiredStates) { "Unknown visual state '$state' for ${pack.id}" }
        val drawable = if (state == "idle") {
            pack.assets.idle
        } else {
            pack.template.actions.firstOrNull { it.visualState == state }
                ?.let { pack.assets.actionStates[it.id] }
                ?: error("No asset mapped for visual state '$state' in ${pack.id}")
        }
        return CareGameVisualState(
            gameId = pack.id,
            state = state,
            sceneDrawableKey = pack.assets.scene,
            drawableKey = drawable,
            anchorX = spec.character.anchorX,
            anchorY = spec.character.anchorY,
            scale = spec.character.scale,
            layers = spec.layers,
            transitionMs = spec.motion.actionTransitionMs,
            touchTargetDp = spec.touch.minimumTargetDp,
            pressedScale = spec.touch.feedbackScale
        )
    }

    fun validateAssets(): List<String> = buildList {
        addAll(spec.validate())
        addAll(pack.assets.validate())
        spec.requiredStates.forEach { state ->
            runCatching { resolve(state) }.exceptionOrNull()?.let { add("visual.asset.$state") }
        }
    }.distinct()
}

object CareGameVisualRuntimeFactory {
    fun create(gameId: String): CareGameVisualRuntime {
        val pack = CareGamePackFactory.catalog()[gameId] ?: error("Unknown care game: $gameId")
        return CareGameVisualRuntime(pack)
    }

    fun catalog(): Map<String, CareGameVisualRuntime> =
        CareGamePackFactory.catalog().mapValues { (_, pack) -> CareGameVisualRuntime(pack) }
}
