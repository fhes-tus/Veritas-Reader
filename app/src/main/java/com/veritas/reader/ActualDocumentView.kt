package com.veritas.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
internal fun ActualDocumentView(
    document: SavedDocument,
    repository: DocumentRepository,
    readingProgress: Float,
    isPlaying: Boolean,
    queueCount: Int,
    rate: Float,
    pitch: Float,
    fontSizeSp: Int,
    onPlayPause: () -> Unit,
    onPageChanged: (Int, Int) -> Unit,
    onOpenExternal: () -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onOpenVoiceStudio: () -> Unit,
    onClose: () -> Unit
) {
    val original = repository.originalFile(document)
    var pageIndex by remember(document.id) { mutableIntStateOf(0) }
    var pageCount by remember(document.id) { mutableIntStateOf(1) }
    var bitmap by remember(document.id) { mutableStateOf<Bitmap?>(null) }
    var message by remember(document.id) { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showBottomMenu by remember { mutableStateOf(false) }
    var zoomScale by remember(document.id) { mutableFloatStateOf(1f) }
    var zoomOffset by remember(document.id) { mutableStateOf(Offset.Zero) }
    var rotationDegrees by remember(document.id) { mutableIntStateOf(0) }

    val isPdf = document.originalMimeType.contains("pdf", ignoreCase = true) ||
        document.originalFileName.endsWith(".pdf", ignoreCase = true)
    val isImage = document.originalMimeType.startsWith("image/") ||
        listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp").any {
            document.originalFileName.endsWith(it, ignoreCase = true)
        }

    LaunchedEffect(original?.absolutePath, pageIndex) {
        bitmap = null
        message = null
        if (original == null) {
            message = "No stored original is available for this reading. Re-import the file to enable actual document view."
            return@LaunchedEffect
        }

        if (isPdf) {
            val rendered = withContext(Dispatchers.IO) {
                runCatching {
                    android.os.ParcelFileDescriptor.open(original, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
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
                    }
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
                runCatching { BitmapFactory.decodeFile(original.absolutePath) }
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
            TextButton(onClick = onClose) { Text("←", style = MaterialTheme.typography.headlineSmall) }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isPdf) "Page ${pageIndex + 1} / $pageCount" else "Actual document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
                        document.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(
                onClick = {
                    rotationDegrees = (rotationDegrees + 90) % 360
                    zoomScale = 1f
                    zoomOffset = Offset.Zero
                }
            ) { Text("⟳", style = MaterialTheme.typography.headlineSmall) }
            Box {
                TextButton(onClick = { showMenu = true }) { Text("⋮", style = MaterialTheme.typography.headlineSmall) }
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
                        Text("Rendering actual document…")
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
                            Text("Actual document", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)),
                            androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.veritas_reader_icon),
                        contentDescription = "Veritas",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                CanvasControlButton(
                    text = "‹",
                    enabled = isPdf && pageIndex > 0,
                    onClick = { selectPage(pageIndex - 1) }
                )
                CanvasControlButton(
                    text = if (isPlaying) "Ⅱ" else "▶",
                    prominent = true,
                    onClick = onPlayPause
                )
                CanvasControlButton(
                    text = "›",
                    enabled = isPdf && pageIndex < pageCount - 1,
                    onClick = { selectPage(pageIndex + 1) }
                )
                Box {
                    TextButton(onClick = { showBottomMenu = true }) { Text("⋮") }
                    DropdownMenu(
                        expanded = showBottomMenu,
                        onDismissRequest = { showBottomMenu = false },
                        modifier = Modifier.width(300.dp)
                    ) {
                        Text(
                            "Playback",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Tune the reading voice for the actual document view.",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            "Speed ${"%.2f".format(rate)}x",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = rate,
                            onValueChange = onRateChange,
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            "Pitch ${"%.2f".format(pitch)}",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = pitch,
                            onValueChange = onPitchChange,
                            valueRange = 0.7f..1.4f,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("Voice and language") },
                            onClick = {
                                showBottomMenu = false
                                onOpenVoiceStudio()
                            }
                        )
                    }
                }
                if (queueCount > 0) {
                    Text("$queueCount", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private data class RenderedPage(
    val pageCount: Int,
    val bitmap: Bitmap
)

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
