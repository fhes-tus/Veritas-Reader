package com.veritas.reader

import com.veritas.reader.ui.ReaderViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun testVersionComparisonCorrectlyIdentifiesNewerVersions() {
        // Remote version is newer
        assertTrue(ReaderViewModel.isVersionNewer("1.0.0", "1.0.1"))
        assertTrue(ReaderViewModel.isVersionNewer("1.0.0", "1.1.0"))
        assertTrue(ReaderViewModel.isVersionNewer("1.0.9", "1.1.0"))
        assertTrue(ReaderViewModel.isVersionNewer("1.0.0", "2.0.0"))

        // Remote version is equal or older
        assertFalse(ReaderViewModel.isVersionNewer("1.0.1", "1.0.1"))
        assertFalse(ReaderViewModel.isVersionNewer("1.1.0", "1.0.9"))
        assertFalse(ReaderViewModel.isVersionNewer("1.1.0", "1.1.0"))
        assertFalse(ReaderViewModel.isVersionNewer("2.0.0", "1.0.0"))
    }
}
