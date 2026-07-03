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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

@Composable
internal fun HomeSidebarDialog(
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
                    .width(318.dp),
                shape = RoundedCornerShape(
                    topEnd = 24.dp,
                    bottomEnd = 24.dp,
                    topStart = 0.dp,
                    bottomStart = 0.dp
                ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(compact = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Veritas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                    ReaderTrackerSidebarCard(snapshot = snapshot, onOpenStats = onOpenStats)
                    HorizontalDivider()
                    SidebarAction("Library", "Saved readings and filters", Icons.AutoMirrored.Filled.LibraryBooks, onOpenLibrary)
                    SidebarAction("Settings", "Reader, voice, import, AI, and backup", Icons.Filled.Settings, onOpenSettings)
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
internal fun ReaderTrackerSidebarCard(snapshot: ReaderTrackerSnapshot, onOpenStats: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
            Text("Reading time", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            WeeklyReadingBarsPager(snapshot.weeklyHistory, barHeight = 56.dp)
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
internal fun ReadingStatsDashboardDialog(
    snapshot: ReaderTrackerSnapshot,
    documents: List<SavedDocument>,
    documentReadingTimes: Map<String, Long>,
    onDismiss: () -> Unit
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

    // Format Distribution slices — every document lands in exactly one bucket so the
    // chart total always matches the library size. E-Books (EPUB) and Documents (Word/text/
    // scanned) are split into their own slices rather than one combined bucket.
    val formatSlices = remember(documents, primaryColor, secondaryColor, tertiaryColor, errorColor, pastedColor) {
        var pdfCount = 0
        var webCount = 0
        var ebookCount = 0
        var docCount = 0
        var pastedCount = 0
        documents.forEach { doc ->
            val mime = doc.originalMimeType.lowercase(Locale.US)
            val title = doc.title.lowercase(Locale.US)
            val label = doc.sourceLabel.lowercase(Locale.US)
            when {
                // sourceLabel is the explicit, reliable classifier set at import time
                // ("PDF", "DOCX", "EPUB", "OCR", "TXT", "Web", ...). Key off it first,
                // then fall back to mime type / filename so older records still bucket.
                label == "pdf" || mime.contains("pdf") || title.endsWith(".pdf") -> pdfCount++
                label.contains("web") || label.contains("http") || label.contains("article") ||
                    mime.contains("html") -> webCount++
                label == "epub" || mime.contains("epub") || title.endsWith(".epub") -> ebookCount++
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
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reading Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            Text("Your local reading rhythm", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onDismiss) { Text("Close") }
                    }
                }

                // Streaks & Stats Card (Glassmorphic design with gradient background)
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Streaks display
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                // Current Streak Card
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalFireDepartment,
                                            contentDescription = "Current streak",
                                            tint = Color(0xFFFF7043),
                                            modifier = Modifier.size(30.dp).scale(emojiScale)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$currentStreakAnimated",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Current Streak",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Longest Streak Card
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiEvents,
                                            contentDescription = "Longest streak",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(30.dp).scale(emojiScale)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$longestStreakAnimated",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Longest Streak",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Weekly Usage Trend (swipe to see previous weeks)
                            Text("Weekly Reading Time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                        totalLabel = "Readings",
                        titleIcon = Icons.Filled.PieChart
                    )
                }

                if (timeSlices.isNotEmpty()) {
                    item {
                        InteractiveDonutChart(
                            title = "Time Allocation — This Month",
                            slices = timeSlices,
                            totalLabel = "Minutes",
                            titleIcon = Icons.Filled.Timer
                        )
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
                        Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(32.dp).size(22.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(completion.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                    Text(formatUpdated(completion.completedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                            Button(onClick = { OnboardingController.activeStep = OnboardingStep.DOCUMENT_SPOTLIGHT }) {
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
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    
    val months = remember {
        val list = mutableListOf<MonthData>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        
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

    var visibleMonths by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..12) {
            delay(30)
            visibleMonths = i
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Reading Heatmap — Year $currentYear",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            months.forEachIndexed { mIdx, month ->
                val alpha by animateFloatAsState(
                    targetValue = if (visibleMonths >= mIdx + 1) 1f else 0f,
                    animationSpec = tween(durationMillis = 400),
                    label = "monthAlpha"
                )

                if (visibleMonths >= mIdx + 1) {
                    // Sketch layout: month name on the left vertically centered against the
                    // grid; weekday letters run across the top of the day cells. The whole
                    // month block is centered in the card.
                    val cellSize = 26.dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { this.alpha = alpha },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = month.name.take(3),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
                                weekdays.forEach { day ->
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.width(cellSize),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            val chunkedWeeks = month.days.chunked(7)
                            chunkedWeeks.forEach { week ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    week.forEach { day ->
                                        if (day == null) {
                                            Box(modifier = Modifier.size(cellSize))
                                        } else {
                                            val isActive = activeDateKeys.contains(day.dateKey)
                                            val isFuture = day.dateKey > todayKey
                                            val color = when {
                                                isActive -> MaterialTheme.colorScheme.primary
                                                isFuture -> Color.Transparent
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            }
                                            val borderStroke = if (isFuture) {
                                                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                                            } else if (!isActive) {
                                                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                                            } else null

                                            Box(
                                                modifier = Modifier
                                                    .size(cellSize)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(color)
                                                    .then(
                                                        if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(4.dp))
                                                        else Modifier
                                                    )
                                                    .clickable {
                                                        if (!isFuture) {
                                                            val formattedDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date(day.timeMillis))
                                                            val msg = if (isActive) "Logged reading on $formattedDate! 📖" else "No activity logged on $formattedDate."
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                }
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
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
                    modifier = Modifier.size(18.dp)
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
                            val strokeWidth = strokeWidthPx * (if (selectedIndex == idx) 1.3f else 1.0f)
                            val r = radius * scale
                            
                            drawArc(
                                color = slice.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(centerX - r, centerY - r),
                                size = Size(r * 2, r * 2),
                                style = Stroke(width = strokeWidth)
                            )
                            startAngle += sweepAngle
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (totalVal >= 1000f) "%.1fk".format(totalVal / 1000f) else "${totalVal.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = totalLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

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
                            .clickable { selectedIndex = if (isSelected) null else idx }
                            .padding(vertical = 2.dp),
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
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        selectedIndex?.let { idx ->
            val slice = slices[idx]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, slice.color.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = slice.color
                    )
                    Text(
                        text = slice.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
internal fun SidebarAction(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun CompactStat(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun BigStat(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
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
                                .clickable(
                                    enabled = todayIndex == -1 || i <= todayIndex,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tooltipVisible = selectedBarIndex >= 0 && selectedBarIndex < visibleWeek.values.size
            val tooltipLabel = if (tooltipVisible && weekStartMonday > 0L)
                dayLabel(selectedBarIndex, weekStartMonday) else ""
            val tooltipDuration = if (tooltipVisible)
                formatTrackerDuration(visibleWeek.values.getOrElse(selectedBarIndex) { 0L }) else ""

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

                if (tooltipVisible) {
                    val colWidth = (width - 60.dp) / 7
                    val colCenter = (colWidth * selectedBarIndex) + (10.dp * selectedBarIndex) + (colWidth / 2)
                    Box(
                        modifier = Modifier
                            .offset(x = colCenter, y = (-38).dp)
                            .width(0.dp)
                            .wrapContentWidth(align = Alignment.CenterHorizontally, unbounded = true)
                    ) {
                        UsageTooltip(
                            visible = tooltipVisible,
                            label = tooltipLabel,
                            duration = tooltipDuration
                        )
                    }
                }
            }

            val week = history[pagerState.currentPage]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    week.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "‹ swipe ›",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
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

