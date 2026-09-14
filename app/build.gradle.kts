import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val generatedAudioRes = layout.buildDirectory.dir("generated/res/audioRaw")

val decodeAnimalAudio by tasks.registering {
    val sourceDir = layout.projectDirectory.dir("src/main/audio_b64")
    inputs.dir(sourceDir)
    outputs.dir(generatedAudioRes)

    doLast {
        val rawDir = generatedAudioRes.get().dir("raw").asFile
        rawDir.mkdirs()

        sourceDir.asFile.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".b64") }
            .forEach { encoded ->
                val outputName = encoded.name.removeSuffix(".b64")
                val base64 = encoded.readText().filterNot(Char::isWhitespace)
                rawDir.resolve(outputName).writeBytes(Base64.getDecoder().decode(base64))
            }
    }
}

android {
    namespace = "com.minhafazendinha.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.minhafazendinha.game"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "0.4.0"
    }

    sourceSets["main"].res.srcDir(generatedAudioRes)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.named("preBuild").configure {
    dependsOn(decodeAnimalAudio)
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
