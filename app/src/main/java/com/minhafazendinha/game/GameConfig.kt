package com.minhafazendinha.game

/**
 * Configuracao central para transformar a Fazendinha em uma base reutilizavel.
 * Nos proximos jogos, tema, recompensas e elenco podem ser trocados aqui sem
 * reescrever as mecanicas principais.
 */
object GameConfig {
    const val TITLE = "🌻 BICHINHOS & FAZENDINHA"
    const val SUBTITLE = "Vamos cuidar dos nossos amigos!"
    const val LAKE_UNLOCK_STARS = 15
    const val CARE_STAR_REWARD = 5
    const val CARE_HEART_REWARD = 1
    const val FARM_REWARD = 2
    const val COOP_REWARD = 1

    data class Animal(val emoji: String, val name: String, val key: String)

    val animals = listOf(
        Animal("🐮", "Vaca", "vaca"),
        Animal("🐔", "Galinha", "galinha"),
        Animal("🐶", "Cachorro", "cachorro"),
        Animal("🐴", "Cavalo", "cavalo"),
        Animal("🐷", "Porco", "porco"),
        Animal("🐑", "Ovelha", "ovelha"),
        Animal("🐐", "Cabra", "cabra"),
        Animal("🫏", "Burro", "burro")
    )

    val accessories = listOf("👒", "🎀", "🧢", "👑", "🕶️", "🧣", "🎩")
}
