package com.minhafazendinha.game

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Android renderer shared by every care-game screen.
 * It executes factory scene commands while keeping asset lookup and layer views
 * out of individual games, so new games mostly provide templates/assets.
 *
 * Depth polish also lives here: every new care game inherits the same subtle
 * overscan, layer separation and action motion without duplicating screen code.
 */
class CareGameSceneRenderer(
    private val context: Context,
    private val host: FrameLayout
) {
    private val views = CareVisualLayer.values().associateWith { layer ->
        ImageView(context).apply {
            scaleType = when (layer) {
                CareVisualLayer.CHARACTER -> ImageView.ScaleType.CENTER_INSIDE
                else -> ImageView.ScaleType.CENTER_CROP
            }
            adjustViewBounds = false
            host.addView(this, FrameLayout.LayoutParams(-1, -1).apply { gravity = Gravity.CENTER })
        }
    }

    fun render(scene: CareGameScene) {
        val byLayer = scene.commands.associateBy { it.layer }
        views.forEach { (layer, view) ->
            val command = byLayer[layer]
            view.visibility = if (command == null) ImageView.GONE else ImageView.VISIBLE
            if (command != null) {
                apply(view, layer, command, scene.animated)
            } else {
                reset(view)
                view.setImageDrawable(null)
            }
        }
    }

    fun clear() {
        views.values.forEach {
            reset(it)
            it.setImageDrawable(null)
            it.visibility = ImageView.GONE
        }
    }

    fun view(layer: CareVisualLayer): ImageView = requireNotNull(views[layer])

    private fun apply(
        view: ImageView,
        layer: CareVisualLayer,
        command: CareGameRenderCommand,
        animated: Boolean
    ) {
        view.setImageDrawable(command.drawableKey?.let(::drawable))
        view.pivotX = view.width * command.anchorX
        view.pivotY = view.height * command.anchorY

        val depthScale = when (layer) {
            CareVisualLayer.BACKGROUND -> 1.02f
            CareVisualLayer.CHARACTER -> 1f
            CareVisualLayer.EFFECTS -> 1.015f
            CareVisualLayer.HUD -> 1f
        }
        val targetScale = command.scale * depthScale
        val targetY = when (layer) {
            CareVisualLayer.CHARACTER -> 0f
            CareVisualLayer.EFFECTS -> -dp(2).toFloat()
            else -> 0f
        }

        view.animate().cancel()
        if (animated && command.transitionMs > 0) {
            if (layer == CareVisualLayer.CHARACTER) {
                view.scaleX = targetScale * .97f
                view.scaleY = targetScale * .97f
                view.translationY = dp(4).toFloat()
            }
            view.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .translationY(targetY)
                .setDuration(command.transitionMs.toLong())
                .start()
        } else {
            view.scaleX = targetScale
            view.scaleY = targetScale
            view.translationY = targetY
        }
    }

    private fun reset(view: ImageView) {
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        view.translationX = 0f
        view.translationY = 0f
        view.alpha = 1f
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    private fun drawable(name: String): Drawable? {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (id == 0) null else runCatching { context.getDrawable(id) }.getOrNull()
    }
}
