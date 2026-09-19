package com.minhafazendinha.game

/**
 * One reusable factory snapshot for QA/debug screens and future game packs.
 * It turns asset and visual-contract readiness into an actionable production state
 * without adding game-specific branching to the host UI.
 */
data class CareFactoryGameSnapshot(
    val gameId: String,
    val readyForInternalTest: Boolean,
    val readyForVisualRelease: Boolean,
    val finalAssetProgress: Float,
    val readyAssets: Int,
    val totalAssets: Int,
    val nextVisualPriorities: List<String>,
    val visualContractReady: Boolean = true,
    val configuredFeedbackStates: Int = 0,
    val requiredFeedbackStates: Int = 0,
    val visualIssues: List<String> = emptyList()
) {
    val readyForProduction: Boolean
        get() = readyForVisualRelease && visualContractReady
}

data class CareFactoryDashboard(
    val games: List<CareFactoryGameSnapshot>
) {
    val internalTestReadyCount: Int get() = games.count { it.readyForInternalTest }
    val visualReleaseReadyCount: Int get() = games.count { it.readyForVisualRelease }
    val productionReadyCount: Int get() = games.count { it.readyForProduction }
    val visualContractReadyCount: Int get() = games.count { it.visualContractReady }
    val overallFinalProgress: Float get() =
        if (games.isEmpty()) 1f else games.map { it.finalAssetProgress }.average().toFloat()
}

object CareGameFactoryDashboard {
    fun build(
        readinessByGame: Map<String, CareGameAssetReadiness>,
        finalKeysByGame: Map<String, Set<String>>
    ): CareFactoryDashboard {
        val packs = CareGamePackFactory.catalog()
        val snapshots = packs.keys.sorted().map { gameId ->
            val pack = requireNotNull(packs[gameId])
            val visualReadiness = CareGameVisualSpecFactory.build(pack).productionReadiness()
            val manifest = CareGameArtManifestFactory.create(gameId)
            val required = manifest.requiredKeys()
            val readiness = readinessByGame[gameId] ?: CareGameAssetReadiness(
                gameId = gameId,
                available = emptySet(),
                missing = required
            )
            val production = CareGameArtPipeline.status(gameId, readiness)
            val release = CareGameVisualReleaseGate.evaluate(
                gameId = gameId,
                readiness = readiness,
                finalKeys = finalKeysByGame[gameId].orEmpty()
            )
            CareFactoryGameSnapshot(
                gameId = gameId,
                readyForInternalTest = release.readyForInternalTest && visualReadiness.ready,
                readyForVisualRelease = release.readyForVisualRelease && visualReadiness.ready,
                finalAssetProgress = release.finalAssetProgress,
                readyAssets = production.readyCount,
                totalAssets = production.totalCount,
                nextVisualPriorities = CareGameVisualReleaseGate.nextVisualPriorities(release) +
                    visualReadiness.issues.map { "visual:$it" },
                visualContractReady = visualReadiness.ready,
                configuredFeedbackStates = visualReadiness.configuredFeedbackStates,
                requiredFeedbackStates = visualReadiness.requiredFeedbackStates,
                visualIssues = visualReadiness.issues
            )
        }
        return CareFactoryDashboard(snapshots)
    }
}
