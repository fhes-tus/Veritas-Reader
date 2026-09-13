package com.veritas.reader.ui.screens

import android.graphics.BitmapFactory
import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.scale
import java.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.veritas.reader.VeritasPackStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.animation.animateContentSize
import java.util.UUID
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import com.veritas.reader.ui.rememberVeritasHaptics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.widget.Toast
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.core.content.edit
import androidx.compose.ui.layout.onGloballyPositioned
import com.veritas.reader.ui.OnboardingController
import com.veritas.reader.ui.OnboardingStep
import com.veritas.reader.*
import com.veritas.reader.ui.ReaderUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.ui.res.stringResource
import com.veritas.reader.R
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

import androidx.compose.foundation.lazy.LazyListState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryDialogsAndSheetsHost(
    activePlayingQuiz: QuizSet?,
    onDismissActivePlayingQuiz: () -> Unit,
    onRecordQuizScore: (String, Int) -> Unit,
    showGeminiApiKeyDialog: Boolean,
    onDismissGeminiApiKeyDialog: () -> Unit,
    showQuizLabMetrics: Boolean,
    onDismissQuizLabMetrics: () -> Unit,
    onPlayQuiz: (QuizSet) -> Unit,
    onShowPasteQuiz: () -> Unit,
    quizToDelete: QuizSet?,
    onDismissQuizToDelete: () -> Unit,
    onDeleteQuiz: (String) -> Unit,
    showPasteQuiz: Boolean,
    onDismissPasteQuiz: () -> Unit,
    onSaveQuiz: (QuizSet) -> Unit,
    showContentSearchResults: Boolean,
    onDismissContentSearchResults: () -> Unit,
    showPasteFlashcards: Boolean,
    onDismissPasteFlashcards: () -> Unit,
    onImportFlashcards: (String, List<Flashcard>) -> Unit,
    onOpenDocumentAt: (SavedDocument, Int) -> Unit,
    // Bottom dialogs & sheets
    showImportSheet: Boolean,
    onDismissImportSheet: () -> Unit,
    importSheetMode: ImportSheetMode,
    onImportSheetModeChange: (ImportSheetMode) -> Unit,
    draftText: String,
    onDraftTextChange: (String) -> Unit,
    onCreateFromDraft: () -> Unit,
    onImportWebArticle: (String) -> Unit,
    onImportFile: () -> Unit,
    onImportImage: () -> Unit,
    onOpenFileBrowser: () -> Unit,
    onOpenClassicsCatalog: () -> Unit,
    onWriteGeneralNote: () -> Unit,
    manageListsDocument: SavedDocument?,
    onDismissManageLists: () -> Unit,
    readingListCatalog: VeritasReadingListCatalog,
    onCreateReadingList: (String, String?) -> Unit,
    onAddDocumentToReadingList: (String, String) -> Unit,
    onRemoveDocumentFromReadingList: (String, String) -> Unit,
    showQueue: Boolean,
    onDismissQueue: () -> Unit,
    queuedDocuments: List<SavedDocument>,
    onMoveQueueBy: (SavedDocument, Int) -> Unit,
    onRemoveFromQueue: (SavedDocument) -> Unit,
    onClearQueue: () -> Unit,
    onPlayQueue: () -> Unit,
    confirmBatchDelete: Boolean,
    onDismissConfirmBatchDelete: () -> Unit,
    selectedDocumentIds: Set<String>,
    onClearSelectedDocuments: () -> Unit,
    onBatchDeleteDocuments: (Set<String>) -> Unit,
    confirmAnnotationDelete: Boolean,
    onDismissConfirmAnnotationDelete: () -> Unit,
    selectedAnnotationKeys: Set<String>,
    onClearSelectedAnnotations: () -> Unit,
    onDeleteAnnotations: (Set<String>) -> Unit,
    showHomeSidebar: Boolean,
    onDismissHomeSidebar: () -> Unit,
    welcomeName: String,
    readerTrackerSnapshot: ReaderTrackerSnapshot,
    onNavigateToTab: (VeritasHomeTab) -> Unit,
    showReadingStatsHome: Boolean,
    onDismissReadingStatsHome: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettingsHub: () -> Unit,
    onOpenReadingLists: () -> Unit,
    onOpenReadingHistory: () -> Unit,
    documents: List<SavedDocument>,
    documentReadingTimes: Map<String, Long>,
    readerSettings: ReaderSettings,
    onSaveReaderSettings: (ReaderSettings) -> Unit,
    viewerCards: List<FlashcardProgress>?,
    viewerSetName: String,
    onDismissViewerCards: () -> Unit,
    onRateFlashcardRecall: (String, String) -> Unit,
    onDeleteFlashcard: (String) -> Unit,
    renameSetTarget: FlashcardSet?,
    onDismissRenameSetTarget: () -> Unit,
    onRenameFlashcardSet: (String, String) -> Unit,
    showBatchCollectionDialog: Boolean,
    onDismissBatchCollectionDialog: () -> Unit,
    batchCollectionDraft: String,
    onBatchCollectionDraftChange: (String) -> Unit,
    onBatchSetCollectionDocuments: (Set<String>, String) -> Unit,
    confirmDeleteVocabDocId: String?,
    onDismissConfirmDeleteVocabDocId: () -> Unit,
    vocabDocs: List<Triple<SavedDocument, GeneralNote, List<VocabularyEntry>>>,
    onRemoveVocabularyWord: (String, String) -> Unit,
    isOpeningDocument: Boolean,
    uiState: ReaderUiState
) {
    val context = LocalContext.current

    if (activePlayingQuiz != null) {
        QuizPlayerDialog(
            questions = activePlayingQuiz!!.questions,
            quizTitle = activePlayingQuiz!!.title,
            onSaveScore = { score -> onRecordQuizScore(activePlayingQuiz!!.id, score) },
            onDismiss = { onDismissActivePlayingQuiz() }
        )
    }
    if (showGeminiApiKeyDialog) {
        GeminiApiKeyDialog(onDismiss = { onDismissGeminiApiKeyDialog() })
    }
    if (showQuizLabMetrics) {
        QuizLabMetricsDialog(
            quizzes = uiState.quizzes,
            documents = uiState.documents,
            onPlayQuiz = { onPlayQuiz(it) },
            onNewQuiz = { onShowPasteQuiz() },
            onDismiss = { onDismissQuizLabMetrics() }
        )
    }
    if (quizToDelete != null) {
        AlertDialog(
            onDismissRequest = { onDismissQuizToDelete() },
            title = { Text("Delete Quiz?") },
            text = { Text("Are you sure you want to delete \"${quizToDelete?.title}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        quizToDelete?.let { onDeleteQuiz(it.id) }
                        onDismissQuizToDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissQuizToDelete() }) { Text("Cancel") }
            }
        )
    }

    if (showPasteQuiz) {
        PasteQuizDialog(onSaveQuiz = onSaveQuiz, onDismiss = { onDismissPasteQuiz() })
    }
    if (showContentSearchResults) {
        AlertDialog(
            onDismissRequest = { onDismissContentSearchResults() },
            confirmButton = {
                TextButton(onClick = { onDismissContentSearchResults() }) { Text("Close") }
            },
            title = { Text("In your documents") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when {
                        uiState.librarySearchInProgress -> {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text("Searching every document…", style = MaterialTheme.typography.bodySmall)
                        }
                        uiState.librarySearchHits.isEmpty() ->
                            Text("No matches inside any document.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else -> uiState.librarySearchHits.forEach { hit ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismissContentSearchResults()
                                        onOpenDocumentAt(hit.document, hit.sentenceIndex)
                                    },
                                shape = VeritasPackStyle.compactShape(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(hit.document.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(hit.snippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Text("Sentence ${hit.sentenceIndex + 1} — tap to open", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
    if (showPasteFlashcards) {
        PasteFlashcardsDialog(
            onImport = { name, parsed -> onImportFlashcards(name, parsed); onDismissPasteFlashcards() },
            onDismiss = { onDismissPasteFlashcards() }
        )
    }


    if (showImportSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                onDismissImportSheet()
                onImportSheetModeChange(ImportSheetMode.MENU)
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            when (importSheetMode) {
                ImportSheetMode.MENU -> {
                    ImportSheetMenu(
                        onSelectOption = { option ->
                            when (option) {
                                ImportOption.FILE -> {
                                    onDismissImportSheet()
                                    onImportFile()
                                }
                                ImportOption.WEB -> {
                                    onImportSheetModeChange(ImportSheetMode.WEB)
                                }
                                ImportOption.PASTE -> {
                                    onImportSheetModeChange(ImportSheetMode.PASTE)
                                }
                                ImportOption.SCAN -> {
                                    onDismissImportSheet()
                                    onImportImage()
                                }
                                ImportOption.BROWSE -> {
                                    onDismissImportSheet()
                                    onOpenFileBrowser()
                                }
                                ImportOption.CLASSICS -> {
                                    onDismissImportSheet()
                                    onOpenClassicsCatalog()
                                }
                                ImportOption.WRITE_NOTE -> {
                                    onDismissImportSheet()
                                    onWriteGeneralNote()
                                }
                            }
                        },
                        onDismiss = { onDismissImportSheet() }
                    )
                }
                ImportSheetMode.WEB -> {
                    ImportSheetWeb(
                        urlText = draftText,
                        onUrlChange = onDraftTextChange,
                        onImport = { url ->
                            onImportWebArticle(url)
                            onDismissImportSheet()
                            onImportSheetModeChange(ImportSheetMode.MENU)
                        },
                        onBack = { onImportSheetModeChange(ImportSheetMode.MENU) }
                    )
                }
                ImportSheetMode.PASTE -> {
                    ImportSheetPaste(
                        pastedText = draftText,
                        onTextChange = onDraftTextChange,
                        onSave = {
                            onCreateFromDraft()
                            onDismissImportSheet()
                            onImportSheetModeChange(ImportSheetMode.MENU)
                        },
                        onBack = { onImportSheetModeChange(ImportSheetMode.MENU) }
                    )
            }
        }
    }
    }

    // Filter dialog removed — filters are handled inline via chip row and
    // the LibraryControlsCard. No modal needed.

    manageListsDocument?.let { doc ->
        ManageDocumentListsDialog(
            document = doc,
            catalog = readingListCatalog,
            onDismiss = { onDismissManageLists() },
            onCreateReadingList = { title ->
                onCreateReadingList(title, doc.id)
            },
            onAddDocumentToReadingList = onAddDocumentToReadingList,
            onRemoveDocumentFromReadingList = onRemoveDocumentFromReadingList
        )
    }

    if (showQueue) {
        VeritasQueueSheet(
            queue = queuedDocuments,
            onMove = onMoveQueueBy,
            onRemove = onRemoveFromQueue,
            onClear = { onClearQueue(); onDismissQueue() },
            onPlay = { onPlayQueue() },
            onDismiss = { onDismissQueue() }
        )
    }

    if (confirmBatchDelete) {
        AlertDialog(
            onDismissRequest = { onDismissConfirmBatchDelete() },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBatchDeleteDocuments(selectedDocumentIds)
                        onClearSelectedDocuments()
                        onDismissConfirmBatchDelete()
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { onDismissConfirmBatchDelete() }) { Text("Cancel") }
            },
            title = { Text("Delete selected readings?") },
            text = { Text("This will remove ${selectedDocumentIds.size} reading${if (selectedDocumentIds.size == 1) "" else "s"} from this device.") }
        )
    }

    if (confirmAnnotationDelete) {
        AlertDialog(
            onDismissRequest = { onDismissConfirmAnnotationDelete() },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAnnotations(selectedAnnotationKeys)
                        onClearSelectedAnnotations()
                        onDismissConfirmAnnotationDelete()
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { onDismissConfirmAnnotationDelete() }) { Text("Cancel") }
            },
            title = { Text("Delete selected marks?") },
            text = { Text("This removes ${selectedAnnotationKeys.size} bookmark/note item${if (selectedAnnotationKeys.size == 1) "" else "s"} and clears bookmark highlights from the reader.") }
        )
    }

    if (showHomeSidebar) {
        HomeSidebarDialog(
            name = welcomeName,
            snapshot = readerTrackerSnapshot,
            onDismiss = { onDismissHomeSidebar() },
            onOpenLibrary = {
                onNavigateToTab(VeritasHomeTab.LIBRARY)
                onDismissHomeSidebar()
            },
            onOpenStats = {
                onOpenStats()
                onDismissHomeSidebar()
            },
            onOpenSettings = {
                onDismissHomeSidebar()
                onOpenSettingsHub()
            },
            onOpenReadingLists = {
                onDismissHomeSidebar()
                onOpenReadingLists()
            },
            onOpenReadingHistory = {
                onDismissHomeSidebar()
                onOpenReadingHistory()
            }
        )
    }

    if (showReadingStatsHome) {
        ReadingStatsDashboardDialog(
            snapshot = readerTrackerSnapshot,
            documents = documents,
            documentReadingTimes = documentReadingTimes,
            readerSettings = readerSettings,
            onGoalMinutesChange = { minutes ->
                onSaveReaderSettings(readerSettings.copy(dailyGoalMinutes = minutes))
            },
            onDismiss = { onDismissReadingStatsHome() }
        )
    }

    viewerCards?.let { deck ->
        FlashcardViewerDialog(
            setName = viewerSetName,
            cards = deck,
            onRate = onRateFlashcardRecall,
            onDeleteCard = onDeleteFlashcard,
            onDismiss = { onDismissViewerCards() }
        )
    }

    renameSetTarget?.let { target ->
        var draft by remember(target.setId) { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { onDismissRenameSetTarget() },
            title = { Text("Rename set") },
            confirmButton = {
                Button(onClick = { onRenameFlashcardSet(target.setId, draft); onDismissRenameSetTarget() }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { onDismissRenameSetTarget() }) { Text("Cancel") } },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    label = { Text("Set name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    if (showBatchCollectionDialog) {
        AlertDialog(
            onDismissRequest = { onDismissBatchCollectionDialog() },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBatchSetCollectionDocuments(selectedDocumentIds, batchCollectionDraft)
                        onClearSelectedDocuments()
                        onBatchCollectionDraftChange("")
                        onDismissBatchCollectionDialog()
                    }
                ) { Text("Move") }
            },
            dismissButton = {
                TextButton(onClick = { onDismissBatchCollectionDialog() }) { Text("Cancel") }
            },
            title = { Text("Move selected readings") },
            text = {
                OutlinedTextField(
                    value = batchCollectionDraft,
                    onValueChange = { onBatchCollectionDraftChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Collection") },
                    placeholder = { Text("Leave blank for Unfiled") },
                    singleLine = true
                )
            }
        )
    }

    confirmDeleteVocabDocId?.let { delDocId ->
        AlertDialog(
            onDismissRequest = { onDismissConfirmDeleteVocabDocId() },
            title = { Text("Delete all vocabulary?") },
            text = { Text("This will permanently remove all vocabulary words saved for this book.") },
            confirmButton = {
                Button(
                    onClick = {
                        vocabDocs.firstOrNull { it.first.id == delDocId }?.third?.forEach { entry ->
                            onRemoveVocabularyWord(delDocId, entry.word)
                        }
                        onDismissConfirmDeleteVocabDocId()
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissConfirmDeleteVocabDocId() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (isOpeningDocument) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text("Opening document...", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

}
