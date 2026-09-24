package com.paintbynumber.prototype

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Centralizes safe PNG sharing for completed paintings.
 * The caller supplies a rendered bitmap so the game view remains responsible
 * only for drawing, while this helper owns cache/file-provider details.
 */
object ArtworkShareHelper {
    private const val SHARE_DIR = "shared_artwork"

    fun share(context: Context, bitmap: Bitmap, drawingName: String) {
        val directory = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
        val safeName = drawingName
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "arte" }
        val file = File(directory, "${safeName}_${System.currentTimeMillis()}.png")

        FileOutputStream(file).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Não foi possível gerar a imagem para compartilhar"
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val text = "Olha a arte que eu terminei no Pintura por Número: $drawingName 🎨"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri("arte", uri)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar minha arte"))
    }
}
