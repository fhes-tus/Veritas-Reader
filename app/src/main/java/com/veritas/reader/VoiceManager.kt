package com.veritas.reader

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine

data class TtsEngineOption(
    val packageName: String,
    val label: String
)

data class TtsVoiceOption(
    val name: String,
    val label: String,
    val localeTag: String,
    val requiresNetwork: Boolean,
    val quality: Int,
    val latency: Int
)

private fun inferVoiceGender(name: String): String {
    val normalized = name.lowercase(Locale.getDefault())
    val femaleMarkers = listOf(
        "female", "woman", "girl", "samantha", "susan", "zira", "victoria", "karen",
        "moira", "tessa", "veena", "joanna", "salli", "kimberly", "ivy", "nicole",
        "emma", "amy", "olivia", "aria", "jenny", "natasha", "elizabeth"
    )
    val maleMarkers = listOf(
        "male", "man", "boy", "david", "mark", "daniel", "alex", "fred", "tom",
        "brian", "matthew", "joey", "justin", "russell", "arthur", "guy"
    )
    return when {
        femaleMarkers.any { it in normalized } -> "F"
        maleMarkers.any { it in normalized } -> "M"
        else -> ""
    }
}

private fun voiceLanguageLabel(localeTag: String): String {
    if (localeTag.isBlank()) return "Default language"
    val locale = Locale.forLanguageTag(localeTag)
    val display = locale.getDisplayName(Locale.getDefault())
    return display.ifBlank { localeTag }
}

private fun buildVoiceLabel(name: String, localeTag: String, requiresNetwork: Boolean): String {
    val networkLabel = if (requiresNetwork) "N" else "L"
    val genderLabel = inferVoiceGender(name)
    val baseLabel = listOf(voiceLanguageLabel(localeTag), networkLabel, genderLabel, name)
        .filter { it.isNotBlank() }
        .joinToString(" • ")
    return if (requiresNetwork) "[HQ] $baseLabel" else baseLabel
}

object VoiceManager {
    fun loadInstalledEngines(context: Context): List<TtsEngineOption> {
        val pm = context.packageManager
        val intent = Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
        return pm.queryIntentServices(intent, 0)
            .mapNotNull { resolveInfo ->
                val serviceInfo = resolveInfo.serviceInfo ?: return@mapNotNull null
                val label = resolveInfo.loadLabel(pm).toString().ifBlank { serviceInfo.packageName }
                TtsEngineOption(packageName = serviceInfo.packageName, label = label)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    suspend fun loadVoices(context: Context, enginePackage: String): List<TtsVoiceOption> = suspendCancellableCoroutine { continuation ->
        val engineDeferred = CompletableDeferred<TextToSpeech>()
        val listener = TextToSpeech.OnInitListener { status ->
            val current = engineDeferred.getCompleted()
            if (status == TextToSpeech.SUCCESS) {
                val voices = current.voices.orEmpty()
                    .map { voice ->
                        val localeTag = voice.locale?.toLanguageTag().orEmpty()
                        TtsVoiceOption(
                            name = voice.name,
                            label = buildVoiceLabel(voice.name, localeTag, voice.isNetworkConnectionRequired),
                            localeTag = localeTag,
                            requiresNetwork = voice.isNetworkConnectionRequired,
                            quality = voice.quality,
                            latency = voice.latency
                        )
                    }
                    .distinctBy { it.name }
                    .sortedWith(
                        compareByDescending<TtsVoiceOption> { it.requiresNetwork }
                            .thenByDescending { it.localeTag.startsWith("en", ignoreCase = true) }
                            .thenBy { it.localeTag.lowercase(Locale.getDefault()) }
                            .thenBy { it.name.lowercase(Locale.getDefault()) }
                    )
                runCatching { current.shutdown() }
                if (continuation.isActive) continuation.resume(voices)
                else runCatching { current.shutdown() } // cleanup if already cancelled
            } else {
                runCatching { current.shutdown() }
                if (continuation.isActive) continuation.resumeWithException(IllegalStateException("Could not initialize this voice engine."))
            }
        }
        val engine = if (enginePackage.isBlank()) {
            TextToSpeech(context.applicationContext, listener)
        } else {
            TextToSpeech(context.applicationContext, listener, enginePackage)
        }
        engineDeferred.complete(engine)
        continuation.invokeOnCancellation { runCatching { engine.shutdown() } }
    }

    fun previewVoice(context: Context, enginePackage: String, voiceName: String, text: String = "This is a preview of the selected voice.") {
        var engine: TextToSpeech? = null
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.voices?.firstOrNull { it.name == voiceName }?.let { voice ->
                    engine?.voice = voice
                }
                engine?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        runCatching { engine?.shutdown() }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        runCatching { engine?.shutdown() }
                    }
                })
                engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "preview")
            } else {
                runCatching { engine?.shutdown() }
            }
        }
        engine = if (enginePackage.isBlank()) {
            TextToSpeech(context.applicationContext, listener)
        } else {
            TextToSpeech(context.applicationContext, listener, enginePackage)
        }
    }
}

object VoiceConfigurator {
    fun apply(tts: TextToSpeech, settings: VoiceSettings): String {
        val locale = settings.localeTag.takeIf { it.isNotBlank() }?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
        val languageStatus = tts.setLanguage(locale)
        val selectedVoice = settings.voiceName.takeIf { it.isNotBlank() }
            ?.let { name -> tts.voices?.firstOrNull { it.name == name } }
        if (selectedVoice != null) {
            tts.voice = selectedVoice
        }
        return when (languageStatus) {
            TextToSpeech.LANG_MISSING_DATA -> "Voice data is missing for ${locale.displayLanguage}."
            TextToSpeech.LANG_NOT_SUPPORTED -> "${locale.displayLanguage} is not supported by this TTS engine."
            else -> "Voice ready: ${settings.displayName}."
        }
    }
}
