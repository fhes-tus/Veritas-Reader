package com.veritas.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.text.Html
import android.util.Xml
import androidx.core.graphics.createBitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text as MlText
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.PDFTextStripperByArea
import com.tom_roush.pdfbox.text.TextPosition
import java.io.ByteArrayInputStream
import java.io.StringReader
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipInputStream
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.yield
import kotlinx.coroutines.sync.withPermit
import org.xmlpull.v1.XmlPullParser

data class PdfImportOptions(
    val startPage: Int? = null,
    val endPage: Int? = null,
    val cleanupRepeatedLines: Boolean = true,
    val removePageNumbers: Boolean = true,
    val repairHyphenation: Boolean = true,
    val includePageMarkers: Boolean = false,
    val forceOcr: Boolean = false,
    val preferOcrWhenLowText: Boolean = true,
    val extractionMode: String = "HTML with images",
    val removeTopPageNoise: Boolean = true,
    val removeBottomPageNoise: Boolean = true,
    val manualCropBeforeExtract: Boolean = false,
    val minWordGap: String = "0.1",
    val separateWordsOnFontChange: Boolean = true,
    val markPdfLinesForCanvas: Boolean = true,
    val forceFreshExtraction: Boolean = false,
    val cropRect: RectF? = null
) {
    fun normalized(pageCount: Int): PdfImportOptions {
        val safePageCount = pageCount.coerceAtLeast(1)
        val safeStart = (startPage ?: 1).coerceIn(1, safePageCount)
        val safeEnd = (endPage ?: safePageCount).coerceIn(safeStart, safePageCount)
        return copy(startPage = safeStart, endPage = safeEnd)
    }
}

data class TextImportOptions(
    val encodingId: String = TextImportEncodingCatalog.AUTO_DETECT_ID
)

data class TextImportEncoding(
    val id: String,
    val label: String,
    val charsetName: String? = null,
    val useDeclaredHtmlCharset: Boolean = false
) {
    val isAvailable: Boolean
        get() = charsetName == null || Charset.isSupported(charsetName)
}

object TextImportEncodingCatalog {
    const val AUTO_DETECT_ID = "auto_detect"
    const val DECLARED_HTML_ID = "declared_html"

    private val rawOptions: List<TextImportEncoding> = listOf(
        TextImportEncoding(AUTO_DETECT_ID, "Auto-detect"),
        TextImportEncoding(DECLARED_HTML_ID, "Declared in HTML", useDeclaredHtmlCharset = true),
        TextImportEncoding("utf_8", "Unicode (UTF-8)", "UTF-8"),
        TextImportEncoding("utf_16le", "Unicode (UTF-16LE)", "UTF-16LE"),
        TextImportEncoding("utf_16be", "Unicode (UTF-16BE)", "UTF-16BE"),
        TextImportEncoding("windows_1256", "Arabic (windows-1256)", "windows-1256"),
        TextImportEncoding("iso_8859_6", "Arabic (ISO-8859-6)", "ISO-8859-6"),
        TextImportEncoding("iso_8859_4", "Baltic (ISO-8859-4)", "ISO-8859-4"),
        TextImportEncoding("iso_8859_13", "Baltic (ISO-8859-13)", "ISO-8859-13"),
        TextImportEncoding("windows_1257", "Baltic (windows-1257)", "windows-1257"),
        TextImportEncoding("iso_8859_14", "Celtic (ISO-8859-14)", "ISO-8859-14"),
        TextImportEncoding("iso_8859_2", "Central European Latin-2 (ISO-8859-2)", "ISO-8859-2"),
        TextImportEncoding("windows_1250", "Central European (windows-1250)", "windows-1250"),
        TextImportEncoding("cp852", "Central European (cp852)", "IBM852"),
        TextImportEncoding("gb2312", "Chinese Simplified (GB2312)", "GB2312"),
        TextImportEncoding("gb18030", "Chinese Simplified (GB18030)", "GB18030"),
        TextImportEncoding("big5", "Chinese Traditional (big5)", "Big5"),
        TextImportEncoding("iso_8859_5", "Cyrillic (ISO-8859-5)", "ISO-8859-5"),
        TextImportEncoding("koi8_r", "Cyrillic (KOI8-R)", "KOI8-R"),
        TextImportEncoding("koi8_u", "Cyrillic (KOI8-U)", "KOI8-U"),
        TextImportEncoding("windows_1251", "Cyrillic (windows-1251)", "windows-1251"),
        TextImportEncoding("cp866", "Cyrillic/Russian DOS (cp-866)", "IBM866"),
        TextImportEncoding("ibm855", "Cyrillic/DOS Alt (IBM855)", "IBM855"),
        TextImportEncoding("x_mac_cyrillic", "Cyrillic/Mac (x-MacCyrillic)", "x-MacCyrillic"),
        TextImportEncoding("windows_1253", "Greek (windows-1253)", "windows-1253"),
        TextImportEncoding("iso_8859_7", "Greek (ISO-8859-7)", "ISO-8859-7"),
        TextImportEncoding("iso_8859_8_i", "Hebrew (ISO-8859-8-I)", "ISO-8859-8"),
        TextImportEncoding("iso_8859_8", "Hebrew (ISO-8859-8)", "ISO-8859-8"),
        TextImportEncoding("windows_1255", "Hebrew (windows-1255)", "windows-1255"),
        TextImportEncoding("shift_jis", "Japanese (Shift_JIS)", "Shift_JIS"),
        TextImportEncoding("euc_jp", "Japanese (EUC-JP)", "EUC-JP"),
        TextImportEncoding("iso_2022_jp", "Japanese (ISO-2022-JP)", "ISO-2022-JP"),
        TextImportEncoding("euc_kr", "Korean (EUC-KR)", "EUC-KR"),
        TextImportEncoding("iso_8859_10", "Nordic Latin-6 (ISO-8859-10)", "ISO-8859-10"),
        TextImportEncoding("iso_8859_3", "South European Latin-3 (ISO-8859-3)", "ISO-8859-3"),
        TextImportEncoding("iso_8859_9", "Turkish Latin-5 (ISO-8859-9)", "ISO-8859-9"),
        TextImportEncoding("windows_1254", "Turkish (windows-1254)", "windows-1254"),
        TextImportEncoding("windows_1258", "Vietnamese (windows-1258)", "windows-1258"),
        TextImportEncoding("iso_8859_1", "West European Latin-1 (ISO-8859-1)", "ISO-8859-1"),
        TextImportEncoding("iso_8859_15", "West European Latin-9 (ISO-8859-15)", "ISO-8859-15"),
        TextImportEncoding("windows_1252", "West European (windows-1252)", "windows-1252")
    )

    val options: List<TextImportEncoding> = rawOptions.filter { it.isAvailable }

    fun byId(id: String): TextImportEncoding =
        options.firstOrNull { it.id == id } ?: options.first()
}

internal data class TextDecodingResult(
    val text: String,
    val encodingLabel: String,
    val diagnostics: List<String>
)

internal object TextImportDecoder {
    fun decode(bytes: ByteArray, options: TextImportOptions): TextDecodingResult {
        val requested = TextImportEncodingCatalog.byId(options.encodingId)
        val resolved = when {
            requested.useDeclaredHtmlCharset -> declaredHtmlCharset(bytes)?.let { charset ->
                DecodeCandidate(charset, requested.label, "Declared HTML charset: ${charset.name()}.")
            }
            requested.charsetName != null -> DecodeCandidate(
                charset = Charset.forName(requested.charsetName),
                requestLabel = requested.label,
                diagnostic = "Text decoded as ${requested.label}."
            )
            else -> autoDetectCandidate(bytes)
        } ?: DecodeCandidate(
            charset = StandardCharsets.UTF_8,
            requestLabel = requested.label,
            diagnostic = "No declared charset was found; decoded as Unicode (UTF-8)."
        )

        val text = decodeLenient(bytes, resolved.charset).trimLeadingBom()
        return TextDecodingResult(
            text = text,
            encodingLabel = resolved.requestLabel,
            diagnostics = listOf(resolved.diagnostic)
        )
    }

    private fun autoDetectCandidate(bytes: ByteArray): DecodeCandidate {
        detectBom(bytes)?.let { return it }
        declaredHtmlCharset(bytes)?.let { charset ->
            return DecodeCandidate(charset, "Auto-detect", "Auto-detected declared HTML charset: ${charset.name()}.")
        }
        if (decodeStrict(bytes, StandardCharsets.UTF_8) != null) {
            return DecodeCandidate(StandardCharsets.UTF_8, "Auto-detect", "Auto-detected Unicode (UTF-8).")
        }
        val fallback = Charset.forName("windows-1252")
        return DecodeCandidate(fallback, "Auto-detect", "Auto-detect fell back to West European (windows-1252).")
    }

    private fun detectBom(bytes: ByteArray): DecodeCandidate? {
        return when {
            bytes.startsWith(0xEF, 0xBB, 0xBF) ->
                DecodeCandidate(StandardCharsets.UTF_8, "Auto-detect", "Auto-detected Unicode (UTF-8) byte order mark.")
            bytes.startsWith(0xFF, 0xFE) ->
                DecodeCandidate(StandardCharsets.UTF_16LE, "Auto-detect", "Auto-detected Unicode (UTF-16LE) byte order mark.")
            bytes.startsWith(0xFE, 0xFF) ->
                DecodeCandidate(StandardCharsets.UTF_16BE, "Auto-detect", "Auto-detected Unicode (UTF-16BE) byte order mark.")
            else -> null
        }
    }

    private fun declaredHtmlCharset(bytes: ByteArray): Charset? {
        val header = bytes
            .take(4096)
            .toByteArray()
            .toString(StandardCharsets.ISO_8859_1)
        val match = Regex("""(?is)\bcharset\s*=\s*["']?\s*([A-Za-z0-9._-]+)""")
            .find(header)
            ?: Regex("""(?is)<\?xml\b[^>]*\bencoding\s*=\s*["']([^"']+)["']""").find(header)
        val name = match?.groupValues?.getOrNull(1)?.trim().orEmpty()
        return if (name.isBlank() || !Charset.isSupported(name)) null else Charset.forName(name)
    }

