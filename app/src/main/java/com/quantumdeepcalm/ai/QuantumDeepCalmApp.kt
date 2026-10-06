package com.quantumdeepcalm.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
internal fun QuantumDeepCalmRoot() {
    QuantumDeepCalmTheme {
        QuantumDeepCalmApp()
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
                            modifier = Modifier.testTag("nav-${destination.route}"),
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
                CommercialPlayerScreen(
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
    var showCatalog by rememberSaveable { mutableStateOf(false) }

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
                text = "Que voulez-vous faire maintenant ? Choisissez une intention pour démarrer immédiatement, hors ligne.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        items(
            items = HomeIntentCatalog.intents,
            key = { intent -> intent.id },
        ) { intent ->
            HomeIntentCard(
                intent = intent,
                onStart = { onOpenSession(intent.sessionId) },
            )
        }

        item {
            OutlinedButton(
                onClick = { showCatalog = !showCatalog },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home-catalog-toggle"),
            ) {
                Text(if (showCatalog) "Masquer les ambiances" else "Voir toutes les ambiances")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenPrivacy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Confidentialité")
            }
        }

        if (showCatalog) {
            item {
                Text(
                    text = "Toutes les ambiances",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Choisissez directement une session ou ajoutez-la à vos favoris.",
                    style = MaterialTheme.typography.bodyMedium,
                )
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
}

@Composable
private fun HomeIntentCard(
    intent: HomeIntent,
    onStart: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = intent.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = intent.subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home-intent-${intent.id}"),
            ) {
                Text("Démarrer")
            }
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
