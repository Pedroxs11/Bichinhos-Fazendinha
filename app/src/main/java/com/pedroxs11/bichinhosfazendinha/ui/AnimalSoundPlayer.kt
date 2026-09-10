package com.pedroxs11.bichinhosfazendinha.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

private const val SAMPLE_RATE = 16_000

@Volatile
private var animalAudioContext: Context? = null

private val playerLock = Any()
private var activePlayer: MediaPlayer? = null

fun initializeAnimalAudio(context: Context) {
    animalAudioContext = context.applicationContext
}

fun playAnimalSound(animalName: String) {
    val context = animalAudioContext
    if (context != null && playRecordedAnimalSound(context, animalName)) return
    if (context != null && playRemoteAnimalSound(animalName)) return
    playSynthesizedAnimalSound(animalName)
}

private fun recordedResourceName(animalName: String): String? = when (animalName) {
    "Vaca" -> "sound_cow"
    "Porquinho" -> "sound_pig"
    "Galinha" -> "sound_chicken"
    "Pintinho" -> "sound_chick"
    "Cachorro" -> "sound_dog"
    "Pato" -> "sound_duck"
    "Ovelha" -> "sound_sheep"
    "Cabra" -> "sound_goat"
    "Cavalo" -> "sound_horse"
    "Burrinho" -> "sound_donkey"
    "Coelho" -> "sound_rabbit"
    else -> null
}

private fun remoteAnimalSoundUrl(animalName: String): String? = when (animalName) {
    "Vaca" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Single_Cow_Moo.ogg"
    "Porquinho" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Mudchute_pig_1.ogg"
    "Galinha" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Hen_announcing_shes_lain_an_egg.ogg"
    "Cachorro" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Sound-of-dog.ogg"
    "Pato" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Anas_platyrhynchos_-_Mallard_-_XC62258.ogg"
    "Ovelha" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Mudchute_sheep_1.ogg"
    "Cabra" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Herd_of_goats_bleating.ogg"
    "Cavalo" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Wiehern.ogg"
    "Burrinho" -> "https://commons.wikimedia.org/wiki/Special:Redirect/file/Personality-of-Wild-Male-Crested-Macaques-(Macaca-nigra)-pone.0069383.s002.oga"
    else -> null
}

private fun playRecordedAnimalSound(context: Context, animalName: String): Boolean {
    val resourceName = recordedResourceName(animalName) ?: return false
    val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
    if (resourceId == 0) return false

    return runCatching {
        val player = MediaPlayer.create(context, resourceId) ?: return false
        installPlayer(player, animalName, false)
        player.start()
        true
    }.getOrDefault(false)
}

private fun playRemoteAnimalSound(animalName: String): Boolean {
    val url = remoteAnimalSoundUrl(animalName) ?: return false
    return runCatching {
        val player = MediaPlayer()
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        installPlayer(player, animalName, true)
        player.setDataSource(url)
        player.setOnPreparedListener { prepared ->
            synchronized(playerLock) {
                if (activePlayer !== prepared) {
                    runCatching { prepared.release() }
                    return@setOnPreparedListener
                }
            }
            prepared.start()
        }
        player.prepareAsync()
        true
    }.getOrElse {
        false
    }
}

private fun installPlayer(player: MediaPlayer, animalName: String, useSynthFallbackOnError: Boolean) {
    synchronized(playerLock) {
        runCatching { activePlayer?.stop() }
        runCatching { activePlayer?.release() }
        activePlayer = player
    }

    player.setOnCompletionListener { completed ->
        synchronized(playerLock) {
            if (activePlayer === completed) activePlayer = null
        }
        completed.release()
    }

    player.setOnErrorListener { failed, _, _ ->
        synchronized(playerLock) {
            if (activePlayer === failed) activePlayer = null
        }
        runCatching { failed.release() }
        if (useSynthFallbackOnError) playSynthesizedAnimalSound(animalName)
        true
    }
}

private fun playSynthesizedAnimalSound(animalName: String) {
    thread(name = "animal-sound", isDaemon = true) {
        val durationSeconds = when (animalName) {
            "Vaca" -> 1.2
            "Porquinho" -> 0.9
            "Galinha", "Pintinho" -> 1.0
            "Cachorro" -> 1.0
            "Pato" -> 0.9
            "Ovelha", "Cabra" -> 1.0
            "Cavalo", "Burrinho" -> 1.2
            "Coelho" -> 0.65
            else -> 0.7
        }
        val sampleCount = (SAMPLE_RATE * durationSeconds).toInt()
        val samples = ShortArray(sampleCount)

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE.toDouble()
            val total = sampleCount.toDouble()
            val position = i / total
            val attack = (position / 0.05).coerceIn(0.0, 1.0)
            val release = ((1.0 - position) / 0.15).coerceIn(0.0, 1.0)
            val envelope = minOf(attack, release)

            val value = when (animalName) {
                "Vaca" -> cowWave(t)
                "Porquinho" -> pigWave(t)
                "Galinha" -> chickenWave(t)
                "Pintinho" -> chickWave(t)
                "Cachorro" -> dogWave(t)
                "Pato" -> duckWave(t)
                "Ovelha" -> sheepWave(t)
                "Cabra" -> goatWave(t)
                "Cavalo" -> horseWave(t)
                "Burrinho" -> donkeyWave(t)
                "Coelho" -> rabbitWave(t)
                else -> 0.35 * sin(2.0 * PI * 440.0 * t)
            }

            samples[i] = (value.coerceIn(-1.0, 1.0) * envelope * 27_000.0).toInt().toShort()
        }

        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBuffer, samples.size * 2)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(samples, 0, samples.size)
            track.play()
            val waitMs = (durationSeconds * 1_000).toLong() + 100L
            Thread.sleep(waitMs)
        } finally {
            runCatching { track.stop() }
            track.release()
        }
    }
}

