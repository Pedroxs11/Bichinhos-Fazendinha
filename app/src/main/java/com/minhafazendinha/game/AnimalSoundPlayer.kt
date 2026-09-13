package com.minhafazendinha.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/** Reprodutor curto e confiável para os sons dos bichinhos. */
class AnimalSoundPlayer(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val sounds = mutableMapOf<String, Int>()
    private var loaded = 0

    init {
        val rawNames = listOf(
            "sound_cow", "sound_chicken", "sound_dog", "sound_donkey",
            "sound_goat", "sound_horse", "sound_pig", "sound_sheep"
        )
        pool.setOnLoadCompleteListener { _, _, status -> if (status == 0) loaded++ }
        rawNames.forEach { name ->
            val id = context.resources.getIdentifier(name, "raw", context.packageName)
            if (id != 0) sounds[name] = pool.load(context, id, 1)
        }
    }

    fun play(animal: String) {
        val key = when (animal.lowercase()) {
            "vaca" -> "sound_cow"
            "galinha", "pintinho" -> "sound_chicken"
            "cachorro" -> "sound_dog"
            "burro" -> "sound_donkey"
            "cabra" -> "sound_goat"
            "cavalo" -> "sound_horse"
            "porco" -> "sound_pig"
            "ovelha" -> "sound_sheep"
            else -> return
        }
        sounds[key]?.let { pool.play(it, 1f, 1f, 1, 0, 1f) }
    }

    fun release() = pool.release()
}
