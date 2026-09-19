package com.minhafazendinha.game

/**
 * Turns the priority queue into small deterministic batches that can be consumed
 * by art, QA and future automated generators without reimplementing scheduling.
 */
data class CareFactoryBatchItem(
    val gameId: String,
    val stage: CareFactoryPriorityStage,
    val tasks: List<String>,
    val score: Int
)

data class CareFactoryBatch(
    val items: List<CareFactoryBatchItem>,
    val remainingGames: Int
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val taskCount: Int get() = items.sumOf { it.tasks.size }
}

object CareGameFactoryBatchPlanner {
    fun next(
        queue: CareFactoryPriorityQueue,
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ): CareFactoryBatch {
        require(maxGames > 0) { "maxGames must be positive" }
        require(maxTasksPerGame > 0) { "maxTasksPerGame must be positive" }

        val pending = queue.items.filterNot { it.readyForProduction }
        val selected = pending.take(maxGames).map { priority ->
            CareFactoryBatchItem(
                gameId = priority.gameId,
                stage = priority.stage,
                tasks = priority.nextTasks.distinct().take(maxTasksPerGame),
                score = priority.score
            )
        }
        return CareFactoryBatch(
            items = selected,
            remainingGames = (pending.size - selected.size).coerceAtLeast(0)
        )
    }

    /** Convenience entry point used by future games: dashboard -> ordered batch. */
    fun next(
        dashboard: CareFactoryDashboard,
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ): CareFactoryBatch = next(
        CareGameFactoryPriorityEngine.build(dashboard),
        maxGames,
        maxTasksPerGame
    )
}
