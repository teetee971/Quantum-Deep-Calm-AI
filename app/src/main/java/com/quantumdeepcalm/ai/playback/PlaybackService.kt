package com.quantumdeepcalm.ai.playback

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
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
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
