package com.quantumdeepcalm.ai

import android.content.Context
import androidx.core.content.edit

internal class FavoritesRepository(
    context: Context,
    preferencesName: String = DEFAULT_PREFERENCES_NAME,
) {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    fun loadFavoriteIds(): Set<String> {
        val stored = preferences.getStringSet(KEY_FAVORITE_IDS, emptySet())
            ?.toSet()
            .orEmpty()
        val valid = stored.intersect(SessionCatalog.ids)

        if (valid != stored) {
            preferences.edit {
                putStringSet(KEY_FAVORITE_IDS, valid)
            }
        }

        return valid
    }

    fun toggleFavorite(sessionId: String): Set<String> {
        require(sessionId in SessionCatalog.ids) { "Unknown session id." }

        val updated = loadFavoriteIds().toMutableSet()
        if (!updated.add(sessionId)) {
            updated.remove(sessionId)
        }

        preferences.edit {
            putStringSet(KEY_FAVORITE_IDS, updated)
        }

        return updated.toSet()
    }

    companion object {
        private const val DEFAULT_PREFERENCES_NAME = "quantum_deep_calm_library"
        private const val KEY_FAVORITE_IDS = "favorite_session_ids"
    }
}
