package android.media

class MediaPlayer {
    val currentPosition: Int get() = 0
    val duration: Int get() = 0
    fun pause() {}
    fun start() {}
    fun setDataSource(path: String) {}
    fun prepare() {}
    fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {}
    fun release() {}
}
