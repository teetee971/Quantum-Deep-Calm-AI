package com.quantumdeepcalm.ai

internal data class SessionAudioProfile(
    val primaryHz: Double,
    val secondaryHz: Double,
    val accentHz: Double,
    val envelopeDepth: Double,
)

internal data class CalmSession(
    val id: String,
    val title: String,
    val subtitle: String,
    val audioProfile: SessionAudioProfile,
    val suitableForEvening: Boolean,
)

internal object SessionCatalog {
    val sessions: List<CalmSession> = listOf(
        CalmSession(
            id = "calm",
            title = "Calm",
            subtitle = "Ambiance douce générée localement pour une écoute posée",
            audioProfile = SessionAudioProfile(110.0, 165.0, 220.0, 0.28),
            suitableForEvening = true,
        ),
        CalmSession(
            id = "alpha-relaxation",
            title = "Alpha Relaxation",
            subtitle = "Paysage sonore de relaxation, sans promesse d’effet neurologique",
            audioProfile = SessionAudioProfile(132.0, 198.0, 264.0, 0.22),
            suitableForEvening = true,
        ),
        CalmSession(
            id = "theta-meditation",
            title = "Theta Meditation",
            subtitle = "Ambiance immersive inspirée de l’identité Quantum Deep Calm",
            audioProfile = SessionAudioProfile(96.0, 144.0, 192.0, 0.34),
            suitableForEvening = true,
        ),
        CalmSession(
            id = "delta-concentration",
            title = "Delta Concentration",
            subtitle = "Ambiance sobre conçue pour une écoute sans distraction",
            audioProfile = SessionAudioProfile(82.5, 123.75, 165.0, 0.16),
            suitableForEvening = false,
        ),
        CalmSession(
            id = "schumann",
            title = "Schumann",
            subtitle = "Ambiance créative thématique, sans allégation thérapeutique ou physiologique",
            audioProfile = SessionAudioProfile(128.0, 192.0, 256.0, 0.30),
            suitableForEvening = false,
        ),
    )

    val defaultSession: CalmSession = sessions.first()
    val ids: Set<String> = sessions.mapTo(linkedSetOf()) { it.id }
    val eveningSessions: List<CalmSession> = sessions.filter { it.suitableForEvening }

    fun find(sessionId: String): CalmSession? = sessions.firstOrNull { it.id == sessionId }
}
