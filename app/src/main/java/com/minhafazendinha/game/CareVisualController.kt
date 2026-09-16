package com.minhafazendinha.game

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView

/**
 * Reusable presentation controller for every care-game character.
 * Keeps activities/panels free from asset lookup and transition details.
 */
class CareVisualController(
    context: Context,
    pet: CarePetDefinition,
    private val imageView: ImageView,
    private val fallback: View? = null
) {
    private val runtime = CareVisualRuntime(context, pet)
    private var currentState = CareVisualState.IDLE

    init {
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        showIdle()
    }

    fun showIdle() = render(runtime.state(null))

    fun showAction(actionId: String) = render(runtime.state(actionId))

    fun sceneDrawable(): Drawable? = runtime.scene().drawableResId
        .takeIf { it != 0 }
        ?.let { imageView.context.getDrawable(it) }

    fun missingAssets(): List<String> = runtime.missingAssets()

    fun currentState(): CareVisualState = currentState

    private fun render(visual: ResolvedCareVisual) {
        currentState = visual.state
        if (visual.hasDrawable) {
            imageView.setImageResource(visual.drawableResId)
            imageView.visibility = View.VISIBLE
            fallback?.visibility = View.GONE
            imageView.animate().cancel()
            imageView.alpha = 0.65f
            imageView.scaleX = 0.97f
            imageView.scaleY = 0.97f
            imageView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(160).start()
        } else {
            imageView.setImageDrawable(null)
            imageView.visibility = View.GONE
            fallback?.visibility = View.VISIBLE
            fallback?.animate()?.cancel()
            fallback?.scaleX = 0.98f
            fallback?.scaleY = 0.98f
            fallback?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(140)?.start()
        }
    }
}
