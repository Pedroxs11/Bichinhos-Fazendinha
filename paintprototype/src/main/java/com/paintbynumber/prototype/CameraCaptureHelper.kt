package com.paintbynumber.prototype

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * Small, dependency-free camera bridge for the future "photo to coloring page" flow.
 * It keeps permission/file-provider details outside PaintGameView so camera UI can
 * be connected incrementally without disturbing the painting engine.
 */
object CameraCaptureHelper {
    const val CAMERA_PERMISSION_REQUEST = 4101
    const val CAMERA_CAPTURE_REQUEST = 4102

    data class CaptureRequest(val intent: Intent, val outputUri: Uri, val outputFile: File)

    fun hasCameraPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun requestCameraPermission(activity: Activity) {
        activity.requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
    }

    fun createCaptureRequest(context: Context): CaptureRequest? {
        val directory = File(context.cacheDir, "camera_import").apply { mkdirs() }
        val file = File(directory, "source_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri("camera_output", uri)
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            CaptureRequest(intent, uri, file)
        } else {
            file.delete()
            null
        }
    }
}
