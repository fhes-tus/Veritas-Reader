package com.veritas.reader


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.content.ContentUris
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.Menu
import android.widget.Toast
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.foundation.BorderStroke
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.veritas.reader.ui.*
import com.veritas.reader.ui.ReaderViewModel
import com.veritas.reader.ui.VeritasPendingImport
import com.veritas.reader.ui.screens.AskAiSettingsDialog
import com.veritas.reader.ui.screens.DocumentNotesDialog
import com.veritas.reader.ui.screens.FeatureDropdownMenuItem
import com.veritas.reader.ui.screens.LibraryScreen
import com.veritas.reader.ui.screens.GeneralNotesEditor
import com.veritas.reader.ui.screens.NarrationStudioDialog
import com.veritas.reader.ui.screens.PronunciationRulesDialog
import com.veritas.reader.ui.screens.ReaderScreen
import com.veritas.reader.ui.screens.ReaderScreenState
import com.veritas.reader.ui.screens.ReaderSettingsDialog
import com.veritas.reader.ui.screens.AccessibilitySettingsDialog
import com.veritas.reader.ui.screens.ReadingListsDialog
import com.veritas.reader.ui.screens.SettingsHubDialog
import com.veritas.reader.ui.screens.StorageDialog
import com.veritas.reader.ui.screens.formatVeritasBytes
import com.veritas.reader.ui.screens.UserManualDialog
import com.veritas.reader.ui.screens.AboutDialog
import com.veritas.reader.ui.screens.SleepTimerDialog
import com.veritas.reader.ui.screens.UpdateAvailableDialog
import com.veritas.reader.ui.screens.ReleaseNotesDialog
import com.veritas.reader.ui.screens.ClassicsCatalogDialog
import com.veritas.reader.ui.screens.OceanOfPdfBrowserDialog
import com.veritas.reader.ui.screens.BookCatalogBrowserDialog
import com.veritas.reader.ui.screens.VeritasHomeTab
import com.veritas.reader.ui.screens.VoiceStudioDialog
import com.veritas.reader.ReaderMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.veritas.reader.ui.screens.OnboardingQuestChecklist
import com.veritas.reader.ui.screens.OnboardingSpotlightOverlay
import com.veritas.reader.ui.screens.RevampedOnboardingFlow
import com.veritas.reader.ui.screens.ConfettiOverlay
import com.veritas.reader.ui.OnboardingStep
import com.veritas.reader.ui.OnboardingController
import androidx.compose.ui.layout.onGloballyPositioned
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt



