package com.quantumdeepcalm.ai

import android.content.Context
import androidx.core.content.edit

internal data class PlaybackProgress(
    val startedSessionCount: Int,
    val lastStartedAtMs: Long?,
    val lastSessionId: String?,
)

internal class ProgressRepository(
    context: Context,
    preferencesName: String = DEFAULT_PREFERENCES_NAME,
) {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    fun load(): PlaybackProgress {
        val hasStoredTimestamp = preferences.contains(KEY_LAST_STARTED_AT_MS)
        val rawCount = preferences.getInt(KEY_STARTED_SESSION_COUNT, 0)
        val rawLastStartedAtMs = preferences.getLong(KEY_LAST_STARTED_AT_MS, 0L)
        val rawLastSessionId = preferences.getString(KEY_LAST_SESSION_ID, null)

        val count = rawCount.coerceAtLeast(0)
        val lastStartedAtMs = rawLastStartedAtMs.takeIf { count > 0 && it > 0L }
        val lastSessionId = rawLastSessionId
            ?.takeIf { lastStartedAtMs != null && it in SessionCatalog.ids }

        val needsRepair =
            rawCount != count ||
                hasStoredTimestamp != (lastStartedAtMs != null) ||
                (lastStartedAtMs != null && rawLastStartedAtMs != lastStartedAtMs) ||
                rawLastSessionId != lastSessionId

        if (needsRepair) {
            preferences.edit {
                putInt(KEY_STARTED_SESSION_COUNT, count)
                if (lastStartedAtMs == null) {
                    remove(KEY_LAST_STARTED_AT_MS)
                } else {
                    putLong(KEY_LAST_STARTED_AT_MS, lastStartedAtMs)
                }
                if (lastSessionId == null) {
                    remove(KEY_LAST_SESSION_ID)
                } else {
                    putString(KEY_LAST_SESSION_ID, lastSessionId)
                }
            }
        }

        return PlaybackProgress(count, lastStartedAtMs, lastSessionId)
    }

    fun recordSessionStarted(sessionId: String, atMs: Long): PlaybackProgress {
        require(sessionId in SessionCatalog.ids) { "Unknown session id." }
        require(atMs > 0L) { "Session start timestamp must be positive." }

        val current = load()
        val nextCount = if (current.startedSessionCount == Int.MAX_VALUE) {
            Int.MAX_VALUE
        } else {
            current.startedSessionCount + 1
        }

        preferences.edit {
            putInt(KEY_STARTED_SESSION_COUNT, nextCount)
            putLong(KEY_LAST_STARTED_AT_MS, atMs)
            putString(KEY_LAST_SESSION_ID, sessionId)
        }

        return PlaybackProgress(nextCount, atMs, sessionId)
    }

    companion object {
        private const val DEFAULT_PREFERENCES_NAME = "quantum_deep_calm_progress"
        private const val KEY_STARTED_SESSION_COUNT = "started_session_count"
        private const val KEY_LAST_STARTED_AT_MS = "last_started_at_ms"
        private const val KEY_LAST_SESSION_ID = "last_session_id"
    }
}
