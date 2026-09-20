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

    fun manifests(): List<CareFactoryWorkManifest> = actionableItems.map { item ->
        CareFactoryWorkManifest(
            workKey = item.key,
            gameId = item.gameId,
            stage = item.stage,
            priorityScore = item.priorityScore,
            steps = item.tasks.mapIndexed { index, task ->
                CareFactoryWorkStep("${item.key}:${index + 1}", index + 1, task)
            }
        )
    }
}

data class CareFactoryWorkStep(val id: String, val order: Int, val task: String)

data class CareFactoryWorkManifest(
    val workKey: String,
    val gameId: String,
    val stage: CareFactoryPriorityStage,
    val priorityScore: Int,
    val steps: List<CareFactoryWorkStep>
) {
    val stepCount: Int get() = steps.size
    val readyToExecute: Boolean get() = steps.isNotEmpty()

    /**
     * Builds a deterministic resumable plan. Generators can persist completed ids
     * and ask for the next work without knowing anything about planner internals.
     */
    fun executionPlan(completedStepIds: Set<String> = emptySet()): CareFactoryExecutionPlan {
        val knownIds = steps.mapTo(mutableSetOf()) { it.id }
        val completed = completedStepIds.intersect(knownIds)
        val pending = steps.filterNot { it.id in completed }
        return CareFactoryExecutionPlan(
            workKey = workKey,
            gameId = gameId,
            stage = stage,
            completedStepIds = completed,
            pendingSteps = pending,
            totalSteps = steps.size
        )
    }
}

data class CareFactoryExecutionPlan(
    val workKey: String,
    val gameId: String,
    val stage: CareFactoryPriorityStage,
    val completedStepIds: Set<String>,
    val pendingSteps: List<CareFactoryWorkStep>,
    val totalSteps: Int
) {
    val completedSteps: Int get() = completedStepIds.size
    val remainingSteps: Int get() = pendingSteps.size
    val complete: Boolean get() = remainingSteps == 0
    val progressPercent: Int
        get() = if (totalSteps == 0) 100 else (completedSteps * 100) / totalSteps
    val nextStep: CareFactoryWorkStep? get() = pendingSteps.minByOrNull { it.order }
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
        return CareFactoryBatch(selected, (pending.size - selected.size).coerceAtLeast(0))
    }

    fun next(
        dashboard: CareFactoryDashboard,
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ): CareFactoryBatch = next(CareGameFactoryPriorityEngine.build(dashboard), maxGames, maxTasksPerGame)

    fun nextHandoff(
        dashboard: CareFactoryDashboard,
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ): CareFactoryBatchHandoff = next(dashboard, maxGames, maxTasksPerGame).handoff()

    fun nextManifests(
        dashboard: CareFactoryDashboard,
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ): List<CareFactoryWorkManifest> = nextHandoff(dashboard, maxGames, maxTasksPerGame).manifests()

    /** One-call resumable execution plans for automated production workers. */
    fun nextExecutionPlans(
        dashboard: CareFactoryDashboard,
        completedStepIds: Set<String> = emptySet(),
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ): List<CareFactoryExecutionPlan> = nextManifests(dashboard, maxGames, maxTasksPerGame)
        .map { it.executionPlan(completedStepIds) }
}
