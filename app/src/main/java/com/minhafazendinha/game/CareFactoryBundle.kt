package com.minhafazendinha.game

/**
 * One-call handoff between game design, implementation and art production.
 * New titles can now be compiled into a deterministic bundle instead of wiring
 * blueprint, scaffold and art requirements independently.
 */
data class CareFactoryBundle(
    val gameId: String,
    val blueprint: CareGameBlueprint,
    val launchPlan: CareGameLaunchPlan,
    val scaffold: CareGameScaffold,
    val artManifest: CareGameArtManifest,
    val blockingIssues: List<String>
) {
    val readyForProduction: Boolean get() = blockingIssues.isEmpty()

    fun requiredVisualAssets(): List<String> =
        artManifest.slots.filter { it.required }.map { it.key }

    fun productionHandoff(): CareProductionHandoff = CareProductionHandoff(
        gameId = gameId,
        ready = readyForProduction,
        runtimeEntryPoint = scaffold.runtimeEntryPoint,
        assetDirectories = scaffold.assetDirectories,
        visualAssets = artManifest.slots.map { slot ->
            CareProductionAsset(slot.key, slot.role, slot.transparent, slot.required)
        },
        implementationQueue = scaffold.implementationSteps.distinct(),
        polishQueue = (artManifest.qualityChecks + scaffold.polishChecks).distinct(),
        blockers = blockingIssues
    )
}

data class CareProductionAsset(
    val key: String,
    val role: String,
    val transparent: Boolean,
    val required: Boolean
)

data class CarePolishTask(
    val id: String,
    val assetKey: String?,
    val check: String,
    val blocking: Boolean
)

data class CarePolishPlan(
    val gameId: String,
    val tasks: List<CarePolishTask>
) {
    val blockingTasks: List<CarePolishTask> get() = tasks.filter { it.blocking }
    val assetTasks: List<CarePolishTask> get() = tasks.filter { it.assetKey != null }
    val globalTasks: List<CarePolishTask> get() = tasks.filter { it.assetKey == null }

    fun validate(): List<String> = buildList {
        if (gameId.isBlank()) add("polish.game_id")
        if (tasks.isEmpty()) add("polish.tasks_empty")
        if (tasks.map { it.id }.distinct().size != tasks.size) add("polish.duplicate_task")
        if (tasks.any { it.check.isBlank() }) add("polish.blank_check")
    }
}

data class CareProductionHandoff(
    val gameId: String,
    val ready: Boolean,
    val runtimeEntryPoint: String,
    val assetDirectories: List<String>,
    val visualAssets: List<CareProductionAsset>,
    val implementationQueue: List<String>,
    val polishQueue: List<String>,
    val blockers: List<String>
) {
    val requiredVisualCount: Int get() = visualAssets.count { it.required }
    val hasPolishPlan: Boolean get() = polishQueue.isNotEmpty()

    /**
     * Expands generic polish checks into deterministic per-asset work.
     * Required visuals become blocking tasks, while optional art and global checks can
     * proceed independently. This gives future games the same ready-made art/QA lane.
     */
    fun visualPolishPlan(): CarePolishPlan {
        val assetChecks = polishQueue.filter { check ->
            check.contains("character") || check.contains("transparent") || check.contains("scene")
        }
        val globalChecks = polishQueue - assetChecks.toSet()
        val tasks = buildList {
            visualAssets.forEach { asset ->
                assetChecks.forEach { check ->
                    add(CarePolishTask(
                        id = "${safeTaskPart(asset.key)}__${safeTaskPart(check)}",
                        assetKey = asset.key,
                        check = check,
                        blocking = asset.required
                    ))
                }
            }
            globalChecks.forEach { check ->
                add(CarePolishTask(
                    id = "global__${safeTaskPart(check)}",
                    assetKey = null,
                    check = check,
                    blocking = false
                ))
            }
        }.distinctBy { it.id }
        return CarePolishPlan(gameId, tasks)
    }

    fun validate(): List<String> = buildList {
        if (gameId.isBlank()) add("handoff.game_id")
        if (runtimeEntryPoint.isBlank()) add("handoff.runtime")
        if (assetDirectories.isEmpty()) add("handoff.asset_directories")
        if (visualAssets.none { it.required }) add("handoff.required_visuals")
        if (implementationQueue.isEmpty()) add("handoff.implementation_queue")
        if (polishQueue.isEmpty()) add("handoff.polish_queue")
        if (ready && blockers.isNotEmpty()) add("handoff.ready_with_blockers")
        addAll(visualPolishPlan().validate())
    }

    private fun safeTaskPart(value: String): String = value.lowercase()
        .map { if (it.isLetterOrDigit()) it else '_' }
        .joinToString("")
        .replace(Regex("_+"), "_")
        .trim('_')
        .ifBlank { "task" }
}

