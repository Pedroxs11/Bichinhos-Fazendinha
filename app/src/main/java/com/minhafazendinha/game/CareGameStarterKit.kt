package com.minhafazendinha.game

/**
 * One entry point for bootstrapping the next care game.
 * Keeps art naming, runtime asset contracts and production diagnostics aligned.
 */
data class CareGameStarterSpec(
    val gameId: String,
    val characterPrefix: String,
    val scenePrefix: String = gameId,
    val actions: List<String> = listOf("feed", "bath", "brush", "play")
) {
    init {
        require(gameId.isNotBlank())
        require(characterPrefix.isNotBlank())
        require(actions.isNotEmpty())
        require(actions.distinct().size == actions.size)
    }
}

data class CareGameStarterKit(
    val spec: CareGameStarterSpec,
    val contract: CareAssetContract,
    val pack: CareGameAssetPack
) {
    fun expectedDrawables(): Set<String> = pack.requiredDrawables()

    fun readiness(available: Set<String>): CareGameAssetPackReadiness = pack.readiness(available)

    fun productionChecklist(available: Set<String>): List<String> {
        val ready = readiness(available)
        return buildList {
            add("${spec.gameId}: ${ready.progress}% ready (${ready.available}/${ready.required})")
            expectedDrawables().sorted().forEach { drawable ->
                add("${if (drawable in available) "OK" else "TODO"}: $drawable")
            }
        }
    }
}

object CareGameStarterFactory {
    fun create(spec: CareGameStarterSpec): CareGameStarterKit {
        val prefix = spec.characterPrefix
        val contract = CareAssetContract(
            characterIdle = "${prefix}_character_idle",
            characterHappy = "${prefix}_character_happy",
            feedProp = "${spec.gameId}_prop_food",
            bathProp = "${spec.gameId}_prop_bath",
            brushProp = "${spec.gameId}_prop_brush",
            playProp = "${spec.gameId}_prop_play",
            feedEffect = "fx_hearts",
            bathEffect = "fx_bubbles",
            brushEffect = "fx_sparkles",
            playEffect = "fx_confetti"
        )
        val pack = CareGameAssetPack(
            gameId = spec.gameId,
            contract = contract,
            background = "${spec.scenePrefix}_care_scene",
            foreground = "${spec.scenePrefix}_care_foreground",
            actionCharacters = spec.actions.associateWith { action ->
                "${prefix}_care_$action"
            }
        )
        return CareGameStarterKit(spec, contract, pack)
    }

    /** Ready-to-copy reference proving the same factory can describe Fazendinha. */
    val MIMOSA_REFERENCE = create(
        CareGameStarterSpec(
            gameId = "farm",
            characterPrefix = "mimosa",
            scenePrefix = "farm"
        )
    )
}
