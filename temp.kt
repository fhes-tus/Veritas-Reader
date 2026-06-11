package com.veritas.reader.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.ActionMode
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import android.graphics.pdf.PdfRenderer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import com.veritas.reader.*


data class ReaderScreenState(
    val document: ReaderDocument,
    val currentIndex: Int,
    val isPlaying: Boolean,
    val isBackgroundActive: Boolean,
    val rate: Float,
    val pitch: Float,
    val statusMessage: String,
    val queueCount: Int,
    val isQueued: Boolean,
    val annotations: List<ReaderAnnotation>,
    val pronunciationRuleCount: Int,
    val readerSettings: ReaderSettings,
    val voiceSettings: VoiceSettings,
    val narrationSettings: NarrationSettings,
    val askAiSettings: AskAiSettings,
    val searchQuery: String,
    val searchMatches: List<Int>,
    val searchCursor: Int,
    val outlineEntries: List<VeritasDocumentOutlineEntry>,
    val hasCanvas: Boolean,
    val sleepTimerDurationMillis: Long,
    val sleepTimerEndsAtMillis: Long,
    val sleepTimerAction: VeritasSleepTimerAction,
    val readingListCount: Int,
    val activeDocumentReadingListCount: Int
)

@Composable
fun ReaderScreen(
    state: ReaderScreenState,
    listState: LazyListState,

    hasCanvas: Boolean,
    onBackToLibrary: () -> Unit,
    onSentenceClick: (Int) -> Unit,
    onSentenceDoubleTap: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onToggleQueue: () -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onEditNote: (Int) -> Unit,
    onEditNotes: (List<Int>) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNextSearchMatch: () -> Unit,
    onPreviousSearchMatch: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenPronunciationRules: () -> Unit,
    onOpenVoiceStudio: () -> Unit,
    onOpenNarrationStudio: () -> Unit,
    onOpenDocumentNotes: () -> Unit,
    onOpenCanvas: () -> Unit,
    onOpenStudyTools: () -> Unit,
    onOpenTranslationTools: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenReadingLists: () -> Unit,
    onOpenReadingHistory: () -> Unit,
    onAskCurrentSection: () -> Unit,
    onSelectAskAiAssistant: (AiAssistantOption, String) -> Unit,
    onOpenTextEditor: () -> Unit,
    onStartRecord: () -> Unit,
    onExportAudio: () -> Unit,
    onCopySelection: (String) -> Unit,
    onShareSelection: (String) -> Unit,
    onGoogleSelection: (String) -> Unit,
    onTranslateSelection: (String) -> Unit,
    onAskAiSelection: (String) -> Unit,
    onEditSpeechSelection: (String) -> Unit,
    onReadSelection: (String) -> Unit,
    onEditExtractedSelection: (ReaderTextSelection) -> Unit,
    onPlayQueue: () -> Unit
) {
    val document = state.document
    val currentIndex = state.currentIndex
    val isPlaying = state.isPlaying
    val isBackgroundActive = state.isBackgroundActive
    val rate = state.rate
    val pitch = state.pitch
    val statusMessage = state.statusMessage
    val queueCount = state.queueCount
    val isQueued = state.isQueued
    val annotations = state.annotations
    val pronunciationRuleCount = state.pronunciationRuleCount
    val readerSettings = state.readerSettings
    val voiceSettings = state.voiceSettings
    val narrationSettings = state.narrationSettings
    val askAiSettings = state.askAiSettings
    val sleepTimerSnapshot = VeritasSleepTimerSnapshot(
        durationMillis = state.sleepTimerDurationMillis,
        endsAtMillis = state.sleepTimerEndsAtMillis,
        action = state.sleepTimerAction
    ).takeIf { state.sleepTimerDurationMillis > 0L && it.isActive() }
    val searchQuery = state.searchQuery
    val searchMatches = state.searchMatches
    val searchCursor = state.searchCursor
    val hasCanvas = state.hasCanvas
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    var showTools by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showOutline by remember { mutableStateOf(false) }
    var selectedTextSelection by remember(document.id) { mutableStateOf<ReaderTextSelection?>(null) }
    var selectedTextView by remember(document.id) { mutableStateOf<TextView?>(null) }
    var feedbackSentenceIndex by remember(document.id) { mutableStateOf<Int?>(null) }

    val readerModel = remember(document.rawText, document.pageCount, document.chunks.size) {
        ReaderTextModelCache.get(document.id, document.rawText, document.pageCount)
    }
    val currentPart = readerModel.partForSentence(currentIndex)
    val currentPartIndex = currentPart?.index ?: 0
    val progress = if (document.chunks.isEmpty()) 0f else ((currentIndex + 1).toFloat() / document.chunks.size.toFloat()).coerceIn(0f, 1f)
    val progressLabel = if (document.chunks.isEmpty()) {
        "0 / 0"
    } else {
        "Part ${currentPartIndex + 1}/${readerModel.parts.size.coerceAtLeast(1)} ÔÇó Sentence ${currentIndex + 1}/${document.chunks.size}"
    }
    val bookmarkCount = annotations.count { it.type == AnnotationType.BOOKMARK }
    val noteCount = annotations.count { it.type == AnnotationType.NOTE }
    val canGoPreviousPart = currentPartIndex > 0
    val canGoNextPart = currentPartIndex < readerModel.parts.lastIndex
    val previousPartStart = readerModel.parts.getOrNull(currentPartIndex - 1)?.sentenceStartIndex ?: 0
    val nextPartStart = readerModel.parts.getOrNull(currentPartIndex + 1)?.sentenceStartIndex ?: document.chunks.lastIndex
    val partListItemIndex = 1

    LaunchedEffect(feedbackSentenceIndex) {
        if (feedbackSentenceIndex != null) {
            delay(420)
            feedbackSentenceIndex = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.statusBarsPadding().background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBackToLibrary) { Text("ÔåÉ", style = MaterialTheme.typography.titleLarge) }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        document.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "${document.sourceLabel} ÔÇó $progressLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val toolbarIconPadding = PaddingValues(horizontal = 3.dp, vertical = 2.dp)
                TextButton(onClick = { showSearch = !showSearch }, contentPadding = toolbarIconPadding) { Text("Ôîò", style = MaterialTheme.typography.titleMedium) }
                if (hasCanvas) TextButton(onClick = onOpenCanvas, contentPadding = toolbarIconPadding) { Text("Ôûú", style = MaterialTheme.typography.titleMedium) }
                TextButton(onClick = { showOutline = true }, contentPadding = toolbarIconPadding) { Text("ÔÿÀ", style = MaterialTheme.typography.titleMedium) }
                TextButton(onClick = onOpenDocumentNotes, contentPadding = toolbarIconPadding) { Text("Ôÿà", style = MaterialTheme.typography.titleMedium) }
                Box {
                    TextButton(onClick = { showTools = true }, contentPadding = toolbarIconPadding) { Text("Ôï«", style = MaterialTheme.typography.titleMedium) }
                    ReaderToolsMenu(
                        expanded = showTools,
                        onDismiss = { showTools = false },
                        summary = "${document.sourceLabel} ÔÇó $progressLabel ÔÇó $bookmarkCount bookmarks ÔÇó $noteCount notes",
                        showSearch = showSearch,
                        showBookmarks = showBookmarks,
                        hasCanvas = hasCanvas,
                        noteCount = noteCount,
                        narrationEnabled = narrationSettings.enabled,
                        isQueued = isQueued,
                        queueCount = queueCount,
                        askAiSettings = askAiSettings,
                        onToggleSearch = { showSearch = !showSearch },
                        onToggleBookmarks = { showBookmarks = !showBookmarks },
                        onOpenDocumentNotes = onOpenDocumentNotes,
                        onOpenCanvas = onOpenCanvas,
                        onOpenStudyTools = onOpenStudyTools,
                        onOpenTranslationTools = onOpenTranslationTools,
                        sleepTimerLabel = sleepTimerSnapshot?.menuLabel().orEmpty(),
                        onOpenSleepTimer = onOpenSleepTimer,
                        readingListCount = state.readingListCount,
                        activeDocumentReadingListCount = state.activeDocumentReadingListCount,
                        onOpenReadingLists = onOpenReadingLists,
                        onOpenReadingHistory = onOpenReadingHistory,
                        onAskCurrentSection = onAskCurrentSection,
                        onSelectAskAiAssistant = onSelectAskAiAssistant,
                        onOpenTextEditor = onOpenTextEditor,
                        onStartRecord = onStartRecord,
                        onOpenReaderSettings = onOpenReaderSettings,
                        onOpenVoiceStudio = onOpenVoiceStudio,
                        onOpenNarrationStudio = onOpenNarrationStudio,
                        onOpenPronunciationRules = onOpenPronunciationRules,
                        onExportAudio = onExportAudio,
                        onToggleQueue = onToggleQueue,
                        onPlayQueue = onPlayQueue
                    )
                }
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }

        HorizontalDivider()

        if (document.chunks.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No readable text found.")
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(selectedTextSelection) {
                        if (selectedTextSelection != null) {
                            detectTapGestures(onTap = {
                                clearNativeTextSelection(selectedTextView)
                                selectedTextSelection = null
                            })
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(bottom = 450.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(6.dp)) }
                    item(key = "part-${currentPart?.index ?: 0}") {
                        val part = currentPart
                        if (part == null) {
                            Text("No readable text found.", modifier = Modifier.padding(18.dp))
                        } else {
                            var showPartMenu by remember(document.id, part.index) { mutableStateOf(false) }
                            val sentenceBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            val highlightBackground = MaterialTheme.colorScheme.tertiaryContainer
                            val feedbackBackground = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                            val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                            val activeSentenceColor = sentenceBackground.toArgb()
                            val highlightColor = highlightBackground.toArgb()
                            val feedbackColor = feedbackBackground.toArgb()
                            val activeRange = part.sentenceRanges.firstOrNull { it.sentenceIndex == currentIndex }
                            LaunchedEffect(part.index, currentIndex, activeRange?.start, activeRange?.endExclusive, selectedTextView, partListItemIndex, readerSettings.fontSizeSp) {
                                val textView = selectedTextView
                                val range = activeRange
                                if (textView != null && range != null) {
                                    val topPaddingPx = with(density) { 132.dp.toPx().roundToInt() }
                                    listOf(0L, 90L, 220L).forEach { settleDelay ->
                                        if (settleDelay > 0L) delay(settleDelay)
                                        val layout = textView.layout ?: return@forEach
                                        val textLength = textView.text?.length ?: part.text.length
                                        val targetLine = layout.getLineForOffset(range.start.coerceIn(0, textLength.coerceAtLeast(0)))
                                        val targetTop = layout.getLineTop(targetLine).coerceAtLeast(0)
                                        val targetBottom = layout.getLineBottom(targetLine).coerceAtLeast(targetTop + 1)
                                        listState.scrollToItem(partListItemIndex, (targetTop - topPaddingPx).coerceAtLeast(0))
                                    }
                                }
                            }
                            val bookmarkedSentenceIndexes = annotations
                                .filter { it.type == AnnotationType.BOOKMARK }
                                .map { it.chunkIndex }
                                .toSet()
                            val bookmarked = annotations.any { it.type == AnnotationType.BOOKMARK && it.chunkIndex in part.sentenceStartIndex until part.sentenceEndIndexExclusive }
                            val note = annotations.firstOrNull { it.type == AnnotationType.NOTE && it.chunkIndex == currentIndex }
                            val renderedPart = remember(
                                part.text,
                                currentIndex,
                                isPlaying,
                                feedbackSentenceIndex,
                                bookmarkedSentenceIndexes,
                                activeSentenceColor,
                                highlightColor,
                                feedbackColor
                            ) {
                                buildReaderPartSpannable(
                                    part = part,
                                    activeSentenceIndex = if (isPlaying) currentIndex else null,
                                    feedbackSentenceIndex = feedbackSentenceIndex,
                                    highlightedSentenceIndexes = bookmarkedSentenceIndexes,
                                    activeSentenceColor = activeSentenceColor,
                                    highlightColor = highlightColor,
                                    feedbackColor = feedbackColor
                                )
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = (12 + readerSettings.sectionSpacingDp / 4).dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (readerSettings.showSectionNumbers) {
                                            Text(
                                                "Part ${part.index + 1} of ${readerModel.parts.size} ÔÇó pages ${part.pageRange.startPage}-${part.pageRange.endPage}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                        if (bookmarked) AnnotationPill("Ôÿà")
                                        if (note != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AnnotationPill("Ô£Ä")
                                        }
                                        Box {
                                            TextButton(onClick = { showPartMenu = true }) { Text("Ôï«") }
                                            DropdownMenu(expanded = showPartMenu, onDismissRequest = { showPartMenu = false }) {
                                                Text(
                                                    "Part ${part.index + 1}",
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Black
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("ÔûÂ Play from part start") },
                                                    onClick = {
                                                        showPartMenu = false
                                                        onSentenceClick(part.sentenceStartIndex)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Ôÿà Bookmark current sentence") },
                                                    onClick = {
                                                        showPartMenu = false
                                                        onToggleBookmark(currentIndex)
                                                        showBookmarks = true
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Ô£Ä Note current sentence") },
                                                    onClick = {
                                                        showPartMenu = false
                                                        onEditNote(currentIndex)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    AndroidView(
                                        modifier = Modifier.fillMaxWidth(),
                                        factory = { viewContext ->
                                            TextView(viewContext).apply {
                                                setTextIsSelectable(true)
                                                includeFontPadding = true
                                            }
                                        },
                                        update = { textView ->
                                            selectedTextView = textView
                                            textView.text = renderedPart
                                            textView.setTextColor(textColor)
                                            textView.textSize = readerSettings.fontSizeSp.toFloat()
                                            textView.setLineSpacing(with(density) { 4.dp.toPx() }, 1.0f)
                                            textView.customSelectionActionModeCallback = readerSelectionActionModeCallback(
                                                textView = textView,
                                                part = part,
                                                documentId = document.id,
                                                context = context,
                                                haptics = haptics,
                                                bookmarkedSentenceIndexes = bookmarkedSentenceIndexes,
                                                onSelectionChanged = { },
                                                onSearchQueryChange = {
                                                    onSearchQueryChange(it)
                                                    showSearch = true
                                                },
                                                onToggleBookmark = { idx ->
                                                    onToggleBookmark(idx)
                                                    showBookmarks = true
                                                },
                                                onEditNotes = onEditNotes,
                                                onTranslateSelection = onTranslateSelection,
                                                onCopySelection = onCopySelection,
                                                onGoogleSelection = onGoogleSelection,
                                                onShareSelection = onShareSelection,
                                                onEditSpeechSelection = onEditSpeechSelection,
                                                onEditExtractedSelection = onEditExtractedSelection,
                                                onAskAiSelection = onAskAiSelection,
                                                onReadSelection = onReadSelection
                                            )
                                            val detector = GestureDetector(
                                                textView.context,
                                                object : GestureDetector.SimpleOnGestureListener() {
                                                    override fun onDoubleTap(event: MotionEvent): Boolean {
                                                        val offset = textView.getOffsetForPosition(event.x, event.y).coerceIn(0, part.text.length)
                                                        readerModel.sentenceForPartOffset(part, offset)?.let { range ->
                                                             haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                             feedbackSentenceIndex = range.sentenceIndex
                                                             onSentenceDoubleTap(range.sentenceIndex)
                                                         }
                                                          return true
                                                     }
                                                }
                                            )
                                            textView.setOnTouchListener { _, event ->
                                                detector.onTouchEvent(event)
                                                false
                                            }
                                        }
                                    )

                                    if (!note?.note.isNullOrBlank()) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Note", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                Text(note.note, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Text(
                                        "(Part ${part.index + 1} of ${readerModel.parts.size})",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { onSentenceClick(previousPartStart) },
                                            enabled = canGoPreviousPart
                                        ) { Text("ÔÅ¬", style = MaterialTheme.typography.titleLarge) }
                                        Spacer(modifier = Modifier.width(18.dp))
                                        TextButton(
                                            onClick = { onSentenceClick(nextPartStart) },
                                            enabled = canGoNextPart
                                        ) { Text("ÔÅ®", style = MaterialTheme.typography.titleLarge) }
                                    }
                                }
                            }
                        }
                    }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
                if (showSearch) {
                    SearchPanel(
                        query = searchQuery,
                        matchCount = searchMatches.size,
                        currentMatch = if (searchMatches.isEmpty()) 0 else searchCursor + 1,
                        onQueryChange = onSearchQueryChange,
                        onPrevious = onPreviousSearchMatch,
                        onNext = onNextSearchMatch,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                    )
                }
        }
        }

        PlayerPanel(
            isPlaying = isPlaying,
            isBackgroundActive = isBackgroundActive,
            statusMessage = statusMessage,
            rate = rate,
            pitch = pitch,
            fontSizeSp = readerSettings.fontSizeSp,
            queueCount = queueCount,
            canGoPrevious = currentIndex > 0,
            canGoNext = currentIndex < document.chunks.lastIndex || queueCount > 0,
            onPrevious = onPrevious,
            onPlayPause = onPlayPause,
            onStop = onStop,
            onNext = onNext,
            onRateChange = onRateChange,
            onPitchChange = onPitchChange,
            onFontSizeChange = onFontSizeChange,
            onOpenVoiceStudio = onOpenVoiceStudio,
            onPlayQueue = onPlayQueue
        )
    }

    if (showOutline) {
        SmartOutlineDialog(
            document = document,
            documentOutline = state.outlineEntries,
            currentIndex = currentIndex,
            onJumpToSection = { index ->
                showOutline = false
                onSentenceClick(index)
            },
            onDismiss = { showOutline = false }
        )
    }

    if (showBookmarks) {
        BookmarksOverviewDialog(
            document = document,
            annotations = annotations,
            onJumpToSection = { index ->
                showBookmarks = false
                onSentenceClick(index)
            },
            onDismiss = { showBookmarks = false }
        )
    }
}

@Composable
private fun ReaderToolsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    summary: String,
    showSearch: Boolean,
    showBookmarks: Boolean,
    hasCanvas: Boolean,
    noteCount: Int,
    narrationEnabled: Boolean,
    isQueued: Boolean,
    queueCount: Int,
    askAiSettings: AskAiSettings,
    onToggleSearch: () -> Unit,
    onToggleBookmarks: () -> Unit,
    onOpenDocumentNotes: () -> Unit,
    onOpenCanvas: () -> Unit,
    onOpenStudyTools: () -> Unit,
    onOpenTranslationTools: () -> Unit,
    sleepTimerLabel: String,
    onOpenSleepTimer: () -> Unit,
    readingListCount: Int,
    activeDocumentReadingListCount: Int,
    onOpenReadingLists: () -> Unit,
    onOpenReadingHistory: () -> Unit,
    onAskCurrentSection: () -> Unit,
    onSelectAskAiAssistant: (AiAssistantOption, String) -> Unit,
    onOpenTextEditor: () -> Unit,
    onStartRecord: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenVoiceStudio: () -> Unit,
    onOpenNarrationStudio: () -> Unit,
    onOpenPronunciationRules: () -> Unit,
    onExportAudio: () -> Unit,
    onToggleQueue: () -> Unit,
    onPlayQueue: () -> Unit
) {
    val context = LocalContext.current
    var showAiChooser by remember { mutableStateOf(false) }
    val readerFeatures = remember(queueCount) {
        VeritasFeatureRegistry.resolve(
            VeritasFeatureSurface.READER_OVERFLOW,
            VeritasFeatureContext(
                hasActiveDocument = true,
                hasSavedDocument = true,
                queueCount = queueCount
            )
        ).associateBy { it.definition.id }
    }

    fun readerFeature(id: VeritasFeatureId): ResolvedVeritasFeature =
        readerFeatures.requireResolvedFeature(id)

    fun choose(action: () -> Unit) {
        onDismiss()
        action()
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(320.dp)
    ) {
        Text(
            "Reader tools",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black
        )
        Text(
            summary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text("Read", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        DropdownMenuItem(text = { Text(if (showSearch) "Ôîò Hide search" else "Ôîò Search document") }, onClick = { choose(onToggleSearch) })
        DropdownMenuItem(text = { Text("Ôûú Actual document") }, enabled = hasCanvas, onClick = { choose(onOpenCanvas) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.SLEEP_TIMER),
            label = if (sleepTimerLabel.isBlank()) "ÔÅ▒ Sleep timer" else "ÔÅ▒ $sleepTimerLabel",
            onClick = { choose(onOpenSleepTimer) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.READING_LISTS),
            label = "Reading lists ($activeDocumentReadingListCount/$readingListCount)",
            onClick = { choose(onOpenReadingLists) }
        )
        DropdownMenuItem(text = { Text(if (isQueued) "Ô£ô Remove from Listen Later" else "+ Add to Listen Later") }, onClick = { choose(onToggleQueue) })
        DropdownMenuItem(text = { Text("ÔûÂ Play Listen Later ($queueCount)") }, enabled = queueCount > 0, onClick = { choose(onPlayQueue) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.READING_HISTORY),
            label = "Ôå║ Reading history",
            onClick = { choose(onOpenReadingHistory) }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text("Notes and bookmarks", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        DropdownMenuItem(text = { Text(if (showBookmarks) "Ôÿà Hide bookmarks" else "Ôÿà Bookmarks") }, onClick = { choose(onToggleBookmarks) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.BOOKMARKS_AND_NOTES),
            label = "Ô£Ä Document notes${if (noteCount > 0) " ÔÇó $noteCount sentence${if (noteCount == 1) "" else "s"}" else ""}",
            onClick = { choose(onOpenDocumentNotes) }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text("Study", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        DropdownMenuItem(
            text = { Text("AI Assistant: ${askAiSettings.assistantLabel}") },
            onClick = { showAiChooser = !showAiChooser }
        )
        if (showAiChooser) {
            aiAssistantOptions.filter { it.packageName.isNotBlank() }.forEach { option ->
                val installedPackage = installedPackageForOption(context, option)
                val isSelected = askAiSettings.assistantId == option.id
                DropdownMenuItem(
                    text = {
                        Text(
                            "${if (isSelected) "Ô£ô " else ""}${option.label}${if (installedPackage == null) " ÔÇó install" else ""}",
                            color = if (installedPackage == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        if (installedPackage != null) {
                            onSelectAskAiAssistant(option, installedPackage)
                            showAiChooser = false
                        } else {
                            openPlayStoreForPackage(context, option.packageName)
                        }
                    }
                )
            }
        }
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.AI_APP_HANDOFF),
            label = "AI Ask current part",
            onClick = { choose(onAskCurrentSection) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.OFFLINE_STUDY_TOOLS),
            label = "AI Study tools",
            onClick = { choose(onOpenStudyTools) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.TRANSLATION_HANDOFF),
            label = "Ôçä Translation handoff",
            onClick = { choose(onOpenTranslationTools) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.EXTRACTED_TEXT_EDITOR),
            label = "T+ Edit extracted text",
            onClick = { choose(onOpenTextEditor) }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text("Voice and settings", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.VOICE_STUDIO),
            label = "­ƒÄÖ Voice and language",
            onClick = { choose(onOpenVoiceStudio) }
        )
        DropdownMenuItem(text = { Text(if (narrationEnabled) "­ƒÄ¡ Narration mode on" else "­ƒÄ¡ Narration mode") }, onClick = { choose(onOpenNarrationStudio) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.PRONUNCIATION_RULES),
            label = "Aa Pronunciation rules",
            onClick = { choose(onOpenPronunciationRules) }
        )
        DropdownMenuItem(text = { Text("ÔÜÖ Reader appearance") }, onClick = { choose(onOpenReaderSettings) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT),
            label = "ÔùÅ Record sound file",
            onClick = { choose(onStartRecord) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT),
            label = "Ôç® Export audio",
            onClick = { choose(onExportAudio) }
        )
    }
}

@Composable
internal fun FeatureDropdownMenuItem(
    feature: ResolvedVeritasFeature,
    label: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { FeatureMenuText(feature, label) },
        enabled = feature.enabled,
        onClick = onClick
    )
}

@Composable
private fun FeatureMenuText(feature: ResolvedVeritasFeature, label: String) {
    Column {
        Text(label)
        if (!feature.enabled && feature.disabledReason != null) {
            Text(
                feature.disabledReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SleepTimerDialog(
    activeTimer: VeritasSleepTimerSnapshot?,
    onSetTimer: (VeritasSleepTimerRequest) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDurationMillis by remember {
        mutableLongStateOf(activeTimer?.durationMillis ?: 15L * 60L * 1000L)
    }
    var selectedAction by remember {
        mutableStateOf(activeTimer?.action ?: VeritasSleepTimerAction.PAUSE)
    }
    val activeLabel = activeTimer?.takeIf { it.isActive() }?.menuLabel()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep timer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (activeLabel != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(activeLabel, fontWeight = FontWeight.Black)
                            Text(activeTimer.action.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                VeritasSleepTimerPresets.durationsMillis.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { duration ->
                            val selected = selectedDurationMillis == duration
                            val content: @Composable () -> Unit = {
                                Text(VeritasSleepTimerFormatter.formatDuration(duration))
                            }
                            if (selected) {
                                Button(
                                    onClick = { selectedDurationMillis = duration },
                                    modifier = Modifier.weight(1f)
                                ) { content() }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedDurationMillis = duration },
                                    modifier = Modifier.weight(1f)
                                ) { content() }
                            }
                        }
                    }
                }

                VeritasSleepTimerAction.entries.forEach { action ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selectedAction = action },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedAction == action, onClick = { selectedAction = action })
                        Text(action.label)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSetTimer(
                        VeritasSleepTimerRequest(
                            durationMillis = selectedDurationMillis,
                            action = selectedAction
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (activeTimer != null) {
                    TextButton(
                        onClick = {
                            onCancelTimer()
                            onDismiss()
                        }
                    ) {
                        Text("Cancel timer")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
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

private fun buildReaderTextSelection(
    part: ReaderPart,
    rawStart: Int,
    rawEnd: Int
): ReaderTextSelection? {
    val source = part.text
    if (source.isBlank()) return null
    val anchor = rawStart.coerceIn(0, source.length)
    val focus = rawEnd.coerceIn(0, source.length)
    var start = minOf(anchor, focus)
    var endExclusive = maxOf(anchor, focus)

    while (start < endExclusive && source[start].isWhitespace()) start++
    while (endExclusive > start && source[endExclusive - 1].isWhitespace()) endExclusive--

    if (start >= endExclusive) return null
    val selected = source.substring(start, endExclusive)
    if (selected.isBlank()) return null
    val sentenceIndexes = part.sentenceRanges
        .filter { range -> range.endExclusive > start && range.start < endExclusive }
        .map { it.sentenceIndex }
        .distinct()
    if (sentenceIndexes.isEmpty()) return null
    return ReaderTextSelection(part.index, start, endExclusive, selected, sentenceIndexes)
}

private fun buildReaderPartSpannable(
    part: ReaderPart,
    activeSentenceIndex: Int?,
    feedbackSentenceIndex: Int?,
    highlightedSentenceIndexes: Set<Int>,
    activeSentenceColor: Int,
    highlightColor: Int,
    feedbackColor: Int
): SpannableString {
    val spannable = SpannableString(part.text)
    fun addBackground(start: Int, endExclusive: Int, color: Int) {
        if (start < endExclusive && start in 0..part.text.length && endExclusive in 0..part.text.length) {
            spannable.setSpan(BackgroundColorSpan(color), start, endExclusive, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    part.sentenceRanges.forEach { range ->
        if (range.sentenceIndex in highlightedSentenceIndexes) {
            addBackground(range.start, range.endExclusive, highlightColor)
        }
        if (range.sentenceIndex == feedbackSentenceIndex) {
            addBackground(range.start, range.endExclusive, feedbackColor)
        }
        if (range.sentenceIndex == activeSentenceIndex) {
            addBackground(range.start, range.endExclusive, activeSentenceColor)
        }
    }
    return spannable
}

private fun clearNativeTextSelection(textView: TextView?) {
    val text = textView?.text
    if (text is android.text.Spannable) {
        android.text.Selection.removeSelection(text)
    }
    textView?.clearFocus()
}

private fun readerSelectionActionModeCallback(
    textView: TextView,
    part: ReaderPart,
    documentId: String?,
    context: Context,
    haptics: HapticFeedback,
    bookmarkedSentenceIndexes: Set<Int>,
    onSelectionChanged: (ReaderTextSelection?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onEditNotes: (List<Int>) -> Unit,
    onTranslateSelection: (String) -> Unit,
    onCopySelection: (String) -> Unit,
    onGoogleSelection: (String) -> Unit,
    onShareSelection: (String) -> Unit,
    onEditSpeechSelection: (String) -> Unit,
    onEditExtractedSelection: (ReaderTextSelection) -> Unit,
    onAskAiSelection: (String) -> Unit,
    onReadSelection: (String) -> Unit
): ActionMode.Callback {
    fun currentSelection(): ReaderTextSelection? =
        buildReaderTextSelection(part, textView.selectionStart, textView.selectionEnd)

    fun finish(mode: ActionMode, selection: ReaderTextSelection? = null) {
        onSelectionChanged(selection)
        mode.finish()
    }

    return object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            onSelectionChanged(currentSelection())
            val selectionFeatures = VeritasFeatureRegistry.resolve(
                VeritasFeatureSurface.SELECTION_OVERFLOW,
                VeritasFeatureContext(
                    hasActiveDocument = !documentId.isNullOrBlank(),
                    hasTextSelection = true,
                    hasSavedDocument = !documentId.isNullOrBlank()
                )
            ).associateBy { it.definition.id }
            menu.add(0, READER_SELECTION_READ_FROM_HERE, 0, "Read from here").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.addReaderSelectionFeature(
                itemId = READER_SELECTION_BOOKMARK,
                order = 1,
                title = "Bookmark",
                feature = selectionFeatures.requireResolvedFeature(VeritasFeatureId.BOOKMARKS_AND_NOTES),
                showAsAction = MenuItem.SHOW_AS_ACTION_IF_ROOM
            )
            menu.addReaderSelectionFeature(
                itemId = READER_SELECTION_NOTE,
                order = 2,
                title = "Note",
                feature = selectionFeatures.requireResolvedFeature(VeritasFeatureId.BOOKMARKS_AND_NOTES),
                showAsAction = MenuItem.SHOW_AS_ACTION_IF_ROOM
            )
            menu.add(0, READER_SELECTION_COPY, 4, "Copy")
            menu.add(0, READER_SELECTION_SEARCH, 5, "Search")
            menu.addReaderSelectionFeature(
                itemId = READER_SELECTION_TRANSLATE,
                order = 6,
                title = "Translate",
                feature = selectionFeatures.requireResolvedFeature(VeritasFeatureId.TRANSLATION_HANDOFF)
            )
            menu.add(0, READER_SELECTION_GOOGLE, 8, "Web lookup")
            menu.addReaderSelectionFeature(
                itemId = READER_SELECTION_EDIT_TEXT,
                order = 9,
                title = "Edit selected text",
                feature = selectionFeatures.requireResolvedFeature(VeritasFeatureId.EXTRACTED_TEXT_EDITOR)
            )
            menu.addReaderSelectionFeature(
                itemId = READER_SELECTION_EDIT_SPEECH,
                order = 10,
                title = "Edit speech",
                feature = selectionFeatures.requireResolvedFeature(VeritasFeatureId.PRONUNCIATION_RULES)
            )
            menu.add(0, READER_SELECTION_READ_ALOUD, 11, "Read aloud")
            menu.addReaderSelectionFeature(
                itemId = READER_SELECTION_ASK_AI,
                order = 12,
                title = "Ask AI",
                feature = selectionFeatures.requireResolvedFeature(VeritasFeatureId.AI_APP_HANDOFF)
            )
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            onSelectionChanged(currentSelection())
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val selection = currentSelection() ?: return false
            onSelectionChanged(selection)
            when (item.itemId) {
                READER_SELECTION_READ_FROM_HERE -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!documentId.isNullOrBlank()) {
                        val intent = Intent(context, PlaybackService::class.java).apply {
                            action = PlaybackActions.ACTION_JUMP_TO
                            putExtra(PlaybackActions.EXTRA_DOCUMENT_ID, documentId)
                            putExtra(PlaybackActions.EXTRA_START_INDEX, selection.firstSentenceIndex)
                            putExtra(PlaybackActions.EXTRA_CHAR_OFFSET, 0)
                        }
                        context.startService(intent)
                    }
                }
                READER_SELECTION_NOTE -> onEditNotes(selection.sentenceIndexes)
                READER_SELECTION_BOOKMARK -> {
                    val anyUnbookmarked = selection.sentenceIndexes.any { it !in bookmarkedSentenceIndexes }
                    if (anyUnbookmarked) {
                        selection.sentenceIndexes.filter { it !in bookmarkedSentenceIndexes }.forEach(onToggleBookmark)
                    } else {
                        selection.sentenceIndexes.forEach(onToggleBookmark)
                    }
                }
                READER_SELECTION_COPY -> onCopySelection(selection.text)
                READER_SELECTION_SEARCH -> onSearchQueryChange(selection.text.take(80))
                READER_SELECTION_TRANSLATE -> onTranslateSelection(selection.text)
                READER_SELECTION_GOOGLE -> onGoogleSelection(selection.text)
                READER_SELECTION_EDIT_TEXT -> onEditExtractedSelection(selection)
                READER_SELECTION_EDIT_SPEECH -> onEditSpeechSelection(selection.text)
                READER_SELECTION_READ_ALOUD -> onReadSelection(selection.text)
                READER_SELECTION_ASK_AI -> onAskAiSelection(selection.text)
                else -> return false
            }
            finish(mode)
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            onSelectionChanged(null)
        }
    }
}

private fun Menu.addReaderSelectionFeature(
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

private const val READER_SELECTION_READ_FROM_HERE = 6101
private const val READER_SELECTION_NOTE = 6103
private const val READER_SELECTION_BOOKMARK = 6104
private const val READER_SELECTION_COPY = 6105
private const val READER_SELECTION_SEARCH = 6106
private const val READER_SELECTION_TRANSLATE = 6107
private const val READER_SELECTION_SHARE = 6108
private const val READER_SELECTION_GOOGLE = 6109
private const val READER_SELECTION_EDIT_TEXT = 6110
private const val READER_SELECTION_EDIT_SPEECH = 6111
private const val READER_SELECTION_READ_ALOUD = 6112
private const val READER_SELECTION_ASK_AI = 6113


private fun countSearchOccurrences(source: String, query: String): Int {
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

@Composable
private fun SelectedTextToolbar(
    text: String,
    onDismiss: () -> Unit,
    onTranslate: () -> Unit,
    onCopy: () -> Unit,
    onSelectAll: () -> Unit,
    onSearch: () -> Unit,
    onBookmark: () -> Unit,
    onNote: () -> Unit,
    onGoogle: () -> Unit,
    onShare: () -> Unit,
    onEditSpeech: () -> Unit,
    onEditExtracted: () -> Unit,
    onAskAi: () -> Unit,
    onReadAloud: () -> Unit,
    onReadFromHere: () -> Unit
) {
    var showMore by remember { mutableStateOf(false) }
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(onClick = { onReadFromHere() }) { Text("ÔûÂ", color = MaterialTheme.colorScheme.inverseOnSurface) }
            TextButton(onClick = { onTranslate(); onDismiss() }) { Text("µûç", color = MaterialTheme.colorScheme.inverseOnSurface) }
            TextButton(onClick = { onCopy(); onDismiss() }) { Text("Ôºë", color = MaterialTheme.colorScheme.inverseOnSurface) }
            TextButton(onClick = { onSelectAll() }) { Text("Ôûª", color = MaterialTheme.colorScheme.inverseOnSurface) }
            TextButton(onClick = { onSearch(); onDismiss() }) { Text("Ôîò", color = MaterialTheme.colorScheme.inverseOnSurface) }
            TextButton(onClick = { onBookmark(); onDismiss() }) { Text("Ôÿå", color = MaterialTheme.colorScheme.inverseOnSurface) }
            TextButton(onClick = { onNote(); onDismiss() }) { Text("Ô£Ä", color = MaterialTheme.colorScheme.inverseOnSurface) }
            Box {
                TextButton(onClick = { showMore = true }) { Text("Ôï«", color = MaterialTheme.colorScheme.inverseOnSurface) }
                DropdownMenu(expanded = showMore, onDismissRequest = { showMore = false }, modifier = Modifier.width(280.dp)) {
                    Text(
                        text.take(90),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Read from here") }, onClick = { showMore = false; onReadFromHere() })
                    DropdownMenuItem(text = { Text("Web lookup") }, onClick = { showMore = false; onGoogle(); onDismiss() })
                    DropdownMenuItem(text = { Text("Share") }, onClick = { showMore = false; onShare(); onDismiss() })
                    DropdownMenuItem(text = { Text("Edit selected text") }, onClick = { showMore = false; onEditExtracted(); onDismiss() })
                    DropdownMenuItem(text = { Text("Edit speech / pronunciation") }, onClick = { showMore = false; onEditSpeech(); onDismiss() })
                    DropdownMenuItem(text = { Text("Read aloud selection") }, onClick = { showMore = false; onReadAloud() })
                    DropdownMenuItem(text = { Text("Ask AI") }, onClick = { showMore = false; onAskAi(); onDismiss() })
                }
            }
        }
    }
}

@Composable
private fun SearchPanel(
    query: String,
    matchCount: Int,
    currentMatch: Int,
    onQueryChange: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp).imePadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface),
                label = { Text("Search in this document") },
                placeholder = { Text("Find a word, phrase, formula, or nameÔÇª") },
                singleLine = true
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (query.isBlank()) "Type to search sentences" else "$matchCount match${if (matchCount == 1) "" else "es"}${if (matchCount > 0) " ÔÇó $currentMatch of $matchCount" else ""}",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onPrevious, enabled = matchCount > 0) { Text("Prev") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onNext, enabled = matchCount > 0) { Text("Next") }
            }
        }
    }
}

private data class SmartOutlineEntry(
    val index: Int,
    val title: String,
    val preview: String,
    val isHeading: Boolean,
    val level: Int = 0,
    val pageNumber: Int? = null,
    val source: String = "Smart outline"
)

private const val MAX_SMART_OUTLINE_SCAN_SENTENCES = 1200
private const val MAX_SMART_OUTLINE_ENTRIES = 220

@Composable
private fun SmartOutlineDialog(
    document: ReaderDocument,
    documentOutline: List<VeritasDocumentOutlineEntry>,
    currentIndex: Int,
    onJumpToSection: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember(document.id) { mutableStateOf("") }
    val entries = remember(document.id, document.chunks, documentOutline) {
        if (documentOutline.isNotEmpty()) {
            documentOutline.map { outline ->
                val preview = document.chunks.getOrNull(outline.targetIndex).orEmpty().replace(Regex("\\s+"), " ").trim()
                SmartOutlineEntry(
                    index = outline.targetIndex,
                    title = outline.title,
                    preview = preview.take(180),
                    isHeading = true,
                    level = outline.level,
                    pageNumber = outline.pageNumber,
                    source = outline.source
                )
            }
        } else {
            buildSmartOutline(document.chunks)
        }
    }
    val filteredEntries = remember(entries, query) {
        val needle = query.trim()
        if (needle.isBlank()) {
            entries
        } else {
            entries.filter { entry ->
                entry.title.contains(needle, ignoreCase = true) ||
                    entry.preview.contains(needle, ignoreCase = true) ||
                    entry.source.contains(needle, ignoreCase = true) ||
                    entry.pageNumber?.toString() == needle ||
                    (entry.index + 1).toString() == needle
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (documentOutline.isNotEmpty()) "ÔÿÀ Table of contents" else "ÔÿÀ Smart outline") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    document.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Filter outline") },
                    placeholder = { Text("Chapter, topic, sentence numberÔÇª") },
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 390.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredEntries.isEmpty()) {
                        item {
                            Text(
                                "No outline matches.",
                                modifier = Modifier.padding(vertical = 18.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    itemsIndexed(filteredEntries, key = { index, entry -> "$index:${entry.index}:${entry.title}" }) { _, entry ->
                        val active = entry.index == currentIndex
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (entry.level.coerceIn(0, 5) * 14).dp)
                                .clickable { onJumpToSection(entry.index) },
                            shape = MaterialTheme.shapes.small,
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    active -> MaterialTheme.colorScheme.primaryContainer
                                    entry.isHeading -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    if (entry.source.startsWith("PDF")) "Ôÿ░" else if (entry.isHeading) "Ôùå" else "┬º",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        entry.title,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (active || entry.isHeading) FontWeight.Black else FontWeight.SemiBold
                                    )
                                    Text(
                                        listOfNotNull(
                                            entry.pageNumber?.let { "Page $it" },
                                            "Sentence ${entry.index + 1}",
                                            entry.source.takeIf { it.isNotBlank() }
                                        ).joinToString(" ÔÇó "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (entry.preview.isNotBlank()) {
                                        Text(
                                            entry.preview,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private fun buildSmartOutline(chunks: List<String>): List<SmartOutlineEntry> {
    val scanChunks = chunks.take(MAX_SMART_OUTLINE_SCAN_SENTENCES)
    val structuralEntries = (
        extractTableOfContentsOutline(scanChunks) + extractHeadingOutline(scanChunks)
    )
        .distinctBy { it.index }
        .sortedBy { it.index }
        .take(MAX_SMART_OUTLINE_ENTRIES)

    if (structuralEntries.isNotEmpty()) return structuralEntries

    val step = (chunks.size / MAX_SMART_OUTLINE_ENTRIES).coerceAtLeast(1)
    val fallbackIndexes = buildSet {
        scanChunks.forEachIndexed { index, chunk ->
            val firstLine = chunk.lineSequence().map { it.trim() }.firstOrNull { it.isNotBlank() }.orEmpty()
            if (looksLikeOutlineHeading(firstLine)) add(index)
        }
        var index = 0
        while (index < chunks.size) {
            add(index)
            index += step
        }
        if (chunks.isNotEmpty()) add(chunks.lastIndex)
    }.sorted()

    return fallbackIndexes.mapNotNull { index ->
        val chunk = chunks.getOrNull(index).orEmpty()
        val firstLine = chunk
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
        val isHeading = looksLikeOutlineHeading(firstLine)
        val clean = chunk.replace(Regex("\\s+"), " ").trim()
        if (clean.isBlank()) return@mapNotNull null

        val title = outlineTitle(if (isHeading) firstLine else clean, index)
        val preview = clean.take(180)

        SmartOutlineEntry(
            index = index,
            title = title,
            preview = if (preview == title) "" else preview,
            isHeading = isHeading
        )
    }.take(MAX_SMART_OUTLINE_ENTRIES)
}

private fun extractTableOfContentsOutline(chunks: List<String>): List<SmartOutlineEntry> {
    val tocLine = Regex("^(.{3,120}?)(?:\\.{2,}|\\s{3,}|\\t+)(\\d{1,4})$")
    val entries = mutableListOf<SmartOutlineEntry>()
    chunks.take(30).forEachIndexed { chunkIndex, chunk ->
        val looksLikeContents = chunk.contains("table of contents", ignoreCase = true) ||
            chunk.lineSequence().any { it.trim().equals("contents", ignoreCase = true) }
        if (!looksLikeContents && entries.isEmpty()) return@forEachIndexed

        chunk.lineSequence()
            .map { it.trim() }
            .filter { it.length in 6..140 }
            .forEach { line ->
                val match = tocLine.matchEntire(line) ?: return@forEach
                val rawTitle = match.groupValues[1].trim().trim('.', '-', 'ÔÇó')
                if (rawTitle.length < 3) return@forEach
                val targetIndex = locateOutlineTarget(chunks, rawTitle) ?: return@forEach
                val clean = chunks.getOrNull(targetIndex).orEmpty().replace(Regex("\\s+"), " ").trim()
                entries.add(
                    SmartOutlineEntry(
                        index = targetIndex,
                        title = rawTitle.take(96),
                        preview = clean.take(180),
                        isHeading = true
                    )
                )
            }
    }
    return entries
}

private fun extractHeadingOutline(chunks: List<String>): List<SmartOutlineEntry> {
    return chunks.mapIndexedNotNull { index, chunk ->
        val heading = chunk.lineSequence()
            .map { it.trim() }
            .take(8)
            .firstOrNull { looksLikeOutlineHeading(it) }
            ?: chunk.replace(Regex("\\s+"), " ").trim()
                .take(120)
                .takeIf { looksLikeOutlineHeading(it) }
            ?: return@mapIndexedNotNull null
        val clean = chunk.replace(Regex("\\s+"), " ").trim()
        SmartOutlineEntry(
            index = index,
            title = outlineTitle(heading, index),
            preview = if (clean.startsWith(heading)) clean.removePrefix(heading).trim().take(180) else clean.take(180),
            isHeading = true
        )
    }
}

private fun locateOutlineTarget(chunks: List<String>, title: String): Int? {
    val needle = normalizeOutlineNeedle(title)
    if (needle.length < 4) return null
    chunks.take(MAX_SMART_OUTLINE_SCAN_SENTENCES).forEachIndexed { index, chunk ->
        val haystack = normalizeOutlineNeedle(chunk.take(600))
        if (haystack.contains(needle)) return index
    }
    val compactNeedle = needle.split(' ').take(6).joinToString(" ")
    if (compactNeedle.length >= 8) {
        chunks.take(MAX_SMART_OUTLINE_SCAN_SENTENCES).forEachIndexed { index, chunk ->
            if (normalizeOutlineNeedle(chunk.take(600)).contains(compactNeedle)) return index
        }
    }
    return null
}

private fun normalizeOutlineNeedle(value: String): String {
    return value
        .replace(Regex("^\\d+(\\.\\d+)*\\s+"), "")
        .replace(Regex("[^A-Za-z0-9 ]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase(Locale.getDefault())
}

private fun outlineTitle(source: String, index: Int): String {
    val clean = source.replace(Regex("\\s+"), " ").trim()
    if (clean.isBlank()) return "Sentence ${index + 1}"

    val sentenceEnd = clean.indexOfAny(charArrayOf('.', '!', '?'))
    val title = if (sentenceEnd in 20..120) clean.take(sentenceEnd + 1) else clean.take(96)
    return title.trim().ifBlank { "Sentence ${index + 1}" }
}

private fun looksLikeOutlineHeading(firstLine: String): Boolean {
    val clean = firstLine.trim().trim(':', '-', 'ÔÇó', '#')
    if (clean.length !in 3..120) return false

    val headingKeyword = Regex(
        pattern = "^(chapter|section|part|unit|lesson|module|book|article|introduction|conclusion|summary|abstract|contents|references|appendix|glossary|index|foreword|preface|prologue|epilogue|bibliography|afterword|notes|citations|sources)\\b",
        option = RegexOption.IGNORE_CASE
    ).containsMatchIn(clean)

    val numberedHeading = Regex(
        pattern = "^(\\d+|[ivxlcdm]+)(\\.\\d+)*[.)\\s:-]+",
        option = RegexOption.IGNORE_CASE
    ).containsMatchIn(clean)

    val landmarkKeyword = Regex(
        pattern = "\\b(Task|Requirement|Exercise|Solution|Example|Definition|Theorem|Lemma|Proof|Corollary|Proposition|Remark|Case|Scenario|Feature|Instruction|Step|Goal|Outcome|Impact|Conclusion|Recommendation|Background|Methodology|Result|Discussion|Future Work)\\b",
        option = RegexOption.IGNORE_CASE
    ).containsMatchIn(clean)

    val words = clean.split(Regex("\\s+")).filter { word -> word.any { it.isLetter() } }
    val titleCaseWords = words.count { word -> word.firstOrNull { it.isLetter() }?.isUpperCase() == true }
    val mostlyTitleCase = words.isNotEmpty() && titleCaseWords >= maxOf(1, (words.size * 0.70f).roundToInt())
    val allCaps = words.isNotEmpty() && words.all { word -> word.all { !it.isLetter() || it.isUpperCase() } }
    val compactHeading = !clean.endsWith(".") && clean.count { it == ',' } <= 1 && clean.length < 90

    return headingKeyword || numberedHeading || landmarkKeyword || ((mostlyTitleCase || allCaps) && compactHeading)
}


@Composable
fun DocumentNotesDialog(
    document: ReaderDocument,
    annotations: List<ReaderAnnotation>,
    documentNote: String,
    currentIndex: Int,
    onDocumentNoteChange: (String) -> Unit,
    onSaveDocumentNote: () -> Unit,
    onAddCurrentNote: () -> Unit,
    onJumpToSection: (Int) -> Unit,
    onExportNotes: () -> Unit,
    onDismiss: () -> Unit
) {
    val notes = annotations
        .filter { it.type == AnnotationType.NOTE }
        .sortedBy { it.chunkIndex }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                TextButton(onClick = onSaveDocumentNote) { Text("Save") }
                TextButton(onClick = onExportNotes, enabled = documentNote.isNotBlank() || notes.any { it.note.isNotBlank() }) { Text("Export") }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        title = { Text("Ô£Ä Document notes") },
        text = {
            Column(modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    value = documentNote,
                    onValueChange = { onDocumentNoteChange(capWords(it, 500)) },
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    label = { Text("General note") },
                    placeholder = { Text("Write notes about this document") },
                    minLines = 8,
                    maxLines = 14
                )
                Text(
                    "${documentNote.trim().split(Regex("\\s+")).count { it.isNotBlank() }} / 500 words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sentence notes", fontWeight = FontWeight.Black)
                        Text(
                            "Pencil notes stay attached to individual sentences.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onAddCurrentNote) { Text("Ô£Ä Add current") }
                }
                Text(
                    "Current sentence: ${currentIndex + 1}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (notes.isEmpty()) {
                    Text("No sentence notes yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    notes.forEach { note ->
                        val excerpt = document.chunks.getOrNull(note.chunkIndex)
                            .orEmpty()
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onJumpToSection(note.chunkIndex) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Sentence ${note.chunkIndex + 1}", fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                    Text("ÔÇ║", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(note.note.ifBlank { "Empty note" }, fontWeight = FontWeight.SemiBold)
                                if (excerpt.isNotBlank()) {
                                    Text(excerpt, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun BookmarksOverviewDialog(
    document: ReaderDocument,
    annotations: List<ReaderAnnotation>,
    onJumpToSection: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val bookmarks = annotations
        .filter { it.type == AnnotationType.BOOKMARK }
        .sortedBy { it.chunkIndex }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Ôÿà Bookmarks") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                HorizontalDivider()

                if (bookmarks.isNotEmpty()) {
                    Text("Bookmarks", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    bookmarks.forEach { bookmark ->
                        val excerpt = document.chunks.getOrNull(bookmark.chunkIndex)
                            .orEmpty()
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJumpToSection(bookmark.chunkIndex) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Ôÿà Bookmark (Sentence ${bookmark.chunkIndex + 1})", fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    Text("ÔÇ║", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (excerpt.isNotBlank()) {
                                    Text(excerpt, maxLines = 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                if (bookmarks.isEmpty()) {
                    Text("No bookmarks yet in this document. Bookmark a sentence to highlight it and keep it here.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    )
}

@Composable
private fun PlayerPanel(
    isPlaying: Boolean,
    isBackgroundActive: Boolean,
    statusMessage: String,
    rate: Float,
    pitch: Float,
    fontSizeSp: Int,
    queueCount: Int,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onOpenVoiceStudio: () -> Unit,
    onPlayQueue: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(50),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onOpenVoiceStudio, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                BrandMark(compact = true)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BouncyTextButton(label = "ÔÇ╣", enabled = canGoPrevious, onClick = onPrevious)
                BouncyFilledButton(label = if (isPlaying) "Ôàí" else "ÔûÂ", onClick = onPlayPause)
                BouncyTextButton(label = "ÔÇ║", enabled = canGoNext, onClick = onNext)
            }
            Box {
                    TextButton(onClick = { expanded = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("${"%.1f".format(rate)}x", style = MaterialTheme.typography.labelMedium)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(320.dp)
                    ) {
                        Text(
                            "Playback",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            statusMessage,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            "Speed ${"%.2f".format(rate)}x",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = rate,
                            onValueChange = onRateChange,
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            "Pitch ${"%.2f".format(pitch)}",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = pitch,
                            onValueChange = onPitchChange,
                            valueRange = 0.7f..1.4f,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            "Text size ${fontSizeSp}sp",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = fontSizeSp.toFloat(),
                            onValueChange = { onFontSizeChange(it.toInt().coerceIn(14, 28)) },
                            valueRange = 14f..28f,
                            steps = 13,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("Voice and language") },
                            onClick = {
                                expanded = false
                                onOpenVoiceStudio()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (queueCount == 0) "Queue empty" else "Continue queue ($queueCount)") },
                            enabled = queueCount > 0,
                            onClick = {
                                expanded = false
                                onPlayQueue()
                            }
                        )
                    }
                }
        }
    }
}
