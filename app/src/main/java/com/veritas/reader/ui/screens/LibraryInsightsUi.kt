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
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Schedule
import kotlinx.coroutines.launch
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
import com.veritas.reader.ui.pressScale
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

@Composable
internal fun HomeSidebarDialog(
    name: String,
    snapshot: ReaderTrackerSnapshot,
    onDismiss: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenReadingLists: () -> Unit = {},
    onOpenReadingHistory: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.60f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )

            // Animated Drawer Sheet
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(220)
                ) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 320.dp, max = 340.dp),
                    shape = RoundedCornerShape(
                        topEnd = 32.dp,
                        bottomEnd = 32.dp,
                        topStart = 0.dp,
                        bottomStart = 0.dp
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Header with Brand Mark and Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BrandMark(compact = false)
                                Column {
                                    Text(
                                        "Veritas Reader",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val welcomeLabel = if (name.isNotBlank() && name != "Reader") name else "Personal Library"
                                    Text(
                                        welcomeLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // 2. Dynamic Reader Tier Status Badge
                        val streak = snapshot.currentStreak
                        val tierTitle = when {
                            streak >= 30 -> "🏆 Master Reader"
                            streak >= 14 -> "⚡ Habit Champion"
                            streak >= 7 -> "🔥 7-Day Flame"
                            streak >= 3 -> "✨ Consistency Pro"
                            else -> "🌱 Daily Habit"
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        tierTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    "${snapshot.currentStreak}d Streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // 3. Compact Reader Tracker Hero
                        ReaderTrackerSidebarCard(snapshot = snapshot, onOpenStats = onOpenStats)

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        // 4. Primary Navigation Items
                        Text(
                            "Navigation",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        SidebarAction(
                            title = "Your Library",
                            subtitle = "All saved readings, collections & formats",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            onClick = onOpenLibrary
                        )

                        SidebarAction(
                            title = "Reading Insights",
                            subtitle = "Daily streaks, analytics & activity heatmap",
                            icon = Icons.Filled.Insights,
                            badgeText = "${snapshot.currentStreak}d 🔥",
                            onClick = onOpenStats
                        )

                        SidebarAction(
                            title = "Reading Lists",
                            subtitle = "Curated collections and book queues",
                            icon = Icons.AutoMirrored.Filled.List,
                            onClick = onOpenReadingLists
                        )

                        SidebarAction(
                            title = "Reading History",
                            subtitle = "Recent sessions and timestamps",
                            icon = Icons.Outlined.History,
                            onClick = onOpenReadingHistory
                        )

                        SidebarAction(
                            title = "Settings Hub",
                            subtitle = "Display themes, voice studio, AI & accessibility",
                            icon = Icons.Filled.Settings,
                            onClick = onOpenSettings
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // 5. Local Privacy & Offline Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "100% On-Device Privacy • Veritas Reader",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ReaderTrackerSidebarCard(snapshot: ReaderTrackerSnapshot, onOpenStats: () -> Unit) {
    Card(
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        "Reading Rhythm",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Last 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = onOpenStats,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Insights", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            WeeklyReadingBarsPager(snapshot.weeklyHistory, barHeight = 50.dp)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactStat(
                    value = formatTrackerDuration(snapshot.weeklyUsageMillis),
                    label = "This Week",
                    modifier = Modifier.weight(1f)
                )
                CompactStat(
                    value = "${snapshot.documentsReadThisWeek}",
                    label = "Readings",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun ReadingStatsDashboardDialog(
    snapshot: ReaderTrackerSnapshot,
    documents: List<SavedDocument>,
    documentReadingTimes: Map<String, Long>,
    onDismiss: () -> Unit,
    readerSettings: ReaderSettings = ReaderSettings(),
    onGoalMinutesChange: (Int) -> Unit = {}
) {
    // Animation for stats count-up
    var targetCurrentStreak by remember { mutableStateOf(0) }
    var targetLongestStreak by remember { mutableStateOf(0) }
    var prevCurrentStreak by remember { mutableStateOf(0) }
    var prevLongestStreak by remember { mutableStateOf(0) }
    LaunchedEffect(snapshot) {
        delay(100)
        targetCurrentStreak = snapshot.currentStreak
        targetLongestStreak = snapshot.longestStreak
    }
    // Scale the roll-up duration with the delta (~70ms per step) so a +1 increment animates
    // quickly while the initial 0→N count-up still reads as a roll.
    val currentStreakDuration = (kotlin.math.abs(targetCurrentStreak - prevCurrentStreak) * 70).coerceIn(220, 650)
    val longestStreakDuration = (kotlin.math.abs(targetLongestStreak - prevLongestStreak) * 70).coerceIn(220, 650)
    val currentStreakAnimated by animateIntAsState(
        targetValue = targetCurrentStreak,
        animationSpec = tween(durationMillis = currentStreakDuration, easing = FastOutSlowInEasing)
    )
    val longestStreakAnimated by animateIntAsState(
        targetValue = targetLongestStreak,
        animationSpec = tween(durationMillis = longestStreakDuration, easing = FastOutSlowInEasing)
    )
    LaunchedEffect(targetCurrentStreak) { prevCurrentStreak = targetCurrentStreak }
    LaunchedEffect(targetLongestStreak) { prevLongestStreak = targetLongestStreak }

    // Pulsing/floating emoji transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val pastedColor = MaterialTheme.colorScheme.inversePrimary
    val slidesColor = blendColors(secondaryColor, errorColor, 0.5f)

    // Format Distribution slices — every document lands in exactly one bucket so the
    // chart total always matches the library size. E-Books (EPUB), Slide Decks (PPTX),
    // and Documents (Word/text/scanned) are split into their own slices.
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
                // sourceLabel is the explicit, reliable classifier set at import time
                // ("PDF", "DOCX", "PPTX", "EPUB", "OCR", "TXT", "Web", ...). Key off it
                // first, then fall back to mime type / filename so older records still bucket.
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
                description = "PDFs imported from local file storage or other directories. Excellent for study outlines."
            ),
            DonutSlice(
                label = "Web Articles",
                value = webCount.toFloat(),
                color = secondaryColor,
                description = "Online articles, blogs, and papers saved via URL import. Perfect for quick news reading."
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
                description = "PowerPoint presentations read slide by slide with titles, bullets, and speaker notes."
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
                description = "Text pasted directly into the reader interface or manually typed drafts."
            )
        ).filter { it.value > 0f }
    }

    // Time Allocation slices — real listening/reading time recorded per document for
    // the current month, so the chart resets monthly instead of stacking up forever.
    val timeSlices = remember(documents, documentReadingTimes, primaryColor, secondaryColor, tertiaryColor, errorColor) {
        val docTimes = documents.mapNotNull { doc ->
            val readingTime = documentReadingTimes[doc.id] ?: 0L
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
                        value = time.toFloat() / 60000f, // convert to minutes
                        color = colors[idx % colors.size],
                        description = "You spent ${time / 60000} minutes reading this document. That's ${(time * 100f / totalTime.coerceAtLeast(1)).toInt()}% of your total time."
                    )
                )
            }
            if (othersTime > 0L) {
                list.add(
                    DonutSlice(
                        label = "Others",
                        value = othersTime.toFloat() / 60000f,
                        color = colors[4],
                        description = "All other documents combined account for ${othersTime / 60000} minutes of your reading sessions."
                    )
                )
            }
            list
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Reading Insights",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Personal reading rhythm & library metrics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = onDismiss,
                            shape = VeritasPackStyle.chipShape()
                        ) {
                            Text("Done")
                        }
                    }
                }

                // Streaks & Stats Card (Modern card with gradient streak pills)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = VeritasPackStyle.cardShape(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Streaks display
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                // Current Streak Card
                                val streakMilestone = when {
                                    snapshot.currentStreak >= 30 -> "🏆 30-Day Master"
                                    snapshot.currentStreak >= 14 -> "⚡ 2-Week Pro"
                                    snapshot.currentStreak >= 7 -> "🔥 7-Day Flame"
                                    snapshot.currentStreak >= 3 -> "✨ 3-Day Habit"
                                    snapshot.currentStreak >= 1 -> "🌱 Started"
                                    else -> "Ready to start"
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = VeritasPackStyle.cardShape(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    ),
                                    border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalFireDepartment,
                                            contentDescription = "Current streak",
                                            tint = Color(0xFFFF7043),
                                            modifier = Modifier.size(32.dp).scale(emojiScale)
                                        )
                                        Text(
                                            text = "$currentStreakAnimated",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Current Streak",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = streakMilestone,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Longest Streak Card
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = VeritasPackStyle.cardShape(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                    ),
                                    border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiEvents,
                                            contentDescription = "Longest streak",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(32.dp).scale(emojiScale)
                                        )
                                        Text(
                                            text = "$longestStreakAnimated",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Best Streak",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Personal Record",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Weekly Usage Trend (swipe to see previous weeks)
                            Text(
                                "Weekly Reading Rhythm",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            WeeklyReadingBarsPager(snapshot.weeklyHistory, barHeight = 96.dp)

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                BigStat(animatedTrackerDuration(snapshot.weeklyUsageMillis), "This week", Modifier.weight(1f))
                                BigStat(animatedTrackerDuration(snapshot.weeklyAverageMillis), "Daily avg", Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Heatmap and Distribution Donut Charts
                item {
                    CalendarHeatMap(snapshot.activeDateKeys)
                }

                item {
                    InteractiveDonutChart(
                        title = "Library Source Distribution",
                        slices = formatSlices,
                        totalLabel = "Total docs",
                        titleIcon = Icons.Filled.PieChart
                    )
                }

                if (timeSlices.isNotEmpty()) {
                    item {
                        InteractiveDonutChart(
                            title = "Time Allocation — This Month",
                            slices = timeSlices,
                            totalLabel = "Total min",
                            titleIcon = Icons.Filled.Timer
                        )
                    }
                }

                item {
                    ReadingVelocityAndPaceCard(
                        weeklyMillis = snapshot.weeklyUsageMillis,
                        activeDocsCount = snapshot.documentsReadThisWeek,
                        streakDays = snapshot.currentStreak,
                        dailyGoalMinutes = readerSettings.dailyGoalMinutes,
                        onGoalMinutesChange = onGoalMinutesChange
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        BigStat("${snapshot.documentsReadThisWeek}", "Active docs this week", Modifier.weight(1f))
                        BigStat("${snapshot.documentsCompletedThisMonth}", "Finished this month", Modifier.weight(1f))
                    }
                }

                item {
                    Text(
                        "Recent completions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (snapshot.recentCompletions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = VeritasPackStyle.cardShape(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                        ) {
                            Text(
                                "Finish a book or document and it will be celebrated here.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(snapshot.recentCompletions, key = { it.documentId }) { completion ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = VeritasPackStyle.cardShape(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Completed",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        completion.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Completed ${formatUpdated(completion.completedAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Onboarding tour card: the dashboard dialog window covers the main spotlight
            // overlay, so the insights step renders its own card here.
            if (OnboardingController.activeStep == OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape = VeritasPackStyle.cardShape(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { OnboardingController.activeStep = OnboardingStep.INSIGHTS_SPOTLIGHT }) {
                                Text("Back")
                            }
                            Button(onClick = { OnboardingController.activeStep = OnboardingStep.NOTES_TAB_SPOTLIGHT }) {
                                Text("Next")
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
internal fun CalendarHeatMap(activeDateKeys: Set<String>) {
    val context = LocalContext.current
    val currentCal = remember { Calendar.getInstance() }
    val currentYear = currentCal.get(Calendar.YEAR)
    val currentMonth = currentCal.get(Calendar.MONTH) // 0..11

    var selectedMonthIndex by remember { mutableIntStateOf(currentMonth) }
    var isYearView by remember { mutableStateOf(false) }
    var selectedDayData by remember { mutableStateOf<DayData?>(null) }

    val monthNames = remember {
        listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }

    val months = remember {
        val list = mutableListOf<MonthData>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        for (m in 0..11) {
            cal.set(currentYear, m, 1)
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 7 = Sat
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            val days = mutableListOf<DayData?>()
            repeat(firstDayOfWeek - 1) {
                days.add(null)
            }
            for (d in 1..maxDays) {
                cal.set(currentYear, m, d)
                val dateStr = sdf.format(cal.time)
                days.add(DayData(dayOfMonth = d, dateKey = dateStr, timeMillis = cal.timeInMillis))
            }
            while (days.size % 7 != 0) {
                days.add(null)
            }
            list.add(MonthData(name = monthNames[m], days = days))
        }
        list
    }

    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Title & View Mode Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Reading Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Segmented Pill Toggle: Month vs Year
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        Surface(
                            onClick = { isYearView = false },
                            shape = RoundedCornerShape(16.dp),
                            color = if (!isYearView) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Text(
                                "Month",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!isYearView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            onClick = { isYearView = true },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isYearView) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Text(
                                "Year",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isYearView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (!isYearView) {
                // ==================== MONTH VIEW (DEFAULT) ====================
                val currentMonthData = months.getOrNull(selectedMonthIndex) ?: months[0]
                val validDays = currentMonthData.days.filterNotNull()
                val activeDaysInMonth = validDays.count { activeDateKeys.contains(it.dateKey) }
                val totalDaysInMonth = validDays.size
                val percentage = if (totalDaysInMonth > 0) (activeDaysInMonth * 100 / totalDaysInMonth) else 0

                // Month Navigator Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            selectedMonthIndex = (selectedMonthIndex - 1 + 12) % 12
                            selectedDayData = null
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${currentMonthData.name} $currentYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedMonthIndex != currentMonth) {
                            Text(
                                text = "Tap to jump to ${monthNames[currentMonth]}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    selectedMonthIndex = currentMonth
                                    selectedDayData = null
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            selectedMonthIndex = (selectedMonthIndex + 1) % 12
                            selectedDayData = null
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month")
                    }
                }

                // Month Summary Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (activeDaysInMonth > 0) "🔥 $activeDaysInMonth active days in ${currentMonthData.name}" else "No reading days yet in ${currentMonthData.name}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activeDaysInMonth > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$percentage% consistency",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Weekday Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Month Calendar Grid (Chunked Weeks)
                val chunkedWeeks = currentMonthData.days.chunked(7)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chunkedWeeks.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            week.forEach { day ->
                                if (day == null) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val isActive = activeDateKeys.contains(day.dateKey)
                                    val isToday = day.dateKey == todayKey
                                    val isFuture = day.dateKey > todayKey
                                    val isSelected = selectedDayData?.dateKey == day.dateKey

                                    val cellBg = when {
                                        isActive -> MaterialTheme.colorScheme.primary
                                        isFuture -> Color.Transparent
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
                                    }

                                    val textColor = when {
                                        isActive -> MaterialTheme.colorScheme.onPrimary
                                        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    val borderStroke = when {
                                        isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
                                        isToday -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                        isFuture -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                                        !isActive -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f))
                                        else -> null
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(cellBg)
                                            .then(
                                                if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(8.dp))
                                                else Modifier
                                            )
                                            .clickable {
                                                if (!isFuture) {
                                                    selectedDayData = day
                                                    val formattedDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date(day.timeMillis))
                                                    val msg = if (isActive) "Logged reading on $formattedDate! 📖" else "No activity logged on $formattedDate."
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${day.dayOfMonth}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = if (isActive || isToday || isSelected) FontWeight.Black else FontWeight.Normal
                                            ),
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Day Info Card
                val inspectedDay = selectedDayData ?: validDays.firstOrNull { it.dateKey == todayKey }
                if (inspectedDay != null && inspectedDay.dateKey.isNotBlank()) {
                    val isInspectedActive = activeDateKeys.contains(inspectedDay.dateKey)
                    val dateFormatted = remember(inspectedDay) {
                        SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US).format(Date(inspectedDay.timeMillis))
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isInspectedActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f),
                        border = BorderStroke(
                            1.dp,
                            if (isInspectedActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                if (isInspectedActive) "🔥" else "📅",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Column {
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isInspectedActive) "Active reading session logged" else "No reading activity recorded",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // ==================== YEAR OVERVIEW GRID ====================
                // Quick Month Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    monthNames.forEachIndexed { mIdx, name ->
                        val activeInMonth = months[mIdx].days.filterNotNull().count { activeDateKeys.contains(it.dateKey) }
                        Surface(
                            onClick = {
                                selectedMonthIndex = mIdx
                                isYearView = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedMonthIndex == mIdx) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    name.take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMonthIndex == mIdx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (activeInMonth > 0) {
                                    Text(
                                        "$activeInMonth",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                // Responsive 2-Column Year Matrix Grid
                val monthPairs = months.chunked(2)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    monthPairs.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { month ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedMonthIndex = monthNames.indexOf(month.name).coerceAtLeast(0)
                                            isYearView = false
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = month.name.take(3),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        val miniWeeks = month.days.chunked(7)
                                        miniWeeks.forEach { week ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                week.forEach { day ->
                                                    if (day == null) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    } else {
                                                        val isActive = activeDateKeys.contains(day.dateKey)
                                                        val isFuture = day.dateKey > todayKey
                                                        val miniBg = when {
                                                            isActive -> MaterialTheme.colorScheme.primary
                                                            isFuture -> Color.Transparent
                                                            else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .aspectRatio(1f)
                                                                .clip(RoundedCornerShape(3.dp))
                                                                .background(miniBg)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

internal data class MonthData(
    val name: String,
    val days: List<DayData?>
)

internal data class DayData(
    val dayOfMonth: Int,
    val dateKey: String,
    val timeMillis: Long
)

@Composable
internal fun InteractiveDonutChart(
    title: String,
    slices: List<DonutSlice>,
    totalLabel: String,
    modifier: Modifier = Modifier,
    titleIcon: ImageVector? = null
) {
    LocalContext.current
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val totalVal = slices.sumOf { it.value.toDouble() }.toFloat()

    var animationPlayed by remember { mutableStateOf(false) }
    val entryAnimFraction by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "donutChartEntryAnim"
    )
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val scaleFactors = slices.indices.map { idx ->
        animateFloatAsState(
            targetValue = if (selectedIndex == idx) 1.15f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "sliceScale_$idx"
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                titleIcon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val density = LocalDensity.current
                    val strokeWidthPx = with(density) { 14.dp.toPx() }
                    
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(slices, totalVal) {
                                detectTapGestures { offset ->
                                    if (totalVal > 0f) {
                                        val centerX = size.width / 2f
                                        val centerY = size.height / 2f
                                        val x = offset.x - centerX
                                        val y = offset.y - centerY
                                        val dist = Math.sqrt((x * x + y * y).toDouble()).toFloat()
                                        val radius = Math.min(size.width, size.height) / 2f
                                        
                                        if (dist in (radius - strokeWidthPx * 2.5f)..radius) {
                                            var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()
                                            angle = (angle + 90f + 360f) % 360f
                                            
                                            var currentAngle = 0f
                                            var found: Int? = null
                                            for (i in slices.indices) {
                                                val sweep = (slices[i].value / totalVal) * 360f
                                                if (angle in currentAngle..(currentAngle + sweep)) {
                                                    found = i
                                                    break
                                                }
                                                currentAngle += sweep
                                            }
                                            if (found != null) {
                                                selectedIndex = if (selectedIndex == found) null else found
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = Math.min(size.width, size.height) / 2f - strokeWidthPx / 2f
                        
                        if (totalVal == 0f) {
                            drawCircle(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                radius = radius,
                                center = Offset(centerX, centerY),
                                style = Stroke(width = strokeWidthPx)
                            )
                        } else {
                            var startAngle = -90f
                            slices.forEachIndexed { idx, slice ->
                                val sweepAngle = (slice.value / totalVal) * 360f * entryAnimFraction
                                val scale = scaleFactors[idx].value
                                val strokeWidth = strokeWidthPx * (if (selectedIndex == idx) 1.25f else 1.0f)
                                val r = radius * scale
                                
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = (sweepAngle - 2f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = Offset(centerX - r, centerY - r),
                                    size = Size(r * 2, r * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (totalVal >= 1000f) "%.1fk".format(totalVal / 1000f) else "${totalVal.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = totalLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    slices.forEachIndexed { idx, slice ->
                        val isSelected = selectedIndex == idx
                        val percentage = if (totalVal > 0f) (slice.value * 100f / totalVal).toInt() else 0
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else Color.Transparent
                                )
                                .clickable { selectedIndex = if (isSelected) null else idx }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 10.dp else 7.dp)
                                    .background(slice.color, CircleShape)
                            )
                            Text(
                                text = slice.label,
                                style = if (isSelected) MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = slice.color.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = slice.color,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            selectedIndex?.let { idx ->
                val slice = slices[idx]
                val percentage = if (totalVal > 0f) (slice.value * 100f / totalVal).toInt() else 0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = BorderStroke(1.dp, slice.color.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = slice.label,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = slice.color
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = slice.color.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${slice.value.toInt()} docs ($percentage%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = slice.color,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = slice.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = slice.color.copy(alpha = 0.15f),
                                modifier = Modifier.clickable { selectedIndex = null }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = slice.color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Active filter: ${slice.label.split(" ").firstOrNull() ?: slice.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = slice.color
                                    )
                                }
                            }
                            TextButton(
                                onClick = { selectedIndex = null },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    "Dismiss",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color,
    val description: String
)

@Composable
internal fun ReadingVelocityAndPaceCard(
    weeklyMillis: Long,
    activeDocsCount: Int,
    streakDays: Int,
    dailyGoalMinutes: Int = 0,
    onGoalMinutesChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isGoalEnabled = dailyGoalMinutes > 0
    val goalMillis = dailyGoalMinutes * 60 * 1000L
    val dailyAvgMillis = if (weeklyMillis > 0L) weeklyMillis / 7L else 0L
    val progress = if (goalMillis > 0L) (dailyAvgMillis.toFloat() / goalMillis).coerceIn(0f, 1f) else 0f
    val goalReached = isGoalEnabled && dailyAvgMillis >= goalMillis

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Reading Velocity & Daily Goal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        !isGoalEnabled -> MaterialTheme.colorScheme.surfaceContainerHigh
                        goalReached -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                ) {
                    Text(
                        text = when {
                            !isGoalEnabled -> "Target Off"
                            goalReached -> "🎉 Goal Met!"
                            else -> "${(progress * 100).toInt()}% Target"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalEnabled && goalReached) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Stats grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "~240",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Avg WPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTrackerDuration(dailyAvgMillis),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Daily Pace",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$activeDocsCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Active Docs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Daily Goal Target Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Reading Target",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isGoalEnabled) {
                        Text(
                            text = "${dailyGoalMinutes} min/day",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "Off", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "60m").forEach { (minutes, label) ->
                        val isSelected = dailyGoalMinutes == minutes
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onGoalMinutesChange(minutes) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 7.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Linear Progress Indicator to Target (when enabled)
            if (isGoalEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Current: ${formatTrackerDuration(dailyAvgMillis)}/day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Target: ${dailyGoalMinutes}m/day",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SidebarAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = VeritasPackStyle.cardShape(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun CompactStat(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(value, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun BigStat(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

// Monday-based index (Mon=0 … Sun=6) for today, used to emphasise today's capsule.
internal fun mondayBasedTodayIndex(): Int {
    val cal = Calendar.getInstance()
    return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
}

/**
 * Resolves a Long milliseconds value into a day-of-week label (Mon–Sun).
 * `weekStartMonday` is the epoch-ms of Monday 00:00 for the displayed week.
 */
internal fun dayLabel(dayIndex: Int, weekStartMonday: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = weekStartMonday + dayIndex * 86_400_000L
    return SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        .format(Date(cal.timeInMillis))
}

/**
 * Floating tooltip shown above the selected day capsule.
 * Uses an `AnimatedVisibility` (fade + slide) for smooth entrance/exit.
 */
@Composable
internal fun UsageTooltip(visible: Boolean, label: String, duration: String) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) +
                slideInVertically(tween(180)) { it / 2 },
        exit = fadeOut(tween(140)) +
               slideOutVertically(tween(140)) { it / 2 }
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
            tonalElevation = 6.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
internal fun MiniWeekBars(
    values: List<Long>,
    height: androidx.compose.ui.unit.Dp = 64.dp,
    todayIndex: Int = -1,
    selectedIndex: Int = -1,
    onSelectDay: (Int) -> Unit = {},
    weekStartMonday: Long = 0L
) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val max = (values.maxOrNull()?.coerceAtLeast(1L) ?: 1L) * 1.25f
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val primary = MaterialTheme.colorScheme.primary
    val fillBrush = Brush.verticalGradient(
        listOf(lerp(primary, Color.White, 0.45f), primary)
    )
    val dimBrush = Brush.verticalGradient(
        listOf(lerp(primary, Color.White, 0.25f).copy(alpha = 0.7f), primary.copy(alpha = 0.7f))
    )
    val selectedBrush = Brush.verticalGradient(
        listOf(lerp(primary, Color.White, 0.6f), primary)
    )
    val areaAlpha = if (MaterialTheme.colorScheme.primary.luminance() > 0.5f) 0.12f else 0.18f
    Column(modifier = Modifier.fillMaxWidth()) {
        val fracs = values.map { v ->
            if (v > 0L) (v.toFloat() / max).coerceIn(0.14f, 1f) else 0f
        }
        val animatedFracs = fracs.mapIndexed { i, f ->
            animateFloatAsState(f, tween(550, easing = FastOutSlowInEasing), label = "areaFrac$i").value
        }
        Box(modifier = Modifier.fillMaxWidth().height(height)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (animatedFracs.size == 7 && size.width > 0f) {
                    val barWidth = size.width / 7f
                    val path = Path()
                    val points = animatedFracs.mapIndexed { i, frac ->
                        Offset(barWidth * i + barWidth / 2f, size.height * (1f - frac))
                    }
                    if (points.isNotEmpty()) {
                        path.moveTo(0f, size.height)
                        path.lineTo(0f, points.first().y)
                        path.lineTo(points.first().x, points.first().y)
                        for (k in 1 until points.size) {
                            val prev = points[k - 1]
                            val curr = points[k]
                            val cx = (prev.x + curr.x) / 2f
                            path.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                        }
                        path.lineTo(size.width, points.last().y)
                        path.lineTo(size.width, size.height)
                        path.close()
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(primary.copy(alpha = areaAlpha), Color.Transparent),
                                startY = 0f,
                                endY = size.height
                            )
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEachIndexed { i, value ->
                    val isToday = i == todayIndex
                    val isSelected = i == selectedIndex
                    val targetFrac = if (value > 0L) (value.toFloat() / max).coerceIn(0.14f, 1f) else 0f
                    val frac by animateFloatAsState(
                        targetValue = targetFrac,
                        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
                        label = "barFrac$i"
                    )
                    val dayDesc = if (weekStartMonday > 0L && value > 0L)
                        "${labels.getOrElse(i) { "" }}: ${formatTrackerDuration(value)}"
                    else labels.getOrElse(i) { "" }
                    val barInteraction = remember { MutableInteractionSource() }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = dayDesc },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 18.dp else 14.dp)
                                .height(height)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else trackColor
                                )
                                .pressScale(barInteraction)
                                .clickable(
                                    enabled = todayIndex == -1 || i <= todayIndex,
                                    indication = null,
                                    interactionSource = barInteraction
                                ) { onSelectDay(if (isSelected) -1 else i) },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (frac > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(frac)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            when {
                                                isSelected -> selectedBrush
                                                isToday -> fillBrush
                                                else -> dimBrush
                                            }
                                        )
                                )
                            }
                        }
                        Text(
                            labels.getOrElse(i) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Swipeable weekly chart: pages through `history` (oldest→newest), defaulting to the current
 * (last) week, with a small caption naming the week and its total. Falls back to a single
 * static week when no history is available. Tapping a bar shows a floating usage tooltip.
 */
@Composable
internal fun WeeklyReadingBarsPager(
    history: List<WeekBars>,
    modifier: Modifier = Modifier,
    barHeight: androidx.compose.ui.unit.Dp = 64.dp
) {
    if (history.isEmpty()) {
        MiniWeekBars(List(7) { 0L }, height = barHeight)
        return
    }
    val pagerState = rememberPagerState(initialPage = history.lastIndex) { history.size }
    val todayIndex = remember { mondayBasedTodayIndex() }
    var selectedBarIndex by remember { mutableIntStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) { selectedBarIndex = -1 }

    val visibleWeek = history[pagerState.currentPage]
    val chartDescription = "Weekly reading chart. ${visibleWeek.label}: " +
        "${formatTrackerDuration(visibleWeek.totalMillis)} total. Swipe left or right to change week."

    val weekStartMonday = remember(pagerState.currentPage) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weeksBack = history.lastIndex - pagerState.currentPage
        cal.add(Calendar.WEEK_OF_YEAR, -weeksBack)
        cal.timeInMillis
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = chartDescription }
    ) {
        val width = maxWidth
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tooltipVisible = selectedBarIndex >= 0 && selectedBarIndex < visibleWeek.values.size
            val tooltipLabel = if (tooltipVisible && weekStartMonday > 0L)
                dayLabel(selectedBarIndex, weekStartMonday) else ""
            val tooltipDuration = if (tooltipVisible)
                formatTrackerDuration(visibleWeek.values.getOrElse(selectedBarIndex) { 0L }) else ""

            // Week Selector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }
                        },
                        enabled = pagerState.currentPage > 0,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous week",
                            tint = if (pagerState.currentPage > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (visibleWeek.isCurrentWeek) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = visibleWeek.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (visibleWeek.isCurrentWeek) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage < history.lastIndex) {
                                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                        },
                        enabled = pagerState.currentPage < history.lastIndex,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next week",
                            tint = if (pagerState.currentPage < history.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "Total: ${formatTrackerDuration(visibleWeek.totalMillis)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                HorizontalPager(state = pagerState) { page ->
                    val week = history[page]
                    MiniWeekBars(
                        values = week.values,
                        height = barHeight,
                        todayIndex = if (week.isCurrentWeek) todayIndex else -1,
                        selectedIndex = if (page == pagerState.currentPage) selectedBarIndex else -1,
                        onSelectDay = { idx -> selectedBarIndex = idx },
                        weekStartMonday = weekStartMonday
                    )
                }
            }

            if (tooltipVisible) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$tooltipLabel: $tooltipDuration",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable { selectedBarIndex = -1 }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

internal fun shareGeneralNote(context: Context, note: GeneralNote) {
    val body = if (note.isChecklist) {
        note.content.lineSequence().joinToString("\n") { line ->
            when {
                line.startsWith("[x]") -> "☑ " + line.removePrefix("[x]").trim()
                line.startsWith("[ ]") -> "☐ " + line.removePrefix("[ ]").trim()
                else -> line
            }
        }
    } else {
        RichTextFormatter.stripMarkup(note.content)
    }
    val plain = buildString {
        if (note.title.isNotBlank()) {
            append(note.title)
            append("\n\n")
        }
        append(body)
    }.trim()
    if (plain.isBlank()) {
        Toast.makeText(context, "Nothing to share yet", Toast.LENGTH_SHORT).show()
        return
    }
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, note.title.ifBlank { "Veritas note" })
        putExtra(android.content.Intent.EXTRA_TEXT, plain)
    }
    runCatching { context.startActivity(android.content.Intent.createChooser(send, "Share note")) }
}

// Count-up variant of formatTrackerDuration: rolls the minute total up to its
// target on first show / change, so the big stats animate like the streak counter.
@Composable
internal fun animatedTrackerDuration(millis: Long): String {
    val goal = (millis / 60_000L).coerceAtLeast(0L).toInt()
    var target by remember { mutableIntStateOf(0) }
    LaunchedEffect(goal) { target = goal }
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "trackerDurationCountUp"
    )
    return formatTrackerDuration(animated.toLong() * 60_000L)
}

internal fun formatTrackerDuration(millis: Long): String {
    val minutes = (millis / 60_000L).coerceAtLeast(0L)
    val hours = minutes / 60L
    val remaining = minutes % 60L
    return when {
        hours > 0L && remaining > 0L -> "${hours}h ${remaining}m"
        hours > 0L -> "${hours}h"
        else -> "${remaining}m"
    }
}

