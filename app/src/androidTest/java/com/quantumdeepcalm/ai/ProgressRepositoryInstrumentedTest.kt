package com.quantumdeepcalm.ai

import android.content.Context
import androidx.core.content.edit
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressRepositoryInstrumentedTest {

    @Test
    fun playbackStartsPersistAcrossRepositoryInstances() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesName = "progress-test-${System.nanoTime()}"
        val firstRepository = ProgressRepository(context, preferencesName)

        try {
            val initial = firstRepository.load()
            assertEquals(0, initial.startedSessionCount)
            assertNull(initial.lastStartedAtMs)
            assertNull(initial.lastSessionId)

            val first = firstRepository.recordSessionStarted("calm", 1_000L)
            assertEquals(1, first.startedSessionCount)
            assertEquals(1_000L, first.lastStartedAtMs)
            assertEquals("calm", first.lastSessionId)

            val secondRepository = ProgressRepository(context, preferencesName)
            val restored = secondRepository.load()
            assertEquals(1, restored.startedSessionCount)
            assertEquals(1_000L, restored.lastStartedAtMs)
            assertEquals("calm", restored.lastSessionId)

            val second = secondRepository.recordSessionStarted("theta-meditation", 2_000L)
            assertEquals(2, second.startedSessionCount)
            assertEquals(2_000L, second.lastStartedAtMs)
            assertEquals("theta-meditation", second.lastSessionId)
        } finally {
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit(commit = true) {
                clear()
            }
        }
    }

    @Test
    fun loadRemovesInvalidStoredSessionIdWithoutDiscardingValidLegacyProgress() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesName = "progress-invalid-session-test-${System.nanoTime()}"
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val repository = ProgressRepository(context, preferencesName)

        try {
            preferences.edit(commit = true) {
                putInt("started_session_count", 2)
                putLong("last_started_at_ms", 5_000L)
                putString("last_session_id", "unknown-session")
            }

            val restored = repository.load()
            assertEquals(2, restored.startedSessionCount)
            assertEquals(5_000L, restored.lastStartedAtMs)
            assertNull(restored.lastSessionId)
            assertFalse(preferences.contains("last_session_id"))
        } finally {
            preferences.edit(commit = true) {
                clear()
            }
        }
    }

    @Test
    fun loadClearsStaleMetadataWhenStoredCountIsInvalid() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesName = "progress-invalid-count-test-${System.nanoTime()}"
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val repository = ProgressRepository(context, preferencesName)

        try {
            preferences.edit(commit = true) {
                putInt("started_session_count", -7)
                putLong("last_started_at_ms", 8_000L)
                putString("last_session_id", "calm")
            }

            val restored = repository.load()
            assertEquals(0, restored.startedSessionCount)
            assertNull(restored.lastStartedAtMs)
            assertNull(restored.lastSessionId)
            assertEquals(0, preferences.getInt("started_session_count", -1))
            assertFalse(preferences.contains("last_started_at_ms"))
            assertFalse(preferences.contains("last_session_id"))
        } finally {
            preferences.edit(commit = true) {
                clear()
            }
        }
    }

    @Test
    fun loadRemovesExplicitZeroTimestampAndDependentSessionMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesName = "progress-zero-timestamp-test-${System.nanoTime()}"
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val repository = ProgressRepository(context, preferencesName)

        try {
            preferences.edit(commit = true) {
                putInt("started_session_count", 3)
                putLong("last_started_at_ms", 0L)
                putString("last_session_id", "calm")
            }

            val restored = repository.load()
            assertEquals(3, restored.startedSessionCount)
            assertNull(restored.lastStartedAtMs)
            assertNull(restored.lastSessionId)
            assertFalse(preferences.contains("last_started_at_ms"))
            assertFalse(preferences.contains("last_session_id"))
        } finally {
            preferences.edit(commit = true) {
                clear()
            }
        }
    }
}
