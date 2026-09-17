package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

/** Cena visual da Mimosa: preview aprovado enquanto os layers reais nao chegaram. */
class MimosaProductionView(context: Context) : FrameLayout(context) {
    private val reference = layer(ImageView.ScaleType.CENTER_CROP)
    private val background = layer(ImageView.ScaleType.CENTER_CROP)
    private val character = layer(ImageView.ScaleType.CENTER_INSIDE)
    private val foreground = layer(ImageView.ScaleType.CENTER_CROP)
    private var careState = 3

    init {
        setBackgroundColor(Color.TRANSPARENT)
        addView(reference, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(background, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(character, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply { gravity = Gravity.CENTER })
        addView(foreground, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        render()
    }

    fun setCareState(state: Int) { careState = state.coerceIn(0, 3); render() }

    fun hasVisualPreview(): Boolean = drawable(ProductionVisuals.mimosa.referenceAsset) != null

    fun hasLayeredProduction(): Boolean {
        val s = ProductionVisuals.mimosa
        return drawable(s.backgroundAsset) != null && drawable(s.characterAsset) != null
    }

    /** Compatibilidade com o piloto antigo: preview ou layers permitem abrir a cena. */
    fun hasProductionAssets(): Boolean = hasVisualPreview() || hasLayeredProduction()

    private fun render() {
        val s = ProductionVisuals.mimosa
        val layered = hasLayeredProduction()
        reference.visibility = if (layered) GONE else VISIBLE
        reference.setImageDrawable(if (layered) null else drawable(s.referenceAsset))
        background.setImageDrawable(if (layered) drawable(s.backgroundAsset) else null)
        foreground.setImageDrawable(if (layered) drawable(s.foregroundAsset) else null)

        val stateKey = when (careState) { 0 -> "feed"; 1 -> "bath"; 2 -> "brush"; else -> "happy" }
        val stateAsset = s.interactionAssets[stateKey]
        character.setImageDrawable(if (layered) drawable(stateAsset ?: s.characterAsset) ?: drawable(s.characterAsset) else null)
    }

    private fun layer(scale: ImageView.ScaleType) = ImageView(context).apply { scaleType = scale; adjustViewBounds = false }
    private fun drawable(name: String): Drawable? {
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        return if (id == 0) null else runCatching { context.getDrawable(id) }.getOrNull()
    }
}
