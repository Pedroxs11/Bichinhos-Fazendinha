package com.minhafazendinha.game

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Reusable layered renderer for care-game templates.
 * New games can supply a CareGameVisualRecipe instead of creating a bespoke scene View.
 */
class CareGameSceneRuntime(
    context: Context,
    private val recipe: CareGameVisualRecipe,
    reactionTheme: CareReactionTheme = CareReactionThemes.farm
) : FrameLayout(context) {
    private val background = layer(ImageView.ScaleType.CENTER_CROP)
    private val character = layer(ImageView.ScaleType.CENTER_INSIDE)
    private val foreground = layer(ImageView.ScaleType.CENTER_CROP)
    private val effect = layer(ImageView.ScaleType.CENTER_INSIDE)
    private val prop = layer(ImageView.ScaleType.CENTER_INSIDE)
    private val reactionAnimator: CareReactionAnimator
    private var playbackToken = 0

    init {
        clipChildren = false
        addView(background, LayoutParams(-1, -1))
        addView(character, LayoutParams(-1, -1).apply { gravity = Gravity.CENTER })
        addView(effect, LayoutParams(-1, -1).apply { gravity = Gravity.CENTER })
        addView(foreground, LayoutParams(-1, -1))
        addView(prop, LayoutParams(dp(150), dp(150)).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            bottomMargin = dp(36)
        })
        reactionAnimator = CareReactionAnimator(this, reactionTheme)
        showIdle()
        alpha = 0f
        scaleX = .985f
        scaleY = .985f
        animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(recipe.enterDurationMs).start()
    }

    fun showIdle() {
        background.setImageDrawable(drawable(recipe.backgroundAsset))
        foreground.setImageDrawable(recipe.foregroundAsset?.let(::drawable))
        character.setImageDrawable(drawable(recipe.characterIdleAsset))
        prop.setImageDrawable(null)
        effect.setImageDrawable(null)
        prop.visibility = View.GONE
        effect.visibility = View.GONE
    }

    fun play(actionId: String) {
        val action = recipe.action(actionId) ?: return
        val token = ++playbackToken
        character.setImageDrawable(drawable(action.characterAsset) ?: drawable(recipe.characterIdleAsset))
        prop.setImageDrawable(action.propAsset?.let(::drawable))
        effect.setImageDrawable(action.effectAsset?.let(::drawable))
        prop.visibility = if (prop.drawable == null) View.GONE else View.VISIBLE
        effect.visibility = if (effect.drawable == null) View.GONE else View.VISIBLE
        pulse(character)
        if (prop.visibility == View.VISIBLE) pulse(prop)
        if (effect.visibility == View.VISIBLE) {
            effect.alpha = 0f
            effect.scaleX = .9f
            effect.scaleY = .9f
            effect.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(recipe.actionPulseInMs).start()
            postDelayed({
                if (token == playbackToken) effect.animate().alpha(0f)
                    .setDuration(recipe.actionPulseOutMs).withEndAction {
                        if (token == playbackToken) effect.visibility = View.GONE
                    }.start()
            }, recipe.effectHoldMs)
        }
        reactionAnimator.play(actionId)
        postDelayed({
            if (token == playbackToken) showIdle()
        }, maxOf(recipe.propHoldMs, recipe.effectHoldMs) + recipe.idleReturnDelayMs)
    }

    fun missingAssets(): List<String> {
        val names = buildList {
            add(recipe.backgroundAsset)
            add(recipe.characterIdleAsset)
            recipe.foregroundAsset?.let(::add)
            recipe.actions.values.forEach { action ->
                add(action.characterAsset)
                action.propAsset?.let(::add)
                action.effectAsset?.let(::add)
            }
        }.distinct()
        return names.filter { drawable(it) == null }
    }

    private fun pulse(view: View) {
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        view.animate()
            .scaleX(recipe.actionPulseScale).scaleY(recipe.actionPulseScale)
            .setDuration(recipe.actionPulseInMs)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f)
                    .setDuration(recipe.actionPulseOutMs).start()
            }.start()
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
