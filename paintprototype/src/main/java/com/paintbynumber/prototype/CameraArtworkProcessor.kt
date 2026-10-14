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
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

        // Downsample before allocating pixel memory. A modern phone photo can be
        // 40+ megapixels; decoding it at full size can exhaust the app's heap.
        val targetSize = maxSize.coerceAtLeast(64)
        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / sampleSize > targetSize * 2) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeFile(source.absolutePath, options) ?: return false
        var working: Bitmap? = null
        var lineArt: Bitmap? = null
        var succeeded = false

        try {
            val largest = max(decoded.width, decoded.height)
            val scale = if (largest > targetSize) targetSize.toFloat() / largest else 1f
            val width = max(1, (decoded.width * scale).toInt())
            val height = max(1, (decoded.height * scale).toInt())
            working = if (width != decoded.width || height != decoded.height) {
                Bitmap.createScaledBitmap(decoded, width, height, true)
            } else {
                decoded
            }

            val gray = IntArray(width * height)
            working.getPixels(gray, 0, width, 0, 0, width, height)
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

            lineArt = Bitmap.createBitmap(result, width, height, Bitmap.Config.ARGB_8888)
            output.parentFile?.mkdirs()
            succeeded = FileOutputStream(output).use {
                lineArt.compress(Bitmap.CompressFormat.PNG, 100, it)
            } && output.exists() && output.length() > 0L
            return succeeded
        } catch (_: Exception) {
            return false
        } finally {
            lineArt?.recycle()
            if (working != null && working !== decoded) working.recycle()
            decoded.recycle()
            if (!succeeded) output.delete()
        }
    }
}