    private fun decodeStrict(bytes: ByteArray, charset: Charset): String? {
        return try {
            charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (_: CharacterCodingException) {
            null
        }
    }

    private fun decodeLenient(bytes: ByteArray, charset: Charset): String =
        charset.decode(ByteBuffer.wrap(bytes)).toString()

    private fun ByteArray.startsWith(vararg values: Int): Boolean =
        size >= values.size && values.indices.all { index -> (this[index].toInt() and 0xFF) == values[index] }

    private fun String.trimLeadingBom(): String = trimStart('\uFEFF')

    private data class DecodeCandidate(
        val charset: Charset,
        val requestLabel: String,
        val diagnostic: String
    )
}

object DocumentExtractor {
    const val DEFAULT_IMPORT_TIMEOUT_MS = 60_000L

    fun getPdfPageCount(context: Context, uri: Uri): Int {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                PDDocument.load(stream).use { document ->
                    document.numberOfPages
                }
            } ?: 0
        }.getOrDefault(0)
    }

    /**
     * Opens a PDF document and returns the PDDocument and page count.
     * Caller is responsible for closing the document via document.close().
     */
    fun openPdfDocument(context: Context, uri: Uri): Pair<PDDocument, Int> {
        PDFBoxResourceLoader.init(context.applicationContext)
        val tempFile = java.io.File(context.cacheDir, "temp_pdf_load_${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { stream ->
            tempFile.outputStream().use { out ->
                stream.copyTo(out)
            }
        } ?: throw IllegalStateException("Cannot open PDF URI")

        val document = try {
            PDDocument.load(tempFile)
        } catch (e: com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException) {
            throw IllegalArgumentException("This PDF is password-protected. Please remove password protection before importing.", e)
        } catch (e: java.io.IOException) {
            if (e.message?.contains("password", ignoreCase = true) == true || e.message?.contains("encrypted", ignoreCase = true) == true) {
                throw IllegalArgumentException("This PDF is password-protected or encrypted. Please remove password protection before importing.", e)
            }
            throw e
        } finally {
            runCatching { tempFile.delete() }
        }
        return document to document.numberOfPages
    }

    /**
     * Applies a crop rect to all pages of an already-open PDDocument.
     * Call once before chunked extraction begins.
     */
    fun applyCropRect(document: PDDocument, cropRect: RectF) {
        for (page in document.pages) {
            val mediaBox = page.mediaBox
            val cropX = mediaBox.lowerLeftX + cropRect.left * mediaBox.width
            val cropY = mediaBox.lowerLeftY + (1f - cropRect.bottom) * mediaBox.height
            val cropW = (cropRect.right - cropRect.left) * mediaBox.width
            val cropH = (cropRect.bottom - cropRect.top) * mediaBox.height
            page.cropBox = com.tom_roush.pdfbox.pdmodel.common.PDRectangle(cropX, cropY, cropW, cropH)
        }
    }

    /**
     * Extracts text from a chunk of pages in an already-open PDDocument.
     * Does NOT open or close the document.
     */
    suspend fun extractPdfChunk(
        context: Context,
        uri: Uri,
        document: PDDocument,
        totalPageCount: Int,
        displayName: String,
        options: PdfImportOptions
    ): ExtractedImport {
        coroutineContext.ensureActive()
        val normalizedOptions = options.normalized(totalPageCount)
        val startPage = normalizedOptions.startPage ?: 1
        val endPage = normalizedOptions.endPage ?: totalPageCount
        val selectedPageCount = (endPage - startPage + 1).coerceAtLeast(1)
        val diagnostics = mutableListOf<String>()

        val internalPdfTitle = runCatching { document.documentInformation?.title }.getOrNull()?.trim()
        val resolvedTitle = if (displayName.isNotBlank() && displayName != "Imported document" && displayName != "Imported Book" && !displayName.startsWith("Fetching Resource", ignoreCase = true)) {
            displayName
        } else if (!internalPdfTitle.isNullOrBlank() && !internalPdfTitle.equals("untitled", ignoreCase = true) && !internalPdfTitle.startsWith("Fetching Resource", ignoreCase = true)) {
            cleanDocumentTitle(internalPdfTitle)
        } else {
            displayName.ifBlank { "Imported document" }
        }

        if (normalizedOptions.forceOcr) {
            diagnostics.add("Forced OCR mode was used for this PDF import.")
            val ocr = extractPdfOcr(context, uri, normalizedOptions, null)
            val text = ocr.text.normalizeExtractedText().smartFormatPdfContent().let { MathText.beautify(it) }
            return ExtractedImport(
                title = resolvedTitle,
                text = text,
                sourceLabel = "PDF",
                note = (diagnostics + ocr.diagnostics).joinToString("\n").ifBlank { null },
                pageCount = selectedPageCount,
                partial = ocr.partial
            )
        }

        val pageNumbers = mutableListOf<Int>()
        val pageTexts = mutableListOf<String>()
        val cacheKey = "pdf_" + uri.toString().hashCode().toString()
        val cacheDir = java.io.File(context.cacheDir, cacheKey)
        cacheDir.mkdirs()

        for (pageNumber in startPage..endPage) {
            coroutineContext.ensureActive()
            pageNumbers.add(pageNumber)
            val cacheFile = java.io.File(cacheDir, "page_${pageNumber}.txt")
            val pageText = if (cacheFile.exists()) {
                cacheFile.readText(Charsets.UTF_8)
            } else {
                val txt = PdfPageTextExtractor.extractPage(document, pageNumber)
                runCatching { cacheFile.writeText(txt, Charsets.UTF_8) }
                txt
            }
            pageTexts.add(pageText)
            yield()
        }

        yield()
        val cleaned = PdfTextCleaner.cleanPages(pageTexts, pageNumbers, normalizedOptions)

        if (normalizedOptions.cleanupRepeatedLines && cleaned.removedRepeatedLineCount > 0) {
            diagnostics.add("Removed ${cleaned.removedRepeatedLineCount} repeated header/footer line${if (cleaned.removedRepeatedLineCount == 1) "" else "s"}.")
        }
        if (normalizedOptions.removePageNumbers && cleaned.removedPageNumberCount > 0) {
            diagnostics.add("Removed ${cleaned.removedPageNumberCount} standalone page number${if (cleaned.removedPageNumberCount == 1) "" else "s"}.")
        }
        if (normalizedOptions.repairHyphenation && cleaned.joinedHyphenationCount > 0) {
            diagnostics.add("Joined ${cleaned.joinedHyphenationCount} hyphenated line break${if (cleaned.joinedHyphenationCount == 1) "" else "s"}.")
        }

        val extractedPageCount = pageTexts.size.coerceAtLeast(1)
        val averageCharsPerPage = cleaned.text.length / extractedPageCount
        if (normalizedOptions.preferOcrWhenLowText && (cleaned.text.isBlank() || averageCharsPerPage < 80)) {
            diagnostics.add("Very little extractable PDF text was found, so OCR was attempted.")
            val ocr = extractPdfOcr(context, uri, normalizedOptions, null)
            if (ocr.text.isNotBlank()) {
                val text = ocr.text.normalizeExtractedText().smartFormatPdfContent().let { MathText.beautify(it) }
                runCatching { cacheDir.deleteRecursively() }
                return ExtractedImport(
                    title = resolvedTitle,
                    text = text,
                    sourceLabel = "PDF",
                    note = (diagnostics + ocr.diagnostics).joinToString("\n").ifBlank { null },
                    pageCount = selectedPageCount,
                    partial = ocr.partial
                )
            }
            diagnostics.add("OCR did not find readable text.")
        }

        runCatching { cacheDir.deleteRecursively() }

        val text = cleaned.text.normalizeExtractedText().smartFormatPdfContent().let { MathText.beautify(it) }
        return ExtractedImport(
            title = resolvedTitle,
            text = text,
            sourceLabel = "PDF",
            note = diagnostics.joinToString("\n").ifBlank { null },
            pageCount = selectedPageCount
        )
    }

    suspend fun extract(
        context: Context,
        uri: Uri,
        displayName: String,
        pdfOptions: PdfImportOptions = PdfImportOptions(),
        textOptions: TextImportOptions = TextImportOptions(),
        pptxOptions: PptxImportOptions = PptxImportOptions(),
        foregroundBudgetMillis: Long? = DEFAULT_IMPORT_TIMEOUT_MS
    ): ExtractedImport {
        coroutineContext.ensureActive()
        val extension = displayName.substringAfterLast('.', missingDelimiterValue = "").lowercase(Locale.getDefault())
        val mimeType = context.contentResolver.getType(uri).orEmpty().lowercase(Locale.getDefault())

        val sourceLabel = when {
            mimeType.contains("pdf") || extension == "pdf" || uri.path?.lowercase(Locale.getDefault())?.endsWith(".pdf") == true -> "PDF"
            mimeType.contains("wordprocessingml") || extension == "docx" || uri.path?.lowercase(Locale.getDefault())?.endsWith(".docx") == true -> "DOCX"
            mimeType.contains("presentationml") || extension == "pptx" || uri.path?.lowercase(Locale.getDefault())?.endsWith(".pptx") == true -> "PPTX"
            extension == "ppt" || mimeType.contains("ms-powerpoint") -> "PPT"
            mimeType.contains("epub") || extension == "epub" || uri.path?.lowercase(Locale.getDefault())?.endsWith(".epub") == true -> "EPUB"
            mimeType.startsWith("image/") || extension in imageExtensions || imageExtensions.any { uri.path?.lowercase(Locale.getDefault())?.endsWith(".$it") == true } -> "OCR"
            else -> "TXT"
        }

        val extracted = when (sourceLabel) {
            "OCR" -> extractImageOcr(context, uri)
            "PDF" -> extractPdf(context, uri, pdfOptions, foregroundBudgetMillis)
            "DOCX" -> ExtractionBody(extractDocx(readAllBytes(context, uri)))
            "PPTX" -> extractPptx(readAllBytes(context, uri), pptxOptions, foregroundBudgetMillis)
            "PPT" -> PptLegacyExtractor.extract(readAllBytes(context, uri))
            "EPUB" -> ExtractionBody(extractEpub(readAllBytes(context, uri)))
            else -> extractPlainText(context, uri, textOptions, isHtmlish(displayName) || mimeType.contains("html"))
        }

        val text = extracted.text.normalizeExtractedText()
            .let { if (sourceLabel == "PDF") it.smartFormatPdfContent() else it }
            // Prettify equations once, at extraction, so every downstream consumer
            // (reader display, selection offsets, TTS) sees the same Unicode math.
            .let { MathText.beautify(it) }

        val baseNote = when (sourceLabel) {
            "PDF" -> "PDF text was extracted with the current import options. If very little text was found, OCR may have been attempted depending on your settings."
            "DOCX" -> "DOCX body text was extracted. Images, footnotes, comments, and advanced layout are not fully modeled yet."
            "PPTX" -> buildString {
                append("PowerPoint slide text was extracted (titles, bullets, tables")
                if (pptxOptions.includeSpeakerNotes) append(", speaker notes")
                append("). Charts, SmartArt, and slide design are not included")
                if (pptxOptions.ocrSlideImages) append("; text found inside slide images was read with OCR")
                append(". Use Open original for the visual deck.")
            }
            "EPUB" -> "EPUB spine text was extracted. DRM-protected books are not supported."
            "OCR" -> "OCR extracted text from this image-based file."
            else -> null
        }

        val note = buildList {
            baseNote?.let { add(it) }
            addAll(extracted.diagnostics)
        }.joinToString("\n").ifBlank { null }

        return ExtractedImport(
            title = displayName.ifBlank { "Imported document" },
            text = text,
            sourceLabel = sourceLabel,
            note = note,
            pageCount = extracted.pageCount,
            partial = extracted.partial
        )
    }

    private suspend fun extractImageOcr(context: Context, uri: Uri): ExtractionBody {
        val image = InputImage.fromFilePath(context.applicationContext, uri)
        val recognized = recognizeText(image)
        val text = formatRecognizedText(recognized)
        val diagnostics = buildList {
            add("OCR completed using on-device Latin-script text recognition.")
            add("Detected ${recognized.textBlocks.size} text block${if (recognized.textBlocks.size == 1) "" else "s"}.")
            if (text.length < 40) {
                add("Only a small amount of text was detected. Try a clearer, straighter, higher-resolution image if the result looks incomplete.")
            }
        }
        return ExtractionBody(text, diagnostics, pageCount = 1)
    }

    private suspend fun extractPdfOcr(
        context: Context,
        uri: Uri,
        options: PdfImportOptions = PdfImportOptions(),
        deadlineMillis: Long? = null
    ): ExtractionBody = kotlinx.coroutines.coroutineScope {
        val diagnostics = mutableListOf<String>()
        val output = StringBuilder()
        var partial = false
        var requestedPagesForResult = 0
        val descriptor = if (uri.scheme == "file") {
            try {
                val filePath = uri.path
                    ?: return@coroutineScope ExtractionBody("", listOf("Could not open PDF pages for OCR rendering."))
                android.os.ParcelFileDescriptor.open(java.io.File(filePath), android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            } catch (e: Exception) {
                context.contentResolver.openFileDescriptor(uri, "r")
            }
        } else {
            context.contentResolver.openFileDescriptor(uri, "r")
        } ?: return@coroutineScope ExtractionBody("", listOf("Could not open PDF pages for OCR rendering."))

        descriptor.use { pfd ->
            val renderer = try {
                PdfRenderer(pfd)
            } catch (e: SecurityException) {
                return@coroutineScope ExtractionBody("", listOf("This PDF is password-protected or encrypted. Please remove password protection before running OCR."))
            } catch (e: Exception) {
                return@coroutineScope ExtractionBody("", listOf("Could not initialize PDF renderer for OCR: ${e.localizedMessage ?: "Unknown error"}"))
            }
            try {
                val normalizedOptions = options.normalized(renderer.pageCount)
                val startPage = normalizedOptions.startPage ?: 1
                val endPage = normalizedOptions.endPage ?: renderer.pageCount
                val requestedPages = (endPage - startPage + 1).coerceAtLeast(1)
                requestedPagesForResult = requestedPages
                val pagesToProcess = requestedPages.coerceAtMost(MAX_PDF_OCR_PAGES)
                if (requestedPages > MAX_PDF_OCR_PAGES) {
                    diagnostics.add("OCR was limited to $MAX_PDF_OCR_PAGES page${if (MAX_PDF_OCR_PAGES == 1) "" else "s"} from the selected range to protect memory and keep the app responsive.")
                    partial = true
                }

                val cacheKey = "ocr_" + uri.toString().hashCode().toString()
                val cacheDir = java.io.File(context.cacheDir, cacheKey)
                cacheDir.mkdirs()

                val ocrSemaphore = kotlinx.coroutines.sync.Semaphore(3)

                val deferredResults = (0 until pagesToProcess).map { offset ->
                    coroutineContext.ensureActive()
                    val pageIndex = startPage - 1 + offset
                    val cacheFile = java.io.File(cacheDir, "page_${pageIndex}.txt")
                    
                    if (cacheFile.exists()) {
                        kotlinx.coroutines.CompletableDeferred(pageIndex to cacheFile.readText(Charsets.UTF_8))
                    } else {
                        val page = renderer.openPage(pageIndex)
                        val bitmap = try {
                            renderPdfPage(page)
                        } finally {
                            page.close()
                        }
                        
                        val croppedBitmap = if (options.cropRect != null) {
                            val r = options.cropRect
                            val left = (r.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
                            val top = (r.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
                            val right = (r.right * bitmap.width).toInt().coerceIn(left + 1, bitmap.width)
                            val bottom = (r.bottom * bitmap.height).toInt().coerceIn(top + 1, bitmap.height)
                            Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top).also {
                                bitmap.recycle()
                            }
                        } else {
                            bitmap
                        }

                        this@coroutineScope.async(kotlinx.coroutines.Dispatchers.Default) {
                            ocrSemaphore.withPermit {
                                try {
                                    coroutineContext.ensureActive()
                                    if (!hasImportTimeRemaining(deadlineMillis)) {
                                        return@withPermit pageIndex to ""
                                    }
                                    val recognized = recognizeText(InputImage.fromBitmap(croppedBitmap, 0))
                                    val formatted = formatRecognizedText(recognized)
                                    runCatching { cacheFile.writeText(formatted, Charsets.UTF_8) }
                                    pageIndex to formatted
                                } finally {
                                    croppedBitmap.recycle()
                                }
                            }
                        }
                    }
                }

                val results = deferredResults.awaitAll()
                var droppedPageCount = 0
                results.forEach { (pageIndex, pageText) ->
                    if (pageText.isNotBlank()) {
                        if (output.isNotBlank()) output.append("\n\n")
                        output.append("Page ${pageIndex + 1}\n")
                        output.append(pageText)
                    } else {
                        val cacheFile = java.io.File(cacheDir, "page_${pageIndex}.txt")
                        if (!cacheFile.exists()) {
                            partial = true
                            droppedPageCount++
                        }
                    }
                }

                if (partial && !hasImportTimeRemaining(deadlineMillis)) {
                    diagnostics.add(
                        "$droppedPageCount page${if (droppedPageCount == 1) "" else "s"} ran out of time during OCR and will finish in the background, or can be re-imported with a smaller page range."
                    )
                }

                if (!partial) {
                    runCatching { cacheDir.deleteRecursively() }
                }
            } finally {
                renderer.close()
            }
        }

        if (output.isNotBlank()) {
            diagnostics.add("OCR extracted text from rendered PDF pages.")
        }
        ExtractionBody(output.toString(), diagnostics, pageCount = requestedPagesForResult, partial = partial)
    }

    private fun hasImportTimeRemaining(deadlineMillis: Long?): Boolean =
        deadlineMillis == null || System.currentTimeMillis() <= deadlineMillis

    private fun renderPdfPage(page: PdfRenderer.Page): Bitmap {
        val scale = (OCR_RENDER_TARGET_WIDTH.toFloat() / page.width.toFloat()).coerceIn(1.0f, 3.0f)
        val width = (page.width * scale).roundToInt().coerceAtLeast(1)
        val height = (page.height * scale).roundToInt().coerceAtLeast(1)
        val bitmap = createBitmap(width, height)
        Canvas(bitmap).drawColor(AndroidColor.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    private suspend fun recognizeText(image: InputImage): MlText {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            recognizer.process(image).awaitResult()
        } finally {
            recognizer.close()
        }
    }

    private fun formatRecognizedText(result: MlText): String {
        return result.textBlocks
            .sortedWith(compareBy<MlText.TextBlock> { it.boundingBox?.top ?: 0 }.thenBy { it.boundingBox?.left ?: 0 })
            .joinToString("\n\n") { block ->
                block.lines
                    .sortedWith(compareBy<MlText.Line> { it.boundingBox?.top ?: 0 }.thenBy { it.boundingBox?.left ?: 0 })
                    .joinToString("\n") { it.text }
            }
            .normalizeExtractedText()
    }

    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
        addOnCanceledListener {
            if (continuation.isActive) continuation.resumeWithException(CancellationException("ML Kit OCR task was cancelled."))
        }
    }

    private fun extractPlainText(
        context: Context,
        uri: Uri,
        options: TextImportOptions,
        htmlish: Boolean
    ): ExtractionBody {
        val bytes = readAllBytes(context, uri)
        if (bytes.isEmpty()) return ExtractionBody("")
        val decoded = TextImportDecoder.decode(bytes, options)
        val text = if (htmlish) htmlishToText(decoded.text) else decoded.text
        return ExtractionBody(text, decoded.diagnostics)
    }

    private suspend fun extractPdf(
        context: Context,
        uri: Uri,
        options: PdfImportOptions = PdfImportOptions(),
        foregroundBudgetMillis: Long? = DEFAULT_IMPORT_TIMEOUT_MS
    ): ExtractionBody {
        PDFBoxResourceLoader.init(context.applicationContext)
        context.contentResolver.openInputStream(uri).use { stream ->
            if (stream == null) return ExtractionBody("")
            val document = try {
                PDDocument.load(stream)
            } catch (e: com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException) {
                throw IllegalArgumentException("This PDF is password-protected. Please remove password protection before importing.", e)
            } catch (e: java.io.IOException) {
                if (e.message?.contains("password", ignoreCase = true) == true || e.message?.contains("encrypted", ignoreCase = true) == true) {
                    throw IllegalArgumentException("This PDF is password-protected or encrypted. Please remove password protection before importing.", e)
                }
                throw e
            }
            return try {
                val pageCount = document.numberOfPages.coerceAtLeast(1)
                val normalizedOptions = options.normalized(pageCount)
                val startPage = normalizedOptions.startPage ?: 1
                val endPage = normalizedOptions.endPage ?: pageCount
                val selectedPageCount = (endPage - startPage + 1).coerceAtLeast(1)
                val diagnostics = mutableListOf<String>()

                if (startPage != 1 || endPage != pageCount) {
                    diagnostics.add("Imported selected PDF page range: $startPage–$endPage of $pageCount pages.")
                }
                if (normalizedOptions.extractionMode.isNotBlank()) {
                    diagnostics.add("Import mode: ${normalizedOptions.extractionMode}.")
                }
                if (normalizedOptions.markPdfLinesForCanvas) {
                    diagnostics.add("Canvas line marks are enabled for comparing extracted text with the original PDF view.")
                }
                if (normalizedOptions.cropRect != null) {
                    diagnostics.add("Manual crop applied to the extraction area.")
                    val r = normalizedOptions.cropRect
                    for (page in document.pages) {
                        val mediaBox = page.mediaBox
                        val cropX = mediaBox.lowerLeftX + r.left * mediaBox.width
                        val cropY = mediaBox.lowerLeftY + (1f - r.bottom) * mediaBox.height
                        val cropW = (r.right - r.left) * mediaBox.width
                        val cropH = (r.bottom - r.top) * mediaBox.height
                        page.cropBox = com.tom_roush.pdfbox.pdmodel.common.PDRectangle(cropX, cropY, cropW, cropH)
                    }
                }
                if (normalizedOptions.forceFreshExtraction) {
                    diagnostics.add("Fresh extraction was requested for this import.")
                }

                val deadlineMillis = foregroundBudgetMillis?.let { System.currentTimeMillis() + it }

                if (normalizedOptions.forceOcr) {
                    diagnostics.add("Forced OCR mode was used for this PDF import.")
                    val ocr = extractPdfOcr(context, uri, normalizedOptions, deadlineMillis)
                    return ExtractionBody(
                        text = ocr.text,
                        diagnostics = diagnostics + ocr.diagnostics,
                        pageCount = selectedPageCount,
                        partial = ocr.partial
                    )
                }

                val pageNumbers = mutableListOf<Int>()
                val pageTexts = mutableListOf<String>()
                var partial = false
                val cacheKey = "pdf_" + uri.toString().hashCode().toString()
                val cacheDir = java.io.File(context.cacheDir, cacheKey)
                cacheDir.mkdirs()

                for (pageNumber in startPage..endPage) {
                    coroutineContext.ensureActive()
                    if (deadlineMillis != null && pageTexts.isNotEmpty() && System.currentTimeMillis() >= deadlineMillis) {
                        partial = true
                        break
                    }
                    pageNumbers.add(pageNumber)
                    val cacheFile = java.io.File(cacheDir, "page_${pageNumber}.txt")
                    val pageText = if (cacheFile.exists()) {
                        cacheFile.readText(Charsets.UTF_8)
                    } else {
                        val txt = PdfPageTextExtractor.extractPage(document, pageNumber)
                        runCatching { cacheFile.writeText(txt, Charsets.UTF_8) }
                        txt
                    }
                    pageTexts.add(pageText)
                    yield()
                }

                yield()
                val cleaned = PdfTextCleaner.cleanPages(pageTexts, pageNumbers, normalizedOptions)
                if (partial) {
                    diagnostics.add("Opened ${pageTexts.size} of $selectedPageCount selected PDF pages after the foreground import window. Veritas will continue extracting the rest in the background.")
                } else {
                    runCatching { cacheDir.deleteRecursively() }
                }
                if (normalizedOptions.cleanupRepeatedLines && cleaned.removedRepeatedLineCount > 0) {
                    diagnostics.add("Removed ${cleaned.removedRepeatedLineCount} repeated header/footer line${if (cleaned.removedRepeatedLineCount == 1) "" else "s"}.")
                }
                if (normalizedOptions.removePageNumbers && cleaned.removedPageNumberCount > 0) {
                    diagnostics.add("Removed ${cleaned.removedPageNumberCount} standalone page number${if (cleaned.removedPageNumberCount == 1) "" else "s"}.")
                }
                if (normalizedOptions.repairHyphenation && cleaned.joinedHyphenationCount > 0) {
                    diagnostics.add("Joined ${cleaned.joinedHyphenationCount} hyphenated line break${if (cleaned.joinedHyphenationCount == 1) "" else "s"}.")
                }
                if (normalizedOptions.includePageMarkers) {
                    diagnostics.add("Inserted page markers into the imported text.")
                }

                val extractedPageCount = pageTexts.size.coerceAtLeast(1)
                val averageCharsPerPage = cleaned.text.length / extractedPageCount
                if (!partial && normalizedOptions.preferOcrWhenLowText && (cleaned.text.isBlank() || averageCharsPerPage < 80)) {
                    diagnostics.add("Very little extractable PDF text was found in the selected range, so OCR was attempted on rendered PDF pages.")
                    val ocr = extractPdfOcr(context, uri, normalizedOptions, deadlineMillis)
                    if (ocr.text.isNotBlank()) {
                        return ExtractionBody(
                            text = ocr.text,
                            diagnostics = diagnostics + ocr.diagnostics,
                            pageCount = selectedPageCount,
                            partial = ocr.partial
                        )
                    }
                    diagnostics.add("OCR did not find readable text. The PDF may be low-resolution, handwritten, encrypted, or image quality may be too poor.")
                }
                ExtractionBody(cleaned.text, diagnostics, pageCount = selectedPageCount, partial = partial)
            } finally {
                document.close()
            }
        }
    }

    private fun extractDocx(bytes: ByteArray): String {
        var documentXml: String? = null
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory && entry.name == "word/document.xml") {
                    documentXml = zip.readBytes().toString(Charsets.UTF_8)
                    break
                }
            }
        }
        return documentXml?.let { xmlBodyToText(it) }.orEmpty()
    }

    private suspend fun extractPptx(
        bytes: ByteArray,
        options: PptxImportOptions,
        foregroundBudgetMillis: Long?
    ): ExtractionBody {
        val deck = PptxExtractor.parseDeck(bytes, includeSpeakerNotes = options.includeSpeakerNotes)
        if (deck.slides.isEmpty()) {
            return ExtractionBody("", listOf("No slides were found in this presentation."))
        }
        val diagnostics = mutableListOf<String>()
        var partial = false

        val ocrLinesBySlide = if (options.ocrSlideImages) {
            val deadlineMillis = foregroundBudgetMillis?.let { System.currentTimeMillis() + it }
            // One second pass over the zip pulls only the images slides reference.
            val wantedPaths = deck.slides.flatMap { it.mediaPaths }
                .distinct()
                .take(MAX_PPTX_OCR_IMAGES)
                .toSet()
            val mediaBytes = mutableMapOf<String, ByteArray>()
            if (wantedPaths.isNotEmpty()) {
                ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        val name = entry.name.trimStart('/')
                        if (!entry.isDirectory && name in wantedPaths) {
                            mediaBytes[name] = zip.readBytes()
                            if (mediaBytes.size == wantedPaths.size) break
                        }
                    }
                }
            }
            val ocrByPath = mutableMapOf<String, List<String>>()
            for ((path, imageBytes) in mediaBytes) {
                coroutineContext.ensureActive()
                if (!hasImportTimeRemaining(deadlineMillis)) {
                    partial = true
                    diagnostics.add("Some slide images ran out of time during OCR; re-import to retry.")
                    break
                }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
                // Decoration filter: logos, icons, and dividers aren't worth reading.
                if (minOf(bounds.outWidth, bounds.outHeight) < MIN_PPTX_OCR_IMAGE_DIMENSION) continue
                val sampled = BitmapFactory.Options().apply {
                    inSampleSize = (bounds.outWidth / OCR_RENDER_TARGET_WIDTH).coerceAtLeast(1)
                }
                val bitmap = runCatching {
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, sampled)
                }.getOrNull() ?: continue
                try {
                    val recognized = runCatching {
                        formatRecognizedText(recognizeText(InputImage.fromBitmap(bitmap, 0)))
                    }.getOrDefault("")
                    val lines = recognized.lines().map { it.trim() }.filter { it.isNotBlank() }
                    if (lines.isNotEmpty()) ocrByPath[path] = lines
                } finally {
                    bitmap.recycle()
                }
            }
            if (ocrByPath.isNotEmpty()) {
                diagnostics.add("OCR read text from ${ocrByPath.size} slide image${if (ocrByPath.size == 1) "" else "s"}.")
            }
            deck.slides.associate { slide ->
                slide.number to slide.mediaPaths.flatMap { ocrByPath[it].orEmpty() }
            }
        } else {
            emptyMap()
        }

        val text = PptxExtractor.renderDeckText(
            deck = deck,
            ocrLinesBySlide = ocrLinesBySlide,
            autoPunctuate = options.autoPunctuate
        )
        return ExtractionBody(
            text = text,
            diagnostics = diagnostics,
            pageCount = deck.slideCount,
            partial = partial
        )
    }

    private fun extractEpub(bytes: ByteArray): String {
        val entries = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val normalizedName = entry.name.trimStart('/')
                    val lower = normalizedName.lowercase(Locale.getDefault())
                    val shouldRead = lower == "meta-inf/container.xml" ||
                        lower.endsWith(".opf") || lower.endsWith(".xhtml") ||
                        lower.endsWith(".html") || lower.endsWith(".htm")
                    if (shouldRead) {
                        entries[normalizedName] = zip.readBytes().toString(Charsets.UTF_8)
                    }
                }
            }
        }

        val containerXml = entries.entries.firstOrNull { it.key.equals("META-INF/container.xml", ignoreCase = true) }?.value
        val opfPath = containerXml?.let { findContainerRootFile(it) }
        val opfXml = opfPath?.let { path -> entries[path] ?: entries.entries.firstOrNull { it.key.equals(path, ignoreCase = true) }?.value }

        val orderedContentPaths = if (opfPath != null && opfXml != null) {
            spineContentPaths(opfPath, opfXml)
        } else {
            entries.keys.filter { isHtmlish(it) }.sorted()
        }

        val output = StringBuilder()
        for (path in orderedContentPaths) {
            val content = entries[path] ?: entries.entries.firstOrNull { it.key.equals(path, ignoreCase = true) }?.value ?: continue
            val text = htmlishToText(content)
            if (text.isNotBlank()) {
                if (output.isNotBlank()) output.append("\n\n")
                output.append(text)
            }
        }

        if (output.isBlank()) {
            entries.filterKeys { isHtmlish(it) }.toSortedMap().values.forEach { html ->
                val text = htmlishToText(html)
                if (text.isNotBlank()) {
                    if (output.isNotBlank()) output.append("\n\n")
                    output.append(text)
                }
            }
        }

        return output.toString()
    }

    private fun findContainerRootFile(containerXml: String): String? {
        val match = Regex("""full-path\s*=\s*[\"']([^\"']+)[\"']""", RegexOption.IGNORE_CASE).find(containerXml)
        return match?.groupValues?.getOrNull(1)?.trimStart('/')
    }

    private fun spineContentPaths(opfPath: String, opfXml: String): List<String> {
        val baseDir = opfPath.substringBeforeLast('/', missingDelimiterValue = "")
        val manifest = mutableMapOf<String, String>()
        Regex("""<item\b[^>]*>""", RegexOption.IGNORE_CASE).findAll(opfXml).forEach { item ->
            val attrs = parseAttributes(item.value)
            val id = attrs["id"].orEmpty()
            val href = attrs["href"].orEmpty()
            val mediaType = attrs["media-type"].orEmpty().lowercase(Locale.getDefault())
            val looksReadable = mediaType.contains("xhtml") || mediaType.contains("html") || isHtmlish(href)
            if (id.isNotBlank() && href.isNotBlank() && looksReadable) {
                manifest[id] = resolveZipPath(baseDir, href)
            }
        }

        val ordered = mutableListOf<String>()
        Regex("""<itemref\b[^>]*>""", RegexOption.IGNORE_CASE).findAll(opfXml).forEach { itemref ->
            val attrs = parseAttributes(itemref.value)
            val idref = attrs["idref"].orEmpty()
            manifest[idref]?.let { ordered.add(it) }
        }
        return ordered.ifEmpty { manifest.values.toList() }
    }

    private fun parseAttributes(tag: String): Map<String, String> {
        val attrs = mutableMapOf<String, String>()
        Regex("""([A-Za-z_:][-A-Za-z0-9_:.]*)\s*=\s*[\"']([^\"']*)[\"']""").findAll(tag).forEach { match ->
            attrs[match.groupValues[1].lowercase(Locale.getDefault())] = match.groupValues[2]
        }
        return attrs
    }

    private fun resolveZipPath(baseDir: String, href: String): String {
        val decoded = runCatching { URLDecoder.decode(href, "UTF-8") }.getOrDefault(href)
            .substringBefore('#')
            .trimStart('/')
        if (baseDir.isBlank()) return decoded
        return "$baseDir/$decoded".replace("//", "/")
    }

    private fun isHtmlish(path: String): Boolean {
        val lower = path.lowercase(Locale.getDefault())
        return lower.endsWith(".xhtml") || lower.endsWith(".html") || lower.endsWith(".htm")
    }

    private fun xmlBodyToText(xml: String): String {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))
        val output = StringBuilder()
        var event = parser.eventType
        var inInstrText = false
        while (event != XmlPullParser.END_DOCUMENT) {
            val name = parser.name.orEmpty().substringAfter(':').lowercase(Locale.getDefault())
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (name == "instrtext") {
                        inInstrText = true
                    } else if (!inInstrText) {
                        when (name) {
                            "tab" -> output.append('\t')
                            "br" -> output.append('\n')
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (!inInstrText) {
                        output.append(parser.text.orEmpty())
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "instrtext") {
                        inInstrText = false
                    } else if (!inInstrText) {
                        when (name) {
                            "p", "tr", "h1", "h2", "h3", "h4", "h5", "h6" -> output.append("\n\n")
                            "tc", "td", "th" -> output.append('\t')
                        }
                    }
                }
            }
            event = parser.next()
        }
        return output.toString()
    }

    private fun htmlishToText(html: String): String {
        val withoutNoise = html
            .replace(Regex("(?is)<(script|style|svg|math)[^>]*>.*?</\\1>"), " ")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</(p|div|section|article|blockquote|li|h[1-6]|tr)>"), "\n\n")
            .replace(Regex("<[^>]+>"), " ")
        return decodeHtmlEntities(withoutNoise)
    }

    private fun decodeHtmlEntities(text: String): String {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    private fun readAllBytes(context: Context, uri: Uri): ByteArray {
        context.contentResolver.openInputStream(uri).use { stream ->
            return stream?.readBytes() ?: ByteArray(0)
        }
    }

    private const val OCR_RENDER_TARGET_WIDTH = 1600
    private const val MAX_PDF_OCR_PAGES = 150 // Increased to support longer scanned documents
    private const val MAX_PPTX_OCR_IMAGES = 80
    private const val MIN_PPTX_OCR_IMAGE_DIMENSION = 200 // px; skips logos/icons/dividers
    private val imageExtensions = setOf("png", "jpg", "jpeg", "webp", "bmp", "tif", "tiff")
}

