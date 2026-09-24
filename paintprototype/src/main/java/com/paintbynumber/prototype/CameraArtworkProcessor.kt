package com.paintbynumber.prototype

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Lightweight first pass for photo-to-coloring-page.
 * Produces a high-contrast line-art preview locally, without network or ML.
 */
object CameraArtworkProcessor {
    fun createLineArt(source: File, output: File, maxSize: Int = 1200): Boolean {
        val original = BitmapFactory.decodeFile(source.absolutePath) ?: return false
        val largest = max(original.width, original.height)
        val scale = if (largest > maxSize) maxSize.toFloat() / largest else 1f
        val width = max(1, (original.width * scale).toInt())
        val height = max(1, (original.height * scale).toInt())
        val bitmap = if (width != original.width || height != original.height) {
            Bitmap.createScaledBitmap(original, width, height, true)
        } else original

        val gray = IntArray(width * height)
        bitmap.getPixels(gray, 0, width, 0, 0, width, height)
        for (i in gray.indices) {
            val c = gray[i]
            gray[i] = (Color.red(c) * 30 + Color.green(c) * 59 + Color.blue(c) * 11) / 100
        }

        val result = IntArray(gray.size) { Color.WHITE }
        val threshold = 42
        for (y in 1 until height - 1) {
            val row = y * width
            for (x in 1 until width - 1) {
                val i = row + x
                val gx = kotlin.math.abs(gray[i + 1] - gray[i - 1])
                val gy = kotlin.math.abs(gray[i + width] - gray[i - width])
                val edge = min(255, gx + gy)
                result[i] = if (edge >= threshold) Color.BLACK else Color.WHITE
            }
        }

        val lineArt = Bitmap.createBitmap(result, width, height, Bitmap.Config.ARGB_8888)
        output.parentFile?.mkdirs()
        val ok = FileOutputStream(output).use { lineArt.compress(Bitmap.CompressFormat.PNG, 100, it) }
        if (bitmap !== original) bitmap.recycle()
        original.recycle()
        lineArt.recycle()
        return ok
    }
}
