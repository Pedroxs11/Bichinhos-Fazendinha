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
        rawDir.deleteRecursively()
        rawDir.mkdirs()

        sourceDir.asFile.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name }
            ?.forEach { animalDir ->
                val parts = animalDir.listFiles()
                    ?.filter { it.isFile && it.name.matches(Regex("part\\d+\\.txt")) }
                    ?.sortedBy { it.name }
                    .orEmpty()

                if (parts.isEmpty()) return@forEach

                val encoded = buildString {
                    parts.forEach { append(it.readText().filterNot(Char::isWhitespace)) }
                }
                val decoded = Base64.getDecoder().decode(encoded)
                rawDir.resolve("sound_${animalDir.name}.ogg").writeBytes(decoded)
                println("Audio ${animalDir.name}: ${parts.size} blocos -> ${decoded.size} bytes")
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
        versionCode = 6
        versionName = "0.6.0"
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
