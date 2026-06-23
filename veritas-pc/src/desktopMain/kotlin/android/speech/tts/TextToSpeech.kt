package android.speech.tts

import android.content.Context
import android.os.Bundle
import java.io.File
import java.util.Locale
import java.util.UUID
import javax.swing.SwingUtilities

class TextToSpeech {
    var voices: Set<Voice>? = null
    var voice: Voice? = null

    private var activeProcess: Process? = null
    private var activeThread: Thread? = null
    private var progressListener: UtteranceProgressListener? = null
    private var currentLanguage: Locale = Locale.getDefault()
    private var currentRate: Float = 1.0f
    private var currentPitch: Float = 1.0f
    private var currentVoiceName: String? = null

    constructor(context: Context, listener: OnInitListener) : this(context, listener, "")
    
    constructor(context: Context, listener: OnInitListener, engine: String) {
        // Query system voices on Windows
        val voiceList = mutableSetOf<Voice>()
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("win")) {
            try {
                val cmd = arrayOf(
                    "powershell", "-NoProfile", "-Command",
                    "Add-Type -AssemblyName System.Speech; \$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; \$synth.GetInstalledVoices() | ForEach-Object { \$_.VoiceInfo.Name }"
                )
                val process = ProcessBuilder(*cmd).start()
                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val name = line.trim()
                        if (name.isNotEmpty()) {
                            voiceList.add(Voice(name))
                        }
                    }
                }
                process.waitFor()
            } catch (e: Exception) {
                voiceList.add(Voice("Microsoft David Desktop"))
            }
        } else {
            voiceList.add(Voice("System default"))
        }
        voices = voiceList
        voice = voiceList.firstOrNull()

        // Notify initializer asynchronously on the EDT
        Thread {
            try { Thread.sleep(50L) } catch (e: Exception) {}
            SwingUtilities.invokeLater {
                listener.onInit(SUCCESS)
            }
        }.start()
    }

    fun setLanguage(locale: Locale): Int {
        currentLanguage = locale
        return SUCCESS
    }

    fun setSpeechRate(rate: Float): Int {
        currentRate = rate
        return SUCCESS
    }

    fun setPitch(pitch: Float): Int {
        currentPitch = pitch
        return SUCCESS
    }

    fun speak(text: CharSequence, queueMode: Int, params: Bundle?, utteranceId: String): Int {
        if (queueMode == QUEUE_FLUSH) {
            stop()
        }
        val listener = progressListener
        val txt = text.toString()
        val voiceName = voice?.name ?: currentVoiceName
        val rate = currentRate

        activeThread = Thread {
            var tempFile: File? = null
            try {
                SwingUtilities.invokeLater {
                    listener?.onStart(utteranceId)
                }

                // Write text to a temp file to avoid command-line parsing, escaping and encoding issues
                val userHome = System.getProperty("user.home")
                val dataDir = File(userHome, ".veritas_reader").apply { mkdirs() }
                tempFile = File(dataDir, "temp_speech_${UUID.randomUUID()}.txt")
                tempFile.writeText(txt, Charsets.UTF_8)
                val tempFilePath = tempFile.absolutePath

                val os = System.getProperty("os.name").lowercase()
                val process = if (os.contains("win")) {
                    val psRate = ((rate - 1.0f) * 10).toInt().coerceIn(-10, 10)
                    val selectVoiceCmd = if (voiceName != null && voiceName != "System default") {
                        "\$synth.SelectVoice('$voiceName'); "
                    } else ""
                    val command = "Add-Type -AssemblyName System.Speech; \$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; \$synth.Rate = $psRate; $selectVoiceCmd\$text = Get-Content -Path '$tempFilePath' -Raw -Encoding UTF8; \$synth.Speak(\$text)"
                    ProcessBuilder("powershell", "-NoProfile", "-Command", command).start()
                } else if (os.contains("mac")) {
                    val rateArg = (rate * 175).toInt()
                    val voiceArg = if (voiceName != null && voiceName != "System default") arrayOf("-v", voiceName) else emptyArray()
                    ProcessBuilder("say", *voiceArg, "-r", rateArg.toString(), "-f", tempFilePath).start()
                } else {
                    ProcessBuilder("espeak", "-f", tempFilePath).start()
                }
                
                synchronized(this) {
                    activeProcess = process
                }
                
                val exitCode = process.waitFor()
                synchronized(this) {
                    if (activeProcess == process) {
                        activeProcess = null
                    }
                }
                
                SwingUtilities.invokeLater {
                    if (exitCode == 0) {
                        listener?.onDone(utteranceId)
                    } else {
                        listener?.onError(utteranceId)
                    }
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    listener?.onError(utteranceId)
                }
            } finally {
                runCatching { tempFile?.delete() }
            }
        }.apply { start() }
        
        return SUCCESS
    }

    fun synthesizeToFile(text: CharSequence, params: Bundle?, file: File, utteranceId: String): Int = SUCCESS

    fun setOnUtteranceProgressListener(listener: UtteranceProgressListener) {
        this.progressListener = listener
    }

    fun stop(): Int {
        synchronized(this) {
            activeProcess?.destroy()
            activeProcess = null
        }
        activeThread?.interrupt()
        activeThread = null
        return SUCCESS
    }

    fun shutdown() {
        stop()
    }

    fun interface OnInitListener {
        fun onInit(status: Int)
    }

    class Voice(val name: String) {
        val locale: Locale get() = Locale.getDefault()
        val isNetworkConnectionRequired: Boolean get() = false
        val quality: Int get() = 300
        val latency: Int get() = 0
    }

    object Engine {
        const val KEY_PARAM_UTTERANCE_ID = "utteranceId"
        const val ACTION_INSTALL_TTS_DATA = "android.speech.tts.engine.INSTALL_TTS_DATA"
    }

    companion object {
        const val SUCCESS = 0
        const val ERROR = -1
        const val QUEUE_FLUSH = 0
        const val LANG_MISSING_DATA = -1
        const val LANG_NOT_SUPPORTED = -2

        fun getMaxSpeechInputLength(): Int = 4000
    }
}

abstract class UtteranceProgressListener {
    abstract fun onStart(utteranceId: String?)
    abstract fun onDone(utteranceId: String?)
    @Deprecated("Deprecated")
    abstract fun onError(utteranceId: String?)
    open fun onError(utteranceId: String?, errorCode: Int) {}
    open fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {}
}
