package com.veritas.reader.ui.screens

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
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

internal enum class VeritasHomeTab {
    HOME,
    LIBRARY,
    STUDY
}

internal data class MarkedDocument(
    val document: SavedDocument,
    val annotations: List<ReaderAnnotation>,
    val documentNote: String,
    val updatedAt: Long
)

internal enum class ImportSheetMode {
    MENU,
    WEB,
    PASTE
}

internal enum class ImportOption {
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
    onDeleteFlashcard: (String) -> Unit = {},
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null
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
    var sortMode by remember { mutableStateOf("Updated") }
    var showQueue by remember { mutableStateOf(false) }
    var activeReviewDeck by remember { mutableStateOf<List<FlashcardProgress>?>(null) }
    var selectedDocumentIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val cards = uiState.flashcards
    val dueCards = remember(cards) {
        cards.filter { it.nextReviewTime <= System.currentTimeMillis() }
    }
    var confirmBatchDelete by remember { mutableStateOf(false) }
    var showBatchMenu by remember { mutableStateOf(false) }
    var showBatchCollectionDialog by remember { mutableStateOf(false) }
    var batchCollectionDraft by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val repository = remember(context) { DocumentRepository(context) }
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

    val pagerState = rememberPagerState(initialPage = selectedHomeTab.ordinal) { VeritasHomeTab.entries.size }
    val homeListState = rememberLazyListState()
    val studyListState = rememberLazyListState()

    // Sync pager scroll to selectedHomeTab changes (tab taps, programmatic switches):
    LaunchedEffect(selectedHomeTab) {
        if (pagerState.currentPage != selectedHomeTab.ordinal) {
            pagerState.animateScrollToPage(selectedHomeTab.ordinal)
        }
    }

    // Sync selectedHomeTab from the pager via targetPage: during a user swipe/fling it
    // updates the moment the destination is known (crossing halfway), so the chrome syncs
    // as responsively as a tab tap; during a programmatic animateScrollToPage it already
    // equals the final destination, so intermediate pages crossed mid-animation can never
    // hijack the selection (the old currentPage bug where Home→Study landed on Library).
    LaunchedEffect(pagerState.targetPage) {
        val targetTab = VeritasHomeTab.entries[pagerState.targetPage]
        if (selectedHomeTab != targetTab) {
            selectedHomeTab = targetTab
        }
    }

