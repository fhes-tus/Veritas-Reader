package com.veritas.reader

import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.material.icons.outlined.PowerSettingsNew
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
import com.veritas.reader.ui.*
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

    val initialMemos = remember(audioPath, audioDuration) {
        if (audioPath.isNullOrBlank()) emptyList<Pair<String, Int>>()
        else {
            val parts = audioPath.split("||").map { it.trim() }.filter { it.isNotEmpty() }
            parts.map { p ->
                val d = try {
                    val f = java.io.File(p)
                    if (f.exists()) {
                        val ret = android.media.MediaMetadataRetriever()
                        ret.setDataSource(p)
                        val s = ret.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                        ret.release()
                        (s / 1000).toInt()
                    } else audioDuration
                } catch (e: Exception) {
                    audioDuration
                }
                Pair(p, d)
            }
        }
    }
    var audioMemos by remember { mutableStateOf(initialMemos) }
    val recordingState by VoiceNoteRecorder.recordingState.collectAsState()
    val recordingDuration by VoiceNoteRecorder.recordingDurationSeconds.collectAsState()
    val activeAudioPath by VoiceNoteRecorder.activeAudioPath.collectAsState()

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

                // Multiple Voice Memos Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    audioMemos.forEachIndexed { idx, memo ->
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
                                val isThisPlaying = recordingState == VoiceRecordingState.PLAYING && activeAudioPath == memo.first
                                Text(
                                    if (isThisPlaying) "▶️ Playing memo ${idx + 1} (${memo.second}s)"
                                    else "🎙️ Voice memo ${idx + 1} (${memo.second}s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isThisPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        if (isThisPlaying) {
                                            VoiceNoteRecorder.stopPlayback()
                                        } else {
                                            VoiceNoteRecorder.playAudio(memo.first)
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (isThisPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow,
                                        contentDescription = if (isThisPlaying) "Stop" else "Play",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (isThisPlaying) {
                                            VoiceNoteRecorder.stopPlayback()
                                        }
                                        VoiceNoteRecorder.deleteAudioFile(memo.first)
                                        val updated = audioMemos.filterIndexed { i, _ -> i != idx }
                                        audioMemos = updated
                                        val combined = if (updated.isEmpty()) null else updated.joinToString("||") { it.first }
                                        val totDur = updated.sumOf { it.second }
                                        onAudioChange(combined, totDur)
                                    }
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "Delete voice memo",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Recording or Add Memo Row
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (recordingState == VoiceRecordingState.RECORDING) {
                                Text(
                                    "🔴 Recording memo... (${recordingDuration}s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val result = VoiceNoteRecorder.stopRecording()
                                        if (result != null) {
                                            val updated = audioMemos + Pair(result.first, result.second)
                                            audioMemos = updated
                                            val combined = updated.joinToString("||") { it.first }
                                            val totDur = updated.sumOf { it.second }
                                            onAudioChange(combined, totDur)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Stop")
                                }
                            } else {
                                Text(
                                    if (audioMemos.isEmpty()) "Attach voice memo" else "Add another voice memo",
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
                                    Text(if (audioMemos.isEmpty()) "Record" else "Add memo")
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
                    val combined = if (audioMemos.isEmpty()) null else audioMemos.joinToString("||") { it.first }
                    val totDur = audioMemos.sumOf { it.second }
                    onAudioChange(combined, totDur)
                    onSave()
                },
                enabled = noteDraft.trim().isNotBlank() || audioMemos.isNotEmpty(),
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
                        audioMemos.forEach { VoiceNoteRecorder.deleteAudioFile(it.first) }
                        onAudioChange(null, 0)
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

@Preview(showBackground = true)
@Composable
internal fun SentenceNoteDialogPreview() {
    val sampleDoc = remember {
        ReaderDocument(
            id = "doc_1",
            title = "Meditations",
            sourceLabel = "EPUB",
            rawText = "Waste no more time arguing what a good man should be. Be one.",
            sentences = listOf("Waste no more time arguing what a good man should be.", "Be one.")
        )
    }
    MaterialTheme {
        SentenceNoteDialog(
            document = sampleDoc,
            sentenceIndexes = listOf(0),
            noteDraft = "Reflect on this daily.",
            audioPath = null,
            audioDuration = 0,
            onNoteChange = {},
            onAudioChange = { _, _ -> },
            onSave = {},
            onDelete = {},
            onDismiss = {}
        )
    }
}
