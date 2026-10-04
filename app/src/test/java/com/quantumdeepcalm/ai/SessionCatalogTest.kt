package com.quantumdeepcalm.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SessionCatalogTest {

    @Test
    fun catalogIdsAreUniqueAndEveningSelectionIsPlayable() {
        assertEquals(SessionCatalog.sessions.size, SessionCatalog.ids.size)
        assertTrue(SessionCatalog.eveningSessions.isNotEmpty())
        assertTrue(SessionCatalog.eveningSessions.all { it.id in SessionCatalog.ids })
    }

    @Test
    fun audioProfileRejectsInvalidValues() {
        expectIllegalArgument {
            SessionAudioProfile(
                primaryHz = Double.NaN,
                secondaryHz = 100.0,
                accentHz = 200.0,
                envelopeDepth = 0.2,
            )
        }
        expectIllegalArgument {
            SessionAudioProfile(
                primaryHz = 100.0,
                secondaryHz = -1.0,
                accentHz = 200.0,
                envelopeDepth = 0.2,
            )
        }
        expectIllegalArgument {
            SessionAudioProfile(
                primaryHz = 100.0,
                secondaryHz = 150.0,
                accentHz = 200.0,
                envelopeDepth = 1.1,
            )
        }
    }

    @Test
    fun sessionRejectsInvalidIdentityAndBlankCopy() {
        val profile = SessionAudioProfile(100.0, 150.0, 200.0, 0.2)

        expectIllegalArgument {
            CalmSession(
                id = "Invalid Session",
                title = "Valid",
                subtitle = "Valid",
                audioProfile = profile,
                suitableForEvening = false,
            )
        }
        expectIllegalArgument {
            CalmSession(
                id = "valid-session",
                title = " ",
                subtitle = "Valid",
                audioProfile = profile,
                suitableForEvening = false,
            )
        }
    }

    private fun expectIllegalArgument(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException.")
        } catch (_: IllegalArgumentException) {
            // Expected invariant rejection.
        }
    }
}
