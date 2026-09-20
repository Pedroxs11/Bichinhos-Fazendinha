package com.minhafazendinha.game

/**
 * Stable naming contract between generated art and the reusable care-game factory.
 * New games can ship art without changing runtime code as long as these slots exist.
 */
data class CareAssetContract(
    val characterIdle: String,
    val characterHappy: String,
    val feedProp: String,
    val bathProp: String,
    val brushProp: String,
    val playProp: String,
    val feedEffect: String = "fx_hearts",
    val bathEffect: String = "fx_bubbles",
    val brushEffect: String = "fx_sparkles",
    val playEffect: String = "fx_confetti"
) {
    fun requiredAssets(): Set<String> = setOf(
        characterIdle, characterHappy, feedProp, bathProp, brushProp, playProp
    )

    fun optionalPolishAssets(): Set<String> = setOf(
        feedEffect, bathEffect, brushEffect, playEffect
    )

    /**
     * Deterministic drawable manifest used by art export/import tooling.
     * Keeping filenames here lets future games drop generated PNGs into the
     * same pipeline without changing screen code.
     */
    fun drawableManifest(): Map<String, String> = linkedMapOf(
        "character_idle" to characterIdle,
        "character_happy" to characterHappy,
        "action_feed" to feedProp,
        "action_bath" to bathProp,
        "action_brush" to brushProp,
        "action_play" to playProp,
        "effect_feed" to feedEffect,
        "effect_bath" to bathEffect,
        "effect_brush" to brushEffect,
        "effect_play" to playEffect
    )

    fun missingDrawables(available: Set<String>): Set<String> =
        drawableManifest().values.filterNot(available::contains).toSet()

    fun readyForVisualIntegration(available: Set<String>): Boolean =
        requiredAssets().all(available::contains)

    fun assetFor(actionId: String): String? = when (actionId.lowercase()) {
        "feed", "eat", "food", "comer" -> feedProp
        "bath", "bathe", "wash", "banho" -> bathProp
        "brush", "clean", "escovar" -> brushProp
        "play", "ball", "brincar" -> playProp
        else -> null
    }

    fun effectFor(actionId: String): String? = when (actionId.lowercase()) {
        "feed", "eat", "food", "comer" -> feedEffect
        "bath", "bathe", "wash", "banho" -> bathEffect
        "brush", "clean", "escovar" -> brushEffect
        "play", "ball", "brincar" -> playEffect
        else -> null
    }

    companion object {
        val MIMOSA = CareAssetContract(
            characterIdle = "mimosa_character_idle",
            characterHappy = "mimosa_character_happy",
            feedProp = "farm_prop_food",
            bathProp = "farm_prop_bath",
            brushProp = "farm_prop_brush",
            playProp = "farm_prop_play_ball"
        )
    }
}
