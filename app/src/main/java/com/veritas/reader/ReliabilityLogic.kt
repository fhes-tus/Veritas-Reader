package com.veritas.reader

import org.json.JSONArray

/**
 * Pure decision logic for playback advancement, extracted from PlaybackService so the
 * "rat bug" class of errors (advancing from a UI-mutated shared index instead of the
 * sentence actually spoken) stays pinned by unit tests.
 */
object PlaybackAdvance {
    /**
     * The authoritative index of the sentence that was just spoken. [activeChunkIndex] is
     * the service's internal record (-1 when nothing was spoken); the shared index is a
     * fallback only, clamped to bounds, because the UI can mutate it mid-utterance.
     */
    fun resolveCurrentIndex(activeChunkIndex: Int, sharedIndex: Int, lastIndex: Int): Int {
        if (lastIndex < 0) return 0
        return activeChunkIndex.takeIf { it in 0..lastIndex }
            ?: sharedIndex.coerceIn(0, lastIndex)
    }

    /** Next index to speak after [current], or null when the document is finished. */
    fun nextIndex(current: Int, lastIndex: Int): Int? =
        if (current < lastIndex) current + 1 else null
}

/**
 * Pure fallback decision for the resilient prefs-JSON store: prefer the primary value if
 * it parses, else recover from the last-known-good backup, else empty. Extracted so the
 * recovery path is actually exercised by tests rather than only running on corruption.
 */
internal object ResilientJson {
    fun chooseArray(primary: String?, backup: String?): JSONArray {
        primary?.let { raw ->
            runCatching { JSONArray(raw) }.getOrNull()?.let { return it }
        }
        backup?.let { raw ->
            runCatching { JSONArray(raw) }.getOrNull()?.let { return it }
        }
        return JSONArray()
    }
}
