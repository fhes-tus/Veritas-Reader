package com.veritas.reader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.Menu
import android.widget.Toast
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.History
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.foundation.BorderStroke
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.veritas.reader.ui.ReaderViewModel
import com.veritas.reader.ui.VeritasPendingImport
import com.veritas.reader.ui.VeritasSwitch
import com.veritas.reader.ui.screens.AskAiSettingsDialog
import com.veritas.reader.ui.screens.DocumentNotesDialog
import com.veritas.reader.ui.screens.FeatureDropdownMenuItem
import com.veritas.reader.ui.screens.LibraryScreen
import com.veritas.reader.ui.screens.GeneralNotesEditor
import com.veritas.reader.ui.screens.NarrationStudioDialog
import com.veritas.reader.ui.screens.PronunciationRulesDialog
import com.veritas.reader.ui.screens.ReaderScreen
import com.veritas.reader.ui.screens.ReaderScreenState
import com.veritas.reader.ui.screens.ReaderSettingsDialog
import com.veritas.reader.ui.screens.ReadingListsDialog
import com.veritas.reader.ui.screens.SettingsHubDialog
import com.veritas.reader.ui.screens.UserManualDialog
import com.veritas.reader.ui.screens.SleepTimerDialog
import com.veritas.reader.ui.screens.UpdateAvailableDialog
import com.veritas.reader.ui.screens.ReleaseNotesDialog
import com.veritas.reader.ui.screens.VoiceStudioDialog
import com.veritas.reader.ReaderMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.veritas.reader.ui.rememberVeritasHaptics
import com.veritas.reader.ui.screens.OnboardingQuestChecklist
import com.veritas.reader.ui.screens.OnboardingSpotlightOverlay
import com.veritas.reader.ui.screens.ConfettiOverlay
import com.veritas.reader.ui.OnboardingStep
import com.veritas.reader.ui.OnboardingController
import androidx.compose.ui.layout.onGloballyPositioned
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun SentenceNoteDialog(
    document: ReaderDocument,
    sentenceIndexes: List<Int>,
    noteDraft: String,
    audioPath: String? = null,
    audioDuration: Int = 0,
    onNoteChange: (String) -> Unit,
    onAudioChange: (String?, Int) -> Unit = { _, _ -> },
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val indexes = sentenceIndexes
        .filter { it in document.chunks.indices }
        .distinct()
        .sorted()
    val title = if (indexes.size == 1) {
        "Sentence ${indexes.first() + 1} note"
    } else {
        "${indexes.size} sentence note"
    }
    val wordCount = noteDraft.trim().split(Regex("\\s+")).count { it.isNotBlank() }

    var currentAudioPath by remember { mutableStateOf(audioPath) }
    var currentAudioDuration by remember { mutableIntStateOf(audioDuration) }
    val recordingState by VoiceNoteRecorder.recordingState.collectAsState()

    var permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val docId = document.id ?: "doc"
            val targetIdx = indexes.firstOrNull() ?: 0
            VoiceNoteRecorder.startRecording(context, docId, targetIdx)
        }
    }

    AlertDialog(
        onDismissRequest = {
            VoiceNoteRecorder.stopAll()
            onDismiss()
        },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    document.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    label = { Text("Sentence note") },
                    placeholder = { Text("Write the note to attach to this sentence") },
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    "$wordCount / 300 words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Voice Memo Recording & Playback Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (recordingState) {
                            VoiceRecordingState.RECORDING -> {
                                Text(
                                    "🔴 Recording voice memo...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val result = VoiceNoteRecorder.stopRecording()
                                        if (result != null) {
                                            currentAudioPath = result.first
                                            currentAudioDuration = result.second
                                            onAudioChange(result.first, result.second)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Stop")
                                }
                            }
                            VoiceRecordingState.PLAYING -> {
                                Text(
                                    "▶️ Playing voice memo (${currentAudioDuration}s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(onClick = { VoiceNoteRecorder.stopPlayback() }) {
                                    Text("Stop")
                                }
                            }
                            VoiceRecordingState.IDLE -> {
                                if (currentAudioPath != null) {
                                    Text(
                                        "🎙️ Voice memo (${currentAudioDuration}s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            currentAudioPath?.let { path ->
                                                VoiceNoteRecorder.playAudio(path)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            currentAudioPath?.let { path ->
                                                VoiceNoteRecorder.deleteAudioFile(path)
                                            }
                                            currentAudioPath = null
                                            currentAudioDuration = 0
                                            onAudioChange(null, 0)
                                        }
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete voice memo", tint = MaterialTheme.colorScheme.error)
                                    }
                                } else {
                                    Text(
                                        "Spoken audio memo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED
                                            ) {
                                                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                            } else {
                                                val docId = document.id ?: "doc"
                                                val targetIdx = indexes.firstOrNull() ?: 0
                                                VoiceNoteRecorder.startRecording(context, docId, targetIdx)
                                            }
                                        },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(Icons.Outlined.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Record")
                                    }
                                }
                            }
                        }
                    }
                }

                indexes.take(5).forEach { index ->
                    val excerpt =
                        document.chunks.getOrNull(index).orEmpty().replace(Regex("\\s+"), " ")
                            .trim()
                    if (excerpt.isNotBlank()) {
                        Text(
                            "Sentence ${index + 1}: $excerpt",
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (indexes.size > 5) {
                    Text(
                        "+ ${indexes.size - 5} more selected sentences",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    VoiceNoteRecorder.stopAll()
                    onSave()
                },
                enabled = noteDraft.trim().isNotBlank() || currentAudioPath != null,
                shape = RoundedCornerShape(50)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        VoiceNoteRecorder.stopAll()
                        onDelete()
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Delete")
                }
                TextButton(
                    onClick = {
                        VoiceNoteRecorder.stopAll()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

/**
 * Small thumbnail for an image entry, decoded off the main thread and downsampled hard —
 * these are only ever drawn at 56dp, and a delete confirmation may show several at once.
 * Returns null for anything that is not a decodable image, so callers fall back to the name.
 */
@Composable
private fun rememberFileThumbnail(file: VeritasBrowserFile): Bitmap? {
    val context = LocalContext.current
    var thumb by remember(file.uri) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(file.uri) {
        if (file.type != VeritasBrowserTab.OCR) return@LaunchedEffect
        thumb = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(file.uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                var sample = 1
                while (bounds.outWidth / sample > 256 || bounds.outHeight / sample > 256) sample *= 2
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(file.uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
            }.getOrNull()
        }
    }
    return thumb
}

/** One doomed file: its picture when we have one, otherwise its name and folder. */
@Composable
private fun DeletePreviewRow(file: VeritasBrowserFile) {
    val thumb = rememberFileThumbnail(file)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            val bmp = thumb
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val (icon, tint, _) = getFileColorAndIcon(file)
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                file.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                file.relativePath.ifBlank { file.rootLabel },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun FileBrowserDialog(
    roots: List<VeritasBrowserRoot>,
    entries: List<VeritasBrowserFile>,
    location: VeritasBrowserLocation?,
    canGoUp: Boolean,
    scanning: Boolean,
    message: String?,
    allFilesAccessGranted: Boolean,
    importing: Boolean,
    importingName: String,
    onDismiss: () -> Unit,
    onPickFolder: () -> Unit,
    onRequestAllFilesAccess: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onRefresh: () -> Unit,
    onGoUp: () -> Unit,
    onEnterDirectory: (VeritasBrowserFile) -> Unit,
    onRemoveAllAccess: () -> Unit,
    onImportFile: (VeritasBrowserFile) -> Unit,
    onImportMultipleFiles: (List<VeritasBrowserFile>, Boolean) -> Unit,
    onDeleteFiles: (List<VeritasBrowserFile>) -> Unit = {}
) {
    val selectedFiles = remember { mutableStateListOf<VeritasBrowserFile>() }
    // Deleting reaches the user's own storage and cannot be undone, so nothing is removed
    // until this is confirmed with the files named.
    var pendingDelete by remember { mutableStateOf<List<VeritasBrowserFile>>(emptyList()) }
    
    if (pendingDelete.isNotEmpty()) {
        val doomed = pendingDelete
        AlertDialog(
            onDismissRequest = { pendingDelete = emptyList() },
            icon = {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    if (doomed.size == 1) "Delete this file?" else "Delete ${doomed.size} files?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This removes the file from your phone's storage, not just from Veritas. It cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Column(
                        modifier = Modifier
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        doomed.take(5).forEach { file -> DeletePreviewRow(file) }
                    }
                    if (doomed.size > 5) {
                        Text(
                            "and ${doomed.size - 5} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFiles(doomed)
                        selectedFiles.clear()
                        pendingDelete = emptyList()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = emptyList() }) { Text("Cancel") }
            }
        )
    }

    LaunchedEffect(location) {
        selectedFiles.clear()
    }

    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(VeritasBrowserTab.ALL) }
    var sortMode by remember { mutableStateOf(VeritasBrowserSort.NAME) }
    var sortAscending by remember { mutableStateOf(true) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val browserPrefs = remember { context.getSharedPreferences("veritas_library_settings", Context.MODE_PRIVATE) }
    var viewMode by remember {
        mutableStateOf(
            runCatching {
                LibraryViewMode.valueOf(
                    browserPrefs.getString("file_view_mode", LibraryViewMode.TILES.name) ?: LibraryViewMode.TILES.name
                )
            }.getOrDefault(LibraryViewMode.TILES)
        )
    }
    var showSortDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showViewMenu by remember { mutableStateOf(false) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val columnCount = remember(configuration) {
        when {
            configuration.screenWidthDp >= 840 -> 4
            configuration.screenWidthDp >= 600 -> 3
            else -> 2
        }
    }
    val browserFeatures = remember(roots, allFilesAccessGranted) {
        VeritasFeatureRegistry.resolve(
            VeritasFeatureSurface.FILE_BROWSER_OVERFLOW,
            VeritasFeatureContext(hasFileBrowserSession = roots.isNotEmpty() || allFilesAccessGranted)
        ).associateBy { it.definition.id }
    }
    val visibleEntries = remember(entries, query, selectedTab, sortMode, sortAscending) {
        val needle = query.trim()
        val filtered = entries
            .filter { selectedTab == VeritasBrowserTab.ALL || it.isDirectory || it.type == selectedTab }
            .filter { file ->
                needle.isBlank() ||
                        file.name.contains(needle, ignoreCase = true) ||
                        file.rootLabel.contains(needle, ignoreCase = true) ||
                        file.relativePath.contains(needle, ignoreCase = true)
            }
        val comparator = when (sortMode) {
            VeritasBrowserSort.NAME -> compareBy<VeritasBrowserFile> { it.name.lowercase(Locale.getDefault()) }
            VeritasBrowserSort.DATE -> compareBy { it.modifiedAt }
            VeritasBrowserSort.SIZE -> compareBy { it.sizeBytes }
            VeritasBrowserSort.PATH -> compareBy { it.relativePath.lowercase(Locale.getDefault()) }
        }
        val sorted =
            if (sortAscending) filtered.sortedWith(comparator) else filtered.sortedWith(comparator.reversed())
        sorted.sortedBy { it.isDirectory }
    }

    val folders = remember(visibleEntries, canGoUp, query) {
        val list = visibleEntries.filter { it.isDirectory }
        if (canGoUp && query.isBlank()) {
            listOf(
                VeritasBrowserFile(
                    uri = Uri.parse("veritas://parent_directory"),
                    name = ".. (Go up)",
                    mimeType = "",
                    sizeBytes = 0L,
                    modifiedAt = 0L,
                    rootLabel = "",
                    relativePath = "",
                    isDirectory = true,
                    isSupported = true,
                    targetLocation = VeritasBrowserLocation(rootLabel = "Parent")
                )
            ) + list
        } else {
            list
        }
    }
    val files = remember(visibleEntries) {
        visibleEntries.filter { !it.isDirectory }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme))
        ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    if (selectedFiles.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = { selectedFiles.clear() }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear selection",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${selectedFiles.size} selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { pendingDelete = selectedFiles.toList() }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = if (selectedFiles.size == 1) {
                                        "Delete file"
                                    } else {
                                        "Delete ${selectedFiles.size} files"
                                    },
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            Button(
                                onClick = {
                                    onImportMultipleFiles(selectedFiles.toList(), false)
                                    selectedFiles.clear()
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Batch Import", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    onImportMultipleFiles(selectedFiles.toList(), true)
                                    selectedFiles.clear()
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Batch Queue", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (canGoUp) {
                                            onGoUp()
                                        } else {
                                            onDismiss()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        "File browser",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        when {
                                            allFilesAccessGranted -> "All Files access • ${location?.label ?: "Phone storage"}"
                                            roots.isEmpty() -> "No folders approved"
                                            else -> location?.label
                                                ?: "${roots.size} approved folder${if (roots.size == 1) "" else "s"}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Box {
                                IconButton(
                                    onClick = { showMoreMenu = true },
                                    enabled = !importing
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "More options",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Import with file picker") },
                                        enabled = !importing,
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenFilePicker()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Go up") },
                                        enabled = canGoUp && !scanning,
                                        onClick = {
                                            showMoreMenu = false
                                            onGoUp()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Refresh files") },
                                        enabled = (roots.isNotEmpty() || allFilesAccessGranted) && !scanning,
                                        onClick = {
                                            showMoreMenu = false
                                            onRefresh()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (allFilesAccessGranted) "All files access granted" else "Grant all files access") },
                                        enabled = !allFilesAccessGranted,
                                        onClick = {
                                            showMoreMenu = false
                                            onRequestAllFilesAccess()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Folders to scan") },
                                        onClick = {
                                            showMoreMenu = false
                                            onPickFolder()
                                        }
                                    )
                                    FeatureDropdownMenuItem(
                                        feature = browserFeatures.requireResolvedFeature(
                                            VeritasFeatureId.FILE_BROWSER_SORTING
                                        ),
                                        label = "Sort files",
                                        onClick = {
                                            showMoreMenu = false
                                            showSortDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Remove all files access") },
                                        enabled = roots.isNotEmpty(),
                                        onClick = {
                                            showMoreMenu = false
                                            onRemoveAllAccess()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .padding(horizontal = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "Search files...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VeritasBrowserTab.entries.forEach { tab ->
                            val count =
                                if (tab == VeritasBrowserTab.ALL) entries.count { !it.isDirectory } else entries.count { !it.isDirectory && it.type == tab }
                            if (selectedTab == tab) {
                                Button(
                                    onClick = { selectedTab = tab },
                                    shape = VeritasPackStyle.chipShape(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${tab.label} $count")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedTab = tab },
                                    shape = VeritasPackStyle.chipShape(),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${tab.label} $count")
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${visibleEntries.count { !it.isDirectory }} files, ${visibleEntries.count { it.isDirectory }} folders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )

                        // Sort Dropdown Chip
                        Box {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                                    .clickable { showSortMenu = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${sortMode.label} ${if (sortAscending) "▲" else "▼"}",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                VeritasBrowserSort.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text("Sort by ${mode.label}") },
                                        onClick = {
                                            sortMode = mode
                                            showSortMenu = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(if (sortAscending) "Descending order" else "Ascending order") },
                                    onClick = {
                                        sortAscending = !sortAscending
                                        showSortMenu = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // View Menu (View mode / Refresh)
                        Box {
                            IconButton(onClick = { showViewMenu = true }) {
                                Text(viewMode.icon, style = MaterialTheme.typography.titleMedium)
                            }
                            DropdownMenu(expanded = showViewMenu, onDismissRequest = { showViewMenu = false }) {
                                LibraryViewMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text("${mode.icon} ${mode.label}") },
                                        onClick = {
                                            viewMode = mode
                                            browserPrefs.edit().putString("file_view_mode", mode.name).apply()
                                            showViewMenu = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Refresh files") },
                                    onClick = {
                                        onRefresh()
                                        showViewMenu = false
                                    }
                                )
                            }
                        }
                    }

                    message?.let {
                        Text(
                            it,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (importing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Importing ${importingName.ifBlank { "selected file" }}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    when {
                        roots.isEmpty() && !allFilesAccessGranted -> FileBrowserEmptyState(
                            onPickFolder = onPickFolder,
                            onRequestAllFilesAccess = onRequestAllFilesAccess
                        )

                        scanning -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    "Opening folder.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        visibleEntries.isEmpty() -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No files or folders match this view.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        else -> LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {

                            if (files.isNotEmpty()) {
                                item("files-header") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            "Documents",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "PDF, DOCX, PPTX, TXT, EPUB, HTML",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (viewMode == LibraryViewMode.TILES) {
                                val chunkedFiles = files.chunked(columnCount)
                                items(chunkedFiles.size) { rowIndex ->
                                    val rowFiles = chunkedFiles[rowIndex]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowFiles.forEach { file ->
                                            val isSelected = selectedFiles.any { it.uri == file.uri }
                                            FileBrowserFileTileCard(
                                                file = file,
                                                importing = importing,
                                                onOpenDirectory = { onEnterDirectory(file) },
                                                onImport = { onImportFile(file) },
                                                isSelected = isSelected,
                                                onSelectedChange = { checked ->
                                                    if (checked) {
                                                        if (selectedFiles.none { it.uri == file.uri }) selectedFiles.add(file)
                                                    } else {
                                                        selectedFiles.removeAll { it.uri == file.uri }
                                                    }
                                                },
                                                selectionMode = selectedFiles.isNotEmpty(),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        val remainder = columnCount - rowFiles.size
                                        repeat(remainder) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                items(files, key = { it.uri.toString() }) { file ->
                                    val isSelected = selectedFiles.any { it.uri == file.uri }
                                    FileBrowserFileRow(
                                        file = file,
                                        viewMode = viewMode,
                                        importing = importing,
                                        onOpenDirectory = { onEnterDirectory(file) },
                                        onImport = { onImportFile(file) },
                                        isSelected = isSelected,
                                        onSelectedChange = { checked ->
                                            if (checked) {
                                                if (selectedFiles.none { it.uri == file.uri }) selectedFiles.add(file)
                                            } else {
                                                selectedFiles.removeAll { it.uri == file.uri }
                                            }
                                        },
                                        selectionMode = selectedFiles.isNotEmpty()
                                    )
                                }
                            }
                            if (folders.isNotEmpty()) {
                                item("folders-header") {
                                    Text(
                                        "Folders",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }
                            }
                            if (viewMode == LibraryViewMode.TILES) {
                                val chunkedFolders = folders.chunked(columnCount)
                                items(chunkedFolders.size) { rowIndex ->
                                    val rowFolders = chunkedFolders[rowIndex]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowFolders.forEach { folder ->
                                            FileBrowserFileTileCard(
                                                file = folder,
                                                importing = importing,
                                                onOpenDirectory = {
                                                    if (folder.name == ".. (Go up)") {
                                                        onGoUp()
                                                    } else {
                                                        onEnterDirectory(folder)
                                                    }
                                                },
                                                onImport = { onImportFile(folder) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        val remainder = columnCount - rowFolders.size
                                        repeat(remainder) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                items(folders, key = { it.uri.toString() }) { folder ->
                                    FileBrowserFileRow(
                                        file = folder,
                                        viewMode = viewMode,
                                        importing = importing,
                                        onOpenDirectory = {
                                            if (folder.name == ".. (Go up)") {
                                                onGoUp()
                                            } else {
                                                onEnterDirectory(folder)
                                            }
                                        },
                                        onImport = { onImportFile(folder) }
                                    )
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = onOpenFilePicker,
                    enabled = !importing,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(22.dp),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text("＋ Import")
                }
                if (importing) {
                    ImportProgressOverlay(importingName.ifBlank { "selected file" })
                }

                if (showSortDialog) {
                    FileBrowserSortDialog(
                        sortMode = sortMode,
                        sortAscending = sortAscending,
                        onSortModeChange = { sortMode = it },
                        onSortAscendingChange = { sortAscending = it },
                        onDismiss = { showSortDialog = false }
                    )
                }
            }
        }
    }

@Composable
internal fun FileBrowserEmptyState(
    onPickFolder: () -> Unit,
    onRequestAllFilesAccess: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "No file access yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Grant All Files access for broad phone storage browsing, or choose specific folders to scan.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onRequestAllFilesAccess,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Grant all files access") }
                Button(
                    onClick = onPickFolder,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Folders to scan") }
            }
        }
    }
}

@Composable
internal fun ImportProgressOverlay(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    "Importing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Extracting readable text and preserving the original file.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun getFileColorAndIcon(file: VeritasBrowserFile): Triple<ImageVector, Color, Color> {
    if (file.isDirectory) {
        if (file.name == ".. (Go up)") {
            val tint = MaterialTheme.colorScheme.primary
            val bg = MaterialTheme.colorScheme.primaryContainer
            return Triple(Icons.AutoMirrored.Filled.ArrowBack, tint, bg)
        }
        val tint = Color(0xFFF2994A)
        val bg = tint.copy(alpha = 0.18f)
        return Triple(Icons.Outlined.Folder, tint, bg)
    }
    val tint = when (file.type) {
        VeritasBrowserTab.PDF -> Color(0xFFE24B4A)
        VeritasBrowserTab.DOC -> Color(0xFF7C6FFF)
        VeritasBrowserTab.BOOKS -> Color(0xFF0288D1)
        VeritasBrowserTab.HTML -> Color(0xFF2F80ED)
        VeritasBrowserTab.TXT -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.secondary
    }
    val bg = tint.copy(alpha = 0.18f)
    val icon = when (file.type) {
        VeritasBrowserTab.PDF -> Icons.Outlined.PictureAsPdf
        VeritasBrowserTab.DOC -> Icons.Outlined.Description
        VeritasBrowserTab.BOOKS -> Icons.Outlined.Book
        VeritasBrowserTab.HTML -> Icons.Outlined.Language
        VeritasBrowserTab.TXT -> Icons.AutoMirrored.Outlined.Article
        else -> Icons.AutoMirrored.Outlined.Article
    }
    return Triple(icon, tint, bg)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FileBrowserFileRow(
    file: VeritasBrowserFile,
    viewMode: LibraryViewMode,
    importing: Boolean,
    onOpenDirectory: () -> Unit,
    onImport: () -> Unit,
    isSelected: Boolean? = null,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    selectionMode: Boolean = false
) {
    val enabled = if (file.isDirectory) file.targetLocation != null else file.isSupported && !importing
    val action = if (file.isDirectory) onOpenDirectory else onImport
    val haptic = rememberVeritasHaptics()

    val padding = when (viewMode) {
        LibraryViewMode.SMALL -> 6.dp
        LibraryViewMode.LIST -> 8.dp
        LibraryViewMode.MEDIUM -> 12.dp
        LibraryViewMode.DETAILS -> 14.dp
        else -> 10.dp
    }
    val coverSize = when (viewMode) {
        LibraryViewMode.SMALL -> 36.dp
        LibraryViewMode.LIST -> 46.dp
        LibraryViewMode.MEDIUM -> 58.dp
        LibraryViewMode.DETAILS -> 72.dp
        else -> 54.dp
    }
    val titleStyle = when (viewMode) {
        LibraryViewMode.SMALL -> MaterialTheme.typography.bodyMedium
        LibraryViewMode.LIST -> MaterialTheme.typography.bodyLarge
        LibraryViewMode.MEDIUM -> MaterialTheme.typography.titleSmall
        LibraryViewMode.DETAILS -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.bodyLarge
    }
    val showDetails = viewMode == LibraryViewMode.DETAILS || viewMode == LibraryViewMode.LIST
    val shape = if (viewMode == LibraryViewMode.SMALL || viewMode == LibraryViewMode.LIST) MaterialTheme.shapes.medium else MaterialTheme.shapes.large

    val (icon, tint, bg) = getFileColorAndIcon(file)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    if (selectionMode && !file.isDirectory) {
                        haptic.toggle(isSelected != true)
                        if (isSelected == true) onSelectedChange?.invoke(false)
                        else onSelectedChange?.invoke(true)
                    } else {
                        action()
                    }
                },
                onLongClick = {
                    if (!file.isDirectory && onSelectedChange != null) {
                        haptic.longPress()
                        onSelectedChange(true)
                    }
                }
            ),
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
        ),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Row(
            modifier = Modifier.padding(padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectionMode && isSelected != null && onSelectedChange != null && !file.isDirectory) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        haptic.toggle(checked)
                        onSelectedChange(checked)
                    },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(coverSize)
                    .background(bg, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (viewMode == LibraryViewMode.SMALL) 20.dp else 24.dp),
                    tint = tint
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = file.name,
                    maxLines = if (showDetails) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (showDetails) {
                    Text(
                        text = fileBrowserFolderLine(file),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = when {
                        file.name == ".. (Go up)" -> "Go up one folder level"
                        file.isDirectory && file.targetLocation != null -> "Folder • ${formatBrowserModified(file.modifiedAt)}"
                        file.isDirectory -> "Protected folder • Android may block this path"
                        file.isSupported -> "${formatBrowserFileSize(file.sizeBytes)} • ${formatBrowserModified(file.modifiedAt)}"
                        else -> "Unsupported file • ${formatBrowserFileSize(file.sizeBytes)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val buttonEnabled = enabled && (!selectionMode || file.isDirectory)
            Box(
                modifier = Modifier
                    .background(
                        if (buttonEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(50)
                    )
                    .clickable(enabled = buttonEnabled) {
                        if (file.isDirectory) onOpenDirectory() else onImport()
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (file.isDirectory) "Open" else if (file.isSupported) "Import" else "Unsupported",
                    color = if (buttonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FileBrowserFileTileCard(
    file: VeritasBrowserFile,
    importing: Boolean,
    onOpenDirectory: () -> Unit,
    onImport: () -> Unit,
    isSelected: Boolean? = null,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    selectionMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val enabled = if (file.isDirectory) file.targetLocation != null else file.isSupported && !importing
    val action = if (file.isDirectory) onOpenDirectory else onImport
    val haptic = rememberVeritasHaptics()

    val (icon, tint, bg) = getFileColorAndIcon(file)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    if (selectionMode && !file.isDirectory) {
                        haptic.toggle(isSelected != true)
                        if (isSelected == true) onSelectedChange?.invoke(false)
                        else onSelectedChange?.invoke(true)
                    } else {
                        action()
                    }
                },
                onLongClick = {
                    if (!file.isDirectory && onSelectedChange != null) {
                        haptic.longPress()
                        onSelectedChange(true)
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .background(bg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = tint
                    )
                }
                if (selectionMode && isSelected != null && onSelectedChange != null && !file.isDirectory) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            haptic.toggle(checked)
                            onSelectedChange(checked)
                        },
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    )
                }
            }
            Text(
                text = file.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    file.name == ".. (Go up)" -> "Parent directory"
                    file.isDirectory && file.targetLocation != null -> "Folder • ${formatBrowserModified(file.modifiedAt)}"
                    file.isDirectory -> "Protected folder"
                    file.isSupported -> "${formatBrowserFileSize(file.sizeBytes)} • ${formatBrowserModified(file.modifiedAt)}"
                    else -> "Unsupported file"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val buttonEnabled = enabled && (!selectionMode || file.isDirectory)
                Box(
                    modifier = Modifier
                        .background(
                            if (buttonEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(50)
                        )
                        .clickable(enabled = buttonEnabled) {
                            if (file.isDirectory) onOpenDirectory() else onImport()
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (file.name == ".. (Go up)") "Go Up" else if (file.isDirectory) "Open" else if (file.isSupported) "Import" else "Unsupported",
                        color = if (buttonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun FileBrowserSortDialog(
    sortMode: VeritasBrowserSort,
    sortAscending: Boolean,
    onSortModeChange: (VeritasBrowserSort) -> Unit,
    onSortAscendingChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        title = { Text("Sort files by") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VeritasBrowserSort.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortModeChange(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortMode == option,
                            onClick = { onSortModeChange(option) })
                        Text(option.label, modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Order:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortAscendingChange(true) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = sortAscending, onClick = { onSortAscendingChange(true) })
                    Text("Ascending", modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortAscendingChange(false) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !sortAscending,
                        onClick = { onSortAscendingChange(false) })
                    Text("Descending", modifier = Modifier.weight(1f))
                }
            }
        }
    )
}

internal fun fileBrowserFolderLine(file: VeritasBrowserFile): String {
    if (file.name == ".. (Go up)") return "Parent directory"
    val folderPath = file.relativePath.substringBeforeLast('/', missingDelimiterValue = "")
    return if (folderPath.isBlank()) file.rootLabel else "${file.rootLabel}/$folderPath"
}

internal fun formatBrowserFileSize(bytes: Long): String {
    if (bytes <= 0L) return "Unknown size"
    val units = listOf("B", "kB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return if (unitIndex == 0) {
        "${bytes} ${units[unitIndex]}"
    } else {
        String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
    }
}

internal fun formatBrowserModified(timestamp: Long): String =
    if (timestamp > 0L) formatUpdated(timestamp) else "Unknown date"

@Composable
internal fun ReadingHistoryDialog(
    history: List<ReadingHistoryEntry>,
    documents: List<SavedDocument>,
    onDismiss: () -> Unit,
    onOpenDocument: (SavedDocument) -> Unit,
    onClearHistory: () -> Unit
) {
    val docsById = remember(documents) { documents.associateBy { it.id } }
    val visibleHistory = remember(history, documents) {
        history.mapNotNull { entry ->
            docsById[entry.documentId]?.let { document -> entry to document }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Close") } },
        dismissButton = {
            OutlinedButton(
                onClick = onClearHistory,
                enabled = visibleHistory.isNotEmpty(),
                shape = RoundedCornerShape(50)
            ) { Text("Clear") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Reading history")
            }
        },
        text = {
            if (visibleHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "No reading history yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Open a reading from your library and it will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        visibleHistory,
                        key = { (entry, _) -> entry.documentId }) { (entry, document) ->
                        ReadingHistoryRow(
                            entry = entry,
                            document = document,
                            onOpen = { onOpenDocument(document) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
internal fun ReadingHistoryRow(
    entry: ReadingHistoryEntry,
    document: SavedDocument,
    onOpen: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coverFile = remember(document.id) { CoverExtractor.coverFile(context, document.id) }
    val safeChunkCount = entry.chunkCount.coerceAtLeast(document.chunkCount).coerceAtLeast(1)
    val safeIndex = entry.currentIndex.coerceIn(0, safeChunkCount - 1)
    val progress = ((safeIndex + 1).toFloat() / safeChunkCount.toFloat()).coerceIn(0f, 1f)
    val percent = (progress * 100f).toInt().coerceIn(0, 100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp, 68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (coverFile != null && coverFile.exists()) {
                    val bitmap = remember(coverFile.absolutePath) {
                        android.graphics.BitmapFactory.decodeFile(coverFile.absolutePath)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = document.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            document.sourceLabel.take(3).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    Text(
                        document.sourceLabel.take(3).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    document.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "$percent%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        "Sentence ${safeIndex + 1} of $safeChunkCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Text(
                    "Opened ${formatUpdated(entry.openedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            IconButton(
                onClick = onOpen,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Resume",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}


fun Map<VeritasFeatureId, ResolvedVeritasFeature>.requireResolvedFeature(
    id: VeritasFeatureId
): ResolvedVeritasFeature = getValue(id)

data class ReaderTextSelection(
    val partIndex: Int,
    val start: Int,
    val endExclusive: Int,
    val text: String,
    val sentenceIndexes: List<Int>
) {
    val firstSentenceIndex: Int
        get() = sentenceIndexes.firstOrNull() ?: 0

    val endSentenceIndexExclusive: Int
        get() = (sentenceIndexes.lastOrNull() ?: firstSentenceIndex) + 1
}

@Composable
internal fun PdfImportOptionsDialog(
    options: PdfImportOptions,
    textOptions: TextImportOptions,
    onOptionsChange: (PdfImportOptions) -> Unit,
    onTextOptionsChange: (TextImportOptions) -> Unit,
    onPickPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    var startPageDraft by remember(options.startPage) {
        mutableStateOf(
            options.startPage?.toString().orEmpty()
        )
    }
    var endPageDraft by remember(options.endPage) {
        mutableStateOf(
            options.endPage?.toString().orEmpty()
        )
    }
    var modeExpanded by remember { mutableStateOf(false) }
    var encodingExpanded by remember { mutableStateOf(false) }
    val extractionModes = remember {
        listOf(
            "HTML with images",
            "Plain text",
            "Prefer OCR when text is poor",
            "Force OCR"
        )
    }
    val selectedEncoding = TextImportEncodingCatalog.byId(textOptions.encodingId)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            // imePadding: this page has page-number inputs; with edge-to-edge the soft
            // keyboard must not cover them.
            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("PDF & import tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Control how PDFs and text files are imported before they enter the reader.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = startPageDraft,
                            onValueChange = { value ->
                                startPageDraft = value.filter { it.isDigit() }.take(5)
                                onOptionsChange(
                                    options.copy(
                                        startPage = startPageDraft.toIntOrNull()?.takeIf { it > 0 })
                                )
                            },
                            label = { Text("Start page") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endPageDraft,
                            onValueChange = { value ->
                                endPageDraft = value.filter { it.isDigit() }.take(5)
                                onOptionsChange(
                                    options.copy(
                                        endPage = endPageDraft.toIntOrNull()?.takeIf { it > 0 })
                                )
                            },
                            label = { Text("End page") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Column {
                        Text("Extraction mode", fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(
                                onClick = { modeExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    options.extractionMode.ifBlank { "HTML with images" },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = modeExpanded,
                                onDismissRequest = { modeExpanded = false }) {
                                extractionModes.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode) },
                                        onClick = {
                                            modeExpanded = false
                                            onOptionsChange(
                                                options.copy(
                                                    extractionMode = mode,
                                                    forceOcr = mode == "Force OCR",
                                                    preferOcrWhenLowText = mode != "Plain text"
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Column {
                        Text("Text encoding", fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(
                                onClick = { encodingExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    selectedEncoding.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = encodingExpanded,
                                onDismissRequest = { encodingExpanded = false }) {
                                TextImportEncodingCatalog.options.forEach { encoding ->
                                    DropdownMenuItem(
                                        text = { Text(encoding.label) },
                                        onClick = {
                                            encodingExpanded = false
                                            onTextOptionsChange(textOptions.copy(encodingId = encoding.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    PdfImportToggleRow(
                        title = "Clean repeated headers and footers",
                        checked = options.cleanupRepeatedLines,
                        onCheckedChange = { onOptionsChange(options.copy(cleanupRepeatedLines = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Remove page numbers",
                        checked = options.removePageNumbers,
                        onCheckedChange = { onOptionsChange(options.copy(removePageNumbers = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Repair hyphenated line breaks",
                        checked = options.repairHyphenation,
                        onCheckedChange = { onOptionsChange(options.copy(repairHyphenation = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Include page markers",
                        checked = options.includePageMarkers,
                        onCheckedChange = { onOptionsChange(options.copy(includePageMarkers = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Match original layout where possible",
                        checked = options.markPdfLinesForCanvas,
                        onCheckedChange = { onOptionsChange(options.copy(markPdfLinesForCanvas = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Remove top page noise",
                        checked = options.removeTopPageNoise,
                        onCheckedChange = { onOptionsChange(options.copy(removeTopPageNoise = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Remove bottom page noise",
                        checked = options.removeBottomPageNoise,
                        onCheckedChange = { onOptionsChange(options.copy(removeBottomPageNoise = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Manual crop before extract",
                        checked = options.manualCropBeforeExtract,
                        onCheckedChange = { onOptionsChange(options.copy(manualCropBeforeExtract = it)) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = options.minWordGap,
                        onValueChange = { value ->
                            onOptionsChange(options.copy(minWordGap = value.filter { it.isDigit() || it == '.' }
                                .take(6).ifBlank { "0.1" }))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Minimum word gap") },
                        singleLine = true
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Separate words when font changes",
                        checked = options.separateWordsOnFontChange,
                        onCheckedChange = { onOptionsChange(options.copy(separateWordsOnFontChange = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Prefer OCR when PDF text is weak",
                        checked = options.preferOcrWhenLowText,
                        onCheckedChange = { onOptionsChange(options.copy(preferOcrWhenLowText = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Force OCR for PDF import",
                        checked = options.forceOcr,
                        onCheckedChange = { onOptionsChange(options.copy(forceOcr = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Force fresh extraction",
                        checked = options.forceFreshExtraction,
                        onCheckedChange = { onOptionsChange(options.copy(forceFreshExtraction = it)) }
                    )
                }
            }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Done") }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onPickPdf, shape = RoundedCornerShape(50)) { Text("Open file browser") }
                }
            }
        }
    }
}

@Composable
internal fun PdfImportToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, modifier = Modifier.weight(1f))
        VeritasSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

internal fun Menu.addReaderSelectionFeature(
    itemId: Int,
    order: Int,
    title: String,
    feature: ResolvedVeritasFeature,
    showAsAction: Int? = null
) {
    val item = add(0, itemId, order, title)
    item.isEnabled = feature.enabled
    if (showAsAction != null) {
        item.setShowAsAction(showAsAction)
    }
}

internal const val READER_SELECTION_READ_FROM_HERE = 6101
internal const val READER_SELECTION_NOTE = 6103
internal const val READER_SELECTION_BOOKMARK = 6104

@Composable
internal fun SyncCenterDialog(
    documentCount: Int,
    annotationCount: Int,
    queueCount: Int,
    pronunciationRuleCount: Int,
    inProgress: Boolean,
    message: String?,
    onExportSyncPack: () -> Unit,
    onShareSyncPack: () -> Unit,
    onImportSyncPack: () -> Unit,
    onDismiss: () -> Unit,
    fullBackupEstimateBytes: Long = 0L,
    autoBackupEnabled: Boolean = true,
    onToggleAutoBackup: () -> Unit = {},
    onExportFull: () -> Unit = {}
) {
    SyncAndBackupCenterDialog(
        documentCount = documentCount,
        annotationCount = annotationCount,
        queueCount = queueCount,
        pronunciationRuleCount = pronunciationRuleCount,
        inProgress = inProgress,
        message = message,
        onExportSyncPack = onExportSyncPack,
        onShareSyncPack = onShareSyncPack,
        onImportSyncPack = onImportSyncPack,
        onDismiss = onDismiss,
        fullBackupEstimateBytes = fullBackupEstimateBytes,
        autoBackupEnabled = autoBackupEnabled,
        onToggleAutoBackup = onToggleAutoBackup,
        onExportFull = onExportFull
    )
}

@Composable
internal fun SyncAndBackupCenterDialog(
    documentCount: Int,
    annotationCount: Int,
    queueCount: Int,
    pronunciationRuleCount: Int,
    inProgress: Boolean,
    message: String?,
    onExportSyncPack: () -> Unit,
    onShareSyncPack: () -> Unit,
    onImportSyncPack: () -> Unit,
    onDismiss: () -> Unit,
    fullBackupEstimateBytes: Long = 0L,
    autoBackupEnabled: Boolean = true,
    onToggleAutoBackup: () -> Unit = {},
    onExportFull: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sync & Backup Center",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Summary Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Library Sync & Backup Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Veritas Sync & Backup creates portable backup files containing your entire library, reading progress, flashcards, notes, bookmarks, and custom voice rules. Import safely merges data without deleting local readings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Included Data Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                        ),
                        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Data Included in Backup & Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            SyncInfoRow("Saved Readings", documentCount.toString())
                            SyncInfoRow("Bookmarks & Notes", annotationCount.toString())
                            SyncInfoRow("Reading Queue", queueCount.toString())
                            SyncInfoRow("Pronunciation Rules", pronunciationRuleCount.toString())
                            SyncInfoRow("Flashcards, Decks & Progress", "Included")
                            SyncInfoRow("Reading Streaks & History", "Included")
                            SyncInfoRow("Voice & Reader Settings", "Included")
                        }
                    }

                    // Actions Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                        ),
                        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Backup & Sync Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = onExportSyncPack,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = documentCount > 0,
                                shape = VeritasPackStyle.chipShape(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Export Data Backup (.json)", fontWeight = FontWeight.Bold)
                            }

                            val fullBackupSizeMb = fullBackupEstimateBytes / (1024.0 * 1024.0)
                            OutlinedButton(
                                onClick = onExportFull,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = documentCount > 0,
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text(
                                    if (fullBackupEstimateBytes > 0)
                                        String.format(Locale.getDefault(), "Export Full Library (.zip ~%.1f MB)", fullBackupSizeMb)
                                    else
                                        "Export Full Library (.zip)",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onShareSyncPack,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = documentCount > 0,
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text("Share Sync Pack (Drive / WhatsApp / Files)", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onImportSyncPack,
                                modifier = Modifier.fillMaxWidth(),
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text("Import Sync / Backup Pack", fontWeight = FontWeight.Bold)
                            }

                            BackupStatusBlock(inProgress = inProgress, message = message)
                        }
                    }

                    // Auto-Backup Settings Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                        ),
                        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatic Weekly Backups",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Automatically save local backup archives every 7 days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            VeritasSwitch(
                                checked = autoBackupEnabled,
                                onCheckedChange = { onToggleAutoBackup() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SyncInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SoftChip(value)
    }
}
