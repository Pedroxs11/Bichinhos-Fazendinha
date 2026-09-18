package com.minhafazendinha.game

import android.os.SystemClock
import android.view.Choreographer

/**
 * Reusable Android bridge between the UI-agnostic interaction loop and the
 * scene renderer. Future care games only dispatch actions; frame scheduling,
 * progress and scene rendering stay inside the factory.
 */
class CareGameScenePlayer(
    private val loop: CareGameInteractionLoop,
    private val renderer: CareGameSceneRenderer,
    private val onSnapshot: (CareGameInteractionSnapshot) -> Unit = {}
) : Choreographer.FrameCallback {
    private var running = false
    private var lastFrameMs = 0L

    fun play(actionId: String, resultState: String? = null) {
        stopFrames()
        val snapshot = loop.dispatch(actionId, resultState)
        render(snapshot)
        if (snapshot.busy) startFrames()
    }

    fun reset() {
        stopFrames()
        renderer.clear()
        onSnapshot(loop.reset())
    }

    fun stop() {
        stopFrames()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        val nowMs = frameTimeNanos / 1_000_000L
        val delta = if (lastFrameMs == 0L) 0 else (nowMs - lastFrameMs).coerceAtLeast(0L).coerceAtMost(100L).toInt()
        lastFrameMs = nowMs
        val snapshot = loop.tick(delta)
        render(snapshot)
        if (snapshot.busy) {
            Choreographer.getInstance().postFrameCallback(this)
        } else {
            running = false
        }
    }

    private fun render(snapshot: CareGameInteractionSnapshot) {
        snapshot.scene?.let(renderer::render)
        onSnapshot(snapshot)
    }

    private fun startFrames() {
        running = true
        lastFrameMs = SystemClock.uptimeMillis()
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun stopFrames() {
        if (running) Choreographer.getInstance().removeFrameCallback(this)
        running = false
        lastFrameMs = 0L
    }
}

object CareGameScenePlayerFactory {
    fun create(
        gameId: String,
        renderer: CareGameSceneRenderer,
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming(),
        onSnapshot: (CareGameInteractionSnapshot) -> Unit = {}
    ): CareGameScenePlayer = CareGameScenePlayer(
        loop = CareGameInteractionFactory.create(gameId, preferences, timing),
        renderer = renderer,
        onSnapshot = onSnapshot
    )
}
