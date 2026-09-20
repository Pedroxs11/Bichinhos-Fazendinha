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
) {
    val workKey: String get() = "$gameId:${stage.name.lowercase()}"
}

data class CareFactoryBatch(
    val items: List<CareFactoryBatchItem>,
    val remainingGames: Int
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val taskCount: Int get() = items.sumOf { it.tasks.size }

    /** Stable handoff used by art/QA/generators without exposing scheduler internals. */
    fun handoff(): CareFactoryBatchHandoff = CareFactoryBatchHandoff(
        workItems = items.map { item ->
            CareFactoryWorkItem(
                key = item.workKey,
                gameId = item.gameId,
                stage = item.stage,
                tasks = item.tasks,
                priorityScore = item.score
            )
        },
        remainingGames = remainingGames
    )
}

data class CareFactoryWorkItem(
    val key: String,
    val gameId: String,
    val stage: CareFactoryPriorityStage,
    val tasks: List<String>,
    val priorityScore: Int
) {
    val readyToStart: Boolean get() = tasks.isNotEmpty()
}

data class CareFactoryBatchHandoff(
    val workItems: List<CareFactoryWorkItem>,
    val remainingGames: Int
) {
    val actionableItems: List<CareFactoryWorkItem> get() = workItems.filter { it.readyToStart }
    val taskCount: Int get() = actionableItems.sumOf { it.tasks.size }
    val complete: Boolean get() = actionableItems.isEmpty() && remainingGames == 0
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

    /** One-call production contract for future art, QA and generation tooling. */
    fun nextHandoff(
        dashboard: CareFactoryDashboard,
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ): CareFactoryBatchHandoff = next(dashboard, maxGames, maxTasksPerGame).handoff()
}
