package com.veritas.reader

import android.content.Context
import android.net.Uri
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.PDFTextStripperByArea
import org.apache.pdfbox.text.TextPosition
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipInputStream
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield
import java.awt.geom.Rectangle2D

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
    val pageHeight: Float
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
            val file = File(uri.path ?: uri.toString())
            PDDocument.load(file).use { document ->
                document.numberOfPages
            }
        }.getOrDefault(0)
    }

    fun openPdfDocument(context: Context, uri: Uri): Pair<PDDocument, Int> {
        val file = File(uri.path ?: uri.toString())
        if (!file.exists()) {
            throw IllegalStateException("Cannot open PDF: file does not exist at $uri")
        }
        val document = PDDocument.load(file)
        return document to document.numberOfPages
    }

    fun applyCropRect(document: PDDocument, cropRect: android.graphics.RectF) {
        for (page in document.pages) {
            val mediaBox = page.mediaBox
            val cropX = mediaBox.lowerLeftX + cropRect.left * mediaBox.width
            val cropY = mediaBox.lowerLeftY + (1f - cropRect.bottom) * mediaBox.height
            val cropW = (cropRect.right - cropRect.left) * mediaBox.width
            val cropH = (cropRect.bottom - cropRect.top) * mediaBox.height
            page.cropBox = org.apache.pdfbox.pdmodel.common.PDRectangle(cropX, cropY, cropW, cropH)
        }
    }

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

        if (normalizedOptions.forceOcr) {
            diagnostics.add("OCR is not supported on desktop. Standard text extraction will be used.")
        }

        val pageNumbers = mutableListOf<Int>()
        val pageTexts = mutableListOf<String>()
        val cacheKey = "pdf_" + uri.toString().hashCode().toString()
        val cacheDir = java.io.File(System.getProperty("java.io.tmpdir"), cacheKey)
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

        val cleaned = PdfTextCleaner.cleanPages(pageTexts, pageNumbers, normalizedOptions)
        runCatching { cacheDir.deleteRecursively() }

        return ExtractedImport(
            title = displayName.ifBlank { "Imported document" },
            text = cleaned.text,
            sourceLabel = "PDF",
            note = diagnostics.joinToString("\n").ifBlank { null },
            pageCount = selectedPageCount,
            partial = false
        )
    }

    suspend fun extract(
        context: Context,
        uri: Uri,
        displayName: String,
        pdfOptions: PdfImportOptions = PdfImportOptions(),
        textOptions: TextImportOptions = TextImportOptions(),
        foregroundBudgetMillis: Long? = DEFAULT_IMPORT_TIMEOUT_MS
    ): ExtractedImport {
        val mimeType = context.contentResolver.getType(uri).orEmpty().lowercase()
        val extension = displayName.substringAfterLast('.', "").lowercase()
        val isPdf = mimeType.contains("pdf") || extension == "pdf" || uri.path?.lowercase()?.endsWith(".pdf") == true
        val isDocx = mimeType.contains("officedocument") || extension == "docx"
        val isEpub = mimeType.contains("epub") || extension == "epub"

        if (isPdf) {
            val body = extractPdf(context, uri, pdfOptions, foregroundBudgetMillis)
            return ExtractedImport(
                title = displayName,
                text = body.text,
                sourceLabel = "PDF",
                note = body.diagnostics.joinToString("\n").ifBlank { null },
                pageCount = body.pageCount,
                partial = body.partial
            )
        }

        val bytes = readAllBytes(context, uri)
        val text = when {
            isDocx -> extractDocx(bytes)
            isEpub -> extractEpub(bytes)
            else -> extractPlainText(context, uri, textOptions, isHtmlish(displayName) || mimeType.contains("html")).text
        }

        return ExtractedImport(
            title = displayName,
            text = text,
            sourceLabel = when {
                isDocx -> "Word Document"
                isEpub -> "EPUB eBook"
                isHtmlish(displayName) || mimeType.contains("html") -> "Web Page"
                else -> "Text Document"
            },
            pageCount = 0,
            partial = false
        )
    }

    private suspend fun extractPdf(
        context: Context,
        uri: Uri,
        options: PdfImportOptions = PdfImportOptions(),
        foregroundBudgetMillis: Long? = DEFAULT_IMPORT_TIMEOUT_MS
    ): ExtractionBody {
        val file = File(uri.path ?: uri.toString())
        if (!file.exists()) {
            return ExtractionBody("")
        }
        val document = PDDocument.load(file)
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
            if (normalizedOptions.cropRect != null) {
                diagnostics.add("Manual crop applied to the extraction area.")
                val r = normalizedOptions.cropRect
                applyCropRect(document, r)
            }

            val deadlineMillis = foregroundBudgetMillis?.let { System.currentTimeMillis() + it }
            val pageNumbers = mutableListOf<Int>()
            val pageTexts = mutableListOf<String>()
            var partial = false
            val cacheKey = "pdf_" + uri.toString().hashCode().toString()
            val cacheDir = java.io.File(System.getProperty("java.io.tmpdir"), cacheKey)
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

            val cleaned = PdfTextCleaner.cleanPages(pageTexts, pageNumbers, normalizedOptions)
            if (partial) {
                diagnostics.add("Opened ${pageTexts.size} of $selectedPageCount selected PDF pages after the foreground import window. Veritas will continue extracting the rest in the background.")
            } else {
                runCatching { cacheDir.deleteRecursively() }
            }
            if (normalizedOptions.cleanupRepeatedLines && cleaned.removedRepeatedLineCount > 0) {
                diagnostics.add("Removed ${cleaned.removedRepeatedLineCount} repeated header/footer lines.")
            }
            if (normalizedOptions.removePageNumbers && cleaned.removedPageNumberCount > 0) {
                diagnostics.add("Removed ${cleaned.removedPageNumberCount} standalone page numbers.")
            }
            if (normalizedOptions.repairHyphenation && cleaned.joinedHyphenationCount > 0) {
                diagnostics.add("Joined ${cleaned.joinedHyphenationCount} hyphenated line breaks.")
            }

            ExtractionBody(cleaned.text, diagnostics, pageCount = selectedPageCount, partial = partial)
        } finally {
            document.close()
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
        val opfDir = opfPath.substringBeforeLast('/', "").let { if (it.isEmpty()) "" else "$it/" }
        val manifest = mutableMapOf<String, String>()
        Regex("""<item\b[^>]*>""", RegexOption.IGNORE_CASE).findAll(opfXml).forEach { item ->
            val tag = item.value
            val id = parseAttributes(tag)["id"].orEmpty()
            val href = parseAttributes(tag)["href"].orEmpty()
            if (id.isNotBlank() && href.isNotBlank()) {
                manifest[id] = URLDecoder.decode(href, "UTF-8")
            }
        }

        val spine = mutableListOf<String>()
        Regex("""<itemref\b[^>]*>""", RegexOption.IGNORE_CASE).findAll(opfXml).forEach { itemref ->
            val tag = itemref.value
            val idref = parseAttributes(tag)["idref"].orEmpty()
            manifest[idref]?.let { href ->
                val resolvedPath = resolveZipPath(opfDir, href)
                spine.add(resolvedPath)
            }
        }
        return spine
    }

    private fun parseAttributes(tag: String): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        val matches = Regex("""(\b[a-zA-Z0-9:-]+)\s*=\s*["']([^"']*)["']""").findAll(tag)
        for (m in matches) {
            attributes[m.groupValues[1]] = m.groupValues[2]
        }
        return attributes
    }

    private fun resolveZipPath(baseDir: String, href: String): String {
        val combined = baseDir + href
        val parts = combined.split('/')
        val resolved = mutableListOf<String>()
        for (p in parts) {
            when (p) {
                "." -> {}
                ".." -> if (resolved.isNotEmpty()) resolved.removeAt(resolved.lastIndex)
                else -> resolved.add(p)
            }
        }
        return resolved.joinToString("/")
    }

    private fun isHtmlish(path: String): Boolean {
        val lower = path.lowercase(Locale.getDefault())
        return lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".xhtml")
    }

    private fun htmlishToText(html: String): String {
        val withoutNoise = html.replace(Regex("(?s)<!--.*?-->"), "")
            .replace(Regex("(?s)<style.*?>.*?</style>"), "")
            .replace(Regex("(?s)<script.*?>.*?</script>"), "")
        return decodeHtmlEntities(withoutNoise)
    }

    private fun decodeHtmlEntities(text: String): String {
        var result = text.replace(Regex("<[^>]+>"), "")
        val entities = mapOf(
            "&nbsp;" to " ", "&lt;" to "<", "&gt;" to ">", "&amp;" to "&",
            "&quot;" to "\"", "&apos;" to "'", "&cent;" to "¢", "&pound;" to "£",
            "&yen;" to "¥", "&euro;" to "€", "&copy;" to "©", "&reg;" to "®"
        )
        for ((entity, value) in entities) {
            result = result.replace(entity, value)
        }
        result = Regex("&#(\\d+);").replace(result) { match ->
            match.groupValues[1].toInt().toChar().toString()
        }
        result = Regex("&#x([0-9a-fA-F]+);").replace(result) { match ->
            match.groupValues[1].toInt(16).toChar().toString()
        }
        return result
    }

    private fun readAllBytes(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    }

    private fun xmlBodyToText(xml: String): String {
        val sb = StringBuilder()
        val matcher = java.util.regex.Pattern.compile("<w:t[^>]*>(.*?)</w:t>|<w:p[^>]*>").matcher(xml)
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                sb.append(matcher.group(1))
            } else {
                sb.append("\n")
            }
        }
        return sb.toString().trim()
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
                output.append(line)
                previousWasHeading = currentIsHeading
                return@forEach
            }

            when {
                currentIsHeading || currentIsNumberedItem || previousWasHeading -> {
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
        val parts = Regex("""\s{4,}""")
            .split(trimmed)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        return if (parts.size > 1 && parts.all { it.length >= 2 }) parts else listOf(trimmed)
    }

    private fun looksLikeHeading(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.length > 90) return false
        if (Regex("""^\d+(\.\d+)*\s+\S+""").containsMatchIn(trimmed)) return true
        val letters = trimmed.filter { it.isLetter() }
        if (letters.length < 4) return false
        val upperRatio = letters.count { it.isUpperCase() }.toFloat() / letters.length
        return upperRatio > 0.75f
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
        detectColumnsFromSegments(probe.segments, pageWidth, pageHeight)?.let { return it }
        val usefulLines = probe.lines
            .filter { it.text.length >= 2 && it.width > 8f && it.y in (pageHeight * 0.08f)..(pageHeight * 0.92f) }
            .sortedWith(compareBy<PositionedPdfLine> { it.y }.thenBy { it.minX })
        if (usefulLines.size < 12) return null

        val contentMinX = usefulLines.minOf { it.minX }
        val contentMaxX = usefulLines.maxOf { it.maxX }
        val contentWidth = (contentMaxX - contentMinX).coerceAtLeast(1f)
        val midX = contentMinX + contentWidth / 2f
        val fullWidthThreshold = contentWidth * 0.64f
        val columnLines = usefulLines.filterNot { line ->
            line.width >= fullWidthThreshold || (line.minX < midX && line.maxX > midX)
        }
        if (columnLines.size < 10) return null

        val left = columnLines.filter { it.centerX < midX }
        val right = columnLines.filter { it.centerX >= midX }
        if (left.size < 5 || right.size < 5) return null

        val leftMaxX = left.maxOf { it.maxX }
        val rightMinX = right.minOf { it.minX }
        val gutter = rightMinX - leftMaxX
        if (gutter < maxOf(12f, contentWidth * 0.02f)) return null

        val leftTop = left.minOf { it.y }
        val rightTop = right.minOf { it.y }
        val leftBottom = left.maxOf { it.y }
        val rightBottom = right.maxOf { it.y }
        val overlapTop = maxOf(leftTop, rightTop)
        val overlapBottom = minOf(leftBottom, rightBottom)
        val overlapHeight = overlapBottom - overlapTop
        val columnHeight = (maxOf(leftBottom, rightBottom) - minOf(leftTop, rightTop)).coerceAtLeast(1f)
        if (overlapHeight < columnHeight * 0.42f) return null

        val averageLineHeight = columnLines.map { it.height.toDouble() }.average()
            .takeIf { !it.isNaN() }
            ?.toFloat()
            ?: 10f
        val columnTopY = (minOf(leftTop, rightTop) - averageLineHeight).coerceIn(0f, pageHeight)
        val columnBottomY = (maxOf(leftBottom, rightBottom) + averageLineHeight * 2f).coerceIn(columnTopY, pageHeight)
        return PdfColumnLayout(
            splitX = ((leftMaxX + rightMinX) / 2f).coerceIn(1f, pageWidth - 1f),
            columnTopY = columnTopY,
            columnBottomY = columnBottomY,
            pageWidth = pageWidth,
            pageHeight = pageHeight
        )
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
        val top = Rectangle2D.Float(0f, 0f, layout.pageWidth, layout.columnTopY)
        val left = Rectangle2D.Float(0f, layout.columnTopY, layout.splitX, layout.columnBottomY - layout.columnTopY)
        val right = Rectangle2D.Float(layout.splitX, layout.columnTopY, layout.pageWidth - layout.splitX, layout.columnBottomY - layout.columnTopY)
        val bottom = Rectangle2D.Float(0f, layout.columnBottomY, layout.pageWidth, layout.pageHeight - layout.columnBottomY)
        val stripper = PDFTextStripperByArea().apply {
            sortByPosition = true
            if (top.height > 4f) addRegion("top", top)
            addRegion("left", left)
            addRegion("right", right)
            if (bottom.height > 4f) addRegion("bottom", bottom)
        }
        stripper.extractRegions(page)
        fun regionText(name: String): String {
            return if (name in stripper.regions) cleanRegionText(stripper.getTextForRegion(name)) else ""
        }
        val topText = regionText("top")
        val leftText = regionText("left")
        val rightText = regionText("right")
        val bottomText = regionText("bottom")
        val middleText = if (looksLikeDuplicateColumnText(leftText, rightText)) leftText else {
            listOf(leftText, rightText).filter { it.isNotBlank() }.joinToString("\n\n")
        }
        return listOf(topText, middleText, bottomText)
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
            .trim()
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
                ?: 10f

            val segments = mutableListOf<PositionedPdfSegment>()
            var currentSegment = mutableListOf<PositionedPdfGlyph>()

            for (glyph in visibleGlyphs) {
                if (currentSegment.isEmpty()) {
                    currentSegment.add(glyph)
                } else {
                    val last = currentSegment.last()
                    val verticalGap = Math.abs(glyph.y - last.y)
                    val horizontalGap = glyph.minX - last.maxX
                    if (verticalGap < averageHeight * 0.3f && horizontalGap < averageHeight * 1.5f) {
                        currentSegment.add(glyph)
                    } else {
                        segments.add(createSegment(currentSegment))
                        currentSegment = mutableListOf(glyph)
                    }
                }
            }
            if (currentSegment.isNotEmpty()) {
                segments.add(createSegment(currentSegment))
            }
            return segments
        }

        private fun createSegment(glyphs: List<PositionedPdfGlyph>): PositionedPdfSegment {
            val minX = glyphs.minOf { it.minX }
            val maxX = glyphs.maxOf { it.maxX }
            val y = glyphs.map { it.y.toDouble() }.average().toFloat()
            val height = glyphs.map { it.height.toDouble() }.average().toFloat()
            return PositionedPdfSegment(minX, maxX, y, height, glyphs.size)
        }
    }
}

fun getDisplayName(context: Context, uri: Uri): String {
    val pathStr = uri.path ?: uri.toString()
    val file = File(pathStr)
    return file.name.ifBlank { "document.pdf" }
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
    var inQuoteBlock = false
    var previousWasHeading = false

    lines.forEach { line ->
        val trimmed = line.trim()

        val isQuote = trimmed.startsWith("\"") || trimmed.startsWith("\u201c")
        if (isQuote && !inQuoteBlock && formatted.isNotEmpty()) {
            formatted.add("")
            inQuoteBlock = true
        } else if (!isQuote && inQuoteBlock) {
            inQuoteBlock = false
        }

        if (previousWasHeading && trimmed.isNotEmpty() && !trimmed.startsWith("\"") && !trimmed.startsWith("\u201c")) {
            formatted.add("")
            previousWasHeading = false
        }

        formatted.add(line.trimEnd())

        previousWasHeading = trimmed.length < 100 &&
            (trimmed == trimmed.uppercase() || Regex("""^\d+\.\s+[A-Z]""").containsMatchIn(trimmed))
    }

    return formatted.joinToString("\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}
