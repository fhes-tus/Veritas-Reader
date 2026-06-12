package com.veritas.reader.ui

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot

enum class OnboardingStep(
    val key: String,
    val targetKey: String?,
    val title: String,
    val body: String
) {
    WELCOME(
        key = "welcome",
        targetKey = null,
        title = "Welcome to Veritas",
        body = "Let's walk through your reading environment. Tap Next to begin a quick tour of key locations."
    ),
    NAME_INPUT(
        key = "name_input",
        targetKey = null,
        title = "What should we call you?",
        body = "Please enter your name so we can personalize your dashboard and messages."
    ),
    FAB_SPOTLIGHT(
        key = "fab",
        targetKey = "add_fab",
        title = "Quest 1: Add a Document",
        body = "This is the Add button. Tap it to import PDFs, EPUBs, Word files, text files, images, or web links."
    ),
    CHECKLIST_SPOTLIGHT(
        key = "checklist",
        targetKey = "quest_checklist",
        title = "Your Quest Checklist",
        body = "This floating checklist shows your active missions. Complete all quests to master the app!"
    ),
    INSIGHTS_SPOTLIGHT(
        key = "insights",
        targetKey = "insights_trigger",
        title = "Reading Insights Dashboard",
        body = "Tap the menu button to open your personalized dashboard, track reading streaks, review stats, and manage preferences."
    ),
    DOCUMENT_SPOTLIGHT(
        key = "document",
        targetKey = "document_card_0",
        title = "Open your Document",
        body = "Your readings appear here. Tap a card to open it and enter the reader screen."
    ),
    MODE_TOGGLE_SPOTLIGHT(
        key = "mode_toggle",
        targetKey = "reader_mode_toggle",
        title = "Layout Mode Switcher",
        body = "Toggle layouts dynamically: TEXT for clean reading, LISTEN for lyrics/sentence highlights, and ORIGINAL for PDF layouts."
    ),
    PLAYER_PANEL_SPOTLIGHT(
        key = "player_panel",
        targetKey = "player_panel_header",
        title = "Expandable Playback Studio",
        body = "Tap or drag this panel up to access speed, pitch, premium voice selections, and sleep timers."
    ),
    READER_TEXT_SPOTLIGHT(
        key = "reader_text",
        targetKey = "reader_text_view",
        title = "Highlight & Study Actions",
        body = "Long-press any sentence to bookmark it, add study notes, translate text, or consult the AI Assistant."
    ),
    CONGRATULATIONS(
        key = "congratulations",
        targetKey = null,
        title = "Tour Complete!",
        body = "Awesome! You have completed the guided tour. Now complete the remaining quests on your checklist to unlock the full app!"
    )
}

object OnboardingController {
    var activeStep by mutableStateOf<OnboardingStep?>(null)
    val componentBounds = mutableStateMapOf<String, Rect>()
    
    fun updateBounds(key: String, coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            componentBounds[key] = coordinates.boundsInRoot()
        } else {
            componentBounds.remove(key)
        }
    }
}
