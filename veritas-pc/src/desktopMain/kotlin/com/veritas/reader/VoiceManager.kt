package com.veritas.reader

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

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

object VoiceConfigurator {
    fun apply(tts: TextToSpeech, settings: VoiceSettings): String {
        val locale = settings.localeTag.takeIf { it.isNotBlank() }?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
        tts.setLanguage(locale)
        val selectedVoice = settings.voiceName.takeIf { it.isNotBlank() }
            ?.let { name -> tts.voices?.firstOrNull { it.name == name } }
        if (selectedVoice != null) {
            tts.voice = selectedVoice
        }
        return "Voice ready: ${settings.displayName}."
    }
}

object VoiceManager {
    fun loadInstalledEngines(context: Context): List<TtsEngineOption> {
        return listOf(TtsEngineOption("system_default", "System Default Engine"))
    }

    fun loadVoices(context: Context, enginePackage: String): List<TtsVoiceOption> {
        val list = mutableListOf<TtsVoiceOption>()
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("win")) {
            try {
                val cmd = arrayOf(
                    "powershell", "-NoProfile", "-Command",
                    "Add-Type -AssemblyName System.Speech; \$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; \$synth.GetInstalledVoices() | ForEach-Object { \$_.VoiceInfo.Name + '|' + \$_.VoiceInfo.Culture.Name }"
                )
                val process = ProcessBuilder(*cmd).start()
                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split("|")
                        if (parts.size >= 2) {
                            val name = parts[0].trim()
                            val localeTag = parts[1].trim()
                            if (name.isNotEmpty()) {
                                list.add(
                                    TtsVoiceOption(
                                        name = name,
                                        label = buildVoiceLabel(name, localeTag, false),
                                        localeTag = localeTag,
                                        requiresNetwork = false,
                                        quality = 300,
                                        latency = 0
                                    )
                                )
                            }
                        }
                    }
                }
                process.waitFor()
            } catch (e: Exception) {
                // Fallback
            }
        }
        
        if (list.isEmpty()) {
            list.add(
                TtsVoiceOption(
                    name = "System default",
                    label = "System default • L • System default voice",
                    localeTag = "en-US",
                    requiresNetwork = false,
                    quality = 300,
                    latency = 0
                )
            )
        }
        return list
    }

    fun previewVoice(context: Context, enginePackage: String, voiceName: String, text: String = "This is a preview of the selected voice.") {
        Thread {
            try {
                val os = System.getProperty("os.name").lowercase()
                if (os.contains("win")) {
                    val cleanText = text.replace("\"", "`\"").replace("'", "`'")
                    val selectVoiceCmd = if (voiceName.isNotBlank() && voiceName != "System default") {
                        "\$synth.SelectVoice('$voiceName'); "
                    } else ""
                    val command = "Add-Type -AssemblyName System.Speech; \$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; $selectVoiceCmd\$synth.Speak(\"$cleanText\")"
                    ProcessBuilder("powershell", "-NoProfile", "-Command", command).start().waitFor()
                } else if (os.contains("mac")) {
                    val voiceArg = if (voiceName.isNotBlank() && voiceName != "System default") arrayOf("-v", voiceName) else emptyArray()
                    val cleanText = text.replace("\"", "\\\"")
                    ProcessBuilder("say", *voiceArg, cleanText).start().waitFor()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }.start()
    }
}
