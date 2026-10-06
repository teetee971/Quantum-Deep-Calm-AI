package com.quantumdeepcalm.ai.playback

import com.quantumdeepcalm.ai.SessionCatalog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuousAudioEngineTest {

    private val defaultProfile = SessionCatalog.defaultSession.audioProfile
    private val testDurationSeconds = 4

    @Test
    fun renderedWavHasExpectedContainerAndSize() {
        val bytes = ContinuousAudioEngine.renderWavBytes(
            profile = defaultProfile,
            durationSeconds = testDurationSeconds,
        )

        assertEquals(
            ContinuousAudioEngine.expectedByteSize(testDurationSeconds),
            bytes.size,
        )
        assertEquals("RIFF", bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals("WAVE", bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        assertEquals("fmt ", bytes.copyOfRange(12, 16).toString(Charsets.US_ASCII))
        assertEquals("data", bytes.copyOfRange(36, 40).toString(Charsets.US_ASCII))
    }

    @Test
    fun productionRenderIsContinuousRatherThanAnEightSecondLoopArtifact() {
        assertTrue(ContinuousAudioEngine.DEFAULT_DURATION_SECONDS >= 180)
        assertTrue(
            ContinuousAudioEngine.expectedByteSize() >
                ContinuousAudioEngine.expectedByteSize(8),
        )
    }

    @Test
    fun mixContainsIndependentBoundedLayers() {
        val mix = ContinuousAudioEngine.buildMix(defaultProfile)

        assertTrue(mix.layers.size >= 4)
        assertTrue(mix.layers.all { it.level in 0.0..1.0 })
        assertTrue(mix.layers.all { it.frequencyHz > 0.0 })
        assertTrue(mix.layers.sumOf { it.level } <= 1.0)
        assertTrue(mix.masterGain in 0.0..0.9)
    }

    @Test
    fun changingOneLayerLevelChangesRenderedAudio() {
        val originalMix = ContinuousAudioEngine.buildMix(defaultProfile)
        val mutedPrimaryMix = originalMix.copy(
            layers = originalMix.layers.mapIndexed { index, layer ->
                if (index == 0) layer.copy(level = 0.0) else layer
            },
        )

        val original = ContinuousAudioEngine.renderWavBytes(
            mix = originalMix,
            durationSeconds = 2,
        )
        val mutedPrimary = ContinuousAudioEngine.renderWavBytes(
            mix = mutedPrimaryMix,
            durationSeconds = 2,
        )

        assertFalse(original.contentEquals(mutedPrimary))
    }

    @Test
    fun renderIsDeterministicForTheSameInput() {
        val first = ContinuousAudioEngine.renderWavBytes(
            profile = defaultProfile,
            durationSeconds = 2,
        )
        val second = ContinuousAudioEngine.renderWavBytes(
            profile = defaultProfile,
            durationSeconds = 2,
        )

        assertTrue(first.contentEquals(second))
    }

    @Test
    fun everyCatalogSessionKeepsHealthyPeakAndClippingHeadroom() {
        SessionCatalog.sessions.forEach { session ->
            val bytes = ContinuousAudioEngine.renderWavBytes(
                profile = session.audioProfile,
                durationSeconds = testDurationSeconds,
            )
            val pcm = ByteBuffer
                .wrap(bytes, 44, bytes.size - 44)
                .order(ByteOrder.LITTLE_ENDIAN)
            var peak = 0

            while (pcm.remaining() >= 2) {
                peak = maxOf(peak, abs(pcm.short.toInt()))
            }

            val normalizedPeak = peak.toDouble() / Short.MAX_VALUE.toDouble()
            assertTrue(
                "${session.id} output peak is too low: $normalizedPeak",
                normalizedPeak >= 0.25,
            )
            assertTrue(
                "${session.id} output clips or leaves insufficient headroom: $normalizedPeak",
                normalizedPeak < 0.90,
            )
        }
    }

    @Test
    fun catalogSessionsRemainDistinct() {
        val rendered = SessionCatalog.sessions.associate { session ->
            session.id to ContinuousAudioEngine.renderWavBytes(
                profile = session.audioProfile,
                durationSeconds = 2,
            )
        }

        for (left in SessionCatalog.sessions.indices) {
            for (right in (left + 1) until SessionCatalog.sessions.size) {
                val leftSession = SessionCatalog.sessions[left]
                val rightSession = SessionCatalog.sessions[right]
                assertNotEquals(leftSession.id, rightSession.id)
                assertFalse(
                    rendered.getValue(leftSession.id)
                        .contentEquals(rendered.getValue(rightSession.id)),
                )
            }
        }
    }
}
