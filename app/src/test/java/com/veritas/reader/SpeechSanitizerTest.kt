package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechSanitizerTest {

    @Test
    fun `bullet glyphs become spaces and length is preserved`() {
        val input = "• I am a boy"
        val output = SpeechSanitizer.forSpeech(input)
        assertEquals(input.length, output.length)
        assertEquals("  I am a boy", output)
    }

    @Test
    fun `arrows checkmarks and box drawing are silenced`() {
        val input = "→ done ✓ next ► ─── end"
        val output = SpeechSanitizer.forSpeech(input)
        assertEquals(input.length, output.length)
        assertFalse(output.any { it == '→' || it == '✓' || it == '►' || it == '─' })
        assertTrue(output.contains("done"))
        assertTrue(output.contains("next"))
        assertTrue(output.contains("end"))
    }

    @Test
    fun `dot leaders collapse to one pause with length preserved`() {
        val input = "Chapter 1......Page 9"
        val output = SpeechSanitizer.forSpeech(input)
        assertEquals(input.length, output.length)
        assertEquals("Chapter 1.     Page 9", output)
    }

    @Test
    fun `normal punctuation and sentences pass through untouched`() {
        val input = "Dr. Smith said: \"It works!\" (12.5% up, e.g. now)."
        assertEquals(input, SpeechSanitizer.forSpeech(input))
    }

    @Test
    fun `ellipsis character becomes a single period`() {
        val output = SpeechSanitizer.forSpeech("Wait… what")
        assertEquals("Wait. what", output)
    }

    @Test
    fun `pure decoration chunks are not speakable`() {
        assertFalse(SpeechSanitizer.isSpeakable("•••"))
        assertFalse(SpeechSanitizer.isSpeakable("───────"))
        assertTrue(SpeechSanitizer.isSpeakable("• real text"))
        assertTrue(SpeechSanitizer.isSpeakable("plain sentence."))
    }

    @Test
    fun `two dots are left alone - only runs of three or more collapse`() {
        assertEquals("a..b", SpeechSanitizer.forSpeech("a..b"))
    }
}
