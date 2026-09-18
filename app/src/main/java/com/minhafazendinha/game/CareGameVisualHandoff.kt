package com.minhafazendinha.game

data class CareVisualExportSpec(
    val key: String, val role: String, val fileName: String,
    val transparent: Boolean, val widthPx: Int, val heightPx: Int,
    val notes: List<String>
)

data class CareGameVisualHandoff(
    val gameId: String,
    val exports: List<CareVisualExportSpec>,
    val qualityChecks: List<String>
) {
    fun validate(): List<String> = buildList {
        if (exports.isEmpty()) add("handoff.exports")
        if (exports.map { it.fileName }.distinct().size != exports.size) add("handoff.duplicate_files")
        if (exports.any { it.widthPx <= 0 || it.heightPx <= 0 }) add("handoff.invalid_dimensions")
        if (exports.any { !it.fileName.endsWith(".png") }) add("handoff.png_required")
    }
}

/**
 * Locks the exact export contract between final 3D/cartoon art and Android.
 * Artists/generated assets can be dropped into drawable without gameplay changes.
 */
object CareGameVisualHandoffFactory {
    private const val SCENE_WIDTH = 1080
    private const val SCENE_HEIGHT = 1440
    private const val CHARACTER_SIZE = 1024

    fun create(gameId: String): CareGameVisualHandoff {
        val manifest = CareGameArtManifestFactory.create(gameId)
        val exports = manifest.slots.map { slot ->
            val character = slot.transparent
            CareVisualExportSpec(
                key = slot.key,
                role = slot.role,
                fileName = slot.key + ".png",
                transparent = character,
                widthPx = if (character) CHARACTER_SIZE else SCENE_WIDTH,
                heightPx = if (character) CHARACTER_SIZE else SCENE_HEIGHT,
                notes = if (character) listOf(
                    "transparent_background",
                    "keep_character_anchor_centered",
                    "keep_scale_consistent_across_states",
                    "match_scene_light_direction",
                    "no_ui_or_text"
                ) else listOf(
                    "full_bleed_portrait_scene",
                    "leave_center_play_space_for_character",
                    "foreground_depth_without_covering_face",
                    "warm_soft_sunlight",
                    "no_ui_or_text"
                )
            )
        }
        val handoff = CareGameVisualHandoff(gameId, exports, manifest.qualityChecks)
        require(handoff.validate().isEmpty()) {
            "Invalid visual handoff for " + gameId + ": " + handoff.validate().joinToString()
        }
        return handoff
    }

    fun catalog(): Map<String, CareGameVisualHandoff> =
        CareGameArtManifestFactory.catalog().keys.associateWith(::create)
}
