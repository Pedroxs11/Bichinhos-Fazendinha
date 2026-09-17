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
    val template: CareGameTemplate,
    val controller: CareGameController,
    val runtime: CareGameRuntime
) {
    fun start() = runtime.start()
    fun perform(actionId: String) = runtime.perform(actionId)
    fun reset() = runtime.reset()
}

object CareGameBootstrap {
    fun create(templateId: String, bindings: CareGameBindings): CareGameInstance {
        val template = requireNotNull(CareTemplateCatalog.byId(templateId)) {
            "Unknown care-game template: $templateId"
        }
        return create(template, bindings)
    }

    fun create(template: CareGameTemplate, bindings: CareGameBindings): CareGameInstance {
        val controller = CareGameController(template)
        val runtime = CareGameRuntime(controller, bindings.renderer, bindings.audio)
        return CareGameInstance(template, controller, runtime)
    }

    /** Useful for launchers/catalog screens and future game packs. */
    fun availableTemplates(): List<CareGameTemplate> = CareTemplateCatalog.all()
}
