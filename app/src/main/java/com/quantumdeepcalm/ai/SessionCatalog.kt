package com.quantumdeepcalm.ai

internal data class CalmSession(
    val id: String,
    val title: String,
    val subtitle: String,
)

internal object SessionCatalog {
    val sessions: List<CalmSession> = listOf(
        CalmSession(
            id = "calm",
            title = "Calm",
            subtitle = "Ambiance locale pour ralentir et retrouver un rythme plus calme",
        ),
        CalmSession(
            id = "alpha-relaxation",
            title = "Alpha Relaxation",
            subtitle = "Session de relaxation sonore, sans promesse d’effet neurologique",
        ),
        CalmSession(
            id = "theta-meditation",
            title = "Theta Meditation",
            subtitle = "Méditation immersive inspirée de l’identité Quantum Deep Calm",
        ),
        CalmSession(
            id = "delta-concentration",
            title = "Delta Concentration",
            subtitle = "Session audio conçue pour une écoute posée et sans distraction",
        ),
        CalmSession(
            id = "schumann",
            title = "Schumann",
            subtitle = "Ambiance sonore thématique ; aucune allégation thérapeutique",
        ),
    )

    val ids: Set<String> = sessions.mapTo(linkedSetOf()) { it.id }
}
