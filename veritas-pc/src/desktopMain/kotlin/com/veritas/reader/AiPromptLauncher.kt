package com.veritas.reader

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

private const val MAX_AI_PROMPT_TEXT_CHARS = 18_000

enum class AiPromptScope(val label: String) {
    CURRENT_SECTION("current section"),
    WHOLE_DOCUMENT("whole document")
}

enum class AiPromptType(val label: String) {
    SUMMARY("Summary"),
    KEY_POINTS("Key points"),
    EXPLAIN_SECTION("Explain current section"),
    STUDY_NOTES("Study notes"),
    SIMPLIFY("Simplify"),
    SECTION_BY_SECTION("Section-by-section"),
    QUIZ("Quiz"),
    FLASHCARDS("Flashcards"),
    CUSTOM("Custom prompt")
}

object AiPromptLauncher {
    fun launch(
        context: Context,
        title: String,
        chunks: List<String>,
        currentIndex: Int,
        type: AiPromptType,
        customInstruction: String = "",
        scope: AiPromptScope = AiPromptScope.WHOLE_DOCUMENT,
        settings: AskAiSettings? = null
    ): Boolean {
        val prompt = buildPrompt(
            title = title,
            chunks = chunks,
            currentIndex = currentIndex,
            type = type,
            customInstruction = customInstruction,
            scope = scope
        )

        val isOversized = prompt.length > MAX_AI_PROMPT_TEXT_CHARS

        val intent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Veritas Reader - ${type.label}")
            preferredAiPackage(context, settings)?.let { packageName ->
                setPackage(packageName)
            }

            if (isOversized) {
                try {
                    val cacheDir = File(context.cacheDir, "ai_prompts").apply { mkdirs() }
                    val file = File(cacheDir, "Veritas_Prompt_${System.currentTimeMillis()}.txt")
                    file.writeText(prompt, Charsets.UTF_8)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    // Required for Android 11+ so recipient apps can read the attached file
                    clipData = ClipData.newRawUri("Veritas Prompt", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_TEXT, prompt.take(MAX_AI_PROMPT_TEXT_CHARS) + "\n\n[Veritas Note: Prompt was truncated here. The full prompt is attached as a text file.]")
                } catch (e: Exception) {
                    android.util.Log.w("AiPromptLauncher", "Could not create AI prompt file: ${e.message}", e)
                    putExtra(Intent.EXTRA_TEXT, prompt.take(MAX_AI_PROMPT_TEXT_CHARS) + "\n\n[Veritas Note: Prompt truncated due to Android share limits.]")
                }
            } else {
                putExtra(Intent.EXTRA_TEXT, prompt)
            }
        }

        return try {
            if (intent.`package`.isNullOrBlank()) {
                context.startActivity(Intent.createChooser(intent, "Send to an AI app"))
            } else {
                context.startActivity(intent)
            }
            true
        } catch (e: ActivityNotFoundException) {
            android.util.Log.w("AiPromptLauncher", "No AI app found, using clipboard: ${e.message}")
            copyPromptToClipboard(context, prompt)
            Toast.makeText(context, "No compatible app found. Prompt copied to clipboard.", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun preferredAiPackage(context: Context, settings: AskAiSettings?): String? {
        if (settings == null) return null
        if (settings.packageName.isNotBlank() && isPackageInstalled(context, settings.packageName)) {
            return settings.packageName
        }
        if (settings.assistantId.isNotBlank()) {
            aiAssistantOptions.firstOrNull { it.id == settings.assistantId }
                ?.let { installedPackageForOption(context, it) }
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }
        return installedAiOptions(context).firstOrNull()?.second
    }

    fun buildPrompt(
        title: String,
        chunks: List<String>,
        currentIndex: Int,
        type: AiPromptType,
        customInstruction: String = "",
        scope: AiPromptScope = AiPromptScope.WHOLE_DOCUMENT
    ): String {
        val selectedChunks = when (scope) {
            AiPromptScope.CURRENT_SECTION -> listOfNotNull(chunks.getOrNull(currentIndex.coerceIn(0, (chunks.size - 1).coerceAtLeast(0))))
            AiPromptScope.WHOLE_DOCUMENT -> chunks
        }
        val documentText = documentTextForPrompt(selectedChunks)
        val currentSection = chunks.getOrNull(currentIndex.coerceIn(0, (chunks.size - 1).coerceAtLeast(0)))
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()

        val instruction = when (type) {
            AiPromptType.SUMMARY -> if (scope == AiPromptScope.CURRENT_SECTION) {
                "Summarize only the current section/chunk clearly. Give the main idea, key points, and anything the reader should remember."
            } else {
                "Summarize the whole document clearly. Give a short overview first, then the main points, then any important conclusions or action items."
            }
            AiPromptType.KEY_POINTS -> "Extract the key points from this document. Group related ideas together and keep the wording clear for revision."
            AiPromptType.EXPLAIN_SECTION -> "Explain the current section in simple language. Identify the main idea, difficult terms, and why the section matters in the document."
            AiPromptType.STUDY_NOTES -> "Turn this document into organized study notes. Use headings, bullet points, definitions, examples, likely exam areas, and a short final revision checklist."
            AiPromptType.SIMPLIFY -> "Rewrite and explain the document in simpler language without removing important meaning. Define difficult words and give short examples where useful."
            AiPromptType.SECTION_BY_SECTION -> "This is part of a long-document workflow. Summarize this current section only, give 3-6 key points, define difficult terms, and end with a short note saying what the user should send next."
            AiPromptType.QUIZ -> "Create an exam-style revision quiz from this document. Include multiple choice questions, short answer questions, and answers with explanations."
            AiPromptType.FLASHCARDS -> "Create flashcards from this document. Use a question on the front and a concise answer on the back. Focus on definitions, processes, comparisons, and important facts."
            AiPromptType.CUSTOM -> customInstruction.ifBlank { "Help me study this document." }
        }

        val builder = StringBuilder()
        builder.appendLine("You are helping me read and study a document from Veritas Reader.")
        builder.appendLine()
        builder.appendLine("Task:")
        builder.appendLine(instruction)
        builder.appendLine()
        builder.appendLine("Document title:")
        builder.appendLine(title.ifBlank { "Untitled document" })
        builder.appendLine()
        builder.appendLine("Scope:")
        builder.appendLine(scope.label)
        builder.appendLine()

        if ((type == AiPromptType.EXPLAIN_SECTION || scope == AiPromptScope.CURRENT_SECTION) && currentSection.isNotBlank()) {
            builder.appendLine(if (type == AiPromptType.EXPLAIN_SECTION) "Current section to explain:" else "Current section to summarize/study:")
            builder.appendLine(currentSection)
            builder.appendLine()
            if (type == AiPromptType.EXPLAIN_SECTION && scope == AiPromptScope.WHOLE_DOCUMENT) {
                builder.appendLine("Full document context:")
                builder.appendLine(documentText)
            }
        } else {
            builder.appendLine(if (scope == AiPromptScope.CURRENT_SECTION) "Selected section text:" else "Document text:")
            builder.appendLine(documentText)
        }

        return builder.toString().trim()
    }

    private fun documentTextForPrompt(chunks: List<String>): String {
        val text = chunks
            .joinToString("\n\n") { it.replace(Regex("\\s+"), " ").trim() }
            .trim()
        // We now support attaching the full text as a file if it exceeds the limit,
        // so we return the full un-truncated text here.
        return text
    }

    fun copyPromptToClipboard(context: Context, prompt: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Veritas Reader AI prompt", prompt))
    }

    fun copyTextToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied to clipboard.", Toast.LENGTH_SHORT).show()
    }
}
