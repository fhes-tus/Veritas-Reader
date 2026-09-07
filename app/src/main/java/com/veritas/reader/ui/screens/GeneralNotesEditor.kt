package com.veritas.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritas.reader.GeneralNote
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date
import android.media.MediaRecorder
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import com.veritas.reader.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.*
import android.graphics.BitmapFactory
import android.os.Build
import android.os.SystemClock
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.net.Uri
import androidx.compose.ui.text.style.TextOverflow
import androidx.activity.compose.BackHandler

enum class NotesToolbarMenu {
    NONE,
    FORMATTING,
    ATTACHMENTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralNotesEditor(
    note: GeneralNote?,
    onSave: (String, String, String?, Boolean, Boolean, String?, String?, Long?, Boolean, List<String>) -> Unit,
    onDelete: (String) -> Unit,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val initialRawContent = note?.content ?: ""
    val initialContentWithAttachments = remember(note) {
        val sb = StringBuilder()
        // If note has legacy stand-alone imageUrl and it's not already in the content markdown, prepend it
        val legacyImg = note?.imageUrl?.takeIf { it.isNotBlank() }
        if (legacyImg != null && !initialRawContent.contains(legacyImg)) {
            sb.append("![image](").append(legacyImg).append(")\n")
        }
        sb.append(initialRawContent)
        // If note has legacy standalone audioUrls not yet present in content markdown, append them
        note?.allAudioUrls?.forEach { aUrl ->
            if (aUrl.isNotBlank() && !sb.contains(aUrl)) {
                if (sb.isNotEmpty() && !sb.endsWith("\n")) sb.append("\n")
                sb.append("[audio](").append(aUrl).append(")\n")
            }
        }
        sb.toString()
    }
    var title by remember { mutableStateOf(note?.title ?: "") }
    val blocks = remember(note?.id) {
        mutableStateListOf<NoteBlock>().apply {
            addAll(VeritasNoteEditing.parseNoteBlocks(initialContentWithAttachments))
        }
    }
    var focusedBlockIndex by remember { mutableIntStateOf(0) }
    var contentText by remember { mutableStateOf(VeritasNoteEditing.serializeNoteBlocks(blocks)) }
    var contentValue by remember {
        val firstText = blocks.filterIsInstance<NoteBlock.Text>().firstOrNull()?.value ?: TextFieldValue(contentText)
        mutableStateOf(firstText)
    }
    var editVersion by remember { mutableIntStateOf(0) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var noteColor by remember { mutableStateOf(note?.color) }
    var isPinned by remember { mutableStateOf(note?.pinned ?: false) }
    var isChecklist by remember { mutableStateOf(note?.isChecklist ?: false) }
    var imageUrl by remember { mutableStateOf(note?.imageUrl) }
    var audioUrls by remember { mutableStateOf(note?.allAudioUrls ?: emptyList()) }
    var reminderAt by remember { mutableStateOf(note?.reminderAt) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }
    var confirmDeleteNote by remember { mutableStateOf(false) }
    var showExactAlarmPrompt by remember { mutableStateOf(false) }

    fun syncBlocksToContent() {
        contentText = VeritasNoteEditing.serializeNoteBlocks(blocks)
        val firstImg = blocks.filterIsInstance<NoteBlock.Image>().firstOrNull()?.path
        if (firstImg != null) imageUrl = firstImg
        val noteAudios = blocks.filterIsInstance<NoteBlock.Audio>().map { it.path }
        if (noteAudios.isNotEmpty()) {
            audioUrls = (noteAudios + audioUrls).distinct()
        }
        hasUnsavedChanges = true
        editVersion++
    }
    var viewingImagePath by remember { mutableStateOf<String?>(null) }
    var viewingVideoPath by remember { mutableStateOf<String?>(null) }

    fun insertAttachmentAtCursor(attachment: NoteBlock) {
        if (blocks.isEmpty()) {
            blocks.add(attachment)
            val newText = NoteBlock.Text(TextFieldValue(""))
            blocks.add(newText)
            focusedBlockIndex = 1
            contentValue = newText.value
            syncBlocksToContent()
            return
        }

        val targetIndex = focusedBlockIndex.coerceIn(0, blocks.lastIndex)
        val currentBlock = blocks.getOrNull(targetIndex)
        if (currentBlock is NoteBlock.Text) {
            val tfv = currentBlock.value
            val cursorPos = tfv.selection.start.coerceIn(0, tfv.text.length)
            val textBefore = tfv.text.substring(0, cursorPos).trimEnd('\n')
            val textAfter = tfv.text.substring(cursorPos).trimStart('\n')

            if (textBefore.isNotEmpty()) {
                currentBlock.value = TextFieldValue(textBefore, TextRange(textBefore.length))
                blocks[targetIndex] = NoteBlock.Text(TextFieldValue(textBefore, TextRange(textBefore.length)))
                blocks.add(targetIndex + 1, attachment)
                val remainingBlock = NoteBlock.Text(TextFieldValue(textAfter, TextRange(0)))
                blocks.add(targetIndex + 2, remainingBlock)
                focusedBlockIndex = targetIndex + 2
                contentValue = remainingBlock.value
            } else {
                blocks.add(targetIndex, attachment)
                val remainingBlock = NoteBlock.Text(TextFieldValue(textAfter, TextRange(0)))
                blocks[targetIndex + 1] = remainingBlock
                focusedBlockIndex = targetIndex + 1
                contentValue = remainingBlock.value
            }
        } else {
            val insertAt = (targetIndex + 1).coerceIn(0, blocks.size)
            blocks.add(insertAt, attachment)
            if (insertAt + 1 >= blocks.size || blocks[insertAt + 1] !is NoteBlock.Text) {
                blocks.add(insertAt + 1, NoteBlock.Text(TextFieldValue("")))
            }
            focusedBlockIndex = (insertAt + 1).coerceAtMost(blocks.lastIndex)
            val nextText = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
            if (nextText != null) contentValue = nextText.value
        }
        syncBlocksToContent()
    }

    fun removeBlockAt(idx: Int) {
        if (idx !in blocks.indices) return
        val removed = blocks.removeAt(idx)
        if (removed is NoteBlock.Image && imageUrl == removed.path) {
            imageUrl = blocks.filterIsInstance<NoteBlock.Image>().firstOrNull()?.path
        } else if (removed is NoteBlock.Audio) {
            audioUrls = audioUrls.filter { it != removed.path }
        }
        val beforeIdx = idx - 1
        if (beforeIdx >= 0 && beforeIdx < blocks.size && blocks[beforeIdx] is NoteBlock.Text &&
            idx < blocks.size && blocks[idx] is NoteBlock.Text
        ) {
            val prevText = (blocks[beforeIdx] as NoteBlock.Text).value
            val nextText = (blocks[idx] as NoteBlock.Text).value
            val merged = TextFieldValue(
                text = prevText.text + nextText.text,
                selection = TextRange(prevText.text.length)
            )
            (blocks[beforeIdx] as NoteBlock.Text).value = merged
            blocks[beforeIdx] = NoteBlock.Text(merged)
            blocks.removeAt(idx)
            focusedBlockIndex = beforeIdx
            contentValue = merged
        } else {
            focusedBlockIndex = idx.coerceAtMost(blocks.lastIndex)
            val cur = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
            if (cur != null) contentValue = cur.value
        }
        if (blocks.isEmpty()) {
            val emptyText = NoteBlock.Text(TextFieldValue(""))
            blocks.add(emptyText)
            focusedBlockIndex = 0
            contentValue = emptyText.value
        }
        syncBlocksToContent()
    }

    fun moveBlockUp(idx: Int) {
        if (idx > 0 && idx < blocks.size) {
            val item = blocks.removeAt(idx)
            blocks.add(idx - 1, item)
            syncBlocksToContent()
            editVersion++
            hasUnsavedChanges = true
        }
    }

    fun moveBlockDown(idx: Int) {
        if (idx >= 0 && idx < blocks.size - 1) {
            val item = blocks.removeAt(idx)
            blocks.add(idx + 1, item)
            syncBlocksToContent()
            editVersion++
            hasUnsavedChanges = true
        }
    }

    fun copyAttachment(path: String) {
        com.veritas.reader.copyTextToClipboard(context, "Attachment", path)
    }

    // Reminders fall back to inexact alarms without the "Alarms & reminders" permission on
    // Android 12+, arriving late with no explanation. Ask once when a reminder is set.
    LaunchedEffect(reminderAt) {
        if (reminderAt != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPrompt = true
            }
        }
    }
    if (showExactAlarmPrompt) {
        AlertDialog(
            onDismissRequest = { showExactAlarmPrompt = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.veritas.reader.R.string.reminder_exact_title)) },
            text = { Text(androidx.compose.ui.res.stringResource(com.veritas.reader.R.string.reminder_exact_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmPrompt = false
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        runCatching {
                            context.startActivity(
                                Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                            )
                        }
                    }
                }) { Text(androidx.compose.ui.res.stringResource(com.veritas.reader.R.string.reminder_exact_allow)) }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmPrompt = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.veritas.reader.R.string.reminder_exact_later))
                }
            }
        )
    }
    if (confirmDeleteNote && note != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteNote = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.veritas.reader.R.string.delete_note_title)) },
            text = { Text(androidx.compose.ui.res.stringResource(com.veritas.reader.R.string.delete_note_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteNote = false
                    onDelete(note.id)
                }) {
                    Text(
                        androidx.compose.ui.res.stringResource(com.veritas.reader.R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteNote = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.veritas.reader.R.string.action_cancel))
                }
            }
        )
    }

    var expandedMenu by remember { mutableStateOf(NotesToolbarMenu.NONE) }
    var triggerImagePickerOnStart by remember { mutableStateOf(false) }

    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }
    val items = remember {
        val list = mutableStateListOf<Pair<Boolean, TextFieldValue>>()
        val lines = contentText.split("\n").filter { it.isNotBlank() }
        lines.forEach { line ->
            val checked = line.startsWith("[x]")
            val text = line.removePrefix("[ ] ").removePrefix("[x] ")
            list.add(checked to TextFieldValue(text, TextRange(text.length)))
        }
        if (list.isEmpty()) {
            list.add(false to TextFieldValue(""))
        }
        list
    }

    fun updateChecklistString() {
        contentText = items.joinToString("\n") { (checked, tfv) ->
            if (checked) "[x] ${tfv.text}" else "[ ] ${tfv.text}"
        }
    }

    LaunchedEffect(isChecklist) {
        if (isChecklist) {
            val lines = contentText.split("\n").filter { it.isNotBlank() }
            items.clear()
            lines.forEach { line ->
                val checked = line.startsWith("[x]")
                val text = line.removePrefix("[ ] ").removePrefix("[x] ")
                items.add(checked to TextFieldValue(text, TextRange(text.length)))
            }
            if (items.isEmpty()) {
                items.add(false to TextFieldValue(""))
            }
        }
    }

    LaunchedEffect(note) {
        if (note == null) {
            val act = context as? MainActivity
            val state = act?.viewModel?.uiState?.value
            if (state != null) {
                if (state.noteEditorChecklistOnStart) {
                    isChecklist = true
                    if (contentText.isBlank()) {
                        contentText = "[ ] "
                    }
                }
                if (state.noteEditorImageOnStart) {
                    triggerImagePickerOnStart = true
                }
                if (state.noteEditorReminderOnStart) {
                    showReminderMenu = true
                }
                act.viewModel.clearNoteEditorFlags()
            }
        }
    }

    // Undo / redo history for the rich-text body.
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }

    fun pushHistory() {
        val curText = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
        if (curText != null) {
            undoStack.add(curText.value)
            if (undoStack.size > 200) undoStack.removeAt(0)
            redoStack.clear()
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordFilePath by remember { mutableStateOf<String?>(null) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var recordingAmplitudes by remember { mutableStateOf(listOf<Int>()) }
    var recordingInsertTargetIndex by remember { mutableIntStateOf(-1) }
    var recordingTickerJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun stopRecording() {
        try {
            recordingTickerJob?.cancel()
            recordingTickerJob = null
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            val newPath = recordFilePath
            if (!newPath.isNullOrBlank() && File(newPath).exists()) {
                insertAttachmentAtCursor(NoteBlock.Audio(newPath))
                audioUrls = (audioUrls + newPath).distinct()
                Toast.makeText(context, "Voice memo inserted in note", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to stop recording: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            recordFilePath = null
            recordingDurationSec = 0
            recordingAmplitudes = emptyList()
            recordingInsertTargetIndex = -1
        }
    }

    fun cancelRecording() {
        try {
            recordingTickerJob?.cancel()
            recordingTickerJob = null
            mediaRecorder?.apply {
                runCatching { stop() }
                release()
            }
            mediaRecorder = null
            isRecording = false
            recordFilePath?.let { path ->
                val f = File(path)
                if (f.exists()) f.delete()
            }
            Toast.makeText(context, "Recording discarded", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recordFilePath = null
            recordingDurationSec = 0
            recordingAmplitudes = emptyList()
            recordingInsertTargetIndex = -1
        }
    }

    fun startRecording(insertAtIndex: Int = -1) {
        try {
            val mediaDir = File(context.filesDir, "notes_media")
            if (!mediaDir.exists()) mediaDir.mkdirs()
            val fileName = "rec_${System.currentTimeMillis()}.3gp"
            val file = File(mediaDir, fileName)
            val path = file.absolutePath
            recordFilePath = path
            recordingInsertTargetIndex = if (insertAtIndex >= 0) insertAtIndex else focusedBlockIndex

            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(path)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
            recordingDurationSec = 0
            recordingAmplitudes = emptyList()

            recordingTickerJob?.cancel()
            val startTime = SystemClock.elapsedRealtime()
            recordingTickerJob = coroutineScope.launch {
                while (isRecording && mediaRecorder != null) {
                    val elapsed = ((SystemClock.elapsedRealtime() - startTime) / 1000).toInt()
                    recordingDurationSec = elapsed
                    val amp = runCatching { mediaRecorder?.maxAmplitude ?: 0 }.getOrDefault(0)
                    recordingAmplitudes = (recordingAmplitudes + amp).takeLast(60)
                    delay(100)
                }
            }

            Toast.makeText(context, "Recording started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start recording: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording(recordingInsertTargetIndex)
        } else {
            Toast.makeText(context, "Microphone permission is required to record audio notes", Toast.LENGTH_SHORT).show()
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val mediaDir = File(context.filesDir, "notes_media")
                    if (!mediaDir.exists()) mediaDir.mkdirs()
                    val fileName = "img_${System.currentTimeMillis()}.jpg"
                    val file = File(mediaDir, fileName)
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    imageUrl = file.absolutePath

                    insertAttachmentAtCursor(NoteBlock.Image(file.absolutePath))
                    Toast.makeText(context, "Image inserted in note", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to attach image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val mediaDir = File(context.filesDir, "notes_media")
                    if (!mediaDir.exists()) mediaDir.mkdirs()
                    val fileName = "vid_${System.currentTimeMillis()}.mp4"
                    val file = File(mediaDir, fileName)
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()

                    insertAttachmentAtCursor(NoteBlock.Video(file.absolutePath))
                    Toast.makeText(context, "Video attached to note", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to attach video: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openVideo(path: String) {
        try {
            val file = File(path)
            if (!file.exists()) {
                Toast.makeText(context, "Video file not found", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Play video"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Cannot open video: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareMediaFile(path: String, mimeType: String) {
        try {
            val file = File(path)
            if (!file.exists()) return
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to share: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(triggerImagePickerOnStart) {
        if (triggerImagePickerOnStart) {
            imageLauncher.launch("image/*")
            triggerImagePickerOnStart = false
        }
    }

    var activePlayingAudioPath by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableStateOf(0f) }
    var currentPosition by remember { mutableStateOf("0:00") }
    var audioDurations by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    fun seekAudio(fraction: Float, path: String) {
        try {
            val clamped = fraction.coerceIn(0f, 1f)
            if (activePlayingAudioPath == path && mediaPlayer != null) {
                val dur = mediaPlayer?.duration ?: 0
                if (dur > 0) {
                    val targetMs = (clamped * dur).toInt()
                    mediaPlayer?.seekTo(targetMs)
                    playProgress = clamped
                    val curSecs = targetMs / 1000
                    currentPosition = String.format(Locale.US, "%d:%02d", curSecs / 60, curSecs % 60)
                }
            } else {
                // If seeking while paused or not currently active, initialize and seek
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                val player = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    val dur = duration.coerceAtLeast(1)
                    val targetMs = (clamped * dur).toInt()
                    seekTo(targetMs)
                    setOnCompletionListener {
                        isPlaying = false
                        playProgress = 0f
                        currentPosition = "0:00"
                        activePlayingAudioPath = null
                    }
                }
                mediaPlayer = player
                activePlayingAudioPath = path
                playProgress = clamped
                val curSecs = player.currentPosition / 1000
                currentPosition = String.format(Locale.US, "%d:%02d", curSecs / 60, curSecs % 60)
                player.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(audioUrls) {
        val durations = mutableMapOf<String, String>()
        audioUrls.forEach { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(path)
                    val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durMs = durStr?.toLongOrNull() ?: 0L
                    val durSecs = durMs / 1000
                    durations[path] = String.format(Locale.US, "%d:%02d", durSecs / 60, durSecs % 60)
                    retriever.release()
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        audioDurations = durations
    }

    LaunchedEffect(isPlaying, activePlayingAudioPath) {
        if (isPlaying && activePlayingAudioPath != null) {
            while (isPlaying && mediaPlayer != null) {
                try {
                    val current = mediaPlayer?.currentPosition ?: 0
                    val duration = mediaPlayer?.duration ?: 1
                    playProgress = current.toFloat() / duration.coerceAtLeast(1)

                    val curSecs = current / 1000
                    val durSecs = duration / 1000
                    currentPosition = String.format("%d:%02d", curSecs / 60, curSecs % 60)
                } catch (e: Exception) {
                    // ignore
                }
                kotlinx.coroutines.delay(200)
            }
        }
    }

    fun togglePlayPause(path: String) {
        try {
            if (activePlayingAudioPath == path && isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            } else if (activePlayingAudioPath == path && mediaPlayer != null) {
                mediaPlayer?.start()
                isPlaying = true
            } else {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                playProgress = 0f
                currentPosition = "0:00"

                val player = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    setOnCompletionListener {
                        isPlaying = false
                        playProgress = 0f
                        currentPosition = "0:00"
                        activePlayingAudioPath = null
                    }
                }
                mediaPlayer = player
                activePlayingAudioPath = path
                player.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Playback error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeAudio(path: String) {
        try {
            if (activePlayingAudioPath == path) {
                mediaPlayer?.release()
                mediaPlayer = null
                isPlaying = false
                activePlayingAudioPath = null
                playProgress = 0f
                currentPosition = "0:00"
            }
            audioUrls = audioUrls.filter { it != path }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            mediaRecorder = null
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // ── Continuous auto-save ────────────────────────────────────────────
    // Debounced save: fires 800 ms after the user stops typing.
    // This serializes ONLY when typing pauses, leaving keystrokes completely unblocked.
    LaunchedEffect(editVersion, title, noteColor, isPinned, isChecklist, imageUrl, audioUrls, reminderAt) {
        if (editVersion == 0 && !hasUnsavedChanges) return@LaunchedEffect
        kotlinx.coroutines.delay(800)
        val contentToSave = if (isChecklist) {
            items.joinToString("\n") { (checked, tfv) ->
                if (checked) "[x] ${tfv.text}" else "[ ] ${tfv.text}"
            }
        } else {
            VeritasNoteEditing.serializeNoteBlocks(blocks)
        }
        if (title.isNotBlank() || contentToSave.isNotBlank() || imageUrl != null || audioUrls.isNotEmpty()) {
            onSave(title, contentToSave, noteColor, isPinned, isChecklist, imageUrl, audioUrls.firstOrNull(), reminderAt, false, audioUrls)
            hasUnsavedChanges = false
        }
    }
    // Heartbeat save: fires every 2 seconds while the editor is open.
    // Ensures the note is saved even if no new edits occur (e.g. the user
    // only scrolled or left the phone idle with the editor open).
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2_000)
            if (hasUnsavedChanges) {
                val currentContent = if (isChecklist) {
                    items.joinToString("\n") { (checked, tfv) ->
                        if (checked) "[x] ${tfv.text}" else "[ ] ${tfv.text}"
                    }
                } else {
                    VeritasNoteEditing.serializeNoteBlocks(blocks)
                }
                if (title.isNotBlank() || currentContent.isNotBlank() || imageUrl != null || audioUrls.isNotEmpty()) {
                    onSave(title, currentContent, noteColor, isPinned, isChecklist, imageUrl, audioUrls.firstOrNull(), reminderAt, false, audioUrls)
                    hasUnsavedChanges = false
                }
            }
        }
    }

    // Safe pastel colors for note cards (matches Keep style)
    val noteColors = listOf(
        "Default" to null,
        "Red" to "#FFCDD2",
        "Orange" to "#FFE0B2",
        "Yellow" to "#FFF9C4",
        "Green" to "#C8E6C9",
        "Teal" to "#B2DFDB",
        "Blue" to "#B3E5FC",
        "DarkBlue" to "#C5CAE9",
        "Purple" to "#D1C4E9",
        "Pink" to "#F8BBD0",
        "Brown" to "#D7CCC8"
    )

    // Wrap the current selection (or insert empty markers at the caret) with an inline
    // marker, or unwrap if the selection is already wrapped. The markers themselves are
    // hidden by RichTextVisualTransformation so the user only sees the styled result.
    fun applyInlineMarker(marker: String) {
        if (isChecklist) return
        pushHistory()
        val textBlock = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
        if (textBlock != null) {
            val newValue = VeritasNoteEditing.toggleInlineMarker(textBlock.value, marker)
            blocks[focusedBlockIndex] = NoteBlock.Text(newValue)
            contentValue = newValue
            syncBlocksToContent()
        } else {
            val newValue = VeritasNoteEditing.toggleInlineMarker(contentValue, marker)
            contentValue = newValue
            contentText = newValue.text
        }
    }

    // Toggle a line-level prefix (heading / list marker) across every line touched by the
    // selection. Logic lives in VeritasNoteEditing so it is unit-tested.
    fun applyLinePrefix(prefix: String) {
        if (isChecklist) return
        pushHistory()
        val textBlock = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
        if (textBlock != null) {
            val newValue = VeritasNoteEditing.toggleLinePrefix(textBlock.value, prefix)
            blocks[focusedBlockIndex] = NoteBlock.Text(newValue)
            contentValue = newValue
            syncBlocksToContent()
        } else {
            val newValue = VeritasNoteEditing.toggleLinePrefix(contentValue, prefix)
            contentValue = newValue
            contentText = newValue.text
        }
    }

    fun applyIndent() {
        if (isChecklist) return
        pushHistory()
        val textBlock = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
        if (textBlock != null) {
            val newValue = VeritasNoteEditing.applyIndent(textBlock.value)
            blocks[focusedBlockIndex] = NoteBlock.Text(newValue)
            contentValue = newValue
            syncBlocksToContent()
        } else {
            val newValue = VeritasNoteEditing.applyIndent(contentValue)
            contentValue = newValue
            contentText = newValue.text
        }
    }

    fun applyOutdent() {
        if (isChecklist) return
        pushHistory()
        val textBlock = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
        if (textBlock != null) {
            val newValue = VeritasNoteEditing.applyOutdent(textBlock.value)
            blocks[focusedBlockIndex] = NoteBlock.Text(newValue)
            contentValue = newValue
            syncBlocksToContent()
        } else {
            val newValue = VeritasNoteEditing.applyOutdent(contentValue)
            contentValue = newValue
            contentText = newValue.text
        }
    }

    fun cycleHeading() {
        if (isChecklist) return
        pushHistory()
        val textBlock = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
        if (textBlock != null) {
            val newValue = VeritasNoteEditing.cycleHeading(textBlock.value)
            blocks[focusedBlockIndex] = NoteBlock.Text(newValue)
            contentValue = newValue
            syncBlocksToContent()
        } else {
            val newValue = VeritasNoteEditing.cycleHeading(contentValue)
            contentValue = newValue
            contentText = newValue.text
        }
    }

    fun toggleQuote() {
        if (isChecklist) return
        pushHistory()
        val textBlock = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
        if (textBlock != null) {
            val newValue = VeritasNoteEditing.toggleQuotePrefix(textBlock.value)
            blocks[focusedBlockIndex] = NoteBlock.Text(newValue)
            contentValue = newValue
            syncBlocksToContent()
        } else {
            val newValue = VeritasNoteEditing.toggleQuotePrefix(contentValue)
            contentValue = newValue
            contentText = newValue.text
        }
    }

    fun toggleTask() {
        if (isChecklist) return
        pushHistory()
        val textBlock = blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text
        if (textBlock != null) {
            val newValue = VeritasNoteEditing.toggleTaskCheckbox(textBlock.value)
            blocks[focusedBlockIndex] = NoteBlock.Text(newValue)
            contentValue = newValue
            syncBlocksToContent()
        } else {
            val newValue = VeritasNoteEditing.toggleTaskCheckbox(contentValue)
            contentValue = newValue
            contentText = newValue.text
        }
    }

    // Continue list markers when Enter is pressed; terminate the list on an empty item.
    // Diff-based detection in VeritasNoteEditing tolerates IME batch edits (autocorrect +
    // newline committed together), which used to break numbering after a couple of items.
    fun handleSmartNewline(old: TextFieldValue, candidate: TextFieldValue): TextFieldValue {
        return VeritasNoteEditing.continueListOnNewline(old, candidate)
    }

    fun shareNote() {
        val body = if (isChecklist) {
            contentText.lineSequence().joinToString("\n") { line ->
                when {
                    line.startsWith("[x]") -> "☑ " + line.removePrefix("[x]").trim()
                    line.startsWith("[ ]") -> "☐ " + line.removePrefix("[ ]").trim()
                    else -> line
                }
            }
        } else {
            RichTextFormatter.stripMarkup(contentValue.text)
        }
        val plain = buildString {
            if (title.isNotBlank()) {
                append(title)
                append("\n\n")
            }
            append(body)
        }.trim()
        if (plain.isBlank()) {
            Toast.makeText(context, "Nothing to share yet", Toast.LENGTH_SHORT).show()
            return
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Veritas note" })
            putExtra(Intent.EXTRA_TEXT, plain)
        }
        runCatching { context.startActivity(Intent.createChooser(send, "Share note")) }
    }

    fun pickReminderDateTime() {
        val now = Calendar.getInstance()
        val base = reminderAt?.let { Calendar.getInstance().apply { timeInMillis = it } } ?: now
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                        }
                        if (cal.timeInMillis <= System.currentTimeMillis()) {
                            Toast.makeText(context, "Pick a time in the future", Toast.LENGTH_SHORT).show()
                        } else {
                            reminderAt = cal.timeInMillis
                        }
                    },
                    base.get(Calendar.HOUR_OF_DAY),
                    base.get(Calendar.MINUTE),
                    false
                ).show()
            },
            base.get(Calendar.YEAR),
            base.get(Calendar.MONTH),
            base.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = now.timeInMillis
        }.show()
    }

    val cardBgColor = noteColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.surface
    val onCardColor = if (noteColor != null) Color.Black else MaterialTheme.colorScheme.onSurface

    val richTextTransformation = remember(onCardColor) { RichTextVisualTransformation(onCardColor) }

    fun performSave() {
        val finalContent = if (isChecklist) {
            items.joinToString("\n") { (checked, tfv) ->
                if (checked) "[x] ${tfv.text}" else "[ ] ${tfv.text}"
            }
        } else {
            VeritasNoteEditing.serializeNoteBlocks(blocks)
        }
        onSave(title, finalContent, noteColor, isPinned, isChecklist, imageUrl, audioUrls.firstOrNull(), reminderAt, true, audioUrls)
        hasUnsavedChanges = false
    }

    BackHandler {
        val hasAnyText = if (isChecklist) items.any { it.second.text.isNotBlank() } else blocks.any { it is NoteBlock.Text && it.value.text.isNotBlank() }
        if (title.isNotBlank() || hasAnyText || imageUrl != null || audioUrls.isNotEmpty()) {
            performSave()
        } else {
            onDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        val hasAnyText = if (isChecklist) items.any { it.second.text.isNotBlank() } else blocks.any { it is NoteBlock.Text && it.value.text.isNotBlank() }
                        if (title.isNotBlank() || hasAnyText || imageUrl != null || audioUrls.isNotEmpty()) {
                            performSave()
                        } else {
                            onDismiss()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Pin Button
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f)
                        )
                    }

                    // Reminder Button
                    Box {
                        IconButton(onClick = { showReminderMenu = true }) {
                            Icon(
                                imageVector = if (reminderAt != null) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationAdd,
                                contentDescription = "Reminder",
                                tint = if (reminderAt != null) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f)
                            )
                        }
                        DropdownMenu(expanded = showReminderMenu, onDismissRequest = { showReminderMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Later today (3 hrs)") },
                                onClick = {
                                    reminderAt = System.currentTimeMillis() + 3 * 60 * 60 * 1000L
                                    showReminderMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Tomorrow morning (9 AM)") },
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, 1)
                                        set(Calendar.HOUR_OF_DAY, 9)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    reminderAt = cal.timeInMillis
                                    showReminderMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.WbSunny, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Pick date & time…") },
                                onClick = {
                                    showReminderMenu = false
                                    pickReminderDateTime()
                                },
                                leadingIcon = { Icon(Icons.Filled.EditCalendar, contentDescription = null) }
                            )
                            if (reminderAt != null) {
                                DropdownMenuItem(
                                    text = { Text("Remove reminder") },
                                    onClick = {
                                        reminderAt = null
                                        showReminderMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Filled.NotificationsOff, contentDescription = null) }
                                )
                            }
                        }
                    }

                    // Save (Archive) Button
                    IconButton(onClick = { performSave() }) {
                        Icon(
                            imageVector = Icons.Filled.Archive,
                            contentDescription = "Save Note",
                            tint = onCardColor.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBgColor,
                    titleContentColor = onCardColor,
                    actionIconContentColor = onCardColor.copy(alpha = 0.8f),
                    navigationIconContentColor = onCardColor
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBgColor)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                if (showColorPicker) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        noteColors.forEach { (_, hex) ->
                            val color = hex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.surface
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(color, shape = RoundedCornerShape(18.dp))
                                    .clickable {
                                        noteColor = hex
                                        showColorPicker = false
                                    }
                                    .border(
                                        width = if (noteColor == hex) 2.dp else 1.dp,
                                        color = if (noteColor == hex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            )
                        }
                    }
                }

                Surface(
                    tonalElevation = 3.dp,
                    color = cardBgColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val showUndoRedo = true
                    
                    when (expandedMenu) {
                        NotesToolbarMenu.FORMATTING -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { expandedMenu = NotesToolbarMenu.NONE }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close Menu", tint = onCardColor)
                                }
                                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp), color = onCardColor.copy(alpha = 0.2f))
                                
                                FormatToolbarButton(Icons.Filled.Title, "Heading", onCardColor) { cycleHeading() }
                                FormatToolbarButton(Icons.Filled.FormatBold, "Bold", onCardColor) { applyInlineMarker("**") }
                                FormatToolbarButton(Icons.Filled.FormatItalic, "Italic", onCardColor) { applyInlineMarker("*") }
                                FormatToolbarButton(Icons.Filled.FormatUnderlined, "Underline", onCardColor) { applyInlineMarker("__") }
                                FormatToolbarButton(Icons.Filled.StrikethroughS, "Strikethrough", onCardColor) { applyInlineMarker("~~") }
                                FormatToolbarButton(Icons.Filled.FormatQuote, "Quote", onCardColor) { toggleQuote() }
                                FormatToolbarButton(Icons.Filled.Code, "Monospace", onCardColor) { applyInlineMarker("`") }
                                FormatToolbarButton(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet list", onCardColor) { applyLinePrefix("- ") }
                                FormatToolbarButton(Icons.Filled.FormatListNumbered, "Numbered list", onCardColor) { applyLinePrefix("1. ") }
                                FormatToolbarButton(Icons.Filled.CheckBoxOutlineBlank, "Task", onCardColor) { toggleTask() }
                                FormatToolbarButton(Icons.AutoMirrored.Filled.FormatIndentDecrease, "Outdent", onCardColor) { applyOutdent() }
                                FormatToolbarButton(Icons.AutoMirrored.Filled.FormatIndentIncrease, "Indent", onCardColor) { applyIndent() }
                            }
                        }
                        NotesToolbarMenu.ATTACHMENTS -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { expandedMenu = NotesToolbarMenu.NONE }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close Menu", tint = onCardColor)
                                }
                                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp), color = onCardColor.copy(alpha = 0.2f))

                                // Checklist Toggle
                                IconButton(onClick = {
                                    val activeTfv = (blocks.getOrNull(focusedBlockIndex) as? NoteBlock.Text)?.value ?: contentValue
                                    if (activeTfv.selection.min != activeTfv.selection.max) {
                                        toggleTask()
                                    } else {
                                        isChecklist = !isChecklist
                                        if (isChecklist && contentText.isBlank()) {
                                            contentText = "[ ] "
                                        }
                                    }
                                    expandedMenu = NotesToolbarMenu.NONE
                                }) {
                                    Icon(
                                        imageVector = if (isChecklist) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                                        contentDescription = "Checklist Toggle",
                                        tint = if (isChecklist) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f)
                                    )
                                }

                                // Attach Image
                                IconButton(onClick = {
                                    imageLauncher.launch("image/*")
                                    expandedMenu = NotesToolbarMenu.NONE
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = "Attach Image",
                                        tint = onCardColor.copy(alpha = 0.8f)
                                    )
                                }

                                // Attach Video
                                IconButton(onClick = {
                                    videoLauncher.launch("video/*")
                                    expandedMenu = NotesToolbarMenu.NONE
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.VideoLibrary,
                                        contentDescription = "Attach Video",
                                        tint = onCardColor.copy(alpha = 0.8f)
                                    )
                                }

                                // Record Audio
                                IconButton(onClick = {
                                    expandedMenu = NotesToolbarMenu.NONE
                                    if (isRecording) {
                                        stopRecording()
                                    } else {
                                        val targetIdx = (focusedBlockIndex + 1).coerceIn(0, blocks.size)
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            startRecording(targetIdx)
                                        } else {
                                            recordingInsertTargetIndex = targetIdx
                                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Outlined.Mic,
                                        contentDescription = if (isRecording) "Stop Recording" else "Record Audio",
                                        tint = if (isRecording) MaterialTheme.colorScheme.error else onCardColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        NotesToolbarMenu.NONE -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { expandedMenu = NotesToolbarMenu.ATTACHMENTS }) {
                                        Icon(Icons.Filled.AddBox, contentDescription = "Add Attachments", tint = onCardColor.copy(alpha = 0.8f))
                                    }
                                    IconButton(onClick = {
                                        if (isRecording) {
                                            stopRecording()
                                        } else {
                                            val targetIdx = (focusedBlockIndex + 1).coerceIn(0, blocks.size)
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                startRecording(targetIdx)
                                            } else {
                                                recordingInsertTargetIndex = targetIdx
                                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Outlined.Mic,
                                            contentDescription = if (isRecording) "Stop Recording" else "Record Audio",
                                            tint = if (isRecording) MaterialTheme.colorScheme.error else (if (audioUrls.isNotEmpty()) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f))
                                        )
                                    }
                                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                                        Icon(Icons.Outlined.Palette, contentDescription = "Color Picker", tint = onCardColor.copy(alpha = 0.8f))
                                    }
                                    if (!isChecklist) {
                                        IconButton(onClick = { expandedMenu = NotesToolbarMenu.FORMATTING }) {
                                            Icon(Icons.Filled.TextFields, contentDescription = "Formatting Tools", tint = onCardColor.copy(alpha = 0.8f))
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (showUndoRedo) {
                                        IconButton(
                                            onClick = {
                                                if (undoStack.isNotEmpty()) {
                                                    val prev = undoStack.removeLast()
                                                    redoStack.add(contentValue)
                                                    contentValue = prev
                                                    if (isChecklist) {
                                                        contentText = prev.text
                                                    }
                                                }
                                            },
                                            enabled = undoStack.isNotEmpty()
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Undo,
                                                contentDescription = "Undo",
                                                tint = onCardColor.copy(alpha = if (undoStack.isNotEmpty()) 0.85f else 0.3f)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (redoStack.isNotEmpty()) {
                                                    val next = redoStack.removeLast()
                                                    undoStack.add(contentValue)
                                                    contentValue = next
                                                    if (isChecklist) {
                                                        contentText = next.text
                                                    }
                                                }
                                            },
                                            enabled = redoStack.isNotEmpty()
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Redo,
                                                contentDescription = "Redo",
                                                tint = onCardColor.copy(alpha = if (redoStack.isNotEmpty()) 0.85f else 0.3f)
                                            )
                                        }
                                    }

                                    var showOverflow by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { showOverflow = true }) {
                                            Icon(Icons.Filled.MoreVert, contentDescription = "More Options", tint = onCardColor.copy(alpha = 0.8f))
                                        }
                                        DropdownMenu(
                                            expanded = showOverflow,
                                            onDismissRequest = { showOverflow = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Share") },
                                                onClick = {
                                                    showOverflow = false
                                                    shareNote()
                                                },
                                                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) }
                                            )
                                            if (note != null) {
                                                DropdownMenuItem(
                                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                                    onClick = {
                                                        showOverflow = false
                                                        confirmDeleteNote = true
                                                    },
                                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Make a Copy") },
                                                    onClick = {
                                                        showOverflow = false
                                                        onCopy()
                                                    },
                                                    leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(cardBgColor)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reminder chip
            if (reminderAt != null) {
                val label = remember(reminderAt) {
                    SimpleDateFormat("EEE, d MMM • h:mm a", Locale.getDefault()).format(Date(reminderAt!!))
                }
                AssistChip(
                    onClick = { showReminderMenu = true },
                    label = { Text("Reminder: $label") },
                    leadingIcon = { Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove reminder",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { reminderAt = null }
                        )
                    }
                )
            }

            // Sleek live recording panel with pulse indicator, timer, live Canvas waveform, and clean controls
            if (isRecording) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "recPulse")
                            val pulse by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(700, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "recPulseVal"
                            )
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(20.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size((16 * pulse).dp)
                                        .background(Color.Red.copy(alpha = 0.35f * pulse), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .background(Color.Red, CircleShape)
                                )
                            }
                            val timerFormatted = String.format(
                                Locale.US,
                                "%02d:%02d",
                                recordingDurationSec / 60,
                                recordingDurationSec % 60
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Recording Voice Memo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = timerFormatted,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = { cancelRecording() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Discard",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Button(
                                onClick = { stopRecording() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Insert", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Live audio waveform amplitude visualization using smooth Canvas bars
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val amps = recordingAmplitudes
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasW = size.width
                                val canvasH = size.height
                                val barW = 2.5.dp.toPx()
                                val barGap = 2.dp.toPx()
                                val step = barW + barGap
                                val count = (canvasW / step).toInt().coerceAtLeast(10)
                                val recentAmps = if (amps.size > count) amps.takeLast(count) else amps
                                val padCount = (count - recentAmps.size).coerceAtLeast(0)
                                val fullAmps = List(padCount) { 0 } + recentAmps
                                val maxAmp = (fullAmps.maxOrNull() ?: 1).coerceAtLeast(1)
                                val centerY = canvasH / 2f
                                val startX = (canvasW - (count * step - barGap)) / 2f

                                for (i in 0 until count) {
                                    val amp = fullAmps[i]
                                    val norm = (amp.toFloat() / maxAmp.toFloat()).coerceIn(0.08f, 1f)
                                    val barH = (norm * (canvasH - 4.dp.toPx())).coerceAtLeast(3.dp.toPx())
                                    val halfH = barH / 2f
                                    val x = startX + i * step + barW / 2f
                                    drawLine(
                                        color = primaryColor,
                                        start = androidx.compose.ui.geometry.Offset(x, centerY - halfH),
                                        end = androidx.compose.ui.geometry.Offset(x, centerY + halfH),
                                        strokeWidth = barW,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = onCardColor,
                        fontWeight = FontWeight.Bold
                    ),
                    placeholder = {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = onCardColor.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = onCardColor
                    )
                )

                if (!isChecklist) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        blocks.forEachIndexed { index, block ->
                            when (block) {
                                is NoteBlock.Text -> {
                                    NoteTextBlockItem(
                                        block = block,
                                        onValueChange = { processed ->
                                            val prev = block.value
                                            val isBoundary = processed.text.length < prev.text.length ||
                                                (processed.text.length - prev.text.length) > 1 ||
                                                processed.text.lastOrNull()?.isWhitespace() == true
                                            if (isBoundary && prev.text != processed.text) {
                                                undoStack.add(prev)
                                                if (undoStack.size > 200) undoStack.removeAt(0)
                                                redoStack.clear()
                                            }
                                            block.value = processed
                                            contentValue = processed
                                            focusedBlockIndex = index
                                            hasUnsavedChanges = true
                                            editVersion++
                                        },
                                        onFocus = {
                                            focusedBlockIndex = index
                                            contentValue = block.value
                                        },
                                        onBackspaceAtStart = {
                                            if (index > 0) {
                                                val prevBlock = blocks.getOrNull(index - 1)
                                                if (prevBlock !is NoteBlock.Text) {
                                                    removeBlockAt(index - 1)
                                                    true
                                                } else false
                                            } else false
                                        },
                                        visualTransformation = richTextTransformation,
                                        onCardColor = onCardColor,
                                        isOnlyBlock = blocks.size == 1
                                    )
                                }
                                is NoteBlock.Image -> {
                                    val bitmap = remember(block.path) {
                                        loadNoteBitmap(context, block.path)
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .clickable { viewingImagePath = block.path },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = onCardColor.copy(alpha = 0.05f))
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap.asImageBitmap(),
                                                    contentDescription = "Inline Note Image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text("Error loading image", color = onCardColor)
                                                }
                                            }
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.55f),
                                                shape = RoundedCornerShape(50),
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Visibility,
                                                        contentDescription = "View image",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text("Tap to view", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                                }
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                NoteAttachmentMenu(
                                                    onCopy = { copyAttachment(block.path) },
                                                    canMoveUp = index > 0,
                                                    onMoveUp = { moveBlockUp(index) },
                                                    canMoveDown = index < blocks.size - 1,
                                                    onMoveDown = { moveBlockDown(index) },
                                                    onDelete = { removeBlockAt(index) }
                                                )
                                            }
                                        }
                                    }
                                }
                                is NoteBlock.Audio -> {
                                    val audioPath = block.path
                                    val isThisPlaying = isPlaying && activePlayingAudioPath == audioPath
                                    val memoTotalDur = remember(audioPath) {
                                        try {
                                            val file = File(audioPath)
                                            if (file.exists()) {
                                                val retriever = android.media.MediaMetadataRetriever()
                                                retriever.setDataSource(audioPath)
                                                val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                                                val durMs = durStr?.toLongOrNull() ?: 0L
                                                val totalSec = durMs / 1000
                                                retriever.release()
                                                String.format(Locale.US, "%d:%02d", totalSec / 60, totalSec % 60)
                                            } else "0:00"
                                        } catch (_: Exception) {
                                            "0:00"
                                        }
                                    }
                                    val dynamicDurationLabel = if (isThisPlaying) "$currentPosition / $memoTotalDur" else memoTotalDur

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AudioVoiceMemoWaveform(
                                            durationLabel = dynamicDurationLabel,
                                            isPlaying = isThisPlaying,
                                            progress = if (isThisPlaying) playProgress else 0f,
                                            onTogglePlay = { togglePlayPause(audioPath) },
                                            onSeek = { fraction -> seekAudio(fraction, audioPath) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        NoteAttachmentMenu(
                                            onCopy = { copyAttachment(audioPath) },
                                            canMoveUp = index > 0,
                                            onMoveUp = { moveBlockUp(index) },
                                            canMoveDown = index < blocks.size - 1,
                                            onMoveDown = { moveBlockDown(index) },
                                            onDelete = { removeBlockAt(index) }
                                        )
                                    }
                                }
                                is NoteBlock.Video -> {
                                    val videoPath = block.path
                                    val videoThumbnail = remember(videoPath) {
                                        try {
                                            android.media.ThumbnailUtils.createVideoThumbnail(
                                                videoPath,
                                                android.provider.MediaStore.Images.Thumbnails.MINI_KIND
                                            )
                                        } catch (_: Exception) {
                                            null
                                        }
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(190.dp)
                                            .clickable { viewingVideoPath = videoPath },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = onCardColor.copy(alpha = 0.08f)),
                                        border = BorderStroke(1.dp, onCardColor.copy(alpha = 0.2f))
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (videoThumbnail != null) {
                                                Image(
                                                    bitmap = videoThumbnail.asImageBitmap(),
                                                    contentDescription = "Video Thumbnail",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.35f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Movie,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                }
                                            }

                                            // Play overlay button
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.65f),
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .align(Alignment.Center)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Filled.PlayArrow,
                                                        contentDescription = "Play Video",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            }

                                            Surface(
                                                color = Color.Black.copy(alpha = 0.55f),
                                                shape = RoundedCornerShape(50),
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.PlayCircle,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text("Tap to play", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                NoteAttachmentMenu(
                                                    onCopy = { copyAttachment(videoPath) },
                                                    canMoveUp = index > 0,
                                                    onMoveUp = { moveBlockUp(index) },
                                                    canMoveDown = index < blocks.size - 1,
                                                    onMoveDown = { moveBlockDown(index) },
                                                    onDelete = { removeBlockAt(index) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val coroutineScope = rememberCoroutineScope()
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val mediaBlocks = remember(blocks, editVersion) { blocks.filter { it !is NoteBlock.Text } }
                        if (mediaBlocks.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                mediaBlocks.forEach { mediaBlock ->
                                    val index = blocks.indexOf(mediaBlock)
                                    when (mediaBlock) {
                                        is NoteBlock.Image -> {
                                            val bitmap = remember(mediaBlock.path) {
                                                loadNoteBitmap(context, mediaBlock.path)
                                            }
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .clickable { viewingImagePath = mediaBlock.path },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = onCardColor.copy(alpha = 0.05f))
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    if (bitmap != null) {
                                                        Image(
                                                            bitmap = bitmap.asImageBitmap(),
                                                            contentDescription = "Inline Note Image",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    } else {
                                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                            Text("Error loading image", color = onCardColor)
                                                        }
                                                    }
                                                    Surface(
                                                        color = Color.Black.copy(alpha = 0.55f),
                                                        shape = RoundedCornerShape(50),
                                                        modifier = Modifier
                                                            .align(Alignment.BottomStart)
                                                            .padding(8.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Filled.Visibility,
                                                                contentDescription = "View image",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Text("Tap to view", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        NoteAttachmentMenu(
                                                            onCopy = { copyAttachment(mediaBlock.path) },
                                                            canMoveUp = index > 0,
                                                            onMoveUp = { moveBlockUp(index) },
                                                            canMoveDown = index < blocks.size - 1,
                                                            onMoveDown = { moveBlockDown(index) },
                                                            onDelete = { removeBlockAt(index) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        is NoteBlock.Audio -> {
                                            val audioPath = mediaBlock.path
                                            val isThisPlaying = isPlaying && activePlayingAudioPath == audioPath
                                            val memoTotalDur = remember(audioPath) {
                                                try {
                                                    val file = File(audioPath)
                                                    if (file.exists()) {
                                                        val retriever = android.media.MediaMetadataRetriever()
                                                        retriever.setDataSource(audioPath)
                                                        val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                                                        val durMs = durStr?.toLongOrNull() ?: 0L
                                                        val totalSec = durMs / 1000
                                                        retriever.release()
                                                        String.format(Locale.US, "%d:%02d", totalSec / 60, totalSec % 60)
                                                    } else "0:00"
                                                } catch (_: Exception) {
                                                    "0:00"
                                                }
                                            }
                                            val dynamicDurationLabel = if (isThisPlaying) "$currentPosition / $memoTotalDur" else memoTotalDur

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                AudioVoiceMemoWaveform(
                                                    durationLabel = dynamicDurationLabel,
                                                    isPlaying = isThisPlaying,
                                                    progress = if (isThisPlaying) playProgress else 0f,
                                                    onTogglePlay = { togglePlayPause(audioPath) },
                                                    onSeek = { fraction -> seekAudio(fraction, audioPath) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                NoteAttachmentMenu(
                                                    onCopy = { copyAttachment(audioPath) },
                                                    canMoveUp = index > 0,
                                                    onMoveUp = { moveBlockUp(index) },
                                                    canMoveDown = index < blocks.size - 1,
                                                    onMoveDown = { moveBlockDown(index) },
                                                    onDelete = { removeBlockAt(index) }
                                                )
                                            }
                                        }
                                        is NoteBlock.Video -> {
                                            val videoPath = mediaBlock.path
                                            val videoThumbnail = remember(videoPath) {
                                                try {
                                                    android.media.ThumbnailUtils.createVideoThumbnail(
                                                        videoPath,
                                                        android.provider.MediaStore.Images.Thumbnails.MINI_KIND
                                                    )
                                                } catch (_: Exception) {
                                                    null
                                                }
                                            }
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(190.dp)
                                                    .clickable { viewingVideoPath = videoPath },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = onCardColor.copy(alpha = 0.08f)),
                                                border = BorderStroke(1.dp, onCardColor.copy(alpha = 0.2f))
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    if (videoThumbnail != null) {
                                                        Image(
                                                            bitmap = videoThumbnail.asImageBitmap(),
                                                            contentDescription = "Video Thumbnail",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(Color.Black.copy(alpha = 0.35f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Filled.Movie,
                                                                contentDescription = null,
                                                                tint = Color.White.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(48.dp)
                                                            )
                                                        }
                                                    }
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color.Black.copy(alpha = 0.65f),
                                                        modifier = Modifier
                                                            .size(52.dp)
                                                            .align(Alignment.Center)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                Icons.Filled.PlayArrow,
                                                                contentDescription = "Play Video",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(30.dp)
                                                            )
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        NoteAttachmentMenu(
                                                            onCopy = { copyAttachment(videoPath) },
                                                            canMoveUp = index > 0,
                                                            onMoveUp = { moveBlockUp(index) },
                                                            canMoveDown = index < blocks.size - 1,
                                                            onMoveDown = { moveBlockDown(index) },
                                                            onDelete = { removeBlockAt(index) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                        items.forEachIndexed { idx, (checked, tfv) ->
                            val focusRequester = focusRequesters.getOrPut(idx) { FocusRequester() }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        items[idx] = isChecked to tfv
                                        updateChecklistString()
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                                BasicTextField(
                                    value = tfv,
                                    onValueChange = { newTfv ->
                                        items[idx] = checked to newTfv
                                        hasUnsavedChanges = true
                                        editVersion++
                                    },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = onCardColor,
                                        textDecoration = if (checked) TextDecoration.LineThrough else null
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown) {
                                                val cursor = tfv.selection.start
                                                val textBefore = tfv.text.substring(0, cursor)
                                                val textAfter = tfv.text.substring(cursor)
                                                
                                                items[idx] = checked to tfv.copy(text = textBefore, selection = TextRange(textBefore.length))
                                                val newItem = false to TextFieldValue(textAfter, TextRange(0))
                                                items.add(idx + 1, newItem)
                                                updateChecklistString()
                                                
                                                coroutineScope.launch {
                                                    delay(50)
                                                    focusRequesters[idx + 1]?.requestFocus()
                                                }
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                )
                                IconButton(
                                    onClick = {
                                        if (items.size > 1) {
                                            items.removeAt(idx)
                                            updateChecklistString()
                                        } else {
                                            items[0] = false to TextFieldValue("")
                                            updateChecklistString()
                                        }
                                    },
                                    // 40dp touch target (was 24dp); icon stays small.
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove item",
                                        tint = onCardColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                items.add(false to TextFieldValue(""))
                                updateChecklistString()
                                val nextIdx = items.lastIndex
                                coroutineScope.launch {
                                    delay(50)
                                    focusRequesters[nextIdx]?.requestFocus()
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = onCardColor)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Item")
                                Text("Add list item")
                            }
                        }
                    }
                }
            }
        }
    }

    viewingImagePath?.let { imgPath ->
        NoteImageViewerDialog(
            path = imgPath,
            onShare = { path -> shareMediaFile(path, "image/*") },
            onDismiss = { viewingImagePath = null }
        )
    }

    viewingVideoPath?.let { vidPath ->
        NoteVideoViewerDialog(
            path = vidPath,
            onOpenExternal = { path -> openVideo(path) },
            onShare = { path -> shareMediaFile(path, "video/*") },
            onDismiss = { viewingVideoPath = null }
        )
    }
}

@Composable
private fun NoteImageViewerDialog(
    path: String,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(path) {
        loadNoteBitmap(context, path)
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Full view image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.8f, 5f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )
            } else {
                Text(
                    "Cannot load image",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Top control bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }

                IconButton(
                    onClick = { onShare(path) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun NoteVideoViewerDialog(
    path: String,
    onOpenExternal: (String) -> Unit,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    android.widget.VideoView(ctx).apply {
                        val uri = Uri.fromFile(File(path))
                        setVideoURI(uri)
                        val controller = android.widget.MediaController(ctx)
                        controller.setAnchorView(this)
                        setMediaController(controller)
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                            start()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top control bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onOpenExternal(path) },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Filled.Launch, contentDescription = "Open in player", tint = Color.White)
                    }
                    IconButton(
                        onClick = { onShare(path) },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(icon, contentDescription = description, tint = tint.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
    }
}

/**
 * Renders inline markdown-style markers as real formatting while HIDING the marker
 * characters themselves, so the editor shows styled text instead of raw `**markup**`.
 */
@Composable
private fun NoteTextBlockItem(
    block: NoteBlock.Text,
    onValueChange: (TextFieldValue) -> Unit,
    onFocus: () -> Unit,
    onBackspaceAtStart: () -> Boolean,
    visualTransformation: VisualTransformation,
    onCardColor: Color,
    isOnlyBlock: Boolean,
    modifier: Modifier = Modifier
) {
    var textValue by remember(block) { mutableStateOf(block.value) }

    LaunchedEffect(block.value.text, block.value.selection) {
        if (textValue.text != block.value.text || textValue.selection != block.value.selection) {
            textValue = block.value
        }
    }

    TextField(
        value = textValue,
        onValueChange = { raw ->
            val processed = VeritasNoteEditing.continueListOnNewline(textValue, raw)
            textValue = processed
            onValueChange(processed)
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = onCardColor),
        placeholder = {
            if (isOnlyBlock && textValue.text.isEmpty()) {
                Text(
                    "Note",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onCardColor.copy(alpha = 0.4f)
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (isOnlyBlock) 200.dp else 24.dp)
            .onFocusChanged { fState ->
                if (fState.isFocused) {
                    onFocus()
                }
            }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyDown) {
                    val isAtStart = textValue.selection.start == 0 && textValue.selection.end == 0
                    if (isAtStart) {
                        return@onPreviewKeyEvent onBackspaceAtStart()
                    }
                }
                false
            },
        visualTransformation = visualTransformation,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = onCardColor
        )
    )
}

/**
 * Applies visual styling for inline Markdown markers and headings.
 * Maintains an exact bidirectional offset mapping so the caret and selection stay correct.
 */
class RichTextVisualTransformation(private val baseColor: Color) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return RichTextFormatter.transform(text.text, baseColor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RichTextVisualTransformation) return false
        return baseColor == other.baseColor
    }

    override fun hashCode(): Int {
        return baseColor.hashCode()
    }
}

object RichTextFormatter {
    private val HEADING_REGEX = Regex("^(#{1,3}) ")
    private val inlineMarkers: List<Pair<Regex, (String) -> SpanStyle>> = listOf(
        Regex("\\*\\*(.*?)\\*\\*") to { _: String -> SpanStyle(fontWeight = FontWeight.Bold) },
        Regex("__(.*?)__") to { _: String -> SpanStyle(textDecoration = TextDecoration.Underline) },
        Regex("~~(.*?)~~") to { _: String -> SpanStyle(textDecoration = TextDecoration.LineThrough) },
        Regex("`(.*?)`") to { _: String -> SpanStyle(fontFamily = FontFamily.Monospace) },
        Regex("\\*(.*?)\\*") to { _: String -> SpanStyle(fontStyle = FontStyle.Italic) }
    )

    private val markerLengths = listOf(2, 2, 2, 1, 1)

    fun transform(raw: String, baseColor: Color = Color.Unspecified): TransformedText {
        val n = raw.length
        if (n == 0) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        // Fast-path: if text contains no markdown indicator characters, skip all regex parsing entirely!
        val hasMarkdown = raw.any { it == '*' || it == '_' || it == '~' || it == '`' || it == '#' || it == '>' }
        if (!hasMarkdown) {
            return TransformedText(AnnotatedString(raw), OffsetMapping.Identity)
        }

        val markerStyle = if (baseColor != Color.Unspecified) {
            SpanStyle(color = baseColor.copy(alpha = 0.35f))
        } else {
            SpanStyle(color = Color.Gray.copy(alpha = 0.5f))
        }

        val annotated = buildAnnotatedString {
            append(raw)

            // Line-level headings and quotes
            var lineStart = 0
            while (lineStart < n) {
                val nl = raw.indexOf('\n', lineStart).let { if (it < 0) n else it }
                val line = raw.substring(lineStart, nl)
                val m = HEADING_REGEX.find(line)
                if (m != null) {
                    val prefixLen = m.value.length
                    addStyle(markerStyle, lineStart, lineStart + prefixLen)
                    val level = m.groupValues[1].length
                    val size = when (level) {
                        1 -> 24f
                        2 -> 20f
                        else -> 17f
                    }
                    if (nl > lineStart + prefixLen) {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = size.sp), lineStart + prefixLen, nl)
                    }
                } else if (line.startsWith("> ")) {
                    addStyle(markerStyle, lineStart, lineStart + 2)
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), lineStart + 2, nl)
                }
                if (nl >= n) break
                lineStart = nl + 1
            }

            // Inline markers
            val consumed = BooleanArray(n)
            inlineMarkers.forEachIndexed { index, (regex, styleFor) ->
                val mlen = markerLengths[index]
                regex.findAll(raw).forEach { match ->
                    val s = match.range.first
                    val e = match.range.last // inclusive
                    if (s < 0 || e >= n) return@forEach
                    var overlaps = false
                    for (i in s..e) if (consumed[i]) { overlaps = true; break }
                    if (overlaps) return@forEach
                    val innerStart = s + mlen
                    val innerEnd = e + 1 - mlen
                    if (innerEnd <= innerStart) return@forEach
                    for (i in s..e) consumed[i] = true

                    addStyle(markerStyle, s, innerStart)
                    addStyle(styleFor(match.groupValues.getOrElse(1) { "" }), innerStart, innerEnd)
                    addStyle(markerStyle, innerEnd, e + 1)
                }
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }

    /** Removes inline markup and heading prefixes for plain-text sharing. */
    fun stripMarkup(raw: String): String {
        var out = raw
        out = out.replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        out = out.replace(Regex("__(.+?)__"), "$1")
        out = out.replace(Regex("~~(.+?)~~"), "$1")
        out = out.replace(Regex("`(.+?)`"), "$1")
        out = out.replace(Regex("\\*(.+?)\\*"), "$1")
        out = out.lineSequence().joinToString("\n") {
            it.replaceFirst(Regex("^#{1,3} "), "").replaceFirst(Regex("^> "), "")
        }
        return out
    }
}

fun loadNoteBitmap(context: android.content.Context, pathOrUri: String): android.graphics.Bitmap? {
    if (pathOrUri.isBlank()) return null
    return try {
        if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
            val uri = android.net.Uri.parse(pathOrUri)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } else {
            val file = File(pathOrUri)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                val uri = android.net.Uri.parse(pathOrUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun NoteAttachmentMenu(
    onCopy: () -> Unit,
    canMoveUp: Boolean,
    onMoveUp: () -> Unit,
    canMoveDown: Boolean,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                .size(32.dp)
        ) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Attachment options",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Copy Path") },
                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                onClick = {
                    expanded = false
                    onCopy()
                }
            )
            if (canMoveUp) {
                DropdownMenuItem(
                    text = { Text("Move Up") },
                    leadingIcon = { Icon(Icons.Outlined.ArrowUpward, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onMoveUp()
                    }
                )
            }
            if (canMoveDown) {
                DropdownMenuItem(
                    text = { Text("Move Down") },
                    leadingIcon = { Icon(Icons.Outlined.ArrowDownward, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onMoveDown()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

