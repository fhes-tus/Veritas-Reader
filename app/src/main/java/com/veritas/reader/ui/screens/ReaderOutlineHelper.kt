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


internal data class SmartOutlineEntry(
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
internal fun findContentsRange(chunks: List<String>): IntRange? {
    val startIndex = chunks.take(MAX_SMART_OUTLINE_TOC_SCAN).indexOfFirst { chunk ->
        val head = chunk.take(200).lowercase()
        head.contains("table of contents") ||
            head.contains("brief contents") ||
            head.contains("summary of contents") ||
            head.contains("index of chapters") ||
            chunk.lineSequence().any { line ->
                val trimmed = line.trim().lowercase()
                trimmed == "contents" || trimmed == "table of contents" ||
                    trimmed == "brief contents" || trimmed == "summary of contents"
            }
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
            Regex("[.\\s]{3,}$").containsMatchIn(text) ||
            Regex("(?:\\.\\s*){2,}\\s*\\d{1,4}").containsMatchIn(text)
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
internal fun SmartOutlineDialog(
    document: ReaderDocument,
    documentOutline: List<VeritasDocumentOutlineEntry>,
    currentIndex: Int,
    onJumpToSection: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember(document.id) { mutableStateOf("") }
    val entries = remember(document.id, document.chunks, documentOutline) {
        if (documentOutline.isNotEmpty()) {
            documentOutline.mapNotNull { outline ->
                val title = cleanTocTitle(outline.title)
                if (title.isBlank() || title.all { it == '.' || it.isWhitespace() || it == '•' || it == '·' }) return@mapNotNull null
                val preview = document.chunks.getOrNull(outline.targetIndex).orEmpty()
                    .replace(Regex("\\s+"), " ").trim()
                SmartOutlineEntry(
                    index = outline.targetIndex,
                    title = title,
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

internal fun buildSmartOutline(chunks: List<String>): List<SmartOutlineEntry> {
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
internal fun dropRunningHeaders(entries: List<SmartOutlineEntry>): List<SmartOutlineEntry> {
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
internal fun extractTableOfContentsOutline(chunks: List<String>): List<SmartOutlineEntry> {
    val tocLine = Regex("^(.{3,140}?)[\\s.]*?(?:\\.{2,}|\\s{3,}|\\t+)[\\s.]*(\\d{1,4})$")
    val contentsIndexes = mutableListOf<Int>()
    chunks.take(MAX_SMART_OUTLINE_TOC_SCAN).forEachIndexed { index, chunk ->
        val head = chunk.take(400).lowercase()
        val isContents = head.contains("table of contents") ||
            head.contains("brief contents") ||
            head.contains("summary of contents") ||
            head.contains("index of chapters") ||
            chunk.lineSequence().any { line ->
                val trimmed = line.trim().lowercase()
                trimmed == "contents" || trimmed == "table of contents" ||
                    trimmed == "brief contents" || trimmed == "summary of contents"
            }
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
internal fun cleanTocTitle(raw: String): String {
    var title = raw.trim().trim('.', '-', '\u2022', '\u00b7', ' ')
    // Strip trailing leader dots/dashes/bullets followed by trailing page number:
    // e.g. "Chapter 1 .......... 15", "Chapter 1 . . . . . . 15", "Chapter 1    15"
    title = title.replace(Regex("""(?:\s*[\.\-_·•…](?:\s*[\.\-_·•…])+|\s{2,})\s*\d{1,5}$"""), "").trim()
    // Strip trailing runs of leader dots, dashes, or ellipses without page numbers:
    // e.g. "Chapter 1 ..........", "Chapter 1 . . . . . ."
    title = title.replace(Regex("""(?:[\.\-_·•…]\s*){2,}$"""), "").trim()
    title = title.trim('.', '-', '\u2022', '\u00b7', ' ')

    // Strip leading printed page numbers (e.g. "12 Introduction") while preserving real numbered headings
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
internal fun joinSplitHeading(chunks: List<String>, index: Int): String? {
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
internal fun extractHeadingOutline(
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
internal fun looksLikeTableOfContentsRow(chunk: String): Boolean {
    val line = chunk.lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() }
        .orEmpty()
    if (line.isBlank()) return false
    if (Regex("^[.\\s\\u00b7\\u2022]+$").matches(line)) return true
    return Regex("(?:\\.\\s*){2,}\\s*\\d{1,4}\\s*$").containsMatchIn(line) ||
        Regex("\\s{3,}\\d{1,4}\\s*$").containsMatchIn(line) ||
        Regex("(?:\\.\\s*){3,}").containsMatchIn(line)
}

/**
 * Finds where a contents title actually appears in the body.
 *
 * [from] skips the contents region so a title cannot resolve to its own listing, and
 * the scan runs to the end of the document rather than stopping at a fixed window —
 * chapter headings in a long book sit far past any leading cap.
 */
internal fun locateOutlineTarget(chunks: List<String>, title: String, from: Int = 0): Int? {
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
internal fun normalizeOutlineNeedle(value: String): String {
    return value
        .replace(Regex("^\\d+(\\.\\d+)*\\s+"), "")
        .replace(Regex("[^A-Za-z0-9 ]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase(Locale.getDefault())
}

internal fun outlineTitle(source: String, index: Int): String {
    val clean = source.replace(Regex("\\s+"), " ").trim()
    if (clean.isBlank()) return "Sentence ${index + 1}"

    val sentenceEnd = clean.indexOfAny(charArrayOf('.', '!', '?'))
    val title = if (sentenceEnd in 20..120) clean.take(sentenceEnd + 1) else clean.take(96)
    return title.trim().ifBlank { "Sentence ${index + 1}" }
}

internal fun looksLikeOutlineHeading(firstLine: String): Boolean {
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




