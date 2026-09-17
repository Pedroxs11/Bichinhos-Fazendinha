package com.minhafazendinha.game

/**
 * Reusable visual contract for every care game.
 * Keeps art/layout polish independent from the screen implementation so a new game
 * can inherit proven motion, touch and character-placement rules automatically.
 */
data class CareGameVisualSpec(
    val gameId: String,
    val character: CareCharacterVisualSpec,
    val motion: CareMotionVisualSpec,
    val touch: CareTouchVisualSpec,
    val layers: List<CareVisualLayer>,
    val requiredStates: Set<String>
) {
    fun validate(): List<String> = buildList {
        if (gameId.isBlank()) add("visual.game_id")
        if (character.anchorX !in 0f..1f || character.anchorY !in 0f..1f) add("visual.character_anchor")
        if (character.scale <= 0f) add("visual.character_scale")
        if (motion.actionTransitionMs <= 0 || motion.completionFeedbackMs <= 0) add("visual.motion_duration")
        if (touch.minimumTargetDp < 48) add("visual.touch_target")
        if (layers.isEmpty()) add("visual.layers")
        if (requiredStates.isEmpty() || "idle" !in requiredStates) add("visual.idle_state")
    }
}

data class CareCharacterVisualSpec(
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.72f,
    val scale: Float = 1f,
    val keepAnchorBetweenStates: Boolean = true,
    val keepScaleBetweenStates: Boolean = true
)

data class CareMotionVisualSpec(
    val actionTransitionMs: Int = 180,
    val completionFeedbackMs: Int = 420,
    val returnToIdle: Boolean = true,
    val reduceMotionSupported: Boolean = true
)

data class CareTouchVisualSpec(
    val minimumTargetDp: Int = 56,
    val feedbackScale: Float = 0.96f,
    val showPressedFeedback: Boolean = true
)

enum class CareVisualLayer { BACKGROUND, CHARACTER, EFFECTS, HUD }

object CareGameVisualSpecFactory {
    fun build(pack: CareGamePack): CareGameVisualSpec {
        val durations = pack.polish.transitions.map { it.durationMs }
        val transitionMs = if (durations.isEmpty()) 180 else durations.average().toInt().coerceAtLeast(1)
        val states = buildSet {
            add("idle")
            pack.template.actions.forEach { add(it.visualState) }
        }
        return CareGameVisualSpec(
            gameId = pack.id,
            character = CareCharacterVisualSpec(),
            motion = CareMotionVisualSpec(actionTransitionMs = transitionMs),
            touch = CareTouchVisualSpec(),
            layers = listOf(
                CareVisualLayer.BACKGROUND,
                CareVisualLayer.CHARACTER,
                CareVisualLayer.EFFECTS,
                CareVisualLayer.HUD
            ),
            requiredStates = states
        )
    }

    fun catalog(): Map<String, CareGameVisualSpec> =
        CareGamePackFactory.catalog().mapValues { (_, pack) -> build(pack) }
}
