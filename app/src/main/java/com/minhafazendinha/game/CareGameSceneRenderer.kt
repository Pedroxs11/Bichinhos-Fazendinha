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
                apply(view, command, scene.animated)
            } else {
                view.setImageDrawable(null)
            }
        }
    }

    fun clear() {
        views.values.forEach {
            it.animate().cancel()
            it.setImageDrawable(null)
            it.visibility = ImageView.GONE
        }
    }

    fun view(layer: CareVisualLayer): ImageView = requireNotNull(views[layer])

    private fun apply(view: ImageView, command: CareGameRenderCommand, animated: Boolean) {
        view.setImageDrawable(command.drawableKey?.let(::drawable))
        view.pivotX = view.width * command.anchorX
        view.pivotY = view.height * command.anchorY

        if (animated && command.transitionMs > 0) {
            view.animate().cancel()
            view.animate()
                .scaleX(command.scale)
                .scaleY(command.scale)
                .setDuration(command.transitionMs.toLong())
                .start()
        } else {
            view.animate().cancel()
            view.scaleX = command.scale
            view.scaleY = command.scale
        }
    }

    private fun drawable(name: String): Drawable? {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (id == 0) null else runCatching { context.getDrawable(id) }.getOrNull()
    }
}
