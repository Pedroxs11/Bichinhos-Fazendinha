package com.minhafazendinha.game

/**
 * Single bootstrap object for future games made with the Fazendinha factory.
 * It joins declarative product configuration to the reusable care engine.
 */
data class GameKit(
    val template: GameTemplate,
    val character: CarePetDefinition,
    val state: MimosaCareState,
    val visualKeys: Map<CareAction, String>,
    val soundKeys: Map<CareAction, String>
) {
    fun validate(): List<String> = buildList {
        addAll(GameTemplateFactory.validate(template))
        if (character.id != template.characterId) add("character.mismatch")
        if (visualKeys.keys.containsAll(template.primaryLoop).not()) add("visual.actions")
        if (soundKeys.keys.containsAll(template.primaryLoop).not()) add("sound.actions")
        template.primaryLoop.forEach { action ->
            if (character.actions.none { it.id == action.key }) add("care.${action.key}")
        }
    }
}

object GameKitFactory {
    fun create(template: GameTemplate): GameKit {
        val character = requireNotNull(CareGameFactory.pet(template.characterId)) {
            "Unknown care character: ${template.characterId}"
        }
        val state = CareGameFactory.stateFor(character).apply { coins = template.startingCoins }
        val visuals = template.primaryLoop.associateWith { action ->
            character.visual.assetFor(action.key)
        }
        val sounds = template.primaryLoop.associateWith { action -> character.soundKey }
        return GameKit(template, character, state, visuals, sounds)
    }

    /** Ready-to-use kit for the current game and reference implementation for clones. */
    val farm: GameKit by lazy { create(GameTemplateFactory.farm) }
}
