package com.veritas.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import com.veritas.reader.ui.screens.KeepScreenAwake
import com.veritas.reader.ui.rememberSliderHaptics
import com.veritas.reader.ui.screens.monitorReadingActivity

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ActualDocumentView(
    document: SavedDocument,
    repository: DocumentRepository,
    /** 1-based page the active sentence sits on; 0 when it is not known yet. */
    activeSentencePage: Int,
    /** Text of the sentence being spoken, used to highlight the matching line. */
    activeSentenceText: String,
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
    /** Selected text plus the 1-based page it was selected on, so the match can be scoped. */
    onReadFromSentence: ((String, Int) -> Unit)? = null,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val original = repository.originalUri(document)
    // Opening Original view used to discard where the reader actually was and start at
    // page 1. Seed from the active sentence's own page instead.
    var pageIndex by remember(document.id) {
        mutableIntStateOf((activeSentencePage - 1).coerceAtLeast(0))
    }
    var pageCount by remember(document.id) { mutableIntStateOf(1) }
    var bitmap by remember(document.id) { mutableStateOf<Bitmap?>(null) }
    var message by remember(document.id) { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var zoomScale by remember(document.id) { mutableFloatStateOf(1f) }
    var zoomOffset by remember(document.id) { mutableStateOf(Offset.Zero) }
    var rotationDegrees by remember(document.id) { mutableIntStateOf(0) }
    var pageTurnDirection by remember(document.id) { mutableIntStateOf(0) }
    var topBarVisible by remember { mutableStateOf(!isLandscape) }
    var bottomBarVisible by remember { mutableStateOf(!isLandscape) }
    var paperToneMode by remember { mutableStateOf(PaperToneMode.ACTIVE_THEME) }
    var selectedCanvasText by remember { mutableStateOf<String?>(null) }
    var showJumpToPageDialog by remember { mutableStateOf(false) }
    var showDocInfoDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }
    var interactionTrigger by remember { mutableStateOf(0L) }
    KeepScreenAwake(enabled = true, interactionTrigger = interactionTrigger)

    // Bars retire on their own after a quiet spell so the page owns the screen while reading.
    // Keyed on interactionTrigger, which monitorReadingActivity bumps on any touch, so each
    // touch restarts this countdown rather than stacking another one behind it. Tapping the
    // page brings them back. Held open while a menu, dialog or text selection is up: those are
    // all driven from the bars, and collapsing underneath them would strand the user again.
    LaunchedEffect(
        interactionTrigger,
        topBarVisible,
        bottomBarVisible,
        showMenu,
        showJumpToPageDialog,
        showDocInfoDialog,
        selectedCanvasText
    ) {
        val overlayOpen = showMenu ||
            showJumpToPageDialog ||
            showDocInfoDialog ||
            selectedCanvasText != null
        if ((topBarVisible || bottomBarVisible) && !overlayOpen) {
            kotlinx.coroutines.delay(BARS_AUTO_HIDE_MS)
            topBarVisible = false
            bottomBarVisible = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            (context as? android.app.Activity)?.requestedOrientation =
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

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

    val isPresentation = remember(document, isPdf, isImage) {
        if (isPdf || isImage) false
        else document.sourceLabel == "PPTX" || document.sourceLabel == "PPT" ||
                document.originalFileName.endsWith(".pptx", ignoreCase = true) ||
                document.originalFileName.endsWith(".ppt", ignoreCase = true) ||
                document.originalMimeType.contains("presentationml") ||
                document.originalMimeType.contains("powerpoint")
    }

    val isEpub = remember(document, isPdf, isImage, isPresentation) {
        if (isPdf || isImage || isPresentation) false
        else document.sourceLabel == "EPUB" ||
                document.originalFileName.endsWith(".epub", ignoreCase = true) ||
                document.originalMimeType.contains("epub")
    }

    val isDocx = remember(document, isPdf, isImage, isPresentation, isEpub) {
        if (isPdf || isImage || isPresentation || isEpub) false
        else document.sourceLabel == "DOCX" ||
                document.originalFileName.endsWith(".docx", ignoreCase = true) ||
                document.originalMimeType.contains("wordprocessingml")
    }

    var pptxDeck by remember { mutableStateOf<PptxDeck?>(null) }
    var currentSlideImages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var showSpeakerNotes by remember { mutableStateOf(false) }
    var epubBook by remember { mutableStateOf<EpubBook?>(null) }
    var docxDoc by remember { mutableStateOf<DocxDocument?>(null) }

    // Parsing belongs to the document, not the page. This all used to live in the page-keyed
    // effect below, so every swipe re-read the whole file from disk and re-parsed the entire
    // deck, book or document. For slides it rebuilt the embedded images too, which is exactly
    // why they blinked away and back on each turn.
    LaunchedEffect(original?.toString()) {
        message = null
        if (original == null) {
            message = "No stored original is available for this reading. Re-import the file to enable Original View."
            return@LaunchedEffect
        }
        if (isPresentation) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = context.contentResolver.openInputStream(original)?.use { it.readBytes() }
                        ?: throw IllegalStateException("Could not read presentation file")
                    if (PptLegacyExtractor.isPptFile(bytes)) {
                        val body = PptLegacyExtractor.extract(bytes)
                        val lines = body.text.lines()
                        val slides = mutableListOf<PptxSlideContent>()
                        var currentSlideNum = 1
                        var currentLines = mutableListOf<String>()
                        for (line in lines) {
                            if (line.startsWith("[[VERITAS_PAGE:")) {
                                if (currentLines.isNotEmpty()) {
                                    val title = currentLines.firstOrNull().orEmpty()
                                    val content = currentLines.drop(1)
                                    slides.add(PptxSlideContent(currentSlideNum, listOf(title), content, emptyList(), emptyList()))
                                    currentSlideNum++
                                    currentLines = mutableListOf()
                                }
                            } else if (line.isNotBlank()) {
                                currentLines.add(line)
                            }
                        }
                        if (currentLines.isNotEmpty()) {
                            val title = currentLines.firstOrNull().orEmpty()
                            val content = currentLines.drop(1)
                            slides.add(PptxSlideContent(currentSlideNum, listOf(title), content, emptyList(), emptyList()))
                        }
                        val finalSlides = if (slides.isNotEmpty()) slides else listOf(PptxSlideContent(1, listOf(document.title), lines.filter { it.isNotBlank() }, emptyList(), emptyList()))
                        PptxDeck(slides = finalSlides, slideCount = finalSlides.size)
                    } else {
                        PptxExtractor.parseDeck(bytes, includeSpeakerNotes = true)
                    }
                }
            }
            loaded.onSuccess { deck ->
                pptxDeck = deck
                pageCount = deck.slideCount.coerceAtLeast(1)
                if (pageIndex > pageCount - 1) pageIndex = pageCount - 1
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                message = "Could not load presentation slides: ${e.message ?: "unknown error"}"
            }
        } else if (isEpub) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = context.contentResolver.openInputStream(original)?.use { it.readBytes() }
                        ?: throw IllegalStateException("Could not read EPUB file")
                    EpubDocumentParser.parse(bytes, document.title)
                }
            }
            loaded.onSuccess { book ->
                epubBook = book
                pageCount = book.totalChapters.coerceAtLeast(1)
                if (pageIndex > pageCount - 1) pageIndex = pageCount - 1
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                message = "Could not parse EPUB book: ${e.message ?: "unknown error"}"
            }
        } else if (isDocx) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = context.contentResolver.openInputStream(original)?.use { it.readBytes() }
                        ?: throw IllegalStateException("Could not read DOCX file")
                    DocxDocumentParser.parse(bytes, document.title)
                }
            }
            loaded.onSuccess { doc ->
                docxDoc = doc
                pageCount = doc.totalPages.coerceAtLeast(1)
                if (pageIndex > pageCount - 1) pageIndex = pageCount - 1
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                message = "Could not parse Word document: ${e.message ?: "unknown error"}"
            }
        } else if (!isPdf && !isImage) {
            message = "This file type is preserved as an original document, but Veritas cannot render it in-app yet. Use Open original from the menu."
        }
    }

    // Page-scoped work only: the rendered PDF page or the decoded image.
    LaunchedEffect(original?.toString(), pageIndex) {
        if (original == null) return@LaunchedEffect
        if (isPdf || isImage) bitmap = null
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
        }
    }

    // Slide images are page work and are re-read per slide rather than held for the whole
    // deck, which would pin every bitmap in memory at once. Cleared first so the previous
    // slide's pictures never sit underneath the new one while this loads.
    LaunchedEffect(original?.toString(), pageIndex, pptxDeck) {
        if (original == null || !isPresentation) return@LaunchedEffect
        currentSlideImages = emptyList()
        val deck = pptxDeck ?: return@LaunchedEffect
        val slide = deck.slides.getOrNull(pageIndex.coerceIn(0, (deck.slideCount - 1).coerceAtLeast(0)))
            ?: return@LaunchedEffect
        val images = withContext(Dispatchers.IO) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(original)?.use { it.readBytes() }
                    ?: return@runCatching emptyList<Bitmap>()
                PptxExtractor.extractSlideImages(bytes, slide).mapNotNull { imgBytes ->
                    BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
                }
            }.getOrDefault(emptyList())
        }
        currentSlideImages = images
    }

    LaunchedEffect(activeSentencePage, pageCount) {
        // Sentences are not spread evenly across pages, so scaling reading progress by page
        // count drifted further the longer the document ran. The sentence carries its own
        // page number; use it.
        if ((isPdf || isPresentation || isEpub || isDocx) && pageCount > 1 && activeSentencePage > 0) {
            val syncedPage = (activeSentencePage - 1).coerceIn(0, pageCount - 1)
            if (syncedPage != pageIndex) {
                pageTurnDirection = if (syncedPage > pageIndex) 1 else -1
                pageIndex = syncedPage
            }
        }
    }

    LaunchedEffect(document.id, pageIndex) {
        zoomScale = 1f
        zoomOffset = Offset.Zero
    }

    fun selectPage(target: Int) {
        if ((!isPdf && !isPresentation && !isEpub && !isDocx) || pageCount <= 1) return
        val safeTarget = target.coerceIn(0, pageCount - 1)
        if (safeTarget != pageIndex) {
            pageTurnDirection = if (safeTarget > pageIndex) 1 else -1
            pageIndex = safeTarget
            onPageChanged(safeTarget, pageCount)
        }
    }

    // Page-turn entrance: when a freshly rendered page arrives, it slides in a little
    // from the swipe direction while fading, instead of hard-cutting. 0 = just arrived.
    val pageEnter = remember { Animatable(1f) }
    LaunchedEffect(bitmap) {
        if (bitmap != null) {
            pageEnter.snapTo(0f)
            pageEnter.animateTo(1f, tween(durationMillis = 260, easing = LinearOutSlowInEasing))
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

    val colorScheme = MaterialTheme.colorScheme
    val activeBg = colorScheme.background
    val activeSurface = colorScheme.surface
    val activeOnSurface = colorScheme.onSurface
    val isThemeDark = remember(activeBg) {
        val bg = activeBg
        (0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue) < 0.5f
    }

    // Dynamic theme palette ColorMatrix: Active Theme maps white paper to theme's dark surface,
    // Dark mode maps to high contrast inverted paper, and Natural White disables filter.
    val darkThemeColorFilter = remember(activeSurface, activeOnSurface, isThemeDark, paperToneMode) {
        when (paperToneMode) {
            PaperToneMode.ACTIVE_THEME -> {
                if (isThemeDark) {
                    val bgR = activeSurface.red
                    val bgG = activeSurface.green
                    val bgB = activeSurface.blue
                    val fgR = activeOnSurface.red
                    val fgG = activeOnSurface.green
                    val fgB = activeOnSurface.blue

                    val deltaR = bgR - fgR
                    val deltaG = bgG - fgG
                    val deltaB = bgB - fgB

                    ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                        0.299f * deltaR, 0.587f * deltaR, 0.114f * deltaR, 0.0f, fgR * 255.0f,
                        0.299f * deltaG, 0.587f * deltaG, 0.114f * deltaG, 0.0f, fgG * 255.0f,
                        0.299f * deltaB, 0.587f * deltaB, 0.114f * deltaB, 0.0f, fgB * 255.0f,
                        0.0f,            0.0f,            0.0f,            1.0f, 0.0f
                    )))
                } else null
            }
            PaperToneMode.DARK -> {
                ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            PaperToneMode.NATURAL_WHITE -> null
        }
    }

    val originalToolbar = androidx.compose.ui.platform.LocalTextToolbar.current
    val customToolbar = remember(originalToolbar) {
        object : androidx.compose.ui.platform.TextToolbar {
            override val status: androidx.compose.ui.platform.TextToolbarStatus
                get() = originalToolbar.status

            override fun showMenu(
                rect: androidx.compose.ui.geometry.Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                val wrappedCopy = onCopyRequested?.let { originalCopy ->
                    {
                        originalCopy.invoke()
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clipText.isNullOrBlank()) {
                            selectedCanvasText = clipText
                        }
                    }
                }
                originalToolbar.showMenu(
                    rect = rect,
                    onCopyRequested = wrappedCopy ?: onCopyRequested,
                    onPasteRequested = onPasteRequested,
                    onCutRequested = onCutRequested,
                    onSelectAllRequested = onSelectAllRequested
                )
            }

            override fun hide() {
                originalToolbar.hide()
            }
        }
    }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalTextToolbar provides customToolbar) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .monitorReadingActivity { interactionTrigger = System.currentTimeMillis() }
        ) {
            // 1. Full-screen rendering canvas with universal Zoom & Pan
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
            val density = LocalDensity.current
            val viewportWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val viewportHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
            fun clampOffset(offset: Offset, scale: Float): Offset {
                if (scale <= 1.0f) return Offset.Zero
                val maxOffsetX = maxOf(0f, (viewportWidthPx * (scale - 1f)) / 2f)
                val maxOffsetY = maxOf(0f, (viewportHeightPx * (scale - 1f)) / 2f)
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val enter = pageEnter.value
                        scaleX = zoomScale
                        scaleY = zoomScale
                        rotationZ = rotationDegrees.toFloat()
                        translationX = zoomOffset.x + (1f - enter) * pageTurnDirection * 48.dp.toPx()
                        translationY = zoomOffset.y
                        alpha = 0.3f + 0.7f * enter
                    }
                    .pointerInput(pageIndex, pageCount, viewportWidthPx, viewportHeightPx, zoomScale) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var zoomCentroid = down.position
                            var dragTotalX = 0f
                            var dragTotalY = 0f
                            var isPinchZoom = false

                            do {
                                val event = awaitPointerEvent()
                                val pressed = event.changes.filter { it.pressed }
                                if (pressed.size > 1) {
                                    isPinchZoom = true
                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    val centroid = event.calculateCentroid(useCurrent = true)
                                    if (centroid != Offset.Unspecified) {
                                        zoomCentroid = centroid
                                    }

                                    val oldScale = zoomScale
                                    val nextScale = (oldScale * zoom).coerceIn(MIN_CANVAS_ZOOM, MAX_CANVAS_ZOOM)
                                    val actualFactor = if (oldScale > 0.0001f) nextScale / oldScale else 1f

                                    val center = Offset(viewportWidthPx / 2f, viewportHeightPx / 2f)
                                    val focal = zoomCentroid - center

                                    val targetOffset = if (nextScale > 0.80f) {
                                        (zoomOffset - focal) * actualFactor + focal + pan
                                    } else {
                                        Offset.Zero
                                    }
                                    setZoom(nextScale, targetOffset)
                                    pressed.forEach { it.consume() }
                                    dragTotalX = 0f
                                    dragTotalY = 0f
                                } else if (pressed.size == 1) {
                                    val change = pressed.first()
                                    val pan = change.positionChange()
                                    if (zoomScale > 1.05f) {
                                        if (pan != Offset.Zero) {
                                            zoomOffset = clampOffset(zoomOffset + pan, zoomScale)
                                            change.consume()
                                        }
                                    } else if (!isPinchZoom) {
                                        dragTotalX += pan.x
                                        dragTotalY += pan.y
                                        if (kotlin.math.abs(dragTotalX) > 24f && kotlin.math.abs(dragTotalX) > kotlin.math.abs(dragTotalY)) {
                                            change.consume()
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            if (!isPinchZoom && zoomScale <= 1.05f) {
                                val absX = kotlin.math.abs(dragTotalX)
                                val absY = kotlin.math.abs(dragTotalY)
                                if (absX > 40f && absX > absY) {
                                    if (dragTotalX < -40f) {
                                        selectPage(pageIndex + 1)
                                    } else if (dragTotalX > 40f) {
                                        selectPage(pageIndex - 1)
                                    }
                                } else if (absX < 12f && absY < 12f) {
                                    // Tapping the page is the only way back to the bars on this
                                    // path. Full Screen Mode is toggled from the overflow menu,
                                    // which lives in the top bar it hides, and landscape starts
                                    // with both bars hidden — so without this the PDF view can
                                    // be left with no reachable control at all. The slide, EPUB
                                    // and DOCX canvases already have their own tap toggle.
                                    topBarVisible = !topBarVisible
                                    bottomBarVisible = !bottomBarVisible
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val image = bitmap
                when {
                    image != null -> {
                        Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = "Rendered original document",
                            colorFilter = darkThemeColorFilter,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    isPresentation && pptxDeck != null -> {
                        val currentSlide = pptxDeck?.slides?.getOrNull(pageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0)))
                        if (currentSlide != null) {
                            PresentationSlideCanvas(
                                slide = currentSlide,
                                slideCount = pageCount,
                                slideImages = currentSlideImages,
                                showNotes = showSpeakerNotes,
                                onToggleNotes = { showSpeakerNotes = !showSpeakerNotes },
                                onNextSlide = { selectPage(pageIndex + 1) },
                                onPrevSlide = { selectPage(pageIndex - 1) },
                                onToggleBars = {
                                    topBarVisible = !topBarVisible
                                    bottomBarVisible = !bottomBarVisible
                                },
                                onSelectText = { selectedCanvasText = it },
                                isPlaying = isPlaying,
                                activeSentencePage = activeSentencePage,
                                activeSentenceText = activeSentenceText,
                                paperToneMode = paperToneMode,
                                rotationDegrees = rotationDegrees,
                                isLandscape = isLandscape,
                                modifier = if (isLandscape) {
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp, vertical = if (topBarVisible) 40.dp else 4.dp)
                                } else {
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = if (!topBarVisible) 4.dp else 72.dp)
                                }
                            )
                        }
                    }
                    isEpub && epubBook != null -> {
                        val currentChapter = epubBook?.chapters?.getOrNull(pageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0)))
                        if (currentChapter != null) {
                            EpubBookCanvas(
                                bookTitle = epubBook?.title ?: document.title,
                                chapter = currentChapter,
                                chapterCount = pageCount,
                                onNextChapter = { selectPage(pageIndex + 1) },
                                onPrevChapter = { selectPage(pageIndex - 1) },
                                onToggleBars = {
                                    topBarVisible = !topBarVisible
                                    bottomBarVisible = !bottomBarVisible
                                },
                                onSelectText = { selectedCanvasText = it },
                                isPlaying = isPlaying,
                                activeSentencePage = activeSentencePage,
                                activeSentenceText = activeSentenceText,
                                paperToneMode = paperToneMode,
                                rotationDegrees = rotationDegrees,
                                isLandscape = isLandscape,
                                modifier = if (isLandscape) {
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp, vertical = if (topBarVisible) 40.dp else 4.dp)
                                } else {
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = if (!topBarVisible) 4.dp else 72.dp)
                                }
                            )
                        }
                    }
                    isDocx && docxDoc != null -> {
                        val currentPage = docxDoc?.pages?.getOrNull(pageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0)))
                        if (currentPage != null) {
                            DocxDocumentCanvas(
                                docTitle = docxDoc?.title ?: document.title,
                                page = currentPage,
                                pageCount = pageCount,
                                onNextPage = { selectPage(pageIndex + 1) },
                                onPrevPage = { selectPage(pageIndex - 1) },
                                onToggleBars = {
                                    topBarVisible = !topBarVisible
                                    bottomBarVisible = !bottomBarVisible
                                },
                                onSelectText = { selectedCanvasText = it },
                                isPlaying = isPlaying,
                                activeSentencePage = activeSentencePage,
                                activeSentenceText = activeSentenceText,
                                paperToneMode = paperToneMode,
                                rotationDegrees = rotationDegrees,
                                isLandscape = isLandscape,
                                modifier = if (isLandscape) {
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp, vertical = if (topBarVisible) 40.dp else 4.dp)
                                } else {
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = if (!topBarVisible) 4.dp else 72.dp)
                                }
                            )
                        }
                    }
                    message == null && (isPdf || isImage || isPresentation || isEpub || isDocx) -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    else -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            shape = RoundedCornerShape(16.dp),
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

            // Universal Zoom Control Pill at Bottom-Right
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = if (bottomBarVisible && !isLandscape) 82.dp else 16.dp),
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    TextButton(
                        onClick = {
                            val next = ((Math.round(zoomScale * 4f) - 1) / 4f).coerceIn(MIN_CANVAS_ZOOM, MAX_CANVAS_ZOOM)
                            setZoom(next)
                        },
                        enabled = zoomScale > MIN_CANVAS_ZOOM + 0.01f
                    ) { Text("−", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    TextButton(onClick = { setZoom(1f, Offset.Zero) }) {
                        Text(
                            "${(zoomScale * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(
                        onClick = {
                            val next = ((Math.round(zoomScale * 4f) + 1) / 4f).coerceIn(MIN_CANVAS_ZOOM, MAX_CANVAS_ZOOM)
                            setZoom(next)
                        },
                        enabled = zoomScale < MAX_CANVAS_ZOOM - 0.01f
                    ) { Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                }
            }
        }

        // 2. Floating Top app bar (Modernized Branded Identity)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer { translationY = topBarOffset }
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = VeritasPackStyle.cardShape(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha()),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
            tonalElevation = 4.dp,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Back Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Title & Page Slider / Subtitle
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = if (isPdf) "PDF" else if (isPresentation) "PPT" else if (isEpub) "EPUB" else if (isDocx) "DOCX" else if (isImage) "IMG" else "DOC",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = document.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if ((isPdf || isPresentation || isEpub || isDocx) && pageCount > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${if (isPresentation) "Slide" else if (isEpub) "Chapter" else "Page"} ${pageIndex + 1} of $pageCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (zoomScale > 1.05f) {
                                Text(
                                    text = "• ${(zoomScale * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (rotationDegrees != 0) {
                                Text(
                                    text = "• ${rotationDegrees}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        SlimPageSlider(
                            pageIndex = pageIndex,
                            pageCount = pageCount,
                            onPageSelected = ::selectPage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(18.dp)
                        )
                    } else {
                        Text(
                            text = if (isPresentation) "PowerPoint Slide View" else if (isEpub) "EPUB Book View" else if (isDocx) "Word Document View" else if (isImage) "Original Image View" else "Original Document View",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Switch to Text Reader
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = "Switch to Extracted Text",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Rotate Page Button
                IconButton(
                    onClick = {
                        val activity = context as? android.app.Activity
                        activity?.let { act ->
                            act.requestedOrientation = if (isLandscape) {
                                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                            } else {
                                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        }
                        // Only the window turns. Rotating the canvas as well double-counted:
                        // the activity used to be destroyed on the orientation change, which
                        // reset this back to 0 and hid the second rotation. Now that the view
                        // survives the change, both applied and the page rendered sideways.
                        zoomScale = 1f
                        zoomOffset = Offset.Zero
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "Rotate",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // More Options / Tools Menu (3-dots overflow)
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .width(290.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "Document Tools",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        // --- VIEW & DISPLAY ---
                        Text(
                            text = "Display",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(if (topBarVisible) "Full Screen Mode" else "Exit Full Screen", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(if (topBarVisible) "Hide toolbar & player bars" else "Show toolbar & player bars", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = {
                                Icon(painter = painterResource(R.drawable.ic_m3_fullscreen), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                topBarVisible = !topBarVisible
                                bottomBarVisible = !bottomBarVisible
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Fit to Screen", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Reset zoom to 100%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.FitScreen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                zoomScale = 1f
                                zoomOffset = Offset.Zero
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Rotate View (90°)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(if (isLandscape) "Switch to portrait" else "Switch to landscape", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                val activity = context as? android.app.Activity
                                activity?.let { act ->
                                    act.requestedOrientation = if (isLandscape) {
                                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                                    } else {
                                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                    }
                                }
                                zoomScale = 1f
                                zoomOffset = Offset.Zero
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        when (paperToneMode) {
                                            PaperToneMode.ACTIVE_THEME -> "Theme-Adapted Paper (Active)"
                                            PaperToneMode.DARK -> "Theme-Adapted Paper (Dark)"
                                            PaperToneMode.NATURAL_WHITE -> "Natural Paper Colors (White)"
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        when (paperToneMode) {
                                            PaperToneMode.ACTIVE_THEME -> "Tap for dark paper"
                                            PaperToneMode.DARK -> "Tap for authentic white paper"
                                            PaperToneMode.NATURAL_WHITE -> "Tap to adapt paper to active theme"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.InvertColors, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                paperToneMode = when (paperToneMode) {
                                    PaperToneMode.ACTIVE_THEME -> PaperToneMode.DARK
                                    PaperToneMode.DARK -> PaperToneMode.NATURAL_WHITE
                                    PaperToneMode.NATURAL_WHITE -> PaperToneMode.ACTIVE_THEME
                                }
                                showMenu = false
                            }
                        )

                        // --- PAGE NAVIGATION ---
                        if ((isPdf || isPresentation || isEpub || isDocx) && pageCount > 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Text(
                                text = "Navigation",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Jump to ${if (isPresentation) "Slide" else if (isEpub) "Chapter" else "Page"}...", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Go to 1–$pageCount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Numbers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                onClick = {
                                    jumpPageInput = "${pageIndex + 1}"
                                    showJumpToPageDialog = true
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("First ${if (isPresentation) "Slide" else if (isEpub) "Chapter" else "Page"} (1)", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    Icon(Icons.Filled.FirstPage, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                enabled = pageIndex > 0,
                                onClick = {
                                    selectPage(0)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Last ${if (isPresentation) "Slide" else if (isEpub) "Chapter" else "Page"} ($pageCount)", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                enabled = pageIndex < pageCount - 1,
                                onClick = {
                                    selectPage(pageCount - 1)
                                    showMenu = false
                                }
                            )
                        }

                        // --- STUDY & AUDIO ---
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            text = "Reading & Audio",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Switch to Text Reader", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Flowing text, notes & speed reader", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                showMenu = false
                                onClose()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Voice Studio", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Narrators, speed & audio tuning", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                showMenu = false
                                onOpenVoiceStudio()
                            }
                        )

                        // --- FILE & SHARE ---
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Share Original File", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Send to other apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            enabled = original != null,
                            onClick = {
                                showMenu = false
                                original?.let { uri ->
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = document.originalMimeType.ifBlank { "application/pdf" }
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Document"))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Open in External App", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Use system PDF or photo viewer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            enabled = original != null,
                            onClick = {
                                showMenu = false
                                onOpenExternal()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Document Information", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            onClick = {
                                showDocInfoDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Jump to Page Dialog
        if (showJumpToPageDialog) {
            AlertDialog(
                onDismissRequest = { showJumpToPageDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                title = { Text("Jump to Page", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Enter ${if (isPresentation) "slide" else "page"} number between 1 and $pageCount:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = jumpPageInput,
                            onValueChange = { jumpPageInput = it.filter { ch -> ch.isDigit() } },
                            singleLine = true,
                            placeholder = { Text("1–$pageCount") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = jumpPageInput.toIntOrNull()
                            if (parsed != null && parsed in 1..pageCount) {
                                selectPage(parsed - 1)
                            }
                            showJumpToPageDialog = false
                        }
                    ) {
                        Text("Go")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJumpToPageDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Document Info Dialog
        if (showDocInfoDialog) {
            AlertDialog(
                onDismissRequest = { showDocInfoDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                title = { Text("Document Info", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Title: ${document.title}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Format: ${if (isPdf) "PDF Document" else if (isPresentation) "PowerPoint Presentation" else if (isImage) "Image" else document.sourceLabel}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (isPdf || isPresentation) {
                            Text("Total ${if (isPresentation) "Slides" else "Pages"}: $pageCount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Current ${if (isPresentation) "Slide" else "Page"}: ${pageIndex + 1} (${((pageIndex + 1) * 100 / pageCount.coerceAtLeast(1))}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Chunks / Sentences: ${document.chunkCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (document.originalFileName.isNotBlank()) {
                            Text("File: ${document.originalFileName.substringAfterLast('/')}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showDocInfoDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }
        // Selected Text Action Card
        if (selectedCanvasText != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (bottomBarVisible && !isLandscape) 88.dp else 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.FormatQuote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Selected Text Actions",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { selectedCanvasText = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "“${selectedCanvasText?.take(100)}${if ((selectedCanvasText?.length ?: 0) > 100) "…" else ""}”",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val textToRead = selectedCanvasText.orEmpty()
                                selectedCanvasText = null
                                onReadFromSentence?.invoke(textToRead, pageIndex + 1)
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Continue reading from here", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = {
                                val textToCopy = selectedCanvasText.orEmpty()
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Veritas Text", textToCopy))
                                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                selectedCanvasText = null
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                        }
                        OutlinedButton(
                            onClick = {
                                val textToSearch = selectedCanvasText.orEmpty()
                                val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                    putExtra(android.app.SearchManager.QUERY, textToSearch)
                                }
                                runCatching { context.startActivity(searchIntent) }
                                selectedCanvasText = null
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                        }
                    }
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
                onVoiceSelected = onVoiceSelected,
                onToggleTextMode = onClose
            )
        }
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
    onVoiceSelected: (TtsVoiceOption) -> Unit,
    onToggleTextMode: () -> Unit = {}
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
        shape = VeritasPackStyle.cardShape(),
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha()),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
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
                    onClick = onToggleTextMode,
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
                            contentDescription = "Switch to Text Mode",
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
                        androidx.compose.animation.AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                (androidx.compose.animation.scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                                    androidx.compose.animation.fadeIn(tween(150)))
                                    .togetherWith(androidx.compose.animation.fadeOut(tween(100)))
                            },
                            label = "originalPlayMorph"
                        ) { playing ->
                            Icon(
                                if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play"
                            )
                        }
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
                        onValueChange = rememberSliderHaptics(rate, 0.5f..2.0f, 0, onRateChange),
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
                                onValueChange = rememberSliderHaptics(pitch, 0.7f..1.4f, 0, onPitchChange),
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
                                onValueChange = rememberSliderHaptics(fontSizeSp.toFloat(), 14f..28f, 13) { onFontSizeChange(it.toInt().coerceIn(14, 28)) },
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
    val tickingPageSelect = com.veritas.reader.ui.rememberStepHaptics(pageIndex, onPageSelected)
    BoxWithConstraints(
        modifier = modifier
            .pointerInput(pageCount) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    tickingPageSelect(((offset.x / width).coerceIn(0f, 1f) * (pageCount - 1)).roundToInt())
                }
            }
            .pointerInput(pageCount) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        tickingPageSelect(((offset.x / width).coerceIn(0f, 1f) * (pageCount - 1)).roundToInt())
                    },
                    onDrag = { change, _ ->
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        tickingPageSelect(((change.position.x / width).coerceIn(0f, 1f) * (pageCount - 1)).roundToInt())
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
private fun getCanvasColors(paperToneMode: PaperToneMode): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return when (paperToneMode) {
        PaperToneMode.ACTIVE_THEME -> MaterialTheme.colorScheme.surfaceContainerLow to MaterialTheme.colorScheme.onSurface
        PaperToneMode.DARK -> androidx.compose.ui.graphics.Color(0xFF141414) to androidx.compose.ui.graphics.Color(0xFFE8E8E8)
        PaperToneMode.NATURAL_WHITE -> androidx.compose.ui.graphics.Color(0xFFFFFFFF) to androidx.compose.ui.graphics.Color(0xFF1C1B1F)
    }
}

@Composable
private fun PresentationSlideCanvas(
    slide: PptxSlideContent,
    slideCount: Int,
    slideImages: List<Bitmap>,
    showNotes: Boolean,
    onToggleNotes: () -> Unit,
    onNextSlide: () -> Unit,
    onPrevSlide: () -> Unit,
    onToggleBars: () -> Unit,
    onSelectText: ((String) -> Unit)? = null,
    isPlaying: Boolean = false,
    activeSentencePage: Int = 0,
    activeSentenceText: String = "",
    paperToneMode: PaperToneMode = PaperToneMode.ACTIVE_THEME,
    rotationDegrees: Int = 0,
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dragAmountX by remember { mutableFloatStateOf(0f) }
    val (cardBg, contentColor) = getCanvasColors(paperToneMode)

    Card(
        modifier = if (isLandscape) modifier.fillMaxSize() else modifier.fillMaxWidth(),
        shape = if (isLandscape) RectangleShape else RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = if (isLandscape) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLandscape) 0.dp else 8.dp)
    ) {
        Column(
            modifier = if (isLandscape) Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp) else Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 14.dp)
        ) {
            // Slide Top Bar (Badge + Notes Pill + Prev/Next Buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Slide ${slide.number} of $slideCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (slide.notesLines.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.clickable { onToggleNotes() },
                            shape = RoundedCornerShape(8.dp),
                            color = if (showNotes) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "📝 Notes (${slide.notesLines.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (showNotes) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevSlide,
                        enabled = slide.number > 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Previous Slide",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onNextSlide,
                        enabled = slide.number < slideCount,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "Next Slide",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Slide Title
            if (slide.titleLines.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = slide.titleLines.joinToString("\n"),
                        style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                    )
                }
            }

            // Slide Body Content (Scrollable with Selectable text & Speech highlighting)
            androidx.compose.foundation.text.selection.SelectionContainer {
                Column(
                    modifier = if (isLandscape) {
                        Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
                    } else {
                        Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState())
                    },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slide.contentLines.forEachIndexed { lineIdx, line ->
                        val isHighlighted = isPlaying &&
                            slide.number == activeSentencePage &&
                            ActiveSentenceMatcher.matches(line, activeSentenceText)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectText?.invoke(line) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f) else androidx.compose.ui.graphics.Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isHighlighted) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(16.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                }
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Embedded Slide Images
                    if (slideImages.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            slideImages.forEach { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Slide Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    // Speaker Notes Area
                    if (showNotes && slide.notesLines.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Speaker Notes",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                slide.notesLines.forEach { note ->
                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.bodySmall,
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
}

@Composable
private fun EpubBookCanvas(
    bookTitle: String,
    chapter: EpubChapter,
    chapterCount: Int,
    onNextChapter: () -> Unit,
    onPrevChapter: () -> Unit,
    onToggleBars: () -> Unit,
    onSelectText: ((String) -> Unit)? = null,
    isPlaying: Boolean = false,
    activeSentencePage: Int = 0,
    activeSentenceText: String = "",
    paperToneMode: PaperToneMode = PaperToneMode.ACTIVE_THEME,
    rotationDegrees: Int = 0,
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dragAmountX by remember { mutableFloatStateOf(0f) }
    val (cardBg, contentColor) = getCanvasColors(paperToneMode)

    Card(
        modifier = if (isLandscape) modifier.fillMaxSize() else modifier.fillMaxWidth(),
        shape = if (isLandscape) RectangleShape else RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = if (isLandscape) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLandscape) 0.dp else 6.dp)
    ) {
        Column(
            modifier = if (isLandscape) Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp) else Modifier.fillMaxWidth().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 14.dp)
        ) {
            // Book Header (Badge + Prev/Next buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "📕 Chapter ${chapter.number} of $chapterCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevChapter,
                        enabled = chapter.number > 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Previous Chapter",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onNextChapter,
                        enabled = chapter.number < chapterCount,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "Next Chapter",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Chapter Title & Classic Book Ornament
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = chapter.title,
                    style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "— ❦ —",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            // Chapter Paragraphs (Scrollable with Selectable text & Speech highlighting)
            androidx.compose.foundation.text.selection.SelectionContainer {
                Column(
                    modifier = if (isLandscape) {
                        Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
                    } else {
                        Modifier.fillMaxWidth().heightIn(max = 460.dp).verticalScroll(rememberScrollState())
                    },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    chapter.paragraphs.forEachIndexed { paraIdx, para ->
                        val isHighlighted = isPlaying &&
                            chapter.number == activeSentencePage &&
                            ActiveSentenceMatcher.matches(para, activeSentenceText)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectText?.invoke(para) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f) else androidx.compose.ui.graphics.Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isHighlighted) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(18.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                }
                                Text(
                                    text = "    $para",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 22.sp
                                    ),
                                    color = contentColor,
                                    modifier = Modifier.weight(1f)
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
private fun DocxDocumentCanvas(
    docTitle: String,
    page: DocxPage,
    pageCount: Int,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    onToggleBars: () -> Unit,
    onSelectText: ((String) -> Unit)? = null,
    isPlaying: Boolean = false,
    activeSentencePage: Int = 0,
    activeSentenceText: String = "",
    paperToneMode: PaperToneMode = PaperToneMode.ACTIVE_THEME,
    rotationDegrees: Int = 0,
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dragAmountX by remember { mutableFloatStateOf(0f) }
    val (cardBg, contentColor) = getCanvasColors(paperToneMode)

    Card(
        modifier = if (isLandscape) modifier.fillMaxSize() else modifier.fillMaxWidth(),
        shape = if (isLandscape) RectangleShape else RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = if (isLandscape) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLandscape) 0.dp else 6.dp)
    ) {
        Column(
            modifier = if (isLandscape) Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp) else Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "📄 Page ${page.pageNumber} of $pageCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevPage,
                        enabled = page.pageNumber > 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Previous Page",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onNextPage,
                        enabled = page.pageNumber < pageCount,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "Next Page",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Blocks (Scrollable with Selectable text & Speech highlighting)
            androidx.compose.foundation.text.selection.SelectionContainer {
                Column(
                    modifier = if (isLandscape) {
                        Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
                    } else {
                        Modifier.fillMaxWidth().heightIn(max = 460.dp).verticalScroll(rememberScrollState())
                    },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    page.blocks.forEachIndexed { blockIdx, block ->
                        val isHighlighted = isPlaying &&
                            page.pageNumber == activeSentencePage &&
                            ActiveSentenceMatcher.matches(docxBlockPlainText(block), activeSentenceText)
                        when (block) {
                            is DocxBlock.Heading -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                        .clickable { onSelectText?.invoke(block.text) },
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(if (block.level == 1) 22.dp else 16.dp)
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                    )
                                    Text(
                                        text = block.text,
                                        style = if (block.level == 1) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = contentColor
                                    )
                                }
                            }
                            is DocxBlock.Bullet -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectText?.invoke(block.text) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f) else androidx.compose.ui.graphics.Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = (block.level * 16).dp, top = 2.dp, bottom = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = if (block.level == 0) "•" else "◦",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = block.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                            is DocxBlock.Paragraph -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectText?.invoke(block.text) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f) else androidx.compose.ui.graphics.Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isHighlighted) {
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(16.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                        }
                                        Text(
                                            text = block.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = contentColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                            is DocxBlock.Table -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onSelectText?.invoke(block.rows.joinToString("\n") { it.joinToString(" | ") }) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        block.rows.forEachIndexed { rowIndex, row ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                row.forEach { cell ->
                                                    Text(
                                                        text = cell,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = if (rowIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (rowIndex == 0) MaterialTheme.colorScheme.primary else contentColor,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            if (rowIndex < block.rows.size - 1) {
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
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

private const val MIN_CANVAS_ZOOM = 0.75f
private const val MAX_CANVAS_ZOOM = 5.0f
private const val CANVAS_ZOOM_STEP = 0.25f

/**
 * Decides whether a rendered line in the original-document view is the one currently being
 * spoken. The two sides do not agree on granularity: the reader speaks sentences, while the
 * canvases render slide bullets, EPUB paragraphs, and DOCX blocks, so a line can hold several
 * sentences or a sentence can span several lines. Containment in either direction covers both,
 * and the length floor stops a short line like "Introduction" from matching every sentence that
 * happens to contain the word.
 */
internal object ActiveSentenceMatcher {
    /** A sentence short enough to be a stray fragment is not worth matching a whole line on. */
    private const val MIN_SENTENCE_LENGTH = 12

    /**
     * When the spoken sentence is the longer side, the rendered line is only a fragment of it,
     * and a one- or two-word fragment matches far too much: a "Introduction" heading would
     * light up for every sentence that happens to contain the word. Require the fragment to
     * carry real content instead.
     */
    private const val MIN_FRAGMENT_WORDS = 4

    fun normalize(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    fun matches(line: String, activeSentence: String): Boolean {
        if (line.isBlank() || activeSentence.isBlank()) return false
        val normalizedLine = normalize(line)
        val normalizedSentence = normalize(activeSentence)
        if (normalizedLine.isBlank() || normalizedSentence.isBlank()) return false
        if (normalizedLine == normalizedSentence) return true

        // The line holds several sentences and one of them is being spoken.
        if (normalizedSentence.length >= MIN_SENTENCE_LENGTH &&
            normalizedLine.contains(normalizedSentence)
        ) return true

        // The sentence wrapped across several rendered lines and this is one of them.
        if (normalizedSentence.contains(normalizedLine) &&
            normalizedLine.split(' ').count { it.isNotBlank() } >= MIN_FRAGMENT_WORDS
        ) return true

        return false
    }
}

/** Flattens a DOCX block to the plain text the extractor would have spoken for it. */
internal fun docxBlockPlainText(block: DocxBlock): String = when (block) {
    is DocxBlock.Heading -> block.text
    is DocxBlock.Paragraph -> block.text
    is DocxBlock.Bullet -> block.text
    is DocxBlock.Table -> block.rows.joinToString(" ") { row -> row.joinToString(" ") }
}

/** Quiet time before the reader's chrome collapses on its own. */
private const val BARS_AUTO_HIDE_MS = 5_000L
