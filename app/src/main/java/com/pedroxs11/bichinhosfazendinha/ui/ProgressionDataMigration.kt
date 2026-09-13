package com.pedroxs11.bichinhosfazendinha.ui

import android.content.Context

private const val MIGRATION_PREFS = "wardrobe_prefs"
private const val KEY_MIGRATION_DONE = "progression_v2_migration_done"
private const val OLD_SELECTED_ANIMAL = "selected_wardrobe_animal"
private const val NEW_SELECTED_ANIMAL = "progression_wardrobe_animal"
private const val OLD_ROYAL_AT = "royal_unlocked_at"
private const val NEW_ROYAL_AT = "progression_royal_unlocked_at"
private const val LEGACY_COW_OUTFIT = "selected_outfit"

private val wardrobeIdMigration = mapOf(
    "cow" to "cow",
    "pig" to "pig",
    "dog" to "dog",
    "chicken" to "chick"
)

fun migrateLegacyProgressionData(context: Context) {
    val prefs = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
    if (prefs.getBoolean(KEY_MIGRATION_DONE, false)) return

    val editor = prefs.edit()

    // Estrelas não precisam ser copiadas: V1 e V2 usam game_progress/stars.
    if (!prefs.contains(NEW_SELECTED_ANIMAL)) {
        val oldSelected = prefs.getString(OLD_SELECTED_ANIMAL, null)
        val mappedSelected = oldSelected?.let { wardrobeIdMigration[it] }
        if (mappedSelected != null) {
            editor.putString(NEW_SELECTED_ANIMAL, mappedSelected)
        }
    }

    if (!prefs.contains(NEW_ROYAL_AT) && prefs.contains(OLD_ROYAL_AT)) {
        editor.putLong(NEW_ROYAL_AT, prefs.getLong(OLD_ROYAL_AT, 0L))
    }

    wardrobeIdMigration.forEach { (oldId, newId) ->
        val newKey = "progression_outfit_$newId"
        if (!prefs.contains(newKey)) {
            val oldKey = "selected_outfit_$oldId"
            val legacyCow = if (oldId == "cow") prefs.getString(LEGACY_COW_OUTFIT, null) else null
            val oldOutfit = prefs.getString(oldKey, legacyCow)
            if (!oldOutfit.isNullOrBlank()) {
                editor.putString(newKey, oldOutfit)
            }
        }
    }

    editor.putBoolean(KEY_MIGRATION_DONE, true).apply()
}
