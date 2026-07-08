package com.veritas.reader.ui.screens

import android.graphics.BitmapFactory
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import com.veritas.reader.VeritasPackStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
internal fun HomeQuickActions(
    continueDocument: SavedDocument?,
    documentCount: Int,
    longestStreak: Int,
    onOpenContinue: (SavedDocument) -> Unit,
    onPlayPauseContinue: (SavedDocument) -> Unit,
    onClearContinue: (SavedDocument) -> Unit,
    onImportClick: () -> Unit,
    importMenuExpanded: Boolean,
    onDismissImportMenu: () -> Unit,
    onOpenFile: () -> Unit,
    onOpenFileBrowser: () -> Unit,
    onOpenImportSettings: () -> Unit,
    onPasteText: () -> Unit,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val disabled = continueDocument == null
        val context = LocalContext.current
        val coverFile = remember(continueDocument?.id) { continueDocument?.id?.let { CoverExtractor.coverFile(context, it) } }
        val coverBitmap = remember(coverFile) {
            coverFile?.let { file ->
                runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
            }
        }

        val isFirstDay = longestStreak <= 1
        val headerTitle = if (continueDocument != null && isFirstDay) "First day of reading" else "Continue reading"

        Text(
            text = headerTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                // Always clickable so an empty card still gives feedback instead of feeling
                // broken: when there's nothing to resume, guide the user with a toast.
                .clickable {
                    if (continueDocument != null) {
                        onOpenContinue(continueDocument)
                    } else {
                        val msg = if (documentCount == 0) {
                            "No documents yet — tap Add to import a file or paste text."
                        } else {
                            "No recent reading yet — open a document from your Library to resume here."
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
            shape = VeritasPackStyle.cardShape(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (disabled) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f * VeritasPackStyle.surfaceAlpha())
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Larger Premium Cover Thumbnail
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(96.dp)
                        .clip(VeritasPackStyle.compactShape())
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .then(
                            if (sharedTransitionScope != null && animatedVisibilityScope != null && continueDocument?.id != null) {
                                with(sharedTransitionScope) {
                                    Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(key = "recent_cover_${continueDocument.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverBitmap != null) {
                        Image(
                            bitmap = coverBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🎧", fontSize = 28.sp)
                    }
                }
                
                // Detailed Information
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (continueDocument == null) {
                        Text(
                            text = "Resume reading",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (documentCount == 0) "Open a file to start reading" else "Pick a document from your library to begin.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = continueDocument.title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        val progressPercentVal = progressPercent(continueDocument)
                        
                        Text(
                            text = "Sentence ${continueDocument.currentIndex + 1} of ${continueDocument.chunkCount} • $progressPercentVal% read",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Inline Progress Bar
                        LinearProgressIndicator(
                            progress = { progressFraction(continueDocument) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    }
                }
                
                // Audio controls inline
                if (continueDocument != null) {
                    val isActiveAndPlaying = PlaybackStateStore.activeDocumentId == continueDocument.id && PlaybackStateStore.isPlaying
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { onPlayPauseContinue(continueDocument) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isActiveAndPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isActiveAndPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        IconButton(
                            onClick = { onClearContinue(continueDocument) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Add content",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Box {
                HomeActionRow(
                    icon = Icons.Filled.Add,
                    title = "Import file",
                    body = "Upload PDF, EPUB, DOCX, PPTX, TXT, or HTML",
                    iconBackground = Color(0xFFF0F3FF),
                    iconForeground = Color(0xFF7C6FFF),
                    onClick = onImportClick
                )
                DropdownMenu(expanded = importMenuExpanded, onDismissRequest = onDismissImportMenu) {
                    DropdownMenuItem(text = { Text("Open file") }, onClick = onOpenFile)
                    DropdownMenuItem(text = { Text("File browser") }, onClick = onOpenFileBrowser)
                    DropdownMenuItem(text = { Text("Import settings") }, onClick = onOpenImportSettings)
                    DropdownMenuItem(text = { Text("Paste text or URL") }, onClick = onPasteText)
                }
            }
        }
    }
}

@Composable
internal fun HomeActionRow(
    icon: ImageVector,
    title: String,
    body: String,
    iconBackground: Color,
    iconForeground: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconForeground,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}






@Composable
internal fun OverviewPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun RecentImportItem(
    document: SavedDocument,
    isQueued: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleQueue: () -> Unit,
    onSetCollection: () -> Unit,
    onManageLists: () -> Unit,
    onRename: () -> Unit,
    onShowDetails: () -> Unit,
    onDelete: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coverFile = remember(document.id) { CoverExtractor.coverFile(context, document.id) }
    val coverBitmap = remember(coverFile) {
        coverFile?.let { file ->
            runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (coverBitmap != null) {
                Image(
                    bitmap = coverBitmap.asImageBitmap(),
                    contentDescription = "Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitleText = buildString {
                append(document.sourceLabel)
                val progress = progressPercent(document)
                if (progress > 0) {
                    append(" • $progress% read")
                }
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    .clickable { onOpen() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
                        .clickable { showActions = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showActions,
                    onDismissRequest = { showActions = false },
                    modifier = Modifier.width(240.dp)
                ) {
                    DropdownMenuItem(text = { Text("Open reading") }, onClick = { showActions = false; onOpen() })
                    DropdownMenuItem(text = { Text(if (document.favorite) "Remove from favorites" else "Add to favorites") }, onClick = { showActions = false; onToggleFavorite() })
                    DropdownMenuItem(text = { Text(if (isQueued) "Remove from Queue" else "Add to Queue") }, onClick = { showActions = false; onToggleQueue() })
                    DropdownMenuItem(text = { Text("Move to collection") }, onClick = { showActions = false; onSetCollection() })
                    DropdownMenuItem(text = { Text("Save to lists") }, onClick = { showActions = false; onManageLists() })
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { showActions = false; onRename() })
                    DropdownMenuItem(text = { Text("Details") }, onClick = { showActions = false; onShowDetails() })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { showActions = false; onDelete() })
                }
            }
        }
    }
}

@Composable
internal fun DashboardDonutChart(
    title: String,
    slices: List<DonutSlice>,
    totalLabel: String,
    modifier: Modifier = Modifier
) {
    val totalVal = slices.sumOf { it.value.toDouble() }.toFloat()
    var animationPlayed by remember { mutableStateOf(false) }
    val entryAnimFraction by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "dashboardDonutChartEntryAnim"
    )
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Card(
        modifier = modifier,
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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

                    Canvas(modifier = Modifier.fillMaxSize()) {
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
                            slices.forEach { slice ->
                                val sweepAngle = (slice.value / totalVal) * 360f * entryAnimFraction
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = Offset(centerX - radius, centerY - radius),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidthPx)
                                )
                                startAngle += (slice.value / totalVal) * 360f
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    slices.take(4).forEach { slice ->
                        val percentage = if (totalVal > 0f) (slice.value * 100f / totalVal).toInt() else 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(slice.color, CircleShape)
                            )
                            Text(
                                text = slice.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomePanel(
    totalCount: Int,
    unreadCount: Int,
    inProgressCount: Int,
    completedCount: Int,
    favoriteCount: Int,
    collectionCount: Int,
    sourceCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Reading Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatTile(value = "$totalCount", label = "Saved", modifier = Modifier.width(84.dp))
            StatTile(value = "$unreadCount", label = "Unread", modifier = Modifier.width(84.dp))
            StatTile(value = "$inProgressCount", label = "Reading", modifier = Modifier.width(84.dp))
            StatTile(value = "$completedCount", label = "Done", modifier = Modifier.width(72.dp))
            StatTile(value = "$favoriteCount", label = "Starred", modifier = Modifier.width(72.dp))
            StatTile(value = "$collectionCount", label = "Collections", modifier = Modifier.width(80.dp))
            StatTile(value = "$sourceCount", label = "Formats", modifier = Modifier.width(72.dp))
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
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
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
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
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
fun QueueSection(
    queuedDocuments: List<SavedDocument>,
    onPlayQueue: () -> Unit,
    onOpenDocument: (SavedDocument) -> Unit,
    onMoveUp: (SavedDocument) -> Unit,
    onMoveDown: (SavedDocument) -> Unit,
    onRemove: (SavedDocument) -> Unit,
    onClearQueue: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Queue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(
                        if (queuedDocuments.isEmpty()) "Build a playlist from your library." else "${queuedDocuments.size} queued item${if (queuedDocuments.size == 1) "" else "s"} ready for continuous playback.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = onClearQueue, enabled = queuedDocuments.isNotEmpty()) { Text("Clear") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onPlayQueue, enabled = queuedDocuments.isNotEmpty()) { Text("Play") }
            }

            if (queuedDocuments.isEmpty()) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "Use Queue on any document card to add it here. Queue order controls what plays next in the background service.",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                queuedDocuments.forEachIndexed { index, document ->
                    QueueItemRow(
                        position = index + 1,
                        document = document,
                        canMoveUp = index > 0,
                        canMoveDown = index < queuedDocuments.lastIndex,
                        onOpen = { onOpenDocument(document) },
                        onMoveUp = { onMoveUp(document) },
                        onMoveDown = { onMoveDown(document) },
                        onRemove = { onRemove(document) }
                    )
                }
            }
        }
    }
}

@Composable
fun QueueItemRow(
    position: Int,
    document: SavedDocument,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val progress = progressFraction(document)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$position", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f).clickable { onOpen() }) {
                    Text(document.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Text("${document.sourceLabel} • ${progressPercent(document)}% • sentence ${document.currentIndex + 1}/${document.chunkCount.coerceAtLeast(1)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SourceBadge(document.sourceLabel)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onMoveUp, enabled = canMoveUp) { Text("Up") }
                OutlinedButton(onClick = onMoveDown, enabled = canMoveDown) { Text("Down") }
                TextButton(onClick = onRemove) { Text("Remove") }
            }
        }
    }
}

@Composable
fun EmptyLibraryCard(onImportFile: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = VeritasPackStyle.surfaceAlpha())),
        shape = VeritasPackStyle.cardShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BrandMark(compact = true)
            Text("No saved readings yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(
                "Import a file or paste text to create your first local reading item. Progress, annotations, and queue state will be saved automatically.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onImportFile) { Text("Import first file") }
        }
    }
}

@Composable
fun DocumentCard(
    document: SavedDocument,
    isQueued: Boolean,
    viewMode: LibraryViewMode,
    selectionMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelected: () -> Unit,
    onDelete: () -> Unit,
    onToggleQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onSetCollection: () -> Unit,
    onShowDetails: () -> Unit,
    onManageLists: () -> Unit = {},
    modifier: Modifier = Modifier,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null
) {
    val progress = progressFraction(document)
    var showActions by remember { mutableStateOf(false) }
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 0.965f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "documentCardSelectionScale"
    )
    val coverSize = when (viewMode) {
        LibraryViewMode.SMALL, LibraryViewMode.LIST -> 48.dp
        LibraryViewMode.DETAILS -> 72.dp
        else -> 64.dp
    }
    val showChips = viewMode == LibraryViewMode.MEDIUM || viewMode == LibraryViewMode.DETAILS
    val showPreview = viewMode == LibraryViewMode.DETAILS

    val context = LocalContext.current
    val coverFile = remember(document.id) { CoverExtractor.coverFile(context, document.id) }
    val coverBitmap = remember(coverFile) {
        coverFile?.let { file ->
            runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = selectionScale, scaleY = selectionScale)
            .pointerInput(selectionMode, selected, document.id) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = {
                        if (selectionMode) onToggleSelected() else onOpen()
                    }
                )
            },
        shape = VeritasPackStyle.compactShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = VeritasPackStyle.surfaceAlpha())
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(if (viewMode == LibraryViewMode.LIST) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(coverSize)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                when {
                                    selected -> MaterialTheme.colorScheme.primary
                                    document.favorite -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .then(
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "cover_${document.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (coverBitmap != null && !selected) {
                    Image(
                        bitmap = coverBitmap.asImageBitmap(),
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        when {
                            selected -> "✓"
                            document.favorite -> "★"
                            else -> document.sourceLabel.take(3).uppercase()
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${document.sourceLabel} • ${progressPercent(document)}% read • ${document.chunkCount} sentences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showPreview) {
                    Text(document.preview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (viewMode != LibraryViewMode.LIST) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (showChips) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (document.collection.isNotBlank()) SoftChip(document.collection, emphasis = true)
                        if (isQueued) SoftChip("Queued")
                        if (document.favorite) SoftChip("Favorite")
                        SoftChip("Updated ${formatUpdated(document.updatedAt)}")
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectionMode) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                CircleShape
                            )
                            .clickable { onToggleSelected() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (selected) "✓" else "+", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isQueued) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                CircleShape
                            )
                            .clickable { onToggleQueue() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isQueued) "✓" else "+",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
                                .clickable { showActions = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Document actions",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showActions,
                            onDismissRequest = { showActions = false },
                            modifier = Modifier.width(260.dp)
                        ) {
                            DropdownMenuItem(text = { Text("Open reading") }, onClick = { showActions = false; onOpen() })
                            DropdownMenuItem(text = { Text(if (document.favorite) "Remove from favorites" else "Add to favorites") }, onClick = { showActions = false; onToggleFavorite() })
                            DropdownMenuItem(text = { Text(if (isQueued) "Remove from Queue" else "Add to Queue") }, onClick = { showActions = false; onToggleQueue() })
                            DropdownMenuItem(text = { Text("Move to collection") }, onClick = { showActions = false; onSetCollection() })
                            DropdownMenuItem(text = { Text("Save to lists") }, onClick = { showActions = false; onManageLists() })
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { showActions = false; onRename() })
                            DropdownMenuItem(text = { Text("Details") }, onClick = { showActions = false; onShowDetails() })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showActions = false; onDelete() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentTileCard(
    document: SavedDocument,
    isQueued: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelected: () -> Unit,
    onDelete: () -> Unit,
    onToggleQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onSetCollection: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier,
    onManageLists: () -> Unit = {},
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null
) {
    var showActions by remember { mutableStateOf(false) }
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 0.965f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "documentTileSelectionScale"
    )
    val context = LocalContext.current
    val coverFile = remember(document.id) { CoverExtractor.coverFile(context, document.id) }
    val coverBitmap = remember(coverFile) {
        coverFile?.let { file ->
            runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
        }
    }
    val isUnread = document.currentIndex == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = selectionScale, scaleY = selectionScale),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = VeritasPackStyle.surfaceAlpha())
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (selectionMode) onToggleSelected() else onOpen() }
                .pointerInput(selectionMode, selected, document.id) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onTap = { if (selectionMode) onToggleSelected() else onOpen() }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .then(
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "cover_${document.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        } else Modifier
                    )
            ) {
                if (coverBitmap != null) {
                    Image(
                        bitmap = coverBitmap.asImageBitmap(),
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = document.sourceLabel.take(3).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    }
                }

                if (selectionMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isUnread) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "NEW",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (document.favorite) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Yellow.copy(alpha = 0.9f), CircleShape)
                                    .size(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Favorite",
                                    tint = Color.Black,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Box {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clickable { showActions = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Document actions",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                            DropdownMenuItem(text = { Text("Open reading") }, onClick = { showActions = false; onOpen() })
                            DropdownMenuItem(text = { Text(if (document.favorite) "Remove favorite" else "Add favorite") }, onClick = { showActions = false; onToggleFavorite() })
                            DropdownMenuItem(text = { Text(if (isQueued) "Remove from Queue" else "Add to Queue") }, onClick = { showActions = false; onToggleQueue() })
                            DropdownMenuItem(text = { Text("Move to collection") }, onClick = { showActions = false; onSetCollection() })
                            DropdownMenuItem(text = { Text("Save to lists") }, onClick = { showActions = false; onManageLists() })
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { showActions = false; onRename() })
                            DropdownMenuItem(text = { Text("Details") }, onClick = { showActions = false; onShowDetails() })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showActions = false; onDelete() })
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = document.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${progressPercent(document)}% read",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${document.chunkCount} sent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                LinearProgressIndicator(
                    progress = { progressFraction(document) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
internal fun EmbeddedOnboardingBlock(
    onOpenFileBrowser: () -> Unit,
    onPasteText: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text("🎧", fontSize = 40.sp)
            }
            Text(
                "Listen to anything, eyes-free.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Import PDFs, EPUBs, documents, or paste web articles to get started.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onOpenFileBrowser,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Browse Files")
                }
                FilledTonalButton(
                    onClick = onPasteText,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Paste Text")
                }
            }
        }
    }
}


