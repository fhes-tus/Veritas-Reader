package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderTextModelTest {

    @Test
    fun `academic et al citations do not trigger false sentence breaks`() {
        val input = "According to Johnson et al. (2020), the results were conclusive."
        val sentences = ReaderTextIndex.sentences(input)
        assertEquals(1, sentences.size)
        assertEquals(input, sentences[0])
    }

    @Test
    fun `pdf newline with capitalized next line splits as requested`() {
        val input = "The lead researcher at\nHarvard University discovered a cure."
        val sentences = ReaderTextIndex.sentences(input)
        assertTrue(sentences.size >= 2)
        assertEquals("The lead researcher at", sentences[0])
        assertEquals("Harvard University discovered a cure.", sentences[1])
    }
}
