package com.veritas.reader.ui.screens


import android.graphics.BitmapFactory
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.graphics.luminance
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Autorenew
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import com.veritas.reader.VeritasPackStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb



@Composable
internal fun DashboardDonutChart(
    title: String,
    slices: List<DonutSlice>,
    totalLabel: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onSliceClick: ((DonutSlice) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val totalVal = slices.sumOf { it.value.toDouble() }.toFloat()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    var animationPlayed by remember { mutableStateOf(false) }
    val entryAnimFraction by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "dashboardDonutChartEntryAnim"
    )
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val selectedSlice = selectedIndex?.let { slices.getOrNull(it) }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDetailDialog = true
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val density = LocalDensity.current
                    val strokeWidthPx = with(density) { 8.dp.toPx() }

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
                                        if (dist in (radius - strokeWidthPx * 2.2f)..radius) {
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
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedIndex = if (selectedIndex == found) null else found
                                            }
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showDetailDialog = true
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
                                val isSelected = selectedIndex == idx
                                val sliceColor = if (selectedIndex == null || isSelected) slice.color else slice.color.copy(alpha = 0.35f)
                                val currentStroke = if (isSelected) strokeWidthPx * 1.35f else strokeWidthPx
                                val sweepAngle = (slice.value / totalVal) * 360f * entryAnimFraction
                                drawArc(
                                    color = sliceColor,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = Offset(centerX - radius, centerY - radius),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = currentStroke)
                                )
                                startAngle += (slice.value / totalVal) * 360f
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selectedSlice != null && totalVal > 0f) {
                            val pct = (selectedSlice.value * 100f / totalVal).toInt()
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = selectedSlice.color
                            )
                            Text(
                                text = selectedSlice.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = if (totalVal >= 1000f) "%.1fk".format(totalVal / 1000f) else "${totalVal.toInt()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = totalLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    slices.take(4).forEachIndexed { idx, slice ->
                        val isSelected = selectedIndex == idx
                        val percentage = if (totalVal > 0f) (slice.value * 100f / totalVal).toInt() else 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedIndex = if (selectedIndex == idx) null else idx
                                }
                                .padding(horizontal = 2.dp, vertical = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 8.dp else 6.dp)
                                    .background(slice.color, CircleShape)
                            )
                            Text(
                                text = slice.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDetailDialog) {
        DonutChartDetailDialog(
            title = title,
            slices = slices,
            totalLabel = totalLabel,
            totalVal = totalVal,
            onActionClick = onClick,
            onSliceClick = onSliceClick,
            onDismiss = { showDetailDialog = false }
        )
    }
}

