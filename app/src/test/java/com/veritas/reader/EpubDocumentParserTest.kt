package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EpubDocumentParserTest {

    @Test
    fun parse_createsValidEpubBook_fromSyntheticArchive() {
        val containerXml = """
            <?xml version="1.0"?>
            <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
              <rootfiles>
                <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
              </rootfiles>
            </container>
        """.trimIndent()

        val opfXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <package xmlns="http://www.idpf.org/2007/opf" version="2.0">
              <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                <dc:title>Adventures in Wonderland</dc:title>
              </metadata>
              <manifest>
                <item id="chapter1" href="chap1.xhtml" media-type="application/xhtml+xml"/>
                <item id="chapter2" href="chap2.xhtml" media-type="application/xhtml+xml"/>
              </manifest>
              <spine>
                <itemref idref="chapter1"/>
                <itemref idref="chapter2"/>
              </spine>
            </package>
        """.trimIndent()

        val chap1Xhtml = """
            <!DOCTYPE html>
            <html>
            <head><title>Down the Rabbit-Hole</title></head>
            <body>
              <h1>Down the Rabbit-Hole</h1>
              <p>Alice was beginning to get very tired of sitting by her sister on the bank.</p>
              <p>Once or twice she had peeped into the book her sister was reading.</p>
            </body>
            </html>
        """.trimIndent()

        val chap2Xhtml = """
            <!DOCTYPE html>
            <html>
            <head><title>The Pool of Tears</title></head>
            <body>
              <h1>The Pool of Tears</h1>
              <p>Curiouser and curiouser! cried Alice.</p>
            </body>
            </html>
        """.trimIndent()

        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(ZipEntry("META-INF/container.xml"))
            zip.write(containerXml.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("OEBPS/content.opf"))
            zip.write(opfXml.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("OEBPS/chap1.xhtml"))
            zip.write(chap1Xhtml.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("OEBPS/chap2.xhtml"))
            zip.write(chap2Xhtml.toByteArray())
            zip.closeEntry()
        }

        val book = EpubDocumentParser.parse(baos.toByteArray(), "Fallback Title")

        assertEquals("Adventures in Wonderland", book.title)
        assertEquals(2, book.totalChapters)
        assertEquals("Down the Rabbit-Hole", book.chapters[0].title)
        assertTrue(book.chapters[0].paragraphs.any { it.contains("Alice was beginning to get very tired") })
        assertEquals("The Pool of Tears", book.chapters[1].title)
        assertTrue(book.chapters[1].paragraphs.any { it.contains("Curiouser and curiouser!") })
    }

    @Test
    fun parse_handlesCorruptedOrEmptyArchiveGracefully() {
        val emptyBytes = ByteArray(0)
        val book = EpubDocumentParser.parse(emptyBytes, "Safe Default")
        assertNotNull(book)
        assertEquals("Safe Default", book.title)
        assertEquals(1, book.totalChapters)
    }
}
