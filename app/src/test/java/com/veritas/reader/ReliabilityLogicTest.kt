package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReliabilityLogicTest {

    // ── PlaybackAdvance: the "rat bug" pinned ─────────────────────────────

    @Test
    fun advanceUsesSpokenIndexEvenWhenSharedIndexWasMutated() {
        // Service spoke sentence 5; meanwhile the UI stomped the shared index to 42.
        assertEquals(5, PlaybackAdvance.resolveCurrentIndex(activeChunkIndex = 5, sharedIndex = 42, lastIndex = 99))
        // Next sentence must be 6, not 43.
        assertEquals(6, PlaybackAdvance.nextIndex(5, 99))
    }

    @Test
    fun advanceFallsBackToClampedSharedIndexWhenNothingWasSpoken() {
        assertEquals(42, PlaybackAdvance.resolveCurrentIndex(activeChunkIndex = -1, sharedIndex = 42, lastIndex = 99))
        assertEquals(99, PlaybackAdvance.resolveCurrentIndex(activeChunkIndex = -1, sharedIndex = 500, lastIndex = 99))
        assertEquals(0, PlaybackAdvance.resolveCurrentIndex(activeChunkIndex = -1, sharedIndex = -3, lastIndex = 99))
    }

    @Test
    fun spokenIndexOutOfRangeIsTreatedAsUnset() {
        // Document shrank (re-extraction) below the previously spoken index.
        assertEquals(10, PlaybackAdvance.resolveCurrentIndex(activeChunkIndex = 500, sharedIndex = 10, lastIndex = 99))
    }

    @Test
    fun lastSentenceFinishesDocument() {
        assertNull(PlaybackAdvance.nextIndex(99, 99))
        assertEquals(99, PlaybackAdvance.nextIndex(98, 99))
        assertNull(PlaybackAdvance.nextIndex(0, 0))
    }

    @Test
    fun emptyDocumentResolvesToZero() {
        assertEquals(0, PlaybackAdvance.resolveCurrentIndex(activeChunkIndex = 3, sharedIndex = 7, lastIndex = -1))
    }

    // ── ResilientJson: recovery path exercised ────────────────────────────

    @Test
    fun validPrimaryIsPreferred() {
        val out = ResilientJson.chooseArray("""[{"id":"a"}]""", """[{"id":"old"}]""")
        assertEquals(1, out.length())
        assertEquals("a", out.getJSONObject(0).getString("id"))
    }

    @Test
    fun corruptPrimaryRecoversFromBackup() {
        val out = ResilientJson.chooseArray("""[{"id":"a"}""" /* truncated */, """[{"id":"old"}]""")
        assertEquals(1, out.length())
        assertEquals("old", out.getJSONObject(0).getString("id"))
    }

    @Test
    fun missingPrimaryRecoversFromBackup() {
        val out = ResilientJson.chooseArray(null, """[{"id":"old"},{"id":"older"}]""")
        assertEquals(2, out.length())
    }

    @Test
    fun bothCorruptYieldsEmptyNotCrash() {
        assertEquals(0, ResilientJson.chooseArray("garbage", "{not json either").length())
        assertEquals(0, ResilientJson.chooseArray(null, null).length())
    }
}
