package com.paintbynumber.prototype

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/** Small reusable thumbnail helpers kept outside PaintGameView to make gallery art safer to extend. */
object GalleryThumbnailRenderer {
    fun drawRainbow(canvas: Canvas, paint: Paint, cx: Float, cy: Float, width: Float, height: Float) {
        val bands = listOf(
            Color.rgb(244, 67, 54),
            Color.rgb(255, 152, 0),
            Color.rgb(255, 193, 7),
            Color.rgb(76, 175, 80),
            Color.rgb(33, 150, 243)
        )
        val previousStyle = paint.style
        val previousStrokeWidth = paint.strokeWidth
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = width * .055f
        bands.forEachIndexed { band, color ->
            paint.color = color
            val inset = band * width * .035f
            val bounds = RectF(
                cx - width * .23f + inset,
                cy - height * .08f + inset,
                cx + width * .23f - inset,
                cy + height * .28f
            )
            canvas.drawArc(bounds, 180f, 180f, false, paint)
        }
        paint.style = previousStyle
        paint.strokeWidth = previousStrokeWidth
    }
}
