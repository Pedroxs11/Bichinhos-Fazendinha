package com.minhafazendinha.game

/**
 * Production gate shared by every care game created by the factory.
 * It turns the scaffold into explicit, machine-readable stages so future games can
 * move from template to playable/polished without duplicating release logic.
 */
enum class CareProductionStage {
    DESIGN,
    ASSETS,
    RUNTIME,
    GAMEPLAY_QA,
    VISUAL_POLISH,
    READY
}

data class CareProductionStatus(
    val gameId: String,
    val stage: CareProductionStage,
    val completed: Set<String>,
    val pending: List<String>,
    val blockers: List<String>,
    val progress: Float
) {
    val isReady: Boolean get() = stage == CareProductionStage.READY && blockers.isEmpty()
}

class CareGameProductionPipeline(private val scaffold: CareGameScaffold) {
    private val required: List<String> = (
        scaffold.implementationSteps + scaffold.polishChecks.map { "polish:$it" }
    ).distinct()

    fun evaluate(completedSteps: Set<String>): CareProductionStatus {
        val scaffoldBlockers = scaffold.validate()
        val completed = completedSteps.intersect(required.toSet())
        val pending = required.filterNot(completed::contains)
        val progress = if (required.isEmpty()) 1f else completed.size.toFloat() / required.size

        return CareProductionStatus(
            gameId = scaffold.gameId,
            stage = resolveStage(completed, scaffoldBlockers),
            completed = completed,
            pending = pending,
            blockers = scaffoldBlockers,
            progress = progress.coerceIn(0f, 1f)
        )
    }

    fun nextBatch(completedSteps: Set<String>, maxItems: Int = 4): List<String> {
        require(maxItems > 0) { "maxItems must be greater than zero" }
        return evaluate(completedSteps).pending.take(maxItems)
    }

    private fun resolveStage(completed: Set<String>, blockers: List<String>): CareProductionStage {
        if (blockers.isNotEmpty()) return CareProductionStage.DESIGN
        if (!completed.contains("register_blueprint_or_template") ||
            !completed.contains("generate_production_pack")) return CareProductionStage.DESIGN
        if (!completed.contains("add_scene_and_character_assets") ||
            !completed.contains("add_action_state_assets") ||
            !completed.contains("add_action_audio_assets")) return CareProductionStage.ASSETS
        if (!completed.contains("bind_runtime_to_screen")) return CareProductionStage.RUNTIME
        if (!completed.contains("run_touch_and_progress_smoke_test")) return CareProductionStage.GAMEPLAY_QA
        if (!completed.contains("run_visual_polish_pass") ||
            scaffold.polishChecks.any { !completed.contains("polish:$it") }) return CareProductionStage.VISUAL_POLISH
        return CareProductionStage.READY
    }
}

object CareGameProductionFactory {
    fun create(gameId: String): CareGameProductionPipeline {
        val scaffold = CareGameScaffoldFactory.catalog()[gameId]
            ?: error("Unknown care game: $gameId")
        return CareGameProductionPipeline(scaffold)
    }

    fun catalog(): Map<String, CareGameProductionPipeline> =
        CareGameScaffoldFactory.catalog().mapValues { (_, scaffold) ->
            CareGameProductionPipeline(scaffold)
        }
}