object CareFactoryBundleFactory {
    fun build(product: CareBlueprintProduct): CareFactoryBundle {
        val blueprint = product.blueprint
        val scaffold = CareGameScaffoldFactory.build(product)
        val artManifest = manifestFor(product)
        val blocking = buildList {
            addAll(scaffold.validate())
            if (artManifest.slots.isEmpty()) add("factory.art_manifest_empty")
            if (artManifest.requiredKeys().isEmpty()) add("factory.required_art_empty")
        }.distinct()

        return CareFactoryBundle(
            gameId = blueprint.id,
            blueprint = blueprint,
            launchPlan = product.launchPlan,
            scaffold = scaffold,
            artManifest = artManifest,
            blockingIssues = blocking
        )
    }

    fun catalog(): Map<String, CareFactoryBundle> =
        CareBlueprintCompiler.compileCatalog().associate { product ->
            product.blueprint.id to build(product)
        }

    fun validateCatalog(): Map<String, List<String>> =
        catalog().mapValues { (_, bundle) ->
            (bundle.blockingIssues + bundle.productionHandoff().validate()).distinct()
        }.filterValues { it.isNotEmpty() }

    fun productionCatalog(): Map<String, CareProductionHandoff> =
        catalog().mapValues { (_, bundle) -> bundle.productionHandoff() }

    /** Reusable art/QA work queue for every title in the factory catalog. */
    fun polishCatalog(): Map<String, CarePolishPlan> =
        productionCatalog().mapValues { (_, handoff) -> handoff.visualPolishPlan() }

    private fun manifestFor(product: CareBlueprintProduct): CareGameArtManifest {
        val blueprint = product.blueprint
        val id = safeName(blueprint.id)
        val character = safeName(blueprint.characterId)
        val slots = buildList {
            add(CareArtSlot("${id}_scene", "background_scene", transparent = false))
            add(CareArtSlot("${id}_${character}_base", "character_idle", transparent = true))
            product.launchPlan.visualStates.forEach { state ->
                add(CareArtSlot(
                    key = "${id}_${character}_${safeName(state)}",
                    role = "character_${safeName(state)}",
                    transparent = true
                ))
            }
        }.distinctBy { it.key }

        return CareGameArtManifest(
            gameId = blueprint.id,
            slots = slots,
            referenceStyle = "polished_3d_cartoon_kids_game",
            qualityChecks = listOf(
                "transparent_character_edges_clean",
                "character_light_matches_scene",
                "character_scale_consistent_between_states",
                "action_pose_reads_without_text",
                "no_text_or_buttons_baked_into_art"
            )
        )
    }

    private fun safeName(value: String): String {
        val normalized = value.lowercase()
            .map { if (it.isLetterOrDigit()) it else '_' }
            .joinToString("")
            .replace(Regex("_+"), "_")
            .trim('_')
        val safe = normalized.ifBlank { "asset" }
        return if (safe.first().isDigit()) "asset_$safe" else safe
    }
}