@Composable
internal fun MainCatalogAndToolsDialogsHost(
    viewModel: ReaderViewModel,
    uiState: ReaderUiState,
    pendingShareChooser: Pair<String, Uri?>?,
    onDismissShareChooser: () -> Unit,
    onOpenInNotes: (String, Uri?) -> Unit,
    onImportToReader: (String, Uri?) -> Unit
) {
    val context = LocalContext.current

        if (uiState.showNarrationStudio) {
            NarrationStudioDialog(
                settings = uiState.narrationSettings,
                sampleText = uiState.activeDocument?.chunks?.getOrNull(PlaybackStateStore.currentIndex)
                    .orEmpty(),
                availableVoices = uiState.ttsVoices,
                onSettingsChange = { settings -> viewModel.saveNarrationSettings(settings) },
                onDismiss = { viewModel.updateState { it.copy(showNarrationStudio = false) } }
            )
        }

        if (uiState.showAskAiSettings) {
            AskAiSettingsDialog(
                settings = uiState.askAiSettings,
                onSettingsChange = { settings -> viewModel.saveAskAiSettings(settings) },
                onInstallAssistant = { packageName ->
                    openPlayStoreForPackage(
                        context,
                        packageName
                    )
                },
                onDismiss = { viewModel.updateState { it.copy(showAskAiSettings = false) } }
            )
        }

        if (uiState.showAiCenter) {
            AiCenterDialog(
                installedAiCount = installedAiOptions(context).size,
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

        if (uiState.showAiStudyTools && uiState.activeDocument == null) {
            AlertDialog(
                onDismissRequest = { viewModel.updateState { it.copy(showAiStudyTools = false) } },
                title = { Text("AI Study Tools") },
                text = { Text("Open a reading first, then AI Study Tools can prepare the current part or whole document.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.updateState { it.copy(showAiStudyTools = false) } }) {
                        Text(
                            "OK"
                        )
                    }
                }
            )
        }

        uiState.activeDocument?.let { document ->
            if (uiState.showAiStudyTools) {
                AiAppStudyDialog(
                    document = document,
                    currentIndex = PlaybackStateStore.currentIndex,
                    templates = uiState.aiPromptTemplates,
                    history = uiState.aiPromptHistory,
                    askAiSettings = uiState.askAiSettings,
                    onUpdateAskAiSettings = { viewModel.saveAskAiSettings(it) },
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
                            context = context,
                            document = document,
                            currentIndex = PlaybackStateStore.currentIndex,
                            type = type,
                            customInstruction = customInstruction,
                            scope = scope,
                            customPageRange = range,
                            settings = uiState.askAiSettings,
                            // false: the study-tool prompt must be copied to the clipboard
                            // and prepended to the share body — noPrompt=true silently
                            // dropped the user's edited instructions entirely.
                            noPrompt = false
                        )
                        viewModel.recordAiPrompt(document.title, type.label, scope.label, prompt)
                    },
                    onSaveTemplate = { title, instruction ->
                        viewModel.saveAiPromptTemplate(
                            title,
                            instruction
                        )
                    },
                    onDeleteTemplate = { id -> viewModel.deleteAiPromptTemplate(id) },
                    onClearHistory = { viewModel.clearAiPromptHistory() },
                    onCopyText = { label, text -> copyTextToClipboard(context, label, text) },
                    onSaveAiResultAsNote = { result ->
                        viewModel.updateState {
                            it.copy(
                                noteDraft = result,
                                noteTargetIndexes = listOf(PlaybackStateStore.currentIndex)
                            )
                        }
                        viewModel.saveSentenceNote()
                    },
                    onImportFlashcards = { name, cards ->
                        viewModel.importFlashcards(document.id ?: "pasted", name, cards)
                    },
                    onGenerateInAppFlashcards = { scopeText, count, onComplete ->
                        viewModel.generateInAppFlashcards(
                            document = document,
                            count = count,
                            scopeText = scopeText,
                            setName = "${document.title} Flashcards",
                            onComplete = onComplete
                        )
                    },
                    onGenerateInAppQuiz = { scopeText, count, onComplete ->
                        viewModel.generateInAppQuiz(
                            document = document,
                            count = count,
                            scopeText = scopeText,
                            quizTitle = "${document.title} Quiz",
                            onComplete = onComplete
                        )
                    },
                    onGenerateInAppSummary = { scopeText, onComplete ->
                        viewModel.generateInAppStudySummary(
                            document = document,
                            scopeText = scopeText,
                            onComplete = onComplete
                        )
                    },
                    onGenerateInAppExplanation = { scopeText, targetPassage, onComplete ->
                        viewModel.generateInAppExplanation(
                            document = document,
                            scopeText = scopeText,
                            targetPassage = targetPassage,
                            onComplete = onComplete
                        )
                    },
                    onGenerateInAppStudyGuide = { scopeText, onComplete ->
                        viewModel.generateInAppStudyGuide(
                            document = document,
                            scopeText = scopeText,
                            onComplete = onComplete
                        )
                    },
                    onSaveQuiz = { quiz ->
                        viewModel.saveQuiz(quiz)
                    },
                    onRecordQuizScore = { quizId, score ->
                        viewModel.recordQuizScore(quizId, score)
                    },
                    onRateFlashcard = { cardId, recall ->
                        viewModel.rateFlashcardRecall(cardId, recall)
                    },
                    onOpenStudyHub = {
                        viewModel.updateState { it.copy(showAiStudyTools = false, showAiCenter = false) }
                        viewModel.navigateToHomeTab(VeritasHomeTab.STUDY)
                    }
                )
            }
        }

        if ((uiState.exportInProgress || uiState.exportMessage != null || uiState.exportedAudioFile != null) && !uiState.recordMode && !uiState.recordAwaitingDecision) {
            ExportAudioStatusDialog(
                inProgress = uiState.exportInProgress,
                message = uiState.exportMessage,
                file = uiState.exportedAudioFile,
                onShare = { file -> viewModel.shareExportedAudio(file) },
                onCancel = { viewModel.cancelAudioExport() },
                onDismiss = {
                    viewModel.updateState {
                        it.copy(
                            exportMessage = null,
                            exportedAudioFile = null
                        )
                    }
                }
            )
        }

        pendingShareChooser?.let { (text, uri) ->
            ShareTargetChooserDialog(
                sharedText = text,
                sharedUri = uri,
                onImportToReader = {
                    onDismissShareChooser()
                    onImportToReader(text, uri)
                },
                onAddToNotes = {
                    onDismissShareChooser()
                    onOpenInNotes(text, uri)
                },
                onDismiss = {
                    onDismissShareChooser()
                }
            )
        }

        if (uiState.showSleepTimerDialog) {
            SleepTimerDialog(
                activeTimer = PlaybackStateStore.activeSleepTimerSnapshot(),
                onSetTimer = viewModel::setSleepTimer,
                onCancelTimer = viewModel::cancelSleepTimer,
                onDismiss = { viewModel.updateState { it.copy(showSleepTimerDialog = false) } }
            )
        }

        if (uiState.showUpdateDialog) {
            val context = LocalContext.current
            val isPlayStore = remember(context) { isInstalledFromGooglePlay(context) }
            UpdateAvailableDialog(
                versionName = uiState.updateVersionName,
                changelog = uiState.updateChangelog,
                isDownloading = uiState.isDownloadingUpdate,
                downloadProgress = uiState.updateDownloadProgress,
                downloadError = uiState.updateDownloadError,
                isPlayStoreInstall = isPlayStore,
                onUpdate = {
                    if (uiState.updateApkUrl.isNotEmpty()) {
                        viewModel.startUpdateDownload(uiState.updateApkUrl)
                    } else {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uiState.updateUrl))
                            context.startActivity(intent)
                        }
                        viewModel.updateState { it.copy(showUpdateDialog = false) }
                    }
                },
                onOpenPlayStore = {
                    openGooglePlayStore(context)
                },
                onCancelDownload = {
                    viewModel.cancelUpdateDownload()
                },
                onDismiss = {
                    viewModel.cancelUpdateDownload()
                    viewModel.updateState { it.copy(showUpdateDialog = false) }
                }
            )
        }

        if (uiState.showReleaseNotesDialog) {
            ReleaseNotesDialog(
                versionName = uiState.releaseNotesVersionName,
                changelog = uiState.releaseNotesChangelog,
                onDismiss = {
                    viewModel.dismissReleaseNotes()
                }
            )
        }

        if (uiState.showClassicsCatalog) {
            ClassicsCatalogDialog(
                existingDocuments = uiState.documents,
                onDownloadBook = { book ->
                    viewModel.downloadClassicBook(book)
                },
                onOpenBook = { doc ->
                    viewModel.updateState { it.copy(showClassicsCatalog = false) }
                    viewModel.openSavedDocument(doc)
                },
                onOpenOceanOfPdf = { query ->
                    viewModel.updateState {
                        it.copy(
                            showBookBrowser = true,
                            bookBrowserUrl = "https://oceanofpdf.com/",
                            bookBrowserTitle = "Ocean of PDF",
                            bookBrowserQuery = query
                        )
                    }
                },
                onOpenBookBrowser = { url, name, query ->
                    viewModel.updateState {
                        it.copy(
                            showBookBrowser = true,
                            bookBrowserUrl = url,
                            bookBrowserTitle = name,
                            bookBrowserQuery = query
                        )
                    }
                },
                onDismiss = {
                    viewModel.updateState { it.copy(showClassicsCatalog = false) }
                }
            )
        }

        if (uiState.showBookBrowser || uiState.showOceanOfPdfBrowser) {
            val url = uiState.bookBrowserUrl.ifBlank { "https://oceanofpdf.com/" }
            val name = uiState.bookBrowserTitle.ifBlank { "Free Books" }
            val query = uiState.bookBrowserQuery.ifBlank { uiState.oceanOfPdfQuery }
            BookCatalogBrowserDialog(
                initialUrl = url,
                siteName = name,
                initialQuery = query,
                onImportDownloadedFile = { file, title ->
                    viewModel.importDownloadedBook(file, title)
                },
                onDismiss = {
                    viewModel.updateState {
                        it.copy(
                            showBookBrowser = false,
                            showOceanOfPdfBrowser = false,
                            bookBrowserUrl = "",
                            bookBrowserTitle = "",
                            bookBrowserQuery = "",
                            oceanOfPdfQuery = ""
                        )
                    }
                }
            )
        }

        val pending = uiState.pendingImport
        if (pending != null) {
            VeritasImportPreviewDialog(
                pendingImport = pending,
                onConfirm = viewModel::executePendingImport,
                onCancel = viewModel::cancelPendingImport
            )
        }

        if (uiState.showReadingLists) {
            ReadingListsDialog(
                catalog = uiState.readingListCatalog,
                documents = uiState.documents,
                activeDocumentId = uiState.activeDocument?.id,
                onDismiss = { viewModel.updateState { it.copy(showReadingLists = false) } },
                onCreateList = { title -> viewModel.createReadingList(title) },
                onAddDocument = viewModel::addDocumentToReadingList,
                onRemoveDocument = viewModel::removeDocumentFromReadingList,
                onOpenDocument = { doc ->
                    viewModel.updateState { it.copy(showReadingLists = false) }
                    viewModel.openSavedDocument(doc)
                },
                onMoveDocument = viewModel::moveReadingListDocument,
                onSetSortMode = viewModel::setReadingListSortMode,
                onArchiveList = viewModel::archiveReadingList,
                onDeleteList = viewModel::deleteReadingList
            )
        }

        if (uiState.showGeneralNotesEditor) {
            GeneralNotesEditor(
                note = uiState.generalNoteEditorTarget,
                onSave = { title, content, color, pinned, isChecklist, imageUrl, audioUrl, reminderAt, closeEditor, audioUrls ->
                    viewModel.saveGeneralNote(title, content, color, pinned, isChecklist, imageUrl, audioUrl, reminderAt, closeEditor, audioUrls)
                },
                onDelete = { noteId -> viewModel.deleteGeneralNote(noteId) },
                onCopy = {
                    uiState.generalNoteEditorTarget?.let { target ->
                        viewModel.duplicateGeneralNote(target)
                    }
                },
                onDismiss = { viewModel.updateState { it.copy(showGeneralNotesEditor = false, generalNoteEditorTarget = null) } }
            )
        }

        uiState.activeDocument?.let { document ->
            if (uiState.showTranslationTools) {
                TranslationToolsDialog(
                    document = document,
                    currentIndex = PlaybackStateStore.currentIndex,
                    onDismiss = { viewModel.updateState { it.copy(showTranslationTools = false) } },
                    onSend = { targetLanguage, mode ->
                        TranslationLauncher.launch(
                            context = context,
                            title = document.title,
                            chunks = document.chunks,
                            currentIndex = PlaybackStateStore.currentIndex,
                            targetLanguage = targetLanguage,
                            mode = mode
                        )
                        viewModel.updateState { it.copy(showTranslationTools = false) }
                    }
                )
            }
        }


}
