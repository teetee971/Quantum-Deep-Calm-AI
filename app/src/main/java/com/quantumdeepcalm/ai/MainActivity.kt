package com.quantumdeepcalm.ai

import android.content.ComponentName
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
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

private data class CalmSection(
    val title: String,
    val subtitle: String,
)

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
    val navController = rememberNavController()
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
                HomeScreen(onOpenPlayer = { navController.navigate(AppNavigationContract.PLAYER) })
            }
            composable(AppNavigationContract.LIBRARY) {
                SimpleSectionScreen(
                    title = "Bibliothèque",
                    body = "Module en préparation : les téléchargements, favoris et contenus hors connexion ne sont pas encore activés dans ce build.",
                )
            }
            composable(AppNavigationContract.SLEEP) {
                SimpleSectionScreen(
                    title = "Sommeil",
                    body = "Module en préparation : les routines du soir, histoires et programmes d’endormissement ne sont pas encore activés dans ce build.",
                )
            }
            composable(AppNavigationContract.PROGRESS) {
                SimpleSectionScreen(
                    title = "Progression",
                    body = "Module en préparation : aucun historique, objectif ou suivi de régularité n’est encore enregistré dans ce build.",
                )
            }
            composable(AppNavigationContract.PLAYER) {
                PlayerScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun HomeScreen(onOpenPlayer: () -> Unit) {
    val sections = listOf(
        CalmSection("Calm", "Ambiance locale pour ralentir et retrouver un rythme plus calme"),
        CalmSection("Alpha Relaxation", "Session de relaxation sonore, sans promesse d’effet neurologique"),
        CalmSection("Theta Meditation", "Méditation immersive inspirée de l’identité Quantum Deep Calm"),
        CalmSection("Delta Concentration", "Session audio conçue pour une écoute posée et sans distraction"),
        CalmSection("Schumann", "Ambiance sonore thématique ; aucune allégation thérapeutique"),
    )

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
                text = "Respirez. Ralentissez. Retrouvez votre calme.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onOpenPlayer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Commencer une session")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(sections) { section ->
            CalmSectionCard(section)
        }
    }
}

@Composable
private fun CalmSectionCard(section: CalmSection) {
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
                text = section.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = section.subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SimpleSectionScreen(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(text = body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PlayerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Connexion au lecteur…") }

    DisposableEffect(context) {
        var attachedController: MediaController? = null
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
                statusMessage = if (isPlayingNow) {
                    "Lecture en cours • mode hors ligne"
                } else {
                    "Prêt • lecture hors ligne"
                }
            }
        }

        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java),
        )
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                runCatching { controllerFuture.get() }
                    .onSuccess { connectedController ->
                        attachedController = connectedController
                        connectedController.addListener(listener)
                        controller = connectedController
                        isPlaying = connectedController.isPlaying
                        statusMessage = if (connectedController.isPlaying) {
                            "Lecture en cours • mode hors ligne"
                        } else {
                            "Prêt • lecture hors ligne"
                        }
                    }
                    .onFailure {
                        statusMessage = "Lecteur indisponible"
                    }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            attachedController?.removeListener(listener)
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
            text = "Ambiance calme générée localement. Aucune connexion réseau n’est nécessaire.",
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
