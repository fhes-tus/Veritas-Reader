package com.veritas.reader

import com.veritas.reader.ui.ReaderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The self-updater decides whether to prompt an update with isVersionNewer(). A wrong
 * comparison either hides real updates or loops users on an update they already installed,
 * so this is pinned exhaustively.
 */
class VersionComparisonTest {

    @Test
    fun remoteNewerPatchPromptsUpdate() {
        assertTrue(ReaderViewModel.isVersionNewer("1.0.1", "1.0.2"))
    }

    @Test
    fun equalVersionsDoNotPrompt() {
        assertFalse(ReaderViewModel.isVersionNewer("1.0.1", "1.0.1"))
        assertFalse(ReaderViewModel.isVersionNewer("1.0.1.1", "1.0.1.1"))
    }

    @Test
    fun localNewerDoesNotPrompt() {
        assertFalse(ReaderViewModel.isVersionNewer("1.0.2", "1.0.1"))
        assertFalse(ReaderViewModel.isVersionNewer("2.0.0", "1.9.9"))
    }

    @Test
    fun fourPartVersionsCompareCorrectly() {
        // Local 1.0.1.1 (current shipping scheme) vs remote three-part tags.
        assertTrue(ReaderViewModel.isVersionNewer("1.0.1.1", "1.0.2"))
        assertFalse(ReaderViewModel.isVersionNewer("1.0.1.1", "1.0.1"))
        assertTrue(ReaderViewModel.isVersionNewer("1.0.1", "1.0.1.1"))
    }

    @Test
    fun comparisonIsNumericNotLexicographic() {
        assertTrue(ReaderViewModel.isVersionNewer("1.9.0", "1.10.0"))
        assertFalse(ReaderViewModel.isVersionNewer("1.10.0", "1.9.0"))
        assertFalse(ReaderViewModel.isVersionNewer("1.0.10", "1.0.9"))
    }

    @Test
    fun vPrefixesAreStripped() {
        assertTrue(ReaderViewModel.isVersionNewer("v1.0.1", "v1.0.2"))
        assertTrue(ReaderViewModel.isVersionNewer("1.0.1", "V1.0.2"))
        assertTrue(ReaderViewModel.isVersionNewer("v_1.0.1", "v1.0.2"))
    }

    @Test
    fun suffixedTagsCompareOnNumericCore() {
        assertTrue(ReaderViewModel.isVersionNewer("1.0.1-beta", "1.0.2"))
        assertFalse(ReaderViewModel.isVersionNewer("1.0.2", "1.0.2-hotfix"))
    }

    @Test
    fun malformedRemoteTagNeverPrompts() {
        assertFalse(ReaderViewModel.isVersionNewer("1.0.1", ""))
        assertFalse(ReaderViewModel.isVersionNewer("1.0.1", "garbage"))
        assertFalse(ReaderViewModel.isVersionNewer("1.0.1", "latest"))
    }

    @Test
    fun blankLocalVersionStillPromptsForRealRemote() {
        assertTrue(ReaderViewModel.isVersionNewer("", "1.0.0"))
    }

    @Test
    fun cleanVersionStringNormalizes() {
        assertEquals("1.0.2", ReaderViewModel.cleanVersionString("v1.0.2"))
        assertEquals("1.0.2", ReaderViewModel.cleanVersionString("V_1.0.2"))
        assertEquals("1.0.2", ReaderViewModel.cleanVersionString("1.0.2-beta.1"))
        assertEquals("1.0.2", ReaderViewModel.cleanVersionString("1.0.2."))
        assertEquals("", ReaderViewModel.cleanVersionString("latest"))
    }
}
