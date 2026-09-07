package com.veritas.desktop.parser

import com.veritas.desktop.models.DesktopDocument
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.InputStream
import java.net.URI
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

object TextChunker {
    private val ABBREVIATIONS = setOf(
        "mr.", "mrs.", "ms.", "dr.", "prof.", "sr.", "jr.", "vs.", "etc.",
        "e.g.", "i.e.", "jan.", "feb.", "mar.", "apr.", "jun.", "jul.",
        "aug.", "sep.", "sept.", "oct.", "nov.", "dec.", "vol.", "no.",
        "p.", "pp.", "approx.", "dept.", "est.", "inc.", "ltd.", "co."
    )

    fun chunk(text: String): List<String> {
        if (text.isBlank()) return emptyList()

        val normalized = text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("[ \\t]+"), " ")

        val paragraphs = normalized.split(Regex("\n{2,}"))
        val result = mutableListOf<String>()

        for (paragraph in paragraphs) {
            val trimmedPara = paragraph.trim()
            if (trimmedPara.isEmpty()) continue

            // Split into sentences using punctuation boundaries
            val rawSentences = splitIntoSentences(trimmedPara)
            for (sentence in rawSentences) {
                val clean = sentence.trim()
                if (clean.isNotBlank()) {
                    result.add(clean)
                }
            }
        }

        return if (result.isEmpty() && text.isNotBlank()) listOf(text.trim()) else result
    }

    private fun splitIntoSentences(text: String): List<String> {
        val sentences = mutableListOf<String>()
        val sb = StringBuilder()
        val len = text.length
        var i = 0

        while (i < len) {
            val c = text[i]
            sb.append(c)

            if (c == '.' || c == '!' || c == '?' || c == '…' || c == '։' || c == '。') {
                // Check if this period is an abbreviation or decimal number
                val currentWord = sb.toString().trim().substringAfterLast(' ').lowercase()
                val isAbbrev = ABBREVIATIONS.contains(currentWord)
                val isDecimal = (i + 1 < len && text[i + 1].isDigit()) && (i > 0 && text[i - 1].isDigit())
                val isEllipsis = (i + 1 < len && text[i + 1] == '.') || (i > 0 && text[i - 1] == '.')

                if (!isAbbrev && !isDecimal && !isEllipsis) {
                    // Check if followed by quote, bracket, or whitespace
                    var nextIdx = i + 1
                    while (nextIdx < len && (text[nextIdx] == '"' || text[nextIdx] == '”' || text[nextIdx] == ')' || text[nextIdx] == ']' || text[nextIdx] == '’' || text[nextIdx] == '\'')) {
                        sb.append(text[nextIdx])
                        nextIdx++
                    }
                    i = nextIdx - 1

                    if (nextIdx >= len || text[nextIdx].isWhitespace() || text[nextIdx] == '\n') {
                        sentences.add(sb.toString().trim())
                        sb.clear()
                    }
                }
            }
            i++
        }

        if (sb.isNotBlank()) {
            sentences.add(sb.toString().trim())
        }

        return sentences
    }
}

object DocumentParser {

    fun parseFile(file: File): DesktopDocument {
        val extension = file.extension.lowercase()
        val title = file.nameWithoutExtension.replace('_', ' ')

        val (rawText, sourceLabel) = when (extension) {
            "pdf" -> parsePdf(file) to "PDF Document"
            "epub" -> parseEpub(file) to "EPUB Book"
            "docx" -> parseDocx(file) to "Word Document"
            "md", "markdown" -> parseText(file) to "Markdown Document"
            "html", "htm" -> stripHtml(file.readText(Charsets.UTF_8)) to "Web Page"
            else -> parseText(file) to "Text Document"
        }

        val chunks = TextChunker.chunk(rawText)

        return DesktopDocument(
            title = title,
            sourceLabel = sourceLabel,
            filePath = file.absolutePath,
            rawText = rawText,
            chunks = chunks,
            currentIndex = 0
        )
    }

