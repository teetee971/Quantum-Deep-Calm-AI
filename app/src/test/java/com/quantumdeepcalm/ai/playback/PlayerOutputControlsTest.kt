package com.quantumdeepcalm.ai.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerOutputControlsTest {

    @Test
    fun outputGainCombinesApplicationVolumeAndIntensityWithoutExceedingUnity() {
        assertEquals(
            1f,
            PlayerOutputControls(appVolume = 1f, intensity = 1f).effectiveGain,
            0.0001f,
        )
        assertEquals(
            0.55f,
            PlayerOutputControls(appVolume = 1f, intensity = 0f).effectiveGain,
            0.0001f,
        )
        assertEquals(
            0.3875f,
            PlayerOutputControls(appVolume = 0.5f, intensity = 0.5f).effectiveGain,
            0.0001f,
        )
    }

    @Test
    fun playbackFractionIsClampedAndRejectsUnknownDurationAsZero() {
        assertEquals(0f, playbackFraction(positionMs = 5_000L, durationMs = 0L), 0.0001f)
        assertEquals(0f, playbackFraction(positionMs = -1_000L, durationMs = 10_000L), 0.0001f)
        assertEquals(0.5f, playbackFraction(positionMs = 5_000L, durationMs = 10_000L), 0.0001f)
        assertEquals(1f, playbackFraction(positionMs = 15_000L, durationMs = 10_000L), 0.0001f)
    }

    @Test
    fun playbackTimeFormattingIsStableAndNeverNegative() {
        assertEquals("0:00", formatPlaybackTime(-1L))
        assertEquals("0:09", formatPlaybackTime(9_999L))
        assertEquals("1:05", formatPlaybackTime(65_000L))
        assertEquals("60:00", formatPlaybackTime(3_600_000L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun applicationVolumeOutsideRangeFailsClosed() {
        PlayerOutputControls(appVolume = 1.01f, intensity = 1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun intensityOutsideRangeFailsClosed() {
        PlayerOutputControls(appVolume = 1f, intensity = -0.01f)
    }
}
