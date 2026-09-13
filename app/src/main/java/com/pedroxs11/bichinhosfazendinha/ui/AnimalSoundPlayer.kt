package com.pedroxs11.bichinhosfazendinha.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock

private const val TAP_DEBOUNCE_MS = 180L
private const val PLAYBACK_VOLUME = 0.58f

@Volatile
private var animalAudioContext: Context? = null

private val playerLock = Any()
private var activePlayer: MediaPlayer? = null
private var lastAnimalName: String? = null
private var lastTapAtMs: Long = 0L

fun initializeAnimalAudio(context: Context) {
    animalAudioContext = context.applicationContext
}

fun stopAnimalAudio() {
    synchronized(playerLock) {
        runCatching { activePlayer?.stop() }
        runCatching { activePlayer?.release() }
        activePlayer = null
    }
}

fun playAnimalSound(animalName: String) {
    val now = SystemClock.elapsedRealtime()
    synchronized(playerLock) {
        if (animalName == lastAnimalName && now - lastTapAtMs < TAP_DEBOUNCE_MS) return
        lastAnimalName = animalName
        lastTapAtMs = now
    }

    stopAnimalAudio()

    val context = animalAudioContext ?: return
    playRecordedAnimalSound(context, animalName)
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

fun hasRecordedAnimalSound(context: Context, animalName: String): Boolean {
    val resourceName = recordedResourceName(animalName) ?: return false
    return context.resources.getIdentifier(resourceName, "raw", context.packageName) != 0
}

private fun playRecordedAnimalSound(context: Context, animalName: String): Boolean {
    val resourceName = recordedResourceName(animalName) ?: return false
    val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
    if (resourceId == 0) return false

    return runCatching {
        val player = MediaPlayer.create(context, resourceId) ?: return false
        player.setVolume(PLAYBACK_VOLUME, PLAYBACK_VOLUME)
        synchronized(playerLock) {
            activePlayer = player
        }
        player.setOnCompletionListener { completed ->
            synchronized(playerLock) {
                if (activePlayer === completed) activePlayer = null
            }
            runCatching { completed.release() }
        }
        player.setOnErrorListener { failed, _, _ ->
            synchronized(playerLock) {
                if (activePlayer === failed) activePlayer = null
            }
            runCatching { failed.release() }
            true
        }
        player.start()
        true
    }.getOrDefault(false)
}
