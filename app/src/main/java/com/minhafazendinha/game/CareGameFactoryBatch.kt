package com.minhafazendinha.game

/** Turns the priority queue into deterministic batches for art, QA and generators. */
data class CareFactoryBatchItem(
    val gameId: String,
    val stage: CareFactoryPriorityStage,
    val tasks: List<String>,
    val score: Int
) {
    val workKey: String get() = "$gameId:${stage.name.lowercase()}"
}

data class CareFactoryBatch(val items: List<CareFactoryBatchItem>, val remainingGames: Int) {
    val isEmpty: Boolean get() = items.isEmpty()
    val taskCount: Int get() = items.sumOf { it.tasks.size }
    fun handoff() = CareFactoryBatchHandoff(items.map {
        CareFactoryWorkItem(it.workKey, it.gameId, it.stage, it.tasks, it.score)
    }, remainingGames)
}

data class CareFactoryWorkItem(
    val key: String,
    val gameId: String,
    val stage: CareFactoryPriorityStage,
    val tasks: List<String>,
    val priorityScore: Int
) { val readyToStart: Boolean get() = tasks.isNotEmpty() }

data class CareFactoryBatchHandoff(val workItems: List<CareFactoryWorkItem>, val remainingGames: Int) {
    val actionableItems get() = workItems.filter { it.readyToStart }
    val taskCount get() = actionableItems.sumOf { it.tasks.size }
    val complete get() = actionableItems.isEmpty() && remainingGames == 0
    fun manifests() = actionableItems.map { item ->
        CareFactoryWorkManifest(item.key, item.gameId, item.stage, item.priorityScore,
            item.tasks.mapIndexed { index, task -> CareFactoryWorkStep("${item.key}:${index + 1}", index + 1, task) })
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
    val stepCount get() = steps.size
    val readyToExecute get() = steps.isNotEmpty()
    fun executionPlan(completedStepIds: Set<String> = emptySet()): CareFactoryExecutionPlan {
        val knownIds = steps.mapTo(mutableSetOf()) { it.id }
        val completed = completedStepIds.intersect(knownIds)
        return CareFactoryExecutionPlan(workKey, gameId, stage, completed,
            steps.filterNot { it.id in completed }, steps.size)
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
    val completedSteps get() = completedStepIds.size
    val remainingSteps get() = pendingSteps.size
    val complete get() = remainingSteps == 0
    val progressPercent get() = if (totalSteps == 0) 100 else (completedSteps * 100) / totalSteps
    val nextStep get() = pendingSteps.minByOrNull { it.order }

    /** Stable snapshot that a worker can persist after each art/QA task. */
    fun checkpoint(): CareFactoryExecutionCheckpoint = CareFactoryExecutionCheckpoint(
        workKey, gameId, stage, completedStepIds, totalSteps
    )
}

/** Portable persisted state: independent from a specific generated manifest instance. */
data class CareFactoryExecutionCheckpoint(
    val workKey: String,
    val gameId: String,
    val stage: CareFactoryPriorityStage,
    val completedStepIds: Set<String>,
    val totalSteps: Int
) {
    val progressPercent get() = if (totalSteps == 0) 100 else (completedStepIds.size * 100) / totalSteps

    fun markCompleted(stepId: String): CareFactoryExecutionCheckpoint =
        copy(completedStepIds = completedStepIds + stepId)

    fun resume(manifest: CareFactoryWorkManifest): CareFactoryExecutionPlan {
        require(manifest.workKey == workKey) { "Checkpoint belongs to another work item" }
        return manifest.executionPlan(completedStepIds)
    }
}

object CareGameFactoryBatchPlanner {
    fun next(queue: CareFactoryPriorityQueue, maxGames: Int = 3, maxTasksPerGame: Int = 2): CareFactoryBatch {
        require(maxGames > 0) { "maxGames must be positive" }
        require(maxTasksPerGame > 0) { "maxTasksPerGame must be positive" }
        val pending = queue.items.filterNot { it.readyForProduction }
        val selected = pending.take(maxGames).map { priority ->
            CareFactoryBatchItem(priority.gameId, priority.stage,
                priority.nextTasks.distinct().take(maxTasksPerGame), priority.score)
        }
        return CareFactoryBatch(selected, (pending.size - selected.size).coerceAtLeast(0))
    }

    fun next(dashboard: CareFactoryDashboard, maxGames: Int = 3, maxTasksPerGame: Int = 2) =
        next(CareGameFactoryPriorityEngine.build(dashboard), maxGames, maxTasksPerGame)

    fun nextHandoff(dashboard: CareFactoryDashboard, maxGames: Int = 3, maxTasksPerGame: Int = 2) =
        next(dashboard, maxGames, maxTasksPerGame).handoff()

    fun nextManifests(dashboard: CareFactoryDashboard, maxGames: Int = 3, maxTasksPerGame: Int = 2) =
        nextHandoff(dashboard, maxGames, maxTasksPerGame).manifests()

    fun nextExecutionPlans(
        dashboard: CareFactoryDashboard,
        completedStepIds: Set<String> = emptySet(),
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ) = nextManifests(dashboard, maxGames, maxTasksPerGame).map { it.executionPlan(completedStepIds) }

    /** Resume many workers from persisted checkpoints in one deterministic pass. */
    fun resumeExecutionPlans(
        dashboard: CareFactoryDashboard,
        checkpoints: Collection<CareFactoryExecutionCheckpoint>,
        maxGames: Int = 3,
        maxTasksPerGame: Int = 2
    ): List<CareFactoryExecutionPlan> {
        val byWorkKey = checkpoints.associateBy { it.workKey }
        return nextManifests(dashboard, maxGames, maxTasksPerGame).map { manifest ->
            byWorkKey[manifest.workKey]?.resume(manifest) ?: manifest.executionPlan()
        }
    }
}
