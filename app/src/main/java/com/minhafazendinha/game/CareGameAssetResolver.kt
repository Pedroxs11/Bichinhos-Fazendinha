package com.minhafazendinha.game

import android.content.Context
import android.graphics.drawable.Drawable

/**
 * Runtime resolver shared by every factory-made care game.
 * Production names always win; aliases keep approved preview art live while
 * final exports are replaced slot-by-slot.
 */
data class CareResolvedAsset(
    val requestedKey: String,
    val resolvedKey: String?,
    val drawable: Drawable?
) {
    val available: Boolean get() = drawable != null
    val usingFallback: Boolean get() = resolvedKey != null && resolvedKey != requestedKey
}

object CareGameAssetResolver {
    fun resolve(context: Context, gameId: String, key: String?): CareResolvedAsset {
        if (key.isNullOrBlank()) return CareResolvedAsset(key.orEmpty(), null, null)
        CareGameAssetAliasCatalog.candidates(gameId, key).forEach { candidate ->
            val drawable = ProductionAssetResolver.drawable(context, candidate)
            if (drawable != null) return CareResolvedAsset(key, candidate, drawable)
        }
        return CareResolvedAsset(key, null, null)
    }

    fun drawable(context: Context, gameId: String, key: String?): Drawable? =
        resolve(context, gameId, key).drawable

    fun available(context: Context, gameId: String, vararg keys: String?): Boolean =
        keys.filterNotNull().all { resolve(context, gameId, it).available }

    /** Useful for debug/QA: shows which slots still depend on preview aliases. */
    fun fallbackKeys(context: Context, gameId: String, keys: Iterable<String>): Map<String, String> =
        keys.mapNotNull { key ->
            resolve(context, gameId, key).takeIf { it.usingFallback }?.resolvedKey?.let { key to it }
        }.toMap()
}