data class ExtractionBody(
    val text: String,
    val diagnostics: List<String> = emptyList(),
    val pageCount: Int = 0,
    val partial: Boolean = false
)

data class PdfCleanupResult(
    val text: String,
    val removedRepeatedLineCount: Int,
    val removedPageNumberCount: Int,
    val joinedHyphenationCount: Int
)

private data class PositionedPdfLine(
    val text: String,
    val minX: Float,
    val maxX: Float,
    val y: Float,
    val height: Float
) {
    val centerX: Float
        get() = (minX + maxX) / 2f

    val width: Float
        get() = maxX - minX
}

private data class PositionedPdfGlyph(
    val minX: Float,
    val maxX: Float,
    val y: Float,
    val height: Float
) {
    val width: Float
        get() = maxX - minX
}

private data class PositionedPdfSegment(
    val minX: Float,
    val maxX: Float,
    val y: Float,
    val height: Float,
    val glyphCount: Int
) {
    val centerX: Float
        get() = (minX + maxX) / 2f

    val width: Float
        get() = maxX - minX
}

private data class PdfPageProbe(
    val plainText: String,
    val lines: List<PositionedPdfLine>,
    val segments: List<PositionedPdfSegment>
)

private data class PdfColumnLayout(
    val splitX: Float,
    val columnTopY: Float,
    val columnBottomY: Float,
    val pageWidth: Float,
    val pageHeight: Float,
    val rowBreaks: List<Pair<Float, Float>> = emptyList()
)

