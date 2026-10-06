package com.quantumdeepcalm.ai.playback

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var playbackTimer: PlaybackTimer? = null
    private val timerHandler = Handler(Looper.getMainLooper())

    private val timerTick = object : Runnable {
        override fun run() {
            val session = mediaSession ?: return
            val player = session.player
            if (!player.isPlaying) {
                return
            }

            val timer = playbackTimer ?: return
            val snapshot = timer.snapshot(SystemClock.elapsedRealtime())
            player.volume = snapshot.outputGain

            if (snapshot.shouldStop) {
                timerHandler.removeCallbacks(this)
                player.volume = 0f
                player.pause()
                player.seekTo(0)
                timer.reset()
                player.volume = 1f
                return
            }

            timerHandler.postDelayed(this, TIMER_TICK_MS)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val timer = playbackTimer ?: return
            val now = SystemClock.elapsedRealtime()

            timerHandler.removeCallbacks(timerTick)
            if (isPlaying) {
                val snapshot = timer.resume(now)
                mediaSession?.player?.volume = snapshot.outputGain
                timerHandler.post(timerTick)
            } else {
                timer.pause(now)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            resetTimerAndGain()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                mediaSession?.player?.seekTo(0)
                resetTimerAndGain()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        playbackTimer = PlaybackTimer(
            durationMs = ContinuousAudioEngine.DEFAULT_DURATION_SECONDS * 1_000L,
            fadeDurationMs = FADE_OUT_DURATION_MS,
        )

        val player = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            addListener(playerListener)
        }

        mediaSession = MediaSession.Builder(this, player).build()
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo,
    ): MediaSession? {
        val isOwnApp = controllerInfo.packageName == packageName
        return if (isOwnApp || controllerInfo.isTrusted) {
            mediaSession
        } else {
            null
        }
    }

    override fun onDestroy() {
        timerHandler.removeCallbacksAndMessages(null)
        mediaSession?.run {
            player.removeListener(playerListener)
            player.release()
            release()
        }
        mediaSession = null
        playbackTimer = null
        super.onDestroy()
    }

    private fun resetTimerAndGain() {
        timerHandler.removeCallbacks(timerTick)
        playbackTimer?.reset()
        mediaSession?.player?.volume = 1f
    }

    private companion object {
        const val FADE_OUT_DURATION_MS = 10_000L
        const val TIMER_TICK_MS = 250L
    }
}
