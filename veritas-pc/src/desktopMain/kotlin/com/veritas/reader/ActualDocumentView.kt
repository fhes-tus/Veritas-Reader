package com.veritas.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import androidx.compose.material.icons.filled.Book
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
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import com.veritas.reader.ui.screens.KeepScreenAwake
import com.veritas.reader.ui.screens.monitorReadingActivity

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
    var topBarVisible by remember { mutableStateOf(true) }
    var bottomBarVisible by remember { mutableStateOf(true) }
    var interactionTrigger by remember { mutableStateOf(0L) }
    KeepScreenAwake(enabled = true, interactionTrigger = interactionTrigger)

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
                    BitmapFactory.decodeFile(file.absolutePath) != null
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
                    val file = File(original.path ?: original.toString())
                    org.apache.pdfbox.pdmodel.PDDocument.load(file).use { doc ->
                        val count = doc.numberOfPages.coerceAtLeast(1)
                        val safePage = pageIndex.coerceIn(0, count - 1)
                        val renderer = org.apache.pdfbox.rendering.PDFRenderer(doc)
                        val bufferedImage = renderer.renderImageWithDPI(safePage, 150f)
                        
                        val tempFile = File.createTempFile("veritas_pdf_page_", ".png")
                        tempFile.deleteOnExit()
                        javax.imageio.ImageIO.write(bufferedImage, "PNG", tempFile)
                        val output = BitmapFactory.decodeFile(tempFile.absolutePath) ?: throw IllegalStateException("Failed to decode rendered PDF page")
                        tempFile.delete()
                        RenderedPage(count, output)
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
                runCatching {
                    val path = original.path ?: original.toString()
                    BitmapFactory.decodeFile(path) ?: throw IllegalStateException("Could not decode image file")
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

    val topBarOffset by animateFloatAsState(
        targetValue = if (topBarVisible) 0f else -300f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "topBarOffset"
    )
    val bottomBarOffset by animateFloatAsState(
        targetValue = if (bottomBarVisible) 0f else 400f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bottomBarOffset"
    )

    val activeBg = MaterialTheme.colorScheme.background
    val isThemeDark = remember(activeBg) {
        val bg = activeBg
        (0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue) < 0.5f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isThemeDark) androidx.compose.ui.graphics.Color(0xFF1C1C1E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
            .monitorReadingActivity { interactionTrigger = System.currentTimeMillis() }
    ) {
        // 1. Full-screen rendering canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val viewportWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val viewportHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
            fun clampOffset(offset: Offset, scale: Float): Offset {
                val maxOffsetX = (viewportWidthPx * (scale - 1f)) / 2f
                val maxOffsetY = (viewportHeightPx * (scale - 1f)) / 2f
                return Offset(
                    offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                    offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                )
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
                            colorFilter = if (isThemeDark) {
                                // True dark mode (not a plain colour inversion): softly inverts
                                // BRIGHTNESS while PRESERVING HUE. White paper maps to ~#242424
                                // and black text to ~#EBEBEB, but a red heading stays reddish and
                                // a blue link stays bluish instead of flipping to cyan/orange.
                                // Derived from (soft-invert ∘ 180° hue-rotation about luminance).
                                ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                                     0.4477f, -1.1154f, -0.1123f, 0.0f, 235.0f,
                                    -0.3323f, -0.3354f, -0.1123f, 0.0f, 235.0f,
                                    -0.3323f, -1.1154f,  0.6677f, 0.0f, 235.0f,
                                     0.0f,     0.0f,     0.0f,    1.0f,   0.0f
                                )))
                            } else null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                    rotationZ = rotationDegrees.toFloat()
                                    translationX = zoomOffset.x
                                    translationY = zoomOffset.y
                                }
                                .pointerInput(isPdf, pageIndex, pageCount, viewportWidthPx, viewportHeightPx, zoomScale) {
                                    awaitEachGesture {
                                        var dragTotalX = 0f
                                        var dragTotalY = 0f
                                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                                        val startY = firstDown.position.y
                                        do {
                                            val event = awaitPointerEvent()
                                            val pressedCount = event.changes.count { it.pressed }
                                            if (pressedCount > 1) {
                                                val nextScale = (zoomScale * event.calculateZoom()).coerceIn(MIN_CANVAS_ZOOM, MAX_CANVAS_ZOOM)
                                                val nextOffset = zoomOffset + event.calculatePan()
                                                setZoom(nextScale, nextOffset)
                                                event.changes.forEach { change -> if (change.pressed) change.consume() }
                                                dragTotalX = 0f
                                                dragTotalY = 0f
                                            } else {
                                                val change = event.changes.firstOrNull { it.id == firstDown.id }
                                                    ?: event.changes.firstOrNull()
                                                if (change != null && change.pressed) {
                                                    val delta = change.positionChange()
                                                    if (zoomScale > 1.01f) {
                                                        zoomOffset = clampOffset(zoomOffset + delta, zoomScale)
                                                        change.consume()
                                                    } else {
                                                        dragTotalX += delta.x
                                                        dragTotalY += delta.y
                                                        if (Math.abs(dragTotalY) > 20f && Math.abs(dragTotalY) > Math.abs(dragTotalX)) {
                                                            change.consume()
                                                        }
                                                    }
                                                }
                                            }
                                        } while (event.changes.any { it.pressed })

                                        if (zoomScale <= 1.01f) {
                                            if (Math.abs(dragTotalY) > 50f && Math.abs(dragTotalY) > Math.abs(dragTotalX)) {
                                                val isDragUp = dragTotalY < 0
                                                if (startY < viewportHeightPx / 2) {
                                                    topBarVisible = !isDragUp
                                                } else {
                                                    bottomBarVisible = isDragUp
                                                }
                                            } else {
                                                when {
                                                    dragTotalX < -80f -> selectPage(pageIndex + 1)
                                                    dragTotalX > 80f -> selectPage(pageIndex - 1)
                                                }
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
                                    Text(
                                        "${(zoomScale * 100).roundToInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                else -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚠️ original document view is unavailable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = message ?: "Unrecognized document format.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = onOpenExternal, enabled = original != null) { Text("Open original") }
                                TextButton(onClick = onClose) { Text("Extracted text") }
                            }
                        }
                    }
                }
            }
        }

        // 2. Floating Top app bar
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer { translationY = topBarOffset }
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isPdf) "Page ${pageIndex + 1} / $pageCount" else "Original View",
                    style = MaterialTheme.typography.titleSmall,
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
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = "Rotate",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
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

        // 3. Floating Bottom Player Panel
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer { translationY = bottomBarOffset }
        ) {
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
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Veritas",
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                            color = MaterialTheme.colorScheme.onSurface,
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
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                            activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
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
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = pitch,
                                onValueChange = onPitchChange,
                                valueRange = 0.7f..1.4f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                                    activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                    inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Text size ${fontSizeSp}sp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = fontSizeSp.toFloat(),
                                onValueChange = { onFontSizeChange(it.toInt().coerceIn(14, 28)) },
                                valueRange = 14f..28f,
                                steps = 13,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                                    activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                    inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
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
