package com.veritas.reader.ui.screens

import com.veritas.reader.ui.ReaderUiState

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import com.veritas.reader.*

private enum class VeritasHomeTab(val label: String, val icon: String) {
    DASHBOARD("Dashboard", "▦"),
    LIBRARY("Library", "▤"),
    FILES("Files", "▣"),
    BOOKMARKS_NOTES("Notes", "★")
}

private data class MarkedDocument(
    val document: SavedDocument,
    val annotations: List<ReaderAnnotation>,
    val documentNote: String,
    val updatedAt: Long
)

@Composable
fun LibraryScreen(
    uiState: ReaderUiState,
    onDraftTextChange: (String) -> Unit,
    onCreateFromDraft: () -> Unit,
    onImportWebArticle: (String) -> Unit,
    onImportFile: () -> Unit,
    onAdvancedPdfImport: () -> Unit,
    onOpenFileBrowser: () -> Unit,
    onOpenReadingLists: () -> Unit,
    onOpenReadingHistory: () -> Unit,
    onOpenDocument: (SavedDocument) -> Unit,
    onOpenDocumentAt: (SavedDocument, Int) -> Unit,
    onClearContinueDocument: (SavedDocument) -> Unit,
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
    onBackupRestore: () -> Unit,
    onOpenSyncCenter: () -> Unit,
    onOpenAiCenter: () -> Unit,
    onOpenSettingsHub: () -> Unit,
    onRefreshMainPage: () -> Unit,
    onBatchDeleteDocuments: (Set<String>) -> Unit,
    onBatchFavoriteDocuments: (Set<String>) -> Unit,
    onBatchQueueDocuments: (Set<String>) -> Unit,
    onBatchSetCollectionDocuments: (Set<String>, String) -> Unit,
    onDeleteAnnotations: (Set<String>) -> Unit
) {
    val documents = uiState.documents
    val queuedDocuments = uiState.queuedDocuments
    val draftText = uiState.draftText
    var libraryQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var sourceFilter by remember { mutableStateOf("All") }
    var collectionFilter by remember { mutableStateOf("All") }
    var sortMode by remember { mutableStateOf("Updated") }
    var showQuickPaste by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var selectedDocumentIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var confirmBatchDelete by remember { mutableStateOf(false) }
    var showBatchMenu by remember { mutableStateOf(false) }
    var showBatchCollectionDialog by remember { mutableStateOf(false) }
    var batchCollectionDraft by remember { mutableStateOf("") }
    var libraryViewMode by remember { mutableStateOf(LibraryViewMode.MEDIUM) }
    var showLibraryViewMenu by remember { mutableStateOf(false) }
    var showImportMenu by remember { mutableStateOf(false) }
    var selectedHomeTab by remember { mutableStateOf(VeritasHomeTab.DASHBOARD) }
    var showDashboardSidebar by remember { mutableStateOf(false) }
    var showReadingStatsDashboard by remember { mutableStateOf(false) }
    var selectedAnnotationKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var confirmAnnotationDelete by remember { mutableStateOf(false) }
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

    val completedCount by remember(documents) { androidx.compose.runtime.derivedStateOf { documents.count { it.chunkCount > 0 && it.currentIndex >= it.chunkCount - 1 } } }
    val readingCount by remember(documents) { androidx.compose.runtime.derivedStateOf { documents.count { it.chunkCount > 1 && it.currentIndex in 1 until it.chunkCount - 1 } } }
    val favoriteCount by remember(documents) { androidx.compose.runtime.derivedStateOf { documents.count { it.favorite } } }
    val sourceOptions by remember(documents) { androidx.compose.runtime.derivedStateOf { listOf("All") + documents.map { it.sourceLabel.ifBlank { "Text" } }.distinct().sorted() } }
    val collectionOptions by remember(documents) { androidx.compose.runtime.derivedStateOf { listOf("All", "Unfiled") + documents.map { it.collection.trim() }.filter { it.isNotBlank() }.distinct().sorted() } }
    val continueDocument by remember(documents) {
        androidx.compose.runtime.derivedStateOf {
            documents
                .filter { it.chunkCount > 1 && it.currentIndex in 1 until it.chunkCount }
                .maxByOrNull { it.updatedAt }
        }
    }
    val selectionMode = selectedDocumentIds.isNotEmpty()
    val welcomeName = uiState.userName.trim().ifBlank { "Reader" }
    val dashboardHeadline = if (uiState.hasImportedOrOpenedDocument) {
        "Enjoy your read, $welcomeName."
    } else {
        "Welcome, $welcomeName"
    }
    val dashboardSubtitle = if (uiState.hasImportedOrOpenedDocument) {
        "Pick up where you left off, or choose something new."
    } else {
        "What do you want to read today?"
    }
    val annotatedDocuments = remember(documents, uiState.allAnnotations, uiState.documentNotes) {
        val annotationsByDocument = uiState.allAnnotations
            .filter { it.type == AnnotationType.BOOKMARK || it.type == AnnotationType.NOTE }
            .groupBy { it.documentId }
        val markedDocumentIds = annotationsByDocument.keys + uiState.documentNotes.keys
        markedDocumentIds.mapNotNull { documentId ->
            val document = documents.firstOrNull { it.id == documentId } ?: return@mapNotNull null
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
    val annotationSelectionMode = selectedAnnotationKeys.isNotEmpty()

    val visibleDocuments by remember(documents, queuedDocuments, libraryQuery, statusFilter, sourceFilter, collectionFilter, sortMode) {
        androidx.compose.runtime.derivedStateOf {
            documents
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

    if (showQuickPaste) {
        AlertDialog(
            onDismissRequest = { showQuickPaste = false },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateFromDraft()
                        showQuickPaste = false
                    },
                    enabled = draftText.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showQuickPaste = false }) { Text("Cancel") } },
            title = { Text("Quick paste / web article") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Paste loose text or a web article link and save it as a reading item.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = onDraftTextChange,
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        label = { Text("Text to read") },
                        placeholder = { Text("Paste a URL, article, note, chapter, or paragraph…") }
                    )
                    if (WebArticleExtractor.looksLikeUrl(draftText)) {
                        OutlinedButton(
                            onClick = {
                                onImportWebArticle(draftText)
                                showQuickPaste = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Import web article") }
                    }
                }
            }
        )
    }

    if (showFilters) {
        AlertDialog(
            onDismissRequest = { showFilters = false },
            confirmButton = { TextButton(onClick = { showFilters = false }) { Text("Done") } },
            title = { Text("Library filters") },
            text = {
                Column(modifier = Modifier.height(520.dp).verticalScroll(rememberScrollState())) {
                    LibraryControlsCard(
                        query = libraryQuery,
                        onQueryChange = { libraryQuery = it },
                        statusFilter = statusFilter,
                        onStatusFilterChange = { statusFilter = it },
                        sourceFilter = sourceFilter,
                        onSourceFilterChange = { sourceFilter = it },
                        collectionFilter = collectionFilter,
                        onCollectionFilterChange = { collectionFilter = it },
                        sortMode = sortMode,
                        onSortModeChange = { sortMode = it },
                        sourceOptions = sourceOptions,
                        collectionOptions = collectionOptions,
                        visibleCount = visibleDocuments.size,
                        totalCount = documents.size
                    )
                }
            }
        )
    }

    if (showQueue) {
        AlertDialog(
            onDismissRequest = { showQueue = false },
            confirmButton = { TextButton(onClick = { showQueue = false }) { Text("Close") } },
            title = { Text("Listen Later") },
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

    if (showDashboardSidebar) {
        DashboardSidebarDialog(
            name = welcomeName,
            snapshot = uiState.readerTrackerSnapshot,
            onDismiss = { showDashboardSidebar = false },
            onOpenLibrary = {
                selectedHomeTab = VeritasHomeTab.LIBRARY
                showDashboardSidebar = false
            },
            onOpenStats = {
                showReadingStatsDashboard = true
                showDashboardSidebar = false
            },
            onOpenSettings = {
                showDashboardSidebar = false
                onOpenSettingsHub()
            }
        )
    }

    if (showReadingStatsDashboard) {
        ReadingStatsDashboardDialog(
            snapshot = uiState.readerTrackerSnapshot,
            onDismiss = { showReadingStatsDashboard = false }
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                VeritasHomeTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedHomeTab == tab,
                        onClick = { selectedHomeTab = tab },
                        icon = { Text(tab.icon) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { homePadding ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
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
        contentPadding = PaddingValues(bottom = homePadding.calculateBottomPadding() + 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            VeritasHomeTopBar(
                selectedTab = selectedHomeTab,
                onMenu = {
                    when (selectedHomeTab) {
                        VeritasHomeTab.DASHBOARD -> showDashboardSidebar = true
                        VeritasHomeTab.LIBRARY -> showFilters = true
                        VeritasHomeTab.FILES,
                        VeritasHomeTab.BOOKMARKS_NOTES -> Unit
                    }
                },
                onSettings = onOpenSettingsHub
            )
        }

        if (selectedHomeTab == VeritasHomeTab.DASHBOARD) {
            item {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
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

        if (selectedHomeTab == VeritasHomeTab.LIBRARY) {
            item {
                LibrarySearchAndFilters(
                    query = libraryQuery,
                    onQueryChange = { libraryQuery = it },
                    statusFilter = statusFilter,
                    onStatusFilterChange = { filter ->
                        statusFilter = filter
                        if (filter == "Recent") {
                            statusFilter = "All"
                            sortMode = "Updated"
                        }
                    },
                    onSync = onOpenSyncCenter
                )
            }
        }

        if (selectedHomeTab == VeritasHomeTab.DASHBOARD) item {
            DashboardQuickActions(
                continueDocument = continueDocument,
                documentCount = documents.size,
                draftHasText = draftText.isNotBlank(),
                onOpenContinue = { document -> onOpenDocument(document) },
                onClearContinue = { document -> onClearContinueDocument(document) },
                onImportClick = { showImportMenu = true },
                onPasteClick = { showQuickPaste = true },
                onClearClick = {
                    continueDocument?.let(onClearContinueDocument) ?: onDraftTextChange("")
                },
                importMenuExpanded = showImportMenu,
                onDismissImportMenu = { showImportMenu = false },
                onOpenFile = {
                    showImportMenu = false
                    onImportFile()
                },
                onOpenFileBrowser = {
                    showImportMenu = false
                    onOpenFileBrowser()
                },
                onOpenImportSettings = {
                    showImportMenu = false
                    onAdvancedPdfImport()
                },
                onPasteText = {
                    showImportMenu = false
                    showQuickPaste = true
                }
            )
        }

        if (selectedHomeTab == VeritasHomeTab.DASHBOARD && queuedDocuments.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showQueue = true },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("▶", fontWeight = FontWeight.Black) }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Listen Later", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Text("${queuedDocuments.size} queued reading${if (queuedDocuments.size == 1) "" else "s"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = onPlayQueue) { Text("Play") }
                    }
                }
            }
        }

        if (selectedHomeTab == VeritasHomeTab.FILES) {
            item {
                FilesHomePanel(
                    onOpenBrowser = onOpenFileBrowser,
                    onOpenFile = onImportFile,
                    onImportSettings = onAdvancedPdfImport,
                    onPasteText = { showQuickPaste = true }
                )
            }
        }

        if (selectedHomeTab == VeritasHomeTab.BOOKMARKS_NOTES) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bookmarks & Notes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text(
                            if (annotationSelectionMode) {
                                "${selectedAnnotationKeys.size} selected"
                            } else {
                                val bookmarkCount = uiState.allAnnotations.count { it.type == AnnotationType.BOOKMARK }
                                val noteCount = uiState.allAnnotations.count { it.type == AnnotationType.NOTE } + uiState.documentNotes.size
                                "$bookmarkCount bookmarks • $noteCount notes"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (annotationSelectionMode) {
                        TextButton(onClick = { selectedAnnotationKeys = emptySet() }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(onClick = { confirmAnnotationDelete = true }) { Text("Delete") }
                    } else {
                        OutlinedButton(onClick = onOpenSyncCenter) { Text("Sync") }
                    }
                }
            }
            if (annotatedDocuments.isEmpty()) {
                item {
                    HomeActionPanel(
                        title = "No saved marks yet",
                        body = "Bookmark a sentence or save a general note while reading. It will stay here for reference.",
                        primaryLabel = "Go to library",
                        onPrimary = { selectedHomeTab = VeritasHomeTab.LIBRARY },
                        secondaryLabel = "Import",
                        onSecondary = onImportFile
                    )
                }
            } else {
                annotatedDocuments.forEach { markedDocument ->
                    val document = markedDocument.document
                    item(key = "marks-${document.id}") {
                        AnnotationDocumentCard(
                            document = document,
                            annotations = markedDocument.annotations,
                            documentNote = markedDocument.documentNote,
                            selectedKeys = selectedAnnotationKeys,
                            selectionMode = annotationSelectionMode,
                            onToggleDocumentNoteSelected = {
                                val key = documentNoteStableKey(document.id)
                                selectedAnnotationKeys = if (key in selectedAnnotationKeys) {
                                    selectedAnnotationKeys - key
                                } else {
                                    selectedAnnotationKeys + key
                                }
                            },
                            onLongPressDocumentNote = {
                                selectedAnnotationKeys = selectedAnnotationKeys + documentNoteStableKey(document.id)
                            },
                            onToggleSelected = { annotation ->
                                selectedAnnotationKeys = if (annotation.stableKey in selectedAnnotationKeys) {
                                    selectedAnnotationKeys - annotation.stableKey
                                } else {
                                    selectedAnnotationKeys + annotation.stableKey
                                }
                            },
                            onLongPressAnnotation = { annotation -> selectedAnnotationKeys = selectedAnnotationKeys + annotation.stableKey },
                            onOpenDocumentNote = { onOpenDocumentAt(document, document.currentIndex.coerceAtLeast(0)) },
                            onOpenAt = { index -> onOpenDocumentAt(document, index) }
                        )
                    }
                }
            }
        }

        if (selectedHomeTab == VeritasHomeTab.LIBRARY) item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Your library", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(
                        if (selectionMode) "${selectedDocumentIds.size} selected • ${visibleDocuments.size} showing" else "$readingCount in progress • $completedCount completed • ${visibleDocuments.size} showing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (selectionMode) {
                    Box {
                        TextButton(onClick = { showBatchMenu = true }) { Text("⋮", style = MaterialTheme.typography.titleLarge) }
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
                                text = { Text("Add to Listen Later") },
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
                                LibraryViewMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text("${mode.icon} ${mode.label}") },
                                        onClick = {
                                            libraryViewMode = mode
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
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                ) {
                    Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No matching readings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text("Clear the search or change filters to see more saved readings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(onClick = { showFilters = true }) { Text("Change filters") }
                    }
                }
            }
        } else if (selectedHomeTab == VeritasHomeTab.LIBRARY) {
            if (libraryViewMode == LibraryViewMode.TILES) {
                itemsIndexed(visibleDocuments.chunked(2), key = { index, row -> row.joinToString("-") { it.id }.ifBlank { "row-$index" } }) { _, rowDocs ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowDocs.size == 1) Spacer(modifier = Modifier.weight(1f))
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
                        onShowDetails = { onShowDetails(doc) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(22.dp)) }
    }
    }
}

