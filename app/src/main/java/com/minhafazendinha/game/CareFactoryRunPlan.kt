package com.minhafazendinha.game

/** A deterministic preview of the next factory wave, useful for tooling and UI. */
data class CareFactoryRunPlan(
    val tasks: List<CareProductionTask>,
    val remainingBefore: Int,
    val remainingAfter: Int,
    val checkpointAfterRun: Boolean
) {
    val isEmpty: Boolean get() = tasks.isEmpty()
    val completesProduction: Boolean get() = remainingAfter == 0
}

/**
 * Plans production without mutating the package. Keeping planning separate from
 * execution lets future games, dashboards and visual-polish tooling show the
 * exact next wave before committing it.
 */
object CareGameFactoryPlanner {
    fun plan(
        workPackage: CareFactoryWorkPackage,
        maxTasks: Int = workPackage.preset.batchSize
    ): CareFactoryRunPlan {
        require(maxTasks > 0) { "maxTasks must be greater than zero" }
        val tasks = workPackage.tasks.take(maxTasks)
        val remainingAfter = (workPackage.remaining - tasks.size).coerceAtLeast(0)
        return CareFactoryRunPlan(
            tasks = tasks,
            remainingBefore = workPackage.remaining,
            remainingAfter = remainingAfter,
            checkpointAfterRun = remainingAfter == 0 ||
                workPackage.completedSinceCheckpoint + tasks.size >= workPackage.preset.checkpointEvery
        )
    }

    fun execute(
        workPackage: CareFactoryWorkPackage,
        plan: CareFactoryRunPlan = plan(workPackage)
    ): CareFactoryRunResult {
        if (plan.tasks.isEmpty()) return CareGameFactoryRun.complete(workPackage, emptyList())
        return CareGameFactoryRun.complete(workPackage, plan.tasks)
    }
}
