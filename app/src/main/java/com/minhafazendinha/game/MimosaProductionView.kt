package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

/** Cena da Mimosa: preview aprovado agora, layers reais quando estiverem disponiveis. */
class MimosaProductionView(context: Context) : FrameLayout(context) {
    private val reference = layer(ImageView.ScaleType.CENTER_CROP)
    private val background = layer(ImageView.ScaleType.CENTER_CROP)
    private val contactShadow = ImageView(context)
    private val character = layer(ImageView.ScaleType.CENTER_INSIDE)
    private val foreground = layer(ImageView.ScaleType.CENTER_CROP)
    private var visualState = CareVisualState.IDLE
    private val reactionAnimator: CareReactionAnimator

    init {
        setBackgroundColor(Color.TRANSPARENT)
        clipChildren = false
        addView(reference, LayoutParams(-1, -1))
        addView(background, LayoutParams(-1, -1))
        addView(contactShadow, LayoutParams(dp(190), dp(42)).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            bottomMargin = dp(78)
        })
        addView(character, LayoutParams(-1, -1).apply { gravity = Gravity.CENTER })
        addView(foreground, LayoutParams(-1, -1))
        contactShadow.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.argb(52, 32, 24, 18))
        }
        contactShadow.alpha = 0f
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
        target.scaleX = .965f
        target.scaleY = .965f
        target.translationY = dp(5).toFloat()
        target.animate()
            .scaleX(1.025f).scaleY(1.025f)
            .translationY(-dp(3).toFloat())
            .setDuration(140)
            .withEndAction {
                target.animate()
                    .scaleX(1f).scaleY(1f)
                    .translationY(0f)
                    .setDuration(180)
                    .start()
            }.start()

        if (hasLayeredProduction()) {
            contactShadow.animate().cancel()
            contactShadow.scaleX = .88f
            contactShadow.alpha = .22f
            contactShadow.animate().scaleX(1f).alpha(.34f).setDuration(220).start()
            foreground.animate().cancel()
            foreground.translationY = dp(3).toFloat()
            foreground.animate().translationY(0f).setDuration(260).start()
        }
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
        contactShadow.visibility = if (layered) VISIBLE else GONE
        contactShadow.alpha = if (layered) .30f else 0f

        // Slight overscan creates depth and prevents edge gaps during future parallax motion.
        background.scaleX = if (layered) 1.025f else 1f
        background.scaleY = if (layered) 1.025f else 1f
        foreground.scaleX = if (layered) 1.035f else 1f
        foreground.scaleY = if (layered) 1.035f else 1f

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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
