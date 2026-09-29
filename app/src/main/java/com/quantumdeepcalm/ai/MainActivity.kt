package com.quantumdeepcalm.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

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
        colorScheme = lightColorScheme(),
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
                    body = "Les méditations téléchargées, favorites et disponibles hors connexion seront regroupées ici.",
                )
            }
            composable(AppNavigationContract.SLEEP) {
                SimpleSectionScreen(
                    title = "Sommeil",
                    body = "Routines du soir, histoires, sons continus et programmes d'endormissement.",
                )
            }
            composable(AppNavigationContract.PROGRESS) {
                SimpleSectionScreen(
                    title = "Progression",
                    body = "Historique, régularité, minutes méditées et objectifs personnels.",
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
        CalmSection("Méditation", "Séances guidées et programmes personnalisés"),
        CalmSection("Respiration", "Exercices courts pour ralentir et se recentrer"),
        CalmSection("Sommeil", "Routines du soir, sons et séances d'endormissement"),
        CalmSection("Concentration", "Sessions conçues pour retrouver une attention stable"),
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
                text = "Application Android native",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(sections) { section ->
            CalmSectionCard(section)
        }

        item {
            Button(
                onClick = onOpenPlayer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Ouvrir le lecteur")
            }
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
            text = "Le moteur Media3 / ExoPlayer est maintenant intégré pour la lecture en arrière-plan. Les vrais contenus audio seront raccordés au prochain jalon.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onBack) {
            Text("Retour")
        }
    }
}
