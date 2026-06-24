package com.veritas.reader

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.text.style.TextOverflow
import com.veritas.reader.ui.ReaderViewModel
import com.veritas.reader.ui.VeritasTheme
import com.veritas.reader.ui.screens.*
import com.veritas.reader.ui.OnboardingController
import com.veritas.reader.ui.OnboardingStep
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private fun readableImportMimeTypes(): Array<String> = arrayOf(
    "text/plain",
    "text/*",
    "text/html",
    "application/pdf",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/epub+zip",
    "application/octet-stream",
    "image/*"
)

private fun veritasBackupFileName(prefix: String): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "${prefix}_$timestamp.json"
}

private fun veritasBackupMimeTypes(): Array<String> = arrayOf(
    "application/json",
    "text/json",
    "text/plain",
    "application/octet-stream"
)

private fun textEditorDownloadName(
    document: ReaderDocument,
    target: VeritasTextEditTarget
): String {
    val scope = when (target) {
        is VeritasTextEditTarget.Part -> target.label
        is VeritasTextEditTarget.SentenceRange -> target.label
    }
    val safeTitle = document.title
        .replace(Regex("[^A-Za-z0-9._ -]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(48)
        .ifBlank { "veritas_text" }
    val safeScope = scope
        .replace(Regex("[^A-Za-z0-9._ -]"), " ")
        .replace(Regex("\\s+"), "_")
        .trim('_')
        .ifBlank { "edited" }
    return "${safeTitle}_$safeScope.txt"
}

fun main() {
    System.setProperty("kotlinx.coroutines.fast.service.loader", "false")
    application {
        Window(
            onCloseRequest = ::exitApplication,
        title = "Veritas Reader PC"
    ) {
        val application = remember { Application() }
        val viewModel = remember { ReaderViewModel(application) }
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val documentRepository = remember(application) { DocumentRepository(application) }

        val folderPickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            viewModel.approveFileBrowserFolder(uri)
        }
        val importFileLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                viewModel.prepareImport(uri)
            }
        }
        val textDownloadLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
            val pending = viewModel.uiState.value.pendingTextDownload
            if (uri == null || pending == null) {
                viewModel.updateState { it.copy(pendingTextDownload = null) }
            } else {
                val result = runCatching {
                    application.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(pending.second.toByteArray(Charsets.UTF_8))
                    } ?: throw IllegalStateException("Could not open the selected save location.")
                }
                viewModel.updateState {
                    it.copy(
                        pendingTextDownload = null,
                        exportMessage = result.fold(
                            onSuccess = { "Edited text saved." },
                            onFailure = { error -> "Could not save edited text: ${error.message ?: "unknown error"}" }
                        )
                    )
                }
            }
        }
        val backupExportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri == null) {
                viewModel.updateState { it.copy(backupMessage = "Backup export cancelled.") }
            } else {
                viewModel.exportLibraryBackup(uri)
            }
        }
        val backupImportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) {
                viewModel.updateState { it.copy(backupMessage = "Backup import cancelled.") }
            } else {
                viewModel.importLibraryBackup(uri)
            }
        }

        LaunchedEffect(uiState.recordMode, uiState.recordAwaitingDecision, uiState.recordStartedAt) {
            while (uiState.recordMode && !uiState.recordAwaitingDecision && uiState.recordStartedAt > 0L) {
                val elapsed =
                    ((System.currentTimeMillis() - uiState.recordStartedAt) / 1000L).coerceAtLeast(0L)
                viewModel.updateState { it.copy(recordElapsedSeconds = elapsed) }
                delay(1_000L)
            }
        }

        // Initialize welcome docs and onboarding check
        LaunchedEffect(Unit) {
            viewModel.refreshAll()
            if (uiState.showTutorial && !uiState.hasCompletedOnboarding && OnboardingController.activeStep == null) {
                viewModel.createWelcomeDocumentSilently()
                OnboardingController.activeStep = OnboardingStep.WELCOME
            }
        }

        VeritasTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (uiState.activeDocument == null) {
                    LibraryScreen(
                        uiState = uiState,
                        onDraftTextChange = { text -> viewModel.updateState { it.copy(draftText = text) } },
                        onCreateFromDraft = {
                            if (WebArticleExtractor.looksLikeUrl(uiState.draftText)) viewModel.importWebArticle(
                                uiState.draftText
                            )
                            else viewModel.createAndOpenDocument(
                                "Pasted text",
                                uiState.draftText,
                                "Pasted"
                            )
                        },
                        onImportWebArticle = { viewModel.importWebArticle(it) },
                        onImportFile = { importFileLauncher.launch(readableImportMimeTypes()) },
                        onImportImage = { importFileLauncher.launch(arrayOf("image/*")) },
                        onAdvancedPdfImport = { viewModel.updateState { it.copy(showPdfImportTools = true) } },
                        onOpenFileBrowser = { viewModel.openFileBrowser() },
                        onOpenReadingLists = { viewModel.updateState { it.copy(showReadingLists = true) } },
                        onCreateReadingList = { title, docId -> viewModel.createReadingList(title, docId) },
                        onAddDocumentToReadingList = viewModel::addDocumentToReadingList,
                        onRemoveDocumentFromReadingList = viewModel::removeDocumentFromReadingList,
                        onOpenReadingHistory = { viewModel.updateState { it.copy(showReadingHistory = true) } },
                        onOpenDocument = { viewModel.openSavedDocument(it) },
                        onOpenDocumentAt = { document, sentenceIndex ->
                            viewModel.openSavedDocument(
                                document,
                                sentenceIndex
                            )
                        },
                        onClearContinueDocument = { viewModel.clearContinueReading(it) },
                        onPlayPauseContinue = { viewModel.playOrPauseSavedDocument(it) },
                        onDeleteDocument = { doc -> viewModel.updateState { it.copy(deleteTarget = doc) } },
                        onToggleQueue = { viewModel.toggleQueue(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onRenameDocument = { doc ->
                            viewModel.updateState {
                                it.copy(
                                    renameTarget = doc,
                                    renameDraft = doc.title
                                )
                            }
                        },
                        onSetCollection = { doc ->
                            viewModel.updateState {
                                it.copy(
                                    collectionTarget = doc,
                                    collectionDraft = doc.collection
                                )
                            }
                        },
                        onShowDetails = { doc -> viewModel.updateState { it.copy(detailsTarget = doc) } },
                        isQueued = { doc -> uiState.queuedDocuments.any { it.id == doc.id } },
                        onPlayQueue = { viewModel.playQueue() },
                        onMoveQueueUp = { viewModel.moveQueueItem(it, -1) },
                        onMoveQueueDown = { viewModel.moveQueueItem(it, 1) },
                        onRemoveFromQueue = { viewModel.toggleQueue(it) },
                        onClearQueue = { viewModel.clearQueue() },
                        onOpenSyncCenter = { viewModel.updateState { it.copy(showSyncCenter = true) } },
                        onOpenSettingsHub = { viewModel.updateState { it.copy(showSettingsHub = true) } },
                        onRefreshMainPage = { viewModel.refreshAll() },
                        onBatchDeleteDocuments = { viewModel.deleteDocuments(it) },
                        onBatchFavoriteDocuments = { viewModel.favoriteDocuments(it) },
                        onBatchQueueDocuments = { viewModel.queueDocuments(it) },
                        onBatchSetCollectionDocuments = { ids, collection ->
                            viewModel.setCollectionForDocuments(
                                ids,
                                collection
                            )
                        },
                        onDeleteAnnotations = { keys -> viewModel.deleteAnnotations(keys) },
                        onWriteGeneralNote = { viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = null) } },
                        onEditGeneralNote = { note -> viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = note) } },
                        onRemoveVocabularyWord = { docId, word -> viewModel.removeVocabularyWord(docId, word) },
                        onClearReadingHistory = { viewModel.clearReadingHistory() },
                        onRemoveReadingHistoryEntry = viewModel::removeReadingHistoryEntry,
                        onToggleGeneralNotePin = viewModel::toggleGeneralNotePin,
                        onChangeGeneralNoteColor = viewModel::changeGeneralNoteColor,
                        onDeleteGeneralNote = viewModel::deleteGeneralNote
                    )
                    if (uiState.showTutorial) {
                        OnboardingQuestChecklist(
                            questTourDone = uiState.questTourDone,
                            questImportDone = uiState.questImportDone,
                            questSpeedDone = uiState.questSpeedDone,
                            questBookmarkDone = uiState.questBookmarkDone,
                            onStartTour = {
                                viewModel.createWelcomeDocumentSilently()
                                OnboardingController.activeStep = OnboardingStep.WELCOME
                            },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth(0.4f)
                                .padding(16.dp)
                        )
                    }
                } else {
                    val activeDocument = uiState.activeDocument!!
                    val activeMetadata = activeDocument.id?.let { activeId ->
                        uiState.documents.firstOrNull { it.id == activeId }
                    }
                    ReaderScreen(
                        state = ReaderScreenState(
                            document = activeDocument,
                            currentIndex = PlaybackStateStore.currentIndex,
                            isPlaying = PlaybackStateStore.isPlaying,
                            isBackgroundActive = PlaybackStateStore.isForegroundActive,
                            rate = PlaybackStateStore.rate,
                            pitch = PlaybackStateStore.pitch,
                            statusMessage = PlaybackStateStore.statusMessage,
                            queueCount = PlaybackStateStore.queueCount,
                            isQueued = uiState.queuedDocuments.any { it.id == activeDocument.id },
                            annotations = uiState.annotations,
                            pronunciationRuleCount = uiState.pronunciationRules.size,
                            readerSettings = uiState.readerSettings,
                            voiceSettings = uiState.voiceSettings,
                            narrationSettings = uiState.narrationSettings,
                            askAiSettings = uiState.askAiSettings,
                            searchQuery = uiState.searchQuery,
                            searchMatches = uiState.searchMatches,
                            searchCursor = uiState.searchCursor,
                            outlineEntries = uiState.documentOutline,
                            hasCanvas = activeMetadata?.originalFileName?.isNotBlank() == true || activeDocument.chunks.any {
                                it.trim().startsWith("[CANVAS")
                            },
                            sleepTimerDurationMillis = PlaybackStateStore.sleepTimerDurationMillis,
                            sleepTimerEndsAtMillis = PlaybackStateStore.sleepTimerEndsAtMillis,
                            sleepTimerAction = PlaybackStateStore.sleepTimerAction,
                            readingListCount = uiState.readingListCatalog.activeLists.size,
                            activeDocumentReadingListCount = uiState.activeDocument?.id?.let { activeId ->
                                uiState.readingListCatalog.listsContaining(activeId)
                                    .count { !it.archived }
                            } ?: 0,
                            voices = uiState.ttsVoices,
                            readerMode = PlaybackStateStore.readerMode
                        ),
                        listState = rememberLazyListState(),
                        hasCanvas = activeMetadata?.originalFileName?.isNotBlank() == true || activeDocument.chunks.any {
                            it.trim().startsWith("[CANVAS")
                        },
                        onBackToLibrary = { viewModel.returnToLibrary() },
                        onSentenceClick = { viewModel.moveTo(it, false) },
                        onSentenceDoubleTap = {
                            viewModel.moveTo(
                                it,
                                autoPlay = true,
                                forcePlaybackStart = true
                            )
                        },
                        onPlayPause = { viewModel.playOrPause() },
                        onStop = { viewModel.stopAndForgetPlayback() },
                        onPrevious = {
                            viewModel.moveTo(
                                (PlaybackStateStore.currentIndex - 1).coerceAtLeast(0),
                                false
                            )
                        },
                        onNext = {
                            viewModel.moveTo(
                                (PlaybackStateStore.currentIndex + 1).coerceAtMost(activeDocument.chunks.size - 1),
                                false
                            )
                        },
                        onRateChange = {
                            viewModel.saveVoiceSettings(uiState.voiceSettings.copy(preferredRate = it))
                        },
                        onPitchChange = {
                            viewModel.saveVoiceSettings(uiState.voiceSettings.copy(preferredPitch = it))
                        },
                        onFontSizeChange = {
                            viewModel.saveReaderSettings(uiState.readerSettings.copy(fontSizeSp = it))
                        },
                        onToggleQueue = {
                            uiState.documents.firstOrNull { it.id == uiState.activeDocument?.id }
                                ?.let(viewModel::toggleQueue)
                        },
                        onToggleBookmark = viewModel::toggleBookmark,
                        onEditNote = { idx -> viewModel.beginSentenceNote(listOf(idx)) },
                        onEditNotes = { idxs -> viewModel.beginSentenceNote(idxs) },
                        onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                        onNextSearchMatch = { viewModel.moveToNextSearchMatch() },
                        onPreviousSearchMatch = { viewModel.moveToPreviousSearchMatch() },
                        onOpenReaderSettings = { viewModel.updateState { it.copy(showReaderSettings = true) } },
                        onOpenPronunciationRules = { viewModel.updateState { it.copy(showPronunciationRules = true) } },
                        onOpenVoiceStudio = { viewModel.updateState { it.copy(showVoiceStudio = true) } },
                        onOpenNarrationStudio = { viewModel.updateState { it.copy(showNarrationStudio = true) } },
                        onOpenDocumentNotes = { viewModel.openDocumentNotes() },
                        onOpenCanvas = {
                            viewModel.updateState { it.copy(showCanvasView = true) }
                        },
                        onOpenStudyTools = { viewModel.updateState { it.copy(showAiStudyTools = true) } },
                        onOpenTranslationTools = { viewModel.updateState { it.copy(showTranslationTools = true) } },
                        onOpenSleepTimer = { viewModel.updateState { it.copy(showSleepTimerDialog = true) } },
                        onOpenReadingLists = { viewModel.updateState { it.copy(showReadingLists = true) } },
                        onOpenReadingHistory = { viewModel.updateState { it.copy(showReadingHistory = true) } },
                        onAskCurrentSection = {
                            uiState.activeDocument?.let { document ->
                                val prompt = AiPromptLauncher.buildPrompt(
                                    title = document.title,
                                    chunks = document.chunks,
                                    currentIndex = PlaybackStateStore.currentIndex,
                                    type = AiPromptType.EXPLAIN_SECTION,
                                    scope = AiPromptScope.CURRENT_SECTION
                                )
                                viewModel.recordAiPrompt(
                                    document.title,
                                    AiPromptType.EXPLAIN_SECTION.label,
                                    AiPromptScope.CURRENT_SECTION.label,
                                    prompt
                                )
                                copyTextToClipboard(application, "Ask AI", prompt)
                            }
                        },
                        onSelectAskAiAssistant = { option, installedPackage ->
                            viewModel.saveAskAiSettings(
                                uiState.askAiSettings.copy(
                                    assistantId = option.id,
                                    assistantLabel = option.label,
                                    packageName = installedPackage
                                )
                            )
                        },
                        onOpenTextEditor = { viewModel.openCurrentPartTextEditor() },
                        onStartRecord = { viewModel.startRecordSoundFile() },
                        onExportAudio = { viewModel.exportActiveDocumentToAudio() },
                        onCopySelection = { copyTextToClipboard(application, "Veritas selection", it) },
                        onShareSelection = { sharePlainText(application, "Veritas selection", it) },
                        onGoogleSelection = {
                            viewModel.appendVocabularyWord(it, "Looked up definition / web references.")
                            openGoogleSearch(application, it)
                        },
                        onTranslateSelection = {
                            viewModel.appendVocabularyWord(it, "Looked up translation.")
                            openGoogleTranslate(application, it)
                        },
                        onAskAiSelection = {
                            viewModel.appendVocabularyWord(it, "Asked AI for explanation.")
                            askAiWithSelection(application, uiState.askAiSettings, it)
                        },
                        onEditSpeechSelection = { selection ->
                            viewModel.updateState {
                                it.copy(
                                    showPronunciationRules = true,
                                    newRuleFind = selection.replace(Regex("\\s+"), " ").trim().take(120),
                                    newRuleReplaceWith = ""
                                )
                            }
                        },
                        onReadSelection = { sendSelectionSpeechIntent(application, it) },
                        onEditExtractedSelection = { selection ->
                            viewModel.openSelectionTextEditor(selection.sentenceIndexes)
                        },
                        onPlayQueue = { viewModel.playQueue() },
                        onVoiceSelected = { voice ->
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    voiceName = voice.name,
                                    voiceLabel = voice.name,
                                    localeTag = voice.localeTag
                                )
                            )
                        },
                        onReaderModeChange = { PlaybackStateStore.readerMode = it }
                    )
                }

                if (uiState.showGeneralNotesEditor) {
                    GeneralNotesEditor(
                        note = uiState.generalNoteEditorTarget,
                        onSave = { title, content, color, pinned, isChecklist, imageUrl, audioUrl, reminderAt, closeEditor ->
                            viewModel.saveGeneralNote(title, content, color, pinned, isChecklist, imageUrl, audioUrl, reminderAt, closeEditor)
                        },
                        onDelete = { noteId -> viewModel.deleteGeneralNote(noteId) },
                        onCopy = { uiState.generalNoteEditorTarget?.let { viewModel.duplicateGeneralNote(it) } },
                        onDismiss = { viewModel.updateState { it.copy(showGeneralNotesEditor = false, generalNoteEditorTarget = null) } },
                        noteEditorChecklistOnStart = uiState.noteEditorChecklistOnStart,
                        noteEditorReminderOnStart = uiState.noteEditorReminderOnStart,
                        noteEditorImageOnStart = uiState.noteEditorImageOnStart,
                        clearNoteEditorFlags = { viewModel.clearNoteEditorFlags() }
                    )
                }

                if (uiState.importInProgress) {
                    ImportProgressOverlay(
                        title = uiState.importSourceName.ifBlank { "document" }
                    )
                }

                if (uiState.showSettingsHub) {
                    SettingsHubDialog(
                        uiState = uiState,
                        onDismiss = { viewModel.updateState { it.copy(showSettingsHub = false) } },
                        onOpenReaderSettings = { viewModel.updateState { it.copy(showReaderSettings = true) } },
                        onOpenVoiceStudio = { viewModel.updateState { it.copy(showVoiceStudio = true) } },
                        onOpenNarrationStudio = { viewModel.updateState { it.copy(showNarrationStudio = true) } },
                        onOpenPronunciationRules = { viewModel.updateState { it.copy(showPronunciationRules = true) } },
                        onOpenBackupRestore = { viewModel.updateState { it.copy(showBackupTools = true) } },
                        onOpenSyncCenter = { viewModel.updateState { it.copy(showSyncCenter = true) } },
                        onOpenAiCenter = { viewModel.updateState { it.copy(showAiCenter = true) } },
                        onOpenAskAiSettings = { viewModel.updateState { it.copy(showAskAiSettings = true) } },
                        onStartRecord = { viewModel.startRecordSoundFile() },
                        onOpenTextEditor = { viewModel.openCurrentPartTextEditor() },
                        onOpenTutorial = {
                            viewModel.resetQuestProgress()
                            viewModel.updateState { it.copy(showSettingsHub = false) }
                            viewModel.createWelcomeDocumentSilently()
                            OnboardingController.activeStep = OnboardingStep.WELCOME
                        },
                        onOpenPdfTools = { viewModel.updateState { it.copy(showPdfImportTools = true) } },
                        onOpenFileBrowser = { viewModel.openFileBrowser() },
                        onOpenSleepTimer = { viewModel.updateState { it.copy(showSleepTimerDialog = true) } },
                        onOpenReadingLists = { viewModel.updateState { it.copy(showReadingLists = true) } },
                        onOpenUserManual = { viewModel.updateState { it.copy(showUserManual = true) } }
                    )
                }

                if (uiState.showUserManual) {
                    UserManualDialog(
                        onDismiss = { viewModel.updateState { it.copy(showUserManual = false) } },
                        onNavigateToSetting = { target ->
                            viewModel.updateState { it.copy(showUserManual = false) }
                            when (target) {
                                "reader_settings" -> viewModel.updateState { it.copy(showReaderSettings = true) }
                                "pdf_tools" -> viewModel.updateState { it.copy(showPdfImportTools = true) }
                                "sleep_timer" -> viewModel.updateState { it.copy(showSleepTimerDialog = true) }
                                "pronunciation" -> viewModel.updateState { it.copy(showPronunciationRules = true) }
                                "history" -> viewModel.updateState { it.copy(showReadingHistory = true) }
                            }
                        }
                    )
                }

                if (uiState.showVoiceStudio) {
                    VoiceStudioDialog(
                        settings = uiState.voiceSettings,
                        engines = uiState.ttsEngines,
                        voices = uiState.ttsVoices,
                        loadingVoices = uiState.voiceLoadInProgress,
                        onRefreshEngines = {
                            viewModel.updateState {
                                it.copy(ttsEngines = VoiceManager.loadInstalledEngines(application))
                            }
                        },
                        onLoadVoices = { viewModel.loadVoicesForEngine() },
                        onUseSystemDefault = {
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    enginePackage = "",
                                    engineLabel = "System default",
                                    voiceName = "",
                                    voiceLabel = "System default voice"
                                )
                            )
                        },
                        onEngineSelected = { engine ->
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    enginePackage = engine.packageName,
                                    engineLabel = engine.label,
                                    voiceName = "",
                                    voiceLabel = "System default voice"
                                )
                            )
                        },
                        onLanguageSelected = { localeTag ->
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    localeTag = localeTag,
                                    voiceName = "",
                                    voiceLabel = "System default voice"
                                )
                            )
                        },
                        onShowNetworkVoicesChange = { showNetwork ->
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    showNetworkVoices = showNetwork
                                )
                            )
                        },
                        onVoiceSelected = { voice ->
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    voiceName = voice.name,
                                    voiceLabel = voice.label,
                                    localeTag = voice.localeTag
                                )
                            )
                        },
                        onPreviewVoice = { voice -> viewModel.previewVoice(voice) },
                        onPresetSelected = { name, rate, pitch ->
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    profileName = name,
                                    preferredRate = rate,
                                    preferredPitch = pitch
                                )
                            )
                        },
                        onAddLanguageVoice = { viewModel.openTtsDataInstaller() },
                        onOpenSystemTtsSettings = { viewModel.openSystemTtsSettings() },
                        onOpenSpeechEdits = {
                            viewModel.updateState {
                                it.copy(
                                    showVoiceStudio = false,
                                    showPronunciationRules = true
                                )
                            }
                        },
                        onOpenNarrationStudio = {
                            viewModel.updateState {
                                it.copy(
                                    showVoiceStudio = false,
                                    showNarrationStudio = true
                                )
                            }
                        },
                        onDismiss = { viewModel.updateState { it.copy(showVoiceStudio = false) } }
                    )
                }

                if (uiState.showReaderSettings) {
                    ReaderSettingsDialog(
                        settings = uiState.readerSettings,
                        onDismiss = { viewModel.updateState { it.copy(showReaderSettings = false) } },
                        onFontSizeChange = { size ->
                            viewModel.saveReaderSettings(
                                uiState.readerSettings.copy(fontSizeSp = size)
                            )
                        },
                        onSpacingChange = { spacing ->
                            viewModel.saveReaderSettings(
                                uiState.readerSettings.copy(sectionSpacingDp = spacing)
                            )
                        },
                        onThemeChange = { themeId ->
                            viewModel.saveReaderSettings(
                                uiState.readerSettings.copy(themeId = themeId)
                            )
                        },
                        onThemePackChange = { packId ->
                            viewModel.saveReaderSettings(
                                uiState.readerSettings.copy(themePackId = packId)
                            )
                        },
                        onToggleSectionNumbers = {
                            viewModel.saveReaderSettings(
                                uiState.readerSettings.copy(showSectionNumbers = !uiState.readerSettings.showSectionNumbers)
                            )
                        },
                        onToggleAutoPlayQueue = {
                            viewModel.saveReaderSettings(
                                uiState.readerSettings.copy(autoPlayQueue = !uiState.readerSettings.autoPlayQueue)
                            )
                        }
                    )
                }

                if (uiState.showPronunciationRules) {
                    PronunciationRulesDialog(
                        rules = uiState.pronunciationRules,
                        newFind = uiState.newRuleFind,
                        newReplaceWith = uiState.newRuleReplaceWith,
                        onNewFindChange = { text -> viewModel.updateState { it.copy(newRuleFind = text) } },
                        onNewReplaceChange = { text -> viewModel.updateState { it.copy(newRuleReplaceWith = text) } },
                        onAddRule = { viewModel.addPronunciationRule() },
                        onToggleRule = { rule -> viewModel.togglePronunciationRule(rule) },
                        onRemoveRule = { rule -> viewModel.removePronunciationRule(rule) },
                        onDismiss = { viewModel.updateState { it.copy(showPronunciationRules = false) } }
                    )
                }

                if (uiState.showNarrationStudio) {
                    NarrationStudioDialog(
                        settings = uiState.narrationSettings,
                        sampleText = uiState.activeDocument?.chunks?.getOrNull(PlaybackStateStore.currentIndex).orEmpty(),
                        onSettingsChange = { settings -> viewModel.saveNarrationSettings(settings) },
                        onDismiss = { viewModel.updateState { it.copy(showNarrationStudio = false) } }
                    )
                }

                if (uiState.showAskAiSettings) {
                    AskAiSettingsDialog(
                        settings = uiState.askAiSettings,
                        onSettingsChange = { settings -> viewModel.saveAskAiSettings(settings) },
                        onInstallAssistant = { packageName ->
                            openPlayStoreForPackage(application, packageName)
                        },
                        onDismiss = { viewModel.updateState { it.copy(showAskAiSettings = false) } }
                    )
                }

                if (uiState.showAiCenter) {
                    AiCenterDialog(
                        installedAiCount = installedAiOptions(application).size,
                        documentCount = uiState.documents.size,
                        onOpenAskAiSettings = {
                            viewModel.updateState {
                                it.copy(
                                    showAiCenter = false,
                                    showAskAiSettings = true
                                )
                            }
                        },
                        onOpenStudyTools = {
                            viewModel.updateState {
                                it.copy(
                                    showAiCenter = false,
                                    showAiStudyTools = true
                                )
                            }
                        },
                        onDismiss = { viewModel.updateState { it.copy(showAiCenter = false) } }
                    )
                }

                uiState.activeDocument?.let { document ->
                    if (uiState.showAiStudyTools) {
                        AiAppStudyDialog(
                            document = document,
                            currentIndex = PlaybackStateStore.currentIndex,
                            templates = uiState.aiPromptTemplates,
                            history = uiState.aiPromptHistory,
                            onDismiss = { viewModel.updateState { it.copy(showAiStudyTools = false) } },
                            onSendToAiApp = { type, customInstruction, scope, range ->
                                val prompt = AiPromptLauncher.buildPrompt(
                                    title = document.title,
                                    chunks = document.chunks,
                                    currentIndex = PlaybackStateStore.currentIndex,
                                    type = type,
                                    customInstruction = customInstruction,
                                    scope = scope
                                )
                                AiPromptLauncher.launch(
                                    context = application,
                                    document = document,
                                    currentIndex = PlaybackStateStore.currentIndex,
                                    type = type,
                                    customInstruction = customInstruction,
                                    scope = scope,
                                    customPageRange = range,
                                    settings = uiState.askAiSettings
                                )
                                viewModel.recordAiPrompt(document.title, type.label, scope.label, prompt)
                            },
                            onSaveTemplate = { title, instruction ->
                                viewModel.saveAiPromptTemplate(title, instruction)
                            },
                            onDeleteTemplate = { id -> viewModel.deleteAiPromptTemplate(id) },
                            onClearHistory = { viewModel.clearAiPromptHistory() },
                            onCopyText = { label, text -> copyTextToClipboard(application, label, text) },
                            onSaveAiResultAsNote = { result ->
                                viewModel.updateState {
                                    it.copy(
                                        noteDraft = result,
                                        noteTargetIndexes = listOf(PlaybackStateStore.currentIndex)
                                    )
                                }
                                viewModel.saveSentenceNote()
                            },
                            onOpenOfflineStudyTools = {
                                viewModel.updateState {
                                    it.copy(
                                        showAiStudyTools = false,
                                        showOfflineStudyTools = true
                                    )
                                }
                            }
                        )
                    }
                }

                uiState.activeDocument?.let { document ->
                    if (uiState.showOfflineStudyTools) {
                        StudyToolsDialog(
                            studyPack = StudyAssistant.buildStudyPack(
                                document.title,
                                document.chunks,
                                PlaybackStateStore.currentIndex
                            ),
                            onDismiss = { viewModel.updateState { it.copy(showOfflineStudyTools = false) } }
                        )
                    }
                }

                if (uiState.showSleepTimerDialog) {
                    SleepTimerDialog(
                        activeTimer = VeritasSleepTimerSnapshot(
                            durationMillis = PlaybackStateStore.sleepTimerDurationMillis,
                            endsAtMillis = PlaybackStateStore.sleepTimerEndsAtMillis,
                            action = PlaybackStateStore.sleepTimerAction
                        ).takeIf { it.durationMillis > 0L && it.isActive() },
                        onSetTimer = { request -> viewModel.setSleepTimer(request) },
                        onCancelTimer = { viewModel.cancelSleepTimer() },
                        onDismiss = { viewModel.updateState { it.copy(showSleepTimerDialog = false) } }
                    )
                }

                uiState.pendingImport?.let { pending ->
                    VeritasImportPreviewDialog(
                        pendingImport = pending,
                        onConfirm = { title, pdfOpts, textOpts -> viewModel.executePendingImport(title, pdfOpts, textOpts) },
                        onCancel = { viewModel.cancelPendingImport() }
                    )
                }

                if (uiState.showReadingLists) {
                    ReadingListsDialog(
                        catalog = uiState.readingListCatalog,
                        documents = uiState.documents,
                        activeDocumentId = uiState.activeDocument?.id,
                        onDismiss = { viewModel.updateState { it.copy(showReadingLists = false) } },
                        onCreateList = { title -> viewModel.createReadingList(title, null) },
                        onAddDocument = { listId, docId -> viewModel.addDocumentToReadingList(listId, docId) },
                        onRemoveDocument = { listId, docId -> viewModel.removeDocumentFromReadingList(listId, docId) },
                        onOpenDocument = { viewModel.openSavedDocument(it) },
                        onMoveDocument = { listId, docId, offset -> viewModel.moveReadingListDocument(listId, docId, offset) },
                        onSetSortMode = { listId, sortMode -> viewModel.setReadingListSortMode(listId, sortMode) },
                        onArchiveList = { listId -> viewModel.archiveReadingList(listId) },
                        onDeleteList = { listId -> viewModel.deleteReadingList(listId) }
                    )
                }

                if (uiState.showTranslationTools) {
                    val activeDocument = uiState.activeDocument
                    if (activeDocument != null) {
                        TranslationToolsDialog(
                            document = activeDocument,
                            currentIndex = PlaybackStateStore.currentIndex,
                            onDismiss = { viewModel.updateState { it.copy(showTranslationTools = false) } },
                            onSend = { targetLang, mode ->
                                TranslationLauncher.launch(
                                    context = application,
                                    title = activeDocument.title,
                                    chunks = activeDocument.chunks,
                                    currentIndex = PlaybackStateStore.currentIndex,
                                    targetLanguage = targetLang,
                                    mode = mode
                                )
                            }
                        )
                    }
                }

                if (uiState.showPdfImportTools) {
                    PdfImportOptionsDialog(
                        options = uiState.advancedPdfOptions,
                        textOptions = uiState.textImportOptions,
                        onOptionsChange = { opt -> viewModel.updateState { it.copy(advancedPdfOptions = opt) } },
                        onTextOptionsChange = { opt -> viewModel.updateState { it.copy(textImportOptions = opt) } },
                        onPickPdf = {
                            importFileLauncher.launch(arrayOf("application/pdf"))
                        },
                        onDismiss = { viewModel.updateState { it.copy(showPdfImportTools = false) } }
                    )
                }

                if (uiState.showFileBrowser) {
                    FileBrowserDialog(
                        roots = uiState.fileBrowserRoots,
                        entries = uiState.fileBrowserFiles,
                        location = uiState.fileBrowserLocation,
                        canGoUp = uiState.fileBrowserBackStack.isNotEmpty(),
                        scanning = uiState.fileBrowserScanning,
                        message = uiState.fileBrowserMessage,
                        allFilesAccessGranted = true,
                        importing = uiState.importInProgress,
                        importingName = uiState.importSourceName,
                        onDismiss = { viewModel.updateState { it.copy(showFileBrowser = false) } },
                        onPickFolder = { folderPickerLauncher.launch(null) },
                        onRequestAllFilesAccess = {},
                        onOpenFilePicker = { importFileLauncher.launch(readableImportMimeTypes()) },
                        onRefresh = { viewModel.refreshFileBrowser() },
                        onGoUp = { viewModel.goUpFileBrowserDirectory() },
                        onEnterDirectory = { file -> viewModel.enterFileBrowserDirectory(file) },
                        onRemoveAllAccess = { viewModel.clearFileBrowserAccess() },
                        onImportFile = { file ->
                            if (file.isSupported && !file.isDirectory) {
                                viewModel.prepareImport(uri = file.uri, sourceNameHint = file.name)
                            }
                        },
                        onImportMultipleFiles = { files, queue ->
                            viewModel.importMultipleDocuments(files.map { it.uri }, queue)
                        }
                    )
                }

                if (uiState.showReadingHistory) {
                    ReadingHistoryDialog(
                        history = uiState.readingHistory,
                        documents = uiState.documents,
                        onOpenDocument = { viewModel.openSavedDocument(it) },
                        onClearHistory = { viewModel.clearReadingHistory() },
                        onDismiss = { viewModel.updateState { it.copy(showReadingHistory = false) } }
                    )
                }

                if (uiState.showDocumentNotes) {
                    val activeDocument = uiState.activeDocument
                    if (activeDocument != null) {
                        DocumentNotesDialog(
                            document = activeDocument,
                            annotations = uiState.annotations,
                            documentNote = uiState.documentNoteDraft,
                            currentIndex = PlaybackStateStore.currentIndex,
                            onDocumentNoteChange = { text -> viewModel.updateState { it.copy(documentNoteDraft = text) } },
                            onSaveDocumentNote = { viewModel.saveDocumentNoteDraft() },
                            onAddCurrentNote = { viewModel.beginSentenceNote(listOf(PlaybackStateStore.currentIndex)) },
                            onJumpToSection = { index -> viewModel.moveTo(index, false) },
                            onExportNotes = {
                                val exportedNotes = buildDocumentNotesExport(
                                    activeDocument,
                                    uiState.annotations,
                                    uiState.documentNoteDraft
                                )
                                viewModel.updateState { it.copy(pendingTextDownload = activeDocument.title to exportedNotes) }
                                textDownloadLauncher.launch(veritasBackupFileName("${activeDocument.title}_notes"))
                            },
                            onDismiss = { viewModel.updateState { it.copy(showDocumentNotes = false) } }
                        )
                    }
                }

                val noteIndexes = uiState.noteTargetIndexes.ifEmpty {
                    uiState.noteTargetIndex?.let(::listOf).orEmpty()
                }
                if (noteIndexes.isNotEmpty()) {
                    val activeDocument = uiState.activeDocument
                    if (activeDocument != null) {
                        SentenceNoteDialog(
                            document = activeDocument,
                            sentenceIndexes = noteIndexes,
                            noteDraft = uiState.noteDraft,
                            onNoteChange = { draft -> viewModel.updateState { it.copy(noteDraft = draft) } },
                            onSave = { viewModel.saveSentenceNote() },
                            onDelete = { viewModel.deleteSentenceNote() },
                            onDismiss = { viewModel.updateState { it.copy(noteTargetIndex = null, noteTargetIndexes = emptyList(), noteDraft = "") } }
                        )
                    }
                }

                if (uiState.showTextEditor) {
                    val document = uiState.activeDocument
                    val target = uiState.editorTarget
                    if (document != null && target != null) {
                        TextEditorDialog(
                            document = document,
                            currentIndex = PlaybackStateStore.currentIndex,
                            text = uiState.editorText,
                            target = target,
                            onTextChange = { text -> viewModel.updateState { it.copy(editorText = text) } },
                            onSave = { viewModel.saveTextEditorChanges() },
                            onDownloadToPhone = {
                                val fileName = textEditorDownloadName(document, target)
                                viewModel.updateState { it.copy(pendingTextDownload = fileName to uiState.editorText) }
                                textDownloadLauncher.launch(fileName)
                            },
                            onDismiss = { viewModel.dismissTextEditor() }
                        )
                    }
                }

                if (uiState.showBackupTools) {
                    BackupRestoreDialog(
                        documentCount = uiState.documents.size,
                        annotationCount = uiState.annotationCount,
                        queueCount = uiState.queuedDocuments.size,
                        inProgress = uiState.backupInProgress,
                        message = uiState.backupMessage,
                        onExport = {
                            backupExportLauncher.launch(veritasBackupFileName("veritas_backup"))
                        },
                        onImport = {
                            backupImportLauncher.launch(veritasBackupMimeTypes())
                        },
                        onDismiss = { viewModel.updateState { it.copy(showBackupTools = false) } }
                    )
                }

                if (uiState.showSyncCenter) {
                    SyncCenterDialog(
                        documentCount = uiState.documents.size,
                        annotationCount = uiState.annotationCount,
                        queueCount = uiState.queuedDocuments.size,
                        pronunciationRuleCount = uiState.pronunciationRules.size,
                        inProgress = uiState.backupInProgress,
                        message = uiState.backupMessage,
                        onExportSyncPack = {
                            backupExportLauncher.launch(veritasBackupFileName("veritas_sync_pack"))
                        },
                        onShareSyncPack = {
                            viewModel.updateState { it.copy(showSyncCenter = false) }
                            // copy to clipboard as sync pack action on desktop
                            coroutineScope.launch {
                                viewModel.exportLibraryBackup(Uri.parse("file://" + File(application.filesDir, "veritas_sync_pack.json").absolutePath))
                            }
                        },
                        onImportSyncPack = {
                            backupImportLauncher.launch(veritasBackupMimeTypes())
                        },
                        onDismiss = { viewModel.updateState { it.copy(showSyncCenter = false) } }
                    )
                }

                uiState.deleteTarget?.let { target ->
                    AlertDialog(
                        onDismissRequest = { viewModel.updateState { it.copy(deleteTarget = null) } },
                        title = { Text("Delete reading?") },
                        text = {
                            Text("Are you sure you want to permanently delete \"${target.title}\"? This action cannot be undone.")
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.deleteDocument(target) }) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.updateState { it.copy(deleteTarget = null) } }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                uiState.renameTarget?.let { target ->
                    AlertDialog(
                        onDismissRequest = { viewModel.updateState { it.copy(renameTarget = null, renameDraft = "") } },
                        title = { Text("Rename reading") },
                        text = {
                            OutlinedTextField(
                                value = uiState.renameDraft,
                                onValueChange = { text -> viewModel.updateState { it.copy(renameDraft = text) } },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Title") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.renameDocument(target, uiState.renameDraft)
                                },
                                enabled = uiState.renameDraft.isNotBlank()
                            ) {
                                Text("Rename")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.updateState { it.copy(renameTarget = null, renameDraft = "") } }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                uiState.collectionTarget?.let { target ->
                    AlertDialog(
                        onDismissRequest = { viewModel.updateState { it.copy(collectionTarget = null, collectionDraft = "") } },
                        title = { Text("Set collection") },
                        text = {
                            OutlinedTextField(
                                value = uiState.collectionDraft,
                                onValueChange = { text -> viewModel.updateState { it.copy(collectionDraft = text) } },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Collection name") },
                                placeholder = { Text("e.g. Work, School, Articles") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.setDocumentCollection(target, uiState.collectionDraft)
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.updateState { it.copy(collectionTarget = null, collectionDraft = "") } }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                uiState.detailsTarget?.let { target ->
                    AlertDialog(
                        onDismissRequest = { viewModel.updateState { it.copy(detailsTarget = null) } },
                        title = { Text(target.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Source: ${target.sourceLabel.ifBlank { "Text" }}")
                                Text("Collection: ${target.collection.ifBlank { "Unfiled" }}")
                                Text("Progress: ${target.currentIndex + 1} / ${target.chunkCount.coerceAtLeast(1)}")
                                Text("Characters: ${target.charCount}")
                                Text("Updated: ${formatUpdated(target.updatedAt)}")
                                Text(
                                    target.preview,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.updateState { it.copy(detailsTarget = null) } }) {
                                Text("Close")
                            }
                        }
                    )
                }
            }
        }
    }
}
}

