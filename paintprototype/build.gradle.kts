plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pedroxs11.colorbynumber"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pedroxs11.colorbynumber"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "0.1.4"
    }

    buildTypes {
        getByName("debug") {
            // Keep Android's standard debug signing. Gradle generates the
            // debug keystore automatically when it is missing (including CI).
        }
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
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
