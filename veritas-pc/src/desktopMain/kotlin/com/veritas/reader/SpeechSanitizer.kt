package com.veritas.reader

/**
 * Desktop copy of the app's SpeechSanitizer: silences decorative glyphs (list
 * bullets, arrows, dingbats, box-drawing, dot leaders) before text reaches the
 * TTS engine, so it never verbalizes "black circle" or "rightwards arrow".
 * Replacements are length-preserving (glyph → space, ellipsis → period) so any
 * offset-based highlighting stays aligned with the audio.
 */
object SpeechSanitizer {
    private val extraSilentGlyphs = setOf(
        '•', '‣', '⁃', '∙',
        '▪', '▫', '●', '○', '■', '□', '◆', '◇',
        '▶', '◀', '▲', '▼', '►', '◄',
        '★', '☆',
        '✓', '✔', '✗', '✘',
        '❤'
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
                char == '…' -> '.'
                else -> char
            }
        }
        // Dot leaders: keep the first dot for a single pause, blank the rest.
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
