package com.minhafazendinha.game

import android.content.Context

/**
 * Progresso persistente reutilizavel pela fabrica de jogos.
 * Mantem a Activity livre dos detalhes de armazenamento.
 * O nome legado e mantido para preservar estrelas e coracoes de instalacoes existentes.
 */
class GameProgress(context: Context) {
    private val prefs = context.getSharedPreferences("farm_progress", Context.MODE_PRIVATE)

    val stars: Int get() = prefs.getInt("stars", 0)
    val hearts: Int get() = prefs.getInt("hearts", 0)

    fun addStars(amount: Int) {
        if (amount > 0) prefs.edit().putInt("stars", stars + amount).apply()
    }

    fun addHearts(amount: Int) {
        if (amount > 0) prefs.edit().putInt("hearts", hearts + amount).apply()
    }

    fun hasStars(required: Int): Boolean = stars >= required

    fun reward(starReward: Int = 0, heartReward: Int = 0) {
        if (starReward <= 0 && heartReward <= 0) return
        prefs.edit().apply {
            if (starReward > 0) putInt("stars", stars + starReward)
            if (heartReward > 0) putInt("hearts", hearts + heartReward)
        }.apply()
    }

    fun reset() = prefs.edit().clear().apply()
}
