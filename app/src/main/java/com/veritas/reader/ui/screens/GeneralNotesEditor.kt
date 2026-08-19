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
    var title by remember { mutableStateOf(note?.title ?: "") }
    var contentText by remember { mutableStateOf(note?.content ?: "") }
    var contentValue by remember { mutableStateOf(TextFieldValue(note?.content ?: "")) }
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
        undoStack.add(contentValue)
        if (undoStack.size > 200) undoStack.removeAt(0)
        redoStack.clear()
    }

    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordFilePath by remember { mutableStateOf<String?>(null) }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val mediaDir = File(context.filesDir, "notes_media")
                if (!mediaDir.exists()) mediaDir.mkdirs()
                val fileName = "rec_${System.currentTimeMillis()}.3gp"
                val file = File(mediaDir, fileName)
                val path = file.absolutePath
                recordFilePath = path

                val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
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
                Toast.makeText(context, "Recording started...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to start recording: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission is required to record audio notes", Toast.LENGTH_SHORT).show()
        }
    }

    fun startRecording() {
        try {
            val mediaDir = File(context.filesDir, "notes_media")
            if (!mediaDir.exists()) mediaDir.mkdirs()
            val fileName = "rec_${System.currentTimeMillis()}.3gp"
            val file = File(mediaDir, fileName)
            val path = file.absolutePath
            recordFilePath = path

            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
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
            Toast.makeText(context, "Recording started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start recording: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            val newPath = recordFilePath
            if (!newPath.isNullOrBlank() && File(newPath).exists()) {
                audioUrls = audioUrls + newPath
                Toast.makeText(context, "Voice memo saved (${audioUrls.size} total)", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to stop recording: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Image attached successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to attach image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
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
    // This covers fast edits without hammering storage on every keystroke.
    val autoSaveContent = if (isChecklist) contentText else contentValue.text
    LaunchedEffect(title, autoSaveContent, noteColor, isPinned, isChecklist, imageUrl, audioUrls, reminderAt) {
        kotlinx.coroutines.delay(800)
        if (title.isNotBlank() || autoSaveContent.isNotBlank() || imageUrl != null || audioUrls.isNotEmpty()) {
            onSave(title, autoSaveContent, noteColor, isPinned, isChecklist, imageUrl, audioUrls.firstOrNull(), reminderAt, false, audioUrls)
        }
    }
    // Heartbeat save: fires every 2 seconds while the editor is open.
    // Ensures the note is saved even if no new edits occur (e.g. the user
    // only scrolled or left the phone idle with the editor open).
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2_000)
            val currentContent = if (isChecklist) contentText else contentValue.text
            if (title.isNotBlank() || currentContent.isNotBlank() || imageUrl != null || audioUrls.isNotEmpty()) {
                onSave(title, currentContent, noteColor, isPinned, isChecklist, imageUrl, audioUrls.firstOrNull(), reminderAt, false, audioUrls)
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
        val newValue = VeritasNoteEditing.toggleInlineMarker(contentValue, marker)
        contentValue = newValue
        contentText = newValue.text
    }

    // Toggle a line-level prefix (heading / list marker) across every line touched by the
    // selection. Logic lives in VeritasNoteEditing so it is unit-tested.
    fun applyLinePrefix(prefix: String) {
        if (isChecklist) return
        pushHistory()
        val newValue = VeritasNoteEditing.toggleLinePrefix(contentValue, prefix)
        contentValue = newValue
        contentText = newValue.text
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

    fun performSave() {
        val finalContent = if (isChecklist) contentText else contentValue.text
        onSave(title, finalContent, noteColor, isPinned, isChecklist, imageUrl, audioUrls.firstOrNull(), reminderAt, true, audioUrls)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        val finalContent = if (isChecklist) contentText else contentValue.text
                        if (title.isNotBlank() || finalContent.isNotBlank() || imageUrl != null || audioUrls.isNotEmpty()) {
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
                    val showUndoRedo = if (isChecklist) contentText.isNotEmpty() else contentValue.text.isNotEmpty()
                    
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
                                
                                FormatToolbarButton(Icons.Filled.Title, "Heading", onCardColor) { applyLinePrefix("# ") }
                                FormatToolbarButton(Icons.Filled.FormatBold, "Bold", onCardColor) { applyInlineMarker("**") }
                                FormatToolbarButton(Icons.Filled.FormatItalic, "Italic", onCardColor) { applyInlineMarker("*") }
                                FormatToolbarButton(Icons.Filled.FormatUnderlined, "Underline", onCardColor) { applyInlineMarker("__") }
                                FormatToolbarButton(Icons.Filled.StrikethroughS, "Strikethrough", onCardColor) { applyInlineMarker("~~") }
                                FormatToolbarButton(Icons.Filled.Code, "Monospace", onCardColor) { applyInlineMarker("`") }
                                FormatToolbarButton(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet list", onCardColor) { applyLinePrefix("- ") }
                                FormatToolbarButton(Icons.Filled.FormatListNumbered, "Numbered list", onCardColor) { applyLinePrefix("1. ") }
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
                                    isChecklist = !isChecklist
                                    if (isChecklist && contentText.isBlank()) {
                                        contentText = "[ ] "
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
                                        imageVector = if (imageUrl != null) Icons.Filled.Image else Icons.Outlined.Image,
                                        contentDescription = "Attach Image",
                                        tint = if (imageUrl != null) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f)
                                    )
                                }

                                // Record Audio
                                IconButton(onClick = {
                                    expandedMenu = NotesToolbarMenu.NONE
                                    if (isRecording) {
                                        stopRecording()
                                    } else {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            startRecording()
                                        } else {
                                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Filled.Stop else (if (audioUrls.isNotEmpty()) Icons.Filled.Mic else Icons.Outlined.Mic),
                                        contentDescription = if (isRecording) "Stop Recording" else "Record Audio",
                                        tint = if (isRecording) MaterialTheme.colorScheme.error else (if (audioUrls.isNotEmpty()) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f))
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
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                startRecording()
                                            } else {
                                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (isRecording) Icons.Filled.Stop else (if (audioUrls.isNotEmpty()) Icons.Filled.Mic else Icons.Outlined.Mic),
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

            // Display Image attachment preview if attached
            if (imageUrl != null) {
                val bitmap = remember(imageUrl) {
                    try {
                        android.graphics.BitmapFactory.decodeFile(imageUrl)
                    } catch (e: Exception) {
                        null
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = onCardColor.copy(alpha = 0.05f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Attached image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error loading image", color = onCardColor)
                            }
                        }
                        IconButton(
                            onClick = { imageUrl = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Live recording banner
            if (isRecording) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        )
                        Text(
                            "Recording audio memo…",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { stopRecording() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Done", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Display Audio attachments preview if any attached
            if (audioUrls.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    audioUrls.forEachIndexed { index, path ->
                        val isThisPlaying = isPlaying && activePlayingAudioPath == path
                        val memoTotalDur = audioDurations[path] ?: "0:00"
                        val durDisplay = if (isThisPlaying) "$currentPosition / $memoTotalDur" else memoTotalDur

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = onCardColor.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, onCardColor.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isThisPlaying) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.12f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    IconButton(
                                        onClick = { togglePlayPause(path) },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = if (isThisPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (isThisPlaying) "Pause" else "Play",
                                            tint = if (isThisPlaying) MaterialTheme.colorScheme.onPrimary else onCardColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (audioUrls.size == 1) "Voice Memo" else "Voice Memo ${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = onCardColor
                                        )
                                        Text(
                                            text = durDisplay,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onCardColor.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { if (isThisPlaying) playProgress else 0f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = onCardColor.copy(alpha = 0.15f)
                                    )
                                }

                                IconButton(
                                    onClick = { removeAudio(path) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove Memo",
                                        tint = onCardColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
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
                    TextField(
                        value = contentValue,
                        onValueChange = { raw ->
                            val processed = handleSmartNewline(contentValue, raw)
                            // Coalesce keystrokes into word-level undo steps.
                            val prev = contentValue
                            val isBoundary = processed.text.length < prev.text.length ||
                                (processed.text.length - prev.text.length) > 1 ||
                                processed.text.lastOrNull()?.isWhitespace() == true
                            if (isBoundary && prev.text != processed.text) {
                                undoStack.add(prev)
                                if (undoStack.size > 200) undoStack.removeAt(0)
                                redoStack.clear()
                            }
                            contentValue = processed
                            contentText = processed.text
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = onCardColor),
                        placeholder = {
                            Text(
                                "Note",
                                style = MaterialTheme.typography.bodyLarge,
                                color = onCardColor.copy(alpha = 0.4f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 200.dp),
                        visualTransformation = RichTextVisualTransformation(onCardColor),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = onCardColor
                        )
                    )
                } else {
                    val coroutineScope = rememberCoroutineScope()
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                        updateChecklistString()
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
 * Maintains an exact bidirectional offset mapping so the caret and selection stay correct.
 */
class RichTextVisualTransformation(private val baseColor: Color) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return RichTextFormatter.transform(text.text)
    }
}

object RichTextFormatter {
    private data class StyleSpan(val start: Int, val endExclusive: Int, val style: SpanStyle)

    private val inlineMarkers: List<Pair<Regex, (String) -> SpanStyle>> = listOf(
        Regex("\\*\\*(.*?)\\*\\*") to { _: String -> SpanStyle(fontWeight = FontWeight.Bold) },
        Regex("__(.*?)__") to { _: String -> SpanStyle(textDecoration = TextDecoration.Underline) },
        Regex("~~(.*?)~~") to { _: String -> SpanStyle(textDecoration = TextDecoration.LineThrough) },
        Regex("`(.*?)`") to { _: String -> SpanStyle(fontFamily = FontFamily.Monospace) },
        Regex("\\*(.*?)\\*") to { _: String -> SpanStyle(fontStyle = FontStyle.Italic) }
    )

    private val markerLengths = listOf(2, 2, 2, 1, 1)

    fun transform(raw: String): TransformedText {
        val n = raw.length
        val hidden = BooleanArray(n)
        val consumed = BooleanArray(n)
        val spans = mutableListOf<StyleSpan>()

        // Line-level headings: hide the leading "# " / "## " / "### " and enlarge the line.
        run {
            var lineStart = 0
            while (lineStart <= n) {
                val nl = raw.indexOf('\n', lineStart).let { if (it < 0) n else it }
                val line = raw.substring(lineStart, nl)
                val m = Regex("^(#{1,3}) ").find(line)
                if (m != null) {
                    val prefixLen = m.value.length
                    for (i in lineStart until (lineStart + prefixLen)) hidden[i] = true
                    val level = m.groupValues[1].length
                    val size = when (level) {
                        1 -> 24f
                        2 -> 20f
                        else -> 17f
                    }
                    if (nl > lineStart + prefixLen) {
                        spans.add(StyleSpan(lineStart + prefixLen, nl, SpanStyle(fontWeight = FontWeight.Bold, fontSize = size.sp)))
                    }
                }
                if (nl >= n) break
                lineStart = nl + 1
            }
        }

        // Inline markers, in priority order, claiming non-overlapping balanced pairs.
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
                for (i in s until innerStart) hidden[i] = true
                for (i in innerEnd..e) hidden[i] = true
                spans.add(StyleSpan(innerStart, innerEnd, styleFor(match.groupValues.getOrElse(1) { "" })))
            }
        }

        // Build the visible text plus exact offset maps.
        val origToTrans = IntArray(n + 1)
        val sb = StringBuilder()
        val transToOrigList = ArrayList<Int>(n + 1)
        var t = 0
        for (i in 0 until n) {
            origToTrans[i] = t
            if (!hidden[i]) {
                sb.append(raw[i])
                transToOrigList.add(i)
                t++
            }
        }
        origToTrans[n] = t
        transToOrigList.add(n)
        val transToOrig = IntArray(t + 1) { idx -> transToOrigList.getOrElse(idx) { n } }

        val annotated = buildAnnotatedString {
            append(sb.toString())
            spans.forEach { span ->
                val ts = origToTrans[span.start.coerceIn(0, n)]
                val te = origToTrans[span.endExclusive.coerceIn(0, n)]
                if (ts < te) addStyle(span.style, ts, te)
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = origToTrans[offset.coerceIn(0, n)]
            override fun transformedToOriginal(offset: Int): Int = transToOrig[offset.coerceIn(0, t)]
        }

        return TransformedText(annotated, mapping)
    }

    /** Removes inline markup and heading prefixes for plain-text sharing. */
    fun stripMarkup(raw: String): String {
        var out = raw
        out = out.replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        out = out.replace(Regex("__(.+?)__"), "$1")
        out = out.replace(Regex("~~(.+?)~~"), "$1")
        out = out.replace(Regex("`(.+?)`"), "$1")
        out = out.replace(Regex("\\*(.+?)\\*"), "$1")
        out = out.lineSequence().joinToString("\n") { it.replaceFirst(Regex("^#{1,3} "), "") }
        return out
    }
}
