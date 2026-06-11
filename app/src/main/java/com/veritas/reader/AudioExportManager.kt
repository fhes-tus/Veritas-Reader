package com.veritas.reader

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Exports a saved reading to a local WAV file using Android's installed TTS engine.
 *
 * Android's platform TTS API can synthesize speech to files. Long documents are synthesized
 * section-by-section, then the PCM data chunks are merged into a single WAV file.
 */
class AudioExportManager(private val context: Context) {
    data class ExportResult(
        val file: File,
        val displayName: String,
        val synthesizedParts: Int
    )

    suspend fun exportToWav(
        title: String,
        chunks: List<String>,
        rate: Float,
        pitch: Float,
        transformText: (String) -> String
    ): ExportResult {
        val cleanChunks = chunks
            .map { transformText(it).replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotBlank() }

        require(cleanChunks.isNotEmpty()) { "This document has no readable text to export." }

        val repository = DocumentRepository(context)
        val voiceSettings = repository.loadVoiceSettings()
        val narrationSettings = repository.loadNarrationSettings()
        val tts = createReadyTts(voiceSettings)
        val tempDir = File(context.cacheDir, "tts_export_${UUID.randomUUID()}").apply { mkdirs() }
        val partFiles = mutableListOf<File>()

        try {
            val maxLen = (TextToSpeech.getMaxSpeechInputLength() - 250).coerceAtLeast(1000)
            val speechParts = cleanChunks.flatMap { splitForTts(it, maxLen) }

            speechParts.forEachIndexed { index, text ->
                coroutineContext.ensureActive()
                tts.setSpeechRate(NarrationAnalyzer.effectiveRate(rate, narrationSettings, text))
                tts.setPitch(NarrationAnalyzer.effectivePitch(pitch, narrationSettings, text))
                val partFile = File(tempDir, "part_${index.toString().padStart(5, '0')}.wav")
                withTimeout(30_000L) {
                    synthesizePart(tts, text, partFile, "veritas_export_$index")
                }
                if (partFile.exists() && partFile.length() > 44L) partFiles.add(partFile)
            }

            require(partFiles.isNotEmpty()) { "The TTS engine did not create audio. Try another installed voice/engine." }

            val exportDir = File(context.cacheDir, "VeritasExports").apply { mkdirs() }
            val displayName = "${safeFileName(title)}_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.wav"
            val finalFile = File(exportDir, displayName)

            withContext(Dispatchers.IO) {
                if (partFiles.size == 1) {
                    val parsed = WavFile.read(partFiles.first())
                    if (parsed == null) {
                        partFiles.first().copyTo(finalFile, overwrite = true)
                    } else {
                        WavFile.write(finalFile, parsed.formatChunk, listOf(parsed.dataChunk))
                    }
                } else {
                    val parsedParts = partFiles.mapNotNull { WavFile.read(it) }
                    require(parsedParts.size == partFiles.size) {
                        "Could not parse all exported audio parts. Try exporting a shorter document or using a different voice."
                    }
                    val firstFormat = parsedParts.first().formatChunk
                    val mismatchIndex = parsedParts.indexOfFirst { !it.formatChunk.contentEquals(firstFormat) }
                    require(mismatchIndex == -1) {
                        "The TTS engine produced incompatible WAV chunks at part ${mismatchIndex + 1}. " +
                            "Try another voice, another engine, or export a shorter range."
                    }
                    WavFile.write(finalFile, firstFormat, parsedParts.map { it.dataChunk })
                }
            }

            Log.i(TAG, "Successfully exported $title to ${finalFile.absolutePath} (${finalFile.length()} bytes, ${partFiles.size} parts)")
            return ExportResult(finalFile, displayName, partFiles.size)
        } catch (e: CancellationException) {
            Log.i(TAG, "WAV export cancelled for $title")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "WAV export failed for $title: ${e.message}", e)
            throw e
        } finally {
            runCatching { tts.stop() }.onFailure { Log.w(TAG, "Failed to stop TTS after export", it) }
            runCatching { tts.shutdown() }.onFailure { Log.w(TAG, "Failed to shut down TTS after export", it) }
            runCatching { tempDir.deleteRecursively() }.onFailure { Log.w(TAG, "Failed to delete temporary export files", it) }
        }
    }

    private suspend fun createReadyTts(voiceSettings: VoiceSettings): TextToSpeech = suspendCancellableCoroutine { continuation ->
        var engine: TextToSpeech? = null
        val requestedEngine = voiceSettings.enginePackage.ifBlank { null }
        val listener = TextToSpeech.OnInitListener { status ->
            val current = engine
            if (current == null) {
                if (continuation.isActive) continuation.resumeWithException(IllegalStateException("TTS engine was not created."))
            } else if (status == TextToSpeech.SUCCESS) {
                VoiceConfigurator.apply(current, voiceSettings)
                if (continuation.isActive) continuation.resume(current)
            } else {
                runCatching { current.shutdown() }
                if (continuation.isActive) continuation.resumeWithException(IllegalStateException("Could not initialize the TTS engine."))
            }
        }
        engine = if (requestedEngine == null) {
            TextToSpeech(context.applicationContext, listener)
        } else {
            TextToSpeech(context.applicationContext, listener, requestedEngine)
        }
        continuation.invokeOnCancellation { runCatching { engine.shutdown() } }
    }

    private suspend fun synthesizePart(
        tts: TextToSpeech,
        text: String,
        outputFile: File,
        utteranceId: String
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(doneId: String?) {
                if (doneId == utteranceId && continuation.isActive) continuation.resume(Unit)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(errorId: String?) {
                if (errorId == utteranceId && continuation.isActive) {
                    continuation.resumeWithException(IllegalStateException("TTS failed while exporting audio."))
                }
            }

            override fun onError(errorId: String?, errorCode: Int) {
                if (errorId == utteranceId && continuation.isActive) {
                    continuation.resumeWithException(IllegalStateException("TTS export failed with code $errorCode."))
                }
            }
        })

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        continuation.invokeOnCancellation {
            runCatching { tts.stop() }.onFailure { error -> Log.w(TAG, "Failed to stop TTS after export cancellation", error) }
            runCatching { outputFile.delete() }
        }
        val result = tts.synthesizeToFile(text, params, outputFile, utteranceId)
        if (result == TextToSpeech.ERROR && continuation.isActive) {
            continuation.resumeWithException(IllegalStateException("The TTS engine rejected audio export."))
        }
    }

    private fun splitForTts(text: String, maxLen: Int): List<String> {
        if (text.length <= maxLen) return listOf(text)
        val parts = mutableListOf<String>()
        var remaining = text.trim()
        while (remaining.length > maxLen) {
            val window = remaining.take(maxLen)
            val cut = listOf(
                window.lastIndexOf(". "),
                window.lastIndexOf("! "),
                window.lastIndexOf("? "),
                window.lastIndexOf("; "),
                window.lastIndexOf(", "),
                window.lastIndexOf(" ")
            ).filter { it > maxLen / 2 }.maxOrNull() ?: maxLen
            parts.add(remaining.take(cut + 1).trim())
            remaining = remaining.drop(cut + 1).trim()
        }
        if (remaining.isNotBlank()) parts.add(remaining)
        return parts.filter { it.isNotBlank() }
    }

    private fun safeFileName(title: String): String {
        return title
            .replace(Regex("[^A-Za-z0-9 _.-]"), " ")
            .replace(Regex("\\s+"), "_")
            .trim('_', '.', ' ')
            .ifBlank { "veritas_audio" }
            .take(50)
    }

    private data class ParsedWav(val formatChunk: ByteArray, val dataChunk: ByteArray)

    private object WavFile {
        fun read(file: File): ParsedWav? {
            val bytes = file.readBytes()
            if (bytes.size < 44) return null
            if (String(bytes, 0, 4, Charsets.US_ASCII) != "RIFF" || String(bytes, 8, 4, Charsets.US_ASCII) != "WAVE") return null

            var offset = 12
            var fmt: ByteArray? = null
            var data: ByteArray? = null
            while (offset + 8 <= bytes.size) {
                val id = String(bytes, offset, 4, Charsets.US_ASCII)
                val size = littleEndianInt(bytes, offset + 4)
                val start = offset + 8
                val end = (start + size).coerceAtMost(bytes.size)
                if (end < start) return null
                when (id) {
                    "fmt " -> fmt = bytes.copyOfRange(start, end)
                    "data" -> data = bytes.copyOfRange(start, end)
                }
                offset = end + (size % 2)
            }
            val formatChunk = fmt ?: return null
            val dataChunk = data ?: return null
            return ParsedWav(formatChunk, dataChunk)
        }

        fun write(file: File, formatChunk: ByteArray, dataChunks: List<ByteArray>) {
            val dataSize = dataChunks.sumOf { it.size }
            val formatPadding = if (formatChunk.size % 2 == 1) 1 else 0
            val riffSize = 4 + (8 + formatChunk.size + formatPadding) + (8 + dataSize)
            ByteArrayOutputStream().use { out ->
                out.writeAscii("RIFF")
                out.writeIntLE(riffSize)
                out.writeAscii("WAVE")
                out.writeAscii("fmt ")
                out.writeIntLE(formatChunk.size)
                out.write(formatChunk)
                if (formatChunk.size % 2 == 1) out.write(0)
                out.writeAscii("data")
                out.writeIntLE(dataSize)
                dataChunks.forEach { out.write(it) }
                file.writeBytes(out.toByteArray())
            }
        }

        private fun littleEndianInt(bytes: ByteArray, offset: Int): Int {
            return ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.LITTLE_ENDIAN).int
        }

        private fun ByteArrayOutputStream.writeAscii(value: String) {
            write(value.toByteArray(Charsets.US_ASCII))
        }

        private fun ByteArrayOutputStream.writeIntLE(value: Int) {
            write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array())
        }
    }

    private companion object {
        private const val TAG = "AudioExportManager"
    }
}
