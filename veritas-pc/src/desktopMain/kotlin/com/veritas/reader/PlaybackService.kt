package com.veritas.reader

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.File
import java.util.UUID

class PlaybackService(private val context: Context) {
    private val repository = DocumentRepository(context)
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeak = false
    private var pendingSelectionText: String? = null
    
    private var activeDocument: SavedDocument? = null
    private var chunks: List<String> = emptyList()
    private var activeChunkUtteranceId: String? = null
    private var activeChunkIndex = -1
    private var pendingJumpCharOffset: Int? = null

    init {
        PlaybackStateStore.queueCount = repository.loadQueueDocuments().size
    }

    companion object {
        private var instance: PlaybackService? = null
        
        fun start(intent: Intent) {
            val ctx = Context()
            val service = instance ?: PlaybackService(ctx).also { instance = it }
            service.onStartCommand(intent)
        }
    }

    fun onStartCommand(intent: Intent) {
        when (intent.action) {
            PlaybackActions.ACTION_PLAY -> handlePlay(intent)
            PlaybackActions.ACTION_PAUSE -> pauseSpeech()
            PlaybackActions.ACTION_STOP -> stopSpeechAndService()
            PlaybackActions.ACTION_NEXT -> moveBy(1)
            PlaybackActions.ACTION_PREVIOUS -> moveBy(-1)
            PlaybackActions.ACTION_JUMP_TO -> handleJump(intent)
            PlaybackActions.ACTION_SPEAK_SELECTION -> handleSpeakSelection(intent)
            PlaybackActions.ACTION_UPDATE_PLAYBACK_SETTINGS -> handlePlaybackSettingsUpdate(intent)
            PlaybackActions.ACTION_SET_SLEEP_TIMER -> handleSetSleepTimer(intent)
            PlaybackActions.ACTION_CANCEL_SLEEP_TIMER -> cancelSleepTimer()
        }
    }

    private fun handlePlay(intent: Intent) {
        val documentId = intent.getStringExtra(PlaybackActions.EXTRA_DOCUMENT_ID)
            ?: PlaybackStateStore.activeDocumentId
            ?: repository.loadQueueDocuments().firstOrNull()?.id
            ?: return
        val startIndex = intent.getIntExtra(PlaybackActions.EXTRA_START_INDEX, PlaybackStateStore.currentIndex)
        val charOffset = intent.getIntExtra(PlaybackActions.EXTRA_CHAR_OFFSET, -1).takeIf { it >= 0 }

        PlaybackStateStore.rate = intent.getFloatExtra(PlaybackActions.EXTRA_RATE, PlaybackStateStore.rate).coerceIn(0.5f, 2.0f)
        PlaybackStateStore.pitch = intent.getFloatExtra(PlaybackActions.EXTRA_PITCH, PlaybackStateStore.pitch).coerceIn(0.7f, 1.4f)

        if (!loadDocument(documentId, startIndex)) return

        pendingJumpCharOffset = charOffset
        PlaybackStateStore.isPlaying = true
        PlaybackStateStore.isForegroundActive = true
        PlaybackStateStore.statusMessage = "Preparing playback…"
        ensureTtsReadyAndSpeak()
    }

    private fun handleJump(intent: Intent) {
        val documentId = intent.getStringExtra(PlaybackActions.EXTRA_DOCUMENT_ID)
            ?: PlaybackStateStore.activeDocumentId
            ?: return
        val index = intent.getIntExtra(PlaybackActions.EXTRA_START_INDEX, PlaybackStateStore.currentIndex)
        val charOffset = intent.getIntExtra(PlaybackActions.EXTRA_CHAR_OFFSET, -1).takeIf { it >= 0 }

        val wasPlaying = PlaybackStateStore.isPlaying
        if (!loadDocument(documentId, index)) return
        repository.updateProgress(documentId, PlaybackStateStore.currentIndex, chunks.size)

        pendingJumpCharOffset = charOffset
        if (wasPlaying || charOffset != null) {
            PlaybackStateStore.isPlaying = true
            speakCurrent()
        }
    }

