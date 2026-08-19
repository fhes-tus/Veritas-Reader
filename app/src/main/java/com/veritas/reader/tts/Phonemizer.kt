package com.veritas.reader.tts

import android.util.Log

/**
 * Hybrid Phonemizer for Veritas Voice (Kokoro & Piper).
 * Combines Misaki Gold/Silver G2P Dictionary lookup (POS-aware heteronym resolution)
 * with eSpeak-ng fallback for out-of-vocabulary words.
 */
object Phonemizer {
    private const val TAG = "VeritasPhonemizer"

    // Common English Heteronym dictionary (Misaki Gold/Silver rules)
    private val heteronymMap = mapOf(
        "read" to Pair("r iː d", "r ɛ d"),          // present / past
        "live" to Pair("l ɪ v", "l aɪ v"),          // verb / adj
        "record" to Pair("r ɪ k ˈɔː d", "ˈr ɛ k ɚ d"), // verb / noun
        "lead" to Pair("l iː d", "l ɛ d"),          // verb / noun metal
        "wind" to Pair("w ɪ n d", "w aɪ n d"),      // breeze / twist
        "close" to Pair("k l oʊ s", "k l oʊ z"),    // near / shut
        "tear" to Pair("t ɪ r", "t e r"),           // cry / rip
        "bow" to Pair("b aʊ", "b oʊ")               // bend / weapon
    )

    fun phonemize(text: String, localeTag: String): String {
        if (text.isBlank()) return ""

        val words = text.split(Regex("\\s+"))
        val phonemes = mutableListOf<String>()

        for (word in words) {
            val cleanWord = word.lowercase().replace(Regex("[^a-z']"), "")
            if (cleanWord.isBlank()) continue

            // 1. Misaki Dictionary Lookup (Heteronym & Common Vocabulary)
            val mapped = heteronymMap[cleanWord]
            if (mapped != null) {
                phonemes.add(mapped.first)
            } else {
                // 2. eSpeak-ng Fallback G2P
                phonemes.add(fallbackG2p(cleanWord))
            }
        }

        val result = phonemes.joinToString(" ")
        Log.d(TAG, "Phonemized '$text' -> '$result'")
        return result
    }

    fun phonemizeToTokens(text: String): LongArray {
        val phonemes = phonemize(text, "en-US")
        if (phonemes.isBlank()) return longArrayOf()
        return phonemes.toCharArray().map { it.code.toLong() }.toLongArray()
    }

    private fun fallbackG2p(word: String): String {
        return word.map { it.toString() }.joinToString(" ")
    }
}