    fun parseFromRawText(title: String, text: String, sourceLabel: String = "Pasted Text"): DesktopDocument {
        val cleanText = if (text.trim().startsWith("<html", ignoreCase = true) || text.contains("<body", ignoreCase = true)) {
            stripHtml(text)
        } else {
            text
        }
        val chunks = TextChunker.chunk(cleanText)
        return DesktopDocument(
            title = title.ifBlank { "Untitled Document" },
            sourceLabel = sourceLabel,
            rawText = cleanText,
            chunks = chunks,
            currentIndex = 0
        )
    }

    fun parseFromUrl(urlString: String): DesktopDocument {
        val url = URI(urlString).toURL()
        val connection = url.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        val html = connection.getInputStream().bufferedReader(Charsets.UTF_8).use { it.readText() }

        val titleMatch = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE).find(html)
        val extractedTitle = titleMatch?.groupValues?.get(1)?.trim() ?: url.host

        val cleanText = stripHtml(html)
        val chunks = TextChunker.chunk(cleanText)

        return DesktopDocument(
            title = extractedTitle.ifBlank { "Web Article" },
            sourceLabel = "Web Article (${url.host})",
            filePath = urlString,
            rawText = cleanText,
            chunks = chunks,
            currentIndex = 0
        )
    }

    private fun parsePdf(file: File): String {
        return try {
            PDDocument.load(file).use { document ->
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true
                stripper.getText(document)
            }
        } catch (e: Exception) {
            "Could not extract text from PDF: ${e.localizedMessage}"
        }
    }

    private fun parseText(file: File): String {
        return try {
            file.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            try {
                file.readText(Charsets.ISO_8859_1)
            } catch (e2: Exception) {
                ""
            }
        }
    }

    private fun parseDocx(file: File): String {
        return try {
            ZipFile(file).use { zip ->
                val entry = zip.getEntry("word/document.xml") ?: return "Invalid DOCX file."
                zip.getInputStream(entry).use { stream ->
                    extractTextFromDocxXml(stream)
                }
            }
        } catch (e: Exception) {
            "Could not parse DOCX: ${e.localizedMessage}"
        }
    }

    private fun extractTextFromDocxXml(stream: InputStream): String {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(stream)
        val paragraphs = doc.getElementsByTagName("w:p")
        val sb = StringBuilder()

        for (i in 0 until paragraphs.length) {
            val p = paragraphs.item(i)
            val textNodes = (p as? org.w3c.dom.Element)?.getElementsByTagName("w:t") ?: continue
            val paraBuilder = StringBuilder()
            for (j in 0 until textNodes.length) {
                paraBuilder.append(textNodes.item(j).textContent)
            }
            val line = paraBuilder.toString().trim()
            if (line.isNotEmpty()) {
                sb.append(line).append("\n\n")
            }
        }
        return sb.toString()
    }

    private fun parseEpub(file: File): String {
        return try {
            ZipFile(file).use { zip ->
                val entries = zip.entries().asSequence()
                    .filter { it.name.endsWith(".xhtml", ignoreCase = true) || it.name.endsWith(".html", ignoreCase = true) }
                    .sortedBy { it.name }
                    .toList()

                val sb = StringBuilder()
                for (entry in entries) {
                    zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).use { reader ->
                        val htmlContent = reader.readText()
                        sb.append(stripHtml(htmlContent)).append("\n\n")
                    }
                }
                sb.toString()
            }
        } catch (e: Exception) {
            "Could not parse EPUB: ${e.localizedMessage}"
        }
    }

    fun stripHtml(html: String): String {
        var clean = html
            .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<!--[\\s\\S]*?-->"), " ")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("</div>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</li>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")

        clean = clean.replace(Regex("[ \\t]+"), " ").replace(Regex("\n{3,}"), "\n\n")
        return clean.trim()
    }
}
