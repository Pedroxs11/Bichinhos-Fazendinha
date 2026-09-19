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

data class CareFactoryWorkPackage(
    val session: CareProductionSession,
    val tasks: List<CareProductionTask>,
    val checkpoint: CareProductionCheckpoint
) {
    val isComplete: Boolean get() = session.isComplete
    val remaining: Int get() = session.progress.remaining
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
        return session.toWorkPackage()
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
        return session.toWorkPackage()
    }

    private fun CareProductionSession.toWorkPackage() = CareFactoryWorkPackage(
        session = this,
        tasks = nextBatch.tasks,
        checkpoint = checkpoint()
    )
}