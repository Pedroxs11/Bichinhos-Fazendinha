package com.minhafazendinha.game

/**
 * Reusable production profile derived from a GameTemplate.
 * Centralizes naming/layout conventions so future games can share the same art pipeline.
 */
data class GameFactoryProfile(
    val gameId: String,
    val resourcePrefix: String,
    val drawableFolder: String,
    val audioFolder: String,
    val sceneAsset: String,
    val idleAsset: String,
    val actionVisuals: Map<CareAction, String>,
    val actionSounds: Map<CareAction, String>
) {
    val allVisuals: List<String>
        get() = listOf(sceneAsset, idleAsset) + actionVisuals.values

    val allSounds: List<String>
        get() = actionSounds.values.toList()

    fun assetPath(asset: String, audio: Boolean = false): String =
        "${if (audio) audioFolder else drawableFolder}/$asset"
}

object GameFactoryProfileBuilder {
    fun build(template: GameTemplate): GameFactoryProfile {
        val game = sanitize(template.id)
        val character = sanitize(template.characterId)
        val prefix = "${game}_${character}"

        return GameFactoryProfile(
            gameId = template.id,
            resourcePrefix = prefix,
            drawableFolder = "app/src/main/res/drawable",
            audioFolder = "app/src/main/res/raw",
            sceneAsset = "${game}_scene",
            idleAsset = "${prefix}_idle",
            actionVisuals = template.primaryLoop.associateWith { action ->
                "${prefix}_${action.key}"
            },
            actionSounds = template.primaryLoop.associateWith { action ->
                "${game}_sfx_${action.key}"
            }
        )
    }

    fun validate(profile: GameFactoryProfile): List<String> = buildList {
        if (profile.resourcePrefix.isBlank()) add("resource.prefix")
        if (profile.sceneAsset.isBlank()) add("scene.asset")
        if (profile.idleAsset.isBlank()) add("idle.asset")
        if (profile.actionVisuals.isEmpty()) add("action.visuals")
        if (profile.actionSounds.isEmpty()) add("action.sounds")
        val duplicateNames = (profile.allVisuals + profile.allSounds)
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        duplicateNames.forEach { add("duplicate.asset:$it") }
    }

    /** Stable artist/QA handoff lines that can later be exported by CI tooling. */
    fun handoff(template: GameTemplate): List<String> {
        val profile = build(template)
        return buildList {
            add("GAME ${template.title} (${template.id})")
            add("VISUAL ${profile.assetPath(profile.sceneAsset)}")
            add("VISUAL ${profile.assetPath(profile.idleAsset)}")
            profile.actionVisuals.forEach { (action, asset) ->
                add("VISUAL ${action.key} ${profile.assetPath(asset)}")
            }
            profile.actionSounds.forEach { (action, asset) ->
                add("AUDIO ${action.key} ${profile.assetPath(asset, audio = true)}")
            }
        }
    }

    fun buildAll(templates: Iterable<GameTemplate>): Map<String, GameFactoryProfile> =
        templates.associate { it.id to build(it) }

    private fun sanitize(value: String): String = value
        .trim()
        .lowercase()
        .map { if (it.isLetterOrDigit()) it else '_' }
        .joinToString("")
        .replace(Regex("_+"), "_")
        .trim('_')

    val farm: GameFactoryProfile by lazy { build(GameTemplateFactory.farm) }
}
