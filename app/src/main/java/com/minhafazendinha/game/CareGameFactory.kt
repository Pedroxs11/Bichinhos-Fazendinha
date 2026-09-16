package com.minhafazendinha.game

/**
 * Reusable definition layer for virtual-pet care games.
 * New animals can share the same status/action engine and replace only content + art.
 */
data class CarePetDefinition(
    val id: String,
    val name: String,
    val emoji: String,
    val level: Int = 1,
    val soundKey: String,
    val initialStats: CareStats = CareStats(),
    val actions: List<CareActionDefinition>
)

data class CareStats(
    val hunger: Int = 80,
    val hygiene: Int = 70,
    val happiness: Int = 75,
    val energy: Int = 90,
    val coins: Int = 120
)

data class CareDelta(
    val hunger: Int = 0,
    val hygiene: Int = 0,
    val happiness: Int = 0,
    val energy: Int = 0,
    val coins: Int = 0
)

data class CareActionDefinition(
    val id: String,
    val icon: String,
    val label: String,
    val message: String,
    val buttonColor: Int,
    val delta: CareDelta
)

object CareGameFactory {
    val mimosa = CarePetDefinition(
        id = "mimosa",
        name = "Mimosa",
        emoji = "🐮",
        soundKey = "vaca",
        actions = listOf(
            CareActionDefinition("feed", "🍎", "Alimentar", "Muuu! Que delícia!", 0xFFFF625C.toInt(), CareDelta(hunger = 10, coins = 2)),
            CareActionDefinition("bathe", "🚿", "Banho", "Muuu! Estou limpinha!", 0xFF55B8FF.toInt(), CareDelta(hygiene = 15, coins = 2)),
            CareActionDefinition("brush", "🧹", "Escovar", "Que carinho gostoso!", 0xFFFFC83D.toInt(), CareDelta(happiness = 8, coins = 1)),
            CareActionDefinition("play", "🏐", "Brincar", "Muuu! Vamos brincar!", 0xFFFF65B7.toInt(), CareDelta(happiness = 12, energy = -8, coins = 2))
        )
    )

    fun stateFor(pet: CarePetDefinition) = MimosaCareState(
        hunger = pet.initialStats.hunger,
        hygiene = pet.initialStats.hygiene,
        happiness = pet.initialStats.happiness,
        energy = pet.initialStats.energy,
        coins = pet.initialStats.coins
    )
}
