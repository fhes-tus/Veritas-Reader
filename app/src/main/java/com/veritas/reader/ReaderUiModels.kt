package com.veritas.reader

import java.util.Locale

fun progressFraction(document: SavedDocument): Float {
    return if (document.chunkCount <= 0) {
        0f
    } else {
        ((document.currentIndex + 1).toFloat() / document.chunkCount.toFloat()).coerceIn(0f, 1f)
    }
}

fun progressPercent(document: SavedDocument): Int = (progressFraction(document) * 100f).toInt().coerceIn(0, 100)

fun capWords(text: String, maxWords: Int): String {
    val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.size <= maxWords) return text
    return words.take(maxWords).joinToString(" ")
}

data class AiAssistantOption(
    val id: String,
    val label: String,
    val packageName: String,
    val alternatePackageNames: List<String> = emptyList(),
    val labelKeywords: List<String> = emptyList()
) {
    val packageNames: List<String>
        get() = (listOf(packageName) + alternatePackageNames).filter { it.isNotBlank() }.distinct()
}

enum class LibraryViewMode(val label: String, val icon: String) {
    MEDIUM("Medium-sized icons", "▣"),
    SMALL("Small icons", "▫"),
    LIST("List", "☰"),
    DETAILS("Details", "≡"),
    TILES("Tiles", "▦")
}

val aiAssistantOptions = listOf(
    AiAssistantOption("chooser", "Choose each time", ""),
    AiAssistantOption("chatgpt", "ChatGPT", "com.openai.chatgpt"),
    AiAssistantOption(
        "gemini",
        "Gemini",
        "com.google.android.apps.bard",
        alternatePackageNames = listOf("com.google.android.googlequicksearchbox"),
        labelKeywords = listOf("gemini", "google")
    ),
    AiAssistantOption("claude", "Claude", "com.anthropic.claude"),
    AiAssistantOption(
        "copilot",
        "Copilot",
        "com.microsoft.copilot",
        alternatePackageNames = listOf("com.microsoft.bing"),
        labelKeywords = listOf("copilot", "bing")
    ),
    AiAssistantOption("perplexity", "Perplexity", "ai.perplexity.app.android", labelKeywords = listOf("perplexity")),
    AiAssistantOption(
        "grok",
        "Grok",
        "com.xai.grok",
        alternatePackageNames = listOf("ai.x.grok"),
        labelKeywords = listOf("grok")
    )
)

fun String.veritasSortKey(): String = lowercase(Locale.getDefault())
