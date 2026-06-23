package com.veritas.reader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class WebArticle(
    val title: String,
    val text: String,
    val url: String
)

object WebArticleExtractor {
    suspend fun extract(urlText: String): WebArticle = withContext(Dispatchers.IO) {
        val normalizedUrl = normalizeUrl(urlText)
        val connection = (URL(normalizedUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 16000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "VeritasReaderAndroid/1.0")
        }
        val html = try {
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
        val title = extractTitle(html).ifBlank { URL(normalizedUrl).host.removePrefix("www.") }
        val articleHtml = extractArticleHtml(html)
        val text = htmlToText(articleHtml).ifBlank { htmlToText(html) }
        if (text.isBlank()) throw IllegalArgumentException("No readable article text was found at this link.")
        WebArticle(title = title, text = text, url = normalizedUrl)
    }

    fun looksLikeUrl(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            Regex("^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}([/?#].*)?$").matches(trimmed)
    }

    private fun normalizeUrl(text: String): String {
        val trimmed = text.trim()
        require(looksLikeUrl(trimmed)) { "Enter a valid web article link." }
        return if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun extractTitle(html: String): String {
        return Regex("(?is)<title[^>]*>(.*?)</title>")
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?.let(::decodeEntities)
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()
    }

    private fun extractArticleHtml(html: String): String {
        val article = Regex("(?is)<article[^>]*>(.*?)</article>").find(html)?.value
        if (!article.isNullOrBlank()) return article
        val main = Regex("(?is)<main[^>]*>(.*?)</main>").find(html)?.value
        return main ?: html
    }

    private fun htmlToText(html: String): String {
        return html
            .replace(Regex("(?is)<!--.*?-->"), " ")
            .replace(Regex("(?is)<script[^>]*>.*?</script>"), " ")
            .replace(Regex("(?is)<style[^>]*>.*?</style>"), " ")
            .replace(Regex("(?is)<noscript[^>]*>.*?</noscript>"), " ")
            .replace(Regex("(?is)<nav[^>]*>.*?</nav>"), " ")
            .replace(Regex("(?is)<header[^>]*>.*?</header>"), " ")
            .replace(Regex("(?is)<footer[^>]*>.*?</footer>"), " ")
            .replace(Regex("(?is)<aside[^>]*>.*?</aside>"), " ")
            .replace(Regex("(?is)<form[^>]*>.*?</form>"), " ")
            .replace(Regex("(?is)<svg[^>]*>.*?</svg>"), " ")
            .replace(Regex("(?is)<iframe[^>]*>.*?</iframe>"), " ")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</(p|div|section|article|blockquote|li|h[1-6])>"), "\n\n")
            .replace(Regex("<[^>]+>"), " ")
            .let(::decodeEntities)
            .lines()
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter(::isReadableArticleLine)
            .joinToString("\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun isReadableArticleLine(line: String): Boolean {
        if (line.length <= 1) return false
        val lower = line.lowercase(Locale.getDefault())
        if (lower.matches(Regex("^(cookie|privacy|advertisement|subscribe|sign in|sign up|log in|menu|navigation|contents|table of contents).*$"))) {
            return false
        }
        if (line.contains("{{") || line.contains("}}") || line.contains("\\n") || line.contains("\\\"")) return false
        if (Regex("\"(wt|function|params|i)\"\\s*:").containsMatchIn(line)) return false
        if (Regex("[{}\\[\\]<>]{3,}").containsMatchIn(line)) return false
        val letterCount = line.count { it.isLetter() }
        val symbolCount = line.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        if (letterCount < 8 && line.length > 24) return false
        if (symbolCount > letterCount && line.length > 32) return false
        return true
    }

    private fun decodeEntities(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }
}
