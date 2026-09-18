package com.minhafazendinha.game

/**
 * Reusable bridge between the art manifest and runtime readiness.
 * Gives every future game the same deterministic production queue and progress model.
 */
data class CareArtProductionItem(
    val key: String,
    val role: String,
    val transparent: Boolean,
    val ready: Boolean
)

data class CareArtProductionStatus(
    val gameId: String,
    val items: List<CareArtProductionItem>
) {
    val readyCount: Int get() = items.count { it.ready }
    val totalCount: Int get() = items.size
    val progress: Float get() = if (items.isEmpty()) 1f else readyCount.toFloat() / totalCount
    val complete: Boolean get() = items.all { it.ready }
    val pending: List<CareArtProductionItem> get() = items.filterNot { it.ready }
}

object CareGameArtPipeline {
    fun status(gameId: String, readiness: CareGameAssetReadiness): CareArtProductionStatus {
        val manifest = CareGameArtManifestFactory.create(gameId)
        require(readiness.gameId == gameId) { "Readiness belongs to ${readiness.gameId}, expected $gameId" }
        return CareArtProductionStatus(
            gameId = gameId,
            items = manifest.slots.map { slot ->
                CareArtProductionItem(
                    key = slot.key,
                    role = slot.role,
                    transparent = slot.transparent,
                    ready = slot.key in readiness.available
                )
            }
        )
    }

    /** Ordered queue for artists/export automation: scene first, then idle, then actions. */
    fun pendingQueue(gameId: String, readiness: CareGameAssetReadiness): List<CareArtProductionItem> =
        status(gameId, readiness).pending

    /** Human-readable checklist useful in debug/QA screens and CI diagnostics. */
    fun checklist(gameId: String, readiness: CareGameAssetReadiness): List<String> {
        val status = status(gameId, readiness)
        return buildList {
            add("${status.readyCount}/${status.totalCount} production assets ready")
            status.items.forEach { item ->
                add("${if (item.ready) "OK" else "TODO"}: ${item.key} [${item.role}]${if (item.transparent) " transparent" else ""}")
            }
        }
    }
}
