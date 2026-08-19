package com.veritas.reader.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Continuous 4-Sentence Pre-Buffering Pipeline (Producer-Consumer Sliding Window).
 * Pre-synthesizes up to 4 sentences ahead in background RAM for 0ms gapless narration.
 */
class VeritasAudioBuffer(
    private val context: Context,
    private val engine: TtsEngine
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val pcmCache = ConcurrentHashMap<Int, ShortArray>()
    // Sherpa's native TTS object is not safe to generate audio from multiple coroutines.
    private val synthesisMutex = Mutex()

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    // Only one look-ahead pass may be in flight. speakChunk calls prebufferAhead for
    // every sentence, which used to stack a fresh coroutine each time: four jobs were
    // observed walking the same window, all queued on synthesisMutex ahead of the
    // sentence the user was actually waiting to hear.
    private var prebufferJob: Job? = null

    fun prebufferAhead(chunks: List<String>, currentIndex: Int, windowSize: Int = 4) {
        prebufferAhead(chunks.size, currentIndex, windowSize) { index ->
            chunks.getOrNull(index).orEmpty()
        }
    }

    fun prebufferAhead(
        chunkCount: Int,
        currentIndex: Int,
        windowSize: Int = 4,
        textAt: (Int) -> String
    ) {
        prebufferJob?.cancel()
        prebufferJob = scope.launch {
            // The current sentence may be resumed from a character offset, so it must be
            // synthesized by playSentencePcm using that exact text. Only cache future text.
            val startIndex = (currentIndex + 1).coerceAtMost(chunkCount)
            val targetEnd = (startIndex + windowSize).coerceAtMost(chunkCount)
            // This is a sliding window. Seeking could otherwise leave PCM from every
            // abandoned position retained in memory for the rest of the service lifetime.
            pcmCache.keys.removeIf { it !in startIndex until targetEnd }
            for (i in startIndex until targetEnd) {
                if (!pcmCache.containsKey(i)) {
                    val rawText = textAt(i).trim()
                    if (rawText.isNotBlank()) {
                        val pcm = synthesizeAndCache(i, rawText)
                        if (pcm != null && pcm.isNotEmpty()) {
                            Log.d(TAG, "Pre-buffered sentence $i (${pcm.size} samples)")
                        }
                    }
                }
            }
        }
    }

    fun playSentencePcm(index: Int, text: String, onComplete: () -> Unit) {
        scope.launch(Dispatchers.IO) {
            isPlaying = true
            val requestedAt = System.currentTimeMillis()
            var pcm = pcmCache[index]
            val cacheHit = pcm != null
            if (pcm == null) {
                // kotlinx Mutex is FIFO-fair, so the sentence being waited on would
                // otherwise sit behind every look-ahead item already queued — measured
                // at 14s on a sentence whose own synthesis took 11s. Stand the queue
                // down first; the window is rebuilt as soon as this one is playing.
                prebufferJob?.cancelAndJoin()
                val rawText = text.trim()
                if (rawText.isNotBlank()) {
                    pcm = synthesizeAndCache(index, rawText)
                }
            }

            if (pcm != null && pcm.isNotEmpty()) {
                val sampleRate = engine.sampleRate
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                // The track is long-lived, but its sample rate is fixed at construction.
                // Switching voice (Kokoro 24kHz <-> Piper 22.05kHz) without rebuilding
                // it played every sample at the wrong rate — audible as a gritty,
                // slightly-off-pitch rasp rather than an obvious failure.
                if (audioTrack != null && trackSampleRate != sampleRate) {
                    Log.i(TAG, "Sample rate changed $trackSampleRate -> $sampleRate; rebuilding AudioTrack")
                    runCatching { audioTrack?.stop() }
                    audioTrack?.release()
                    audioTrack = null
                }

                if (audioTrack == null) {
                    trackSampleRate = sampleRate
                    audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        // Sized from the sample rate (about half a second) rather than
                        // from the first sentence's length, which is arbitrary.
                        .setBufferSizeInBytes(minBufferSize.coerceAtLeast(sampleRate))
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                }

                val track = audioTrack
                if (track != null && track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                }
                // write() is blocking in MODE_STREAM: it returns once the data is
                // queued, not once it has been heard. Sleeping for the clip's full
                // duration on top of that added a gap at every sentence boundary, so
                // wait on the head position actually reaching the end instead.
                val startHead = track?.playbackHeadPosition ?: 0
                track?.write(pcm, 0, pcm.size)
                pcmCache.remove(index)

                if (track != null) {
                    val target = startHead.toLong() + pcm.size
                    val timeoutAt = System.currentTimeMillis() +
                        (pcm.size.toDouble() / sampleRate * 1000).toLong() + 1_000L
                    while (isPlaying &&
                        track.playbackHeadPosition.toLong() < target &&
                        System.currentTimeMillis() < timeoutAt
                    ) {
                        delay(15)
                    }
                }
            } else {
                Log.w(TAG, "No audio generated for sentence $index")
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    private suspend fun synthesizeAndCache(index: Int, text: String): ShortArray? = synthesisMutex.withLock {
        pcmCache[index]?.let { return@withLock it }
        val startedAt = System.currentTimeMillis()
        val pcm = engine.synthesize(text)?.takeIf { it.isNotEmpty() }
        if (pcm != null) {
            pcmCache[index] = pcm
        }
        pcm
    }

    /** Sample rate the live [audioTrack] was built for; -1 when there is no track. */
    private var trackSampleRate: Int = -1

    fun flush() {
        prebufferJob?.cancel()
        prebufferJob = null
        scope.coroutineContext.cancelChildren()
        pcmCache.clear()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        trackSampleRate = -1
        isPlaying = false
    }

    fun shutdown() {
        flush()
        engine.shutdown()
    }

    companion object {
        private const val TAG = "VeritasAudioBuffer"
    }
}
