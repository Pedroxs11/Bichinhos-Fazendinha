package com.minhafazendinha.game

/**
 * One reusable factory snapshot for QA/debug screens and future game packs.
 * It turns asset readiness into an actionable production state without adding
 * game-specific branching to the host UI.
 */
data class CareFactoryGameSnapshot(
    val gameId: String,
    val readyForInternalTest: Boolean,
    val readyForVisualRelease: Boolean,
    val finalAssetProgress: Float,
    val readyAssets: Int,
    val totalAssets: Int,
    val nextVisualPriorities: List<String>
)

data class CareFactoryDashboard(
    val games: List<CareFactoryGameSnapshot>
) {
    val internalTestReadyCount: Int get() = games.count { it.readyForInternalTest }
    val visualReleaseReadyCount: Int get() = games.count { it.readyForVisualRelease }
    val overallFinalProgress: Float get() =
        if (games.isEmpty()) 1f else games.map { it.finalAssetProgress }.average().toFloat()
}

object CareGameFactoryDashboard {
    fun build(
        readinessByGame: Map<String, CareGameAssetReadiness>,
        finalKeysByGame: Map<String, Set<String>>
    ): CareFactoryDashboard {
        val snapshots = CareGamePackFactory.catalog().keys.sorted().map { gameId ->
            val manifest = CareGameArtManifestFactory.create(gameId)
            val readiness = readinessByGame[gameId] ?: CareGameAssetReadiness(
                gameId = gameId,
                required = manifest.requiredKeys(),
                available = emptySet()
            )
            val production = CareGameArtPipeline.status(gameId, readiness)
            val release = CareGameVisualReleaseGate.evaluate(
                gameId = gameId,
                readiness = readiness,
                finalKeys = finalKeysByGame[gameId].orEmpty()
            )
            CareFactoryGameSnapshot(
                gameId = gameId,
                readyForInternalTest = release.readyForInternalTest,
                readyForVisualRelease = release.readyForVisualRelease,
                finalAssetProgress = release.finalAssetProgress,
                readyAssets = production.readyCount,
                totalAssets = production.totalCount,
                nextVisualPriorities = CareGameVisualReleaseGate.nextVisualPriorities(release)
            )
        }
        return CareFactoryDashboard(snapshots)
    }
}
