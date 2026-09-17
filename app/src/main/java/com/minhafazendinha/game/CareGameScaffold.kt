package com.minhafazendinha.game

/**
 * Generates a deterministic implementation plan for a care-game pack.
 * This is the bridge between declarative game design and the production UI/art pass:
 * a new game can reuse the same folder conventions, runtime wiring and QA gates.
 */
data class CareGameScaffold(
    val gameId: String,
    val packageName: String,
    val runtimeEntryPoint: String,
    val assetDirectories: List<String>,
    val requiredAssets: List<String>,
    val implementationSteps: List<String>,
    val polishChecks: List<String>
) {
    fun validate(): List<String> = buildList {
        if (gameId.isBlank()) add("scaffold.game_id")
        if (packageName.isBlank()) add("scaffold.package")
        if (runtimeEntryPoint.isBlank()) add("scaffold.runtime")
        if (assetDirectories.isEmpty()) add("scaffold.asset_directories")
        if (requiredAssets.isEmpty()) add("scaffold.required_assets")
        if (implementationSteps.isEmpty()) add("scaffold.steps")
        if (polishChecks.isEmpty()) add("scaffold.polish")
    }
}

object CareGameScaffoldFactory {
    fun build(
        pack: CareGamePack,
        packageName: String = "com.minhafazendinha.game"
    ): CareGameScaffold {
        val id = pack.id.lowercase()
        val drawableDir = "app/src/main/res/drawable/$id"
        val rawDir = "app/src/main/res/raw/$id"
        val requiredAssets = buildList {
            add("$drawableDir/${pack.assets.scene}.png")
            add("$drawableDir/${pack.assets.idle}.png")
            pack.assets.actionStates.values.distinct().forEach { add("$drawableDir/$it.png") }
            pack.assets.sounds.values.distinct().forEach { add("$rawDir/$it.ogg") }
        }

        return CareGameScaffold(
            gameId = pack.id,
            packageName = packageName,
            runtimeEntryPoint = "CareGameBootstrap.create(\"${pack.id}\")",
            assetDirectories = listOf(drawableDir, rawDir),
            requiredAssets = requiredAssets,
            implementationSteps = listOf(
                "register_blueprint_or_template",
                "generate_production_pack",
                "add_scene_and_character_assets",
                "add_action_state_assets",
                "add_action_audio_assets",
                "bind_runtime_to_screen",
                "run_touch_and_progress_smoke_test",
                "run_visual_polish_pass"
            ),
            polishChecks = pack.polish.qualityChecks + listOf(
                "action_transition_has_no_jump",
                "touch_target_is_child_friendly",
                "audio_matches_visible_action",
                "completion_feedback_is_clear"
            )
        )
    }

    fun catalog(): Map<String, CareGameScaffold> =
        CareGamePackFactory.catalog().mapValues { (_, pack) -> build(pack) }
}
