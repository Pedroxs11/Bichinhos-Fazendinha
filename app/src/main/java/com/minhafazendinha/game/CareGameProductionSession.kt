package com.minhafazendinha.game

/**
 * High-level mutable-free session for the reusable production factory.
 * Tooling can keep one value, apply completed assets and persist a checkpoint
 * without knowing how dashboard, wave and batching are assembled.
 */
data class CareProductionSession(
    val state: CareFactoryProductionState,
    val batchSize: Int
) {
    val progress: CareProductionProgress get() = state.progress
    val nextBatch: CareProductionBatch get() = state.nextBatch
    val isComplete: Boolean get() = state.isComplete

    fun complete(gameId: String, assetKey: String): CareProductionSession = copy(
        state = CareGameProductionOrchestrator.complete(
            state = state,
            gameId = gameId,
            assetKey = assetKey,
            batchSize = batchSize
        )
    )

    fun completeBatch(tasks: Iterable<CareProductionTask>): CareProductionSession =
        tasks.fold(this) { session, task -> session.complete(task.gameId, task.assetKey) }

    fun checkpoint(): CareProductionCheckpoint = CareGameProductionCheckpoint.capture(state)
}

object CareGameProductionSession {
    fun start(
        readinessByGame: Map<String, CareGameAssetReadiness>,
        finalKeysByGame: Map<String, Set<String>>,
        completedKeys: Set<String> = emptySet(),
        batchSize: Int = 4
    ): CareProductionSession {
        require(batchSize > 0) { "batchSize must be greater than zero" }
        return CareProductionSession(
            state = CareGameProductionOrchestrator.build(
                readinessByGame = readinessByGame,
                finalKeysByGame = finalKeysByGame,
                completedKeys = completedKeys,
                batchSize = batchSize
            ),
            batchSize = batchSize
        )
    }

    fun resume(
        checkpoint: CareProductionCheckpoint,
        readinessByGame: Map<String, CareGameAssetReadiness>,
        finalKeysByGame: Map<String, Set<String>>,
        fallbackBatchSize: Int = 4
    ): CareProductionSession {
        val size = checkpoint.batchSize.takeIf { it > 0 } ?: fallbackBatchSize
        return CareProductionSession(
            state = CareGameProductionCheckpoint.restore(
                checkpoint = checkpoint,
                readinessByGame = readinessByGame,
                finalKeysByGame = finalKeysByGame,
                fallbackBatchSize = fallbackBatchSize
            ),
            batchSize = size
        )
    }
}
