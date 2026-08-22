package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * The extracted-text view and the original-document view have to agree on what page a
 * sentence is on. These cover the three joints where they used to drift: the page markers
 * the extractors emit, the sentence/page lookups both directions, and the line matching
 * the canvases use to highlight what is being spoken.
 */
class PageSentenceSyncTest {

    // --- Marker-driven page mapping ---------------------------------------------

    private fun modelOf(vararg pages: Pair<Int, String>): ReaderTextModel {
        val text = pages.joinToString("\n\n") { (number, body) ->
            "${ReaderTextIndex.pageMarker(number)}\n$body"
        }
        return ReaderTextIndex.build(text, pages.size)
    }

    @Test
    fun sentencesInheritThePageOfTheMarkerAboveThem() {
        val model = modelOf(
            1 to "Alpha one. Alpha two.",
            2 to "Beta one.",
            3 to "Gamma one. Gamma two. Gamma three."
        )

        assertEquals(3, model.pageCount)
        // Every sentence must land on the page its marker declared, regardless of how
        // unevenly sentences are distributed — the old interpolation assumed even spread.
        assertEquals(listOf(1, 1, 2, 3, 3, 3), model.sentences.map { it.pageNumber })
    }

    @Test
    fun pageToFirstSentenceResolvesExactly() {
        val model = modelOf(
            1 to "Alpha one. Alpha two.",
            2 to "Beta one.",
            3 to "Gamma one. Gamma two."
        )

        // This is the lookup onPageChanged performs when the user flips a page while paused.
        assertEquals(0, model.sentences.indexOfFirst { it.pageNumber == 1 })
        assertEquals(2, model.sentences.indexOfFirst { it.pageNumber == 2 })
        assertEquals(3, model.sentences.indexOfFirst { it.pageNumber == 3 })
    }

    @Test
    fun sentenceToPageResolvesExactly() {
        val model = modelOf(
            1 to "Alpha one. Alpha two.",
            2 to "Beta one.",
            3 to "Gamma one. Gamma two."
        )

        // And this is the lookup ActualDocumentView performs to pick its opening page.
        assertEquals(1, model.sentences[1].pageNumber)
        assertEquals(2, model.sentences[2].pageNumber)
        assertEquals(3, model.sentences[4].pageNumber)
    }

    @Test
    fun unevenSentenceDistributionDoesNotDriftTheMapping() {
        // The failure the interpolation produced: one dense page followed by sparse ones
        // dragged later sentences onto the wrong page.
        val model = modelOf(
            1 to "A one. A two. A three. A four. A five. A six.",
            2 to "B one.",
            3 to "C one."
        )

        val last = model.sentences.lastIndex
        assertEquals(3, model.sentences[last].pageNumber)
        // Linear interpolation would have put the final sentence's page at
        // round(last / lastIndex * (pageCount - 1)) + 1, which is only right by accident.
        assertEquals(1, model.sentences[5].pageNumber)
    }

    @Test
    fun pageWithNoSentencesIsSimplyAbsentFromTheLookup() {
        // A blank or image-only page: nothing should claim to start there.
        val model = modelOf(
            1 to "Alpha one.",
            2 to "",
            3 to "Gamma one."
        )
        assertEquals(-1, model.sentences.indexOfFirst { it.pageNumber == 2 })
    }

    // --- Selection scoping -------------------------------------------------------

    @Test
    fun selectionPrefersAMatchOnThePageItWasMadeOn() {
        // The same sentence appears on pages 1 and 3. Tapping it on page 3 must not send
        // playback back to page 1, which is what the old global scan did.
        val model = modelOf(
            1 to "The quick brown fox jumps over the lazy dog.",
            2 to "Something else entirely on this page.",
            3 to "The quick brown fox jumps over the lazy dog."
        )

        val match = PdfSelectionLocator.findMatch(
            selectedText = "The quick brown fox jumps over the lazy dog.",
            model = model,
            currentPage = 3,
            preferredSentenceIndex = 2
        )

        assertNotNull(match)
        assertEquals(3, model.sentences[match!!.chunkIndex].pageNumber)
    }

    @Test
    fun selectionStillResolvesWhenThePageIsUnknown() {
        val model = modelOf(1 to "Alpha one.", 2 to "A uniquely worded beta sentence here.")

        val match = PdfSelectionLocator.findMatch(
            selectedText = "A uniquely worded beta sentence here.",
            model = model,
            currentPage = null,
            preferredSentenceIndex = 0
        )

        assertNotNull(match)
        assertEquals(2, model.sentences[match!!.chunkIndex].pageNumber)
    }

    @Test
    fun selectionThatMatchesNothingReturnsNull() {
        val model = modelOf(1 to "Alpha one.", 2 to "Beta one.")
        assertNull(
            PdfSelectionLocator.findMatch(
                selectedText = "zzzz qqqq vvvv nothing like this exists",
                model = model,
                currentPage = 1,
                preferredSentenceIndex = 0
            )
        )
    }

    private fun assertNull(value: Any?) = assertTrue("expected null but was $value", value == null)

    // --- Canvas line matching ----------------------------------------------------

