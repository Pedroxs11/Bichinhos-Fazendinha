package com.minhafazendinha.game

/**
 * Locked visual target for the Mimosa production pass.
 * Keeps the approved 3D-cartoon look measurable instead of drifting between art iterations.
 */
data class MimosaVisualTarget(
    val style: String,
    val composition: String,
    val lighting: String,
    val depth: String,
    val character: String,
    val requiredProps: List<String>,
    val requiredLayers: List<String>,
    val actionStates: Map<String, String>,
    val qualityChecks: List<String>
)

object MimosaVisualProduction {
    val target = MimosaVisualTarget(
        style = "polished_3d_cartoon_kids_game",
        composition = "mimosa_centered_large_with_clear_play_space",
        lighting = "warm_sunny_soft_global_light",
        depth = "2_5d_layered_depth_foreground_character_midground_background_parallax",
        character = "friendly_round_mimosa_big_glossy_eyes_pink_muzzle",
        requiredProps = listOf(
            "red_barn", "wood_fence", "hay", "flowers", "trees",
            "water_tub", "brush", "food", "play_ball"
        ),
        requiredLayers = listOf(
            "sky_farmland", "farm_midground", "mimosa", "foreground_props", "effects"
        ),
        actionStates = mapOf(
            "idle" to "friendly_breathing_idle",
            "feed" to "mimosa_eating",
            "bath" to "mimosa_wet_bubbles",
            "brush" to "mimosa_happy_brushed",
            "play" to "mimosa_playing_ball"
        ),
        qualityChecks = listOf(
            "same_mimosa_face_every_state",
            "same_character_anchor_and_scale",
            "no_ui_baked_into_scene_art",
            "soft_contact_shadow",
            "foreground_depth",
            "warm_consistent_light",
            "large_child_friendly_touch_targets",
            "reference_quality_or_better",\n            "2_5d_depth_visible_in_motion",\n            "3d_rendered_look_without_realtime_3d_models"
        )
    )

    /** Concrete drawable contract used by the next art export/import pass. */
    val productionRule = "FINAL_VISUALS_2_5D_ONLY_NO_FLAT_CANVAS_REPLACEMENTS"\n\n    val drawableContract: Map<String, String> = mapOf(
        "background" to "farm_care_scene",
        "idle" to "farm_care_idle",
        "feed" to "farm_care_feed",
        "bath" to "farm_care_bath",
        "brush" to "farm_care_brush",
        "play" to "farm_care_play"
    )
}