// One-time staggered entrance for top-level home cards: fade in + slide up ~16dp
// with a per-card delay, on the tab's FIRST show only. `played` is saveable, so
// scrolling away and back (or revisiting the tab) never replays it — the animation
// can therefore never run during scroll frames.
@Composable
internal fun Modifier.staggeredEntrance(position: Int): Modifier {
    if (VeritasThemeState.reduceMotion) return this
    var played by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (played) 1f else 0f) }
    if (!played) {
        LaunchedEffect(Unit) {
            delay(position * 40L)
            progress.animateTo(1f, tween(durationMillis = 280, easing = LinearOutSlowInEasing))
            played = true
        }
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 16.dp.toPx()
    }
}

@Composable
internal fun RowScope.BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
    label: String
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "navColor"
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    // 0.93x press-scale with a springy release so taps feel physical.
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navPress"
    )
    // Pill indicator softly scales/fades in behind the active icon (Material 3 style).
    val pillProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navPill"
    )
    val pillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    Column(
        modifier = Modifier
            .weight(1f)
            // Merge into one focusable tab that announces its label and selected
            // state to TalkBack (e.g. "Library, tab, selected").
            .semantics(mergeDescendants = true) {
                this.selected = selected
                this.role = Role.Tab
                this.contentDescription = label
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(width = 46.dp, height = 27.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 0.55f + 0.45f * pillProgress
                        alpha = pillProgress.coerceIn(0f, 1f)
                    }
                    .background(pillColor, RoundedCornerShape(50))
            )
            icon(contentColor)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = contentColor,
            maxLines = 1
        )
    }
}

