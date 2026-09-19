package com.minhafazendinha.game

/**
 * Reusable configuration for production waves. Keeping these decisions outside
 * individual games makes it cheap to tune asset throughput for the whole
 * factory without duplicating setup code in each animal.
 */
data class CareFactoryPreset(
    val batchSize: Int = 4,
    val checkpointEvery: Int = 4
) {
    init {
        require(batchSize > 0) { "batchSize must be greater than zero" }
        require(checkpointEvery > 0) { "checkpointEvery must be greater than zero" }
    }
}

/**
 * Executable unit of work for the reusable game factory. Besides exposing the
 * next production tasks, it keeps checkpoint cadence with the package so tools
 * and future games do not need to duplicate progress bookkeeping.
 */
data class CareFactoryWorkPackage(
    val session: CareProductionSession,
    val tasks: List<CareProductionTask>,
    val checkpoint: CareProductionCheckpoint,
    val preset: CareFactoryPreset = CareGameFactoryPreset.DEFAULT,
    val completedSinceCheckpoint: Int = 0
) {
    val isComplete: Boolean get() = session.isComplete
    val remaining: Int get() = session.progress.remaining
    val shouldCheckpoint: Boolean
        get() = isComplete || completedSinceCheckpoint >= preset.checkpointEvery

    fun complete(task: CareProductionTask): CareFactoryWorkPackage {
        val updated = session.complete(task.gameId, task.assetKey)
        return fromSession(updated, completedSinceCheckpoint + 1)
    }

    fun completeBatch(completedTasks: Iterable<CareProductionTask> = tasks): CareFactoryWorkPackage {
        val completed = completedTasks.toList()
        if (completed.isEmpty()) return this
        val updated = session.completeBatch(completed)
        return fromSession(updated, completedSinceCheckpoint + completed.size)
    }

    /** Marks the current checkpoint as persisted and starts a fresh cadence. */
    fun checkpointSaved(): CareFactoryWorkPackage = copy(
        checkpoint = session.checkpoint(),
        completedSinceCheckpoint = 0
    )

    private fun fromSession(
        updated: CareProductionSession,
        completedCount: Int
    ) = copy(
        session = updated,
        tasks = updated.nextBatch.tasks,
        checkpoint = updated.checkpoint(),
        completedSinceCheckpoint = completedCount
    )
}

/**
 * Small facade used by tooling and future games to start/resume production
 * with one preset. It intentionally delegates planning to the existing factory
 * so new games automatically inherit prioritisation and readiness rules.
 */
object CareGameFactoryPreset {
    val DEFAULT = CareFactoryPreset()
    val POLISH = CareFactoryPreset(batchSize = 6, checkpointEvery = 6)

    fun start(
        readinessByGame: Map<String, CareGameAssetReadiness>,
        finalKeysByGame: Map<String, Set<String>>,
        completedKeys: Set<String> = emptySet(),
        preset: CareFactoryPreset = DEFAULT
    ): CareFactoryWorkPackage {
        val session = CareGameProductionSession.start(
            readinessByGame = readinessByGame,
            finalKeysByGame = finalKeysByGame,
            completedKeys = completedKeys,
            batchSize = preset.batchSize
        )
        return session.toWorkPackage(preset)
    }

    fun resume(
        checkpoint: CareProductionCheckpoint,
        readinessByGame: Map<String, CareGameAssetReadiness>,
        finalKeysByGame: Map<String, Set<String>>,
        preset: CareFactoryPreset = DEFAULT
    ): CareFactoryWorkPackage {
        val session = CareGameProductionSession.resume(
            checkpoint = checkpoint,
            readinessByGame = readinessByGame,
            finalKeysByGame = finalKeysByGame,
            fallbackBatchSize = preset.batchSize
        )
        return session.toWorkPackage(preset)
    }

    private fun CareProductionSession.toWorkPackage(preset: CareFactoryPreset) = CareFactoryWorkPackage(
        session = this,
        tasks = nextBatch.tasks,
        checkpoint = checkpoint(),
        preset = preset
    )
}
