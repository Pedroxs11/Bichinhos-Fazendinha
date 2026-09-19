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

/**
 * A production wave groups work that can be advanced together without losing the
 * deterministic order of the factory queue. Future games inherit these lanes automatically.
 */
data class CareProductionWave(
    val number: Int,
    val tasks: List<CareProductionTask>
) {
    val blocking: Boolean get() = tasks.any { it.blocksInternalTest }
    val assetTasks: List<CareProductionTask> get() = tasks.filter { it.kind == CareProductionTaskKind.ASSET }
    val visualContractTasks: List<CareProductionTask> get() =
        tasks.filter { it.kind == CareProductionTaskKind.VISUAL_CONTRACT }
    val gameIds: Set<String> get() = tasks.mapTo(linkedSetOf()) { it.gameId }
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

    /**
     * Splits the backlog into reusable execution waves. Each wave limits work per game,
     * preventing one unfinished pack from starving the others while still prioritizing blockers.
     */
    fun waves(maxTasksPerGame: Int = 2): List<CareProductionWave> {
        require(maxTasksPerGame > 0) { "maxTasksPerGame must be positive" }
        val remaining = tasks.toMutableList()
        val result = mutableListOf<CareProductionWave>()
        var number = 1
        while (remaining.isNotEmpty()) {
            val selected = mutableListOf<CareProductionTask>()
            val counts = mutableMapOf<String, Int>()
            val iterator = remaining.iterator()
            while (iterator.hasNext()) {
                val task = iterator.next()
                val used = counts[task.gameId] ?: 0
                if (used < maxTasksPerGame) {
                    selected += task
                    counts[task.gameId] = used + 1
                    iterator.remove()
                }
            }
            result += CareProductionWave(number++, selected)
        }
        return result
    }

    fun nextWave(maxTasksPerGame: Int = 2): CareProductionWave? = waves(maxTasksPerGame).firstOrNull()
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
