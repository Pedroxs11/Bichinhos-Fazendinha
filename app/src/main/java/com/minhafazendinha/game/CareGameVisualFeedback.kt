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
    private var particleRunning: AnimatorSet? = null
    private var activeCharacter: View? = null
    private var characterBase: CharacterBase? = null

    fun play(state: String, animated: Boolean, character: View? = null) {
        clear()
        if (state == "idle" || !animated) return

        val preset = visualSpec.feedbackFor(state)
        character?.let {
            activeCharacter = it
            characterBase = CharacterBase(it.scaleX, it.scaleY, it.translationY, it.rotation)
            playCharacterReaction(it, preset)
        }
        playParticles(preset)
    }

    fun clear() {
        running?.cancel()
        particleRunning?.cancel()
        running = null
        particleRunning = null
        activeCharacter?.let { character ->
            characterBase?.let { base ->
                character.scaleX = base.scaleX
                character.scaleY = base.scaleY
                character.translationY = base.translationY
                character.rotation = base.rotation
            }
        }
        activeCharacter = null
        characterBase = null
        particles.forEach(host::removeView)
        particles.clear()
    }

    private fun playCharacterReaction(character: View, preset: CareActionFeedbackSpec) {
        val duration = visualSpec.motion.completionFeedbackMs.toLong()
        val baseScaleX = character.scaleX
        val baseScaleY = character.scaleY
        val baseY = character.translationY
        running = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(character, View.SCALE_X, baseScaleX, baseScaleX * preset.reactionScale, baseScaleX),
                ObjectAnimator.ofFloat(character, View.SCALE_Y, baseScaleY, baseScaleY * preset.reactionScale, baseScaleY),
                ObjectAnimator.ofFloat(character, View.TRANSLATION_Y, baseY, baseY - dp(preset.liftDp), baseY),
                ObjectAnimator.ofFloat(character, View.ROTATION, 0f, preset.tiltDegrees, -preset.tiltDegrees * .55f, 0f)
            )
            this.duration = duration
            start()
        }
    }

    private fun playParticles(preset: CareActionFeedbackSpec) {
        val duration = visualSpec.motion.completionFeedbackMs.toLong()
        val centerX = host.width * visualSpec.character.anchorX
        val centerY = host.height * visualSpec.character.anchorY
        val particleAnimators = mutableListOf<android.animation.Animator>()

        repeat(preset.particleCount) { index ->
            val particle = View(context).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(colorFor(preset.style))
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

            val angle = Math.toRadians((index * (360.0 / preset.particleCount.coerceAtLeast(1))) - 90.0)
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
        particleRunning = AnimatorSet().apply {
            playTogether(particleAnimators)
            start()
        }
    }

    private fun colorFor(style: CareFeedbackStyle): Int = when (style) {
        CareFeedbackStyle.BUBBLE -> Color.argb(210, 160, 220, 255)
        CareFeedbackStyle.CRUMB -> Color.argb(220, 255, 220, 120)
        CareFeedbackStyle.SPARKLE -> Color.argb(220, 255, 245, 210)
        CareFeedbackStyle.PLAYFUL -> Color.argb(225, 255, 190, 210)
    }

    private data class CharacterBase(
        val scaleX: Float,
        val scaleY: Float,
        val translationY: Float,
        val rotation: Float
    )

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
