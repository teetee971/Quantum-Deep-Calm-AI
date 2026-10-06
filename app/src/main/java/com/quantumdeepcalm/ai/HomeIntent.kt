package com.quantumdeepcalm.ai

internal data class HomeIntent(
    val id: String,
    val title: String,
    val subtitle: String,
    val sessionId: String,
)

internal object HomeIntentCatalog {
    val intents: List<HomeIntent> = listOf(
        HomeIntent(
            id = "calm",
            title = "Calme",
            subtitle = "Retrouver une ambiance douce immédiatement.",
            sessionId = "calm",
        ),
        HomeIntent(
            id = "sleep",
            title = "Sommeil",
            subtitle = "Passer à une ambiance du soir, sans promesse sur le sommeil.",
            sessionId = "theta-meditation",
        ),
        HomeIntent(
            id = "focus",
            title = "Focus",
            subtitle = "Réduire les distractions avec une ambiance sobre.",
            sessionId = "delta-concentration",
        ),
        HomeIntent(
            id = "breathe",
            title = "Respirer",
            subtitle = "Respiration libre avec une ambiance calme ; le guidage chronométré arrivera plus tard.",
            sessionId = "alpha-relaxation",
        ),
    )

    init {
        require(intents.map { it.id }.toSet().size == intents.size) {
            "Home intent ids must be unique."
        }
        require(intents.map { it.sessionId }.all { it in SessionCatalog.ids }) {
            "Every home intent must point to a real catalog session."
        }
    }
}
