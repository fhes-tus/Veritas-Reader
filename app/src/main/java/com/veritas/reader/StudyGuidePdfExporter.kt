package com.veritas.reader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StudyGuidePdfExporter {
    private const val TAG = "StudyGuidePdfExporter"
    private const val PAGE_WIDTH = 595 // A4 standard pt at 72 dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    fun generateStudyGuidePdf(
        context: Context,
        documentTitle: String,
        documentId: String,
        annotations: List<ReaderAnnotation>,
        generalNote: String,
        sentences: List<ReaderSentence>
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            val dateString = dateFormat.format(Date())

            val titlePaint = Paint().apply {
                color = Color.parseColor("#1C1B1F")
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#49454F")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val sectionPaint = Paint().apply {
                color = Color.parseColor("#6750A4")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.parseColor("#1C1B1F")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val quotePaint = Paint().apply {
                color = Color.parseColor("#313033")
                textSize = 9.5f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                isAntiAlias = true
            }

            val tagPaint = Paint().apply {
                color = Color.parseColor("#79747E")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#E7E0EC")
                strokeWidth = 1f
                isAntiAlias = true
            }

            val highlightBarPaint = Paint().apply {
                isAntiAlias = true
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var currentPage = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = currentPage.canvas
            var y = MARGIN

            fun checkNewPage(neededHeight: Float) {
                if (y + neededHeight > PAGE_HEIGHT - MARGIN) {
                    val footerText = "Veritas Study Guide • Page $pageNumber"
                    canvas.drawText(footerText, MARGIN, PAGE_HEIGHT - 20f, tagPaint)
                    pdfDocument.finishPage(currentPage)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    y = MARGIN
                }
            }

            // --- Header ---
            canvas.drawText("VERITAS STUDY GUIDE", MARGIN, y + 10f, tagPaint)
            y += 24f
            canvas.drawText(documentTitle, MARGIN, y + 10f, titlePaint)
            y += 26f
            canvas.drawText("Generated on $dateString • ${annotations.size} Highlights & Bookmarks", MARGIN, y, subtitlePaint)
            y += 14f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 20f

            // --- Section 1: General Booknote ---
            if (generalNote.isNotBlank()) {
                checkNewPage(40f)
                canvas.drawText("📝 Document Study Note", MARGIN, y, sectionPaint)
                y += 16f

                val noteLines = wrapText(generalNote, CONTENT_WIDTH - 16f, bodyPaint)
                checkNewPage((noteLines.size * 14f) + 30f)

                val boxTop = y
                val boxHeight = (noteLines.size * 14f) + 14f
                val bgPaint = Paint().apply { color = Color.parseColor("#F7F2FA"); isAntiAlias = true }
                canvas.drawRoundRect(MARGIN, boxTop, PAGE_WIDTH - MARGIN, boxTop + boxHeight, 6f, 6f, bgPaint)

                var lineY = boxTop + 18f
                noteLines.forEach { line ->
                    canvas.drawText(line, MARGIN + 10f, lineY, bodyPaint)
                    lineY += 14f
                }
                y = boxTop + boxHeight + 18f
            }

            // --- Section 2: Sentence Notes ---
            val sentenceNotes = annotations.filter { it.type == AnnotationType.NOTE }
            if (sentenceNotes.isNotEmpty()) {
                checkNewPage(40f)
                canvas.drawText("📌 Sentence Notes", MARGIN, y, sectionPaint)
                y += 18f

                sentenceNotes.forEach { note ->
                    val sentenceText = sentences.getOrNull(note.chunkIndex)?.text?.trim().orEmpty()
                    val quoteLines = if (sentenceText.isNotBlank()) wrapText("“$sentenceText”", CONTENT_WIDTH - 20f, quotePaint) else emptyList()
                    val noteLines = if (note.note.isNotBlank()) wrapText(note.note, CONTENT_WIDTH - 20f, bodyPaint) else emptyList()
                    val audioLabel = if (note.audioPath != null) " • 🎙️ Audio Memo (${note.audioDurationSeconds}s)" else ""
                    val totalH = (quoteLines.size * 13f) + (noteLines.size * 13f) + 26f
                    checkNewPage(totalH)

                    val pageNum = sentences.getOrNull(note.chunkIndex)?.pageNumber ?: 1
                    canvas.drawText("Sentence #${note.chunkIndex + 1} • Page $pageNum$audioLabel", MARGIN + 10f, y + 10f, tagPaint)
                    var textY = y + 24f
                    quoteLines.forEach { line ->
                        canvas.drawText(line, MARGIN + 10f, textY, quotePaint)
                        textY += 13f
                    }
                    noteLines.forEach { line ->
                        canvas.drawText(line, MARGIN + 10f, textY, bodyPaint)
                        textY += 13f
                    }
                    y = textY + 12f
                }
            }

            // --- Section 3: Key Highlights ---
            val highlights = annotations.filter { it.type == AnnotationType.HIGHLIGHT }
            if (highlights.isNotEmpty()) {
                checkNewPage(40f)
                canvas.drawText("✨ Key Highlights", MARGIN, y, sectionPaint)
                y += 18f

                highlights.forEach { hl ->
                    val sentenceText = sentences.getOrNull(hl.chunkIndex)?.text?.trim().orEmpty()
                    if (sentenceText.isNotBlank()) {
                        val quoteLines = wrapText("“$sentenceText”", CONTENT_WIDTH - 20f, quotePaint)
                        val noteLines = if (hl.note.isNotBlank()) wrapText("Note: ${hl.note}", CONTENT_WIDTH - 20f, bodyPaint) else emptyList()
                        val totalH = (quoteLines.size * 13f) + (noteLines.size * 13f) + 24f
                        checkNewPage(totalH)

                        val barColor = when (hl.highlightColor?.lowercase(Locale.getDefault())) {
                            "yellow", "#fff59d" -> Color.parseColor("#FBC02D")
                            "green", "#c8e6c9" -> Color.parseColor("#4CAF50")
                            "blue", "#bbdefb" -> Color.parseColor("#2196F3")
                            "purple", "#e1bee7" -> Color.parseColor("#9C27B0")
                            "orange", "#ffe0b2" -> Color.parseColor("#FF9800")
                            else -> Color.parseColor("#6750A4")
                        }
                        highlightBarPaint.color = barColor

                        val itemTop = y
                        canvas.drawRect(MARGIN, itemTop, MARGIN + 4f, itemTop + totalH - 8f, highlightBarPaint)

                        val pageNum = sentences.getOrNull(hl.chunkIndex)?.pageNumber ?: 1
                        canvas.drawText("Sentence #${hl.chunkIndex + 1} • Page $pageNum", MARGIN + 12f, itemTop + 10f, tagPaint)
                        var textY = itemTop + 24f
                        quoteLines.forEach { line ->
                            canvas.drawText(line, MARGIN + 12f, textY, quotePaint)
                            textY += 13f
                        }
                        noteLines.forEach { line ->
                            canvas.drawText(line, MARGIN + 12f, textY, bodyPaint)
                            textY += 13f
                        }
                        y = itemTop + totalH + 6f
                    }
                }
            }

            // --- Section 4: Bookmarks ---
            val bookmarks = annotations.filter { it.type == AnnotationType.BOOKMARK }
            if (bookmarks.isNotEmpty()) {
                checkNewPage(40f)
                canvas.drawText("🔖 Bookmarks & Audio Memos", MARGIN, y, sectionPaint)
                y += 18f

                bookmarks.forEach { bm ->
                    val sentenceText = sentences.getOrNull(bm.chunkIndex)?.text?.trim().orEmpty()
                    val quoteLines = if (sentenceText.isNotBlank()) wrapText("“$sentenceText”", CONTENT_WIDTH - 20f, quotePaint) else emptyList()
                    val noteLines = if (bm.note.isNotBlank()) wrapText("Note: ${bm.note}", CONTENT_WIDTH - 20f, bodyPaint) else emptyList()
                    val hasAudio = bm.audioPath != null
                    val audioLabel = if (hasAudio) " • 🎙️ Voice Memo (${bm.audioDurationSeconds}s)" else ""
                    val totalH = (quoteLines.size * 13f) + (noteLines.size * 13f) + 24f
                    checkNewPage(totalH)

                    val pageNum = sentences.getOrNull(bm.chunkIndex)?.pageNumber ?: 1
                    canvas.drawText("Bookmark at Sentence #${bm.chunkIndex + 1} • Page $pageNum$audioLabel", MARGIN + 12f, y + 10f, tagPaint)
                    var textY = y + 24f
                    quoteLines.forEach { line ->
                        canvas.drawText(line, MARGIN + 12f, textY, quotePaint)
                        textY += 13f
                    }
                    noteLines.forEach { line ->
                        canvas.drawText(line, MARGIN + 12f, textY, bodyPaint)
                        textY += 13f
                    }
                    y = textY + 10f
                }
            }

            // Draw final footer
            canvas.drawText("Veritas Study Guide • Page $pageNumber", MARGIN, PAGE_HEIGHT - 20f, tagPaint)
            pdfDocument.finishPage(currentPage)

            val dir = File(context.cacheDir, "study_guides").apply { if (!exists()) mkdirs() }
            val sanitized = documentTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(40)
            val outFile = File(dir, "Study_Guide_${sanitized}_${System.currentTimeMillis()}.pdf")
            FileOutputStream(outFile).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()

            Log.d(TAG, "Successfully generated Study Guide PDF: ${outFile.absolutePath}")
            return outFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate Study Guide PDF", e)
            return null
        }
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        if (text.isBlank()) return emptyList()
        val result = mutableListOf<String>()
        val words = text.split(Regex("""\s+"""))
        var currentLine = StringBuilder()

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                if (currentLine.isNotEmpty()) {
                    result.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            result.add(currentLine.toString())
        }
        return result
    }
}
