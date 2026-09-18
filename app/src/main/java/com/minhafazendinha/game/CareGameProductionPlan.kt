package com.minhafazendinha.game

/** A deterministic factory backlog generated from the visual dashboard. */
data class CareProductionTask(
    val gameId: String,
    val assetKey: String,
    val priority: Int,
    val blocksInternalTest: Boolean
)

data class CareProductionPlan(
    val tasks: List<CareProductionTask>
) {
    val next: CareProductionTask? get() = tasks.firstOrNull()
    fun forGame(gameId: String): List<CareProductionTask> = tasks.filter { it.gameId == gameId }
}

/**
 * Converts release/readiness data into one reusable art-production queue.
 * New games automatically enter the same pipeline without product-specific QA code.
 */
object CareGameProductionPlanner {
    fun build(dashboard: CareFactoryDashboard): CareProductionPlan {
        val tasks = dashboard.games.flatMap { game ->
            game.nextVisualPriorities.mapIndexed { index, key ->
                CareProductionTask(
                    gameId = game.gameId,
                    assetKey = key,
                    priority = index + 1,
                    blocksInternalTest = !game.readyForInternalTest
                )
            }
        }.sortedWith(
            compareByDescending<CareProductionTask> { it.blocksInternalTest }
                .thenBy { it.priority }
                .thenBy { it.gameId }
                .thenBy { it.assetKey }
        )
        return CareProductionPlan(tasks)
    }
}
