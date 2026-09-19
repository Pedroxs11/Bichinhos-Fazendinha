package com.minhafazendinha.game

/** The kind of factory work required before a game can advance. */
enum class CareProductionTaskKind { ASSET, VISUAL_CONTRACT }

/** A deterministic unit of work generated from the visual dashboard. */
data class CareProductionTask(
    val gameId: String,
    val assetKey: String,
    val priority: Int,
    val blocksInternalTest: Boolean,
    val kind: CareProductionTaskKind = CareProductionTaskKind.ASSET
) {
    val workKey: String get() = "$gameId:${kind.name.lowercase()}:$assetKey"
}

data class CareProductionPlan(
    val tasks: List<CareProductionTask>
) {
    val next: CareProductionTask? get() = tasks.firstOrNull()
    val blocking: List<CareProductionTask> get() = tasks.filter { it.blocksInternalTest }
    val assetTasks: List<CareProductionTask> get() = tasks.filter { it.kind == CareProductionTaskKind.ASSET }
    val visualContractTasks: List<CareProductionTask> get() =
        tasks.filter { it.kind == CareProductionTaskKind.VISUAL_CONTRACT }

    fun forGame(gameId: String): List<CareProductionTask> = tasks.filter { it.gameId == gameId }
}

/**
 * Converts release/readiness data into one reusable production queue.
 * Asset work and visual-contract work share the same ordering, so a newly generated
 * game automatically receives an actionable backlog instead of only a readiness flag.
 */
object CareGameProductionPlanner {
    fun build(dashboard: CareFactoryDashboard): CareProductionPlan {
        val tasks = dashboard.games.flatMap { game ->
            val seen = mutableSetOf<String>()
            game.nextVisualPriorities.mapIndexedNotNull { index, rawKey ->
                val isVisualContract = rawKey.startsWith("visual:")
                val key = rawKey.removePrefix("visual:")
                val kind = if (isVisualContract) {
                    CareProductionTaskKind.VISUAL_CONTRACT
                } else {
                    CareProductionTaskKind.ASSET
                }
                val uniqueKey = "${kind.name}:$key"
                if (!seen.add(uniqueKey)) return@mapIndexedNotNull null

                CareProductionTask(
                    gameId = game.gameId,
                    assetKey = key,
                    priority = index + 1,
                    blocksInternalTest = !game.readyForInternalTest,
                    kind = kind
                )
            }
        }.sortedWith(
            compareByDescending<CareProductionTask> { it.blocksInternalTest }
                .thenBy { it.priority }
                .thenBy { it.gameId }
                .thenBy { it.kind }
                .thenBy { it.assetKey }
        )
        return CareProductionPlan(tasks)
    }
}
