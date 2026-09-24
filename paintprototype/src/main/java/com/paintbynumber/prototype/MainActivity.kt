package com.paintbynumber.prototype

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {
    private lateinit var gameView: PaintGameView
    private lateinit var root: FrameLayout
    private var pendingCameraFile: File? = null
    private var cameraPreview: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = PaintGameView(this)

        root = FrameLayout(this)
        root.addView(gameView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        val cameraButton = Button(this).apply {
            text = "📷"
            textSize = 22f
            setTextColor(Color.DKGRAY)
            contentDescription = "Criar arte pela câmera"
            setOnClickListener { openCameraImport() }
        }
        val density = resources.displayMetrics.density
        root.addView(cameraButton, FrameLayout.LayoutParams(
            (58 * density).toInt(),
            (52 * density).toInt(),
            Gravity.TOP or Gravity.END
        ).apply {
            topMargin = (8 * density).toInt()
            marginEnd = (8 * density).toInt()
        })
        setContentView(root)
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

    private fun showCameraPreview(file: File) {
        cameraPreview?.let(root::removeView)
        val density = resources.displayMetrics.density
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding((18*density).toInt(), (18*density).toInt(), (18*density).toInt(), (18*density).toInt())
            setBackgroundColor(Color.WHITE)
        }
        panel.addView(TextView(this).apply {
            text = "Sua foto ficou assim 📷"
            textSize = 20f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        panel.addView(ImageView(this).apply {
            setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            adjustViewBounds = true
            contentDescription = "Prévia da foto capturada"
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply {
            topMargin = (12*density).toInt()
            bottomMargin = (12*density).toInt()
        })

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(Button(this).apply {
            text = "Tirar outra"
            setOnClickListener {
                cameraPreview?.let(root::removeView)
                cameraPreview = null
                file.delete()
                launchCamera()
            }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(Button(this).apply {
            text = "Usar foto"
            setOnClickListener {
                getSharedPreferences("paint_progress", MODE_PRIVATE).edit()
                    .putString("last_camera_photo", file.absolutePath)
                    .apply()
                cameraPreview?.let(root::removeView)
                cameraPreview = null
                Toast.makeText(this@MainActivity, "Foto salva para virar desenho 🎨", Toast.LENGTH_LONG).show()
            }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        panel.addView(actions)

        cameraPreview = panel
        root.addView(panel, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
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
            showCameraPreview(file)
        } else {
            file?.delete()
            Toast.makeText(this, "Foto cancelada", Toast.LENGTH_SHORT).show()
        }
    }
}
