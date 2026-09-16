package com.minhafazendinha.game

/**
 * Contrato da arte de producao do Cantinho da Mimosa.
 * Mantem a UI desacoplada dos assets para podermos trocar renders sem
 * reescrever a logica do jogo e reutilizar o mesmo pipeline nos proximos jogos.
 */
data class VisualAssetSet(
    val sceneId: String,
    val backgroundAsset: String,
    val characterAsset: String,
    val foregroundAsset: String,
    val interactionAssets: Map<String, String>
)

object ProductionVisuals {
    val mimosa = VisualAssetSet(
        sceneId = "cow_care",
        backgroundAsset = "mimosa_farm_background",
        characterAsset = "mimosa_character_idle",
        foregroundAsset = "mimosa_farm_foreground",
        interactionAssets = linkedMapOf(
            "feed" to "mimosa_character_feed",
            "bath" to "mimosa_character_bath",
            "brush" to "mimosa_character_brush",
            "happy" to "mimosa_character_happy"
        )
    )

    // Regra de qualidade aprovada para o piloto:
    // 3D cartoon infantil, volumes suaves, luz quente, profundidade de campo,
    // madeira/vegetacao com textura e personagem expressivo em primeiro plano.
    const val ART_DIRECTION = "3d_cartoon_farm_premium"
}
