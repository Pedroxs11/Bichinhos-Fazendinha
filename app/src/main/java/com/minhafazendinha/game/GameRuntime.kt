package com.minhafazendinha.game

/**
 * Estado mutavel minimo compartilhado pelos jogos produzidos pela fabrica.
 * Mantem selecao, moedas de progressao e desbloqueios fora das telas para
 * permitir que novos temas reutilizem a mesma engine de sessao.
 */
data class GameRuntimeState(
    val profileId: String,
    val selectedAnimalId: String,
    val selectedAreaId: String,
    val stars: Int,
    val hearts: Int,
    val unlockedAnimalIds: Set<String>,
    val unlockedAreaIds: Set<String>
)

object GameRuntime {
    fun fromBootstrap(state: GameBootstrapState): GameRuntimeState = GameRuntimeState(
        profileId = state.profile.identity.id,
        selectedAnimalId = state.initialAnimal.id,
        selectedAreaId = state.initialArea.id,
        stars = state.stars.coerceAtLeast(0),
        hearts = state.hearts.coerceIn(0, state.profile.progression.maxHearts),
        unlockedAnimalIds = state.animals
            .filter { it.unlockStars <= state.stars }
            .mapTo(linkedSetOf()) { it.id }
            .plus(state.initialAnimal.id),
        unlockedAreaIds = state.areas
            .filter { it.unlockStars <= state.stars }
            .mapTo(linkedSetOf()) { it.id }
            .plus(state.initialArea.id)
    )

    fun addStars(state: GameRuntimeState, amount: Int): GameRuntimeState {
        val profile = requireProfile(state)
        val stars = (state.stars + amount).coerceAtLeast(0)
        return state.copy(
            stars = stars,
            unlockedAnimalIds = profile.animals()
                .filter { it.unlockStars <= stars }
                .mapTo(linkedSetOf()) { it.id }
                .plus(profile.initialAnimalId),
            unlockedAreaIds = profile.areas()
                .filter { it.unlockStars <= stars }
                .mapTo(linkedSetOf()) { it.id }
                .plus(profile.initialAreaId)
        )
    }

    fun addHearts(state: GameRuntimeState, amount: Int): GameRuntimeState {
        val profile = requireProfile(state)
        return state.copy(
            hearts = (state.hearts + amount).coerceIn(0, profile.progression.maxHearts)
        )
    }

    fun selectAnimal(state: GameRuntimeState, animalId: String): GameRuntimeState =
        if (animalId in state.unlockedAnimalIds) state.copy(selectedAnimalId = animalId) else state

    fun selectArea(state: GameRuntimeState, areaId: String): GameRuntimeState =
        if (areaId in state.unlockedAreaIds) state.copy(selectedAreaId = areaId) else state

    private fun requireProfile(state: GameRuntimeState): GameProfile {
        val profile = FarmGameProfile.current
        require(profile.identity.id == state.profileId) {
            "Runtime profile not registered: ${state.profileId}"
        }
        return profile
    }
}
