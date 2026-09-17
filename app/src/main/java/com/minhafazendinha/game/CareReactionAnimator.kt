package com.minhafazendinha.game

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

/** Visual feedback recipe reusable by farm, princess, car-care and future templates. */
data class CareReactionStyle(
    val icon: String,
    val durationMs: Long = 180L,
    val holdMs: Long = 420L,
    val exitMs: Long = 260L,
    val startScale: Float = .65f,
    val enterOffsetY: Float = 35f,
    val exitOffsetY: Float = -45f
)

data class CareReactionTheme(
    val styles: Map<String, CareReactionStyle>,
    val fallback: CareReactionStyle = CareReactionStyle("✨")
) {
    fun styleFor(actionId: String?): CareReactionStyle =
        styles[actionId?.lowercase()] ?: fallback
}

object CareReactionThemes {
    val farm = CareReactionTheme(
        styles = mapOf(
            "feed" to CareReactionStyle("🍎✨"),
            "bath" to CareReactionStyle("💦🫧"),
            "bathe" to CareReactionStyle("💦🫧"),
            "brush" to CareReactionStyle("✨🧹"),
            "play" to CareReactionStyle("🏐💖"),
            "idle" to CareReactionStyle("💚")
        ),
        fallback = CareReactionStyle("💚")
    )

    val princess = CareReactionTheme(
        styles = mapOf(
            "dress" to CareReactionStyle("👗✨"),
            "makeup" to CareReactionStyle("💄✨"),
            "hair" to CareReactionStyle("👑💖"),
            "play" to CareReactionStyle("🎀✨")
        )
    )

    val carCare = CareReactionTheme(
        styles = mapOf(
            "wash" to CareReactionStyle("🚿🚗"),
            "polish" to CareReactionStyle("✨🚘"),
            "repair" to CareReactionStyle("🔧✨"),
            "customize" to CareReactionStyle("🎨🚗")
        )
    )
}

class CareReactionAnimator(
    private val host: FrameLayout,
    private val theme: CareReactionTheme = CareReactionThemes.farm
) {
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
        val style = theme.styleFor(actionId)
        badge.animate().cancel()
        badge.text = style.icon
        badge.visibility = View.VISIBLE
        badge.alpha = 0f
        badge.translationY = style.enterOffsetY
        badge.scaleX = style.startScale
        badge.scaleY = style.startScale
        badge.animate()
            .alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
            .setDuration(style.durationMs)
            .withEndAction {
                badge.animate().alpha(0f).translationY(style.exitOffsetY)
                    .setStartDelay(style.holdMs).setDuration(style.exitMs)
                    .withEndAction { badge.visibility = View.INVISIBLE }
                    .start()
            }.start()
    }
}
