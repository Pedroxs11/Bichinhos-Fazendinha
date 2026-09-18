package com.minhafazendinha.game

/**
 * High-level interaction loop shared by every care game.
 * It owns playback state and exposes simple commands for Android views:
 * dispatch an action, tick animation time, and render the returned snapshot.
 */
data class CareGameInteractionSnapshot(
    val gameId: String,
    val actionId: String?,
    val playback: CareGamePlaybackState?,
    val scene: CareGameScene?,
    val progress: Float,
    val busy: Boolean
)

class CareGameInteractionLoop(
    private val gameId: String,
    private val playback: CareGamePlaybackController
) {
    private var current: CareGamePlaybackState? = null

    fun dispatch(actionId: String, resultState: String? = null): CareGameInteractionSnapshot {
        current = playback.startAction(actionId, resultState)
        return snapshot()
    }

    fun tick(deltaMs: Int): CareGameInteractionSnapshot {
        require(deltaMs >= 0) { "deltaMs must be >= 0" }
        current = current?.let { playback.advance(it, deltaMs) }
        return snapshot()
    }

    fun reset(): CareGameInteractionSnapshot {
        current = null
        return snapshot()
    }

    fun snapshot(): CareGameInteractionSnapshot {
        val state = current
        return CareGameInteractionSnapshot(
            gameId = gameId,
            actionId = state?.sequence?.actionId,
            playback = state,
            scene = state?.scene,
            progress = state?.progress ?: 0f,
            busy = state?.finished == false
        )
    }
}

/**
 * One-line factory entry point for future screens.
 * A princess, garage or farm screen only needs its game id to inherit
 * standardized sequencing, accessibility preferences and timing.
 */
object CareGameInteractionFactory {
    fun create(
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming()
    ): CareGameInteractionLoop = CareGameInteractionLoop(
        gameId = gameId,
        playback = CareGamePlaybackCatalog.create(gameId, preferences, timing)
    )

    fun catalog(
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming()
    ): Map<String, CareGameInteractionLoop> = CareGamePlaybackCatalog
        .catalog(preferences, timing)
        .mapValues { (gameId, controller) -> CareGameInteractionLoop(gameId, controller) }
}
