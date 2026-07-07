package com.veritas.reader.ui.screens

import com.veritas.reader.TextChunker
import android.graphics.BitmapFactory
import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.text.font.FontStyle
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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
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
import androidx.compose.ui.layout.onGloballyPositioned
import com.veritas.reader.ui.OnboardingController
import com.veritas.reader.ui.OnboardingStep
import com.veritas.reader.*
import com.veritas.reader.ui.ReaderUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

private enum class VeritasHomeTab {
    HOME,
    LIBRARY,
    STUDY
}

private data class MarkedDocument(
    val document: SavedDocument,
    val annotations: List<ReaderAnnotation>,
    val documentNote: String,
    val updatedAt: Long
)

private enum class ImportSheetMode {
    MENU,
    WEB,
    PASTE
}

private enum class ImportOption {
    FILE,
    WEB,
    PASTE,
    SCAN,
    BROWSE,
    WRITE_NOTE
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_VALUE", "ASSIGNED_VALUE_IS_NEVER_READ")
@Composable
fun LibraryScreen(
    uiState: ReaderUiState,
    onDraftTextChange: (String) -> Unit,
    onCreateFromDraft: () -> Unit,
    widgetAction: String? = null,
    onImportWebArticle: (String) -> Unit,
    onImportFile: () -> Unit,
    onImportImage: () -> Unit,
    onAdvancedPdfImport: () -> Unit,
    onOpenFileBrowser: () -> Unit,
    onOpenReadingLists: () -> Unit,
    onOpenReadingHistory: () -> Unit,
    onOpenDocument: (SavedDocument) -> Unit,
    onOpenDocumentAt: (SavedDocument, Int) -> Unit,
    onClearContinueDocument: (SavedDocument) -> Unit,
    onPlayPauseContinue: (SavedDocument) -> Unit = {},
    onDeleteDocument: (SavedDocument) -> Unit,
    onToggleQueue: (SavedDocument) -> Unit,
    onToggleFavorite: (SavedDocument) -> Unit,
    onRenameDocument: (SavedDocument) -> Unit,
    onSetCollection: (SavedDocument) -> Unit,
    onShowDetails: (SavedDocument) -> Unit,
    isQueued: (SavedDocument) -> Boolean,
    onPlayQueue: () -> Unit,
    onMoveQueueUp: (SavedDocument) -> Unit,
    onMoveQueueDown: (SavedDocument) -> Unit,
    onRemoveFromQueue: (SavedDocument) -> Unit,
    onClearQueue: () -> Unit,
    onOpenSyncCenter: () -> Unit,
    onOpenSettingsHub: () -> Unit,
    onRefreshMainPage: () -> Unit,
    onBatchDeleteDocuments: (Set<String>) -> Unit,
    onBatchFavoriteDocuments: (Set<String>) -> Unit,
    onBatchQueueDocuments: (Set<String>) -> Unit,
    onBatchSetCollectionDocuments: (Set<String>, String) -> Unit,
    onDeleteAnnotations: (Set<String>) -> Unit,
    onWriteGeneralNote: () -> Unit,
    onEditGeneralNote: (GeneralNote) -> Unit,
    onCreateReadingList: (String, String?) -> Unit = { _, _ -> },
    onAddDocumentToReadingList: (String, String) -> Unit = { _, _ -> },
    onRemoveDocumentFromReadingList: (String, String) -> Unit = { _, _ -> },
    onRemoveVocabularyWord: (String, String) -> Unit = { _, _ -> },
    onClearReadingHistory: () -> Unit = {},
    onRemoveReadingHistoryEntry: (String) -> Unit = {},
    onToggleGeneralNotePin: (String) -> Unit = {},
    onChangeGeneralNoteColor: (String, String?) -> Unit = { _, _ -> },
    onDeleteGeneralNote: (String) -> Unit = {},
    onGradeFlashcard: (String, Int) -> Unit = { _, _ -> },
    onDeleteFlashcard: (String) -> Unit = {}
) {
    val documents = uiState.documents
    val configuration = LocalConfiguration.current
    val columnCount = when {
        configuration.screenWidthDp >= 840 -> 4
        configuration.screenWidthDp >= 600 -> 3
        else -> 2
    }
    val queuedDocuments = uiState.queuedDocuments
    val draftText = uiState.draftText
    var libraryQuery by rememberSaveable { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var sourceFilter by remember { mutableStateOf("All") }
    var collectionFilter by remember { mutableStateOf("All") }
    var readingListFilter by remember { mutableStateOf("All") }
    var manageListsDocument by remember { mutableStateOf<SavedDocument?>(null) }
    var activeReviewDeck by remember { mutableStateOf<List<FlashcardProgress>?>(null) }
    var sortMode by remember { mutableStateOf("Updated") }
    var showQueue by remember { mutableStateOf(false) }
    var selectedDocumentIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var confirmBatchDelete by remember { mutableStateOf(false) }
    var showBatchMenu by remember { mutableStateOf(false) }
    var showBatchCollectionDialog by remember { mutableStateOf(false) }
    var batchCollectionDraft by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val repository = remember(context) { com.veritas.reader.DocumentRepository(context) }
    var loadedDocSentences by remember { mutableStateOf(emptyMap<String, List<String>>()) }
    val docIdsWithAnnotations = remember(uiState.allAnnotations) {
        uiState.allAnnotations.map { it.documentId }.toSet()
    }
    LaunchedEffect(docIdsWithAnnotations, uiState.documents) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val neededIds = docIdsWithAnnotations.filter { it !in loadedDocSentences }
            if (neededIds.isNotEmpty()) {
                val newMap = neededIds.associateWith { docId ->
                    val docMetadata = uiState.documents.firstOrNull { it.id == docId }
                    if (docMetadata != null) {
                        val text = repository.readText(docMetadata)
                        TextChunker.chunk(text)
                    } else {
                        emptyList()
                    }
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    loadedDocSentences = loadedDocSentences + newMap
                }
            }
        }
    }
    val libraryPrefs = remember { context.getSharedPreferences("veritas_library_settings", Context.MODE_PRIVATE) }
    var libraryViewMode by remember {
        mutableStateOf(
            runCatching {
                LibraryViewMode.valueOf(
                    libraryPrefs.getString("library_view_mode", LibraryViewMode.TILES.name) ?: LibraryViewMode.TILES.name
                )
            }.getOrDefault(LibraryViewMode.TILES)
        )
    }
    var showLibraryViewMenu by remember { mutableStateOf(false) }
    var selectedHomeTab by remember(widgetAction) {
        mutableStateOf(
            when (widgetAction) {
                "show_study_dashboard",
                "show_notes",
                "new_note",
                "new_checklist_note",
                "new_reminder_note" -> VeritasHomeTab.STUDY
                "open_library" -> VeritasHomeTab.LIBRARY
                else -> VeritasHomeTab.HOME
            }
        )
    }
    // Auto-switch tabs during onboarding so each spotlight target is actually on screen:
    // the hamburger lives on the HOME tab, the FAB and document cards on LIBRARY.
    val isTourActive = OnboardingController.activeStep != null
    LaunchedEffect(OnboardingController.activeStep) {
        when {
            OnboardingController.activeStep == OnboardingStep.INSIGHTS_SPOTLIGHT ->
                selectedHomeTab = VeritasHomeTab.HOME
            isTourActive -> selectedHomeTab = VeritasHomeTab.LIBRARY
        }
    }
    var showHomeSidebar by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var importSheetMode by remember { mutableStateOf(ImportSheetMode.MENU) }
    var showReadingStatsHome by remember(widgetAction) {
        mutableStateOf(widgetAction == "show_study_dashboard")
    }
    // During the insights onboarding step the dashboard opens itself so the tour can
    // describe it in place; it closes again when the tour moves on.
    val activeOnboardingStep = OnboardingController.activeStep
    LaunchedEffect(activeOnboardingStep) {
        if (activeOnboardingStep == OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT) {
            showReadingStatsHome = true
        } else if (activeOnboardingStep != null) {
            showReadingStatsHome = false
        }
    }
    var selectedAnnotationKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var confirmAnnotationDelete by remember { mutableStateOf(false) }
    var annotationFilter by remember(widgetAction) {
        mutableStateOf(
            if (widgetAction == "show_notes" || widgetAction == "new_note" || widgetAction == "new_checklist_note" || widgetAction == "new_reminder_note") "General" else "General"
        )
    }
    var expandedVocabDocIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val libraryListState = rememberLazyListState()
    var lastMainPageRefreshAt by remember { mutableLongStateOf(0L) }
    val libraryFeatures = remember(documents.size, queuedDocuments.size) {
        VeritasFeatureRegistry.resolve(
            VeritasFeatureSurface.LIBRARY_OVERFLOW,
            VeritasFeatureContext(
                hasSavedDocument = documents.isNotEmpty(),
                queueCount = queuedDocuments.size
            )
        ).associateBy { it.definition.id }
    }

    fun libraryFeature(id: VeritasFeatureId): ResolvedVeritasFeature =
        libraryFeatures.requireResolvedFeature(id)

    val completedCount by remember(documents) { derivedStateOf { documents.count { it.chunkCount > 0 && it.currentIndex >= it.chunkCount - 1 } } }
    val readingCount by remember(documents) { derivedStateOf { documents.count { it.chunkCount > 1 && it.currentIndex in 1 until it.chunkCount - 1 } } }
    val favoriteCount by remember(documents) { derivedStateOf { documents.count { it.favorite } } }
    val continueDocument by remember(documents) {
        derivedStateOf {
            documents
                .filter { it.chunkCount > 1 && it.currentIndex in 1 until it.chunkCount }
                .maxByOrNull { it.updatedAt }
        }
    }
    val selectionMode = selectedDocumentIds.isNotEmpty()
    val currentStreak = uiState.readerTrackerSnapshot.currentStreak
    val longestStreak = uiState.readerTrackerSnapshot.longestStreak
    val welcomeName = uiState.userName.trim().ifBlank { "Reader" }
    val (dashboardHeadline, dashboardSubtitle) = when {
        documents.isEmpty() -> {
            "Welcome to Veritas." to "Add your first reading to get started."
        }
        currentStreak >= 2 -> {
            val subMsg = if (currentStreak >= 7) "You're on a roll!" else "Keep it going!"
            "$welcomeName, you're on a $currentStreak-day streak 🔥" to subMsg
        }
        else -> {
            "Welcome back, $welcomeName." to "Pick up where you left off."
        }
    }
    val annotatedDocuments = remember(documents, uiState.allAnnotations, uiState.documentNotes, uiState.documentTitles) {
        val annotationsByDocument = uiState.allAnnotations
            .filter { it.type == AnnotationType.BOOKMARK || it.type == AnnotationType.NOTE }
            .groupBy { it.documentId }
        val markedDocumentIds = annotationsByDocument.keys + uiState.documentNotes.keys
        markedDocumentIds.mapNotNull { documentId ->
            val document = documents.firstOrNull { it.id == documentId } ?: SavedDocument(
                id = documentId,
                title = uiState.documentTitles[documentId] ?: "Deleted Book",
                fileName = "",
                sourceLabel = "Deleted",
                createdAt = 0,
                updatedAt = 0,
                currentIndex = 0,
                chunkCount = 0,
                charCount = 0,
                preview = ""
            )
            val annotations = annotationsByDocument[documentId].orEmpty().sortedBy { annotation -> annotation.chunkIndex }
            val documentNote = uiState.documentNotes[documentId].orEmpty()
            if (annotations.isEmpty() && documentNote.isBlank()) {
                null
            } else {
                MarkedDocument(
                    document = document,
                    annotations = annotations,
                    documentNote = documentNote,
                    updatedAt = maxOf(
                        annotations.maxOfOrNull { it.updatedAt } ?: 0L,
                        if (documentNote.isBlank()) 0L else document.updatedAt
                    )
                )
            }
        }.sortedByDescending { it.updatedAt }
    }
    val filteredAnnotatedDocuments = remember(annotatedDocuments, annotationFilter) {
        annotatedDocuments.map { markedDoc ->
            val bookmarks = markedDoc.annotations.filter { it.type == AnnotationType.BOOKMARK }
            val notes = markedDoc.annotations.filter { it.type == AnnotationType.NOTE }
            markedDoc.copy(
                annotations = when (annotationFilter) {
                    "Bookmarks" -> bookmarks
                    "Notes" -> notes
                    else -> markedDoc.annotations
                },
                documentNote = if (annotationFilter == "Bookmarks") "" else markedDoc.documentNote
            )
        }.filter {
            (annotationFilter == "All") || it.annotations.isNotEmpty() || it.documentNote.isNotBlank()
        }
    }
    val annotationSelectionMode = selectedAnnotationKeys.isNotEmpty()

