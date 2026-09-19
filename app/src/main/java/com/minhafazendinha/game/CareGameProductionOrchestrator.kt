package com.minhafazendinha.game

/**
 * Single entry point for the reusable care-game production factory.
 * It joins readiness, release gating, prioritisation and resumable batching so
 * tooling and future games do not need to reproduce the same orchestration.
 */
data class CareFactoryProductionState(
    val dashboard: CareFactoryDashboard,
    val wave: CareProductionWave,
    val nextBatch: CareProductionBatch
) {
    val progress: CareProductionProgress get() = wave.progress()
    val isComplete: Boolean get() = progress.remaining == 0
    val blockedGames: Set<String> get() =
        dashboard.games.filterNot { it.readyForInternalTest }.mapTo(linkedSetOf()) { it.gameId }
    val visualReleasePendingGames: Set<String> get() =
        dashboard.games.filterNot { it.readyForVisualRelease }.mapTo(linkedSetOf()) { it.gameId }
}

object CareGameProductionOrchestrator {
    fun build(
        readinessByGame: Map<String, CareGameAssetReadiness>,
        finalKeysByGame: Map<String, Set<String>>,
        completedKeys: Set<String> = emptySet(),
        batchSize: Int = 4
    ): CareFactoryProductionState {
        require(batchSize > 0) { "batchSize must be greater than zero" }
        val dashboard = CareGameFactoryDashboard.build(readinessByGame, finalKeysByGame)
        val plan = CareGameProductionPlanner.build(dashboard)
        val wave = CareProductionWave(plan = plan, completedKeys = completedKeys)
        return CareFactoryProductionState(
            dashboard = dashboard,
            wave = wave,
            nextBatch = wave.nextBatch(batchSize)
        )
    }

    fun complete(
        state: CareFactoryProductionState,
        gameId: String,
        assetKey: String,
        batchSize: Int = 4
    ): CareFactoryProductionState {
        require(batchSize > 0) { "batchSize must be greater than zero" }
        val wave = state.wave.markCompleted(gameId, assetKey)
        return state.copy(wave = wave, nextBatch = wave.nextBatch(batchSize))
    }

    fun completedKeys(state: CareFactoryProductionState): Set<String> = state.wave.completedKeys
}
