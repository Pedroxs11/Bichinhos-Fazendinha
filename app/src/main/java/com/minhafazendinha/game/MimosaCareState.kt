package com.minhafazendinha.game

data class MimosaCareState(
    var hunger: Int = 80,
    var hygiene: Int = 70,
    var happiness: Int = 75,
    var energy: Int = 90,
    var coins: Int = 120
) {
    fun apply(action: CareActionDefinition): CareReaction {
        val d = action.delta
        val old = CareStats(hunger, hygiene, happiness, energy, coins)
        hunger = (hunger + d.hunger).coerceIn(0, 100)
        hygiene = (hygiene + d.hygiene).coerceIn(0, 100)
        happiness = (happiness + d.happiness).coerceIn(0, 100)
        energy = (energy + d.energy).coerceIn(0, 100)
        val changed = hunger != old.hunger || hygiene != old.hygiene || happiness != old.happiness || energy != old.energy
        val gained = if (changed || d.coins < 0) d.coins else 0
        coins = (coins + gained).coerceAtLeast(0)
        return CareReaction(action.icon, action.message, gained)
    }

    fun feed() = apply(CareGameFactory.mimosa.actions.first { it.id == "feed" })
    fun bathe() = apply(CareGameFactory.mimosa.actions.first { it.id == "bathe" })
    fun brush() = apply(CareGameFactory.mimosa.actions.first { it.id == "brush" })
    fun play() = apply(CareGameFactory.mimosa.actions.first { it.id == "play" })
}

data class CareReaction(val icon: String, val message: String, val coins: Int)