@Composable
private fun VeritasHomeTopBar(
    selectedTab: VeritasHomeTab,
    onMenu: () -> Unit,
    onSettings: () -> Unit
) {
    val showMenu = selectedTab == VeritasHomeTab.DASHBOARD || selectedTab == VeritasHomeTab.LIBRARY
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            if (showMenu) {
                TextButton(
                    onClick = onMenu,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-18).dp)
                        .width(44.dp)
                        .height(48.dp)
                ) {
                    Text("☰", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            VeritasWordmark(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = if (showMenu) 42.dp else 0.dp)
                    .offset(y = (-2).dp)
            )
            TextButton(
                onClick = onSettings,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("⚙", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DashboardSidebarDialog(
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
                    .width(318.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp, topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(compact = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Veritas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onDismiss) { Text("×", style = MaterialTheme.typography.titleLarge) }
                    }
                    ReaderTrackerSidebarCard(snapshot = snapshot, onOpenStats = onOpenStats)
                    HorizontalDivider()
                    SidebarAction("Library", "Saved readings and filters", "▤", onOpenLibrary)
                    SidebarAction("Settings", "Reader, voice, import, AI, and backup", "⚙", onOpenSettings)
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f))
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
            Text("This week", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            MiniWeekBars(snapshot.weeklyUsageByDay)
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
private fun ReadingStatsDashboardDialog(snapshot: ReaderTrackerSnapshot, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reading Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            Text("Your local reading rhythm", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onDismiss) { Text("Close") }
                    }
                }
                item {
                    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                BigStat("${snapshot.currentStreak}", "Current streak", Modifier.weight(1f))
                                BigStat("${snapshot.longestStreak}", "Longest", Modifier.weight(1f))
                            }
                            MiniWeekBars(snapshot.weeklyUsageByDay, height = 96.dp)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                BigStat(formatTrackerDuration(snapshot.weeklyUsageMillis), "This week", Modifier.weight(1f))
                                BigStat(formatTrackerDuration(snapshot.weeklyAverageMillis), "Daily avg", Modifier.weight(1f))
                            }
                        }
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
                        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("✓", fontWeight = FontWeight.Black, modifier = Modifier.width(32.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(completion.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                    Text(formatUpdated(completion.completedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun SidebarAction(title: String, subtitle: String, icon: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontWeight = FontWeight.Black)
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f), RoundedCornerShape(16.dp))
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
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MiniWeekBars(values: List<Long>, height: androidx.compose.ui.unit.Dp = 52.dp) {
    val max = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    Row(
        modifier = Modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEach { value ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight((value.toFloat() / max.toFloat()).coerceIn(0.08f, 1f))
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            )
        }
    }
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
private fun DashboardQuickActions(
    continueDocument: SavedDocument?,
    documentCount: Int,
    draftHasText: Boolean,
    onOpenContinue: (SavedDocument) -> Unit,
    onClearContinue: (SavedDocument) -> Unit,
    onImportClick: () -> Unit,
    onPasteClick: () -> Unit,
    onClearClick: () -> Unit,
    importMenuExpanded: Boolean,
    onDismissImportMenu: () -> Unit,
    onOpenFile: () -> Unit,
    onOpenFileBrowser: () -> Unit,
    onOpenImportSettings: () -> Unit,
    onPasteText: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val disabled = continueDocument == null
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = continueDocument != null) { continueDocument?.let(onOpenContinue) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (disabled) {
                    MaterialTheme.colorScheme.surfaceContainerLow().copy(alpha = 0.62f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ActionIcon(
                    label = "♪",
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    foreground = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Continue Listening", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    if (continueDocument == null) {
                        Text(
                            if (documentCount == 0) "No document currently open" else "Open a reading to resume it here",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "${continueDocument.title} • ${progressPercent(continueDocument)}%",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LinearProgressIndicator(progress = { progressFraction(continueDocument) }, modifier = Modifier.fillMaxWidth())
                    }
                }
                if (continueDocument != null) {
                    TextButton(onClick = { onClearContinue(continueDocument) }) { Text("Clear") }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Box {
                    DashboardActionRow(
                        icon = "⇧",
                        title = "Import",
                        body = "Upload PDF, EPUB, DOCX, TXT, or HTML",
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        iconForeground = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onImportClick
                    )
                    DropdownMenu(expanded = importMenuExpanded, onDismissRequest = onDismissImportMenu) {
                        DropdownMenuItem(text = { Text("Open file") }, onClick = onOpenFile)
                        DropdownMenuItem(text = { Text("File browser") }, onClick = onOpenFileBrowser)
                        DropdownMenuItem(text = { Text("Import settings") }, onClick = onOpenImportSettings)
                        DropdownMenuItem(text = { Text("Paste text or URL") }, onClick = onPasteText)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DashboardActionRow(
                    icon = "▣",
                    title = "Paste",
                    body = "Insert text from clipboard",
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                    iconForeground = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onPasteClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DashboardActionRow(
                    icon = "×",
                    title = "Clear",
                    body = if (continueDocument != null) "Clear current resume point" else if (draftHasText) "Clear pasted draft" else "Workspace is already clear",
                    iconBackground = MaterialTheme.colorScheme.errorContainer,
                    iconForeground = MaterialTheme.colorScheme.onErrorContainer,
                    onClick = onClearClick
                )
            }
        }
    }
}

@Composable
private fun DashboardActionRow(
    icon: String,
    title: String,
    body: String,
    iconBackground: Color,
    iconForeground: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ActionIcon(label = icon, background = iconBackground, foreground = iconForeground)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActionIcon(
    label: String,
    background: Color,
    foreground: Color
) {
    Box(
        modifier = Modifier.size(46.dp).background(background, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = foreground, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun LibrarySearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onSync: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                leadingIcon = { Text("⌕", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                placeholder = { Text("Search library...") },
                singleLine = true,
                shape = RoundedCornerShape(50)
            )
            OutlinedButton(
                onClick = onSync,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                shape = RoundedCornerShape(50)
            ) { Text("↻") }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Recent", "Favorites", "Unread").forEach { option ->
                val active = when (option) {
                    "All" -> statusFilter == "All"
                    "Recent" -> false
                    else -> statusFilter == option
                }
                if (active) {
                    Button(onClick = { onStatusFilterChange(option) }, shape = RoundedCornerShape(50)) {
                        Text(if (option == "All") "All Documents" else option)
                    }
                } else {
                    OutlinedButton(onClick = { onStatusFilterChange(option) }, shape = RoundedCornerShape(50)) {
                        Text(if (option == "All") "All Documents" else option)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilesHomePanel(
    onOpenBrowser: () -> Unit,
    onOpenFile: () -> Unit,
    onImportSettings: () -> Unit,
    onPasteText: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Files", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text(
                "Browse phone storage, approved folders, and importable reading files.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                DashboardActionRow(
                    icon = "⌂",
                    title = "Phone storage",
                    body = "Open the navigable file browser",
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                    iconForeground = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onOpenBrowser
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DashboardActionRow(
                    icon = "+",
                    title = "Import File",
                    body = "Use Android's picker for one document",
                    iconBackground = MaterialTheme.colorScheme.primaryContainer,
                    iconForeground = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onOpenFile
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DashboardActionRow(
                    icon = "Aa",
                    title = "Paste text",
                    body = "Create a reading from clipboard text",
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    iconForeground = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onPasteText
                )
            }
        }
        OutlinedButton(onClick = onImportSettings, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)) {
            Text("PDF and import settings")
        }
    }
}

private fun ColorScheme.surfaceContainerLow(): Color = surfaceVariant.copy(alpha = 0.36f)

@Composable
fun DashboardPanel(
    totalCount: Int,
    unreadCount: Int,
    inProgressCount: Int,
    completedCount: Int,
    favoriteCount: Int,
    queuedCount: Int,
    collectionCount: Int,
    sourceCount: Int,
    onOpenQueue: () -> Unit,
    onOpenFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("A quick view of your reading library", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onOpenFilters) { Text("⌕") }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onOpenQueue, enabled = queuedCount > 0) { Text("▶ $queuedCount") }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(value = "$totalCount", label = "Saved", modifier = Modifier.width(112.dp))
                StatTile(value = "$unreadCount", label = "Unread", modifier = Modifier.width(112.dp))
                StatTile(value = "$inProgressCount", label = "Reading", modifier = Modifier.width(112.dp))
                StatTile(value = "$completedCount", label = "Done", modifier = Modifier.width(112.dp))
                StatTile(value = "$favoriteCount", label = "Starred", modifier = Modifier.width(112.dp))
                StatTile(value = "$collectionCount", label = "Collections", modifier = Modifier.width(132.dp))
                StatTile(value = "$sourceCount", label = "Formats", modifier = Modifier.width(112.dp))
            }
        }
    }
}

@Composable
private fun HomeActionPanel(
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onPrimary, modifier = Modifier.weight(1f)) { Text(primaryLabel) }
                OutlinedButton(onClick = onSecondary, modifier = Modifier.weight(1f)) { Text(secondaryLabel) }
            }
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
    onOpenAt: (Int) -> Unit
) {
    val hasDocumentNote = documentNote.isNotBlank()
    val documentNoteKey = documentNoteStableKey(document.id)
    val selectedDocumentNote = documentNoteKey in selectedKeys
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(document.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                "${annotations.count { it.type == AnnotationType.BOOKMARK }} bookmarks • ${annotations.count { it.type == AnnotationType.NOTE } + if (hasDocumentNote) 1 else 0} notes",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasDocumentNote) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selectedDocumentNote) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                            RoundedCornerShape(14.dp)
                        )
                        .pointerInput(selectionMode, selectedDocumentNote, documentNoteKey) {
                            detectTapGestures(
                                onLongPress = { onLongPressDocumentNote() },
                                onTap = {
                                    if (selectionMode) onToggleDocumentNoteSelected() else onOpenDocumentNote()
                                }
                            )
                        }
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Text(
                        if (selectedDocumentNote) "✓" else "✎",
                        modifier = Modifier.width(30.dp),
                        fontWeight = FontWeight.Black
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("General note", fontWeight = FontWeight.SemiBold)
                        Text(documentNote, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (selectionMode) {
                        TextButton(onClick = onToggleDocumentNoteSelected) { Text(if (selectedDocumentNote) "Selected" else "Select") }
                    } else {
                        TextButton(onClick = onOpenDocumentNote) { Text("Open") }
                    }
                }
            }
            annotations.take(8).forEach { annotation ->
                val selected = annotation.stableKey in selectedKeys
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .pointerInput(selectionMode, selected, annotation.stableKey) {
                            detectTapGestures(
                                onLongPress = { onLongPressAnnotation(annotation) },
                                onTap = {
                                    if (selectionMode) onToggleSelected(annotation) else onOpenAt(annotation.chunkIndex)
                                }
                            )
                        }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (selected) "✓" else if (annotation.type == AnnotationType.BOOKMARK) "★" else "✎",
                        modifier = Modifier.width(30.dp),
                        fontWeight = FontWeight.Black
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sentence ${annotation.chunkIndex + 1}", fontWeight = FontWeight.SemiBold)
                        if (annotation.note.isNotBlank()) {
                            Text(annotation.note, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (selectionMode) {
                        TextButton(onClick = { onToggleSelected(annotation) }) { Text(if (selected) "Selected" else "Select") }
                    } else {
                        TextButton(onClick = { onOpenAt(annotation.chunkIndex) }) { Text("Open") }
                    }
                }
            }
            if (annotations.size > 8) {
                Text("+ ${annotations.size - 8} more", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun LibraryControlsCard(
    query: String,
    onQueryChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    sourceFilter: String,
    onSourceFilterChange: (String) -> Unit,
    collectionFilter: String,
    onCollectionFilterChange: (String) -> Unit,
    sortMode: String,
    onSortModeChange: (String) -> Unit,
    sourceOptions: List<String>,
    collectionOptions: List<String>,
    visibleCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Find and organize", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("$visibleCount of $totalCount readings visible", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = {
                    onQueryChange("")
                    onStatusFilterChange("All")
                    onSourceFilterChange("All")
                    onCollectionFilterChange("All")
                    onSortModeChange("Updated")
                }) { Text("Reset") }
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search library") },
                placeholder = { Text("Title, preview, source, or collection") },
                singleLine = true
            )
            FilterRow(
                title = "Status",
                options = listOf("All", "Favorites", "Queued", "Unread", "In progress", "Completed"),
                selected = statusFilter,
                onSelected = onStatusFilterChange
            )
            FilterRow(
                title = "Source",
                options = sourceOptions,
                selected = sourceFilter,
                onSelected = onSourceFilterChange
            )
            FilterRow(
                title = "Collection",
                options = collectionOptions,
                selected = collectionFilter,
                onSelected = onCollectionFilterChange
            )
            FilterRow(
                title = "Sort",
                options = listOf("Updated", "Newest", "Title", "Progress", "Type"),
                selected = sortMode,
                onSelected = onSortModeChange
            )
        }
    }
}

@Composable
fun FilterRow(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                if (option == selected) {
                    Button(onClick = { onSelected(option) }, contentPadding = ButtonDefaults.ContentPadding) { Text(option) }
                } else {
                    OutlinedButton(onClick = { onSelected(option) }, contentPadding = ButtonDefaults.ContentPadding) { Text(option) }
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Listen Later", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(28.dp),
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
    onShowDetails: () -> Unit
) {
    val progress = progressFraction(document)
    var showActions by remember { mutableStateOf(false) }
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 0.965f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "documentCardSelectionScale"
    )
    val coverSize = when (viewMode) {
        LibraryViewMode.SMALL, LibraryViewMode.LIST -> 46.dp
        LibraryViewMode.DETAILS -> 70.dp
        else -> 64.dp
    }
    val showChips = viewMode == LibraryViewMode.MEDIUM || viewMode == LibraryViewMode.DETAILS
    val showPreview = viewMode == LibraryViewMode.DETAILS

    Card(
        modifier = Modifier
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
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(if (viewMode == LibraryViewMode.LIST) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                        .size(coverSize)
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
                        ),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when {
                        selected -> "✓"
                        document.favorite -> "★"
                        else -> document.sourceLabel.take(3).uppercase()
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${document.sourceLabel} • ${progressPercent(document)}% • ${document.chunkCount} sentences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showPreview) {
                    Text(document.preview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (viewMode != LibraryViewMode.LIST) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (selectionMode) {
                    TextButton(onClick = onToggleSelected) { Text(if (selected) "✓" else "+") }
                } else {
                    TextButton(onClick = onToggleQueue) { Text(if (isQueued) "✓" else "+") }
                    Box {
                        TextButton(onClick = { showActions = true }) { Text("⋮", style = MaterialTheme.typography.titleLarge) }
                        DropdownMenu(
                            expanded = showActions,
                            onDismissRequest = { showActions = false },
                            modifier = Modifier.width(260.dp)
                        ) {
                            DropdownMenuItem(text = { Text("Open reading") }, onClick = { showActions = false; onOpen() })
                            DropdownMenuItem(text = { Text(if (document.favorite) "Remove from favorites" else "Add to favorites") }, onClick = { showActions = false; onToggleFavorite() })
                            DropdownMenuItem(text = { Text(if (isQueued) "Remove from Listen Later" else "Add to Listen Later") }, onClick = { showActions = false; onToggleQueue() })
                            DropdownMenuItem(text = { Text("Move to collection") }, onClick = { showActions = false; onSetCollection() })
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
    modifier: Modifier = Modifier
) {
    var showActions by remember { mutableStateOf(false) }
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 0.965f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "documentTileSelectionScale"
    )
    Card(
        modifier = modifier.graphicsLayer(scaleX = selectionScale, scaleY = selectionScale).pointerInput(selectionMode, selected, document.id) {
            detectTapGestures(
                onLongPress = { onLongPress() },
                onTap = { if (selectionMode) onToggleSelected() else onOpen() }
            )
        },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(98.dp).background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceVariant)),
                    RoundedCornerShape(18.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (selected) "✓" else document.sourceLabel.take(3).uppercase(), fontWeight = FontWeight.Black)
            }
            Text(document.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
            Text("${progressPercent(document)}% • ${document.chunkCount} sentences", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(progress = { progressFraction(document) }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectionMode) {
                    TextButton(onClick = onToggleSelected) { Text(if (selected) "✓" else "+") }
                } else {
                    TextButton(onClick = onToggleQueue) { Text(if (isQueued) "✓" else "+") }
                    Box {
                        TextButton(onClick = { showActions = true }) { Text("⋮") }
                        DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                            DropdownMenuItem(text = { Text("Open reading") }, onClick = { showActions = false; onOpen() })
                            DropdownMenuItem(text = { Text(if (document.favorite) "Remove favorite" else "Add favorite") }, onClick = { showActions = false; onToggleFavorite() })
                            DropdownMenuItem(text = { Text(if (isQueued) "Remove from Listen Later" else "Add to Listen Later") }, onClick = { showActions = false; onToggleQueue() })
                            DropdownMenuItem(text = { Text("Move to collection") }, onClick = { showActions = false; onSetCollection() })
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
