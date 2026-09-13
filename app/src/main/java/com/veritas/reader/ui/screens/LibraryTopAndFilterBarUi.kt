package com.veritas.reader.ui.screens

import androidx.compose.ui.tooling.preview.Preview
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


@Composable
internal fun LibraryTopAndFilterBar(
    activeNavTab: VeritasHomeTab,
    documents: List<SavedDocument>,
    queuedDocuments: List<SavedDocument>,
    uiState: ReaderUiState,
    currentStreak: Int,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    sourceFilter: String,
    onSourceFilterChange: (String) -> Unit,
    collectionFilter: String,
    onCollectionFilterChange: (String) -> Unit,
    readingListFilter: String,
    onReadingListFilterChange: (String) -> Unit,
    selectedGeneralNoteTag: String,
    onSelectedGeneralNoteTagChange: (String) -> Unit,
    onOpenHomeSidebar: () -> Unit,
    onOpenSettingsHub: () -> Unit,
    modifier: Modifier = Modifier
) {
                val scheme = MaterialTheme.colorScheme
                val isDark = scheme.surface.luminance() < 0.5f
                val topBarColor = if (isDark) scheme.surface else scheme.primaryContainer
                val topBarContentColor = if (isDark) scheme.onSurface else scheme.onPrimaryContainer
                Surface(
                    color = topBarColor,
                    contentColor = topBarContentColor,
                    tonalElevation = if (isDark) 0.dp else 3.dp,
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
                            when (activeNavTab) {
                                VeritasHomeTab.HOME -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            TextButton(
                                                onClick = { onOpenHomeSidebar() },
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
                                            VeritasWordmark()
                                        }
                                        IconButton(
                                            onClick = onOpenSettingsHub,
                                            modifier = Modifier.onGloballyPositioned { OnboardingController.updateBounds("settings_trigger", it) }
                                        ) {
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
                                        IconButton(
                                            onClick = onOpenSettingsHub,
                                            modifier = Modifier.onGloballyPositioned { OnboardingController.updateBounds("settings_trigger", it) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Dynamic filters based on actual available document states and collections
                                        val hasFavorites = remember(documents) { documents.any { it.favorite } }
                                        val hasUnread = remember(documents) { documents.any { it.currentIndex <= 0 } }
                                        val hasInProgress = remember(documents) { documents.any { it.chunkCount > 1 && it.currentIndex in 1 until it.chunkCount - 1 } }
                                        val hasCompleted = remember(documents) { documents.any { it.chunkCount > 0 && it.currentIndex >= it.chunkCount - 1 } }

                                        val inProgressCount = remember(documents) { documents.count { it.chunkCount > 1 && it.currentIndex in 1 until it.chunkCount - 1 } }
                                        val unreadCount = remember(documents) { documents.count { it.currentIndex <= 0 } }
                                        val completedCount = remember(documents) { documents.count { it.chunkCount > 0 && it.currentIndex >= it.chunkCount - 1 } }
                                        val favoritesCount = remember(documents) { documents.count { it.favorite } }

                                        val collections = remember(documents) {
                                            documents.map { it.collection.trim() }
                                                .filter { it.isNotEmpty() }
                                                .distinct()
                                                .sorted()
                                        }
                                        val readingLists = remember(uiState.readingListCatalog, documents) {
                                            uiState.readingListCatalog.lists
                                                .filterNot { it.archived }
                                                .filter { list -> documents.any { doc -> list.contains(doc.id) } || readingListFilter == list.id }
                                                .sortedBy { it.title.lowercase(Locale.getDefault()) }
                                        }
                                        val formats = remember(documents) {
                                            documents.map { it.sourceLabel.trim() }.filter { it.isNotBlank() }.distinct().sorted()
                                        }

                                        data class LibraryChip(
                                            val label: String,
                                            val active: Boolean,
                                            val apply: () -> Unit
                                        )

                                        fun reset() {
                                            onStatusFilterChange("All")
                                            onSourceFilterChange("All")
                                            onCollectionFilterChange("All")
                                            onReadingListFilterChange("All")
                                        }

                                        val chips = buildList {
                                            add(LibraryChip(
                                                "All (${documents.size})",
                                                statusFilter == "All" && sourceFilter == "All" &&
                                                    collectionFilter == "All" && readingListFilter == "All"
                                            ) { reset() })

                                            if (hasInProgress || statusFilter == "In progress") {
                                                add(LibraryChip("In progress ($inProgressCount)", statusFilter == "In progress") {
                                                    if (statusFilter == "In progress") reset() else { reset(); onStatusFilterChange("In progress") }
                                                })
                                            }

                                            if (hasUnread || statusFilter == "Unread") {
                                                add(LibraryChip("Unread ($unreadCount)", statusFilter == "Unread") {
                                                    if (statusFilter == "Unread") reset() else { reset(); onStatusFilterChange("Unread") }
                                                })
                                            }

                                            if (hasCompleted || statusFilter == "Completed") {
                                                add(LibraryChip("Completed ($completedCount)", statusFilter == "Completed") {
                                                    if (statusFilter == "Completed") reset() else { reset(); onStatusFilterChange("Completed") }
                                                })
                                            }

                                            if (hasFavorites || statusFilter == "Favorites") {
                                                add(LibraryChip("Favorites ($favoritesCount)", statusFilter == "Favorites") {
                                                    if (statusFilter == "Favorites") reset() else { reset(); onStatusFilterChange("Favorites") }
                                                })
                                            }

                                            if (queuedDocuments.isNotEmpty()) {
                                                add(LibraryChip("Queue ${queuedDocuments.size}", statusFilter == "Queued") {
                                                    reset(); onStatusFilterChange("Queued")
                                                })
                                            }

                                            collections.forEach { name ->
                                                val count = documents.count { it.collection.trim() == name }
                                                add(LibraryChip("$name ($count)", collectionFilter == name) {
                                                    if (collectionFilter == name) reset() else { reset(); onCollectionFilterChange(name) }
                                                })
                                            }

                                            readingLists.forEach { readingList ->
                                                val count = documents.count { readingList.contains(it.id) }
                                                add(LibraryChip("≡ ${readingList.title} ($count)", readingListFilter == readingList.id) {
                                                    if (readingListFilter == readingList.id) reset() else { reset(); onReadingListFilterChange(readingList.id) }
                                                })
                                            }

                                            formats.forEach { fmt ->
                                                val count = documents.count { it.sourceLabel.trim().equals(fmt, ignoreCase = true) }
                                                add(LibraryChip("$fmt ($count)", sourceFilter == fmt) {
                                                    if (sourceFilter == fmt) reset() else { reset(); onSourceFilterChange(fmt) }
                                                })
                                            }
                                        }

                                        chips.forEach { chip ->
                                            if (chip.active) {
                                                Button(
                                                    onClick = chip.apply,
                                                    shape = VeritasPackStyle.chipShape(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                                ) {
                                                    Text(chip.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                OutlinedButton(
                                                    onClick = chip.apply,
                                                    shape = VeritasPackStyle.chipShape(),
                                                    border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                                ) {
                                                    Text(chip.label, style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                                VeritasHomeTab.NOTES -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Notes & Annotations",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            val totalNotesCount = remember(uiState.generalNotes) { uiState.generalNotes.size }
                                            Text(
                                                "$totalNotesCount note${if (totalNotesCount == 1) "" else "s"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                        IconButton(onClick = onOpenSettingsHub) {
                                            Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val noteFilterOptions = remember(uiState.generalNotes, uiState.allAnnotations) {
                                            buildList {
                                                add("All Notes")
                                                if (uiState.generalNotes.any { it.pinned }) add("Pinned")
                                                if (uiState.generalNotes.any { it.allAudioUrls.isNotEmpty() }) add("Voice Memos")
                                                if (uiState.generalNotes.any { it.isChecklist }) add("Checklists")
                                                if (uiState.generalNotes.any { it.reminderAt != null }) add("Reminders")
                                                if (uiState.allAnnotations.isNotEmpty()) add("Highlights")
                                            }
                                        }
                                        noteFilterOptions.forEach { option ->
                                            val active = when (option) {
                                                "All Notes" -> selectedGeneralNoteTag == "All"
                                                "Pinned" -> selectedGeneralNoteTag == "Pinned"
                                                "Voice Memos" -> selectedGeneralNoteTag == "Audio"
                                                "Checklists" -> selectedGeneralNoteTag == "Checklists"
                                                "Reminders" -> selectedGeneralNoteTag == "Reminders"
                                                "Highlights" -> selectedGeneralNoteTag == "Highlights"
                                                else -> selectedGeneralNoteTag == option
                                            }
                                            if (active) {
                                                Button(
                                                    onClick = {
                                                        onSelectedGeneralNoteTagChange(when (option) {
                                                            "All Notes" -> "All"
                                                            "Pinned" -> "Pinned"
                                                            "Voice Memos" -> "Audio"
                                                            "Checklists" -> "Checklists"
                                                            "Reminders" -> "Reminders"
                                                            "Highlights" -> "Highlights"
                                                            else -> option
                                                        })
                                                    },
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
                                                    onClick = {
                                                        onSelectedGeneralNoteTagChange(when (option) {
                                                            "All Notes" -> "All"
                                                            "Pinned" -> "Pinned"
                                                            "Voice Memos" -> "Audio"
                                                            "Checklists" -> "Checklists"
                                                            "Reminders" -> "Reminders"
                                                            "Highlights" -> "Highlights"
                                                            else -> option
                                                        })
                                                    },
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
                                VeritasHomeTab.STUDY -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Study Hub",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text("🔥", fontSize = 14.sp)
                                                val streak = uiState.readerTrackerSnapshot.currentStreak
                                                Text(
                                                    text = if (streak > 0) "$streak-Day Streak" else "0-Day Streak",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text("🔥", fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

}

@Preview(showBackground = true)
@Composable
internal fun LibraryTopAndFilterBarPreview() {
    MaterialTheme {
        LibraryTopAndFilterBar(
            activeNavTab = VeritasHomeTab.HOME,
            documents = emptyList(),
            queuedDocuments = emptyList(),
            uiState = ReaderUiState(),
            currentStreak = 3,
            statusFilter = "All",
            onStatusFilterChange = {},
            sourceFilter = "All",
            onSourceFilterChange = {},
            collectionFilter = "All",
            onCollectionFilterChange = {},
            readingListFilter = "All",
            onReadingListFilterChange = {},
            selectedGeneralNoteTag = "All",
            onSelectedGeneralNoteTagChange = {},
            onOpenHomeSidebar = {},
            onOpenSettingsHub = {}
        )
    }
}
