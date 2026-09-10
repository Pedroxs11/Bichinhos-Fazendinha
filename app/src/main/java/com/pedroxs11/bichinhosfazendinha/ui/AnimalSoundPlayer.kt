package com.pedroxs11.bichinhosfazendinha.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

private const val SAMPLE_RATE = 16_000

fun playAnimalSound(animalName: String) {
    thread(name = "animal-sound", isDaemon = true) {
        val durationSeconds = when (animalName) {
            "Vaca" -> 1.2
            "Porquinho" -> 0.9
            "Galinha" -> 1.0
            "Cachorro" -> 1.0
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
                "Cachorro" -> dogWave(t)
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
