package com.veritas.reader

import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.graphics.drawable.StateListDrawable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.pdf.PdfRect
import androidx.pdf.view.Highlight
import androidx.pdf.view.PdfView
import androidx.pdf.viewer.fragment.PdfViewerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class VeritasPdfViewerActivity : AppCompatActivity() {
    private lateinit var repository: DocumentRepository
    private var document: SavedDocument? = null
    private var fragmentContainer: FrameLayout? = null
    private var viewerFragment: PdfViewerFragment? = null
    private var pdfView: PdfView? = null
    private var playPauseControl: TextView? = null
    private var isSyncEnabled = true
    private var syncPill: LinearLayout? = null
    private var syncLabel: TextView? = null
    private var highlightJob: Job? = null
    private var extractedChunks: List<String> = emptyList()
    private var readerTextModel: ReaderTextModel? = null
    private var lastHighlightKey: String = ""
    private var lastHighlightPage: Int? = null
    private var pendingManualPageSync = false
    private var lastSyncedTargetPage: Int? = null
    private var toolbarChrome: View? = null
    private var bottomChrome: View? = null
    private var chromeHideJob: Job? = null
    private var chromeVisible = false
    private var chromeMenuOpen = false
    private var tapDownX = 0f
    private var tapDownY = 0f
    private var tapDownTime = 0L
    private var tapMoved = false
    // Expandable bottom panel state
    private var panelExpanded = false
    private var expandedPanelContent: LinearLayout? = null
    private var panelExpandArrow: TextView? = null
    private var panelStatusLabel: TextView? = null
    private var panelSpeedLabel: TextView? = null
    private var panelPitchLabel: TextView? = null
    private var keepAwakeTimerJob: Job? = null

    private var isLightTheme = false
    private var colorPrimary = 0
    private var colorBackground = 0
    private var colorToolbar = 0
    private var colorSurface = 0
    private var colorSurfaceVariant = 0
    private var colorTextPrimary = 0
    private var colorTextSecondary = 0
    private var colorOutline = 0
    private var colorSyncBackground = 0
    private var colorActiveStrip = 0
    private var colorAccentButton = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        repository = DocumentRepository(applicationContext)

        val settings = repository.loadReaderSettings()
        val themeId = settings.themeId
        val packId = settings.themePackId

        val resolvedTheme = if (themeId == "system") {
            val mode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            if (mode == android.content.res.Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
        } else {
            themeId
        }

        val baseScheme = veritasColorScheme(resolvedTheme, this)
        val scheme = veritasPackColorScheme(baseScheme, packId)

        isLightTheme = scheme.background.luminance() > 0.45f

        colorPrimary = scheme.primary.toArgb()
        colorBackground = scheme.background.toArgb()
        colorToolbar = scheme.surface.toArgb()
        colorSurface = scheme.surface.toArgb()
        colorSurfaceVariant = scheme.surfaceVariant.toArgb()
        colorTextPrimary = scheme.onSurface.toArgb()
        colorTextSecondary = scheme.onSurfaceVariant.toArgb()
        colorOutline = scheme.outlineVariant.toArgb()
        colorSyncBackground = scheme.primaryContainer.toArgb()
        colorActiveStrip = scheme.primary.toArgb()
        colorAccentButton = scheme.primary.toArgb()

        configureSystemBars()

        val documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID).orEmpty()
        val metadata = repository.findDocument(documentId)
        val uri = metadata?.let { repository.originalUri(it) }
        if (metadata == null || uri == null) {
            showFallback("The original PDF is no longer available.")
            return
        }
        document = metadata
        buildLayout(metadata.title.ifBlank { getString(R.string.app_name) })
        loadHighlightTextAsync(metadata)
        runCatching {
            var fragment = supportFragmentManager.findFragmentByTag(VIEWER_TAG) as? PdfViewerFragment
            if (fragment == null) {
                fragment = PdfViewerFragment()
                supportFragmentManager.beginTransaction()
                    .replace(requireNotNull(fragmentContainer).id, fragment, VIEWER_TAG)
                    .commitNowAllowingStateLoss()
                fragment.documentUri = uri
            } else {
                fragment.documentUri = uri
            }
            viewerFragment = fragment
            schedulePdfViewLookup(fragment)
        }.onFailure { error ->
            showFallback("Veritas could not open this PDF viewer: ${error.message ?: "unknown error"}")
        }
    }

    private fun buildLayout(title: String) {
        // FrameLayout so the PDF stays full-screen underneath and the bars float
        // OVER it — sliding them away never resizes or jumps the document.
        val root = FrameLayout(this).apply {
            setBackgroundColor(colorBackground)
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, statusBarHeight() + 8.dp, 6.dp, 4.dp)
            setBackgroundColor(colorToolbar)
            elevation = 6f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
            )
        }
        applyToolbarInsets(toolbar)
        toolbar.addView(iconButton(R.drawable.ic_m3_chevron_left) { finish() })

        val titleCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4.dp, 0, 4.dp, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val titleView = TextView(this).apply {
            text = title
            setTextColor(colorTextPrimary)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        val subRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 2.dp, 0, 0)
        }
        val badgeView = TextView(this).apply {
            text = "PDF"
            setTextColor(colorPrimary)
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(5.dp, 1.dp, 5.dp, 1.dp)
            background = rounded(colorSyncBackground, 4.dp)
        }
        val subTextView = TextView(this).apply {
            text = " • Original View"
            setTextColor(colorTextSecondary)
            textSize = 11f
            maxLines = 1
        }
        subRow.addView(badgeView)
        subRow.addView(subTextView)
        titleCol.addView(titleView)
        titleCol.addView(subRow)
        toolbar.addView(titleCol)

        // Modern Material 3 Live Sync Pill Chip
        val pill = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, 5.dp, 8.dp, 5.dp)
            background = rounded(colorSyncBackground, 14.dp)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = 4.dp
            }
        }
        val label = TextView(this).apply {
            text = "🔗 Live Sync"
            setTextColor(colorPrimary)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
        }
        pill.addView(label)
        syncPill = pill
        syncLabel = label

        pill.setOnClickListener {
            isSyncEnabled = !isSyncEnabled
            updateSyncPillUi()
            if (isSyncEnabled) {
                pendingManualPageSync = true
                lifecycleScope.launch { updateSentenceHighlight(forceSync = true) }
                Toast.makeText(this@VeritasPdfViewerActivity, "Live sync enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@VeritasPdfViewerActivity, "Live sync disabled", Toast.LENGTH_SHORT).show()
            }
        }
        toolbar.addView(pill)

        toolbar.addView(iconButton(R.drawable.ic_m3_search) { toggleSearch() })
        toolbar.addView(iconButton(R.drawable.ic_m3_rotate_right) { rotateViewer() })
        toolbar.addView(iconButton(R.drawable.ic_m3_more_vert) { showTopMenu(toolbar) })
        toolbarChrome = toolbar

        fragmentContainer = FrameLayout(this).apply {
            id = R.id.pdf_fragment_container
            setBackgroundColor(if (isLightTheme) colorBackground else Color.WHITE)
            if (!isLightTheme) {
                val settings = repository.loadReaderSettings()
                val resolvedTheme = if (settings.themeId == "system") {
                    val mode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                    if (mode == android.content.res.Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
                } else settings.themeId
                val scheme = veritasPackColorScheme(veritasColorScheme(resolvedTheme, this@VeritasPdfViewerActivity), settings.themePackId)
                val bgR = scheme.surface.red
                val bgG = scheme.surface.green
                val bgB = scheme.surface.blue
                val fgR = scheme.onSurface.red
                val fgG = scheme.onSurface.green
                val fgB = scheme.onSurface.blue
                
                val deltaR = bgR - fgR
                val deltaG = bgG - fgG
                val deltaB = bgB - fgB
                
                val paint = android.graphics.Paint().apply {
                    colorFilter = android.graphics.ColorMatrixColorFilter(floatArrayOf(
                        0.299f * deltaR, 0.587f * deltaR, 0.114f * deltaR, 0.0f, fgR * 255.0f,
                        0.299f * deltaG, 0.587f * deltaG, 0.114f * deltaG, 0.0f, fgG * 255.0f,
                        0.299f * deltaB, 0.587f * deltaB, 0.114f * deltaB, 0.0f, fgB * 255.0f,
                        0.0f,            0.0f,            0.0f,            1.0f, 0.0f
                    ))
                }
                setLayerType(View.LAYER_TYPE_HARDWARE, paint)
            }
            setOnTouchListener { _, event ->
                handleDocumentChromeTouch(this, event)
                false
            }
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val controlsOuter = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10.dp, 7.dp, 10.dp, 13.dp)
            setBackgroundColor(colorBackground)
            elevation = 6f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        }
        applyDeckInsets(controlsOuter)
        bottomChrome = controlsOuter

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(colorSurface, 24.dp)
            elevation = 8f
        }

        // Progress strip (thin coloured bar at top of panel)
        val progressStrip = View(this).apply {
            setBackgroundColor(if (PlaybackStateStore.isPlaying) colorActiveStrip else colorOutline)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3.dp)
        }

        // ── Header row (always visible) ─────────────────────────────────────
        val controlRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10.dp, 8.dp, 8.dp, 8.dp)
        }

        // Brand tile — tap to expand/collapse
        val brand = FrameLayout(this).apply {
            background = rounded(colorSyncBackground, 12.dp)
            layoutParams = LinearLayout.LayoutParams(50.dp, 50.dp)
            addView(ImageView(context).apply {
                setImageResource(R.drawable.veritas_reader_icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(5.dp, 5.dp, 5.dp, 5.dp)
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            setOnClickListener { togglePanelExpand() }
        }
        controlRow.addView(brand)

        // Status text
        panelStatusLabel = TextView(this).apply {
            text = if (PlaybackStateStore.isPlaying) "Now reading" else "Ready to read"
            setTextColor(colorTextPrimary)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 8.dp
                marginEnd = 2.dp
            }
        }
        controlRow.addView(panelStatusLabel)

        // Playback controls
        controlRow.addView(iconButton(R.drawable.ic_m3_chevron_left) { sendPlaybackIntent(this, PlaybackActions.ACTION_PREVIOUS) })
        playPauseControl = prominentButton("") {
            sendPlaybackIntent(this, if (PlaybackStateStore.isPlaying) PlaybackActions.ACTION_PAUSE else PlaybackActions.ACTION_PLAY)
            playPauseControl?.postDelayed({ updatePlaybackControls() }, 180)
        }
        applyPlayPauseIcon(PlaybackStateStore.isPlaying)
        controlRow.addView(requireNotNull(playPauseControl))
        controlRow.addView(iconButton(R.drawable.ic_m3_chevron_right) { sendPlaybackIntent(this, PlaybackActions.ACTION_NEXT) })

        // Expand arrow indicator
        panelExpandArrow = TextView(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(36.dp, 44.dp)
            setOnClickListener { togglePanelExpand() }
        }
        panelExpandArrow?.let { arrow ->
            applyIconGlyph(arrow, R.drawable.ic_m3_expand_less)
            arrow.foreground?.setTint(colorTextSecondary)
        }
        controlRow.addView(panelExpandArrow)

        // ── Expanded content (initially hidden) ─────────────────────────────
        val expandedSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 0.dp, 16.dp, 8.dp)
            visibility = View.GONE
            // height = 0 initially so animation starts from 0
        }
        expandedPanelContent = expandedSection

        // Divider
        expandedSection.addView(View(this).apply {
            setBackgroundColor(colorOutline)
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1.dp).apply {
            topMargin = 2.dp; bottomMargin = 10.dp
        })

        // Status message row
        val expandedStatus = TextView(this).apply {
            text = PlaybackStateStore.statusMessage.ifBlank { "Original View" }
            setTextColor(colorTextSecondary)
            textSize = 12f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(0, 0, 0, 10.dp)
        }
        expandedSection.addView(expandedStatus)

        // Speed slider
        panelSpeedLabel = labeledSeekBar(
            menu = expandedSection,
            title = "Speed",
            min = 0.5f,
            max = 2.0f,
            current = PlaybackStateStore.rate,
            suffix = "x"
        ) { value -> adjustPlayback(rate = value, pitch = PlaybackStateStore.pitch) }

        // Pitch slider
        panelPitchLabel = labeledSeekBar(
            menu = expandedSection,
            title = "Pitch",
            min = 0.7f,
            max = 1.4f,
            current = PlaybackStateStore.pitch,
            suffix = ""
        ) { value -> adjustPlayback(rate = PlaybackStateStore.rate, pitch = value) }

        // Bottom row: Voice Studio + Queue
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8.dp, 0, 0)
        }
        bottomRow.addView(TextView(this).apply {
            text = if (PlaybackStateStore.queueCount == 0) "Queue empty"
                   else "Queue (${PlaybackStateStore.queueCount})"
            setTextColor(colorTextSecondary)
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        bottomRow.addView(TextView(this).apply {
            text = "Voice Studio ›"
            setTextColor(colorPrimary)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { openVoiceAndLanguage() }
        })
        expandedSection.addView(bottomRow)

        controls.addView(progressStrip)
        controls.addView(controlRow)
        controls.addView(expandedSection)
        controlsOuter.addView(controls)

        root.addView(fragmentContainer)
        root.addView(toolbar)
        root.addView(controlsOuter)
        setContentView(root)
        ViewCompat.requestApplyInsets(root)
        // Minimized chrome is the default. INVISIBLE (not GONE) so the bars are
        // measured on the first layout pass and can slide in from their real heights.
        listOfNotNull(toolbarChrome, bottomChrome).forEach { it.visibility = View.INVISIBLE }
    }

    private fun resetInactivityTimer() {
        keepAwakeTimerJob?.cancel()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        keepAwakeTimerJob = lifecycleScope.launch {
            delay(20L * 60L * 1000L) // 20 minutes
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Silent reading in this viewer happens while MainActivity is stopped, so the app's
    // session timer isn't running — without this, Original-mode reading earned no reading
    // time and no streak day. Wall-clock while resumed, recorded on pause.
    private var viewerReadingStartedAt = 0L

    override fun onResume() {
        super.onResume()
        viewerReadingStartedAt = System.currentTimeMillis()
        updatePlaybackControls()
        if (chromeVisible) scheduleChromeAutoHide()
        resetInactivityTimer()
    }

    override fun onPause() {
        super.onPause()
        saveCurrentProgress()
        recordViewerReadingSession()
        keepAwakeTimerJob?.cancel()
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun recordViewerReadingSession() {
        val doc = document ?: return
        val startedAt = viewerReadingStartedAt
        viewerReadingStartedAt = 0L
        if (startedAt <= 0L) return
        val delta = System.currentTimeMillis() - startedAt
        // Cap defensively: a forgotten open viewer overnight shouldn't count 8 hours.
        if (delta in 1_000L..(3L * 60L * 60L * 1000L)) {
            lifecycleScope.launch(Dispatchers.IO) {
                repository.recordDocReadingTime(doc.id, delta)
                repository.recordDocumentRead(doc.id, doc.title)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        resetInactivityTimer()
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        highlightJob?.cancel()
        chromeHideJob?.cancel()
        super.onDestroy()
    }

    private fun saveCurrentProgress() {
        val metadata = document ?: return
        val view = pdfView ?: return
        val visiblePage = runCatching { view.firstVisiblePage }.getOrNull() ?: return
        val model = readerTextModel ?: return
        // Eyes don't move the voice: while TTS is actively reading this document, the page
        // being LOOKED at must not overwrite the sentence being SPOKEN — that stomped the
        // live position and caused playback to restart from far above after navigation.
        if (PlaybackStateStore.isPlaying && PlaybackStateStore.activeDocumentId == metadata.id) return

        lifecycleScope.launch(Dispatchers.IO) {
            val pageNum = visiblePage + 1
            var sentenceIndex = model.sentences.indexOfFirst { it.pageNumber == pageNum }
            if (sentenceIndex == -1) {
                sentenceIndex = model.sentences.mapIndexed { idx, s -> idx to abs(s.pageNumber - pageNum) }
                    .minByOrNull { it.second }?.first ?: -1
            }
            
            if (sentenceIndex != -1) {
                repository.updateProgress(metadata.id, sentenceIndex, model.sentences.size)
                if (PlaybackStateStore.activeDocumentId == metadata.id) {
                    PlaybackStateStore.currentIndex = sentenceIndex
                }
            }
        }
    }

    override fun onActionModeStarted(mode: android.view.ActionMode?) {
        super.onActionModeStarted(mode)
        val menu = mode?.menu ?: return

        val readItem = menu.findItem(1001) ?: menu.add(0, 1001, 1, "Read from here")
        readItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS or android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        readItem.titleCondensed = "Read"
        readItem.isVisible = true
        readItem.isEnabled = true
        readItem.setOnMenuItemClickListener {
            performActionOnCopiedSelection(menu, mode) { text ->
                jumpToText(text)
            }
            true
        }

        val fixPronunciationItem = menu.findItem(1002) ?: menu.add(0, 1002, 2, "Fix pronunciation")
        fixPronunciationItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS or android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        fixPronunciationItem.isVisible = true
        fixPronunciationItem.isEnabled = true
        fixPronunciationItem.setOnMenuItemClickListener {
            performActionOnCopiedSelection(menu, mode) { text ->
                PlaybackStateStore.pendingPronunciationFixWord = text.replace(Regex("\\s+"), " ").trim().take(120)
                val intent = Intent(this@VeritasPdfViewerActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
                finish()
            }
            true
        }

        val bookmarkItem = menu.findItem(1003) ?: menu.add(0, 1003, 3, "Add bookmark")
        bookmarkItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM or android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        bookmarkItem.isVisible = true
        bookmarkItem.isEnabled = true
        bookmarkItem.setOnMenuItemClickListener {
            performActionOnCopiedSelection(menu, mode) { text ->
                val index = findSentenceIndexForText(text)
                lifecycleScope.launch(Dispatchers.IO) {
                    val docId = document?.id ?: return@launch
                    repository.toggleAnnotation(docId, index, AnnotationType.BOOKMARK)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@VeritasPdfViewerActivity, "Bookmark toggled.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }

        val noteItem = menu.findItem(1004) ?: menu.add(0, 1004, 4, "Add note")
        noteItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM or android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        noteItem.isVisible = true
        noteItem.isEnabled = true
        noteItem.setOnMenuItemClickListener {
            performActionOnCopiedSelection(menu, mode) { text ->
                val index = findSentenceIndexForText(text)
                showAddNoteDialog(index)
            }
            true
        }

        val askAiItem = menu.findItem(1005) ?: menu.add(0, 1005, 5, "Ask AI")
        askAiItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM or android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        askAiItem.isVisible = true
        askAiItem.isEnabled = true
        askAiItem.setOnMenuItemClickListener {
            performActionOnCopiedSelection(menu, mode) { text ->
                val cleanText = text.replace(Regex("\\s+"), " ").trim()
                appendVocabularyWord(cleanText, "Asked AI for explanation.")
                lifecycleScope.launch(Dispatchers.IO) {
                    val settings = repository.loadAskAiSettings()
                    withContext(Dispatchers.Main) {
                        askAiWithSelection(this@VeritasPdfViewerActivity, settings, cleanText)
                    }
                }
            }
            true
        }

        val copyItem = menu.findItem(1006) ?: menu.add(0, 1006, 6, "Copy")
        copyItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER)
        copyItem.isVisible = true
        copyItem.isEnabled = true
        copyItem.setOnMenuItemClickListener {
            performActionOnCopiedSelection(menu, mode) { text ->
                copyTextToClipboard(this@VeritasPdfViewerActivity, "Veritas selection", text)
            }
            true
        }

        val shareItem = menu.findItem(1007) ?: menu.add(0, 1007, 7, "Share")
        shareItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER)
        shareItem.isVisible = true
        shareItem.isEnabled = true
        shareItem.setOnMenuItemClickListener {
            performActionOnCopiedSelection(menu, mode) { text ->
                sharePlainText(this@VeritasPdfViewerActivity, "Veritas selection", text)
            }
            true
        }

        mode.invalidate()
    }

    private fun performActionOnCopiedSelection(
        menu: android.view.Menu,
        mode: android.view.ActionMode,
        onTextRetrieved: (String) -> Unit
    ) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val oldClip = clipboard.primaryClip
        val sentinel = "veritas-pdf-selection-${System.nanoTime()}"
        clipboard.setPrimaryClip(ClipData.newPlainText("Veritas selection marker", sentinel))

        val copyItem = findCopyMenuItem(menu)
        if (copyItem != null) {
            val copyStarted = performCopyMenuAction(menu, copyItem)
            if (!copyStarted) {
                restoreClipboard(clipboard, oldClip)
                Toast.makeText(this@VeritasPdfViewerActivity, "Copy action did not start.", Toast.LENGTH_SHORT).show()
                mode.finish()
                return
            }
            pollCopiedSelection(clipboard, oldClip, sentinel, mode, 0, onTextRetrieved)
        } else {
            Toast.makeText(this@VeritasPdfViewerActivity, "Copy action is not available.", Toast.LENGTH_SHORT).show()
            restoreClipboard(clipboard, oldClip)
            mode.finish()
        }
    }

    private fun pollCopiedSelection(
        clipboard: ClipboardManager,
        previousClip: ClipData?,
        sentinel: String,
        mode: android.view.ActionMode,
        attempt: Int,
        onTextRetrieved: (String) -> Unit
    ) {
        val delayMs = when (attempt) {
            0 -> 180L
            1 -> 260L
            else -> 360L
        }
        window.decorView.postDelayed({
            val newClip = clipboard.primaryClip
            val selectedText = newClip?.getItemAt(0)?.coerceToText(this@VeritasPdfViewerActivity)?.toString()
            if (!selectedText.isNullOrBlank() && selectedText != sentinel) {
                restoreClipboard(clipboard, previousClip)
                mode.finish()
                onTextRetrieved(selectedText)
            } else if (attempt < 2) {
                pollCopiedSelection(clipboard, previousClip, sentinel, mode, attempt + 1, onTextRetrieved)
            } else {
                Toast.makeText(this@VeritasPdfViewerActivity, "Could not extract selected text.", Toast.LENGTH_SHORT).show()
                restoreClipboard(clipboard, previousClip)
                mode.finish()
            }
        }, delayMs)
    }

    private fun findSentenceIndexForText(selectedText: String): Int {
        val clean = selectedText.trim()
        if (clean.isBlank()) return PlaybackStateStore.currentIndex
        val model = readerTextModel ?: return PlaybackStateStore.currentIndex
        val index = model.sentences.indexOfFirst { it.text.contains(clean, ignoreCase = true) }
        if (index != -1) return index
        val bestMatch = model.sentences.maxByOrNull { sentence ->
            val common = sentence.text.split(" ").filter { it.length > 3 && clean.contains(it, ignoreCase = true) }
            common.size
        }
        return bestMatch?.index ?: PlaybackStateStore.currentIndex
    }

    private fun appendVocabularyWord(word: String, explanation: String) {
        val docId = document?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val existing = repository.loadGeneralNotes().toMutableList()
            val targetTitle = "__vocab__$docId"
            val vocabIndex = existing.indexOfFirst { it.title == targetTitle }
            val now = System.currentTimeMillis()
            val formattedTime = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(now))

            val currentIndex = findSentenceIndexForText(word)
            val textModel = readerTextModel
            val part = textModel?.partForSentence(currentIndex)
            val sectionNum = (part?.index ?: 0) + 1

            val entryText = buildString {
                appendLine(word.trim())
                appendLine("  $explanation")
                appendLine("  (looked up: Section $sectionNum, sentence ${currentIndex + 1})")
                append("  [$formattedTime]")
            }

            if (vocabIndex != -1) {
                val oldNote = existing[vocabIndex]
                val newContent = if (oldNote.content.isBlank()) entryText else oldNote.content + "\n\n" + entryText
                existing[vocabIndex] = oldNote.copy(content = newContent, updatedAt = now)
            } else {
                val newNote = GeneralNote(
                    id = java.util.UUID.randomUUID().toString(),
                    title = targetTitle,
                    content = entryText,
                    updatedAt = now
                )
                existing.add(0, newNote)
            }

            repository.saveGeneralNotes(existing)
        }
    }

    private fun showAddNoteDialog(sentenceIndex: Int) {
        val docId = document?.id ?: return
        val input = android.widget.EditText(this).apply {
            hint = "Write sentence note..."
            setTextColor(colorTextPrimary)
            setHintTextColor(colorTextSecondary)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(16.dp, 12.dp, 16.dp, 12.dp)
        }
        val container = FrameLayout(this).apply {
            val padding = 16.dp
            setPadding(padding, 8.dp, padding, 8.dp)
            addView(input)
        }
        val titleView = TextView(this).apply {
            text = "Add note to sentence ${sentenceIndex + 1}"
            setTextColor(colorTextPrimary)
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(20.dp, 16.dp, 20.dp, 4.dp)
        }
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        repository.upsertAnnotation(
                            documentId = docId,
                            chunkIndex = sentenceIndex,
                            type = AnnotationType.NOTE,
                            note = text
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@VeritasPdfViewerActivity, "Note saved.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(colorSurface))
        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(colorPrimary)
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(colorTextSecondary)
    }

    private fun findCopyMenuItem(menu: android.view.Menu): android.view.MenuItem? {
        menu.findItem(android.R.id.copy)?.let { return it }
        for (index in 0 until menu.size()) {
            val item = menu.getItem(index)
            val title = item.title?.toString().orEmpty()
            val description = item.contentDescription?.toString().orEmpty()
            if (title.contains("copy", ignoreCase = true) || description.contains("copy", ignoreCase = true)) {
                return item
            }
            item.subMenu?.let { subMenu ->
                findCopyMenuItem(subMenu)?.let { return it }
            }
        }
        return null
    }

    private fun performCopyMenuAction(menu: android.view.Menu, item: android.view.MenuItem): Boolean {
        if (menu.performIdentifierAction(item.itemId, 0)) return true
        if (item.itemId != android.R.id.copy && menu.performIdentifierAction(android.R.id.copy, 0)) return true
        item.subMenu?.let { subMenu ->
            findCopyMenuItem(subMenu)?.let { nested ->
                if (subMenu.performIdentifierAction(nested.itemId, 0)) return true
            }
        }
        return false
    }

    private fun restoreClipboard(clipboard: ClipboardManager, previousClip: ClipData?) {
        if (previousClip != null) {
            clipboard.setPrimaryClip(previousClip)
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip()
        }
    }

    private fun jumpToText(selectedText: String) {
        val docId = document?.id ?: return
        val currentPage = pdfView?.firstVisiblePage?.plus(1)
        val model = readerTextModel
        val match = if (model != null) {
            PdfSelectionLocator.findMatch(
                selectedText = selectedText,
                model = model,
                currentPage = currentPage,
                preferredSentenceIndex = PlaybackStateStore.currentIndex
            )
        } else {
            PdfSelectionLocator.findMatch(selectedText, extractedChunks)
        }

        if (match != null) {
            val intent = Intent(this, PlaybackService::class.java).apply {
                action = PlaybackActions.ACTION_JUMP_TO
                putExtra(PlaybackActions.EXTRA_DOCUMENT_ID, docId)
                putExtra(PlaybackActions.EXTRA_START_INDEX, match.chunkIndex)
                putExtra(PlaybackActions.EXTRA_CHAR_OFFSET, 0)
            }
            startService(intent)
            Toast.makeText(this, "Reading from selection", Toast.LENGTH_SHORT).show()
            updatePlaybackControls()
        } else {
            Toast.makeText(this, "Text not found in extracted document.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSearch() {
        val fragment = viewerFragment ?: return
        runCatching {
            fragment.isTextSearchActive = !fragment.isTextSearchActive
        }.onFailure {
            Toast.makeText(this, "Search is not available for this PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateViewer() {
        requestedOrientation = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    private fun openOriginal() {
        val metadata = document ?: return
        val uri = repository.originalUri(metadata) ?: run {
            Toast.makeText(this, "Could not prepare the file for opening.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, metadata.originalMimeType.ifBlank { "application/pdf" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Open original document")) }
    }

    enum class PaperToneMode {
        ACTIVE_THEME,
        DARK,
        NATURAL_WHITE
    }

    data class OverflowMenuItem(
        val title: String,
        val subtitle: String? = null,
        val iconRes: Int? = null,
        val isHeader: Boolean = false,
        val enabled: Boolean = true,
        val action: (() -> Unit)? = null
    )

    private var paperToneMode = PaperToneMode.ACTIVE_THEME

    private fun toggleFullScreen() {
        if (chromeVisible) {
            hideChrome()
        } else {
            showChrome(keepVisible = true)
        }
    }

    private fun applyPaperToneMode() {
        when (paperToneMode) {
            PaperToneMode.ACTIVE_THEME -> {
                val settings = repository.loadReaderSettings()
                val resolvedTheme = if (settings.themeId == "system") {
                    val mode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                    if (mode == android.content.res.Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
                } else settings.themeId
                val scheme = veritasPackColorScheme(veritasColorScheme(resolvedTheme, this@VeritasPdfViewerActivity), settings.themePackId)
                val bgR = scheme.surface.red
                val bgG = scheme.surface.green
                val bgB = scheme.surface.blue
                val fgR = scheme.onSurface.red
                val fgG = scheme.onSurface.green
                val fgB = scheme.onSurface.blue

                val deltaR = bgR - fgR
                val deltaG = bgG - fgG
                val deltaB = bgB - fgB

                val paint = android.graphics.Paint().apply {
                    colorFilter = android.graphics.ColorMatrixColorFilter(floatArrayOf(
                        0.299f * deltaR, 0.587f * deltaR, 0.114f * deltaR, 0.0f, fgR * 255.0f,
                        0.299f * deltaG, 0.587f * deltaG, 0.114f * deltaG, 0.0f, fgG * 255.0f,
                        0.299f * deltaB, 0.587f * deltaB, 0.114f * deltaB, 0.0f, fgB * 255.0f,
                        0.0f,            0.0f,            0.0f,            1.0f, 0.0f
                    ))
                }
                fragmentContainer?.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
            }
            PaperToneMode.DARK -> {
                val colorMatrix = android.graphics.ColorMatrix(floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                ))
                val paint = android.graphics.Paint().apply {
                    colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
                }
                fragmentContainer?.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
            }
            PaperToneMode.NATURAL_WHITE -> {
                fragmentContainer?.setLayerType(View.LAYER_TYPE_NONE, null)
            }
        }
    }

    private fun cyclePaperToneMode() {
        paperToneMode = when (paperToneMode) {
            PaperToneMode.ACTIVE_THEME -> PaperToneMode.DARK
            PaperToneMode.DARK -> PaperToneMode.NATURAL_WHITE
            PaperToneMode.NATURAL_WHITE -> PaperToneMode.ACTIVE_THEME
        }
        applyPaperToneMode()
        val toastMessage = when (paperToneMode) {
            PaperToneMode.ACTIVE_THEME -> "Active Theme Paper Tone"
            PaperToneMode.DARK -> "Dark Paper (High Contrast)"
            PaperToneMode.NATURAL_WHITE -> "Natural Paper Colors (White)"
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showJumpToPageDialog() {
        val total = runCatching { pdfView?.pdfDocument?.pageCount }.getOrNull() ?: 1
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "1–$total"
            setTextColor(colorTextPrimary)
            setHintTextColor(colorTextSecondary)
            setPadding(16.dp, 12.dp, 16.dp, 12.dp)
        }
        val layout = FrameLayout(this).apply {
            setPadding(20.dp, 8.dp, 20.dp, 8.dp)
            addView(input)
        }
        val titleView = TextView(this).apply {
            text = "Jump to Page"
            setTextColor(colorTextPrimary)
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(20.dp, 16.dp, 20.dp, 4.dp)
        }
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(layout)
            .setPositiveButton("Go") { _, _ ->
                val num = input.text.toString().trim().toIntOrNull()
                if (num != null && num in 1..total) {
                    pdfView?.scrollToPage(num - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(colorSurface))
        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(colorPrimary)
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(colorTextSecondary)
    }

    private fun sharePdf() {
        val metadata = document ?: return
        val uri = repository.originalUri(metadata) ?: run {
            Toast.makeText(this, "Could not locate PDF file to share.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Share PDF Document")) }
    }

    private fun showTopMenu(anchor: View) {
        showChrome(keepVisible = true)
        val pageCount = runCatching { pdfView?.pdfDocument?.pageCount }.getOrNull() ?: 1
        val items = mutableListOf<OverflowMenuItem>()

        // Display
        items.add(OverflowMenuItem(title = "Display", isHeader = true))
        items.add(OverflowMenuItem(
            title = if (chromeVisible) "Full Screen Mode" else "Exit Full Screen",
            subtitle = if (chromeVisible) "Hide toolbar & player bars" else "Show toolbar & player bars",
            iconRes = R.drawable.ic_m3_fullscreen,
            action = ::toggleFullScreen
        ))
        items.add(OverflowMenuItem(
            title = "Fit to Screen",
            subtitle = "Reset zoom and fit page width",
            iconRes = R.drawable.ic_m3_fitscreen,
            action = { pdfView?.scrollToPage(pdfView?.firstVisiblePage ?: 0) }
        ))
        items.add(OverflowMenuItem(
            title = "Rotate View (90°)",
            subtitle = "Switch screen orientation",
            iconRes = R.drawable.ic_m3_rotate_right,
            action = ::rotateViewer
        ))
        items.add(OverflowMenuItem(
            title = when (paperToneMode) {
                PaperToneMode.ACTIVE_THEME -> "Theme-Adapted Paper (Active)"
                PaperToneMode.DARK -> "Theme-Adapted Paper (Dark)"
                PaperToneMode.NATURAL_WHITE -> "Natural Paper Colors (White)"
            },
            subtitle = when (paperToneMode) {
                PaperToneMode.ACTIVE_THEME -> "Tap for dark paper"
                PaperToneMode.DARK -> "Tap for authentic white paper"
                PaperToneMode.NATURAL_WHITE -> "Tap to adapt paper to active theme"
            },
            iconRes = R.drawable.ic_m3_contrast,
            action = ::cyclePaperToneMode
        ))

        // Navigation
        if (pageCount > 1) {
            items.add(OverflowMenuItem(title = "Navigation", isHeader = true))
            items.add(OverflowMenuItem(
                title = "Jump to Page...",
                subtitle = "Go to 1–$pageCount",
                iconRes = R.drawable.ic_m3_jump_page,
                action = ::showJumpToPageDialog
            ))
            items.add(OverflowMenuItem(
                title = "First Page (1)",
                subtitle = "Jump to beginning",
                iconRes = R.drawable.ic_m3_first_page,
                action = { pdfView?.scrollToPage(0) }
            ))
            items.add(OverflowMenuItem(
                title = "Last Page ($pageCount)",
                subtitle = "Jump to end of document",
                iconRes = R.drawable.ic_m3_last_page,
                action = { pdfView?.scrollToPage(pageCount - 1) }
            ))
        }

        // Reading & Audio
        items.add(OverflowMenuItem(title = "Reading & Audio", isHeader = true))
        items.add(OverflowMenuItem(
            title = if (isSyncEnabled) "Live Sync (Enabled)" else "Live Sync (Disabled)",
            subtitle = if (isSyncEnabled) "Tap to pause auto-highlighting" else "Tap to sync speech with PDF",
            iconRes = R.drawable.ic_m3_link,
            action = {
                isSyncEnabled = !isSyncEnabled
                updateSyncPillUi()
                if (isSyncEnabled) {
                    pendingManualPageSync = true
                    lifecycleScope.launch { updateSentenceHighlight(forceSync = true) }
                    Toast.makeText(this@VeritasPdfViewerActivity, "Live sync enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@VeritasPdfViewerActivity, "Live sync disabled", Toast.LENGTH_SHORT).show()
                }
            }
        ))
        items.add(OverflowMenuItem(
            title = "Switch to Text Reader",
            subtitle = "Flowing text, notes & speed reader",
            iconRes = R.drawable.ic_m3_book,
            action = ::finish
        ))
        items.add(OverflowMenuItem(
            title = "Voice Studio",
            subtitle = "Narrators, speed & audio tuning",
            iconRes = R.drawable.ic_m3_mic,
            action = ::togglePanelExpand
        ))

        // File & Share
        items.add(OverflowMenuItem(title = "File & Share", isHeader = true))
        items.add(OverflowMenuItem(
            title = "Share Original File",
            subtitle = "Send to other apps",
            iconRes = R.drawable.ic_m3_share,
            action = ::sharePdf
        ))
        items.add(OverflowMenuItem(
            title = "Open in External App",
            subtitle = "Use system PDF or photo viewer",
            iconRes = R.drawable.ic_m3_open_in_new,
            action = ::openOriginal
        ))
        items.add(OverflowMenuItem(
            title = "Document Information",
            subtitle = "Metadata, length & format",
            iconRes = R.drawable.ic_m3_info,
            action = ::showDocInfoDialog
        ))

        showOverflowPopup(anchor, items)
    }

    private fun showOverflowPopup(
        anchor: View,
        items: List<OverflowMenuItem>
    ) {
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(300.dp, (resources.displayMetrics.heightPixels * 0.70f).toInt())
            isVerticalScrollBarEnabled = false
        }
        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12.dp, 0, 12.dp)
            background = rounded(colorSurface, 18.dp)
        }
        scroll.addView(menu)

        // Header Title matching Screenshot 2
        val headerCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp, 4.dp, 18.dp, 6.dp)
        }
        val headerTitle = TextView(this).apply {
            text = "Document Tools"
            setTextColor(colorTextPrimary)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
        }
        val headerSubtitle = TextView(this).apply {
            text = document?.title.orEmpty()
            setTextColor(colorTextSecondary)
            textSize = 11f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(0, 2.dp, 0, 0)
        }
        headerCol.addView(headerTitle)
        headerCol.addView(headerSubtitle)
        menu.addView(headerCol)

        val popup = PopupWindow(scroll, 300.dp, LinearLayout.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = 14f
            isOutsideTouchable = true
            isClippingEnabled = false
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        showChrome(keepVisible = true)
        chromeMenuOpen = true

        items.forEach { item ->
            if (item.isHeader) {
                val divider = View(this).apply {
                    setBackgroundColor(colorSyncBackground)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1.dp).apply {
                        setMargins(16.dp, 6.dp, 16.dp, 6.dp)
                    }
                }
                menu.addView(divider)

                val headerView = TextView(this).apply {
                    text = item.title
                    setTextColor(colorPrimary)
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(18.dp, 4.dp, 18.dp, 2.dp)
                }
                menu.addView(headerView)
            } else {
                val itemRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(16.dp, 9.dp, 16.dp, 9.dp)
                    isClickable = item.enabled
                    isFocusable = item.enabled
                    background = StateListDrawable().apply {
                        addState(intArrayOf(android.R.attr.state_pressed), rounded(colorSyncBackground, 8.dp))
                    }

                    if (item.iconRes != null) {
                        val iconView = ImageView(this@VeritasPdfViewerActivity).apply {
                            setImageResource(item.iconRes)
                            setColorFilter(colorPrimary)
                            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                                marginEnd = 14.dp
                            }
                        }
                        addView(iconView)
                    } else {
                        val spacer = View(this@VeritasPdfViewerActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                                marginEnd = 14.dp
                            }
                        }
                        addView(spacer)
                    }

                    val textCol = LinearLayout(this@VeritasPdfViewerActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }
                    val titleView = TextView(this@VeritasPdfViewerActivity).apply {
                        text = item.title
                        setTextColor(if (item.enabled) colorTextPrimary else colorTextSecondary)
                        textSize = 13.5f
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    textCol.addView(titleView)
                    if (item.subtitle != null) {
                        val subView = TextView(this@VeritasPdfViewerActivity).apply {
                            text = item.subtitle
                            setTextColor(colorTextSecondary)
                            textSize = 10.5f
                            setPadding(0, 2.dp, 0, 0)
                        }
                        textCol.addView(subView)
                    }
                    addView(textCol)

                    setOnClickListener {
                        popup.dismiss()
                        item.action?.invoke()
                    }
                }
                menu.addView(itemRow)
            }
        }

        popup.setOnDismissListener {
            chromeMenuOpen = false
            scheduleChromeAutoHide()
        }
        popup.showAtLocation(window.decorView, Gravity.TOP or Gravity.END, 12.dp, statusBarHeight() + 56.dp)
    }

    private fun showDocInfoDialog() {
        val metadata = document ?: return
        val items = listOf(
            "Title" to metadata.title,
            "Format" to "PDF (Original Document)",
            "Total Sentences" to "${metadata.sentenceCount}",
            "Est. Reading Time" to "${(metadata.sentenceCount * 2.5 / 60).toInt().coerceAtLeast(1)} min",
            "Added Date" to SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(metadata.createdAt))
        )
        val view = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp, 16.dp, 20.dp, 16.dp)
            items.forEach { (label, value) ->
                val row = LinearLayout(this@VeritasPdfViewerActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 6.dp, 0, 6.dp)
                    addView(TextView(this@VeritasPdfViewerActivity).apply {
                        text = label
                        setTextColor(colorTextSecondary)
                        textSize = 13f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    })
                    addView(TextView(this@VeritasPdfViewerActivity).apply {
                        text = value
                        setTextColor(colorTextPrimary)
                        textSize = 13f
                        typeface = Typeface.DEFAULT_BOLD
                    })
                }
                addView(row)
            }
        }
        val titleView = TextView(this).apply {
            text = "Document Details"
            setTextColor(colorTextPrimary)
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(20.dp, 16.dp, 20.dp, 4.dp)
        }
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(view)
            .setPositiveButton("Close", null)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(colorSurface))
        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(colorPrimary)
    }

    private fun updateSyncPillUi() {
        val pill = syncPill ?: return
        val label = syncLabel ?: return
        if (isSyncEnabled) {
            pill.background = rounded(colorSyncBackground, 14.dp)
            label.setTextColor(colorPrimary)
            label.text = "🔗 Live Sync"
        } else {
            pill.background = rounded(colorSurface, 14.dp)
            label.setTextColor(colorTextSecondary)
            label.text = "🔗 Sync Off"
        }
    }

    private fun showPlaybackMenu(anchor: View) {
        showChrome(keepVisible = true)
        chromeMenuOpen = true
        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 14.dp, 16.dp, 14.dp)
            background = rounded(colorSurface, 18.dp)
        }
        val popup = PopupWindow(menu, 330.dp, LinearLayout.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = 12f
            isOutsideTouchable = true
            isClippingEnabled = false
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val status = PlaybackStateStore.statusMessage.ifBlank {
            if (PlaybackStateStore.isPlaying) "Reading." else "Paused."
        }
        menu.addView(TextView(this).apply {
            text = "Playback"
            setTextColor(colorTextPrimary)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 10.dp)
        })
        menu.addView(TextView(this).apply {
            text = status
            setTextColor(colorTextSecondary)
            textSize = 15f
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setPadding(0, 0, 0, 12.dp)
        })
        menu.addView(View(this).apply {
            setBackgroundColor(colorOutline)
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1.dp).apply {
            bottomMargin = 12.dp
        })
        val speedLabel = labeledSeekBar(
            menu = menu,
            title = "Speed",
            min = 0.5f,
            max = 2.0f,
            current = PlaybackStateStore.rate,
            suffix = "x"
        ) { value -> adjustPlayback(rate = value, pitch = PlaybackStateStore.pitch) }
        val pitchLabel = labeledSeekBar(
            menu = menu,
            title = "Pitch",
            min = 0.7f,
            max = 1.4f,
            current = PlaybackStateStore.pitch,
            suffix = ""
        ) { value -> adjustPlayback(rate = PlaybackStateStore.rate, pitch = value) }
        menu.addView(TextView(this).apply {
            text = "Voice and language"
            setTextColor(colorTextPrimary)
            textSize = 15f
            setPadding(0, 12.dp, 0, 12.dp)
            setOnClickListener {
                popup.dismiss()
                openVoiceAndLanguage()
            }
        })
        menu.addView(TextView(this).apply {
            text = if (PlaybackStateStore.queueCount == 0) "Queue empty" else "Continue queue (${PlaybackStateStore.queueCount})"
            setTextColor(colorTextSecondary)
            textSize = 14f
            setPadding(0, 8.dp, 0, 0)
        })
        popup.setOnDismissListener {
            speedLabel.text = ""
            pitchLabel.text = ""
            chromeMenuOpen = false
            scheduleChromeAutoHide()
        }
        popup.showAtLocation(window.decorView, Gravity.BOTTOM or Gravity.END, 12.dp, navigationBarHeight() + 92.dp)
    }

    private fun labeledSeekBar(
        menu: LinearLayout,
        title: String,
        min: Float,
        max: Float,
        current: Float,
        suffix: String,
        onCommitted: (Float) -> Unit
    ): TextView {
        val steps = ((max - min) * 100).toInt().coerceAtLeast(1)
        val label = TextView(this).apply {
            text = "$title ${"%.2f".format(current)}$suffix"
            setTextColor(colorTextPrimary)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 4.dp, 0, 4.dp)
        }
        menu.addView(label)
        menu.addView(SeekBar(this).apply {
            this.max = steps
            progress = (((current.coerceIn(min, max) - min) * 100).toInt()).coerceIn(0, steps)
            progressTintList = ColorStateList.valueOf(colorPrimary)
            thumbTintList = ColorStateList.valueOf(colorPrimary)
            progressBackgroundTintList = ColorStateList.valueOf(colorOutline)
            setPadding(0, 0, 0, 8.dp)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = (min + (progress / 100f)).coerceIn(min, max)
                    label.text = "$title ${"%.2f".format(value)}$suffix"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val value = (min + ((seekBar?.progress ?: 0) / 100f)).coerceIn(min, max)
                    onCommitted(value)
                }
            })
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        return label
    }

    private fun adjustPlayback(rate: Float = PlaybackStateStore.rate, pitch: Float = PlaybackStateStore.pitch) {
        val voiceSettings = repository.loadVoiceSettings()
        val newRate = rate.coerceIn(0.5f, 2.0f)
        val newPitch = pitch.coerceIn(0.7f, 1.4f)
        PlaybackStateStore.rate = newRate
        PlaybackStateStore.pitch = newPitch
        repository.saveVoiceSettings(
            voiceSettings.copy(preferredRate = newRate, preferredPitch = newPitch)
        )
        if (PlaybackStateStore.isForegroundActive || PlaybackStateStore.activeDocumentId != null) {
            sendPlaybackIntent(
                this,
                PlaybackActions.ACTION_UPDATE_PLAYBACK_SETTINGS,
                rate = newRate,
                pitch = newPitch
            )
        }
    }

    private fun openVoiceAndLanguage() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_VOICE_STUDIO, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun showMenu(
        anchor: View,
        actions: List<Pair<String, () -> Unit>>,
        alignTopEnd: Boolean = false,
        alignBottom: Boolean = false
    ) {
        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8.dp, 0, 8.dp)
            background = rounded(colorSurface, 18.dp)
        }
        val popup = PopupWindow(menu, 260.dp, LinearLayout.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = 10f
            isOutsideTouchable = true
            isClippingEnabled = false
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        showChrome(keepVisible = true)
        chromeMenuOpen = true
        actions.forEach { (label, action) ->
            menu.addView(TextView(this).apply {
                text = label
                setTextColor(colorTextPrimary)
                textSize = 16f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(18.dp, 12.dp, 18.dp, 12.dp)
                setOnClickListener {
                    popup.dismiss()
                    action()
                }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        popup.setOnDismissListener {
            chromeMenuOpen = false
            scheduleChromeAutoHide()
        }
        if (alignBottom) {
            popup.showAtLocation(window.decorView, Gravity.BOTTOM or Gravity.END, 12.dp, navigationBarHeight() + 92.dp)
        } else if (alignTopEnd) {
            popup.showAtLocation(window.decorView, Gravity.TOP or Gravity.END, 8.dp, statusBarHeight() + 56.dp)
        } else {
            popup.showAsDropDown(anchor, -230.dp, 0)
        }
    }

    private fun startHighlightUpdates() {
        if (highlightJob?.isActive == true) return
        highlightJob = lifecycleScope.launch {
            while (true) {
                updateSentenceHighlight()
                updatePlaybackControls()
                delay(900)
            }
        }
    }

    private fun loadHighlightTextAsync(metadata: SavedDocument) {
        lifecycleScope.launch {
            val model = withContext(Dispatchers.IO) {
                ReaderTextModelCache.get(metadata.id, repository.readText(metadata), metadata.pageCount)
            }
            readerTextModel = model
            extractedChunks = model.sentences.map { it.text }
        }
    }

    private fun schedulePdfViewLookup(fragment: PdfViewerFragment) {
        lifecycleScope.launch {
            repeat(24) {
                val found = fragment.view?.let(::findPdfView) ?: fragmentContainer?.let(::findPdfView)
                if (found != null) {
                    pdfView = found
                    found.setOnTouchListener { _, event ->
                        handleDocumentChromeTouch(found, event)
                        false
                    }
                    startHighlightUpdates()
                    
                    // Initial scroll to saved page
                    launch {
                        val metadata = document ?: return@launch
                        var retryCount = 0
                        while (readerTextModel == null && retryCount < 50) {
                            delay(100)
                            retryCount++
                        }
                        val model = readerTextModel ?: return@launch
                        
                        var docRetryCount = 0
                        while (runCatching { found.pdfDocument }.getOrNull() == null && docRetryCount < 50) {
                            delay(100)
                            docRetryCount++
                        }
                        val pdfDoc = runCatching { found.pdfDocument }.getOrNull() ?: return@launch
                        
                        val safeIndex = metadata.currentIndex.coerceIn(0, model.sentences.lastIndex.coerceAtLeast(0))
                        val targetPage = model.sentences.getOrNull(safeIndex)?.pageNumber?.minus(1) ?: 0
                        val pageCount = pdfDoc.pageCount.coerceAtLeast(1)
                        val scrollPage = targetPage.coerceIn(0, pageCount - 1)
                        runCatching { found.scrollToPage(scrollPage) }
                    }
                    
                    return@launch
                }
                delay(150)
            }
        }
    }

    private fun findPdfView(view: View): PdfView? {
        if (view is PdfView) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findPdfView(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private fun handleDocumentChromeTouch(view: View, event: MotionEvent) {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                tapDownX = event.x
                tapDownY = event.y
                tapDownTime = event.eventTime
                tapMoved = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - tapDownX
                val dy = event.y - tapDownY
                if ((dx * dx + dy * dy) > (slop * slop)) {
                    // The finger is scrolling, not tapping — glide the bars away
                    // the moment the scroll starts.
                    if (!tapMoved && chromeVisible) hideChrome()
                    tapMoved = true
                }
            }
            MotionEvent.ACTION_UP -> {
                val quickTap = event.eventTime - tapDownTime < 360L
                val dx = event.x - tapDownX
                val dy = event.y - tapDownY
                val moved = tapMoved || (dx * dx + dy * dy) > (slop * slop)
                // A clean tap anywhere on the document toggles the chrome.
                if (!moved && quickTap) {
                    toggleChromeFromDocumentTap()
                }
            }
            MotionEvent.ACTION_CANCEL -> tapMoved = false
        }
    }

    private fun toggleChromeFromDocumentTap() {
        if (chromeMenuOpen) return
        if (chromeVisible) {
            hideChrome()
        } else {
            showChrome()
            scheduleChromeAutoHide()
        }
    }

    private fun showChrome(keepVisible: Boolean = false) {
        chromeVisible = true
        toolbarChrome?.let { slideChromeIn(it, offscreenY = -it.height.toFloat()) }
        bottomChrome?.let { slideChromeIn(it, offscreenY = it.height.toFloat()) }
        chromeHideJob?.cancel()
        if (!keepVisible) scheduleChromeAutoHide()
    }

    private fun hideChrome() {
        chromeHideJob?.cancel()
        // While the bottom panel is expanded the user is actively adjusting
        // controls (speed/pitch sliders, queue) — never pull the bars away.
        if (chromeMenuOpen || panelExpanded || !chromeVisible) return
        chromeVisible = false
        toolbarChrome?.let { slideChromeOut(it, offscreenY = -it.height.toFloat()) }
        bottomChrome?.let { slideChromeOut(it, offscreenY = it.height.toFloat()) }
    }

    // The bars glide on/off screen (top bar upward, bottom bar downward) with a
    // decelerating ease instead of a hard fade — they read as part of the UI
    // sliding away, and the document underneath never moves.
    private fun slideChromeIn(bar: View, offscreenY: Float) {
        bar.animate().cancel()
        if (bar.visibility != View.VISIBLE) {
            bar.translationY = offscreenY
            bar.alpha = 0f
            bar.visibility = View.VISIBLE
        }
        bar.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(240L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun slideChromeOut(bar: View, offscreenY: Float) {
        bar.animate().cancel()
        bar.animate()
            .translationY(offscreenY)
            .alpha(0f)
            .setDuration(250L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                if (!chromeVisible) bar.visibility = View.INVISIBLE
            }
            .start()
    }

    private fun scheduleChromeAutoHide() {
        chromeHideJob?.cancel()
        if (!chromeVisible || chromeMenuOpen || panelExpanded) return
        chromeHideJob = lifecycleScope.launch {
            delay(CHROME_AUTO_HIDE_MS)
            hideChrome()
        }
    }

    private suspend fun updateSentenceHighlight(forceSync: Boolean = false) {
        val view = pdfView ?: return
        val metadata = document ?: return
        if (PlaybackStateStore.activeDocumentId != metadata.id || !PlaybackStateStore.isPlaying) {
            if (lastHighlightKey.isNotBlank()) {
                view.setHighlights(emptyList())
                lastHighlightKey = ""
                lastHighlightPage = null
            }
            return
        }
        val manualSync = forceSync || pendingManualPageSync
        if (manualSync) pendingManualPageSync = false
        val safeSentenceIndex = PlaybackStateStore.currentIndex.coerceAtLeast(0)
        val chunk = extractedChunks.getOrNull(safeSentenceIndex).orEmpty()
        val start = PlaybackStateStore.currentSentenceStart.coerceIn(0, chunk.length)
        val end = PlaybackStateStore.currentSentenceEnd.coerceIn(0, chunk.length)
        val document = runCatching { view.pdfDocument }.getOrNull() ?: return
        val pageCount = document.pageCount.coerceAtLeast(1)
        val estimatedPage = readerTextModel
            ?.sentences
            ?.getOrNull(safeSentenceIndex)
            ?.pageNumber
            ?.minus(1)
            ?: if (PlaybackStateStore.chunkCount > 1) {
                ((safeSentenceIndex.toFloat() / (PlaybackStateStore.chunkCount - 1).toFloat()) * (pageCount - 1)).toInt()
            } else {
                view.firstVisiblePage.coerceIn(0, pageCount - 1)
            }
        val targetPage = estimatedPage.coerceIn(0, pageCount - 1)
        val syncEnabled = isSyncEnabled
        val visiblePage = runCatching { view.firstVisiblePage }.getOrDefault(targetPage).coerceIn(0, pageCount - 1)
        if (syncEnabled && !manualSync && lastSyncedTargetPage == targetPage && abs(visiblePage - targetPage) >= 1) {
            syncPill?.post {
                isSyncEnabled = false
                updateSyncPillUi()
            }
            lastSyncedTargetPage = null
            return
        }
        if (!syncEnabled && !manualSync) {
            lastSyncedTargetPage = null
        }
        if (chunk.isBlank()) return
        val sentence = chunk.substring(start, end.coerceAtLeast(start)).replace(Regex("\\s+"), " ").trim()
        if (sentence.length < 12) {
            view.setHighlights(emptyList())
            lastHighlightKey = ""
            lastHighlightPage = null
            return
        }
        val key = "$safeSentenceIndex:$start:$end"
        if (key == lastHighlightKey && !manualSync) return
        lastHighlightKey = key

        val previousHighlightPage = lastHighlightPage
        val centerPage = (previousHighlightPage ?: targetPage).coerceIn(0, pageCount - 1)
        val pageRange = maxOf(0, centerPage - 4)..minOf(pageCount - 1, centerPage + 4)
        lastHighlightPage = null
        val highlights = withContext(Dispatchers.IO) { findSentenceHighlights(document, sentence, pageRange) }
        view.setHighlights(highlights)
        if (syncEnabled || manualSync) {
            val scrollPage = (lastHighlightPage ?: targetPage).coerceIn(0, pageCount - 1)
            if (manualSync || lastSyncedTargetPage != scrollPage) {
                runCatching { view.scrollToPage(scrollPage) }
                lastSyncedTargetPage = scrollPage
            }
        }
    }

    private suspend fun findSentenceHighlights(
        document: androidx.pdf.PdfDocument,
        sentence: String,
        pageRange: IntRange
    ): List<Highlight> {
        fun sanitize(str: String): String = str
            .replace('\u2018', '\'')
            .replace('\u2019', '\'')
            .replace('\u201C', '"')
            .replace('\u201D', '"')
            .replace('\u2014', ' ') // em-dash
            .replace('\u2013', ' ') // en-dash
            .replace('\u2212', '-') // minus
            .replace("ﬁ", "fi")
            .replace("ﬂ", "fl")
            .replace("ﬀ", "ff")
            .replace("ﬃ", "ffi")
            .replace("ﬄ", "ffl")
            .replace(Regex("[\\r\\n\\t]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val cleanSentence = sanitize(sentence)
        val words = cleanSentence.split(' ').filter { it.isNotBlank() }
        val alphanumericWords = words.map { it.replace(Regex("[^a-zA-Z0-9]"), "") }.filter { it.length >= 3 }
        val stopWords = setOf("the", "and", "for", "are", "but", "not", "you", "all", "any", "can", "had", "her", "was", "one", "our", "out", "day", "get", "has", "him", "his", "how", "man", "new", "now", "old", "see", "two", "way", "who", "boy", "did", "its", "let", "put", "say", "she", "too", "use", "with", "from", "that", "this", "they", "have", "been", "were", "what", "when", "your", "said", "each", "which", "their", "time", "will", "about", "many", "then", "them", "some", "into", "more", "other")
        val distinctiveWords = alphanumericWords.filter { it.lowercase() !in stopWords }

        val candidates = buildList {
            // Level 1: Full raw sentence prefix
            add(cleanSentence.take(160))
            if (cleanSentence.length > 80) add(cleanSentence.take(80))

            // Level 2: Punctuation-stripped prefix
            val stripped = cleanSentence.replace(Regex("[.,:;!?\"'()\\[\\]{}]"), " ").replace(Regex("\\s+"), " ").trim()
            if (stripped != cleanSentence) {
                add(stripped.take(120))
                if (stripped.length > 60) add(stripped.take(60))
            }

            // Level 3: Distinctive word n-grams (3-5 words)
            if (distinctiveWords.size >= 4) {
                add(distinctiveWords.take(4).joinToString(" "))
                if (distinctiveWords.size >= 8) {
                    add(distinctiveWords.drop(distinctiveWords.size / 2).take(4).joinToString(" "))
                }
            }

            // Level 4: Sliding consecutive word windows (3-5 words)
            if (words.size >= 5) {
                add(words.take(5).joinToString(" "))
                add(words.takeLast(minOf(5, words.size)).joinToString(" "))
                if (words.size >= 8) {
                    val mid = words.size / 2
                    add(words.subList(maxOf(0, mid - 2), minOf(words.size, mid + 3)).joinToString(" "))
                }
            }
            if (words.size in 3..4) {
                add(words.joinToString(" "))
            }

            // Level 5: Longest distinctive words
            val longestWords = alphanumericWords.sortedByDescending { it.length }.take(3)
            if (longestWords.size >= 2 && longestWords.first().length >= 6) {
                add(longestWords.joinToString(" "))
            }
        }.map { sanitize(it) }
            .filter { it.length >= 6 }
            .distinct()

        for (query in candidates) {
            val highlights = runCatching {
                val matches = document.searchDocument(query, pageRange)
                buildList {
                    for (i in 0 until matches.size()) {
                        val page = matches.keyAt(i)
                        if (lastHighlightPage == null) lastHighlightPage = page
                        matches.valueAt(i).firstOrNull()?.bounds?.forEach { rect ->
                            add(Highlight(PdfRect(page, rect), Color.argb(170, 255, 200, 0)))
                        }
                    }
                }
            }.getOrDefault(emptyList())
            if (highlights.isNotEmpty()) return highlights
        }
        return emptyList()
    }

    private fun applyToolbarInsets(toolbar: View) {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val top = maxOf(
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                statusBarHeight(),
                34.dp
            )
            view.setPadding(8.dp, top + 14.dp, 6.dp, 8.dp)
            insets
        }
    }

    private fun applyDeckInsets(deck: View) {
        ViewCompat.setOnApplyWindowInsetsListener(deck) { view, insets ->
            val bottom = maxOf(
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom,
                navigationBarHeight(),
                6.dp
            )
            view.setPadding(10.dp, 7.dp, 10.dp, bottom + 7.dp)
            insets
        }
    }

    @Suppress("DEPRECATION")
    private fun configureSystemBars() {
        window.statusBarColor = colorToolbar
        window.navigationBarColor = colorBackground
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = isLightTheme
        controller.isAppearanceLightNavigationBars = isLightTheme
    }

    private fun showFallback(message: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
            setBackgroundColor(colorBackground)
        }
        root.addView(TextView(this).apply {
            text = "Original View"
            setTextColor(colorTextPrimary)
            textSize = 22f
        })
        root.addView(TextView(this).apply {
            text = message
            setTextColor(colorTextSecondary)
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 12.dp, 0, 18.dp)
        })
        root.addView(prominentButton("Back to Veritas") { finish() })
        setContentView(root)
    }

    // Material play/pause glyphs rendered as a centered foreground drawable; the
    // control stays a TextView so the prominentButton styling is unchanged.
    private fun applyPlayPauseIcon(playing: Boolean) {
        val control = playPauseControl ?: return
        control.text = ""
        val icon = androidx.core.content.ContextCompat.getDrawable(
            this,
            if (playing) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
        )?.mutate()
        icon?.setTint(if (isLightTheme) Color.WHITE else Color.rgb(8, 34, 40))
        control.foreground = icon
        control.foregroundGravity = Gravity.CENTER
    }

    private fun updatePlaybackControls() {
        val playing = PlaybackStateStore.isPlaying
        applyPlayPauseIcon(playing)
        panelStatusLabel?.text = if (playing) "Now reading" else "Ready to read"
        panelSpeedLabel?.text = "Speed ${"%.2f".format(PlaybackStateStore.rate)}x"
        panelPitchLabel?.text = "Pitch ${"%.2f".format(PlaybackStateStore.pitch)}"
    }

    private fun togglePanelExpand() {
        val section = expandedPanelContent ?: return
        panelExpanded = !panelExpanded
        panelExpandArrow?.let { arrow ->
            applyIconGlyph(arrow, if (panelExpanded) R.drawable.ic_m3_expand_more else R.drawable.ic_m3_expand_less)
            arrow.foreground?.setTint(colorTextSecondary)
        }
        showChrome(keepVisible = true)
        if (panelExpanded) {
            section.visibility = View.VISIBLE
            section.measure(
                View.MeasureSpec.makeMeasureSpec(
                    (resources.displayMetrics.widthPixels * 0.95f).toInt(),
                    View.MeasureSpec.AT_MOST
                ),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val target = section.measuredHeight
            section.layoutParams.height = 0
            section.requestLayout()
            ValueAnimator.ofInt(0, target).apply {
                duration = 240
                addUpdateListener {
                    section.layoutParams.height = it.animatedValue as Int
                    section.requestLayout()
                }
                start()
            }
        } else {
            val start = section.measuredHeight
            ValueAnimator.ofInt(start, 0).apply {
                duration = 200
                addUpdateListener {
                    section.layoutParams.height = it.animatedValue as Int
                    section.requestLayout()
                }
                doOnEnd { section.visibility = View.GONE }
                start()
            }
        }
        if (chromeVisible && !chromeMenuOpen) scheduleChromeAutoHide()
    }

    private fun iconButton(label: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(colorTextPrimary)
            textSize = 25f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(44.dp, 44.dp)
            setOnClickListener {
                action()
                if (chromeVisible && !chromeMenuOpen) scheduleChromeAutoHide()
            }
        }
    }

    // Material icon variant: the glyph renders as a centered, tinted foreground
    // drawable so the buttons match the Compose screens' Material 3 icons.
    private fun iconButton(iconRes: Int, action: () -> Unit): TextView {
        return iconButton("", action).apply { applyIconGlyph(this, iconRes) }
    }

    private fun applyIconGlyph(view: TextView, iconRes: Int) {
        val icon = androidx.core.content.ContextCompat.getDrawable(this, iconRes)?.mutate()
        icon?.setTint(colorTextPrimary)
        view.text = ""
        view.foreground = icon
        view.foregroundGravity = Gravity.CENTER
    }

    private fun prominentButton(label: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(if (isLightTheme) Color.WHITE else Color.rgb(8, 34, 40))
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = rounded(colorAccentButton, 22.dp)
            val targetWidth = if (label.length > 3) 170.dp else 70.dp
            layoutParams = LinearLayout.LayoutParams(targetWidth, 46.dp).apply {
                marginStart = 4.dp
                marginEnd = 4.dp
            }
            setOnClickListener {
                action()
                if (chromeVisible && !chromeMenuOpen) scheduleChromeAutoHide()
            }
        }
    }

    private fun brandTile(): View {
        return FrameLayout(this).apply {
            background = rounded(colorSyncBackground, 12.dp)
            layoutParams = LinearLayout.LayoutParams(50.dp, 50.dp)
            addView(ImageView(context).apply {
                setImageResource(R.drawable.veritas_reader_icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(5.dp, 5.dp, 5.dp, 5.dp)
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun statusBarHeight(): Int = systemBarHeight("status_bar")

    private fun navigationBarHeight(): Int = systemBarHeight("navigation_bar")

    private fun systemBarHeight(name: String): Int {
        val id = resources.getIdentifier(name, "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    companion object {
        private const val EXTRA_DOCUMENT_ID = "document_id"
        private const val VIEWER_TAG = "veritas_pdf_viewer"
        private const val CHROME_AUTO_HIDE_MS = 4_000L

        fun intent(context: Context, documentId: String): Intent {
            return Intent(context, VeritasPdfViewerActivity::class.java)
                .putExtra(EXTRA_DOCUMENT_ID, documentId)
        }
    }
}
