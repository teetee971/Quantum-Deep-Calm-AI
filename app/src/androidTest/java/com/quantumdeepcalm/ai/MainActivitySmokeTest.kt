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
    fun offlinePlayerConnectsStartsCalmAndFeedsProgress() {
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText("Commencer avec Calm")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Commencer avec Calm")
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
            .onNodeWithText("Progression")
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
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText("Alpha Relaxation")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

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
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText("Commencer avec Calm")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Commencer avec Calm")
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
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText("Commencer avec Calm")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Commencer avec Calm")
            .performClick()

        waitForReadyAndStartPlayback()

        composeTestRule
            .onNodeWithText("Retour")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Commencer avec Calm")
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
            .onNodeWithText("Sommeil")
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
    fun favoriteAddedFromHomeAppearsInLibraryAndRemainsPlayable() {
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
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
            .onNodeWithText("Bibliothèque")
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