@Composable
internal fun ImportSheetMenu(
    onSelectOption: (ImportOption) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add something", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ImportSheetOptionCard(
                icon = Icons.Filled.FolderOpen,
                title = "File browser",
                subtitle = "Browse and batch import local files",
                onClick = { onSelectOption(ImportOption.BROWSE) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.ContentPaste,
                title = "Paste text",
                subtitle = "Copy and paste any content",
                onClick = { onSelectOption(ImportOption.PASTE) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.Language,
                title = "From web",
                subtitle = "Paste a link to an article",
                onClick = { onSelectOption(ImportOption.WEB) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.PhotoCamera,
                title = "Scan / OCR",
                subtitle = "Take a photo of printed text",
                onClick = { onSelectOption(ImportOption.SCAN) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.Description,
                title = "Browse phone folders",
                subtitle = "Open system file chooser",
                onClick = { onSelectOption(ImportOption.FILE) }
            )
            ImportSheetOptionCard(
                icon = Icons.Filled.EditNote,
                title = "Write note",
                subtitle = "Create a freeform reading note",
                onClick = { onSelectOption(ImportOption.WRITE_NOTE) }
            )
        }
    }
}

@Composable
internal fun ImportSheetOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun ImportSheetWeb(
    urlText: String,
    onUrlChange: (String) -> Unit,
    onImport: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("Import link", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        Text("Paste a link to any web article, report, or blog post below:", color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        OutlinedTextField(
            value = urlText,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Article Link") },
            placeholder = { Text("https://example.com/article...") },
            singleLine = true
        )
        
        Button(
            onClick = { onImport(urlText) },
            modifier = Modifier.fillMaxWidth(),
            enabled = urlText.isNotBlank() && WebArticleExtractor.looksLikeUrl(urlText)
        ) {
            Text("Import web article")
        }
    }
}

@Composable
internal fun ImportSheetPaste(
    pastedText: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("Paste text", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        Text("Paste loose text, a document snippet, or email contents below:", color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        OutlinedTextField(
            value = pastedText,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            label = { Text("Content") },
            placeholder = { Text("Paste text here...") }
        )
        
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = pastedText.isNotBlank()
        ) {
            Text("Save reading")
        }
    }
}

@Composable
internal fun StudyEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    onGoToLibrary: () -> Unit,
    onImportFile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onImportFile) {
                    Text("Import file")
                }
                OutlinedButton(onClick = onGoToLibrary) {
                    Text("Go to Library")
                }
            }
        }
    }
}

