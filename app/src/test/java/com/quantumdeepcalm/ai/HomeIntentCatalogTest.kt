package com.quantumdeepcalm.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeIntentCatalogTest {

    @Test
    fun homeExposesExactlyTheFourProductIntentions() {
        assertEquals(
            listOf("Calme", "Sommeil", "Focus", "Respirer"),
            HomeIntentCatalog.intents.map { it.title },
        )
    }

    @Test
    fun everyIntentTargetsARealDistinctCatalogSession() {
        val sessionIds = HomeIntentCatalog.intents.map { it.sessionId }

        assertEquals(HomeIntentCatalog.intents.size, sessionIds.toSet().size)
        assertTrue(sessionIds.all { it in SessionCatalog.ids })
    }

    @Test
    fun breatheIntentDoesNotClaimGuidedBreathingYet() {
        val breathe = HomeIntentCatalog.intents.single { it.id == "breathe" }

        assertTrue(breathe.subtitle.contains("Respiration libre"))
        assertTrue(breathe.subtitle.contains("guidage chronométré arrivera plus tard"))
    }
}
