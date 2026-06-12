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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritas.reader.GeneralNote
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import java.text.SimpleDateFormat
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralNotesEditor(
    note: GeneralNote?,
    onSave: (String, String, String?, Boolean, Boolean, String?, String?) -> Unit,
    onDelete: (String) -> Unit,
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
    var audioUrl by remember { mutableStateOf(note?.audioUrl) }
    var showColorPicker by remember { mutableStateOf(false) }

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
            audioUrl = recordFilePath
            Toast.makeText(context, "Recording saved and attached", Toast.LENGTH_SHORT).show()
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

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableStateOf(0f) }
    var currentPosition by remember { mutableStateOf("0:00") }
    var totalDuration by remember { mutableStateOf("0:00") }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && mediaPlayer != null) {
                try {
                    val current = mediaPlayer?.currentPosition ?: 0
                    val duration = mediaPlayer?.duration ?: 1
                    playProgress = current.toFloat() / duration.coerceAtLeast(1)

                    val curSecs = current / 1000
                    val durSecs = duration / 1000
                    currentPosition = String.format("%d:%02d", curSecs / 60, curSecs % 60)
                    totalDuration = String.format("%d:%02d", durSecs / 60, durSecs % 60)
                } catch (e: Exception) {
                    // ignore
                }
                kotlinx.coroutines.delay(200)
            }
        }
    }

    fun togglePlayPause() {
        val path = audioUrl ?: return
        try {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            } else {
                if (mediaPlayer == null) {
                    val player = MediaPlayer().apply {
                        setDataSource(path)
                        prepare()
                        setOnCompletionListener {
                            isPlaying = false
                            playProgress = 0f
                            currentPosition = "0:00"
                        }
                    }
                    mediaPlayer = player
                }
                mediaPlayer?.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Playback error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeAudio() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            audioUrl = null
            playProgress = 0f
            currentPosition = "0:00"
            totalDuration = "0:00"
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

    // Helper to format text with Markdown bold/italic
    fun insertMarkdown(syntax: String) {
        if (isChecklist) return
        val text = contentValue.text
        val selection = contentValue.selection
        val start = selection.min
        val end = selection.max
        
        val newText = if (start != end) {
            text.substring(0, start) + syntax + text.substring(start, end) + syntax + text.substring(end)
        } else {
            text.substring(0, start) + syntax + syntax + text.substring(start)
        }
        
        val newSelectionStart = if (start != end) end + syntax.length * 2 else start + syntax.length
        contentValue = TextFieldValue(text = newText, selection = androidx.compose.ui.text.TextRange(newSelectionStart))
        contentText = newText
    }

    val cardBgColor = noteColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.surface
    val onCardColor = if (noteColor != null) Color.Black else MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "New Note" else "Edit Note", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        val finalContent = if (isChecklist) contentText else contentValue.text
                        if (title.isNotBlank() || finalContent.isNotBlank() || imageUrl != null || audioUrl != null) {
                            onSave(title, finalContent, noteColor, isPinned, isChecklist, imageUrl, audioUrl)
                        } else {
                            onDismiss()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Pinned Status Action
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.6f)
                        )
                    }
                    if (note != null) {
                        IconButton(onClick = { onDelete(note.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = {
                        val finalContent = if (isChecklist) contentText else contentValue.text
                        onSave(title, finalContent, noteColor, isPinned, isChecklist, imageUrl, audioUrl)
                    }) {
                        Icon(Icons.Filled.Done, contentDescription = "Save Note", tint = MaterialTheme.colorScheme.primary)
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
                        noteColors.forEach { (name, hex) ->
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color picker trigger
                        IconButton(onClick = { showColorPicker = !showColorPicker }) {
                            Icon(Icons.Outlined.Palette, contentDescription = "Color Picker", tint = onCardColor.copy(alpha = 0.8f))
                        }
                        
                        // Checklist Toggle
                        IconButton(onClick = { 
                            isChecklist = !isChecklist 
                            if (isChecklist && contentText.isBlank()) {
                                contentText = "[ ] Add item"
                            }
                        }) {
                            Icon(
                                imageVector = if (isChecklist) Icons.Filled.Checklist else Icons.Outlined.Checklist, 
                                contentDescription = "Checklist Toggle", 
                                tint = if (isChecklist) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f)
                            )
                        }

                        // Image Attachment
                        IconButton(onClick = { 
                            imageLauncher.launch("image/*")
                        }) {
                            Icon(
                                imageVector = if (imageUrl != null) Icons.Filled.Image else Icons.Outlined.Image, 
                                contentDescription = "Attach Image", 
                                tint = if (imageUrl != null) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f)
                            )
                        }

                        // Audio Attachment
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
                                imageVector = if (isRecording) Icons.Filled.Stop else (if (audioUrl != null) Icons.Filled.Mic else Icons.Outlined.Mic), 
                                contentDescription = if (isRecording) "Stop Recording" else "Record Audio", 
                                tint = if (isRecording) MaterialTheme.colorScheme.error else (if (audioUrl != null) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.8f))
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (!isChecklist) {
                            FilledTonalButton(
                                onClick = { insertMarkdown("**") },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = onCardColor.copy(alpha = 0.1f), contentColor = onCardColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 36.dp, minHeight = 32.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("B", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            FilledTonalButton(
                                onClick = { insertMarkdown("*") },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = onCardColor.copy(alpha = 0.1f), contentColor = onCardColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 36.dp, minHeight = 32.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("I", fontStyle = FontStyle.Italic, fontSize = 14.sp)
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
                        // Close button to remove image
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

            // Display Audio attachment preview if attached
            if (audioUrl != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = onCardColor.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, onCardColor.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = { togglePlayPause() }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = onCardColor
                            )
                        }
                        LinearProgressIndicator(
                            progress = { playProgress },
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = onCardColor.copy(alpha = 0.2f)
                        )
                        Text("$currentPosition / $totalDuration", style = MaterialTheme.typography.labelMedium, color = onCardColor)
                        IconButton(onClick = { removeAudio() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove Audio",
                                tint = onCardColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    width = 1.dp,
                    color = onCardColor.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Title input
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

                    // Horizontal line separator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(onCardColor.copy(alpha = 0.15f))
                    )

                    // Content body input or Checklist input
                    if (!isChecklist) {
                        TextField(
                            value = contentValue,
                            onValueChange = { 
                                contentValue = it 
                                contentText = it.text
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = onCardColor
                            ),
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
                            visualTransformation = MarkdownVisualTransformation(),
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
                        // Checklist View Mode
                        val items = remember(contentText) {
                            val list = contentText.split("\n").filter { it.isNotBlank() }.map { line ->
                                val checked = line.startsWith("[x]")
                                val text = line.removePrefix("[ ] ").removePrefix("[x] ")
                                checked to text
                            }.toMutableStateList()
                            if (list.isEmpty()) {
                                list.add(false to "")
                            }
                            list
                        }

                        fun updateChecklistString() {
                            contentText = items.joinToString("\n") { (checked, text) ->
                                if (checked) "[x] $text" else "[ ] $text"
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items.forEachIndexed { idx, (checked, text) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            items[idx] = isChecked to text
                                            updateChecklistString()
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                    BasicTextField(
                                        value = text,
                                        onValueChange = { newText ->
                                            items[idx] = checked to newText
                                            updateChecklistString()
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = onCardColor,
                                            textDecoration = if (checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            items.removeAt(idx)
                                            updateChecklistString()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = onCardColor.copy(alpha = 0.5f))
                                    }
                                }
                            }
                            
                            // Add Item Button
                            TextButton(
                                onClick = {
                                    items.add(false to "")
                                    updateChecklistString()
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
}

class MarkdownVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val annotated = buildAnnotatedString {
            append(rawText)
            
            // Apply bold style for **text**
            val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
            boldRegex.findAll(rawText).forEach { matchResult ->
                val start = matchResult.range.first
                val end = matchResult.range.last + 1
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
            }
            
            // Apply italic style for *text*
            val italicRegex = Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)")
            italicRegex.findAll(rawText).forEach { matchResult ->
                val start = matchResult.range.first
                val end = matchResult.range.last + 1
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
