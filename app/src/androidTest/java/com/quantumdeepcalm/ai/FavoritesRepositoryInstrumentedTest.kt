package com.quantumdeepcalm.ai

import android.content.Context
import androidx.core.content.edit
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FavoritesRepositoryInstrumentedTest {

    @Test
    fun favoritesPersistAcrossRepositoryInstances() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesName = "favorites-test-${System.nanoTime()}"
        val firstRepository = FavoritesRepository(context, preferencesName)

        try {
            assertTrue(firstRepository.loadFavoriteIds().isEmpty())

            val afterAdd = firstRepository.toggleFavorite("calm")
            assertTrue("calm" in afterAdd)

            val secondRepository = FavoritesRepository(context, preferencesName)
            assertEquals(setOf("calm"), secondRepository.loadFavoriteIds())

            val afterRemove = secondRepository.toggleFavorite("calm")
            assertFalse("calm" in afterRemove)
            assertTrue(FavoritesRepository(context, preferencesName).loadFavoriteIds().isEmpty())
        } finally {
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit(commit = true) {
                clear()
            }
        }
    }

    @Test
    fun invalidFavoriteIdsAreRejectedAndLegacyStorageIsSanitized() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesName = "favorites-invalid-test-${System.nanoTime()}"
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val repository = FavoritesRepository(context, preferencesName)

        try {
            preferences.edit(commit = true) {
                putStringSet("favorite_session_ids", setOf("calm", "unknown-session"))
            }

            assertEquals(setOf("calm"), repository.loadFavoriteIds())
            assertEquals(setOf("calm"), preferences.getStringSet("favorite_session_ids", emptySet()))

            try {
                repository.toggleFavorite("unknown-session")
                fail("Unknown catalog ids must not be persisted as favorites.")
            } catch (_: IllegalArgumentException) {
                // Expected: repository invariant rejects unknown catalog ids.
            }

            assertEquals(setOf("calm"), repository.loadFavoriteIds())
        } finally {
            preferences.edit(commit = true) {
                clear()
            }
        }
    }
}
