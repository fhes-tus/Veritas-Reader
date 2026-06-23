package com.veritas.reader

import android.content.Context

object NoteReminderScheduler {
    const val CHANNEL_ID = "veritas_note_reminders"
    const val EXTRA_NOTE_ID = "note_id"
    const val EXTRA_TITLE = "note_title"
    const val EXTRA_BODY = "note_body"

    fun schedule(context: Context, noteId: String, title: String, body: String, triggerAtMillis: Long) {}
    fun cancel(context: Context, noteId: String) {}
    fun ensureChannel(context: Context) {}
}

class NoteReminderReceiver
