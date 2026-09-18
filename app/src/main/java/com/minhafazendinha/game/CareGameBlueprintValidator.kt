package com.minhafazendinha.game

/** Severity used by the reusable factory preflight. */
enum class CareBlueprintIssueSeverity { WARNING, ERROR }

/** A machine-readable preflight issue that tooling can surface before a new game is wired. */
data class CareBlueprintIssue(
    val severity: CareBlueprintIssueSeverity,
    val code: String,
    val message: String,
    val actionId: String? = null
)

/** Compact factory result suitable for dashboards and release tooling. */
data class CareBlueprintPreflight(
    val gameId: String,
    val issues: List<CareBlueprintIssue>
) {
    val errors: List<CareBlueprintIssue> get() = issues.filter { it.severity == CareBlueprintIssueSeverity.ERROR }
    val warnings: List<CareBlueprintIssue> get() = issues.filter { it.severity == CareBlueprintIssueSeverity.WARNING }
    val canBuild: Boolean get() = errors.isEmpty()
    val isPolishReady: Boolean get() = canBuild && warnings.isEmpty()

    fun summary(): String = when {
        !canBuild -> "$gameId: ${errors.size} error(s), ${warnings.size} warning(s)"
        warnings.isNotEmpty() -> "$gameId: build-ready, ${warnings.size} polish warning(s)"
        else -> "$gameId: factory-ready"
    }
}

/**
 * Shared quality gate for every care-game blueprint.
 *
 * CareGameBlueprint already protects the minimum runtime invariants. This validator
 * catches production mistakes that are legal Kotlin but expensive to discover after
 * cloning a title: unstable identifiers, repeated visual states, incomplete sound
 * mapping and suspicious labels. Keeping these rules here means future games inherit
 * the same preflight without product-specific validation code.
 */
object CareGameBlueprintValidator {
    private val stableId = Regex("^[a-z][a-z0-9_]*$")

    fun validate(blueprint: CareGameBlueprint): CareBlueprintPreflight {
        val issues = mutableListOf<CareBlueprintIssue>()

        if (!stableId.matches(blueprint.id)) {
            issues += error(
                code = "GAME_ID_FORMAT",
                message = "Game id '${blueprint.id}' should use lowercase snake_case for stable asset keys."
            )
        }
        if (!stableId.matches(blueprint.characterId)) {
            issues += error(
                code = "CHARACTER_ID_FORMAT",
                message = "Character id '${blueprint.characterId}' should use lowercase snake_case."
            )
        }

        blueprint.actions.forEach { action ->
            if (!stableId.matches(action.id)) {
                issues += error(
                    code = "ACTION_ID_FORMAT",
                    message = "Action id '${action.id}' should use lowercase snake_case.",
                    actionId = action.id
                )
            }
            if (action.label != action.label.trim()) {
                issues += warning(
                    code = "ACTION_LABEL_WHITESPACE",
                    message = "Action '${action.id}' has leading or trailing whitespace in its label.",
                    actionId = action.id
                )
            }
            if (action.soundKey == null) {
                issues += warning(
                    code = "ACTION_SOUND_MISSING",
                    message = "Action '${action.id}' has no sound key yet.",
                    actionId = action.id
                )
            } else if (!stableId.matches(action.soundKey)) {
                issues += warning(
                    code = "SOUND_KEY_FORMAT",
                    message = "Sound key '${action.soundKey}' should use lowercase snake_case.",
                    actionId = action.id
                )
            }
        }

        blueprint.actions
            .groupBy { it.visualState }
            .filterValues { it.size > 1 }
            .forEach { (state, actions) ->
                issues += warning(
                    code = "VISUAL_STATE_SHARED",
                    message = "Visual state '$state' is shared by actions ${actions.joinToString { it.id }}."
                )
            }

        blueprint.actions
            .groupBy { it.label.trim().lowercase() }
            .filterValues { it.size > 1 }
            .forEach { (label, actions) ->
                issues += warning(
                    code = "ACTION_LABEL_DUPLICATE",
                    message = "Label '$label' is repeated by actions ${actions.joinToString { it.id }}."
                )
            }

        return CareBlueprintPreflight(blueprint.id, issues)
    }

    fun validateAll(blueprints: Iterable<CareGameBlueprint>): List<CareBlueprintPreflight> =
        blueprints.map(::validate)

    fun requireBuildable(blueprint: CareGameBlueprint): CareGameBlueprint {
        val result = validate(blueprint)
        require(result.canBuild) { result.summary() + ": " + result.errors.joinToString { it.message } }
        return blueprint
    }

    private fun error(code: String, message: String, actionId: String? = null) =
        CareBlueprintIssue(CareBlueprintIssueSeverity.ERROR, code, message, actionId)

    private fun warning(code: String, message: String, actionId: String? = null) =
        CareBlueprintIssue(CareBlueprintIssueSeverity.WARNING, code, message, actionId)
}
