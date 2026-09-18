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
        val id = safeResourceName(pack.id)
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
            implementationSteps = standardImplementationSteps(),
            polishChecks = pack.polish.qualityChecks + standardPolishChecks()
        )
    }

    /**
     * Fast factory path introduced for compiled blueprints. A designer can now clone or
     * author a blueprint and immediately receive the same concrete resource/QA scaffold
     * used by production packs, without first hand-writing a CareGamePack.
     */
    fun build(
        product: CareBlueprintProduct,
        packageName: String = "com.minhafazendinha.game"
    ): CareGameScaffold {
        val blueprint = product.blueprint
        val plan = product.launchPlan
        val id = safeResourceName(blueprint.id)
        val character = safeResourceName(blueprint.characterId)
        val drawableDir = "app/src/main/res/drawable/$id"
        val rawDir = "app/src/main/res/raw/$id"

        val visualAssets = buildList {
            add("$drawableDir/${id}_${character}_base.png")
            plan.visualStates.forEach { state ->
                add("$drawableDir/${id}_${character}_${safeResourceName(state)}.png")
            }
        }.distinct()

        val audioAssets = blueprint.actions.mapNotNull { action ->
            action.soundKey?.let { key -> "$rawDir/${id}_${safeResourceName(key)}.ogg" }
        }.distinct()

        val polish = buildList {
            addAll(standardPolishChecks())
            add("all_visual_states_have_final_art")
            add("character_scale_is_consistent_between_states")
            add("reaction_timing_matches_action_feedback")
            if (plan.missingSoundActionIds.isNotEmpty()) add("replace_missing_action_audio")
            if (plan.polishWarnings.isNotEmpty()) add("resolve_blueprint_polish_warnings")
        }.distinct()

        return CareGameScaffold(
            gameId = blueprint.id,
            packageName = packageName,
            runtimeEntryPoint = "CareBlueprintCompiler.compileTemplate(CareTemplateCatalog.require(\"${blueprint.id}\"))",
            assetDirectories = listOf(drawableDir, rawDir),
            requiredAssets = visualAssets + audioAssets,
            implementationSteps = standardImplementationSteps() + listOf(
                "verify_blueprint_preflight",
                "verify_generated_asset_manifest",
                "verify_launch_plan_before_release"
            ),
            polishChecks = polish
        )
    }

    /** Existing production packs and compiled blueprints can be inspected side by side. */
    fun catalog(): Map<String, CareGameScaffold> =
        CareGamePackFactory.catalog().mapValues { (_, pack) -> build(pack) }

    fun compiledCatalog(): Map<String, CareGameScaffold> =
        CareBlueprintCompiler.compileCatalog().associate { product ->
            product.blueprint.id to build(product)
        }

    /** One-call smoke gate for tooling/CI: an empty result means every scaffold is usable. */
    fun validateCompiledCatalog(): Map<String, List<String>> =
        compiledCatalog().mapValues { (_, scaffold) -> scaffold.validate() }
            .filterValues { it.isNotEmpty() }

    private fun standardImplementationSteps() = listOf(
        "register_blueprint_or_template",
        "generate_production_pack",
        "add_scene_and_character_assets",
        "add_action_state_assets",
        "add_action_audio_assets",
        "bind_runtime_to_screen",
        "run_touch_and_progress_smoke_test",
        "run_visual_polish_pass"
    )

    private fun standardPolishChecks() = listOf(
        "action_transition_has_no_jump",
        "touch_target_is_child_friendly",
        "audio_matches_visible_action",
        "completion_feedback_is_clear"
    )

    private fun safeResourceName(value: String): String {
        val normalized = value.lowercase()
            .map { if (it.isLetterOrDigit()) it else '_' }
            .joinToString("")
            .replace(Regex("_+"), "_")
            .trim('_')
        val safe = normalized.ifBlank { "asset" }
        return if (safe.first().isDigit()) "asset_$safe" else safe
    }
}
