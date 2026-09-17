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
        val event = controller.perform(actionId) ?: return false
        renderer.renderCareState(event.visualState)
        event.soundKey?.takeIf { it.isNotBlank() }?.let { audio?.play(it) }
        renderer.renderProgress(event.completedActions, event.totalActions, event.progress)
        if (event.completed) renderer.renderCompletion()
        return true
    }

    fun reset() {
        controller.reset()
        start()
    }

    private fun publishProgress() {
        val snapshot = controller.snapshot()
        renderer.renderProgress(snapshot.completedActions, snapshot.totalActions, snapshot.progress)
        if (snapshot.completed) renderer.renderCompletion()
    }
}
