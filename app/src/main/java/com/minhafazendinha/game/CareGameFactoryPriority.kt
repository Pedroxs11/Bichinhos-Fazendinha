package com.minhafazendinha.game

/**
 * Converts the factory dashboard into a deterministic production order.
 * Future games inherit the same decision rules, so art and QA can always work
 * on the item that unlocks playable content fastest instead of sorting by hand.
 */
data class CareFactoryPriority(
    val gameId: String,
    val rank: Int,
    val score: Int,
    val stage: CareFactoryPriorityStage,
    val nextTasks: List<String>,
    val readyForProduction: Boolean
)

enum class CareFactoryPriorityStage {
    VISUAL_CONTRACT,
    INTERNAL_TEST,
    FINAL_ART,
    READY
}

data class CareFactoryPriorityQueue(
    val items: List<CareFactoryPriority>
) {
    val next: CareFactoryPriority? get() = items.firstOrNull { !it.readyForProduction }
    val blockedCount: Int get() = items.count { !it.readyForProduction }
    val readyCount: Int get() = items.count { it.readyForProduction }
}

object CareGameFactoryPriorityEngine {
    fun build(dashboard: CareFactoryDashboard): CareFactoryPriorityQueue {
        val ordered = dashboard.games
            .map { snapshot -> snapshot to score(snapshot) }
            .sortedWith(
                compareByDescending<Pair<CareFactoryGameSnapshot, Int>> { it.second }
                    .thenBy { it.first.gameId }
            )

        return CareFactoryPriorityQueue(
            ordered.mapIndexed { index, (snapshot, score) ->
                CareFactoryPriority(
                    gameId = snapshot.gameId,
                    rank = index + 1,
                    score = score,
                    stage = stage(snapshot),
                    nextTasks = snapshot.nextVisualPriorities.distinct().take(4),
                    readyForProduction = snapshot.readyForProduction
                )
            }
        )
    }

    private fun stage(snapshot: CareFactoryGameSnapshot): CareFactoryPriorityStage = when {
        snapshot.readyForProduction -> CareFactoryPriorityStage.READY
        !snapshot.visualContractReady -> CareFactoryPriorityStage.VISUAL_CONTRACT
        !snapshot.readyForInternalTest -> CareFactoryPriorityStage.INTERNAL_TEST
        else -> CareFactoryPriorityStage.FINAL_ART
    }

    private fun score(snapshot: CareFactoryGameSnapshot): Int {
        if (snapshot.readyForProduction) return 0
        val progress = (snapshot.finalAssetProgress.coerceIn(0f, 1f) * 100).toInt()
        val unlockBonus = when (stage(snapshot)) {
            CareFactoryPriorityStage.VISUAL_CONTRACT -> 300
            CareFactoryPriorityStage.INTERNAL_TEST -> 200
            CareFactoryPriorityStage.FINAL_ART -> 100
            CareFactoryPriorityStage.READY -> 0
        }
        val feedbackGap = (snapshot.requiredFeedbackStates - snapshot.configuredFeedbackStates)
            .coerceAtLeast(0)
        return unlockBonus + progress - feedbackGap * 5
    }
}
