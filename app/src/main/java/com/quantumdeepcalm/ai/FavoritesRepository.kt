package com.quantumdeepcalm.ai

import android.content.Context

internal class FavoritesRepository(
    context: Context,
    preferencesName: String = DEFAULT_PREFERENCES_NAME,
) {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    fun loadFavoriteIds(): Set<String> {
        return preferences.getStringSet(KEY_FAVORITE_IDS, emptySet())
            ?.toSet()
            .orEmpty()
    }

    fun toggleFavorite(sessionId: String): Set<String> {
        val updated = loadFavoriteIds().toMutableSet()
        if (!updated.add(sessionId)) {
            updated.remove(sessionId)
        }

        preferences.edit()
            .putStringSet(KEY_FAVORITE_IDS, updated)
            .apply()

        return updated.toSet()
    }

    companion object {
        private const val DEFAULT_PREFERENCES_NAME = "quantum_deep_calm_library"
        private const val KEY_FAVORITE_IDS = "favorite_session_ids"
    }
}
