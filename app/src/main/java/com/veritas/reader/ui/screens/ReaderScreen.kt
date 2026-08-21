package com.veritas.reader.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ReplacementSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.ActionMode
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import com.veritas.reader.aiAssistantIcon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import com.veritas.reader.ShareScope
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
import com.veritas.reader.ReaderPageRange
import com.veritas.reader.ReaderPart
import com.veritas.reader.ReaderPartSentenceRange
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
import com.veritas.reader.ui.rememberSliderHaptics
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

data class ReaderPageItem(
    val pageNumber: Int,
    val text: String,
    val sentenceStartIndex: Int,
    val sentenceEndIndexExclusive: Int,
    val sentenceRanges: List<ReaderPartSentenceRange>
) {
    fun toReaderPart(partIndex: Int = 0): ReaderPart {
        return ReaderPart(
            index = partIndex,
            pageRange = ReaderPageRange(partIndex, pageNumber, pageNumber),
            sentenceStartIndex = sentenceStartIndex,
            sentenceEndIndexExclusive = sentenceEndIndexExclusive,
            text = text,
            sentenceRanges = sentenceRanges
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onExportStudyGuidePdf: () -> Unit = {},
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
    onReaderModeChange: (ReaderMode) -> Unit,
    onAddBookmarkGroup: (List<Int>, String) -> Unit = { _, _ -> },
    onShareToAi: (ShareScope, ReaderTextSelection?, IntRange?, Boolean) -> Unit = { _, _, _, _ -> },
    showShareToAi: Boolean = false,
    onDismissShareToAi: () -> Unit = {},
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null
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
    var showRsvpSpeedReader by remember(document.id) { mutableStateOf(false) }
    val showAudioMode = state.readerMode == ReaderMode.LISTEN
    var selectedTextSelection by remember(document.id) { mutableStateOf<ReaderTextSelection?>(null) }
    var selectedTextView by remember(document.id) { mutableStateOf<TextView?>(null) }
    var feedbackSentenceIndex by remember(document.id) { mutableStateOf<Int?>(null) }
    var colorPaletteTargetIndexes by remember { mutableStateOf<List<Int>?>(null) }
    var showShareToAiSheet by remember { mutableStateOf(false) }
    var shareToAiSelection by remember { mutableStateOf<ReaderTextSelection?>(null) }
    var shareToAiNoPrompt by remember { mutableStateOf(false) }
    // Bumped to force the auto-scroll effect to re-anchor the active sentence after events
    // that otherwise leave its keys unchanged (bookmarking, switching reader modes), which
    // previously left the page locked at the top of the section.
    var scrollTick by remember(document.id) { mutableStateOf(0) }
    var interactionTrigger by remember { mutableStateOf(0L) }
    LaunchedEffect(showShareToAi) {
        if (showShareToAi) {
            shareToAiSelection = null
            shareToAiNoPrompt = true
            showShareToAiSheet = true
        }
    }
    KeepScreenAwake(enabled = (state.readerMode == ReaderMode.TEXT), interactionTrigger = interactionTrigger)

    val readerModel = remember(document.rawText, document.pageCount, document.chunks.size) {
        ReaderTextModelCache.get(document.id, document.rawText, document.pageCount)
    }

    val totalPages = remember(readerModel) {
        maxOf(
            readerModel.pageCount,
            readerModel.sentences.maxOfOrNull { it.pageNumber } ?: 1
        ).coerceAtLeast(1)
    }
    val pageItems = remember(readerModel) {
        val sentencesByPage = readerModel.sentences.groupBy { it.pageNumber }
        val maxPage = maxOf(
            totalPages,
            sentencesByPage.keys.maxOrNull() ?: 1
        )
        var runningSentenceIndex = 0
        (1..maxPage).map { pageNum ->
            val pageSentences = sentencesByPage[pageNum].orEmpty()
            if (pageSentences.isNotEmpty()) {
                val textBuilder = StringBuilder()
                val ranges = mutableListOf<ReaderPartSentenceRange>()
                pageSentences.forEachIndexed { i, sentence ->
                    val sep = if (i == 0) "" else if (sentence.separatorBefore.isNotEmpty()) sentence.separatorBefore else " "
                    textBuilder.append(sep)
                    val start = textBuilder.length
                    textBuilder.append(sentence.text)
                    val end = textBuilder.length
                    ranges.add(ReaderPartSentenceRange(sentence.index, start, end))
                }
                val pageText = textBuilder.toString()
                runningSentenceIndex = pageSentences.last().index + 1
                ReaderPageItem(
                    pageNumber = pageNum,
                    text = pageText,
                    sentenceStartIndex = pageSentences.first().index,
                    sentenceEndIndexExclusive = runningSentenceIndex,
                    sentenceRanges = ranges
                )
            } else {
                val safeIndex = runningSentenceIndex.coerceIn(0, (readerModel.sentences.size - 1).coerceAtLeast(0))
                ReaderPageItem(
                    pageNumber = pageNum,
                    text = "",
                    sentenceStartIndex = safeIndex,
                    sentenceEndIndexExclusive = safeIndex,
                    sentenceRanges = emptyList()
                )
            }
        }.ifEmpty {
            val part = readerModel.parts.firstOrNull()
            listOf(
                ReaderPageItem(
                    pageNumber = 1,
                    text = part?.text.orEmpty(),
                    sentenceStartIndex = part?.sentenceStartIndex ?: 0,
                    sentenceEndIndexExclusive = part?.sentenceEndIndexExclusive ?: 0,
                    sentenceRanges = part?.sentenceRanges.orEmpty()
                )
            )
        }
    }

    val currentPageNumber = remember(readerModel.sentences, currentIndex) {
        readerModel.sentences.getOrNull(currentIndex)?.pageNumber ?: 1
    }
    val pagerState = rememberPagerState(
        initialPage = (pageItems.indexOfFirst { it.pageNumber == currentPageNumber }.takeIf { it >= 0 } ?: (currentPageNumber - 1))
            .coerceIn(0, (pageItems.size - 1).coerceAtLeast(0))
    ) { pageItems.size.coerceAtLeast(1) }

    // Page sync: whether playing TTS or user jumps to a section/sentence via outline/bookmarks/slider
    LaunchedEffect(currentPageNumber) {
        val targetPage = (pageItems.indexOfFirst { it.pageNumber == currentPageNumber }.takeIf { it >= 0 } ?: (currentPageNumber - 1))
            .coerceIn(0, (pageItems.size - 1).coerceAtLeast(0))
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            if (isPlaying) {
                pagerState.animateScrollToPage(
                    page = targetPage,
                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
                )
            } else {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    // Manual page swipe -> update playback sentence so it stays on the flipped page smoothly
    LaunchedEffect(pagerState) {
        androidx.compose.runtime.snapshotFlow { pagerState.settledPage }
            .collect { pageIdx ->
                if (!pagerState.isScrollInProgress) {
                    val targetPageItem = pageItems.getOrNull(pageIdx)
                    if (targetPageItem != null && !isPlaying) {
                        val currentSentencePage = readerModel.sentences.getOrNull(currentIndex)?.pageNumber ?: 1
                        if (currentSentencePage != targetPageItem.pageNumber) {
                            onSentenceClick(targetPageItem.sentenceStartIndex)
                        }
                    }
                }
            }
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
    val isCollapsible = state.readerSettings.collapsibleReaderBars
    var topBarVisible by remember(isCollapsible) { mutableStateOf(true) }
    var bottomBarVisible by remember(isCollapsible) { mutableStateOf(true) }

    val effectiveTopBarVisible = !isCollapsible || topBarVisible
    val effectiveBottomBarVisible = !isCollapsible || bottomBarVisible

    val topBarOffset by animateFloatAsState(
        targetValue = if (effectiveTopBarVisible) 0f else -650f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "readerTopBarOffset"
    )
    val bottomBarOffset by animateFloatAsState(
        targetValue = if (effectiveBottomBarVisible) 0f else 450f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "readerBottomBarOffset"
    )
    val topContentPadding by animateDpAsState(
        targetValue = if (effectiveTopBarVisible) 138.dp else 24.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "readerTopContentPadding"
    )
    val bottomContentPadding by animateDpAsState(
        targetValue = if (effectiveBottomBarVisible) 84.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "readerBottomContentPadding"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme))
            .monitorReadingActivity { interactionTrigger = System.currentTimeMillis() }
    ) {
        // 1. Full-screen Reading Content (with dynamic animated padding)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topContentPadding, bottom = bottomContentPadding)
        ) {
            if (document.chunks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No readable text found.")
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedTextSelection) {
                            if (selectedTextSelection != null) {
                                detectTapGestures(onTap = {
                                    clearNativeTextSelection(selectedTextView)
                                    selectedTextSelection = null
                                })
                            }
                        }
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 16.dp,
                        key = { pageItems.getOrNull(it)?.pageNumber ?: it }
                    ) { pageIndex ->
                    val pageItem = pageItems.getOrNull(pageIndex)
                    if (pageItem == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Page not found", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        val pageNumber = pageItem.pageNumber
                        val part = remember(pageItem) { pageItem.toReaderPart(pageIndex) }

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

                        val bookmarkedSentenceIndexes = remember(annotations) {
                            annotations
                                .filter { it.type == AnnotationType.BOOKMARK }
                                .map { it.chunkIndex }
                                .toSet()
                        }
                        val bookmarkedSentences = remember(annotations) {
                            annotations
                                .filter { it.type == AnnotationType.BOOKMARK }
                                .associate { it.chunkIndex to (it.highlightColor ?: "#FFE082") }
                        }
                        val bookmarked =
                            annotations.any { it.type == AnnotationType.BOOKMARK && it.chunkIndex in part.sentenceStartIndex until part.sentenceEndIndexExclusive }
                        val note =
                            annotations.firstOrNull { it.type == AnnotationType.NOTE && it.chunkIndex == currentIndex }

                        val renderedPage = remember(
                            part.text,
                            currentIndex,
                            isPlaying,
                            feedbackSentenceIndex,
                            bookmarkedSentences,
                            searchMatches,
                            searchCursor,
                            activeSentenceColor,
                            highlightColor,
                            feedbackColor,
                            searchMatchColor,
                            activeSearchMatchColor,
                            state.readerSettings.bionicReading
                        ) {
                            buildReaderPartSpannable(
                                part = part,
                                activeSentenceIndex = currentIndex,
                                feedbackSentenceIndex = feedbackSentenceIndex,
                                highlightedSentences = bookmarkedSentences,
                                searchMatches = searchMatches,
                                searchCursor = searchCursor,
                                activeSentenceColor = activeSentenceColor,
                                defaultHighlightColor = highlightColor,
                                feedbackColor = feedbackColor,
                                searchMatchColor = searchMatchColor,
                                activeSearchMatchColor = activeSearchMatchColor,
                                bionicReading = state.readerSettings.bionicReading
                            )
                        }

                        val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                        val absOffset = kotlin.math.abs(pageOffset)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val scale = 0.97f + 0.03f * (1f - absOffset)
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = (0.6f + 0.4f * (1f - absOffset)).coerceIn(0f, 1f)
                                    cameraDistance = 18f * density.density
                                    rotationY = pageOffset * -8f
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                            ),
                            tonalElevation = 2.dp,
                            shadowElevation = 3.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(selectedTextSelection) {
                                        detectTapGestures(
                                            onTap = {
                                                if (isCollapsible && selectedTextSelection == null) {
                                                    topBarVisible = !topBarVisible
                                                    bottomBarVisible = topBarVisible
                                                }
                                            }
                                        )
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    // Page Top Header (Tappable to toggle collapsible bars)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                if (isCollapsible) {
                                                    topBarVisible = !topBarVisible
                                                    bottomBarVisible = topBarVisible
                                                }
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = document.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (bookmarked) {
                                                AnnotationPill(Icons.Filled.Bookmark, "Bookmarked")
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = "Page $pageNumber of ${pageItems.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                                    )

                                    // Scrollable content on page
                                    val pageScrollState = rememberScrollState()
                                    LaunchedEffect(pageScrollState.isScrollInProgress) {
                                        if (pageScrollState.isScrollInProgress && pageScrollState.value > 60) {
                                            topBarVisible = false
                                            bottomBarVisible = false
                                        }
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .verticalScroll(pageScrollState)
                                            .padding(bottom = if (bottomBarVisible) 60.dp else 12.dp)
                                    ) {
                                        if (pageNumber == 1) {
                                            val context = LocalContext.current
                                            val coverFile = remember(document.id) { CoverExtractor.coverFile(context, document.id.orEmpty()) }
                                            val coverBitmap = remember(coverFile) {
                                                coverFile?.takeIf { it.exists() }?.let { file ->
                                                    runCatching { android.graphics.BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
                                                }
                                            }
                                            if (coverBitmap != null) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(72.dp)
                                                            .height(100.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                    ) {
                                                        androidx.compose.foundation.Image(
                                                            bitmap = coverBitmap.asImageBitmap(),
                                                            contentDescription = "Cover",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        AndroidView(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .onGloballyPositioned { OnboardingController.updateBounds("reader_text_view", it) },
                                            factory = { viewContext ->
                                                TextView(viewContext).apply {
                                                    setTextIsSelectable(true)
                                                    includeFontPadding = false
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                        justificationMode = android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
                                                    }
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                                        breakStrategy = android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
                                                        hyphenationFrequency = android.text.Layout.HYPHENATION_FREQUENCY_FULL_FAST
                                                    }
                                                }
                                            },
                                            onRelease = { released ->
                                                runCatching {
                                                    clearNativeTextSelection(released)
                                                    released.clearFocus()
                                                }
                                            },
                                            update = { textView ->
                                                if (currentPageNumber == pageNumber) {
                                                    selectedTextView = textView
                                                }
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    textView.justificationMode = android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
                                                }
                                                textView.text = renderedPage
                                                textView.setTextColor(textColor)
                                                textView.textSize = readerSettings.fontSizeSp.toFloat()
                                                textView.setLineSpacing(
                                                    with(density) { 6.dp.toPx() },
                                                    1.15f
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
                                                        },
                                                        onHighlightSelection = { selection ->
                                                            colorPaletteTargetIndexes = selection.sentenceIndexes
                                                        },
                                                        onEditNotes = onEditNotes,
                                                        onTranslateSelection = onTranslateSelection,
                                                        onCopySelection = onCopySelection,
                                                        onGoogleSelection = onGoogleSelection,
                                                        onShareSelection = onShareSelection,
                                                        onShareSelectionToAi = { selection ->
                                                            shareToAiSelection = selection
                                                            shareToAiNoPrompt = false
                                                            showShareToAiSheet = true
                                                        },
                                                        onEditSpeechSelection = onEditSpeechSelection,
                                                        onEditExtractedSelection = onEditExtractedSelection,
                                                        onAskAiSelection = onAskAiSelection,
                                                        onReadSelection = onReadSelection
                                                    )
                                                val detector = GestureDetector(
                                                    textView.context,
                                                    object : GestureDetector.SimpleOnGestureListener() {
                                                        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                                                            clearNativeTextSelection(textView)
                                                            if (isCollapsible && selectedTextSelection == null) {
                                                                topBarVisible = !topBarVisible
                                                                bottomBarVisible = topBarVisible
                                                            }
                                                            return true
                                                        }
                                                        override fun onDoubleTap(event: MotionEvent): Boolean {
                                                            clearNativeTextSelection(textView)
                                                            val offset = textView.getOffsetForPosition(
                                                                event.x,
                                                                event.y
                                                            ).coerceIn(0, part.text.length)
                                                            val hitRange = part.sentenceRanges.firstOrNull { offset in it.start until it.endExclusive }
                                                            hitRange?.let { range ->
                                                                haptics.performHapticFeedback(
                                                                    HapticFeedbackType.Confirm
                                                                )
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
                                            Spacer(modifier = Modifier.height(10.dp))
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
                                    }
                                }

                                // Tactile Book Spine Crease along left edge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .width(10.dp)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

        // 2. Docked Top app bar (Collapsible)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer { translationY = topBarOffset },
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = if (topBarVisible) 3.dp else 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
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
                            } else {
                                topBarVisible = true
                                bottomBarVisible = true
                            }
                        }
                    ) { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface) }
                    IconButton(
                        onClick = onOpenDocumentNotes
                    ) { Icon(Icons.Outlined.EditNote, contentDescription = "Booknotes", tint = MaterialTheme.colorScheme.onSurface) }
                    IconButton(
                        onClick = { showOutline = true }
                    ) { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Outline", tint = MaterialTheme.colorScheme.onSurface) }
                    Box {
                        IconButton(
                            onClick = { showTools = true }
                        ) { Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface) }
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
                            onOpenAskAi = { noPrompt ->
                                shareToAiSelection = null
                                shareToAiNoPrompt = noPrompt
                                showShareToAiSheet = true
                            },
                            onSelectAskAiAssistant = onSelectAskAiAssistant,
                            onOpenTextEditor = onOpenTextEditor,
                            onStartRecord = onStartRecord,
                            onOpenReaderSettings = onOpenReaderSettings,
                            onOpenVoiceStudio = onOpenVoiceStudio,
                            onOpenNarrationStudio = onOpenNarrationStudio,
                            onOpenPronunciationRules = onOpenPronunciationRules,
                            onExportAudio = onExportAudio,
                            onExportStudyGuidePdf = onExportStudyGuidePdf,
                            onToggleQueue = onToggleQueue,
                            onPlayQueue = onPlayQueue,
                            onOpenRsvpSpeedReader = { showRsvpSpeedReader = true }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                ReaderModeToggle(
                    currentMode = state.readerMode,
                    onModeSelected = onReaderModeChange,
                    hasCanvas = hasCanvas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { OnboardingController.updateBounds("reader_mode_toggle", it) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }

        // 3. Floating Bottom Player Panel (Collapsible)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer { translationY = bottomBarOffset }
        ) {
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
                onVoiceSelected = onVoiceSelected,
                documentId = document.id,
                onToggleDocumentMode = {
                    if (hasCanvas) {
                        onOpenCanvas()
                    } else {
                        onReaderModeChange(ReaderMode.ORIGINAL)
                    }
                }
            )
        }

        // 4. Floating Search Panel (Anchored cleanly to IME keyboard)
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
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
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
            onToggleBookmark = {
                if (isBookmarked) {
                    onToggleBookmark(currentIndex)
                } else {
                    colorPaletteTargetIndexes = listOf(currentIndex)
                }
            },
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

    if (showRsvpSpeedReader) {
        RsvpSpeedReader(
            document = document,
            initialSentenceIndex = currentIndex,
            onClose = { targetSentenceIndex ->
                showRsvpSpeedReader = false
                if (targetSentenceIndex != currentIndex) {
                    onSentenceClick(targetSentenceIndex)
                }
            }
        )
    }

    if (colorPaletteTargetIndexes != null) {
        val targetIndexes = colorPaletteTargetIndexes ?: emptyList()
        val existingBookmarks = remember(targetIndexes, state.annotations) {
            state.annotations.filter { it.type == AnnotationType.BOOKMARK && targetIndexes.contains(it.chunkIndex) }
        }
        val isAlreadyBookmarked = existingBookmarks.isNotEmpty()
        val currentHex = existingBookmarks.firstOrNull()?.highlightColor
        val colorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                colorPaletteTargetIndexes = null
                clearNativeTextSelection(selectedTextView)
            },
            sheetState = colorSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isAlreadyBookmarked) "Bookmark Options" else "Highlight Passage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isAlreadyBookmarked) {
                        OutlinedButton(
                            onClick = {
                                targetIndexes.forEach { idx ->
                                    if (state.annotations.any { it.chunkIndex == idx && it.type == AnnotationType.BOOKMARK }) {
                                        onToggleBookmark(idx)
                                    }
                                }
                                colorPaletteTargetIndexes = null
                                clearNativeTextSelection(selectedTextView)
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Outlined.BookmarkRemove,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remove")
                        }
                    }
                }
                Text(
                    text = if (isAlreadyBookmarked) "Select another color to update bookmark:" else "Select a highlight color:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val colors = listOf(
                        "#FFE082" to "Yellow",
                        "#A5D6A7" to "Green",
                        "#90CAF9" to "Blue",
                        "#F48FB1" to "Pink",
                        "#B39DDB" to "Purple",
                        "#FFCC80" to "Orange"
                    )
                    colors.forEach { (hex, name) ->
                        val isSelectedColor = isAlreadyBookmarked && (currentHex.equals(hex, ignoreCase = true) || (currentHex.isNullOrBlank() && hex == "#FFE082"))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                .border(
                                    width = if (isSelectedColor) 3.dp else 2.dp,
                                    color = if (isSelectedColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    colorPaletteTargetIndexes?.let { indexes ->
                                        onAddBookmarkGroup(indexes, hex)
                                    }
                                    colorPaletteTargetIndexes = null
                                    clearNativeTextSelection(selectedTextView)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelectedColor) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Selected color",
                                    tint = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    val currentPage = remember(readerModel.sentences, currentIndex) {
        readerModel.sentences.getOrNull(currentIndex)?.pageNumber ?: 1
    }

    if (showShareToAiSheet) {
        val selectionVal = shareToAiSelection
        var selectedScope by remember { mutableStateOf<ShareScope>(if (selectionVal != null) ShareScope.SELECTED_TEXT else ShareScope.CURRENT_SECTION) }
        var startPageStr by remember { mutableStateOf(currentPage.toString()) }
        var endPageStr by remember { mutableStateOf(currentPage.toString()) }
        
        AlertDialog(
            onDismissRequest = { 
                showShareToAiSheet = false
                onDismissShareToAi()
            },
            title = {
                Text(
                    text = "Ask AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Select how much of the document content you'd like to share as a Markdown file (.md):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (selectionVal != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedScope = ShareScope.SELECTED_TEXT }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (selectedScope == ShareScope.SELECTED_TEXT),
                                onClick = { selectedScope = ShareScope.SELECTED_TEXT }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selected Text (${selectionVal.sentenceIndexes.size} sentences)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    val partLabel = currentPart?.let { "Section ${it.index + 1} (${if (document.sourceLabel == "PPTX") "Slides" else "Pages"} ${it.pageRange.startPage}-${it.pageRange.endPage})" } ?: "Current Section"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedScope = ShareScope.CURRENT_SECTION }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedScope == ShareScope.CURRENT_SECTION),
                            onClick = { selectedScope = ShareScope.CURRENT_SECTION }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(partLabel, style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedScope = ShareScope.CUSTOM_PAGE_RANGE }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedScope == ShareScope.CUSTOM_PAGE_RANGE),
                            onClick = { selectedScope = ShareScope.CUSTOM_PAGE_RANGE }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Custom Page Range", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    if (selectedScope == ShareScope.CUSTOM_PAGE_RANGE) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(start = 32.dp)
                        ) {
                            OutlinedTextField(
                                value = startPageStr,
                                onValueChange = { startPageStr = it.filter { char -> char.isDigit() } },
                                label = { Text("From") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endPageStr,
                                onValueChange = { endPageStr = it.filter { char -> char.isDigit() } },
                                label = { Text("To") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedScope = ShareScope.ENTIRE_DOCUMENT }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedScope == ShareScope.ENTIRE_DOCUMENT),
                            onClick = { selectedScope = ShareScope.ENTIRE_DOCUMENT }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entire Document (${document.pageCount} pages)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = startPageStr.toIntOrNull() ?: 1
                        val end = endPageStr.toIntOrNull() ?: 1
                        val range = if (selectedScope == ShareScope.CUSTOM_PAGE_RANGE) {
                            val min = minOf(start, end).coerceIn(1, document.pageCount)
                            val max = maxOf(start, end).coerceIn(1, document.pageCount)
                            min..max
                        } else {
                            null
                        }
                        
                        onShareToAi(selectedScope, shareToAiSelection, range, shareToAiNoPrompt)
                        showShareToAiSheet = false
                        onDismissShareToAi()
                    }
                ) {
                    Text("Share", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showShareToAiSheet = false
                    onDismissShareToAi()
                }) {
                    Text("Cancel")
                }
            }
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
    onOpenAskAi: (Boolean) -> Unit,
    onSelectAskAiAssistant: (AiAssistantOption, String) -> Unit,
    onOpenTextEditor: () -> Unit,
    onStartRecord: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenVoiceStudio: () -> Unit,
    onOpenNarrationStudio: () -> Unit,
    onOpenPronunciationRules: () -> Unit,
    onExportAudio: () -> Unit,
    onExportStudyGuidePdf: () -> Unit = {},
    onToggleQueue: () -> Unit,
    onPlayQueue: () -> Unit,
    onOpenRsvpSpeedReader: () -> Unit = {}
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
            text = { Text("RSVP Speed Reader") },
            leadingIcon = { Icon(Icons.Outlined.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            onClick = { choose(onOpenRsvpSpeedReader) })
        DropdownMenuItem(
            text = { Text(if (showSearch) "Hide search" else "Search document") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            onClick = { choose(onToggleSearch) })
        DropdownMenuItem(
            text = { Text("Original View") },
            leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
            enabled = hasCanvas,
            onClick = { choose(onOpenCanvas) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.SLEEP_TIMER),
            label = if (sleepTimerLabel.isBlank()) "Sleep timer" else sleepTimerLabel,
            onClick = { choose(onOpenSleepTimer) },
            leadingIcon = Icons.Filled.Timer
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.READING_LISTS),
            label = "Reading lists ($activeDocumentReadingListCount/$readingListCount)",
            onClick = { choose(onOpenReadingLists) },
            leadingIcon = Icons.Filled.CollectionsBookmark
        )
        DropdownMenuItem(
            text = { Text(if (isQueued) "Remove from Queue" else "Add to Queue") },
            leadingIcon = {
                Icon(
                    if (isQueued) Icons.AutoMirrored.Filled.PlaylistAddCheck else Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = null
                )
            },
            onClick = { choose(onToggleQueue) })
        DropdownMenuItem(
            text = { Text("Play Queue ($queueCount)") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
            enabled = queueCount > 0,
            onClick = { choose(onPlayQueue) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.READING_HISTORY),
            label = "Reading history",
            onClick = { choose(onOpenReadingHistory) },
            leadingIcon = Icons.Filled.History
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            "Notes and bookmarks",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        DropdownMenuItem(
            text = { Text(if (showBookmarks) "Hide bookmarks" else "Bookmarks") },
            leadingIcon = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
            onClick = { choose(onToggleBookmarks) })
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.BOOKMARKS_AND_NOTES),
            label = "Document notes${if (noteCount > 0) " • $noteCount sentence${if (noteCount == 1) "" else "s"}" else ""}",
            onClick = { choose(onOpenDocumentNotes) },
            leadingIcon = Icons.Filled.EditNote
        )
        DropdownMenuItem(
            text = { Text("Export Study Guide PDF") },
            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null) },
            onClick = { choose(onExportStudyGuidePdf) }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            "Study",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        DropdownMenuItem(
            text = { Text("AI Assistant: ${askAiSettings.assistantLabel}") },
            leadingIcon = { Icon(aiAssistantIcon(askAiSettings.assistantId), contentDescription = null, modifier = Modifier.size(20.dp)) },
            onClick = { showAiChooser = !showAiChooser }
        )
        if (showAiChooser) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Default Assistant App",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                    aiAssistantOptions.filter { it.packageName.isNotBlank() }.forEach { option ->
                        val installedPackage = installedPackageForOption(context, option)
                        val isSelected = askAiSettings.assistantId == option.id
                        DropdownMenuItem(
                            leadingIcon = { Icon(aiAssistantIcon(option.id), contentDescription = null, modifier = Modifier.size(18.dp)) },
                            text = {
                                Text(
                                    "${if (isSelected) "✓ " else ""}${option.label}${if (installedPackage == null) " • install" else ""}",
                                    color = if (installedPackage == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
            }
        }
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.AI_APP_HANDOFF),
            label = "Ask AI",
            leadingIcon = Icons.Outlined.AutoAwesome,
            onClick = {
                choose {
                    onOpenAskAi(false)
                }
            }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.AI_APP_HANDOFF),
            label = "AI Ask current part",
            leadingIcon = Icons.Outlined.Psychology,
            onClick = { choose(onAskCurrentSection) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.OFFLINE_STUDY_TOOLS),
            label = "AI Study tools",
            leadingIcon = Icons.Outlined.School,
            onClick = {
                choose {
                    onOpenStudyTools()
                }
            }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.TRANSLATION_HANDOFF),
            label = "Translation handoff",
            leadingIcon = Icons.Outlined.Translate,
            onClick = { choose(onOpenTranslationTools) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.EXTRACTED_TEXT_EDITOR),
            label = "Edit extracted text",
            leadingIcon = Icons.Outlined.EditNote,
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
            label = "Voice and language",
            leadingIcon = Icons.Outlined.RecordVoiceOver,
            onClick = { choose(onOpenVoiceStudio) }
        )
        DropdownMenuItem(
            text = { Text(if (narrationEnabled) "Narration mode on" else "Narration mode") },
            leadingIcon = { Icon(Icons.Outlined.TheaterComedy, contentDescription = null) },
            onClick = { choose(onOpenNarrationStudio) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.PRONUNCIATION_RULES),
            label = "Pronunciation rules",
            leadingIcon = Icons.Outlined.Spellcheck,
            onClick = { choose(onOpenPronunciationRules) }
        )
        DropdownMenuItem(
            text = { Text("Reader appearance") },
            leadingIcon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
            onClick = { choose(onOpenReaderSettings) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT),
            label = "Record sound file",
            leadingIcon = Icons.Outlined.GraphicEq,
            onClick = { choose(onStartRecord) }
        )
        FeatureDropdownMenuItem(
            feature = readerFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT),
            label = "Export audio",
            leadingIcon = Icons.Outlined.FileDownload,
            onClick = { choose(onExportAudio) }
        )
    }
}

