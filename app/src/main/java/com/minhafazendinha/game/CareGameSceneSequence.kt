package com.minhafazendinha.game

/**
 * Reusable scene sequencing for every care game.
 * Screens can ask for a complete interaction sequence instead of rebuilding
 * idle -> pressed/action -> result transitions for every new character.
 */
data class CareGameSceneFrame(
    val scene: CareGameScene,
    val holdMs: Int
) {
    init {
        require(holdMs >= 0) { "holdMs must be >= 0" }
    }
}

data class CareGameSceneSequence(
    val gameId: String,
    val actionId: String,
    val frames: List<CareGameSceneFrame>
) {
    val durationMs: Int get() = frames.sumOf { it.holdMs }

    fun validate(): List<String> = buildList {
        if (gameId.isBlank()) add("sequence.game_id")
        if (actionId.isBlank()) add("sequence.action_id")
        if (frames.isEmpty()) add("sequence.frames")
        if (frames.any { it.scene.gameId != gameId }) add("sequence.mixed_games")
        if (frames.any { it.scene.validate().isNotEmpty() }) add("sequence.invalid_scene")
    }
}

/**
 * Standard timing profile. Future games inherit consistent tactile feedback and
 * motion rhythm while still allowing a game to override the timings when needed.
 */
data class CareGameSequenceTiming(
    val idleLeadMs: Int = 80,
    val actionHoldMs: Int = 420,
    val resultHoldMs: Int = 650
) {
    init {
        require(idleLeadMs >= 0 && actionHoldMs >= 0 && resultHoldMs >= 0) {
            "Scene sequence timings must be >= 0"
        }
    }
}

class CareGameSceneSequencer(
    private val scenes: CareGameSceneFactory,
    private val timing: CareGameSequenceTiming = CareGameSequenceTiming()
) {
    fun forAction(actionId: String, resultState: String? = null): CareGameSceneSequence {
        val action = scenes.forAction(actionId)
        val result = resultState?.let(scenes::forState)

        val frames = buildList {
            if (timing.idleLeadMs > 0) add(CareGameSceneFrame(scenes.idle(), timing.idleLeadMs))
            add(CareGameSceneFrame(action, timing.actionHoldMs))
            if (result != null) add(CareGameSceneFrame(result, timing.resultHoldMs))
        }

        return CareGameSceneSequence(
            gameId = action.gameId,
            actionId = actionId,
            frames = frames
        ).also { sequence ->
            require(sequence.validate().isEmpty()) {
                "Invalid scene sequence for ${sequence.gameId}/$actionId: ${sequence.validate().joinToString()}"
            }
        }
    }
}

object CareGameSceneSequencerFactory {
    fun create(
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming()
    ): CareGameSceneSequencer = CareGameSceneSequencer(
        scenes = CareGameSceneCatalog.create(gameId, preferences),
        timing = timing
    )

    fun catalog(
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming()
    ): Map<String, CareGameSceneSequencer> = CareGameSceneCatalog.catalog(preferences)
        .mapValues { (_, scenes) -> CareGameSceneSequencer(scenes, timing) }
}
