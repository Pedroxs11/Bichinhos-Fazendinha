package com.minhafazendinha.game

/**
 * Contrato visual reutilizavel. A referencia aprovada nunca e confundida com
 * uma camada de producao: ela serve de preview ate background/personagem/
 * foreground separados estarem prontos.
 */
data class VisualAssetSet(
    val sceneId: String,
    val referenceAsset: String,
    val backgroundAsset: String,
    val characterAsset: String,
    val foregroundAsset: String,
    val interactionAssets: Map<String, String>
)

object ProductionVisuals {
    val mimosa = VisualAssetSet(
        sceneId = "cow_care",
        referenceAsset = "mimosa_character_idle",
        backgroundAsset = "mimosa_farm_background",
        characterAsset = "mimosa_character_layer_idle",
        foregroundAsset = "mimosa_farm_foreground",
        interactionAssets = linkedMapOf(
            "feed" to "mimosa_character_layer_feed",
            "bath" to "mimosa_character_layer_bath",
            "brush" to "mimosa_character_layer_brush",
            "happy" to "mimosa_character_layer_happy"
        )
    )

    const val ART_DIRECTION = "3d_cartoon_farm_premium"
}