internal fun renderMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    // Defensive slice: a marker's closing delimiter can, in malformed input, resolve
    // to a position at or before the opening one. An unguarded substring(begin, end)
    // with begin > end throws StringIndexOutOfBounds during the tap/draw pass that
    // builds this AnnotatedString — the crash this guards against.
    fun String.safeSlice(begin: Int, end: Int): String {
        val a = begin.coerceIn(0, length)
        val b = end.coerceIn(a, length)
        return substring(a, b)
    }
    return androidx.compose.ui.text.buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                        append(text.safeSlice(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("**")
                        i += 2
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.safeSlice(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append("*")
                        i += 1
                    }
                }
                text.startsWith("##", i) -> {
                    pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp))
                    val end = text.indexOf("\n", i)
                    if (end != -1) {
                        append(text.safeSlice(i + 2, end).trim())
                        pop()
                        i = end
                    } else {
                        append(text.safeSlice(i + 2, text.length).trim())
                        pop()
                        i = text.length
                    }
                }
                text.startsWith("__", i) -> {
                    val end = text.indexOf("__", i + 2)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline))
                        append(text.safeSlice(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("__")
                        i += 2
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

/**
 * A flashcard set as a tile (two per row), coloured like the old spaced-repetition
 * deck card. Shows the set name, a "View cards" button, and recall pills that
 * double as filters (tap "Hard" → only that bucket). Overflow menu renames/deletes.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun FlashcardSetTile(
    set: FlashcardSet,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
    onViewBucket: (String) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val counts = set.recallCounts
    // Cover-sized tile matching the library grid (portrait 0.75 aspect). Tap anywhere
    // opens the viewer (no "View cards" button).
    Card(
        modifier = modifier.aspectRatio(0.75f).clickable { onOpen() },
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Set options", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Rename") }, onClick = { menuOpen = false; onRename() })
                        DropdownMenuItem(text = { Text("Delete set") }, onClick = { menuOpen = false; onDelete() })
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                set.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${set.cards.size} card${if (set.cards.size == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Recall pills — larger and centered; each filters to its bucket.
            if (counts.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FLASHCARD_RECALL_META.forEach { (key, label, color) ->
                        val n = counts[key] ?: 0
                        if (n > 0) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = color.copy(alpha = 0.16f),
                                modifier = Modifier.clickable { onViewBucket(key) }
                            ) {
                                Text(
                                    "$n $label",
                                    color = color,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Softer, calmer shades (same hues) — the saturated originals were eye-piercing as
// solid button fills. Used for both the viewer buttons and the tile recall pills.
internal val FLASHCARD_RECALL_META: List<Triple<String, String, Color>> = listOf(
    Triple("again", "Again", Color(0xFFE57373)),
    Triple("hard", "Hard", Color(0xFFFFB74D)),
    Triple("good", "Good", Color(0xFF64B5F6)),
    Triple("easy", "Easy", Color(0xFF4DB6AC))
)

/**
 * Full-screen single-card viewer (see design reference): one card at a time, tap
 * to flip front↔back, forward/back through the set, recall buttons that bucket the
 * card (latest-wins), a subtle per-card delete, and "Exit card". Cards are passed
 * pre-filtered when opened from a recall pill.
 */
@Composable
internal fun FlashcardViewerDialog(
    setName: String,
    cards: List<FlashcardProgress>,
    onRate: (String, String) -> Unit,
    onDeleteCard: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var order by remember(cards) { mutableStateOf(cards) }
    var index by remember { mutableStateOf(0) }
    var flipped by remember { mutableStateOf(false) }

    if (order.isEmpty()) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    val safeIndex = index.coerceIn(0, order.lastIndex)
    val card = order[safeIndex]

    fun goTo(next: Int) {
        index = next.coerceIn(0, order.lastIndex)
        flipped = false
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        setName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismiss) { Text("Exit card") }
                }

                // Card is centered in the free space with a squarer aspect ratio so
                // text isn't marooned in a tall card. Corner radius stays the theme
                // pack's shape (VeritasPackStyle.cardShape()).
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { flipped = !flipped },
                    shape = VeritasPackStyle.cardShape(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        // Scrollable so long answers never overflow the fixed-aspect card;
                        // the corner icons below stay fixed (outside this scroll).
                        Box(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                // Prettify equations (x², √, ×, π…) so cards read as math.
                                text = MathText.beautify(if (flipped) card.back else card.front),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Subtle per-card delete — present but not distracting.
                        IconButton(
                            onClick = {
                                val removedId = card.id
                                val remaining = order.filterNot { it.id == removedId }
                                onDeleteCard(removedId)
                                order = remaining
                                if (remaining.isNotEmpty()) index = safeIndex.coerceIn(0, remaining.lastIndex)
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete this card",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        // Flip affordance.
                        Icon(
                            Icons.Filled.Autorenew,
                            contentDescription = "Tap card to flip",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.align(Alignment.BottomEnd).size(22.dp)
                        )
                    }
                }
                }

                Text(
                    "Rate your recall",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                )
                // All four recall buttons, sized to fit one centered row without clipping.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FLASHCARD_RECALL_META.forEach { (key, label, color) ->
                        val selected = card.recall == key
                        Button(
                            onClick = {
                                onRate(card.id, key)
                                order = order.map { if (it.id == card.id) it.copy(recall = key) else it }
                                if (safeIndex < order.lastIndex) goTo(safeIndex + 1)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) color else color.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).height(48.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                        ) {
                            Text(
                                if (selected) "✓$label" else label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { goTo(safeIndex - 1) }, enabled = safeIndex > 0) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous card")
                    }
                    Text(
                        "${safeIndex + 1} of ${order.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { goTo(safeIndex + 1) }, enabled = safeIndex < order.lastIndex) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next card")
                    }
                }
            }
        }
    }
}

