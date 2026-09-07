package com.veritas.reader

import java.io.ByteArrayInputStream
import java.net.URLDecoder
import java.util.Locale
import java.util.zip.ZipInputStream

data class EpubChapter(
    val number: Int,
    val title: String,
    val paragraphs: List<String>,
    val images: List<ByteArray> = emptyList()
)

data class EpubBook(
    val title: String,
    val chapters: List<EpubChapter>,
    val totalChapters: Int = chapters.size
)

object EpubDocumentParser {

    fun parse(bytes: ByteArray, defaultTitle: String): EpubBook {
        val entries = linkedMapOf<String, String>()
        val imageEntries = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val normalizedName = entry.name.trimStart('/')
                    val lower = normalizedName.lowercase(Locale.getDefault())
                    val shouldReadText = lower == "meta-inf/container.xml" ||
                            lower.endsWith(".opf") || lower.endsWith(".xhtml") ||
                            lower.endsWith(".html") || lower.endsWith(".htm")
                    val isImage = lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                            lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif")
                    if (shouldReadText) {
                        entries[normalizedName] = zip.readBytes().toString(Charsets.UTF_8)
                    } else if (isImage) {
                        imageEntries[normalizedName] = zip.readBytes()
                    }
                }
            }
        }

        val containerXml = entries.entries.firstOrNull { it.key.equals("META-INF/container.xml", ignoreCase = true) }?.value
        val opfPath = containerXml?.let { findContainerRootFile(it) }
        val opfXml = opfPath?.let { path -> entries[path] ?: entries.entries.firstOrNull { it.key.equals(path, ignoreCase = true) }?.value }

        var bookTitle = defaultTitle
        if (opfXml != null) {
            val titleMatch = Regex("""<dc:title[^>]*>(.*?)</dc:title>""", RegexOption.IGNORE_CASE).find(opfXml)
            if (titleMatch != null && titleMatch.groupValues[1].isNotBlank()) {
                bookTitle = cleanHtmlText(titleMatch.groupValues[1])
            }
        }

        val orderedContentPaths = if (opfPath != null && opfXml != null) {
            spineContentPaths(opfPath, opfXml)
        } else {
            entries.keys.filter { isHtmlish(it) }.sorted()
        }

        val chapters = mutableListOf<EpubChapter>()
        var chapterIndex = 1

        for (path in orderedContentPaths) {
            val rawHtml = entries[path] ?: entries.entries.firstOrNull { it.key.equals(path, ignoreCase = true) }?.value ?: continue
            val baseDir = path.substringBeforeLast('/', missingDelimiterValue = "")
            val (chapterTitle, paragraphs, chapterImages) = extractChapterContent(rawHtml, chapterIndex, baseDir, imageEntries)
            if (paragraphs.isNotEmpty() || chapterImages.isNotEmpty()) {
                chapters.add(
                    EpubChapter(
                        number = chapterIndex,
                        title = chapterTitle,
                        paragraphs = if (paragraphs.isNotEmpty()) paragraphs else listOf("Visual section"),
                        images = chapterImages
                    )
                )
                chapterIndex++
            }
        }

        if (chapters.isEmpty()) {
            chapters.add(
                EpubChapter(
                    number = 1,
                    title = bookTitle,
                    paragraphs = listOf("No readable chapters could be parsed from this EPUB archive.")
                )
            )
        }

        return EpubBook(
            title = bookTitle,
            chapters = chapters,
            totalChapters = chapters.size
        )
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

        val spine = mutableListOf<String>()
        Regex("""<itemref\b[^>]*>""", RegexOption.IGNORE_CASE).findAll(opfXml).forEach { ref ->
            val idref = parseAttributes(ref.value)["idref"].orEmpty()
            val path = manifest[idref]
            if (!path.isNullOrBlank()) {
                spine.add(path)
            }
        }

        return if (spine.isNotEmpty()) spine else manifest.values.toList()
    }

    private fun parseAttributes(tag: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val regex = Regex("""([a-zA-Z0-9_:-]+)\s*=\s*["']([^"']*)["']""")
        regex.findAll(tag).forEach { match ->
            map[match.groupValues[1].lowercase(Locale.getDefault())] = match.groupValues[2]
        }
        return map
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

    private fun extractChapterContent(
        html: String,
        defaultNum: Int,
        baseDir: String = "",
        imageEntries: Map<String, ByteArray> = emptyMap()
    ): Triple<String, List<String>, List<ByteArray>> {
        var chapterTitle = "Chapter $defaultNum"

        val titleMatch = Regex("""<title[^>]*>(.*?)</title>""", RegexOption.IGNORE_CASE).find(html)
            ?: Regex("""<h[1-3][^>]*>(.*?)</h[1-3]>""", RegexOption.IGNORE_CASE).find(html)

        if (titleMatch != null) {
            val text = cleanHtmlText(titleMatch.groupValues[1])
            if (text.isNotBlank() && text.length < 80) {
                chapterTitle = text
            }
        }

        val plainText = cleanHtmlText(html)
        val paragraphs = plainText.split(Regex("""\n\s*\n+"""))
            .map { it.trim().replace(Regex("""\s+"""), " ") }
            .filter { it.isNotBlank() && it != chapterTitle }

        val images = mutableListOf<ByteArray>()
        if (imageEntries.isNotEmpty()) {
            val imgRegex = Regex("""<img\b[^>]*src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            imgRegex.findAll(html).forEach { match ->
                val src = match.groupValues[1]
                val resolved = resolveZipPath(baseDir, src)
                val imgBytes = imageEntries[resolved] ?: imageEntries.entries.firstOrNull { it.key.endsWith(src.substringAfterLast('/'), ignoreCase = true) }?.value
                if (imgBytes != null && imgBytes.isNotEmpty()) {
                    images.add(imgBytes)
                }
            }
        }

        return Triple(chapterTitle, paragraphs, images)
    }

    private fun cleanHtmlText(raw: String): String {
        return raw
            .replace(Regex("(?is)<(script|style|svg|math)[^>]*>.*?</\\1>"), " ")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</(p|div|section|article|blockquote|li|h[1-6]|tr)>"), "\n\n")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .replace("&#39;", "'")
            .trim()
    }
}
