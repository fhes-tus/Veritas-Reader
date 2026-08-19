package com.veritas.reader

import kotlin.math.ceil

data class ReaderPageRange(
    val partIndex: Int,
    val startPage: Int,
    val endPage: Int
) {
    val pageCount: Int
        get() = (endPage - startPage + 1).coerceAtLeast(0)
}

data class ReaderSentence(
    val index: Int,
    val text: String,
    val pageNumber: Int,
    val separatorBefore: String = ""
)

data class ReaderPartSentenceRange(
    val sentenceIndex: Int,
    val start: Int,
    val endExclusive: Int
)

data class ReaderPart(
    val index: Int,
    val pageRange: ReaderPageRange,
    val sentenceStartIndex: Int,
    val sentenceEndIndexExclusive: Int,
    val text: String,
    val sentenceRanges: List<ReaderPartSentenceRange>
) {
    val markerText: String
        get() = "(Part ${index + 1} of ${pageRange.partIndex + 1})"
}

data class ReaderTextModel(
    val sentences: List<ReaderSentence>,
    val parts: List<ReaderPart>,
    val pageCount: Int,
    val cleanText: String
) {
    fun partForSentence(sentenceIndex: Int): ReaderPart? {
        if (parts.isEmpty()) return null
        val safeIndex = sentenceIndex.coerceIn(0, (sentences.size - 1).coerceAtLeast(0))
        return parts.firstOrNull { safeIndex in it.sentenceStartIndex until it.sentenceEndIndexExclusive }
            ?: parts.lastOrNull()
    }

    fun sentenceForPartOffset(part: ReaderPart, offset: Int): ReaderPartSentenceRange? {
        if (part.sentenceRanges.isEmpty()) return null
        val safeOffset = offset.coerceIn(0, part.text.length)
        return part.sentenceRanges.firstOrNull { safeOffset in it.start until it.endExclusive }
            ?: part.sentenceRanges.minByOrNull { range ->
                when {
                    safeOffset < range.start -> range.start - safeOffset
                    else -> safeOffset - range.endExclusive
                }
            }
    }
}

object ReaderPagePartPlanner {
    fun plan(pageCount: Int): List<ReaderPageRange> {
        val safePageCount = pageCount.coerceAtLeast(0)
        if (safePageCount == 0) return emptyList()
        val maxPagesPerPart = 12
        val minPagesPerPart = if (safePageCount > 30) 9 else 6
        var partCount = ceil(safePageCount.toDouble() / maxPagesPerPart.toDouble()).toInt().coerceAtLeast(1)
        while (partCount > 1 && safePageCount / partCount < minPagesPerPart) {
            partCount--
        }
        val baseSize = safePageCount / partCount
        val extra = safePageCount % partCount
        var nextPage = 1
        return List(partCount) { index ->
            val size = baseSize + if (index < extra) 1 else 0
            val start = nextPage
            val end = nextPage + size - 1
            nextPage = end + 1
            ReaderPageRange(index, start, end)
        }
    }
}

object ReaderTextIndex {
    private val pageMarkerRegex = Regex("""^\[\[VERITAS_PAGE:(\d+)]]$""")
    private val sentenceEndMarks = setOf('.', '!', '?')
    private val abbreviations = setOf(
        "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "st", "v", "vs", "etc", "e.g", "i.e",
        "fig", "figs", "no", "nos", "vol", "pp", "p", "ch", "dept", "univ", "inc", "ltd",
        "et al", "al", "ed", "eds", "ref", "refs", "sec", "secs", "para", "paras", "eq", "eqs",
        "app", "approx", "c", "ca", "cf", "ff", "op", "cit", "ibid"
    )

    fun pageMarker(pageNumber: Int): String = "[[VERITAS_PAGE:${pageNumber.coerceAtLeast(1)}]]"

