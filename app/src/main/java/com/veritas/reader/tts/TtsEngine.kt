package com.veritas.reader.tts

/**
 * Common interface for all TTS engines (System TextToSpeech, Kokoro, Piper).
 * Keeps playback and UI code agnostic of the underlying TTS synthesis implementation.
 */
interface TtsEngine {
    /**
     * Synthesize a single sentence into raw PCM 16-bit ShortArray audio samples.
     * Returns null if synthesis fails or engine is not ready.
     */
    suspend fun synthesize(sentence: String): ShortArray?

    /**
     * Returns whether the engine is initialized and ready to synthesize audio.
     */
    fun isReady(): Boolean

    /**
     * Sample rate of the synthesized PCM audio in Hz (e.g. 22050 Hz or 24000 Hz).
     */
    val sampleRate: Int

    /**
     * Release all native and system resources.
     */
    fun shutdown()
}