@Composable
internal fun FeatureDropdownMenuItem(
    feature: ResolvedVeritasFeature,
    label: String,
    onClick: () -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    DropdownMenuItem(
        text = { FeatureMenuText(feature, label) },
        enabled = feature.enabled,
        onClick = onClick,
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } }
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
    var tickerNow by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeTimer) {
        if (activeTimer != null && (activeTimer.stopAtEndOfSection || activeTimer.isActive())) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                tickerNow = System.currentTimeMillis()
            }
        }
    }

    var selectedDurationMillis by remember {
        mutableLongStateOf(activeTimer?.durationMillis?.takeIf { it > 0L } ?: (15L * 60L * 1000L))
    }
    var selectedAction by remember {
        mutableStateOf(activeTimer?.action ?: VeritasSleepTimerAction.PAUSE)
    }
    var modeBySection by remember {
        mutableStateOf(activeTimer?.stopAtEndOfSection ?: false)
    }
    val isTimerActive = activeTimer != null && (activeTimer.stopAtEndOfSection || activeTimer.isActive(tickerNow))
    val activeLabel = activeTimer?.takeIf { it.stopAtEndOfSection || it.isActive(tickerNow) }?.menuLabel(tickerNow)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Sleep timer")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isTimerActive && activeLabel != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = VeritasPackStyle.surfaceAlpha())),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(activeLabel, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    activeTimer.action.label,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            OutlinedButton(
                                onClick = onCancelTimer,
                                shape = VeritasPackStyle.chipShape(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Cancel timer")
                            }
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
                Text(if (isTimerActive) "Update timer" else "Start timer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = VeritasPackStyle.chipShape()) {
                Text("Close")
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
    highlightedSentences: Map<Int, String>,
    searchMatches: List<Int>,
    searchCursor: Int,
    activeSentenceColor: Int,
    defaultHighlightColor: Int,
    feedbackColor: Int,
    searchMatchColor: Int,
    activeSearchMatchColor: Int,
    bionicReading: Boolean = false
): SpannableString {
    val spannable = SpannableString(part.text)
    // Render inline markdown (bold/italic/headings/etc.) that pasted text often carries,
    // so the reader shows formatting instead of literal ** and ## markers. Crucially the
    // delimiter characters are kept in the text and only drawn zero-width, so every
    // downstream character offset (TTS word highlight, selection→sentence mapping, search)
    // still lines up with part.text.
    applyMarkdownFormatting(spannable, part.text)
    if (bionicReading) {
        val activeRange = part.sentenceRanges.firstOrNull { it.sentenceIndex == activeSentenceIndex }
        if (activeRange != null) {
            applyBionicFormatting(spannable, part.text, activeRange.start, activeRange.endExclusive)
        }
    }
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
        val hexColor = highlightedSentences[range.sentenceIndex]
        if (hexColor != null) {
            val parsedColor = runCatching { android.graphics.Color.parseColor(hexColor) }
                .getOrDefault(defaultHighlightColor)
            val colorWithAlpha = (parsedColor and 0x00FFFFFF) or (0x66 shl 24)
            addBackground(range.start, range.endExclusive, colorWithAlpha)
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

/**
 * Bionic reading formatter: bolds the first 2–3 letters of each word in the active reading area
 * to guide the eye's fixation points in sync with audio narration.
 */
private fun applyBionicFormatting(
    spannable: SpannableString,
    text: String,
    startOffset: Int = 0,
    endOffset: Int = text.length
) {
    var inWord = false
    var wordStart = 0
    val start = startOffset.coerceIn(0, text.length)
    val end = endOffset.coerceIn(start, text.length)
    for (i in start..end) {
        val char = if (i < end) text[i] else ' '
        val isWordChar = char.isLetterOrDigit()
        if (isWordChar && !inWord) {
            inWord = true
            wordStart = i
        } else if (!isWordChar && inWord) {
            inWord = false
            val wordLen = i - wordStart
            val fixationLen = when {
                wordLen <= 1 -> 1
                wordLen <= 3 -> 1
                wordLen <= 6 -> 2
                wordLen <= 9 -> 3
                wordLen <= 12 -> 4
                else -> (wordLen * 0.45f).toInt().coerceAtLeast(3)
            }
            val fixationEnd = (wordStart + fixationLen).coerceAtMost(i)
            if (fixationEnd > wordStart) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    wordStart,
                    fixationEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}

/**
 * A span that occupies its character range but renders nothing and takes zero width — used to
 * hide markdown delimiters (** __ ~~ ` #) without deleting them, so character offsets are
 * preserved for the reader's highlight/selection/search machinery.
 */
private class HiddenMarkupSpan : ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int = 0

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        // Intentionally draw nothing.
    }
}

private fun SpannableString.hideMarkup(start: Int, end: Int) {
    if (start in 0 until end && end <= length) {
        setSpan(HiddenMarkupSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

private fun SpannableString.styleRange(span: Any, start: Int, end: Int) {
    if (start in 0 until end && end <= length) {
        setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

/**
 * Applies a conservative subset of markdown (ATX headings, **bold**, *italic*, `code`,
 * ~~strikethrough~~) as visual spans over [text]. Only well-formed, clearly-delimited markers
 * are styled; anything ambiguous (a lone asterisk, a bullet "* item", "2 * 3") is left as plain
 * text so ordinary prose is never mangled.
 */
private fun applyMarkdownFormatting(spannable: SpannableString, text: String) {
    if (text.isEmpty()) return
    var lineStart = 0
    while (lineStart <= text.length) {
        val newline = text.indexOf('\n', lineStart)
        val lineEnd = if (newline == -1) text.length else newline
        applyMarkdownLine(spannable, text, lineStart, lineEnd)
        if (newline == -1) break
        lineStart = newline + 1
    }
}

private fun applyMarkdownLine(spannable: SpannableString, text: String, lineStart: Int, lineEnd: Int) {
    if (lineStart >= lineEnd) return
    // Completely suppress any accidental internal page markers from displaying on screen
    val trimmedLine = text.substring(lineStart, lineEnd).trim()
    if (trimmedLine.contains("VERITAS_PAGE", ignoreCase = true) || trimmedLine.contains("veritas page", ignoreCase = true)) {
        spannable.hideMarkup(lineStart, lineEnd)
        return
    }

    // ATX heading: optional leading spaces, 1–6 '#', then a space and the heading text.
    var cursor = lineStart
    while (cursor < lineEnd && text[cursor] == ' ') cursor++
    var hashes = 0
    while (cursor < lineEnd && text[cursor] == '#' && hashes < 6) {
        cursor++
        hashes++
    }
    if (hashes in 1..6 && cursor < lineEnd && text[cursor] == ' ') {
        val contentStart = cursor + 1
        spannable.hideMarkup(lineStart, contentStart)
        val relativeSize = when (hashes) {
            1 -> 1.45f
            2 -> 1.28f
            else -> 1.18f
        }
        spannable.styleRange(RelativeSizeSpan(relativeSize), contentStart, lineEnd)
        spannable.styleRange(StyleSpan(Typeface.BOLD), contentStart, lineEnd)
        applyInlineMarkdown(spannable, text, contentStart, lineEnd)
        return
    }

    // Blockquote: starts with '>'
    if (cursor < lineEnd && text[cursor] == '>') {
        val quoteStart = if (cursor + 1 < lineEnd && text[cursor + 1] == ' ') cursor + 2 else cursor + 1
        spannable.hideMarkup(lineStart, quoteStart)
        spannable.styleRange(StyleSpan(Typeface.ITALIC), quoteStart, lineEnd)
        applyInlineMarkdown(spannable, text, quoteStart, lineEnd)
        return
    }

    // Bullet items: starts with "- ", "* ", or "• "
    if (cursor < lineEnd && (text.startsWith("- ", cursor) || text.startsWith("* ", cursor) || text.startsWith("• ", cursor))) {
        spannable.styleRange(StyleSpan(Typeface.BOLD), cursor, cursor + 1)
        applyInlineMarkdown(spannable, text, cursor + 2, lineEnd)
        return
    }

    // Tabular formatting with monospace for pipe-delimited tables
    if (trimmedLine.startsWith("|") && trimmedLine.endsWith("|") && trimmedLine.length > 2) {
        spannable.styleRange(TypefaceSpan("monospace"), lineStart, lineEnd)
        return
    }

    // Chapter / section heading detection (e.g. "CHAPTER ONE", "Chapter 1", "Prologue")
    val isChapterHeading = Regex("""^(CHAPTER|Chapter|PROLOGUE|Prologue|EPILOGUE|Epilogue|INTRODUCTION|Introduction|PREFACE|Preface|PART|Part|BOOK|Book)\b.*""", RegexOption.IGNORE_CASE).matches(trimmedLine)
    if (isChapterHeading) {
        spannable.styleRange(RelativeSizeSpan(1.35f), lineStart, lineEnd)
        spannable.styleRange(StyleSpan(Typeface.BOLD), lineStart, lineEnd)
        return
    }

    // Standalone uppercase headings
    if (trimmedLine.length in 4..60 && trimmedLine.any { it.isLetter() } && trimmedLine.all { !it.isLetter() || it.isUpperCase() }) {
        spannable.styleRange(RelativeSizeSpan(1.22f), lineStart, lineEnd)
        spannable.styleRange(StyleSpan(Typeface.BOLD), lineStart, lineEnd)
        return
    }

    applyInlineMarkdown(spannable, text, lineStart, lineEnd)
}

private fun applyInlineMarkdown(spannable: SpannableString, text: String, start: Int, end: Int) {
    var i = start
    while (i < end) {
        val c = text[i]
        when {
            // **bold**
            c == '*' && i + 1 < end && text[i + 1] == '*' -> {
                val close = text.indexOf("**", i + 2)
                if (close != -1 && close + 2 <= end && close > i + 2) {
                    spannable.styleRange(StyleSpan(Typeface.BOLD), i + 2, close)
                    spannable.hideMarkup(i, i + 2)
                    spannable.hideMarkup(close, close + 2)
                    i = close + 2
                    continue
                }
            }
            // ~~strikethrough~~
            c == '~' && i + 1 < end && text[i + 1] == '~' -> {
                val close = text.indexOf("~~", i + 2)
                if (close != -1 && close + 2 <= end && close > i + 2) {
                    spannable.styleRange(StrikethroughSpan(), i + 2, close)
                    spannable.hideMarkup(i, i + 2)
                    spannable.hideMarkup(close, close + 2)
                    i = close + 2
                    continue
                }
            }
            // `inline code`
            c == '`' -> {
                val close = text.indexOf('`', i + 1)
                if (close != -1 && close < end && close > i + 1) {
                    spannable.styleRange(TypefaceSpan("monospace"), i + 1, close)
                    spannable.hideMarkup(i, i + 1)
                    spannable.hideMarkup(close, close + 1)
                    i = close + 1
                    continue
                }
            }
            // *italic* — only when the delimiters hug non-space text (so bullets "* item"
            // and arithmetic "2 * 3" are never treated as emphasis).
            c == '*' && i + 1 < end && !text[i + 1].isWhitespace() -> {
                var j = i + 1
                var close = -1
                while (j < end) {
                    val cj = text[j]
                    if (cj == '\n') break
                    if (cj == '*' && !text[j - 1].isWhitespace()) {
                        close = j
                        break
                    }
                    j++
                }
                if (close > i + 1) {
                    spannable.styleRange(StyleSpan(Typeface.ITALIC), i + 1, close)
                    spannable.hideMarkup(i, i + 1)
                    spannable.hideMarkup(close, close + 1)
                    i = close + 1
                    continue
                }
            }
        }
        i++
    }
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
    onHighlightSelection: (ReaderTextSelection) -> Unit,
    onEditNotes: (List<Int>) -> Unit,
    onTranslateSelection: (String) -> Unit,
    onCopySelection: (String) -> Unit,
    onGoogleSelection: (String) -> Unit,
    onShareSelection: (String) -> Unit,
    onShareSelectionToAi: (ReaderTextSelection) -> Unit,
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
        clearNativeTextSelection(textView)
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
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
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
                READER_SELECTION_BOOKMARK -> onHighlightSelection(selection)

                READER_SELECTION_COPY -> onCopySelection(selection.text)
                READER_SELECTION_SEARCH -> onSearchQueryChange(selection.text.take(80))
                READER_SELECTION_TRANSLATE -> onTranslateSelection(selection.text)
                READER_SELECTION_GOOGLE -> onGoogleSelection(selection.text)
                READER_SELECTION_EDIT_TEXT -> onEditExtractedSelection(selection)
                READER_SELECTION_EDIT_SPEECH -> onEditSpeechSelection(selection.text)
                READER_SELECTION_READ_ALOUD -> onReadSelection(selection.text)
                READER_SELECTION_ASK_AI -> onShareSelectionToAi(selection)
                else -> return false
            }
            finish(mode)
            clearNativeTextSelection(textView)
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            onSelectionChanged(null)
            clearNativeTextSelection(textView)
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
                    imageVector = Icons.Outlined.Translate,
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
            .navigationBarsPadding()
            .imePadding(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    onClose()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            if (matchCount > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "$currentMatch of $matchCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else if (query.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "0 found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
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
/** How many leading sentences may hold a contents page. */
private const val MAX_SMART_OUTLINE_TOC_SCAN = 400
/** How far a contents listing may run past its heading. */
private const val MAX_SMART_OUTLINE_TOC_SPAN = 120
/**
 * The span of sentences occupied by a table of contents, or null if there is none.
 *
 * Sentence splitting shreds a contents page: "The Sign of the Four . . . . . 63"
 * arrives as a chunk reading "1 The Sign of the Four ." with the page number split
 * away. Nothing about that fragment looks like a contents row any more — but it does
 * satisfy the numbered-heading rule, so each fragment became its own outline entry
 * pointing back at the contents page. Excluding the region by position is the only
 * reliable defence, since the pattern is gone by the time we see it.
 */
private fun findContentsRange(chunks: List<String>): IntRange? {
    val startIndex = chunks.take(MAX_SMART_OUTLINE_TOC_SCAN).indexOfFirst { chunk ->
        val head = chunk.take(200)
        head.contains("table of contents", ignoreCase = true) ||
            chunk.lineSequence().any { it.trim().equals("contents", ignoreCase = true) }
    }
    if (startIndex < 0) return null

    // Walk forward while the chunks still look like listing debris: very short, or
    // leader dots, or a bare number, or a fragment opening with a page number.
    var end = startIndex
    var misses = 0
    var index = startIndex + 1
    while (index <= chunks.lastIndex && index - startIndex < MAX_SMART_OUTLINE_TOC_SPAN) {
        val text = chunks[index].replace(Regex("\\s+"), " ").trim()
        val debris = text.isBlank() ||
            text.length < 60 ||
            Regex("^[.\\s\\u00b7\\u2022]+$").matches(text) ||
            Regex("^\\d{1,4}\\b").containsMatchIn(text) ||
            Regex("[.\\s]{3,}$").containsMatchIn(text)
        if (debris) {
            end = index
            misses = 0
        } else {
            misses++
            if (misses >= 3) break
        }
        index++
    }
    return startIndex..end
}

/** Position markers offered only when a document has no detectable structure. */
private const val MAX_SMART_OUTLINE_FALLBACK_MARKERS = 40
/** A weak-signal heading repeating this often is a running header. */
private const val MAX_OUTLINE_TITLE_REPEATS = 3

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
                        key = { index, entry -> "$index:${entry.index}:${entry.title}" }) { idx, entry ->
                        val nextEntryIndex = filteredEntries.getOrNull(idx + 1)?.index ?: Int.MAX_VALUE
                        val active = currentIndex >= entry.index && (currentIndex < nextEntryIndex || idx == filteredEntries.lastIndex)
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
                                    // Location only. The body preview that used to sit
                                    // under each row was the sentence the entry lands on,
                                    // which says nothing about the section and turned the
                                    // list into a wall of prose. The dialog title already
                                    // states whether this is a real table of contents.
                                    Text(
                                        listOfNotNull(
                                            entry.pageNumber?.let { "Page $it" },
                                            "Sentence ${entry.index + 1}"
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
    // Scan the whole document: capping structure detection at the first 1,200
    // sentences meant a long book's outline stopped a few percent in.
    val contentsRange = findContentsRange(chunks)
    val structuralEntries = (
            extractTableOfContentsOutline(chunks) + extractHeadingOutline(chunks, contentsRange)
            )
        .distinctBy { it.index }
        .let(::dropRunningHeaders)
        .sortedBy { it.index }
        .take(MAX_SMART_OUTLINE_ENTRIES)

    if (structuralEntries.isNotEmpty()) return structuralEntries

    // No structure found. Offer evenly spaced position markers instead of adding
    // every Nth sentence as though it were a heading, which buried any real entry
    // among hundreds of arbitrary ones.
    val markerCount = MAX_SMART_OUTLINE_FALLBACK_MARKERS.coerceAtMost(chunks.size)
    if (markerCount <= 0) return emptyList()
    val step = (chunks.size / markerCount).coerceAtLeast(1)
    val fallbackIndexes = (0 until chunks.size step step).toMutableList().also { marks ->
        if (chunks.isNotEmpty() && marks.lastOrNull() != chunks.lastIndex) marks.add(chunks.lastIndex)
    }

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

/**
 * Removes running headers that repeat across the book.
 *
 * A book title printed at the top of every page produces one identical candidate per
 * page — "The Hound of the Baskervilles" five times here — none of which is a section.
 *
 * Only the weak title-case and all-caps matches are filtered. Keyword and numbered
 * headings are left alone deliberately: "CHAPTER I." legitimately recurs once per
 * story in a collection, and frequency-filtering those would delete real entries.
 */
private fun dropRunningHeaders(entries: List<SmartOutlineEntry>): List<SmartOutlineEntry> {
    val counts = entries.groupingBy { normalizeOutlineNeedle(it.title) }.eachCount()
    return entries.filter { entry ->
        val key = normalizeOutlineNeedle(entry.title)
        val repeats = counts[key] ?: 0
        if (repeats < MAX_OUTLINE_TITLE_REPEATS) return@filter true
        // Keep a repeated title only when it carries an explicit structural marker.
        Regex("^(chapter|part|section|book|adventure|volume)\\b", RegexOption.IGNORE_CASE)
            .containsMatchIn(entry.title.trim()) ||
            Regex("^\\d+[.)\\s]").containsMatchIn(entry.title.trim())
    }
}


/**
 * Parses a table-of-contents page into outline entries.
 *
 * Extracted PDF text mangles contents pages two ways this has to survive. Leader dots
 * end up inside the line ("The Red-Headed League . . . . . 119"), and adjacent rows
 * often merge, so a line arrives carrying the *previous* entry's page number on the
 * front ("63 The Adventures of Sherlock Holmes ... 119"). Both are stripped.
 *
 * Targets resolve from *after* the contents region. Searching the whole document
 * matched each title inside the contents listing itself, so every entry navigated
 * back to the table of contents instead of to its chapter.
 */
private fun extractTableOfContentsOutline(chunks: List<String>): List<SmartOutlineEntry> {
    val tocLine = Regex("^(.{3,140}?)[\\s.]*?(?:\\.{2,}|\\s{3,}|\\t+)[\\s.]*(\\d{1,4})$")
    val contentsIndexes = mutableListOf<Int>()
    chunks.take(MAX_SMART_OUTLINE_TOC_SCAN).forEachIndexed { index, chunk ->
        val isContents = chunk.take(400).contains("table of contents", ignoreCase = true) ||
            chunk.lineSequence().any { it.trim().equals("contents", ignoreCase = true) }
        if (isContents) contentsIndexes.add(index)
    }
    if (contentsIndexes.isEmpty()) return emptyList()

    val tocStart = contentsIndexes.first()
    val tocEnd = (contentsIndexes.last() + MAX_SMART_OUTLINE_TOC_SPAN).coerceAtMost(chunks.lastIndex)
    val bodyStart = (tocEnd + 1).coerceAtMost(chunks.lastIndex)

    val seen = mutableSetOf<String>()
    val entries = mutableListOf<SmartOutlineEntry>()
    for (index in tocStart..tocEnd) {
        chunks.getOrNull(index)?.lineSequence()
            ?.map { it.trim() }
            ?.filter { it.length in 6..160 }
            ?.forEach { line ->
                val match = tocLine.matchEntire(line) ?: return@forEach
                val title = cleanTocTitle(match.groupValues[1])
                if (title.length < 3) return@forEach
                val key = normalizeOutlineNeedle(title)
                if (key.length < 4 || !seen.add(key)) return@forEach
                val target = locateOutlineTarget(chunks, title, bodyStart) ?: return@forEach
                val clean = chunks.getOrNull(target).orEmpty().replace(Regex("\\s+"), " ").trim()
                entries.add(
                    SmartOutlineEntry(
                        index = target,
                        title = title.take(96),
                        preview = clean.take(180),
                        isHeading = true
                    )
                )
            }
    }
    return entries
}

/**
 * Strips leader dots and a stray leading page number from a contents line.
 *
 * The number in "63 The Adventures of Sherlock Holmes" belongs to the row above it.
 * It is only dropped when enough text follows for that text to be the real title, so
 * a genuinely numbered heading is left intact.
 */
private fun cleanTocTitle(raw: String): String {
    var title = raw.trim().trim('.', '-', '\u2022', '\u00b7', ' ')
    title = title.replace(Regex("[.\\s]{3,}$"), "").trim()
    Regex("^(\\d{1,4})\\s+(\\p{L}.*)$").matchEntire(title)?.let { m ->
        val rest = m.groupValues[2].trim()
        if (rest.length >= 4) title = rest
    }
    return title.replace(Regex("\\s+"), " ").trim()
}
/**
 * Reassembles a heading the text extractor split mid-word.
 *
 * "CHAPTER II." routinely arrives as two chunks — "CHAPT" then "ER II." — because the
 * extractor breaks on the page's column boundary. Measured on The Complete Sherlock
 * Holmes: 33 occurrences of the fragment "CHAPT", against 30 intact "CHAPTER n."
 * lines, so roughly half the book's chapter headings were unreachable as headings and
 * showed up as meaningless stubs instead.
 *
 * A join is only attempted when the first fragment is a short run of letters with no
 * spaces and no terminal punctuation — a word cut in half, never a real short heading.
 */
private fun joinSplitHeading(chunks: List<String>, index: Int): String? {
    val head = chunks.getOrNull(index)?.trim() ?: return null
    if (head.length > 8 || head.isEmpty()) return null
    if (head.any { it.isWhitespace() } || head.any { !it.isLetter() }) return null
    val tail = chunks.getOrNull(index + 1)?.trim().orEmpty()
    if (tail.isEmpty() || tail.first().isWhitespace()) return null
    val joined = (head + tail).trim()
    return joined.takeIf { it.length in 4..120 }
}


/**
 * Headings found in the body of the document.
 *
 * Contents-page rows are excluded. A line like "1 The Sign of the Four . . . . 63"
 * satisfies the numbered-heading rule, so every row of a table of contents used to
 * become its own outline entry pointing at the contents page — which is why the
 * outline read like the TOC and every entry jumped to the same few sentences.
 */
private fun extractHeadingOutline(
    chunks: List<String>,
    contentsRange: IntRange? = null
): List<SmartOutlineEntry> {
    return chunks.mapIndexedNotNull { index, chunk ->
        if (contentsRange != null && index in contentsRange) return@mapIndexedNotNull null
        if (looksLikeTableOfContentsRow(chunk)) return@mapIndexedNotNull null

        // A heading the extractor cut in half is repaired before it is judged.
        joinSplitHeading(chunks, index)?.let { repaired ->
            if (looksLikeOutlineHeading(repaired)) {
                return@mapIndexedNotNull SmartOutlineEntry(
                    index = index,
                    title = outlineTitle(cleanTocTitle(repaired), index),
                    preview = "",
                    isHeading = true
                )
            }
        }
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
            title = outlineTitle(cleanTocTitle(heading), index),
            preview = if (clean.startsWith(heading)) clean.removePrefix(heading).trim()
                .take(180) else clean.take(180),
            isHeading = true
        )
    }
}

/**
 * True for a line shaped like a contents listing: leader dots or a wide gap followed
 * by a page number, or a run of leader dots on its own.
 */
private fun looksLikeTableOfContentsRow(chunk: String): Boolean {
    val line = chunk.lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() }
        .orEmpty()
    if (line.isBlank()) return false
    if (Regex("^[.\\s\\u00b7\\u2022]+$").matches(line)) return true
    return Regex("(?:\\.\\s*){2,}\\s*\\d{1,4}\\s*$").containsMatchIn(line) ||
        Regex("\\s{3,}\\d{1,4}\\s*$").containsMatchIn(line)
}

/**
 * Finds where a contents title actually appears in the body.
 *
 * [from] skips the contents region so a title cannot resolve to its own listing, and
 * the scan runs to the end of the document rather than stopping at a fixed window —
 * chapter headings in a long book sit far past any leading cap.
 */
private fun locateOutlineTarget(chunks: List<String>, title: String, from: Int = 0): Int? {
    val needle = normalizeOutlineNeedle(title)
    if (needle.length < 4) return null
    for (index in from..chunks.lastIndex) {
        if (normalizeOutlineNeedle(chunks[index].take(600)).contains(needle)) return index
    }
    val compact = needle.split(' ').take(6).joinToString(" ")
    if (compact.length >= 8) {
        for (index in from..chunks.lastIndex) {
            if (normalizeOutlineNeedle(chunks[index].take(600)).contains(compact)) return index
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

    val words = clean.split(Regex("\\s+")).filter { word -> word.any { it.isLetter() } }

    val headingKeyword = Regex(
        pattern = "^(chapter|section|part|unit|lesson|module|book|article|introduction|conclusion|summary|abstract|contents|references|appendix|glossary|index|foreword|preface|prologue|epilogue|bibliography|afterword|notes|citations|sources)\\b",
        option = RegexOption.IGNORE_CASE
    ).containsMatchIn(clean)

    // "1.2 Methods" or "IV. The Sign of the Four". The roman-numeral branch is
    // deliberately case-SENSITIVE and refuses a bare "I": matching it case-insensitively
    // made every sentence beginning "I " a heading — along with any opening on did,
    // mix, civil or mild — which filled the outline of a novel with narration.
    val arabicHeading = Regex("^\\d+(\\.\\d+)*[.)\\s:-]+").containsMatchIn(clean)
    val romanHeading = Regex("^(?!I\\b)[IVXLCDM]{1,7}[.)\\s:-]+").containsMatchIn(clean)
    val numberedHeading = arabicHeading || romanHeading

    // Anchored to the start of the line. These are ordinary English words — "case",
    // "step", "result", "goal" — so matching them anywhere marked any sentence that
    // happened to contain one as a heading.
    val landmarkKeyword = Regex(
        pattern = "^(Task|Requirement|Exercise|Solution|Example|Definition|Theorem|Lemma|Proof|Corollary|Proposition|Remark|Case|Scenario|Feature|Instruction|Step|Goal|Outcome|Impact|Conclusion|Recommendation|Background|Methodology|Result|Discussion|Future Work)\\b",
        option = RegexOption.IGNORE_CASE
    ).containsMatchIn(clean)

    val titleCaseWords =
        words.count { word -> word.firstOrNull { it.isLetter() }?.isUpperCase() == true }
    val mostlyTitleCase =
        words.isNotEmpty() && titleCaseWords >= maxOf(1, (words.size * 0.70f).roundToInt())
    val allCaps =
        words.isNotEmpty() && words.all { word -> word.all { !it.isLetter() || it.isUpperCase() } }
    val compactHeading = !clean.endsWith(".") && clean.count { it == ',' } <= 1 && clean.length < 90

    // A heading is a label, not a sentence. Sentence-like punctuation disqualifies the
    // weaker signals even when a keyword matched.
    val sentenceLike = clean.length > 90 || clean.count { it == ',' } > 1 ||
        Regex("[.!?]\\s+\\p{Lu}").containsMatchIn(clean)
    if (sentenceLike) return false

    // A bare page number off a running header is not a heading.
    if (clean.none { it.isLetter() }) return false

    // Neither is a one-word fragment such as the "CHAPT" left behind when a running
    // header is split mid-word. Real one-word headings ("Introduction", "Appendix")
    // come through the keyword rules instead.
    if (words.size < 2 && !headingKeyword && !landmarkKeyword) return false

    return headingKeyword || numberedHeading || landmarkKeyword ||
        ((mostlyTitleCase || allCaps) && compactHeading)
}


@Composable
fun BooknotesDialog(
    document: ReaderDocument,
    annotations: List<ReaderAnnotation>,
    documentNote: String,
    currentIndex: Int,
    onDocumentNoteChange: (String) -> Unit,
    onSaveDocumentNote: () -> Unit,
    onAddCurrentNote: () -> Unit,
    onJumpToSection: (Int) -> Unit,
    onExportNotes: () -> Unit,
    onExportPdf: () -> Unit = {},
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
                    onClick = onExportPdf,
                    enabled = documentNote.isNotBlank() || annotations.isNotEmpty(),
                    shape = VeritasPackStyle.chipShape()
                ) {
                    Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Study Guide PDF")
                }
                OutlinedButton(
                    onClick = onExportNotes,
                    enabled = documentNote.isNotBlank() || notes.any { it.note.isNotBlank() },
                    shape = VeritasPackStyle.chipShape()
                ) {
                    Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
                TextButton(onClick = onDismiss, shape = VeritasPackStyle.chipShape()) { Text("Close") }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Booknotes")
            }
        },
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
                            "Notes attached to individual sentences.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onAddCurrentNote,
                        shape = VeritasPackStyle.chipShape()
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add current")
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
                                    Icon(
                                        Icons.AutoMirrored.Filled.NavigateNext,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    note.note.ifBlank { "Empty note" },
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (note.audioPath != null) {
                                    AssistChip(
                                        onClick = { com.veritas.reader.VoiceNoteRecorder.playAudio(note.audioPath) },
                                        label = { Text("🎙️ Voice Memo (${note.audioDurationSeconds}s)") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
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
    onExportPdf: () -> Unit = {},
    onDismiss: () -> Unit
) {
    BooknotesDialog(
        document = document,
        annotations = annotations,
        documentNote = documentNote,
        currentIndex = currentIndex,
        onDocumentNoteChange = onDocumentNoteChange,
        onSaveDocumentNote = onSaveDocumentNote,
        onAddCurrentNote = onAddCurrentNote,
        onJumpToSection = onJumpToSection,
        onExportNotes = onExportNotes,
        onExportPdf = onExportPdf,
        onDismiss = onDismiss
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeritasThinRoundSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trackHeight: androidx.compose.ui.unit.Dp = 3.5.dp,
    thumbSize: androidx.compose.ui.unit.Dp = 6.5.dp
) {
    Slider(
        value = value,
        onValueChange = rememberSliderHaptics(value, valueRange, steps, onValueChange),
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        modifier = modifier,
        thumb = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                    .border(0.75.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(trackHeight),
                drawStopIndicator = null,
                drawTick = { _, _ -> },
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            )
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
    onVoiceSelected: (TtsVoiceOption) -> Unit,
    documentId: String? = null,
    onToggleDocumentMode: () -> Unit = {}
) {
    val context = LocalContext.current
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
    val heightDp = 72.dp + (160.dp * progress)

    val currentLocaleTag = voiceSettings.localeTag
    val availableVoices = remember(voices, currentLocaleTag) {
        if (currentLocaleTag.isBlank()) {
            voices.take(8)
        } else {
            voices.filter { it.localeTag.equals(currentLocaleTag, ignoreCase = true) }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val coverFile = remember(documentId) { CoverExtractor.coverFile(context, documentId.orEmpty()) }
    val coverBitmap = remember(coverFile) {
        if (coverFile != null && coverFile.exists()) {
            try {
                android.graphics.BitmapFactory.decodeFile(coverFile.absolutePath)
            } catch (_: Exception) {
                null
            }
        } else null
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .height(heightDp)
            .anchoredDraggable(
                state = draggableState,
                orientation = Orientation.Vertical
            )
            .onGloballyPositioned { OnboardingController.updateBounds("player_panel_header", it) },
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
        ),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row (Capsule Main Controls)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Book Cover / Veritas Mode Toggle & 1-tap Speed Cycler Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleDocumentMode,
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (coverBitmap != null) {
                            Image(
                                bitmap = coverBitmap.asImageBitmap(),
                                contentDescription = "Switch to Original Document",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            BrandMark(compact = true)
                        }
                    }

                    // Speed cycle chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable {
                                val nextRate = when {
                                    rate < 0.95f -> 1.0f
                                    rate < 1.20f -> 1.25f
                                    rate < 1.45f -> 1.5f
                                    rate < 1.95f -> 2.0f
                                    else -> 0.75f
                                }
                                onRateChange(nextRate)
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${"%.2f".format(rate).trimEnd('0').trimEnd('.')}x",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Center: Previous, Morphing Play/Pause, Next
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Previous",
                            tint = if (canGoPrevious) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Morphing high-contrast Play/Pause circle
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        enabled = canGoNext,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "Next",
                            tint = if (canGoNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Right: Audio Immersion Button & Expand Chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onOpenAudioMode,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Headphones,
                            contentDescription = "Audio Immersion Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                draggableState.animateTo(
                                    if (draggableState.currentValue == DragValue.Collapsed) DragValue.Expanded else DragValue.Collapsed
                                )
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (draggableState.currentValue == DragValue.Expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Expand Player Panel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expanded Area (Speed Presets, Pitch, Font size, Quick Voice Picker)
            if (progress > 0.05f) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer { alpha = progress }
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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

                    // Speed Section with 1-tap Preset Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Speed ${"%.2f".format(rate)}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        // 1-tap speed preset chips
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speedPreset ->
                                val isSelected = kotlin.math.abs(rate - speedPreset) < 0.04f
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .clickable { onRateChange(speedPreset) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${speedPreset}x",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    VeritasThinRoundSlider(
                        value = rate,
                        onValueChange = onRateChange,
                        valueRange = 0.5f..2.5f,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Pitch & Font Size Sliders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pitch ${"%.2f".format(pitch)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            VeritasThinRoundSlider(
                                value = pitch,
                                onValueChange = onPitchChange,
                                valueRange = 0.7f..1.4f
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Text size ${fontSizeSp}sp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            VeritasThinRoundSlider(
                                value = fontSizeSp.toFloat(),
                                onValueChange = { onFontSizeChange(it.toInt().coerceIn(14, 28)) },
                                valueRange = 14f..28f,
                                steps = 13
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
