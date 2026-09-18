package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout

/**
 * Drop-in Android host for games produced by the reusable care-game factory.
 * New screens can now provide only a game id and dispatch actions; scene layers,
 * playback timing, rendering and cleanup are shared automatically.
 */
class CareGameProductionHost(
    context: Context,
    private val gameId: String,
    preferences: CareGameScenePreferences = CareGameScenePreferences(),
    timing: CareGameSequenceTiming = CareGameSequenceTiming(),
    onSnapshot: (CareGameInteractionSnapshot) -> Unit = {}
) : FrameLayout(context) {

    private val sceneHost = FrameLayout(context)
    private val renderer: CareGameSceneRenderer
    private val player: CareGameScenePlayer

    init {
        setBackgroundColor(Color.TRANSPARENT)
        sceneHost.setBackgroundColor(Color.TRANSPARENT)
        addView(sceneHost, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        renderer = CareGameSceneRenderer(context, sceneHost)
        player = CareGameScenePlayerFactory.create(
            gameId = gameId,
            renderer = renderer,
            preferences = preferences,
            timing = timing,
            onSnapshot = onSnapshot
        )
    }

    /** Runs a complete idle -> action -> result sequence using factory timing. */
    fun play(actionId: String, resultState: String? = null) {
        player.play(actionId, resultState)
    }

    /** Clears the current interaction and rendered scene. */
    fun resetGame() {
        player.reset()
    }

    /** Stops frame callbacks while keeping the last rendered scene visible. */
    fun pausePlayback() {
        player.stop()
    }

    override fun onDetachedFromWindow() {
        player.stop()
        super.onDetachedFromWindow()
    }
}

/**
 * Small factory facade used by future activities/fragments. Keeping creation here
 * prevents every game from rebuilding Android renderer/player wiring.
 */
object CareGameProductionHostFactory {
    fun create(
        context: Context,
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming(),
        onSnapshot: (CareGameInteractionSnapshot) -> Unit = {}
    ): CareGameProductionHost = CareGameProductionHost(
        context = context,
        gameId = gameId,
        preferences = preferences,
        timing = timing,
        onSnapshot = onSnapshot
    )
}