private class PdfLayoutProbeStripper : PDFTextStripper() {
    private val positionedLines = mutableListOf<PositionedPdfLine>()
    private val positionedGlyphs = mutableListOf<PositionedPdfGlyph>()

    init {
        sortByPosition = true
        setShouldSeparateByBeads(false)
    }

    override fun processTextPosition(text: TextPosition) {
        if (!text.unicode.isNullOrBlank() && text.widthDirAdj >= 0f) {
            positionedGlyphs.add(
                PositionedPdfGlyph(
                    minX = text.xDirAdj,
                    maxX = text.xDirAdj + text.widthDirAdj,
                    y = text.yDirAdj,
                    height = text.heightDir
                )
            )
        }
        super.processTextPosition(text)
    }

    override fun writeString(text: String, textPositions: MutableList<TextPosition>) {
        val cleanText = text.replace(Regex("\\s+"), " ").trim()
        if (cleanText.isNotBlank() && textPositions.isNotEmpty()) {
            val minX = textPositions.minOf { it.xDirAdj.toDouble() }.toFloat()
            val maxX = textPositions.maxOf { (it.xDirAdj + it.widthDirAdj).toDouble() }.toFloat()
            val y = textPositions.map { it.yDirAdj.toDouble() }.average().toFloat()
            val height = textPositions.map { it.heightDir.toDouble() }.average()
                .takeIf { !it.isNaN() }
                ?.toFloat()
                ?: 8f
            positionedLines.add(PositionedPdfLine(cleanText, minX, maxX, y, height))
        }
        super.writeString(text, textPositions)
    }

