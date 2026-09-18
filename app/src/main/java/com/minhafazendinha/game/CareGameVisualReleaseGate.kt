package com.minhafazendinha.game

/**
 * Reusable release gate for every factory-made care game.
 * Combines the art manifest, production queue and export handoff so a game can
 * move from fallback art to final polish without bespoke release logic.
 */
data class CareVisualReleaseIssue(
    val key: String,
    val reason: String,
    val blocking: Boolean
)

data class CareVisualReleaseReport(
    val gameId: String,
    val readyForInternalTest: Boolean,
    val readyForVisualRelease: Boolean,
    val finalAssetProgress: Float,
    val issues: List<CareVisualReleaseIssue>
) {
    val blockers: List<CareVisualReleaseIssue> get() = issues.filter { it.blocking }
}

object CareGameVisualReleaseGate {
    fun evaluate(
        gameId: String,
        readiness: CareGameAssetReadiness,
        finalKeys: Set<String>
    ): CareVisualReleaseReport {
        val manifest = CareGameArtManifestFactory.create(gameId)
        val handoff = CareGameVisualHandoffFactory.create(gameId)
        val production = CareGameArtPipeline.status(gameId, readiness)
        val required = manifest.requiredKeys()
        val finalRequired = required.intersect(finalKeys)
        val missing = required.filterNot { it in readiness.available }
        val fallback = required.filter { it in readiness.available && it !in finalKeys }
        val issues = buildList {
            missing.forEach { add(CareVisualReleaseIssue(it, "asset_missing", true)) }
            fallback.forEach { add(CareVisualReleaseIssue(it, "using_fallback_art", false)) }
            handoff.validate().forEach { add(CareVisualReleaseIssue("handoff", it, true)) }
            if (!production.complete) add(CareVisualReleaseIssue("production", "production_queue_incomplete", true))
        }
        val progress = if (required.isEmpty()) 1f else finalRequired.size.toFloat() / required.size
        return CareVisualReleaseReport(
            gameId = gameId,
            readyForInternalTest = missing.isEmpty() && handoff.validate().isEmpty(),
            readyForVisualRelease = issues.isEmpty() && progress == 1f,
            finalAssetProgress = progress,
            issues = issues
        )
    }

    fun nextVisualPriorities(report: CareVisualReleaseReport): List<String> =
        report.issues.sortedByDescending { it.blocking }.map { it.key }.distinct()
}
