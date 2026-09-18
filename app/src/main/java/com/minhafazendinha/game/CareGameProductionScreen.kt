package com.minhafazendinha.game

import android.content.Context
import android.widget.FrameLayout

/**
 * Reusable screen-level controller for care games.
 * Activities/fragments can bind action ids once and inherit the production host,
 * playback lifecycle and visual snapshot state without rebuilding glue code.
 */
class CareGameProductionScreen(
    context: Context,
    val gameId: String,
    preferences: CareGameScenePreferences = CareGameScenePreferences(),
    timing: CareGameSequenceTiming = CareGameSequenceTiming(),
    private val onSnapshot: (CareGameInteractionSnapshot) -> Unit = {}
) {
    val view: CareGameProductionHost = CareGameProductionHostFactory.create(
        context = context,
        gameId = gameId,
        preferences = preferences,
        timing = timing,
        onSnapshot = { snapshot ->
            lastSnapshot = snapshot
            onSnapshot(snapshot)
        }
    )

    var lastSnapshot: CareGameInteractionSnapshot? = null
        private set

    /** Bind a UI control to a factory action with optional completion state. */
    fun bindAction(
        control: android.view.View,
        actionId: String,
        resultState: String? = null
    ) {
        control.isClickable = true
        control.setOnClickListener { play(actionId, resultState) }
    }

    fun play(actionId: String, resultState: String? = null) {
        view.play(actionId, resultState)
    }

    fun reset() = view.resetGame()

    fun pause() = view.pausePlayback()

    /** Drop the shared production view into any existing activity/fragment container. */
    fun attachTo(container: FrameLayout) {
        (view.parent as? android.view.ViewGroup)?.removeView(view)
        container.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }
}

object CareGameProductionScreenFactory {
    fun create(
        context: Context,
        gameId: String,
        preferences: CareGameScenePreferences = CareGameScenePreferences(),
        timing: CareGameSequenceTiming = CareGameSequenceTiming(),
        onSnapshot: (CareGameInteractionSnapshot) -> Unit = {}
    ): CareGameProductionScreen = CareGameProductionScreen(
        context = context,
        gameId = gameId,
        preferences = preferences,
        timing = timing,
        onSnapshot = onSnapshot
    )
}
