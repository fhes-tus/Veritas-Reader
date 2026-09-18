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
import androidx.compose.ui.res.stringResource
import com.veritas.reader.ui.screens.VeritasHomeTab
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
internal fun MainIntentAndRoutingEffects(
    context: Context,
    viewModel: ReaderViewModel,
    uiState: ReaderUiState,
    initialSharedText: String,
    initialSharedUri: Uri?,
    isShareToNotes: Boolean,
    openVoiceStudioOnStart: Boolean,
    widgetAction: String?,
    noteId: String?,
    onOpenInNotes: (String, Uri?) -> Unit,
    onPendingShareChooserChange: (Pair<String, Uri?>?) -> Unit
) {
    val documentRepository = remember(context) { DocumentRepository(context.applicationContext) }
    LaunchedEffect(Unit) {
        if (!uiState.handledInitialShare) {
            if (isShareToNotes) {
                if (initialSharedText.isNotBlank() || initialSharedUri != null) {
                    onOpenInNotes(initialSharedText, initialSharedUri)
                }
            } else if (initialSharedText.isNotBlank() || initialSharedUri != null) {
                onPendingShareChooserChange(Pair(initialSharedText, initialSharedUri))
            }
            if (openVoiceStudioOnStart) {
                viewModel.updateState { it.copy(showVoiceStudio = true) }
            }
            viewModel.updateState { it.copy(handledInitialShare = true) }
        }
    }

    LaunchedEffect(uiState.pendingWidgetAction) {
        val action = uiState.pendingWidgetAction
        val docId = uiState.pendingWidgetDocId
        val noteId = uiState.pendingWidgetNoteId

        if (action != null) {
            when (action) {
                MainActivity.ACTION_NEW_NOTE -> {
                    viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = null) }
                }
                MainActivity.ACTION_SHOW_NOTES -> {
                    viewModel.navigateToHomeTab(VeritasHomeTab.NOTES)
                }
                MainActivity.ACTION_NEW_CHECKLIST_NOTE -> {
                    viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = null, noteEditorChecklistOnStart = true) }
                }
                MainActivity.ACTION_NEW_REMINDER_NOTE -> {
                    viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = null, noteEditorReminderOnStart = true) }
                }
                MainActivity.ACTION_NEW_IMAGE_NOTE -> {
                    viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = null, noteEditorImageOnStart = true) }
                }
                MainActivity.ACTION_OPEN_LIBRARY -> {
                    viewModel.navigateToHomeTab(VeritasHomeTab.LIBRARY)
                }
                MainActivity.ACTION_SHOW_STUDY_DASHBOARD,
                MainActivity.ACTION_SHOW_FLASHCARDS -> {
                    viewModel.navigateToHomeTab(VeritasHomeTab.STUDY)
                }
                MainActivity.ACTION_IMPORT_DOCUMENTS -> {
                    viewModel.returnToLibrary()
                    viewModel.openFileBrowser()
                }
                MainActivity.ACTION_ACTIVE_READING,
                MainActivity.ACTION_CONTINUE_READING -> {
                    val activeDocId = uiState.activeDocument?.id
                    val docToOpen: SavedDocument? = if (activeDocId != null) {
                        uiState.documents.firstOrNull { it.id == activeDocId }
                    } else {
                        val lastHistory = uiState.readingHistory.maxByOrNull { it.openedAt }
                        val found: SavedDocument? = if (lastHistory != null) {
                            uiState.documents.firstOrNull { it.id == lastHistory.documentId }
                        } else {
                            uiState.documents.maxByOrNull { it.updatedAt }
                        }
                        found
                    }
                    if (docToOpen != null) {
                        PlaybackStateStore.readerMode = ReaderMode.TEXT
                        viewModel.openSavedDocument(docToOpen)
                    }
                }
                MainActivity.ACTION_NEW_READING_NOTE -> {
                    if (uiState.activeDocument != null) {
                        viewModel.updateState { it.copy(showDocumentNotes = true) }
                    } else {
                        viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = null) }
                    }
                }
                MainActivity.ACTION_NEW_STUDY_NOTE -> {
                    viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = null) }
                }
                MainActivity.ACTION_VOICE_NOTE -> {
                    viewModel.updateState { it.copy(showVoiceStudio = true) }
                }
                MainActivity.ACTION_EDIT_NOTE -> {
                    noteId?.let { nid ->
                        val note = documentRepository.loadGeneralNotes().firstOrNull { it.id == nid }
                        if (note != null) {
                            viewModel.updateState { it.copy(showGeneralNotesEditor = true, generalNoteEditorTarget = note) }
                        }
                    }
                }
            }
            viewModel.updateState { it.copy(pendingWidgetAction = null, pendingWidgetDocId = null, pendingWidgetNoteId = null, pendingImportOnStart = false) }
        }
    }

    val pendingFixWord = PlaybackStateStore.pendingPronunciationFixWord
    LaunchedEffect(pendingFixWord) {
        if (pendingFixWord != null) {
            viewModel.updateState {
                it.copy(
                    showPronunciationRules = true,
                    newRuleFind = pendingFixWord,
                    newRuleReplaceWith = ""
                )
            }
            PlaybackStateStore.pendingPronunciationFixWord = null
        }
    }

    LaunchedEffect(PlaybackStateStore.readerMode, uiState.activeDocument) {
        val activeDoc = uiState.activeDocument
        if (PlaybackStateStore.readerMode == ReaderMode.ORIGINAL && activeDoc != null) {
            val activeMetadata = uiState.documents.firstOrNull { it.id == activeDoc.id }
            if (activeMetadata != null) {
                val file = documentRepository.originalFile(activeMetadata)
                val isPdf = activeMetadata.originalMimeType.contains("pdf", ignoreCase = true) ||
                        activeMetadata.originalFileName.endsWith(".pdf", ignoreCase = true) ||
                        (file != null && file.exists() && runCatching {
                            file.inputStream().use { input ->
                                val bytes = ByteArray(4)
                                val read = input.read(bytes)
                                read == 4 && bytes[0] == '%'.code.toByte() && bytes[1] == 'P'.code.toByte() && bytes[2] == 'D'.code.toByte() && bytes[3] == 'F'.code.toByte()
                            }
                        }.getOrDefault(false))
                if (isPdf) {
                    context.startActivity(
                        VeritasPdfViewerActivity.intent(
                            context,
                            activeMetadata.id
                        )
                    )
                    PlaybackStateStore.readerMode = ReaderMode.TEXT
                } else {
                    viewModel.updateState { it.copy(showCanvasView = true) }
                    PlaybackStateStore.readerMode = ReaderMode.TEXT
                }
            }
        }
    }

    BackHandler(enabled = true) {
        when {
            uiState.showExitConfirmationDialog -> viewModel.updateState { it.copy(showExitConfirmationDialog = false) }
            uiState.detailsTarget != null -> viewModel.updateState { it.copy(detailsTarget = null) }
            uiState.renameTarget != null -> viewModel.updateState { it.copy(renameTarget = null) }
            uiState.collectionTarget != null -> viewModel.updateState { it.copy(collectionTarget = null) }
            uiState.showClassicsCatalog -> viewModel.updateState { it.copy(showClassicsCatalog = false) }
            uiState.showOceanOfPdfBrowser -> viewModel.updateState { it.copy(showOceanOfPdfBrowser = false) }
            uiState.showBookBrowser -> viewModel.updateState { it.copy(showBookBrowser = false) }
            uiState.showUserManual -> viewModel.updateState { it.copy(showUserManual = false) }
            uiState.showAccessibilitySettings -> viewModel.updateState { it.copy(showAccessibilitySettings = false) }
            uiState.showTextEditor -> viewModel.dismissTextEditor()
            uiState.showReadingHistory -> viewModel.updateState { it.copy(showReadingHistory = false) }
            uiState.showDocumentNotes -> viewModel.updateState { it.copy(showDocumentNotes = false) }
            uiState.showAiStudyTools -> viewModel.updateState { it.copy(showAiStudyTools = false) }
            uiState.showReadingLists -> viewModel.updateState { it.copy(showReadingLists = false) }
            uiState.showReaderSettings -> viewModel.updateState { it.copy(showReaderSettings = false) }
            uiState.showVoiceStudio -> viewModel.updateState { it.copy(showVoiceStudio = false) }
            uiState.showNarrationStudio -> viewModel.updateState { it.copy(showNarrationStudio = false) }
            uiState.showPronunciationRules -> viewModel.updateState { it.copy(showPronunciationRules = false) }
            uiState.showSleepTimerDialog -> viewModel.updateState { it.copy(showSleepTimerDialog = false) }
            uiState.showGeneralNotesEditor -> viewModel.updateState { it.copy(showGeneralNotesEditor = false, generalNoteEditorTarget = null) }
            uiState.noteTargetIndexes.isNotEmpty() || uiState.noteTargetIndex != null -> viewModel.dismissSentenceNote()
            uiState.showCanvasView -> viewModel.updateState { it.copy(showCanvasView = false) }
            uiState.showFileBrowser && uiState.fileBrowserBackStack.isNotEmpty() -> viewModel.goUpFileBrowserDirectory()
            uiState.showFileBrowser -> viewModel.updateState { it.copy(showFileBrowser = false) }
            uiState.navStack.isNotEmpty() -> viewModel.navigateBack()
            uiState.activeDocument != null -> viewModel.returnToLibrary()
            else -> viewModel.updateState { it.copy(showExitConfirmationDialog = true) }
        }
    }

    if (uiState.showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.updateState { it.copy(showExitConfirmationDialog = false) } },
            title = { Text("Exit Vern?") },
            text = { Text("Are you sure you want to close Vern?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateState { it.copy(showExitConfirmationDialog = false) }
                        (context as? ComponentActivity)?.finish()
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.updateState { it.copy(showExitConfirmationDialog = false) } }
                ) {
                    Text("Cancel")
                }
            }
        )
    }


}