    @Test
    fun matcherIgnoresPunctuationAndCaseDifferences() {
        assertTrue(
            ActiveSentenceMatcher.matches(
                "The quick brown fox jumps",
                "the quick brown fox jumps!"
            )
        )
    }

    @Test
    fun matcherMatchesASentenceContainedInALongerLine() {
        // A slide bullet holding two sentences: the spoken one is only part of the line.
        assertTrue(
            ActiveSentenceMatcher.matches(
                "First sentence here. Second sentence here.",
                "Second sentence here."
            )
        )
    }

    @Test
    fun matcherMatchesALineContainedInALongerSentence() {
        // A sentence that wrapped across two rendered lines.
        assertTrue(
            ActiveSentenceMatcher.matches(
                "a sentence that was split across lines",
                "This is a sentence that was split across lines when rendered."
            )
        )
    }

    @Test
    fun matcherRejectsShortIncidentalOverlap() {
        // "Introduction" appears inside the sentence, but highlighting the heading whenever
        // any sentence mentions it is exactly the wrong-line highlighting being fixed.
        assertFalse(ActiveSentenceMatcher.matches("Introduction", "Introduction to the topic at hand."))
    }

    @Test
    fun matcherRejectsUnrelatedLines() {
        assertFalse(ActiveSentenceMatcher.matches("Completely unrelated bullet", "The spoken sentence."))
    }

    @Test
    fun matcherRejectsBlankInput() {
        assertFalse(ActiveSentenceMatcher.matches("", "The spoken sentence."))
        assertFalse(ActiveSentenceMatcher.matches("A rendered line", ""))
        assertFalse(ActiveSentenceMatcher.matches("...", "---"))
    }

    // --- Extractor and parser must agree -----------------------------------------

    private fun zipOf(vararg entries: Pair<String, String>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            entries.forEach { (name, body) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(body.toByteArray())
                zip.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    @Test
    fun docxExtractionPageCountMatchesTheParsersPageCount() {
        val paragraphs = (1..40).joinToString("") {
            "<w:p><w:r><w:t>Paragraph number $it with enough text to carry weight in pagination.</w:t></w:r></w:p>"
        }
        // Built by concatenation rather than a trimIndent block: interpolating multi-line
        // content into one leaves the XML declaration indented, which the parser rejects.
        val documentXml =
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
                """<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""" +
                "<w:body>$paragraphs</w:body></w:document>"
        val bytes = zipOf("word/document.xml" to documentXml)

        val parsed = DocxDocumentParser.parse(bytes, "Doc")
        val extracted = DocumentExtractor.extractDocx(bytes)

        // The whole point of routing extraction through the parser: one page count, and
        // markers that address exactly the pages the canvas will render.
        assertTrue("expected the fixture to paginate", parsed.totalPages > 1)
        assertEquals(parsed.totalPages, extracted.pageCount)

        val model = ReaderTextIndex.build(extracted.text, extracted.pageCount)
        assertEquals(parsed.totalPages, model.sentences.maxOf { it.pageNumber })
        assertEquals(1, model.sentences.minOf { it.pageNumber })
    }

    @Test
    fun epubExtractionChapterCountMatchesTheParsersChapterCount() {
        fun chapter(n: Int) =
            "<html><head><title>Chapter $n</title></head><body>" +
                "<p>Chapter $n opening paragraph with a reasonable amount of prose.</p>" +
                "<p>Chapter $n closing paragraph with a little more prose.</p>" +
                "</body></html>"

        val bytes = zipOf(
            "META-INF/container.xml" to
                """<container><rootfiles><rootfile full-path="OEBPS/content.opf"/></rootfiles></container>""",
            "OEBPS/content.opf" to """
                <package><manifest>
                  <item id="c1" href="c1.xhtml" media-type="application/xhtml+xml"/>
                  <item id="c2" href="c2.xhtml" media-type="application/xhtml+xml"/>
                  <item id="c3" href="c3.xhtml" media-type="application/xhtml+xml"/>
                </manifest><spine>
                  <itemref idref="c1"/><itemref idref="c2"/><itemref idref="c3"/>
                </spine></package>
            """.trimIndent(),
            "OEBPS/c1.xhtml" to chapter(1),
            "OEBPS/c2.xhtml" to chapter(2),
            "OEBPS/c3.xhtml" to chapter(3)
        )

        val parsed = EpubDocumentParser.parse(bytes, "Book")
        val extracted = DocumentExtractor.extractEpub(bytes)

        assertEquals(3, parsed.totalChapters)
        assertEquals(parsed.totalChapters, extracted.pageCount)

        val model = ReaderTextIndex.build(extracted.text, extracted.pageCount)
        assertEquals(parsed.totalChapters, model.sentences.maxOf { it.pageNumber })
    }

    @Test
    fun docxTableCellsAreSeparatedSoSpeechDoesNotRunThemTogether() {
        val text = docxBlockPlainText(
            DocxBlock.Table(listOf(listOf("Module", "Role"), listOf("Reader", "Canvas")))
        )
        assertTrue(text.contains("Module"))
        assertTrue(text.contains("Role"))
        assertFalse("cells must not be concatenated", text.contains("ModuleRole"))
    }
}
