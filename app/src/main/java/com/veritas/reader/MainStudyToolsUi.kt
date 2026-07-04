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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Article
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
internal fun BackupRestoreDialog(
    documentCount: Int,
    annotationCount: Int,
    queueCount: Int,
    inProgress: Boolean,
    message: String?,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Close") } },
        title = { Text("Backup & restore") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Export a portable Veritas backup, or import a backup into this library. Import adds restored readings without deleting the current library.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Current library", fontWeight = FontWeight.Black)
                        Text(
                            "$documentCount readings • $annotationCount bookmarks/notes • $queueCount queued",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = documentCount > 0,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Export library backup")
                }
                OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)) {
                    Text("Import backup file")
                }
                BackupStatusBlock(inProgress = inProgress, message = message)
                Text(
                    "Backup includes saved text, progress, queue, reading lists, bookmarks, document notes, sentence notes, pronunciation rules, reader settings, and voice settings.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
internal fun BackupStatusBlock(
    inProgress: Boolean,
    message: String?
) {
    if (!inProgress && message.isNullOrBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (inProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Working on backup file...", style = MaterialTheme.typography.bodySmall)
            }
            if (!message.isNullOrBlank()) {
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun TranslationToolsDialog(
    document: ReaderDocument,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSend: (String, TranslationLauncher.Mode) -> Unit
) {
    var targetLanguage by remember { mutableStateOf("English") }
    val currentPreview = document.chunks.getOrNull(currentIndex).orEmpty().take(180)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("⇄") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Use the translation or AI apps already installed on this phone. Veritas prepares the prompt and opens the Android share sheet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = targetLanguage,
                    onValueChange = { targetLanguage = it },
                    label = { Text("Target language") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (currentPreview.isNotBlank()) {
                    Text(
                        "Current sentence: $currentPreview${
                            if (document.chunks.getOrNull(
                                    currentIndex
                                ).orEmpty().length > 180
                            ) "…" else ""
                        }",
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { onSend(targetLanguage, TranslationLauncher.Mode.CURRENT_SECTION) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Translate current sentence") }
                OutlinedButton(
                    onClick = { onSend(targetLanguage, TranslationLauncher.Mode.DOCUMENT) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Translate full document") }
                OutlinedButton(
                    onClick = {
                        onSend(
                            targetLanguage,
                            TranslationLauncher.Mode.BILINGUAL_SECTION
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Bilingual current sentence") }
                OutlinedButton(
                    onClick = {
                        onSend(
                            targetLanguage,
                            TranslationLauncher.Mode.BILINGUAL_DOCUMENT
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Bilingual full document") }
            }
        }
    )
}


@Composable
internal fun AiFreeModeDialog(
    documentCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("AI & study mode") },
        text = {
            Column(
                modifier = Modifier
                    .height(520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Free AI approach",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Veritas does not bundle a large offline model and does not require your OpenAI API key. It uses installed AI apps on the phone plus lightweight offline study tools.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StudyCard(title = "1. AI app handoff") {
                    Text("Open a document, tap Reader tools → AI, choose a task, then send the prepared prompt to ChatGPT, Gemini, Claude, Copilot, Perplexity, or another installed app.")
                }
                StudyCard(title = "2. Offline study tools") {
                    Text("For no-internet revision, Veritas can create simple summaries, key points, terms, flashcards, and quizzes using local document logic. This is smaller but less powerful than cloud AI.")
                }
                StudyCard(title = "3. Base app stays lighter") {
                    Text("No heavy local AI model is bundled in the base app. A real offline model can be optional later as a separate downloadable pack.")
                }
                Text(
                    "Current library: $documentCount saved reading${if (documentCount == 1) "" else "s"}.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
internal fun AiCenterDialog(
    installedAiCount: Int,
    documentCount: Int,
    onOpenAskAiSettings: () -> Unit,
    onOpenStudyTools: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("AI tools") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Use installed AI apps or local study tools without adding paid APIs or account-gated services inside Veritas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("$installedAiCount compatible AI app${if (installedAiCount == 1) "" else "s"} detected.")
                Text("$documentCount reading${if (documentCount == 1) "" else "s"} available for study workflows.")
                Button(onClick = onOpenStudyTools, modifier = Modifier.fillMaxWidth()) {
                    Text("Open AI Study Tools")
                }
                OutlinedButton(onClick = onOpenAskAiSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Ask AI app settings")
                }
            }
        }
    )
}

@Composable
internal fun ExportAudioStatusDialog(
    inProgress: Boolean,
    message: String?,
    file: File?,
    onShare: (File) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!inProgress) onDismiss() },
        title = { Text("Export audio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (inProgress) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Creating audio from this reading...")
                    }
                }
                Text(
                    message ?: "Preparing export...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (file != null) {
                    Text(file.name, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            if (file != null) {
                Button(onClick = { onShare(file) }) { Text("Share") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !inProgress) { Text("Close") }
        }
    )
}

@Composable
internal fun ScopeSelector(
    selectedScope: AiPromptScope,
    onScopeSelected: (AiPromptScope) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Scope", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiPromptScope.values().forEach { scope ->
                val isSelected = selectedScope == scope
                val label = when (scope) {
                    AiPromptScope.CURRENT_SENTENCE -> "Sentence"
                    AiPromptScope.CURRENT_SECTION -> "Section"
                    AiPromptScope.CUSTOM_PAGE_RANGE -> "Page range"
                    AiPromptScope.WHOLE_DOCUMENT -> "Whole doc"
                }
                if (isSelected) {
                    Button(
                        onClick = { onScopeSelected(scope) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 32.dp)
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onScopeSelected(scope) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 32.dp)
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PageRangeInputs(
    startVal: String,
    onStartChange: (String) -> Unit,
    endVal: String,
    onEndChange: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = startVal,
            onValueChange = { onStartChange(it.filter { char -> char.isDigit() }) },
            label = { Text("From Page", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = endVal,
            onValueChange = { onEndChange(it.filter { char -> char.isDigit() }) },
            label = { Text("To Page", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
internal fun TaskExpandableCard(
    title: String,
    taskKey: String,
    expandedTask: String?,
    onToggleExpand: (String?) -> Unit,
    prompt: String,
    onPromptChange: (String) -> Unit,
    showScopeSelector: Boolean = false,
    selectedScope: AiPromptScope = AiPromptScope.WHOLE_DOCUMENT,
    onScopeSelected: (AiPromptScope) -> Unit = {},
    startPage: String = "",
    onStartPageChange: (String) -> Unit = {},
    endPage: String = "",
    onEndPageChange: (String) -> Unit = {},
    onSend: () -> Unit
) {
    val isExpanded = expandedTask == taskKey
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand(if (isExpanded) null else taskKey) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isExpanded) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            if (isExpanded) {
                if (showScopeSelector) {
                    ScopeSelector(
                        selectedScope = selectedScope,
                        onScopeSelected = onScopeSelected
                    )
                }

                if (selectedScope == AiPromptScope.CUSTOM_PAGE_RANGE || (!showScopeSelector && taskKey == "long_doc")) {
                    PageRangeInputs(
                        startVal = startPage,
                        onStartChange = onStartPageChange,
                        endVal = endPage,
                        onEndChange = onEndPageChange
                    )
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    label = { Text("Custom Prompt (instructions)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = onSend,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send to AI App")
                }
            }
        }
    }
}

@Composable
internal fun AiAppStudyDialog(
    document: ReaderDocument,
    currentIndex: Int,
    templates: List<AiPromptTemplate>,
    history: List<AiPromptHistoryEntry>,
    onDismiss: () -> Unit,
    onSendToAiApp: (AiPromptType, String, AiPromptScope, IntRange?) -> Unit,
    onSaveTemplate: (String, String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onClearHistory: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onSaveAiResultAsNote: (String) -> Unit,
    onOpenOfflineStudyTools: () -> Unit
) {
    var customPrompt by remember { mutableStateOf("") }
    var templateTitle by remember { mutableStateOf("Custom study prompt") }
    var aiResultDraft by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Tasks") }

    val expandedTask = remember { mutableStateOf<String?>(null) }

    val summarizeWholeDocPrompt = remember { mutableStateOf("Summarize the whole document clearly. Give a short overview first, then the main points, then any important conclusions or action items.") }
    val longDocSummaryPrompt = remember { mutableStateOf("This is part of a long-document workflow. Summarize this section or page range only, give 3-6 key points, define difficult terms, and end with a short note saying what the user should send next.") }
    val extractKeypointsPrompt = remember { mutableStateOf("Extract the key points from this document. Group related ideas together and keep the wording clear for revision.") }
    val explainSentencePrompt = remember { mutableStateOf("Explain the current section in simple language. Identify the main idea, difficult terms, and why the section matters in the document.") }
    val studyNotesPrompt = remember { mutableStateOf("Turn this document into organized study notes. Use headings, bullet points, definitions, examples, likely exam areas, and a short final revision checklist.") }
    val simplifyPrompt = remember { mutableStateOf("Rewrite and explain the document in simpler language without removing important meaning. Define difficult words and give short examples where useful.") }
    val quizPrompt = remember { mutableStateOf("Create an exam-style revision quiz from this document. Include multiple choice questions, short answer questions, and answers with explanations.") }
    val flashcardsPrompt = remember { mutableStateOf("Create flashcards from this document. Use a question on the front and a concise answer on the back. Focus on definitions, processes, comparisons, and important facts.") }

    val extractKeypointsScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }
    val studyNotesScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }
    val simplifyScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }
    val quizScope = remember { mutableStateOf(AiPromptScope.CUSTOM_PAGE_RANGE) }
    val flashcardsScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }

    val longDocStartPage = remember { mutableStateOf("") }
    val longDocEndPage = remember { mutableStateOf("") }
    val extractKeypointsStartPage = remember { mutableStateOf("") }
    val extractKeypointsEndPage = remember { mutableStateOf("") }
    val studyNotesStartPage = remember { mutableStateOf("") }
    val studyNotesEndPage = remember { mutableStateOf("") }
    val simplifyStartPage = remember { mutableStateOf("") }
    val simplifyEndPage = remember { mutableStateOf("") }
    val quizStartPage = remember { mutableStateOf("") }
    val quizEndPage = remember { mutableStateOf("") }
    val flashcardsStartPage = remember { mutableStateOf("") }
    val flashcardsEndPage = remember { mutableStateOf("") }
    val safeIndex =
        if (document.chunks.isEmpty()) 0 else currentIndex.coerceIn(0, document.chunks.lastIndex)
    val estimatedTextLength = document.chunks.sumOf { it.length }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("AI handoff") },
        text = {
            Column(
                modifier = Modifier
                    .height(560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Use installed AI apps for free-tier summaries and study help. Veritas prepares the prompt, then you choose ChatGPT, Gemini, Claude, Copilot, Perplexity, or another app.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            document.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${document.sourceLabel} • ${document.chunks.size} sentences • current ${safeIndex + 1} • about $estimatedTextLength characters",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    listOf("Tasks", "Templates", "History", "Result → note").forEach { tab ->
                        val selected = selectedTab == tab
                        if (selected) {
                            Button(onClick = { selectedTab = tab }) { Text(tab) }
                        } else {
                            OutlinedButton(onClick = { selectedTab = tab }) { Text(tab) }
                        }
                    }
                }

                when (selectedTab) {
                    "Tasks" -> {
                        TaskExpandableCard(
                            title = "Summarize whole document",
                            taskKey = "summarize_whole_doc",
                            expandedTask = expandedTask.value,
                            onToggleExpand = { expandedTask.value = it },
                            prompt = summarizeWholeDocPrompt.value,
                            onPromptChange = { summarizeWholeDocPrompt.value = it },
                            onSend = {
                                onSendToAiApp(
                                    AiPromptType.SUMMARY,
                                    summarizeWholeDocPrompt.value,
                                    AiPromptScope.WHOLE_DOCUMENT,
                                    null
                                )
                            }
                        )

                        TaskExpandableCard(
                            title = "Long document: page-to-page summary",
                            taskKey = "long_doc",
                            expandedTask = expandedTask.value,
                            onToggleExpand = { expandedTask.value = it },
                            prompt = longDocSummaryPrompt.value,
                            onPromptChange = { longDocSummaryPrompt.value = it },
                            startPage = longDocStartPage.value,
                            onStartPageChange = { longDocStartPage.value = it },
                            endPage = longDocEndPage.value,
                            onEndPageChange = { longDocEndPage.value = it },
                            onSend = {
                                val start = longDocStartPage.value.toIntOrNull() ?: 1
                                val end = longDocEndPage.value.toIntOrNull() ?: 1
                                val min = minOf(start, end).coerceIn(1, document.pageCount)
                                val max = maxOf(start, end).coerceIn(1, document.pageCount)
                                onSendToAiApp(
                                    AiPromptType.SECTION_BY_SECTION,
                                    longDocSummaryPrompt.value,
                                    AiPromptScope.CUSTOM_PAGE_RANGE,
                                    min..max
                                )
                            }
                        )

                        TaskExpandableCard(
                            title = "Extract key points",
                            taskKey = "extract_keypoints",
                            expandedTask = expandedTask.value,
                            onToggleExpand = { expandedTask.value = it },
                            prompt = extractKeypointsPrompt.value,
                            onPromptChange = { extractKeypointsPrompt.value = it },
                            showScopeSelector = true,
                            selectedScope = extractKeypointsScope.value,
                            onScopeSelected = { extractKeypointsScope.value = it },
                            startPage = extractKeypointsStartPage.value,
                            onStartPageChange = { extractKeypointsStartPage.value = it },
                            endPage = extractKeypointsEndPage.value,
                            onEndPageChange = { extractKeypointsEndPage.value = it },
                            onSend = {
                                val range = if (extractKeypointsScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                    val start = extractKeypointsStartPage.value.toIntOrNull() ?: 1
                                    val end = extractKeypointsEndPage.value.toIntOrNull() ?: 1
                                    minOf(start, end).coerceIn(1, document.pageCount)..maxOf(start, end).coerceIn(1, document.pageCount)
                                } else null
                                onSendToAiApp(
                                    AiPromptType.KEY_POINTS,
                                    extractKeypointsPrompt.value,
                                    extractKeypointsScope.value,
                                    range
                                )
                            }
                        )

                        TaskExpandableCard(
                            title = "Explain current sentence",
                            taskKey = "explain_sentence",
                            expandedTask = expandedTask.value,
                            onToggleExpand = { expandedTask.value = it },
                            prompt = explainSentencePrompt.value,
                            onPromptChange = { explainSentencePrompt.value = it },
                            onSend = {
                                onSendToAiApp(
                                    AiPromptType.EXPLAIN_SECTION,
                                    explainSentencePrompt.value,
                                    AiPromptScope.CURRENT_SENTENCE,
                                    null
                                )
                            }
                        )

                        TaskExpandableCard(
                            title = "Create study notes",
                            taskKey = "study_notes",
                            expandedTask = expandedTask.value,
                            onToggleExpand = { expandedTask.value = it },
                            prompt = studyNotesPrompt.value,
                            onPromptChange = { studyNotesPrompt.value = it },
                            showScopeSelector = true,
                            selectedScope = studyNotesScope.value,
                            onScopeSelected = { studyNotesScope.value = it },
                            startPage = studyNotesStartPage.value,
                            onStartPageChange = { studyNotesStartPage.value = it },
                            endPage = studyNotesEndPage.value,
                            onEndPageChange = { studyNotesEndPage.value = it },
                            onSend = {
                                val range = if (studyNotesScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                    val start = studyNotesStartPage.value.toIntOrNull() ?: 1
                                    val end = studyNotesEndPage.value.toIntOrNull() ?: 1
                                    minOf(start, end).coerceIn(1, document.pageCount)..maxOf(start, end).coerceIn(1, document.pageCount)
                                } else null
                                onSendToAiApp(
                                    AiPromptType.STUDY_NOTES,
                                    studyNotesPrompt.value,
                                    studyNotesScope.value,
                                    range
                                )
                            }
                        )

                        TaskExpandableCard(
                            title = "Simplify difficult text",
                            taskKey = "simplify_text",
                            expandedTask = expandedTask.value,
                            onToggleExpand = { expandedTask.value = it },
                            prompt = simplifyPrompt.value,
                            onPromptChange = { simplifyPrompt.value = it },
                            showScopeSelector = true,
                            selectedScope = simplifyScope.value,
                            onScopeSelected = { simplifyScope.value = it },
                            startPage = simplifyStartPage.value,
                            onStartPageChange = { simplifyStartPage.value = it },
                            endPage = simplifyEndPage.value,
                            onEndPageChange = { simplifyEndPage.value = it },
                            onSend = {
                                val range = if (simplifyScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                    val start = simplifyStartPage.value.toIntOrNull() ?: 1
                                    val end = simplifyEndPage.value.toIntOrNull() ?: 1
                                    minOf(start, end).coerceIn(1, document.pageCount)..maxOf(start, end).coerceIn(1, document.pageCount)
                                } else null
                                onSendToAiApp(
                                    AiPromptType.SIMPLIFY,
                                    simplifyPrompt.value,
                                    simplifyScope.value,
                                    range
                                )
                            }
                        )

                        TaskExpandableCard(
                            title = "Create page-to-page quiz",
                            taskKey = "quiz",
                            expandedTask = expandedTask.value,
                            onToggleExpand = { expandedTask.value = it },
                            prompt = quizPrompt.value,
                            onPromptChange = { quizPrompt.value = it },
                            showScopeSelector = true,
                            selectedScope = quizScope.value,
                            onScopeSelected = { quizScope.value = it },
                            startPage = quizStartPage.value,
                            onStartPageChange = { quizStartPage.value = it },
                            endPage = quizEndPage.value,
                            onEndPageChange = { quizEndPage.value = it },
                            onSend = {
                                val range = if (quizScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                    val start = quizStartPage.value.toIntOrNull() ?: 1
                                    val end = quizEndPage.value.toIntOrNull() ?: 1
                                    minOf(start, end).coerceIn(1, document.pageCount)..maxOf(start, end).coerceIn(1, document.pageCount)
                                } else null
                                onSendToAiApp(
                                    AiPromptType.QUIZ,
                                    quizPrompt.value,
                                    quizScope.value,
                                    range
                                )
                            }
                        )

                        TaskExpandableCard(
                            title = "Create flashcards",
                            taskKey = "flashcards",
                            expandedTask = expandedTask.value,
                            onToggleExpand = { expandedTask.value = it },
                            prompt = flashcardsPrompt.value,
                            onPromptChange = { flashcardsPrompt.value = it },
                            showScopeSelector = true,
                            selectedScope = flashcardsScope.value,
                            onScopeSelected = { flashcardsScope.value = it },
                            startPage = flashcardsStartPage.value,
                            onStartPageChange = { flashcardsStartPage.value = it },
                            endPage = flashcardsEndPage.value,
                            onEndPageChange = { flashcardsEndPage.value = it },
                            onSend = {
                                val range = if (flashcardsScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                    val start = flashcardsStartPage.value.toIntOrNull() ?: 1
                                    val end = flashcardsEndPage.value.toIntOrNull() ?: 1
                                    minOf(start, end).coerceIn(1, document.pageCount)..maxOf(start, end).coerceIn(1, document.pageCount)
                                } else null
                                onSendToAiApp(
                                    AiPromptType.FLASHCARDS,
                                    flashcardsPrompt.value,
                                    flashcardsScope.value,
                                    range
                                )
                            }
                        )

                        TextButton(
                            onClick = onOpenOfflineStudyTools,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Use offline study tools instead")
                        }
                    }

                    "Templates" -> {
                        OutlinedTextField(
                            value = templateTitle,
                            onValueChange = { templateTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Template title") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customPrompt,
                            onValueChange = { customPrompt = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            label = { Text("Custom instruction") },
                            placeholder = { Text("Example: Explain this like I am preparing for an exam, then give likely questions.") }
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    onSendToAiApp(
                                        AiPromptType.CUSTOM,
                                        customPrompt,
                                        AiPromptScope.WHOLE_DOCUMENT,
                                        null
                                    )
                                },
                                enabled = customPrompt.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Send") }
                            OutlinedButton(
                                onClick = { onSaveTemplate(templateTitle, customPrompt) },
                                enabled = customPrompt.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Save") }
                        }
                        HorizontalDivider()
                        if (templates.isEmpty()) {
                            Text(
                                "Saved custom prompts will appear here.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            templates.forEach { template ->
                                Card(
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(template.title, fontWeight = FontWeight.Black)
                                        Text(
                                            template.instruction,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            TextButton(onClick = {
                                                onSendToAiApp(
                                                    AiPromptType.CUSTOM,
                                                    template.instruction,
                                                    AiPromptScope.WHOLE_DOCUMENT,
                                                    null
                                                )
                                            }) { Text("Use") }
                                            TextButton(onClick = {
                                                onCopyText(
                                                    "Veritas AI template",
                                                    template.instruction
                                                )
                                            }) { Text("Copy") }
                                            TextButton(onClick = { onDeleteTemplate(template.id) }) {
                                                Text(
                                                    "Delete"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "History" -> {
                        if (history.isEmpty()) {
                            Text(
                                "Prompts you send to AI apps will appear here.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = onClearHistory) { Text("Clear history") }
                            }
                            history.take(12).forEach { item ->
                                Card(
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "${item.promptType} • ${item.scope}",
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            item.documentTitle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            item.promptPreview,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            formatUpdated(item.createdAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        TextButton(onClick = {
                                            onCopyText(
                                                "Veritas AI prompt preview",
                                                item.promptPreview
                                            )
                                        }) { Text("Copy preview") }
                                    }
                                }
                            }
                        }
                    }

                    "Result → note" -> {
                        Text(
                            "After the AI app replies, copy its answer, return here, paste it below, and save it as a note on the current sentence.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = aiResultDraft,
                            onValueChange = { aiResultDraft = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            label = { Text("Paste AI result") },
                            placeholder = { Text("Paste summary, explanation, quiz answer, or study notes here…") }
                        )
                        Button(
                            onClick = {
                                onSaveAiResultAsNote(aiResultDraft)
                                aiResultDraft = ""
                            },
                            enabled = aiResultDraft.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save result to current-sentence note") }
                    }
                }

                Text(
                    "For long documents, use the sentence-by-sentence button repeatedly as you move through the reader. Whole-document prompts may be shortened by Android share limits.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
internal fun StudyToolsDialog(
    studyPack: StudyPack,
    onDismiss: () -> Unit
) {
    var tab by remember { mutableStateOf("Summary") }
    val tabs = listOf("Summary", "Points", "Terms", "Cards", "Quiz", "Sentence")
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Study tools") },
        text = {
            Column(
                modifier = Modifier
                    .height(500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Offline study help generated from this document. It is meant for revision, not as a final authority.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.take(3).forEach { item ->
                        if (tab == item) Button(onClick = { tab = item }) { Text(item) }
                        else OutlinedButton(onClick = { tab = item }) { Text(item) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.drop(3).forEach { item ->
                        if (tab == item) Button(onClick = { tab = item }) { Text(item) }
                        else OutlinedButton(onClick = { tab = item }) { Text(item) }
                    }
                }

                when (tab) {
                    "Summary" -> StudyListBlock(
                        title = "Document summary",
                        emptyText = "No summary could be generated.",
                        items = studyPack.summary
                    )

                    "Points" -> StudyListBlock(
                        title = "Key points",
                        emptyText = "No key points could be generated.",
                        items = studyPack.keyPoints
                    )

                    "Terms" -> StudyListBlock(
                        title = "Key terms",
                        emptyText = "No key terms could be detected.",
                        items = studyPack.keyTerms
                    )

                    "Cards" -> {
                        Text(
                            "Flashcards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (studyPack.flashcards.isEmpty()) {
                            Text(
                                "No flashcards could be generated.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            studyPack.flashcards.forEachIndexed { index, card ->
                                StudyCard(title = "Card ${index + 1}") {
                                    Text(card.front, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        card.back,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    "Quiz" -> {
                        Text(
                            "Quick quiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (studyPack.quiz.isEmpty()) {
                            Text(
                                "No quiz could be generated.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            studyPack.quiz.forEachIndexed { index, question ->
                                StudyCard(title = "Question ${index + 1}") {
                                    Text(question.question, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    question.options.forEach { option ->
                                        Text("• $option")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Answer: ${question.answer}", fontWeight = FontWeight.Bold)
                                    Text(
                                        question.explanation,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    "Sentence" -> StudyListBlock(
                        title = "Current sentence explained",
                        emptyText = "No sentence explanation could be generated.",
                        items = studyPack.currentSectionExplanation
                    )
                }
            }
        }
    )
}

@Composable
internal fun StudyListBlock(
    title: String,
    emptyText: String,
    items: List<String>
) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    if (items.isEmpty()) {
        Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        items.forEachIndexed { index, item ->
            StudyCard(title = "${index + 1}") {
                Text(item, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
internal fun StudyCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
            content()
        }
    }
}


data class VoicePreset(
    val name: String,
    val rate: Float,
    val pitch: Float,
    val summary: String
)

fun voicePresets(): List<VoicePreset> = listOf(
    VoicePreset("Balanced", 1.0f, 1.0f, "Everyday reading with neutral timing."),
    VoicePreset("Study focus", 0.88f, 0.98f, "Slower pacing for dense material and note taking."),
    VoicePreset("Quick scan", 1.35f, 1.02f, "Fast skim for review and familiar documents."),
    VoicePreset("Story warm", 0.96f, 0.92f, "Softer narration for fiction and long listening."),
    VoicePreset("Clear lecture", 1.06f, 1.06f, "Brighter delivery for technical or academic text."),
    VoicePreset("Calm night", 0.82f, 0.90f, "Low, relaxed reading for quiet listening.")
)


internal fun textEditorDownloadName(
    document: ReaderDocument,
    target: VeritasTextEditTarget
): String {
    val scope = when (target) {
        is VeritasTextEditTarget.Part -> target.label
        is VeritasTextEditTarget.SentenceRange -> target.label
    }
    val safeTitle = document.title
        .replace(Regex("[^A-Za-z0-9._ -]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(48)
        .ifBlank { "veritas_text" }
    val safeScope = scope
        .replace(Regex("[^A-Za-z0-9._ -]"), " ")
        .replace(Regex("\\s+"), "_")
        .trim('_')
        .ifBlank { "edited" }
    return "${safeTitle}_$safeScope.txt"
}

internal fun countSearchOccurrences(source: String, query: String): Int {
    val needle = query.trim()
    if (source.isBlank() || needle.isBlank()) return 0
    var count = 0
    var cursor = 0
    while (cursor <= source.length - needle.length) {
        val found = source.indexOf(needle, startIndex = cursor, ignoreCase = true)
        if (found < 0) break
        count++
        cursor = found + needle.length.coerceAtLeast(1)
        if (count >= 500) break
    }
    return count
}

internal fun openOriginalDocument(
    context: Context,
    repository: DocumentRepository,
    document: SavedDocument
) {
    val original = repository.originalFile(document)
    if (original == null) {
        Toast.makeText(
            context,
            "No stored original is available for this reading.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }
    val uri = runCatching {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", original)
    }.getOrNull()
    if (uri == null) {
        Toast.makeText(context, "Could not prepare the original file.", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, document.originalMimeType.ifBlank { "application/octet-stream" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Open original document"))
    }.onFailure {
        Toast.makeText(context, "No app can open this original document.", Toast.LENGTH_SHORT)
            .show()
    }
}

internal fun openAllFilesAccessSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        Toast.makeText(
            context,
            "All Files access is already available on this Android version.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }
    val appIntent = Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        "package:${context.packageName}".toUri()
    )
    val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    runCatching {
        context.startActivity(appIntent)
    }.onFailure {
        runCatching { context.startActivity(fallback) }
            .onFailure {
                Toast.makeText(
                    context,
                    "Could not open All Files access settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}

internal fun veritasBackupFileName(prefix: String): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "${prefix}_$timestamp.json"
}

internal fun veritasBackupMimeTypes(): Array<String> = arrayOf(
    "application/json",
    "text/json",
    "text/plain",
    "application/octet-stream"
)

@Composable
internal fun TextEditorDialog(
    document: ReaderDocument,
    currentIndex: Int,
    text: String,
    target: VeritasTextEditTarget,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDownloadToPhone: () -> Unit,
    onDismiss: () -> Unit
) {
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var editorValue by remember(target) {
        mutableStateOf(
            TextFieldValue(
                text,
                selection = TextRange(text.length)
            )
        )
    }
    var undoStack by remember(target) { mutableStateOf<List<TextFieldValue>>(emptyList()) }
    var redoStack by remember(target) { mutableStateOf<List<TextFieldValue>>(emptyList()) }
    val scopeLabel = when (target) {
        is VeritasTextEditTarget.SentenceRange -> target.label
        is VeritasTextEditTarget.Part -> target.label
    }
    val searchMatches = remember(editorValue.text, searchQuery) {
        countSearchOccurrences(
            editorValue.text,
            searchQuery
        )
    }

    LaunchedEffect(text, target) {
        if (text != editorValue.text) {
            val safeSelection = TextRange(text.length)
            editorValue = TextFieldValue(text, selection = safeSelection)
        }
    }

    fun commitValue(next: TextFieldValue) {
        if (next.text == editorValue.text && next.selection == editorValue.selection) {
            editorValue = next
            return
        }
        undoStack = (undoStack + editorValue).takeLast(80)
        redoStack = emptyList()
        editorValue = next
        onTextChange(next.text)
    }

    fun replaceSelection(prefix: String, suffix: String = prefix, placeholder: String = "") {
        val value = editorValue
        val start = value.selection.min.coerceIn(0, value.text.length)
        val end = value.selection.max.coerceIn(0, value.text.length)
        val selected = value.text.substring(start, end).ifBlank { placeholder }
        val replacement = "$prefix$selected$suffix"
        val nextText = value.text.replaceRange(start, end, replacement)
        val cursorStart = start + prefix.length
        val cursorEnd = cursorStart + selected.length
        commitValue(TextFieldValue(nextText, selection = TextRange(cursorStart, cursorEnd)))
    }

    fun findNextSearchMatch() {
        val needle = searchQuery.trim()
        if (needle.isBlank()) return
        val start = editorValue.selection.max.coerceIn(0, editorValue.text.length)
        val first = editorValue.text.indexOf(needle, startIndex = start, ignoreCase = true)
        val match = if (first >= 0) first else editorValue.text.indexOf(needle, ignoreCase = true)
        if (match >= 0) {
            editorValue = editorValue.copy(selection = TextRange(match, match + needle.length))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "←",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Edit text",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            document.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showSearch = !showSearch }) { Text("⌕") }
                    TextButton(
                        onClick = onDownloadToPhone,
                        enabled = editorValue.text.isNotBlank()
                    ) { Text("⇩") }
                    Button(
                        onClick = onSave,
                        enabled = editorValue.text.isNotBlank()
                    ) { Text("Save") }
                }
                if (showSearch) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it.take(120) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Search in edited text") }
                        )
                        Text("$searchMatches", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(
                            onClick = ::findNextSearchMatch,
                            enabled = searchMatches > 0
                        ) { Text("Next") }
                    }
                }
                Text(
                    "Editing $scopeLabel • current sentence ${currentIndex + 1}/${
                        document.chunks.size.coerceAtLeast(
                            1
                        )
                    }",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                BasicTextField(
                    value = editorValue,
                    onValueChange = { next ->
                        commitValue(next)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = (MaterialTheme.typography.bodyLarge.fontSize.value + 8).sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.inverseSurface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = { showSearch = !showSearch }) {
                        Text(
                            "⌕",
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                    TextButton(onClick = {
                        replaceSelection(
                            "<p>\n",
                            "\n</p>",
                            "Paragraph"
                        )
                    }) { Text("<p>", color = MaterialTheme.colorScheme.inverseOnSurface) }
                    TextButton(
                        onClick = { replaceSelection("_") },
                        enabled = editorValue.text.isNotBlank()
                    ) {
                        Text(
                            "I",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { replaceSelection("**") },
                        enabled = editorValue.text.isNotBlank()
                    ) {
                        Text(
                            "B",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Black
                        )
                    }
                    TextButton(
                        onClick = { replaceSelection("<u>", "</u>") },
                        enabled = editorValue.text.isNotBlank()
                    ) {
                        Text(
                            "U",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = {
                            val previous = undoStack.lastOrNull() ?: return@TextButton
                            undoStack = undoStack.dropLast(1)
                            redoStack = (redoStack + editorValue).takeLast(80)
                            editorValue = previous
                            onTextChange(previous.text)
                        },
                        enabled = undoStack.isNotEmpty()
                    ) { Text("↶", color = MaterialTheme.colorScheme.inverseOnSurface) }
                    TextButton(
                        onClick = {
                            val next = redoStack.lastOrNull() ?: return@TextButton
                            redoStack = redoStack.dropLast(1)
                            undoStack = (undoStack + editorValue).takeLast(80)
                            editorValue = next
                            onTextChange(next.text)
                        },
                        enabled = redoStack.isNotEmpty()
                    ) { Text("↷", color = MaterialTheme.colorScheme.inverseOnSurface) }
                    TextButton(
                        onClick = { replaceSelection("`") },
                        enabled = editorValue.text.isNotBlank()
                    ) {
                        Text(
                            "</>",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = onDownloadToPhone,
                        enabled = editorValue.text.isNotBlank()
                    ) { Text("▣", color = MaterialTheme.colorScheme.inverseOnSurface) }
                }
            }
        }
    }
}

@Composable
internal fun TutorialDialog(
    initialName: String,
    onDismiss: (String) -> Unit,
    onImport: (String) -> Unit,
    onVoice: (String) -> Unit,
    onThemes: (String) -> Unit
) {
    val context = LocalContext.current
    var nameDraft by rememberSaveable { mutableStateOf(initialName) }
    var ttsReady by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val steps = remember {
        listOf(
            TutorialFrame(
                "1",
                "Welcome to Veritas Reader",
                "Transform your research, documents, and reading materials into focused audio experiences.",
                "📖",
                null
            ),
            TutorialFrame(
                "2",
                "What should we call you?",
                "Your name personalizes the Home tab and reading experience.",
                "👤",
                null
            ),
            TutorialFrame(
                "3",
                "Add a reading",
                "Import PDFs, EPUBs, Word documents, text files, images, or paste texts and links.",
                "➕",
                { onImport(nameDraft) }),
            TutorialFrame(
                "4",
                "Read your way",
                "Switch between Extracted Text, Listen Mode, and the Original PDF/Image layouts.",
                "📄",
                null
            ),
            TutorialFrame(
                "5",
                "Listen & synthesis",
                "Pick voices, adjust speed/pitch, and control playback from the expandable panel or system notification.",
                "🎧",
                { onVoice(nameDraft) }),
            TutorialFrame(
                "6",
                "Mark & remember",
                "Bookmark sentences to highlight them, add study notes, translate text, search, and fix pronunciation.",
                "🔖",
                null
            ),
            TutorialFrame(
                "7",
                "Make it yours",
                "Choose from 10+ premium color themes, set up widget shortcuts, and export standard WAV audio files.",
                "🎨",
                { onThemes(nameDraft) }),
            TutorialFrame(
                "8",
                "Ready to read?",
                "Your calm reading environment is configured. Open the library and import your first document.",
                "🚀",
                null
            )
        )
    }
    var stepIndex by remember { mutableIntStateOf(0) }
    val pulse by animateFloatAsState(
        targetValue = if (stepIndex % 2 == 0) 1.08f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tutorialPulse"
    )
    DisposableEffect(context) {
        val engine = TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }
    LaunchedEffect(stepIndex, ttsReady, nameDraft) {
        if (ttsReady) {
            val greeting = nameDraft.trim().ifBlank { "reader" }
            val frame = steps[stepIndex]
            val spoken = if (stepIndex == 0) {
                "Welcome to Veritas Reader. Transform your research, documents, and reading materials into focused audio experiences."
            } else if (stepIndex == 1) {
                "What should we call you? This name will appear on your dashboard."
            } else if (stepIndex == steps.lastIndex) {
                "Ready to read, $greeting. Your setup is complete."
            } else {
                "${frame.title}. ${frame.body}"
            }
            tts?.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "veritas-onboarding-$stepIndex")
        }
    }
    Dialog(
        onDismissRequest = { onDismiss(nameDraft) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Veritas setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onDismiss(nameDraft) }) { Text("Skip") }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    if (stepIndex == 0) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(112.dp)
                                    .graphicsLayer(scaleX = pulse, scaleY = pulse)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.shapes.large
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                BrandMark(compact = true)
                            }
                        }
                        Text(
                            "Welcome to Veritas Reader",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Transform your research, documents, and reading materials into high-quality, focused audio experiences.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (stepIndex == 1) {
                        Text(
                            "What should we call you?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        OutlinedTextField(
                            value = nameDraft,
                            onValueChange = { nameDraft = it.take(48) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your preferred name") },
                            placeholder = { Text("Name for the Home tab welcome") },
                            singleLine = true,
                            shape = RoundedCornerShape(50)
                        )
                        Text(
                            "This is used to personalize your Home tab and reading experience.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (stepIndex >= 2) {
                        TutorialStage(
                            frame = steps[stepIndex],
                            progress = (stepIndex + 1).toFloat() / steps.size.toFloat(),
                            pulse = pulse
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .width(if (index == stepIndex) 28.dp else 8.dp)
                                    .height(8.dp)
                                    .background(
                                        if (index == stepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    )
                                    .clickable { stepIndex = index }
                            )
                        }
                    }
                    steps.drop(2).dropLast(1).forEachIndexed { offset, frame ->
                        val index = offset + 2
                        TutorialStep(
                            number = frame.number,
                            title = frame.title,
                            body = frame.body,
                            action = frame.action,
                            selected = index == stepIndex,
                            onSelect = { stepIndex = index }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { stepIndex = (stepIndex - 1).coerceAtLeast(0) },
                        enabled = stepIndex > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) { Text("Back") }
                    Button(
                        onClick = {
                            if (stepIndex >= steps.lastIndex) onDismiss(nameDraft) else stepIndex += 1
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(if (stepIndex >= steps.lastIndex) "Go to Home" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
internal fun TutorialStage(frame: TutorialFrame, progress: Float, pulse: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .graphicsLayer(scaleX = pulse, scaleY = pulse)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        frame.icon,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                        .fillMaxWidth(0.62f)
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            Text(
                frame.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
            Text(frame.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (frame.action != null) {
                Button(onClick = frame.action, shape = RoundedCornerShape(50)) { Text("Open") }
            }
        }
    }
}

@Composable
internal fun TutorialStep(
    number: String,
    title: String,
    body: String,
    action: (() -> Unit)?,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tutorialStepBounce"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(number, fontWeight = FontWeight.Black) }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, fontWeight = FontWeight.Black)
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (action != null) {
                Button(onClick = action, shape = RoundedCornerShape(50)) { Text("Open") }
            }
        }
    }
}

internal data class TutorialFrame(
    val number: String,
    val title: String,
    val body: String,
    val icon: String,
    val action: (() -> Unit)?
)

@Composable
internal fun FloatingRecordOverlay(
    inProgress: Boolean,
    fileReady: Boolean,
    awaitingDecision: Boolean,
    elapsedSeconds: Long,
    onStopRecording: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val elapsed = formatRecordElapsed(elapsedSeconds)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(22.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
            shape = if (awaitingDecision) MaterialTheme.shapes.large else MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            if (awaitingDecision) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecordPillDot(inProgress = inProgress)
                    if (fileReady) {
                        TextButton(onClick = onSave) { Text("Save") }
                        TextButton(onClick = onDiscard) { Text("Discard") }
                    } else {
                        Text(
                            if (inProgress) "Finishing…" else "No audio yet",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = onDiscard) { Text("Discard") }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .clickable { onStopRecording() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecordPillDot(
                        inProgress = inProgress,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        elapsed,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
internal fun RecordPillDot(
    inProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(
                if (inProgress) Color(0xFFE53935) else MaterialTheme.colorScheme.primary,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (inProgress) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(Color.White, CircleShape)
            )
        } else {
            Text(
                "✓",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

internal fun formatRecordElapsed(seconds: Long): String {
    val safeSeconds = seconds.coerceAtLeast(0L)
    val minutes = safeSeconds / 60L
    val remainingSeconds = safeSeconds % 60L
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Composable
internal fun AppHealthDialog(
    documentCount: Int,
    queueCount: Int,
    themePackName: String,
    themeName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Close") } },
        title = { Text("App health") },
        text = {
            Column(
                modifier = Modifier
                    .height(560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Veritas Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Version: 1.0.1",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Library: $documentCount reading${if (documentCount == 1) "" else "s"} • $queueCount queued",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Theme: $themePackName / $themeName",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ReadinessBlock(
                    title = "Permissions review",
                    items = listOf(
                        "Notifications are only used for media playback controls.",
                        "Foreground service is used for reading aloud while the app is outside the screen.",
                        "Document access uses Android file pickers and shared files; no broad storage permission is required.",
                        "Internet is not required for core reading. AI handoff uses installed apps through Android sharing."
                    )
                )

                ReadinessBlock(
                    title = "Before sharing an APK",
                    items = listOf(
                        "Run the final QA checklist on a physical phone.",
                        "Test import, playback, background controls, OCR, export, sync pack merge, notes export, and AI handoff.",
                        "Make a backup copy of this project folder once it passes.",
                        "Use a debug APK for private testing only. Use a signed release APK/AAB for wider distribution."
                    )
                )

                ReadinessBlock(
                    title = "Current limits",
                    items = listOf(
                        "Google Drive login is planned for a later OAuth pass.",
                        "Long scanned PDFs and WAV exports may be slow on low-memory phones.",
                        "Canvas/source view requires newly imported originals to be preserved.",
                        "AI features depend on installed AI apps or the small offline helper."
                    )
                )
            }
        }
    )
}

@Composable
internal fun ReadinessBlock(title: String, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
            items.forEach { item ->
                Text(
                    "• $item",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


object VeritasPackStyle {
    @Composable
    fun currentPackId(): String =
        VeritasThemePackCatalog.normalizePackId(VeritasThemeState.themePackId)

    @Composable
    fun cardShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(34.dp)
        "liquid_glass" -> RoundedCornerShape(42.dp)
        "one_ui" -> RoundedCornerShape(28.dp)
        else -> RoundedCornerShape(18.dp)
    }

    @Composable
    fun compactShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(28.dp)
        "liquid_glass" -> RoundedCornerShape(34.dp)
        "one_ui" -> RoundedCornerShape(18.dp)
        else -> RoundedCornerShape(12.dp)
    }

    @Composable
    fun chipShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(50)
        "liquid_glass" -> RoundedCornerShape(36.dp)
        "one_ui" -> RoundedCornerShape(16.dp)
        else -> RoundedCornerShape(10.dp)
    }

    @Composable
    fun surfaceAlpha(): Float = when (currentPackId()) {
        "liquid_glass" -> 0.42f
        "one_ui" -> 0.94f
        "material_you" -> 0.88f
        else -> 0.78f
    }

    // Liquid Glass gets a real glass edge: a catch-light that's brightest along the
    // top rim and fades to a faint primary tint — the cue that sells "glossy pane".
    // Other packs keep the standard hairline outline.
    @Composable
    fun cardBorder(colorScheme: ColorScheme): BorderStroke = when (currentPackId()) {
        "liquid_glass" -> BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.07f),
                    colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        )
        else -> BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f))
    }

    @Composable
    fun backgroundBrush(colorScheme: ColorScheme): Brush = when (currentPackId()) {
        "liquid_glass" -> Brush.verticalGradient(
            listOf(
                colorScheme.primaryContainer.copy(alpha = 0.34f),
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.86f),
                colorScheme.primaryContainer.copy(alpha = 0.22f),
                colorScheme.background
            )
        )

        "one_ui" -> Brush.verticalGradient(
            listOf(
                colorScheme.secondaryContainer.copy(alpha = 0.20f),
                colorScheme.background,
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.72f)
            )
        )

        "material_you" -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.76f),
                colorScheme.primaryContainer.copy(alpha = 0.22f),
                colorScheme.tertiaryContainer.copy(alpha = 0.16f),
                colorScheme.background
            )
        )

        else -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.primaryContainer.copy(alpha = 0.08f),
                colorScheme.background,
                colorScheme.secondaryContainer.copy(alpha = 0.10f)
            )
        )
    }

    @Composable
    fun label(): String = VeritasThemePackCatalog.displayName(currentPackId())
}


fun packPreviewSymbols(packId: String): String =
    when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "material_you" -> "rounded • adaptive • soft"
        "liquid_glass" -> "translucent • floating • glossy"
        "one_ui" -> "large • reachable • calm"
        else -> "media • compact • bold"
    }


// Swatches are the theme's REAL scheme values — [background, primary, key accent] —
// so what the picker shows is exactly what the app paints once the theme is applied.
fun themePreviewColors(themeId: String): List<Color> {
    return when (VeritasThemeCatalog.normalizeThemeId(themeId)) {
        "light" -> listOf(Color(0xFFF4F6FA), Color(0xFF7C6FFF), Color(0xFF1A1A2E))
        "neon" -> listOf(Color(0xFF000000), Color(0xFF00FFFF), Color(0xFF39FF14))
        "solarized_dark" -> listOf(Color(0xFF002B36), Color(0xFF2AA198), Color(0xFFB58900))
        "tomorrow_night_blue" -> listOf(Color(0xFF002451), Color(0xFFBBDAFF), Color(0xFFFFC58F))
        "dark_high_contrast" -> listOf(Color.Black, Color.White, Color(0xFFFFD400))
        "white_high_contrast" -> listOf(Color.White, Color.Black, Color(0xFF004B65))
        "bw_gradient_light" -> listOf(Color(0xFFFAFAFA), Color(0xFF111111), Color(0xFF707070))
        "bw_gradient_dark" -> listOf(Color(0xFF050505), Color(0xFFF2F2F2), Color(0xFF9A9A9A))
        "blue_high_contrast" -> listOf(Color(0xFF001B3A), Color(0xFFBDE9FF), Color(0xFFFFF176))
        "one_dark_pro" -> listOf(Color(0xFF21252B), Color(0xFF61AFEF), Color(0xFF98C379))
        "github_dark" -> listOf(Color(0xFF0D1117), Color(0xFF58A6FF), Color(0xFF3FB950))
        "github_light" -> listOf(Color(0xFFFFFFFF), Color(0xFF0969DA), Color(0xFF1A7F37))
        "dracula" -> listOf(Color(0xFF282A36), Color(0xFFBD93F9), Color(0xFFFF79C6))
        "material_you" -> listOf(Color(0xFFFFFBFE), Color(0xFF6750A4), Color(0xFF7D5260))
        "dark" -> listOf(Color(0xFF111827), Color(0xFFA79BFF), Color(0xFF9BD8E0))
        else -> listOf(Color(0xFF050505), Color(0xFF00D4E6), Color(0xFFD8B7FF))
    }
}


@Composable
fun AnnotationPill(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AnnotationPill(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}


internal fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
    val r = color1.red * (1f - ratio) + color2.red * ratio
    val g = color1.green * (1f - ratio) + color2.green * ratio
    val b = color1.blue * (1f - ratio) + color2.blue * ratio
    return Color(r, g, b, 1f)
}

internal fun adaptColorScheme(base: ColorScheme, palette: androidx.palette.graphics.Palette, isLight: Boolean): ColorScheme {
    val dominantColor = Color(palette.getDominantColor(base.primary.toArgb()))
    val vibrantColor = Color(palette.getVibrantColor(base.primary.toArgb()))
    val darkVibrant = Color(palette.getDarkVibrantColor(base.primary.toArgb()))
    val lightVibrant = Color(palette.getLightVibrantColor(base.primary.toArgb()))
    val muted = Color(palette.getMutedColor(base.secondary.toArgb()))

    return if (!isLight) {
        val bgTint = blendColors(dominantColor, base.background, 0.88f)
        val surfTint = blendColors(dominantColor, base.surface, 0.88f)
        val surfVarTint = blendColors(dominantColor, base.surfaceVariant, 0.88f)
        
        base.copy(
            primary = if (vibrantColor != base.primary) vibrantColor else lightVibrant,
            primaryContainer = blendColors(dominantColor, base.primaryContainer, 0.7f),
            secondary = if (muted != base.secondary) muted else dominantColor,
            secondaryContainer = blendColors(dominantColor, base.secondaryContainer, 0.8f),
            background = bgTint,
            surface = surfTint,
            surfaceVariant = surfVarTint
        )
    } else {
        val bgTint = blendColors(dominantColor, base.background, 0.93f)
        val surfTint = blendColors(dominantColor, base.surface, 0.95f)
        val surfVarTint = blendColors(dominantColor, base.surfaceVariant, 0.93f)
        
        base.copy(
            primary = if (vibrantColor != base.primary) vibrantColor else dominantColor,
            primaryContainer = blendColors(dominantColor, base.primaryContainer, 0.85f),
            secondary = if (muted != base.secondary) muted else dominantColor,
            secondaryContainer = blendColors(dominantColor, base.secondaryContainer, 0.9f),
            background = bgTint,
            surface = surfTint,
            surfaceVariant = surfVarTint
        )
    }
}

@Composable
internal fun VeritasTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val selectedTheme = VeritasThemeCatalog.normalizeThemeId(VeritasThemeState.themeId)
    val selectedPack = VeritasThemePackCatalog.normalizePackId(VeritasThemeState.themePackId)
    
    val resolvedTheme = if (selectedTheme == "system") {
        if (isSystemInDarkTheme()) "default_dark_2026" else "light"
    } else {
        selectedTheme
    }

    val isLight = when (resolvedTheme) {
        "light", "white_high_contrast", "bw_gradient_light", "github_light" -> true
        else -> false
    }

    val activeDocId = VeritasThemeState.activeDocumentId
    val adaptiveCover = VeritasThemeState.adaptiveCover
    var coverPalette by remember(activeDocId) { mutableStateOf<androidx.palette.graphics.Palette?>(null) }
    
    LaunchedEffect(activeDocId, adaptiveCover) {
        if (adaptiveCover && activeDocId != null) {
            val palette = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val file = CoverExtractor.coverFile(context, activeDocId)
                if (file != null && file.exists()) {
                    runCatching {
                        val bmp = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                        if (bmp != null) androidx.palette.graphics.Palette.from(bmp).generate() else null
                    }.getOrNull()
                } else null
            }
            coverPalette = palette
        } else {
            coverPalette = null
        }
    }

    val baseColorScheme = veritasColorScheme(resolvedTheme, context)
    val palette = coverPalette
    
    val blendedColorScheme = if (adaptiveCover && palette != null) {
        adaptColorScheme(baseColorScheme, palette, isLight)
    } else {
        baseColorScheme
    }

    val colorScheme = veritasPackColorScheme(
        base = blendedColorScheme,
        packId = selectedPack
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.surface.toArgb()
                val isLight = when (resolvedTheme) {
                    "light", "white_high_contrast", "bw_gradient_light", "github_light" -> true
                    else -> false
                }
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        shapes = veritasPackShapes(selectedPack),
        content = content
    )
}

internal fun veritasPackShapes(packId: String): Shapes {
    return when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "material_you" -> Shapes(
            extraSmall = RoundedCornerShape(14.dp),
            small = RoundedCornerShape(20.dp),
            medium = RoundedCornerShape(28.dp),
            large = RoundedCornerShape(36.dp),
            extraLarge = RoundedCornerShape(44.dp)
        )

        "liquid_glass" -> Shapes(
            extraSmall = RoundedCornerShape(18.dp),
            small = RoundedCornerShape(26.dp),
            medium = RoundedCornerShape(34.dp),
            large = RoundedCornerShape(44.dp),
            extraLarge = RoundedCornerShape(52.dp)
        )

        "one_ui" -> Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(22.dp),
            large = RoundedCornerShape(30.dp),
            extraLarge = RoundedCornerShape(38.dp)
        )

        else -> Shapes(
            extraSmall = RoundedCornerShape(6.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(26.dp)
        )
    }
}

internal fun veritasPackColorScheme(base: ColorScheme, packId: String): ColorScheme {
    return when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "liquid_glass" -> base.copy(
            surface = base.surface.copy(alpha = 0.74f),
            surfaceVariant = base.surfaceVariant.copy(alpha = 0.58f),
            primaryContainer = base.primaryContainer.copy(alpha = 0.84f),
            secondaryContainer = base.secondaryContainer.copy(alpha = 0.72f),
            tertiaryContainer = base.tertiaryContainer.copy(alpha = 0.68f)
        )

        "one_ui" -> base.copy(
            surface = base.surface,
            surfaceVariant = base.secondaryContainer.copy(alpha = 0.94f),
            primaryContainer = base.primaryContainer.copy(alpha = 0.98f),
            tertiaryContainer = base.tertiaryContainer.copy(alpha = 0.90f)
        )

        "material_you" -> base.copy(
            surface = base.surface.copy(alpha = 0.96f),
            surfaceVariant = base.primaryContainer.copy(alpha = 0.46f),
            secondaryContainer = base.tertiaryContainer.copy(alpha = 0.90f)
        )

        else -> base
    }
}

@Composable
internal fun veritasColorScheme(themeId: String, context: Context): ColorScheme {
    return when (VeritasThemeCatalog.normalizeThemeId(themeId)) {
        "light" -> lightColorScheme(
            primary = Color(0xFF7C6FFF),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF0F3FF),
            onPrimaryContainer = Color(0xFF7C6FFF),
            secondary = Color(0xFF5B4FCF),
            secondaryContainer = Color(0xFFDCE2FF),
            onSecondaryContainer = Color(0xFF5B4FCF),
            tertiary = Color(0xFF1A1A2E),
            tertiaryContainer = Color(0xFFEAEAEE),
            onTertiaryContainer = Color(0xFF1A1A2E),
            background = Color(0xFFF4F6FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF4F6FA),
            onSurface = Color(0xFF1A1A2E),
            onSurfaceVariant = Color(0xFF545464),
            outline = Color(0xFFC8C8D0),
            outlineVariant = Color(0xFFEAEAEE),
            error = Color(0xFFBA1A1A),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF93000A)
        )

        "neon" -> darkColorScheme(
            primary = Color(0xFF00FFFF),          // Pure Neon Cyan
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF002A30),
            onPrimaryContainer = Color(0xFF00FFFF),
            secondary = Color(0xFF39FF14),        // Neon Lime Green
            secondaryContainer = Color(0xFF0C240A),
            onSecondaryContainer = Color(0xFF39FF14),
            tertiary = Color(0xFFFF007F),         // Neon Magenta
            tertiaryContainer = Color(0xFF3A001C),
            background = Color(0xFF000000),       // Pure Black
            surface = Color(0xFF0B0E14),          // Very Dark Cyan-Slate
            surfaceVariant = Color(0xFF151B24),
            onSurface = Color(0xFFFFFFFF),        // High Contrast White
            onSurfaceVariant = Color(0xFF8CD3EC)
        )

        "solarized_dark" -> darkColorScheme(
            primary = Color(0xFF2AA198),          // Teal
            onPrimary = Color(0xFF002B36),
            primaryContainer = Color(0xFF073642),
            onPrimaryContainer = Color(0xFF93A1A1),
            secondary = Color(0xFF268BD2),        // Blue
            secondaryContainer = Color(0xFF002B36),
            onSecondaryContainer = Color(0xFF2AA198),
            tertiary = Color(0xFFB58900),         // Yellow/Gold
            tertiaryContainer = Color(0xFF073642),
            background = Color(0xFF002B36),
            surface = Color(0xFF073642),
            surfaceVariant = Color(0xFF0A3F4E),
            onSurface = Color(0xFFEEE8D5),        // High Contrast Solarized Base3
            onSurfaceVariant = Color(0xFF93A1A1)  // Solarized Base1
        )

        // Canonical Tomorrow Night Blue palette (the previous accents were actually
        // Material Palenight's): blue #BBDAFF, purple #EBBBFF, orange #FFC58F on #002451.
        "tomorrow_night_blue" -> darkColorScheme(
            primary = Color(0xFFBBDAFF),
            onPrimary = Color(0xFF002451),
            primaryContainer = Color(0xFF00346B),
            onPrimaryContainer = Color(0xFFEEFFFF),
            secondary = Color(0xFFEBBBFF),
            secondaryContainer = Color(0xFF002451),
            onSecondaryContainer = Color(0xFFEBBBFF),
            tertiary = Color(0xFFFFC58F),
            tertiaryContainer = Color(0xFF00346B),
            background = Color(0xFF002451),
            surface = Color(0xFF002F6C),
            surfaceVariant = Color(0xFF003C7A),
            onSurface = Color(0xFFFFFFFF),
            onSurfaceVariant = Color(0xFFB2CCD6)
        )

        "dark_high_contrast" -> darkColorScheme(
            primary = Color(0xFFFFFFFF),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFFE2E2E2),
            onPrimaryContainer = Color.Black,
            secondary = Color(0xFFFFD400),
            secondaryContainer = Color(0xFF3A3000),
            onSecondaryContainer = Color(0xFFFFFFD1),
            tertiary = Color(0xFF00E5FF),
            tertiaryContainer = Color(0xFF003D44),
            background = Color(0xFF000000),
            surface = Color(0xFF0A0A0A),
            surfaceVariant = Color(0xFF1D1D1D),
            onSurface = Color(0xFFFFFFFF),
            onSurfaceVariant = Color(0xFFE0E0E0)
        )

        "white_high_contrast" -> lightColorScheme(
            primary = Color(0xFF000000),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDADADA),
            onPrimaryContainer = Color.Black,
            secondary = Color(0xFF004B65),
            secondaryContainer = Color(0xFFC8EFFF),
            onSecondaryContainer = Color(0xFF001E2B),
            tertiary = Color(0xFF6B3A00),
            tertiaryContainer = Color(0xFFFFDDB5),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFEFEF),
            onSurface = Color(0xFF000000),
            onSurfaceVariant = Color(0xFF202020)
        )

        "bw_gradient_light" -> lightColorScheme(
            primary = Color(0xFF111111),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE7E7E7),
            onPrimaryContainer = Color(0xFF111111),
            secondary = Color(0xFF4A4A4A),
            secondaryContainer = Color(0xFFF0F0F0),
            onSecondaryContainer = Color(0xFF161616),
            tertiary = Color(0xFF707070),
            tertiaryContainer = Color(0xFFE0E0E0),
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEDEDED),
            onSurface = Color(0xFF101010),
            onSurfaceVariant = Color(0xFF3F3F3F)
        )

        "bw_gradient_dark" -> darkColorScheme(
            primary = Color(0xFFF2F2F2),
            onPrimary = Color(0xFF090909),
            primaryContainer = Color(0xFF2D2D2D),
            onPrimaryContainer = Color(0xFFF6F6F6),
            secondary = Color(0xFFC7C7C7),
            secondaryContainer = Color(0xFF1E1E1E),
            onSecondaryContainer = Color(0xFFEDEDED),
            tertiary = Color(0xFF9A9A9A),
            tertiaryContainer = Color(0xFF252525),
            background = Color(0xFF050505),
            surface = Color(0xFF111111),
            surfaceVariant = Color(0xFF242424),
            onSurface = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFFC9C9C9)
        )

        "blue_high_contrast" -> darkColorScheme(
            primary = Color(0xFFBDE9FF),
            onPrimary = Color(0xFF001E30),
            primaryContainer = Color(0xFF00517A),
            onPrimaryContainer = Color(0xFFE9F7FF),
            secondary = Color(0xFFFFF176),
            secondaryContainer = Color(0xFF4A4500),
            onSecondaryContainer = Color(0xFFFFFBD0),
            tertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF263B67),
            background = Color(0xFF001B3A),
            surface = Color(0xFF002857),
            surfaceVariant = Color(0xFF003B7A),
            onSurface = Color(0xFFFFFFFF),
            onSurfaceVariant = Color(0xFFD9E9FF)
        )

        "one_dark_pro" -> darkColorScheme(
            primary = Color(0xFF61AFEF),
            onPrimary = Color(0xFF21252B),
            primaryContainer = Color(0xFF2E3440),
            onPrimaryContainer = Color(0xFF61AFEF),
            secondary = Color(0xFF98C379),
            secondaryContainer = Color(0xFF21252B),
            onSecondaryContainer = Color(0xFF98C379),
            tertiary = Color(0xFFC678DD),
            tertiaryContainer = Color(0xFF282C34),
            background = Color(0xFF21252B),
            surface = Color(0xFF282C34),
            surfaceVariant = Color(0xFF353B45),
            onSurface = Color(0xFFE5E9F0),        // Lighter high contrast text
            onSurfaceVariant = Color(0xFFABB2BF)  // Secondary text
        )

        "github_dark" -> darkColorScheme(
            primary = Color(0xFF58A6FF),
            onPrimary = Color(0xFF0D1117),
            primaryContainer = Color(0xFF1F6FEB),
            onPrimaryContainer = Color(0xFFF0F6FC),
            secondary = Color(0xFF3FB950),
            secondaryContainer = Color(0xFF0D1117),
            onSecondaryContainer = Color(0xFF3FB950),
            tertiary = Color(0xFFFFA657),
            tertiaryContainer = Color(0xFF161B22),
            background = Color(0xFF0D1117),
            surface = Color(0xFF161B22),
            surfaceVariant = Color(0xFF21262D),
            onSurface = Color(0xFFF0F6FC),        // GitHub fg.default
            onSurfaceVariant = Color(0xFFC9D1D9)  // GitHub fg.muted
        )

        "github_light" -> lightColorScheme(
            primary = Color(0xFF0969DA),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDDF4FF),
            onPrimaryContainer = Color(0xFF0969DA),
            secondary = Color(0xFF1A7F37),
            secondaryContainer = Color(0xFFFFFFFF),
            onSecondaryContainer = Color(0xFF1A7F37),
            tertiary = Color(0xFF9A6700),
            tertiaryContainer = Color(0xFFF6F8FA),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF6F8FA),
            surfaceVariant = Color(0xFFEAEFF4),
            onSurface = Color(0xFF24292F),
            onSurfaceVariant = Color(0xFF57606A)
        )

        "dracula" -> darkColorScheme(
            primary = Color(0xFFBD93F9),          // Purple
            onPrimary = Color(0xFF282A36),
            primaryContainer = Color(0xFF44475A),
            onPrimaryContainer = Color(0xFFF8F8F2),
            secondary = Color(0xFF50FA7B),        // Green
            secondaryContainer = Color(0xFF282A36),
            onSecondaryContainer = Color(0xFF50FA7B),
            tertiary = Color(0xFFFF79C6),         // Pink
            tertiaryContainer = Color(0xFF44475A),
            background = Color(0xFF282A36),
            surface = Color(0xFF1E1F29),
            surfaceVariant = Color(0xFF44475A),
            onSurface = Color(0xFFF8F8F2),
            // Dracula's comment blue (#6272A4) is authentic but ~2.9:1 on this bg;
            // brightened one step (same hue) so captions stay readable.
            onSurfaceVariant = Color(0xFF8E9AC9)
        )

        "material_you" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                    context
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF6750A4),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFEADDFF),
                    onPrimaryContainer = Color(0xFF21005D),
                    secondary = Color(0xFF625B71),
                    secondaryContainer = Color(0xFFE8DEF8),
                    onSecondaryContainer = Color(0xFF1D192B),
                    tertiary = Color(0xFF7D5260),
                    tertiaryContainer = Color(0xFFFFD8E4),
                    background = Color(0xFFFFFBFE),
                    surface = Color(0xFFFFFBFE),
                    surfaceVariant = Color(0xFFE7E0EC),
                    onSurface = Color(0xFF1C1B1F),
                    onSurfaceVariant = Color(0xFF49454F)
                )
            }
        }

        // Brand mirror of the Light theme: soft Veritas purple on slate navy, with the
        // old ice-cyan demoted to tertiary. Distinguishes Dark from the cyan-on-black
        // Default Dark 2026 and Neon themes.
        "dark" -> darkColorScheme(
            primary = Color(0xFFA79BFF),
            onPrimary = Color(0xFF221656),
            primaryContainer = Color(0xFF3B2E75),
            onPrimaryContainer = Color(0xFFE6DFFF),
            secondary = Color(0xFFC9D6DF),
            secondaryContainer = Color(0xFF29333B),
            onSecondaryContainer = Color(0xFFE6EEF3),
            tertiary = Color(0xFF9BD8E0),
            tertiaryContainer = Color(0xFF164B54),
            background = Color(0xFF111827),
            surface = Color(0xFF171F2B),
            surfaceVariant = Color(0xFF273140),
            onSurface = Color(0xFFE9EEF5),
            onSurfaceVariant = Color(0xFFC4CED9)
        )

        else -> darkColorScheme(
            primary = Color(0xFF00D4E6),
            onPrimary = Color(0xFF002124),
            primaryContainer = Color(0xFF003B42),
            onPrimaryContainer = Color(0xFFB8F4FA),
            secondary = Color(0xFFCAD3D7),
            secondaryContainer = Color(0xFF20272B),
            onSecondaryContainer = Color(0xFFE6EEF2),
            tertiary = Color(0xFFD8B7FF),
            tertiaryContainer = Color(0xFF332245),
            background = Color(0xFF050505),
            surface = Color(0xFF101010),
            surfaceVariant = Color(0xFF1C1C1E),
            onSurface = Color(0xFFEFF7FA),
            onSurfaceVariant = Color(0xFFC2CCD2)
        )
    }
}

@Composable
fun VeritasImportPreviewDialog(
    pendingImport: VeritasPendingImport,
    onConfirm: (String, PdfImportOptions, TextImportOptions, PptxImportOptions) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(pendingImport.name.substringBeforeLast(".")) }
    var pdfOptions by remember { mutableStateOf(pendingImport.pdfOptions) }
    var textOptions by remember { mutableStateOf(pendingImport.textOptions) }
    var pptxOptions by remember { mutableStateOf(pendingImport.pptxOptions) }

    var startPageDraft by remember { mutableStateOf(pdfOptions.startPage?.toString().orEmpty()) }
    var endPageDraft by remember { mutableStateOf(pdfOptions.endPage?.toString().orEmpty()) }

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

    val sizeKB = pendingImport.sizeBytes / 1024.0
    val sizeMB = sizeKB / 1024.0
    val sizeText = if (sizeMB >= 1.0) {
        String.format(Locale.US, "%.2f MB", sizeMB)
    } else {
        String.format(Locale.US, "%.1f KB", sizeKB)
    }

    val pageCount = pendingImport.pageCount
    val isPageRangeInvalid = pendingImport.isPdf && pageCount > 0 && run {
        val start = startPageDraft.toIntOrNull()
        val end = endPageDraft.toIntOrNull()
        start == null || end == null || start < 1 || end > pageCount || start > end
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Import settings") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "File size: $sizeText" + if (pendingImport.isPdf && pageCount > 0) " • Total pages: $pageCount" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Document title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (pendingImport.isPptx) {
                    item {
                        Text("Slides options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Speaker notes", fontWeight = FontWeight.SemiBold)
                                Text("Read presenter notes after each slide.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = pptxOptions.includeSpeakerNotes,
                                onCheckedChange = { pptxOptions = pptxOptions.copy(includeSpeakerNotes = it) }
                            )
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-punctuate", fontWeight = FontWeight.SemiBold)
                                Text("Add end punctuation to unpunctuated lines for smoother listening.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = pptxOptions.autoPunctuate,
                                onCheckedChange = { pptxOptions = pptxOptions.copy(autoPunctuate = it) }
                            )
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Read text in slide images", fontWeight = FontWeight.SemiBold)
                                Text("OCR screenshots and figures. Large decks import slower.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = pptxOptions.ocrSlideImages,
                                onCheckedChange = { pptxOptions = pptxOptions.copy(ocrSlideImages = it) }
                            )
                        }
                    }
                }

                if (pendingImport.isPdf) {
                    item {
                        Text("PDF options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = startPageDraft,
                                onValueChange = { value ->
                                    startPageDraft = value.filter { it.isDigit() }.take(5)
                                    val startVal = startPageDraft.toIntOrNull()
                                    pdfOptions = pdfOptions.copy(startPage = startVal)
                                },
                                label = { Text("Start page") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endPageDraft,
                                onValueChange = { value ->
                                    endPageDraft = value.filter { it.isDigit() }.take(5)
                                    val endVal = endPageDraft.toIntOrNull()
                                    pdfOptions = pdfOptions.copy(endPage = endVal)
                                },
                                label = { Text("End page") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (isPageRangeInvalid && pageCount > 0) {
                        item {
                            Text(
                                text = "Warning: Page range must be between 1 and $pageCount.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
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
                                        pdfOptions.extractionMode.ifBlank { "HTML with images" },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                DropdownMenu(
                                    expanded = modeExpanded,
                                    onDismissRequest = { modeExpanded = false }
                                ) {
                                    extractionModes.forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode) },
                                            onClick = {
                                                modeExpanded = false
                                                pdfOptions = pdfOptions.copy(
                                                    extractionMode = mode,
                                                    forceOcr = mode == "Force OCR",
                                                    preferOcrWhenLowText = mode != "Plain text"
                                                )
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
                            checked = pdfOptions.cleanupRepeatedLines,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(cleanupRepeatedLines = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Remove page numbers",
                            checked = pdfOptions.removePageNumbers,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(removePageNumbers = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Repair hyphenated line breaks",
                            checked = pdfOptions.repairHyphenation,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(repairHyphenation = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Include page markers",
                            checked = pdfOptions.includePageMarkers,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(includePageMarkers = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Remove top page noise",
                            checked = pdfOptions.removeTopPageNoise,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(removeTopPageNoise = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Remove bottom page noise",
                            checked = pdfOptions.removeBottomPageNoise,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(removeBottomPageNoise = it) }
                        )
                    }
                } else if (pendingImport.mimeType.contains("text") || pendingImport.mimeType.contains("html") || pendingImport.name.endsWith(".txt") || pendingImport.name.endsWith(".html") || pendingImport.name.endsWith(".htm")) {
                    item {
                        Text("Text Options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
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
                                    onDismissRequest = { encodingExpanded = false }
                                ) {
                                    TextImportEncodingCatalog.options.forEach { encoding ->
                                        DropdownMenuItem(
                                            text = { Text(encoding.label) },
                                            onClick = {
                                                encodingExpanded = false
                                                textOptions = textOptions.copy(encodingId = encoding.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(title.ifBlank { pendingImport.name }, pdfOptions, textOptions, pptxOptions)
                },
                enabled = !isPageRangeInvalid
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}
