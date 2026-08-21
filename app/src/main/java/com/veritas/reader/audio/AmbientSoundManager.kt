package com.veritas.reader.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

/**
 * Ambient soundscapes available to blend under reading narration.
 */
enum class AmbientSoundscape(val id: String, val title: String, val emoji: String, val assetPath: String) {
    NONE("none", "Off", "🔇", ""),
    RAIN("rain", "Gentle Rain", "🌧️", "ambient/rain.wav"),
    WHITE_NOISE("white_noise", "Air & Breeze", "💨", "ambient/breeze.wav"),
    BROWN_NOISE("brown_noise", "Deep Brown Noise", "🌊", "ambient/brown_noise.wav"),
    CAMPFIRE("campfire", "Cozy Campfire", "🔥", "ambient/campfire.wav"),
    LO_FI_PULSE("lo_fi", "Lo-Fi Focus Chords", "🎧", "ambient/lofi.wav");

    companion object {
        fun fromId(id: String): AmbientSoundscape =
            entries.firstOrNull { it.id == id } ?: NONE
    }
}

/**
 * Studio-grade, zero-crash ambient soundscape engine.
 * Plays high-fidelity studio loop recordings directly through Android MediaPlayer with seamless looping.
 */
object AmbientSoundManager {
    private const val TAG = "AmbientSoundManager"

    @Volatile
    private var mediaPlayer: MediaPlayer? = null
    private val lock = Any()
    private var appContext: Context? = null

    var currentSoundscape: AmbientSoundscape = AmbientSoundscape.NONE
        private set

    var volume: Float = 0.35f
        set(value) {
            field = value.coerceIn(0f, 1f)
            synchronized(lock) {
                try {
                    mediaPlayer?.setVolume(field, field)
                } catch (_: Exception) {}
            }
        }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun play(soundscape: AmbientSoundscape, context: Context? = null) {
        synchronized(lock) {
            context?.let { appContext = it.applicationContext }
            val ctx = appContext ?: return

            if (currentSoundscape == soundscape && mediaPlayer?.isPlaying == true) return
            stopInternal()
            if (soundscape == AmbientSoundscape.NONE) return

            currentSoundscape = soundscape

            try {
                ctx.assets.openFd(soundscape.assetPath).use { afd ->
                    val player = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        isLooping = true
                        setVolume(volume, volume)
                        prepare()
                        start()
                    }
                    mediaPlayer = player
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start studio ambient sound '${soundscape.title}': ${e.message}", e)
                stopInternal()
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            stopInternal()
        }
    }

    private fun stopInternal() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (_: Exception) {}
        }
        mediaPlayer = null
        currentSoundscape = AmbientSoundscape.NONE
    }
}
