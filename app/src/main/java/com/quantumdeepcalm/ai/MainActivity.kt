package com.quantumdeepcalm.ai

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quantumdeepcalm.ai.playback.CalmAudioGenerator
import com.quantumdeepcalm.ai.playback.PlaybackService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuantumDeepCalmTheme {
                QuantumDeepCalmApp()
            }
        }
    }
}

@Composable
private fun QuantumDeepCalmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF35E7F2),
            onPrimary = Color(0xFF06101B),
            secondary = Color(0xFF9C6CFF),
            background = Color(0xFF0A1026),
            surface = Color(0xFF111A3A),
            onBackground = Color(0xFFF7F7FF),
            onSurface = Color(0xFFF7F7FF),
        ),
        content = content,
    )
}

@Composable
private fun QuantumDeepCalmApp() {
    val context = LocalContext.current
    val favoritesRepository = remember(context) {
        FavoritesRepository(context.applicationContext)
    }
    var favoriteIds by remember {
        mutableStateOf(
            favoritesRepository.loadFavoriteIds().intersect(SessionCatalog.ids),
        )
    }

    val progressRepository = remember(context) {
        ProgressRepository(context.applicationContext)
    }
    var playbackProgress by remember {
        mutableStateOf(progressRepository.load())
    }

    val navController = rememberNavController()
    var selectedSessionId by rememberSaveable {
        mutableStateOf(SessionCatalog.defaultSession.id)
    }

    val toggleFavorite: (String) -> Unit = { sessionId ->
        if (sessionId in SessionCatalog.ids) {
            favoriteIds = favoritesRepository
                .toggleFavorite(sessionId)
                .intersect(SessionCatalog.ids)
        }
    }

    val openSession: (String) -> Unit = { sessionId ->
        if (sessionId in SessionCatalog.ids) {
            selectedSessionId = sessionId
            navController.navigate(AppNavigationContract.PLAYER) {
                launchSingleTop = true
            }
        }
    }

    val recordPlaybackStarted: (String) -> Unit = { sessionId ->
        playbackProgress = progressRepository.recordSessionStarted(
            sessionId = sessionId,
            atMs = System.currentTimeMillis(),
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in AppNavigationContract.topLevelDestinations.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    AppNavigationContract.topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(text = "•") },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppNavigationContract.START_ROUTE,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppNavigationContract.HOME) {
                HomeScreen(
                    favoriteIds = favoriteIds,
                    onToggleFavorite = toggleFavorite,
                    onOpenSession = openSession,
                    onOpenPrivacy = { navController.navigate(AppNavigationContract.PRIVACY) },
                )
            }
            composable(AppNavigationContract.LIBRARY) {
                LibraryScreen(
                    favoriteIds = favoriteIds,
                    onToggleFavorite = toggleFavorite,
                    onOpenSession = openSession,
                )
            }
            composable(AppNavigationContract.SLEEP) {
                SleepScreen(
                    favoriteIds = favoriteIds,
                    onToggleFavorite = toggleFavorite,
                    onOpenSession = openSession,
                )
            }
            composable(AppNavigationContract.PROGRESS) {
                ProgressScreen(progress = playbackProgress)
            }
            composable(AppNavigationContract.PLAYER) {
                val session = SessionCatalog.find(selectedSessionId) ?: SessionCatalog.defaultSession
                PlayerScreen(
                    session = session,
                    onBack = { navController.popBackStack() },
                    onPlaybackStarted = { recordPlaybackStarted(session.id) },
                )
            }
            composable(AppNavigationContract.PRIVACY) {
                PrivacyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun HomeScreen(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpenSession: (String) -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "Quantum Deep Calm AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cinq ambiances générées sur votre appareil. Aucun compte ni connexion réseau requis.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { onOpenSession(SessionCatalog.defaultSession.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Commencer avec Calm")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenPrivacy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Confidentialité")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = SessionCatalog.sessions,
            key = { session -> session.id },
        ) { session ->
            CalmSessionCard(
                session = session,
                isFavorite = session.id in favoriteIds,
                onOpenSession = { onOpenSession(session.id) },
                onToggleFavorite = { onToggleFavorite(session.id) },
            )
        }
    }
}

