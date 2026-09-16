package com.minhafazendinha.game

/**
 * Fachada reutilizavel de sessao da fabrica de jogos.
 * Une bootstrap + runtime para que telas e futuros jogos trabalhem com uma
 * unica API, sem conhecer detalhes de catalogo, desbloqueio ou progressao.
 */
class GameSession private constructor(
    val bootstrap: GameBootstrapState,
    initialState: GameRuntimeState
) {
    var state: GameRuntimeState = initialState
        private set

    val selectedAnimal: AnimalDefinition?
        get() = bootstrap.animals.firstOrNull { it.id == state.selectedAnimalId }

    val selectedArea: AreaDefinition?
        get() = bootstrap.areas.firstOrNull { it.id == state.selectedAreaId }

    val unlockedAnimals: List<AnimalDefinition>
        get() = bootstrap.animals.filter { it.id in state.unlockedAnimalIds }

    val unlockedAreas: List<AreaDefinition>
        get() = bootstrap.areas.filter { it.id in state.unlockedAreaIds }

    fun addStars(amount: Int): GameRuntimeState = update(GameRuntime.addStars(state, amount))

    fun addHearts(amount: Int): GameRuntimeState = update(GameRuntime.addHearts(state, amount))

    fun selectAnimal(animalId: String): Boolean {
        val next = GameRuntime.selectAnimal(state, animalId)
        val changed = next.selectedAnimalId != state.selectedAnimalId
        state = next
        return changed
    }

    fun selectArea(areaId: String): Boolean {
        val next = GameRuntime.selectArea(state, areaId)
        val changed = next.selectedAreaId != state.selectedAreaId
        state = next
        return changed
    }

    fun reset(): GameRuntimeState = update(GameRuntime.fromBootstrap(bootstrap))

    private fun update(next: GameRuntimeState): GameRuntimeState {
        state = next
        return state
    }

    companion object {
        fun create(profile: GameProfile = FarmGameProfile.current): GameSession {
            val bootstrap = GameBootstrap.requireReady(profile)
            return GameSession(bootstrap, GameRuntime.fromBootstrap(bootstrap))
        }
    }
}
