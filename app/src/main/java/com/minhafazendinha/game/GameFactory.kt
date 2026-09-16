package com.minhafazendinha.game

/**
 * Camada de fabrica para reduzir codigo repetido entre jogos.
 * Centraliza consultas de conteudo, desbloqueios e recompensas para que
 * futuras Activities apenas descrevam o jogo e renderizem as telas.
 */
object GameFactory {
    fun animal(key: String) = GameCatalog.animals.firstOrNull { it.key == key }

    fun activity(id: String) = GameCatalog.activities.firstOrNull { it.id == id }

    fun area(id: String) = GameCatalog.areas.firstOrNull { it.id == id }

    fun unlockedAreas(progress: GameProgress) =
        GameCatalog.areas.filter { progress.hasStars(it.requiredStars) }

    fun lockedAreas(progress: GameProgress) =
        GameCatalog.areas.filterNot { progress.hasStars(it.requiredStars) }

    fun starsUntilArea(areaId: String, progress: GameProgress): Int {
        val required = area(areaId)?.requiredStars ?: return 0
        return (required - progress.stars).coerceAtLeast(0)
    }

    fun grantActivityReward(activityId: String, progress: GameProgress): Boolean {
        val item = activity(activityId) ?: return false
        progress.reward(item.starReward, item.heartReward)
        return true
    }

    fun validateCatalog(): List<String> {
        val errors = mutableListOf<String>()
        if (GameCatalog.animals.map { it.key }.distinct().size != GameCatalog.animals.size) errors += "animal keys duplicadas"
        if (GameCatalog.activities.map { it.id }.distinct().size != GameCatalog.activities.size) errors += "activity ids duplicados"
        if (GameCatalog.areas.map { it.id }.distinct().size != GameCatalog.areas.size) errors += "area ids duplicados"
        GameCatalog.activities.filter { it.starReward < 0 || it.heartReward < 0 }.forEach { errors += "recompensa invalida: ${it.id}" }
        GameCatalog.areas.filter { it.requiredStars < 0 }.forEach { errors += "desbloqueio invalido: ${it.id}" }
        return errors
    }
}
