package com.quantumdeepcalm.ai

internal data class TopLevelDestination(
    val route: String,
    val label: String,
)

internal object AppNavigationContract {
    const val HOME = "home"
    const val LIBRARY = "library"
    const val SLEEP = "sleep"
    const val PROGRESS = "progress"
    const val PLAYER = "player"
    const val PRIVACY = "privacy"

    const val START_ROUTE = HOME

    val topLevelDestinations = listOf(
        TopLevelDestination(HOME, "Accueil"),
        TopLevelDestination(LIBRARY, "Bibliothèque"),
        TopLevelDestination(SLEEP, "Sommeil"),
        TopLevelDestination(PROGRESS, "Progression"),
    )

    fun hasValidTopLevelContract(): Boolean {
        val routes = topLevelDestinations.map { it.route }
        return routes.isNotEmpty() &&
            routes.toSet().size == routes.size &&
            START_ROUTE in routes &&
            topLevelDestinations.all { destination ->
                destination.route.isNotBlank() &&
                    destination.route == destination.route.lowercase() &&
                    destination.label.isNotBlank()
            }
    }
}
