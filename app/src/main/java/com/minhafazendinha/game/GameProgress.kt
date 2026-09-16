package com.minhafazendinha.game

import android.content.Context

/**
 * Progresso persistente reutilizavel pela fabrica de jogos.
 * Mantem a Activity livre dos detalhes de armazenamento.
 */
class GameProgress(context: Context) {
    private val prefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)

    var stars: Int
        get() = prefs.getInt("stars", 0)
        private set(value) { prefs.edit().putInt("stars", value.coerceAtLeast(0)).apply() }

    var hearts: Int
        get() = prefs.getInt("hearts", 0)
        private set(value) { prefs.edit().putInt("hearts", value.coerceAtLeast(0)).apply() }

    fun addStars(amount: Int) { if (amount > 0) stars += amount }
    fun addHearts(amount: Int) { if (amount > 0) hearts += amount }
    fun hasStars(required: Int): Boolean = stars >= required

    fun reward(starReward: Int = 0, heartReward: Int = 0) {
        addStars(starReward)
        addHearts(heartReward)
    }

    fun reset() {
        prefs.edit().clear().apply()
    }
}
