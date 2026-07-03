package com.veritas.reader.ui.screens

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Pure text-editing logic for the general notes editor: inline markers, line prefixes
 * (headings/lists), and smart list continuation on Enter. Kept free of composable state so
 * it can be unit-tested — the editor delegates here.
 */
object VeritasNoteEditing {

    val LINE_PREFIX_REGEX = Regex("^(#{1,3} |[-*•] |\\d+\\. )")
    private val NUMBERED_LINE_REGEX = Regex("^\\d+\\. .*")
    private val NUMBERED_PREFIX_REGEX = Regex("\\d+\\. ")
    private val LIST_ITEM_REGEX = Regex("^(\\s*)(?:([-*•])|(\\d+)\\.|(\\[[ xX]\\]))\\s+(.*)$")

    /**
     * Wraps the selection (or inserts empty markers at the caret) with an inline marker,
     * or unwraps if the selection is already wrapped.
     */
    fun toggleInlineMarker(value: TextFieldValue, marker: String): TextFieldValue {
        val text = value.text
        val s = value.selection.min
        val e = value.selection.max
        val selected = text.substring(s, e)
        val mlen = marker.length
        return when {
            selected.length >= 2 * mlen && selected.startsWith(marker) && selected.endsWith(marker) -> {
                val inner = selected.substring(mlen, selected.length - mlen)
                val nt = text.substring(0, s) + inner + text.substring(e)
                TextFieldValue(nt, TextRange(s, s + inner.length))
            }
            s >= mlen && e + mlen <= text.length &&
                text.substring(s - mlen, s) == marker && text.substring(e, e + mlen) == marker -> {
                val nt = text.substring(0, s - mlen) + selected + text.substring(e + mlen)
                TextFieldValue(nt, TextRange(s - mlen, e - mlen))
            }
            else -> {
                val nt = text.substring(0, s) + marker + selected + marker + text.substring(e)
                val sel = if (s == e) TextRange(s + mlen) else TextRange(s + mlen, e + mlen)
                TextFieldValue(nt, sel)
            }
        }
    }

    /**
     * Toggles a line-level prefix (heading / list marker) across every line touched by the
     * selection. A numbered-list prefix numbers each selected line sequentially (1., 2., 3.,
     * …) so "select all → numbered list" numbers the whole block; bullets/headings apply the
     * same marker per line. With no selection it toggles the single caret line.
     */
    fun toggleLinePrefix(value: TextFieldValue, prefix: String): TextFieldValue {
        val text = value.text
        val selMin = value.selection.min
        val selMax = value.selection.max
        val blockStart = text.lastIndexOf('\n', (selMin - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val blockEnd = text.indexOf('\n', selMax).let { if (it < 0) text.length else it }
        val block = text.substring(blockStart, blockEnd)
        val lines = block.split("\n")
        val isNumbered = prefix.matches(NUMBERED_PREFIX_REGEX)
        // Toggle off when every non-blank line already carries this kind of marker.
        val allHave = lines.all { line ->
            line.isBlank() || if (isNumbered) line.matches(NUMBERED_LINE_REGEX) else line.startsWith(prefix)
        }
        val rebuilt = if (allHave) {
            lines.joinToString("\n") { it.replaceFirst(LINE_PREFIX_REGEX, "") }
        } else {
            var n = 1
            lines.joinToString("\n") { line ->
                if (line.isBlank()) {
                    line
                } else {
                    val stripped = line.replaceFirst(LINE_PREFIX_REGEX, "")
                    if (isNumbered) "${n++}. $stripped" else "$prefix$stripped"
                }
            }
        }
        val nt = text.substring(0, blockStart) + rebuilt + text.substring(blockEnd)
        val newCaret = (blockStart + rebuilt.length).coerceIn(0, nt.length)
        return TextFieldValue(nt, TextRange(newCaret))
    }

    /**
     * Continues list markers when Enter is pressed and ends the list when Enter is pressed
     * on an empty item.
     *
     * Detection is diff-based rather than requiring the text to grow by exactly one char:
     * IMEs frequently commit an autocorrected word plus the newline as a single batch edit,
     * which is why naive +1 detection made numbering stop after a couple of items. We accept
     * any edit whose inserted segment ends with '\n' and whose caret sits right after it.
     */
    fun continueListOnNewline(old: TextFieldValue, candidate: TextFieldValue): TextFieldValue {
        val oldText = old.text
        val newText = candidate.text
        if (newText.length <= oldText.length) return candidate
        val caret = candidate.selection.start
        if (caret <= 0 || newText.getOrNull(caret - 1) != '\n') return candidate

        // Diff old vs new via common prefix/suffix to find the inserted segment.
        var prefixLen = 0
        val maxPrefix = minOf(oldText.length, newText.length)
        while (prefixLen < maxPrefix && oldText[prefixLen] == newText[prefixLen]) prefixLen++
        var suffixLen = 0
        val maxSuffix = minOf(oldText.length, newText.length) - prefixLen
        while (suffixLen < maxSuffix &&
            oldText[oldText.length - 1 - suffixLen] == newText[newText.length - 1 - suffixLen]
        ) suffixLen++
        val inserted = newText.substring(prefixLen, newText.length - suffixLen)
        // Only react to edits that actually introduced the newline the caret sits after.
        if (!inserted.contains('\n') || caret < prefixLen || caret > newText.length - suffixLen + 1) {
            return candidate
        }

        val before = newText.substring(0, caret - 1)
        val lineStart = before.lastIndexOf('\n') + 1
        val line = before.substring(lineStart)
        val match = LIST_ITEM_REGEX.find(line) ?: return candidate
        val indent = match.groupValues[1]
        val bullet = match.groupValues[2]
        val number = match.groupValues[3]
        val check = match.groupValues[4]
        val itemContent = match.groupValues[5]
        if (itemContent.isBlank()) {
            // Empty list item + Enter => remove the marker and end the list.
            val nt = newText.substring(0, lineStart) + newText.substring(caret)
            return candidate.copy(text = nt, selection = TextRange(lineStart))
        }
        val nextMarker = when {
            bullet.isNotEmpty() -> "$indent$bullet "
            number.isNotEmpty() -> "$indent${(number.toIntOrNull() ?: 1) + 1}. "
            check.isNotEmpty() -> "$indent[ ] "
            else -> return candidate
        }
        val nt = newText.substring(0, caret) + nextMarker + newText.substring(caret)
        return candidate.copy(text = nt, selection = TextRange(caret + nextMarker.length))
    }
}