@Composable
private fun LibraryScreen(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpenSession: (String) -> Unit,
) {
    val favoriteSessions = SessionCatalog.sessions.filter { session ->
        session.id in favoriteIds
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "Bibliothèque",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vos favoris restent sur cet appareil. Les ambiances sont générées localement : aucun téléchargement n’est nécessaire.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (favoriteSessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                ) {
                    Text(
                        text = "Aucun favori pour le moment. Ajoutez une session depuis l’accueil.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        } else {
            items(
                items = favoriteSessions,
                key = { session -> session.id },
            ) { session ->
                CalmSessionCard(
                    session = session,
                    isFavorite = true,
                    onOpenSession = { onOpenSession(session.id) },
                    onToggleFavorite = { onToggleFavorite(session.id) },
                )
            }
        }
    }
}

@Composable
private fun SleepScreen(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpenSession: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "Sommeil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sélection d’ambiances calmes pour la soirée. Aucun effet sur le sommeil n’est garanti.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        items(
            items = SessionCatalog.eveningSessions,
            key = { session -> session.id },
        ) { session ->
            CalmSessionCard(
                session = session,
                isFavorite = session.id in favoriteIds,
                onOpenSession = { onOpenSession(session.id) },
                onToggleFavorite = { onToggleFavorite(session.id) },
            )
        }
    }
}

@Composable
private fun CalmSessionCard(
    session: CalmSession,
    isFavorite: Boolean,
    onOpenSession: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = session.subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onOpenSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("play-session-${session.id}"),
            ) {
                Text("Écouter")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onToggleFavorite,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris")
            }
        }
    }
}

@Composable
private fun ProgressScreen(progress: PlaybackProgress) {
    val lastStartedLabel = progress.lastStartedAtMs?.let { timestamp ->
        java.text.DateFormat
            .getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
            .format(java.util.Date(timestamp))
    }
    val lastSessionTitle = progress.lastSessionId
        ?.let(SessionCatalog::find)
        ?.title
    val lastPlaybackLabel = when {
        lastStartedLabel == null -> "Aucune lecture confirmée pour le moment."
        lastSessionTitle == null -> "Dernière lecture confirmée avant le suivi par session • $lastStartedLabel"
        else -> "Dernière lecture confirmée : $lastSessionTitle • $lastStartedLabel"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Progression",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Sessions audio démarrées : ${progress.startedSessionCount}",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = lastPlaybackLabel,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Le compteur augmente uniquement lorsque Media3 confirme que l’audio a réellement commencé.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PrivacyScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Politique de confidentialité",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        item { Text("Quantum Deep Calm AI fonctionne hors ligne et ne nécessite pas de compte utilisateur.") }
        item { Text("L’application ne transmet pas vos favoris, votre historique de lecture ou vos préférences hors de votre appareil.") }
        item { Text("Les favoris et la progression sont stockés localement. Les sauvegardes applicatives Android sont désactivées.") }
        item { Text("Vous pouvez supprimer ces données en effaçant le stockage de l’application ou en la désinstallant.") }
        item { Text("Aucun contenu de l’application ne constitue un conseil médical, un diagnostic ou un traitement.") }
        item {
            Button(onClick = onBack) {
                Text("Retour")
            }
        }
    }
}

@Composable
private fun PlayerScreen(
    session: CalmSession,
    onBack: () -> Unit,
    onPlaybackStarted: () -> Unit,
) {
    val context = LocalContext.current
    val mediaItem = remember(context, session.id) {
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

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var playbackStartRecorded by remember(session.id) { mutableStateOf(false) }
    var statusMessage by remember(session.id) { mutableStateOf("Connexion au lecteur…") }

    DisposableEffect(context, session.id) {
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
                        connectedController.pause()
                        connectedController.setMediaItem(mediaItem)
                        connectedController.prepare()
                        controller = connectedController
                        isPlaying = false
                        hasPlaybackError = false
                        statusMessage = "Prêt • lecture hors ligne"
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
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
            text = "Ambiance générée localement. Aucune connexion réseau n’est nécessaire.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium,
        )
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
                            activeController.play()
                        }
                    }
                },
            ) {
                Text(if (isPlaying) "Pause" else "Lecture")
            }

            OutlinedButton(
                enabled = controller != null,
                onClick = {
                    controller?.let { activeController ->
                        activeController.pause()
                        activeController.seekTo(0)
                    }
                },
            ) {
                Text("Recommencer")
            }
        }

        Button(onClick = onBack) {
            Text("Retour")
        }
    }
}
