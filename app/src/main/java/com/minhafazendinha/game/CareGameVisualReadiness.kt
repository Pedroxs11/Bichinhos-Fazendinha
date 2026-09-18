package com.minhafazendinha.game

import android.content.Context

/**
 * Runtime visual readiness shared by every care game.
 * Bridges the production manifest with the real Android resources so QA can
 * immediately see which final exports are present, missing or still aliases.
 */
data class CareVisualSlotReadiness(
    val key: String,
    val resolvedKey: String?,
    val available: Boolean,
    val usingFallback: Boolean
)

data class CareVisualReadinessReport(
    val gameId: String,
    val slots: List<CareVisualSlotReadiness>
) {
    val readyCount: Int get() = slots.count { it.available }
    val finalCount: Int get() = slots.count { it.available && !it.usingFallback }
    val missingKeys: List<String> get() = slots.filterNot { it.available }.map { it.key }
    val fallbackKeys: Map<String, String> get() = slots
        .filter { it.usingFallback && it.resolvedKey != null }
        .associate { it.key to requireNotNull(it.resolvedKey) }
    val playable: Boolean get() = missingKeys.isEmpty()
    val productionReady: Boolean get() = playable && fallbackKeys.isEmpty()
    val finalProgress: Float get() = if (slots.isEmpty()) 1f else finalCount.toFloat() / slots.size
}

object CareGameVisualReadiness {
    fun inspect(context: Context, gameId: String): CareVisualReadinessReport {
        val manifest = CareGameArtManifestFactory.create(gameId)
        val slots = manifest.slots.map { slot ->
            val resolved = CareGameAssetResolver.resolve(context, gameId, slot.key)
            CareVisualSlotReadiness(
                key = slot.key,
                resolvedKey = resolved.resolvedKey,
                available = resolved.available,
                usingFallback = resolved.usingFallback
            )
        }
        return CareVisualReadinessReport(gameId, slots)
    }

    /** Compact CI/debug diagnostics without coupling UI to asset internals. */
    fun checklist(context: Context, gameId: String): List<String> {
        val report = inspect(context, gameId)
        return buildList {
            add("${report.finalCount}/${report.slots.size} final visual assets")
            report.slots.forEach { slot ->
                val state = when {
                    !slot.available -> "MISSING"
                    slot.usingFallback -> "FALLBACK:${slot.resolvedKey}"
                    else -> "FINAL"
                }
                add("$state ${slot.key}")
            }
        }
    }
}