    // Keep document progress fresh: the PlaybackService writes currentIndex straight to the
    // repository while speaking, so the uiState document list goes stale during playback.
    // Refresh on entering this screen (initial false) and every time playback pauses/stops,
    // so the hero card and library rows show real progress without reopening the app.
    LaunchedEffect(PlaybackStateStore.isPlaying) {
        if (!PlaybackStateStore.isPlaying) {
            onRefreshMainPage()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val pastedColor = MaterialTheme.colorScheme.inversePrimary
    val slidesColor = blendColors(secondaryColor, errorColor, 0.5f)

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
                description = "PDFs imported from local file storage or other directories."
            ),
            DonutSlice(
                label = "Web Articles",
                value = webCount.toFloat(),
                color = secondaryColor,
                description = "Online articles, blogs, and papers saved via URL import."
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
                description = "PowerPoint presentations read slide by slide."
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
                description = "Text pasted directly into the reader interface."
            )
        ).filter { it.value > 0f }
    }

    val timeSlices = remember(documents, uiState.documentReadingTimes, primaryColor, secondaryColor, tertiaryColor, errorColor) {
        val docTimes = documents.mapNotNull { doc ->
            val readingTime = uiState.documentReadingTimes[doc.id] ?: 0L
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
                        value = time.toFloat() / 60000f,
                        color = colors[idx % colors.size],
                        description = "You spent ${time / 60000} minutes reading this document."
                    )
                )
            }
            if (othersTime > 0L) {
                list.add(
                    DonutSlice(
                        label = "Others",
                        value = othersTime.toFloat() / 60000f,
                        color = colors[4],
                        description = "All other documents combined account for ${othersTime / 60000} minutes."
                    )
                )
            }
            list
        }
    }
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
    val allBookmarkGroups = remember(bookmarksOnly, uiState.allAnnotations) {
        bookmarksOnly.flatMap { markedDocument ->
            val doc = markedDocument.document
            val docBookmarks = markedDocument.annotations.filter { it.type == AnnotationType.BOOKMARK }
            groupBookmarks(doc, docBookmarks)
        }
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

    activeReviewDeck?.let { deck ->
        FlashcardReviewDialog(
            dueCards = deck,
            onGrade = onGradeFlashcard,
            onDismiss = { activeReviewDeck = null }
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
                // FAB pops in/out with the Library tab and its + rotates 45° with a
                // spring while the import sheet is open (micro-morph, no layout risk).
                AnimatedVisibility(
                    visible = selectedHomeTab == VeritasHomeTab.LIBRARY,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(150)),
                    exit = scaleOut(tween(120)) + fadeOut(tween(120))
                ) {
                    val fabPlusRotation by animateFloatAsState(
                        targetValue = if (showImportSheet) 45f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "fabPlusRotation"
                    )
                    ExtendedFloatingActionButton(
                        onClick = { showImportSheet = true },
                        shape = VeritasPackStyle.chipShape(),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = {
                            Text(
                                "+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.graphicsLayer { rotationZ = fabPlusRotation }
                            )
                        },
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
                                            val bookmarkCount = remember(uiState.allAnnotations) { uiState.allAnnotations.count { it.type == AnnotationType.BOOKMARK } }
                                            val noteCount = remember(uiState.allAnnotations, uiState.documentNotes) { uiState.allAnnotations.count { it.type == AnnotationType.NOTE } + uiState.documentNotes.size }
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
                                        imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
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
                                        imageVector = Icons.Filled.Layers,
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
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (VeritasHomeTab.entries[page]) {
                            VeritasHomeTab.HOME -> {
                                val recentImports = remember(documents) {
                                    documents.sortedByDescending { maxOf(it.createdAt, it.updatedAt) }.take(4)
                                }
                                val tracker = uiState.readerTrackerSnapshot
                                val weeklyMinutes = tracker.weeklyUsageMillis / 60000L

                                // Two user-selectable hero styles (Settings → Vibrant hero card):
                                // subtle (default) uses container tones so the card reads as a
                                // large themed surface in every palette; vibrant is the original
                                // accent-derived HSV "poster" gradient. Both track the active
                                // color scheme, so adaptive cover keeps working in either mode.
                                val heroScheme = MaterialTheme.colorScheme
                                val streakGradient: Brush
                                val streakOnCard: Color
                                if (uiState.readerSettings.vibrantHero) {
                                    val streakPrimary = heroScheme.primary
                                    val streakHsl = FloatArray(3)
                                    android.graphics.Color.colorToHSV(streakPrimary.toArgb(), streakHsl)
                                    streakGradient = Brush.linearGradient(
                                        listOf(
                                            Color(android.graphics.Color.HSVToColor(floatArrayOf(streakHsl[0], (streakHsl[1] * 0.7f).coerceIn(0f, 1f), (streakHsl[2] * 1.15f).coerceIn(0f, 1f)))),
                                            Color(android.graphics.Color.HSVToColor(floatArrayOf((streakHsl[0] + 15f) % 360f, streakHsl[1].coerceIn(0f, 1f), (streakHsl[2] * 0.85f).coerceIn(0f, 1f))))
                                        )
                                    )
                                    streakOnCard = if (streakPrimary.luminance() > 0.35f)
                                        Color(0xFF1A1A2E) else Color.White
                                } else {
                                    streakGradient = Brush.linearGradient(
                                        listOf(
                                            heroScheme.primaryContainer,
                                            blendColors(heroScheme.primaryContainer, heroScheme.surface, 0.55f)
                                        )
                                    )
                                    streakOnCard = heroScheme.onPrimaryContainer
                                }

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(homeListState) {
                                            var pullDistance = 0f
                                            detectVerticalDragGestures(
                                                onDragStart = { pullDistance = 0f },
                                                onVerticalDrag = { _, dragAmount ->
                                                    val atTop = homeListState.firstVisibleItemIndex == 0 && homeListState.firstVisibleItemScrollOffset == 0
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
                                    state = homeListState,
                                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // 1. Overview Pills (scrollable row, no header) - compact sized
                                    item {
                                        val collectionCount = remember(documents) { documents.map { it.collection.trim() }.filter { it.isNotBlank() }.distinct().size }
                                        val sourceCount = remember(documents) { documents.map { it.sourceLabel.trim().ifBlank { "Text" } }.distinct().size }
                                        val unreadCountVal = remember(documents) { documents.count { it.currentIndex <= 0 } }
                                        Row(
                                            modifier = Modifier
                                                .staggeredEntrance(0)
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OverviewPill(value = "${documents.size}", label = "Saved", modifier = Modifier.width(68.dp))
                                            OverviewPill(value = "$unreadCountVal", label = "Unread", modifier = Modifier.width(70.dp))
                                            OverviewPill(value = "$readingCount", label = "Reading", modifier = Modifier.width(72.dp))
                                            OverviewPill(value = "$completedCount", label = "Done", modifier = Modifier.width(60.dp))
                                            OverviewPill(value = "$favoriteCount", label = "Starred", modifier = Modifier.width(64.dp))
                                            OverviewPill(value = "$collectionCount", label = "Collections", modifier = Modifier.width(84.dp))
                                            OverviewPill(value = "$sourceCount", label = "Formats", modifier = Modifier.width(72.dp))
                                        }
                                    }

                                    // 2. Hero Card: continue-reading-first, playback-aware,
                                    // with data-earned rotating insight copy.
                                    item {
                                        val todayMinutes = remember(tracker.weeklyUsageByDay) {
                                            val idx = (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
                                            (tracker.weeklyUsageByDay.getOrNull(idx) ?: 0L) / 60000L
                                        }
                                        Box(modifier = Modifier.staggeredEntrance(1)) {
                                        VeritasHomeHeroCard(
                                            tracker = tracker,
                                            continueDocument = continueDocument,
                                            gradient = streakGradient,
                                            onCardColor = streakOnCard,
                                            weeklyMinutes = weeklyMinutes,
                                            todayMinutes = todayMinutes,
                                            onOpen = { onOpenDocument(it) },
                                            onPlayPause = { onPlayPauseContinue(it) },
                                            onClear = { onClearContinueDocument(it) },
                                            onAddContent = { showImportSheet = true }
                                        )
                                        }
                                    }

                                    // 3. Active library (directly on background, no cards, no lines, compact spaced)
                                    if (recentImports.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Active library",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.staggeredEntrance(2).padding(top = 8.dp)
                                            )
                                        }
                                        item {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                                modifier = Modifier.staggeredEntrance(3).fillMaxWidth()
                                            ) {
                                                recentImports.forEach { doc ->
                                                    val isQueued = isQueued(doc)
                                                    RecentImportItem(
                                                        document = doc,
                                                        isQueued = isQueued,
                                                        onOpen = { onOpenDocument(doc) },
                                                        onToggleFavorite = { onToggleFavorite(doc) },
                                                        onToggleQueue = { onToggleQueue(doc) },
                                                        onSetCollection = { onSetCollection(doc) },
                                                        onManageLists = { manageListsDocument = doc },
                                                        onRename = { onRenameDocument(doc) },
                                                        onShowDetails = { onShowDetails(doc) },
                                                        onDelete = { onDeleteDocument(doc) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 4. Continue Reading now lives inside the hero card above.

                                    // 5. Add Content card (No section heading, updated styling)
                                    item {
                                        Card(
                                            modifier = Modifier.staggeredEntrance(4).fillMaxWidth(),
                                            shape = VeritasPackStyle.cardShape(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
                                            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                                        ) {
                                            Box {
                                                HomeActionRow(
                                                    icon = Icons.Filled.Add,
                                                    title = "Add content",
                                                    body = "Import file - Upload PDF, EPUB, DOCX, PPTX, TXT, or HTML",
                                                    iconBackground = Color(0xFFF0F3FF),
                                                    iconForeground = Color(0xFF7C6FFF),
                                                    onClick = { showImportSheet = true }
                                                )
                                            }
                                        }
                                    }

                                    // 6. Side-by-side Animated Donut Charts
                                    item {
                                        val displayTimeSlices = if (timeSlices.isEmpty()) {
                                            listOf(
                                                DonutSlice(
                                                    label = "No reading yet",
                                                    value = 0f,
                                                    color = Color.Gray.copy(alpha = 0.4f),
                                                    description = "Start reading to track your time allocation."
                                                )
                                            )
                                        } else {
                                            timeSlices
                                        }

                                        Row(
                                            modifier = Modifier.staggeredEntrance(5).fillMaxWidth().padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            DashboardDonutChart(
                                                title = "Library Source Distribution",
                                                slices = formatSlices,
                                                totalLabel = "Readings",
                                                modifier = Modifier.weight(1f)
                                            )
                                            DashboardDonutChart(
                                                title = "Time Allocation — This Month",
                                                slices = displayTimeSlices,
                                                totalLabel = "Minutes",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            VeritasHomeTab.LIBRARY -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                            modifier = Modifier
                                .staggeredEntrance(0)
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

                                    LazyColumn(
                                        modifier = Modifier
                                            .staggeredEntrance(1)
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
                                        contentPadding = PaddingValues(top = 0.dp, bottom = 22.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
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

        if (documents.isEmpty()) {
            item { EmptyLibraryCard(onImportFile = onImportFile) }
        } else if (visibleDocuments.isEmpty()) {
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
        } else {
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
                                onManageLists = { manageListsDocument = doc },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
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
                        },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(22.dp)) }
                                    }
                                }
                            }

                            VeritasHomeTab.STUDY -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(studyListState) {
                                            var pullDistance = 0f
                                            detectVerticalDragGestures(
                                                onDragStart = { pullDistance = 0f },
                                                onVerticalDrag = { _, dragAmount ->
                                                    val atTop = studyListState.firstVisibleItemIndex == 0 && studyListState.firstVisibleItemScrollOffset == 0
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
                                    state = studyListState,
                                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
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
                                        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
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
                            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
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
                                        imageVector = Icons.Filled.Layers,
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
                            var confirmNoteDelete by remember { mutableStateOf(false) }
                            val noteShareContext = LocalContext.current

                            if (confirmNoteDelete) {
                                AlertDialog(
                                    onDismissRequest = { confirmNoteDelete = false },
                                    title = { Text(stringResource(R.string.delete_note_title)) },
                                    text = { Text(stringResource(R.string.delete_note_message)) },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            confirmNoteDelete = false
                                            onDeleteGeneralNote(generalNote.id)
                                        }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { confirmNoteDelete = false }) {
                                            Text(stringResource(R.string.action_cancel))
                                        }
                                    }
                                )
                            }

                            Box {
                            val noteHaptic = LocalHapticFeedback.current
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onEditGeneralNote(generalNote) },
                                        onLongClick = {
                                            noteHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showNoteMenu = true
                                        }
                                    )
                                    .padding(vertical = 4.dp),
                                shape = VeritasPackStyle.compactShape(),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
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
                                            // 40dp touch target (was 24dp — too small to hit
                                            // reliably); the icon itself stays 16dp.
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (generalNote.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                                contentDescription = if (generalNote.pinned) "Unpin note" else "Pin note",
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
                                    onClick = { showNoteMenu = false; confirmNoteDelete = true }
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
                                item(key = "general-notes-grid-pinned") {
                                    Row(
                                        modifier = Modifier.animateItem().fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val leftNotes = pinnedNotes.filterIndexed { index, _ -> index % 2 == 0 }
                                        val rightNotes = pinnedNotes.filterIndexed { index, _ -> index % 2 == 1 }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(0.dp)
                                        ) {
                                            leftNotes.forEach { note ->
                                                NoteCardItem(note)
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(0.dp)
                                        ) {
                                            rightNotes.forEach { note ->
                                                NoteCardItem(note)
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
                                item(key = "general-notes-grid-other") {
                                    Row(
                                        modifier = Modifier.animateItem().fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val leftNotes = otherNotes.filterIndexed { index, _ -> index % 2 == 0 }
                                        val rightNotes = otherNotes.filterIndexed { index, _ -> index % 2 == 1 }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(0.dp)
                                        ) {
                                            leftNotes.forEach { note ->
                                                NoteCardItem(note)
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(0.dp)
                                        ) {
                                            rightNotes.forEach { note ->
                                                NoteCardItem(note)
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
                            var confirmClearHistory by remember { mutableStateOf(false) }
                            if (confirmClearHistory) {
                                AlertDialog(
                                    onDismissRequest = { confirmClearHistory = false },
                                    title = { Text(stringResource(R.string.clear_history_title)) },
                                    text = { Text(stringResource(R.string.clear_history_message)) },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            confirmClearHistory = false
                                            onClearReadingHistory()
                                        }) { Text(stringResource(R.string.action_clear), color = MaterialTheme.colorScheme.error) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { confirmClearHistory = false }) {
                                            Text(stringResource(R.string.action_cancel))
                                        }
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recent history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { confirmClearHistory = true }) {
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
                                    border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
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
                                                val badgeColor = if (isDue) Color(0xFFEF4444) else Color(0xFF10B981)
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