    val bookmarksOnly = remember(filteredAnnotatedDocuments) {
        filteredAnnotatedDocuments.filter { markedDoc -> markedDoc.annotations.any { it.type == AnnotationType.BOOKMARK } }
    }
    val notesOnly = remember(filteredAnnotatedDocuments) {
        filteredAnnotatedDocuments.filter { markedDoc -> markedDoc.annotations.any { it.type == AnnotationType.NOTE } || markedDoc.documentNote.isNotBlank() }
    }
    val vocabDocs = remember(uiState.generalNotes, uiState.documents, uiState.documentTitles) {
        uiState.generalNotes
            .filter { it.title.startsWith("__vocab__") }
            .mapNotNull { note ->
                val docId = note.title.removePrefix("__vocab__")
                val doc = uiState.documents.firstOrNull { it.id == docId } ?: SavedDocument(
                    id = docId,
                    title = uiState.documentTitles[docId] ?: "Deleted Book",
                    fileName = "",
                    sourceLabel = "Deleted",
                    createdAt = 0,
                    updatedAt = 0,
                    currentIndex = 0,
                    chunkCount = 0,
                    charCount = 0,
                    preview = ""
                )
                val entries = parseVocabularyNoteContent(note.content)
                if (entries.isNotEmpty()) {
                    Triple(doc, note, entries)
                } else {
                    null
                }
            }
    }
    val trueGeneralNotes = remember(uiState.generalNotes) {
        uiState.generalNotes.filterNot { it.title.startsWith("__vocab__") }
    }
    var noteSearchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var noteSortOrder by remember { mutableStateOf("date") }
    val processedGeneralNotes = remember(trueGeneralNotes, noteSearchQuery, noteSortOrder) {
        var list = trueGeneralNotes.filter { note ->
            noteSearchQuery.isBlank() || 
            note.title.contains(noteSearchQuery, ignoreCase = true) ||
            note.content.contains(noteSearchQuery, ignoreCase = true)
        }
        list = if (noteSortOrder == "title") {
            list.sortedBy { it.title.lowercase() }
        } else {
            list.sortedByDescending { it.updatedAt }
        }
        list
    }
    val cards = uiState.flashcards
    val dueCards = remember(cards) {
        val now = System.currentTimeMillis()
        cards.filter { it.nextReviewTime <= now }
    }
    val visibleDocuments by remember(documents, queuedDocuments, libraryQuery, statusFilter, sourceFilter, collectionFilter, readingListFilter, sortMode, uiState.readingListCatalog) {
        derivedStateOf {
            documents.asSequence()
                .filter { doc ->
                    val q = libraryQuery.trim()
                    q.isBlank() || doc.title.contains(q, ignoreCase = true) || doc.preview.contains(q, ignoreCase = true) || doc.sourceLabel.contains(q, ignoreCase = true) || doc.collection.contains(q, ignoreCase = true)
                }
                .filter { doc ->
                    when (statusFilter) {
                        "Favorites" -> doc.favorite
                        "Queued" -> isQueued(doc)
                        "Unread" -> doc.currentIndex <= 0
                        "In progress" -> doc.chunkCount > 1 && doc.currentIndex in 1 until doc.chunkCount - 1
                        "Completed" -> doc.chunkCount > 0 && doc.currentIndex >= doc.chunkCount - 1
                        else -> true
                    }
                }
                .filter { doc -> sourceFilter == "All" || doc.sourceLabel == sourceFilter }
                .filter { doc ->
                    when (collectionFilter) {
                        "All" -> true
                        "Unfiled" -> doc.collection.isBlank()
                        else -> doc.collection == collectionFilter
                    }
                }
                .filter { doc ->
                    if (readingListFilter == "All") {
                        true
                    } else {
                        val list = uiState.readingListCatalog.list(readingListFilter)
                        list?.contains(doc.id) == true
                    }
                }
                .toList()
                .let { list ->
                    when (sortMode) {
                        "Title" -> list.sortedBy { it.title.lowercase(Locale.getDefault()) }
                        "Progress" -> list.sortedByDescending { progressFraction(it) }
                        "Type" -> list.sortedWith(compareBy<SavedDocument> { it.sourceLabel }.thenBy { it.title.lowercase(Locale.getDefault()) })
                        "Newest" -> list.sortedByDescending { it.createdAt }
                        else -> list.sortedByDescending { it.updatedAt }
                    }
                }
        }
    }
    if (showImportSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                showImportSheet = false
                importSheetMode = ImportSheetMode.MENU
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            when (importSheetMode) {
                ImportSheetMode.MENU -> {
                    ImportSheetMenu(
                        onSelectOption = { option ->
                            when (option) {
                                ImportOption.FILE -> {
                                    showImportSheet = false
                                    onImportFile()
                                }
                                ImportOption.WEB -> {
                                    importSheetMode = ImportSheetMode.WEB
                                }
                                ImportOption.PASTE -> {
                                    importSheetMode = ImportSheetMode.PASTE
                                }
                                ImportOption.SCAN -> {
                                    showImportSheet = false
                                    onImportImage()
                                }
                                ImportOption.BROWSE -> {
                                    showImportSheet = false
                                    onOpenFileBrowser()
                                }
                                ImportOption.WRITE_NOTE -> {
                                    showImportSheet = false
                                    onWriteGeneralNote()
                                }
                            }
                        },
                        onDismiss = { showImportSheet = false }
                    )
                }
                ImportSheetMode.WEB -> {
                    ImportSheetWeb(
                        urlText = draftText,
                        onUrlChange = onDraftTextChange,
                        onImport = { url ->
                            onImportWebArticle(url)
                            showImportSheet = false
                            importSheetMode = ImportSheetMode.MENU
                        },
                        onBack = { importSheetMode = ImportSheetMode.MENU }
                    )
                }
                ImportSheetMode.PASTE -> {
                    ImportSheetPaste(
                        pastedText = draftText,
                        onTextChange = onDraftTextChange,
                        onSave = {
                            onCreateFromDraft()
                            showImportSheet = false
                            importSheetMode = ImportSheetMode.MENU
                        },
                        onBack = { importSheetMode = ImportSheetMode.MENU }
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
            catalog = uiState.readingListCatalog,
            onDismiss = { manageListsDocument = null },
            onCreateReadingList = { title ->
                onCreateReadingList(title, doc.id)
            },
            onAddDocumentToReadingList = onAddDocumentToReadingList,
            onRemoveDocumentFromReadingList = onRemoveDocumentFromReadingList
        )
    }

    if (showQueue) {
        AlertDialog(
            onDismissRequest = { showQueue = false },
            confirmButton = { TextButton(onClick = { showQueue = false }) { Text("Close") } },
            title = { Text("Queue") },
            text = {
                Column(modifier = Modifier.height(520.dp).verticalScroll(rememberScrollState())) {
                    QueueSection(
                        queuedDocuments = queuedDocuments,
                        onPlayQueue = onPlayQueue,
                        onOpenDocument = onOpenDocument,
                        onMoveUp = onMoveQueueUp,
                        onMoveDown = onMoveQueueDown,
                        onRemove = onRemoveFromQueue,
                        onClearQueue = onClearQueue
                    )
                }
            }
        )
    }

    if (confirmBatchDelete) {
        AlertDialog(
            onDismissRequest = { confirmBatchDelete = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBatchDeleteDocuments(selectedDocumentIds)
                        selectedDocumentIds = emptySet()
                        confirmBatchDelete = false
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmBatchDelete = false }) { Text("Cancel") }
            },
            title = { Text("Delete selected readings?") },
            text = { Text("This will remove ${selectedDocumentIds.size} reading${if (selectedDocumentIds.size == 1) "" else "s"} from this device.") }
        )
    }

