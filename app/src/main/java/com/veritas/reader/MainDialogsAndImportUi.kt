package com.veritas.reader


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.content.ContentUris
import android.provider.DocumentsContract
import android.provider.MediaStore
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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import com.veritas.reader.ui.screens.CURATED_CLASSICS
import com.veritas.reader.ui.screens.ClassicBookCover
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
import androidx.compose.animation.scaleIn
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
import com.veritas.reader.ui.*
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
import com.veritas.reader.ui.screens.AccessibilitySettingsDialog
import com.veritas.reader.ui.screens.ReadingListsDialog
import com.veritas.reader.ui.screens.SettingsHubDialog
import com.veritas.reader.ui.screens.StorageDialog
import com.veritas.reader.ui.screens.formatVeritasBytes
import com.veritas.reader.ui.screens.UserManualDialog
import com.veritas.reader.ui.screens.AboutDialog
import com.veritas.reader.ui.screens.SleepTimerDialog
import com.veritas.reader.ui.screens.UpdateAvailableDialog
import com.veritas.reader.ui.screens.ReleaseNotesDialog
import com.veritas.reader.ui.screens.ClassicsCatalogDialog
import com.veritas.reader.ui.screens.OceanOfPdfBrowserDialog
import com.veritas.reader.ui.screens.BookCatalogBrowserDialog
import com.veritas.reader.ui.screens.VeritasHomeTab
import com.veritas.reader.ui.screens.VoiceStudioDialog
import com.veritas.reader.ReaderMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.veritas.reader.ui.screens.OnboardingQuestChecklist
import com.veritas.reader.ui.screens.OnboardingSpotlightOverlay
import com.veritas.reader.ui.screens.RevampedOnboardingFlow
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
internal fun MainDialogsHost(
    viewModel: ReaderViewModel,
    uiState: ReaderUiState,
    onOpenFilePicker: () -> Unit
) {
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        viewModel.approveFileBrowserFolder(uri)
    }
    val textDownloadLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val pending = viewModel.uiState.value.pendingTextDownload
        if (uri == null || pending == null) {
            viewModel.updateState { it.copy(pendingTextDownload = null) }
        } else {
            val result = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(pending.second.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("Could not open the selected save location.")
            }
            viewModel.updateState {
                it.copy(
                    pendingTextDownload = null,
                    exportMessage = result.fold(
                        onSuccess = { "Edited text saved to phone." },
                        onFailure = { error -> "Could not save edited text: ${error.message ?: "unknown error"}" }
                    )
                )
            }
        }
    }
    val backupExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            viewModel.updateState { it.copy(backupMessage = "Backup export cancelled.") }
        } else {
            viewModel.exportLibraryBackup(uri)
        }
    }
    val fullBackupExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) {
            viewModel.updateState { it.copy(backupMessage = "Backup export cancelled.") }
        } else {
            viewModel.exportFullBackup(uri)
        }
    }
    val backupImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            viewModel.updateState { it.copy(backupMessage = "Backup import cancelled.") }
        } else {
            viewModel.importLibraryBackup(uri)
        }
    }



        // --- Extracted dialogs ---
        if (uiState.showPdfImportTools) {
            PdfImportOptionsDialog(
                options = uiState.advancedPdfOptions,
                textOptions = uiState.textImportOptions,
                onOptionsChange = { opt -> viewModel.updateState { it.copy(advancedPdfOptions = opt) } },
                onTextOptionsChange = { opt -> viewModel.updateState { it.copy(textImportOptions = opt) } },
                onPickPdf = {
                    viewModel.updateState { it.copy(showPdfImportTools = false) }
                    viewModel.openFileBrowser()
                },
                onDismiss = { viewModel.updateState { it.copy(showPdfImportTools = false) } }
            )
        }

        if (uiState.showFileBrowser && uiState.pendingImport == null) {
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.refreshFileBrowser()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            FileBrowserDialog(
                roots = uiState.fileBrowserRoots,
                entries = uiState.fileBrowserFiles,
                location = uiState.fileBrowserLocation,
                canGoUp = uiState.fileBrowserBackStack.isNotEmpty(),
                scanning = uiState.fileBrowserScanning,
                message = uiState.fileBrowserMessage,
                allFilesAccessGranted = uiState.fileBrowserAllFilesGranted,
                importing = uiState.importInProgress,
                importingName = uiState.importSourceName,
                onDismiss = { viewModel.updateState { it.copy(showFileBrowser = false) } },
                onPickFolder = { folderPickerLauncher.launch(null) },
                onRequestAllFilesAccess = {
                    openAllFilesAccessSettings(context)
                    viewModel.refreshFileBrowserAccessState()
                },
                onOpenFilePicker = onOpenFilePicker,
                onRefresh = { viewModel.refreshFileBrowser() },
                onGoUp = { viewModel.goUpFileBrowserDirectory() },
                onEnterDirectory = { viewModel.enterFileBrowserDirectory(it) },
                onRemoveAllAccess = { viewModel.clearFileBrowserAccess() },
                onImportFile = { file ->
                    if (file.isSupported && !file.isDirectory) {
                        viewModel.prepareImport(uri = file.uri, sourceNameHint = file.name)
                    }
                },
                onImportMultipleFiles = { files, queue ->
                    viewModel.importMultipleDocuments(files.map { it.uri }, queue)
                },
                onDeleteFiles = { files -> viewModel.deleteBrowserFiles(files) }
            )
        }

        if (uiState.showReadingHistory) {
            ReadingHistoryDialog(
                history = uiState.readingHistory,
                documents = uiState.documents,
                onDismiss = { viewModel.updateState { it.copy(showReadingHistory = false) } },
                onOpenDocument = { doc ->
                    viewModel.updateState { it.copy(showReadingHistory = false) }; viewModel.openSavedDocument(
                    doc
                )
                },
                onClearHistory = { viewModel.clearReadingHistory() }
            )
        }

        if (uiState.showDocumentNotes) {
            uiState.activeDocument?.let { document ->
                DocumentNotesDialog(
                    document = document,
                    annotations = uiState.annotations,
                    documentNote = uiState.documentNoteDraft,
                    currentIndex = PlaybackStateStore.currentIndex.coerceIn(
                        0,
                        document.chunks.lastIndex.coerceAtLeast(0)
                    ),
                    onDocumentNoteChange = { draft ->
                        viewModel.updateState {
                            it.copy(
                                documentNoteDraft = draft
                            )
                        }
                    },
                    onSaveDocumentNote = { viewModel.saveDocumentNoteDraft() },
                    onAddCurrentNote = { viewModel.beginSentenceNote(listOf(PlaybackStateStore.currentIndex)) },
                    onJumpToSection = { index ->
                        viewModel.moveTo(index, false)
                        viewModel.updateState { it.copy(showDocumentNotes = false) }
                    },
                    onExportNotes = {
                        viewModel.saveDocumentNoteDraft()
                        viewModel.shareActiveDocumentNotes()
                    },
                    onExportPdf = {
                        viewModel.saveDocumentNoteDraft()
                        viewModel.exportStudyGuidePdf()
                    },
                    onDismiss = { viewModel.updateState { it.copy(showDocumentNotes = false) } }
                )
            }
        }

        val noteIndexes = uiState.noteTargetIndexes.ifEmpty {
            uiState.noteTargetIndex?.let(::listOf).orEmpty()
        }
        if (noteIndexes.isNotEmpty()) {
            uiState.activeDocument?.let { document ->
                SentenceNoteDialog(
                    document = document,
                    sentenceIndexes = noteIndexes,
                    noteDraft = uiState.noteDraft,
                    audioPath = uiState.noteAudioPath,
                    audioDuration = uiState.noteAudioDuration,
                    onNoteChange = { draft ->
                        viewModel.updateState {
                            it.copy(
                                noteDraft = capWords(
                                    draft,
                                    300
                                )
                            )
                        }
                    },
                    onAudioChange = { path, duration ->
                        viewModel.updateState {
                            it.copy(
                                noteAudioPath = path,
                                noteAudioDuration = duration
                            )
                        }
                    },
                    onSave = { viewModel.saveSentenceNote() },
                    onDelete = { viewModel.deleteSentenceNote() },
                    onDismiss = { viewModel.dismissSentenceNote() }
                )
            }
        }

        if (uiState.showTextEditor) {
            val document = uiState.activeDocument
            val target = uiState.editorTarget
            if (document != null && target != null) {
                TextEditorDialog(
                    document = document,
                    currentIndex = PlaybackStateStore.currentIndex,
                    text = uiState.editorText,
                    target = target,
                    onTextChange = { text -> viewModel.updateState { it.copy(editorText = text) } },
                    onSave = { partIdx, text -> viewModel.saveTextEditorChanges(partIdx, text) },
                    onDownloadToPhone = {
                        val fileName = textEditorDownloadName(document, target)
                        viewModel.updateState { it.copy(pendingTextDownload = fileName to uiState.editorText) }
                        textDownloadLauncher.launch(fileName)
                    },
                    onDismiss = { viewModel.dismissTextEditor() }
                )
            }
        }

        uiState.deleteTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { viewModel.updateState { it.copy(deleteTarget = null) } },
                title = { Text("Delete reading?") },
                text = { Text("This removes ${target.title} from the local library, queue, reading lists, history, bookmarks, and notes.") },
                confirmButton = {
                    Button(onClick = { viewModel.deleteDocument(target) }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.updateState { it.copy(deleteTarget = null) } }) {
                        Text(
                            "Cancel"
                        )
                    }
                }
            )
        }

        uiState.renameTarget?.let { target ->
            AlertDialog(
                onDismissRequest = {
                    viewModel.updateState {
                        it.copy(
                            renameTarget = null,
                            renameDraft = ""
                        )
                    }
                },
                title = { Text("Rename reading") },
                text = {
                    OutlinedTextField(
                        value = uiState.renameDraft,
                        onValueChange = { value -> viewModel.updateState { it.copy(renameDraft = value) } },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.renameDocument(target, uiState.renameDraft) },
                        enabled = uiState.renameDraft.trim().isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.updateState {
                            it.copy(
                                renameTarget = null,
                                renameDraft = ""
                            )
                        }
                    }) { Text("Cancel") }
                }
            )
        }

        uiState.collectionTarget?.let { target ->
            AlertDialog(
                onDismissRequest = {
                    viewModel.updateState {
                        it.copy(
                            collectionTarget = null,
                            collectionDraft = ""
                        )
                    }
                },
                title = { Text("Move to collection") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(target.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        OutlinedTextField(
                            value = uiState.collectionDraft,
                            onValueChange = { value ->
                                viewModel.updateState {
                                    it.copy(
                                        collectionDraft = value
                                    )
                                }
                            },
                            label = { Text("Collection") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setDocumentCollection(
                            target,
                            uiState.collectionDraft
                        )
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.updateState {
                            it.copy(
                                collectionTarget = null,
                                collectionDraft = ""
                            )
                        }
                    }) { Text("Cancel") }
                }
            )
        }

        @OptIn(ExperimentalMaterial3Api::class)
        uiState.detailsTarget?.let { target ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val coverFile = remember(target.id) { CoverExtractor.coverFile(context, target.id) }
            val coverBitmap = remember(coverFile) {
                coverFile?.let { file ->
                    if (file.exists()) runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull() else null
                }
            }
            val progress = progressFraction(target)
            val percent = (progress * 100).toInt().coerceIn(0, 100)
            val bookmarksCount = remember(target.id, uiState.allAnnotations) {
                uiState.allAnnotations.count { it.documentId == target.id && it.type == AnnotationType.BOOKMARK }
            }
            val notesCount = remember(target.id, uiState.allAnnotations, uiState.documentNotes) {
                uiState.allAnnotations.count { it.documentId == target.id && it.type != AnnotationType.BOOKMARK } +
                    (if (uiState.documentNotes.containsKey(target.id)) 1 else 0)
            }
            val matchingClassic = remember(target) {
                CURATED_CLASSICS.firstOrNull { classic ->
                    target.title.contains(classic.title, ignoreCase = true) ||
                        (target.originalFileName.isNotBlank() && target.originalFileName.contains(classic.id, ignoreCase = true))
                }
            }

            val singleParagraphSummary = remember(target.preview, target.id, matchingClassic) {
                matchingClassic?.description?.takeIf { it.isNotBlank() }
                    ?: run {
                        val clean = extractSynopsisHeuristic(target.preview)
                        if (clean.isNotBlank() && clean.split(Regex("\\s+")).size >= 10) {
                            clean
                        } else {
                            val fullText = runCatching { viewModel.repository.readText(target) }.getOrNull().orEmpty()
                            val fromFull = if (fullText.isNotBlank()) extractSynopsisHeuristic(fullText) else ""
                            if (fromFull.isNotBlank()) fromFull else clean.ifBlank { target.preview }.ifBlank { "No preview synopsis available for this document." }
                        }
                    }
            }

            val displayAuthor = remember(target, matchingClassic) {
                matchingClassic?.author ?: run {
                    val t = target.title
                    when {
                        t.contains(" by ", ignoreCase = true) -> t.substringAfterLast(" by ", "").trim()
                        t.contains(" - ") -> {
                            val candidate = t.substringAfterLast(" - ").trim()
                            if (candidate.length in 2..35 && !candidate.contains('.')) candidate else null
                        }
                        target.collection.isNotBlank() -> target.collection
                        else -> null
                    }
                }
            }

            val fileExtension = remember(target) {
                viewModel.repository.detectExtensionFromNameOrType(
                    displayName = target.title,
                    sourceLabel = target.sourceLabel,
                    mimeType = target.originalMimeType,
                    fileName = target.originalFileName
                ).uppercase()
            }

            val docFile = remember(target) {
                viewModel.repository.originalFile(target) ?: java.io.File(viewModel.repository.docsDir, target.fileName).takeIf { it.exists() }
            }

            val fileSizeText = remember(docFile) {
                docFile?.length()?.let { len ->
                    if (len <= 0L) null
                    else {
                        val kb = len / 1024.0
                        val mb = kb / 1024.0
                        when {
                            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
                            else -> "$len B"
                        }
                    }
                }
            }

            val highlightQuote = remember(matchingClassic, uiState.allAnnotations, target.id) {
                matchingClassic?.quote?.takeIf { it.isNotBlank() }
                    ?: uiState.allAnnotations.firstOrNull { it.documentId == target.id && it.note.isNotBlank() }?.note?.take(160)
            }

            ModalBottomSheet(
                onDismissRequest = { viewModel.updateState { it.copy(detailsTarget = null) } },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Centered Book Cover
                    if (matchingClassic != null) {
                        ClassicBookCover(
                            book = matchingClassic,
                            width = 110.dp,
                            height = 160.dp,
                            large = true
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(110.dp, 160.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            if (coverBitmap != null) {
                                Image(
                                    bitmap = coverBitmap.asImageBitmap(),
                                    contentDescription = target.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                                )
                                            )
                                        )
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = when (fileExtension.lowercase()) {
                                                "pdf" -> Icons.Outlined.PictureAsPdf
                                                "epub" -> Icons.Outlined.Book
                                                else -> Icons.Outlined.Description
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = fileExtension.ifBlank { target.sourceLabel.take(4).uppercase() },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Title & Author (Centered)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = target.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!displayAuthor.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "by $displayAuthor",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Compact Horizontal Pills Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = fileExtension.ifBlank { target.sourceLabel.ifBlank { "Document" } },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (!fileSizeText.isNullOrBlank()) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = fileSizeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            val lengthLabel = if (target.pageCount > 0) "${target.pageCount} pgs" else "${target.chunkCount} sents"
                            Text(
                                text = lengthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "⏱️ ${formatEstimatedReadTime(target)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (bookmarksCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "🔖 $bookmarksCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (notesCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "📝 $notesCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Highlight Quote Card
                    if (!highlightQuote.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“$highlightQuote”",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                    }

                    // Full Synopsis directly underneath without "Overview" or "Summary" header
                    Text(
                        text = singleParagraphSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        lineHeight = 20.sp,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Reading Progress Indicator (if started)
                    if (progress > 0f) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reading Progress",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Action Bar: Full-Width Stretched Read/Resume Button + Native Share Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateState { it.copy(detailsTarget = null) }
                                viewModel.openSavedDocument(target)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = if (target.currentIndex > 0) Icons.Filled.PlayArrow else Icons.Outlined.Book,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (target.currentIndex > 0) "Resume" else "Read",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        FilledTonalIconButton(
                            onClick = {
                                val uri = viewModel.repository.originalUri(target)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    if (uri != null) {
                                        val mime = target.originalMimeType.ifBlank {
                                            when (fileExtension.lowercase()) {
                                                "pdf" -> "application/pdf"
                                                "epub" -> "application/epub+zip"
                                                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                                "txt" -> "text/plain"
                                                else -> "application/octet-stream"
                                            }
                                        }
                                        type = mime
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, target.title)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    } else {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, target.title)
                                        putExtra(Intent.EXTRA_TEXT, "${target.title}\n\n${target.preview}")
                                    }
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Document"))
                            },
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share Document",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showBackupTools) {
            var fullBackupEstimate by remember { mutableStateOf(0L) }
            LaunchedEffect(Unit) {
                fullBackupEstimate = viewModel.estimateFullBackupBytes()
            }
            BackupRestoreDialog(
                documentCount = uiState.documents.size,
                annotationCount = uiState.annotationCount,
                queueCount = uiState.queuedDocuments.size,
                inProgress = uiState.backupInProgress,
                message = uiState.backupMessage,
                fullBackupEstimateBytes = fullBackupEstimate,
                autoBackupEnabled = uiState.readerSettings.autoBackupWeekly,
                onToggleAutoBackup = {
                    viewModel.saveReaderSettings(
                        uiState.readerSettings.copy(autoBackupWeekly = !uiState.readerSettings.autoBackupWeekly)
                    )
                },
                onExport = {
                    backupExportLauncher.launch(veritasBackupFileName("vern_backup"))
                },
                onExportFull = {
                    fullBackupExportLauncher.launch(veritasBackupZipFileName("vern_full_backup"))
                },
                onImport = {
                    backupImportLauncher.launch(veritasBackupMimeTypes())
                },
                onDismiss = { viewModel.updateState { it.copy(showBackupTools = false) } }
            )
        }

        if (uiState.showSyncCenter) {
            var fullBackupEstimate by remember { mutableStateOf(0L) }
            LaunchedEffect(Unit) {
                fullBackupEstimate = viewModel.estimateFullBackupBytes()
            }
            SyncCenterDialog(
                documentCount = uiState.documents.size,
                annotationCount = uiState.annotationCount,
                queueCount = uiState.queuedDocuments.size,
                pronunciationRuleCount = uiState.pronunciationRules.size,
                inProgress = uiState.backupInProgress,
                message = uiState.backupMessage,
                fullBackupEstimateBytes = fullBackupEstimate,
                autoBackupEnabled = uiState.readerSettings.autoBackupWeekly,
                onToggleAutoBackup = {
                    viewModel.saveReaderSettings(
                        uiState.readerSettings.copy(autoBackupWeekly = !uiState.readerSettings.autoBackupWeekly)
                    )
                },
                onExportSyncPack = {
                    backupExportLauncher.launch(veritasBackupFileName("vern_sync_pack"))
                },
                onExportFull = {
                    fullBackupExportLauncher.launch(veritasBackupZipFileName("vern_full_backup"))
                },
                onShareSyncPack = { viewModel.updateState { it.copy(showSyncCenter = false) }; viewModel.shareLibrarySyncPack() },
                onImportSyncPack = {
                    backupImportLauncher.launch(veritasBackupMimeTypes())
                },
                onDismiss = { viewModel.updateState { it.copy(showSyncCenter = false) } }
            )
        }

        if (uiState.showAppHealth) {
            AppHealthDialog(
                documentCount = uiState.documents.size,
                queueCount = uiState.queuedDocuments.size,
                themePackName = VeritasThemePackCatalog.displayName(uiState.readerSettings.themePackId),
                themeName = VeritasThemeCatalog.displayName(uiState.readerSettings.themeId),
                onDismiss = { viewModel.updateState { it.copy(showAppHealth = false) } }
            )
        }


}
