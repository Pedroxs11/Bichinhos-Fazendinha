package com.minhafazendinha.game

/**
 * Serializable-friendly snapshot of factory progress. Callers only need to persist
 * primitive collections; the production plan is rebuilt from the live catalog.
 */
data class CareProductionCheckpoint(
    val completedKeys: Set<String>,
    val batchSize: Int,
    val progressPercent: Int,
    val pendingGameIds: Set<String>
)

/**
 * Shared save/restore boundary for production tooling. This keeps persistence
 * concerns out of individual games and makes a production wave safely resumable.
 */
object CareGameProductionCheckpoint {
    fun capture(state: CareFactoryProductionState): CareProductionCheckpoint =
        CareProductionCheckpoint(
            completedKeys = state.wave.completedKeys,
            batchSize = state.nextBatch.tasks.size.coerceAtLeast(1),
            progressPercent = state.progress.percent,
            pendingGameIds = state.wave.remainingPlan.tasks
                .mapTo(linkedSetOf()) { it.gameId }
        )

    fun restore(
        checkpoint: CareProductionCheckpoint,
        readinessByGame: Map<String, CareGameAssetReadiness>,
        finalKeysByGame: Map<String, Set<String>>,
        fallbackBatchSize: Int = 4
    ): CareFactoryProductionState {
        val size = checkpoint.batchSize.takeIf { it > 0 } ?: fallbackBatchSize
        return CareGameProductionOrchestrator.build(
            readinessByGame = readinessByGame,
            finalKeysByGame = finalKeysByGame,
            completedKeys = checkpoint.completedKeys,
            batchSize = size
        )
    }
}
