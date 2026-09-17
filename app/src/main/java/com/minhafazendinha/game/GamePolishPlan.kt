package com.minhafazendinha.game

/** Declarative visual polish recipe shared by every game produced by the factory. */
data class GamePolishPlan(
    val gameId: String,
    val visualPack: String,
    val scene: PolishAsset,
    val characterStates: List<PolishAsset>,
    val transitions: List<PolishTransition>,
    val qualityChecks: List<String>
) {
    val assets: List<PolishAsset> get() = listOf(scene) + characterStates
    val isComplete: Boolean get() = assets.all { it.resourceName.isNotBlank() } && transitions.isNotEmpty()
}

data class PolishAsset(
    val role: String,
    val resourceName: String,
    val layer: Int,
    val notes: String
)

data class PolishTransition(
    val from: String,
    val to: String,
    val durationMs: Int,
    val easing: String = "ease_out"
)

object GamePolishPlanner {
    private const val ACTION_TRANSITION_MS = 180

    fun build(template: GameTemplate): GamePolishPlan {
        val profile = GameFactoryProfileBuilder.build(template)
        val states = buildList {
            add(PolishAsset("idle", profile.idleAsset, 20, "Base character pose; preserve silhouette and framing"))
            template.primaryLoop.forEach { action ->
                val asset = profile.actionVisuals.getValue(action)
                add(PolishAsset(action.key, asset, 20, "Action pose; match idle scale, anchor and lighting"))
            }
        }
        val transitions = template.primaryLoop.flatMap { action ->
            listOf(
                PolishTransition("idle", action.key, ACTION_TRANSITION_MS),
                PolishTransition(action.key, "idle", ACTION_TRANSITION_MS)
            )
        }
        return GamePolishPlan(
            gameId = template.id,
            visualPack = template.visualPack,
            scene = PolishAsset("scene", profile.sceneAsset, 0, "Background plate; keep interaction area visually quiet"),
            characterStates = states,
            transitions = transitions,
            qualityChecks = listOf(
                "same_character_anchor",
                "same_character_scale",
                "consistent_light_direction",
                "safe_touch_contrast",
                "no_ui_baked_into_art",
                "transition_has_idle_fallback"
            )
        )
    }

    fun validate(plan: GamePolishPlan): List<String> = buildList {
        if (plan.gameId.isBlank()) add("polish.game_id")
        if (plan.visualPack.isBlank()) add("polish.visual_pack")
        if (plan.scene.resourceName.isBlank()) add("polish.scene")
        if (plan.characterStates.none { it.role == "idle" }) add("polish.idle")
        if (plan.characterStates.map { it.role }.distinct().size != plan.characterStates.size) add("polish.duplicate_state")
        if (plan.transitions.any { it.durationMs <= 0 }) add("polish.transition_duration")
        if (plan.qualityChecks.isEmpty()) add("polish.quality_checks")
    }

    /** Stable handoff for art production and later automated QA/export tooling. */
    fun handoff(template: GameTemplate): List<String> {
        val plan = build(template)
        return buildList {
            add("POLISH ${template.title} pack=${plan.visualPack}")
            plan.assets.sortedBy { it.layer }.forEach { asset ->
                add("ASSET layer=${asset.layer} role=${asset.role} name=${asset.resourceName} | ${asset.notes}")
            }
            plan.transitions.forEach { transition ->
                add("TRANSITION ${transition.from}->${transition.to} ${transition.durationMs}ms ${transition.easing}")
            }
            plan.qualityChecks.forEach { add("QA $it") }
        }
    }

    fun buildAll(templates: Iterable<GameTemplate>): Map<String, GamePolishPlan> =
        templates.associate { it.id to build(it) }

    val farm: GamePolishPlan by lazy { build(GameTemplateFactory.farm) }
}
