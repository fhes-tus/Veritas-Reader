package com.veritas.reader.tts

data class SentenceSpan(
    val text: String,
    val startCharIndex: Int,
    val endCharIndex: Int
)

object SentenceSplitter {
    private val ABBREVIATIONS = setOf(
        "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "st", "vs", "eg", "ie", "etc",
        "vol", "no", "fig", "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep",
        "oct", "nov", "dec", "approx", "dept", "co", "corp", "inc", "ltd", "ave",
        "rd", "blvd", "capt", "col", "gen", "lt", "sgt", "rev", "hon"
    )

    /**
     * Splits text into individual sentences along sentence boundary punctuation (. ! ? \n),
     * preserving character offsets relative to the original string.
     */
    fun splitIntoSentences(fullText: String): List<SentenceSpan> {
        if (fullText.isBlank()) return emptyList()

        val spans = mutableListOf<SentenceSpan>()
        val len = fullText.length
        var start = 0
        var i = 0

        while (i < len) {
            val ch = fullText[i]

            if (ch == '\n' || ch == '\r') {
                // Newline boundary
                val candidate = fullText.substring(start, i).trim()
                if (candidate.isNotEmpty()) {
                    spans.add(SentenceSpan(candidate, start, i))
                }
                while (i < len && (fullText[i] == '\n' || fullText[i] == '\r' || fullText[i] == ' ')) {
                    i++
                }
                start = i
                continue
            }

            if (ch == '.' || ch == '!' || ch == '?') {
                // Check if '.' is part of a known abbreviation or decimal number (e.g. 3.14 or Dr. Smith)
                val isAbbrev = if (ch == '.') {
                    val wordBefore = getWordBeforeIndex(fullText, i)
                    wordBefore.lowercase() in ABBREVIATIONS || isDecimalNumber(fullText, i)
                } else false

                if (!isAbbrev) {
                    // Check if followed by whitespace or end of text
                    val nextIdx = i + 1
                    if (nextIdx >= len || fullText[nextIdx].isWhitespace() || fullText[nextIdx] == '"' || fullText[nextIdx] == '”') {
                        // Advance past closing quotes/brackets if any
                        var endBoundary = nextIdx
                        while (endBoundary < len && (fullText[endBoundary] == '"' || fullText[endBoundary] == '”' || fullText[endBoundary] == '\'')) {
                            endBoundary++
                        }
                        val candidate = fullText.substring(start, endBoundary).trim()
                        if (candidate.isNotEmpty()) {
                            spans.add(SentenceSpan(candidate, start, endBoundary))
                        }
                        i = endBoundary
                        while (i < len && fullText[i].isWhitespace()) {
                            i++
                        }
                        start = i
                        continue
                    }
                }
            }
            i++
        }

        if (start < len) {
            val remaining = fullText.substring(start).trim()
            if (remaining.isNotEmpty()) {
                spans.add(SentenceSpan(remaining, start, len))
            }
        }

        return spans
    }

    private fun getWordBeforeIndex(text: String, dotIndex: Int): String {
        var idx = dotIndex - 1
        while (idx >= 0 && text[idx].isLetter()) {
            idx--
        }
        return text.substring(idx + 1, dotIndex)
    }

    private fun isDecimalNumber(text: String, dotIndex: Int): Boolean {
        val prevDigit = dotIndex > 0 && text[dotIndex - 1].isDigit()
        val nextDigit = dotIndex + 1 < text.length && text[dotIndex + 1].isDigit()
        return prevDigit && nextDigit
    }
}
