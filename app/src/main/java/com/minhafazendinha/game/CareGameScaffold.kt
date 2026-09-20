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

    /** Resource-level release gate used by CI/art import tooling. */
    fun releaseReadiness(availablePaths: Set<String>): CareScaffoldReadiness {
        val missing = requiredAssets.filterNot(availablePaths::contains).toSet()
        return CareScaffoldReadiness(
            gameId = gameId,
            ready = validate().isEmpty() && missing.isEmpty(),
            required = requiredAssets.size,
            available = requiredAssets.count(availablePaths::contains),
            missing = missing,
            scaffoldErrors = validate()
        )
    }
}

data class CareScaffoldReadiness(
    val gameId: String,
    val ready: Boolean,
    val required: Int,
    val available: Int,
    val missing: Set<String>,
    val scaffoldErrors: List<String>
) {
    val progress: Int get() = if (required == 0) 100 else ((available * 100f) / required).toInt()
}

data class CareFactoryReleaseReport(val games: Map<String, CareScaffoldReadiness>) {
    val ready: Boolean get() = games.values.all { it.ready }
    val missing: Set<String> get() = games.values.flatMap { it.missing }.toSet()
    val progress: Int get() = if (games.isEmpty()) 100 else games.values.map { it.progress }.average().toInt()
}

object CareGameScaffoldFactory {
    fun build(pack: CareGamePack, packageName: String = "com.minhafazendinha.game"): CareGameScaffold {
        val id = safeResourceName(pack.id)
        val drawableDir = "app/src/main/res/drawable/$id"
        val rawDir = "app/src/main/res/raw/$id"
        val requiredAssets = buildList {
            add("$drawableDir/${pack.assets.scene}.png")
            add("$drawableDir/${pack.assets.idle}.png")
            pack.assets.actionStates.values.distinct().forEach { add("$drawableDir/$it.png") }
            pack.assets.sounds.values.distinct().forEach { add("$rawDir/$it.ogg") }
        }
        return CareGameScaffold(pack.id, packageName, "CareGameBootstrap.create(\"${pack.id}\")", listOf(drawableDir, rawDir), requiredAssets, standardImplementationSteps(), pack.polish.qualityChecks + standardPolishChecks())
    }

    fun build(product: CareBlueprintProduct, packageName: String = "com.minhafazendinha.game"): CareGameScaffold {
        val blueprint = product.blueprint
        val plan = product.launchPlan
        val id = safeResourceName(blueprint.id)
        val character = safeResourceName(blueprint.characterId)
        val drawableDir = "app/src/main/res/drawable/$id"
        val rawDir = "app/src/main/res/raw/$id"
        val visualAssets = buildList {
            add("$drawableDir/${id}_${character}_base.png")
            plan.visualStates.forEach { state -> add("$drawableDir/${id}_${character}_${safeResourceName(state)}.png") }
        }.distinct()
        val audioAssets = blueprint.actions.mapNotNull { action -> action.soundKey?.let { "$rawDir/${id}_${safeResourceName(it)}.ogg" } }.distinct()
        val polish = buildList {
            addAll(standardPolishChecks())
            add("all_visual_states_have_final_art")
            add("character_scale_is_consistent_between_states")
            add("reaction_timing_matches_action_feedback")
            if (plan.missingSoundActionIds.isNotEmpty()) add("replace_missing_action_audio")
            if (plan.polishWarnings.isNotEmpty()) add("resolve_blueprint_polish_warnings")
        }.distinct()
        return CareGameScaffold(blueprint.id, packageName, "CareBlueprintCompiler.compileTemplate(CareTemplateCatalog.require(\"${blueprint.id}\"))", listOf(drawableDir, rawDir), visualAssets + audioAssets, standardImplementationSteps() + listOf("verify_blueprint_preflight", "verify_generated_asset_manifest", "verify_launch_plan_before_release"), polish)
    }

    fun catalog(): Map<String, CareGameScaffold> = CareGamePackFactory.catalog().mapValues { (_, pack) -> build(pack) }
    fun compiledCatalog(): Map<String, CareGameScaffold> = CareBlueprintCompiler.compileCatalog().associate { product -> product.blueprint.id to build(product) }
    fun validateCompiledCatalog(): Map<String, List<String>> = compiledCatalog().mapValues { (_, scaffold) -> scaffold.validate() }.filterValues { it.isNotEmpty() }

    /** One call tells CI/art tooling whether every generated game has its final resources. */
    fun releaseReport(availablePaths: Set<String>): CareFactoryReleaseReport = CareFactoryReleaseReport(
        compiledCatalog().mapValues { (_, scaffold) -> scaffold.releaseReadiness(availablePaths) }
    )

    private fun standardImplementationSteps() = listOf("register_blueprint_or_template", "generate_production_pack", "add_scene_and_character_assets", "add_action_state_assets", "add_action_audio_assets", "bind_runtime_to_screen", "run_touch_and_progress_smoke_test", "run_visual_polish_pass")
    private fun standardPolishChecks() = listOf("action_transition_has_no_jump", "touch_target_is_child_friendly", "audio_matches_visible_action", "completion_feedback_is_clear")
    private fun safeResourceName(value: String): String {
        val normalized = value.lowercase().map { if (it.isLetterOrDigit()) it else '_' }.joinToString("").replace(Regex("_+"), "_").trim('_')
        val safe = normalized.ifBlank { "asset" }
        return if (safe.first().isDigit()) "asset_$safe" else safe
    }
}
