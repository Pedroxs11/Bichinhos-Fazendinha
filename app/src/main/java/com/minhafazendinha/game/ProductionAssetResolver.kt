package com.minhafazendinha.game

import android.content.Context
import android.graphics.drawable.Drawable

/** Centraliza a resolucao segura de assets para cenas reutilizaveis. */
object ProductionAssetResolver {
    fun drawable(context: Context, name: String?): Drawable? {
        if (name.isNullOrBlank()) return null
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id == 0) return null
        return runCatching { context.getDrawable(id) }.getOrNull()
    }

    fun available(context: Context, vararg names: String?): Boolean =
        names.filterNotNull().all { drawable(context, it) != null }
}
