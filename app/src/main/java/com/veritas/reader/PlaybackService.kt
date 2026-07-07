package com.veritas.reader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.createBitmap
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import java.io.File
import java.util.UUID
import kotlin.math.min

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private lateinit var repository: DocumentRepository
    private var mediaSession: MediaSession? = null
    private var mediaSessionPlayer: VeritasMediaSessionPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeak = false
    private var pendingSelectionText: String? = null
    private var activeEnginePackage: String? = null

    private var activeDocument: SavedDocument? = null
    // Slide number per chunk for PPTX docs (null otherwise): drives the short
    // silence beats at slide transitions and after slide titles.
    private var chunkPageNumbers: IntArray? = null
    private var lastSavedRate = Float.NaN
    private var lastSavedPitch = Float.NaN
    private var chunks: List<String> = emptyList()
    private var artworkDocumentId: String? = null
    private var notificationArtwork: Bitmap? = null
    private var artworkBytes: ByteArray? = null
    private var artworkBytesDocumentId: String? = null
    private var activeChunkUtteranceId: String? = null
    private var activeChunkIndex = -1
    // Timestamp when the current sentence started speaking; used to attribute background
    // listening time to the active document (see recordBackgroundListening()).
    private var activeChunkStartedAt = 0L
    private var activeChunkSpeechText = ""
    private var activeChunkBaseOffset = 0
    private var spokenCharOffset = 0
    private var spokenWordCount = 0
    private var resumeDocumentId: String? = null
    private var resumeChunkIndex = -1
    private var resumeCharOffset = 0
    private var resumeWordCount = 0
    private var pendingJumpCharOffset: Int? = null
    private var sleepTimerRunnable: Runnable? = null
    // When true, the sleep timer has fired but we are mid-sentence.
    // The timer action will execute at the next sentence boundary (onDone)
    // so playback never cuts off mid-word.
    private var pendingTimerFire = false

    private var audioFocusRequest: android.media.AudioFocusRequest? = null
    private var pausedDueToTransientFocusLoss = false
    private val audioManager by lazy { getSystemService(AUDIO_SERVICE) as android.media.AudioManager }

    override fun onCreate() {
        super.onCreate()
        repository = DocumentRepository(applicationContext)
        PlaybackStateStore.queueCount = repository.loadQueueDocuments().size
        createNotificationChannel()
        setupMediaSession()
        // If the process died while a sleep timer was running, restore it from prefs
        // so the timer survives instead of silently disappearing.
        if (PlaybackStateStore.activeSleepTimerSnapshot() == null) {
            repository.loadPersistedSleepTimer()?.let { persisted ->
                PlaybackStateStore.sleepTimerDurationMillis = persisted.durationMillis
                PlaybackStateStore.sleepTimerEndsAtMillis = persisted.endsAtMillis
                PlaybackStateStore.sleepTimerActionName = persisted.action.name
                PlaybackStateStore.sleepTimerStopAtEndOfSection = persisted.stopAtEndOfSection
            }
        }
        scheduleSleepTimerFromStore()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            PlaybackActions.ACTION_PLAY -> handlePlay(intent.extras)
            PlaybackActions.ACTION_PAUSE -> pauseSpeech()
            PlaybackActions.ACTION_STOP -> stopSpeechAndService()
            PlaybackActions.ACTION_NEXT -> moveBy(1)
            PlaybackActions.ACTION_PREVIOUS -> moveBy(-1)
            PlaybackActions.ACTION_JUMP_TO -> handleJump(intent.extras)
            PlaybackActions.ACTION_SPEAK_SELECTION -> handleSpeakSelection(intent.extras)
            PlaybackActions.ACTION_UPDATE_PLAYBACK_SETTINGS -> handlePlaybackSettingsUpdate(intent.extras)
            PlaybackActions.ACTION_SET_SLEEP_TIMER -> handleSetSleepTimer(intent.extras)
            PlaybackActions.ACTION_CANCEL_SLEEP_TIMER -> cancelSleepTimer("Sleep timer cancelled.")
            PlaybackActions.ACTION_MEDIA_BUTTON -> handleMediaButton(intent.extras)
            else -> refreshForegroundNotification()
        }
        return START_STICKY
    }

    private fun handleMediaButton(extras: Bundle?) {
        val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras?.getParcelable(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras?.getParcelable(Intent.EXTRA_KEY_EVENT)
        }

        if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN || keyEvent.repeatCount > 0) return

        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                if (!PlaybackStateStore.isPlaying) handlePlay(null)
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> pauseSpeech()
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                if (PlaybackStateStore.isPlaying) pauseSpeech() else handlePlay(null)
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> moveBy(1)
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> moveBy(-1)
            KeyEvent.KEYCODE_MEDIA_STOP -> stopSpeechAndService()
            else -> Unit
        }
    }

    private fun handlePlay(extras: Bundle?) {
        val documentId = extras?.getString(PlaybackActions.EXTRA_DOCUMENT_ID)
            ?: PlaybackStateStore.activeDocumentId
            ?: repository.loadQueueDocuments().firstOrNull()?.id
            ?: return
        val startIndex = extras?.getInt(PlaybackActions.EXTRA_START_INDEX, PlaybackStateStore.currentIndex)
            ?: PlaybackStateStore.currentIndex
        val charOffset = extras?.getInt(PlaybackActions.EXTRA_CHAR_OFFSET, -1).takeIf { it != null && it >= 0 }

        PlaybackStateStore.rate = extras?.getFloat(PlaybackActions.EXTRA_RATE, PlaybackStateStore.rate)
            ?.coerceIn(0.5f, 2.0f) ?: PlaybackStateStore.rate
        PlaybackStateStore.pitch = extras?.getFloat(PlaybackActions.EXTRA_PITCH, PlaybackStateStore.pitch)
            ?.coerceIn(0.7f, 1.4f) ?: PlaybackStateStore.pitch

        pausedDueToTransientFocusLoss = false
        if (!loadDocument(documentId, startIndex)) return

        pendingJumpCharOffset = charOffset
        PlaybackStateStore.isPlaying = true
        PlaybackStateStore.isForegroundActive = true
        PlaybackStateStore.statusMessage = "Preparing playback…"
        if (!requestAudioFocus()) {
            Log.w(TAG, "Audio focus not granted; continuing anyway.")
        }
        startForegroundNow()
        ensureTtsReadyAndSpeak()
    }

    private fun handleJump(extras: Bundle?) {
        val documentId = extras?.getString(PlaybackActions.EXTRA_DOCUMENT_ID)
            ?: PlaybackStateStore.activeDocumentId
            ?: return
        val index = extras?.getInt(PlaybackActions.EXTRA_START_INDEX, PlaybackStateStore.currentIndex)
            ?: PlaybackStateStore.currentIndex
        val charOffset = extras?.getInt(PlaybackActions.EXTRA_CHAR_OFFSET, -1).takeIf { it != null && it >= 0 }

        val wasPlaying = PlaybackStateStore.isPlaying
        if (!loadDocument(documentId, index)) return
        repository.updateProgress(documentId, PlaybackStateStore.currentIndex, chunks.size)

        pendingJumpCharOffset = charOffset
        if (wasPlaying || charOffset != null) {
            PlaybackStateStore.isPlaying = true
            startForegroundNow()
            speakCurrent()
        } else {
            refreshForegroundNotification()
        }
    }

    private fun handleSpeakSelection(extras: Bundle?) {
        val selection = extras?.getString(PlaybackActions.EXTRA_SELECTION_TEXT)
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            ?.take(1200)
            .orEmpty()
        if (selection.isBlank()) return
        if (PlaybackStateStore.isPlaying) rememberPausePoint()
        PlaybackStateStore.isPlaying = false
        ensureTtsReadyAndSpeakSelection(selection)
    }

    private fun handlePlaybackSettingsUpdate(extras: Bundle?) {
        PlaybackStateStore.rate = extras?.getFloat(PlaybackActions.EXTRA_RATE, PlaybackStateStore.rate)
            ?.coerceIn(0.5f, 2.0f) ?: PlaybackStateStore.rate
        PlaybackStateStore.pitch = extras?.getFloat(PlaybackActions.EXTRA_PITCH, PlaybackStateStore.pitch)
            ?.coerceIn(0.7f, 1.4f) ?: PlaybackStateStore.pitch
        if (PlaybackStateStore.isPlaying) {
            speakCurrent()
        } else {
            updateMediaSessionState()
            refreshForegroundNotification()
        }
    }

    private fun handleSetSleepTimer(extras: Bundle?) {
        val durationMillis = extras?.getLong(PlaybackActions.EXTRA_SLEEP_TIMER_DURATION_MILLIS, 0L) ?: 0L
        val action = VeritasSleepTimerAction.fromName(extras?.getString(PlaybackActions.EXTRA_SLEEP_TIMER_ACTION))
        val stopAtEndOfSection = extras?.getBoolean(PlaybackActions.EXTRA_SLEEP_TIMER_STOP_AT_END_OF_SECTION, false) ?: false
        val request = runCatching {
            VeritasSleepTimerRequest(durationMillis = durationMillis, action = action, stopAtEndOfSection = stopAtEndOfSection)
        }.getOrNull()

        if (request == null) {
            cancelSleepTimer("Sleep timer could not be started.")
            return
        }

        PlaybackStateStore.setSleepTimer(request)
        repository.saveSleepTimerState(
            durationMillis = PlaybackStateStore.sleepTimerDurationMillis,
            endsAtMillis = PlaybackStateStore.sleepTimerEndsAtMillis,
            actionName = PlaybackStateStore.sleepTimerActionName,
            stopAtEndOfSection = PlaybackStateStore.sleepTimerStopAtEndOfSection
        )
        scheduleSleepTimerFromStore()
        PlaybackStateStore.statusMessage = if (stopAtEndOfSection) {
            "Sleep timer set to stop at end of section."
        } else {
            "Sleep timer set for ${VeritasSleepTimerFormatter.formatDuration(request.durationMillis)}."
        }
        updateMediaSessionState()
        refreshForegroundNotification()
    }

    private fun loadDocument(documentId: String, requestedIndex: Int): Boolean {
        val existingLoaded = activeDocument?.id == documentId && chunks.isNotEmpty()
        val doc = if (existingLoaded) activeDocument else repository.findDocument(documentId)
        if (doc == null) {
            PlaybackStateStore.statusMessage = "Could not find this saved reading."
            return false
        }

        if (!existingLoaded) {
            val rawText = repository.readText(doc)
            chunks = TextChunker.chunk(rawText)
            // Restore this document's remembered narration pace (if any).
            repository.loadDocVoiceMemory(doc.id)?.let { (rate, pitch) ->
                PlaybackStateStore.rate = rate
                PlaybackStateStore.pitch = pitch
            }
            chunkPageNumbers = if (doc.sourceLabel == "PPTX") {
                val model = ReaderTextModelCache.get(doc.id, rawText, doc.pageCount)
                if (model.sentences.size == chunks.size) {
                    IntArray(model.sentences.size) { model.sentences[it].pageNumber }
                } else null
            } else null
            activeDocument = doc
            clearResumePoint()
            if (artworkDocumentId != doc.id) {
                artworkDocumentId = null
                notificationArtwork = null
                artworkBytes = null
                artworkBytesDocumentId = null
            }
        }

        if (chunks.isEmpty()) {
            PlaybackStateStore.statusMessage = "No readable sentences were found."
            return false
        }

        val safeIndex = requestedIndex.coerceIn(0, chunks.lastIndex)
        PlaybackStateStore.activeDocumentId = documentId
        PlaybackStateStore.documentTitle = doc.title
        PlaybackStateStore.sourceLabel = doc.sourceLabel
        PlaybackStateStore.currentIndex = safeIndex
        PlaybackStateStore.chunkCount = chunks.size
        PlaybackStateStore.queueCount = repository.loadQueueDocuments().size
        return true
    }

    private fun ensureTtsReadyAndSpeak() {
        val voiceSettings = repository.loadVoiceSettings()
        val requestedEngine = voiceSettings.enginePackage.ifBlank { null }
        if (tts != null && activeEnginePackage != requestedEngine) {
            runCatching { tts?.stop() }
            runCatching { tts?.shutdown() }
            tts = null
            ttsReady = false
        }

        if (ttsReady && tts != null) {
            tts?.let { VoiceConfigurator.apply(it, voiceSettings) }
            speakCurrent()
            return
        }

        pendingSpeak = true
        if (tts == null) {
            activeEnginePackage = requestedEngine
            tts = if (requestedEngine == null) {
                TextToSpeech(applicationContext) { status -> handleTtsInit(status) }
            } else {
                TextToSpeech(applicationContext, { status -> handleTtsInit(status) }, requestedEngine)
            }
        }
    }

    private fun ensureTtsReadyAndSpeakSelection(text: String) {
        val voiceSettings = repository.loadVoiceSettings()
        val requestedEngine = voiceSettings.enginePackage.ifBlank { null }
        if (tts != null && activeEnginePackage != requestedEngine) {
            runCatching { tts?.stop() }
            runCatching { tts?.shutdown() }
            tts = null
            ttsReady = false
        }

        pendingSelectionText = text
        pendingSpeak = false
        if (ttsReady && tts != null) {
            pendingSelectionText = null
            tts?.let { VoiceConfigurator.apply(it, voiceSettings) }
            speakSelectionText(text)
            return
        }

        if (tts == null) {
            activeEnginePackage = requestedEngine
            tts = if (requestedEngine == null) {
                TextToSpeech(applicationContext) { status -> handleTtsInit(status) }
            } else {
                TextToSpeech(applicationContext, { status -> handleTtsInit(status) }, requestedEngine)
            }
        }
    }

    private fun handleTtsInit(status: Int) {
        mainHandler.post {
            if (status == TextToSpeech.SUCCESS) {
                val message = tts?.let { VoiceConfigurator.apply(it, repository.loadVoiceSettings()) }
                    ?: "Reading in background."
                ttsReady = true
                attachListener()
                PlaybackStateStore.statusMessage = message
                pendingSelectionText?.let { selection ->
                    pendingSelectionText = null
                    pendingSpeak = false
                    speakSelectionText(selection)
                    return@post
                }
                if (pendingSpeak) {
                    pendingSpeak = false
                    speakCurrent()
                }
            } else {
                val statusMsg = when (status) {
                    TextToSpeech.ERROR -> "TTS engine error. Check voice engine settings."
                    else -> "Voice initialization failed (status $status). Try another engine."
                }
                Log.e(TAG, "TTS init failed with status $status")
                ttsReady = false
                pendingSpeak = false
                pendingSelectionText = null
                runCatching { tts?.shutdown() }
                tts = null
                activeEnginePackage = null
                PlaybackStateStore.isPlaying = false
                PlaybackStateStore.statusMessage = statusMsg
                refreshForegroundNotification()
            }
        }
    }

    private fun attachListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId != activeChunkUtteranceId && utteranceId?.startsWith(SELECTION_UTTERANCE_PREFIX) != true) {
                        return@post
                    }
                    if (utteranceId?.startsWith(SELECTION_UTTERANCE_PREFIX) == true) {
                        PlaybackStateStore.statusMessage = "Selected text finished."
                        refreshForegroundNotification()
                    } else if (PlaybackStateStore.isPlaying) {
                        // Attribute this sentence's duration to background listening time.
                        recordBackgroundListening()
                        // If the sleep timer fired while this sentence was playing,
                        // execute it now at the clean sentence boundary.
                        if (pendingTimerFire) {
                            pendingTimerFire = false
                            executePendingTimerFire()
                            return@post
                        }
                        clearResumePoint()
                        advanceAfterSection()
                    }
                }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                mainHandler.post {
                    if (utteranceId == activeChunkUtteranceId && activeChunkSpeechText.isNotBlank()) {
                        val absoluteStart = (activeChunkBaseOffset + start).coerceIn(0, activeChunkSpeechText.length)
                        spokenCharOffset = absoluteStart
                        spokenWordCount = wordCountBefore(activeChunkSpeechText, absoluteStart)
                        updateCurrentSentenceBounds(absoluteStart)
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId != activeChunkUtteranceId && utteranceId?.startsWith(SELECTION_UTTERANCE_PREFIX) != true) {
                        return@post
                    }
                    handleTtsFailure("Voice engine failed. Try another sentence.", utteranceId)
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post {
                    if (utteranceId != activeChunkUtteranceId && utteranceId?.startsWith(SELECTION_UTTERANCE_PREFIX) != true) {
                        return@post
                    }
                    val errorMsg = when (errorCode) {
                        TextToSpeech.ERROR_NETWORK -> "Network error. Check internet connection."
                        TextToSpeech.ERROR_NETWORK_TIMEOUT -> "Network timeout. Try a local voice."
                        TextToSpeech.ERROR_OUTPUT -> "Audio output error. Check device volume."
                        TextToSpeech.ERROR_NOT_INSTALLED_YET -> "Voice not ready yet. Try again."
                        else -> "Voice engine error (code $errorCode). Try another voice."
                    }
                    handleTtsFailure(errorMsg, utteranceId)
                }
            }
        })
    }

    /**
     * Attributes the time spent speaking the just-finished sentence to the active document's
     * reading time, but ONLY while the app UI is backgrounded. Foreground reading time is
     * tracked by the ViewModel's session timer, so gating on appInForeground avoids double
     * counting. Recording per-sentence means background listening is captured incrementally
     * even if the process is later killed.
     */
    private fun recordBackgroundListening() {
        val startedAt = activeChunkStartedAt
        activeChunkStartedAt = 0L
        if (startedAt <= 0L || PlaybackStateStore.appInForeground) return
        val docId = activeDocument?.id ?: return
        val delta = System.currentTimeMillis() - startedAt
        if (delta in 1L..600_000L) {
            repository.recordDocReadingTime(docId, delta)
        }
    }

    private fun handleTtsFailure(message: String, utteranceId: String? = null) {
        Log.w(TAG, "$message utterance=$utteranceId")
        pendingSpeak = false
        pendingSelectionText = null
        PlaybackStateStore.isPlaying = false
        activeDocument?.let { repository.updateProgress(it.id, PlaybackStateStore.currentIndex, chunks.size) }
        clearResumePoint()
        runCatching { tts?.stop() }
        PlaybackStateStore.statusMessage = message
        updateMediaSessionState()
        refreshForegroundNotification()
    }

    private fun speakCurrent() {
        if (chunks.isEmpty()) return
        val index = PlaybackStateStore.currentIndex.coerceIn(0, chunks.lastIndex)
        val rawChunkText = chunks.getOrNull(index)?.trim().orEmpty()

        val jumpOffset = pendingJumpCharOffset?.coerceIn(0, rawChunkText.length)
        pendingJumpCharOffset = null

        val resumeOffset = jumpOffset ?: resumeOffsetForCurrentChunk(rawChunkText, index)
        val text = repository.applyPronunciationRules(rawChunkText.substring(resumeOffset).trimStart())
        if (text.isBlank()) return

        // Persist the pace being used with this document, but only when it changes.
        if (PlaybackStateStore.rate != lastSavedRate || PlaybackStateStore.pitch != lastSavedPitch) {
            activeDocument?.let { repository.saveDocVoiceMemory(it.id, PlaybackStateStore.rate, PlaybackStateStore.pitch) }
            lastSavedRate = PlaybackStateStore.rate
            lastSavedPitch = PlaybackStateStore.pitch
        }
        val narrationSettings = repository.loadNarrationSettings()
        val effectiveRate = NarrationAnalyzer.effectiveRate(PlaybackStateStore.rate, narrationSettings, text)
        val effectivePitch = NarrationAnalyzer.effectivePitch(PlaybackStateStore.pitch, narrationSettings, text)
        tts?.let { VoiceConfigurator.apply(it, repository.loadVoiceSettings()) }
        tts?.setSpeechRate(effectiveRate)
        tts?.setPitch(effectivePitch)
        PlaybackStateStore.currentIndex = index
        PlaybackStateStore.isPlaying = true
        PlaybackStateStore.statusMessage = if (narrationSettings.enabled) {
            "Reading ${NarrationAnalyzer.labelFor(text, narrationSettings).lowercase()} sentence."
        } else {
            "Reading in background."
        }
        // Silence decorative glyphs (bullets, arrows, dot leaders). Replacements are
        // length-preserving, so word-highlight offsets from onRangeStart stay valid.
        val speakText = SpeechSanitizer.forSpeech(text)
        if (speakText.isBlank()) {
            // Pure decoration (a line of glyphs): treat it as already spoken.
            activeChunkIndex = index
            PlaybackStateStore.currentIndex = index
            clearResumePoint()
            advanceAfterSection()
            return
        }
        activeChunkUtteranceId = "$CHUNK_UTTERANCE_PREFIX${UUID.randomUUID()}"
        activeChunkIndex = index
        activeChunkStartedAt = System.currentTimeMillis()
        activeChunkSpeechText = rawChunkText
        activeChunkBaseOffset = resumeOffset
        spokenCharOffset = resumeOffset
        spokenWordCount = wordCountBefore(rawChunkText, resumeOffset)
        updateCurrentSentenceBounds(resumeOffset)
        PlaybackStateStore.queueCount = repository.loadQueueDocuments().size
        activeDocument?.let { repository.updateProgress(it.id, index, chunks.size) }
        updateMediaSessionMetadata()
        updateMediaSessionState()
        refreshForegroundNotification()
        // Slide decks get a breathing beat at slide changes and after titles, so the
        // narration has a presenter's cadence instead of an unbroken stream. The
        // silence utterance's onDone is ignored by the activeChunkUtteranceId guard.
        val silenceMs = leadingSilenceMsFor(index)
        val silenceQueued = silenceMs > 0L && tts?.playSilentUtterance(
            silenceMs, TextToSpeech.QUEUE_FLUSH, "silence-${UUID.randomUUID()}"
        ) == TextToSpeech.SUCCESS
        val queueMode = if (silenceQueued) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
        val result = tts?.speak(speakText, queueMode, null, activeChunkUtteranceId)
            ?: TextToSpeech.ERROR
        if (result == TextToSpeech.ERROR) {
            handleTtsFailure("The voice engine rejected this sentence.", activeChunkUtteranceId)
        }
    }

    private fun leadingSilenceMsFor(index: Int): Long {
        val pages = chunkPageNumbers ?: return 0L
        if (index !in 0 until pages.size) return 0L
        val page = pages[index]
        val firstOfSlide = index == 0 || pages[index - 1] != page
        if (firstOfSlide) return SLIDE_TRANSITION_SILENCE_MS
        val secondOfSlide = index == 1 || pages[index - 2] != page
        if (secondOfSlide) return TITLE_BEAT_SILENCE_MS
        return 0L
    }

    private fun speakSelectionText(rawText: String) {
        val text = repository.applyPronunciationRules(rawText.trim())
        if (text.isBlank()) return
        val narrationSettings = repository.loadNarrationSettings()
        val effectiveRate = NarrationAnalyzer.effectiveRate(PlaybackStateStore.rate, narrationSettings, text)
        val effectivePitch = NarrationAnalyzer.effectivePitch(PlaybackStateStore.pitch, narrationSettings, text)
        tts?.let { VoiceConfigurator.apply(it, repository.loadVoiceSettings()) }
        tts?.setSpeechRate(effectiveRate)
        tts?.setPitch(effectivePitch)
        PlaybackStateStore.statusMessage = "Reading selected text."
        updateMediaSessionState()
        refreshForegroundNotification()
        val utteranceId = "$SELECTION_UTTERANCE_PREFIX${UUID.randomUUID()}"
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId) ?: TextToSpeech.ERROR
        if (result == TextToSpeech.ERROR) {
            handleTtsFailure("The voice engine rejected the selected text.", utteranceId)
        }
    }

    private fun advanceAfterSection() {
        if (!PlaybackStateStore.isPlaying || chunks.isEmpty()) return

        // Fallback check: Did the sleep timer expire during deep sleep?
        val now = System.currentTimeMillis()
        val timer = PlaybackStateStore.activeSleepTimerSnapshot(now)
        if (timer != null && !timer.stopAtEndOfSection && now >= timer.endsAtMillis) {
            fireSleepTimer()
            return
        }

        // Advance from the sentence we ACTUALLY just spoke (activeChunkIndex), not the
        // UI-shared PlaybackStateStore.currentIndex. The shared index can be mutated by
        // unrelated UI actions (opening/closing a document, recomposition, a stray tap)
        // while an utterance is in flight; reading it here is the root cause of playback
        // jumping to the wrong place ("rat bug"). The internal index is the source of truth.
        val current = PlaybackAdvance.resolveCurrentIndex(
            activeChunkIndex = activeChunkIndex,
            sharedIndex = PlaybackStateStore.currentIndex,
            lastIndex = chunks.lastIndex
        )

        // Section sleep timer check
        if (timer != null && timer.stopAtEndOfSection) {
            val doc = activeDocument
            if (doc != null) {
                val rawText = repository.readText(doc)
                val readerModel = ReaderTextModelCache.get(doc.id, rawText, doc.pageCount)
                val currentPart = readerModel.partForSentence(current)
                if (currentPart != null && current == currentPart.sentenceEndIndexExclusive - 1) {
                    fireSleepTimer()
                    return
                }
            }
        }

        if (current < chunks.lastIndex) {
            PlaybackStateStore.currentIndex = current + 1
            activeDocument?.let { repository.updateProgress(it.id, PlaybackStateStore.currentIndex, chunks.size) }
            speakCurrent()
        } else {
            val completedId = activeDocument?.id
            activeDocument?.let { repository.updateProgress(it.id, current, chunks.size) }
            if (PlaybackStateStore.autoPlayQueue) {
                playNextQueuedOrFinish(completedId)
            } else {
                finishPlayback("Finished reading.")
            }
        }
    }

    private fun playNextQueuedOrFinish(completedDocumentId: String?) {
        val next = repository.completeCurrentAndGetNextQueued(completedDocumentId)
        PlaybackStateStore.queueCount = repository.loadQueueDocuments().size
        if (next == null) {
            finishPlayback("Finished reading. Queue is empty.")
            return
        }

        val startIndex = next.currentIndex.coerceAtLeast(0)
        if (!loadDocument(next.id, startIndex)) {
            finishPlayback("Finished reading. Could not open the next queued item.")
            return
        }

        PlaybackStateStore.isPlaying = true
        PlaybackStateStore.statusMessage = "Auto-playing next in queue."
        startForegroundNow()
        ensureTtsReadyAndSpeak()
    }

    private fun finishPlayback(message: String) {
        PlaybackStateStore.isPlaying = false
        if (PlaybackStateStore.isForegroundActive) {
            PlaybackStateStore.isForegroundActive = false
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        PlaybackStateStore.statusMessage = message
        updateMediaSessionState()
        refreshForegroundNotification()
    }

    private fun moveBy(delta: Int) {
        if (chunks.isEmpty()) {
            val documentId = PlaybackStateStore.activeDocumentId ?: return
            if (!loadDocument(documentId, PlaybackStateStore.currentIndex)) return
        }

        if (delta > 0 && PlaybackStateStore.currentIndex >= chunks.lastIndex && PlaybackStateStore.autoPlayQueue) {
            playNextQueuedOrFinish(activeDocument?.id)
            return
        }

        val newIndex = (PlaybackStateStore.currentIndex + delta).coerceIn(0, chunks.lastIndex)
        clearResumePoint()
        PlaybackStateStore.currentIndex = newIndex
        activeDocument?.let { repository.updateProgress(it.id, newIndex, chunks.size) }
        if (PlaybackStateStore.isPlaying) {
            speakCurrent()
        } else {
            refreshForegroundNotification()
        }
    }

    private fun pauseSpeech(message: String = "Paused.") {
        pausedDueToTransientFocusLoss = false
        rememberPausePoint()
        tts?.stop()
        PlaybackStateStore.isPlaying = false
        if (PlaybackStateStore.isForegroundActive) {
            PlaybackStateStore.isForegroundActive = false
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        PlaybackStateStore.statusMessage = message
        activeDocument?.let { repository.updateProgress(it.id, PlaybackStateStore.currentIndex, chunks.size) }
        updateMediaSessionState()
        refreshForegroundNotification()
        abandonAudioFocus()
    }

    private fun pauseSpeechTransiently(message: String) {
        rememberPausePoint()
        tts?.stop()
        PlaybackStateStore.isPlaying = false
        if (PlaybackStateStore.isForegroundActive) {
            PlaybackStateStore.isForegroundActive = false
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        PlaybackStateStore.statusMessage = message
        activeDocument?.let { repository.updateProgress(it.id, PlaybackStateStore.currentIndex, chunks.size) }
        updateMediaSessionState()
        refreshForegroundNotification()
        // Do NOT call abandonAudioFocus() here
    }

    private fun rememberPausePoint() {
        val docId = activeDocument?.id ?: return clearResumePoint()
        val index = PlaybackStateStore.currentIndex
        val text = activeChunkSpeechText
        val canResume = activeChunkIndex == index &&
            text.isNotBlank() &&
            spokenWordCount >= RESUME_WORD_THRESHOLD &&
            spokenCharOffset in 1 until text.length
        if (!canResume) {
            clearResumePoint()
            return
        }
        resumeDocumentId = docId
        resumeChunkIndex = index
        resumeCharOffset = normalizeResumeOffset(text, spokenCharOffset)
        resumeWordCount = spokenWordCount
    }

    private fun resumeOffsetForCurrentChunk(text: String, index: Int): Int {
        val docId = activeDocument?.id ?: return 0
        if (docId != resumeDocumentId || index != resumeChunkIndex || resumeWordCount < RESUME_WORD_THRESHOLD) return 0
        return normalizeResumeOffset(text, resumeCharOffset).takeIf { it in 1 until text.length } ?: 0
    }

    private fun normalizeResumeOffset(text: String, offset: Int): Int {
        var safeOffset = offset.coerceIn(0, text.length)
        // Skip whitespace and trailing punctuation so resume never starts on a
        // stray period/comma left over from the previous word boundary.
        while (safeOffset < text.length &&
            (text[safeOffset].isWhitespace() || text[safeOffset] in RESUME_SKIP_CHARS)
        ) safeOffset++
        return if (safeOffset >= text.length) 0 else safeOffset
    }

    private fun wordCountBefore(text: String, charOffset: Int): Int {
        val safeOffset = charOffset.coerceIn(0, text.length)
        if (safeOffset <= 0) return 0
        return Regex("\\S+").findAll(text.take(safeOffset)).count()
    }

    private fun updateCurrentSentenceBounds(charOffset: Int) {
        val text = activeChunkSpeechText
        if (text.isBlank()) {
            PlaybackStateStore.currentSentenceStart = 0
            PlaybackStateStore.currentSentenceEnd = 0
            return
        }
        val safeOffset = charOffset.coerceIn(0, text.length)
        val sentenceStart = text
            .lastIndexOfAny(charArrayOf('.', '!', '?', '\n'), (safeOffset - 1).coerceAtLeast(0))
            .let { if (it < 0) 0 else (it + 1).coerceAtMost(text.length) }
            .let { start ->
                var adjusted = start
                while (adjusted < text.length && text[adjusted].isWhitespace()) adjusted++
                adjusted
            }
        val sentenceEnd = text
            .indexOfAny(charArrayOf('.', '!', '?', '\n'), safeOffset)
            .let { if (it < 0) text.length else (it + 1).coerceAtMost(text.length) }
        if (sentenceEnd > sentenceStart) {
            PlaybackStateStore.currentSentenceStart = sentenceStart
            PlaybackStateStore.currentSentenceEnd = sentenceEnd
        } else {
            PlaybackStateStore.currentSentenceStart = safeOffset
            PlaybackStateStore.currentSentenceEnd = safeOffset
        }
    }

    private fun clearResumePoint() {
        activeChunkUtteranceId = null
        activeChunkIndex = -1
        activeChunkSpeechText = ""
        activeChunkBaseOffset = 0
        spokenCharOffset = 0
        spokenWordCount = 0
        resumeDocumentId = null
        resumeChunkIndex = -1
        resumeCharOffset = 0
        resumeWordCount = 0
        PlaybackStateStore.currentSentenceStart = 0
        PlaybackStateStore.currentSentenceEnd = 0
    }

    private fun stopSpeechAndService(message: String = "Stopped.") {
        pausedDueToTransientFocusLoss = false
        pendingSpeak = false
        pendingSelectionText = null
        cancelSleepTimerCallback()
        PlaybackStateStore.clearSleepTimer()
        repository.clearSleepTimerState()
        clearResumePoint()
        tts?.stop()
        activeDocument?.let { repository.updateProgress(it.id, PlaybackStateStore.currentIndex, chunks.size) }
        PlaybackStateStore.isPlaying = false
        PlaybackStateStore.isForegroundActive = false
        PlaybackStateStore.statusMessage = message
        updateMediaSessionState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        abandonAudioFocus()
        stopSelf()
    }

    private fun scheduleSleepTimerFromStore() {
        cancelSleepTimerCallback()
        val snapshot = PlaybackStateStore.activeSleepTimerSnapshot() ?: return
        if (snapshot.stopAtEndOfSection) {
            return
        }
        val runnable = Runnable { fireSleepTimer() }
        sleepTimerRunnable = runnable
        mainHandler.postDelayed(runnable, snapshot.remainingMillis().coerceAtLeast(1L))
    }

    private fun cancelSleepTimerCallback() {
        sleepTimerRunnable?.let(mainHandler::removeCallbacks)
        sleepTimerRunnable = null
    }

    private fun cancelSleepTimer(message: String) {
        cancelSleepTimerCallback()
        PlaybackStateStore.clearSleepTimer()
        repository.clearSleepTimerState()
        PlaybackStateStore.statusMessage = message
        updateMediaSessionState()
        refreshForegroundNotification()
    }

    private fun fireSleepTimer() {
        val snapshot = PlaybackStateStore.activeSleepTimerSnapshot() ?: return
        cancelSleepTimerCallback()
        repository.clearSleepTimerState()
        // If TTS is actively speaking, defer the action until the current
        // sentence finishes (pendingTimerFire checked in onDone).
        // If not playing, fire immediately.
        if (PlaybackStateStore.isPlaying) {
            pendingTimerFire = true
            // Store the snapshot action so executePendingTimerFire can use it.
            // We clear the store timer here so the section-advance logic doesn't
            // re-trigger, but we keep the action locally.
            PlaybackStateStore.clearSleepTimer()
            // Store the action in a field for deferred execution.
            pendingTimerAction = snapshot.action
        } else {
            PlaybackStateStore.clearSleepTimer()
            when (snapshot.action) {
                VeritasSleepTimerAction.PAUSE -> pauseSpeech("Sleep timer paused playback.")
                VeritasSleepTimerAction.STOP  -> stopSpeechAndService("Sleep timer stopped playback.")
            }
        }
    }

    private var pendingTimerAction: VeritasSleepTimerAction = VeritasSleepTimerAction.PAUSE

    private fun executePendingTimerFire() {
        clearResumePoint()
        when (pendingTimerAction) {
            VeritasSleepTimerAction.PAUSE -> pauseSpeech("Sleep timer paused after sentence.")
            VeritasSleepTimerAction.STOP  -> stopSpeechAndService("Sleep timer stopped after sentence.")
        }
    }

    private fun refreshForegroundNotification() {
        if (PlaybackStateStore.isForegroundActive) {
            startForegroundNow()
        }
    }

    private fun estimatedDurationMs(): Long {
        if (chunks.isEmpty()) return 0L
        val totalChars = chunks.sumOf { it.length.coerceAtLeast(1) }.coerceAtLeast(1)
        val charsPerSecond = (14.0 * PlaybackStateStore.rate.coerceIn(0.5f, 2.0f)).coerceAtLeast(7.0)
        return ((totalChars / charsPerSecond) * 1000.0)
            .toLong()
            .coerceAtLeast(chunks.size * 800L)
            .coerceAtLeast(1000L)
    }

    private fun estimatedPositionMs(includeCurrentSection: Boolean = true): Long {
        if (chunks.isEmpty()) return 0L
        val duration = estimatedDurationMs().coerceAtLeast(1L)
        val totalChars = chunks.sumOf { it.length.coerceAtLeast(1) }.coerceAtLeast(1)
        val safeIndex = PlaybackStateStore.currentIndex.coerceIn(0, chunks.lastIndex)
        val completedChars = chunks.take(safeIndex).sumOf { it.length.coerceAtLeast(1) } +
            if (includeCurrentSection) (chunks.getOrNull(safeIndex)?.length?.coerceAtLeast(1) ?: 0) else 0
        return ((completedChars.toDouble() / totalChars.toDouble()) * duration.toDouble())
            .toLong()
            .coerceIn(0L, duration)
    }

    private fun indexForEstimatedPosition(positionMs: Long): Int {
        if (chunks.isEmpty()) return 0
        val duration = estimatedDurationMs().coerceAtLeast(1L)
        val totalChars = chunks.sumOf { it.length.coerceAtLeast(1) }.coerceAtLeast(1)
        val targetChars = ((positionMs.coerceIn(0L, duration).toDouble() / duration.toDouble()) * totalChars.toDouble()).toInt()
        var running = 0
        chunks.forEachIndexed { index, chunk ->
            running += chunk.length.coerceAtLeast(1)
            if (targetChars <= running) return index
        }
        return chunks.lastIndex
    }

    private fun seekToEstimatedPosition(positionMs: Long) {
        if (chunks.isEmpty()) {
            val documentId = PlaybackStateStore.activeDocumentId ?: return
            if (!loadDocument(documentId, PlaybackStateStore.currentIndex)) return
        }
        val targetIndex = indexForEstimatedPosition(positionMs).coerceIn(0, chunks.lastIndex)
        clearResumePoint()
        PlaybackStateStore.currentIndex = targetIndex
        activeDocument?.let { repository.updateProgress(it.id, targetIndex, chunks.size) }
        if (PlaybackStateStore.isPlaying) {
            speakCurrent()
        } else {
            updateMediaSessionMetadata()
            updateMediaSessionState()
            refreshForegroundNotification()
        }
    }

    private fun requestAudioFocus(): Boolean {
        val focusChangeListener = android.media.AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                android.media.AudioManager.AUDIOFOCUS_LOSS -> {
                    pausedDueToTransientFocusLoss = false
                    pauseSpeech("Paused — audio focus lost.")
                }
                android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (PlaybackStateStore.isPlaying) {
                        pausedDueToTransientFocusLoss = true
                        pauseSpeechTransiently("Paused — interrupted.")
                    }
                }
                android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    tts?.setSpeechRate(PlaybackStateStore.rate * 0.75f)
                }
                android.media.AudioManager.AUDIOFOCUS_GAIN -> {
                    tts?.setSpeechRate(PlaybackStateStore.rate)
                    if (pausedDueToTransientFocusLoss) {
                        pausedDueToTransientFocusLoss = false
                        handlePlay(null)
                    }
                }
            }
        }
        val req = android.media.AudioFocusRequest.Builder(android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
        audioFocusRequest = req
        return audioManager.requestAudioFocus(req) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }

    private fun startForegroundNow() {
        updateMediaSessionMetadata()
        updateMediaSessionState()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this,
            100,
            openAppIntent,
            pendingIntentFlags()
        )

        val playPauseAction = if (PlaybackStateStore.isPlaying) {
            NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", servicePendingIntent(PlaybackActions.ACTION_PAUSE, 101))
        } else {
            NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", servicePendingIntent(PlaybackActions.ACTION_PLAY, 102))
        }

        val nowMillis = System.currentTimeMillis()
        val progressText = if (PlaybackStateStore.chunkCount > 0) {
            val queueText = if (PlaybackStateStore.queueCount > 0) " • ${PlaybackStateStore.queueCount} queued" else ""
            val timerText = PlaybackStateStore.activeSleepTimerSnapshot(nowMillis)
                ?.let { " • ${it.menuLabel(nowMillis)}" }
                .orEmpty()
            "Sentence ${PlaybackStateStore.currentIndex + 1} of ${PlaybackStateStore.chunkCount}$queueText$timerText"
        } else {
            PlaybackStateStore.statusMessage
        }

        val durationMs = estimatedDurationMs()
        val positionMs = estimatedPositionMs()
        val progressMax = 1000
        val progressValue = if (durationMs > 0L) {
            ((positionMs.toDouble() / durationMs.toDouble()) * progressMax).toInt().coerceIn(0, progressMax)
        } else {
            0
        }
        val artwork = currentNotificationArtwork()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_veritas)
            .setLargeIcon(artwork)
            .setContentTitle(PlaybackStateStore.documentTitle.ifBlank { getString(R.string.app_name) })
            .setContentText(progressText)
            .setSubText(PlaybackStateStore.sourceLabel.ifBlank { "Text-to-speech" })
            .setContentIntent(contentIntent)
            .setOngoing(PlaybackStateStore.isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setProgress(progressMax, progressValue, durationMs <= 0L)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous, "Previous", servicePendingIntent(PlaybackActions.ACTION_PREVIOUS, 103)))
            .addAction(playPauseAction)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", servicePendingIntent(PlaybackActions.ACTION_NEXT, 104)))
            .addAction(NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel, "Stop", servicePendingIntent(PlaybackActions.ACTION_STOP, 105)))
        mediaSession?.let { session ->
            builder.setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }
        return builder.build()
    }

    private fun setupMediaSession() {
        val player = VeritasMediaSessionPlayer(
            applicationLooper = Looper.getMainLooper(),
            snapshot = { mediaSessionSnapshot() },
            controller = object : VeritasMediaSessionPlayer.Controller {
                override fun play() {
                    startService(Intent(this@PlaybackService, PlaybackService::class.java).setAction(PlaybackActions.ACTION_PLAY))
                }

                override fun pause() {
                    startService(Intent(this@PlaybackService, PlaybackService::class.java).setAction(PlaybackActions.ACTION_PAUSE))
                }

                override fun stop() {
                    startService(Intent(this@PlaybackService, PlaybackService::class.java).setAction(PlaybackActions.ACTION_STOP))
                }

                override fun next() {
                    startService(Intent(this@PlaybackService, PlaybackService::class.java).setAction(PlaybackActions.ACTION_NEXT))
                }

                override fun previous() {
                    startService(Intent(this@PlaybackService, PlaybackService::class.java).setAction(PlaybackActions.ACTION_PREVIOUS))
                }

                override fun seekTo(positionMs: Long) {
                    seekToEstimatedPosition(positionMs)
                }

                override fun setPlaybackParameters(rate: Float, pitch: Float) {
                    PlaybackStateStore.rate = rate
                    PlaybackStateStore.pitch = pitch
                    if (PlaybackStateStore.isPlaying) speakCurrent()
                    updateMediaSessionState()
                    refreshForegroundNotification()
                }
            }
        )
        mediaSessionPlayer = player
        mediaSession = MediaSession.Builder(this, player)
            .setId("VeritasReaderSession")
            .build()
        updateMediaSessionMetadata()
        updateMediaSessionState()
    }

    private fun updateMediaSessionMetadata() {
        mediaSessionPlayer?.notifyStateChanged()
    }

    private fun updateMediaSessionState() {
        mediaSessionPlayer?.notifyStateChanged()
        updateVeritasWidgets(this)
    }

    private fun mediaSessionSnapshot(): VeritasMediaSessionPlayer.PlaybackSnapshot {
        val sectionLabel = if (PlaybackStateStore.chunkCount > 0) {
            "Sentence ${PlaybackStateStore.currentIndex + 1} of ${PlaybackStateStore.chunkCount}"
        } else {
            PlaybackStateStore.statusMessage
        }
        val documentId = PlaybackStateStore.activeDocumentId.orEmpty()
        // Load artwork bytes lazily and cache per-document
        if (artworkBytesDocumentId != documentId) {
            artworkBytes = documentId.takeIf { it.isNotBlank() }?.let { id ->
                CoverExtractor.coverFile(this, id)?.takeIf { it.exists() }?.let { file ->
                    runCatching { file.readBytes() }.getOrNull()
                }
            }
            artworkBytesDocumentId = documentId
        }
        return VeritasMediaSessionPlayer.PlaybackSnapshot(
            documentId = documentId,
            title = PlaybackStateStore.documentTitle.ifBlank { getString(R.string.app_name) },
            sourceLabel = PlaybackStateStore.sourceLabel.ifBlank { "Text-to-speech reader" },
            sectionLabel = sectionLabel,
            isPlaying = PlaybackStateStore.isPlaying,
            isForegroundActive = PlaybackStateStore.isForegroundActive,
            chunkCount = PlaybackStateStore.chunkCount,
            currentIndex = PlaybackStateStore.currentIndex,
            durationMs = estimatedDurationMs(),
            positionMs = estimatedPositionMs(),
            rate = PlaybackStateStore.rate,
            pitch = PlaybackStateStore.pitch,
            artworkData = artworkBytes
        )
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, PlaybackService::class.java).setAction(action)
        return PendingIntent.getService(this, requestCode, intent, pendingIntentFlags())
    }

    private fun pendingIntentFlags(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }

    private fun currentNotificationArtwork(): Bitmap {
        val documentId = activeDocument?.id ?: PlaybackStateStore.activeDocumentId
        notificationArtwork?.takeIf { artworkDocumentId == documentId }?.let { return it }

        val document = activeDocument ?: documentId?.let { repository.findDocument(it) }
        val source = document?.let { loadNotificationCover(it) }
            ?: BitmapFactory.decodeResource(resources, R.drawable.veritas_reader_icon)
            ?: createBitmap(512, 512).also {
                Canvas(it).drawColor(Color.rgb(18, 23, 27))
            }
        val artwork = squareFitBitmap(source)
        artworkDocumentId = documentId
        notificationArtwork = artwork
        return artwork
    }

    private fun loadNotificationCover(document: SavedDocument): Bitmap? {
        // 1. Try to load pre-extracted cover if it exists
        CoverExtractor.coverFile(this, document.id)?.let { file ->
            runCatching {
                BitmapFactory.decodeFile(file.absolutePath)
            }.getOrNull()?.let { return it }
        }

        // 2. Fallback to on-the-fly loading/rendering
        val original = repository.originalUri(document) ?: return null
        val mime = document.originalMimeType.lowercase()
        return when {
            mime.startsWith("image/") -> {
                contentResolver.openInputStream(original)?.use { BitmapFactory.decodeStream(it) }
            }
            mime == "application/pdf" || document.originalFileName.endsWith(".pdf", ignoreCase = true) -> renderPdfFirstPage(original)
            else -> null
        }
    }

    private fun renderPdfFirstPage(uri: Uri): Bitmap? {
        return runCatching {
            contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    if (renderer.pageCount <= 0) return@use null
                    renderer.openPage(0).use { page ->
                        val width = 512
                        val height = ((width.toFloat() / page.width.toFloat()) * page.height).toInt().coerceAtLeast(1)
                        createBitmap(width, height).also { bitmap ->
                            Canvas(bitmap).drawColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        }
                    }
                }
            }
        }.getOrNull()
    }

    private fun squareFitBitmap(source: Bitmap, targetSize: Int = 512): Bitmap {
        val output = createBitmap(targetSize, targetSize)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.rgb(18, 23, 27))
        val scale = min(
            targetSize.toFloat() / source.width.toFloat().coerceAtLeast(1f),
            targetSize.toFloat() / source.height.toFloat().coerceAtLeast(1f)
        )
        val width = source.width * scale
        val height = source.height * scale
        val left = (targetSize - width) / 2f
        val top = (targetSize - height) / 2f
        canvas.drawBitmap(source, null, RectF(left, top, left + width, top + height), paint)
        return output
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reader playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background text-to-speech playback controls"
            setShowBadge(false)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        pendingSpeak = false
        pendingSelectionText = null
        cancelSleepTimerCallback()
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
        mediaSession?.release()
        mediaSession = null
        mediaSessionPlayer?.release()
        mediaSessionPlayer = null
        abandonAudioFocus()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "PlaybackService"
        private const val CHANNEL_ID = "reader_playback"
        private const val NOTIFICATION_ID = 41
        private const val SELECTION_UTTERANCE_PREFIX = "selection:"
        private const val CHUNK_UTTERANCE_PREFIX = "chunk:"
        // Slide-deck cadence: a beat when a new slide starts, a shorter one after
        // its title line. Snappy on purpose — the user asked for rhythm, not lag.
        private const val SLIDE_TRANSITION_SILENCE_MS = 300L
        private const val TITLE_BEAT_SILENCE_MS = 250L
        private const val RESUME_WORD_THRESHOLD = 8
        private const val RESUME_SKIP_CHARS = ".,;:!?)]}\"'»›—–-"
    }
}
