package com.veritas.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ActualDocumentView(
    document: SavedDocument,
    repository: DocumentRepository,
    readingProgress: Float,
    isPlaying: Boolean,
    statusMessage: String,
    queueCount: Int,
    rate: Float,
    pitch: Float,
    fontSizeSp: Int,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPageChanged: (Int, Int) -> Unit,
    onOpenExternal: () -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onOpenVoiceStudio: () -> Unit,
    voices: List<TtsVoiceOption>,
    voiceSettings: VoiceSettings,
    onVoiceSelected: (TtsVoiceOption) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val original = repository.originalUri(document)
    var pageIndex by remember(document.id) { mutableIntStateOf(0) }
    var pageCount by remember(document.id) { mutableIntStateOf(1) }
    var bitmap by remember(document.id) { mutableStateOf<Bitmap?>(null) }
    var message by remember(document.id) { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var zoomScale by remember(document.id) { mutableFloatStateOf(1f) }
    var zoomOffset by remember(document.id) { mutableStateOf(Offset.Zero) }
    var rotationDegrees by remember(document.id) { mutableIntStateOf(0) }

    val isPdf = remember(document) {
        if (document.originalMimeType.contains("pdf", ignoreCase = true) ||
            document.originalFileName.endsWith(".pdf", ignoreCase = true) ||
            document.title.lowercase().contains(".pdf")
        ) {
            true
        } else {
            val file = repository.originalFile(document)
            if (file != null && file.exists()) {
                runCatching {
                    file.inputStream().use { input ->
                        val bytes = ByteArray(4)
                        val read = input.read(bytes)
                        read == 4 && bytes[0] == '%'.code.toByte() && bytes[1] == 'P'.code.toByte() && bytes[2] == 'D'.code.toByte() && bytes[3] == 'F'.code.toByte()
                    }
                }.getOrDefault(false)
            } else if (document.originalFileName.startsWith("content://")) {
                runCatching {
                    (context.contentResolver.openInputStream(android.net.Uri.parse(document.originalFileName))?.use { input ->
                        val bytes = ByteArray(4)
                        val read = input.read(bytes)
                        read == 4 && bytes[0] == '%'.code.toByte() && bytes[1] == 'P'.code.toByte() && bytes[2] == 'D'.code.toByte() && bytes[3] == 'F'.code.toByte()
                    } ?: false)
                }.getOrDefault(false)
            } else {
                false
            }
        }
    }

    val isImage = remember(document, isPdf) {
        if (isPdf) {
            false
        } else if (document.originalMimeType.startsWith("image/") ||
            listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp").any {
                document.originalFileName.endsWith(it, ignoreCase = true)
            }
        ) {
            true
        } else {
            val file = repository.originalFile(document)
            if (file != null && file.exists()) {
                runCatching {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    options.outWidth > 0 && options.outHeight > 0
                }.getOrDefault(false)
            } else if (document.originalFileName.startsWith("content://")) {
                runCatching {
                    (context.contentResolver.openInputStream(android.net.Uri.parse(document.originalFileName))?.use { input ->
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeStream(input, null, options)
                        options.outWidth > 0 && options.outHeight > 0
                    } ?: false)
                }.getOrDefault(false)
            } else {
                false
            }
        }
    }

    LaunchedEffect(original?.toString(), pageIndex) {
        bitmap = null
        message = null
        if (original == null) {
            message = "No stored original is available for this reading. Re-import the file to enable Original View."
            return@LaunchedEffect
        }

        if (isPdf) {
            val rendered = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openFileDescriptor(original, "r")?.use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            val count = renderer.pageCount.coerceAtLeast(1)
                            val safePage = pageIndex.coerceIn(0, count - 1)
                            renderer.openPage(safePage).use { page ->
                                val targetWidth = 1500
                                val scale = (targetWidth.toFloat() / page.width.toFloat()).coerceIn(1f, 4f)
                                val width = (page.width * scale).toInt().coerceAtLeast(1)
                                val height = (page.height * scale).toInt().coerceAtLeast(1)
                                val output = createBitmap(width, height)
                                Canvas(output).drawColor(AndroidColor.WHITE)
                                page.render(output, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                RenderedPage(count, output)
                            }
                        }
                    } ?: throw IllegalStateException("Could not open PDF descriptor")
                }
            }
            rendered.onSuccess {
                pageCount = it.pageCount
                if (pageIndex > it.pageCount - 1) pageIndex = it.pageCount - 1
                bitmap = it.bitmap
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                message = "Could not render this PDF page: ${e.message ?: "unknown error"}"
            }
        } else if (isImage) {
            val decoded = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(original)?.use { input ->
                        BitmapFactory.decodeStream(input)
                    } ?: throw IllegalStateException("Could not open image stream")
                }
            }
            decoded.onSuccess { image ->
                bitmap = image
                pageCount = 1
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                message = "Could not open this image: ${e.message ?: "unknown error"}"
            }
        } else {
            message = "This file type is preserved as an original document, but Veritas cannot render it in-app yet. Use Open original from the menu."
        }
    }

    LaunchedEffect(readingProgress, pageCount) {
        if (isPdf && pageCount > 1) {
            val syncedPage = (readingProgress.coerceIn(0f, 1f) * (pageCount - 1)).roundToInt().coerceIn(0, pageCount - 1)
            if (syncedPage != pageIndex) pageIndex = syncedPage
        }
    }

    LaunchedEffect(document.id, pageIndex) {
        zoomScale = 1f
        zoomOffset = Offset.Zero
    }

    fun selectPage(target: Int) {
        if (!isPdf || pageCount <= 1) return
        val safeTarget = target.coerceIn(0, pageCount - 1)
        if (safeTarget != pageIndex) {
            pageIndex = safeTarget
            onPageChanged(safeTarget, pageCount)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isPdf) "Page ${pageIndex + 1} / $pageCount" else "Original View",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isPdf && pageCount > 1) {
                    SlimPageSlider(
                        pageIndex = pageIndex,
                        pageCount = pageCount,
                        onPageSelected = ::selectPage,
                        modifier = Modifier.fillMaxWidth().height(22.dp)
                    )
                } else {
                    Text(
                        text = document.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = {
                    rotationDegrees = (rotationDegrees + 90) % 360
                    zoomScale = 1f
                    zoomOffset = Offset.Zero
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = "Rotate",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Open original") },
                        enabled = original != null,
                        onClick = {
                            showMenu = false
                            onOpenExternal()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Back to extracted text") },
                        onClick = {
                            showMenu = false
                            onClose()
                        }
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val viewportWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val viewportHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
            fun clampOffset(offset: Offset, scale: Float): Offset {
                val xLimit = viewportWidthPx * (scale - 1f) / 2f
                val yLimit = viewportHeightPx * (scale - 1f) / 2f
                return if (scale <= 1.01f) {
                    Offset.Zero
                } else {
                    Offset(offset.x.coerceIn(-xLimit, xLimit), offset.y.coerceIn(-yLimit, yLimit))
                }
            }
            fun setZoom(nextScale: Float, nextOffset: Offset = zoomOffset) {
                val safeScale = nextScale.coerceIn(MIN_CANVAS_ZOOM, MAX_CANVAS_ZOOM)
                zoomScale = safeScale
                zoomOffset = clampOffset(nextOffset, safeScale)
            }

            val image = bitmap
            when {
                image != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = "Rendered original document",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                    rotationZ = rotationDegrees.toFloat()
                                    translationX = zoomOffset.x
                                    translationY = zoomOffset.y
                                }
                                .pointerInput(isPdf, pageIndex, pageCount, viewportWidthPx, viewportHeightPx) {
                                    awaitEachGesture {
                                        var dragTotal = 0f
                                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                                        do {
                                            val event = awaitPointerEvent()
                                            val pressedCount = event.changes.count { it.pressed }
                                            if (pressedCount > 1) {
                                                val nextScale = (zoomScale * event.calculateZoom()).coerceIn(MIN_CANVAS_ZOOM, MAX_CANVAS_ZOOM)
                                                val nextOffset = zoomOffset + event.calculatePan()
                                                setZoom(nextScale, nextOffset)
                                                event.changes.forEach { change -> if (change.pressed) change.consume() }
                                                dragTotal = 0f
                                            } else {
                                                val change = event.changes.firstOrNull { it.id == firstDown.id }
                                                    ?: event.changes.firstOrNull()
                                                if (change != null && change.pressed) {
                                                    val delta = change.positionChange()
                                                    if (zoomScale > 1.01f) {
                                                        zoomOffset = clampOffset(zoomOffset + delta, zoomScale)
                                                        change.consume()
                                                    } else {
                                                        dragTotal += delta.x
                                                    }
                                                }
                                            }
                                        } while (event.changes.any { it.pressed })

                                        if (zoomScale <= 1.01f) {
                                            when {
                                                dragTotal < -80f -> selectPage(pageIndex + 1)
                                                dragTotal > 80f -> selectPage(pageIndex - 1)
                                            }
                                        }
                                    }
                                },
                            contentScale = ContentScale.Fit
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                            tonalElevation = 4.dp,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                TextButton(
                                    onClick = { setZoom(zoomScale / CANVAS_ZOOM_STEP) },
                                    enabled = zoomScale > MIN_CANVAS_ZOOM + 0.01f
                                ) { Text("−") }
                                TextButton(onClick = { setZoom(1f, Offset.Zero) }) {
                                    Text("${(zoomScale * 100f).roundToInt()}%")
                                }
                                TextButton(
                                    onClick = { setZoom(zoomScale * CANVAS_ZOOM_STEP) },
                                    enabled = zoomScale < MAX_CANVAS_ZOOM - 0.01f
                                ) { Text("+") }
                            }
                        }
                    }
                }
                message == null && (isPdf || isImage) -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(26.dp))
                        Text("Rendering original…")
                    }
                }
                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Original View", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Text(message.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = onOpenExternal, enabled = original != null) { Text("Open original") }
                                TextButton(onClick = onClose) { Text("Extracted text") }
                            }
                        }
                    }
                }
            }
        }
        DocPlayerPanel(
            isPlaying = isPlaying,
            statusMessage = statusMessage,
            rate = rate,
            pitch = pitch,
            fontSizeSp = fontSizeSp,
            queueCount = queueCount,
            canGoPrevious = canGoPrevious,
            canGoNext = canGoNext,
            onPrevious = onPrevious,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onRateChange = onRateChange,
            onPitchChange = onPitchChange,
            onFontSizeChange = onFontSizeChange,
            onOpenVoiceStudio = onOpenVoiceStudio,
            voices = voices,
            voiceSettings = voiceSettings,
            onVoiceSelected = onVoiceSelected
        )
    }
}

