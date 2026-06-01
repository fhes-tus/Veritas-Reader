package com.veritas.reader

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.text.SimpleDateFormat
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
    val intent = Intent(context, PlaybackService::class.java).setAction(action)
    documentId?.let { intent.putExtra(PlaybackActions.EXTRA_DOCUMENT_ID, it) }
    intent.putExtra(PlaybackActions.EXTRA_START_INDEX, startIndex)
    intent.putExtra(PlaybackActions.EXTRA_RATE, rate)
    intent.putExtra(PlaybackActions.EXTRA_PITCH, pitch)
    if (action == PlaybackActions.ACTION_PLAY) {
        ContextCompat.startForegroundService(context, intent)
    } else {
        context.startService(intent)
    }
}

fun sendSelectionSpeechIntent(context: Context, selection: String) {
    val cleanSelection = selection.replace(Regex("\\s+"), " ").trim()
    if (cleanSelection.isBlank()) return
    val intent = Intent(context, PlaybackService::class.java)
        .setAction(PlaybackActions.ACTION_SPEAK_SELECTION)
        .putExtra(PlaybackActions.EXTRA_SELECTION_TEXT, cleanSelection)
    context.startService(intent)
}

fun isPackageInstalled(context: Context, packageName: String): Boolean {
    if (packageName.isBlank()) return true
    return runCatching { context.packageManager.getPackageInfo(packageName, 0) }.isSuccess
}

fun installedPackageForOption(context: Context, option: AiAssistantOption): String? {
    option.packageNames.firstOrNull { isPackageInstalled(context, it) }?.let { return it }
    if (option.labelKeywords.isEmpty()) return null
    return queryTextShareTargets(context)
        .firstOrNull { target ->
            val label = target.label.lowercase(Locale.getDefault())
            val packageName = target.packageName.lowercase(Locale.getDefault())
            option.labelKeywords.any { keyword -> keyword in label || keyword in packageName }
        }
        ?.packageName
}

fun installedAiOptions(context: Context): List<Pair<AiAssistantOption, String>> {
    return aiAssistantOptions
        .filter { it.packageName.isNotBlank() }
        .mapNotNull { option -> installedPackageForOption(context, option)?.let { option to it } }
}

private data class ShareTarget(val packageName: String, val label: String)

private fun queryTextShareTargets(context: Context): List<ShareTarget> {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
    }
    val pm = context.packageManager
    val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(intent, 0)
    }
    return resolved.mapNotNull { info ->
        val activityInfo = info.activityInfo ?: return@mapNotNull null
        ShareTarget(
            packageName = activityInfo.packageName.orEmpty(),
            label = info.loadLabel(pm).toString()
        )
    }
}

fun openPlayStoreForPackage(context: Context, packageName: String) {
    if (packageName.isBlank()) return
    val market = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
    val web = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri())
    try {
        context.startActivity(market)
    } catch (e: ActivityNotFoundException) {
        android.util.Log.w("ReaderActions", "Play Store app not available for $packageName: ${e.message}")
        try {
            context.startActivity(web)
        } catch (webError: ActivityNotFoundException) {
            android.util.Log.w("ReaderActions", "Could not open Play Store web fallback for $packageName: ${webError.message}")
        }
    }
}

fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied to clipboard.", Toast.LENGTH_SHORT).show()
}

fun sharePlainText(context: Context, title: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Share from Veritas"))
    } catch (e: ActivityNotFoundException) {
        android.util.Log.w("ReaderActions", "No share apps found: ${e.message}")
        copyTextToClipboard(context, title, text)
    }
}

fun openGoogleSearch(context: Context, text: String) {
    val cleanText = text.replace(Regex("\\s+"), " ").trim()
    if (cleanText.isBlank()) return
    val query = Uri.encode(cleanText.take(280))
    val intent = Intent(Intent.ACTION_VIEW, "https://www.google.com/search?q=$query".toUri())
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        android.util.Log.w("ReaderActions", "Could not open web lookup: ${e.message}")
        copyTextToClipboard(context, "Veritas lookup", cleanText)
    }
}

fun openGoogleTranslate(context: Context, text: String) {
    val cleanText = text.replace(Regex("\\s+"), " ").trim()
    if (cleanText.isBlank()) return
    val appIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        setPackage("com.google.android.apps.translate")
        putExtra(Intent.EXTRA_TEXT, cleanText)
    }
    if (isPackageInstalled(context, "com.google.android.apps.translate")) {
        try {
            context.startActivity(appIntent)
            return
        } catch (e: ActivityNotFoundException) {
            android.util.Log.w("ReaderActions", "Google Translate app not found despite package check: ${e.message}")
        }
    }
    val web = Intent(
        Intent.ACTION_VIEW,
        "https://translate.google.com/?sl=auto&text=${Uri.encode(cleanText.take(3_500))}&op=translate".toUri()
    )
    try {
        context.startActivity(web)
    } catch (e: ActivityNotFoundException) {
        android.util.Log.w("ReaderActions", "Could not open Google Translate web: ${e.message}")
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
    val selectedPackage = when {
        settings.packageName.isNotBlank() && isPackageInstalled(context, settings.packageName) -> settings.packageName
        settings.assistantId.isNotBlank() -> aiAssistantOptions
            .firstOrNull { it.id == settings.assistantId }
            ?.let { installedPackageForOption(context, it) }
            .orEmpty()
        else -> installedAiOptions(context).firstOrNull()?.second.orEmpty()
    }
    if (selectedPackage.isBlank()) {
        android.util.Log.w("ReaderActions", "No installed AI assistant found")
        copyTextToClipboard(context, "Veritas AI prompt", prompt)
        Toast.makeText(context, "No installed AI assistant found. Prompt copied.", Toast.LENGTH_LONG).show()
        return
    }
    if (!isPackageInstalled(context, selectedPackage)) {
        android.util.Log.w("ReaderActions", "Selected AI package $selectedPackage not installed, opening Play Store")
        openPlayStoreForPackage(context, selectedPackage)
        return
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, prompt)
        setPackage(selectedPackage)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        android.util.Log.w("ReaderActions", "Could not launch AI app $selectedPackage: ${e.message}")
        copyTextToClipboard(context, "Veritas AI prompt", prompt)
    }
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
