package com.veritas.reader

import android.content.Context
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.net.URLEncoder
import java.text.SimpleDateFormat
import android.content.Intent
import java.util.Date
import java.util.Locale

fun sendPlaybackIntent(
    context: Context,
    action: String,
    documentId: String? = null,
    startIndex: Int = PlaybackStateStore.currentIndex,
    rate: Float = PlaybackStateStore.rate,
    pitch: Float = PlaybackStateStore.pitch
) {
    val intent = Intent(context, PlaybackService::class.java).apply {
        setAction(action)
        putExtra(PlaybackActions.EXTRA_DOCUMENT_ID, documentId)
        putExtra(PlaybackActions.EXTRA_START_INDEX, startIndex)
        putExtra(PlaybackActions.EXTRA_RATE, rate)
        putExtra(PlaybackActions.EXTRA_PITCH, pitch)
    }
    context.startService(intent)
}

fun sendSelectionSpeechIntent(context: Context, selection: String) {
    val intent = Intent(context, PlaybackService::class.java).apply {
        setAction(PlaybackActions.ACTION_SPEAK_SELECTION)
        putExtra(PlaybackActions.EXTRA_SELECTION_TEXT, selection)
    }
    context.startService(intent)
}

fun isPackageInstalled(context: Context, packageName: String): Boolean = false

fun installedPackageForOption(context: Context, option: AiAssistantOption): String? = null

fun installedAiOptions(context: Context): List<Pair<AiAssistantOption, String>> = emptyList()

fun openPlayStoreForPackage(context: Context, packageName: String) {
    val url = "https://play.google.com/store/apps/details?id=$packageName"
    runCatching {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}

fun copyTextToClipboard(context: Context, label: String, text: String) {
    runCatching {
        val selection = StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, null)
    }
}

fun sharePlainText(context: Context, title: String, text: String) {
    // On PC, share copies to clipboard
    copyTextToClipboard(context, title, text)
}

fun openGoogleSearch(context: Context, text: String) {
    val cleanText = text.replace(Regex("\\s+"), " ").trim()
    if (cleanText.isBlank()) return
    runCatching {
        val query = URLEncoder.encode(cleanText.take(280), "UTF-8")
        val url = "https://www.google.com/search?q=$query"
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}

fun openGoogleTranslate(context: Context, text: String) {
    val cleanText = text.replace(Regex("\\s+"), " ").trim()
    if (cleanText.isBlank()) return
    runCatching {
        val query = URLEncoder.encode(cleanText.take(3500), "UTF-8")
        val url = "https://translate.google.com/?sl=auto&text=$query&op=translate"
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}

fun askAiWithSelection(context: Context, settings: AskAiSettings, selection: String) {
    val prompt = settings.promptTemplate
        .replace("{selection}", selection)
        .replace("{text}", selection)
        .ifBlank { selection }
    sendPromptToAi(context, settings, "Ask ${settings.assistantLabel}", prompt)
}

fun sendPromptToAi(context: Context, settings: AskAiSettings, subject: String, prompt: String) {
    // Copy the prompt to clipboard on desktop so the user can paste it into their AI of choice
    copyTextToClipboard(context, subject, prompt)
}

fun buildDocumentNotesExport(
    document: ReaderDocument,
    annotations: List<ReaderAnnotation>,
    documentNote: String
): String {
    val notes = annotations
        .filter { it.type == AnnotationType.NOTE && it.note.isNotBlank() }
        .sortedBy { it.chunkIndex }
    val cleanDocumentNote = documentNote.trim()
    val exportedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    return buildString {
        appendLine("Veritas Reader Notes")
        appendLine("Document: ${document.title}")
        appendLine("Source: ${document.sourceLabel}")
        appendLine("Exported: $exportedAt")
        appendLine()
        if (cleanDocumentNote.isNotBlank()) {
            appendLine("Document note")
            appendLine(cleanDocumentNote)
            appendLine()
            appendLine("---")
            appendLine()
        }
        if (notes.isEmpty()) {
            if (cleanDocumentNote.isBlank()) {
                appendLine("No notes have been added to this document yet.")
            }
        } else {
            appendLine("Sentence notes")
            appendLine()
            notes.forEachIndexed { position, note ->
                val sentenceNumber = note.chunkIndex + 1
                val excerpt = document.chunks.getOrNull(note.chunkIndex)
                    .orEmpty()
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .take(360)
                appendLine("${position + 1}. Sentence $sentenceNumber")
                appendLine(note.note.trim())
                if (excerpt.isNotBlank()) {
                    appendLine()
                    appendLine("Excerpt: $excerpt${if (excerpt.length >= 360) "..." else ""}")
                }
                appendLine()
                appendLine("---")
                appendLine()
            }
        }
    }.trim()
}

fun openOriginalDocument(
    context: Context,
    repository: DocumentRepository,
    document: SavedDocument
) {
    val original = repository.originalFile(document)
    if (original == null || !original.exists()) {
        System.err.println("No stored original is available for this reading.")
        return
    }
    runCatching {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(original)
        } else {
            System.err.println("Desktop open action is not supported on this platform.")
        }
    }.onFailure { error ->
        System.err.println("Could not open the original file: ${error.message}")
    }
}

