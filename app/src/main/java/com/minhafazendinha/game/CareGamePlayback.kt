package com.minhafazendinha.game

/**
 * Reusable, UI-agnostic playback engine for care-game scene sequences.
 * Views only need to render the current frame; timing/progression stays shared
 * across every game produced by the factory.
 */
data class CareGamePlaybackState(
    val sequence: CareGameSceneSequence,
    val frameIndex: Int,
    val elapsedInFrameMs: Int,
    val finished: Boolean
) {
    val frame: CareGameSceneFrame get() = sequence.frames[frameIndex]
    val scene: CareGameScene get() = frame.scene
    val progress: Float
        get() = if (sequence.durationMs <= 0) 1f else {
            val before = sequence.frames.take(frameIndex).sumOf { it.holdMs }
            ((before + elapsedInFrameMs).toFloat() / sequence.durationMs).coerceIn(0f, 1f)
        }
}

class CareGamePlaybackEngine {
    fun start(sequence: CareGameSceneSequence): CareGamePlaybackState {
        require(sequence.validate().isEmpty()) { "Cannot play invalid scene sequence" }
        require(sequence.frames.isNotEmpty()) { "Cannot play an empty scene sequence" }
        return CareGamePlaybackState(sequence, 0, 0, sequence.durationMs == 0)
    }

    fun advance(state: CareGamePlaybackState, deltaMs: Int): CareGamePlaybackState {
        require(deltaMs >= 0) { "deltaMs must be >= 0" }
        if (state.finished || deltaMs == 0) return state

        var index = state.frameIndex
        var elapsed = state.elapsedInFrameMs + deltaMs
        val frames = state.sequence.frames

        while (index < frames.lastIndex && elapsed >= frames[index].holdMs) {
            elapsed -= frames[index].holdMs
            index++
        }

        val lastFrame = frames[index]
        val finished = index == frames.lastIndex && elapsed >= lastFrame.holdMs
        return state.copy(
            frameIndex = index,
            elapsedInFrameMs = if (finished) lastFrame.holdMs else elapsed,
            finished = finished
        )
    }

    fun restart(state: CareGamePlaybackState): CareGamePlaybackState = start(state.sequence)
}

/**
 * One entry point for screens: creates standardized sequences and their player.
 * This is the bridge future games can reuse without rebuilding interaction timing.
 */
class CareGamePlaybackController(
    private val sequencer: CareGameSceneSequencer,
    private val engine: CareGamePlaybackEngine = CareGamePlaybackEngine()
) {
    fun startAction(actionId: String, resultState: String? = null): CareGamePlaybackState =
        engine.start(sequencer.forAction(actionId, resultState))

    fun advance(state: CareGamePlaybackState, deltaMs: Int): CareGamePlaybackState =
        engine.advance(state, deltaMs)
}

object CareGamePlaybackCatalog {
    fun create(
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming()
    ): CareGamePlaybackController = CareGamePlaybackController(
        CareGameSceneSequencerFactory.create(gameId, preferences, timing)
    )

    fun catalog(
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming()
    ): Map<String, CareGamePlaybackController> = CareGameSceneSequencerFactory
        .catalog(preferences, timing)
        .mapValues { (_, sequencer) -> CareGamePlaybackController(sequencer) }
}