    private fun handleSpeakSelection(intent: Intent) {
        val selection = intent.getStringExtra(PlaybackActions.EXTRA_SELECTION_TEXT)
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            ?.take(1200)
            .orEmpty()
        if (selection.isBlank()) return
        PlaybackStateStore.isPlaying = false
        ensureTtsReadyAndSpeakSelection(selection)
    }

    private fun handlePlaybackSettingsUpdate(intent: Intent) {
        PlaybackStateStore.rate = intent.getFloatExtra(PlaybackActions.EXTRA_RATE, PlaybackStateStore.rate).coerceIn(0.5f, 2.0f)
        PlaybackStateStore.pitch = intent.getFloatExtra(PlaybackActions.EXTRA_PITCH, PlaybackStateStore.pitch).coerceIn(0.7f, 1.4f)
        if (PlaybackStateStore.isPlaying) {
            speakCurrent()
        }
    }

    private fun handleSetSleepTimer(intent: Intent) {
        val durationMillis = intent.getLongExtra(PlaybackActions.EXTRA_SLEEP_TIMER_DURATION_MILLIS, 0L)
        val actionName = intent.getStringExtra(PlaybackActions.EXTRA_SLEEP_TIMER_ACTION)
        val action = VeritasSleepTimerAction.fromName(actionName)
        val stopAtEndOfSection = intent.getBooleanExtra(PlaybackActions.EXTRA_SLEEP_TIMER_STOP_AT_END_OF_SECTION, false)

        val request = VeritasSleepTimerRequest(durationMillis, action, stopAtEndOfSection)
        PlaybackStateStore.setSleepTimer(request)
        repository.saveSleepTimerState(
            durationMillis = PlaybackStateStore.sleepTimerDurationMillis,
            endsAtMillis = PlaybackStateStore.sleepTimerEndsAtMillis,
            actionName = PlaybackStateStore.sleepTimerActionName,
            stopAtEndOfSection = PlaybackStateStore.sleepTimerStopAtEndOfSection
        )
        PlaybackStateStore.statusMessage = if (stopAtEndOfSection) {
            "Sleep timer set to stop at end of section."
        } else {
            "Sleep timer set for ${VeritasSleepTimerFormatter.formatDuration(request.durationMillis)}."
        }
    }

    private fun cancelSleepTimer() {
        PlaybackStateStore.clearSleepTimer()
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
            activeDocument = doc
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
        if (tts == null) {
            tts = TextToSpeech(context, { status -> handleTtsInit(status) })
        } else {
            VoiceConfigurator.apply(tts!!, voiceSettings)
            speakCurrent()
        }
    }

    private fun ensureTtsReadyAndSpeakSelection(text: String) {
        val voiceSettings = repository.loadVoiceSettings()
        pendingSelectionText = text
        if (tts == null) {
            tts = TextToSpeech(context, { status -> handleTtsInit(status) })
        } else {
            VoiceConfigurator.apply(tts!!, voiceSettings)
            speakSelectionText(text)
        }
    }

    private fun handleTtsInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val voiceSettings = repository.loadVoiceSettings()
            val message = tts?.let { VoiceConfigurator.apply(it, voiceSettings) }
                ?: "Ready."
            ttsReady = true
            attachListener()
            PlaybackStateStore.statusMessage = message
            
