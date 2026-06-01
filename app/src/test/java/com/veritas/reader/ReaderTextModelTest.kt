package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderTextModelTest {
    @Test
    fun partPlannerBalancesTinyRemainder() {
        val parts = ReaderPagePartPlanner.plan(25)

        assertEquals(listOf(9, 8, 8), parts.map { it.pageCount })
        assertEquals(1, parts.first().startPage)
        assertEquals(25, parts.last().endPage)
    }

    @Test
    fun partPlannerKeepsLargeDocumentsNearTwelvePages() {
        val parts = ReaderPagePartPlanner.plan(987)
        val sizes = parts.map { it.pageCount }

        assertEquals(83, parts.size)
        assertTrue(sizes.all { it in 11..12 })
        assertTrue(sizes.maxOrNull()!! - sizes.minOrNull()!! <= 1)
    }

    @Test
    fun sentenceSelectionMapsPartOffsetToContainingSentence() {
        val text = listOf(
            ReaderTextIndex.pageMarker(1),
            "First sentence. Second sentence has Holmes in it.",
            ReaderTextIndex.pageMarker(2),
            "Third sentence closes the sample."
        ).joinToString("\n")
        val model = ReaderTextIndex.build(text, storedPageCount = 2)
        val part = model.parts.first()
        val holmesOffset = part.text.indexOf("Holmes")

        val range = model.sentenceForPartOffset(part, holmesOffset)

        assertEquals("Second sentence has Holmes in it.", model.sentences[range!!.sentenceIndex].text)
    }

    @Test
    fun partTextPreservesParagraphAndNumberedItemSpacing() {
        val text = listOf(
            ReaderTextIndex.pageMarker(1),
            "INTRODUCTION",
            "",
            "This opening paragraph keeps its own spacing. It should not flatten into the heading.",
            "",
            "LET'S CHAT",
            "",
            "1. First question should start as its own paragraph. It has a second sentence.",
            "",
            "2. Second question should also stay visually separate."
        ).joinToString("\n")

        val model = ReaderTextIndex.build(text, storedPageCount = 1)
        val partText = model.parts.first().text

        assertTrue(partText.contains("INTRODUCTION\n\nThis opening paragraph"))
        assertTrue(partText.contains("LET'S CHAT\n\n1. First question"))
        assertTrue(partText.contains("second sentence.\n\n2. Second question"))
    }

    @Test
    fun readerTextModelCacheReusesSameModelForSameDocumentText() {
        val text = "First page. Second sentence."

        val first = ReaderTextModelCache.get("cache-doc", text, pageCount = 1)
        val second = ReaderTextModelCache.get("cache-doc", text, pageCount = 1)

        assertTrue(first === second)
    }

    @Test
    fun readerTextModelCacheInvalidatesDocumentEntries() {
        val text = "Fresh model after edit."

        val before = ReaderTextModelCache.get("cache-edit-doc", text, pageCount = 1)
        ReaderTextModelCache.invalidate("cache-edit-doc")
        val after = ReaderTextModelCache.get("cache-edit-doc", text, pageCount = 1)

        assertTrue(before !== after)
    }
}
