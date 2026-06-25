package com.veritas.reader.ui

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.veritas.reader.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocumentRepository(application)
    private val delegateUiState = MutableStateFlow(ReaderUiState())
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.InternalCoroutinesApi::class)
    private val _uiState = object : MutableStateFlow<ReaderUiState> by delegateUiState {
        override var value: ReaderUiState
            get() = delegateUiState.value
            set(newVal) {
                delegateUiState.value = synchronizeNavStack(delegateUiState.value, newVal)
            }

        override fun tryEmit(value: ReaderUiState): Boolean {
            val synced = synchronizeNavStack(delegateUiState.value, value)
            return delegateUiState.tryEmit(synced)
        }

        override suspend fun emit(value: ReaderUiState) {
            val synced = synchronizeNavStack(delegateUiState.value, value)
            delegateUiState.emit(synced)
        }

        override fun compareAndSet(expect: ReaderUiState, update: ReaderUiState): Boolean {
            val synced = synchronizeNavStack(expect, update)
            return delegateUiState.compareAndSet(expect, synced)
        }
    }
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun updateState(block: (ReaderUiState) -> ReaderUiState) {
        _uiState.update(block)
    }

    private var importJob: Job? = null
    private var exportJob: Job? = null
    private var backupJob: Job? = null
    private var voiceJob: Job? = null
    private var scanJob: Job? = null
    private var appSessionStartedAt: Long = 0L
    private var activeDocStartedAt: Long = 0L

    init {
        viewModelScope.launch(Dispatchers.IO) {
            checkForUpdates()
        }
        viewModelScope.launch(Dispatchers.IO) {
            val trackerSnapshot = repository.recordAppOpen()
            val documents = repository.loadDocuments()
            val documentReadingTimes = repository.loadDocReadingTimes()
            val queuedDocuments = repository.loadQueueDocuments()
            val pronunciationRules = repository.loadPronunciationRules()
            val voiceSettings = repository.loadVoiceSettings()
            val narrationSettings = repository.loadNarrationSettings()
            val readerSettings = repository.loadReaderSettings()
            val askAiSettings = repository.loadAskAiSettings()
            val aiPromptTemplates = repository.loadAiPromptTemplates()
            val aiPromptHistory = repository.loadAiPromptHistory()
            val readingListCatalog = repository.loadReadingListCatalog()
            val readingHistory = repository.loadReadingHistory()
            val allAnnotations = repository.loadAllAnnotations()
            val documentNotes = repository.loadAllDocumentNotes()
            val documentTitles = repository.loadAllDocumentTitles()
            val annotationCount = repository.loadAnnotationCount()
            val fileBrowserRoots = VeritasFileBrowserScanner.persistedRoots(application)
            val generalNotes = repository.loadGeneralNotes()
            val userName = repository.loadUserName()
            val hasCompletedOnboarding = repository.hasSeenOnboardingTutorial()
            val hasImportedOrOpenedDocument = repository.hasImportedOrOpenedDocument()
            val questProgress = repository.loadQuestProgress()
            val flashcards = repository.loadAllFlashcards()
            // Repair missing covers for existing files
            documents.forEach { doc ->
                if (doc.originalFileName.isNotBlank() && CoverExtractor.coverFile(application, doc.id) == null) {
                    val originalFile = repository.originalFile(doc)
                    if (originalFile != null && originalFile.exists()) {
                        CoverExtractor.extractCoverFromFile(application, doc.id, originalFile)
                    } else if (doc.originalFileName.startsWith("content://")) {
                        runCatching {
                            val uri = Uri.parse(doc.originalFileName)
                            val mimeType = application.contentResolver.getType(uri).orEmpty().lowercase(
                                Locale.getDefault())
                            val isPdf = mimeType.contains("pdf") || doc.originalFileName.lowercase(
                                Locale.getDefault()).contains(".pdf")
                            val isEpub = mimeType.contains("epub") || doc.originalFileName.lowercase(
                                Locale.getDefault()).contains(".epub")
                            val isImage = mimeType.startsWith("image/")
                            when {
                                isPdf -> CoverExtractor.extractPdfCover(application, doc.id, uri)
                                isEpub -> CoverExtractor.extractEpubCover(application, doc.id, uri)
                                isImage -> CoverExtractor.extractImageCover(application, doc.id, uri)
                            }
                        }
                    }
                }
            }

            _uiState.update {
                it.copy(
                    documents = documents,
                    generalNotes = generalNotes,
                    queuedDocuments = queuedDocuments,
                    pronunciationRules = pronunciationRules,
                    voiceSettings = voiceSettings,
                    narrationSettings = narrationSettings,
                    readerSettings = readerSettings,
                    askAiSettings = askAiSettings,
                    aiPromptTemplates = aiPromptTemplates,
                    aiPromptHistory = aiPromptHistory,
                    readingListCatalog = readingListCatalog,
                    readingHistory = readingHistory,
                    allAnnotations = allAnnotations,
                    flashcards = flashcards,
                    documentNotes = documentNotes,
                    documentTitles = documentTitles,
                    annotationCount = annotationCount,
                    fileBrowserRoots = fileBrowserRoots,
                    fileBrowserAllFilesGranted = hasAllFilesAccess(),
                     userName = userName,
                    hasCompletedOnboarding = hasCompletedOnboarding,
                    hasImportedOrOpenedDocument = hasImportedOrOpenedDocument,
                    questTourDone = questProgress.tourDone,
                    questImportDone = questProgress.importDone,
                    questSpeedDone = questProgress.speedDone,
                    questBookmarkDone = questProgress.bookmarkDone,
                    readerTrackerSnapshot = trackerSnapshot,
                    documentReadingTimes = documentReadingTimes,
                    showTutorial = !hasCompletedOnboarding
                )
            }
        }
        viewModelScope.launch {
            androidx.compose.runtime.snapshotFlow { PlaybackStateStore.activeDocumentId }
                .collect { newDocId ->
                    val currentId = uiState.value.activeDocument?.id
                    if (newDocId != null && currentId != newDocId) {
                        val saved = repository.findDocument(newDocId)
                        if (saved != null) {
                            withContext(Dispatchers.Main) {
                                openSavedDocument(saved, PlaybackStateStore.currentIndex)
                            }
                        }
                    }
                }
        }
    }

    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestNotificationPermissionForPlayback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || uiState.value.notificationPermissionRequested) return
        val alreadyGranted = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!alreadyGranted) {
            _uiState.update { it.copy(notificationPermissionRequested = true) }
        }
    }

    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.loadDocuments()
            val queue = repository.loadQueueDocuments()
            val tracker = repository.loadReaderTrackerSnapshot()
            val generalNotes = repository.loadGeneralNotes()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs, queuedDocuments = queue, readerTrackerSnapshot = tracker, generalNotes = generalNotes) }
                refreshAnnotationCatalog()
                PlaybackStateStore.queueCount = queue.size
            }
        }
    }

    fun onAppForegrounded() {
        PlaybackStateStore.appInForeground = true
        appSessionStartedAt = System.currentTimeMillis()
        startActiveDocSessionTime()
        viewModelScope.launch(Dispatchers.IO) {
            val tracker = repository.recordAppOpen(appSessionStartedAt)
            // Pick up reading time the PlaybackService accrued while we were backgrounded.
            val readingTimes = repository.loadDocReadingTimes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(readerTrackerSnapshot = tracker, documentReadingTimes = readingTimes)
                }
            }
        }
    }

    fun onAppBackgrounded() {
        PlaybackStateStore.appInForeground = false
        recordActiveDocSessionTime()
        val startedAt = appSessionStartedAt
        if (startedAt <= 0L) return
        appSessionStartedAt = 0L
        val endedAt = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            val tracker = repository.recordUsageDuration(endedAt - startedAt, endedAt)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readerTrackerSnapshot = tracker) }
            }
        }
    }

    private fun recordActiveDocSessionTime() {
        val docId = uiState.value.activeDocument?.id
        val startedAt = activeDocStartedAt
        if (docId != null && startedAt > 0L) {
            val duration = System.currentTimeMillis() - startedAt
            activeDocStartedAt = 0L
            if (duration > 0L) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.recordDocReadingTime(docId, duration)
                    val updatedTimes = repository.loadDocReadingTimes()
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(documentReadingTimes = updatedTimes) }
                    }
                }
            }
        }
    }

    private fun startActiveDocSessionTime() {
        val docId = uiState.value.activeDocument?.id
        if (docId != null) {
            activeDocStartedAt = System.currentTimeMillis()
        }
    }

    private fun refreshAnnotationCatalog() {
        val annotations = repository.loadAllAnnotations()
        val documentNotes = repository.loadAllDocumentNotes()
        val documentTitles = repository.loadAllDocumentTitles()
        _uiState.update {
            it.copy(
                allAnnotations = annotations,
                documentNotes = documentNotes,
                documentTitles = documentTitles,
                annotationCount = annotations.size + documentNotes.size
            )
        }
    }

    fun refreshAnnotations() {
        val docId = uiState.value.activeDocument?.id
        if (docId.isNullOrBlank()) {
            _uiState.update { it.copy(annotations = emptyList()) }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val loaded = repository.loadAnnotations(docId)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(annotations = loaded) }
                }
            }
        }
    }

    fun saveReaderSettings(update: ReaderSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.saveReaderSettings(update)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readerSettings = saved) }
                VeritasThemeState.themeId = saved.themeId
                VeritasThemeState.themePackId = saved.themePackId
                PlaybackStateStore.autoPlayQueue = saved.autoPlayQueue
            }
        }
    }

    fun refreshPronunciationRules() {
        viewModelScope.launch(Dispatchers.IO) {
            val rules = repository.loadPronunciationRules()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(pronunciationRules = rules) }
            }
        }
    }

    fun addPronunciationRule() {
        val find = uiState.value.newRuleFind
        val replaceWith = uiState.value.newRuleReplaceWith
        if (find.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val rules = repository.addPronunciationRule(find, replaceWith)
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        pronunciationRules = rules,
                        newRuleFind = "",
                        newRuleReplaceWith = ""
                    )
                }
                restartCurrentSectionIfPlaying()
            }
        }
    }

    fun togglePronunciationRule(rule: PronunciationRule) {
        viewModelScope.launch(Dispatchers.IO) {
            val rules = repository.togglePronunciationRule(rule.id)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(pronunciationRules = rules) }
                restartCurrentSectionIfPlaying()
            }
        }
    }

    fun removePronunciationRule(rule: PronunciationRule) {
        viewModelScope.launch(Dispatchers.IO) {
            val rules = repository.removePronunciationRule(rule.id)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(pronunciationRules = rules) }
                restartCurrentSectionIfPlaying()
            }
        }
    }

    fun restartCurrentSectionIfPlaying() {
        val doc = uiState.value.activeDocument ?: return
        val docId = doc.id ?: return
        if (PlaybackStateStore.isPlaying) {
            requestNotificationPermissionForPlayback()
            sendPlaybackIntent(
                context = getApplication(),
                action = PlaybackActions.ACTION_PLAY,
                documentId = docId,
                startIndex = PlaybackStateStore.currentIndex
            )
        }
    }

    fun saveVoiceSettings(update: VoiceSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.saveVoiceSettings(update)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(voiceSettings = saved) }
                PlaybackStateStore.rate = saved.preferredRate
                PlaybackStateStore.pitch = saved.preferredPitch
                PlaybackStateStore.statusMessage = "Voice updated: ${saved.displayName}."
                restartCurrentSectionIfPlaying()
                completeQuestSpeed()
            }
        }
    }

    fun saveNarrationSettings(update: NarrationSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.saveNarrationSettings(update)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(narrationSettings = saved) }
                PlaybackStateStore.statusMessage = if (saved.enabled) "Narration mode enabled." else "Narration mode disabled."
                restartCurrentSectionIfPlaying()
            }
        }
    }

    fun saveAskAiSettings(update: AskAiSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.saveAskAiSettings(update)
            withContext(Dispatchers.Main) {
                _uiState.update { 
                    it.copy(
                        askAiSettings = saved,
                        importMessage = "Ask AI preference updated: ${saved.assistantLabel}."
                    )
                }
            }
        }
    }

    fun saveAiPromptTemplate(title: String, instruction: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val templates = repository.addAiPromptTemplate(title, instruction)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(aiPromptTemplates = templates) }
            }
        }
    }

    fun deleteAiPromptTemplate(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val templates = repository.deleteAiPromptTemplate(id)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(aiPromptTemplates = templates) }
            }
        }
    }

    fun recordAiPrompt(documentTitle: String, promptType: String, scope: String, prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = repository.addAiPromptHistory(documentTitle, promptType, scope, prompt)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(aiPromptHistory = history) }
            }
        }
    }

    fun clearAiPromptHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val history = repository.clearAiPromptHistory()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(aiPromptHistory = history) }
            }
        }
    }

    fun finishTutorial() {
        finishOnboarding(uiState.value.userName)
    }

    fun updateUserNameInMemory(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserName(name)
            val savedName = repository.loadUserName()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(userName = savedName) }
            }
        }
    }

    fun finishOnboarding(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markOnboardingComplete(name)
            val savedName = repository.loadUserName()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        showTutorial = false,
                        userName = savedName,
                        hasCompletedOnboarding = true
                    )
                }
            }
        }
    }

    fun resetQuestProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveQuestProgress(tour = false, import = false, speed = false, bookmark = false)
            repository.resetOnboardingState()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        questTourDone = false,
                        questImportDone = false,
                        questSpeedDone = false,
                        questBookmarkDone = false,
                        showTutorial = true,
                        hasCompletedOnboarding = false
                    )
                }
            }
        }
    }

    fun completeQuestTour() {
        if (uiState.value.questTourDone) return
        viewModelScope.launch(Dispatchers.IO) {
            val state = uiState.value
            repository.saveQuestProgress(
                tour = true,
                import = state.questImportDone,
                speed = state.questSpeedDone,
                bookmark = state.questBookmarkDone
            )
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(questTourDone = true) }
                checkOnboardingOverallCompletion()
            }
        }
    }

    fun completeQuestImport() {
        if (uiState.value.questImportDone) return
        viewModelScope.launch(Dispatchers.IO) {
            val state = uiState.value
            repository.saveQuestProgress(
                tour = state.questTourDone,
                import = true,
                speed = state.questSpeedDone,
                bookmark = state.questBookmarkDone
            )
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(questImportDone = true) }
                checkOnboardingOverallCompletion()
            }
        }
    }

    fun completeQuestSpeed() {
        if (uiState.value.questSpeedDone) return
        viewModelScope.launch(Dispatchers.IO) {
            val state = uiState.value
            repository.saveQuestProgress(
                tour = state.questTourDone,
                import = state.questImportDone,
                speed = true,
                bookmark = state.questBookmarkDone
            )
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(questSpeedDone = true) }
                checkOnboardingOverallCompletion()
            }
        }
    }

    fun completeQuestBookmark() {
        if (uiState.value.questBookmarkDone) return
        viewModelScope.launch(Dispatchers.IO) {
            val state = uiState.value
            repository.saveQuestProgress(
                tour = state.questTourDone,
                import = state.questImportDone,
                speed = state.questSpeedDone,
                bookmark = true
            )
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(questBookmarkDone = true) }
                checkOnboardingOverallCompletion()
            }
        }
    }

    private fun checkOnboardingOverallCompletion() {
        val state = uiState.value
        if (state.questTourDone && state.questImportDone && state.questSpeedDone && state.questBookmarkDone) {
            _uiState.update { it.copy(showConfetti = true) }
        }
    }

    fun finishConfettiCelebration() {
        _uiState.update { it.copy(showConfetti = false) }
        finishOnboarding(uiState.value.userName.ifBlank { "Reader" })
    }

    fun loadVoicesForEngine(enginePackage: String = uiState.value.voiceSettings.enginePackage) {
        _uiState.update { it.copy(voiceLoadInProgress = true, voiceMessage = null) }
        voiceJob?.cancel()
        voiceJob = viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { VoiceManager.loadVoices(getApplication(), enginePackage) }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(voiceLoadInProgress = false) }
                result.onSuccess { options ->
                    _uiState.update { state ->
                        state.copy(
                            ttsVoices = options,
                            voiceMessage = if (options.isEmpty()) "No selectable voices were reported by this TTS engine. Try installing voice data or choosing another engine." else null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            ttsVoices = emptyList(),
                            voiceMessage = "Could not load voices: ${error.message ?: "unknown error"}"
                        )
                    }
                }
            }
        }
    }

    fun previewVoice(voice: TtsVoiceOption) {
        val enginePackage = uiState.value.voiceSettings.enginePackage
        VoiceManager.previewVoice(getApplication(), enginePackage, voice.name, "Surely there is a future, and your hope shall not be cut off")
    }

    fun openSystemTtsSettings() {
        val intent = Intent("com.android.settings.TTS_SETTINGS").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val fallback = Intent(android.provider.Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        runCatching { getApplication<Application>().startActivity(intent) }.onFailure { getApplication<Application>().startActivity(fallback) }
    }

    fun stopServicePlayback() {
        if (PlaybackStateStore.isForegroundActive || PlaybackStateStore.isPlaying) {
            sendPlaybackIntent(getApplication(), PlaybackActions.ACTION_STOP)
        }
    }

    fun stopAndForgetPlayback(message: String = "Stopped.") {
        stopServicePlayback()
        PlaybackStateStore.reset()
        PlaybackStateStore.queueCount = uiState.value.queuedDocuments.size
        PlaybackStateStore.statusMessage = message
    }

    fun stopPlaybackIfDocumentsRemoved(documentIds: Set<String>) {
        val activeId = uiState.value.activeDocument?.id
        val serviceId = PlaybackStateStore.activeDocumentId
        if ((activeId != null && activeId in documentIds) || (serviceId != null && serviceId in documentIds)) {
            stopAndForgetPlayback("Reading removed.")
            recordActiveDocSessionTime()
            _uiState.update { it.copy(activeDocument = null, annotations = emptyList(), documentNoteDraft = "", showCanvasView = false) }
        }
    }

    fun clearContinueReading(document: SavedDocument) {
        stopPlaybackIfDocumentsRemoved(setOf(document.id))
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.clearProgress(document.id)
            val queue = repository.loadQueueDocuments()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs, queuedDocuments = queue, importMessage = "Cleared ${document.title} from Continue listening.") }
                PlaybackStateStore.queueCount = queue.size
            }
        }
    }

    fun deleteDocument(document: SavedDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.deleteDocument(document.id)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs, deleteTarget = null) }
                stopPlaybackIfDocumentsRemoved(setOf(document.id))
            }
        }
    }

    fun deleteDocuments(ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            ids.forEach { repository.deleteDocument(it) }
            val docs = repository.loadDocuments()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs) }
                stopPlaybackIfDocumentsRemoved(ids)
            }
        }
    }

    fun favoriteDocuments(ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            ids.forEach { repository.toggleFavorite(it) }
            val docs = repository.loadDocuments()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs) }
            }
        }
    }

    fun toggleFavorite(document: SavedDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.toggleFavorite(document.id)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs) }
            }
        }
    }

    fun queueDocuments(ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            ids.forEach { repository.addToQueue(it) }
            val queue = repository.loadQueueDocuments()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(queuedDocuments = queue) }
                PlaybackStateStore.queueCount = queue.size
            }
        }
    }

    fun setCollectionForDocuments(ids: Set<String>, collection: String) {
        viewModelScope.launch(Dispatchers.IO) {
            ids.forEach { repository.setCollection(it, collection) }
            val docs = repository.loadDocuments()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs) }
            }
        }
    }

    fun renameDocument(document: SavedDocument, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.renameDocument(document.id, newTitle)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs, renameTarget = null, renameDraft = "") }
            }
        }
    }

    fun setDocumentCollection(document: SavedDocument, collection: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.setCollection(document.id, collection)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs, collectionTarget = null, collectionDraft = "") }
            }
        }
    }

    fun moveQueueItem(document: SavedDocument, offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val queue = repository.moveQueueItem(document.id, offset)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(queuedDocuments = queue) }
            }
        }
    }

    fun clearQueue() {
        viewModelScope.launch(Dispatchers.IO) {
            val queue = repository.clearQueue()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(queuedDocuments = queue) }
                PlaybackStateStore.queueCount = 0
            }
        }
    }

    fun clearReadingHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val history = repository.clearReadingHistory()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingHistory = history) }
            }
        }
    }

    fun removeReadingHistoryEntry(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = repository.removeReadingHistoryEntry(documentId)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingHistory = history) }
            }
        }
    }

    fun persistProgress(index: Int) {
        val doc = uiState.value.activeDocument ?: return
        val docId = doc.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.updateProgress(docId, index, doc.chunks.size)
            val updatedDoc = docs.firstOrNull { it.id == docId }
            val tracker = updatedDoc?.let { repository.recordDocumentProgress(it) }
                ?: repository.loadReaderTrackerSnapshot()
            val history = updatedDoc?.let { repository.addReadingHistory(it, index) }
                ?: repository.loadReadingHistory()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs, readerTrackerSnapshot = tracker, readingHistory = history) }
            }
        }
    }

    fun syncPlaybackStateForDocument(readerDocument: ReaderDocument, startIndex: Int) {
        val liveSameDocument = PlaybackStateStore.isPlaying &&
            PlaybackStateStore.activeDocumentId == readerDocument.id
        if (liveSameDocument) {
            // The service is actively reading this document; its position is the source
            // of truth. Overwriting it here would yank playback to a stale index.
            return
        }
        if (PlaybackStateStore.isPlaying) {
            // A different document is being read aloud; pause it before this one takes
            // over the shared playback state, otherwise the service keeps mutating it.
            sendPlaybackIntent(getApplication(), PlaybackActions.ACTION_PAUSE)
        }
        val safeIndex = if (readerDocument.chunks.isEmpty()) 0 else startIndex.coerceIn(0, readerDocument.chunks.lastIndex)
        PlaybackStateStore.activeDocumentId = readerDocument.id
        PlaybackStateStore.documentTitle = readerDocument.title
        PlaybackStateStore.sourceLabel = readerDocument.sourceLabel
        PlaybackStateStore.currentIndex = safeIndex
        PlaybackStateStore.chunkCount = readerDocument.chunks.size
        PlaybackStateStore.statusMessage = "Ready."
        PlaybackStateStore.queueCount = uiState.value.queuedDocuments.size
    }

    private suspend fun loadReaderDocument(metadata: SavedDocument): ReaderDocument = withContext(Dispatchers.IO) {
        val rawText = repository.readText(metadata)
        buildReaderDocument(metadata, rawText)
    }

    fun openSavedDocument(metadata: SavedDocument, startIndex: Int? = null) {
        _uiState.update { it.copy(isOpeningDocument = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val latestMetadata = repository.findDocument(metadata.id) ?: metadata
            val readerDocument = loadReaderDocument(latestMetadata)
            
            if (latestMetadata.language.isNotBlank()) {
                val currentVoiceSettings = repository.loadVoiceSettings()
                // Only realign the locale for the SYSTEM DEFAULT voice. If the user has
                // explicitly chosen a voice (voiceName set), that voice carries its own
                // locale and must be preserved — previously we wiped voiceName here, which
                // reset the chosen voice to default on almost every document open.
                if (currentVoiceSettings.voiceName.isBlank() &&
                    currentVoiceSettings.localeTag != latestMetadata.language) {
                    val updated = currentVoiceSettings.copy(localeTag = latestMetadata.language)
                    repository.saveVoiceSettings(updated)
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(voiceSettings = updated) }
                    }
                }
            }

            val annotations = repository.loadAnnotations(latestMetadata.id)
            val documentNote = repository.loadDocumentNote(latestMetadata.id)
            val outline = repository.loadDocumentOutline(latestMetadata, readerDocument.chunks)
            val targetIndex = startIndex 
                ?: (if (PlaybackStateStore.activeDocumentId == latestMetadata.id) PlaybackStateStore.currentIndex else latestMetadata.currentIndex)
            repository.markImportedOrOpenedDocument()
            val tracker = repository.recordDocumentRead(latestMetadata.id, latestMetadata.title)
            val history = repository.addReadingHistory(latestMetadata, targetIndex)
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        activeDocument = readerDocument,
                        annotations = annotations,
                        documentNoteDraft = documentNote,
                        documentOutline = outline,
                        searchQuery = "",
                        searchMatches = emptyList(),
                        searchCursor = 0,
                        showCanvasView = false,
                        hasImportedOrOpenedDocument = true,
                        readerTrackerSnapshot = tracker,
                        readingHistory = history,
                        isOpeningDocument = false
                    )
                }
                startActiveDocSessionTime()
                syncPlaybackStateForDocument(readerDocument, targetIndex)
            }
        }
    }

    fun createAndOpenDocument(
        title: String,
        text: String,
        sourceLabel: String,
        originalUri: Uri? = null,
        originalMimeType: String = "",
        pageCount: Int = 0,
        partial: Boolean = false
    ) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.createDocument(
                title = title,
                text = text,
                sourceLabel = sourceLabel,
                originalUri = originalUri,
                originalDisplayName = title,
                originalMimeType = originalMimeType,
                pageCount = pageCount,
                partial = partial
            )
            // Extract cover image in background for compatible documents
            if (originalUri != null) {
                runCatching {
                    val mime = getApplication<Application>().contentResolver.getType(originalUri).orEmpty().lowercase()
                    val ext = saved.title.substringAfterLast('.', "").lowercase()
                    when {
                        mime.contains("pdf") || ext == "pdf" -> {
                            CoverExtractor.extractPdfCover(getApplication(), saved.id, originalUri)
                        }
                        mime.contains("epub") || ext == "epub" -> {
                            CoverExtractor.extractEpubCover(getApplication(), saved.id, originalUri)
                        }
                        mime.startsWith("image/") || ext in setOf("png", "jpg", "jpeg", "webp", "bmp", "gif") -> {
                            CoverExtractor.extractImageCover(getApplication(), saved.id, originalUri)
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                refreshAll()
                openSavedDocument(saved)
                _uiState.update { it.copy(draftText = "", hasImportedOrOpenedDocument = true) }
            }
        }
    }

    fun continuePdfExtractionInBackground(
        saved: SavedDocument,
        uri: Uri,
        title: String,
        pdfOptions: PdfImportOptions
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(importMessage = "Opened the ready pages. Veritas is finishing the rest of this PDF in the background.") }
            }
            val full = runCatching {
                DocumentExtractor.extract(
                    context = getApplication(),
                    uri = uri,
                    displayName = title,
                    pdfOptions = pdfOptions,
                    foregroundBudgetMillis = null
                )
            }.getOrElse { error ->
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(importMessage = "The readable pages are open. Background extraction could not finish: ${error.message ?: "unknown error"}") }
                }
                null
            }

            if (full != null && full.text.isNotBlank()) {
                repository.updateDocumentText(saved.id, full.text)?.let { updated ->
                    withContext(Dispatchers.Main) {
                        refreshAll()
                        if (uiState.value.activeDocument?.id == updated.id) {
                            val previousIndex = PlaybackStateStore.currentIndex
                            viewModelScope.launch(Dispatchers.IO) {
                                val readerDocument = loadReaderDocument(updated)
                                val annotations = repository.loadAnnotations(updated.id)
                                withContext(Dispatchers.Main) {
                                    _uiState.update { it.copy(activeDocument = readerDocument, annotations = annotations) }
                                    syncPlaybackStateForDocument(readerDocument, previousIndex)
                                }
                            }
                        }
                        _uiState.update { it.copy(importMessage = full.note ?: "Finished background extraction for ${updated.title}.") }
                    }
                }
            }
        }
    }

    fun prepareImport(uri: Uri, sourceNameHint: String? = null) {
        val app = getApplication<Application>()
        val name = getDisplayName(app, uri).ifBlank { "Imported document" }
        val mimeType = app.contentResolver.getType(uri).orEmpty().lowercase()
        val extension = name.substringAfterLast('.', "").lowercase()
        val isPdf = mimeType.contains("pdf") || extension == "pdf" || uri.path?.lowercase()?.endsWith(".pdf") == true
        
        val sizeBytes = if (uri.scheme == "file") {
            uri.path?.let { File(it).length() } ?: 0L
        } else {
            try {
                app.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                    fd.statSize
                } ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

        _uiState.update { it.copy(isOpeningDocument = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val pageCount = if (isPdf) {
                DocumentExtractor.getPdfPageCount(app, uri)
            } else 0

            withContext(Dispatchers.Main) {
                val pending = VeritasPendingImport(
                    uri = uri,
                    name = name,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    isPdf = isPdf,
                    pageCount = pageCount,
                    pdfOptions = PdfImportOptions(
                        startPage = 1,
                        endPage = if (pageCount > 0) pageCount else null
                    ),
                    textOptions = TextImportOptions(),
                    sourceNameHint = sourceNameHint
                )
                _uiState.update {
                    it.copy(
                        pendingImport = pending,
                        isOpeningDocument = false
                    )
                }
            }
        }
    }

    fun cancelPendingImport() {
        _uiState.update { it.copy(pendingImport = null) }
    }

    fun executePendingImport(title: String, pdfOptions: PdfImportOptions, textOptions: TextImportOptions) {
        val pending = uiState.value.pendingImport ?: return
        _uiState.update { it.copy(pendingImport = null) }
        importDocumentFromUri(
            uri = pending.uri,
            pdfOptions = pdfOptions,
            textOptions = textOptions,
            sourceNameHint = pending.sourceNameHint,
            customTitle = title
        )
    }

    fun importDocumentFromUri(
        uri: Uri,
        pdfOptions: PdfImportOptions = PdfImportOptions(),
        textOptions: TextImportOptions = TextImportOptions(),
        sourceNameHint: String? = null,
        customTitle: String? = null,
        queueAfterImport: Boolean = false,
        openAfterImport: Boolean = true
    ) {
        val app = getApplication<Application>()
        val title = customTitle?.ifBlank { null } ?: getDisplayName(app, uri).ifBlank { "Imported document" }
        _uiState.update {
            if (openAfterImport) {
                it.copy(showFileBrowser = false, importMessage = "Importing $title in background...", isOpeningDocument = true)
            } else {
                it.copy(importMessage = "Importing $title in background...")
            }
        }
        
        // Ensure we have permission
        if (uri.scheme == "content") {
            try {
                app.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                android.util.Log.w("ReaderViewModel", "Could not take persistable permission for $uri", e)
            }
        }
        
        val inputData = androidx.work.workDataOf(
            "uri" to uri.toString(),
            "title" to title,
            "pdf_startPage" to (pdfOptions.startPage ?: -1),
            "pdf_endPage" to (pdfOptions.endPage ?: -1),
            "pdf_cleanupRepeatedLines" to pdfOptions.cleanupRepeatedLines,
            "pdf_removePageNumbers" to pdfOptions.removePageNumbers,
            "pdf_repairHyphenation" to pdfOptions.repairHyphenation,
            "pdf_includePageMarkers" to pdfOptions.includePageMarkers,
            "pdf_forceOcr" to pdfOptions.forceOcr,
            "pdf_preferOcrWhenLowText" to pdfOptions.preferOcrWhenLowText,
            "pdf_extractionMode" to pdfOptions.extractionMode,
            "pdf_removeTopPageNoise" to pdfOptions.removeTopPageNoise,
            "pdf_removeBottomPageNoise" to pdfOptions.removeBottomPageNoise,
            "pdf_manualCropBeforeExtract" to pdfOptions.manualCropBeforeExtract,
            "pdf_minWordGap" to pdfOptions.minWordGap,
            "pdf_separateWordsOnFontChange" to pdfOptions.separateWordsOnFontChange,
            "pdf_markPdfLinesForCanvas" to pdfOptions.markPdfLinesForCanvas,
            "pdf_forceFreshExtraction" to pdfOptions.forceFreshExtraction,
            "pdf_cropLeft" to (pdfOptions.cropRect?.left ?: -1f),
            "pdf_cropTop" to (pdfOptions.cropRect?.top ?: -1f),
            "pdf_cropRight" to (pdfOptions.cropRect?.right ?: -1f),
            "pdf_cropBottom" to (pdfOptions.cropRect?.bottom ?: -1f),
            "text_encodingId" to textOptions.encodingId
        )
        
        val request = androidx.work.OneTimeWorkRequestBuilder<DocumentImportWorker>()
            .setInputData(inputData)
            .build()
            
        val workManager = androidx.work.WorkManager.getInstance(app)
        workManager.enqueue(request)

        var firstChunkOpened = false
        viewModelScope.launch(Dispatchers.Main) {
            try {
                workManager.getWorkInfoByIdFlow(request.id).collect { workInfo ->
                    if (workInfo != null) {
                        // Auto-open the document as soon as the first chunk is ready
                        if (!firstChunkOpened && workInfo.state == androidx.work.WorkInfo.State.RUNNING) {
                            val firstChunkId = workInfo.progress.getString("firstChunkDocumentId")
                            if (firstChunkId != null) {
                                firstChunkOpened = true
                                refreshAll()
                                if (openAfterImport) {
                                    val saved = repository.findDocument(firstChunkId)
                                    if (saved != null) {
                                        openSavedDocument(saved)
                                    }
                                }
                                _uiState.update { it.copy(importMessage = "Importing remaining pages of $title...") }
                            }
                        }
                        when (workInfo.state) {
                            androidx.work.WorkInfo.State.SUCCEEDED -> {
                                val docId = workInfo.outputData.getString("documentId")
                                if (docId != null) {
                                    refreshAll()
                                    completeQuestImport()
                                    if (queueAfterImport) {
                                        viewModelScope.launch(Dispatchers.IO) {
                                            val queuedDocs = repository.addToQueue(docId)
                                            withContext(Dispatchers.Main) {
                                                _uiState.update { it.copy(queuedDocuments = queuedDocs) }
                                                PlaybackStateStore.queueCount = queuedDocs.size
                                            }
                                        }
                                    }
                                    if (openAfterImport) {
                                        if (!firstChunkOpened) {
                                            // Non-PDF path or small PDF that completed in one chunk
                                            val saved = repository.findDocument(docId)
                                            if (saved != null) {
                                                openSavedDocument(saved)
                                            } else {
                                                _uiState.update { it.copy(isOpeningDocument = false) }
                                            }
                                        }
                                    } else {
                                        _uiState.update { it.copy(isOpeningDocument = false) }
                                    }
                                }
                                _uiState.update { it.copy(importMessage = "Successfully imported $title.") }
                            }
                            androidx.work.WorkInfo.State.FAILED -> {
                                val error = workInfo.outputData.getString("error") ?: "Unknown error"
                                _uiState.update { it.copy(importMessage = "Import failed: $error", isOpeningDocument = false) }
                            }
                            androidx.work.WorkInfo.State.CANCELLED -> {
                                _uiState.update { it.copy(importMessage = "Import cancelled", isOpeningDocument = false) }
                            }
                            else -> {
                                // Still enqueued or running
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ReaderViewModel", "Error observing work info", e)
            }
        }
    }

    fun importMultipleDocuments(uris: List<Uri>, queue: Boolean) {
        if (uris.isEmpty()) return
        val count = uris.size
        _uiState.update { it.copy(importMessage = "Importing $count files in background...") }
        uris.forEach { uri ->
            importDocumentFromUri(
                uri = uri,
                queueAfterImport = queue,
                openAfterImport = false
            )
        }
    }

    fun importWebArticle(url: String) {
        importJob?.cancel()
        importJob = viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(importInProgress = true, importSourceName = "web article") }
            }
            val article = runCatching {
                WebArticleExtractor.extract(url)
            }.getOrElse { error ->
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(importMessage = "Could not import this web article: ${error.message ?: "unknown error"}") }
                }
                null
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(importInProgress = false, importSourceName = "") }
                importJob = null
                if (article != null) {
                    createAndOpenDocument(
                        title = article.title,
                        text = "${article.text}\n\nSource: ${article.url}",
                        sourceLabel = "Web"
                    )
                    _uiState.update { it.copy(importMessage = "Web article imported into Veritas.") }
                }
            }
        }
    }

    fun moveTo(
        index: Int,
        autoPlay: Boolean = PlaybackStateStore.isPlaying,
        forcePlaybackStart: Boolean = false
    ) {
        val doc = uiState.value.activeDocument ?: return
        if (doc.chunks.isEmpty()) return
        val safeIndex = index.coerceIn(0, doc.chunks.lastIndex)
        PlaybackStateStore.currentIndex = safeIndex
        persistProgress(safeIndex)
        val matches = uiState.value.searchMatches
        val matchIndex = matches.indexOf(safeIndex)
        if (matchIndex >= 0) {
            _uiState.update { it.copy(searchCursor = matchIndex) }
        }
        // While the service is actively speaking, every index move must be sent to it.
        // Otherwise the service finishes its current sentence and then advances from the
        // mutated shared index, speaking the wrong sentence.
        val notifyService = autoPlay || PlaybackStateStore.isPlaying
        if (notifyService && doc.id != null) {
            if (forcePlaybackStart) requestNotificationPermissionForPlayback()
            sendPlaybackIntent(
                context = getApplication(),
                action = if (forcePlaybackStart) PlaybackActions.ACTION_PLAY else PlaybackActions.ACTION_JUMP_TO,
                documentId = doc.id,
                startIndex = safeIndex
            )
        }
    }

    fun toggleBookmark(index: Int = PlaybackStateStore.currentIndex) {
        val docId = uiState.value.activeDocument?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = repository.toggleAnnotation(docId, index, AnnotationType.BOOKMARK)
            val allAnnotations = repository.loadAllAnnotations()
            val documentNotes = repository.loadAllDocumentNotes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        annotations = updated,
                        allAnnotations = allAnnotations,
                        documentNotes = documentNotes,
                        annotationCount = allAnnotations.size + documentNotes.size
                    )
                }
                completeQuestBookmark()
            }
        }
    }

    fun beginSentenceNote(indexes: List<Int>) {
        uiState.value.activeDocument?.id ?: return
        if (indexes.isEmpty()) return
        val existing = uiState.value.annotations.firstOrNull { it.chunkIndex == indexes.first() && it.type == AnnotationType.NOTE }
        _uiState.update { 
            it.copy(
                noteTargetIndexes = indexes,
                noteDraft = existing?.note ?: ""
            )
        }
    }

    fun saveSentenceNote() {
        val docId = uiState.value.activeDocument?.id ?: return
        val indexes = uiState.value.noteTargetIndexes.ifEmpty {
            uiState.value.noteTargetIndex?.let(::listOf).orEmpty()
        }
        val text = uiState.value.noteDraft
        viewModelScope.launch(Dispatchers.IO) {
            val annots = repository.loadAllAnnotations()
            val existingGroup = indexes.mapNotNull { idx ->
                annots.firstOrNull { it.documentId == docId && it.chunkIndex == idx && it.type == AnnotationType.NOTE }?.selectionGroupId
            }.firstOrNull { !it.isNullOrBlank() }
            val groupId = existingGroup ?: if (indexes.size >= 2) "note-group-${java.util.UUID.randomUUID()}" else null
            
            var lastUpdated: List<ReaderAnnotation> = emptyList()
            indexes.forEach { idx ->
                lastUpdated = repository.upsertAnnotation(docId, idx, AnnotationType.NOTE, text, selectionGroupId = groupId)
            }
            val allAnnotations = repository.loadAllAnnotations()
            val documentNotes = repository.loadAllDocumentNotes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        annotations = lastUpdated,
                        allAnnotations = allAnnotations,
                        documentNotes = documentNotes,
                        annotationCount = allAnnotations.size + documentNotes.size,
                        noteTargetIndex = null,
                        noteTargetIndexes = emptyList(),
                        noteDraft = ""
                    )
                }
            }
        }
    }

    fun deleteSentenceNote() {
        val docId = uiState.value.activeDocument?.id ?: return
        val indexes = uiState.value.noteTargetIndexes.ifEmpty {
            uiState.value.noteTargetIndex?.let(::listOf).orEmpty()
        }
        viewModelScope.launch(Dispatchers.IO) {
            val annots = repository.loadAllAnnotations()
            val toRemove = mutableSetOf<String>()
            indexes.forEach { idx ->
                val target = annots.firstOrNull { it.documentId == docId && it.chunkIndex == idx && it.type == AnnotationType.NOTE }
                if (target != null) {
                    if (!target.selectionGroupId.isNullOrBlank()) {
                        val groupKeys = annots.filter { it.documentId == docId && it.selectionGroupId == target.selectionGroupId }.map { it.stableKey }
                        toRemove.addAll(groupKeys)
                    } else {
                        toRemove.add(target.stableKey)
                    }
                }
            }
            if (toRemove.isNotEmpty()) {
                repository.removeAnnotations(toRemove)
            }
            val lastUpdated = repository.loadAnnotations(docId)
            val allAnnotations = repository.loadAllAnnotations()
            val documentNotes = repository.loadAllDocumentNotes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        annotations = lastUpdated,
                        allAnnotations = allAnnotations,
                        documentNotes = documentNotes,
                        annotationCount = allAnnotations.size + documentNotes.size,
                        noteTargetIndex = null,
                        noteTargetIndexes = emptyList(),
                        noteDraft = ""
                    )
                }
            }
        }
    }

    fun deleteAnnotations(stableKeys: Set<String>) {
        if (stableKeys.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val documentNoteIds = stableKeys.mapNotNull(::documentIdFromDocumentNoteStableKey).toSet()
            val annotationKeys = stableKeys - documentNoteIds.map(::documentNoteStableKey).toSet()
            val allAnnotations = repository.removeAnnotations(annotationKeys)
            val documentNotes = repository.deleteDocumentNotes(documentNoteIds)
            val activeDocId = uiState.value.activeDocument?.id
            val currentAnnotations = activeDocId?.let { repository.loadAnnotations(it) }.orEmpty()
            val activeDocumentNote = activeDocId?.let { repository.loadDocumentNote(it) }.orEmpty()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        annotations = currentAnnotations,
                        allAnnotations = allAnnotations,
                        documentNotes = documentNotes,
                        annotationCount = allAnnotations.size + documentNotes.size,
                        documentNoteDraft = activeDocumentNote
                    )
                }
            }
        }
    }

    fun dismissSentenceNote() {
        _uiState.update {
            it.copy(
                noteTargetIndex = null,
                noteTargetIndexes = emptyList(),
                noteDraft = ""
            )
        }
    }

    fun openDocumentNotes() {
        val docId = uiState.value.activeDocument?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val note = repository.loadDocumentNote(docId)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(showDocumentNotes = true, documentNoteDraft = note) }
            }
        }
    }

    fun saveDocumentNoteDraft() {
        val docId = uiState.value.activeDocument?.id ?: return
        val draft = uiState.value.documentNoteDraft
        viewModelScope.launch(Dispatchers.IO) {
            val savedNote = repository.saveDocumentNote(docId, draft)
            val allAnnotations = repository.loadAllAnnotations()
            val documentNotes = repository.loadAllDocumentNotes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        showDocumentNotes = false,
                        documentNoteDraft = savedNote,
                        allAnnotations = allAnnotations,
                        documentNotes = documentNotes,
                        annotationCount = allAnnotations.size + documentNotes.size
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        val doc = uiState.value.activeDocument
        val cleanQuery = query.take(120)
        val matches = buildSearchMatches(doc, cleanQuery)
        val currentIndex = PlaybackStateStore.currentIndex
        val cursor = if (matches.isEmpty()) {
            0
        } else {
            matches.indexOfFirst { it >= currentIndex }.takeIf { it >= 0 } ?: 0
        }
        _uiState.update {
            it.copy(
                searchQuery = cleanQuery,
                searchMatches = matches,
                searchCursor = cursor
            )
        }
    }

    fun moveToNextSearchMatch() {
        moveToSearchMatch(offset = 1)
    }

    fun moveToPreviousSearchMatch() {
        moveToSearchMatch(offset = -1)
    }

    private fun moveToSearchMatch(offset: Int) {
        val state = uiState.value
        val doc = state.activeDocument ?: return
        val matches = state.searchMatches.ifEmpty { buildSearchMatches(doc, state.searchQuery) }
        if (matches.isEmpty()) {
            _uiState.update { it.copy(searchMatches = emptyList(), searchCursor = 0) }
            return
        }
        val currentIndex = PlaybackStateStore.currentIndex
        val currentMatchCursor = matches.indexOf(currentIndex)
        val nextCursor = when {
            currentMatchCursor >= 0 -> Math.floorMod(currentMatchCursor + offset, matches.size)
            offset > 0 -> matches.indexOfFirst { it > currentIndex }.takeIf { it >= 0 } ?: 0
            else -> matches.indexOfLast { it < currentIndex }.takeIf { it >= 0 } ?: matches.lastIndex
        }
        _uiState.update {
            it.copy(
                searchMatches = matches,
                searchCursor = nextCursor
            )
        }
        moveTo(matches[nextCursor], autoPlay = false)
    }

    private fun buildSearchMatches(document: ReaderDocument?, query: String): List<Int> {
        val needle = query.trim()
        if (document == null || needle.isBlank()) return emptyList()
        return document.chunks.mapIndexedNotNull { index, chunk ->
            index.takeIf { chunk.contains(needle, ignoreCase = true) }
        }
    }

    fun openCurrentPartTextEditor() {
        val doc = uiState.value.activeDocument ?: return
        val model = ReaderTextModelCache.get(doc.id, doc.rawText, doc.pageCount)
        val part = model.partForSentence(PlaybackStateStore.currentIndex) ?: model.parts.firstOrNull() ?: return
        _uiState.update {
            it.copy(
                showTextEditor = true,
                editorText = part.text,
                editorTarget = VeritasTextEditTarget.Part(
                    partIndex = part.index,
                    label = "part ${part.index + 1}"
                )
            )
        }
    }

    fun openSelectionTextEditor(indexes: List<Int>) {
        val doc = uiState.value.activeDocument ?: return
        val sorted = indexes
            .filter { it in doc.chunks.indices }
            .distinct()
            .sorted()
        if (sorted.isEmpty()) return
        val start = sorted.first()
        val endExclusive = sorted.last() + 1
        val label = if (endExclusive - start == 1) {
            "sentence ${start + 1}"
        } else {
            "sentences ${start + 1}-$endExclusive"
        }
        _uiState.update {
            it.copy(
                showTextEditor = true,
                editorText = doc.chunks.subList(start, endExclusive).joinToString("\n\n"),
                editorTarget = VeritasTextEditTarget.SentenceRange(
                    startSentenceIndex = start,
                    endSentenceIndexExclusive = endExclusive,
                    label = label
                )
            )
        }
    }

    fun saveTextEditorChanges() {
        val doc = uiState.value.activeDocument ?: return
        val target = uiState.value.editorTarget ?: return
        val docId = doc.id ?: return
        val replacement = uiState.value.editorText.trim()
        if (replacement.isBlank()) return

        stopServicePlayback()
        viewModelScope.launch(Dispatchers.IO) {
            val updatedText = when (target) {
                is VeritasTextEditTarget.Part -> ReaderTextIndex.replacePart(
                    rawText = doc.rawText,
                    storedPageCount = doc.pageCount,
                    partIndex = target.partIndex,
                    replacement = replacement
                )
                is VeritasTextEditTarget.SentenceRange -> ReaderTextIndex.replaceSentenceRange(
                    rawText = doc.rawText,
                    storedPageCount = doc.pageCount,
                    startSentenceIndex = target.startSentenceIndex,
                    endSentenceIndexExclusive = target.endSentenceIndexExclusive,
                    replacement = replacement
                )
            }
            val updated = repository.updateDocumentText(docId, updatedText) ?: return@launch
            val updatedDocument = loadReaderDocument(updated)
            val annotations = repository.loadAnnotations(docId)
            val documents = repository.loadDocuments()
            val annotationCount = repository.loadAnnotationCount()
            val outline = repository.loadDocumentOutline(updated, updatedDocument.chunks)
            val startIndex = when (target) {
                is VeritasTextEditTarget.Part -> {
                    ReaderTextModelCache.get(updatedDocument.id, updatedDocument.rawText, updatedDocument.pageCount)
                        .parts
                        .getOrNull(target.partIndex)
                        ?.sentenceStartIndex
                        ?: PlaybackStateStore.currentIndex
                }
                is VeritasTextEditTarget.SentenceRange -> target.startSentenceIndex
            }.coerceIn(0, (updatedDocument.chunks.size - 1).coerceAtLeast(0))
            withContext(Dispatchers.Main) {
                syncPlaybackStateForDocument(updatedDocument, startIndex)
                _uiState.update {
                    it.copy(
                        documents = documents,
                        activeDocument = updatedDocument,
                        annotations = annotations,
                        annotationCount = annotationCount,
                        documentOutline = outline,
                        showTextEditor = false,
                        editorText = "",
                        editorTarget = null,
                        searchMatches = buildSearchMatches(updatedDocument, it.searchQuery),
                        searchCursor = 0,
                        importMessage = "Extracted text updated."
                    )
                }
            }
        }
    }

    fun dismissTextEditor() {
        _uiState.update { it.copy(showTextEditor = false, editorText = "", editorTarget = null) }
    }

    fun openTtsDataInstaller() {
        val intent = Intent(android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        runCatching { getApplication<Application>().startActivity(intent) }
    }

    fun setSleepTimer(request: VeritasSleepTimerRequest) {
        val durationMillis = request.durationMillis
        val action = request.action
        val stopAtEndOfSection = request.stopAtEndOfSection
        PlaybackStateStore.setSleepTimer(request, System.currentTimeMillis())
        val intent = Intent(getApplication(), PlaybackService::class.java)
            .setAction(PlaybackActions.ACTION_SET_SLEEP_TIMER)
            .putExtra(PlaybackActions.EXTRA_SLEEP_TIMER_DURATION_MILLIS, durationMillis)
            .putExtra(PlaybackActions.EXTRA_SLEEP_TIMER_ACTION, action.name)
            .putExtra(PlaybackActions.EXTRA_SLEEP_TIMER_STOP_AT_END_OF_SECTION, stopAtEndOfSection)
        getApplication<Application>().startService(intent)
    }

    fun cancelSleepTimer() {
        PlaybackStateStore.clearSleepTimer()
        val intent = Intent(getApplication(), PlaybackService::class.java)
            .setAction(PlaybackActions.ACTION_CANCEL_SLEEP_TIMER)
        getApplication<Application>().startService(intent)
    }

    fun createReadingList(title: String, documentId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            var catalog = repository.createReadingList(title)
            if (documentId != null) {
                val newList = catalog.activeLists.firstOrNull { it.title == title }
                    ?: catalog.lists.maxByOrNull { it.createdAt }
                if (newList != null) {
                    catalog = repository.addDocumentToReadingList(newList.id, documentId)
                }
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingListCatalog = catalog) }
            }
        }
    }

    fun addDocumentToReadingList(listId: String, documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val catalog = repository.addDocumentToReadingList(listId, documentId)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingListCatalog = catalog) }
            }
        }
    }

    fun removeDocumentFromReadingList(listId: String, documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val catalog = repository.removeDocumentFromReadingList(listId, documentId)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingListCatalog = catalog) }
            }
        }
    }

    fun moveReadingListDocument(listId: String, documentId: String, offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val catalog = repository.moveReadingListDocument(listId, documentId, offset)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingListCatalog = catalog) }
            }
        }
    }

    fun setReadingListSortMode(listId: String, sortMode: VeritasReadingListSortMode) {
        viewModelScope.launch(Dispatchers.IO) {
            val catalog = repository.setReadingListSortMode(listId, sortMode)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingListCatalog = catalog) }
            }
        }
    }

    fun archiveReadingList(listId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val catalog = repository.archiveReadingList(listId)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingListCatalog = catalog) }
            }
        }
    }

    fun deleteReadingList(listId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val catalog = repository.deleteReadingList(listId)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readingListCatalog = catalog) }
            }
        }
    }

    fun playOrPause() {
        val doc = uiState.value.activeDocument ?: return
        val docId = doc.id ?: return
        if (PlaybackStateStore.isPlaying) {
            sendPlaybackIntent(getApplication(), PlaybackActions.ACTION_PAUSE)
        } else {
            requestNotificationPermissionForPlayback()
            sendPlaybackIntent(
                context = getApplication(),
                action = PlaybackActions.ACTION_PLAY,
                documentId = docId,
                startIndex = PlaybackStateStore.currentIndex
            )
        }
    }

    fun playOrPauseSavedDocument(metadata: SavedDocument) {
        if (PlaybackStateStore.activeDocumentId == metadata.id) {
            if (PlaybackStateStore.isPlaying) {
                sendPlaybackIntent(getApplication(), PlaybackActions.ACTION_PAUSE)
            } else {
                requestNotificationPermissionForPlayback()
                sendPlaybackIntent(
                    context = getApplication(),
                    action = PlaybackActions.ACTION_PLAY,
                    documentId = metadata.id,
                    startIndex = PlaybackStateStore.currentIndex
                )
            }
        } else {
            requestNotificationPermissionForPlayback()
            sendPlaybackIntent(
                context = getApplication(),
                action = PlaybackActions.ACTION_PLAY,
                documentId = metadata.id,
                startIndex = metadata.currentIndex
            )
        }
    }

    fun playQueue() {
        viewModelScope.launch(Dispatchers.IO) {
            val first = repository.loadQueueDocuments().firstOrNull() ?: return@launch
            val readerDocument = loadReaderDocument(first)
            val annotations = repository.loadAnnotations(first.id)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(activeDocument = readerDocument, annotations = annotations) }
                syncPlaybackStateForDocument(readerDocument, first.currentIndex)
                requestNotificationPermissionForPlayback()
                sendPlaybackIntent(
                    context = getApplication(),
                    action = PlaybackActions.ACTION_PLAY,
                    documentId = first.id,
                    startIndex = first.currentIndex
                )
            }
        }
    }

    fun openNextQueuedAfterCurrent(autoPlay: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val completedId = uiState.value.activeDocument?.id
            val next = repository.completeCurrentAndGetNextQueued(completedId) ?: return@launch
            val readerDocument = loadReaderDocument(next)
            val annotations = repository.loadAnnotations(next.id)
            withContext(Dispatchers.Main) {
                refreshAll()
                _uiState.update { it.copy(activeDocument = readerDocument, annotations = annotations) }
                syncPlaybackStateForDocument(readerDocument, next.currentIndex)
                if (autoPlay) {
                    requestNotificationPermissionForPlayback()
                    sendPlaybackIntent(
                        context = getApplication(),
                        action = PlaybackActions.ACTION_PLAY,
                        documentId = next.id,
                        startIndex = next.currentIndex
                    )
                }
            }
        }
    }

    fun goToNextSectionOrQueuedDocument() {
        val doc = uiState.value.activeDocument ?: return
        val atLastSection = doc.chunks.isNotEmpty() && PlaybackStateStore.currentIndex >= doc.chunks.lastIndex
        if (!atLastSection) {
            moveTo(PlaybackStateStore.currentIndex + 1, autoPlay = PlaybackStateStore.isPlaying)
            return
        }

        if (uiState.value.readerSettings.autoPlayQueue && uiState.value.queuedDocuments.isNotEmpty()) {
            if (PlaybackStateStore.isPlaying) {
                sendPlaybackIntent(getApplication(), PlaybackActions.ACTION_NEXT)
            } else {
                openNextQueuedAfterCurrent(autoPlay = false)
            }
        }
    }

    fun toggleQueue(document: SavedDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            val queuedDocs = if (repository.isQueued(document.id)) {
                repository.removeFromQueue(document.id)
            } else {
                repository.addToQueue(document.id)
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(queuedDocuments = queuedDocs) }
                PlaybackStateStore.queueCount = queuedDocs.size
            }
        }
    }

    fun exportActiveDocumentToAudio() {
        val doc = uiState.value.activeDocument ?: return
        if (doc.chunks.isEmpty()) {
            _uiState.update { it.copy(exportMessage = "This document has no readable text to export.") }
            return
        }
        stopServicePlayback()
        exportJob?.cancel()
        _uiState.update { it.copy(exportInProgress = true, exportedAudioFile = null, exportMessage = null) }
        exportJob = viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                AudioExportManager(getApplication()).exportToWav(
                    title = doc.title,
                    chunks = doc.chunks,
                    rate = PlaybackStateStore.rate,
                    pitch = PlaybackStateStore.pitch,
                    transformText = { repository.applyPronunciationRules(it) }
                )
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(exportInProgress = false) }
                exportJob = null
                result.onSuccess { exported ->
                    _uiState.update { 
                        it.copy(
                            exportedAudioFile = exported.file,
                            exportMessage = "Audio export complete: ${exported.displayName} (${exported.synthesizedParts} part${if (exported.synthesizedParts == 1) "" else "s"})."
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            exportMessage = if (error is CancellationException) "Audio export cancelled." else "Audio export failed: ${error.message ?: "unknown error"}"
                        )
                    }
                }
            }
        }
    }

    fun startRecordSoundFile() {
        val doc = uiState.value.activeDocument ?: run {
            _uiState.update { it.copy(exportMessage = "Open a reading before recording a sound file.") }
            return
        }
        if (doc.chunks.isEmpty()) {
            _uiState.update { it.copy(exportMessage = "This document has no readable text to record.") }
            return
        }
        stopServicePlayback()
        exportJob?.cancel()
        val startedAt = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                recordMode = true,
                recordAwaitingDecision = false,
                recordStartedAt = startedAt,
                recordElapsedSeconds = 0L,
                exportInProgress = true,
                exportedAudioFile = null,
                exportMessage = "Recording sound file from ${doc.title}..."
            )
        }
        exportJob = viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                AudioExportManager(getApplication()).exportToWav(
                    title = doc.title,
                    chunks = doc.chunks,
                    rate = PlaybackStateStore.rate,
                    pitch = PlaybackStateStore.pitch,
                    transformText = { repository.applyPronunciationRules(it) }
                )
            }
            val elapsedSeconds = ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0L)
            withContext(Dispatchers.Main) {
                exportJob = null
                result.onSuccess { exported ->
                    _uiState.update {
                        it.copy(
                            exportInProgress = false,
                            exportedAudioFile = exported.file,
                            recordAwaitingDecision = true,
                            recordElapsedSeconds = elapsedSeconds,
                            exportMessage = "Sound file ready: ${exported.displayName}"
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            exportInProgress = false,
                            exportedAudioFile = null,
                            recordAwaitingDecision = true,
                            recordElapsedSeconds = elapsedSeconds,
                            exportMessage = if (error is CancellationException) {
                                "Sound file recording stopped before audio was created."
                            } else {
                                "Sound file recording failed: ${error.message ?: "unknown error"}"
                            }
                        )
                    }
                }
            }
        }
    }

    fun stopRecordSoundFile() {
        exportJob?.cancel()
        val startedAt = uiState.value.recordStartedAt
        val elapsedSeconds = if (startedAt > 0L) {
            ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0L)
        } else {
            uiState.value.recordElapsedSeconds
        }
        _uiState.update {
            it.copy(
                exportInProgress = false,
                recordAwaitingDecision = true,
                recordElapsedSeconds = elapsedSeconds,
                exportMessage = if (it.exportedAudioFile == null) "Stopping sound file recording..." else it.exportMessage
            )
        }
    }

    fun saveRecordedSoundFile() {
        val file = uiState.value.exportedAudioFile
        _uiState.update {
            it.copy(
                recordMode = false,
                recordAwaitingDecision = false,
                exportInProgress = false,
                exportMessage = file?.let { saved -> "Sound file saved: ${saved.name}" } ?: "No sound file was created."
            )
        }
    }

    fun discardRecordedSoundFile() {
        exportJob?.cancel()
        uiState.value.exportedAudioFile?.let { file ->
            runCatching { file.delete() }
        }
        _uiState.update {
            it.copy(
                recordMode = false,
                recordAwaitingDecision = false,
                recordStartedAt = 0L,
                recordElapsedSeconds = 0L,
                exportInProgress = false,
                exportedAudioFile = null,
                exportMessage = "Sound file discarded."
            )
        }
    }

    fun shareExportedAudio(file: File) {
        val uri = FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/wav"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(Intent.createChooser(shareIntent, "Share exported audio").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
    }

    fun shareActiveDocumentNotes() {
        val doc = uiState.value.activeDocument ?: return
        val notesText = buildDocumentNotesExport(doc, uiState.value.annotations, uiState.value.documentNoteDraft)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Notes — ${doc.title}")
            putExtra(Intent.EXTRA_TEXT, notesText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(Intent.createChooser(shareIntent, "Export notes to notes app").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
    }

    fun exportLibraryBackup(uri: Uri) {
        backupJob?.cancel()
        _uiState.update { it.copy(backupInProgress = true, backupMessage = null) }
        backupJob = viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val json = repository.buildBackupJson()
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(json.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("Could not open the selected backup location.")
            }
            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    state.copy(
                        backupInProgress = false,
                        backupMessage = result.fold(
                            onSuccess = { "Veritas backup/sync file exported successfully." },
                            onFailure = { if (it is CancellationException) "Backup export cancelled." else "Backup export failed: ${it.message ?: "unknown error"}" }
                        )
                    )
                }
                backupJob = null
            }
        }
    }

    fun shareLibrarySyncPack() {
        backupJob?.cancel()
        _uiState.update { it.copy(backupInProgress = true, backupMessage = null) }
        backupJob = viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val json = repository.buildBackupJson()
                val syncDir = File(getApplication<Application>().cacheDir, "veritas_sync").apply { mkdirs() }
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(syncDir, "veritas_sync_pack_$timestamp.json")
                file.writeText(json, Charsets.UTF_8)
                file
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(backupInProgress = false) }
                backupJob = null
                result.onSuccess { file ->
                    val uri = FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", file)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, "Veritas Reader sync pack")
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "Veritas Reader sync pack. Import this file on another device to merge safely.")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    getApplication<Application>().startActivity(Intent.createChooser(shareIntent, "Share sync pack").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    _uiState.update { it.copy(backupMessage = "Sync pack ready. Choose Google Drive, WhatsApp, Files, Nearby Share, or any compatible app.") }
                }.onFailure { error ->
                    _uiState.update { it.copy(backupMessage = if (error is CancellationException) "Sync share cancelled." else "Could not create sync pack: ${error.message ?: "unknown error"}") }
                }
            }
        }
    }

    fun importLibraryBackup(uri: Uri) {
        backupJob?.cancel()
        _uiState.update { it.copy(backupInProgress = true, backupMessage = null) }
        backupJob = viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val raw = getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: throw IllegalStateException("Could not open the selected backup file.")
                repository.restoreBackupJson(raw, replaceExisting = false)
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(backupInProgress = false) }
                backupJob = null
                result.onSuccess { restored ->
                    refreshAll()
                    viewModelScope.launch(Dispatchers.IO) {
                        val documents = repository.loadDocuments()
                        val queuedDocuments = repository.loadQueueDocuments()
                        val pronunciationRules = repository.loadPronunciationRules()
                        val readerSettings = repository.loadReaderSettings()
                        val voiceSettings = repository.loadVoiceSettings()
                        val narrationSettings = repository.loadNarrationSettings()
                        val askAiSettings = repository.loadAskAiSettings()
                        val aiPromptTemplates = repository.loadAiPromptTemplates()
                        val aiPromptHistory = repository.loadAiPromptHistory()
                        val readingListCatalog = repository.loadReadingListCatalog()
                        val readingHistory = repository.loadReadingHistory()
                        val allAnnotations = repository.loadAllAnnotations()
                        val documentNotes = repository.loadAllDocumentNotes()
                        val annotationCount = repository.loadAnnotationCount()
                        val activeDocId = uiState.value.activeDocument?.id
                        val annotations = if (activeDocId != null) repository.loadAnnotations(activeDocId) else emptyList()
                        withContext(Dispatchers.Main) {
                            _uiState.update {
                                it.copy(
                                    documents = documents,
                                    queuedDocuments = queuedDocuments,
                                    pronunciationRules = pronunciationRules,
                                    readerSettings = readerSettings,
                                    voiceSettings = voiceSettings,
                                    narrationSettings = narrationSettings,
                                    askAiSettings = askAiSettings,
                                    aiPromptTemplates = aiPromptTemplates,
                                    aiPromptHistory = aiPromptHistory,
                                    readingListCatalog = readingListCatalog,
                                    readingHistory = readingHistory,
                                    allAnnotations = allAnnotations,
                                    documentNotes = documentNotes,
                                    annotationCount = annotationCount,
                                    annotations = annotations,
                                    backupMessage = "Sync pack merged: ${restored.documentCount} reading${if (restored.documentCount == 1) "" else "s"}, ${restored.annotationCount} bookmark/note${if (restored.annotationCount == 1) "" else "s"}, ${restored.queueCount} queued item${if (restored.queueCount == 1) "" else "s"}, ${restored.readingListCount} list${if (restored.readingListCount == 1) "" else "s"}."
                                )
                            }
                            PlaybackStateStore.queueCount = queuedDocuments.size
                        }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(backupMessage = if (error is CancellationException) "Backup import cancelled." else "Backup import failed: ${error.message ?: "unknown error"}") }
                }
            }
        }
    }

    fun returnToLibrary() {
        val doc = uiState.value.activeDocument
        val docId = doc?.id
        val currentIndex = PlaybackStateStore.currentIndex
        PlaybackStateStore.readerMode = ReaderMode.TEXT
        recordActiveDocSessionTime()
        _uiState.update {
            it.copy(
                activeDocument = null,
                annotations = emptyList(),
                documentOutline = emptyList(),
                documentNoteDraft = "",
                searchQuery = "",
                searchCursor = 0
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (doc != null && docId != null) {
                repository.updateProgress(docId, currentIndex, doc.chunks.size)
            }
            val docs = repository.loadDocuments()
            val queue = repository.loadQueueDocuments()
            val tracker = repository.loadReaderTrackerSnapshot()
            val generalNotes = repository.loadGeneralNotes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        documents = docs,
                        queuedDocuments = queue,
                        readerTrackerSnapshot = tracker,
                        generalNotes = generalNotes
                    )
                }
                refreshAnnotationCatalog()
                PlaybackStateStore.queueCount = queue.size
            }
        }
    }

    fun approveFileBrowserFolder(uri: Uri?) {
        if (uri == null) return
        val context = getApplication<Application>()
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
        _uiState.update {
            it.copy(
                fileBrowserRoots = VeritasFileBrowserScanner.persistedRoots(context),
                fileBrowserLocation = null,
                fileBrowserBackStack = emptyList()
            )
        }
        refreshFileBrowser()
    }

    fun refreshFileBrowserAccessState() {
        val context = getApplication<Application>()
        _uiState.update {
            it.copy(
                fileBrowserRoots = VeritasFileBrowserScanner.persistedRoots(context),
                fileBrowserAllFilesGranted = hasAllFilesAccess()
            )
        }
    }

    fun clearFileBrowserAccess() {
        val context = getApplication<Application>()
        context.contentResolver.persistedUriPermissions.forEach { permission ->
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    permission.uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        _uiState.update {
            it.copy(
                fileBrowserRoots = emptyList(),
                fileBrowserFiles = emptyList(),
                fileBrowserLocation = null,
                fileBrowserBackStack = emptyList()
            )
        }
    }

    fun openFileBrowser() {
        _uiState.update { it.copy(showFileBrowser = true) }
        refreshFileBrowser()
    }

    fun enterFileBrowserDirectory(entry: VeritasBrowserFile) {
        val next = entry.targetLocation ?: return
        val current = uiState.value.fileBrowserLocation
        _uiState.update {
            it.copy(
                fileBrowserLocation = next,
                fileBrowserBackStack = if (current == null) it.fileBrowserBackStack else it.fileBrowserBackStack + current
            )
        }
        refreshFileBrowser()
    }

    fun goUpFileBrowserDirectory() {
        val stack = uiState.value.fileBrowserBackStack
        val previous = stack.lastOrNull() ?: return
        _uiState.update {
            it.copy(
                fileBrowserLocation = previous,
                fileBrowserBackStack = stack.dropLast(1)
            )
        }
        refreshFileBrowser()
    }

    fun refreshFileBrowser() {
        scanJob?.cancel()
        refreshFileBrowserAccessState()
        _uiState.update { it.copy(fileBrowserScanning = true, fileBrowserMessage = null) }
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                VeritasFileBrowserScanner.scan(
                    context = getApplication(),
                    roots = uiState.value.fileBrowserRoots,
                    includeAllFilesAccess = hasAllFilesAccess(),
                    location = uiState.value.fileBrowserLocation
                )
            }
            val scanResult = result.getOrElse { error ->
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(fileBrowserScanning = false, fileBrowserMessage = "Scan failed: ${error.message}") }
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(
                    fileBrowserScanning = false,
                    fileBrowserFiles = scanResult.files,
                    fileBrowserLocation = scanResult.location,
                    fileBrowserAllFilesGranted = hasAllFilesAccess(),
                    fileBrowserMessage = scanResult.diagnostics.joinToString("\n").ifBlank {
                        if (scanResult.files.isEmpty()) "No files found" else null
                    }
                )}
            }
        }
    }

    fun saveGeneralNote(
        title: String,
        content: String,
        color: String? = null,
        pinned: Boolean = false,
        isChecklist: Boolean = false,
        imageUrl: String? = null,
        audioUrl: String? = null,
        reminderAt: Long? = null,
        closeEditor: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.loadGeneralNotes().toMutableList()
            val target = uiState.value.generalNoteEditorTarget
            val savedNote: GeneralNote
            if (target == null) {
                savedNote = GeneralNote(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    updatedAt = System.currentTimeMillis(),
                    color = color,
                    pinned = pinned,
                    isChecklist = isChecklist,
                    imageUrl = imageUrl,
                    audioUrl = audioUrl,
                    reminderAt = reminderAt
                )
                existing.add(0, savedNote)
            } else {
                val index = existing.indexOfFirst { it.id == target.id }
                savedNote = target.copy(
                    title = title,
                    content = content,
                    updatedAt = System.currentTimeMillis(),
                    color = color,
                    pinned = pinned,
                    isChecklist = isChecklist,
                    imageUrl = imageUrl,
                    audioUrl = audioUrl,
                    reminderAt = reminderAt
                )
                if (index != -1) existing[index] = savedNote
            }
            repository.saveGeneralNotes(existing)
            // (Re)schedule or clear the note's reminder alarm.
            val app = getApplication<Application>()
            val reminderBody = title.ifBlank { content.take(80) }.ifBlank { "Note reminder" }
            if (reminderAt != null && reminderAt > System.currentTimeMillis()) {
                NoteReminderScheduler.ensureChannel(app)
                NoteReminderScheduler.schedule(app, savedNote.id, title.ifBlank { "Veritas note" }, reminderBody, reminderAt)
            } else {
                NoteReminderScheduler.cancel(app, savedNote.id)
            }
            val updated = repository.loadGeneralNotes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    if (closeEditor) {
                        it.copy(
                            generalNotes = updated,
                            showGeneralNotesEditor = false,
                            generalNoteEditorTarget = null
                        )
                    } else {
                        it.copy(
                            generalNotes = updated,
                            generalNoteEditorTarget = savedNote
                        )
                    }
                }
            }
        }
    }

    fun duplicateGeneralNote(note: GeneralNote) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.loadGeneralNotes().toMutableList()
            val newNote = note.copy(
                id = java.util.UUID.randomUUID().toString(),
                title = if (note.title.endsWith(" (Copy)")) note.title else note.title + " (Copy)",
                updatedAt = System.currentTimeMillis()
            )
            existing.add(0, newNote)
            repository.saveGeneralNotes(existing)
            val updated = repository.loadGeneralNotes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        generalNotes = updated
                    )
                }
            }
        }
    }

    fun clearNoteEditorFlags() {
        _uiState.update {
            it.copy(
                noteEditorChecklistOnStart = false,
                noteEditorReminderOnStart = false,
                noteEditorImageOnStart = false
            )
        }
    }

    fun toggleGeneralNotePin(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.loadGeneralNotes().toMutableList()
            val index = existing.indexOfFirst { it.id == noteId }
            if (index != -1) {
                val target = existing[index]
                existing[index] = target.copy(pinned = !target.pinned, updatedAt = System.currentTimeMillis())
                repository.saveGeneralNotes(existing)
                val updated = repository.loadGeneralNotes()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(generalNotes = updated) }
                }
            }
        }
    }

    fun changeGeneralNoteColor(noteId: String, colorHex: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.loadGeneralNotes().toMutableList()
            val index = existing.indexOfFirst { it.id == noteId }
            if (index != -1) {
                val target = existing[index]
                existing[index] = target.copy(color = colorHex, updatedAt = System.currentTimeMillis())
                repository.saveGeneralNotes(existing)
                val updated = repository.loadGeneralNotes()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(generalNotes = updated) }
                }
            }
        }
    }

    fun deleteGeneralNote(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.loadGeneralNotes().filterNot { it.id == noteId }
            repository.saveGeneralNotes(existing)
            NoteReminderScheduler.cancel(getApplication(), noteId)
            val updated = repository.loadGeneralNotes()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                         generalNotes = updated,
                         showGeneralNotesEditor = false,
                         generalNoteEditorTarget = null
                    )
                }
            }
        }
    }

    private data class DictionaryDefinition(
        val definition: String,
        val pronunciation: String? = null
    )

    private fun fetchDictionaryDefinition(word: String): DictionaryDefinition? {
        val cleanWord = word.trim().lowercase().replace(Regex("[^a-z\\-]"), "")
        if (cleanWord.isBlank() || cleanWord.length > 30) return null
        return try {
            val url = java.net.URL("https://api.dictionaryapi.dev/api/v2/entries/en/$cleanWord")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            if (conn.responseCode == 200) {
                val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                val array = org.json.JSONArray(jsonText)
                if (array.length() > 0) {
                    val entry = array.getJSONObject(0)
                    val phonetic = entry.optString("phonetic").takeIf { it.isNotBlank() }
                    val meanings = entry.optJSONArray("meanings")
                    if (meanings != null && meanings.length() > 0) {
                        val firstMeaning = meanings.getJSONObject(0)
                        val pos = firstMeaning.optString("partOfSpeech", "")
                        val definitions = firstMeaning.optJSONArray("definitions")
                        if (definitions != null && definitions.length() > 0) {
                            val defObj = definitions.getJSONObject(0)
                            val defText = defObj.optString("definition", "")
                            if (defText.isNotBlank()) {
                                val fullDef = if (pos.isNotBlank()) "($pos) $defText" else defText
                                return DictionaryDefinition(fullDef, phonetic)
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            android.util.Log.w("ReaderViewModel", "Failed to fetch meaning for $cleanWord", e)
            null
        }
    }

    fun appendVocabularyWord(word: String, explanation: String) {
        val activeDoc = uiState.value.activeDocument ?: return
        val docId = activeDoc.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val isPlaceholder = explanation.contains("Looked up") || explanation.contains("Asked AI")
            val fetched = if (isPlaceholder) fetchDictionaryDefinition(word) else null
            val finalExplanation = fetched?.definition ?: explanation
            val pronunciation = fetched?.pronunciation

            val existing = repository.loadGeneralNotes().toMutableList()
            val targetTitle = "__vocab__$docId"
            val vocabIndex = existing.indexOfFirst { it.title == targetTitle }

            val now = System.currentTimeMillis()
            val formattedTime = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(now))
            val currentIndex = PlaybackStateStore.currentIndex
            val textModel = ReaderTextIndex.build(activeDoc.rawText, activeDoc.pageCount)
            val part = textModel.partForSentence(currentIndex)
            val sectionNum = (part?.index ?: 0) + 1
            val contextSentence = activeDoc.sentences.getOrNull(currentIndex)?.trim()

            val entryText = buildString {
                appendLine(word.trim())
                appendLine("  $finalExplanation")
                appendLine("  (looked up: Section $sectionNum, sentence ${currentIndex + 1})")
                appendLine("  [$formattedTime]")
                if (!contextSentence.isNullOrBlank()) {
                    appendLine("  context: \"$contextSentence\"")
                }
                if (!pronunciation.isNullOrBlank()) {
                    appendLine("  pronunciation: $pronunciation")
                }
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
            val updated = repository.loadGeneralNotes()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(generalNotes = updated) }
            }
        }
    }

    fun removeVocabularyWord(documentId: String, wordToRemove: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.loadGeneralNotes().toMutableList()
            val targetTitle = "__vocab__$documentId"
            val index = existing.indexOfFirst { it.title == targetTitle }
            if (index != -1) {
                val note = existing[index]
                val parsed = parseVocabularyNoteContent(note.content)
                val filtered = parsed.filterNot { it.word.equals(wordToRemove, ignoreCase = true) }
                val newContent = filtered.joinToString("\n\n") { entry ->
                    buildString {
                        appendLine(entry.word)
                        appendLine("  ${entry.explanation}")
                        appendLine("  ${entry.source}")
                    }
                }.trim()
                if (newContent.isBlank()) {
                    existing.removeAt(index)
                } else {
                    existing[index] = note.copy(content = newContent, updatedAt = System.currentTimeMillis())
                }
                repository.saveGeneralNotes(existing)
                val updated = repository.loadGeneralNotes()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(generalNotes = updated) }
                }
            }
        }
    }

    fun importFlashcards(documentId: String, cards: List<Flashcard>) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.loadAllFlashcards().toMutableList()
            cards.forEach { card ->
                val id = java.util.UUID.randomUUID().toString()
                existing.add(
                    FlashcardProgress(
                        id = id,
                        documentId = documentId,
                        front = card.front,
                        back = card.back,
                        nextReviewTime = System.currentTimeMillis()
                    )
                )
            }
            repository.saveAllFlashcards(existing)
            val updated = repository.loadAllFlashcards()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(flashcards = updated) }
            }
        }
    }

    fun gradeFlashcard(cardId: String, score: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.loadAllFlashcards().toMutableList()
            val index = existing.indexOfFirst { it.id == cardId }
            if (index != -1) {
                val card = existing[index]
                val q = when (score) {
                    1 -> 1
                    2 -> 2
                    3 -> 4
                    4 -> 5
                    else -> 3
                }
                val newRepetitions: Int
                val newIntervalDays: Int
                var newEaseFactor = card.easeFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
                if (newEaseFactor < 1.3f) newEaseFactor = 1.3f
                if (q < 3) {
                    newRepetitions = 0
                    newIntervalDays = 1
                } else {
                    newRepetitions = card.repetitions + 1
                    newIntervalDays = when (newRepetitions) {
                        1 -> 1
                        2 -> 6
                        else -> (card.intervalDays * newEaseFactor).toInt().coerceAtLeast(1)
                    }
                }
                val nextReview = System.currentTimeMillis() + (newIntervalDays * 24L * 60L * 60L * 1000L)
                val updatedCard = card.copy(
                    repetitions = newRepetitions,
                    intervalDays = newIntervalDays,
                    nextReviewTime = nextReview,
                    easeFactor = newEaseFactor
                )
                existing[index] = updatedCard
                repository.saveAllFlashcards(existing)
                val updated = repository.loadAllFlashcards()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(flashcards = updated) }
                }
            }
        }
    }

    fun deleteFlashcard(cardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val remaining = repository.loadAllFlashcards().filterNot { it.id == cardId }
            repository.saveAllFlashcards(remaining)
            val updated = repository.loadAllFlashcards()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(flashcards = updated) }
            }
        }
    }

    private fun synchronizeNavStack(old: ReaderUiState, next: ReaderUiState): ReaderUiState {
        var newStack = next.navStack
        val mappings = listOf(
            VeritasScreen.TEXT_EDITOR to { state: ReaderUiState -> state.showTextEditor },
            VeritasScreen.FILE_BROWSER to { state: ReaderUiState -> state.showFileBrowser },
            VeritasScreen.PDF_IMPORT_TOOLS to { state: ReaderUiState -> state.showPdfImportTools },
            VeritasScreen.READER_SETTINGS to { state: ReaderUiState -> state.showReaderSettings },
            VeritasScreen.PRONUNCIATION_RULES to { state: ReaderUiState -> state.showPronunciationRules },
            VeritasScreen.VOICE_STUDIO to { state: ReaderUiState -> state.showVoiceStudio },
            VeritasScreen.NARRATION_STUDIO to { state: ReaderUiState -> state.showNarrationStudio },
            VeritasScreen.AI_STUDY_TOOLS to { state: ReaderUiState -> state.showAiStudyTools },
            VeritasScreen.OFFLINE_STUDY_TOOLS to { state: ReaderUiState -> state.showOfflineStudyTools },
            VeritasScreen.AI_CENTER to { state: ReaderUiState -> state.showAiCenter },
            VeritasScreen.ASK_AI_SETTINGS to { state: ReaderUiState -> state.showAskAiSettings },
            VeritasScreen.TRANSLATION_TOOLS to { state: ReaderUiState -> state.showTranslationTools },
            VeritasScreen.SLEEP_TIMER to { state: ReaderUiState -> state.showSleepTimerDialog },
            VeritasScreen.READING_LISTS to { state: ReaderUiState -> state.showReadingLists },
            VeritasScreen.READING_HISTORY to { state: ReaderUiState -> state.showReadingHistory },
            VeritasScreen.DOCUMENT_NOTES to { state: ReaderUiState -> state.showDocumentNotes },
            VeritasScreen.SETTINGS_HUB to { state: ReaderUiState -> state.showSettingsHub },
            VeritasScreen.BACKUP_TOOLS to { state: ReaderUiState -> state.showBackupTools },
            VeritasScreen.SYNC_CENTER to { state: ReaderUiState -> state.showSyncCenter },
            VeritasScreen.APP_HEALTH to { state: ReaderUiState -> state.showAppHealth },
            VeritasScreen.TUTORIAL to { state: ReaderUiState -> state.showTutorial },
            VeritasScreen.CANVAS_VIEW to { state: ReaderUiState -> state.showCanvasView },
            VeritasScreen.GENERAL_NOTES_EDITOR to { state: ReaderUiState -> state.showGeneralNotesEditor }
        )
        for ((screen, getter) in mappings) {
            val wasVisible = getter(old)
            val isVisible = getter(next)
            if (wasVisible != isVisible) {
                if (isVisible) {
                    if (!newStack.contains(screen)) {
                        newStack = newStack + screen
                    }
                } else {
                    newStack = newStack.filter { it != screen }
                }
            }
        }
        return next.copy(navStack = newStack)
    }

    fun navigateBack() {
        val stack = uiState.value.navStack
        if (stack.isEmpty()) return
        when (val top = stack.last()) {
            VeritasScreen.TEXT_EDITOR -> dismissTextEditor()
            VeritasScreen.TUTORIAL -> finishTutorial()
            else -> {
                updateState {
                    when (top) {
                        VeritasScreen.FILE_BROWSER -> it.copy(showFileBrowser = false)
                        VeritasScreen.PDF_IMPORT_TOOLS -> it.copy(showPdfImportTools = false)
                        VeritasScreen.READER_SETTINGS -> it.copy(showReaderSettings = false)
                        VeritasScreen.PRONUNCIATION_RULES -> it.copy(showPronunciationRules = false)
                        VeritasScreen.VOICE_STUDIO -> it.copy(showVoiceStudio = false)
                        VeritasScreen.NARRATION_STUDIO -> it.copy(showNarrationStudio = false)
                        VeritasScreen.AI_STUDY_TOOLS -> it.copy(showAiStudyTools = false)
                        VeritasScreen.OFFLINE_STUDY_TOOLS -> it.copy(showOfflineStudyTools = false)
                        VeritasScreen.AI_CENTER -> it.copy(showAiCenter = false)
                        VeritasScreen.ASK_AI_SETTINGS -> it.copy(showAskAiSettings = false)
                        VeritasScreen.TRANSLATION_TOOLS -> it.copy(showTranslationTools = false)
                        VeritasScreen.SLEEP_TIMER -> it.copy(showSleepTimerDialog = false)
                        VeritasScreen.READING_LISTS -> it.copy(showReadingLists = false)
                        VeritasScreen.READING_HISTORY -> it.copy(showReadingHistory = false)
                        VeritasScreen.DOCUMENT_NOTES -> it.copy(showDocumentNotes = false)
                        VeritasScreen.SETTINGS_HUB -> it.copy(showSettingsHub = false)
                        VeritasScreen.BACKUP_TOOLS -> it.copy(showBackupTools = false)
                        VeritasScreen.SYNC_CENTER -> it.copy(showSyncCenter = false)
                        VeritasScreen.APP_HEALTH -> it.copy(showAppHealth = false)
                        VeritasScreen.CANVAS_VIEW -> it.copy(showCanvasView = false)
                        VeritasScreen.GENERAL_NOTES_EDITOR -> it.copy(showGeneralNotesEditor = false)
                    }
                }
            }
        }
    }

    fun createWelcomeDocumentSilently() {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.loadDocuments().isEmpty()) {
                repository.createDocument(
                    title = "Veritas Welcome Guide",
                    text = "Welcome to Veritas Reader! This is a sample document designed to help you explore the reading environment. Veritas lets you convert research papers, textbooks, EPUBs, docx files, web articles, and images into high-quality spoken audio. Long-press any sentence in this guide to try highlighting, bookmarking, adding study notes, or asking the AI Assistant a question. Adjust the voice speed or select premium voices in the expandable player panel below. Toggle different layout modes like TEXT for clean reading or LISTEN to follow along sentence-by-sentence. Enjoy your reading journey!",
                    sourceLabel = "System"
                )
                withContext(Dispatchers.Main) {
                    refreshAll()
                }
            }
        }
    }

    private fun checkForUpdates() {
        try {
            val url = java.net.URL("https://api.github.com/repos/fhes-tus/Veritas-Reader/releases/latest")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(responseText)
                val tagName = json.optString("tag_name", "").trim()
                val cleanTagName = tagName.removePrefix("v").trim()
                val localVersion = runCatching {
                    val context = getApplication<Application>()
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                }.getOrNull() ?: "1.0.1"
                if (isVersionNewer(localVersion, cleanTagName)) {
                    val htmlUrl = json.optString("html_url", "https://github.com/fhes-tus/Veritas-Reader/releases")
                    val body = json.optString("body", "")
                    _uiState.update {
                        it.copy(
                            showUpdateDialog = true,
                            updateVersionName = tagName,
                            updateUrl = htmlUrl,
                            updateChangelog = body
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ReaderViewModel", "Error checking for updates", e)
        }
    }

    companion object {
        fun isVersionNewer(local: String, remote: String): Boolean {
            val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }
            val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
            val length = kotlin.math.max(localParts.size, remoteParts.size)
            for (i in 0 until length) {
                val l = localParts.getOrElse(i) { 0 }
                val r = remoteParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (l > r) return false
            }
            return false
        }
    }
}
