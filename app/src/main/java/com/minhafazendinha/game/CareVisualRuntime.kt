package com.minhafazendinha.game

import android.content.Context

/** Runtime bridge between reusable care-game logic and production visual assets. */
data class ResolvedCareVisual(
    val state: CareVisualState,
    val slot: CareAssetSlot,
    val drawableResId: Int = 0,
    val animationResId: Int = 0,
    val modelAssetPath: String? = null
) {
    val hasDrawable: Boolean get() = drawableResId != 0
    val hasAnimation: Boolean get() = animationResId != 0
    val hasModel: Boolean get() = modelAssetPath != null
    val productionReady: Boolean get() = hasDrawable || hasAnimation || hasModel
}

class CareVisualRuntime(private val context: Context, pet: CarePetDefinition) {
    private val manifest = CareAssetResolver.manifest(pet)

    fun scene(): ResolvedCareVisual = resolve(CareVisualState.IDLE, manifest.scene)

    fun state(actionId: String?): ResolvedCareVisual {
        val state = CareAssetResolver.visualState(actionId)
        return resolve(state, manifest.state(state))
    }

    fun missingAssets(): List<String> = buildList {
        if (!scene().productionReady) add(manifest.scene.key)
        CareVisualState.entries.forEach { visualState ->
            val visual = resolve(visualState, manifest.state(visualState))
            if (!visual.productionReady) add(visual.slot.key)
        }
    }.distinct()

    private fun resolve(state: CareVisualState, slot: CareAssetSlot): ResolvedCareVisual {
        val drawable = slot.drawableName?.let { resourceId(it, "drawable") } ?: resourceId(slot.key, "drawable")
        val animation = slot.animationName?.let { resourceId(it, "raw") } ?: 0
        val model = slot.modelName?.let { "models/$it" }
        return ResolvedCareVisual(state, slot, drawable, animation, model)
    }

    private fun resourceId(name: String, type: String): Int {
        val safe = name.lowercase().replace(Regex("[^a-z0-9_]"), "_")
        return context.resources.getIdentifier(safe, type, context.packageName)
    }
}
