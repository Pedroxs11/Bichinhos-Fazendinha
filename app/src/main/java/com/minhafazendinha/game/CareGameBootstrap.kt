package com.minhafazendinha.game

/**
 * Composition root for the reusable care-game factory.
 * A new care game can now be wired by template id + renderer/audio bindings,
 * without rebuilding controller/session/runtime setup in every Activity.
 */
data class CareGameBindings(
    val renderer: CareGameRenderer,
    val audio: CareGameAudio? = null
)

data class CareGameInstance(
    val template: CareTemplate,
    val controller: CareGameController,
    val runtime: CareGameRuntime
) {
    fun start() = runtime.start()
    fun perform(actionId: String) = runtime.perform(actionId)
    fun reset() = runtime.reset()
}

object CareGameBootstrap {
    fun create(templateId: String, bindings: CareGameBindings): CareGameInstance {
        val template = CareTemplateCatalog.require(templateId)
        val controller = CareGameController(template.id)
        val runtime = CareGameRuntime(controller, bindings.renderer, bindings.audio)
        return CareGameInstance(template, controller, runtime)
    }

    /** Useful for launchers/catalog screens and future game packs. */
    fun availableTemplates(): List<CareTemplate> = CareTemplateCatalog.all()
}
