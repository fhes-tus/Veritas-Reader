package com.veritas.reader

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.util.Locale
import kotlinx.coroutines.CancellationException

class DocumentImportWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val uriString = inputData.getString("uri") ?: return Result.failure()
        val title = inputData.getString("title") ?: "Imported document"
        val isPartial = inputData.getBoolean("isPartial", false)

        val uri = Uri.parse(uriString)

        return try {
            val pdfStartPage = inputData.getInt("pdf_startPage", -1).let { if (it == -1) null else it }
            val pdfEndPage = inputData.getInt("pdf_endPage", -1).let { if (it == -1) null else it }
            val cropLeft = inputData.getFloat("pdf_cropLeft", -1f)
            val cropTop = inputData.getFloat("pdf_cropTop", -1f)
            val cropRight = inputData.getFloat("pdf_cropRight", -1f)
            val cropBottom = inputData.getFloat("pdf_cropBottom", -1f)
            val cropRect = if (cropLeft != -1f && cropTop != -1f && cropRight != -1f && cropBottom != -1f) {
                android.graphics.RectF(cropLeft, cropTop, cropRight, cropBottom)
            } else null

            val pdfOptions = PdfImportOptions(
                startPage = pdfStartPage,
                endPage = pdfEndPage,
                cleanupRepeatedLines = inputData.getBoolean("pdf_cleanupRepeatedLines", true),
                removePageNumbers = inputData.getBoolean("pdf_removePageNumbers", true),
                repairHyphenation = inputData.getBoolean("pdf_repairHyphenation", true),
                includePageMarkers = inputData.getBoolean("pdf_includePageMarkers", false),
                forceOcr = inputData.getBoolean("pdf_forceOcr", false),
                preferOcrWhenLowText = inputData.getBoolean("pdf_preferOcrWhenLowText", true),
                extractionMode = inputData.getString("pdf_extractionMode") ?: "HTML with images",
                removeTopPageNoise = inputData.getBoolean("pdf_removeTopPageNoise", true),
                removeBottomPageNoise = inputData.getBoolean("pdf_removeBottomPageNoise", true),
                manualCropBeforeExtract = inputData.getBoolean("pdf_manualCropBeforeExtract", false),
                minWordGap = inputData.getString("pdf_minWordGap") ?: "0.1",
                separateWordsOnFontChange = inputData.getBoolean("pdf_separateWordsOnFontChange", true),
                markPdfLinesForCanvas = inputData.getBoolean("pdf_markPdfLinesForCanvas", true),
                forceFreshExtraction = inputData.getBoolean("pdf_forceFreshExtraction", false),
                cropRect = cropRect
            )
            val textOptions = TextImportOptions(
                encodingId = inputData.getString("text_encodingId") ?: "auto_detect"
            )

            val mimeType = appContext.contentResolver.getType(uri).orEmpty().lowercase(Locale.getDefault())
            val extension = title.substringAfterLast('.', "").lowercase(Locale.getDefault())
            val isPdf = mimeType.contains("pdf") || extension == "pdf" || uri.path?.lowercase(Locale.getDefault())?.endsWith(".pdf") == true

            if (isPdf) {
                try {
                    val (document, totalPages) = DocumentExtractor.openPdfDocument(appContext, uri)
                    try {
                        if (totalPages > 0) {
                            if (pdfOptions.cropRect != null) {
                                DocumentExtractor.applyCropRect(document, pdfOptions.cropRect)
                            }

                            val start = pdfStartPage ?: 1
                            val end = pdfEndPage ?: totalPages
                            val chunkSize = 25
                            var currentStart = start
                            var documentId: String? = null
                            val repository = DocumentRepository(appContext)

                            while (currentStart <= end) {
                                val currentEnd = minOf(currentStart + chunkSize - 1, end)
                                val chunkOptions = pdfOptions.copy(
                                    startPage = currentStart,
                                    endPage = currentEnd,
                                    cropRect = null
                                )

                                val extracted = DocumentExtractor.extractPdfChunk(
                                    context = appContext,
                                    uri = uri,
                                    document = document,
                                    totalPageCount = totalPages,
                                    displayName = title,
                                    options = chunkOptions
                                )

                                val isComplete = currentEnd >= end

                                if (documentId == null) {
                                    if (extracted.text.isBlank() && isComplete) {
                                        return Result.failure(workDataOf("error" to "No readable text was found."))
                                    }

                                    val detectedLanguage = LanguageDetector.detectLanguage(extracted.text)
                                    val (newDocument, _) = repository.createDocumentWithResult(
                                        title = extracted.title,
                                        text = extracted.text,
                                        sourceLabel = extracted.sourceLabel,
                                        originalUri = uri,
                                        originalMimeType = appContext.contentResolver.getType(uri).orEmpty(),
                                        pageCount = totalPages,
                                        partial = !isComplete || isPartial,
                                        language = detectedLanguage
                                    )
                                    documentId = newDocument.id

                                    setProgress(workDataOf("firstChunkDocumentId" to newDocument.id))

                                    runCatching {
                                        val originalFile = repository.originalFile(newDocument)
                                        if (originalFile != null && originalFile.exists()) {
                                            CoverExtractor.extractCoverFromFile(appContext, newDocument.id, originalFile)
                                        }
                                    }
                                } else {
                                    repository.appendDocumentText(documentId, extracted.text, isComplete = isComplete)
                                }
                                currentStart = currentEnd + 1
                            }
                            return Result.success(workDataOf("documentId" to documentId))
                        }
                    } finally {
                        document.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return Result.failure(workDataOf("error" to (e.message ?: "Failed to open PDF document")))
                }
            }

            val extracted = DocumentExtractor.extract(
                context = appContext,
                uri = uri,
                displayName = title,
                pdfOptions = pdfOptions,
                textOptions = textOptions,
                foregroundBudgetMillis = null
            )

            if (extracted.text.isBlank()) {
                Result.failure(workDataOf("error" to "No readable text was found."))
            } else {
                val repository = DocumentRepository(appContext)
                val detectedLanguage = LanguageDetector.detectLanguage(extracted.text)
                val (newDocument, _) = repository.createDocumentWithResult(
                    title = extracted.title,
                    text = extracted.text,
                    sourceLabel = extracted.sourceLabel,
                    originalUri = uri,
                    originalMimeType = appContext.contentResolver.getType(uri).orEmpty(),
                    pageCount = extracted.pageCount,
                    partial = extracted.partial || isPartial,
                    language = detectedLanguage
                )

                runCatching {
                    val originalFile = repository.originalFile(newDocument)
                    if (originalFile != null && originalFile.exists()) {
                        CoverExtractor.extractCoverFromFile(appContext, newDocument.id, originalFile)
                    }
                }

                Result.success(workDataOf("documentId" to newDocument.id))
            }
        } catch (e: CancellationException) {
            Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(workDataOf("error" to e.message))
        }
    }
}
