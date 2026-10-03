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
            .performClick()

        composeTestRule
            .onNodeWithText("Retour")
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
    }

    @Test
    fun sleepTabOffersRealPlayableEveningSessions() {
        composeTestRule
            .onNodeWithText("Sommeil")
            .performClick()

        composeTestRule
            .onNodeWithText("Aucun effet sur le sommeil n’est garanti.", substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("play-session-theta-meditation")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Theta Meditation")
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
            .onNodeWithText("Bibliothèque")
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
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithText("Lecture en cours • mode hors ligne")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Pause")
            .assertIsDisplayed()
    }
}
