package com.minhafazendinha.game

/**
 * Thin reusable controller that connects declarative templates to a game UI.
 * Screens only render callbacks; session/progress/sound/visual decisions stay here.
 */
data class CareGameEvent(
    val templateId: String,
    val actionId: String,
    val label: String,
    val visualState: String,
    val soundKey: String?,
    val actionCount: Int,
    val totalActions: Int,
    val completedActions: Int,
    val actionGoal: Int,
    val progress: Float,
    val roundCompleted: Boolean
)

class CareGameController(templateId: String) {
    val session = CareGameSession.fromTemplate(templateId)
    val template: CareTemplate get() = session.template

    fun perform(actionId: String): CareGameEvent {
        val result = session.perform(actionId)
        val goal = template.actions.size
        val completed = result.completedActionIds.size
        return CareGameEvent(
            templateId = template.id,
            actionId = result.action.id,
            label = result.action.label,
            visualState = result.action.visualState,
            soundKey = result.action.soundKey,
            actionCount = result.actionCount,
            totalActions = result.totalActions,
            completedActions = completed,
            actionGoal = goal,
            progress = if (goal == 0) 1f else completed.toFloat() / goal,
            roundCompleted = result.roundCompleted
        )
    }

    fun reset() = session.reset()

    fun availableActions(): List<CareTemplateAction> = template.actions
}
