package com.minhafazendinha.game

/** A small, parallelizable slice of the shared production backlog. */
data class CareProductionBatch(
    val tasks: List<CareProductionTask>
) {
    val gameIds: Set<String> get() = tasks.mapTo(linkedSetOf()) { it.gameId }
    val blockingCount: Int get() = tasks.count { it.blocksInternalTest }
    val isEmpty: Boolean get() = tasks.isEmpty()
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
