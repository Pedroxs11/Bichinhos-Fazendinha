package com.minhafazendinha.game

data class CareFactoryRunResult(
    val workPackage: CareFactoryWorkPackage,
    val completed: List<CareProductionTask>,
    val checkpointDue: Boolean,
    val checkpoint: CareProductionCheckpoint?
) {
    val isComplete: Boolean get() = workPackage.isComplete
    val remaining: Int get() = workPackage.remaining
}

/** Executes production packages without exposing session bookkeeping to each game. */
object CareGameFactoryRun {
    fun next(
        workPackage: CareFactoryWorkPackage,
        maxTasks: Int = workPackage.preset.batchSize
    ): CareFactoryRunResult {
        require(maxTasks > 0) { "maxTasks must be greater than zero" }
        val completed = workPackage.tasks.take(maxTasks)
        return result(workPackage.completeBatch(completed), completed)
    }

    fun complete(
        workPackage: CareFactoryWorkPackage,
        completedTasks: Iterable<CareProductionTask>
    ): CareFactoryRunResult {
        val completed = completedTasks.toList()
        return result(workPackage.completeBatch(completed), completed)
    }

    fun checkpointSaved(result: CareFactoryRunResult): CareFactoryRunResult {
        if (!result.checkpointDue) return result
        return result.copy(
            workPackage = result.workPackage.checkpointSaved(),
            checkpointDue = false,
            checkpoint = null
        )
    }

    private fun result(
        workPackage: CareFactoryWorkPackage,
        completed: List<CareProductionTask>
    ) = CareFactoryRunResult(
        workPackage = workPackage,
        completed = completed,
        checkpointDue = workPackage.shouldCheckpoint,
        checkpoint = workPackage.session.checkpoint().takeIf { workPackage.shouldCheckpoint }
    )
}
