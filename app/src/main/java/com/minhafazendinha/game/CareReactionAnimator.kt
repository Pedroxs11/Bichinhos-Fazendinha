package com.minhafazendinha.game

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Feedback visual reutilizavel para qualquer jogo de cuidado.
 * O jogo informa apenas a acao; a fabrica cuida de icone, movimento e timing.
 */
data class CareReactionStyle(
    val icon: String,
    val durationMs: Long = 180L,
    val holdMs: Long = 420L
)

object CareReactionCatalog {
    private val defaults = mapOf(
        "feed" to CareReactionStyle("🍎✨"),
        "bath" to CareReactionStyle("💦🫧"),
        "bathe" to CareReactionStyle("💦🫧"),
        "brush" to CareReactionStyle("✨🧹"),
        "play" to CareReactionStyle("🏐💖"),
        "idle" to CareReactionStyle("💚")
    )

    fun forAction(actionId: String?): CareReactionStyle =
        defaults[actionId?.lowercase()] ?: defaults.getValue("idle")
}

class CareReactionAnimator(private val host: FrameLayout) {
    private val badge = TextView(host.context).apply {
        textSize = 48f
        gravity = Gravity.CENTER
        alpha = 0f
        visibility = View.INVISIBLE
        setShadowLayer(8f, 0f, 3f, 0x88000000.toInt())
    }

    init {
        host.addView(badge, FrameLayout.LayoutParams(190, 150).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, 38, 24, 0)
        })
    }

    fun play(actionId: String?) {
        val style = CareReactionCatalog.forAction(actionId)
        badge.animate().cancel()
        badge.text = style.icon
        badge.visibility = View.VISIBLE
        badge.alpha = 0f
        badge.translationY = 35f
        badge.scaleX = .65f
        badge.scaleY = .65f
        badge.animate()
            .alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
            .setDuration(style.durationMs)
            .withEndAction {
                badge.animate().alpha(0f).translationY(-45f)
                    .setStartDelay(style.holdMs).setDuration(260L)
                    .withEndAction { badge.visibility = View.INVISIBLE }
                    .start()
            }.start()
    }
}
