package com.nexxlabs.chhotu.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AssistantScreenTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun assistantScreen_initialState_showsGreeting() {
        // Assert Greeting is displayed
        composeTestRule.onNodeWithText("Chhotu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Voice Assistant").assertIsDisplayed()

        // Assert Mic button is displayed
        composeTestRule.onNodeWithContentDescription("Microphone").assertIsDisplayed()
    }

    @Test
    fun assistantScreen_typeCommand_showsInInput() {
        val command = "Hello Chhotu"

        // Find input field and type command
        composeTestRule.onNodeWithText("Type your command...").performTextInput(command)

        // Assert text matches
        composeTestRule.onNodeWithText(command).assertIsDisplayed()

        // Verify send button is displayed/enabled (icon check)
        composeTestRule.onNodeWithContentDescription("Send Command").assertIsDisplayed()
    }
}
