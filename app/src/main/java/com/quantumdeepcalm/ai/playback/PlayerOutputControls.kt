package com.quantumdeepcalm.ai.playback

internal data class PlayerOutputControls(
    val appVolume: Float,
    val intensity: Float,
) {
    init {
        require(appVolume.isFinite() && appVolume in 0f..1f) {
            "Application volume must be finite and between 0 and 1."
        }
        require(intensity.isFinite() && intensity in 0f..1f) {
            "Intensity must be finite and between 0 and 1."
        }
    }

    val effectiveGain: Float
        get() {
            val intensityGain = MIN_INTENSITY_GAIN +
                (MAX_INTENSITY_GAIN - MIN_INTENSITY_GAIN) * intensity
            return (appVolume * intensityGain).coerceIn(0f, 1f)
        }

    companion object {
        private const val MIN_INTENSITY_GAIN = 0.55f
        private const val MAX_INTENSITY_GAIN = 1f
    }
}

internal fun playbackFraction(positionMs: Long, durationMs: Long): Float {
    if (durationMs <= 0L) {
        return 0f
    }
    return (positionMs.toDouble() / durationMs.toDouble())
        .coerceIn(0.0, 1.0)
        .toFloat()
}

internal fun formatPlaybackTime(valueMs: Long): String {
    val totalSeconds = valueMs.coerceAtLeast(0L) / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
