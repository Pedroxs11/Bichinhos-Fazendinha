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
    val requiredStates: Set<String>,
    val feedback: Map<String, CareActionFeedbackSpec> = emptyMap()
) {
    fun validate(): List<String> = buildList {
        if (gameId.isBlank()) add("visual.game_id")
        if (character.anchorX !in 0f..1f || character.anchorY !in 0f..1f) add("visual.character_anchor")
        if (character.scale <= 0f) add("visual.character_scale")
        if (motion.actionTransitionMs <= 0 || motion.completionFeedbackMs <= 0) add("visual.motion_duration")
        if (touch.minimumTargetDp < 48) add("visual.touch_target")
        if (layers.isEmpty()) add("visual.layers")
        if (requiredStates.isEmpty() || "idle" !in requiredStates) add("visual.idle_state")
        feedback.forEach { (state, spec) ->
            if (state.isBlank()) add("visual.feedback_state")
            if (spec.particleCount < 0 || spec.distanceDp < 0) add("visual.feedback_particles")
            if (spec.reactionScale <= 0f) add("visual.feedback_scale")
        }
    }

    fun feedbackFor(state: String): CareActionFeedbackSpec =
        feedback[state] ?: CareActionFeedbackSpec()
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

data class CareActionFeedbackSpec(
    val style: CareFeedbackStyle = CareFeedbackStyle.SPARKLE,
    val particleCount: Int = 6,
    val distanceDp: Int = 38,
    val largeDp: Int = 10,
    val smallDp: Int = 7,
    val reactionScale: Float = 1.04f,
    val liftDp: Int = 6,
    val tiltDegrees: Float = 2f
)

enum class CareFeedbackStyle { BUBBLE, CRUMB, SPARKLE, PLAYFUL }
enum class CareVisualLayer { BACKGROUND, CHARACTER, EFFECTS, HUD }

object CareGameVisualSpecFactory {
    fun build(pack: CareGamePack): CareGameVisualSpec {
        val durations = pack.polish.transitions.map { it.durationMs }
        val transitionMs = if (durations.isEmpty()) 180 else durations.average().toInt().coerceAtLeast(1)
        val states = buildSet {
            add("idle")
            pack.template.actions.forEach { add(it.visualState) }
        }
        val feedback = pack.template.actions.associate { action ->
            action.visualState to feedbackFor(action.id, action.visualState)
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
            requiredStates = states,
            feedback = feedback
        )
    }

    private fun feedbackFor(actionId: String, state: String): CareActionFeedbackSpec {
        val key = "$actionId $state".lowercase()
        return when {
            listOf("bath", "wash", "wet", "banho").any(key::contains) ->
                CareActionFeedbackSpec(CareFeedbackStyle.BUBBLE, 8, 42, 12, 8, 1.025f, 3, 1.5f)
            listOf("feed", "eat", "food", "comer").any(key::contains) ->
                CareActionFeedbackSpec(CareFeedbackStyle.CRUMB, 6, 36, 9, 6, 1.035f, 4, 2f)
            listOf("brush", "clean", "escov").any(key::contains) ->
                CareActionFeedbackSpec(CareFeedbackStyle.SPARKLE, 7, 38, 10, 6, 1.045f, 6, 2.5f)
            listOf("play", "ball", "brinc").any(key::contains) ->
                CareActionFeedbackSpec(CareFeedbackStyle.PLAYFUL, 9, 48, 11, 7, 1.07f, 12, 4f)
            else -> CareActionFeedbackSpec()
        }
    }

    fun catalog(): Map<String, CareGameVisualSpec> =
        CareGamePackFactory.catalog().mapValues { (_, pack) -> build(pack) }
}
