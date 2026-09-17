package com.minhafazendinha.game

/**
 * Declarative catalog for rapidly bootstrapping care games.
 * A new title can select a template and replace only its assets/text instead of
 * rebuilding interaction, reaction and visual-state wiring.
 */
data class CareTemplateAction(
    val id: String,
    val label: String,
    val visualState: String,
    val soundKey: String? = null
)

data class CareTemplate(
    val id: String,
    val title: String,
    val characterId: String,
    val reactionTheme: CareReactionTheme,
    val actions: List<CareTemplateAction>
) {
    init {
        require(id.isNotBlank())
        require(characterId.isNotBlank())
        require(actions.isNotEmpty())
        require(actions.map { it.id }.distinct().size == actions.size) { "Care action ids must be unique" }
    }

    fun action(id: String): CareTemplateAction? = actions.firstOrNull { it.id == id }
}

object CareTemplateCatalog {
    val farm = CareTemplate(
        id = "farm_care",
        title = "Cuide da Mimosa",
        characterId = "mimosa",
        reactionTheme = CareReactionThemes.farm,
        actions = listOf(
            CareTemplateAction("feed", "Alimentar", "feed", "vaca"),
            CareTemplateAction("bath", "Banho", "bath", "vaca"),
            CareTemplateAction("brush", "Escovar", "brush", "vaca"),
            CareTemplateAction("play", "Brincar", "play", "vaca")
        )
    )

    val princess = CareTemplate(
        id = "princess_care",
        title = "Meu Salão de Princesa",
        characterId = "princess",
        reactionTheme = CareReactionThemes.princess,
        actions = listOf(
            CareTemplateAction("dress", "Vestir", "dress"),
            CareTemplateAction("makeup", "Maquiar", "makeup"),
            CareTemplateAction("hair", "Pentear", "hair"),
            CareTemplateAction("play", "Desfilar", "play")
        )
    )

    val carCare = CareTemplate(
        id = "car_care",
        title = "Minha Garagem",
        characterId = "car",
        reactionTheme = CareReactionThemes.carCare,
        actions = listOf(
            CareTemplateAction("wash", "Lavar", "wash"),
            CareTemplateAction("polish", "Polir", "polish"),
            CareTemplateAction("repair", "Consertar", "repair"),
            CareTemplateAction("customize", "Personalizar", "customize")
        )
    )

    private val templates = listOf(farm, princess, carCare).associateBy { it.id }

    fun require(id: String): CareTemplate =
        requireNotNull(templates[id]) { "Unknown care template: $id" }

    fun all(): List<CareTemplate> = templates.values.toList()
}