    companion object {
        fun extract(document: PDDocument, pageNumber: Int): PdfPageProbe {
            val stripper = PdfLayoutProbeStripper().apply {
                startPage = pageNumber
                endPage = pageNumber
            }
            val plainText = stripper.getText(document)
            return PdfPageProbe(
                plainText = plainText,
                lines = stripper.positionedLines.toList(),
                segments = buildVisualSegments(stripper.positionedGlyphs)
            )
        }

        private fun buildVisualSegments(glyphs: List<PositionedPdfGlyph>): List<PositionedPdfSegment> {
            val visibleGlyphs = glyphs
                .filter { it.width >= 0f && it.height > 0f }
                .sortedWith(compareBy<PositionedPdfGlyph> { it.y }.thenBy { it.minX })
            if (visibleGlyphs.isEmpty()) return emptyList()

            val averageHeight = visibleGlyphs.map { it.height.toDouble() }.average()
                .takeIf { !it.isNaN() }
                ?.toFloat()
                ?: 8f
            val lineTolerance = maxOf(2f, averageHeight * 0.58f)
            val yGroups = mutableListOf<MutableList<PositionedPdfGlyph>>()
            visibleGlyphs.forEach { glyph ->
                val group = yGroups.lastOrNull()
                val groupY = group?.map { it.y.toDouble() }?.average()?.toFloat()
                if (group != null && groupY != null && kotlin.math.abs(groupY - glyph.y) <= lineTolerance) {
                    group.add(glyph)
                } else {
                    yGroups.add(mutableListOf(glyph))
                }
            }

            val averageGlyphWidth = visibleGlyphs.map { it.width.toDouble() }.filter { it > 0.0 }.average()
                .takeIf { !it.isNaN() }
                ?.toFloat()
                ?: 4f
            val segmentGapThreshold = maxOf(12f, averageGlyphWidth * 4.0f)
            return yGroups.flatMap { group ->
                val sorted = group.sortedBy { it.minX }
                val segments = mutableListOf<MutableList<PositionedPdfGlyph>>()
                var current = mutableListOf<PositionedPdfGlyph>()
                sorted.forEach { glyph ->
                    val previous = current.lastOrNull()
                    val gap = previous?.let { glyph.minX - it.maxX } ?: 0f
                    if (previous != null && gap > segmentGapThreshold) {
                        segments.add(current)
                        current = mutableListOf()
                    }
                    current.add(glyph)
                }
                if (current.isNotEmpty()) segments.add(current)
                segments.mapNotNull { segment ->
                    if (segment.size < 2) return@mapNotNull null
                    val minX = segment.minOf { it.minX }
                    val maxX = segment.maxOf { it.maxX }
                    if (maxX - minX < 8f) return@mapNotNull null
                    PositionedPdfSegment(
                        minX = minX,
                        maxX = maxX,
                        y = segment.map { it.y.toDouble() }.average().toFloat(),
                        height = segment.map { it.height.toDouble() }.average().toFloat(),
                        glyphCount = segment.size
                    )
                }
            }
        }
    }
}

