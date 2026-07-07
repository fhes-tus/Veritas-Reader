package com.veritas.reader.ui.screens

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.ActionMode
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.veritas.reader.AiAssistantOption
import com.veritas.reader.AnnotationPill
import com.veritas.reader.AnnotationType
import com.veritas.reader.AskAiSettings
import com.veritas.reader.BouncyFilledButton
import com.veritas.reader.BouncyTextButton
import com.veritas.reader.BrandMark
import com.veritas.reader.NarrationSettings
import com.veritas.reader.PlaybackActions
import com.veritas.reader.PlaybackService
import com.veritas.reader.ReaderAnnotation
import com.veritas.reader.ReaderDocument
import com.veritas.reader.ReaderPart
import com.veritas.reader.ReaderSettings
import com.veritas.reader.CoverExtractor
import com.veritas.reader.ReaderTextModelCache
import com.veritas.reader.ResolvedVeritasFeature
import com.veritas.reader.VeritasDocumentOutlineEntry
import com.veritas.reader.VeritasFeatureContext
import com.veritas.reader.VeritasFeatureId
import com.veritas.reader.VeritasFeatureRegistry
import com.veritas.reader.VeritasFeatureSurface
import com.veritas.reader.VeritasSleepTimerAction
import com.veritas.reader.VeritasSleepTimerFormatter
import com.veritas.reader.VeritasSleepTimerPresets
import com.veritas.reader.VeritasSleepTimerRequest
import com.veritas.reader.VeritasSleepTimerSnapshot
import com.veritas.reader.VoiceSettings
import com.veritas.reader.TtsVoiceOption
import com.veritas.reader.VeritasPackStyle
import com.veritas.reader.ReaderMode
import com.veritas.reader.ReaderModeToggle
import com.veritas.reader.aiAssistantOptions
import com.veritas.reader.capWords
import com.veritas.reader.installedPackageForOption
import com.veritas.reader.openPlayStoreForPackage
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.onGloballyPositioned
import com.veritas.reader.ui.OnboardingController
import java.util.Locale
import kotlin.math.roundToInt


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
    val activeDocumentReadingListCount: Int,
    val voices: List<TtsVoiceOption>,
    val readerMode: ReaderMode
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
    onPlayQueue: () -> Unit,
    onVoiceSelected: (TtsVoiceOption) -> Unit,
    onReaderModeChange: (ReaderMode) -> Unit
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
    state.pronunciationRuleCount
    val readerSettings = state.readerSettings
    state.voiceSettings
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
    val showAudioMode = state.readerMode == ReaderMode.LISTEN
    var selectedTextSelection by remember(document.id) { mutableStateOf<ReaderTextSelection?>(null) }
    var selectedTextView by remember(document.id) { mutableStateOf<TextView?>(null) }
    var feedbackSentenceIndex by remember(document.id) { mutableStateOf<Int?>(null) }
    // Bumped to force the auto-scroll effect to re-anchor the active sentence after events
    // that otherwise leave its keys unchanged (bookmarking, switching reader modes), which
    // previously left the page locked at the top of the section.
    var scrollTick by remember(document.id) { mutableStateOf(0) }
    var interactionTrigger by remember { mutableStateOf(0L) }
    KeepScreenAwake(enabled = (state.readerMode == ReaderMode.TEXT), interactionTrigger = interactionTrigger)

    val readerModel = remember(document.rawText, document.pageCount, document.chunks.size) {
        ReaderTextModelCache.get(document.id, document.rawText, document.pageCount)
    }
    val currentPart = readerModel.partForSentence(currentIndex)
    val currentPartIndex = currentPart?.index ?: 0
    val progress =
        if (document.chunks.isEmpty()) 0f else ((currentIndex + 1).toFloat() / document.chunks.size.toFloat()).coerceIn(
            0f,
            1f
        )
    val progressLabel = if (document.chunks.isEmpty()) {
        "0 / 0"
    } else {
        "Section ${currentPartIndex + 1}/${readerModel.parts.size.coerceAtLeast(1)} • Sentence ${currentIndex + 1}/${document.chunks.size}"
    }
    annotations.count { it.type == AnnotationType.BOOKMARK }
    annotations.count { it.type == AnnotationType.NOTE }
    val canGoPreviousPart = currentPartIndex > 0
    val canGoNextPart = currentPartIndex < readerModel.parts.lastIndex
    val previousPartStart =
        readerModel.parts.getOrNull(currentPartIndex - 1)?.sentenceStartIndex ?: 0
    val nextPartStart = readerModel.parts.getOrNull(currentPartIndex + 1)?.sentenceStartIndex
        ?: document.chunks.lastIndex
    val partListItemIndex = 1

    LaunchedEffect(feedbackSentenceIndex) {
        if (feedbackSentenceIndex != null) {
            delay(420)
            feedbackSentenceIndex = null
        }
    }

    // Re-anchor the active sentence after events that don't change the scroll effect's other
    // keys: annotation changes (bookmark/note) and returning from LISTEN/ORIGINAL to TEXT.
    LaunchedEffect(annotations, state.readerMode) {
        scrollTick++
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme))
        .monitorReadingActivity { interactionTrigger = System.currentTimeMillis() }) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackToLibrary) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        document.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "${document.sourceLabel} • $progressLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {
                        showSearch = !showSearch
                        if (!showSearch) {
                            onSearchQueryChange("")
                        }
                    }
                ) { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface) }
                IconButton(
                    onClick = onOpenDocumentNotes
                ) { Icon(Icons.Default.Edit, contentDescription = "Notes", tint = MaterialTheme.colorScheme.onSurface) }
                IconButton(
                    onClick = { showOutline = true }
                ) { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Outline", tint = MaterialTheme.colorScheme.onSurface) }
                Box {
                    IconButton(
                        onClick = { showTools = true }
                    ) { Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface) }
                    ReaderToolsMenu(
                        expanded = showTools,
                        onDismiss = { showTools = false },
                        summary = "${document.sourceLabel} • $progressLabel",
                        showSearch = showSearch,
                        showBookmarks = showBookmarks,
                        hasCanvas = hasCanvas,
                        noteCount = annotations.count { it.type == AnnotationType.NOTE },
                        narrationEnabled = narrationSettings.enabled,
                        isQueued = isQueued,
                        queueCount = queueCount,
                        askAiSettings = askAiSettings,
                        onToggleSearch = {
                            showSearch = !showSearch
                            if (!showSearch) {
                                onSearchQueryChange("")
                            }
                        },
                        onToggleBookmarks = { showBookmarks = !showBookmarks },
                        onOpenDocumentNotes = onOpenDocumentNotes,
                        onOpenCanvas = onOpenCanvas,
                        onOpenStudyTools = onOpenStudyTools,
                        onOpenTranslationTools = onOpenTranslationTools,
                        sleepTimerLabel = sleepTimerSnapshot?.menuLabel() ?: "",
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
            Spacer(modifier = Modifier.height(6.dp))
            ReaderModeToggle(
                currentMode = state.readerMode,
                onModeSelected = onReaderModeChange,
                hasCanvas = hasCanvas,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { OnboardingController.updateBounds("reader_mode_toggle", it) }
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }

        HorizontalDivider()

        if (document.chunks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
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
                            val sentenceBackground =
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                            val highlightBackground = MaterialTheme.colorScheme.tertiaryContainer
                            val feedbackBackground =
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                            val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                            val activeSentenceColor = sentenceBackground.toArgb()
                            val highlightColor = highlightBackground.toArgb()
                            val feedbackColor = feedbackBackground.toArgb()
                            val searchMatchColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f).toArgb()
                            val activeSearchMatchColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
                            val activeRange =
                                part.sentenceRanges.firstOrNull { it.sentenceIndex == currentIndex }
                            LaunchedEffect(
                                part.index,
                                currentIndex,
                                activeRange?.start,
                                activeRange?.endExclusive,
                                selectedTextView,
                                partListItemIndex,
                                readerSettings.fontSizeSp,
                                scrollTick
                            ) {
                                val textView = selectedTextView
                                val range = activeRange
                                if (textView != null && range != null) {
                                    val topPaddingPx = with(density) { 120.dp.toPx().roundToInt() }

                                    // Resolve the scroll offset of the line holding the active
                                    // sentence so it lands ~120dp below the top of the viewport.
                                    fun activeLineOffset(): Int? {
                                        val layout = textView.layout ?: return null
                                        val textLength = textView.text?.length ?: part.text.length
                                        val targetLine = layout.getLineForOffset(
                                            range.start.coerceIn(0, textLength.coerceAtLeast(0))
                                        )
                                        val targetTop = layout.getLineTop(targetLine).coerceAtLeast(0)
                                        return (targetTop - topPaddingPx).coerceAtLeast(0)
                                    }

                                    // A freshly composed section renders one large TextView
                                    // whose layout is not measured for a frame or two. Poll
                                    // (bounded) until the layout exists so the very first
                                    // scroll lands instead of leaving the page stuck at the
                                    // top of the section.
                                    var waited = 0
                                    while (textView.layout == null && waited < 800) {
                                        delay(32)
                                        waited += 32
                                    }
                                    activeLineOffset()?.let { listState.animateScrollToItem(partListItemIndex, it) }
                                    // Re-check after the layout settles (font metrics, line
                                    // wrapping) so we land precisely on the active sentence.
                                    delay(140)
                                    activeLineOffset()?.let { listState.animateScrollToItem(partListItemIndex, it) }
                                }
                            }
                            val bookmarkedSentenceIndexes = annotations
                                .filter { it.type == AnnotationType.BOOKMARK }
                                .map { it.chunkIndex }
                                .toSet()
                            val bookmarked =
                                annotations.any { it.type == AnnotationType.BOOKMARK && it.chunkIndex in part.sentenceStartIndex until part.sentenceEndIndexExclusive }
                            val note =
                                annotations.firstOrNull { it.type == AnnotationType.NOTE && it.chunkIndex == currentIndex }
                            val renderedPart = remember(
                                part.text,
                                currentIndex,
                                isPlaying,
                                feedbackSentenceIndex,
                                bookmarkedSentenceIndexes,
                                searchMatches,
                                searchCursor,
                                activeSentenceColor,
                                highlightColor,
                                feedbackColor,
                                searchMatchColor,
                                activeSearchMatchColor
                            ) {
                                buildReaderPartSpannable(
                                    part = part,
                                    activeSentenceIndex = currentIndex,
                                    feedbackSentenceIndex = feedbackSentenceIndex,
                                    highlightedSentenceIndexes = bookmarkedSentenceIndexes,
                                    searchMatches = searchMatches,
                                    searchCursor = searchCursor,
                                    activeSentenceColor = activeSentenceColor,
                                    highlightColor = highlightColor,
                                    feedbackColor = feedbackColor,
                                    searchMatchColor = searchMatchColor,
                                    activeSearchMatchColor = activeSearchMatchColor
                                )
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = (12 + readerSettings.sectionSpacingDp / 4).dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (readerSettings.showSectionNumbers) {
                                            Text(
                                                "Section ${part.index + 1} of ${readerModel.parts.size} • ${if (document.sourceLabel == "PPTX") "slides" else "pages"} ${part.pageRange.startPage}-${part.pageRange.endPage}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                        if (bookmarked) AnnotationPill(Icons.Filled.Bookmark, "Bookmarked")
                                        if (note != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AnnotationPill(Icons.Filled.EditNote, "Has note")
                                        }
                                    }

                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { OnboardingController.updateBounds("reader_text_view", it) },
                                        factory = { viewContext ->
                                            TextView(viewContext).apply {
                                                setTextIsSelectable(true)
                                                includeFontPadding = true
                                            }
                                        },
                                        update = { textView ->
                                            // This item only ever renders the current part, so
                                            // this TextView is always the active one. Assign it
                                            // unconditionally so the auto-scroll effect never
                                            // ends up holding a null/stale reference after a
                                            // bookmark, text action, or mode switch.
                                            selectedTextView = textView
                                            textView.text = renderedPart
                                            textView.setTextColor(textColor)
                                            textView.textSize = readerSettings.fontSizeSp.toFloat()
                                            textView.setLineSpacing(
                                                with(density) { 4.dp.toPx() },
                                                1.0f
                                            )
                                            textView.customSelectionActionModeCallback =
                                                readerSelectionActionModeCallback(
                                                    textView = textView,
                                                    part = part,
                                                    documentId = document.id,
                                                    context = context,
                                                    haptics = haptics,
                                                    bookmarkedSentenceIndexes = bookmarkedSentenceIndexes,
                                                    onSelectionChanged = { selectedTextSelection = it },
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
                                                        val offset = textView.getOffsetForPosition(
                                                            event.x,
                                                            event.y
                                                        ).coerceIn(0, part.text.length)
                                                        readerModel.sentenceForPartOffset(
                                                            part,
                                                            offset
                                                        )?.let { range ->
                                                            haptics.performHapticFeedback(
                                                                HapticFeedbackType.LongPress
                                                            )
                                                            feedbackSentenceIndex =
                                                                range.sentenceIndex
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
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    "Note",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    note.note,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Text(
                                        "(Section ${part.index + 1} of ${readerModel.parts.size})",
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
                                        IconButton(
                                            onClick = { onSentenceClick(previousPartStart) },
                                            enabled = canGoPreviousPart
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.SkipPrevious,
                                                contentDescription = "Previous Section"
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(18.dp))
                                        IconButton(
                                            onClick = { onSentenceClick(nextPartStart) },
                                            enabled = canGoNextPart
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.SkipNext,
                                                contentDescription = "Next Section"
                                            )
                                        }
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
                        onClose = {
                            showSearch = false
                            onSearchQueryChange("")
                        },
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
            onPlayQueue = onPlayQueue,
            onOpenAudioMode = { onReaderModeChange(ReaderMode.LISTEN) },
            voices = state.voices,
            voiceSettings = state.voiceSettings,
            onVoiceSelected = onVoiceSelected
        )
    }

    if (showAudioMode) {
        val coverFile = remember(document.id) { CoverExtractor.coverFile(context, document.id.orEmpty()) }
        val isBookmarked = remember(state.annotations, currentIndex) {
            state.annotations.any { it.chunkIndex == currentIndex && it.type == AnnotationType.BOOKMARK }
        }
        AudioModeScreen(
            title = document.title,
            currentIndex = currentIndex,
            totalChunks = document.chunks.size,
            currentSentence = document.chunks.getOrNull(currentIndex).orEmpty(),
            isPlaying = isPlaying,
            coverFile = coverFile,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            onDismiss = { onReaderModeChange(ReaderMode.TEXT) },
            isBookmarked = isBookmarked,
            onToggleBookmark = { onToggleBookmark(currentIndex) },
            rate = state.rate,
            onRateChange = onRateChange,
            readerMode = state.readerMode,
            onReaderModeChange = onReaderModeChange,
            hasCanvas = hasCanvas,
            documentChunks = document.chunks,
            onSentenceClick = { index -> onSentenceDoubleTap(index) },
            onOpenSleepTimer = onOpenSleepTimer,
            onOpenVoiceStudio = onOpenVoiceStudio,
            onOpenNarrationStudio = onOpenNarrationStudio,
            onExportAudio = onExportAudio
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
        Text(
            "Read",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        DropdownMenuItem(
            text = { Text(if (showSearch) "🔎 Hide search" else "🔎 Search document") },
            onClick = { choose(onToggleSearch) })
        DropdownMenuItem(
            text = { Text("💾 Original View") },
            enabled = hasCanvas,
            onClick = { choose(onOpenCanvas) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.SLEEP_TIMER),
            label = if (sleepTimerLabel.isBlank()) "⏱️ Sleep timer" else "⏱️ $sleepTimerLabel",
            onClick = { choose(onOpenSleepTimer) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.READING_LISTS),
            label = "📰 Reading lists ($activeDocumentReadingListCount/$readingListCount)",
            onClick = { choose(onOpenReadingLists) }
        )
        DropdownMenuItem(
            text = { Text(if (isQueued) "✓ Remove from Queue" else "+ Add to Queue") },
            onClick = { choose(onToggleQueue) })
        DropdownMenuItem(
            text = { Text("▶ Play Queue ($queueCount)") },
            enabled = queueCount > 0,
            onClick = { choose(onPlayQueue) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.READING_HISTORY),
            label = "↺ Reading history",
            onClick = { choose(onOpenReadingHistory) }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            "Notes and bookmarks",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        DropdownMenuItem(
            text = { Text(if (showBookmarks) "🔖 Hide bookmarks" else "🔖 Bookmarks") },
            onClick = { choose(onToggleBookmarks) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.BOOKMARKS_AND_NOTES),
            label = "✒️ Document notes${if (noteCount > 0) " • $noteCount sentence${if (noteCount == 1) "" else "s"}" else ""}",
            onClick = { choose(onOpenDocumentNotes) }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            "Study",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        DropdownMenuItem(
            text = { Text("🆘 AI Assistant: ${askAiSettings.assistantLabel}") },
            onClick = { showAiChooser = !showAiChooser }
        )
        if (showAiChooser) {
            aiAssistantOptions.filter { it.packageName.isNotBlank() }.forEach { option ->
                val installedPackage = installedPackageForOption(context, option)
                val isSelected = askAiSettings.assistantId == option.id
                DropdownMenuItem(
                    text = {
                        Text(
                            "${if (isSelected) "✓ " else ""}${option.label}${if (installedPackage == null) " • install" else ""}",
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
            label = "🆘 AI Ask current part",
            onClick = { choose(onAskCurrentSection) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.OFFLINE_STUDY_TOOLS),
            label = "🤖 AI Study tools",
            onClick = { choose(onOpenStudyTools) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.TRANSLATION_HANDOFF),
            label = "📨 Translation handoff",
            onClick = { choose(onOpenTranslationTools) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.EXTRACTED_TEXT_EDITOR),
            label = "🗒️ Edit extracted text",
            onClick = { choose(onOpenTextEditor) }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            "Voice and settings",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.VOICE_STUDIO),
            label = "🎙️ Voice and language",
            onClick = { choose(onOpenVoiceStudio) }
        )
        DropdownMenuItem(
            text = { Text(if (narrationEnabled) "🎭 Narration mode on" else "🎭 Narration mode") },
            onClick = { choose(onOpenNarrationStudio) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.PRONUNCIATION_RULES),
            label = "🗣️ Pronunciation rules",
            onClick = { choose(onOpenPronunciationRules) }
        )
        DropdownMenuItem(
            text = { Text("🎨 Reader appearance") },
            onClick = { choose(onOpenReaderSettings) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT),
            label = "● Record sound file",
            onClick = { choose(onStartRecord) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT),
            label = "📤 Export audio",
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
    var modeBySection by remember {
        mutableStateOf(activeTimer?.stopAtEndOfSection ?: false)
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
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = VeritasPackStyle.surfaceAlpha())),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(activeLabel, fontWeight = FontWeight.Black)
                            Text(
                                activeTimer.action.label,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable { modeBySection = false },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = !modeBySection, onClick = { modeBySection = false })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Countdown")
                    }
                    Row(
                        modifier = Modifier.clickable { modeBySection = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = modeBySection, onClick = { modeBySection = true })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("End of section")
                    }
                }

                if (!modeBySection) {
                    VeritasSleepTimerPresets.durationsMillis.chunked(3).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { duration ->
                                val selected = selectedDurationMillis == duration
                                val content: @Composable () -> Unit = {
                                    Text(VeritasSleepTimerFormatter.formatDuration(duration))
                                }
                                if (selected) {
                                    Button(
                                        onClick = { selectedDurationMillis = duration },
                                        modifier = Modifier.weight(1f),
                                        shape = VeritasPackStyle.chipShape()
                                    ) { content() }
                                } else {
                                    OutlinedButton(
                                        onClick = { selectedDurationMillis = duration },
                                        modifier = Modifier.weight(1f),
                                        shape = VeritasPackStyle.chipShape()
                                    ) { content() }
                                }
                            }
                        }
                    }
                }

                VeritasSleepTimerAction.entries.forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAction = action },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAction == action,
                            onClick = { selectedAction = action })
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
                            durationMillis = if (modeBySection) 0L else selectedDurationMillis,
                            action = selectedAction,
                            stopAtEndOfSection = modeBySection
                        )
                    )
                    onDismiss()
                },
                shape = VeritasPackStyle.chipShape()
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (activeTimer != null) {
                    OutlinedButton(
                        onClick = {
                            onCancelTimer()
                            onDismiss()
                        },
                        shape = VeritasPackStyle.chipShape()
                    ) {
                        Text("Cancel timer")
                    }
                }
                TextButton(onClick = onDismiss, shape = VeritasPackStyle.chipShape()) { Text("Close") }
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
    searchMatches: List<Int>,
    searchCursor: Int,
    activeSentenceColor: Int,
    highlightColor: Int,
    feedbackColor: Int,
    searchMatchColor: Int,
    activeSearchMatchColor: Int
): SpannableString {
    val spannable = SpannableString(part.text)
    fun addBackground(start: Int, endExclusive: Int, color: Int) {
        if (start < endExclusive && start in 0..part.text.length && endExclusive in 0..part.text.length) {
            spannable.setSpan(
                BackgroundColorSpan(color),
                start,
                endExclusive,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    val activeSearchMatchSentenceIndex = searchMatches.getOrNull(searchCursor)
    part.sentenceRanges.forEach { range ->
        if (range.sentenceIndex in searchMatches) {
            if (range.sentenceIndex == activeSearchMatchSentenceIndex) {
                addBackground(range.start, range.endExclusive, activeSearchMatchColor)
            } else {
                addBackground(range.start, range.endExclusive, searchMatchColor)
            }
        }
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
            menu.clear()
            onSelectionChanged(currentSelection())
            menu.add(0, android.R.id.selectAll, 3, "Select all")
            val selectionFeatures = VeritasFeatureRegistry.resolve(
                VeritasFeatureSurface.SELECTION_OVERFLOW,
                VeritasFeatureContext(
                    hasActiveDocument = !documentId.isNullOrBlank(),
                    hasTextSelection = true,
                    hasSavedDocument = !documentId.isNullOrBlank()
                )
            ).associateBy { it.definition.id }
            menu.add(0, READER_SELECTION_READ_FROM_HERE, 0, "Read from here")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
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
                title = "Fix pronunciation",
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
                            putExtra(
                                PlaybackActions.EXTRA_START_INDEX,
                                selection.firstSentenceIndex
                            )
                            putExtra(PlaybackActions.EXTRA_CHAR_OFFSET, 0)
                        }
                        context.startService(intent)
                    }
                }

                READER_SELECTION_NOTE -> onEditNotes(selection.sentenceIndexes)
                READER_SELECTION_BOOKMARK -> {
                    val anyUnbookmarked =
                        selection.sentenceIndexes.any { it !in bookmarkedSentenceIndexes }
                    if (anyUnbookmarked) {
                        selection.sentenceIndexes.filter { it !in bookmarkedSentenceIndexes }
                            .forEach(onToggleBookmark)
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
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(onClick = { onReadFromHere() }) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Read from here",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            TextButton(onClick = { onTranslate(); onDismiss() }) {
                Icon(
                    imageVector = Icons.Filled.Translate,
                    contentDescription = "Translate",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            TextButton(onClick = { onCopy(); onDismiss() }) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            TextButton(onClick = { onSelectAll() }) {
                Icon(
                    imageVector = Icons.Filled.SelectAll,
                    contentDescription = "Select all",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            TextButton(onClick = { onSearch(); onDismiss() }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            TextButton(onClick = { onBookmark(); onDismiss() }) {
                Icon(
                    imageVector = Icons.Filled.BookmarkAdd,
                    contentDescription = "Bookmark",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            TextButton(onClick = { onNote(); onDismiss() }) {
                Icon(
                    imageVector = Icons.Filled.EditNote,
                    contentDescription = "Add note",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            Box {
                TextButton(onClick = { showMore = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMore,
                    onDismissRequest = { showMore = false },
                    modifier = Modifier.width(280.dp)
                ) {
                    Text(
                        text.take(90),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Read from here") },
                        onClick = { showMore = false; onReadFromHere() })
                    DropdownMenuItem(
                        text = { Text("Web lookup") },
                        onClick = { showMore = false; onGoogle(); onDismiss() })
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = { showMore = false; onShare(); onDismiss() })
                    DropdownMenuItem(
                        text = { Text("Edit selected text") },
                        onClick = { showMore = false; onEditExtracted(); onDismiss() })
                    DropdownMenuItem(
                        text = { Text("Edit speech / pronunciation") },
                        onClick = { showMore = false; onEditSpeech(); onDismiss() })
                    DropdownMenuItem(
                        text = { Text("Read aloud selection") },
                        onClick = { showMore = false; onReadAloud() })
                    DropdownMenuItem(
                        text = { Text("Ask AI") },
                        onClick = { showMore = false; onAskAi(); onDismiss() })
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
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    onClose()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        "Search in document...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Text(
                            text = if (matchCount > 0) "$currentMatch/$matchCount" else "0/0",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = matchCount > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Previous match",
                        tint = if (matchCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = onNext,
                    enabled = matchCount > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Next match",
                        tint = if (matchCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
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
                val preview = document.chunks.getOrNull(outline.targetIndex).orEmpty()
                    .replace(Regex("\\s+"), " ").trim()
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
        title = { Text(if (documentOutline.isNotEmpty()) "📇 Table of contents" else "📇 Smart outline") },
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
                    placeholder = { Text("Chapter, topic, sentence number…") },
                    singleLine = true,
                    shape = VeritasPackStyle.chipShape()
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
                    itemsIndexed(
                        filteredEntries,
                        key = { index, entry -> "$index:${entry.index}:${entry.title}" }) { _, entry ->
                        val active = entry.index == currentIndex
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (entry.level.coerceIn(0, 5) * 14).dp)
                                .clickable { onJumpToSection(entry.index) },
                            shape = VeritasPackStyle.compactShape(),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    active -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = VeritasPackStyle.surfaceAlpha())
                                    entry.isHeading -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = VeritasPackStyle.surfaceAlpha())
                                    else -> MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    if (entry.source.startsWith("PDF")) "☰" else if (entry.isHeading) "◆" else "§",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        entry.title,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (active || entry.isHeading) FontWeight.Black else FontWeight.SemiBold,
                                        color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        listOfNotNull(
                                            entry.pageNumber?.let { "Page $it" },
                                            "Sentence ${entry.index + 1}",
                                            entry.source.takeIf { it.isNotBlank() }
                                        ).joinToString(" • "),
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
            Button(onClick = onDismiss, shape = VeritasPackStyle.chipShape()) { Text("Close") }
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
            val firstLine =
                chunk.lineSequence().map { it.trim() }.firstOrNull { it.isNotBlank() }.orEmpty()
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
                val rawTitle = match.groupValues[1].trim().trim('.', '-', '•')
                if (rawTitle.length < 3) return@forEach
                val targetIndex = locateOutlineTarget(chunks, rawTitle) ?: return@forEach
                val clean =
                    chunks.getOrNull(targetIndex).orEmpty().replace(Regex("\\s+"), " ").trim()
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
            preview = if (clean.startsWith(heading)) clean.removePrefix(heading).trim()
                .take(180) else clean.take(180),
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
    val clean = firstLine.trim().trim(':', '-', '•', '#')
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
    val titleCaseWords =
        words.count { word -> word.firstOrNull { it.isLetter() }?.isUpperCase() == true }
    val mostlyTitleCase =
        words.isNotEmpty() && titleCaseWords >= maxOf(1, (words.size * 0.70f).roundToInt())
    val allCaps =
        words.isNotEmpty() && words.all { word -> word.all { !it.isLetter() || it.isUpperCase() } }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSaveDocumentNote,
                    shape = VeritasPackStyle.chipShape()
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = onExportNotes,
                    enabled = documentNote.isNotBlank() || notes.any { it.note.isNotBlank() },
                    shape = VeritasPackStyle.chipShape()
                ) {
                    Text("Export")
                }
                TextButton(onClick = onDismiss, shape = VeritasPackStyle.chipShape()) { Text("Close") }
            }
        },
        title = { Text("✏️ Document notes") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = documentNote,
                    onValueChange = { onDocumentNoteChange(capWords(it, 500)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    label = { Text("General note") },
                    placeholder = { Text("Write notes about this document") },
                    minLines = 8,
                    maxLines = 14,
                    shape = VeritasPackStyle.cardShape()
                )
                Text(
                    "${
                        documentNote.trim().split(Regex("\\s+")).count { it.isNotBlank() }
                    } / 500 words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sentence notes", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Pencil notes stay attached to individual sentences.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onAddCurrentNote,
                        shape = VeritasPackStyle.chipShape()
                    ) {
                        Text("✏️ Add current")
                    }
                }
                Text(
                    "Current sentence: ${currentIndex + 1}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (notes.isEmpty()) {
                    Text(
                        "No sentence notes yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    notes.forEach { note ->
                        val excerpt = document.chunks.getOrNull(note.chunkIndex)
                            .orEmpty()
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJumpToSection(note.chunkIndex) },
                            shape = VeritasPackStyle.compactShape(),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha()))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Sentence ${note.chunkIndex + 1}",
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "›",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    note.note.ifBlank { "Empty note" },
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (excerpt.isNotBlank()) {
                                    Text(
                                        excerpt,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
    // Group contiguous bookmarked sentences into a single file-level bookmark entry so a
    // multi-sentence selection shows as ONE bookmark spanning a range, not many rows.
    val bookmarkRanges = remember(annotations) {
        val indexes = annotations
            .filter { it.type == AnnotationType.BOOKMARK }
            .map { it.chunkIndex }
            .distinct()
            .sorted()
        val ranges = mutableListOf<IntRange>()
        var runStart = -1
        var prev = -2
        indexes.forEach { idx ->
            if (idx == prev + 1) {
                prev = idx
            } else {
                if (runStart >= 0) ranges.add(runStart..prev)
                runStart = idx
                prev = idx
            }
        }
        if (runStart >= 0) ranges.add(runStart..prev)
        ranges
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, shape = VeritasPackStyle.chipShape()) { Text("Close") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bookmarks")
            }
        },
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
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider()

                if (bookmarkRanges.isNotEmpty()) {
                    Text(
                        "Bookmarks",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    bookmarkRanges.forEach { range ->
                        val excerpt = (range.first..range.last)
                            .joinToString(" ") { document.chunks.getOrNull(it).orEmpty() }
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        val label = if (range.first == range.last) {
                            "Sentence ${range.first + 1}"
                        } else {
                            "Sentences ${range.first + 1}–${range.last + 1} (${range.last - range.first + 1})"
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJumpToSection(range.first) },
                            shape = VeritasPackStyle.compactShape(),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha()))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        label,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (excerpt.isNotBlank()) {
                                    Text(
                                        excerpt,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "No bookmarks yet in this document. Select one or more sentences and tap the bookmark icon to save them here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}

private enum class DragValue { Collapsed, Expanded }

@OptIn(ExperimentalFoundationApi::class)
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
    onPlayQueue: () -> Unit,
    onOpenAudioMode: () -> Unit,
    voices: List<TtsVoiceOption>,
    voiceSettings: VoiceSettings,
    onVoiceSelected: (TtsVoiceOption) -> Unit
) {
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { 150.dp.toPx() } // height diff

    @Suppress("DEPRECATION")
    val draggableState = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Collapsed,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            decayAnimationSpec = androidx.compose.animation.core.exponentialDecay()
        )
    }

    val anchors = remember(maxOffsetPx) {
        DraggableAnchors {
            DragValue.Collapsed at maxOffsetPx
            DragValue.Expanded at 0f
        }
    }

    SideEffect {
        draggableState.updateAnchors(anchors)
    }

    val currentOffset = if (draggableState.offset.isNaN()) maxOffsetPx else draggableState.requireOffset()
    val progress = (1f - (currentOffset / maxOffsetPx)).coerceIn(0f, 1f)
    val heightDp = 72.dp + (150.dp * progress) // Varies from 72dp to 222dp

    val currentLocaleTag = voiceSettings.localeTag
    val availableVoices = remember(voices, currentLocaleTag) {
        if (currentLocaleTag.isBlank()) {
            voices.take(8)
        } else {
            voices.filter { it.localeTag.equals(currentLocaleTag, ignoreCase = true) }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(heightDp)
            .anchoredDraggable(
                state = draggableState,
                orientation = Orientation.Vertical
            )
            .onGloballyPositioned { OnboardingController.updateBounds("player_panel_header", it) },
        shape = VeritasPackStyle.cardShape(),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row (visible in both states)
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // BrandMark Logo (clicking transitions state)
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            draggableState.animateTo(
                                if (draggableState.currentValue == DragValue.Collapsed) DragValue.Expanded else DragValue.Collapsed
                            )
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    BrandMark(compact = true)
                }

                // Playback Controls Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BouncyTextButton(icon = Icons.AutoMirrored.Filled.NavigateBefore, label = "Previous", enabled = canGoPrevious, onClick = onPrevious)
                    BouncyFilledButton(icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, label = if (isPlaying) "Pause" else "Play", onClick = onPlayPause)
                    BouncyTextButton(icon = Icons.AutoMirrored.Filled.NavigateNext, label = "Next", enabled = canGoNext, onClick = onNext)
                }

                // Audio Mode Button
                BouncyTextButton(
                    icon = Icons.Filled.Headphones,
                    label = "Audio Mode",
                    onClick = onOpenAudioMode
                )
            }

            // Expanded Area (Speed, Pitch, Font size, Quick Voice Picker)
            if (progress > 0.05f) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer { alpha = progress }
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Status/Playback message
                    Text(
                        statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // Speed Slider Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Speed ${"%.2f".format(rate)}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { onRateChange((rate - 0.05f).coerceIn(0.5f, 2.0f)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = { onRateChange((rate + 0.05f).coerceIn(0.5f, 2.0f)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    Slider(
                        value = rate,
                        onValueChange = onRateChange,
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                            activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    // Pitch & Font Size
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pitch ${"%.2f".format(pitch)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = pitch,
                                onValueChange = onPitchChange,
                                valueRange = 0.7f..1.4f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                                    activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                    inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Text size ${fontSizeSp}sp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = fontSizeSp.toFloat(),
                                onValueChange = { onFontSizeChange(it.toInt().coerceIn(14, 28)) },
                                valueRange = 14f..28f,
                                steps = 13,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                                    activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                    inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    // Quick Voice Picker
                    if (availableVoices.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        Text(
                            "Voice",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableVoices.forEach { voice ->
                                val isSelected = voice.name == voiceSettings.voiceName
                                if (isSelected) {
                                    Button(
                                        onClick = { onVoiceSelected(voice) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = VeritasPackStyle.chipShape(),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(voice.name, style = MaterialTheme.typography.labelMedium)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onVoiceSelected(voice) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = VeritasPackStyle.chipShape(),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(voice.name, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Shortcut to open full Voice Studio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            enabled = queueCount > 0,
                            onClick = onPlayQueue,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (queueCount == 0) "Queue empty" else "Play Queue ($queueCount)", style = MaterialTheme.typography.labelMedium)
                        }
                        TextButton(
                            onClick = onOpenVoiceStudio,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Voice Studio ›", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
