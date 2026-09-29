package com.quantumdeepcalm.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun offlinePlayerConnectsAndStartsPlayback() {
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText("Ouvrir le lecteur")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Ouvrir le lecteur")
            .performClick()

        composeTestRule
            .onNodeWithText("Lecteur de méditation")
            .assertIsDisplayed()

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
