package com.quantumdeepcalm.ai.playback

internal data class PlaybackTimerSnapshot(
    val remainingMs: Long,
    val outputGain: Float,
    val isRunning: Boolean,
    val shouldStop: Boolean,
)

internal class PlaybackTimer(
    private val durationMs: Long,
    private val fadeDurationMs: Long,
) {
    init {
        require(durationMs > 0L) { "Timer duration must be positive." }
        require(fadeDurationMs in 1L..durationMs) { "Fade duration must fit inside the timer duration." }
    }

    private var remainingAtResumeMs: Long = durationMs
    private var resumedAtElapsedMs: Long? = null

    fun resume(nowElapsedMs: Long): PlaybackTimerSnapshot {
        require(nowElapsedMs >= 0L) { "Elapsed realtime must not be negative." }
        if (remainingAtResumeMs <= 0L) {
            return snapshot(nowElapsedMs)
        }
        if (resumedAtElapsedMs == null) {
            resumedAtElapsedMs = nowElapsedMs
        }
        return snapshot(nowElapsedMs)
    }

    fun pause(nowElapsedMs: Long): PlaybackTimerSnapshot {
        require(nowElapsedMs >= 0L) { "Elapsed realtime must not be negative." }
        remainingAtResumeMs = remainingAt(nowElapsedMs)
        resumedAtElapsedMs = null
        return snapshot(nowElapsedMs)
    }

    fun reset(): PlaybackTimerSnapshot {
        remainingAtResumeMs = durationMs
        resumedAtElapsedMs = null
        return snapshot(nowElapsedMs = 0L)
    }

    fun snapshot(nowElapsedMs: Long): PlaybackTimerSnapshot {
        require(nowElapsedMs >= 0L) { "Elapsed realtime must not be negative." }
        val remainingMs = remainingAt(nowElapsedMs)
        val running = resumedAtElapsedMs != null && remainingMs > 0L
        val gain = when {
            remainingMs <= 0L -> 0f
            remainingMs >= fadeDurationMs -> 1f
            else -> (remainingMs.toDouble() / fadeDurationMs.toDouble())
                .coerceIn(0.0, 1.0)
                .toFloat()
        }
        return PlaybackTimerSnapshot(
            remainingMs = remainingMs,
            outputGain = gain,
            isRunning = running,
            shouldStop = remainingMs <= 0L,
        )
    }

    private fun remainingAt(nowElapsedMs: Long): Long {
        val resumedAt = resumedAtElapsedMs ?: return remainingAtResumeMs.coerceAtLeast(0L)
        val elapsedSinceResume = (nowElapsedMs - resumedAt).coerceAtLeast(0L)
        return (remainingAtResumeMs - elapsedSinceResume).coerceAtLeast(0L)
    }
}