data class BookmarkGroup(
    val id: String,
    val document: SavedDocument,
    val annotations: List<ReaderAnnotation>,
    val highlightColor: String?,
    val startSentence: Int,
    val endSentence: Int
)

fun groupBookmarks(
    document: SavedDocument,
    annotations: List<ReaderAnnotation>
): List<BookmarkGroup> {
    val sorted = annotations.sortedBy { it.chunkIndex }
    val groups = mutableListOf<BookmarkGroup>()
    
    val withGroup = sorted.filter { !it.selectionGroupId.isNullOrBlank() }
    val withoutGroup = sorted.filter { it.selectionGroupId.isNullOrBlank() }
    
    val groupedById = withGroup.groupBy { it.selectionGroupId }
    groupedById.forEach { (groupId, groupAnnots) ->
        val sortedAnnots = groupAnnots.sortedBy { it.chunkIndex }
        val start = sortedAnnots.first().chunkIndex
        val end = sortedAnnots.last().chunkIndex
        val color = sortedAnnots.first().highlightColor ?: "#FFE082"
        groups.add(
            BookmarkGroup(
                id = groupId ?: UUID.randomUUID().toString(),
                document = document,
                annotations = sortedAnnots,
                highlightColor = color,
                startSentence = start,
                endSentence = end
            )
        )
    }
    
    if (withoutGroup.isNotEmpty()) {
        var currentRun = mutableListOf<ReaderAnnotation>()
        for (ann in withoutGroup) {
            if (currentRun.isEmpty()) {
                currentRun.add(ann)
            } else {
                val lastAnn = currentRun.last()
                if (ann.chunkIndex == lastAnn.chunkIndex + 1 && ann.highlightColor == lastAnn.highlightColor) {
                    currentRun.add(ann)
                } else {
                    val start = currentRun.first().chunkIndex
                    val end = currentRun.last().chunkIndex
                    val color = currentRun.first().highlightColor ?: "#FFE082"
                    groups.add(
                        BookmarkGroup(
                            id = "legacy-${document.id}-$start-$end",
                            document = document,
                            annotations = currentRun,
                            highlightColor = color,
                            startSentence = start,
                            endSentence = end
                        )
                    )
                    currentRun = mutableListOf(ann)
                }
            }
        }
        if (currentRun.isNotEmpty()) {
            val start = currentRun.first().chunkIndex
            val end = currentRun.last().chunkIndex
            val color = currentRun.first().highlightColor ?: "#FFE082"
            groups.add(
                BookmarkGroup(
                    id = "legacy-${document.id}-$start-$end",
                    document = document,
                    annotations = currentRun,
                    highlightColor = color,
                    startSentence = start,
                    endSentence = end
                )
            )
        }
    }
    
    return groups.sortedBy { it.startSentence }
}

