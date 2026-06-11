package com.veritas.reader

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    // This rule launches the MainActivity before each test
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndShowsLibraryTab() {
        // This simulates a user launching the app and verifying the text "Library" is on the screen
        composeTestRule.onNodeWithText("Library", ignoreCase = true).assertExists()
    }
}
