package com.minhafazendinha.game

/**
 * Runtime reutilizavel que liga controller, cena visual e audio sem acoplar a tela
 * a um jogo especifico. Novos jogos podem implementar CareGameRenderer e trocar
 * apenas template/assets.
 */
interface CareGameRenderer {
    fun renderCareState(state: CareVisualState)
    fun renderProgress(completed: Int, total: Int, progress: Float)
    fun renderCompletion()
}

interface CareGameAudio {
    fun play(soundKey: String)
}

class CareGameRuntime(
    private val controller: CareGameController,
    private val renderer: CareGameRenderer,
    private val audio: CareGameAudio? = null
) {
    fun start() {
        renderer.renderCareState(CareVisualState.IDLE)
        publishProgress()
    }

    fun perform(actionId: String): Boolean {
        val event = runCatching { controller.perform(actionId) }.getOrNull() ?: return false
        renderer.renderCareState(event.visualState.toCareVisualState())
        event.soundKey?.takeIf { it.isNotBlank() }?.let { audio?.play(it) }
        renderer.renderProgress(event.completedActions, event.actionGoal, event.progress)
        if (event.roundCompleted) renderer.renderCompletion()
        return true
    }

    fun reset() {
        controller.reset()
        start()
    }

    private fun publishProgress() {
        val completed = controller.session.completedActionIds.size
        val total = controller.template.actions.size
        val progress = if (total == 0) 1f else completed.toFloat() / total
        renderer.renderProgress(completed, total, progress)
        if (controller.session.roundCompleted) renderer.renderCompletion()
    }

    private fun String.toCareVisualState(): CareVisualState = when (lowercase()) {
        "feed" -> CareVisualState.FEED
        "bath", "wash" -> CareVisualState.BATHE
        "brush", "hair", "polish" -> CareVisualState.BRUSH
        "play", "dress", "makeup", "repair", "customize" -> CareVisualState.PLAY
        else -> CareVisualState.IDLE
    }
}
