package com.quantumdeepcalm.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeIntentShortcutsOpenExpectedRealSessions() {
        val expected = listOf(
            "calm" to "Calm",
            "sleep" to "Theta Meditation",
            "focus" to "Delta Concentration",
            "breathe" to "Alpha Relaxation",
        )

        expected.forEach { (intentId, sessionTitle) ->
            composeTestRule
                .onNodeWithTag("home-intent-$intentId")
                .performScrollTo()
                .assertIsDisplayed()
                .performClick()

            composeTestRule
                .onNodeWithText("Lecteur de méditation")
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText(sessionTitle)
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithText("Retour")
                .performScrollTo()
                .performClick()
        }

        composeTestRule
            .onNodeWithText("Respiration libre", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun offlinePlayerConnectsStartsCalmAndFeedsProgress() {
        composeTestRule
            .onNodeWithTag("home-intent-calm")
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithText("Lecteur de méditation")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Calm")
            .assertIsDisplayed()

        waitForReadyAndStartPlayback()

        composeTestRule
            .onNodeWithText("Pause")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Retour")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("nav-progress")
            .performClick()

        composeTestRule
            .onNodeWithText("Sessions audio démarrées", substring = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Dernière lecture confirmée : Calm", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun catalogSessionActuallySelectsItsOwnPlayerContent() {
        composeTestRule
            .onNodeWithTag("home-catalog-toggle")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("play-session-alpha-relaxation")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Lecteur de méditation")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Alpha Relaxation")
            .assertIsDisplayed()

        waitForReadyAndStartPlayback()

        composeTestRule
            .onNodeWithText("Pause")
            .performScrollTo()
            .performClick()
    }

    @Test
    fun commercialPlayerExposesRealProgressAndApplicationOutputControls() {
        composeTestRule
            .onNodeWithTag("home-intent-calm")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText("Prêt • lecture hors ligne")
                .fetchSemanticsNodes()
                .isNotEmpty() ||
                composeTestRule
                    .onAllNodesWithText("Lecture en cours • mode hors ligne")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("player-progress")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("player-app-volume")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("player-intensity")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("volume système Android", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun activePlaybackSurvivesPlayerScreenNavigationAndReconnectsWithoutRestart() {
        composeTestRule
            .onNodeWithTag("home-intent-calm")
            .assertIsDisplayed()
            .performClick()

        waitForReadyAndStartPlayback()

        composeTestRule
            .onNodeWithText("Retour")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("home-intent-calm")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithText("Lecture en cours • mode hors ligne")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Pause")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun sleepTabOffersRealPlayableEveningSessions() {
        val firstEveningSession = SessionCatalog.eveningSessions.first()

        composeTestRule
            .onNodeWithTag("nav-sleep")
            .performClick()

        composeTestRule
            .onNodeWithText("Aucun effet sur le sommeil n’est garanti.", substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("play-session-${firstEveningSession.id}")
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithText("Lecteur de méditation")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(firstEveningSession.title)
            .assertIsDisplayed()
    }

    @Test
    fun favoriteAddedFromHomeCatalogAppearsInLibraryAndRemainsPlayable() {
        composeTestRule
            .onNodeWithTag("home-catalog-toggle")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithText("Ajouter aux favoris")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onAllNodesWithText("Ajouter aux favoris")[0]
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("nav-library")
            .performClick()

        composeTestRule
            .onNodeWithText("aucun téléchargement n’est nécessaire", substring = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Calm")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("play-session-calm")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Retirer des favoris")
            .assertIsDisplayed()
    }

    private fun waitForReadyAndStartPlayback() {
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText("Prêt • lecture hors ligne")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Lecture")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithText("Lecture en cours • mode hors ligne")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Pause")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
