package com.minhafazendinha.game

/** A small, parallelizable slice of the shared production backlog. */
data class CareProductionBatch(
    val tasks: List<CareProductionTask>
) {
    val gameIds: Set<String> get() = tasks.mapTo(linkedSetOf()) { it.gameId }
    val blockingCount: Int get() = tasks.count { it.blocksInternalTest }
    val isEmpty: Boolean get() = tasks.isEmpty()
}

/** Snapshot used by tooling/UI to show how much of a production wave is complete. */
data class CareProductionProgress(
    val total: Int,
    val completed: Int,
    val remaining: Int,
    val percent: Int,
    val next: CareProductionTask?
)

/**
 * A resumable production wave. Completed asset keys can be persisted by callers,
 * allowing the same reusable factory to resume work without rebuilding manual lists.
 */
data class CareProductionWave(
    val plan: CareProductionPlan,
    val completedKeys: Set<String> = emptySet()
) {
    private fun key(task: CareProductionTask) = "${task.gameId}:${task.assetKey}"

    val remainingPlan: CareProductionPlan
        get() = CareProductionPlan(plan.tasks.filterNot { key(it) in completedKeys })

    fun markCompleted(task: CareProductionTask): CareProductionWave =
        copy(completedKeys = completedKeys + key(task))

    fun markCompleted(gameId: String, assetKey: String): CareProductionWave =
        copy(completedKeys = completedKeys + "$gameId:$assetKey")

    fun progress(): CareProductionProgress {
        val remaining = remainingPlan.tasks
        val total = plan.tasks.size
        val done = total - remaining.size
        return CareProductionProgress(
            total = total,
            completed = done,
            remaining = remaining.size,
            percent = if (total == 0) 100 else (done * 100 / total),
            next = remaining.firstOrNull()
        )
    }

    fun nextBatch(maxTasks: Int = 4): CareProductionBatch =
        CareGameProductionBatcher.next(remainingPlan, maxTasks)
}

/**
 * Turns the global backlog into deterministic work batches.
 * Blocking assets always win; afterwards work is spread across games so a new
 * title cannot starve the rest of the factory while visual polish is underway.
 */
object CareGameProductionBatcher {
    fun next(plan: CareProductionPlan, maxTasks: Int = 4): CareProductionBatch {
        require(maxTasks > 0) { "maxTasks must be greater than zero" }
        if (plan.tasks.isEmpty()) return CareProductionBatch(emptyList())

        val ordered = plan.tasks.sortedWith(
            compareByDescending<CareProductionTask> { it.blocksInternalTest }
                .thenBy { it.priority }
                .thenBy { it.gameId }
                .thenBy { it.assetKey }
        )
        val selected = mutableListOf<CareProductionTask>()
        val usedGames = mutableSetOf<String>()

        // First pass distributes work across titles, useful when producing art in parallel.
        ordered.forEach { task ->
            if (selected.size < maxTasks && usedGames.add(task.gameId)) selected += task
        }
        // Second pass fills remaining capacity with the globally highest priorities.
        ordered.forEach { task ->
            if (selected.size < maxTasks && task !in selected) selected += task
        }
        return CareProductionBatch(selected)
    }

    fun batches(plan: CareProductionPlan, maxTasks: Int = 4): List<CareProductionBatch> {
        require(maxTasks > 0) { "maxTasks must be greater than zero" }
        val remaining = plan.tasks.toMutableList()
        val result = mutableListOf<CareProductionBatch>()
        while (remaining.isNotEmpty()) {
            val batch = next(CareProductionPlan(remaining), maxTasks)
            result += batch
            remaining.removeAll(batch.tasks.toSet())
        }
        return result
    }
}
