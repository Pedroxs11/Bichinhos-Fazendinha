package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Cena de producao da Mimosa baseada em assets renderizados.
 * Os nomes dos drawables seguem ProductionVisuals.mimosa; assim a arte pode
 * evoluir sem alterar a logica de cuidado do jogo.
 */
class MimosaProductionView(context: Context) : FrameLayout(context) {
    private val background = layer(ImageView.ScaleType.CENTER_CROP)
    private val character = layer(ImageView.ScaleType.CENTER_INSIDE)
    private val foreground = layer(ImageView.ScaleType.CENTER_CROP)
    private var careState = 0

    init {
        setBackgroundColor(Color.TRANSPARENT)
        addView(background, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(character, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.CENTER
        })
        addView(foreground, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        render()
    }

    fun setCareState(state: Int) {
        careState = state.coerceIn(0, 3)
        render()
    }

    /** Retorna false enquanto os renders finais ainda nao estiverem no APK. */
    fun hasProductionAssets(): Boolean = drawable(ProductionVisuals.mimosa.backgroundAsset) != null &&
        drawable(ProductionVisuals.mimosa.characterAsset) != null

    private fun render() {
        val spec = ProductionVisuals.mimosa
        background.setImageDrawable(drawable(spec.backgroundAsset))
        foreground.setImageDrawable(drawable(spec.foregroundAsset))
        val stateKey = when (careState) {
            0 -> "feed"
            1 -> "bath"
            2 -> "brush"
            else -> "happy"
        }
        character.setImageDrawable(
            drawable(spec.interactionAssets[stateKey] ?: spec.characterAsset)
                ?: drawable(spec.characterAsset)
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
