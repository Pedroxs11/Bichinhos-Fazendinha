package com.minhafazendinha.game

/**
 * Central asset contract for every care-game character.
 * Game logic asks for semantic keys; renderers decide whether the key resolves to
 * generated PNG/WebP art, animation, a 3D model, or a fallback view.
 */
enum class CareVisualState { IDLE, FEED, BATHE, BRUSH, PLAY }

data class CareAssetSlot(
    val key: String,
    val drawableName: String? = null,
    val animationName: String? = null,
    val modelName: String? = null
) {
    val productionReady: Boolean get() = drawableName != null || animationName != null || modelName != null
}

data class CareAssetManifest(
    val petId: String,
    val scene: CareAssetSlot,
    val states: Map<CareVisualState, CareAssetSlot>
) {
    fun state(state: CareVisualState): CareAssetSlot =
        states[state] ?: states.getValue(CareVisualState.IDLE)

    val missingProductionKeys: List<String> get() =
        (listOf(scene) + states.values).filterNot { it.productionReady }.map { it.key }.distinct()
}

object CareAssetResolver {
    fun manifest(pet: CarePetDefinition): CareAssetManifest {
        val v = pet.visual
        fun slot(key: String) = CareAssetSlot(key = key)
        return CareAssetManifest(
            petId = pet.id,
            scene = slot(v.sceneKey),
            states = mapOf(
                CareVisualState.IDLE to slot(v.idleAssetKey),
                CareVisualState.FEED to slot(v.assetFor("feed")),
                CareVisualState.BATHE to slot(v.assetFor("bathe")),
                CareVisualState.BRUSH to slot(v.assetFor("brush")),
                CareVisualState.PLAY to slot(v.assetFor("play"))
            )
        )
    }

    fun visualState(actionId: String?): CareVisualState = when (actionId) {
        "feed" -> CareVisualState.FEED
        "bathe" -> CareVisualState.BATHE
        "brush" -> CareVisualState.BRUSH
        "play" -> CareVisualState.PLAY
        else -> CareVisualState.IDLE
    }

    /** Useful to drive an asset-generation checklist without inspecting UI code. */
    fun productionChecklist(): Map<String, List<String>> =
        CareGameFactory.allPets().associate { pet -> pet.id to manifest(pet).missingProductionKeys }
}
