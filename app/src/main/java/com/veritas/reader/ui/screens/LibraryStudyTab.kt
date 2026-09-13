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

import androidx.compose.ui.platform.LocalClipboardManager

@Composable
internal fun LibraryStudyTab(
    documents: List<SavedDocument>,
    uiState: ReaderUiState,
    flashcardSets: List<FlashcardSet>,
    studyListState: LazyListState,
    onRefreshMainPage: () -> Unit,
    annotationFilter: String,
    onAnnotationFilterChange: (String) -> Unit,
    selectedAnnotationKeys: Set<String>,
    onSelectedAnnotationKeysChange: (Set<String>) -> Unit,
    onOpenDocument: (SavedDocument) -> Unit,
    onOpenDocumentAt: (SavedDocument, Int) -> Unit,
    onOpenAiStudyTools: () -> Unit,
    onShowGeminiApiKeyDialog: () -> Unit,
    onShowQuizLabMetrics: () -> Unit,
    onShowPasteQuiz: () -> Unit,
    onShowPasteFlashcards: () -> Unit,
    onPlayQuiz: (QuizSet) -> Unit,
    onDeleteQuiz: (QuizSet) -> Unit,
    onGenerateInAppFlashcards: (SavedDocument, String?) -> Unit,
    onImportFlashcards: (String, List<Flashcard>) -> Unit,
    onSaveQuiz: (QuizSet) -> Unit,
    onDeleteAnnotations: (Set<String>) -> Unit,
    onRemoveVocabularyWord: (String, String) -> Unit,
    onConfirmDeleteVocabDocId: (String) -> Unit,
    onClearReadingHistory: () -> Unit,
    onRemoveReadingHistoryEntry: (String) -> Unit,
    onDeleteFlashcardSet: (String) -> Unit,
    onOpenFlashcardViewer: (String, List<FlashcardProgress>) -> Unit,
    onRenameFlashcardSet: (FlashcardSet) -> Unit,
    onNavigateToTab: (VeritasHomeTab) -> Unit,
    onImportFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var lastMainPageRefreshAt by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var dismissedClipboardSnippet by rememberSaveable { mutableStateOf<String?>(null) }
    var detectedClipboardFlashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var detectedClipboardQuiz by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var expandedVocabDocIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val annotationSelectionMode = selectedAnnotationKeys.isNotEmpty()

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
                    "Booknotes" -> notes
                    else -> markedDoc.annotations
                },
                documentNote = if (annotationFilter == "Bookmarks") "" else markedDoc.documentNote
            )
        }.filter {
            (annotationFilter == "All") || it.annotations.isNotEmpty() || it.documentNote.isNotBlank()
        }
    }

    val bookmarksOnly = remember(filteredAnnotatedDocuments) {
        filteredAnnotatedDocuments.filter { markedDoc -> markedDoc.annotations.any { it.type == AnnotationType.BOOKMARK } }
    }
    val notesOnly = remember(filteredAnnotatedDocuments) {
        filteredAnnotatedDocuments.filter { markedDoc -> markedDoc.annotations.any { it.type == AnnotationType.NOTE } || markedDoc.documentNote.isNotBlank() }
    }

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
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    if (uiState.isGeneratingAiStudy) {
                                        item(key = "study-generating-banner") {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                                ),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                                                    Text(
                                                        text = uiState.aiStudyStatusMessage ?: "AI is preparing study materials…",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (detectedClipboardFlashcards.isNotEmpty() || detectedClipboardQuiz.isNotEmpty()) {
                                        item(key = "study-clipboard-banner") {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                                                ),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Study Content on Clipboard",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = if (detectedClipboardFlashcards.isNotEmpty())
                                                                "${detectedClipboardFlashcards.size} flashcards ready to import"
                                                            else
                                                                "${detectedClipboardQuiz.size} quiz questions ready to import",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                                        )
                                                    }
                                                    Button(
                                                        onClick = {
                                                            if (detectedClipboardFlashcards.isNotEmpty()) {
                                                                onImportFlashcards("Imported Deck", detectedClipboardFlashcards)
                                                            } else if (detectedClipboardQuiz.isNotEmpty()) {
                                                                onSaveQuiz(QuizSet(title = "Imported Quiz", questions = detectedClipboardQuiz))
                                                            }
                                                            dismissedClipboardSnippet = clipboardManager.getText()?.text?.toString()?.take(60)
                                                            detectedClipboardFlashcards = emptyList()
                                                            detectedClipboardQuiz = emptyList()
                                                        },
                                                        shape = RoundedCornerShape(50),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("1-Tap Import", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            dismissedClipboardSnippet = clipboardManager.getText()?.text?.toString()?.take(60)
                                                            detectedClipboardFlashcards = emptyList()
                                                            detectedClipboardQuiz = emptyList()
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Filled.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item(key = "study-hero-daily-review") {
                                        val dueFlashcards = remember(uiState.flashcards) {
                                            uiState.flashcards.filter { SpacedRepetitionScheduler.isDue(it) }
                                        }
                                        val totalFlashcards = remember(uiState.flashcards) { uiState.flashcards.size }
                                        val vocabCount = remember(vocabDocs) { vocabDocs.sumOf { it.third.size } }
                                        val bookmarksCount = remember(uiState.allAnnotations) { uiState.allAnnotations.count { it.type == AnnotationType.BOOKMARK } }
                                        val totalReviewCards = dueFlashcards.size + vocabCount + bookmarksCount
                                        val reviewedCardsCount = remember(uiState.flashcards) {
                                            uiState.flashcards.count { it.recall == "good" || it.recall == "easy" }
                                        }
                                        val reviewCompletionPercent = remember(totalFlashcards, reviewedCardsCount, totalReviewCards) {
                                            if (totalFlashcards > 0) {
                                                ((reviewedCardsCount.toFloat() / totalFlashcards) * 100).toInt().coerceIn(0, 100)
                                            } else if (totalReviewCards == 0) {
                                                100
                                            } else {
                                                0
                                            }
                                        }
                                        StudyDailyReviewHeroCard(
                                            completionPercent = reviewCompletionPercent,
                                            cardsToReview = totalReviewCards,
                                            onStartReview = {
                                                onAnnotationFilterChange(if (uiState.flashcards.isNotEmpty()) "Flashcards" else if (vocabDocs.isNotEmpty()) "Vocab" else "Bookmarks")
                                            }
                                        )
                                    }

                                    item(key = "study-active-decks-header") {
                                        Text(
                                            text = "Active Decks",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (flashcardSets.isNotEmpty()) {
                                        item(key = "study-active-decks-list") {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                flashcardSets.take(4).forEachIndexed { index, set ->
                                                    StudyActiveDeckItem(
                                                        indexNumber = index + 1,
                                                        title = set.name,
                                                        cardCount = set.cards.size,
                                                        icon = if (index % 2 == 0) Icons.AutoMirrored.Filled.LibraryBooks else Icons.Filled.Menu,
                                                        onClick = {
                                                            onOpenFlashcardViewer(set.name, set.cards)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else if (documents.isNotEmpty()) {
                                        item(key = "study-active-decks-list") {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                documents.take(3).forEachIndexed { index, doc ->
                                                    val marks = uiState.allAnnotations.count { it.documentId == doc.id }
                                                    val v = vocabDocs.firstOrNull { it.first.id == doc.id }?.third?.size ?: 0
                                                    val cardCount = marks + v
                                                    StudyActiveDeckItem(
                                                        indexNumber = index + 1,
                                                        title = doc.title,
                                                        cardCount = cardCount,
                                                        icon = if (index % 2 == 0) Icons.AutoMirrored.Filled.LibraryBooks else Icons.Filled.Menu,
                                                        onClick = { onOpenDocument(doc) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item(key = "study-ai-tools-header") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "AI Study Tools",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            FilledTonalButton(
                                                onClick = { onShowGeminiApiKeyDialog() },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Icon(
                                                    Icons.Filled.AutoAwesome,
                                                    contentDescription = "Gemini Setup",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    if (GeminiStudyService.hasApiKey(context)) "Gemini Active" else "Setup AI",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }

                                    item(key = "study-ai-tools-grid") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            StudyAiToolCard(
                                                title = "AI Flashcards",
                                                icon = Icons.Filled.Layers,
                                                onClick = {
                                                    val targetDoc = documents.firstOrNull()
                                                    if (targetDoc != null && GeminiStudyService.hasApiKey(context)) {
                                                        onGenerateInAppFlashcards(targetDoc, null)
                                                    } else {
                                                        onShowPasteFlashcards()
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            StudyAiToolCard(
                                                title = "AI Quiz Lab",
                                                icon = Icons.Outlined.EditNote,
                                                onClick = {
                                                    onShowQuizLabMetrics()
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    item(key = "study-category-filter-chips") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val filterOptions = listOf("Bookmarks", "Booknotes", "Vocab", "Flashcards", "Quizzes", "History")
                                            filterOptions.forEach { option ->
                                                val active = annotationFilter == option
                                                if (active) {
                                                    Button(
                                                        onClick = { onAnnotationFilterChange(option) },
                                                        shape = VeritasPackStyle.chipShape(),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(option, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    OutlinedButton(
                                                        onClick = { onAnnotationFilterChange(option) },
                                                        shape = VeritasPackStyle.chipShape(),
                                                        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(option, style = MaterialTheme.typography.labelMedium)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    when (annotationFilter) {
                "Bookmarks" -> {
                    if (bookmarksOnly.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.Bookmark,
                                title = "No bookmarks yet",
                                description = "Bookmark key passages while reading. Your bookmarked sentences will show up here.",
                                onGoToLibrary = { onNavigateToTab(VeritasHomeTab.LIBRARY) },
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
                "Booknotes" -> {
                    if (notesOnly.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.EditNote,
                                title = "No booknotes yet",
                                description = "Add notes to sentences or write general document notes while reading.",
                                onGoToLibrary = { onNavigateToTab(VeritasHomeTab.LIBRARY) },
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
                                        onSelectedAnnotationKeysChange(if (key in selectedAnnotationKeys) selectedAnnotationKeys - key else selectedAnnotationKeys + key)
                                    },
                                    onLongPressDocumentNote = {
                                        onSelectedAnnotationKeysChange(selectedAnnotationKeys + documentNoteStableKey(doc.id))
                                    },
                                    onToggleSelected = { annotation ->
                                        onSelectedAnnotationKeysChange(if (annotation.stableKey in selectedAnnotationKeys) selectedAnnotationKeys - annotation.stableKey else selectedAnnotationKeys + annotation.stableKey)
                                    },
                                    onLongPressAnnotation = { annotation -> onSelectedAnnotationKeysChange(selectedAnnotationKeys + annotation.stableKey) },
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
                                onGoToLibrary = { onNavigateToTab(VeritasHomeTab.LIBRARY) },
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
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            expandedVocabDocIds = if (isExpanded) {
                                                                expandedVocabDocIds - doc.id
                                                            } else {
                                                                expandedVocabDocIds + doc.id
                                                            }
                                                        },
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
                                                }

                                                var showBatchMenu by remember { mutableStateOf(false) }
                                                Box {
                                                    IconButton(onClick = { showBatchMenu = true }) {
                                                        Icon(
                                                            imageVector = Icons.Filled.MoreVert,
                                                            contentDescription = "Batch Actions",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    DropdownMenu(
                                                        expanded = showBatchMenu,
                                                        onDismissRequest = { showBatchMenu = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Share as PDF") },
                                                            onClick = {
                                                                showBatchMenu = false
                                                                val pdf = StudyGuidePdfExporter.generateVocabularyBatchPdf(context, doc.title, entries)
                                                                if (pdf != null) {
                                                                    StudyGuidePdfExporter.sharePdfFile(context, pdf, "Share Vocabulary PDF")
                                                                }
                                                            },
                                                            leadingIcon = { Icon(Icons.Outlined.Book, contentDescription = null) }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Copy all") },
                                                            onClick = {
                                                                showBatchMenu = false
                                                                val allText = entries.joinToString("\n\n") { e ->
                                                                    buildString {
                                                                        append(e.word)
                                                                        if (!e.pronunciation.isNullOrBlank()) append(" (${e.pronunciation})")
                                                                        append("\n").append(e.explanation)
                                                                        if (!e.contextSentence.isNullOrBlank()) append("\n\"${e.contextSentence}\"")
                                                                    }
                                                                }
                                                                copyTextToClipboard(context, "Vocabulary Batch", allText)
                                                            },
                                                            leadingIcon = { Icon(Icons.Filled.ContentPaste, contentDescription = null) }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Delete all", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                                            onClick = {
                                                                showBatchMenu = false
                                                                onConfirmDeleteVocabDocId(doc.id)
                                                            },
                                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                                                        )
                                                    }
                                                }

                                                IconButton(onClick = {
                                                    expandedVocabDocIds = if (isExpanded) {
                                                        expandedVocabDocIds - doc.id
                                                    } else {
                                                        expandedVocabDocIds + doc.id
                                                    }
                                                }) {
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            if (isExpanded) {
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 14.dp))
                                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    entries.forEach { entry ->
                                                        VocabularyEntryRow(
                                                            entry = entry,
                                                            document = doc,
                                                            onOpenDocumentAt = onOpenDocumentAt,
                                                            onRemoveVocabularyWord = onRemoveVocabularyWord
                                                        )
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
                "Quizzes" -> {
                    studyQuizSection(
                        quizzes = uiState.quizzes,
                        onOpenAiStudyTools = onOpenAiStudyTools,
                        onShowPasteQuiz = { onShowPasteQuiz() },
                        onShowQuizLabMetrics = { onShowQuizLabMetrics() },
                        onPlayQuiz = { onPlayQuiz(it) },
                        onDeleteQuiz = { onDeleteQuiz(it) },
                        onGoToLibrary = { onNavigateToTab(VeritasHomeTab.LIBRARY) }
                    )
                }
                "History" -> {
                    studyHistorySection(
                        readingHistory = uiState.readingHistory,
                        documents = documents,
                        onClearReadingHistory = onClearReadingHistory,
                        onRemoveReadingHistoryEntry = onRemoveReadingHistoryEntry,
                        onOpenDocumentAt = onOpenDocumentAt,
                        onGoToLibrary = { onNavigateToTab(VeritasHomeTab.LIBRARY) },
                        onImportFile = onImportFile
                    )
                }
                "Flashcards" -> {
                    if (flashcardSets.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.Book,
                                title = "No flashcards yet",
                                description = "Open a document → Study Tools → 'Create flashcards' to send a prompt to your AI app, or create flashcard sets directly with AI.",
                                onGoToLibrary = { onNavigateToTab(VeritasHomeTab.LIBRARY) },
                                primaryActionLabel = "Open AI Hub",
                                onPrimaryAction = onOpenAiStudyTools
                            )
                        }
                        item {
                            OutlinedButton(
                                onClick = { onShowPasteFlashcards() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Paste AI reply → add cards")
                            }
                        }
                    } else {
                        item {
                            Button(
                                onClick = { onShowPasteFlashcards() },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text("Paste AI reply → add cards")
                            }
                        }
                        // Two set tiles per row, styled like the old deck card.
                        items(flashcardSets.chunked(2), key = { row -> row.first().setId }) { row ->
                            // Match the library cover grid: 2 columns, 12dp gap, 10dp row gap.
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { set ->
                                    FlashcardSetTile(
                                        set = set,
                                        modifier = Modifier.weight(1f),
                                        onOpen = {
                                            onOpenFlashcardViewer(set.name, set.cards)
                                        },
                                        onViewBucket = { bucket ->
                                            onOpenFlashcardViewer("${set.name} · ${bucket.replaceFirstChar { it.uppercase() }}", set.cards.filter { it.recall == bucket })
                                        },
                                        onRename = { onRenameFlashcardSet(set) },
                                        onDelete = { onDeleteFlashcardSet(set.setId) }
                                    )
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

}
