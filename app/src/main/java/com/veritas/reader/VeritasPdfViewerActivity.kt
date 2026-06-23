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
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
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
    private var syncCheckBox: CheckBox? = null
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
    private var chromeVisible = true
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
        isLightTheme = if (themeId == "system") {
            val mode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            mode != android.content.res.Configuration.UI_MODE_NIGHT_YES
        } else {
            themeId == "light" || themeId == "white_high_contrast" || themeId == "bw_gradient_light" || themeId == "github_light"
        }

        if (isLightTheme) {
            colorPrimary = Color.rgb(91, 79, 207)
            colorBackground = Color.rgb(244, 246, 250)
            colorToolbar = Color.rgb(238, 238, 242)
            colorSurface = Color.rgb(255, 255, 255)
            colorSurfaceVariant = Color.rgb(238, 238, 242)
            colorTextPrimary = Color.rgb(26, 26, 46)
            colorTextSecondary = Color.rgb(84, 84, 100)
            colorOutline = Color.rgb(200, 200, 208)
            colorSyncBackground = Color.rgb(220, 226, 255)
            colorActiveStrip = Color.rgb(91, 79, 207)
            colorAccentButton = Color.rgb(91, 79, 207)
        } else {
            colorPrimary = Color.rgb(142, 220, 230)
            colorBackground = Color.rgb(23, 31, 43)
            colorToolbar = Color.rgb(39, 49, 64)
            colorSurface = Color.rgb(23, 31, 43)
            colorSurfaceVariant = Color.rgb(39, 49, 64)
            colorTextPrimary = Color.rgb(233, 238, 245)
            colorTextSecondary = Color.rgb(196, 206, 217)
            colorOutline = Color.rgb(60, 75, 90)
            colorSyncBackground = Color.rgb(12, 78, 86)
            colorActiveStrip = Color.rgb(66, 133, 244)
            colorAccentButton = Color.rgb(126, 218, 230)
        }

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
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(colorBackground)
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, statusBarHeight() + 8.dp, 6.dp, 4.dp)
            setBackgroundColor(colorToolbar)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        applyToolbarInsets(toolbar)
        toolbar.addView(iconButton(R.drawable.ic_m3_chevron_left) { finish() })
        toolbar.addView(TextView(this).apply {
            text = title
            setTextColor(colorTextPrimary)
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        syncCheckBox = CheckBox(this).apply {
            text = "🔗 Sync"
            setTextColor(colorTextSecondary)
            textSize = 11f
            gravity = Gravity.CENTER
            buttonTintList = ColorStateList.valueOf(colorPrimary)
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(68.dp, 44.dp)
            isChecked = true
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    pendingManualPageSync = true
                    lifecycleScope.launch { updateSentenceHighlight(forceSync = true) }
                }
            }
        }
        toolbar.addView(syncCheckBox)
        toolbar.addView(iconButton(R.drawable.ic_m3_search) { toggleSearch() })
        toolbar.addView(iconButton(R.drawable.ic_m3_rotate_right) { rotateViewer() })
        toolbar.addView(iconButton(R.drawable.ic_m3_more_vert) { showTopMenu(toolbar) })
        toolbarChrome = toolbar

        fragmentContainer = FrameLayout(this).apply {
            id = R.id.pdf_fragment_container
            setBackgroundColor(if (isLightTheme) colorBackground else Color.WHITE)
            if (!isLightTheme) {
                // True dark mode: soft brightness inversion that PRESERVES HUE. White paper
                // -> ~#242424, black text -> ~#EBEBEB, but coloured elements keep their hue
                // (red stays reddish, blue stays bluish) instead of flipping to cyan/orange.
                val paint = android.graphics.Paint().apply {
                    colorFilter = android.graphics.ColorMatrixColorFilter(floatArrayOf(
                         0.4477f, -1.1154f, -0.1123f, 0.0f, 235.0f,
                        -0.3323f, -0.3354f, -0.1123f, 0.0f, 235.0f,
                        -0.3323f, -1.1154f,  0.6677f, 0.0f, 235.0f,
                         0.0f,     0.0f,     0.0f,    1.0f,   0.0f
                    ))
                }
                setLayerType(View.LAYER_TYPE_HARDWARE, paint)
            }
            setOnTouchListener { _, event ->
                handleDocumentChromeTouch(this, event)
                false
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val controlsOuter = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10.dp, 7.dp, 10.dp, 13.dp)
            setBackgroundColor(colorBackground)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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

        root.addView(toolbar)
        root.addView(fragmentContainer)
        root.addView(controlsOuter)
        setContentView(root)
        ViewCompat.requestApplyInsets(root)
        showChrome(keepVisible = true)
        scheduleChromeAutoHide()
    }

    private fun resetInactivityTimer() {
        keepAwakeTimerJob?.cancel()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        keepAwakeTimerJob = lifecycleScope.launch {
            delay(20L * 60L * 1000L) // 20 minutes
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onResume() {
        super.onResume()
        updatePlaybackControls()
        if (chromeVisible) scheduleChromeAutoHide()
        resetInactivityTimer()
    }

    override fun onPause() {
        super.onPause()
        saveCurrentProgress()
        keepAwakeTimerJob?.cancel()
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val container = FrameLayout(this).apply {
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
            addView(input)
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add note to sentence ${sentenceIndex + 1}")
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
            .show()
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

    private fun showTopMenu(anchor: View) {
        showChrome(keepVisible = true)
        showMenu(anchor, listOf(
            "Search PDF" to ::toggleSearch,
            "Rotate view" to ::rotateViewer,
            "Open original" to ::openOriginal,
            "Back to extracted text" to ::finish
        ), alignTopEnd = true)
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
                    tapMoved = true
                }
            }
            MotionEvent.ACTION_UP -> {
                val topTapZone = view.height.toFloat() * 0.30f
                val quickTap = event.eventTime - tapDownTime < 360L
                val dx = event.x - tapDownX
                val dy = event.y - tapDownY
                val moved = tapMoved || (dx * dx + dy * dy) > (slop * slop)
                if (!moved && quickTap && tapDownY <= topTapZone) {
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
        listOfNotNull(toolbarChrome, bottomChrome).forEach { view ->
            if (view.visibility != View.VISIBLE) {
                view.alpha = 0f
                view.visibility = View.VISIBLE
            }
            view.animate().cancel()
            view.animate().alpha(1f).setDuration(140L).start()
        }
        chromeHideJob?.cancel()
        if (!keepVisible) scheduleChromeAutoHide()
    }

    private fun hideChrome() {
        chromeHideJob?.cancel()
        if (chromeMenuOpen || !chromeVisible) return
        chromeVisible = false
        listOfNotNull(toolbarChrome, bottomChrome).forEach { view ->
            view.animate().cancel()
            view.animate()
                .alpha(0f)
                .setDuration(160L)
                .withEndAction {
                    if (!chromeVisible) view.visibility = View.GONE
                }
                .start()
        }
    }

    private fun scheduleChromeAutoHide() {
        chromeHideJob?.cancel()
        if (!chromeVisible || chromeMenuOpen) return
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
        val syncEnabled = syncCheckBox?.isChecked == true
        val visiblePage = runCatching { view.firstVisiblePage }.getOrDefault(targetPage).coerceIn(0, pageCount - 1)
        if (syncEnabled && !manualSync && lastSyncedTargetPage == targetPage && abs(visiblePage - targetPage) >= 1) {
            syncCheckBox?.post { syncCheckBox?.isChecked = false }
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
        val words = sentence.split(Regex("\\s+")).filter { it.length > 1 }
        val candidates = buildList {
            add(sentence.take(180))
            if (words.size >= 8) add(words.take(14).joinToString(" "))
            if (words.size >= 12) add(words.drop(words.size / 3).take(12).joinToString(" "))
            if (words.size >= 8) add(words.takeLast(12).joinToString(" "))
        }.map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.length >= 12 }
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
        private const val CHROME_AUTO_HIDE_MS = 5_000L

        fun intent(context: Context, documentId: String): Intent {
            return Intent(context, VeritasPdfViewerActivity::class.java)
                .putExtra(EXTRA_DOCUMENT_ID, documentId)
        }
    }
}
