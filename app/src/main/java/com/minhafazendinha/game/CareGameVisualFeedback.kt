package com.minhafazendinha.game

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lightweight reusable action feedback that does not depend on final art.
 * Every care game gets visible polish immediately while bespoke EFFECTS art can
 * still replace/extend it later.
 */
class CareGameVisualFeedback(
    private val context: Context,
    private val host: FrameLayout,
    private val visualSpec: CareGameVisualSpec
) {
    private val particles = mutableListOf<View>()
    private var running: AnimatorSet? = null

    fun play(state: String, animated: Boolean) {
        clear()
        if (state == "idle" || !animated) return

        val preset = presetFor(state)
        val count = preset.count
        val duration = visualSpec.motion.completionFeedbackMs.toLong()
        val centerX = host.width * visualSpec.character.anchorX
        val centerY = host.height * visualSpec.character.anchorY

        repeat(count) { index ->
            val particle = View(context).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(preset.color)
                }
                alpha = 0f
            }
            val size = dp(if (index % 2 == 0) preset.largeDp else preset.smallDp)
            host.addView(particle, FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.TOP or Gravity.START
                leftMargin = (centerX - size / 2f).toInt()
                topMargin = (centerY - size / 2f).toInt()
            })
            particles += particle

            val angle = Math.toRadians((index * (360.0 / count)) - 90.0)
            val distance = dp(preset.distanceDp + (index % 3) * 8).toFloat()
            val x = (cos(angle) * distance).toFloat()
            val y = (sin(angle) * distance).toFloat()

            val animators = listOf(
                ObjectAnimator.ofFloat(particle, View.ALPHA, 0f, .9f, 0f),
                ObjectAnimator.ofFloat(particle, View.TRANSLATION_X, 0f, x),
                ObjectAnimator.ofFloat(particle, View.TRANSLATION_Y, 0f, y),
                ObjectAnimator.ofFloat(particle, View.SCALE_X, .6f, 1.25f),
                ObjectAnimator.ofFloat(particle, View.SCALE_Y, .6f, 1.25f)
            )
            animators.forEach { it.duration = duration }
            running = (running ?: AnimatorSet()).also { set ->
                set.playTogether(*(set.childAnimations + animators).toTypedArray())
            }
        }
        running?.start()
    }

    fun clear() {
        running?.cancel()
        running = null
        particles.forEach(host::removeView)
        particles.clear()
    }

    private data class FeedbackPreset(
        val count: Int,
        val color: Int,
        val distanceDp: Int,
        val largeDp: Int = 10,
        val smallDp: Int = 7
    )

    /**
     * Semantic presets keep actions visually distinct without requiring bespoke art.
     * Matching is intentionally tolerant so future templates can use localized or
     * domain-specific state names and still inherit useful feedback.
     */
    private fun presetFor(state: String): FeedbackPreset {
        val key = state.lowercase()
        return when {
            key.contains("bath") || key.contains("wash") || key.contains("wet") ||
                key.contains("banho") -> FeedbackPreset(8, Color.argb(210, 160, 220, 255), 42, 12, 8)
            key.contains("feed") || key.contains("eat") || key.contains("food") ||
                key.contains("comer") -> FeedbackPreset(6, Color.argb(220, 255, 220, 120), 36, 9, 6)
            key.contains("brush") || key.contains("clean") || key.contains("escov") ->
                FeedbackPreset(7, Color.argb(220, 255, 245, 210), 38, 10, 6)
            key.contains("play") || key.contains("ball") || key.contains("brinc") ->
                FeedbackPreset(9, Color.argb(225, 255, 190, 210), 48, 11, 7)
            else -> FeedbackPreset(6, Color.argb(205, 255, 255, 255), 38)
        }
    }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
