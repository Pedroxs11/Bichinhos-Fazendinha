package com.minhafazendinha.game

/**
 * Catalogo declarativo da fabrica de jogos.
 * Centraliza conteudo que muda entre jogos sem misturar com progresso ou UI.
 */
data class AnimalDefinition(
    val id: String,
    val name: String,
    val emoji: String,
    val soundAsset: String? = null,
    val unlockStars: Int = 0
)

data class ActivityDefinition(
    val id: String,
    val title: String,
    val emoji: String,
    val rewardStars: Int = 1,
    val rewardHearts: Int = 1
)

data class AreaDefinition(
    val id: String,
    val title: String,
    val emoji: String,
    val unlockStars: Int = 0
)

object GameCatalog {
    val animals = listOf(
        AnimalDefinition("cow", "Vaquinha", "🐮"),
        AnimalDefinition("pig", "Porquinho", "🐷"),
        AnimalDefinition("chicken", "Galinha", "🐔"),
        AnimalDefinition("horse", "Cavalinho", "🐴"),
        AnimalDefinition("sheep", "Ovelhinha", "🐑")
    )

    val activities = listOf(
        ActivityDefinition("feed", "Dar comida", "🥕"),
        ActivityDefinition("water", "Dar agua", "💧"),
        ActivityDefinition("clean", "Dar banho", "🫧"),
        ActivityDefinition("love", "Fazer carinho", "❤️")
    )

    val areas = listOf(
        AreaDefinition("farm", "Fazendinha", "🌾"),
        AreaDefinition("wardrobe", "Armario", "👕"),
        AreaDefinition("lake", "Lago", "🌊", unlockStars = 8)
    )

    fun animal(id: String) = animals.firstOrNull { it.id == id }
    fun activity(id: String) = activities.firstOrNull { it.id == id }
    fun area(id: String) = areas.firstOrNull { it.id == id }
}