    if (confirmAnnotationDelete) {
        AlertDialog(
            onDismissRequest = { confirmAnnotationDelete = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAnnotations(selectedAnnotationKeys)
                        selectedAnnotationKeys = emptySet()
                        confirmAnnotationDelete = false
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmAnnotationDelete = false }) { Text("Cancel") }
            },
            title = { Text("Delete selected marks?") },
            text = { Text("This removes ${selectedAnnotationKeys.size} bookmark/note item${if (selectedAnnotationKeys.size == 1) "" else "s"} and clears bookmark highlights from the reader.") }
        )
    }

    activeReviewDeck?.let { deck ->
        FlashcardReviewDialog(
            dueCards = deck,
            onGrade = onGradeFlashcard,
            onDismiss = { activeReviewDeck = null }
        )
    }

    if (showHomeSidebar) {
        HomeSidebarDialog(
            name = welcomeName,
            snapshot = uiState.readerTrackerSnapshot,
            onDismiss = { showHomeSidebar = false },
            onOpenLibrary = {
                selectedHomeTab = VeritasHomeTab.LIBRARY
                showHomeSidebar = false
            },
            onOpenStats = {
                showReadingStatsHome = true
                showHomeSidebar = false
            },
            onOpenSettings = {
                showHomeSidebar = false
                onOpenSettingsHub()
            }
        )
    }

    if (showReadingStatsHome) {
        ReadingStatsDashboardDialog(
            snapshot = uiState.readerTrackerSnapshot,
            documents = documents,
            documentReadingTimes = uiState.documentReadingTimes,
            onDismiss = { showReadingStatsHome = false }
        )
    }

    if (showBatchCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showBatchCollectionDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBatchSetCollectionDocuments(selectedDocumentIds, batchCollectionDraft)
                        selectedDocumentIds = emptySet()
                        batchCollectionDraft = ""
                        showBatchCollectionDialog = false
                    }
                ) { Text("Move") }
            },
            dismissButton = {
                TextButton(onClick = { showBatchCollectionDialog = false }) { Text("Cancel") }
            },
            title = { Text("Move selected readings") },
            text = {
                OutlinedTextField(
                    value = batchCollectionDraft,
                    onValueChange = { batchCollectionDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Collection") },
                    placeholder = { Text("Leave blank for Unfiled") },
                    singleLine = true
                )
            }
        )
    }

    if (uiState.isOpeningDocument) {
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

    Box(modifier = Modifier.fillMaxSize().background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme))) {
        Scaffold(
            containerColor = Color.Transparent,
            // The container is transparent (we draw our own background brush), so set the
            // content colour explicitly to the theme's onSurface. Otherwise uncoloured Text
            // (section headers, empty states) falls back to the default black LocalContentColor
            // and is unreadable in dark mode on the notes/bookmarks/history/vocabulary tabs.
            contentColor = MaterialTheme.colorScheme.onSurface,
            floatingActionButton = {
                if (selectedHomeTab == VeritasHomeTab.LIBRARY) {
                    ExtendedFloatingActionButton(
                        onClick = { showImportSheet = true },
                        shape = VeritasPackStyle.chipShape(),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                        text = { Text("Add") },
                        modifier = Modifier.onGloballyPositioned { OnboardingController.updateBounds("add_fab", it) }
                    )
                }
            },
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            when (selectedHomeTab) {
                                VeritasHomeTab.HOME -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            TextButton(
                                                onClick = { showHomeSidebar = true },
                                                contentPadding = PaddingValues(horizontal = 0.dp),
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .onGloballyPositioned { OnboardingController.updateBounds("insights_trigger", it) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Menu,
                                                    contentDescription = "Menu",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            VeritasWordmark()
                                        }
                                        IconButton(onClick = onOpenSettingsHub) {
                                            Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                VeritasHomeTab.LIBRARY -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Your library",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        IconButton(onClick = onOpenSettingsHub) {
                                            Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    BasicTextField(
                                        value = libraryQuery,
                                        onValueChange = { libraryQuery = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
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
                                                    if (libraryQuery.isEmpty()) {
                                                        Text(
                                                            text = "Search library...",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        }
                                    )
                                }
                                VeritasHomeTab.STUDY -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Study",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            val bookmarkCount = uiState.allAnnotations.count { it.type == AnnotationType.BOOKMARK }
                                            val noteCount = uiState.allAnnotations.count { it.type == AnnotationType.NOTE } + uiState.documentNotes.size
                                            Text(
                                                "$bookmarkCount bookmark${if (bookmarkCount == 1) "" else "s"} • $noteCount note${if (noteCount == 1) "" else "s"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                        Button(
                                            onClick = onOpenSyncCenter,
                                            shape = VeritasPackStyle.chipShape(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Sync,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text("Sync", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val filterOptions = listOf("General", "Bookmarks", "Notes", "Vocab", "Flashcards", "History")
                                        filterOptions.forEach { option ->
                                            val active = annotationFilter == option
                                            val optionIcon = when (option) {
                                                "General" -> Icons.AutoMirrored.Outlined.Note
                                                "Bookmarks" -> Icons.Outlined.Bookmark
                                                "Notes" -> Icons.Outlined.EditNote
                                                "Vocab" -> Icons.Outlined.Book
                                                "Flashcards" -> Icons.Outlined.Book
                                                "History" -> Icons.Outlined.History
                                                else -> Icons.AutoMirrored.Outlined.Note
                                            }
                                            if (active) {
                                                Button(
                                                    onClick = { annotationFilter = option },
                                                    shape = VeritasPackStyle.chipShape(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = optionIcon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(option)
                                                }
                                            } else {
                                                OutlinedButton(
                                                    onClick = { annotationFilter = option },
                                                    shape = VeritasPackStyle.chipShape(),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = optionIcon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(option)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Row(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .height(56.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BottomNavItem(
                                selected = selectedHomeTab == VeritasHomeTab.HOME,
                                onClick = { selectedHomeTab = VeritasHomeTab.HOME },
                                icon = { color ->
                                    Icon(
                                        imageVector = Icons.Filled.Home,
                                        contentDescription = "Home",
                                        tint = color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = "Home"
                            )

                            BottomNavItem(
                                selected = selectedHomeTab == VeritasHomeTab.LIBRARY,
                                onClick = { selectedHomeTab = VeritasHomeTab.LIBRARY },
                                icon = { color ->
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Library",
                                        tint = color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = "Library"
                            )

                            BottomNavItem(
                                selected = selectedHomeTab == VeritasHomeTab.STUDY,
                                onClick = { selectedHomeTab = VeritasHomeTab.STUDY },
                                icon = { color ->
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Study",
                                        tint = color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = "Study"
                            )
                        }
                    }
                }
            }
        ) { homePadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = homePadding.calculateTopPadding(),
                        bottom = homePadding.calculateBottomPadding()
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedHomeTab == VeritasHomeTab.LIBRARY) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 2.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = listOf("All Docs", "Recent", "Favorites", "Unread")
                            options.forEach { option ->
                                val active = when (option) {
                                    "All Docs" -> statusFilter == "All"
                                    "Recent" -> false
                                    else -> statusFilter == option
                                }
                                if (active) {
                                    Button(
                                        onClick = { statusFilter = if (option == "All Docs") "All" else option },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(option, style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            statusFilter = if (option == "All Docs") "All" else option
                                            if (option == "Recent") {
                                                statusFilter = "All"
                                                sortMode = "Updated"
                                            }
                                        },
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(option, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(libraryListState) {
                                var pullDistance = 0f
                                detectVerticalDragGestures(
                                    onDragStart = { pullDistance = 0f },
                                    onVerticalDrag = { _, dragAmount ->
                                        val atTop = libraryListState.firstVisibleItemIndex == 0 && libraryListState.firstVisibleItemScrollOffset == 0
                                        if (atTop && dragAmount > 0f) pullDistance += dragAmount
                                    },
                                    onDragEnd = {
                                        val now = System.currentTimeMillis()
                                        if (pullDistance > 120f && now - lastMainPageRefreshAt > 1500L) {
                                            lastMainPageRefreshAt = now
                                            onRefreshMainPage()
                                        }
                                        pullDistance = 0f
                                    },
                                    onDragCancel = { pullDistance = 0f }
                                )
                            }
                            .padding(horizontal = 18.dp),
                        state = libraryListState,
                        contentPadding = PaddingValues(
                            top = if (selectedHomeTab == VeritasHomeTab.LIBRARY) 0.dp else 10.dp,
                            bottom = 22.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

        if (selectedHomeTab == VeritasHomeTab.HOME) {
            item {
                val tracker = uiState.readerTrackerSnapshot
                val hasHeroCard = tracker.currentStreak >= 2 || tracker.documentsCompletedThisMonth >= 5
                
                if (hasHeroCard) {
                    val streak = tracker.currentStreak
                    val streakHeadline = when {
                        tracker.documentsCompletedThisMonth >= 10 -> "Reading legend! 🏆"
                        tracker.documentsCompletedThisMonth >= 5 -> "Unstoppable reader! 📚"
                        streak >= 7 -> "You're doing amazing! 🌟"
                        tracker.documentsCompletedThisMonth >= 3 -> "Reading superstar! 📚"
                        tracker.weeklyUsageMillis > 120 * 60 * 1000L -> "In the zone! ⚡"
                        else -> "Keep it going! 🔥"
                    }
                    val streakSubtitle = when {
                        tracker.documentsCompletedThisMonth >= 10 -> "Outstanding achievement! You've completed ${tracker.documentsCompletedThisMonth} books this month."
                        tracker.documentsCompletedThisMonth >= 5 -> "Superb! You've finished ${tracker.documentsCompletedThisMonth} books this month."
                        streak >= 7 -> "Incredible $streak-day streak! You are a reading champion."
                        tracker.documentsCompletedThisMonth >= 3 -> "You've finished ${tracker.documentsCompletedThisMonth} books this month."
                        tracker.weeklyUsageMillis > 0 -> "You've read for ${(tracker.weeklyUsageMillis / 60000L)}m this week."
                        else -> "$streak-day streak! Keep up the momentum."
                    }
                    val streakPrimary = MaterialTheme.colorScheme.primary
                    val streakHsl = FloatArray(3)
                    android.graphics.Color.colorToHSV(streakPrimary.toArgb(), streakHsl)
                    val streakGradient = Brush.linearGradient(
                        listOf(
                            Color(android.graphics.Color.HSVToColor(floatArrayOf(streakHsl[0], (streakHsl[1] * 0.7f).coerceIn(0f, 1f), (streakHsl[2] * 1.15f).coerceIn(0f, 1f)))),
                            Color(android.graphics.Color.HSVToColor(floatArrayOf((streakHsl[0] + 15f) % 360f, streakHsl[1].coerceIn(0f, 1f), (streakHsl[2] * 0.85f).coerceIn(0f, 1f))))
                        )
                    )
                    val streakOnCard = if (streakPrimary.luminance() > 0.35f)
                        Color(0xFF1A1A2E) else Color.White

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(streakGradient)
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(streakOnCard.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("⚡", fontSize = 20.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            streakHeadline,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = streakOnCard
                                        )
                                        Text(
                                            streakSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = streakOnCard.copy(alpha = 0.75f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                val badgeText = when {
                                    tracker.documentsCompletedThisMonth >= 5 -> "${tracker.documentsCompletedThisMonth} books completed 🏆"
                                    else -> "${tracker.currentStreak}-day streak 🔥"
                                }

                                Box(
                                    modifier = Modifier
                                        .background(streakOnCard.copy(alpha = 0.15f), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = streakOnCard,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            HorizontalDivider(color = streakOnCard.copy(alpha = 0.15f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val weeklyMinutes = tracker.weeklyUsageMillis / 60000L
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${tracker.currentStreak}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = streakOnCard
                                        )
                                        Text(
                                            text = "Streak",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = streakOnCard.copy(alpha = 0.7f)
                                        )
                                    }

                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(streakOnCard.copy(alpha = 0.15f)))

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${weeklyMinutes}m",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = streakOnCard
                                        )
                                        Text(
                                            text = "This week",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = streakOnCard.copy(alpha = 0.7f)
                                        )
                                    }

                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(streakOnCard.copy(alpha = 0.15f)))

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${tracker.documentsCompletedThisMonth}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = streakOnCard
                                        )
                                        Text(
                                            text = "Done",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = streakOnCard.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            dashboardHeadline,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            dashboardSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (documents.isNotEmpty()) {
                item {
                    val collectionCount = remember(documents) { documents.map { it.collection.trim() }.filter { it.isNotBlank() }.distinct().size }
                    val sourceCount = remember(documents) { documents.map { it.sourceLabel.trim().ifBlank { "Text" } }.distinct().size }
                    val unreadCount = remember(documents) { documents.count { it.currentIndex <= 0 } }
                    
                    HomePanel(
                        totalCount = documents.size,
                        unreadCount = unreadCount,
                        inProgressCount = readingCount,
                        completedCount = completedCount,
                        favoriteCount = favoriteCount,
                        collectionCount = collectionCount,
                        sourceCount = sourceCount
                    )
                }
            }
        }

        // Library search and filters block removed from middle.

        if (selectedHomeTab == VeritasHomeTab.HOME) item {
            if (documents.isEmpty()) {
                EmbeddedOnboardingBlock(
                    onOpenFileBrowser = onOpenFileBrowser,
                    onPasteText = {
                        showImportSheet = true
                    }
                )
            } else {
                HomeQuickActions(
                    continueDocument = continueDocument,
                    documentCount = documents.size,
                    longestStreak = longestStreak,
                    onOpenContinue = { document -> onOpenDocument(document) },
                    onPlayPauseContinue = onPlayPauseContinue,
                    onClearContinue = { document -> onClearContinueDocument(document) },
                    onImportClick = { showImportSheet = true },
                    importMenuExpanded = false,
                    onDismissImportMenu = { },
                    onOpenFile = {
                        showImportSheet = true
                    },
                    onOpenFileBrowser = {
                        onOpenFileBrowser()
                    },
                    onOpenImportSettings = {
                        onAdvancedPdfImport()
                    },
                    onPasteText = {
                        showImportSheet = true
                    }
                )
            }
        }

        if (selectedHomeTab == VeritasHomeTab.HOME && queuedDocuments.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showQueue = true },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play queue",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Queue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Text("${queuedDocuments.size} queued reading${if (queuedDocuments.size == 1) "" else "s"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = onPlayQueue) { Text("Play") }
                    }
                }
            }
        }

        if (selectedHomeTab == VeritasHomeTab.STUDY) {
            when (annotationFilter) {
                "Bookmarks" -> {
                    if (bookmarksOnly.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.Bookmark,
                                title = "No bookmarks yet",
                                description = "Bookmark key passages while reading. Your bookmarked sentences will show up here.",
                                onGoToLibrary = { selectedHomeTab = VeritasHomeTab.LIBRARY },
                                onImportFile = onImportFile
                            )
                        }
                    } else {
                        bookmarksOnly.forEach { markedDocument ->
                            val doc = markedDocument.document
                            val docBookmarks = markedDocument.annotations.filter { it.type == AnnotationType.BOOKMARK }
                            val groups = groupBookmarks(doc, docBookmarks)
                            if (groups.isNotEmpty()) {
                                item(key = "marks-bookmarks-${doc.id}") {
                                    BookmarkDocumentCard(
                                        document = doc,
                                        groups = groups,
                                        sentenceTextLookup = { chunkIndex ->
                                            loadedDocSentences[doc.id]?.getOrNull(chunkIndex)
                                        },
                                        onOpenAt = { index -> onOpenDocumentAt(doc, index) },
                                        onDeleteAnnotations = onDeleteAnnotations
                                    )
                                }
                            }
                        }
                    }
                }
                "Notes" -> {
                    if (notesOnly.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.EditNote,
                                title = "No notes yet",
                                description = "Add notes to sentences or write general document notes while reading.",
                                onGoToLibrary = { selectedHomeTab = VeritasHomeTab.LIBRARY },
                                onImportFile = onImportFile
                            )
                        }
                    } else {
                        notesOnly.forEach { markedDocument ->
                            val doc = markedDocument.document
                            item(key = "marks-notes-${doc.id}") {
                                AnnotationDocumentCard(
                                    document = doc,
                                    annotations = markedDocument.annotations.filter { it.type == AnnotationType.NOTE },
                                    documentNote = markedDocument.documentNote,
                                    selectedKeys = selectedAnnotationKeys,
                                    selectionMode = annotationSelectionMode,
                                    onToggleDocumentNoteSelected = {
                                        val key = documentNoteStableKey(doc.id)
                                        selectedAnnotationKeys = if (key in selectedAnnotationKeys) {
                                            selectedAnnotationKeys - key
                                        } else {
                                            selectedAnnotationKeys + key
                                        }
                                    },
                                    onLongPressDocumentNote = {
                                        selectedAnnotationKeys = selectedAnnotationKeys + documentNoteStableKey(doc.id)
                                    },
                                    onToggleSelected = { annotation ->
                                        selectedAnnotationKeys = if (annotation.stableKey in selectedAnnotationKeys) {
                                            selectedAnnotationKeys - annotation.stableKey
                                        } else {
                                            selectedAnnotationKeys + annotation.stableKey
                                        }
                                    },
                                    onLongPressAnnotation = { annotation -> selectedAnnotationKeys = selectedAnnotationKeys + annotation.stableKey },
                                    onOpenDocumentNote = { onOpenDocumentAt(doc, doc.currentIndex.coerceAtLeast(0)) },
                                    onOpenAt = { index -> onOpenDocumentAt(doc, index) },
                                    sentenceTextLookup = { chunkIndex ->
                                        loadedDocSentences[doc.id]?.getOrNull(chunkIndex)
                                    },
                                    onDeleteAnnotations = onDeleteAnnotations
                                )
                            }
                        }
                    }
                }
                "Vocab" -> {
                    if (vocabDocs.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.Book,
                                title = "No vocabulary words yet",
                                description = "Select words in the reader and click Ask AI, Google Search, or Translate to automatically accumulate lookups here.",
                                onGoToLibrary = { selectedHomeTab = VeritasHomeTab.LIBRARY },
                                onImportFile = onImportFile
                            )
                        }
                    } else {
                        vocabDocs.forEach { (doc, _, entries) ->
                            val isExpanded = doc.id in expandedVocabDocIds
                            item(key = "vocab-doc-${doc.id}") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = VeritasPackStyle.compactShape(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedVocabDocIds = if (isExpanded) {
                                                            expandedVocabDocIds - doc.id
                                                        } else {
                                                            expandedVocabDocIds + doc.id
                                                        }
                                                    }
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Book,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(doc.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                    Text("Vocabulary (${entries.size} word${if (entries.size == 1) "" else "s"})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            if (isExpanded) {
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 14.dp))
                                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    entries.forEach { entry ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Text(entry.word, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                                    if (!entry.pronunciation.isNullOrBlank()) {
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = entry.pronunciation,
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = MaterialTheme.colorScheme.primary,
                                                                            fontStyle = FontStyle.Italic
                                                                        )
                                                                    }
                                                                }
                                                                Text(entry.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                if (!entry.contextSentence.isNullOrBlank()) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .padding(vertical = 4.dp)
                                                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                                            .padding(8.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = "\"${entry.contextSentence}\"",
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            fontStyle = FontStyle.Italic,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                }
                                                                Text(
                                                                    text = entry.source,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.clickable { onOpenDocumentAt(doc, entry.sentenceIndex) }
                                                                )
                                                            }
                                                            IconButton(onClick = { onRemoveVocabularyWord(doc.id, entry.word) }) {
                                                                Icon(Icons.Default.Delete, contentDescription = "Delete entry", tint = MaterialTheme.colorScheme.error)
                                                            }
                                                        }
                                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                "General" -> {
                    // Search, layout, and sort row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BasicTextField(
                                value = noteSearchQuery,
                                onValueChange = { noteSearchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
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
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (noteSearchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search notes...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                        if (noteSearchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { noteSearchQuery = "" },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            )

                            IconButton(
                                onClick = { isGridView = !isGridView },
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), VeritasPackStyle.chipShape())
                            ) {
                                Icon(
                                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Filled.GridView,
                                    contentDescription = "Toggle layout",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { noteSortOrder = if (noteSortOrder == "date") "title" else "date" },
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), VeritasPackStyle.chipShape())
                            ) {
                                Icon(
                                    imageVector = if (noteSortOrder == "date") Icons.Filled.SortByAlpha else Icons.Filled.Schedule,
                                    contentDescription = "Sort notes",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onWriteGeneralNote() },
                            shape = VeritasPackStyle.cardShape(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Write a note",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Personal notes, goals, or general thoughts",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }



                    if (processedGeneralNotes.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.AutoMirrored.Outlined.Note,
                                title = if (noteSearchQuery.isNotBlank()) "No matching notes" else "No general notes yet",
                                description = if (noteSearchQuery.isNotBlank()) "Try searching for a different keyword." else "Write personal notes, goals, or general thoughts here.",
                                onGoToLibrary = { selectedHomeTab = VeritasHomeTab.LIBRARY },
                                onImportFile = onImportFile
                            )
                        }
                    } else {
                        val pinnedNotes = processedGeneralNotes.filter { it.pinned }
                        val otherNotes = processedGeneralNotes.filter { !it.pinned }

                        @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                        @Composable
                        fun NoteCardItem(generalNote: GeneralNote) {
                            val cardBgColor = generalNote.color?.let { Color(android.graphics.Color.parseColor(it)) }
                                ?: MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                            val onCardColor = if (generalNote.color != null) Color(0xFF1E293B) else MaterialTheme.colorScheme.onSurface
                            // Keep-style quick actions: long-press for color, pin, share, and
                            // delete without opening the editor.
                            var showNoteMenu by remember { mutableStateOf(false) }
                            val noteShareContext = LocalContext.current

                            Box {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onEditGeneralNote(generalNote) },
                                        onLongClick = { showNoteMenu = true }
                                    )
                                    .padding(vertical = 4.dp),
                                shape = VeritasPackStyle.compactShape(),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (generalNote.title.isNotBlank()) {
                                            Text(
                                                generalNote.title, 
                                                fontWeight = FontWeight.Bold, 
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = onCardColor,
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                        
                                        IconButton(
                                            onClick = { onToggleGeneralNotePin(generalNote.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (generalNote.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                                contentDescription = "Pin Note",
                                                tint = if (generalNote.pinned) MaterialTheme.colorScheme.primary else onCardColor.copy(alpha = 0.5f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (generalNote.isChecklist) {
                                        val items = generalNote.content.split("\n").filter { it.isNotBlank() }.take(4).map { line ->
                                            val checked = line.startsWith("[x]")
                                            val text = line.removePrefix("[ ] ").removePrefix("[x] ")
                                            checked to text
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            items.forEach { (checked, text) ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (checked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                                        contentDescription = null,
                                                        tint = onCardColor.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = text,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = if (checked) onCardColor.copy(alpha = 0.5f) else onCardColor,
                                                            textDecoration = if (checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            if (generalNote.content.split("\n").filter { it.isNotBlank() }.size > 4) {
                                                Text(
                                                    text = "+ more items",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = onCardColor.copy(alpha = 0.5f),
                                                    modifier = Modifier.padding(start = 20.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = renderMarkdown(generalNote.content),
                                            maxLines = 5,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = onCardColor
                                        )
                                    }

                                    val locale = LocalConfiguration.current.locales[0]
                                    val updatedTime = remember(generalNote.updatedAt, locale) {
                                        SimpleDateFormat("dd MMM, HH:mm", locale).format(Date(generalNote.updatedAt))
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Updated $updatedTime",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onCardColor.copy(alpha = 0.5f)
                                        )
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (!generalNote.imageUrl.isNullOrBlank()) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = "Image attachment",
                                                    tint = onCardColor.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            if (!generalNote.audioUrl.isNullOrBlank()) {
                                                Icon(
                                                    imageVector = Icons.Default.Mic,
                                                    contentDescription = "Audio attachment",
                                                    tint = onCardColor.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            DropdownMenu(expanded = showNoteMenu, onDismissRequest = { showNoteMenu = false }) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val notePresets = listOf(null, "#FFF59D", "#A5D6A7", "#90CAF9", "#F48FB1", "#FFCC80")
                                    notePresets.forEach { hex ->
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    hex?.let { Color(android.graphics.Color.parseColor(it)) }
                                                        ?: MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                                .clickable {
                                                    onChangeGeneralNoteColor(generalNote.id, hex)
                                                    showNoteMenu = false
                                                }
                                        )
                                    }
                                }
                                DropdownMenuItem(
                                    text = { Text(if (generalNote.pinned) "Unpin" else "Pin") },
                                    leadingIcon = { Icon(if (generalNote.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, contentDescription = null) },
                                    onClick = { showNoteMenu = false; onToggleGeneralNotePin(generalNote.id) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share") },
                                    leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                                    onClick = { showNoteMenu = false; shareGeneralNote(noteShareContext, generalNote) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                    onClick = { showNoteMenu = false; onDeleteGeneralNote(generalNote.id) }
                                )
                            }
                            }
                        }

                        // Render Pinned Notes
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "PINNED",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                            if (isGridView) {
                                val chunked = pinnedNotes.chunked(2)
                                chunked.forEach { rowNotes ->
                                    item(key = "general-note-row-pinned-${rowNotes.first().id}") {
                                        Row(
                                            modifier = Modifier.animateItem().fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                NoteCardItem(rowNotes.first())
                                            }
                                            if (rowNotes.size > 1) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    NoteCardItem(rowNotes[1])
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            } else {
                                pinnedNotes.forEach { note ->
                                    item(key = "general-note-list-pinned-${note.id}") {
                                        Box(modifier = Modifier.animateItem()) { NoteCardItem(note) }
                                    }
                                }
                            }
                        }

                        // Render Other Notes
                        if (otherNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (pinnedNotes.isNotEmpty()) "OTHERS" else "NOTES",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                            if (isGridView) {
                                val chunked = otherNotes.chunked(2)
                                chunked.forEach { rowNotes ->
                                    item(key = "general-note-row-other-${rowNotes.first().id}") {
                                        Row(
                                            modifier = Modifier.animateItem().fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                NoteCardItem(rowNotes.first())
                                            }
                                            if (rowNotes.size > 1) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    NoteCardItem(rowNotes[1])
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            } else {
                                otherNotes.forEach { note ->
                                    item(key = "general-note-list-other-${note.id}") {
                                        Box(modifier = Modifier.animateItem()) { NoteCardItem(note) }
                                    }
                                }
                            }
                        }
                    }
                }
                "Flashcards" -> {
                    if (cards.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.Book,
                                title = "No flashcards yet",
                                description = "Open a document, go to 'Study Tools', and click 'Create flashcards'. Once generated, import them to your deck to start reviewing!",
                                onGoToLibrary = { selectedHomeTab = VeritasHomeTab.LIBRARY },
                                onImportFile = onImportFile
                            )
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = VeritasPackStyle.cardShape(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Book,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Spaced Repetition Deck",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                "${dueCards.size} card${if (dueCards.size == 1) "" else "s"} due out of ${cards.size} total",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { activeReviewDeck = dueCards },
                                        enabled = dueCards.isNotEmpty(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = VeritasPackStyle.chipShape()
                                    ) {
                                        Text(if (dueCards.isNotEmpty()) "Review Now (${dueCards.size})" else "All caught up!")
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "All Deck Cards",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        cards.forEach { card ->
                            item(key = "flashcard-item-${card.id}") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = VeritasPackStyle.compactShape(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = card.front,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = card.back,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                val isDue = card.nextReviewTime <= System.currentTimeMillis()
                                                val badgeColor = if (isDue) Color(0xFFEF4444) else Color(0xFF29B6F6)
                                                val badgeText = if (isDue) "Due" else {
                                                    val diff = card.nextReviewTime - System.currentTimeMillis()
                                                    val days = (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0L)
                                                    if (days == 0L) "Due later today" else "Due in $days d"
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = badgeColor.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = badgeText,
                                                        color = badgeColor,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "Repetitions: ${card.repetitions}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }

                                        IconButton(onClick = { onDeleteFlashcard(card.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete flashcard",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "History" -> {
                    val readingHistory = uiState.readingHistory
                    if (readingHistory.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.History,
                                title = "No reading history yet",
                                description = "Documents you read will show up here.",
                                onGoToLibrary = { selectedHomeTab = VeritasHomeTab.LIBRARY },
                                onImportFile = onImportFile
                            )
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recent history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                TextButton(onClick = onClearReadingHistory) {
                                    Text("Clear all", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        readingHistory.forEach { historyEntry ->
                            val doc = uiState.documents.firstOrNull { it.id == historyEntry.documentId }
                            item(key = "history-entry-${historyEntry.documentId}-${historyEntry.openedAt}") {
                                val isRemoved = doc == null
                                val progress = if (historyEntry.chunkCount > 0)
                                    (historyEntry.currentIndex.toFloat() / historyEntry.chunkCount).coerceIn(0f, 1f)
                                else 0f

                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { newVal ->
                                        if (newVal == SwipeToDismissBoxValue.EndToStart) {
                                            onRemoveReadingHistoryEntry(historyEntry.documentId)
                                            true
                                        } else false
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 4.dp)
                                                .clip(VeritasPackStyle.compactShape())
                                                .background(MaterialTheme.colorScheme.errorContainer),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remove from history",
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(end = 20.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.animateItem()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (!isRemoved) {
                                                    Modifier.clickable {
                                                        onOpenDocumentAt(doc!!, historyEntry.currentIndex)
                                                    }
                                                } else Modifier
                                            ),
                                        shape = VeritasPackStyle.compactShape(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isRemoved) {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            } else {
                                                MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                                            }
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isRemoved) {
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            }
                                        )
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Text(
                                                        text = historyEntry.title,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isRemoved) {
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        },
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = if (isRemoved) "Removed" else "Sentence ${historyEntry.currentIndex + 1} of ${historyEntry.chunkCount}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (isRemoved) {
                                                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        }
                                                    )
                                                    val locale = LocalConfiguration.current.locales[0]
                                                    val openedTime = remember(historyEntry.openedAt, locale) {
                                                        SimpleDateFormat("dd MMM, HH:mm", locale).format(Date(historyEntry.openedAt))
                                                    }
                                                    Text(
                                                        text = "Opened $openedTime",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isRemoved) 0.3f else 0.5f)
                                                    )
                                                }

                                                if (isRemoved) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "Removed",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Filled.ChevronRight,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }

                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                                color = if (isRemoved) {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
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

        if (selectedHomeTab == VeritasHomeTab.LIBRARY) item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (selectionMode) {
                            "${selectedDocumentIds.size} selected • ${visibleDocuments.size} showing"
                        } else {
                            "${documents.size} total • $readingCount in progress • $completedCount completed"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (selectionMode) {
                    Box {
                        TextButton(onClick = { showBatchMenu = true }) {
                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Batch actions")
                        }
                        DropdownMenu(expanded = showBatchMenu, onDismissRequest = { showBatchMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("All") },
                                onClick = {
                                    selectedDocumentIds = visibleDocuments.map { it.id }.toSet()
                                    showBatchMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showBatchMenu = false
                                    confirmBatchDelete = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add to favorites") },
                                onClick = {
                                    onBatchFavoriteDocuments(selectedDocumentIds)
                                    selectedDocumentIds = emptySet()
                                    showBatchMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add to Queue") },
                                onClick = {
                                    onBatchQueueDocuments(selectedDocumentIds)
                                    selectedDocumentIds = emptySet()
                                    showBatchMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Move to collection") },
                                onClick = {
                                    batchCollectionDraft = ""
                                    showBatchMenu = false
                                    showBatchCollectionDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cancel") },
                                onClick = {
                                    selectedDocumentIds = emptySet()
                                    showBatchMenu = false
                                }
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SoftChip(sortMode)
                        Box {
                            TextButton(onClick = { showLibraryViewMenu = true }) { Text("${libraryViewMode.icon} ⋮") }
                            DropdownMenu(expanded = showLibraryViewMenu, onDismissRequest = { showLibraryViewMenu = false }) {
                                LibraryViewMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text("${mode.icon} ${mode.label}") },
                                        onClick = {
                                            libraryViewMode = mode
                                            libraryPrefs.edit {
                                                putString("library_view_mode", mode.name)
                                            }
                                            showLibraryViewMenu = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                FeatureDropdownMenuItem(
                    feature = libraryFeature(VeritasFeatureId.READING_LISTS),
                    label = "Reading lists",
                    onClick = {
                        showLibraryViewMenu = false
                        onOpenReadingLists()
                    }
                )
                FeatureDropdownMenuItem(
                    feature = libraryFeature(VeritasFeatureId.READING_HISTORY),
                    label = "Reading history",
                                    onClick = {
                                        showLibraryViewMenu = false
                                        onOpenReadingHistory()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedHomeTab == VeritasHomeTab.LIBRARY && documents.isEmpty()) {
            item { EmptyLibraryCard(onImportFile = onImportFile) }
        } else if (selectedHomeTab == VeritasHomeTab.LIBRARY && visibleDocuments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No matching readings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text("Clear the search or change filters to see more saved readings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(
                            onClick = {
                                libraryQuery = ""
                                statusFilter = "All"
                                sourceFilter = "All"
                                collectionFilter = "All"
                                readingListFilter = "All"
                            }
                        ) {
                            Text("Clear filters")
                        }
                    }
                }
            }
        } else if (selectedHomeTab == VeritasHomeTab.LIBRARY) {
            if (libraryViewMode == LibraryViewMode.TILES) {
                itemsIndexed(visibleDocuments.chunked(columnCount), key = { index, row -> row.joinToString("-") { it.id }.ifBlank { "row-$index" } }) { _, rowDocs ->
                    Row(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowDocs.forEach { doc ->
                            DocumentTileCard(
                                document = doc,
                                isQueued = isQueued(doc),
                                selectionMode = selectionMode,
                                selected = doc.id in selectedDocumentIds,
                                onOpen = { onOpenDocument(doc) },
                                onLongPress = { selectedDocumentIds = selectedDocumentIds + doc.id },
                                onToggleSelected = {
                                    selectedDocumentIds = if (doc.id in selectedDocumentIds) selectedDocumentIds - doc.id else selectedDocumentIds + doc.id
                                },
                                onDelete = { onDeleteDocument(doc) },
                                onToggleQueue = { onToggleQueue(doc) },
                                onToggleFavorite = { onToggleFavorite(doc) },
                                onRename = { onRenameDocument(doc) },
                                onSetCollection = { onSetCollection(doc) },
                                onShowDetails = { onShowDetails(doc) },
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (doc == visibleDocuments.firstOrNull()) {
                                            Modifier.onGloballyPositioned { OnboardingController.updateBounds("document_card_0", it) }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                onManageLists = { manageListsDocument = doc }
                            )
                        }
                        val remainder = columnCount - rowDocs.size
                        repeat(remainder) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                itemsIndexed(visibleDocuments, key = { _, doc -> doc.id }) { _, doc ->
                    DocumentCard(
                        document = doc,
                        isQueued = isQueued(doc),
                        viewMode = libraryViewMode,
                        selectionMode = selectionMode,
                        selected = doc.id in selectedDocumentIds,
                        onOpen = { onOpenDocument(doc) },
                        onLongPress = { selectedDocumentIds = selectedDocumentIds + doc.id },
                        onToggleSelected = {
                            selectedDocumentIds = if (doc.id in selectedDocumentIds) {
                                selectedDocumentIds - doc.id
                            } else {
                                selectedDocumentIds + doc.id
                            }
                        },
                        onDelete = { onDeleteDocument(doc) },
                        onToggleQueue = { onToggleQueue(doc) },
                        onToggleFavorite = { onToggleFavorite(doc) },
                        onRename = { onRenameDocument(doc) },
                        onSetCollection = { onSetCollection(doc) },
                        onShowDetails = { onShowDetails(doc) },
                        onManageLists = { manageListsDocument = doc },
                        modifier = if (doc == visibleDocuments.firstOrNull()) {
                            Modifier.animateItem().onGloballyPositioned { OnboardingController.updateBounds("document_card_0", it) }
                        } else {
                            Modifier.animateItem()
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(22.dp)) }
    }
                }
            }
        }
    }
}


@Composable
private fun HomeSidebarDialog(
    name: String,
    snapshot: ReaderTrackerSnapshot,
    onDismiss: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.36f))
                    .clickable { onDismiss() }
            )
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(318.dp),
                shape = RoundedCornerShape(
                    topEnd = 24.dp,
                    bottomEnd = 24.dp,
                    topStart = 0.dp,
                    bottomStart = 0.dp
                ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(compact = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Veritas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                    ReaderTrackerSidebarCard(snapshot = snapshot, onOpenStats = onOpenStats)
                    HorizontalDivider()
                    SidebarAction("Library", "Saved readings and filters", Icons.AutoMirrored.Filled.LibraryBooks, onOpenLibrary)
                    SidebarAction("Settings", "Reader, voice, import, AI, and backup", Icons.Filled.Settings, onOpenSettings)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "All stats are stored locally on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderTrackerSidebarCard(snapshot: ReaderTrackerSnapshot, onOpenStats: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reading Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("Quick stats", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onOpenStats) { Text("View") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CompactStat(value = "${snapshot.currentStreak}", label = "Streak", modifier = Modifier.weight(1f))
                CompactStat(value = "${snapshot.longestStreak}", label = "Best", modifier = Modifier.weight(1f))
            }
            Text("Reading time", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            WeeklyReadingBarsPager(snapshot.weeklyHistory, barHeight = 56.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CompactStat(value = formatTrackerDuration(snapshot.weeklyUsageMillis), label = "Total", modifier = Modifier.weight(1f))
                CompactStat(value = formatTrackerDuration(snapshot.weeklyAverageMillis), label = "Average", modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CompactStat(value = "${snapshot.documentsReadThisWeek}", label = "Read", modifier = Modifier.weight(1f))
                CompactStat(value = "${snapshot.documentsCompletedThisMonth}", label = "Done", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReadingStatsDashboardDialog(
    snapshot: ReaderTrackerSnapshot,
    documents: List<SavedDocument>,
    documentReadingTimes: Map<String, Long>,
    onDismiss: () -> Unit
) {
    // Animation for stats count-up
    var targetCurrentStreak by remember { mutableStateOf(0) }
    var targetLongestStreak by remember { mutableStateOf(0) }
    var prevCurrentStreak by remember { mutableStateOf(0) }
    var prevLongestStreak by remember { mutableStateOf(0) }
    LaunchedEffect(snapshot) {
        delay(100)
        targetCurrentStreak = snapshot.currentStreak
        targetLongestStreak = snapshot.longestStreak
    }
    // Scale the roll-up duration with the delta (~70ms per step) so a +1 increment animates
    // quickly while the initial 0→N count-up still reads as a roll.
    val currentStreakDuration = (kotlin.math.abs(targetCurrentStreak - prevCurrentStreak) * 70).coerceIn(220, 650)
    val longestStreakDuration = (kotlin.math.abs(targetLongestStreak - prevLongestStreak) * 70).coerceIn(220, 650)
    val currentStreakAnimated by animateIntAsState(
        targetValue = targetCurrentStreak,
        animationSpec = tween(durationMillis = currentStreakDuration, easing = FastOutSlowInEasing)
    )
    val longestStreakAnimated by animateIntAsState(
        targetValue = targetLongestStreak,
        animationSpec = tween(durationMillis = longestStreakDuration, easing = FastOutSlowInEasing)
    )
    LaunchedEffect(targetCurrentStreak) { prevCurrentStreak = targetCurrentStreak }
    LaunchedEffect(targetLongestStreak) { prevLongestStreak = targetLongestStreak }

    // Pulsing/floating emoji transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val pastedColor = MaterialTheme.colorScheme.inversePrimary
    val slidesColor = androidx.compose.ui.graphics.lerp(secondaryColor, errorColor, 0.5f)

    // Format Distribution slices — every document lands in exactly one bucket so the
    // chart total always matches the library size. E-Books (EPUB), Slide Decks (PPTX),
    // and Documents (Word/text/scanned) are split into their own slices.
    val formatSlices = remember(documents, primaryColor, secondaryColor, tertiaryColor, errorColor, pastedColor, slidesColor) {
        var pdfCount = 0
        var webCount = 0
        var ebookCount = 0
        var slidesCount = 0
        var docCount = 0
        var pastedCount = 0
        documents.forEach { doc ->
            val mime = doc.originalMimeType.lowercase(Locale.US)
            val title = doc.title.lowercase(Locale.US)
            val label = doc.sourceLabel.lowercase(Locale.US)
            when {
                // sourceLabel is the explicit, reliable classifier set at import time
                // ("PDF", "DOCX", "PPTX", "EPUB", "OCR", "TXT", "Web", ...). Key off it
                // first, then fall back to mime type / filename so older records still bucket.
                label == "pdf" || mime.contains("pdf") || title.endsWith(".pdf") -> pdfCount++
                label.contains("web") || label.contains("http") || label.contains("article") ||
                    mime.contains("html") -> webCount++
                label == "epub" || mime.contains("epub") || title.endsWith(".epub") -> ebookCount++
                label == "pptx" || mime.contains("presentationml") || title.endsWith(".pptx") -> slidesCount++
                label in setOf("docx", "txt", "ocr") ||
                    mime.contains("word") || mime.contains("wordprocessingml") ||
                    mime.startsWith("image/") ||
                    title.endsWith(".docx") || title.endsWith(".txt") -> docCount++
                else -> pastedCount++
            }
        }
        listOf(
            DonutSlice(
                label = "PDF Documents",
                value = pdfCount.toFloat(),
                color = primaryColor,
                description = "PDFs imported from local file storage or other directories. Excellent for study outlines."
            ),
            DonutSlice(
                label = "Web Articles",
                value = webCount.toFloat(),
                color = secondaryColor,
                description = "Online articles, blogs, and papers saved via URL import. Perfect for quick news reading."
            ),
            DonutSlice(
                label = "E-Books",
                value = ebookCount.toFloat(),
                color = tertiaryColor,
                description = "EPUB e-books imported into your library."
            ),
            DonutSlice(
                label = "Slide Decks",
                value = slidesCount.toFloat(),
                color = slidesColor,
                description = "PowerPoint presentations read slide by slide with titles, bullets, and speaker notes."
            ),
            DonutSlice(
                label = "Documents",
                value = docCount.toFloat(),
                color = errorColor,
                description = "Word documents, text files, and scanned (OCR) documents."
            ),
            DonutSlice(
                label = "Pasted Text",
                value = pastedCount.toFloat(),
                color = pastedColor,
                description = "Text pasted directly into the reader interface or manually typed drafts."
            )
        ).filter { it.value > 0f }
    }

    // Time Allocation slices — real listening/reading time recorded per document for
    // the current month, so the chart resets monthly instead of stacking up forever.
    val timeSlices = remember(documents, documentReadingTimes, primaryColor, secondaryColor, tertiaryColor, errorColor) {
        val docTimes = documents.mapNotNull { doc ->
            val readingTime = documentReadingTimes[doc.id] ?: 0L
            if (readingTime > 0L) doc to readingTime else null
        }.sortedByDescending { it.second }

        val totalTime = docTimes.sumOf { it.second }

        if (docTimes.isEmpty()) {
            emptyList()
        } else {
            val top4 = docTimes.take(4)
            val othersTime = if (docTimes.size > 4) docTimes.drop(4).sumOf { it.second } else 0L
            
            val colors = listOf(
                primaryColor,
                secondaryColor,
                tertiaryColor,
                errorColor,
                Color.Gray
            )
            
            val list = mutableListOf<DonutSlice>()
            top4.forEachIndexed { idx, (doc, time) ->
                list.add(
                    DonutSlice(
                        label = doc.title,
                        value = time.toFloat() / 60000f, // convert to minutes
                        color = colors[idx % colors.size],
                        description = "You spent ${time / 60000} minutes reading this document. That's ${(time * 100f / totalTime.coerceAtLeast(1)).toInt()}% of your total time."
                    )
                )
            }
            if (othersTime > 0L) {
                list.add(
                    DonutSlice(
                        label = "Others",
                        value = othersTime.toFloat() / 60000f,
                        color = colors[4],
                        description = "All other documents combined account for ${othersTime / 60000} minutes of your reading sessions."
                    )
                )
            }
            list
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reading Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            Text("Your local reading rhythm", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onDismiss) { Text("Close") }
                    }
                }

                // Streaks & Stats Card (Glassmorphic design with gradient background)
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Streaks display
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                // Current Streak Card
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalFireDepartment,
                                            contentDescription = "Current streak",
                                            tint = Color(0xFFFF7043),
                                            modifier = Modifier.size(30.dp).scale(emojiScale)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$currentStreakAnimated",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Current Streak",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Longest Streak Card
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiEvents,
                                            contentDescription = "Longest streak",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(30.dp).scale(emojiScale)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$longestStreakAnimated",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Longest Streak",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Weekly Usage Trend (swipe to see previous weeks)
                            Text("Weekly Reading Time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            WeeklyReadingBarsPager(snapshot.weeklyHistory, barHeight = 96.dp)

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                BigStat(formatTrackerDuration(snapshot.weeklyUsageMillis), "This week", Modifier.weight(1f))
                                BigStat(formatTrackerDuration(snapshot.weeklyAverageMillis), "Daily avg", Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Heatmap and Distribution Donut Charts
                item {
                    CalendarHeatMap(snapshot.activeDateKeys)
                }

                item {
                    InteractiveDonutChart(
                        title = "Library Source Distribution",
                        slices = formatSlices,
                        totalLabel = "Readings",
                        titleIcon = Icons.Filled.PieChart
                    )
                }

                if (timeSlices.isNotEmpty()) {
                    item {
                        InteractiveDonutChart(
                            title = "Time Allocation — This Month",
                            slices = timeSlices,
                            totalLabel = "Minutes",
                            titleIcon = Icons.Filled.Timer
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        BigStat("${snapshot.documentsReadThisWeek}", "Docs read this week", Modifier.weight(1f))
                        BigStat("${snapshot.documentsCompletedThisMonth}", "Completed this month", Modifier.weight(1f))
                    }
                }

                item {
                    Text("Recent completions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }

                if (snapshot.recentCompletions.isEmpty()) {
                    item {
                        Text("Finish a document and it will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(snapshot.recentCompletions, key = { it.documentId }) { completion ->
                        Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(32.dp).size(22.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(completion.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                    Text(formatUpdated(completion.completedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Onboarding tour card: the dashboard dialog window covers the main spotlight
            // overlay, so the insights step renders its own card here.
            if (OnboardingController.activeStep == OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { OnboardingController.activeStep = OnboardingStep.INSIGHTS_SPOTLIGHT }) {
                                Text("Back")
                            }
                            Button(onClick = { OnboardingController.activeStep = OnboardingStep.DOCUMENT_SPOTLIGHT }) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun CalendarHeatMap(activeDateKeys: Set<String>) {
    val context = LocalContext.current
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    
    val months = remember {
        val list = mutableListOf<MonthData>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        
        for (m in 0..11) {
            cal.set(currentYear, m, 1)
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 7 = Sat
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            val days = mutableListOf<DayData?>()
            repeat(firstDayOfWeek - 1) {
                days.add(null)
            }
            for (d in 1..maxDays) {
                cal.set(currentYear, m, d)
                val dateStr = sdf.format(cal.time)
                days.add(DayData(dayOfMonth = d, dateKey = dateStr, timeMillis = cal.timeInMillis))
            }
            while (days.size % 7 != 0) {
                days.add(null)
            }
            list.add(MonthData(name = monthNames[m], days = days))
        }
        list
    }

    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var visibleMonths by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..12) {
            delay(30)
            visibleMonths = i
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Reading Heatmap — Year $currentYear",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            months.forEachIndexed { mIdx, month ->
                val alpha by animateFloatAsState(
                    targetValue = if (visibleMonths >= mIdx + 1) 1f else 0f,
                    animationSpec = tween(durationMillis = 400),
                    label = "monthAlpha"
                )

                if (visibleMonths >= mIdx + 1) {
                    // Sketch layout: month name on the left vertically centered against the
                    // grid; weekday letters run across the top of the day cells. The whole
                    // month block is centered in the card.
                    val cellSize = 26.dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { this.alpha = alpha },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = month.name.take(3),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
                                weekdays.forEach { day ->
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.width(cellSize),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            val chunkedWeeks = month.days.chunked(7)
                            chunkedWeeks.forEach { week ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    week.forEach { day ->
                                        if (day == null) {
                                            Box(modifier = Modifier.size(cellSize))
                                        } else {
                                            val isActive = activeDateKeys.contains(day.dateKey)
                                            val isFuture = day.dateKey > todayKey
                                            val color = when {
                                                isActive -> MaterialTheme.colorScheme.primary
                                                isFuture -> Color.Transparent
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            }
                                            val borderStroke = if (isFuture) {
                                                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                                            } else if (!isActive) {
                                                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                                            } else null

                                            Box(
                                                modifier = Modifier
                                                    .size(cellSize)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(color)
                                                    .then(
                                                        if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(4.dp))
                                                        else Modifier
                                                    )
                                                    .clickable {
                                                        if (!isFuture) {
                                                            val formattedDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date(day.timeMillis))
                                                            val msg = if (isActive) "Logged reading on $formattedDate! 📖" else "No activity logged on $formattedDate."
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
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
}

private data class MonthData(
    val name: String,
    val days: List<DayData?>
)

private data class DayData(
    val dayOfMonth: Int,
    val dateKey: String,
    val timeMillis: Long
)

@Composable
private fun InteractiveDonutChart(
    title: String,
    slices: List<DonutSlice>,
    totalLabel: String,
    modifier: Modifier = Modifier,
    titleIcon: ImageVector? = null
) {
    LocalContext.current
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val totalVal = slices.sumOf { it.value.toDouble() }.toFloat()

    val scaleFactors = slices.indices.map { idx ->
        animateFloatAsState(
            targetValue = if (selectedIndex == idx) 1.15f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "sliceScale_$idx"
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            titleIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val strokeWidthPx = with(density) { 14.dp.toPx() }
                
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(slices, totalVal) {
                            detectTapGestures { offset ->
                                if (totalVal > 0f) {
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val x = offset.x - centerX
                                    val y = offset.y - centerY
                                    val dist = Math.sqrt((x * x + y * y).toDouble()).toFloat()
                                    val radius = Math.min(size.width, size.height) / 2f
                                    
                                    if (dist in (radius - strokeWidthPx * 2.5f)..radius) {
                                        var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()
                                        angle = (angle + 90f + 360f) % 360f
                                        
                                        var currentAngle = 0f
                                        var found: Int? = null
                                        for (i in slices.indices) {
                                            val sweep = (slices[i].value / totalVal) * 360f
                                            if (angle in currentAngle..(currentAngle + sweep)) {
                                                found = i
                                                break
                                            }
                                            currentAngle += sweep
                                        }
                                        if (found != null) {
                                            selectedIndex = if (selectedIndex == found) null else found
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val radius = Math.min(size.width, size.height) / 2f - strokeWidthPx / 2f
                    
                    if (totalVal == 0f) {
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = strokeWidthPx)
                        )
                    } else {
                        var startAngle = -90f
                        slices.forEachIndexed { idx, slice ->
                            val sweepAngle = (slice.value / totalVal) * 360f
                            val scale = scaleFactors[idx].value
                            val strokeWidth = strokeWidthPx * (if (selectedIndex == idx) 1.3f else 1.0f)
                            val r = radius * scale
                            
                            drawArc(
                                color = slice.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(centerX - r, centerY - r),
                                size = Size(r * 2, r * 2),
                                style = Stroke(width = strokeWidth)
                            )
                            startAngle += sweepAngle
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (totalVal >= 1000f) "%.1fk".format(totalVal / 1000f) else "${totalVal.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = totalLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                slices.forEachIndexed { idx, slice ->
                    val isSelected = selectedIndex == idx
                    val percentage = if (totalVal > 0f) (slice.value * 100f / totalVal).toInt() else 0
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIndex = if (isSelected) null else idx }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 7.dp)
                                .background(slice.color, CircleShape)
                        )
                        Text(
                            text = slice.label,
                            style = if (isSelected) MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        selectedIndex?.let { idx ->
            val slice = slices[idx]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, slice.color.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = slice.color
                    )
                    Text(
                        text = slice.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color,
    val description: String
)

@Composable
private fun SidebarAction(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CompactStat(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BigStat(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

// Monday-based index (Mon=0 … Sun=6) for today, used to emphasise today's capsule.
private fun mondayBasedTodayIndex(): Int {
    val cal = Calendar.getInstance()
    return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
}

/**
 * Resolves a Long milliseconds value into a day-of-week label (Mon–Sun).
 * `weekStartMonday` is the epoch-ms of Monday 00:00 for the displayed week.
 */
private fun dayLabel(dayIndex: Int, weekStartMonday: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = weekStartMonday + dayIndex * 86_400_000L
    return SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        .format(Date(cal.timeInMillis))
}

/**
 * Floating tooltip shown above the selected day capsule.
 * Uses an `AnimatedVisibility` (fade + slide) for smooth entrance/exit.
 */
@Composable
private fun UsageTooltip(visible: Boolean, label: String, duration: String) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) +
                slideInVertically(tween(180)) { it / 2 },
        exit = fadeOut(tween(140)) +
               slideOutVertically(tween(140)) { it / 2 }
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
            tonalElevation = 6.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MiniWeekBars(
    values: List<Long>,
    height: androidx.compose.ui.unit.Dp = 64.dp,
    todayIndex: Int = -1,
    selectedIndex: Int = -1,
    onSelectDay: (Int) -> Unit = {},
    weekStartMonday: Long = 0L
) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val max = (values.maxOrNull()?.coerceAtLeast(1L) ?: 1L) * 1.25f
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val primary = MaterialTheme.colorScheme.primary
    val fillBrush = Brush.verticalGradient(
        listOf(lerp(primary, Color.White, 0.45f), primary)
    )
    val dimBrush = Brush.verticalGradient(
        listOf(lerp(primary, Color.White, 0.25f).copy(alpha = 0.7f), primary.copy(alpha = 0.7f))
    )
    val selectedBrush = Brush.verticalGradient(
        listOf(lerp(primary, Color.White, 0.6f), primary)
    )
    val areaAlpha = if (MaterialTheme.colorScheme.primary.luminance() > 0.5f) 0.12f else 0.18f
    Column(modifier = Modifier.fillMaxWidth()) {
        val fracs = values.map { v ->
            if (v > 0L) (v.toFloat() / max).coerceIn(0.14f, 1f) else 0f
        }
        val animatedFracs = fracs.mapIndexed { i, f ->
            animateFloatAsState(f, tween(550, easing = FastOutSlowInEasing), label = "areaFrac$i").value
        }
        Box(modifier = Modifier.fillMaxWidth().height(height)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (animatedFracs.size == 7 && size.width > 0f) {
                    val barWidth = size.width / 7f
                    val path = Path()
                    val points = animatedFracs.mapIndexed { i, frac ->
                        Offset(barWidth * i + barWidth / 2f, size.height * (1f - frac))
                    }
                    if (points.isNotEmpty()) {
                        path.moveTo(0f, size.height)
                        path.lineTo(0f, points.first().y)
                        path.lineTo(points.first().x, points.first().y)
                        for (k in 1 until points.size) {
                            val prev = points[k - 1]
                            val curr = points[k]
                            val cx = (prev.x + curr.x) / 2f
                            path.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                        }
                        path.lineTo(size.width, points.last().y)
                        path.lineTo(size.width, size.height)
                        path.close()
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(primary.copy(alpha = areaAlpha), Color.Transparent),
                                startY = 0f,
                                endY = size.height
                            )
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEachIndexed { i, value ->
                    val isToday = i == todayIndex
                    val isSelected = i == selectedIndex
                    val targetFrac = if (value > 0L) (value.toFloat() / max).coerceIn(0.14f, 1f) else 0f
                    val frac by animateFloatAsState(
                        targetValue = targetFrac,
                        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
                        label = "barFrac$i"
                    )
                    val dayDesc = if (weekStartMonday > 0L && value > 0L)
                        "${labels.getOrElse(i) { "" }}: ${formatTrackerDuration(value)}"
                    else labels.getOrElse(i) { "" }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = dayDesc },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 18.dp else 14.dp)
                                .height(height)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else trackColor
                                )
                                .clickable(
                                    enabled = todayIndex == -1 || i <= todayIndex,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onSelectDay(if (isSelected) -1 else i) },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (frac > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(frac)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            when {
                                                isSelected -> selectedBrush
                                                isToday -> fillBrush
                                                else -> dimBrush
                                            }
                                        )
                                )
                            }
                        }
                        Text(
                            labels.getOrElse(i) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Swipeable weekly chart: pages through `history` (oldest→newest), defaulting to the current
 * (last) week, with a small caption naming the week and its total. Falls back to a single
 * static week when no history is available. Tapping a bar shows a floating usage tooltip.
 */
@Composable
private fun WeeklyReadingBarsPager(
    history: List<WeekBars>,
    modifier: Modifier = Modifier,
    barHeight: androidx.compose.ui.unit.Dp = 64.dp
) {
    if (history.isEmpty()) {
        MiniWeekBars(List(7) { 0L }, height = barHeight)
        return
    }
    val pagerState = rememberPagerState(initialPage = history.lastIndex) { history.size }
    val todayIndex = remember { mondayBasedTodayIndex() }
    var selectedBarIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(pagerState.currentPage) { selectedBarIndex = -1 }

    val visibleWeek = history[pagerState.currentPage]
    val chartDescription = "Weekly reading chart. ${visibleWeek.label}: " +
        "${formatTrackerDuration(visibleWeek.totalMillis)} total. Swipe left or right to change week."

    val weekStartMonday = remember(pagerState.currentPage) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weeksBack = history.lastIndex - pagerState.currentPage
        cal.add(Calendar.WEEK_OF_YEAR, -weeksBack)
        cal.timeInMillis
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = chartDescription }
    ) {
        val width = maxWidth
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tooltipVisible = selectedBarIndex >= 0 && selectedBarIndex < visibleWeek.values.size
            val tooltipLabel = if (tooltipVisible && weekStartMonday > 0L)
                dayLabel(selectedBarIndex, weekStartMonday) else ""
            val tooltipDuration = if (tooltipVisible)
                formatTrackerDuration(visibleWeek.values.getOrElse(selectedBarIndex) { 0L }) else ""

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                HorizontalPager(state = pagerState) { page ->
                    val week = history[page]
                    MiniWeekBars(
                        values = week.values,
                        height = barHeight,
                        todayIndex = if (week.isCurrentWeek) todayIndex else -1,
                        selectedIndex = if (page == pagerState.currentPage) selectedBarIndex else -1,
                        onSelectDay = { idx -> selectedBarIndex = idx },
                        weekStartMonday = weekStartMonday
                    )
                }

                if (tooltipVisible) {
                    val colWidth = (width - 60.dp) / 7
                    val colCenter = (colWidth * selectedBarIndex) + (10.dp * selectedBarIndex) + (colWidth / 2)
                    Box(
                        modifier = Modifier
                            .offset(x = colCenter, y = (-38).dp)
                            .width(0.dp)
                            .wrapContentWidth(align = Alignment.CenterHorizontally, unbounded = true)
                    ) {
                        UsageTooltip(
                            visible = tooltipVisible,
                            label = tooltipLabel,
                            duration = tooltipDuration
                        )
                    }
                }
            }

            val week = history[pagerState.currentPage]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    week.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "‹ swipe ›",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun shareGeneralNote(context: Context, note: GeneralNote) {
    val body = if (note.isChecklist) {
        note.content.lineSequence().joinToString("\n") { line ->
            when {
                line.startsWith("[x]") -> "☑ " + line.removePrefix("[x]").trim()
                line.startsWith("[ ]") -> "☐ " + line.removePrefix("[ ]").trim()
                else -> line
            }
        }
    } else {
        RichTextFormatter.stripMarkup(note.content)
    }
    val plain = buildString {
        if (note.title.isNotBlank()) {
            append(note.title)
            append("\n\n")
        }
        append(body)
    }.trim()
    if (plain.isBlank()) {
        Toast.makeText(context, "Nothing to share yet", Toast.LENGTH_SHORT).show()
        return
    }
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, note.title.ifBlank { "Veritas note" })
        putExtra(android.content.Intent.EXTRA_TEXT, plain)
    }
    runCatching { context.startActivity(android.content.Intent.createChooser(send, "Share note")) }
}

private fun formatTrackerDuration(millis: Long): String {
    val minutes = (millis / 60_000L).coerceAtLeast(0L)
    val hours = minutes / 60L
    val remaining = minutes % 60L
    return when {
        hours > 0L && remaining > 0L -> "${hours}h ${remaining}m"
        hours > 0L -> "${hours}h"
        else -> "${remaining}m"
    }
}

@Composable
private fun HomeQuickActions(
    continueDocument: SavedDocument?,
    documentCount: Int,
    longestStreak: Int,
    onOpenContinue: (SavedDocument) -> Unit,
    onPlayPauseContinue: (SavedDocument) -> Unit,
    onClearContinue: (SavedDocument) -> Unit,
    onImportClick: () -> Unit,
    importMenuExpanded: Boolean,
    onDismissImportMenu: () -> Unit,
    onOpenFile: () -> Unit,
    onOpenFileBrowser: () -> Unit,
    onOpenImportSettings: () -> Unit,
    onPasteText: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val disabled = continueDocument == null
        val context = LocalContext.current
        val coverFile = remember(continueDocument?.id) { continueDocument?.id?.let { CoverExtractor.coverFile(context, it) } }
        val coverBitmap = remember(coverFile) {
            coverFile?.let { file ->
                runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
            }
        }

        val isFirstDay = longestStreak <= 1
        val headerTitle = if (continueDocument != null && isFirstDay) "First day of reading" else "Continue reading"

        Text(
            text = headerTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                // Always clickable so an empty card still gives feedback instead of feeling
                // broken: when there's nothing to resume, guide the user with a toast.
                .clickable {
                    if (continueDocument != null) {
                        onOpenContinue(continueDocument)
                    } else {
                        val msg = if (documentCount == 0) {
                            "No documents yet — tap Add to import a file or paste text."
                        } else {
                            "No recent reading yet — open a document from your Library to resume here."
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
            shape = VeritasPackStyle.cardShape(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (disabled) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f * VeritasPackStyle.surfaceAlpha())
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Larger Premium Cover Thumbnail
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(96.dp)
                        .clip(VeritasPackStyle.compactShape())
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverBitmap != null) {
                        Image(
                            bitmap = coverBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🎧", fontSize = 28.sp)
                    }
                }
                
                // Detailed Information
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (continueDocument == null) {
                        Text(
                            text = "Resume reading",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (documentCount == 0) "Open a file to start reading" else "Pick a document from your library to begin.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = continueDocument.title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        val progressPercentVal = progressPercent(continueDocument)
                        
                        Text(
                            text = "Sentence ${continueDocument.currentIndex + 1} of ${continueDocument.chunkCount} • $progressPercentVal% read",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Inline Progress Bar
                        LinearProgressIndicator(
                            progress = { progressFraction(continueDocument) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    }
                }
                
                // Audio controls inline
                if (continueDocument != null) {
                    val isActiveAndPlaying = PlaybackStateStore.activeDocumentId == continueDocument.id && PlaybackStateStore.isPlaying
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { onPlayPauseContinue(continueDocument) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isActiveAndPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isActiveAndPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        IconButton(
                            onClick = { onClearContinue(continueDocument) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Add content",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Box {
                HomeActionRow(
                    icon = Icons.Filled.Add,
                    title = "Import file",
                    body = "Upload PDF, EPUB, DOCX, PPTX, TXT, or HTML",
                    iconBackground = Color(0xFFF0F3FF),
                    iconForeground = Color(0xFF7C6FFF),
                    onClick = onImportClick
                )
                DropdownMenu(expanded = importMenuExpanded, onDismissRequest = onDismissImportMenu) {
                    DropdownMenuItem(text = { Text("Open file") }, onClick = onOpenFile)
                    DropdownMenuItem(text = { Text("File browser") }, onClick = onOpenFileBrowser)
                    DropdownMenuItem(text = { Text("Import settings") }, onClick = onOpenImportSettings)
                    DropdownMenuItem(text = { Text("Paste text or URL") }, onClick = onPasteText)
                }
            }
        }
    }
}

@Composable
private fun HomeActionRow(
    icon: ImageVector,
    title: String,
    body: String,
    iconBackground: Color,
    iconForeground: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconForeground,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}






@Composable
fun HomePanel(
    totalCount: Int,
    unreadCount: Int,
    inProgressCount: Int,
    completedCount: Int,
    favoriteCount: Int,
    collectionCount: Int,
    sourceCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Reading Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatTile(value = "$totalCount", label = "Saved", modifier = Modifier.width(84.dp))
            StatTile(value = "$unreadCount", label = "Unread", modifier = Modifier.width(84.dp))
            StatTile(value = "$inProgressCount", label = "Reading", modifier = Modifier.width(84.dp))
            StatTile(value = "$completedCount", label = "Done", modifier = Modifier.width(72.dp))
            StatTile(value = "$favoriteCount", label = "Starred", modifier = Modifier.width(72.dp))
            StatTile(value = "$collectionCount", label = "Collections", modifier = Modifier.width(80.dp))
            StatTile(value = "$sourceCount", label = "Formats", modifier = Modifier.width(72.dp))
        }
    }
}


@Composable
private fun AnnotationDocumentCard(
    document: SavedDocument,
    annotations: List<ReaderAnnotation>,
    documentNote: String,
    selectedKeys: Set<String>,
    selectionMode: Boolean,
    onToggleDocumentNoteSelected: () -> Unit,
    onLongPressDocumentNote: () -> Unit,
    onToggleSelected: (ReaderAnnotation) -> Unit,
    onLongPressAnnotation: (ReaderAnnotation) -> Unit,
    onOpenDocumentNote: () -> Unit,
    onOpenAt: (Int) -> Unit,
    sentenceTextLookup: (Int) -> String?,
    onDeleteAnnotations: (Set<String>) -> Unit
) {
    val hasDocumentNote = documentNote.isNotBlank()
    val documentNoteKey = documentNoteStableKey(document.id)
    val selectedDocumentNote = documentNoteKey in selectedKeys
    val noteAnnotations = remember(annotations) { annotations.filter { it.type == AnnotationType.NOTE } }
    
    var expanded by rememberSaveable(document.id) { mutableStateOf(false) }
    var expandedNoteKeys by remember { mutableStateOf(setOf<String>()) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${noteAnnotations.size + if (hasDocumentNote) 1 else 0} note${if (noteAnnotations.size + (if (hasDocumentNote) 1 else 0) == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 14.dp))
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasDocumentNote || noteAnnotations.isNotEmpty()) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (hasDocumentNote) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (selectedDocumentNote) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .pointerInput(selectionMode, selectedDocumentNote, documentNoteKey) {
                                        detectTapGestures(
                                            onLongPress = { onLongPressDocumentNote() },
                                            onTap = {
                                                if (selectionMode) onToggleDocumentNoteSelected() else onOpenDocumentNote()
                                            }
                                        )
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (selectedDocumentNote) Color(0xFFE2F0D9) else Color(0xFFFFF7F0), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedDocumentNote) Icons.Filled.Check else Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = if (selectedDocumentNote) Color(0xFF137333) else Color(0xFFF2994A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("General document note", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(documentNote, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                                        .clickable { if (selectionMode) onToggleDocumentNoteSelected() else onOpenDocumentNote() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (selectionMode) (if (selectedDocumentNote) "Selected" else "Select") else "Open",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        val noteGroups = remember(noteAnnotations) { groupNotes(document, noteAnnotations) }
                        
                        noteGroups.forEach { noteGroup ->
                            val keys = noteGroup.annotations.map { it.stableKey }.toSet()
                            val selected = keys.any { it in selectedKeys }
                            val isExpanded = noteGroup.id in expandedNoteKeys
                            var showNoteMenu by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .pointerInput(selectionMode, selected, noteGroup.id) {
                                        detectTapGestures(
                                            onLongPress = { 
                                                noteGroup.annotations.forEach { onLongPressAnnotation(it) }
                                            },
                                            onTap = {
                                                if (selectionMode) {
                                                    noteGroup.annotations.forEach { onToggleSelected(it) }
                                                } else {
                                                    expandedNoteKeys = if (isExpanded) {
                                                        expandedNoteKeys - noteGroup.id
                                                    } else {
                                                        expandedNoteKeys + noteGroup.id
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    .padding(2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    if (!isExpanded) {
                                        val firstAnn = noteGroup.annotations.firstOrNull()
                                        val firstText = firstAnn?.let { sentenceTextLookup(it.chunkIndex) } ?: ""
                                        val truncatedText = if (firstText.length > 60) firstText.take(57) + "..." else firstText
                                        Text(
                                            text = truncatedText.ifBlank { "Note Entry" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = noteGroup.noteText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            noteGroup.annotations.forEach { ann ->
                                                val sentenceText = sentenceTextLookup(ann.chunkIndex) ?: ""
                                                Text(
                                                    text = sentenceText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = noteGroup.noteText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .clickable { onOpenAt(noteGroup.startSentence) }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Open in document ↗",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Box {
                                                IconButton(
                                                    onClick = { showNoteMenu = true },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.MoreVert,
                                                        contentDescription = "Note actions",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = showNoteMenu,
                                                    onDismissRequest = { showNoteMenu = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Copy") },
                                                        onClick = {
                                                            showNoteMenu = false
                                                            copyTextToClipboard(context, "Note", noteGroup.noteText)
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                                        onClick = {
                                                            showNoteMenu = false
                                                            onDeleteAnnotations(keys)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun QueueSection(
    queuedDocuments: List<SavedDocument>,
    onPlayQueue: () -> Unit,
    onOpenDocument: (SavedDocument) -> Unit,
    onMoveUp: (SavedDocument) -> Unit,
    onMoveDown: (SavedDocument) -> Unit,
    onRemove: (SavedDocument) -> Unit,
    onClearQueue: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Queue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(
                        if (queuedDocuments.isEmpty()) "Build a playlist from your library." else "${queuedDocuments.size} queued item${if (queuedDocuments.size == 1) "" else "s"} ready for continuous playback.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = onClearQueue, enabled = queuedDocuments.isNotEmpty()) { Text("Clear") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onPlayQueue, enabled = queuedDocuments.isNotEmpty()) { Text("Play") }
            }

            if (queuedDocuments.isEmpty()) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "Use Queue on any document card to add it here. Queue order controls what plays next in the background service.",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                queuedDocuments.forEachIndexed { index, document ->
                    QueueItemRow(
                        position = index + 1,
                        document = document,
                        canMoveUp = index > 0,
                        canMoveDown = index < queuedDocuments.lastIndex,
                        onOpen = { onOpenDocument(document) },
                        onMoveUp = { onMoveUp(document) },
                        onMoveDown = { onMoveDown(document) },
                        onRemove = { onRemove(document) }
                    )
                }
            }
        }
    }
}

@Composable
fun QueueItemRow(
    position: Int,
    document: SavedDocument,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val progress = progressFraction(document)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$position", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f).clickable { onOpen() }) {
                    Text(document.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Text("${document.sourceLabel} • ${progressPercent(document)}% • sentence ${document.currentIndex + 1}/${document.chunkCount.coerceAtLeast(1)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SourceBadge(document.sourceLabel)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onMoveUp, enabled = canMoveUp) { Text("Up") }
                OutlinedButton(onClick = onMoveDown, enabled = canMoveDown) { Text("Down") }
                TextButton(onClick = onRemove) { Text("Remove") }
            }
        }
    }
}

@Composable
fun EmptyLibraryCard(onImportFile: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = VeritasPackStyle.surfaceAlpha())),
        shape = VeritasPackStyle.cardShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BrandMark(compact = true)
            Text("No saved readings yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(
                "Import a file or paste text to create your first local reading item. Progress, annotations, and queue state will be saved automatically.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onImportFile) { Text("Import first file") }
        }
    }
}

@Composable
fun DocumentCard(
    document: SavedDocument,
    isQueued: Boolean,
    viewMode: LibraryViewMode,
    selectionMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelected: () -> Unit,
    onDelete: () -> Unit,
    onToggleQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onSetCollection: () -> Unit,
    onShowDetails: () -> Unit,
    onManageLists: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress = progressFraction(document)
    var showActions by remember { mutableStateOf(false) }
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 0.965f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "documentCardSelectionScale"
    )
    val coverSize = when (viewMode) {
        LibraryViewMode.SMALL, LibraryViewMode.LIST -> 48.dp
        LibraryViewMode.DETAILS -> 72.dp
        else -> 64.dp
    }
    val showChips = viewMode == LibraryViewMode.MEDIUM || viewMode == LibraryViewMode.DETAILS
    val showPreview = viewMode == LibraryViewMode.DETAILS

    val context = LocalContext.current
    val coverFile = remember(document.id) { CoverExtractor.coverFile(context, document.id) }
    val coverBitmap = remember(coverFile) {
        coverFile?.let { file ->
            runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = selectionScale, scaleY = selectionScale)
            .pointerInput(selectionMode, selected, document.id) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = {
                        if (selectionMode) onToggleSelected() else onOpen()
                    }
                )
            },
        shape = VeritasPackStyle.compactShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = VeritasPackStyle.surfaceAlpha())
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(if (viewMode == LibraryViewMode.LIST) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(coverSize)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                when {
                                    selected -> MaterialTheme.colorScheme.primary
                                    document.favorite -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (coverBitmap != null && !selected) {
                    Image(
                        bitmap = coverBitmap.asImageBitmap(),
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        when {
                            selected -> "✓"
                            document.favorite -> "★"
                            else -> document.sourceLabel.take(3).uppercase()
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${document.sourceLabel} • ${progressPercent(document)}% read • ${document.chunkCount} sentences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showPreview) {
                    Text(document.preview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (viewMode != LibraryViewMode.LIST) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (showChips) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (document.collection.isNotBlank()) SoftChip(document.collection, emphasis = true)
                        if (isQueued) SoftChip("Queued")
                        if (document.favorite) SoftChip("Favorite")
                        SoftChip("Updated ${formatUpdated(document.updatedAt)}")
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectionMode) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                CircleShape
                            )
                            .clickable { onToggleSelected() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (selected) "✓" else "+", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isQueued) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                CircleShape
                            )
                            .clickable { onToggleQueue() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isQueued) "✓" else "+",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
                                .clickable { showActions = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Document actions",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showActions,
                            onDismissRequest = { showActions = false },
                            modifier = Modifier.width(260.dp)
                        ) {
                            DropdownMenuItem(text = { Text("Open reading") }, onClick = { showActions = false; onOpen() })
                            DropdownMenuItem(text = { Text(if (document.favorite) "Remove from favorites" else "Add to favorites") }, onClick = { showActions = false; onToggleFavorite() })
                            DropdownMenuItem(text = { Text(if (isQueued) "Remove from Queue" else "Add to Queue") }, onClick = { showActions = false; onToggleQueue() })
                            DropdownMenuItem(text = { Text("Move to collection") }, onClick = { showActions = false; onSetCollection() })
                            DropdownMenuItem(text = { Text("Save to lists") }, onClick = { showActions = false; onManageLists() })
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { showActions = false; onRename() })
                            DropdownMenuItem(text = { Text("Details") }, onClick = { showActions = false; onShowDetails() })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showActions = false; onDelete() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentTileCard(
    document: SavedDocument,
    isQueued: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelected: () -> Unit,
    onDelete: () -> Unit,
    onToggleQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onSetCollection: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier,
    onManageLists: () -> Unit = {}
) {
    var showActions by remember { mutableStateOf(false) }
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 0.965f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "documentTileSelectionScale"
    )
    val context = LocalContext.current
    val coverFile = remember(document.id) { CoverExtractor.coverFile(context, document.id) }
    val coverBitmap = remember(coverFile) {
        coverFile?.let { file ->
            runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
        }
    }
    val isUnread = document.currentIndex == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = selectionScale, scaleY = selectionScale),
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = VeritasPackStyle.surfaceAlpha())
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (selectionMode) onToggleSelected() else onOpen() }
                .pointerInput(selectionMode, selected, document.id) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onTap = { if (selectionMode) onToggleSelected() else onOpen() }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (coverBitmap != null) {
                    Image(
                        bitmap = coverBitmap.asImageBitmap(),
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = document.sourceLabel.take(3).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    }
                }

                if (selectionMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isUnread) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "NEW",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (document.favorite) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Yellow.copy(alpha = 0.9f), CircleShape)
                                    .size(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Favorite",
                                    tint = Color.Black,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Box {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clickable { showActions = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Document actions",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                            DropdownMenuItem(text = { Text("Open reading") }, onClick = { showActions = false; onOpen() })
                            DropdownMenuItem(text = { Text(if (document.favorite) "Remove favorite" else "Add favorite") }, onClick = { showActions = false; onToggleFavorite() })
                            DropdownMenuItem(text = { Text(if (isQueued) "Remove from Queue" else "Add to Queue") }, onClick = { showActions = false; onToggleQueue() })
                            DropdownMenuItem(text = { Text("Move to collection") }, onClick = { showActions = false; onSetCollection() })
                            DropdownMenuItem(text = { Text("Save to lists") }, onClick = { showActions = false; onManageLists() })
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { showActions = false; onRename() })
                            DropdownMenuItem(text = { Text("Details") }, onClick = { showActions = false; onShowDetails() })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showActions = false; onDelete() })
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = document.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${progressPercent(document)}% read",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${document.chunkCount} sent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                LinearProgressIndicator(
                    progress = { progressFraction(document) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun EmbeddedOnboardingBlock(
    onOpenFileBrowser: () -> Unit,
    onPasteText: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text("🎧", fontSize = 40.sp)
            }
            Text(
                "Listen to anything, eyes-free.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Import PDFs, EPUBs, documents, or paste web articles to get started.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onOpenFileBrowser,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Browse Files")
                }
                FilledTonalButton(
                    onClick = onPasteText,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Paste Text")
                }
            }
        }
    }
}


@Composable
private fun RowScope.BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
    label: String
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(contentColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = contentColor,
            maxLines = 1
        )
    }
}

@Composable
private fun ImportSheetMenu(
    onSelectOption: (ImportOption) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add something", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ImportSheetOptionCard(
                icon = Icons.Filled.FolderOpen,
                title = "File browser",
                subtitle = "Browse and batch import local files",
                onClick = { onSelectOption(ImportOption.BROWSE) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.ContentPaste,
                title = "Paste text",
                subtitle = "Copy and paste any content",
                onClick = { onSelectOption(ImportOption.PASTE) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.Language,
                title = "From web",
                subtitle = "Paste a link to an article",
                onClick = { onSelectOption(ImportOption.WEB) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.PhotoCamera,
                title = "Scan / OCR",
                subtitle = "Take a photo of printed text",
                onClick = { onSelectOption(ImportOption.SCAN) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.Description,
                title = "Browse phone folders",
                subtitle = "Open system file chooser",
                onClick = { onSelectOption(ImportOption.FILE) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.EditNote,
                title = "Write note",
                subtitle = "Create a freeform reading note",
                onClick = { onSelectOption(ImportOption.WRITE_NOTE) }
            )
        }
    }
}

@Composable
private fun ImportSheetOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ImportSheetWeb(
    urlText: String,
    onUrlChange: (String) -> Unit,
    onImport: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("Import link", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        Text("Paste a link to any web article, report, or blog post below:", color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        OutlinedTextField(
            value = urlText,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Article Link") },
            placeholder = { Text("https://example.com/article...") },
            singleLine = true
        )
        
        Button(
            onClick = { onImport(urlText) },
            modifier = Modifier.fillMaxWidth(),
            enabled = urlText.isNotBlank() && WebArticleExtractor.looksLikeUrl(urlText)
        ) {
            Text("Import web article")
        }
    }
}

@Composable
private fun ImportSheetPaste(
    pastedText: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("Paste text", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        Text("Paste loose text, a document snippet, or email contents below:", color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        OutlinedTextField(
            value = pastedText,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            label = { Text("Content") },
            placeholder = { Text("Paste text here...") }
        )
        
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = pastedText.isNotBlank()
        ) {
            Text("Save reading")
        }
    }
}

@Composable
private fun StudyEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    onGoToLibrary: () -> Unit,
    onImportFile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onImportFile) {
                    Text("Import file")
                }
                OutlinedButton(onClick = onGoToLibrary) {
                    Text("Go to Library")
                }
            }
        }
    }
}

private fun renderMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("**")
                        i += 2
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append("*")
                        i += 1
                    }
                }
                text.startsWith("##", i) -> {
                    pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp))
                    val end = text.indexOf("\n", i)
                    if (end != -1) {
                        append(text.substring(i + 2, end).trim())
                        pop()
                        i = end
                    } else {
                        append(text.substring(i + 2).trim())
                        pop()
                        i = text.length
                    }
                }
                text.startsWith("__", i) -> {
                    val end = text.indexOf("__", i + 2)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline))
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("__")
                        i += 2
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

@Composable
private fun FlashcardReviewDialog(
    dueCards: List<FlashcardProgress>,
    onGrade: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    if (currentIndex >= dueCards.size) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = { Button(onClick = onDismiss) { Text("Awesome") } },
            title = { Text("Session Completed") },
            text = { Text("You've finished reviewing all due cards for now!") }
        )
    } else {
        val card = dueCards[currentIndex]
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Exit Review") }
            },
            title = { Text("Reviewing Card ${currentIndex + 1} of ${dueCards.size}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { showAnswer = !showAnswer },
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                if (!showAnswer) {
                                    Text(card.front, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Tap to flip", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                } else {
                                    Text(card.back, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Tap to show question", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    if (showAnswer) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("Rate your recall:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onGrade(card.id, 1)
                                        showAnswer = false
                                        currentIndex++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEF4444)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text("Again", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
                                }
                                Button(
                                    onClick = {
                                        onGrade(card.id, 2)
                                        showAnswer = false
                                        currentIndex++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF97316)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text("Hard", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
                                }
                                Button(
                                    onClick = {
                                        onGrade(card.id, 3)
                                        showAnswer = false
                                        currentIndex++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF3B82F6)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text("Good", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
                                }
                                Button(
                                    onClick = {
                                        onGrade(card.id, 4)
                                        showAnswer = false
                                        currentIndex++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF00BCD4)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text("Easy", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

data class BookmarkGroup(
    val id: String,
    val document: SavedDocument,
    val annotations: List<ReaderAnnotation>,
    val highlightColor: String?,
    val startSentence: Int,
    val endSentence: Int
)

fun groupBookmarks(
    document: SavedDocument,
    annotations: List<ReaderAnnotation>
): List<BookmarkGroup> {
    val sorted = annotations.sortedBy { it.chunkIndex }
    val groups = mutableListOf<BookmarkGroup>()
    
    val withGroup = sorted.filter { !it.selectionGroupId.isNullOrBlank() }
    val withoutGroup = sorted.filter { it.selectionGroupId.isNullOrBlank() }
    
    val groupedById = withGroup.groupBy { it.selectionGroupId }
    groupedById.forEach { (groupId, groupAnnots) ->
        val sortedAnnots = groupAnnots.sortedBy { it.chunkIndex }
        val start = sortedAnnots.first().chunkIndex
        val end = sortedAnnots.last().chunkIndex
        val color = sortedAnnots.first().highlightColor ?: "#FFE082"
        groups.add(
            BookmarkGroup(
                id = groupId ?: java.util.UUID.randomUUID().toString(),
                document = document,
                annotations = sortedAnnots,
                highlightColor = color,
                startSentence = start,
                endSentence = end
            )
        )
    }
    
    if (withoutGroup.isNotEmpty()) {
        var currentRun = mutableListOf<ReaderAnnotation>()
        for (ann in withoutGroup) {
            if (currentRun.isEmpty()) {
                currentRun.add(ann)
            } else {
                val lastAnn = currentRun.last()
                if (ann.chunkIndex == lastAnn.chunkIndex + 1 && ann.highlightColor == lastAnn.highlightColor) {
                    currentRun.add(ann)
                } else {
                    val start = currentRun.first().chunkIndex
                    val end = currentRun.last().chunkIndex
                    val color = currentRun.first().highlightColor ?: "#FFE082"
                    groups.add(
                        BookmarkGroup(
                            id = "legacy-${document.id}-$start-$end",
                            document = document,
                            annotations = currentRun,
                            highlightColor = color,
                            startSentence = start,
                            endSentence = end
                        )
                    )
                    currentRun = mutableListOf(ann)
                }
            }
        }
        if (currentRun.isNotEmpty()) {
            val start = currentRun.first().chunkIndex
            val end = currentRun.last().chunkIndex
            val color = currentRun.first().highlightColor ?: "#FFE082"
            groups.add(
                BookmarkGroup(
                    id = "legacy-${document.id}-$start-$end",
                    document = document,
                    annotations = currentRun,
                    highlightColor = color,
                    startSentence = start,
                    endSentence = end
                )
            )
        }
    }
    
    return groups.sortedBy { it.startSentence }
}

@Composable
private fun BookmarkDocumentCard(
    document: SavedDocument,
    groups: List<BookmarkGroup>,
    sentenceTextLookup: (Int) -> String?,
    onOpenAt: (Int) -> Unit,
    onDeleteAnnotations: (Set<String>) -> Unit
) {
    var expanded by rememberSaveable(document.id) { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${groups.size} bookmark${if (groups.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 14.dp))
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groups.forEach { group ->
                        BookmarkGroupCard(
                            group = group,
                            sentenceTextLookup = sentenceTextLookup,
                            onOpenAt = onOpenAt,
                            onDeleteGroup = {
                                onDeleteAnnotations(group.annotations.map { it.stableKey }.toSet())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkGroupCard(
    group: BookmarkGroup,
    sentenceTextLookup: (Int) -> String?,
    onOpenAt: (Int) -> Unit,
    onDeleteGroup: () -> Unit
) {
    val context = LocalContext.current
    var expanded by rememberSaveable(group.id) { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    val bookTitle = group.document.title
    val (cleanTitle, authorName) = remember(bookTitle) { getBookAndAuthor(bookTitle) }
    
    val collapsedText = remember(group, sentenceTextLookup) {
        val firstAnn = group.annotations.firstOrNull()
        val firstText = firstAnn?.let { sentenceTextLookup(it.chunkIndex) }
        if (firstText.isNullOrBlank()) {
            if (group.startSentence == group.endSentence) "Sentence ${group.startSentence + 1}"
            else "Sentences ${group.startSentence + 1}–${group.endSentence + 1}"
        } else {
            if (firstText.length > 60) firstText.take(57) + "..." else firstText
        }
    }
    
    val highlightColor = remember(group.highlightColor) {
        val colorHex = group.highlightColor
        if (colorHex != null) {
            runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
                .getOrDefault(Color(0xFFFFE082))
        } else {
            Color(0xFFFFE082)
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color = highlightColor, shape = CircleShape)
                )
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Text(
                    text = collapsedText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))
                
                val sentencesText = remember(group.annotations, sentenceTextLookup) {
                    group.annotations.map { ann ->
                        sentenceTextLookup(ann.chunkIndex) ?: ""
                    }
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    group.annotations.forEachIndexed { idx, ann ->
                        val text = sentencesText.getOrNull(idx)?.ifBlank { null } ?: "Loading sentence text..."
                        
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(color = highlightColor, shape = RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { onOpenAt(group.startSentence) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Open in document ↗",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Actions",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Copy") },
                                    onClick = {
                                        showMenu = false
                                        val fullText = sentencesText.joinToString(" ")
                                        copyTextToClipboard(context, "Bookmark Text", fullText)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        onDeleteGroup()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NoteGroup(
    val id: String,
    val document: SavedDocument,
    val annotations: List<ReaderAnnotation>,
    val noteText: String,
    val startSentence: Int,
    val endSentence: Int
)

fun groupNotes(
    document: SavedDocument,
    annotations: List<ReaderAnnotation>
): List<NoteGroup> {
    val sorted = annotations.sortedBy { it.chunkIndex }
    val groups = mutableListOf<NoteGroup>()
    
    val withGroup = sorted.filter { !it.selectionGroupId.isNullOrBlank() }
    val withoutGroup = sorted.filter { it.selectionGroupId.isNullOrBlank() }
    
    val groupedById = withGroup.groupBy { it.selectionGroupId }
    groupedById.forEach { (groupId, groupAnnots) ->
        val sortedAnnots = groupAnnots.sortedBy { it.chunkIndex }
        val start = sortedAnnots.first().chunkIndex
        val end = sortedAnnots.last().chunkIndex
        val text = sortedAnnots.first().note
        groups.add(
            NoteGroup(
                id = groupId ?: java.util.UUID.randomUUID().toString(),
                document = document,
                annotations = sortedAnnots,
                noteText = text,
                startSentence = start,
                endSentence = end
            )
        )
    }
    
    withoutGroup.forEach { ann ->
        groups.add(
            NoteGroup(
                id = "single-${document.id}-${ann.chunkIndex}",
                document = document,
                annotations = listOf(ann),
                noteText = ann.note,
                startSentence = ann.chunkIndex,
                endSentence = ann.chunkIndex
            )
        )
    }
    
    return groups.sortedBy { it.startSentence }
}

fun getBookAndAuthor(title: String): Pair<String, String> {
    val idx = title.lastIndexOf(" - ")
    if (idx != -1) {
        val book = title.substring(0, idx).trim()
        val author = title.substring(idx + 3).trim()
        return Pair(book, author)
    }
    val idxBy = title.lastIndexOf(" by ", ignoreCase = true)
    if (idxBy != -1) {
        val book = title.substring(0, idxBy).trim()
        val author = title.substring(idxBy + 4).trim()
        return Pair(book, author)
    }
    return Pair(title, "")
}

