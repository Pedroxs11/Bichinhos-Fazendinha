package com.minhafazendinha.game

/**
 * Ponto unico de inicializacao da fabrica.
 * Novos jogos fornecem apenas um GameProfile; esta camada valida o perfil,
 * resolve o conteudo inicial e entrega um snapshot pronto para a UI/engine.
 */
data class GameBootstrapState(
    val profile: GameProfile,
    val initialAnimal: AnimalDefinition,
    val initialArea: AreaDefinition,
    val animals: List<AnimalDefinition>,
    val activities: List<ActivityDefinition>,
    val areas: List<AreaDefinition>,
    val stars: Int,
    val hearts: Int
)

sealed class GameBootstrapResult {
    data class Ready(val state: GameBootstrapState) : GameBootstrapResult()
    data class Invalid(val errors: List<String>) : GameBootstrapResult()
}

object GameBootstrap {
    fun create(profile: GameProfile = FarmGameProfile.current): GameBootstrapResult {
        val errors = profile.validationErrors().toMutableList()
        val animal = GameCatalog.animal(profile.initialAnimalId)
        val area = GameCatalog.area(profile.initialAreaId)

        if (animal == null) errors += "Initial animal not found: ${profile.initialAnimalId}"
        if (area == null) errors += "Initial area not found: ${profile.initialAreaId}"

        if (errors.isNotEmpty() || animal == null || area == null) {
            return GameBootstrapResult.Invalid(errors.distinct())
        }

        return GameBootstrapResult.Ready(
            GameBootstrapState(
                profile = profile,
                initialAnimal = animal,
                initialArea = area,
                animals = profile.animals(),
                activities = profile.activities(),
                areas = profile.areas(),
                stars = profile.progression.initialStars.coerceAtLeast(0),
                hearts = profile.progression.initialHearts
            )
        )
    }

    fun requireReady(profile: GameProfile = FarmGameProfile.current): GameBootstrapState =
        when (val result = create(profile)) {
            is GameBootstrapResult.Ready -> result.state
            is GameBootstrapResult.Invalid -> error(
                "Invalid game profile: ${result.errors.joinToString("; ")}"
            )
        }
}
