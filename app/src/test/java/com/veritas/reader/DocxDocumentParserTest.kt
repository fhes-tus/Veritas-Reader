package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DocxDocumentParserTest {

    @Test
    fun parse_extractsHeadingsParagraphsAndTables_fromDocumentXml() {
        val documentXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>
                <w:p>
                  <w:pPr><w:pStyle w:val="Heading1"/></w:pPr>
                  <w:r><w:t>Project Architecture</w:t></w:r>
                </w:p>
                <w:p>
                  <w:r><w:t>Veritas Reader is built using modern Jetpack Compose and Material 3.</w:t></w:r>
                </w:p>
                <w:tbl>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>Module</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Role</w:t></w:r></w:p></w:tc>
                  </w:tr>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>ActualDocView</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Native Visual Canvas</w:t></w:r></w:p></w:tc>
                  </w:tr>
                </w:tbl>
              </w:body>
            </w:document>
        """.trimIndent()

        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(ZipEntry("word/document.xml"))
            zip.write(documentXml.toByteArray())
            zip.closeEntry()
        }

        val doc = DocxDocumentParser.parse(baos.toByteArray(), "Architecture Spec")

        assertEquals("Architecture Spec", doc.title)
        assertTrue(doc.totalPages >= 1)
        val firstPage = doc.pages[0]
        val headings = firstPage.blocks.filterIsInstance<DocxBlock.Heading>()
        val paragraphs = firstPage.blocks.filterIsInstance<DocxBlock.Paragraph>()
        val tables = firstPage.blocks.filterIsInstance<DocxBlock.Table>()

        assertEquals(1, headings.size)
        assertEquals("Project Architecture", headings[0].text)
        assertTrue(paragraphs.any { it.text.contains("Veritas Reader is built") })
        assertEquals(1, tables.size)
        assertEquals(2, tables[0].rows.size)
    }

    @Test
    fun parse_handlesEmptyBytes() {
        val doc = DocxDocumentParser.parse(ByteArray(0), "Empty")
        assertNotNull(doc)
        assertEquals("Empty", doc.title)
    }
}