    fun stripInternalMarkers(text: String): String {
        return text.replace('\r', '\n')
            .lineSequence()
            .filterNot { pageMarkerRegex.matches(it.trim()) }
            .joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    fun build(rawText: String, storedPageCount: Int = 0): ReaderTextModel {
        val pages = extractPages(rawText, storedPageCount)
        val cleanText = pages.joinToString("\n\n") { it.text }.trim()
        val sentences = mutableListOf<ReaderSentence>()
        pages.forEach { page ->
            splitSentenceFragments(page.text).forEach { sentence ->
                sentences.add(
                    ReaderSentence(
                        index = sentences.size,
                        text = sentence.text,
                        pageNumber = page.pageNumber,
                        separatorBefore = sentence.separatorBefore
                    )
                )
            }
        }
        val fallbackSentences = if (sentences.isEmpty() && cleanText.isNotBlank()) {
            listOf(ReaderSentence(0, cleanText, 1))
        } else {
            sentences
        }
        val pageCount = pages.maxOfOrNull { it.pageNumber }?.coerceAtLeast(storedPageCount) ?: storedPageCount.coerceAtLeast(1)
        val partRanges = ReaderPagePartPlanner.plan(pageCount.coerceAtLeast(estimatePageCount(cleanText)))
        val parts = buildParts(fallbackSentences, partRanges)
        return ReaderTextModel(
            sentences = fallbackSentences,
            parts = parts,
            pageCount = partRanges.lastOrNull()?.endPage ?: pageCount,
            cleanText = cleanText
        )
    }

    fun sentences(text: String): List<String> = build(text).sentences.map { it.text }

    fun replaceSentenceRange(
        rawText: String,
        storedPageCount: Int,
        startSentenceIndex: Int,
        endSentenceIndexExclusive: Int,
        replacement: String
    ): String {
        val model = build(rawText, storedPageCount)
        if (model.sentences.isEmpty()) return replacement.trim()
        val safeStart = startSentenceIndex.coerceIn(0, model.sentences.lastIndex)
        val safeEnd = endSentenceIndexExclusive.coerceIn(safeStart + 1, model.sentences.size)
        val replacementText = replacement.trim()
        if (replacementText.isBlank()) return rawText

        val replacementPage = model.sentences.getOrNull(safeStart)?.pageNumber ?: 1
        val replacementFragments = splitSentenceFragments(replacementText)
            .ifEmpty { listOf(SentenceFragment(replacementText, "")) }
        val replacementSentences = replacementFragments.mapIndexed { index, fragment ->
            ReaderSentence(
                index = -1,
                text = fragment.text,
                pageNumber = replacementPage,
                separatorBefore = if (index == 0) model.sentences.getOrNull(safeStart)?.separatorBefore.orEmpty() else fragment.separatorBefore
            )
        }

        val edited = buildList {
            addAll(model.sentences.take(safeStart))
            addAll(replacementSentences)
            addAll(model.sentences.drop(safeEnd))
        }
        return rebuildPages(edited, model.pageCount)
    }

    fun replacePart(rawText: String, storedPageCount: Int, partIndex: Int, replacement: String): String {
        val model = build(rawText, storedPageCount)
        val part = model.parts.getOrNull(partIndex) ?: return rawText
        return replaceSentenceRange(
            rawText = rawText,
            storedPageCount = storedPageCount,
            startSentenceIndex = part.sentenceStartIndex,
            endSentenceIndexExclusive = part.sentenceEndIndexExclusive,
            replacement = replacement
        )
    }

    private fun extractPages(rawText: String, storedPageCount: Int): List<PageText> {
        val normalized = rawText.replace('\r', '\n')
        val pages = mutableListOf<PageText>()
        var currentPage = 1
        var sawMarker = false
        val buffer = StringBuilder()

        fun flush() {
            val pageText = buffer.toString().trim()
            if (pageText.isNotBlank()) {
                pages.add(PageText(currentPage, pageText))
            }
            buffer.clear()
        }

        normalized.lineSequence().forEach { line ->
            val marker = pageMarkerRegex.matchEntire(line.trim())
            if (marker != null) {
                flush()
                sawMarker = true
                currentPage = marker.groupValues[1].toIntOrNull()?.coerceAtLeast(1) ?: currentPage
            } else {
                buffer.append(line).append('\n')
            }
        }
        flush()

        if (sawMarker && pages.isNotEmpty()) return pages
        val clean = stripInternalMarkers(normalized)
        if (clean.isBlank()) return emptyList()
        val pageCount = storedPageCount.takeIf { it > 0 } ?: estimatePageCount(clean)
        if (pageCount <= 1) return listOf(PageText(1, clean))

        val targetSize = (clean.length / pageCount).coerceAtLeast(1)
        val result = mutableListOf<PageText>()
        var cursor = 0
        for (page in 1..pageCount) {
            if (cursor >= clean.length) break
            val remainingPages = pageCount - page + 1
            val remainingChars = clean.length - cursor
            val desiredEnd = if (remainingPages == 1) clean.length else cursor + minOf(targetSize, remainingChars)
            val end = nearestSoftBreak(clean, desiredEnd, cursor)
            result.add(PageText(page, clean.substring(cursor, end).trim()))
            cursor = end
        }
        return result.filter { it.text.isNotBlank() }.ifEmpty { listOf(PageText(1, clean)) }
    }

    private fun buildParts(sentences: List<ReaderSentence>, ranges: List<ReaderPageRange>): List<ReaderPart> {
        if (sentences.isEmpty()) return emptyList()
        val effectiveRanges = ranges.ifEmpty { ReaderPagePartPlanner.plan(estimatePageCount(sentences.joinToString(" ") { it.text })) }
        return effectiveRanges.mapIndexedNotNull { index, range ->
            val partSentences = sentences.filter { it.pageNumber in range.startPage..range.endPage }
                .ifEmpty {
                    val startFraction = (index.toFloat() / effectiveRanges.size.toFloat()).coerceIn(0f, 1f)
                    val endFraction = ((index + 1).toFloat() / effectiveRanges.size.toFloat()).coerceIn(0f, 1f)
                    val start = (startFraction * sentences.size).toInt().coerceIn(0, sentences.lastIndex)
                    val endExclusive = (endFraction * sentences.size).toInt().coerceIn(start + 1, sentences.size)
                    sentences.subList(start, endExclusive)
                }
            if (partSentences.isEmpty()) return@mapIndexedNotNull null
            val textBuilder = StringBuilder()
            val rangesInPart = mutableListOf<ReaderPartSentenceRange>()
            partSentences.forEachIndexed { sentenceOffset, sentence ->
                if (textBuilder.isNotBlank()) {
                    textBuilder.append(
                        displaySeparator(
                            rawSeparator = sentence.separatorBefore,
                            previousSentence = partSentences.getOrNull(sentenceOffset - 1),
                            currentSentence = sentence
                        )
                    )
                }
                val start = textBuilder.length
                textBuilder.append(sentence.text)
                rangesInPart.add(ReaderPartSentenceRange(sentence.index, start, textBuilder.length))
            }
            ReaderPart(
                index = index,
                pageRange = range,
                sentenceStartIndex = partSentences.first().index,
                sentenceEndIndexExclusive = partSentences.last().index + 1,
                text = textBuilder.toString().trim(),
                sentenceRanges = rangesInPart
            )
        }
    }

    private fun displaySeparator(
        rawSeparator: String,
        previousSentence: ReaderSentence?,
        currentSentence: ReaderSentence
    ): String {
        val normalized = rawSeparator.replace('\r', '\n')
        return when {
            previousSentence != null && previousSentence.pageNumber != currentSentence.pageNumber -> "\n\n"
            normalized.count { it == '\n' } >= 2 -> "\n\n"
            normalized.contains('\n') -> "\n"
            normalized.contains('\t') -> " "
            normalized.isNotBlank() -> " "
            else -> " "
        }
    }

    private fun splitSentences(source: String): List<String> {
        return splitSentenceFragments(source).map { it.text }
    }

    private fun splitSentenceFragments(source: String): List<SentenceFragment> {
        val text = source.replace('\r', '\n').trim()
        if (text.isBlank()) return emptyList()
        val sentences = mutableListOf<SentenceFragment>()
        var start = 0
        var index = 0
        var previousEnd = 0
        while (index < text.length) {
            val char = text[index]
            val boundary = when {
                char in sentenceEndMarks -> isSentenceBoundary(text, index)
                char == '\n' -> isLineBoundary(text, index)
                else -> false
            }
            if (boundary) {
                previousEnd = addTrimmedSentence(text, start, index + 1, previousEnd, sentences)
                start = index + 1
            }
            index++
        }
        addTrimmedSentence(text, start, text.length, previousEnd, sentences)
        return sentences.ifEmpty { listOf(SentenceFragment(text, "")) }
    }

    private fun rebuildPages(sentences: List<ReaderSentence>, storedPageCount: Int): String {
        if (sentences.isEmpty()) return ""
        val output = StringBuilder()
        val ordered = sentences.groupBy { it.pageNumber.coerceAtLeast(1) }.toSortedMap()
        ordered.forEach { (pageNumber, pageSentences) ->
            if (output.isNotBlank()) output.append("\n\n")
            output.append(pageMarker(pageNumber)).append('\n')
            val pageText = StringBuilder()
            pageSentences.forEachIndexed { index, sentence ->
                if (pageText.isNotBlank()) {
                    pageText.append(
                        displaySeparator(
                            rawSeparator = sentence.separatorBefore,
                            previousSentence = pageSentences.getOrNull(index - 1),
                            currentSentence = sentence
                        )
                    )
                }
                pageText.append(sentence.text.trim())
            }
            output.append(pageText.toString().trim())
        }
        return output.toString().trim()
    }

    private fun addTrimmedSentence(
        text: String,
        rawStart: Int,
        rawEnd: Int,
        previousEnd: Int,
        output: MutableList<SentenceFragment>
    ): Int {
        var start = rawStart.coerceIn(0, text.length)
        var end = rawEnd.coerceIn(start, text.length)
        while (start < end && text[start].isWhitespace()) start++
        while (end > start && text[end - 1].isWhitespace()) end--
        if (end > start) {
            val separator = if (output.isEmpty()) "" else text.substring(previousEnd.coerceIn(0, start), start)
            output.add(SentenceFragment(text.substring(start, end), separator))
            return end
        }
        return previousEnd
    }

    private fun isSentenceBoundary(text: String, markIndex: Int): Boolean {
        val next = text.indexOfFirstAfter(markIndex) { !it.isWhitespace() } ?: return true
        val nextChar = text[next]
        if (!nextChar.isUpperCase() && !nextChar.isDigit() && nextChar !in "\"'`(") return false
        if (markIndex > 0 && markIndex < text.lastIndex && text[markIndex - 1].isDigit() && text[markIndex + 1].isDigit()) return false
        val token = text.substring(0, markIndex)
            .takeLastWhile { it.isLetterOrDigit() || it == '.' }
            .trim('.')
            .lowercase()
        if (token in abbreviations) return false
        if (token.length == 1 && token.firstOrNull()?.isLetter() == true) return false
        // Detect multi-part initials like J.R.R. or U.S.A.
        if (Regex("^([a-z]\\.){2,}[a-z]?$").matches(token)) return false
        return true
    }

    private fun isLineBoundary(text: String, newlineIndex: Int): Boolean {
        val previous = text.lastNonWhitespaceBefore(newlineIndex) ?: return false
        val next = text.firstNonWhitespaceAfter(newlineIndex) ?: return false
        if (text.getOrNull(newlineIndex + 1) == '\n') return true
        if (previous == '-') return false
        if (previous in listOf('.', '!', '?', ':', ';')) return true
        return next.isUpperCase() || next.isDigit() || next in "\"'`(["
    }

    private fun nearestSoftBreak(text: String, desiredEnd: Int, min: Int): Int {
        val safeEnd = desiredEnd.coerceIn(min + 1, text.length)
        val searchStart = (safeEnd - 600).coerceAtLeast(min + 1)
        for (index in safeEnd downTo searchStart) {
            if (text.getOrNull(index) == '\n' && text.getOrNull(index - 1) == '\n') return index
        }
        for (index in safeEnd downTo searchStart) {
            val char = text.getOrNull(index)
            if (char in sentenceEndMarks && isSentenceBoundary(text, index)) {
                return (index + 1).coerceAtMost(text.length)
            }
        }
        // Secondary fallback: find nearest preceding whitespace to avoid splitting mid-word
        for (index in safeEnd downTo searchStart) {
            if (text.getOrNull(index)?.isWhitespace() == true) return index
        }
        return safeEnd
    }

    private fun estimatePageCount(text: String): Int {
        if (text.isBlank()) return 1
        return ceil(text.length.toDouble() / 2200.0).toInt().coerceAtLeast(1)
    }

    private inline fun String.indexOfFirstAfter(start: Int, predicate: (Char) -> Boolean): Int? {
        for (index in (start + 1)..lastIndex) {
            if (predicate(this[index])) return index
        }
        return null
    }

    private fun String.lastNonWhitespaceBefore(index: Int): Char? {
        for (cursor in (index - 1).coerceAtLeast(0) downTo 0) {
            val char = this[cursor]
            if (!char.isWhitespace()) return char
        }
        return null
    }

    private fun String.firstNonWhitespaceAfter(index: Int): Char? {
        for (cursor in (index + 1)..lastIndex) {
            val char = this[cursor]
            if (!char.isWhitespace()) return char
        }
        return null
    }

    private data class PageText(
        val pageNumber: Int,
        val text: String
    )

    private data class SentenceFragment(
        val text: String,
        val separatorBefore: String
    )
}
