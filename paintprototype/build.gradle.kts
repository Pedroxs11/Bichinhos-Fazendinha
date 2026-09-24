plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.paintbynumber.prototype"
    compileSdk = 35

    signingConfigs {
        create("releaseLocal") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.pedro.colorbynumber.prototype.release"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "0.1.3"
    }

    buildTypes {
        getByName("debug") {
            // Keep Android's standard debug signing. Gradle generates the
            // debug keystore automatically when it is missing (including CI).
        }
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("releaseLocal")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
}
