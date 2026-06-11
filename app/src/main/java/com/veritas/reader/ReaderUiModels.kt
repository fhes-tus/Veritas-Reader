package com.veritas.reader

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

enum class ReaderMode {
    TEXT, LISTEN, ORIGINAL
}

@Composable
fun ReaderModeToggle(
    currentMode: ReaderMode,
    onModeSelected: (ReaderMode) -> Unit,
    hasCanvas: Boolean,
    modifier: Modifier = Modifier
) {
    val modes = remember(hasCanvas) {
        if (hasCanvas) ReaderMode.values().toList() else listOf(ReaderMode.TEXT, ReaderMode.LISTEN)
    }
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEach { mode ->
            val selected = mode == currentMode
            val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
            val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(background)
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

data class VocabularyEntry(
    val word: String,
    val explanation: String,
    val source: String,
    val sentenceIndex: Int
)

fun parseVocabularyNoteContent(content: String): List<VocabularyEntry> {
    if (content.isBlank()) return emptyList()
    val chunks = content.split("\n\n")
    return chunks.mapNotNull { chunk ->
        val lines = chunk.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return@mapNotNull null
        val word = lines.getOrNull(0).orEmpty()
        val explanation = lines.getOrNull(1).orEmpty()
        val source = lines.getOrNull(2).orEmpty()
        val sentenceIndex = runCatching {
            val match = Regex("""sentence\s+(\d+)""", RegexOption.IGNORE_CASE).find(source)
            match?.groupValues?.getOrNull(1)?.toInt()?.minus(1) ?: 0
        }.getOrDefault(0)
        VocabularyEntry(word, explanation, source, sentenceIndex)
    }
}

