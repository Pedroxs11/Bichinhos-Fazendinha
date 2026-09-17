package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

/** Cena da Mimosa: preview aprovado agora, layers reais quando estiverem disponiveis. */
class MimosaProductionView(context: Context) : FrameLayout(context) {
    private val reference = layer(ImageView.ScaleType.CENTER_CROP)
    private val background = layer(ImageView.ScaleType.CENTER_CROP)
    private val character = layer(ImageView.ScaleType.CENTER_INSIDE)
    private val foreground = layer(ImageView.ScaleType.CENTER_CROP)
    private var visualState = CareVisualState.IDLE
    private val reactionAnimator: CareReactionAnimator

    init {
        setBackgroundColor(Color.TRANSPARENT)
        addView(reference, LayoutParams(-1, -1))
        addView(background, LayoutParams(-1, -1))
        addView(character, LayoutParams(-1, -1).apply { gravity = Gravity.CENTER })
        addView(foreground, LayoutParams(-1, -1))
        reactionAnimator = CareReactionAnimator(this)
        render()
    }

    fun setCareState(state: Int) {
        visualState = when (state.coerceIn(0, 3)) {
            0 -> CareVisualState.FEED
            1 -> CareVisualState.BATHE
            2 -> CareVisualState.BRUSH
            else -> CareVisualState.IDLE
        }
        render()
    }

    fun setCareAction(state: CareVisualState) {
        visualState = state
        render()
        animateCharacter()
        reactionAnimator.play(actionKey(state))
    }

    fun showIdle() {
        visualState = CareVisualState.IDLE
        render()
    }

    fun hasVisualPreview() = drawable(ProductionVisuals.mimosa.referenceAsset) != null

    fun hasLayeredProduction(): Boolean {
        val spec = ProductionVisuals.mimosa
        return drawable(spec.backgroundAsset) != null && drawable(spec.characterAsset) != null
    }

    fun hasProductionAssets() = hasVisualPreview() || hasLayeredProduction()

    private fun animateCharacter() {
        val target = if (hasLayeredProduction()) character else reference
        target.animate().cancel()
        target.scaleX = .96f
        target.scaleY = .96f
        target.animate().scaleX(1.025f).scaleY(1.025f).setDuration(130).withEndAction {
            target.animate().scaleX(1f).scaleY(1f).setDuration(160).start()
        }.start()
    }

    private fun actionKey(state: CareVisualState): String = when (state) {
        CareVisualState.FEED -> "feed"
        CareVisualState.BATHE -> "bath"
        CareVisualState.BRUSH -> "brush"
        CareVisualState.PLAY -> "play"
        CareVisualState.IDLE -> "idle"
    }

    private fun render() {
        val spec = ProductionVisuals.mimosa
        val layered = hasLayeredProduction()
        reference.visibility = if (layered) GONE else VISIBLE
        reference.setImageDrawable(if (layered) null else drawable(spec.referenceAsset))
        background.setImageDrawable(if (layered) drawable(spec.backgroundAsset) else null)
        foreground.setImageDrawable(if (layered) drawable(spec.foregroundAsset) else null)
        val key = actionKey(visualState).takeUnless { it == "idle" }
        val asset = key?.let { spec.interactionAssets[it] }
        character.setImageDrawable(
            if (layered) drawable(asset ?: spec.characterAsset) ?: drawable(spec.characterAsset) else null
        )
    }

    private fun layer(scale: ImageView.ScaleType) = ImageView(context).apply {
        scaleType = scale
        adjustViewBounds = false
    }

    private fun drawable(name: String): Drawable? {
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        return if (id == 0) null else runCatching { context.getDrawable(id) }.getOrNull()
    }
}
