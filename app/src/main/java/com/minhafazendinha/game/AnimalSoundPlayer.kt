package com.minhafazendinha.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/** Reprodutor dos sons dos bichinhos com fila para toques feitos antes do carregamento. */
class AnimalSoundPlayer(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build())
        .build()

    private val sounds = mutableMapOf<String, Int>()
    private val loadedSamples = mutableSetOf<Int>()
    private var pendingSample: Int? = null

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSamples += sampleId
                if (pendingSample == sampleId) {
                    pendingSample = null
                    playSample(sampleId)
                }
            }
        }
        listOf("sound_cow","sound_chicken","sound_dog","sound_donkey","sound_goat","sound_horse","sound_pig","sound_sheep").forEach { name ->
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
        val sample = sounds[key] ?: return
        if (sample in loadedSamples) playSample(sample) else pendingSample = sample
    }

    private fun playSample(sample:Int) { pool.play(sample, 1f, 1f, 1, 0, 1f) }
    fun release() { pendingSample=null; pool.release() }
}
