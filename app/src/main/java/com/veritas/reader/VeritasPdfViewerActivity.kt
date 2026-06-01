package com.veritas.reader

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
import android.net.Uri
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
import androidx.core.content.FileProvider
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        configureSystemBars()
        repository = DocumentRepository(applicationContext)

        val documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID).orEmpty()
        val metadata = repository.findDocument(documentId)
        val original = metadata?.let { repository.originalFile(it) }
        if (metadata == null || original == null) {
            showFallback("The original PDF is no longer available.")
            return
        }
        document = metadata
        buildLayout(metadata.title.ifBlank { getString(R.string.app_name) })
        loadHighlightTextAsync(metadata)

        val uri = runCatching {
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", original)
        }.getOrNull() ?: run {
            showFallback("Veritas could not prepare the PDF file for viewing.")
            return
        }
        runCatching {
            val fragment = PdfViewerFragment()
            viewerFragment = fragment
            supportFragmentManager.beginTransaction()
                .replace(requireNotNull(fragmentContainer).id, fragment, VIEWER_TAG)
                .commitNowAllowingStateLoss()
            fragment.documentUri = uri
            schedulePdfViewLookup(fragment)
        }.onFailure { error ->
            showFallback("Veritas could not open this PDF viewer: ${error.message ?: "unknown error"}")
        }
    }

    private fun buildLayout(title: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(17, 22, 26))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, statusBarHeight() + 16.dp, 6.dp, 8.dp)
            setBackgroundColor(Color.rgb(20, 25, 29))
        }
        applyToolbarInsets(toolbar)
        toolbar.addView(iconButton("‹") { finish() })
        toolbar.addView(TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        syncCheckBox = CheckBox(this).apply {
            text = "Sync"
            setTextColor(Color.rgb(225, 240, 244))
            textSize = 11f
            gravity = Gravity.CENTER
            buttonTintList = ColorStateList.valueOf(Color.rgb(120, 221, 232))
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
        toolbar.addView(iconButton("⌕") { toggleSearch() })
        toolbar.addView(iconButton("⟳") { rotateViewer() })
        toolbar.addView(iconButton("⋮") { showTopMenu(toolbar) })
        toolbarChrome = toolbar

        fragmentContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.rgb(34, 38, 42))
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
            setBackgroundColor(Color.rgb(17, 22, 26))
        }
        applyDeckInsets(controlsOuter)
        bottomChrome = controlsOuter
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.rgb(31, 41, 46), 24.dp)
            elevation = 8f
        }
        val progressStrip = View(this).apply {
            setBackgroundColor(if (PlaybackStateStore.isPlaying) Color.rgb(120, 221, 232) else Color.rgb(24, 30, 34))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3.dp)
        }
        val controlRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10.dp, 8.dp, 8.dp, 8.dp)
        }
        controlRow.addView(brandTile())
        controlRow.addView(TextView(this).apply {
            text = if (PlaybackStateStore.isPlaying) "Now reading" else "Ready to read"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 8.dp
                marginEnd = 2.dp
            }
        })
        controlRow.addView(iconButton("‹") { sendPlaybackIntent(this, PlaybackActions.ACTION_PREVIOUS) })
        playPauseControl = prominentButton(if (PlaybackStateStore.isPlaying) "Ⅱ" else "▶") {
            sendPlaybackIntent(this, if (PlaybackStateStore.isPlaying) PlaybackActions.ACTION_PAUSE else PlaybackActions.ACTION_PLAY)
            playPauseControl?.postDelayed({ updatePlaybackControls() }, 180)
        }
        controlRow.addView(requireNotNull(playPauseControl))
        controlRow.addView(iconButton("›") { sendPlaybackIntent(this, PlaybackActions.ACTION_NEXT) })
        controlRow.addView(iconButton("⋮") { showPlaybackMenu(controlRow) })

        controls.addView(progressStrip)
        controls.addView(controlRow)
        controlsOuter.addView(controls)

        root.addView(toolbar)
        root.addView(fragmentContainer)
        root.addView(controlsOuter)
        setContentView(root)
        ViewCompat.requestApplyInsets(root)
        showChrome(keepVisible = true)
        scheduleChromeAutoHide()
    }

    override fun onResume() {
        super.onResume()
        updatePlaybackControls()
        if (chromeVisible) scheduleChromeAutoHide()
    }

    override fun onDestroy() {
        highlightJob?.cancel()
        chromeHideJob?.cancel()
        super.onDestroy()
    }

    override fun onActionModeStarted(mode: android.view.ActionMode?) {
        super.onActionModeStarted(mode)
        val menu = mode?.menu ?: return
        val readItem = menu.findItem(1001) ?: menu.add(0, 1001, 1, "Read from here")
        readItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS or android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        readItem.setTitleCondensed("Read")
        readItem.isVisible = true
        readItem.isEnabled = true
        readItem.setOnMenuItemClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
                    return@setOnMenuItemClickListener true
                }

                readCopiedSelectionAfterCopy(
                    clipboard = clipboard,
                    previousClip = oldClip,
                    sentinel = sentinel,
                    mode = mode,
                    attempt = 0
                )
            } else {
                Toast.makeText(this@VeritasPdfViewerActivity, "Copy action is not available in this PDF selection menu.", Toast.LENGTH_SHORT).show()
                restoreClipboard(clipboard, oldClip)
                mode.finish()
            }
            true
        }
        mode.invalidate()
    }

    private fun readCopiedSelectionAfterCopy(
        clipboard: ClipboardManager,
        previousClip: ClipData?,
        sentinel: String,
        mode: android.view.ActionMode,
        attempt: Int
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
                jumpToText(selectedText)
                restoreClipboard(clipboard, previousClip)
                mode.finish()
            } else if (attempt < 2) {
                readCopiedSelectionAfterCopy(clipboard, previousClip, sentinel, mode, attempt + 1)
            } else {
                Toast.makeText(this@VeritasPdfViewerActivity, "Could not extract selected text.", Toast.LENGTH_SHORT).show()
                restoreClipboard(clipboard, previousClip)
                mode.finish()
            }
        }, delayMs)
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
        val original = repository.originalFile(metadata) ?: return
        val uri = runCatching {
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", original)
        }.getOrNull() ?: run {
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
            background = rounded(Color.rgb(30, 28, 36), 18.dp)
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
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 10.dp)
        })
        menu.addView(TextView(this).apply {
            text = status
            setTextColor(Color.rgb(202, 199, 212))
            textSize = 15f
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setPadding(0, 0, 0, 12.dp)
        })
        menu.addView(View(this).apply {
            setBackgroundColor(Color.rgb(70, 66, 78))
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
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(0, 12.dp, 0, 12.dp)
            setOnClickListener {
                popup.dismiss()
                openVoiceAndLanguage()
            }
        })
        menu.addView(TextView(this).apply {
            text = if (PlaybackStateStore.queueCount == 0) "Queue empty" else "Continue queue (${PlaybackStateStore.queueCount})"
            setTextColor(Color.rgb(150, 146, 160))
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
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 4.dp, 0, 4.dp)
        }
        menu.addView(label)
        menu.addView(SeekBar(this).apply {
            this.max = steps
            progress = (((current.coerceIn(min, max) - min) * 100).toInt()).coerceIn(0, steps)
            progressTintList = ColorStateList.valueOf(Color.rgb(28, 203, 221))
            thumbTintList = ColorStateList.valueOf(Color.rgb(120, 221, 232))
            progressBackgroundTintList = ColorStateList.valueOf(Color.rgb(31, 48, 49))
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
            background = rounded(Color.rgb(37, 41, 46), 18.dp)
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
                setTextColor(Color.WHITE)
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
                            add(Highlight(PdfRect(page, rect), Color.argb(68, 105, 190, 255)))
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
        window.statusBarColor = Color.rgb(20, 25, 29)
        window.navigationBarColor = Color.rgb(17, 22, 26)
    }

    private fun showFallback(message: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
            setBackgroundColor(Color.rgb(17, 22, 26))
        }
        root.addView(TextView(this).apply {
            text = "Actual document"
            setTextColor(Color.WHITE)
            textSize = 22f
        })
        root.addView(TextView(this).apply {
            text = message
            setTextColor(Color.rgb(210, 218, 224))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 12.dp, 0, 18.dp)
        })
        root.addView(prominentButton("Back to Veritas") { finish() })
        setContentView(root)
    }

    private fun updatePlaybackControls() {
        playPauseControl?.text = if (PlaybackStateStore.isPlaying) "Ⅱ" else "▶"
    }

    private fun iconButton(label: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 25f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(44.dp, 44.dp)
            setOnClickListener {
                action()
                if (chromeVisible && !chromeMenuOpen) scheduleChromeAutoHide()
            }
        }
    }

    private fun prominentButton(label: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(Color.rgb(8, 34, 40))
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(126, 218, 230), 22.dp)
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
            background = rounded(Color.rgb(12, 78, 86), 12.dp)
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
