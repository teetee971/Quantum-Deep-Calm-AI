package com.quantumdeepcalm.ai

import android.content.Context
import androidx.core.content.edit

internal data class PlaybackProgress(
    val startedSessionCount: Int,
    val lastStartedAtMs: Long?,
)

internal class ProgressRepository(
    context: Context,
    preferencesName: String = DEFAULT_PREFERENCES_NAME,
) {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    fun load(): PlaybackProgress {
        val count = preferences.getInt(KEY_STARTED_SESSION_COUNT, 0).coerceAtLeast(0)
        val lastStartedAtMs = preferences.getLong(KEY_LAST_STARTED_AT_MS, 0L).takeIf { it > 0L }
        return PlaybackProgress(count, lastStartedAtMs)
    }

    fun recordSessionStarted(atMs: Long): PlaybackProgress {
        require(atMs > 0L) { "Session start timestamp must be positive." }
        val current = load()
        val nextCount = if (current.startedSessionCount == Int.MAX_VALUE) Int.MAX_VALUE else current.startedSessionCount + 1

        preferences.edit {
            putInt(KEY_STARTED_SESSION_COUNT, nextCount)
            putLong(KEY_LAST_STARTED_AT_MS, atMs)
        }

        return PlaybackProgress(nextCount, atMs)
    }

    companion object {
        private const val DEFAULT_PREFERENCES_NAME = "quantum_deep_calm_progress"
        private const val KEY_STARTED_SESSION_COUNT = "started_session_count"
        private const val KEY_LAST_STARTED_AT_MS = "last_started_at_ms"
    }
}
