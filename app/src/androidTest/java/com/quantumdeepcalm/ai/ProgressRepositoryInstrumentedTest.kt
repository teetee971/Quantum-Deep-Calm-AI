package com.quantumdeepcalm.ai

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
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

            val first = firstRepository.recordSessionStarted(1_000L)
            assertEquals(1, first.startedSessionCount)
            assertEquals(1_000L, first.lastStartedAtMs)

            val secondRepository = ProgressRepository(context, preferencesName)
            val restored = secondRepository.load()
            assertEquals(1, restored.startedSessionCount)
            assertEquals(1_000L, restored.lastStartedAtMs)

            val second = secondRepository.recordSessionStarted(2_000L)
            assertEquals(2, second.startedSessionCount)
            assertEquals(2_000L, second.lastStartedAtMs)
        } finally {
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
        }
    }
}
