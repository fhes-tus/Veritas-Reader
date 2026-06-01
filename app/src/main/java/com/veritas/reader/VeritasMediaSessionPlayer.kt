package com.veritas.reader

import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@OptIn(UnstableApi::class)
internal class VeritasMediaSessionPlayer(
    applicationLooper: Looper,
    private val snapshot: () -> PlaybackSnapshot,
    private val controller: Controller
) : SimpleBasePlayer(applicationLooper) {
    interface Controller {
        fun play()
        fun pause()
        fun stop()
        fun next()
        fun previous()
        fun seekTo(positionMs: Long)
        fun setPlaybackParameters(rate: Float, pitch: Float)
    }

    data class PlaybackSnapshot(
        val documentId: String,
        val title: String,
        val sourceLabel: String,
        val sectionLabel: String,
        val isPlaying: Boolean,
        val isForegroundActive: Boolean,
        val chunkCount: Int,
        val currentIndex: Int,
        val durationMs: Long,
        val positionMs: Long,
        val rate: Float,
        val pitch: Float
    )

    private val availableCommands = Player.Commands.Builder()
        .add(Player.COMMAND_PREPARE)
        .add(Player.COMMAND_PLAY_PAUSE)
        .add(Player.COMMAND_STOP)
        .add(Player.COMMAND_SEEK_BACK)
        .add(Player.COMMAND_SEEK_FORWARD)
        .add(Player.COMMAND_SEEK_TO_PREVIOUS)
        .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
        .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
        .add(Player.COMMAND_SEEK_TO_NEXT)
        .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
        .add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
        .add(Player.COMMAND_GET_METADATA)
        .add(Player.COMMAND_GET_TIMELINE)
        .add(Player.COMMAND_SET_SPEED_AND_PITCH)
        .build()

    override fun handlePrepare(): ListenableFuture<Any> {
        return Futures.immediateFuture(Any())
    }

    override fun getState(): State {
        val current = snapshot()
        if (current.documentId.isBlank() && current.chunkCount <= 0) {
            return State.Builder()
                .setAvailableCommands(availableCommands)
                .setPlaybackState(Player.STATE_IDLE)
                .setPlayWhenReady(false, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
                .setPlaybackSuppressionReason(Player.PLAYBACK_SUPPRESSION_REASON_NONE)
                .build()
        }

        val durationMs = current.durationMs.takeIf { it > 0L } ?: C.TIME_UNSET
        val metadata = MediaMetadata.Builder()
            .setTitle(current.title.ifBlank { "Veritas Reader" })
            .setArtist(current.sourceLabel.ifBlank { "Text-to-speech reader" })
            .setAlbumTitle(current.sectionLabel)
            .build()
        val mediaItem = MediaItem.Builder()
            .setMediaId(current.documentId.ifBlank { "veritas-reader" })
            .setMediaMetadata(metadata)
            .build()
        val itemData = MediaItemData.Builder(current.documentId.ifBlank { "veritas-reader" })
            .setMediaItem(mediaItem)
            .setMediaMetadata(metadata)
            .setDurationUs(if (durationMs == C.TIME_UNSET) C.TIME_UNSET else durationMs * 1000L)
            .setIsSeekable(durationMs != C.TIME_UNSET)
            .build()

        return State.Builder()
            .setAvailableCommands(availableCommands)
            .setPlaylist(listOf(itemData))
            .setCurrentMediaItemIndex(0)
            .setPlaybackState(if (current.isForegroundActive || current.isPlaying) Player.STATE_READY else Player.STATE_IDLE)
            .setPlayWhenReady(current.isPlaying, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .setPlaybackSuppressionReason(Player.PLAYBACK_SUPPRESSION_REASON_NONE)
            .setContentPositionMs(current.positionMs.coerceAtLeast(0L))
            .setPlaybackParameters(PlaybackParameters(current.rate, current.pitch))
            .setPlaylistMetadata(metadata)
            .build()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<Any> {
        if (playWhenReady) controller.play() else controller.pause()
        return Futures.immediateFuture(Any())
    }

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: Int): ListenableFuture<Any> {
        when (seekCommand) {
            Player.COMMAND_SEEK_BACK,
            Player.COMMAND_SEEK_TO_PREVIOUS,
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> controller.previous()
            Player.COMMAND_SEEK_FORWARD,
            Player.COMMAND_SEEK_TO_NEXT,
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> controller.next()
            else -> controller.seekTo(positionMs)
        }
        return Futures.immediateFuture(Any())
    }

    override fun handleSetPlaybackParameters(playbackParameters: PlaybackParameters): ListenableFuture<Any> {
        controller.setPlaybackParameters(
            playbackParameters.speed.coerceIn(0.5f, 2.0f),
            playbackParameters.pitch.coerceIn(0.7f, 1.4f)
        )
        return Futures.immediateFuture(Any())
    }

    override fun handleStop(): ListenableFuture<Any> {
        controller.stop()
        return Futures.immediateFuture(Any())
    }

    fun notifyStateChanged() {
        invalidateState()
    }
}
