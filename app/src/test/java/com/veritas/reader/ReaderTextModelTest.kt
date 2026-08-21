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

    @Test
    fun `reader text model page count matches max sentence page number`() {
        val multilineText = (1..10).joinToString("\n\n") { "[[VERITAS_PAGE:$it]]\nThis is sentence on page $it." }
        val model = ReaderTextIndex.build(multilineText)
        assertEquals(10, model.pageCount)
        assertEquals(10, model.sentences.maxOf { it.pageNumber })
        assertEquals(10, model.sentences.size)
    }
}
