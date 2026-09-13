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

class MainActivity : ComponentActivity() {

    internal val viewModel: ReaderViewModel by viewModels()

    var onHardwarePlayPause: (() -> Unit)? = null
    var onHardwareNext: (() -> Unit)? = null
    var onHardwarePrevious: (() -> Unit)? = null
    var onIncomingShare: ((String, Uri?, Boolean) -> Unit)? = null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val widgetAction = intent.getStringExtra(EXTRA_WIDGET_ACTION)
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        val documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID)
        val incomingAction = intent.action
        val isShareToNotes = intent.component?.className?.endsWith("ShareToNotesActivity") == true
        val sharedText = intent.takeIf { incomingAction == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            .orEmpty()
        val sharedUri = when (incomingAction) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                }
            }
            else -> null
        }

        if (widgetAction == ACTION_CONTINUE_READING && !documentId.isNullOrBlank()) {
            PlaybackStateStore.activeDocumentId = documentId
        }

        if (widgetAction != null) {
            viewModel.updateState { state ->
                state.copy(
                    pendingWidgetAction = widgetAction,
                    pendingWidgetDocId = documentId,
                    pendingWidgetNoteId = noteId,
                    pendingImportOnStart = widgetAction == ACTION_IMPORT_DOCUMENTS
                )
            }
        }

        if (sharedText.isNotBlank() || sharedUri != null) {
            onIncomingShare?.invoke(sharedText, sharedUri, isShareToNotes)
        }

        intent.action = null
        intent.data = null
        intent.removeExtra(Intent.EXTRA_TEXT)
        intent.removeExtra(Intent.EXTRA_STREAM)
        intent.removeExtra(EXTRA_WIDGET_ACTION)
        intent.removeExtra(EXTRA_NOTE_ID)
        intent.removeExtra(EXTRA_DOCUMENT_ID)
        updateVeritasWidgets(this)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
        updateVeritasWidgets(this)
    }

    override fun onResume() {
        super.onResume()
        updateVeritasWidgets(this)
    }

    override fun onStop() {
        viewModel.onAppBackgrounded()
        super.onStop()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isPageKey =
            event.keyCode == KeyEvent.KEYCODE_PAGE_DOWN || event.keyCode == KeyEvent.KEYCODE_PAGE_UP
        if (isPageKey && viewModel.uiState.value.activeDocument == null) {
            return super.dispatchKeyEvent(event)
        }

        val isHardwareControlKey = when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_PAGE_DOWN,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_PAGE_UP -> true

            else -> false
        }

        if (isHardwareControlKey) {
            if (event.action == KeyEvent.ACTION_UP) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                    KeyEvent.KEYCODE_MEDIA_PLAY,
                    KeyEvent.KEYCODE_MEDIA_PAUSE,
                    KeyEvent.KEYCODE_HEADSETHOOK -> onHardwarePlayPause?.invoke()

                    KeyEvent.KEYCODE_MEDIA_NEXT,
                    KeyEvent.KEYCODE_PAGE_DOWN -> onHardwareNext?.invoke()

                    KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                    KeyEvent.KEYCODE_PAGE_UP -> onHardwarePrevious?.invoke()
                }
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun maybeShowTtsEngineHint() {
        val prefs = getSharedPreferences("veritas_reader_library", MODE_PRIVATE)
        if (prefs.getBoolean("tts_engine_hint_shown", false)) return

        // 1. Check if Google TTS package is installed on the phone
        val isGoogleTtsInstalled = runCatching {
            packageManager.getPackageInfo("com.google.android.tts", 0)
            true
        }.getOrElse {
            runCatching {
                val tts = android.speech.tts.TextToSpeech(applicationContext, null)
                val engines = tts.engines?.map { it.name } ?: emptyList()
                tts.shutdown()
                engines.any { it.startsWith("com.google.android.tts") }
            }.getOrElse { false }
        }

        // If Google TTS is already installed on the phone, never prompt the user to download it
        if (isGoogleTtsInstalled) {
            prefs.edit().putBoolean("tts_engine_hint_shown", true).apply()
            return
        }

        // 2. Only show the download recommendation dialog if Google TTS is absent from device
        val engine = runCatching {
            android.provider.Settings.Secure.getString(contentResolver, "tts_default_synth")
        }.getOrNull() ?: return
        if (engine.isNotBlank() && engine.startsWith("com.google.android.tts")) return
        prefs.edit().putBoolean("tts_engine_hint_shown", true).apply()
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.tts_hint_title))
            .setMessage(getString(R.string.tts_hint_message))
            .setPositiveButton(getString(R.string.tts_hint_get)) { _, _ ->
                runCatching {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("market://details?id=com.google.android.tts")
                        )
                    )
                }.onFailure {
                    runCatching {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")
                            )
                        )
                    }
                }
            }
            .setNegativeButton(getString(R.string.tts_hint_dismiss), null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Field visibility: capture uncaught crashes locally and offer (user-initiated,
        // nothing automatic) to share last session's report. Then a one-time hint if the
        // device's TTS engine isn't Google's, which degrades highlighting/voice quality.
        CrashReporter.install(this)
        CrashReporter.offerPendingReport(this)
        maybeShowTtsEngineHint()
        // Weekly data-only safety-net backup (worker checks the setting itself).
        AutoBackupWorker.schedule(this)
        // Evening streak-protection nudge (worker checks the setting itself).
        StreakReminderWorker.schedule(this)
        updateVeritasWidgets(this)

        val incomingAction = intent?.action
        val openVoiceStudioOnStart = intent?.getBooleanExtra(EXTRA_OPEN_VOICE_STUDIO, false) == true
        val widgetAction = intent?.getStringExtra(EXTRA_WIDGET_ACTION)
        val noteId = intent?.getStringExtra(EXTRA_NOTE_ID)
        val documentId = intent?.getStringExtra(EXTRA_DOCUMENT_ID)
        val sharedText = intent?.takeIf { incomingAction == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            .orEmpty()

        val sharedUri = intent?.let { incoming ->
            when (incomingAction) {
                Intent.ACTION_VIEW -> incoming.data
                Intent.ACTION_SEND -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        incoming.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        incoming.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                    }
                }

                else -> null
            }
        }

        // Handle continue reading action from widgets
        if (widgetAction == ACTION_CONTINUE_READING && !documentId.isNullOrBlank()) {
            PlaybackStateStore.activeDocumentId = documentId
        }

        if (widgetAction != null) {
            viewModel.updateState { state ->
                state.copy(
                    pendingWidgetAction = widgetAction,
                    pendingWidgetDocId = documentId,
                    pendingWidgetNoteId = noteId,
                    pendingImportOnStart = widgetAction == ACTION_IMPORT_DOCUMENTS
                )
            }
        }

        // Invalidate and clear system intent action and data to prevent loop on config change / restart
        intent?.let {
            it.action = null
            it.data = null
            it.removeExtra(Intent.EXTRA_TEXT)
            it.removeExtra(Intent.EXTRA_STREAM)
            it.removeExtra(EXTRA_WIDGET_ACTION)
            it.removeExtra(EXTRA_NOTE_ID)
            it.removeExtra(EXTRA_DOCUMENT_ID)
        }

        val isShareToNotes = intent?.component?.className?.endsWith("ShareToNotesActivity") == true

        // Synchronously load saved reader settings and initialize theme state before setContent
        // to guarantee zero-flash frame-1 rendering in the user's chosen theme, font, and pack.
        val initialSettings = DocumentRepository(this).loadReaderSettings()
        PlaybackStateStore.restoreFromPersistence(this)
        VeritasThemeState.themeId = initialSettings.themeId
        VeritasThemeState.themePackId = initialSettings.themePackId
        VeritasThemeState.adaptiveCover = initialSettings.adaptiveCover
        VeritasThemeState.uiFontId = initialSettings.uiFontId
        VeritasThemeState.reduceMotion = initialSettings.reduceMotion
        VeritasThemeState.amoledMode = initialSettings.amoledMode

        setContent {
            VeritasTheme {
                VeritasReaderApp(
                    initialSharedText = sharedText,
                    initialSharedUri = sharedUri,
                    isShareToNotes = isShareToNotes,
                    openVoiceStudioOnStart = openVoiceStudioOnStart,
                    widgetAction = widgetAction,
                    noteId = noteId
                )
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_VOICE_STUDIO = "com.veritas.reader.extra.OPEN_VOICE_STUDIO"
        const val EXTRA_WIDGET_ACTION = "com.veritas.reader.extra.WIDGET_ACTION"
        const val EXTRA_NOTE_ID = "com.veritas.reader.extra.NOTE_ID"
        const val EXTRA_DOCUMENT_ID = "com.veritas.reader.extra.DOCUMENT_ID"
        const val ACTION_NEW_NOTE = "new_note"
        const val ACTION_NEW_READING_NOTE = "new_reading_note"
        const val ACTION_SHOW_NOTES = "show_notes"
        const val ACTION_ACTIVE_READING = "active_reading"
        const val ACTION_NEW_STUDY_NOTE = "new_study_note"
        const val ACTION_VOICE_NOTE = "voice_note"
        const val ACTION_EDIT_NOTE = "edit_note"
        const val ACTION_CONTINUE_READING = "continue_reading"
        const val ACTION_SHOW_STUDY_DASHBOARD = "show_study_dashboard"
        const val ACTION_SHOW_READER_TRACKER = "show_reader_tracker"
        const val ACTION_SHOW_FLASHCARDS = "show_flashcards"
        const val ACTION_NEW_CHECKLIST_NOTE = "new_checklist_note"
        const val ACTION_NEW_REMINDER_NOTE = "new_reminder_note"
        const val ACTION_NEW_IMAGE_NOTE = "new_image_note"
        const val ACTION_OPEN_LIBRARY = "open_library"
        const val ACTION_IMPORT_DOCUMENTS = "import_documents"
    }
}

