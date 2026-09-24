package com.paintbynumber.prototype

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

/** Camera readiness checks for the future photo-to-paint flow. */
object CameraCapability {
    enum class Readiness {
        READY,
        PERMISSION_REQUIRED,
        HARDWARE_UNAVAILABLE
    }

    /** Single source of truth for permissions needed by the camera flow. */
    val requiredPermissions: Array<String>
        get() = arrayOf(Manifest.permission.CAMERA)

    fun hasCameraHardware(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun readiness(context: Context): Readiness = when {
        !hasCameraHardware(context) -> Readiness.HARDWARE_UNAVAILABLE
        !hasPermission(context) -> Readiness.PERMISSION_REQUIRED
        else -> Readiness.READY
    }

    fun isReady(context: Context): Boolean = readiness(context) == Readiness.READY
}
