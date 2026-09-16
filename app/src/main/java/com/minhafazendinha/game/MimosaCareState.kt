package com.minhafazendinha.game

data class MimosaCareState(
    var hunger: Int = 80,
    var hygiene: Int = 70,
    var happiness: Int = 75,
    var energy: Int = 90,
    var coins: Int = 120
) {
    fun feed(): CareReaction {
        val before = hunger
        hunger = (hunger + 10).coerceAtMost(100)
        val gained = if (hunger > before) 2 else 0
        coins += gained
        return CareReaction("🍎", "Muuu! Que delícia!", gained)
    }

    fun bathe(): CareReaction {
        val before = hygiene
        hygiene = (hygiene + 15).coerceAtMost(100)
        val gained = if (hygiene > before) 2 else 0
        coins += gained
        return CareReaction("🚿", "Muuu! Estou limpinha!", gained)
    }

    fun brush(): CareReaction {
        happiness = (happiness + 8).coerceAtMost(100)
        return CareReaction("🧹", "Que carinho gostoso!", 1).also { coins += it.coins }
    }

    fun play(): CareReaction {
        happiness = (happiness + 12).coerceAtMost(100)
        energy = (energy - 8).coerceAtLeast(0)
        return CareReaction("🏐", "Muuu! Vamos brincar!", 2).also { coins += it.coins }
    }
}

data class CareReaction(val icon: String, val message: String, val coins: Int)