data class NoteGroup(
    val id: String,
    val document: SavedDocument,
    val annotations: List<ReaderAnnotation>,
    val noteText: String,
    val highlightColor: String?,
    val startSentence: Int,
    val endSentence: Int
)

fun groupNotes(
    document: SavedDocument,
    annotations: List<ReaderAnnotation>
): List<NoteGroup> {
    val sorted = annotations.sortedBy { it.chunkIndex }
    val groups = mutableListOf<NoteGroup>()
    
    val withGroup = sorted.filter { !it.selectionGroupId.isNullOrBlank() }
    val withoutGroup = sorted.filter { it.selectionGroupId.isNullOrBlank() }
    
    val groupedById = withGroup.groupBy { it.selectionGroupId }
    groupedById.forEach { (groupId, groupAnnots) ->
        val sortedAnnots = groupAnnots.sortedBy { it.chunkIndex }
        val start = sortedAnnots.first().chunkIndex
        val end = sortedAnnots.last().chunkIndex
        val text = sortedAnnots.first().note
        val color = sortedAnnots.first().highlightColor
        groups.add(
            NoteGroup(
                id = groupId ?: java.util.UUID.randomUUID().toString(),
                document = document,
                annotations = sortedAnnots,
                noteText = text,
                highlightColor = color,
                startSentence = start,
                endSentence = end
            )
        )
    }
    
    withoutGroup.forEach { ann ->
        groups.add(
            NoteGroup(
                id = "single-${document.id}-${ann.chunkIndex}",
                document = document,
                annotations = listOf(ann),
                noteText = ann.note,
                highlightColor = ann.highlightColor,
                startSentence = ann.chunkIndex,
                endSentence = ann.chunkIndex
            )
        )
    }
    
    return groups.sortedBy { it.startSentence }
}

