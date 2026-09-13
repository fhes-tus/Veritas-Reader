package com.veritas.reader

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfTextCleanerTest {

    @Test
    fun `cleanPages does not turn book text with wide spacing or tabs into pipe tables`() {
        val page = listOf(
            "me    to    a    larger    hospital    in    Cincinnati.",
            "I    was    rolled    out    of    the    emergency    room    doors",
            "and    toward    the    helipad    across    the    street.",
            "",
            "The stretcher rattled on a bumpy sidewalk."
        ).joinToString("\n")

        val result = PdfTextCleaner.cleanPages(listOf(page), listOf(1), PdfImportOptions(includePageMarkers = false))
        val text = result.text

        assertFalse("Should never contain pipe symbols in book prose", text.contains("|"))
        assertTrue("Should merge paragraph continuously without pipes", text.contains("me to a larger hospital in Cincinnati. I was rolled out of the emergency room doors and toward the helipad across the street."))
        assertTrue("Summary paragraph should be separated by double newline", text.contains("\n\nThe stretcher rattled on a bumpy sidewalk."))
    }

    @Test
    fun `cleanPages preserves existing markdown pipe tables`() {
        val page = listOf(
            "| Parameter | Value |",
            "| Speed | 100 km/h |",
            "| Temperature | 25 C |",
            "",
            "End of specs."
        ).joinToString("\n")

        val result = PdfTextCleaner.cleanPages(listOf(page), listOf(1), PdfImportOptions(includePageMarkers = false))
        val text = result.text

        assertTrue("Should preserve existing markdown table", text.contains("| Parameter | Value |\n| Speed | 100 km/h |\n| Temperature | 25 C |"))
    }

    @Test
    fun `cleanPages does not break regular sentences with spacing after period`() {
        val page = listOf(
            "This is the first sentence.   Then here is another sentence on the same line.",
            "And another normal paragraph follows."
        ).joinToString("\n")

        val result = PdfTextCleaner.cleanPages(listOf(page), listOf(1), PdfImportOptions(includePageMarkers = false))
        val text = result.text

        assertFalse("Should not treat sentence with space after period as table", text.contains("|"))
    }

    @Test
    fun `cleanPages does not treat continuous words in paragraph as headings`() {
        val page = listOf(
            "In his groundbreaking work, James Clear introduced",
            "The Four Laws of Behavior Change",
            "which explain how human habits are formed and maintained."
        ).joinToString("\n")

        val result = PdfTextCleaner.cleanPages(listOf(page), listOf(1), PdfImportOptions(includePageMarkers = false))
        val text = result.text

        assertFalse("Should not treat title-cased words in middle of sentence as heading", text.contains("# The Four Laws"))
        assertTrue("Should merge paragraph continuously", text.contains("In his groundbreaking work, James Clear introduced The Four Laws of Behavior Change which explain"))
    }

    @Test
    fun `cleanPages preserves genuine chapter headings and subheadings`() {
        val page = listOf(
            "CHAPTER 1",
            "The Fundamentals of Atomic Habits",
            "A habit is a routine or practice performed regularly.",
            "An automatic response to a specific type of situation."
        ).joinToString("\n")

        val result = PdfTextCleaner.cleanPages(listOf(page), listOf(1), PdfImportOptions(includePageMarkers = false))
        val text = result.text

        assertTrue("Should recognize Chapter heading", text.contains("# CHAPTER 1"))
        assertTrue("Should preserve paragraph body", text.contains("A habit is a routine or practice performed regularly. An automatic response"))
    }
}