private object PdfPageTextExtractor {
    fun extractPage(document: PDDocument, pageNumber: Int): String {
        val page = document.getPage((pageNumber - 1).coerceAtLeast(0))
        val probe = PdfLayoutProbeStripper.extract(document, pageNumber)
        val layout = detectColumns(probe, page)
        return if (layout == null) {
            probe.plainText
        } else {
            extractColumnPage(page, layout).ifBlank { probe.plainText }
        }
    }

    private fun detectColumns(probe: PdfPageProbe, page: PDPage): PdfColumnLayout? {
        val box = page.cropBox ?: page.mediaBox ?: return null
        val pageWidth = box.width.coerceAtLeast(1f)
        val pageHeight = box.height.coerceAtLeast(1f)
        val fromSegments = detectColumnsFromSegments(probe.segments, pageWidth, pageHeight)
        
        val usefulLines = probe.lines
            .filter { it.text.length >= 2 && it.width > 8f && it.y in (pageHeight * 0.08f)..(pageHeight * 0.92f) }
            .sortedWith(compareBy<PositionedPdfLine> { it.y }.thenBy { it.minX })
        if (usefulLines.size < 12 && fromSegments == null) return null

        val contentMinX = usefulLines.minOfOrNull { it.minX } ?: (pageWidth * 0.08f)
        val contentMaxX = usefulLines.maxOfOrNull { it.maxX } ?: (pageWidth * 0.92f)
        val contentWidth = (contentMaxX - contentMinX).coerceAtLeast(1f)
        val midX = contentMinX + contentWidth / 2f
        val fullWidthThreshold = contentWidth * 0.64f
        val columnLines = usefulLines.filterNot { line ->
            line.width >= fullWidthThreshold || (line.minX < midX && line.maxX > midX)
        }

        val baseLayout = if (columnLines.size >= 10) {
            val left = columnLines.filter { it.centerX < midX }
            val right = columnLines.filter { it.centerX >= midX }
            if (left.size >= 5 && right.size >= 5) {
                val leftMaxX = left.maxOf { it.maxX }
                val rightMinX = right.minOf { it.minX }
                val gutter = rightMinX - leftMaxX
                if (gutter >= maxOf(12f, contentWidth * 0.02f)) {
                    val leftTop = left.minOf { it.y }
                    val rightTop = right.minOf { it.y }
                    val leftBottom = left.maxOf { it.y }
                    val rightBottom = right.maxOf { it.y }
                    val overlapTop = maxOf(leftTop, rightTop)
                    val overlapBottom = minOf(leftBottom, rightBottom)
                    val overlapHeight = overlapBottom - overlapTop
                    val columnHeight = (maxOf(leftBottom, rightBottom) - minOf(leftTop, rightTop)).coerceAtLeast(1f)
                    if (overlapHeight >= columnHeight * 0.42f) {
                        val averageLineHeight = columnLines.map { it.height.toDouble() }.average()
                            .takeIf { !it.isNaN() }
                            ?.toFloat()
                            ?: 10f
                        val columnTopY = (minOf(leftTop, rightTop) - averageLineHeight).coerceIn(0f, pageHeight)
                        val columnBottomY = (maxOf(leftBottom, rightBottom) + averageLineHeight * 2f).coerceIn(columnTopY, pageHeight)
                        PdfColumnLayout(
                            splitX = ((leftMaxX + rightMinX) / 2f).coerceIn(1f, pageWidth - 1f),
                            columnTopY = columnTopY,
                            columnBottomY = columnBottomY,
                            pageWidth = pageWidth,
                            pageHeight = pageHeight
                        )
                    } else null
                } else null
            } else null
        } else null

        val finalBase = baseLayout ?: fromSegments ?: return null

        // Detect horizontal breaks / chapter dividers spanning across the page within the column area
        val breakLines = usefulLines.filter { line ->
            val isHeader = Regex("""^(CHAPTER|Chapter|PROLOGUE|Prologue|EPILOGUE|Epilogue|INTRODUCTION|Introduction|PART|Part|BOOK|Book|SECTION|Section)\b.*""", RegexOption.IGNORE_CASE).containsMatchIn(line.text)
            val crossesGutter = (line.minX < finalBase.splitX - 10f && line.maxX > finalBase.splitX + 10f)
            val isCenteredBreak = kotlin.math.abs(line.centerX - finalBase.splitX) < contentWidth * 0.15f && line.width >= contentWidth * 0.35f
            val isPageSpanningHeader = (crossesGutter || isCenteredBreak) && (isHeader || line.width >= contentWidth * 0.45f)
            isPageSpanningHeader && line.y in (finalBase.columnTopY + 35f)..(finalBase.columnBottomY - 35f)
        }

        val rowBreaks = if (breakLines.isNotEmpty()) {
            val sortedBreaks = breakLines.sortedBy { it.y }
            val clusters = mutableListOf<MutableList<PositionedPdfLine>>()
            sortedBreaks.forEach { line ->
                val lastCluster = clusters.lastOrNull()
                if (lastCluster != null && line.y - lastCluster.last().y < 28f) {
                    lastCluster.add(line)
                } else {
                    clusters.add(mutableListOf(line))
                }
            }
            clusters.map { cluster ->
                val topY = (cluster.minOf { it.y } - 8f).coerceAtLeast(finalBase.columnTopY)
                val bottomY = (cluster.maxOf { it.y + it.height } + 8f).coerceAtMost(finalBase.columnBottomY)
                Pair(topY, bottomY)
            }
        } else emptyList()

        return finalBase.copy(rowBreaks = rowBreaks)
    }

    private fun detectColumnsFromSegments(
        segments: List<PositionedPdfSegment>,
        pageWidth: Float,
        pageHeight: Float
    ): PdfColumnLayout? {
        val usefulSegments = segments
            .filter { it.glyphCount >= 2 && it.width > 8f && it.y in (pageHeight * 0.08f)..(pageHeight * 0.92f) }
            .sortedWith(compareBy<PositionedPdfSegment> { it.y }.thenBy { it.minX })
        if (usefulSegments.size < 12) return null

        val contentMinX = usefulSegments.minOf { it.minX }
        val contentMaxX = usefulSegments.maxOf { it.maxX }
        val contentWidth = (contentMaxX - contentMinX).coerceAtLeast(1f)
        val midX = contentMinX + contentWidth / 2f
        val fullWidthThreshold = contentWidth * 0.64f
        val columnCandidates = usefulSegments.filterNot { segment ->
            segment.width >= fullWidthThreshold || (segment.minX < midX && segment.maxX > midX)
        }
        val left = columnCandidates.filter { it.centerX < midX }
        val right = columnCandidates.filter { it.centerX >= midX }
        if (left.size < 5 || right.size < 5) return null

        val leftMaxX = left.maxOf { it.maxX }
        val rightMinX = right.minOf { it.minX }
        val gutter = rightMinX - leftMaxX
        if (gutter < maxOf(10f, contentWidth * 0.015f)) return null

        val leftTop = left.minOf { it.y }
        val rightTop = right.minOf { it.y }
        val leftBottom = left.maxOf { it.y }
        val rightBottom = right.maxOf { it.y }
        val overlapTop = maxOf(leftTop, rightTop)
        val overlapBottom = minOf(leftBottom, rightBottom)
        val overlapHeight = overlapBottom - overlapTop
        val columnHeight = (maxOf(leftBottom, rightBottom) - minOf(leftTop, rightTop)).coerceAtLeast(1f)
        if (overlapHeight < columnHeight * 0.36f) return null

        val averageLineHeight = columnCandidates.map { it.height.toDouble() }.average()
            .takeIf { !it.isNaN() }
            ?.toFloat()
            ?: 10f
        return PdfColumnLayout(
            splitX = ((leftMaxX + rightMinX) / 2f).coerceIn(1f, pageWidth - 1f),
            columnTopY = (minOf(leftTop, rightTop) - averageLineHeight).coerceIn(0f, pageHeight),
            columnBottomY = (maxOf(leftBottom, rightBottom) + averageLineHeight * 2f).coerceIn(0f, pageHeight),
            pageWidth = pageWidth,
            pageHeight = pageHeight
        )
    }