@Composable
internal fun BookmarkDocumentCard(
    document: SavedDocument,
    groups: List<BookmarkGroup>,
    sentenceTextLookup: (Int) -> String?,
    onOpenAt: (Int) -> Unit,
    onDeleteAnnotations: (Set<String>) -> Unit
) {
    var expanded by rememberSaveable(document.id) { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bookmark,
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
                        text = "${groups.size} bookmark${if (groups.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 14.dp))
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groups.forEach { group ->
                        BookmarkGroupCard(
                            group = group,
                            sentenceTextLookup = sentenceTextLookup,
                            onOpenAt = onOpenAt,
                            onDeleteGroup = {
                                onDeleteAnnotations(group.annotations.map { it.stableKey }.toSet())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BookmarkGroupCard(
    group: BookmarkGroup,
    sentenceTextLookup: (Int) -> String?,
    onOpenAt: (Int) -> Unit,
    onDeleteGroup: () -> Unit
) {
    val context = LocalContext.current
    var expanded by rememberSaveable(group.id) { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    val bookTitle = group.document.title
    val (cleanTitle, authorName) = remember(bookTitle) { getBookAndAuthor(bookTitle) }
    
    val collapsedText = remember(group, sentenceTextLookup) {
        val firstAnn = group.annotations.firstOrNull()
        val firstText = firstAnn?.let { sentenceTextLookup(it.chunkIndex) }
        if (firstText.isNullOrBlank()) {
            if (group.startSentence == group.endSentence) "Sentence ${group.startSentence + 1}"
            else "Sentences ${group.startSentence + 1}–${group.endSentence + 1}"
        } else {
            if (firstText.length > 60) firstText.take(57) + "..." else firstText
        }
    }
    
    val highlightColor = remember(group.highlightColor) {
        runCatching { Color(android.graphics.Color.parseColor(group.highlightColor)) }
            .getOrDefault(Color(0xFFFFE082))
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color = highlightColor, shape = CircleShape)
                )
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Text(
                    text = collapsedText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))
                
                val sentencesText = remember(group.annotations, sentenceTextLookup) {
                    group.annotations.map { ann ->
                        sentenceTextLookup(ann.chunkIndex) ?: ""
                    }
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    group.annotations.forEachIndexed { idx, ann ->
                        val text = sentencesText.getOrNull(idx)?.ifBlank { null } ?: "Loading sentence text..."
                        
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(color = highlightColor, shape = RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { onOpenAt(group.startSentence) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Open in document ↗",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Actions",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Share") },
                                    onClick = {
                                        showMenu = false
                                        val fullText = sentencesText.joinToString(" ")
                                        shareBookmarkAsImage(
                                            context = context,
                                            bookTitle = cleanTitle,
                                            authorName = authorName,
                                            highlightedText = fullText,
                                            highlightColorHex = group.highlightColor ?: "#FFE082"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Copy") },
                                    onClick = {
                                        showMenu = false
                                        val fullText = sentencesText.joinToString(" ")
                                        copyTextToClipboard(context, "Bookmark Text", fullText)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        onDeleteGroup()
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

@Composable
internal fun NoteGroupCard(
    group: NoteGroup,
    sentenceTextLookup: (Int) -> String?,
    onOpenAt: (Int) -> Unit,
    onDeleteGroup: () -> Unit
) {
    val context = LocalContext.current
    var expanded by rememberSaveable(group.id) { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    val bookTitle = group.document.title
    val (cleanTitle, authorName) = remember(bookTitle) { getBookAndAuthor(bookTitle) }
    
    val collapsedText = remember(group, sentenceTextLookup) {
        val firstAnn = group.annotations.firstOrNull()
        val firstText = firstAnn?.let { sentenceTextLookup(it.chunkIndex) }
        if (firstText.isNullOrBlank()) {
            if (group.startSentence == group.endSentence) "Sentence ${group.startSentence + 1}"
            else "Sentences ${group.startSentence + 1}–${group.endSentence + 1}"
        } else {
            if (firstText.length > 60) firstText.take(57) + "..." else firstText
        }
    }
    
    val highlightColor = remember(group.highlightColor) {
        runCatching { Color(android.graphics.Color.parseColor(group.highlightColor)) }
            .getOrDefault(Color(0xFFFFE082))
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color = highlightColor, shape = CircleShape)
                )
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collapsedText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (group.noteText.isNotBlank()) {
                        Text(
                            text = group.noteText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))
                
                val sentencesText = remember(group.annotations, sentenceTextLookup) {
                    group.annotations.map { ann ->
                        sentenceTextLookup(ann.chunkIndex) ?: ""
                    }
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    group.annotations.forEachIndexed { idx, ann ->
                        val text = sentencesText.getOrNull(idx)?.ifBlank { null } ?: "Loading sentence text..."
                        
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(color = highlightColor, shape = RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    if (group.noteText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.EditNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Note",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = group.noteText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { onOpenAt(group.startSentence) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Open in document ↗",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Actions",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Share") },
                                    onClick = {
                                        showMenu = false
                                        val fullText = sentencesText.joinToString(" ") + "\n\nNote: " + group.noteText
                                        shareBookmarkAsImage(
                                            context = context,
                                            bookTitle = cleanTitle,
                                            authorName = authorName,
                                            highlightedText = fullText,
                                            highlightColorHex = group.highlightColor ?: "#FFE082"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Copy") },
                                    onClick = {
                                        showMenu = false
                                        val fullText = sentencesText.joinToString(" ") + "\n\nNote: " + group.noteText
                                        copyTextToClipboard(context, "Note & Context Text", fullText)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        onDeleteGroup()
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


