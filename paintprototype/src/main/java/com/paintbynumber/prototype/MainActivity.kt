package com.paintbynumber.prototype

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {
    private lateinit var gameView: PaintGameView
    private var pendingCameraFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = PaintGameView(this)
        setContentView(gameView)
    }

    fun openCameraImport() {
        if (!CameraCaptureHelper.hasCameraPermission(this)) {
            CameraCaptureHelper.requestCameraPermission(this)
            return
        }
        launchCamera()
    }

    private fun launchCamera() {
        val request = CameraCaptureHelper.createCaptureRequest(this)
        if (request == null) {
            Toast.makeText(this, "Câmera não disponível neste aparelho", Toast.LENGTH_SHORT).show()
            return
        }
        pendingCameraFile = request.outputFile
        startActivityForResult(request.intent, CameraCaptureHelper.CAMERA_CAPTURE_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CameraCaptureHelper.CAMERA_PERMISSION_REQUEST) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this, "Permita o uso da câmera para criar uma arte pela foto", Toast.LENGTH_LONG).show()
            }
        }
    }

    @Deprecated("Kept for the dependency-free camera prototype")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != CameraCaptureHelper.CAMERA_CAPTURE_REQUEST) return

        val file = pendingCameraFile
        pendingCameraFile = null
        if (resultCode == RESULT_OK && file?.exists() == true && file.length() > 0L) {
            gameView.onCameraPhotoCaptured(file)
        } else {
            file?.delete()
            Toast.makeText(this, "Foto cancelada", Toast.LENGTH_SHORT).show()
        }
    }
}
