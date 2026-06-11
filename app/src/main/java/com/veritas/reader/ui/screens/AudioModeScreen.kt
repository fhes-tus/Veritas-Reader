package com.veritas.reader.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.veritas.reader.VeritasPackStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import com.veritas.reader.R
import com.veritas.reader.ReaderMode
import com.veritas.reader.ReaderModeToggle
import java.io.File
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun AudioModeScreen(
    title: String,
    currentIndex: Int,
    totalChunks: Int,
    currentSentence: String,
    isPlaying: Boolean,
    coverFile: File? = null,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismiss: () -> Unit,
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    rate: Float = 1.0f,
    onRateChange: (Float) -> Unit = {},
    readerMode: ReaderMode = ReaderMode.LISTEN,
    onReaderModeChange: (ReaderMode) -> Unit = {},
    hasCanvas: Boolean = false,
    documentChunks: List<String> = emptyList(),
    onSentenceClick: (Int) -> Unit = {},
    onOpenSleepTimer: () -> Unit = {},
    onOpenVoiceStudio: () -> Unit = {},
    onOpenNarrationStudio: () -> Unit = {},
    onExportAudio: () -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current

    // ── Real-time elapsed tracker (Pre-Phase Item 1) ──
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var baseElapsedAtLastChange by remember { mutableLongStateOf(0L) }
    var lastIndexChangeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showRead by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            lastIndexChangeMillis =
                System.currentTimeMillis() - (elapsedSeconds - baseElapsedAtLastChange) * 1_000L
            while (true) {
                delay(1_000L)
                elapsedSeconds = baseElapsedAtLastChange +
                    (System.currentTimeMillis() - lastIndexChangeMillis) / 1_000L
            }
        }
    }

    LaunchedEffect(currentIndex) {
        baseElapsedAtLastChange = elapsedSeconds
        lastIndexChangeMillis = System.currentTimeMillis()
    }

    val avgSecondsPerSentence = if (currentIndex > 0) elapsedSeconds.toFloat() / currentIndex else 3.0f
    val remainingSentences = (totalChunks - currentIndex).coerceAtLeast(0)
    val estimatedRemainingSeconds = (remainingSentences * avgSecondsPerSentence).toLong()
    val totalEstimatedSeconds = elapsedSeconds + estimatedRemainingSeconds

    val progress = if (totalChunks > 0) currentIndex.toFloat() / totalChunks else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(500))

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .pointerInput(Unit) {
                    var dragAccumulator = 0f
                    detectDragGestures(
                        onDragEnd = { dragAccumulator = 0f },
                        onDragCancel = { dragAccumulator = 0f }
                    ) { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount.x
                        if (dragAccumulator > 150) {
                            onPrevious()
                            dragAccumulator = 0f
                        } else if (dragAccumulator < -150) {
                            onNext()
                            dragAccumulator = 0f
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header with mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious, // Wait, a left arrow or back icon
                        contentDescription = "Back",
                        modifier = Modifier.graphicsLayer(rotationZ = 180f), // rotates it to look like back arrow
                        tint = contentColor
                    )
                }
                ReaderModeToggle(
                    currentMode = readerMode,
                    onModeSelected = onReaderModeChange,
                    hasCanvas = hasCanvas,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
                
                var toolsExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { toolsExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Pause, // We can use a custom representation or standard ⋮ icon
                            contentDescription = "More options",
                            tint = contentColor,
                            modifier = Modifier.graphicsLayer(rotationZ = 90f) // Rotates pause to look like ⋮
                        )
                    }
                    DropdownMenu(
                        expanded = toolsExpanded,
                        onDismissRequest = { toolsExpanded = false },
                        modifier = Modifier.width(220.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("⏱️ Sleep timer") },
                            onClick = {
                                toolsExpanded = false
                                onOpenSleepTimer()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🎙️ Voice & language") },
                            onClick = {
                                toolsExpanded = false
                                onOpenVoiceStudio()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🎭 Narration mode") },
                            onClick = {
                                toolsExpanded = false
                                onOpenNarrationStudio()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📤 Export audio") },
                            onClick = {
                                toolsExpanded = false
                                onExportAudio()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player / Read Sub-toggle
            Row(
                modifier = Modifier
                    .background(contentColor.copy(alpha = 0.06f), VeritasPackStyle.chipShape())
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val playerBg = if (!showRead) contentColor.copy(alpha = 0.15f) else Color.Transparent
                val playerTextColor = if (!showRead) contentColor else contentColor.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .clip(VeritasPackStyle.chipShape())
                        .background(playerBg)
                        .clickable { showRead = false }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Player", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = playerTextColor)
                }

                val readBg = if (showRead) contentColor.copy(alpha = 0.15f) else Color.Transparent
                val readTextColor = if (showRead) contentColor else contentColor.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .clip(VeritasPackStyle.chipShape())
                        .background(readBg)
                        .clickable { showRead = true }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Read", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = readTextColor)
                }
            }

            // 2. Center Content: Read List or Cover Art
            if (showRead) {
                val readListState = rememberLazyListState()

                LaunchedEffect(currentIndex) {
                    if (currentIndex in documentChunks.indices) {
                        // Wait for a valid measured viewport height
                        var viewportHeight = 0
                        var attempts = 0
                        while (viewportHeight <= 0 && attempts < 15) {
                            val layoutInfo = readListState.layoutInfo
                            viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                            if (viewportHeight <= 0) {
                                delay(30)
                            }
                            attempts++
                        }
                        if (viewportHeight > 0) {
                            val layoutInfo = readListState.layoutInfo
                            val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentIndex }
                            val itemHeight = itemInfo?.size ?: 100
                            val offset = -(viewportHeight / 3 - itemHeight / 2)
                            readListState.animateScrollToItem(currentIndex, offset)
                        } else {
                            // Fallback to average offset if measurement still pending
                            readListState.animateScrollToItem(currentIndex, -220)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = readListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(vertical = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(documentChunks) { index, sentence ->
                            val isActive = index == currentIndex
                            val alpha = if (isActive) 1.0f else 0.35f
                            val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            val fontSize = if (isActive) 19.sp else 15.sp
                            val textColor = contentColor.copy(alpha = alpha)

                            Text(
                                text = sentence,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = fontSize,
                                    fontWeight = fontWeight,
                                    lineHeight = fontSize * 1.4f
                                ),
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onSentenceClick(index)
                                    }
                            )
                        }
                    }

                    // Top fading gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        backgroundColor,
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Bottom fading gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        backgroundColor
                                    )
                                )
                            )
                    )
                }
            } else {
                // Cover image + Circular progress
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = primaryColor,
                            strokeWidth = 6.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        val coverBitmap = remember(coverFile?.absolutePath) {
                            coverFile?.takeIf { it.exists() }?.let { file ->
                                runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
                            }
                        }
                        if (coverBitmap != null) {
                            Image(
                                bitmap = coverBitmap.asImageBitmap(),
                                contentDescription = "Document cover",
                                modifier = Modifier
                                    .size(242.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.veritas_reader_icon),
                                contentDescription = "Cover",
                                modifier = Modifier
                                    .size(242.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // 3. Sentence details + Ticker (Smooth per-second elapsed / dynamic total estimation)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (readerMode != ReaderMode.TEXT && !showRead) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentSentence,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.heightIn(min = 72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Time progress row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = formatTime(elapsedSeconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.weight(1f),
                        color = primaryColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = formatTime(totalEstimatedSeconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                }
            }

            // 4. Fine-tuning row (Speed and Bookmarks)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Speed cycle pill
                var speedDropdownExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        shape = VeritasPackStyle.chipShape(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable {
                                speedDropdownExpanded = true
                            }
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2fx", rate),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = speedDropdownExpanded,
                        onDismissRequest = { speedDropdownExpanded = false },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text(
                            text = "Playback Speed",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider()
                        
                        // Fine adjustments row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onRateChange((rate - 0.05f).coerceIn(0.5f, 3.0f)) }) {
                                Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format(Locale.US, "%.2fx", rate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onRateChange((rate + 0.05f).coerceIn(0.5f, 3.0f)) }) {
                                Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider()
                        
                        // Preset options
                        listOf(0.8f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = {
                                    onRateChange(speed)
                                    speedDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Right Bookmark button
                IconButton(onClick = onToggleBookmark) {
                    Text(
                        text = if (isBookmarked) "🔖" else "🏷",
                        fontSize = 24.sp
                    )
                }
            }

            // 5. Main Playback Controls (Vector Icons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioModeButton(
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "Previous sentence",
                    size = 72.dp,
                    iconSize = 32.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = contentColor,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPrevious()
                    }
                )

                AudioModeButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    size = 88.dp,
                    iconSize = 44.dp,
                    color = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPlayPause()
                    }
                )

                AudioModeButton(
                    icon = Icons.Default.SkipNext,
                    contentDescription = "Next sentence",
                    size = 72.dp,
                    iconSize = 32.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = contentColor,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNext()
                    }
                )
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.US, "%02d:%02d", m, s)
    }
}

@Composable
fun AudioModeButton(
    icon: ImageVector,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "audioModeButtonScale"
    )

    Surface(
        shape = CircleShape,
        color = color,
        contentColor = contentColor,
        modifier = Modifier
            .size(size)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = contentColor
            )
        }
    }
}
