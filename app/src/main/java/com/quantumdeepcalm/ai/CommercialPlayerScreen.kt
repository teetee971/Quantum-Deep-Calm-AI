package com.quantumdeepcalm.ai

import android.content.ComponentName
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.quantumdeepcalm.ai.playback.CalmAudioGenerator
import com.quantumdeepcalm.ai.playback.ContinuousAudioEngine
import com.quantumdeepcalm.ai.playback.PlayerOutputControls
import com.quantumdeepcalm.ai.playback.PlaybackService
import com.quantumdeepcalm.ai.playback.formatPlaybackTime
import com.quantumdeepcalm.ai.playback.playbackFraction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
internal fun CommercialPlayerScreen(
    session: CalmSession,
    onBack: () -> Unit,
    onPlaybackStarted: () -> Unit,
) {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var playbackStartRecorded by remember(session.id) { mutableStateOf(false) }
    var statusMessage by remember(session.id) { mutableStateOf("Préparation de l’audio…") }
    var preparedMediaItem by remember(session.id) { mutableStateOf<MediaItem?>(null) }
    var audioPreparationFailed by remember(session.id) { mutableStateOf(false) }
    var preparationAttempt by remember(session.id) { mutableStateOf(0) }
    var currentPositionMs by remember(session.id) { mutableStateOf(0L) }
    var durationMs by remember(session.id) {
        mutableStateOf(ContinuousAudioEngine.DEFAULT_DURATION_SECONDS * 1_000L)
    }
    var isSeeking by remember { mutableStateOf(false) }
    var pendingSeekFraction by remember { mutableStateOf(0f) }
    var appVolume by rememberSaveable { mutableStateOf(1f) }
    var intensity by rememberSaveable { mutableStateOf(1f) }

    val outputGain = PlayerOutputControls(
        appVolume = appVolume,
        intensity = intensity,
    ).effectiveGain

    LaunchedEffect(context, session.id, preparationAttempt) {
        preparedMediaItem = null
        audioPreparationFailed = false
        controller = null
        isPlaying = false
        hasPlaybackError = false
        currentPositionMs = 0L
        durationMs = ContinuousAudioEngine.DEFAULT_DURATION_SECONDS * 1_000L
        statusMessage = "Préparation de l’audio…"

        try {
            preparedMediaItem = withContext(Dispatchers.IO) {
                val audioFile = CalmAudioGenerator.ensureGeneratedFile(
                    context = context.applicationContext,
                    sessionId = session.id,
                    profile = session.audioProfile,
                )
                MediaItem.Builder()
                    .setMediaId(session.id)
                    .setUri(Uri.fromFile(audioFile))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(session.title)
                            .setArtist("Quantum Deep Calm AI")
                            .build(),
                    )
                    .build()
            }
            statusMessage = "Connexion au lecteur…"
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            preparedMediaItem = null
            audioPreparationFailed = true
            statusMessage = "Impossible de préparer l’audio local"
        }
    }

    DisposableEffect(context, session.id, preparedMediaItem) {
        val mediaItem = preparedMediaItem
        if (mediaItem == null) {
            return@DisposableEffect onDispose { }
        }

        var attachedController: MediaController? = null
        var disposed = false
        val playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
                if (isPlayingNow) {
                    if (!playbackStartRecorded) {
                        playbackStartRecorded = true
                        onPlaybackStarted()
                    }
                    hasPlaybackError = false
                    statusMessage = "Lecture en cours • mode hors ligne"
                } else if (hasPlaybackError) {
                    statusMessage = "Erreur de lecture"
                } else {
                    statusMessage = "Prêt • lecture hors ligne"
                }
            }

            override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
                if (playbackSuppressionReason != Player.PLAYBACK_SUPPRESSION_REASON_NONE) {
                    statusMessage = "Lecture momentanément interrompue par Android"
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isPlaying = false
                hasPlaybackError = true
                statusMessage = "Erreur de lecture"
            }
        }

        val sessionListener = object : MediaController.Listener {
            override fun onDisconnected(disconnectedController: MediaController) {
                if (!disposed) {
                    attachedController?.removeListener(playerListener)
                    attachedController = null
                    controller = null
                    isPlaying = false
                    hasPlaybackError = false
                    statusMessage = "Lecteur déconnecté"
                }
            }
        }

        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java),
        )
        val controllerFuture = MediaController.Builder(context, sessionToken)
            .setListener(sessionListener)
            .buildAsync()

        controllerFuture.addListener(
            {
                runCatching { controllerFuture.get() }
                    .onSuccess { connectedController ->
                        if (disposed) {
                            return@onSuccess
                        }

                        attachedController = connectedController
                        connectedController.addListener(playerListener)

                        val canResumeCurrentItem =
                            connectedController.currentMediaItem?.mediaId == session.id &&
                                connectedController.mediaItemCount > 0

                        if (!canResumeCurrentItem) {
                            connectedController.pause()
                            connectedController.setMediaItem(mediaItem)
                            connectedController.prepare()
                        }

                        connectedController.volume = outputGain
                        controller = connectedController
                        isPlaying = connectedController.isPlaying
                        currentPositionMs = connectedController.currentPosition.coerceAtLeast(0L)
                        durationMs = connectedController.duration
                            .takeIf { it != C.TIME_UNSET && it > 0L }
                            ?: (ContinuousAudioEngine.DEFAULT_DURATION_SECONDS * 1_000L)
                        hasPlaybackError = false
                        statusMessage = if (connectedController.isPlaying) {
                            "Lecture en cours • mode hors ligne"
                        } else {
                            "Prêt • lecture hors ligne"
                        }
                    }
                    .onFailure {
                        if (disposed) {
                            return@onFailure
                        }
                        controller = null
                        isPlaying = false
                        hasPlaybackError = false
                        statusMessage = "Lecteur indisponible"
                    }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            disposed = true
            attachedController?.removeListener(playerListener)
            attachedController = null
            controller = null
            MediaController.releaseFuture(controllerFuture)
        }
    }

    LaunchedEffect(controller) {
        while (true) {
            val activeController = controller ?: break
            if (!isSeeking) {
                currentPositionMs = activeController.currentPosition.coerceAtLeast(0L)
            }
            durationMs = activeController.duration
                .takeIf { it != C.TIME_UNSET && it > 0L }
                ?: (ContinuousAudioEngine.DEFAULT_DURATION_SECONDS * 1_000L)
            delay(PROGRESS_REFRESH_MS)
        }
    }

    val displayFraction = if (isSeeking) {
        pendingSeekFraction
    } else {
        playbackFraction(currentPositionMs, durationMs)
    }
    val displayPositionMs = if (isSeeking) {
        (durationMs.toDouble() * pendingSeekFraction.toDouble()).toLong()
    } else {
        currentPositionMs
    }
    val remainingMs = (durationMs - displayPositionMs).coerceAtLeast(0L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Lecteur de méditation",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = session.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Ambiance générée localement. La lecture reste disponible écran verrouillé et le volume Android reste indépendant.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = statusMessage,
            modifier = Modifier.testTag("player-status"),
            style = MaterialTheme.typography.bodyMedium,
        )

        Slider(
            value = displayFraction,
            onValueChange = { fraction ->
                isSeeking = true
                pendingSeekFraction = fraction.coerceIn(0f, 1f)
            },
            onValueChangeFinished = {
                val targetPosition = (durationMs.toDouble() * pendingSeekFraction.toDouble()).toLong()
                controller?.seekTo(targetPosition)
                currentPositionMs = targetPosition
                isSeeking = false
            },
            enabled = controller != null && durationMs > 0L,
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player-progress"),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatPlaybackTime(displayPositionMs))
            Text("-${formatPlaybackTime(remainingMs)}")
        }

        Text(
            text = "Volume dans l’application ${(appVolume * 100f).toInt()} %",
            style = MaterialTheme.typography.bodyLarge,
        )
        Slider(
            value = appVolume,
            onValueChange = { value ->
                appVolume = value
                controller?.volume = PlayerOutputControls(
                    appVolume = value,
                    intensity = intensity,
                ).effectiveGain
            },
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player-app-volume"),
        )

        Text(
            text = "Intensité du mix ${(intensity * 100f).toInt()} %",
            style = MaterialTheme.typography.bodyLarge,
        )
        Slider(
            value = intensity,
            onValueChange = { value ->
                intensity = value
                controller?.volume = PlayerOutputControls(
                    appVolume = appVolume,
                    intensity = value,
                ).effectiveGain
            },
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player-intensity"),
        )
        Text(
            text = "Ces deux réglages agissent uniquement dans Quantum Deep Calm AI et ne modifient pas le volume système Android.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (audioPreparationFailed) {
            OutlinedButton(
                onClick = { preparationAttempt += 1 },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Réessayer la préparation")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = controller != null,
                onClick = {
                    controller?.let { activeController ->
                        if (activeController.isPlaying) {
                            activeController.pause()
                        } else {
                            if (hasPlaybackError) {
                                hasPlaybackError = false
                                statusMessage = "Nouvelle tentative…"
                                activeController.prepare()
                            }
                            if (activeController.playbackState == Player.STATE_ENDED) {
                                activeController.seekTo(0)
                            }
                            activeController.play()
                        }
                    }
                },
                modifier = Modifier.testTag("player-play-pause"),
            ) {
                Text(if (isPlaying) "Pause" else "Lecture")
            }

            OutlinedButton(
                enabled = controller != null,
                onClick = {
                    controller?.let { activeController ->
                        activeController.pause()
                        activeController.seekTo(0)
                        currentPositionMs = 0L
                    }
                },
            ) {
                Text("Recommencer")
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Button(onClick = onBack) {
            Text("Retour")
        }
    }
}

private const val PROGRESS_REFRESH_MS = 250L
