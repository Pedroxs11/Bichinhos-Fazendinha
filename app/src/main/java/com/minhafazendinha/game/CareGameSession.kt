package com.minhafazendinha.game

/** Runtime engine shared by every care-game template. */
data class CareSessionResult(
    val action: CareTemplateAction,
    val actionCount: Int,
    val totalActions: Int,
    val completedActionIds: Set<String>,
    val roundCompleted: Boolean
)

class CareGameSession(val template: CareTemplate) {
    private val counts = linkedMapOf<String, Int>()

    val totalActions: Int get() = counts.values.sum()
    val completedActionIds: Set<String>
        get() = template.actions.mapNotNull { action ->
            if ((counts[action.id] ?: 0) > 0) action.id else null
        }.toSet()
    val roundCompleted: Boolean get() = completedActionIds.size == template.actions.size

    fun perform(actionId: String): CareSessionResult {
        val action = requireNotNull(template.action(actionId)) {
            "Unknown action '$actionId' for template '${template.id}'"
        }
        val count = (counts[action.id] ?: 0) + 1
        counts[action.id] = count
        return CareSessionResult(action, count, totalActions, completedActionIds, roundCompleted)
    }

    fun count(actionId: String): Int = counts[actionId] ?: 0

    fun reset() = counts.clear()

    companion object {
        fun fromTemplate(templateId: String) = CareGameSession(CareTemplateCatalog.require(templateId))
    }
}
