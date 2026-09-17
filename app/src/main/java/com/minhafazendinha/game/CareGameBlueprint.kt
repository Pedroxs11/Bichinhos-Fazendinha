package com.minhafazendinha.game

/**
 * Small declarative builder used by the factory to create a new care game.
 *
 * Product-specific code only describes identity, actions and optional reaction
 * styling. Controller/session/runtime wiring remains shared by CareGameBootstrap.
 */
data class CareActionBlueprint(
    val id: String,
    val label: String,
    val visualState: String = id,
    val soundKey: String? = null,
    val reaction: CareReactionStyle? = null
) {
    init {
        require(id.isNotBlank()) { "Care action id cannot be blank" }
        require(label.isNotBlank()) { "Care action label cannot be blank" }
        require(visualState.isNotBlank()) { "Care visual state cannot be blank" }
    }
}

data class CareGameBlueprint(
    val id: String,
    val title: String,
    val characterId: String,
    val actions: List<CareActionBlueprint>,
    val fallbackReaction: CareReactionStyle = CareReactionStyle("✨")
) {
    init {
        require(id.isNotBlank()) { "Care game id cannot be blank" }
        require(title.isNotBlank()) { "Care game title cannot be blank" }
        require(characterId.isNotBlank()) { "Care character id cannot be blank" }
        require(actions.isNotEmpty()) { "Care game needs at least one action" }
        require(actions.map { it.id }.distinct().size == actions.size) {
            "Care action ids must be unique"
        }
    }

    /** Compiles the lightweight blueprint into the existing runtime template. */
    fun toTemplate(baseTheme: CareReactionTheme? = null): CareTemplate {
        val reactionStyles = buildMap {
            baseTheme?.styles?.let(::putAll)
            actions.forEach { action -> action.reaction?.let { put(action.id.lowercase(), it) } }
        }
        val theme = CareReactionTheme(
            styles = reactionStyles,
            fallback = baseTheme?.fallback ?: fallbackReaction
        )
        return CareTemplate(
            id = id,
            title = title,
            characterId = characterId,
            reactionTheme = theme,
            actions = actions.map { action ->
                CareTemplateAction(
                    id = action.id,
                    label = action.label,
                    visualState = action.visualState,
                    soundKey = action.soundKey
                )
            }
        )
    }

    companion object {
        /**
         * Fastest path for spin-off games: clone a proven template and replace
         * only the product identity/actions that differ.
         */
        fun fromTemplate(
            template: CareTemplate,
            id: String = template.id,
            title: String = template.title,
            characterId: String = template.characterId
        ): CareGameBlueprint = CareGameBlueprint(
            id = id,
            title = title,
            characterId = characterId,
            actions = template.actions.map { action ->
                CareActionBlueprint(
                    id = action.id,
                    label = action.label,
                    visualState = action.visualState,
                    soundKey = action.soundKey,
                    reaction = template.reactionTheme.styles[action.id.lowercase()]
                )
            },
            fallbackReaction = template.reactionTheme.fallback
        )
    }
}