private data class RenderedPage(
    val pageCount: Int,
    val bitmap: Bitmap
)

private enum class DocPanelDragValue { Collapsed, Expanded }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocPlayerPanel(
    isPlaying: Boolean,
    statusMessage: String,
    rate: Float,
    pitch: Float,
    fontSizeSp: Int,
    queueCount: Int,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onOpenVoiceStudio: () -> Unit,
    voices: List<TtsVoiceOption>,
    voiceSettings: VoiceSettings,
    onVoiceSelected: (TtsVoiceOption) -> Unit
) {
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { 152.dp.toPx() }

    @Suppress("DEPRECATION")
    val draggableState = remember {
        AnchoredDraggableState(
            initialValue = DocPanelDragValue.Collapsed,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            decayAnimationSpec = androidx.compose.animation.core.exponentialDecay()
        )
    }
    val anchors = remember(maxOffsetPx) {
        DraggableAnchors {
            DocPanelDragValue.Collapsed at maxOffsetPx
            DocPanelDragValue.Expanded at 0f
        }
    }
    SideEffect { draggableState.updateAnchors(anchors) }

    val currentOffset = if (draggableState.offset.isNaN()) maxOffsetPx else draggableState.requireOffset()
    val progress = (1f - (currentOffset / maxOffsetPx)).coerceIn(0f, 1f)
    val heightDp = 72.dp + (152.dp * progress)

    val coroutineScope = rememberCoroutineScope()

    val availableVoices = remember(voices, voiceSettings.localeTag) {
        if (voiceSettings.localeTag.isBlank()) voices.take(8)
        else voices.filter { it.localeTag.equals(voiceSettings.localeTag, ignoreCase = true) }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(heightDp)
            .anchoredDraggable(
                state = draggableState,
                orientation = Orientation.Vertical
            ),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row — visible in both Collapsed and Expanded state
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tap to toggle expand / collapse
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            draggableState.animateTo(
                                if (draggableState.currentValue == DocPanelDragValue.Collapsed)
                                    DocPanelDragValue.Expanded
                                else
                                    DocPanelDragValue.Collapsed
                            )
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.veritas_reader_icon),
                            contentDescription = "Veritas",
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Playback controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Previous",
                            tint = if (canGoPrevious) MaterialTheme.colorScheme.onSurface
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    FilledTonalIconButton(onClick = onPlayPause) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                    IconButton(
                        onClick = onNext,
                        enabled = canGoNext
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "Next",
                            tint = if (canGoNext) MaterialTheme.colorScheme.onSurface
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }

                // Voice Studio shortcut (visible in header)
                TextButton(
                    onClick = onOpenVoiceStudio,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Voice ›", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Expanded area — Speed, Pitch, Font, Voice picker
            if (progress > 0.05f) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer { alpha = progress }
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        statusMessage.ifBlank { if (isPlaying) "Now reading" else "Original View" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // Speed row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Speed ${"%.2f".format(rate)}x",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { onRateChange((rate - 0.05f).coerceIn(0.5f, 2.0f)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                            TextButton(
                                onClick = { onRateChange((rate + 0.05f).coerceIn(0.5f, 2.0f)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    Slider(
                        value = rate,
                        onValueChange = onRateChange,
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Pitch & Font Size row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pitch ${"%.2f".format(pitch)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = pitch,
                                onValueChange = onPitchChange,
                                valueRange = 0.7f..1.4f
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Text size ${fontSizeSp}sp",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
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
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(voice.name, style = MaterialTheme.typography.labelMedium)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onVoiceSelected(voice) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(voice.name, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Full Voice Studio link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
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

@Composable
private fun SlimPageSlider(
    pageIndex: Int,
    pageCount: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier
            .pointerInput(pageCount) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    onPageSelected(((offset.x / width).coerceIn(0f, 1f) * (pageCount - 1)).roundToInt())
                }
            }
            .pointerInput(pageCount) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        onPageSelected(((offset.x / width).coerceIn(0f, 1f) * (pageCount - 1)).roundToInt())
                    },
                    onDrag = { change, _ ->
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        onPageSelected(((change.position.x / width).coerceIn(0f, 1f) * (pageCount - 1)).roundToInt())
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val progress = (pageIndex.toFloat() / (pageCount - 1).toFloat()).coerceIn(0f, 1f)
        val thumbSize = 16.dp
        val thumbPx = with(density) { thumbSize.toPx() }
        val trackWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(thumbPx)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f), androidx.compose.foundation.shape.CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(4.dp)
                .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(((trackWidthPx - thumbPx) * progress).roundToInt(), 0) }
                .size(thumbSize)
                .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
        )
    }
}

@Composable
private fun CanvasControlButton(
    text: String,
    enabled: Boolean = true,
    prominent: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "canvasControlBounce"
    )
    if (prominent) {
        Button(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = androidx.compose.foundation.shape.CircleShape,
            contentPadding = ButtonDefaults.ContentPadding,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            Text(text)
        }
    } else {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            Text(text)
        }
    }
}

private const val MIN_CANVAS_ZOOM = 1f
private const val MAX_CANVAS_ZOOM = 4f
private const val CANVAS_ZOOM_STEP = 1.25f
