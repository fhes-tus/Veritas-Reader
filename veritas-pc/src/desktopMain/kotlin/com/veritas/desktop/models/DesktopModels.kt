package com.veritas.desktop.models

import java.util.UUID

data class DesktopDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sourceLabel: String = "Imported",
    val filePath: String? = null,
    val rawText: String = "",
    val chunks: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val collection: String = "All",
    val isFavorite: Boolean = false,
    val isQueued: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (chunks.isEmpty()) 0f else (currentIndex.toFloat() / chunks.size.coerceAtLeast(1)).coerceIn(0f, 1f)

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    val formattedDate: String
        get() = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(lastReadAt))
}

data class Bookmark(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val chunkIndex: Int,
    val previewText: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class TextAnnotation(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val chunkIndex: Int,
    val selectedText: String,
    val noteContent: String,
    val colorTag: String = "Yellow", // Yellow, Green, Blue, Purple, Orange, Rose
    val createdAt: Long = System.currentTimeMillis()
)

data class ChecklistItem(
    val text: String,
    val isChecked: Boolean = false
)

data class RichNote(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String? = null,
    val documentTitle: String? = null,
    val chunkIndex: Int? = null,
    val title: String = "",
    val content: String = "",
    val colorTag: String = "Yellow", // Yellow, Slate, Green, Ocean, Purple, Amber, Rose
    val isPinned: Boolean = false,
    val isChecklist: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ReadingList(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val documentIds: List<String> = emptyList(),
    val colorTag: String = "Blue",
    val createdAt: Long = System.currentTimeMillis()
)

data class HabitTracker(
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val totalMinutesRead: Long = 0L,
    val weeklyMinutesRead: Long = 0L,
    val todayMinutesRead: Long = 0L,
    val completedBooksCount: Int = 0,
    val lastReadDate: String = "",
    val dailyMinutesHistory: Map<String, Int> = emptyMap() // "yyyy-MM-dd" -> minutes
)

data class PronunciationRule(
    val id: String = UUID.randomUUID().toString(),
    val find: String,
    val replaceWith: String,
    val enabled: Boolean = true,
    val matchCase: Boolean = false
)

data class VoiceProfile(
    val name: String,
    val displayName: String,
    val culture: String = "en-US",
    val gender: String = "Neutral"
)

data class VoiceSettings(
    val voiceName: String = "",
    val rate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val volume: Float = 1.0f,
    val autoAdvance: Boolean = true,
    val pauseAtPunctuation: Boolean = true
)

enum class DesktopThemeType {
    SLATE_DARK,
    LIGHT_AIR,
    WARM_SEPIA,
    OBSIDIAN_OLED
}

enum class DesktopFontFamily(val label: String, val fontName: String) {
    DEFAULT("System Sans", "SansSerif"),
    ATKINSON("Atkinson Hyperlegible", "SansSerif"),
    LITERATA("Literata Book", "Serif"),
    SERIF("Classic Serif", "Serif"),
    MONOSPACE("Clean Mono", "Monospaced")
}

data class ReaderSettings(
    val fontSize: Float = 18f,
    val lineHeightMultiplier: Float = 1.6f,
    val fontFamily: DesktopFontFamily = DesktopFontFamily.DEFAULT,
    val themeType: DesktopThemeType = DesktopThemeType.SLATE_DARK,
    val maxReadingWidthDp: Int = 850,
    val isTwoColumnSpread: Boolean = false,
    val highlightActiveSentence: Boolean = true,
    val autoScrollToSentence: Boolean = true,
    val showSentenceIndices: Boolean = false
)

data class Flashcard(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val mastered: Boolean = false
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class StudyPack(
    val summary: String,
    val keyPoints: List<String>,
    val flashcards: List<Flashcard>,
    val quizQuestions: List<QuizQuestion>
)

enum class AppWindowMode {
    WORKSTATION,
    FLOATER
}

enum class WorkstationTab {
    HOME,
    LIBRARY,
    READER,
    NOTES,
    STUDY_ANALYTICS
}
