package com.veritas.reader

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.veritas.reader.ui.screens.LibraryBottomNavBar
import com.veritas.reader.ui.screens.VeritasHomeTab
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryNavigationUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun libraryBottomNavBarDisplaysAllTabs() {
        var selectedTab by mutableStateOf(VeritasHomeTab.HOME)

        composeTestRule.setContent {
            MaterialTheme {
                LibraryBottomNavBar(
                    activeNavTab = selectedTab,
                    showNavLabels = true,
                    onNavigateToTab = { selectedTab = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Library").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Study").assertIsDisplayed()
    }

    @Test
    fun clickingTabTriggersCallback() {
        var selectedTab = VeritasHomeTab.HOME

        composeTestRule.setContent {
            MaterialTheme {
                LibraryBottomNavBar(
                    activeNavTab = selectedTab,
                    showNavLabels = true,
                    onNavigateToTab = { selectedTab = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Library").performClick()
        assertEquals(VeritasHomeTab.LIBRARY, selectedTab)

        composeTestRule.onNodeWithText("Notes").performClick()
        assertEquals(VeritasHomeTab.NOTES, selectedTab)
    }
}
