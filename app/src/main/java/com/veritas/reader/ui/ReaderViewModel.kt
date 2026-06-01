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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocumentRepository(application)
    private val _uiState = MutableStateFlow(ReaderUiState())
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val trackerSnapshot = repository.recordAppOpen()
            val documents = repository.loadDocuments()
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
            val annotationCount = repository.loadAnnotationCount()
            val fileBrowserRoots = VeritasFileBrowserScanner.persistedRoots(application)
            val userName = repository.loadUserName()
            val hasCompletedOnboarding = repository.hasSeenOnboardingTutorial()
            val hasImportedOrOpenedDocument = repository.hasImportedOrOpenedDocument()

            _uiState.update {
                it.copy(
                    documents = documents,
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
                    documentNotes = documentNotes,
                    annotationCount = annotationCount,
                    fileBrowserRoots = fileBrowserRoots,
                    fileBrowserAllFilesGranted = hasAllFilesAccess(),
                    userName = userName,
                    hasCompletedOnboarding = hasCompletedOnboarding,
                    hasImportedOrOpenedDocument = hasImportedOrOpenedDocument,
                    readerTrackerSnapshot = trackerSnapshot,
                    showTutorial = !hasCompletedOnboarding
                )
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
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs, queuedDocuments = queue, readerTrackerSnapshot = tracker) }
                refreshAnnotationCatalog()
                PlaybackStateStore.queueCount = queue.size
            }
        }
    }

    fun onAppForegrounded() {
        appSessionStartedAt = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            val tracker = repository.recordAppOpen(appSessionStartedAt)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(readerTrackerSnapshot = tracker) }
            }
        }
    }

    fun onAppBackgrounded() {
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

    private fun refreshAnnotationCatalog() {
        val annotations = repository.loadAllAnnotations()
        val documentNotes = repository.loadAllDocumentNotes()
        _uiState.update {
            it.copy(
                allAnnotations = annotations,
                documentNotes = documentNotes,
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

    fun persistProgress(index: Int) {
        val doc = uiState.value.activeDocument ?: return
        val docId = doc.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.updateProgress(docId, index, doc.chunks.size)
            val tracker = docs.firstOrNull { it.id == docId }
                ?.let { repository.recordDocumentProgress(it) }
                ?: repository.loadReaderTrackerSnapshot()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(documents = docs, readerTrackerSnapshot = tracker) }
            }
        }
    }

    fun syncPlaybackStateForDocument(readerDocument: ReaderDocument, startIndex: Int) {
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
        viewModelScope.launch(Dispatchers.IO) {
            val readerDocument = loadReaderDocument(metadata)
            val annotations = repository.loadAnnotations(metadata.id)
            val documentNote = repository.loadDocumentNote(metadata.id)
            val outline = repository.loadDocumentOutline(metadata, readerDocument.chunks)
            val targetIndex = startIndex ?: metadata.currentIndex
            repository.markImportedOrOpenedDocument()
            val tracker = repository.recordDocumentRead(metadata.id, metadata.title)
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
                        readerTrackerSnapshot = tracker
                    )
                }
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

    fun importDocumentFromUri(
        uri: Uri,
        pdfOptions: PdfImportOptions = PdfImportOptions(),
        textOptions: TextImportOptions = TextImportOptions(),
        sourceNameHint: String? = null
    ) {
        importJob?.cancel()
        importJob = viewModelScope.launch(Dispatchers.IO) {
            val title = getDisplayName(getApplication(), uri).ifBlank { "Imported document" }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(importInProgress = true, importSourceName = sourceNameHint?.ifBlank { null } ?: title) }
            }
            val extracted = runCatching {
                DocumentExtractor.extract(getApplication(), uri, title, pdfOptions, textOptions)
            }.getOrElse { error ->
                withContext(Dispatchers.Main) {
                    when (error) {
                        is CancellationException -> {
                            _uiState.update { it.copy(importMessage = "Import cancelled.") }
                        }
                        else -> {
                            _uiState.update { it.copy(importMessage = "Could not import this file: ${error.message ?: "unknown extraction error"}") }
                        }
                    }
                }
                null
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(importInProgress = false, importSourceName = "") }
                importJob = null
                if (extracted != null) {
                    if (extracted.text.isBlank()) {
                        _uiState.update { it.copy(importMessage = extracted.note ?: "No readable text was found in $title. It may be scanned, image-based, DRM-protected, or otherwise unavailable for text extraction.") }
                    } else {
                        _uiState.update { it.copy(showFileBrowser = false) }
                        createAndOpenDocument(
                            title = extracted.title,
                            text = extracted.text,
                            sourceLabel = extracted.sourceLabel,
                            originalUri = uri,
                            originalMimeType = getApplication<Application>().contentResolver.getType(uri).orEmpty(),
                            pageCount = extracted.pageCount,
                            partial = extracted.partial
                        )
                        extracted.note?.let { note -> _uiState.update { it.copy(importMessage = note) } }
                        
                        if (extracted.partial && extracted.sourceLabel == "PDF") {
                            viewModelScope.launch(Dispatchers.IO) {
                                val latest = repository.loadDocuments().firstOrNull { it.title == extracted.title }
                                if (latest != null) {
                                    continuePdfExtractionInBackground(latest, uri, title, pdfOptions)
                                }
                            }
                        }
                    }
                }
            }
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
        if (autoPlay && doc.id != null) {
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
            }
        }
    }

    fun beginSentenceNote(indexes: List<Int>) {
        val docId = uiState.value.activeDocument?.id ?: return
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
        val indexes = uiState.value.noteTargetIndexes
        val text = uiState.value.noteDraft
        viewModelScope.launch(Dispatchers.IO) {
            var lastUpdated: List<ReaderAnnotation> = emptyList()
            indexes.forEach { idx ->
                lastUpdated = repository.upsertAnnotation(docId, idx, AnnotationType.NOTE, text)
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
                        noteTargetIndexes = emptyList(),
                        noteDraft = ""
                    )
                }
            }
        }
    }

    fun deleteSentenceNote() {
        val docId = uiState.value.activeDocument?.id ?: return
        val indexes = uiState.value.noteTargetIndexes
        viewModelScope.launch(Dispatchers.IO) {
            var lastUpdated: List<ReaderAnnotation> = emptyList()
            indexes.forEach { idx ->
                lastUpdated = repository.removeAnnotation(docId, idx, AnnotationType.NOTE)
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
        _uiState.update { it.copy(noteTargetIndexes = emptyList(), noteDraft = "") }
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
        PlaybackStateStore.setSleepTimer(request, System.currentTimeMillis())
        val intent = Intent(getApplication(), PlaybackService::class.java)
            .setAction(PlaybackActions.ACTION_SET_SLEEP_TIMER)
            .putExtra(PlaybackActions.EXTRA_SLEEP_TIMER_DURATION_MILLIS, durationMillis)
            .putExtra(PlaybackActions.EXTRA_SLEEP_TIMER_ACTION, action.name)
        getApplication<Application>().startService(intent)
    }

    fun cancelSleepTimer() {
        PlaybackStateStore.clearSleepTimer()
        val intent = Intent(getApplication(), PlaybackService::class.java)
            .setAction(PlaybackActions.ACTION_CANCEL_SLEEP_TIMER)
        getApplication<Application>().startService(intent)
    }

    fun createReadingList(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val catalog = repository.createReadingList(title)
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
        persistProgress(PlaybackStateStore.currentIndex)
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
        refreshAll()
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
}
