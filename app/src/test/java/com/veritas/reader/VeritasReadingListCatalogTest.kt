package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class VeritasReadingListCatalogTest {
    @Test
    fun addDocumentsDeduplicatesAndKeepsManualOrder() {
        val catalog = VeritasReadingListCatalog()
            .createList(title = "Course readings", now = 100L, id = "course")
            .addDocuments("course", listOf("doc-a", "doc-b", "doc-a", "doc-c"), now = 200L)

        val list = catalog.list("course")!!

        assertEquals(listOf("doc-a", "doc-b", "doc-c"), list.documentIds)
        assertEquals(listOf(0, 1, 2), list.items.map { it.position })
        assertEquals(VeritasReadingListSortMode.MANUAL, list.sortMode)
    }

    @Test
    fun moveDocumentReindexesWithoutLosingItems() {
        val catalog = VeritasReadingListCatalog()
            .createList(title = "Queue", now = 100L, id = "queue")
            .addDocuments("queue", listOf("one", "two", "three"), now = 200L)
            .moveDocument("queue", "three", offset = -2, now = 300L)

        val list = catalog.list("queue")!!

        assertEquals(listOf("three", "one", "two"), list.documentIds)
        assertEquals(listOf(0, 1, 2), list.items.map { it.position })
        assertEquals(300L, list.updatedAt)
    }

    @Test
    fun archiveKeepsListButRemovesItFromActiveLists() {
        val catalog = VeritasReadingListCatalog()
            .createList(title = "Keep visible", now = 100L, id = "visible")
            .createList(title = "Archive me", now = 110L, id = "archived")
            .archiveList("archived", archived = true, now = 200L)

        assertTrue(catalog.list("archived")!!.archived)
        assertEquals(listOf("visible"), catalog.activeLists.map { it.id })
    }

    @Test
    fun missingListMutationsAreNoOpsForStaleUiState() {
        val catalog = VeritasReadingListCatalog().createList(title = "Stable", id = "stable", now = 100L)
        val result = catalog.addDocument("missing", "doc-a", now = 200L)

        assertSame(catalog, result)
    }

    @Test
    fun itemNormalizationTrimsDuplicatesAndReindexes() {
        val list = VeritasReadingList(
            id = "list-1",
            title = VeritasReadingList.cleanTitle("  Local   reads  "),
            createdAt = 100L,
            updatedAt = 200L,
            items = VeritasReadingList.normalizeItems(
                listOf(
                    VeritasReadingListItem(documentId = "doc-b", addedAt = 20L, position = 1),
                    VeritasReadingListItem(documentId = "doc-a", addedAt = 10L, position = 0),
                    VeritasReadingListItem(documentId = "doc-b", addedAt = 30L, position = 2)
                )
            )
        )

        assertEquals("Local reads", list.title)
        assertEquals(listOf("doc-a", "doc-b"), list.documentIds)
        assertEquals(listOf(0, 1), list.items.map { it.position })
    }

    @Test
    fun resolveDocumentsUsesListSortModeAndSkipsMissingDocuments() {
        val documents = listOf(
            savedDocument(id = "z", title = "Zebra"),
            savedDocument(id = "a", title = "Atlas")
        )
        val catalog = VeritasReadingListCatalog()
            .createList(title = "Sorted", now = 100L, id = "sorted")
            .addDocuments("sorted", listOf("missing", "z", "a"), now = 200L)
            .setSortMode("sorted", VeritasReadingListSortMode.TITLE_ASCENDING, now = 300L)

        val resolved = catalog.resolveDocuments("sorted", documents)

        assertEquals(listOf("a", "z"), resolved.map { it.id })
        assertFalse(resolved.any { it.id == "missing" })
    }

    private fun savedDocument(id: String, title: String): SavedDocument =
        SavedDocument(
            id = id,
            title = title,
            fileName = "$id.txt",
            sourceLabel = "Text",
            createdAt = 100L,
            updatedAt = 100L,
            currentIndex = 0,
            chunkCount = 1,
            charCount = title.length,
            preview = title
        )
}
