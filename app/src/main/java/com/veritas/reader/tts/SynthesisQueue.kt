package com.veritas.reader.tts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import java.util.concurrent.ConcurrentHashMap

/**
 * Prefetch queue for TTS sentence synthesis.
 * Keeps 1-2 sentences pre-synthesized in background coroutines ahead of active playback
 * to ensure gapless audio playback across sentence boundaries.
 */
class SynthesisQueue(
    private val ttsEngine: TtsEngine,
    private val lookahead: Int = 2
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val cache = ConcurrentHashMap<Int, Deferred<ShortArray?>>()
    private var sentences: List<SentenceSpan> = emptyList()

    /**
     * Resets the queue with a new list of sentences and primes the prefetch pipeline at initialIndex.
     */
    fun setSentences(newSentences: List<SentenceSpan>, initialIndex: Int = 0) {
        clear()
        this.sentences = newSentences
        primeQueue(initialIndex)
    }

    /**
     * Retrieves or awaits the synthesized PCM audio for sentence at [index].
     * Triggers prefetching of subsequent sentences.
     */
    suspend fun getOrSynthesize(index: Int): ShortArray? {
        if (index !in sentences.indices) return null

        // Trigger prefetch for upcoming sentences
        primeQueue(index + 1)

        val deferred = cache.computeIfAbsent(index) { idx ->
            scope.async {
                ttsEngine.synthesize(sentences[idx].text)
            }
        }

        val result = try {
            deferred.await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            null
        } catch (e: Exception) {
            null
        }
        // Clean up completed cache entry to conserve memory
        cache.remove(index)
        return result
    }

    private fun primeQueue(fromIndex: Int) {
        val endIndex = minOf(fromIndex + lookahead, sentences.size)
        for (i in fromIndex until endIndex) {
            cache.computeIfAbsent(i) { idx ->
                scope.async {
                    ttsEngine.synthesize(sentences[idx].text)
                }
            }
        }
    }

    /**
     * Clears all pending prefetch tasks and releases cached audio buffers.
     */
    fun clear() {
        cache.values.forEach { it.cancel() }
        cache.clear()
    }

    /**
     * Shuts down the prefetch coroutine scope.
     */
    fun shutdown() {
        clear()
        scope.cancel()
    }
}
