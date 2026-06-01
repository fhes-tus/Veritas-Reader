package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ReaderTrackerMathTest {
    @Test
    fun currentStreakCountsOnlyConsecutiveAppOpenDays() {
        val openDays = setOf("2026-05-19", "2026-05-20", "2026-05-22", "2026-05-23")

        assertEquals(2, ReaderTrackerMath.currentStreak(openDays, "2026-05-23"))
    }

    @Test
    fun longestStreakFindsLongestRunAfterMissedDays() {
        val openDays = setOf("2026-05-16", "2026-05-17", "2026-05-19", "2026-05-20", "2026-05-21")

        assertEquals(3, ReaderTrackerMath.longestStreak(openDays))
    }

    @Test
    fun weeklyUsageAggregatesKnownWeekAndAveragesAcrossSevenDays() {
        val weekKeys = listOf(
            "2026-05-18",
            "2026-05-19",
            "2026-05-20",
            "2026-05-21",
            "2026-05-22",
            "2026-05-23",
            "2026-05-24"
        )
        val days = listOf(
            ReaderTrackerDay(dateKey = "2026-05-18", usageMillis = 30 * 60 * 1000L),
            ReaderTrackerDay(dateKey = "2026-05-20", usageMillis = 90 * 60 * 1000L),
            ReaderTrackerDay(dateKey = "2026-05-25", usageMillis = 600 * 60 * 1000L)
        )

        val (total, average) = ReaderTrackerMath.weeklyUsage(days, weekKeys)

        assertEquals(120 * 60 * 1000L, total)
        assertEquals((120 * 60 * 1000L) / 7L, average)
    }

    @Test
    fun monthlyCompletionCountDeduplicatesDocuments() {
        val completions = listOf(
            ReaderTrackerCompletion(documentId = "doc-a", title = "A", completedAt = localDate("2026-05-02")),
            ReaderTrackerCompletion(documentId = "doc-a", title = "A", completedAt = localDate("2026-05-03")),
            ReaderTrackerCompletion(documentId = "doc-b", title = "B", completedAt = localDate("2026-05-14")),
            ReaderTrackerCompletion(documentId = "doc-c", title = "C", completedAt = localDate("2026-04-28"))
        )

        val count = ReaderTrackerMath.monthCompletionCount(
            completions = completions,
            monthPrefix = "2026-05",
            dateKeyFor = ReaderTrackerMath::dateKey
        )

        assertEquals(2, count)
    }

    @Test
    fun deletingBookmarkAnnotationRemovesHighlightSourceForThatSentence() {
        val bookmark = ReaderAnnotation(
            documentId = "doc-1",
            chunkIndex = 7,
            type = AnnotationType.BOOKMARK,
            createdAt = 1L,
            updatedAt = 1L
        )
        val note = ReaderAnnotation(
            documentId = "doc-1",
            chunkIndex = 7,
            type = AnnotationType.NOTE,
            note = "Important",
            createdAt = 2L,
            updatedAt = 2L
        )

        val remaining = listOf(bookmark, note).filterNot { it.stableKey == bookmark.stableKey }

        assertFalse(remaining.any { it.type == AnnotationType.BOOKMARK && it.chunkIndex == bookmark.chunkIndex })
        assertEquals(listOf(note), remaining)
    }

    @Test
    fun documentNoteStableKeyRoundTripsDocumentIdForSelectionDeletion() {
        val key = documentNoteStableKey("doc-42")

        assertEquals("doc-42", documentIdFromDocumentNoteStableKey(key))
        assertEquals(null, documentIdFromDocumentNoteStableKey("doc-42:7:NOTE"))
    }

    private fun localDate(value: String): Long {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)!!.time
    }
}
