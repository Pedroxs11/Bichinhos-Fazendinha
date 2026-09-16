package com.minhafazendinha.game

/**
 * Preflight plan used by every game produced by the Fazendinha factory.
 * Keeps product configuration, care engine and art/sound readiness in one place.
 */
data class GameLaunchPlan(
    val kit: GameKit,
    val requiredVisuals: Set<String>,
    val requiredSounds: Set<String>,
    val issues: List<String>
) {
    val readyForPrototype: Boolean get() = issues.isEmpty()

    /** Stable checklist for artists/build tooling. */
    fun assetChecklist(): List<String> = buildList {
        requiredVisuals.sorted().forEach { add("visual:$it") }
        requiredSounds.sorted().forEach { add("sound:$it") }
    }
}

object GameLaunchPlanner {
    fun plan(template: GameTemplate): GameLaunchPlan {
        val kit = GameKitFactory.create(template)
        val visuals = buildSet {
            add(kit.character.visual.sceneKey)
            add(kit.character.visual.idleKey)
            addAll(kit.visualKeys.values)
        }.filter { it.isNotBlank() }.toSet()
        val sounds = kit.soundKeys.values.filter { it.isNotBlank() }.toSet()
        val issues = buildList {
            addAll(kit.validate())
            if (visuals.isEmpty()) add("assets.visual.empty")
            if (sounds.isEmpty()) add("assets.sound.empty")
        }.distinct()
        return GameLaunchPlan(kit, visuals, sounds, issues)
    }

    /** Reference plan: future games only need another GameTemplate. */
    val farm: GameLaunchPlan by lazy { plan(GameTemplateFactory.farm) }
}
