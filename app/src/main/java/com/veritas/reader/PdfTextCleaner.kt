package com.veritas.reader

import java.util.Locale

data class PdfCleanupResult(
    val text: String,
    val removedRepeatedLineCount: Int,
    val removedPageNumberCount: Int,
    val joinedHyphenationCount: Int
)

object PdfTextCleaner {
    fun cleanPages(pageTexts: List<String>, pageNumbers: List<Int> = pageTexts.indices.map { it + 1 }, options: PdfImportOptions = PdfImportOptions()): PdfCleanupResult {
        val pageLines = pageTexts.map { page ->
            page.replace('\r', '\n')
                .split('\n')
                .map { it.trim() }
        }

        val repeatedKeys = if (options.cleanupRepeatedLines) findRepeatedHeaderFooterKeys(pageLines) else emptySet()
        var removedRepeated = 0
        var removedPageNumbers = 0
        var joinedHyphenations = 0
        val documentOutput = StringBuilder()

        pageLines.forEachIndexed { pageIndex, lines ->
            val cleanedLines = mutableListOf<String>()
            lines.forEach { line ->
                val cleanLine = line.replace(Regex("""\s+"""), " ").trim()
                val key = normalizedLineKey(cleanLine)
                when {
                    options.cleanupRepeatedLines && key in repeatedKeys -> removedRepeated++
                    options.removePageNumbers && isStandalonePageNumber(cleanLine) -> removedPageNumbers++
                    else -> cleanedLines.add(cleanLine)
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
        var previousWasTable = false
        lines.forEachIndexed { index, originalLine ->
            val line = originalLine.trim()
            if (line.isBlank()) {
                if (output.isNotEmpty() && !output.endsWith("\n\n")) {
                    output.append("\n\n")
                }
                previousWasHeading = false
                previousWasTable = false
                return@forEachIndexed
            }
            val currentIsTable = isPipeTableLine(line)
            val prevLine = lines.getOrNull(index - 1)
            val nextLine = lines.getOrNull(index + 1)
            val currentIsHeading = !currentIsTable && looksLikeHeading(line, prevLine, nextLine)
            val currentIsNumberedItem = looksLikeNumberedItemStart(line)

            if (!currentIsTable && repairHyphenation && output.endsWith("-") && line.firstOrNull()?.isLowerCase() == true) {
                output.deleteCharAt(output.length - 1)
                output.append(line)
                previousWasHeading = false
                previousWasTable = false
                onHyphenationJoined()
                return@forEachIndexed
            }

            if (output.isBlank()) {
                if (currentIsHeading && !line.startsWith("#")) {
                    output.append("# ").append(line)
                } else {
                    output.append(line)
                }
                previousWasHeading = currentIsHeading
                previousWasTable = currentIsTable
                return@forEachIndexed
            }

            when {
                output.endsWith("\n\n") -> {
                    if (currentIsHeading && !line.startsWith("#")) {
                        output.append("# ")
                    }
                }
                currentIsTable -> {
                    if (previousWasTable) {
                        output.append("\n")
                    } else {
                        output.append("\n\n")
                    }
                }
                previousWasTable -> {
                    output.append("\n\n")
                    if (currentIsHeading && !line.startsWith("#")) {
                        output.append("# ")
                    }
                }
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
            previousWasTable = currentIsTable
        }
        return output.toString()
    }

    private fun isPipeTableLine(line: String): Boolean {
        return line.startsWith("|") && line.endsWith("|") && line.length > 2
    }



    private fun looksLikeHeading(line: String, prevLine: String? = null, nextLine: String? = null): Boolean {
        val trimmed = line.trim()
        if (trimmed.length > 90 || trimmed.isEmpty()) return false
        if (trimmed.startsWith("[[VERITAS_") || trimmed.contains("VERITAS_PAGE", ignoreCase = true) || trimmed.contains("veritas page", ignoreCase = true)) return false
        if (trimmed.startsWith("#")) return true

        val isExplicitChapterOrSection = Regex(
            """^(CHAPTER|Chapter|PROLOGUE|Prologue|EPILOGUE|Epilogue|INTRODUCTION|Introduction|PREFACE|Preface|PART|Part|BOOK|Book|SECTION|Section|ACT|Act|SCENE|Scene)\b.*""",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(trimmed)
        val isNumberedHeading = Regex("""^\d+(\.\d+)*\s+[A-Z0-9].*""").containsMatchIn(trimmed)

        // Check whether previous line was an unfinished sentence
        val prevTrimmed = prevLine?.trim().orEmpty()
        val prevEndsWithTerminal = prevTrimmed.isEmpty() ||
            prevTrimmed.startsWith("#") ||
            looksLikeExplicitHeading(prevTrimmed) ||
            prevTrimmed.lastOrNull() in listOf('.', '!', '?', ':', '"', '”', '’', '\'')

        // Check whether next line starts with lowercase (sentence continues across wrapped line)
        val nextTrimmed = nextLine?.trim().orEmpty()
        val nextStartsLower = nextTrimmed.firstOrNull()?.isLowerCase() == true

        val isMiddleOfSentence = !prevEndsWithTerminal || nextStartsLower

        if (isMiddleOfSentence) {
            // In the middle of running prose, only explicit structural chapter/section headers qualify
            return isExplicitChapterOrSection || isNumberedHeading
        }

        if (isExplicitChapterOrSection || isNumberedHeading) return true

        val letters = trimmed.filter { it.isLetter() }
        if (letters.length in 4..65) {
            val upperRatio = letters.count { it.isUpperCase() }.toFloat() / letters.length
            if (upperRatio >= 0.85f) return true
        }
        if (isTitleCasedSubheading(trimmed)) return true
        return false
    }

    private fun looksLikeExplicitHeading(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.startsWith("#")) return true
        if (Regex("""^(CHAPTER|Chapter|PROLOGUE|Prologue|EPILOGUE|Epilogue|INTRODUCTION|Introduction|PREFACE|Preface|PART|Part|BOOK|Book|SECTION|Section|ACT|Act|SCENE|Scene)\b.*""", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) return true
        if (Regex("""^\d+(\.\d+)*\s+[A-Z0-9].*""").containsMatchIn(trimmed)) return true
        return false
    }

    private fun isTitleCasedSubheading(trimmed: String): Boolean {
        if (trimmed.length !in 3..55) return false
        if (trimmed.contains("\t") || Regex("""\s{3,}""").containsMatchIn(trimmed)) return false
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