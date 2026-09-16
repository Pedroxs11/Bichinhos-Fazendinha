package com.minhafazendinha.game

/**
 * Camada de fabrica para reduzir codigo repetido entre jogos.
 * Centraliza consultas de conteudo, desbloqueios e recompensas para que
 * futuras Activities apenas descrevam o jogo e renderizem as telas.
 */
object GameFactory {
    fun animal(id: String): AnimalDefinition? = GameCatalog.animal(id)

    fun activity(id: String): ActivityDefinition? = GameCatalog.activity(id)

    fun area(id: String): AreaDefinition? = GameCatalog.area(id)

    fun unlockedAnimals(progress: GameProgress): List<AnimalDefinition> =
        GameCatalog.animals.filter { progress.hasStars(it.unlockStars) }

    fun lockedAnimals(progress: GameProgress): List<AnimalDefinition> =
        GameCatalog.animals.filterNot { progress.hasStars(it.unlockStars) }

    fun unlockedAreas(progress: GameProgress): List<AreaDefinition> =
        GameCatalog.areas.filter { progress.hasStars(it.unlockStars) }

    fun lockedAreas(progress: GameProgress): List<AreaDefinition> =
        GameCatalog.areas.filterNot { progress.hasStars(it.unlockStars) }

    fun starsUntilArea(areaId: String, progress: GameProgress): Int {
        val required = area(areaId)?.unlockStars ?: return 0
        return (required - progress.stars).coerceAtLeast(0)
    }

    fun starsUntilAnimal(animalId: String, progress: GameProgress): Int {
        val required = animal(animalId)?.unlockStars ?: return 0
        return (required - progress.stars).coerceAtLeast(0)
    }

    fun grantActivityReward(activityId: String, progress: GameProgress): Boolean {
        val item = activity(activityId) ?: return false
        progress.reward(item.rewardStars, item.rewardHearts)
        return true
    }

    fun validateCatalog(): List<String> {
        val errors = mutableListOf<String>()
        if (GameCatalog.animals.map { it.id }.distinct().size != GameCatalog.animals.size) errors += "animal ids duplicados"
        if (GameCatalog.activities.map { it.id }.distinct().size != GameCatalog.activities.size) errors += "activity ids duplicados"
        if (GameCatalog.areas.map { it.id }.distinct().size != GameCatalog.areas.size) errors += "area ids duplicados"
        GameCatalog.activities
            .filter { it.rewardStars < 0 || it.rewardHearts < 0 }
            .forEach { errors += "recompensa invalida: ${it.id}" }
        GameCatalog.animals
            .filter { it.unlockStars < 0 }
            .forEach { errors += "desbloqueio de animal invalido: ${it.id}" }
        GameCatalog.areas
            .filter { it.unlockStars < 0 }
            .forEach { errors += "desbloqueio de area invalido: ${it.id}" }
        return errors
    }
}
