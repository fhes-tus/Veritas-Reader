package com.veritas.reader.ui.screens


import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.outlined.Info
import com.veritas.reader.DocumentRepository
import com.veritas.reader.blendColors
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
import kotlinx.coroutines.flow.first
import androidx.compose.ui.layout.onGloballyPositioned
import com.veritas.reader.ui.OnboardingController
import com.veritas.reader.ui.rememberSliderHaptics
import com.veritas.reader.ui.VeritasSleekSlider
import com.veritas.reader.ui.VeritasThinRoundSlider
import com.veritas.reader.SlimPageSlider
import java.util.Locale
import kotlin.math.roundToInt



import com.veritas.reader.ReaderTextModel


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

internal fun buildReaderTextSelection(
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

internal fun buildReaderPartSpannable(
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
    bionicReading: Boolean = false,
    context: Context? = null,
    pageBitmaps: List<android.graphics.Bitmap> = emptyList(),
    sectionSpacingDp: Int = 10,
    searchQuery: String = ""
): Spannable {
    val spannable = SpannableString(part.text)
    // Render inline markdown (bold/italic/headings/etc.) and inline images that text carries,
    // so the reader shows formatting instead of literal ** and ## markers. Crucially the
    // delimiter characters are kept in the text and only drawn zero-width, so every
    // downstream character offset (TTS word highlight, selection→sentence mapping, search)
    // still lines up with part.text.
    applyMarkdownFormatting(spannable, part.text, context, pageBitmaps)

    // Apply custom paragraph spacing over double newlines
    val spacingScale = (sectionSpacingDp.toFloat() / 10f).coerceIn(0.5f, 2.5f)
    var pIdx = part.text.indexOf("\n\n")
    while (pIdx >= 0 && pIdx + 2 <= part.text.length) {
        spannable.setSpan(
            RelativeSizeSpan(spacingScale),
            pIdx + 1,
            pIdx + 2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        pIdx = part.text.indexOf("\n\n", pIdx + 2)
    }

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
            val sentenceTint = if (range.sentenceIndex == activeSearchMatchSentenceIndex) {
                (activeSearchMatchColor and 0x00FFFFFF) or (0x33 shl 24)
            } else {
                (searchMatchColor and 0x00FFFFFF) or (0x22 shl 24)
            }
            addBackground(range.start, range.endExclusive, sentenceTint)
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

    // Highlight the searched word a vivid, distinct color from the highlighted sentence
    if (searchQuery.isNotBlank() && searchMatches.isNotEmpty()) {
        val cleanQuery = searchQuery.trim()
        if (cleanQuery.length >= 2) {
            var matchIdx = part.text.indexOf(cleanQuery, 0, ignoreCase = true)
            while (matchIdx >= 0 && matchIdx + cleanQuery.length <= part.text.length) {
                val matchEnd = matchIdx + cleanQuery.length
                val matchedSentenceIndex = part.sentenceRanges.firstOrNull { matchIdx in it.start until it.endExclusive }?.sentenceIndex
                val isCurrentMatch = (matchedSentenceIndex == activeSearchMatchSentenceIndex)
                val wordBg = if (isCurrentMatch) 0xFFFF8F00.toInt() else 0xFFFFD54F.toInt()
                val wordFg = 0xFF000000.toInt()
                spannable.setSpan(BackgroundColorSpan(wordBg), matchIdx, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(wordFg), matchIdx, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(StyleSpan(Typeface.BOLD), matchIdx, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                matchIdx = part.text.indexOf(cleanQuery, matchEnd, ignoreCase = true)
            }
        }
    }

    return SafeSpannableString(spannable)
}

/**
 * Bionic reading formatter: bolds the first 2–3 letters of each word in the active reading area
 * to guide the eye's fixation points in sync with audio narration.
 */
internal fun applyBionicFormatting(
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
internal class HiddenMarkupSpan : ReplacementSpan() {
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

internal fun SpannableString.hideMarkup(start: Int, end: Int) {
    if (start in 0 until end && end <= length) {
        setSpan(HiddenMarkupSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

internal fun SpannableString.styleRange(span: Any, start: Int, end: Int) {
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
internal fun applyMarkdownFormatting(
    spannable: SpannableString,
    text: String,
    context: Context? = null,
    pageBitmaps: List<android.graphics.Bitmap> = emptyList()
) {
    if (text.isEmpty()) return
    var lineStart = 0
    while (lineStart <= text.length) {
        val newline = text.indexOf('\n', lineStart)
        val lineEnd = if (newline == -1) text.length else newline
        applyMarkdownLine(spannable, text, lineStart, lineEnd, context, pageBitmaps)
        if (newline == -1) break
        lineStart = newline + 1
    }
}

internal fun applyMarkdownLine(
    spannable: SpannableString,
    text: String,
    lineStart: Int,
    lineEnd: Int,
    context: Context? = null,
    pageBitmaps: List<android.graphics.Bitmap> = emptyList()
) {
    if (lineStart >= lineEnd) return
    val trimmedLine = text.substring(lineStart, lineEnd).trim()

    // Inline image marker: [[VERITAS_IMAGE:0]]
    if (trimmedLine.startsWith("[[VERITAS_IMAGE:") && trimmedLine.endsWith("]]")) {
        val idx = trimmedLine.removePrefix("[[VERITAS_IMAGE:").removeSuffix("]]").toIntOrNull()
        val bmp = idx?.let { pageBitmaps.getOrNull(it) }
        if (bmp != null && context != null) {
            val dm = context.resources.displayMetrics
            val maxWidth = (dm.widthPixels - (36 * dm.density)).toInt().coerceAtLeast(200)
            val maxHeight = (380 * dm.density).toInt().coerceAtLeast(200)

            // Only scale down if image exceeds maxWidth or maxHeight; do not stretch small images
            val widthScale = if (bmp.width > maxWidth) maxWidth.toFloat() / bmp.width else 1f
            val heightScale = if (bmp.height * widthScale > maxHeight) maxHeight.toFloat() / (bmp.height * widthScale) else 1f
            val finalScale = widthScale * heightScale

            val targetWidth = (bmp.width * finalScale).toInt().coerceIn(1, maxWidth)
            val targetHeight = (bmp.height * finalScale).toInt().coerceAtLeast(1)
            val drawable = android.graphics.drawable.BitmapDrawable(context.resources, bmp).apply {
                setBounds(0, 0, targetWidth, targetHeight)
            }
            spannable.setSpan(
                android.text.style.ImageSpan(drawable, android.text.style.ImageSpan.ALIGN_BOTTOM),
                lineStart,
                lineEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            spannable.hideMarkup(lineStart, lineEnd)
        }
        return
    }

    // Completely suppress any accidental internal page markers from displaying on screen
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

internal fun applyInlineMarkdown(spannable: SpannableString, text: String, start: Int, end: Int) {
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

internal fun clearNativeTextSelection(textView: TextView?) {
    val text = textView?.text
    if (text is android.text.Spannable) {
        android.text.Selection.removeSelection(text)
    }
    textView?.clearFocus()
}

internal fun readerSelectionActionModeCallback(
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
        }
    }
}

internal fun Menu.addReaderSelectionFeature(
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

/**
 * A resilient Spannable wrapper that prevents framework crashes on Samsung Android 14/15/16.
 * In Samsung's One UI framework (e.g. Editor$HandleView$4.onAnimationEnd), the OS calls
 * Selection.setSelection(text, -1, -1) to dismiss text selection handles.
 * Standard SpannableString throws IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0.
 * SafeSpannableString intercepts negative indices and safely clears the span instead of crashing.
 */
class SafeSpannableString(
    source: CharSequence,
    delegateOverride: Spannable? = null
) : Spannable {
    private val delegate: Spannable = delegateOverride ?: if (source is Spannable) source else SpannableString(source)

    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
        if (start < 0 || end < 0) {
            // Negative start/end is used by vendor Android frameworks (such as Samsung One UI
            // Editor$HandleView$4.onAnimationEnd) when dismissing text selection handles.
            // Rather than letting SpannableStringInternal throw IndexOutOfBoundsException,
            // we safely remove the handle span.
            if (what != null) {
                runCatching { delegate.removeSpan(what) }
            }
            return
        }
        val safeStart = start.coerceIn(0, delegate.length)
        val safeEnd = end.coerceIn(safeStart, delegate.length)
        runCatching { delegate.setSpan(what, safeStart, safeEnd, flags) }
    }

    override fun removeSpan(what: Any?) {
        runCatching { delegate.removeSpan(what) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getSpans(start: Int, end: Int, type: Class<T>?): Array<T> {
        val safeStart = start.coerceIn(0, delegate.length)
        val safeEnd = end.coerceIn(safeStart, delegate.length)
        return runCatching { delegate.getSpans(safeStart, safeEnd, type) }.getOrNull()
            ?: (java.lang.reflect.Array.newInstance(type ?: Any::class.java, 0) as Array<T>)
    }

    override fun getSpanStart(tag: Any?): Int = runCatching { delegate.getSpanStart(tag) }.getOrDefault(-1)
    override fun getSpanEnd(tag: Any?): Int = runCatching { delegate.getSpanEnd(tag) }.getOrDefault(-1)
    override fun getSpanFlags(tag: Any?): Int = runCatching { delegate.getSpanFlags(tag) }.getOrDefault(0)
    override fun nextSpanTransition(start: Int, limit: Int, type: Class<*>?): Int =
        runCatching { delegate.nextSpanTransition(start.coerceAtLeast(0), limit.coerceAtLeast(0), type) }.getOrDefault(limit)

    override val length: Int get() = delegate.length
    override fun get(index: Int): Char = delegate[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        delegate.subSequence(startIndex, endIndex)
    override fun toString(): String = delegate.toString()
}

/**
 * Stable delegate for TextView.customSelectionActionModeCallback.
 * By keeping the exact same callback instance set on the TextView Editor,
 * Android's Editor will NOT terminate active text selection (mTextActionMode.finish())
 * when Compose recomposes during selection state changes.
 */
internal class DelegatingActionModeCallback : ActionMode.Callback {
    var delegate: ActionMode.Callback? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean =
        delegate?.onCreateActionMode(mode, menu) ?: false

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean =
        delegate?.onPrepareActionMode(mode, menu) ?: false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean =
        delegate?.onActionItemClicked(mode, item) ?: false

    override fun onDestroyActionMode(mode: ActionMode) {
        delegate?.onDestroyActionMode(mode)
    }
}





