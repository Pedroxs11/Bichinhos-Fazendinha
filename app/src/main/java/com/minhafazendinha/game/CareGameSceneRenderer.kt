package com.minhafazendinha.game

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

/** Shared renderer: new care games inherit scene depth and motion automatically. */
class CareGameSceneRenderer(
    private val context: Context,
    private val host: FrameLayout
) {
    private data class LayerPolish(
        val overscan: Float,
        val enterScale: Float = 1f,
        val enterOffsetDp: Int = 0,
        val liftDp: Int = 0
    )

    private val polish = mapOf(
        CareVisualLayer.BACKGROUND to LayerPolish(overscan = 1.02f),
        CareVisualLayer.CHARACTER to LayerPolish(overscan = 1f, enterScale = .97f, enterOffsetDp = 4),
        CareVisualLayer.EFFECTS to LayerPolish(overscan = 1.015f, liftDp = 2),
        CareVisualLayer.HUD to LayerPolish(overscan = 1f)
    )

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
            if (command != null) apply(view, layer, command, scene.animated)
            else {
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

    private fun apply(view: ImageView, layer: CareVisualLayer, command: CareGameRenderCommand, animated: Boolean) {
        view.setImageDrawable(command.drawableKey?.let(::drawable))
        view.pivotX = view.width * command.anchorX
        view.pivotY = view.height * command.anchorY

        val tuning = requireNotNull(polish[layer])
        val targetScale = command.scale * tuning.overscan
        val targetY = -dp(tuning.liftDp).toFloat()

        view.animate().cancel()
        if (animated && command.transitionMs > 0) {
            view.scaleX = targetScale * tuning.enterScale
            view.scaleY = targetScale * tuning.enterScale
            view.translationY = dp(tuning.enterOffsetDp).toFloat()
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
