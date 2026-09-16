package com.minhafazendinha.game

/**
 * Reusable product template for quickly spinning up the next kid-friendly care game.
 * Keeps gameplay tuning and visual identity declarative instead of duplicating Activities.
 */
data class GameTemplate(
    val id: String,
    val title: String,
    val characterId: String,
    val primaryLoop: List<CareAction>,
    val startingCoins: Int = 120,
    val level: Int = 1,
    val visualPack: String = id,
    val soundPack: String = characterId
)

enum class CareAction(val key: String) {
    FEED("feed"), BATHE("bathe"), BRUSH("brush"), PLAY("play")
}

object GameTemplateFactory {
    val farm = GameTemplate(
        id = "minha_fazendinha",
        title = "Minha Fazendinha",
        characterId = "mimosa",
        primaryLoop = listOf(CareAction.FEED, CareAction.BATHE, CareAction.BRUSH, CareAction.PLAY),
        visualPack = "farm_soft3d",
        soundPack = "farm_animals"
    )

    /** Clone this template and change IDs/assets to bootstrap another game. */
    fun create(
        id: String,
        title: String,
        characterId: String,
        visualPack: String = id,
        soundPack: String = characterId,
        actions: List<CareAction> = CareAction.entries
    ) = GameTemplate(
        id = id,
        title = title,
        characterId = characterId,
        primaryLoop = actions,
        visualPack = visualPack,
        soundPack = soundPack
    )

    fun validate(template: GameTemplate): List<String> = buildList {
        if (template.id.isBlank()) add("game.id")
        if (template.title.isBlank()) add("game.title")
        if (template.characterId.isBlank()) add("character.id")
        if (template.primaryLoop.isEmpty()) add("care.actions")
        if (template.visualPack.isBlank()) add("visual.pack")
        if (template.soundPack.isBlank()) add("sound.pack")
    }
}