@Composable
private fun DonutChartDetailDialog(
    title: String,
    slices: List<DonutSlice>,
    totalLabel: String,
    totalVal: Float,
    onActionClick: (() -> Unit)?,
    onSliceClick: ((DonutSlice) -> Unit)?,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total: ${if (totalVal >= 1000f) "%.1fk".format(totalVal / 1000f) else "${totalVal.toInt()}"} $totalLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    slices.forEach { slice ->
                        val pct = if (totalVal > 0f) (slice.value * 100f / totalVal).toInt() else 0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = onSliceClick != null) {
                                    onDismiss()
                                    onSliceClick?.invoke(slice)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(slice.color, CircleShape)
                                        )
                                        Text(
                                            text = slice.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${if (slice.value >= 1000f) "%.1fk".format(slice.value / 1000f) else "${slice.value.toInt()}"} (${pct}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (slice.description.isNotBlank()) {
                                    Text(
                                        text = slice.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { if (totalVal > 0f) (slice.value / totalVal).coerceIn(0f, 1f) else 0f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)),
                                    color = slice.color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }

                if (onActionClick != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onActionClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (title.contains("Source", ignoreCase = true)) "Explore in Library 📚" else "View Full Reading Insights 📊",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}



@Composable
internal fun AnnotationDocumentCard(
    document: SavedDocument,
    annotations: List<ReaderAnnotation>,
    documentNote: String,
    selectedKeys: Set<String>,
    selectionMode: Boolean,
    onToggleDocumentNoteSelected: () -> Unit,
    onLongPressDocumentNote: () -> Unit,
    onToggleSelected: (ReaderAnnotation) -> Unit,
    onLongPressAnnotation: (ReaderAnnotation) -> Unit,
    onOpenDocumentNote: () -> Unit,
    onOpenAt: (Int) -> Unit,
    sentenceTextLookup: (Int) -> String?,
    onDeleteAnnotations: (Set<String>) -> Unit
) {
    val hasDocumentNote = documentNote.isNotBlank()
    val documentNoteKey = documentNoteStableKey(document.id)
    val selectedDocumentNote = documentNoteKey in selectedKeys
    val noteAnnotations = remember(annotations) { annotations.filter { it.type == AnnotationType.NOTE } }
    
    var expanded by rememberSaveable(document.id) { mutableStateOf(false) }
    var expandedNoteKeys by remember { mutableStateOf(setOf<String>()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete all notes?") },
            text = { Text("This will permanently remove all ${noteAnnotations.size + if (hasDocumentNote) 1 else 0} notes for \"${document.title}\".") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        val keysToDelete = noteAnnotations.map { it.stableKey }.toSet() + if (hasDocumentNote) setOf(documentNoteKey) else emptySet()
                        onDeleteAnnotations(keysToDelete)
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                        .clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${noteAnnotations.size + if (hasDocumentNote) 1 else 0} note${if (noteAnnotations.size + (if (hasDocumentNote) 1 else 0) == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                                val pdf = StudyGuidePdfExporter.generateNotesBatchPdf(
                                    context = context,
                                    documentTitle = document.title,
                                    documentNote = documentNote,
                                    notes = noteAnnotations,
                                    sentenceTextLookup = sentenceTextLookup
                                )
                                if (pdf != null) {
                                    StudyGuidePdfExporter.sharePdfFile(context, pdf, "Share Notes PDF")
                                }
                            },
                            leadingIcon = { Icon(Icons.Outlined.EditNote, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy all") },
                            onClick = {
                                showBatchMenu = false
                                val allText = buildString {
                                    append("=== ").append(document.title).append(" ===\n\n")
                                    if (documentNote.isNotBlank()) {
                                        append("DOCUMENT NOTE:\n").append(documentNote).append("\n\n")
                                    }
                                    noteAnnotations.forEach { ann ->
                                        append("• ").append(ann.note)
                                        val ctx = sentenceTextLookup(ann.chunkIndex)
                                        if (!ctx.isNullOrBlank()) {
                                            append("\n  Context: \"").append(ctx).append("\"")
                                        }
                                        append("\n\n")
                                    }
                                }
                                copyTextToClipboard(context, "Notes Batch", allText)
                            },
                            leadingIcon = { Icon(Icons.Filled.ContentPaste, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete all") },
                            onClick = {
                                showBatchMenu = false
                                showDeleteConfirmDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 14.dp))
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasDocumentNote || noteAnnotations.isNotEmpty()) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (hasDocumentNote) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (selectedDocumentNote) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .pointerInput(selectionMode, selectedDocumentNote, documentNoteKey) {
                                        detectTapGestures(
                                            onLongPress = { onLongPressDocumentNote() },
                                            onTap = {
                                                if (selectionMode) onToggleDocumentNoteSelected() else onOpenDocumentNote()
                                            }
                                        )
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (selectedDocumentNote) Color(0xFFE3F2FD) else Color(0xFFFFF7F0), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedDocumentNote) Icons.Filled.Check else Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = if (selectedDocumentNote) Color(0xFF1565C0) else Color(0xFFF2994A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("General document note", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(documentNote, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                                        .clickable { if (selectionMode) onToggleDocumentNoteSelected() else onOpenDocumentNote() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (selectionMode) (if (selectedDocumentNote) "Selected" else "Select") else "Open",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        val noteGroups = remember(noteAnnotations) { groupNotes(document, noteAnnotations) }
                        
                        noteGroups.forEach { noteGroup ->
                            val keys = noteGroup.annotations.map { it.stableKey }.toSet()
                            NoteGroupCard(
                                group = noteGroup,
                                sentenceTextLookup = sentenceTextLookup,
                                onOpenAt = onOpenAt,
                                onDeleteGroup = {
                                    onDeleteAnnotations(keys)
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}




@Composable
internal fun AudioVoiceMemoWaveform(
    durationLabel: String,
    isPlaying: Boolean,
    progress: Float = 0f,
    onTogglePlay: () -> Unit,
    onSeek: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var waveformWidthPx by remember { mutableStateOf(1f) }
    val heights = remember {
        listOf(8, 14, 22, 12, 18, 26, 16, 28, 20, 12, 24, 18, 10, 16, 22, 14, 20, 28, 16, 10, 24, 18, 12, 8)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onTogglePlay() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(34.dp)
                .onGloballyPositioned { coordinates ->
                    waveformWidthPx = coordinates.size.width.toFloat().coerceAtLeast(1f)
                }
                .pointerInput(onSeek) {
                    if (onSeek != null) {
                        detectTapGestures { offset ->
                            val frac = (offset.x / waveformWidthPx).coerceIn(0f, 1f)
                            onSeek(frac)
                        }
                    }
                }
                .pointerInput(onSeek) {
                    if (onSeek != null) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val frac = (change.position.x / waveformWidthPx).coerceIn(0f, 1f)
                            onSeek(frac)
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val unplayedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val barW = 2.5.dp.toPx()
                val barGap = 2.dp.toPx()
                val step = barW + barGap
                val count = (canvasW / step).toInt().coerceAtLeast(12)
                val startX = (canvasW - (count * step - barGap)) / 2f
                val centerY = canvasH / 2f
                val progressX = (progress * canvasW).coerceIn(0f, canvasW)

                for (i in 0 until count) {
                    val x = startX + i * step + barW / 2f
                    val normalizedIdx = i.toFloat() / count.toFloat()
                    val waveBase = kotlin.math.sin(normalizedIdx * Math.PI.toFloat())
                    val ripple = kotlin.math.sin(i * 0.85f) * 0.28f + kotlin.math.cos(i * 1.6f) * 0.18f
                    val heightRatio = (waveBase * 0.65f + ripple + 0.35f).coerceIn(0.18f, 0.95f)
                    val currentH = if (isPlaying && x <= progressX) {
                        val animWave = kotlin.math.sin((System.currentTimeMillis() / 150.0 + i * 0.5).toDouble()).toFloat() * 0.15f
                        ((heightRatio + animWave) * (canvasH - 4.dp.toPx())).coerceIn(4.dp.toPx(), canvasH - 2.dp.toPx())
                    } else {
                        (heightRatio * (canvasH - 4.dp.toPx())).coerceIn(4.dp.toPx(), canvasH - 2.dp.toPx())
                    }
                    val halfH = currentH / 2f
                    val isPlayed = x <= progressX
                    drawLine(
                        color = if (isPlayed) primaryColor else unplayedColor,
                        start = androidx.compose.ui.geometry.Offset(x, centerY - halfH),
                        end = androidx.compose.ui.geometry.Offset(x, centerY + halfH),
                        strokeWidth = barW,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        Text(
            text = durationLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}



