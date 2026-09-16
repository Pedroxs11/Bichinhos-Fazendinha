package com.minhafazendinha.game

/**
 * Single bootstrap object for future games made with the Fazendinha factory.
 * A new title should need configuration here instead of copied gameplay code.
 */
data class GameKit(
    val template: GameTemplate,
    val character: CareCharacter,
    val state: CareState,
    val visualKeys: Map<CareAction, String>,
    val soundKeys: Map<CareAction, String>
) {
    fun validate(): List<String> = buildList {
        addAll(GameTemplateFactory.validate(template))
        if (visualKeys.keys.containsAll(template.primaryLoop).not()) add("visual.actions")
        if (soundKeys.keys.containsAll(template.primaryLoop).not()) add("sound.actions")
    }
}

object GameKitFactory {
    fun create(template: GameTemplate): GameKit {
        val character = CareCharacterRegistry.require(template.characterId)
        val state = CareGameFactory.createState(character)
        val visuals = template.primaryLoop.associateWith { action ->
            "${template.visualPack}_${template.characterId}_${action.key}"
        }
        val sounds = template.primaryLoop.associateWith { action ->
            "${template.soundPack}_${action.key}"
        }
        return GameKit(template, character, state, visuals, sounds)
    }

    /** Ready-to-use kit for the current game and reference implementation for clones. */
    val farm: GameKit by lazy { create(GameTemplateFactory.farm) }
}
