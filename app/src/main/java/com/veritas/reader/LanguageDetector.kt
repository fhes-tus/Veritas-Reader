package com.veritas.reader

import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LanguageDetector {
    suspend fun detectLanguage(text: String): String = suspendCancellableCoroutine { continuation ->
        if (text.isBlank()) {
            continuation.resume("en")
            return@suspendCancellableCoroutine
        }
        val sample = text.take(2000)
        val identifier = LanguageIdentification.getClient()
        identifier.identifyLanguage(sample)
            .addOnSuccessListener { languageCode ->
                val result = if (languageCode == "und") "en" else languageCode
                if (continuation.isActive) continuation.resume(result)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume("en")
            }
    }
}
