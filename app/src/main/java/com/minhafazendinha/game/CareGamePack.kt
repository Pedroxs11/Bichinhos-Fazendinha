package com.minhafazendinha.game

/**
 * Production-ready description generated from a CareGameBlueprint.
 * Keeps gameplay wiring, asset naming and polish handoff in one reusable unit.
 */
data class CareGamePack(
    val template: CareTemplate,
    val assets: CareAssetPack,
    val polish: CarePolishPlan
) {
    val id: String get() = template.id
    val isReadyForArt: Boolean get() = assets.validate().isEmpty() && polish.validate().isEmpty()
}

data class CareAssetPack(
    val scene: String,
    val idle: String,
    val actionStates: Map<String, String>,
    val sounds: Map<String, String>
) {
    fun validate(): List<String> = buildList {
        if (scene.isBlank()) add("assets.scene")
        if (idle.isBlank()) add("assets.idle")
        if (actionStates.isEmpty()) add("assets.actions")
        if (actionStates.values.any { it.isBlank() }) add("assets.action_name")
    }
}

data class CarePolishPlan(
    val transitions: List<CarePolishTransition>,
    val qualityChecks: List<String>
) {
    fun validate(): List<String> = buildList {
        if (transitions.isEmpty()) add("polish.transitions")
        if (transitions.any { it.durationMs <= 0 }) add("polish.duration")
        if (qualityChecks.isEmpty()) add("polish.quality_checks")
    }
}

data class CarePolishTransition(
    val from: String,
    val to: String,
    val durationMs: Int = 180
)

/**
 * Single entry point for turning a lightweight blueprint into everything the
 * next care-game screen and art pass need.
 */
object CareGamePackFactory {
    fun build(blueprint: CareGameBlueprint): CareGamePack {
        val template = blueprint.toTemplate()
        val prefix = template.id.lowercase()
        val states = template.actions.associate { action ->
            action.id to "${prefix}_${action.visualState.lowercase()}"
        }
        val sounds = template.actions.mapNotNull { action ->
            action.soundKey?.let { action.id to it }
        }.toMap()
        val transitions = template.actions.flatMap { action ->
            listOf(
                CarePolishTransition("idle", action.visualState),
                CarePolishTransition(action.visualState, "idle")
            )
        }
        return CareGamePack(
            template = template,
            assets = CareAssetPack(
                scene = "${prefix}_scene",
                idle = "${prefix}_idle",
                actionStates = states,
                sounds = sounds
            ),
            polish = CarePolishPlan(
                transitions = transitions,
                qualityChecks = listOf(
                    "same_character_anchor",
                    "same_character_scale",
                    "consistent_light_direction",
                    "safe_touch_contrast",
                    "no_ui_baked_into_art",
                    "idle_fallback_available"
                )
            )
        )
    }

    /** Clone a proven catalog entry and immediately produce its production pack. */
    fun clone(
        source: CareTemplate,
        id: String,
        title: String,
        characterId: String
    ): CareGamePack = build(
        CareGameBlueprint.fromTemplate(
            template = source,
            id = id,
            title = title,
            characterId = characterId
        )
    )

    fun catalog(): Map<String, CareGamePack> = CareTemplateCatalog.all().associate { template ->
        template.id to build(CareGameBlueprint.fromTemplate(template))
    }
}
