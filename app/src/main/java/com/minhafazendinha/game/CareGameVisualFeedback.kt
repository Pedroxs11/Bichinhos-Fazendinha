package com.minhafazendinha.game

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import kotlin.math.cos
import kotlin.math.sin

/** Reusable action feedback shared by every care game. */
class CareGameVisualFeedback(
    private val context: Context,
    private val host: FrameLayout,
    private val visualSpec: CareGameVisualSpec
) {
    private val particles = mutableListOf<View>()
    private var running: AnimatorSet? = null

    fun play(state: String, animated: Boolean, character: View? = null) {
        clear()
        if (state == "idle" || !animated) return

        val preset = presetFor(state)
        character?.let { playCharacterReaction(it, preset) }
        playParticles(preset)
    }

    fun clear() {
        running?.cancel()
        running = null
        particles.forEach(host::removeView)
        particles.clear()
    }

    private fun playCharacterReaction(character: View, preset: FeedbackPreset) {
        val duration = visualSpec.motion.completionFeedbackMs.toLong()
        val baseScaleX = character.scaleX
        val baseScaleY = character.scaleY
        val baseY = character.translationY
        val set = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(character, View.SCALE_X, baseScaleX, baseScaleX * preset.reactionScale, baseScaleX),
                ObjectAnimator.ofFloat(character, View.SCALE_Y, baseScaleY, baseScaleY * preset.reactionScale, baseScaleY),
                ObjectAnimator.ofFloat(character, View.TRANSLATION_Y, baseY, baseY - dp(preset.liftDp), baseY),
                ObjectAnimator.ofFloat(character, View.ROTATION, 0f, preset.tiltDegrees, -preset.tiltDegrees * .55f, 0f)
            )
            this.duration = duration
        }
        running = set
        set.start()
    }

    private fun playParticles(preset: FeedbackPreset) {
        val duration = visualSpec.motion.completionFeedbackMs.toLong()
        val centerX = host.width * visualSpec.character.anchorX
        val centerY = host.height * visualSpec.character.anchorY
        val particleAnimators = mutableListOf<android.animation.Animator>()

        repeat(preset.count) { index ->
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

            val angle = Math.toRadians((index * (360.0 / preset.count)) - 90.0)
            val distance = dp(preset.distanceDp + (index % 3) * 8).toFloat()
            val x = (cos(angle) * distance).toFloat()
            val y = (sin(angle) * distance).toFloat()
            particleAnimators += listOf(
                ObjectAnimator.ofFloat(particle, View.ALPHA, 0f, .9f, 0f),
                ObjectAnimator.ofFloat(particle, View.TRANSLATION_X, 0f, x),
                ObjectAnimator.ofFloat(particle, View.TRANSLATION_Y, 0f, y),
                ObjectAnimator.ofFloat(particle, View.SCALE_X, .6f, 1.25f),
                ObjectAnimator.ofFloat(particle, View.SCALE_Y, .6f, 1.25f)
            ).onEach { it.duration = duration }
        }
        AnimatorSet().apply {
            playTogether(particleAnimators)
            start()
        }
    }

    private data class FeedbackPreset(
        val count: Int,
        val color: Int,
        val distanceDp: Int,
        val largeDp: Int = 10,
        val smallDp: Int = 7,
        val reactionScale: Float = 1.04f,
        val liftDp: Int = 6,
        val tiltDegrees: Float = 2f
    )

    private fun presetFor(state: String): FeedbackPreset {
        val key = state.lowercase()
        return when {
            key.contains("bath") || key.contains("wash") || key.contains("wet") || key.contains("banho") ->
                FeedbackPreset(8, Color.argb(210, 160, 220, 255), 42, 12, 8, 1.025f, 3, 1.5f)
            key.contains("feed") || key.contains("eat") || key.contains("food") || key.contains("comer") ->
                FeedbackPreset(6, Color.argb(220, 255, 220, 120), 36, 9, 6, 1.035f, 4, 2f)
            key.contains("brush") || key.contains("clean") || key.contains("escov") ->
                FeedbackPreset(7, Color.argb(220, 255, 245, 210), 38, 10, 6, 1.045f, 6, 2.5f)
            key.contains("play") || key.contains("ball") || key.contains("brinc") ->
                FeedbackPreset(9, Color.argb(225, 255, 190, 210), 48, 11, 7, 1.07f, 12, 4f)
            else -> FeedbackPreset(6, Color.argb(205, 255, 255, 255), 38)
        }
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
