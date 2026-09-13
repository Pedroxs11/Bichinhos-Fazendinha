package com.minhafazendinha.game

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView

/** Primeiro palco visual 2.5D. Mantém a lógica 2D e prepara a troca futura por modelo 3D real. */
class AnimalStageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val shadow = TextView(context)
    private val animal = TextView(context)
    private var baseRotationY = -8f

    init {
        clipChildren = false
        clipToPadding = false
        minimumHeight = 300
        setPadding(24, 20, 24, 20)
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.rgb(184, 232, 255), Color.rgb(224, 246, 190))
        ).apply { cornerRadius = 36f }

        shadow.apply {
            text = "●"
            textSize = 74f
            gravity = Gravity.CENTER
            alpha = .13f
            scaleX = 1.7f
            scaleY = .38f
            translationY = 78f
        }
        animal.apply {
            text = "🐮"
            textSize = 92f
            gravity = Gravity.CENTER
            elevation = 18f
            rotationY = baseRotationY
            cameraDistance = resources.displayMetrics.density * 8000f
        }
        addView(shadow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(animal, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        ObjectAnimator.ofFloat(animal, "translationY", -4f, 7f, -4f).apply {
            duration = 1800
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    fun setAnimal(emoji: String, accessory: String = "") {
        animal.text = emoji + accessory
        ObjectAnimator.ofFloat(animal, "rotationY", animal.rotationY, 14f, baseRotationY).apply {
            duration = 430
            start()
        }
    }

    fun celebrate() {
        ObjectAnimator.ofFloat(animal, "scaleX", 1f, 1.14f, 1f).apply { duration = 300; start() }
        ObjectAnimator.ofFloat(animal, "scaleY", 1f, 1.14f, 1f).apply { duration = 300; start() }
        ObjectAnimator.ofFloat(animal, "rotationY", baseRotationY, 22f, -18f, baseRotationY).apply {
            duration = 520
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val center = width / 2f
                if (center > 0) animal.rotationY = ((event.x - center) / center * 22f).coerceIn(-22f, 22f)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                ObjectAnimator.ofFloat(animal, "rotationY", animal.rotationY, baseRotationY).apply {
                    duration = 220
                    start()
                }
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        celebrate()
        return true
    }
}
