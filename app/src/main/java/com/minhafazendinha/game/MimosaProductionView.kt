package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

/** Cena da Mimosa: usa o preview aprovado agora e troca automaticamente para layers reais quando existirem. */
class MimosaProductionView(context: Context) : FrameLayout(context) {
    private val reference = layer(ImageView.ScaleType.CENTER_CROP)
    private val background = layer(ImageView.ScaleType.CENTER_CROP)
    private val character = layer(ImageView.ScaleType.CENTER_INSIDE)
    private val foreground = layer(ImageView.ScaleType.CENTER_CROP)
    private val reaction = TextView(context).apply {
        textSize = 48f
        gravity = Gravity.CENTER
        alpha = 0f
        setTextColor(Color.WHITE)
        setShadowLayer(8f, 0f, 3f, 0x88000000.toInt())
    }
    private var visualState = CareVisualState.IDLE

    init {
        setBackgroundColor(Color.TRANSPARENT)
        addView(reference, LayoutParams(-1, -1))
        addView(background, LayoutParams(-1, -1))
        addView(character, LayoutParams(-1, -1).apply { gravity = Gravity.CENTER })
        addView(foreground, LayoutParams(-1, -1))
        addView(reaction, LayoutParams(190, 150).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, 38, 24, 0)
        })
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
        showReaction(state)
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

    private fun showReaction(state: CareVisualState) {
        reaction.animate().cancel()
        reaction.text = when (state) {
            CareVisualState.FEED -> "🍎✨"
            CareVisualState.BATHE -> "💦🫧"
            CareVisualState.BRUSH -> "✨🧹"
            CareVisualState.PLAY -> "🏐💖"
            CareVisualState.IDLE -> "💚"
        }
        reaction.alpha = 0f
        reaction.translationY = 35f
        reaction.scaleX = .65f
        reaction.scaleY = .65f
        reaction.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f).setDuration(180).withEndAction {
            reaction.animate().alpha(0f).translationY(-45f).setStartDelay(420).setDuration(260).start()
        }.start()
    }

    private fun render() {
        val spec = ProductionVisuals.mimosa
        val layered = hasLayeredProduction()
        reference.visibility = if (layered) GONE else VISIBLE
        reference.setImageDrawable(if (layered) null else drawable(spec.referenceAsset))
        background.setImageDrawable(if (layered) drawable(spec.backgroundAsset) else null)
        foreground.setImageDrawable(if (layered) drawable(spec.foregroundAsset) else null)
        val key = when (visualState) {
            CareVisualState.FEED -> "feed"
            CareVisualState.BATHE -> "bath"
            CareVisualState.BRUSH -> "brush"
            CareVisualState.PLAY -> "play"
            CareVisualState.IDLE -> null
        }
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