    private fun extractColumnPage(page: PDPage, layout: PdfColumnLayout): String {
        val stripper = PDFTextStripperByArea().apply {
            sortByPosition = true
        }

        val top = RectF(0f, 0f, layout.pageWidth, layout.columnTopY)
        if (top.height() > 4f) stripper.addRegion("top", top)

        val bottom = RectF(0f, layout.columnBottomY, layout.pageWidth, layout.pageHeight)
        if (bottom.height() > 4f) stripper.addRegion("bottom", bottom)

        val resultParts = mutableListOf<String>()

        if (layout.rowBreaks.isEmpty()) {
            val left = RectF(0f, layout.columnTopY, layout.splitX, layout.columnBottomY)
            val right = RectF(layout.splitX, layout.columnTopY, layout.pageWidth, layout.columnBottomY)
            stripper.addRegion("left", left)
            stripper.addRegion("right", right)
            stripper.extractRegions(page)

            fun regionText(name: String): String =
                if (name in stripper.regions) cleanRegionText(stripper.getTextForRegion(name)) else ""

            val topText = regionText("top")
            val leftText = regionText("left")
            val rightText = regionText("right")
            val bottomText = regionText("bottom")
            val middleText = if (looksLikeDuplicateColumnText(leftText, rightText)) leftText else {
                listOf(leftText, rightText).filter { it.isNotBlank() }.joinToString("\n\n")
            }
            return listOf(topText, middleText, bottomText).filter { it.isNotBlank() }.joinToString("\n\n").trim()
        } else {
            var currentY = layout.columnTopY
            layout.rowBreaks.forEachIndexed { index, (breakTop, breakBottom) ->
                if (breakTop > currentY + 10f) {
                    val leftBand = RectF(0f, currentY, layout.splitX, breakTop)
                    val rightBand = RectF(layout.splitX, currentY, layout.pageWidth, breakTop)
                    stripper.addRegion("left_$index", leftBand)
                    stripper.addRegion("right_$index", rightBand)
                }
                val headerBand = RectF(0f, breakTop, layout.pageWidth, breakBottom)
                stripper.addRegion("header_$index", headerBand)
                currentY = breakBottom
            }
            if (layout.columnBottomY > currentY + 10f) {
                val lastIdx = layout.rowBreaks.size
                val leftBand = RectF(0f, currentY, layout.splitX, layout.columnBottomY)
                val rightBand = RectF(layout.splitX, currentY, layout.pageWidth, layout.columnBottomY)
                stripper.addRegion("left_$lastIdx", leftBand)
                stripper.addRegion("right_$lastIdx", rightBand)
            }

            stripper.extractRegions(page)

            fun regionText(name: String): String =
                if (name in stripper.regions) cleanRegionText(stripper.getTextForRegion(name)) else ""

            val topText = regionText("top")
            if (topText.isNotBlank()) resultParts.add(topText)

            var bandY = layout.columnTopY
            layout.rowBreaks.forEachIndexed { index, (breakTop, breakBottom) ->
                if (breakTop > bandY + 10f) {
                    val l = regionText("left_$index")
                    val r = regionText("right_$index")
                    if (l.isNotBlank()) resultParts.add(l)
                    if (r.isNotBlank() && !looksLikeDuplicateColumnText(l, r)) resultParts.add(r)
                }
                val h = regionText("header_$index")
                if (h.isNotBlank()) resultParts.add(h)
                bandY = breakBottom
            }
            if (layout.columnBottomY > bandY + 10f) {
                val lastIdx = layout.rowBreaks.size
                val l = regionText("left_$lastIdx")
                val r = regionText("right_$lastIdx")
                if (l.isNotBlank()) resultParts.add(l)
                if (r.isNotBlank() && !looksLikeDuplicateColumnText(l, r)) resultParts.add(r)
            }

            val bottomText = regionText("bottom")
            if (bottomText.isNotBlank()) resultParts.add(bottomText)

            return resultParts.filter { it.isNotBlank() }.joinToString("\n\n").trim()
        }
    }

    private fun cleanRegionText(text: String): String {
        return text.replace('\r', '\n')
            .lineSequence()
            .map { it.trimEnd() }
            .dropWhile { it.isBlank() }
            .joinToString("\n")
            .trim()
    }

    private fun looksLikeDuplicateColumnText(left: String, right: String): Boolean {
        val normalizedLeft = normalizeForColumnDuplicate(left)
        val normalizedRight = normalizeForColumnDuplicate(right)
        if (normalizedLeft.length < 120 || normalizedRight.length < 120) return false
        val minLength = minOf(normalizedLeft.length, normalizedRight.length)
        val maxLength = maxOf(normalizedLeft.length, normalizedRight.length)
        if (minLength.toFloat() / maxLength.toFloat() < 0.86f) return false
        val prefixLength = minOf(900, minLength)
        var same = 0
        for (index in 0 until prefixLength) {
            if (normalizedLeft[index] == normalizedRight[index]) same++
        }
        return same.toFloat() / prefixLength.toFloat() >= 0.90f
    }

    private fun normalizeForColumnDuplicate(text: String): String {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9]+"), "")
    }
}

object PdfTextCleaner {
    fun cleanPages(pageTexts: List<String>, pageNumbers: List<Int> = pageTexts.indices.map { it + 1 }, options: PdfImportOptions = PdfImportOptions()): PdfCleanupResult {
        val pageLines = pageTexts.map { page ->
            page.replace('\r', '\n')
                .split('\n')
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }

        val repeatedKeys = if (options.cleanupRepeatedLines) findRepeatedHeaderFooterKeys(pageLines) else emptySet()
        var removedRepeated = 0
        var removedPageNumbers = 0
        var joinedHyphenations = 0
        val documentOutput = StringBuilder()

        pageLines.forEachIndexed { pageIndex, lines ->
            val cleanedLines = mutableListOf<String>()
            lines.flatMap(::splitWideGappedLine).forEach { line ->
                val key = normalizedLineKey(line)
                when {
                    options.cleanupRepeatedLines && key in repeatedKeys -> removedRepeated++
                    options.removePageNumbers && isStandalonePageNumber(line) -> removedPageNumbers++
                    else -> cleanedLines.add(line)
                }
            }

            val merged = mergePdfLines(cleanedLines, repairHyphenation = options.repairHyphenation) { joinedHyphenations++ }
            if (merged.isNotBlank()) {
                if (documentOutput.isNotBlank()) documentOutput.append("\n\n")
                val pageNumber = pageNumbers.getOrNull(pageIndex) ?: (pageIndex + 1)
                documentOutput.append(ReaderTextIndex.pageMarker(pageNumber)).append("\n")
                if (options.includePageMarkers) {
                    documentOutput.append("Page $pageNumber\n")
                }
                documentOutput.append(merged)
            }
            if (pageIndex < pageLines.lastIndex && merged.isNotBlank()) {
                documentOutput.append("\n")
            }
        }

        return PdfCleanupResult(
            text = documentOutput.toString(),
            removedRepeatedLineCount = removedRepeated,
            removedPageNumberCount = removedPageNumbers,
            joinedHyphenationCount = joinedHyphenations
        )
    }

    private fun findRepeatedHeaderFooterKeys(pageLines: List<List<String>>): Set<String> {
        if (pageLines.size < 3) return emptySet()
        val counts = mutableMapOf<String, Int>()
        pageLines.forEach { lines ->
            val candidates = buildSet {
                lines.take(3).forEach { add(it) }
                lines.takeLast(3).forEach { add(it) }
            }
            candidates.forEach { line ->
                val key = normalizedLineKey(line)
                if (key.length in 4..120 && !isStandalonePageNumber(line)) {
                    counts[key] = (counts[key] ?: 0) + 1
                }
            }
        }
        val threshold = maxOf(2, (pageLines.size * 0.55f).toInt())
        return counts.filterValues { it >= threshold }.keys
    }

    private fun mergePdfLines(lines: List<String>, repairHyphenation: Boolean = true, onHyphenationJoined: () -> Unit): String {
        val output = StringBuilder()
        var previousWasHeading = false
        lines.forEach { originalLine ->
            val line = originalLine.trim()
            if (line.isBlank()) return@forEach
            val currentIsHeading = looksLikeHeading(line)
            val currentIsNumberedItem = looksLikeNumberedItemStart(line)

            if (repairHyphenation && output.endsWith("-") && line.firstOrNull()?.isLowerCase() == true) {
                output.deleteCharAt(output.length - 1)
                output.append(line)
                previousWasHeading = false
                onHyphenationJoined()
                return@forEach
            }

            if (output.isBlank()) {
                if (currentIsHeading && !line.startsWith("#")) {
                    output.append("# ").append(line)
                } else {
                    output.append(line)
                }
                previousWasHeading = currentIsHeading
                return@forEach
            }

            when {
                currentIsHeading -> {
                    output.append("\n\n")
                    if (!line.startsWith("#")) {
                        output.append("# ")
                    }
                }
                previousWasHeading || currentIsNumberedItem -> {
                    output.append("\n\n")
                }
                else -> {
                    output.append(' ')
                }
            }
            output.append(line)
            previousWasHeading = currentIsHeading
        }
        return output.toString()
    }