            val selection = pendingSelectionText
            if (selection != null) {
                pendingSelectionText = null
                speakSelectionText(selection)
            } else {
                speakCurrent()
            }
        } else {
            PlaybackStateStore.isPlaying = false
            PlaybackStateStore.statusMessage = "TTS engine initialization failed."
        }
    }

    private fun attachListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                if (utteranceId != activeChunkUtteranceId) return
                if (PlaybackStateStore.isPlaying) {
                    advanceAfterSection()
                }
            }

            override fun onError(utteranceId: String?) {
                if (utteranceId != activeChunkUtteranceId) return
                PlaybackStateStore.isPlaying = false
                PlaybackStateStore.statusMessage = "Speech synthesis error."
            }
        })
    }

    private fun speakCurrent() {
        if (chunks.isEmpty()) return
        val index = PlaybackStateStore.currentIndex.coerceIn(0, chunks.lastIndex)
        val rawChunkText = chunks[index].trim()

        val text = repository.applyPronunciationRules(rawChunkText)
        if (text.isBlank()) return

        val voiceSettings = repository.loadVoiceSettings()
        val narrationSettings = repository.loadNarrationSettings()
        val effectiveRate = NarrationAnalyzer.effectiveRate(PlaybackStateStore.rate, narrationSettings, text)
        val effectivePitch = NarrationAnalyzer.effectivePitch(PlaybackStateStore.pitch, narrationSettings, text)
        
        tts?.let { VoiceConfigurator.apply(it, voiceSettings) }
        tts?.setSpeechRate(effectiveRate)
        tts?.setPitch(effectivePitch)

        PlaybackStateStore.currentIndex = index
        PlaybackStateStore.isPlaying = true
        PlaybackStateStore.statusMessage = "Reading section..."
        
        // Silence decorative glyphs (bullets, arrows, dot leaders) before the
        // engine sees them; replacements are length-preserving. Pure-decoration
        // chunks are skipped like an already-finished utterance.
        val speakText = SpeechSanitizer.forSpeech(text)
        if (speakText.isBlank()) {
            activeChunkIndex = index
            PlaybackStateStore.currentIndex = index
            advanceAfterSection()
            return
        }
        activeChunkUtteranceId = UUID.randomUUID().toString()
        activeChunkIndex = index

        activeDocument?.let { repository.updateProgress(it.id, index, chunks.size) }
        tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, activeChunkUtteranceId!!)
    }

    private fun speakSelectionText(rawText: String) {
        val text = repository.applyPronunciationRules(rawText.trim())
        if (text.isBlank()) return
        val voiceSettings = repository.loadVoiceSettings()
        val narrationSettings = repository.loadNarrationSettings()
        val effectiveRate = NarrationAnalyzer.effectiveRate(PlaybackStateStore.rate, narrationSettings, text)
        val effectivePitch = NarrationAnalyzer.effectivePitch(PlaybackStateStore.pitch, narrationSettings, text)
        
        tts?.let { VoiceConfigurator.apply(it, voiceSettings) }
        tts?.setSpeechRate(effectiveRate)
        tts?.setPitch(effectivePitch)
        
        PlaybackStateStore.statusMessage = "Reading selection..."
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun advanceAfterSection() {
        val current = activeChunkIndex
        if (current < chunks.lastIndex) {
            PlaybackStateStore.currentIndex = current + 1
            speakCurrent()
        } else {
            val completedId = activeDocument?.id
            if (PlaybackStateStore.autoPlayQueue) {
                playNextQueuedOrFinish(completedId)
            } else {
                finishPlayback()
            }
        }
    }

    private fun playNextQueuedOrFinish(completedDocumentId: String?) {
        val next = repository.completeCurrentAndGetNextQueued(completedDocumentId)
        PlaybackStateStore.queueCount = repository.loadQueueDocuments().size
        if (next == null) {
            finishPlayback()
            return
        }
        val startIndex = next.currentIndex.coerceAtLeast(0)
        PlaybackStateStore.currentIndex = startIndex
        if (loadDocument(next.id, startIndex)) {
            speakCurrent()
        } else {
            finishPlayback()
        }
    }

    private fun finishPlayback() {
        PlaybackStateStore.isPlaying = false
        PlaybackStateStore.statusMessage = "Finished reading."
        tts?.stop()
    }

    private fun pauseSpeech() {
        PlaybackStateStore.isPlaying = false
        PlaybackStateStore.statusMessage = "Paused."
        tts?.stop()
    }

    private fun stopSpeechAndService() {
        PlaybackStateStore.isPlaying = false
        PlaybackStateStore.statusMessage = "Ready."
        tts?.stop()
    }

    private fun moveBy(offset: Int) {
        if (chunks.isEmpty()) return
        val newIndex = (PlaybackStateStore.currentIndex + offset).coerceIn(0, chunks.lastIndex)
        PlaybackStateStore.currentIndex = newIndex
        if (PlaybackStateStore.isPlaying) {
            speakCurrent()
        }
    }
}
