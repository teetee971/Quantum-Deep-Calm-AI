package com.quantumdeepcalm.ai.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackTimerTest {

    @Test
    fun countdownPausesAndResumesWithoutLosingTime() {
        val timer = PlaybackTimer(durationMs = 60_000L, fadeDurationMs = 10_000L)

        timer.resume(nowElapsedMs = 1_000L)
        assertEquals(45_000L, timer.snapshot(nowElapsedMs = 16_000L).remainingMs)

        val paused = timer.pause(nowElapsedMs = 16_000L)
        assertEquals(45_000L, paused.remainingMs)
        assertFalse(paused.isRunning)

        assertEquals(45_000L, timer.snapshot(nowElapsedMs = 40_000L).remainingMs)

        timer.resume(nowElapsedMs = 40_000L)
        val resumed = timer.snapshot(nowElapsedMs = 45_000L)
        assertEquals(40_000L, resumed.remainingMs)
        assertTrue(resumed.isRunning)
    }

    @Test
    fun fadeGainFallsOnlyInsideFadeWindow() {
        val timer = PlaybackTimer(durationMs = 20_000L, fadeDurationMs = 5_000L)
        timer.resume(nowElapsedMs = 0L)

        assertEquals(1f, timer.snapshot(nowElapsedMs = 10_000L).outputGain, 0.0001f)
        assertEquals(0.5f, timer.snapshot(nowElapsedMs = 17_500L).outputGain, 0.0001f)
        assertEquals(0.1f, timer.snapshot(nowElapsedMs = 19_500L).outputGain, 0.0001f)
    }

    @Test
    fun timerRequestsStopAtZeroAndResetRestoresFullState() {
        val timer = PlaybackTimer(durationMs = 10_000L, fadeDurationMs = 2_000L)
        timer.resume(nowElapsedMs = 5_000L)

        val ended = timer.snapshot(nowElapsedMs = 15_000L)
        assertEquals(0L, ended.remainingMs)
        assertEquals(0f, ended.outputGain, 0.0001f)
        assertFalse(ended.isRunning)
        assertTrue(ended.shouldStop)

        val reset = timer.reset()
        assertEquals(10_000L, reset.remainingMs)
        assertEquals(1f, reset.outputGain, 0.0001f)
        assertFalse(reset.isRunning)
        assertFalse(reset.shouldStop)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fadeCannotExceedTimerDuration() {
        PlaybackTimer(durationMs = 1_000L, fadeDurationMs = 1_001L)
    }
}
