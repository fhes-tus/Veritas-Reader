package com.veritas.reader

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

enum class AiPromptScope(val label: String) {
    CURRENT_SECTION("current section"),
    WHOLE_DOCUMENT("whole document"),
    CUSTOM_PAGE_RANGE("custom page range")
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
        document: ReaderDocument,
        currentIndex: Int,
        type: AiPromptType,
        customInstruction: String = "",
        scope: AiPromptScope = AiPromptScope.WHOLE_DOCUMENT,
        customPageRange: IntRange? = null,
        settings: AskAiSettings? = null
    ): Boolean {
        val model = ReaderTextModelCache.get(document.id, document.rawText, document.pageCount)
        val selectedSentences = when (scope) {
            AiPromptScope.CURRENT_SECTION -> {
                val part = model.partForSentence(currentIndex)
                if (part == null) model.sentences else model.sentences.subList(part.sentenceStartIndex, part.sentenceEndIndexExclusive)
            }
            AiPromptScope.WHOLE_DOCUMENT -> {
                model.sentences
            }
            AiPromptScope.CUSTOM_PAGE_RANGE -> {
                if (customPageRange == null) model.sentences else model.sentences.filter { it.pageNumber in customPageRange }
            }
        }

        if (selectedSentences.isEmpty()) {
            Toast.makeText(context, "No content to share", Toast.LENGTH_SHORT).show()
            return false
        }

        val markdownText = buildMarkdownForSentences(selectedSentences)

        val minPage = selectedSentences.minOfOrNull { it.pageNumber } ?: 1
        val maxPage = selectedSentences.maxOfOrNull { it.pageNumber } ?: 1
        val bookTitle = document.title.trim()
        val sanitizedTitle = bookTitle.replace(Regex("[\\\\/:*?\"<>|]"), "_")

        val fileName = if (scope == AiPromptScope.CURRENT_SECTION) {
            if (minPage == maxPage) "Veritas - $sanitizedTitle - Page $minPage.md"
            else "Veritas - $sanitizedTitle - Pages $minPage-$maxPage.md"
        } else if (scope == AiPromptScope.CUSTOM_PAGE_RANGE) {
            if (minPage == maxPage) "Veritas - $sanitizedTitle - Page $minPage.md"
            else "Veritas - $sanitizedTitle - Pages $minPage-$maxPage.md"
        } else {
            "Veritas - $sanitizedTitle - Entire Document.md"
        }

        val prompt = buildPrompt(
            title = document.title,
            chunks = document.chunks,
            currentIndex = currentIndex,
            type = type,
            customInstruction = customInstruction,
            scope = scope
        )

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Veritas Prompt", prompt))
        Toast.makeText(context, "Prompt copied to clipboard", Toast.LENGTH_SHORT).show()

        val selectedPackage = preferredAiPackage(context, settings).orEmpty()

        val shareDir = File(context.cacheDir, "shares").apply { mkdirs() }
        val shareFile = File(shareDir, fileName)
        try {
            shareFile.writeText(markdownText, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", shareFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                this.type = "text/markdown"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri("Veritas Share", uri)
                if (selectedPackage.isNotBlank()) {
                    setPackage(selectedPackage)
                }
            }

            if (selectedPackage.isBlank()) {
                context.startActivity(Intent.createChooser(shareIntent, "Share Document to AI"))
            } else {
                try {
                    context.startActivity(shareIntent)
                } catch (e: ActivityNotFoundException) {
                    val chooserIntent = Intent.createChooser(shareIntent.apply { setPackage(null) }, "Share Document to AI")
                    context.startActivity(chooserIntent)
                }
            }
            return true
        } catch (e: Exception) {
            android.util.Log.e("AiPromptLauncher", "Error sharing markdown to AI: ${e.message}", e)
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
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
            AiPromptType.SECTION_BY_SECTION -> "This is part of a long-document workflow. Summarize this section or page range only, give 3-6 key points, define difficult terms, and end with a short note saying what the user should send next."
            AiPromptType.QUIZ -> "Create an exam-style revision quiz from this document. Include multiple choice questions, short answer questions, and answers with explanations."
            AiPromptType.FLASHCARDS -> "Create flashcards from this document. Use a question on the front and a concise answer on the back. Focus on definitions, processes, comparisons, and important facts."
            AiPromptType.CUSTOM -> customInstruction.ifBlank { "Help me study this document." }
        }

        val builder = StringBuilder()
        builder.appendLine("Attached is the document '${title.ifBlank { "Untitled document" }}'. Please perform the following task on it:")
        builder.appendLine()
        builder.appendLine("Task:")
        builder.appendLine(instruction)
        builder.appendLine()
        builder.appendLine("Note that page boundaries are marked with [[VERITAS_PAGE:X]] where X is the page number. Citations should refer to these page numbers.")

        return builder.toString().trim()
    }

    private fun buildMarkdownForSentences(sentences: List<ReaderSentence>): String {
        if (sentences.isEmpty()) return ""
        val output = StringBuilder()
        val ordered = sentences.groupBy { it.pageNumber.coerceAtLeast(1) }.toSortedMap()
        ordered.forEach { (pageNumber, pageSentences) ->
            if (output.isNotBlank()) output.append("\n\n")
            output.append("[[VERITAS_PAGE:$pageNumber]]\n\n")
            val pageText = StringBuilder()
            pageSentences.forEachIndexed { index, sentence ->
                if (pageText.isNotBlank()) {
                    val rawSeparator = sentence.separatorBefore.replace('\r', '\n')
                    val separator = when {
                        index > 0 && pageSentences.getOrNull(index - 1)?.pageNumber != sentence.pageNumber -> "\n\n"
                        rawSeparator.count { it == '\n' } >= 2 -> "\n\n"
                        rawSeparator.contains('\n') -> "\n"
                        else -> " "
                    }
                    pageText.append(separator)
                }
                pageText.append(sentence.text)
            }
            output.append(pageText.toString().trim())
        }
        return output.toString().trim()
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
