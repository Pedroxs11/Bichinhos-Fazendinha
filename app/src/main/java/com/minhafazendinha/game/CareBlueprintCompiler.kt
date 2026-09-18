package com.minhafazendinha.game

/** A validated factory product that can feed runtime, tooling and visual production. */
data class CareBlueprintProduct(
    val blueprint: CareGameBlueprint,
    val template: CareTemplate,
    val preflight: CareBlueprintPreflight,
    val launchPlan: CareGameLaunchPlan
)

/** Machine-readable handoff from game design to audio/visual production. */
data class CareGameLaunchPlan(
    val gameId: String,
    val actionIds: List<String>,
    val visualStates: List<String>,
    val soundKeys: List<String>,
    val missingSoundActionIds: List<String>,
    val polishWarnings: List<String>
) {
    val actionCount: Int get() = actionIds.size
    val soundCoverage: Float
        get() = if (actionCount == 0) 1f else (actionCount - missingSoundActionIds.size).toFloat() / actionCount
    val isPolishReady: Boolean get() = polishWarnings.isEmpty()
}

/**
 * Shared compiler for current and future care games.
 * Structural mistakes fail before runtime; non-blocking gaps become a production
 * plan so cloned games do not need their own setup/checklist code.
 */
object CareBlueprintCompiler {
    fun compile(
        blueprint: CareGameBlueprint,
        baseTheme: CareReactionTheme? = null
    ): CareBlueprintProduct {
        val preflight = CareGameBlueprintValidator.validate(blueprint)
        require(preflight.canBuild) {
            preflight.summary() + ": " + preflight.errors.joinToString { it.message }
        }
        return CareBlueprintProduct(
            blueprint = blueprint,
            template = blueprint.toTemplate(baseTheme),
            preflight = preflight,
            launchPlan = launchPlan(blueprint, preflight)
        )
    }

    /** Fast path for spin-offs: clone a proven title and pass it through the same gate. */
    fun cloneAndCompile(
        source: CareTemplate,
        id: String,
        title: String,
        characterId: String,
        transform: (CareGameBlueprint) -> CareGameBlueprint = { it }
    ): CareBlueprintProduct {
        val clone = CareGameBlueprint.fromTemplate(source, id, title, characterId)
        return compile(transform(clone), source.reactionTheme)
    }

    /** Current catalog and future games share exactly the same validation/reporting path. */
    fun compileTemplate(template: CareTemplate): CareBlueprintProduct =
        compile(CareGameBlueprint.fromTemplate(template), template.reactionTheme)

    fun compileCatalog(): List<CareBlueprintProduct> =
        CareTemplateCatalog.all().map(::compileTemplate)

    private fun launchPlan(
        blueprint: CareGameBlueprint,
        preflight: CareBlueprintPreflight
    ) = CareGameLaunchPlan(
        gameId = blueprint.id,
        actionIds = blueprint.actions.map { it.id },
        visualStates = blueprint.actions.map { it.visualState }.distinct(),
        soundKeys = blueprint.actions.mapNotNull { it.soundKey }.distinct(),
        missingSoundActionIds = blueprint.actions.filter { it.soundKey == null }.map { it.id },
        polishWarnings = preflight.warnings.map { it.message }
    )
}
