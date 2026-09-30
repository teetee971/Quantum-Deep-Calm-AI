package com.quantumdeepcalm.ai

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
        }
    }
}
