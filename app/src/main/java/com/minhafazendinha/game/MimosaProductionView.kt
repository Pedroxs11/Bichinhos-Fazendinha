package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Cena de producao da Mimosa baseada em assets renderizados.
 * Funciona em duas fases: primeiro com um render completo aprovado e,
 * conforme os assets separados chegam, migra automaticamente para camadas.
 */
class MimosaProductionView(context: Context) : FrameLayout(context) {
    private val background = layer(ImageView.ScaleType.CENTER_CROP)
    private val character = layer(ImageView.ScaleType.CENTER_CROP)
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

    /** O primeiro render real ja e suficiente para ativar o piloto no APK. */
    fun hasProductionAssets(): Boolean = drawable(ProductionVisuals.mimosa.characterAsset) != null

    private fun render() {
        val spec = ProductionVisuals.mimosa
        val scene = drawable(spec.backgroundAsset)
        val idle = drawable(spec.characterAsset)

        // Enquanto temos apenas a referencia completa, ela ocupa a cena inteira.
        // Quando background/foreground separados forem adicionados, o mesmo view
        // passa a compor as camadas sem nenhuma mudanca na Activity.
        background.setImageDrawable(scene)
        foreground.setImageDrawable(drawable(spec.foregroundAsset))

        val stateKey = when (careState) {
            0 -> "feed"
            1 -> "bath"
            2 -> "brush"
            else -> "happy"
        }
        character.scaleType = if (scene == null) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.CENTER_INSIDE
        character.setImageDrawable(drawable(spec.interactionAssets[stateKey] ?: spec.characterAsset) ?: idle)
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
