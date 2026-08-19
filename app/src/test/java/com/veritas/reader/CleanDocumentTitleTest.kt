package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Test

class CleanDocumentTitleTest {

    @Test
    fun `strips epoch prefix, copy counter and underscores`() {
        assertEquals(
            "Good Vibes, Good Life (Vex King)",
            cleanDocumentTitle("1741927936_Good_Vibes,_Good_Life_(Vex_King)_(1).pdf")
        )
    }

    @Test
    fun `underscore separated names become prose`() {
        assertEquals(
            "End of Semester Games Budget",
            cleanDocumentTitle("End_of_Semester_Games_Budget.docx")
        )
        assertEquals(
            "5B Medical Device Risk Management Using ISO 14971",
            cleanDocumentTitle("5B_Medical_Device_Risk_Management_Using_ISO_14971.pdf")
        )
    }

    @Test
    fun `already readable names are left alone`() {
        assertEquals("Atomic Habits - James Clear", cleanDocumentTitle("Atomic Habits - James Clear.pdf"))
        assertEquals("Animal Farm and 1984 - George Orwell", cleanDocumentTitle("Animal Farm and 1984 - George Orwell.pdf"))
    }

    @Test
    fun `download duplication markers are removed`() {
        assertEquals("report", cleanDocumentTitle("report (2).pdf"))
        assertEquals("report", cleanDocumentTitle("report(3).pdf"))
        assertEquals("report", cleanDocumentTitle("report - Copy.docx"))
    }

    @Test
    fun `a dot inside the title is not mistaken for an extension`() {
        assertEquals("Vol. 2", cleanDocumentTitle("Vol. 2.pdf"))
        assertEquals("Meditations, Bk. IV", cleanDocumentTitle("Meditations, Bk. IV.epub"))
    }

    @Test
    fun `dot separated names split only when there are no spaces`() {
        assertEquals("a b c", cleanDocumentTitle("a.b.c.pdf"))
        assertEquals("Some Book. Final.pdf", cleanDocumentTitle("Some Book. Final.pdf.txt"))
    }

    @Test
    fun `never returns blank`() {
        assertEquals("Imported document", cleanDocumentTitle(""))
        assertEquals("Imported document", cleanDocumentTitle("   "))
        assertEquals("Imported document", cleanDocumentTitle("___"))
    }

    @Test
    fun `an all digit name survives rather than vanishing`() {
        assertEquals("1741927936", cleanDocumentTitle("1741927936.pdf"))
    }

    @Test
    fun `no extension is fine`() {
        assertEquals("Reading list", cleanDocumentTitle("Reading_list"))
    }
}
