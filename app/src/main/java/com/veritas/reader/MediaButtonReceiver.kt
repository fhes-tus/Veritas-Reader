package com.veritas.reader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MEDIA_BUTTON) return
        val serviceIntent = Intent(context, PlaybackService::class.java).apply {
            action = Intent.ACTION_MEDIA_BUTTON
            putExtras(intent)
        }
        runCatching {
            if (PlaybackStateStore.isForegroundActive) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }.onFailure {
            Log.w(TAG, "Could not dispatch media button to playback service", it)
        }
    }

    private companion object {
        private const val TAG = "MediaButtonReceiver"
    }
}