    private fun splitWideGappedLine(line: String): List<String> {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return emptyList()
        // If line is a chapter/section heading like "CHAPTER   III.", do NOT split wide gaps
        if (Regex("""^(CHAPTER|Chapter|PART|Part|BOOK|Book)\s+.*""", RegexOption.IGNORE_CASE).matches(trimmed)) {
            return listOf(trimmed.replace(Regex("""\s{2,}"""), " "))
        }
        val parts = Regex("""\s{4,}""")
            .split(trimmed)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        return if (parts.size > 1 && parts.all { it.length >= 2 }) parts else listOf(trimmed)
    }

    private fun looksLikeHeading(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.length > 90 || trimmed.isEmpty()) return false
        if (trimmed.startsWith("[[VERITAS_") || trimmed.contains("VERITAS_PAGE", ignoreCase = true) || trimmed.contains("veritas page", ignoreCase = true)) return false
        if (trimmed.startsWith("#")) return true
        if (Regex("""^(CHAPTER|Chapter|PROLOGUE|Prologue|EPILOGUE|Epilogue|INTRODUCTION|Introduction|PREFACE|Preface|PART|Part|BOOK|Book|SECTION|Section|ACT|Act|SCENE|Scene)\b.*""", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) return true
        if (Regex("""^\d+(\.\d+)*\s+[A-Z0-9].*""").containsMatchIn(trimmed)) return true
        val letters = trimmed.filter { it.isLetter() }
        if (letters.length in 4..65) {
            val upperRatio = letters.count { it.isUpperCase() }.toFloat() / letters.length
            if (upperRatio >= 0.70f) return true
        }
        if (isTitleCasedSubheading(trimmed)) return true
        return false
    }

    private fun isTitleCasedSubheading(trimmed: String): Boolean {
        if (trimmed.length !in 3..55) return false
        if (trimmed.startsWith("\"") || trimmed.startsWith("“") || trimmed.startsWith("‘") || trimmed.startsWith("—") || trimmed.startsWith("-")) return false
        if (trimmed.endsWith(",") || trimmed.endsWith(";") || trimmed.endsWith("-") || trimmed.endsWith(":")) return false
        if (trimmed.endsWith(".") && !Regex("""^(CHAPTER|Chapter|Part|Section)?\s*[IVXLCDM\d]+(\.[IVXLCDM\d]+)*\.$""", RegexOption.IGNORE_CASE).matches(trimmed)) {
            return false
        }
        val words = trimmed.split(Regex("""\s+""")).filter { it.isNotBlank() }
        if (words.isEmpty() || words.size > 8) return false
        val minorWords = setOf("a", "an", "the", "and", "but", "or", "for", "nor", "on", "at", "to", "by", "with", "in", "of", "vs", "vs.", "v", "v.")
        val significantWords = words.filter { it.lowercase(Locale.getDefault()) !in minorWords }
        if (significantWords.isEmpty()) return false
        val capitalizedSignificant = significantWords.count { word -> word.firstOrNull()?.isUpperCase() == true }
        return capitalizedSignificant == significantWords.size
    }

    private fun looksLikeNumberedItemStart(line: String): Boolean {
        return Regex("""^\s*\d{1,3}[.)]\s+\S+""").containsMatchIn(line)
    }

    private fun normalizedLineKey(line: String): String {
        return line.lowercase(Locale.getDefault())
            .replace(Regex("\\d+"), "#")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isStandalonePageNumber(line: String): Boolean {
        val trimmed = line.trim()
        return Regex("""^[-–—]?\s*\d{1,4}\s*[-–—]?$""").matches(trimmed) ||
            Regex("""^(page|p\.)\s*\d{1,4}(\s*(of|/)\s*\d{1,4})?$""", RegexOption.IGNORE_CASE).matches(trimmed)
    }
}

fun String.normalizeExtractedText(): String {
    return replace('\u00A0', ' ')
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')
        .joinToString("\n") { line ->
            val expanded = line.replace("\t", "    ")
            val leadingSpaces = expanded.takeWhile { it == ' ' }.length.coerceAtMost(16)
            val leadingIndent = " ".repeat(leadingSpaces)
            val body = expanded
                .drop(leadingSpaces)
                .replace(Regex(" {3,}"), "  ")
                .trimEnd()
            if (body.isBlank()) {
                ""
            } else {
                "$leadingIndent${body.trimStart()}"
            }
        }
        .replace(Regex("\n{4,}"), "\n\n\n")
        .trim()
}

fun String.smartFormatPdfContent(): String {
    val lines = this.split('\n')
    val formatted = mutableListOf<String>()
    
    lines.forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            if (formatted.isNotEmpty() && formatted.last().isNotBlank()) {
                formatted.add("")
            }
            return@forEach
        }
        
        if (trimmed.startsWith("[[VERITAS_") || trimmed.contains("VERITAS_PAGE", ignoreCase = true) || trimmed.contains("veritas page", ignoreCase = true)) {
            formatted.add(trimmed)
            return@forEach
        }
        
        val isExplicitHeader = trimmed.startsWith("#")
        val isChapterOrTopic = Regex("""^(CHAPTER|Chapter|PROLOGUE|Prologue|EPILOGUE|Epilogue|INTRODUCTION|Introduction|PREFACE|Preface|PART|Part|BOOK|Book|SECTION|Section|ACT|Act|SCENE|Scene)\b.*""", RegexOption.IGNORE_CASE).matches(trimmed) ||
            Regex("""^\d+(\.\d+)*\s+[A-Z0-9].*""").matches(trimmed) ||
            (trimmed.length in 4..65 && trimmed.filter { it.isLetter() }.length >= 4 && trimmed.filter { it.isLetter() }.all { it.isUpperCase() }) ||
            (trimmed.length in 3..55 && !trimmed.endsWith(".") && !trimmed.endsWith(",") && trimmed.split(Regex("""\s+""")).all { w -> w.isEmpty() || w.first().isUpperCase() || w.lowercase(Locale.getDefault()) in setOf("a", "an", "the", "and", "or", "in", "on", "of", "vs", "vs.") })
            
        if (isChapterOrTopic && !isExplicitHeader) {
            if (formatted.isNotEmpty() && formatted.last().isNotBlank()) {
                formatted.add("")
            }
            formatted.add("## $trimmed")
            formatted.add("")
        } else {
            formatted.add(line.trimEnd())
        }
    }
    
    return formatted.joinToString("\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

/**
 * Turns a raw file name into something worth showing as a title.
 *
 * Downloads and shares arrive as things like
 * "1741927936_Good_Vibes,_Good_Life_(Vex_King)_(1).pdf", and that string was
 * being used verbatim as the reading's title everywhere in the app.
 *
 * Drops the extension, a leading epoch/id prefix, a trailing copy counter, and
 * separator underscores/dots. Deliberately conservative: anything it cannot
 * confidently improve is returned unchanged, and it never returns blank.
 */
fun cleanDocumentTitle(fileName: String): String {
    val withoutExtension = fileName.trim().let { name ->
        val dot = name.lastIndexOf('.')
        // Only strip a plausible extension, not the dot in "Vol. 2".
        if (dot > 0 && name.length - dot in 2..6 && name.drop(dot + 1).all { it.isLetterOrDigit() }) {
            name.take(dot)
        } else {
            name
        }
    }

    var title = withoutExtension
        // "1741927936_Good_Vibes" / "20260802-notes" — a long digit run up front is
        // a timestamp or export id, never part of the title.
        .replace(Regex("^\\d{6,}[_\\-\\s.]+"), "")
        // OceanofPDF download prefixes/suffixes/tags with any wrapping slashes, brackets, or delimiters
        .replace(Regex("""(?i)[/_(\[]?OceanofPDF(\.com)?[/_\])]?"""), "")
        .replace(Regex("""(?i)\bOceanofPDF(\.com)?\b"""), "")
        .replace(Regex("""(?i)\[(PDF|EPUB)\]"""), "")
        .replace(Regex("""(?i)\((PDF|EPUB)\)"""), "")
        .replace(Regex("""(?i)\bDownload\b"""), "")
        // "report (1)" / "report(2)" / "report - Copy" — download de-duplication.
        .replace(Regex("\\s*[_\\-]?\\(\\d+\\)$"), "")
        .replace(Regex("\\s*-\\s*Copy$", RegexOption.IGNORE_CASE), "")

    // Underscores are separators in file names but never in prose.
    if (title.contains('_')) title = title.replace('_', ' ')
    // Same for dot-separated names, but only when there are no spaces already.
    if (!title.contains(' ') && title.count { it == '.' } >= 2) title = title.replace('.', ' ')

    title = title.replace(Regex("\\s{2,}"), " ")
        .trim()
        .trim('-', '_', '.', ' ', '/', '\\', ':', '|', '•')

    if (title.equals("Fetching Resource", ignoreCase = true) ||
        title.equals("Fetching Resource...", ignoreCase = true) ||
        title.isBlank()) {
        return "Imported document"
    }

    return title
}

fun getDisplayName(context: Context, uri: Uri): String {
    val fallback = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported text"
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                // Cursor.getString is a platform type and IS null for some share
                // providers (crash report 2026-07-05: NPE in prepareImport when a
                // file was shared into Veritas). Never let that null escape.
                if (index >= 0) cursor.getString(index) ?: fallback else fallback
            } else {
                fallback
            }
        }
    }.getOrDefault(fallback)
}
