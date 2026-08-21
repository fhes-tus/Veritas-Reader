package com.veritas.reader

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class VoiceRecordingState {
    IDLE,
    RECORDING,
    PLAYING
}

object VoiceNoteRecorder {
    private const val TAG = "VoiceNoteRecorder"

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var activeRecordingFile: File? = null
    private var recordingStartTime: Long = 0L

    private val _recordingState = MutableStateFlow(VoiceRecordingState.IDLE)
    val recordingState: StateFlow<VoiceRecordingState> = _recordingState.asStateFlow()

    private val _activeAudioPath = MutableStateFlow<String?>(null)
    val activeAudioPath: StateFlow<String?> = _activeAudioPath.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startRecording(context: Context, documentId: String, sentenceIndex: Int): File? {
        stopAll()
        try {
            val dir = File(context.filesDir, "voice_notes").apply { if (!exists()) mkdirs() }
            val file = File(dir, "note_${documentId.replace(Regex("[^a-zA-Z0-9_-]"), "_")}_${sentenceIndex}_${System.currentTimeMillis()}.m4a")
            
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            activeRecordingFile = file
            recordingStartTime = SystemClock.elapsedRealtime()
            _activeAudioPath.value = file.absolutePath
            _recordingState.value = VoiceRecordingState.RECORDING
            Log.d(TAG, "Started voice recording to: ${file.absolutePath}")
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start voice recording", e)
            stopAll()
            return null
        }
    }

    fun stopRecording(): Pair<String, Int>? {
        if (_recordingState.value != VoiceRecordingState.RECORDING) return null
        val durationMs = SystemClock.elapsedRealtime() - recordingStartTime
        val durationSeconds = (durationMs / 1000).toInt().coerceAtLeast(1)
        val file = activeRecordingFile

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaRecorder", e)
        } finally {
            mediaRecorder = null
            _recordingState.value = VoiceRecordingState.IDLE
        }

        return if (file != null && file.exists() && file.length() > 0) {
            Pair(file.absolutePath, durationSeconds)
        } else {
            null
        }
    }

    fun playAudio(path: String, onFinished: () -> Unit = {}) {
        stopAll()
        val file = File(path)
        if (!file.exists()) {
            Log.w(TAG, "Audio file does not exist: $path")
            return
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    stopAll()
                    onFinished()
                }
                start()
            }
            mediaPlayer = player
            _activeAudioPath.value = path
            _recordingState.value = VoiceRecordingState.PLAYING

            progressJob?.cancel()
            progressJob = scope.launch {
                while (isActive && _recordingState.value == VoiceRecordingState.PLAYING) {
                    val duration = player.duration
                    if (duration > 0) {
                        _playbackProgress.value = (player.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    }
                    delay(100)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio note", e)
            stopAll()
        }
    }

    fun stopPlayback() {
        if (_recordingState.value == VoiceRecordingState.PLAYING) {
            stopAll()
        }
    }

    fun stopAll() {
        progressJob?.cancel()
        progressJob = null
        _playbackProgress.value = 0f

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null

        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        _recordingState.value = VoiceRecordingState.IDLE
    }

    fun deleteAudioFile(path: String) {
        if (_activeAudioPath.value == path) {
            stopAll()
        }
        runCatching {
            val file = File(path)
            if (file.exists()) file.delete()
        }
    }
}
