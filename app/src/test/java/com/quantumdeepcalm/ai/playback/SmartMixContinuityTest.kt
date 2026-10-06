package com.quantumdeepcalm.ai.playback

import com.quantumdeepcalm.ai.SessionCatalog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartMixContinuityTest {

    @Test
    fun catalogMixesAvoidAbruptSampleDiscontinuities() {
        SessionCatalog.sessions.forEach { session ->
            val bytes = ContinuousAudioEngine.renderWavBytes(
                profile = session.audioProfile,
                durationSeconds = 4,
            )
            val pcm = ByteBuffer
                .wrap(bytes, 44, bytes.size - 44)
                .order(ByteOrder.LITTLE_ENDIAN)

            var previous = 0
            var maximumNormalizedDelta = 0.0
            var sawNonZeroSample = false

            while (pcm.remaining() >= 2) {
                val current = pcm.short.toInt()
                if (current != 0) {
                    sawNonZeroSample = true
                }
                val normalizedDelta = abs(current - previous).toDouble() / Short.MAX_VALUE.toDouble()
                maximumNormalizedDelta = maxOf(maximumNormalizedDelta, normalizedDelta)
                previous = current
            }

            assertTrue("${session.id} rendered only silence", sawNonZeroSample)
            assertTrue(
                "${session.id} contains an abrupt transition: $maximumNormalizedDelta",
                maximumNormalizedDelta < 0.15,
            )
        }
    }

    @Test
    fun smartMixUsesDifferentSlowModulationPeriodsAcrossLayers() {
        val mix = ContinuousAudioEngine.buildMix(SessionCatalog.defaultSession.audioProfile)
        val periods = mix.layers.map { it.modulationPeriodSeconds }

        assertTrue(periods.distinct().size == periods.size)
        assertTrue(periods.all { it >= 10.0 })
    }
}
