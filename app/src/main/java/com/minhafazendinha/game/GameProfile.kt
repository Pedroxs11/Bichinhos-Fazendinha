package com.minhafazendinha.game

/**
 * Perfil de alto nivel para transformar a base atual em uma fabrica de jogos.
 * Cada novo titulo pode declarar identidade, progressao e quais itens do
 * catalogo estao ativos sem reescrever a engine.
 */
data class GameIdentity(
    val id: String,
    val title: String,
    val subtitle: String,
    val versionLabel: String = "1.0"
)

data class ProgressionProfile(
    val initialStars: Int = 0,
    val initialHearts: Int = 5,
    val maxHearts: Int = 5
)

data class GameProfile(
    val identity: GameIdentity,
    val progression: ProgressionProfile,
    val initialAnimalId: String,
    val initialAreaId: String,
    val enabledAnimalIds: Set<String>,
    val enabledActivityIds: Set<String>,
    val enabledAreaIds: Set<String>
) {
    fun animals(): List<AnimalDefinition> =
        GameCatalog.animals.filter { it.id in enabledAnimalIds }

    fun activities(): List<ActivityDefinition> =
        GameCatalog.activities.filter { it.id in enabledActivityIds }

    fun areas(): List<AreaDefinition> =
        GameCatalog.areas.filter { it.id in enabledAreaIds }

    fun validationErrors(): List<String> = buildList {
        if (identity.id.isBlank()) add("Game id cannot be blank")
        if (identity.title.isBlank()) add("Game title cannot be blank")
        if (progression.maxHearts <= 0) add("maxHearts must be greater than zero")
        if (progression.initialHearts !in 0..progression.maxHearts) add("Invalid initial hearts")
        if (initialAnimalId !in enabledAnimalIds) add("Initial animal must be enabled")
        if (initialAreaId !in enabledAreaIds) add("Initial area must be enabled")
        enabledAnimalIds.filter { GameCatalog.animal(it) == null }.forEach { add("Unknown animal: $it") }
        enabledActivityIds.filter { GameCatalog.activity(it) == null }.forEach { add("Unknown activity: $it") }
        enabledAreaIds.filter { GameCatalog.area(it) == null }.forEach { add("Unknown area: $it") }
    }
}

object FarmGameProfile {
    val current = GameProfile(
        identity = GameIdentity(
            id = "minha_fazendinha",
            title = "Minha Fazendinha",
            subtitle = "Cuide, brinque e descubra!"
        ),
        progression = ProgressionProfile(),
        initialAnimalId = "cow",
        initialAreaId = "farm",
        enabledAnimalIds = GameCatalog.animals.mapTo(linkedSetOf()) { it.id },
        enabledActivityIds = GameCatalog.activities.mapTo(linkedSetOf()) { it.id },
        enabledAreaIds = GameCatalog.areas.mapTo(linkedSetOf()) { it.id }
    )
}
