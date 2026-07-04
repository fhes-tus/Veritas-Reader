package com.veritas.reader

import org.json.JSONArray

/**
 * Pure decision logic for playback advancement, extracted from PlaybackService so the
 * "rat bug" class of errors (advancing from a UI-mutated shared index instead of the
 * sentence actually spoken) stays pinned by unit tests.
 */
object PlaybackAdvance {
    /**
     * The authoritative index of the sentence that was just spoken. [activeChunkIndex] is
     * the service's internal record (-1 when nothing was spoken); the shared index is a
     * fallback only, clamped to bounds, because the UI can mutate it mid-utterance.
     */
    fun resolveCurrentIndex(activeChunkIndex: Int, sharedIndex: Int, lastIndex: Int): Int {
        if (lastIndex < 0) return 0
        return activeChunkIndex.takeIf { it in 0..lastIndex }
            ?: sharedIndex.coerceIn(0, lastIndex)
    }

    /** Next index to speak after [current], or null when the document is finished. */
    fun nextIndex(current: Int, lastIndex: Int): Int? =
        if (current < lastIndex) current + 1 else null
}

/**
 * Silences decorative glyphs (list bullets, arrows, dingbats, box-drawing, dot
 * leaders) before text reaches the TTS engine, so it never verbalizes "black
 * circle" or "rightwards arrow". CRITICAL invariant: every replacement is
 * length-preserving (glyph → space, ellipsis → period) because word-level
 * highlight sync maps engine character offsets back onto the DISPLAYED string —
 * deleting characters would drift the highlight out of sync with the audio.
 */
object SpeechSanitizer {
    private val extraSilentGlyphs = setOf(
        '•', // • bullet
        '‣', // ‣ triangular bullet
        '⁃', // ⁃ hyphen bullet
        '∙', // ∙ bullet operator
        '▪', '▫', '●', '○', '■', '□', '◆', '◇',
        '▶', '◀', '▲', '▼', '►', '◄',
        '★', '☆', // ★ ☆
        '✓', '✔', '✗', '✘', // ✓ ✔ ✗ ✘
        '❤' // ❤
    )

    private fun isSilent(char: Char): Boolean {
        val code = char.code
        return char in extraSilentGlyphs ||
            code in 0x2190..0x21FF || // arrows
            code in 0x2500..0x25FF || // box drawing, blocks, geometric shapes
            code in 0x2700..0x27BF    // dingbats
    }

    fun forSpeech(text: String): String {
        if (text.isEmpty()) return text
        val chars = CharArray(text.length) { index ->
            val char = text[index]
            when {
                isSilent(char) -> ' '
                char == '…' -> '.' // … one pause, not "dot dot dot"
                else -> char
            }
        }
        // Dot leaders ("......" in tables of contents): keep the first dot for a
        // single pause, blank the rest — run length is preserved.
        var index = 0
        while (index < chars.size) {
            if (chars[index] == '.') {
                var end = index + 1
                while (end < chars.size && chars[end] == '.') end++
                if (end - index >= 3) {
                    for (i in index + 1 until end) chars[i] = ' '
                }
                index = end
            } else {
                index++
            }
        }
        return String(chars)
    }

    /** False when a chunk is pure decoration and should be skipped, not spoken. */
    fun isSpeakable(text: String): Boolean = forSpeech(text).isNotBlank()
}

/**
 * Pure fallback decision for the resilient prefs-JSON store: prefer the primary value if
 * it parses, else recover from the last-known-good backup, else empty. Extracted so the
 * recovery path is actually exercised by tests rather than only running on corruption.
 */
internal object ResilientJson {
    fun chooseArray(primary: String?, backup: String?): JSONArray {
        primary?.let { raw ->
            runCatching { JSONArray(raw) }.getOrNull()?.let { return it }
        }
        backup?.let { raw ->
            runCatching { JSONArray(raw) }.getOrNull()?.let { return it }
        }
        return JSONArray()
    }
}
