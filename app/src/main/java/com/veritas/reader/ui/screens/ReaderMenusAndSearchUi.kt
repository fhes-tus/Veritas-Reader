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


@Composable
internal fun ReaderToolsMenu(
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
    onOpenRsvpSpeedReader: () -> Unit = {},
    onOpenDocumentDetails: () -> Unit = {}
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
            text = { Text("Document details") },
            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            onClick = { choose(onOpenDocumentDetails) })
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
internal fun FeatureMenuText(feature: ResolvedVeritasFeature, label: String) {
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



fun Map<VeritasFeatureId, ResolvedVeritasFeature>.requireResolvedFeature(
    id: VeritasFeatureId
): ResolvedVeritasFeature = getValue(id)



internal fun countSearchOccurrences(source: String, query: String): Int {
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
internal fun SelectedTextToolbar(
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
internal fun SearchPanel(
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