private fun cowWave(t: Double): Double {
    val base = 112.0 + 14.0 * sin(2.0 * PI * 1.3 * t)
    return 0.58 * sin(2.0 * PI * base * t) +
        0.20 * sin(2.0 * PI * base * 2.0 * t) +
        0.08 * sin(2.0 * PI * base * 3.0 * t)
}

private fun pigWave(t: Double): Double {
    val pulse = 0.35 + 0.65 * sin(2.0 * PI * 3.2 * t).let { it * it }
    val base = 205.0 + 32.0 * sin(2.0 * PI * 4.0 * t)
    return pulse * (
        0.48 * sin(2.0 * PI * base * t) +
            0.20 * sin(2.0 * PI * base * 2.0 * t)
        )
}

private fun chickenWave(t: Double): Double {
    val pulse = 0.25 + 0.75 * sin(2.0 * PI * 6.0 * t).let {
        val squared = it * it
        squared * squared
    }
    val base = 640.0 + 170.0 * sin(2.0 * PI * 7.0 * t)
    return pulse * (
        0.36 * sin(2.0 * PI * base * t) +
            0.17 * sin(2.0 * PI * 980.0 * t)
        )
}

private fun chickWave(t: Double): Double {
    val pulse = if ((t * 6.5).toInt() % 2 == 0) 1.0 else 0.25
    val base = 1250.0 + 180.0 * sin(2.0 * PI * 8.0 * t)
    return pulse * (0.32 * sin(2.0 * PI * base * t) + 0.12 * sin(2.0 * PI * 1850.0 * t))
}

private fun dogWave(t: Double): Double {
    val first = barkBurst(t, 0.05, 0.27)
    val second = barkBurst(t, 0.48, 0.72)
    return first + second
}

private fun barkBurst(t: Double, start: Double, end: Double): Double {
    if (t !in start..end) return 0.0
    val local = t - start
    val decay = exp(-7.0 * local)
    return decay * (
        0.62 * sin(2.0 * PI * 185.0 * t) +
            0.25 * sin(2.0 * PI * 370.0 * t) +
            0.10 * sin(2.0 * PI * 555.0 * t)
        )
}

private fun duckWave(t: Double): Double {
    val wobble = 0.7 + 0.3 * sin(2.0 * PI * 5.5 * t)
    val base = 310.0 + 70.0 * sin(2.0 * PI * 3.0 * t)
    return wobble * (0.45 * sin(2.0 * PI * base * t) + 0.18 * sin(2.0 * PI * base * 2.0 * t))
}

private fun sheepWave(t: Double): Double {
    val base = 185.0 + 28.0 * sin(2.0 * PI * 5.0 * t)
    return 0.50 * sin(2.0 * PI * base * t) + 0.20 * sin(2.0 * PI * base * 2.0 * t)
}

private fun goatWave(t: Double): Double {
    val base = 245.0 + 55.0 * sin(2.0 * PI * 6.0 * t)
    return 0.46 * sin(2.0 * PI * base * t) + 0.22 * sin(2.0 * PI * base * 2.2 * t)
}

private fun horseWave(t: Double): Double {
    val sweep = 320.0 + 170.0 * sin(2.0 * PI * 1.7 * t)
    val tremolo = 0.65 + 0.35 * sin(2.0 * PI * 7.0 * t)
    return tremolo * (0.38 * sin(2.0 * PI * sweep * t) + 0.18 * sin(2.0 * PI * sweep * 1.8 * t))
}

private fun donkeyWave(t: Double): Double {
    val pulse = 0.55 + 0.45 * sin(2.0 * PI * 2.2 * t)
    val base = 165.0 + 90.0 * sin(2.0 * PI * 1.2 * t)
    return pulse * (0.46 * sin(2.0 * PI * base * t) + 0.20 * sin(2.0 * PI * base * 2.0 * t))
}

private fun rabbitWave(t: Double): Double {
    val pulse = if ((t * 10.0).toInt() % 3 == 0) 1.0 else 0.18
    return pulse * (0.18 * sin(2.0 * PI * 1500.0 * t) + 0.08 * sin(2.0 * PI * 2200.0 * t))
}
