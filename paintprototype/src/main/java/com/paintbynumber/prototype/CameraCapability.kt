package com.paintbynumber.prototype

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

/** Camera readiness checks for the future photo-to-paint flow. */
object CameraCapability {
    fun hasCameraHardware(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun isReady(context: Context): Boolean =
        hasCameraHardware(context) && hasPermission(context)
}
