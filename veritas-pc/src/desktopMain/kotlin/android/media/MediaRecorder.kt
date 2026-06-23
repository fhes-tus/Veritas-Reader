package android.media

import android.content.Context

class MediaRecorder(context: Context? = null) {
    fun setAudioSource(source: Int) {}
    fun setOutputFormat(format: Int) {}
    fun setAudioEncoder(encoder: Int) {}
    fun setOutputFile(path: String) {}
    fun prepare() {}
    fun start() {}
    fun stop() {}
    fun release() {}

    object AudioSource {
        const val MIC = 1
    }

    object OutputFormat {
        const val THREE_GPP = 1
    }

    object AudioEncoder {
        const val AMR_NB = 1
    }
}
