package com.veritas.reader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.PhotoCamera
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
import com.veritas.reader.ui.screens.ReadingListsDialog
import com.veritas.reader.ui.screens.SettingsHubDialog
import com.veritas.reader.ui.screens.UserManualDialog
import com.veritas.reader.ui.screens.SleepTimerDialog
import com.veritas.reader.ui.screens.UpdateAvailableDialog
import com.veritas.reader.ui.screens.ReleaseNotesDialog
import com.veritas.reader.ui.screens.VoiceStudioDialog
import com.veritas.reader.ReaderMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.veritas.reader.ui.screens.OnboardingQuestChecklist
import com.veritas.reader.ui.screens.OnboardingSpotlightOverlay
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val widgetAction = intent.getStringExtra(EXTRA_WIDGET_ACTION)
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        val documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID)

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

        intent.action = null
        intent.data = null
        intent.removeExtra(Intent.EXTRA_TEXT)
        intent.removeExtra(Intent.EXTRA_STREAM)
        intent.removeExtra(EXTRA_WIDGET_ACTION)
        intent.removeExtra(EXTRA_NOTE_ID)
        intent.removeExtra(EXTRA_DOCUMENT_ID)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
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
        val engine = runCatching {
            android.provider.Settings.Secure.getString(contentResolver, "tts_default_synth")
        }.getOrNull() ?: return
        if (engine.isBlank() || engine.startsWith("com.google.android.tts")) return
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

        setContent {
            VeritasTheme {
                VeritasReaderApp(
                    initialSharedText = sharedText,
                    initialSharedUri = sharedUri,
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
        const val ACTION_NEW_CHECKLIST_NOTE = "new_checklist_note"
        const val ACTION_NEW_REMINDER_NOTE = "new_reminder_note"
        const val ACTION_NEW_IMAGE_NOTE = "new_image_note"
        const val ACTION_OPEN_LIBRARY = "open_library"
        const val ACTION_IMPORT_DOCUMENTS = "import_documents"
    }
}

internal const val MAIN_ACTIVITY_TAG = "MainActivity"

object VeritasThemeState {
    var themeId by mutableStateOf(VeritasThemeCatalog.DEFAULT_ID)
    var themePackId by mutableStateOf(VeritasThemePackCatalog.DEFAULT_ID)
    var activeDocumentId by mutableStateOf<String?>(null)
    var adaptiveCover by mutableStateOf(false)
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Image(
        painter = painterResource(id = R.drawable.veritas_reader_icon),
        contentDescription = "Veritas",
        modifier = modifier
            .size(if (compact) 24.dp else 58.dp)
            .clip(if (compact) MaterialTheme.shapes.extraSmall else MaterialTheme.shapes.small),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun BouncyTextButton(
    label: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncyTextButton"
    )
    TextButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        if (icon != null) {
            BouncyButtonIcon(icon = icon, label = label)
        } else {
            Text(label)
        }
    }
}

// Springy scale-morph when a button's icon changes (e.g. play↔pause) — the same
// motion the home hero card uses, so icon swaps feel consistent app-wide.
@Composable
private fun BouncyButtonIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    AnimatedContent(
        targetState = icon,
        transitionSpec = {
            (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(tween(150)))
                .togetherWith(fadeOut(tween(100)))
        },
        label = "bouncyIconMorph"
    ) { targetIcon ->
        Icon(imageVector = targetIcon, contentDescription = label.ifBlank { null })
    }
}

@Composable
fun BouncyFilledButton(
    label: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncyFilledButton"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = CircleShape,
        contentPadding = if (icon != null) PaddingValues(12.dp) else ButtonDefaults.ContentPadding,
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        if (icon != null) {
            BouncyButtonIcon(icon = icon, label = label)
        } else {
            Text(label)
        }
    }
}

@Composable
fun VeritasWordmark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BrandMark(compact = true)
        Text(
            "eritas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
fun SoftChip(label: String, emphasis: Boolean = false) {
    Box(
        modifier = Modifier
            .background(
                if (emphasis) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                VeritasPackStyle.chipShape()
            )
            .padding(horizontal = 11.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (emphasis) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = VeritasPackStyle.surfaceAlpha()
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SourceBadge(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

enum class VeritasBrowserTab(val label: String, val emoji: String) {
    ALL("ALL", "📁"),
    BOOKS("EPUB", "📕"),
    PDF("PDF", "📄"),
    DOC("DOCX", "📘"),
    HTML("WEB", "🌐"),
    TXT("TXT", "📝"),
    OCR("OCR", "📷");

    val icon: androidx.compose.ui.graphics.vector.ImageVector
        get() = when (this) {
            ALL -> Icons.Outlined.Folder
            BOOKS -> Icons.Outlined.Book
            PDF -> Icons.Outlined.PictureAsPdf
            DOC -> Icons.Outlined.Description
            HTML -> Icons.Outlined.Language
            TXT -> Icons.Outlined.Article
            OCR -> Icons.Outlined.PhotoCamera
        }
}

enum class VeritasBrowserSort(val label: String) {
    NAME("File name"),
    DATE("Date/time"),
    SIZE("Size"),
    PATH("Path")
}

data class VeritasBrowserRoot(
    val uri: Uri,
    val label: String
)

data class VeritasBrowserLocation(
    val rootLabel: String,
    val relativePath: String = "",
    val filePath: String? = null,
    val rootUri: Uri? = null,
    val documentId: String? = null
) {
    val label: String
        get() = if (relativePath.isBlank()) rootLabel else "$rootLabel/$relativePath"
}

data class VeritasBrowserFile(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val modifiedAt: Long,
    val rootLabel: String,
    val relativePath: String,
    val type: VeritasBrowserTab = VeritasBrowserTab.ALL,
    val isDirectory: Boolean = false,
    val isSupported: Boolean = true,
    val targetLocation: VeritasBrowserLocation? = null
)

data class VeritasFileBrowserScanResult(
    val files: List<VeritasBrowserFile>,
    val location: VeritasBrowserLocation? = null,
    val diagnostics: List<String> = emptyList()
)

object VeritasFileBrowserScanner {
    private val childProjection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED
    )

    fun persistedRoots(context: Context): List<VeritasBrowserRoot> {
        return context.contentResolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .map { permission ->
                VeritasBrowserRoot(
                    uri = permission.uri,
                    label = displayNameForRoot(context, permission.uri)
                )
            }
            .distinctBy { it.uri }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    fun initialLocation(
        context: Context,
        roots: List<VeritasBrowserRoot>,
        includeAllFilesAccess: Boolean
    ): VeritasBrowserLocation? {
        if (includeAllFilesAccess) {
            val storageRoot = Environment.getExternalStorageDirectory()
            return VeritasBrowserLocation(
                rootLabel = "Phone storage",
                filePath = storageRoot.absolutePath
            )
        }
        val root = roots.firstOrNull() ?: return null
        return VeritasBrowserLocation(
            rootLabel = root.label,
            rootUri = root.uri,
            documentId = runCatching { DocumentsContract.getTreeDocumentId(root.uri) }.getOrNull()
        )
    }

    fun scan(
        context: Context,
        roots: List<VeritasBrowserRoot>,
        includeAllFilesAccess: Boolean = false,
        location: VeritasBrowserLocation? = null
    ): VeritasFileBrowserScanResult {
        val diagnostics = mutableListOf<String>()
        val activeLocation = location ?: initialLocation(context, roots, includeAllFilesAccess)
        if (activeLocation == null) {
            return VeritasFileBrowserScanResult(
                emptyList(),
                null,
                listOf("Grant All Files access or approve a folder to browse files.")
            )
        }
        val entries = when {
            activeLocation.filePath != null -> listFileDirectory(activeLocation, diagnostics)
            activeLocation.rootUri != null && activeLocation.documentId != null -> {
                val root = roots.firstOrNull { it.uri == activeLocation.rootUri }
                if (root == null) {
                    diagnostics.add("This approved folder is no longer available. Add it again from Folders to scan.")
                    emptyList()
                } else {
                    listSafDirectory(
                        context = context,
                        root = root,
                        location = activeLocation,
                        diagnostics = diagnostics
                    )
                }
            }

            else -> emptyList()
        }
        return VeritasFileBrowserScanResult(entries, activeLocation, diagnostics)
    }

    private fun listFileDirectory(
        location: VeritasBrowserLocation,
        diagnostics: MutableList<String>
    ): List<VeritasBrowserFile> {
        val storageRoot = Environment.getExternalStorageDirectory()
        val current = location.filePath?.let(::File) ?: storageRoot
        val safeCurrent =
            if (current.absolutePath.startsWith(storageRoot.absolutePath)) current else storageRoot
        if (!safeCurrent.exists()) {
            diagnostics.add("${location.label} no longer exists.")
            return emptyList()
        }
        val children =
            runCatching { safeCurrent.listFiles()?.toList().orEmpty() }.getOrElse { error ->
                diagnostics.add("Android blocked access to ${location.label}: ${error.message ?: "folder is protected"}.")
                emptyList()
            }
        if (children.isEmpty() && safeCurrent.isDirectory && !safeCurrent.canRead()) {
            diagnostics.add("Android protects this folder. Shared storage can be browsed, but private system/app folders may remain unavailable.")
        }
        val currentEntries = children.sortedWith(compareBy<File> { !it.isDirectory }.thenBy {
            it.name.lowercase(Locale.getDefault())
        })
            .mapNotNull { child ->
                val nameLower = child.name.lowercase(Locale.US)
                if (child.name.startsWith(".") || child.name == "..") return@mapNotNull null
                if (!child.isDirectory) {
                    val isBinaryOrSystem = nameLower.endsWith(".bin") ||
                            nameLower.endsWith(".apk") ||
                            nameLower.endsWith(".exe") ||
                            nameLower.endsWith(".so") ||
                            nameLower.endsWith(".class") ||
                            nameLower.endsWith(".dex") ||
                            nameLower.endsWith(".tmp") ||
                            nameLower.endsWith(".temp") ||
                            nameLower.endsWith(".db") ||
                            nameLower.endsWith(".sqlite") ||
                            nameLower.endsWith(".sys") ||
                            nameLower.endsWith(".dll") ||
                            nameLower.endsWith(".log") ||
                            nameLower.endsWith(".dat")
                    if (isBinaryOrSystem) return@mapNotNull null
                }
                val relativePath =
                    child.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/')
                if (child.isDirectory) {
                    VeritasBrowserFile(
                        uri = Uri.fromFile(child),
                        name = child.name.ifBlank { "Folder" },
                        mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                        sizeBytes = 0L,
                        modifiedAt = child.lastModified(),
                        rootLabel = location.rootLabel,
                        relativePath = relativePath,
                        isDirectory = true,
                        isSupported = child.canRead(),
                        targetLocation = if (child.canRead()) {
                            VeritasBrowserLocation(
                                rootLabel = location.rootLabel,
                                relativePath = relativePath,
                                filePath = child.absolutePath
                            )
                        } else {
                            null
                        }
                    )
                } else {
                    val type = fileTypeFor(child.name, "")
                    VeritasBrowserFile(
                        uri = Uri.fromFile(child),
                        name = child.name.ifBlank { "Untitled file" },
                        mimeType = mimeTypeForFileName(child.name),
                        sizeBytes = child.length(),
                        modifiedAt = child.lastModified(),
                        rootLabel = location.rootLabel,
                        relativePath = relativePath,
                        type = type ?: VeritasBrowserTab.ALL,
                        isSupported = type != null
                    )
                }
            }
        if (safeCurrent.absolutePath == storageRoot.absolutePath) {
            val documentEntries = collectSupportedDocumentFiles(
                storageRoot = storageRoot,
                current = storageRoot,
                diagnostics = diagnostics
            )
            return (documentEntries + currentEntries)
                .distinctBy { it.uri.toString() }
                .sortedWith(compareBy<VeritasBrowserFile> { it.isDirectory }.thenByDescending { it.modifiedAt })
        }
        return currentEntries
    }

    private fun collectSupportedDocumentFiles(
        storageRoot: File,
        current: File,
        diagnostics: MutableList<String>,
        depth: Int = 0,
        results: MutableList<VeritasBrowserFile> = mutableListOf()
    ): List<VeritasBrowserFile> {
        if (depth > 6 || results.size >= 300 || shouldSkipRecursiveDirectory(
                current,
                storageRoot
            )
        ) return results
        val children = runCatching { current.listFiles()?.toList().orEmpty() }.getOrElse { error ->
            if (depth <= 1) diagnostics.add("Some protected folders could not be indexed: ${error.message ?: "access denied"}.")
            emptyList()
        }
        children.forEach { child ->
            if (results.size >= 300) return@forEach
            if (child.isDirectory) {
                collectSupportedDocumentFiles(storageRoot, child, diagnostics, depth + 1, results)
            } else {
                val type = fileTypeFor(child.name, "")
                if (type != null && type != VeritasBrowserTab.OCR) {
                    val relativePath =
                        child.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/')
                    results.add(
                        VeritasBrowserFile(
                            uri = Uri.fromFile(child),
                            name = child.name.ifBlank { "Untitled file" },
                            mimeType = mimeTypeForFileName(child.name),
                            sizeBytes = child.length(),
                            modifiedAt = child.lastModified(),
                            rootLabel = "Phone storage",
                            relativePath = relativePath,
                            type = type,
                            isSupported = true
                        )
                    )
                }
            }
        }
        return results
    }

    private fun shouldSkipRecursiveDirectory(folder: File, storageRoot: File): Boolean {
        val relative = folder.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/')
            .lowercase(Locale.getDefault())
        if (relative.isBlank() || relative == ".") return false
        return relative.startsWith("android/data") ||
                relative.startsWith("android/obb") ||
                relative.contains("/cache") ||
                folder.name.startsWith(".")
    }

    private fun listSafDirectory(
        context: Context,
        root: VeritasBrowserRoot,
        location: VeritasBrowserLocation,
        diagnostics: MutableList<String>
    ): List<VeritasBrowserFile> {
        val documentId = location.documentId ?: return emptyList()
        val entries = mutableListOf<VeritasBrowserFile>()
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(root.uri, documentId)
        runCatching {
            context.contentResolver.query(childrenUri, childProjection, null, null, null)
                ?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val childId =
                            cursor.stringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                                ?: continue
                        val name =
                            cursor.stringValue(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                                .orEmpty()
                        if (name.startsWith(".")) continue
                        val mimeType =
                            cursor.stringValue(DocumentsContract.Document.COLUMN_MIME_TYPE)
                                .orEmpty()
                        val isDir = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
                        if (!isDir) {
                            val nameLower = name.lowercase(Locale.US)
                            val isBinaryOrSystem = nameLower.endsWith(".bin") ||
                                    nameLower.endsWith(".apk") ||
                                    nameLower.endsWith(".exe") ||
                                    nameLower.endsWith(".so") ||
                                    nameLower.endsWith(".class") ||
                                    nameLower.endsWith(".dex") ||
                                    nameLower.endsWith(".tmp") ||
                                    nameLower.endsWith(".temp") ||
                                    nameLower.endsWith(".db") ||
                                    nameLower.endsWith(".sqlite") ||
                                    nameLower.endsWith(".sys") ||
                                    nameLower.endsWith(".dll") ||
                                    nameLower.endsWith(".log") ||
                                    nameLower.endsWith(".dat")
                            if (isBinaryOrSystem) continue
                        }
                        val size = cursor.longValue(DocumentsContract.Document.COLUMN_SIZE)
                        val modified =
                            cursor.longValue(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                        val childPath =
                            if (location.relativePath.isBlank()) name else "${location.relativePath}/$name"
                        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                            entries.add(
                                VeritasBrowserFile(
                                    uri = DocumentsContract.buildDocumentUriUsingTree(
                                        root.uri,
                                        childId
                                    ),
                                    name = name.ifBlank { "Folder" },
                                    mimeType = mimeType,
                                    sizeBytes = 0L,
                                    modifiedAt = modified,
                                    rootLabel = root.label,
                                    relativePath = childPath,
                                    isDirectory = true,
                                    targetLocation = VeritasBrowserLocation(
                                        rootLabel = root.label,
                                        relativePath = childPath,
                                        rootUri = root.uri,
                                        documentId = childId
                                    )
                                )
                            )
                        } else {
                            val type = fileTypeFor(name, mimeType)
                            entries.add(
                                VeritasBrowserFile(
                                    uri = DocumentsContract.buildDocumentUriUsingTree(
                                        root.uri,
                                        childId
                                    ),
                                    name = name.ifBlank { "Untitled file" },
                                    mimeType = mimeType,
                                    sizeBytes = size,
                                    modifiedAt = modified,
                                    rootLabel = root.label,
                                    relativePath = childPath,
                                    type = type ?: VeritasBrowserTab.ALL,
                                    isSupported = type != null
                                )
                            )
                        }
                    }
                }
        }.onFailure { error ->
            diagnostics.add("Android blocked access to ${location.label}: ${error.message ?: "folder is protected"}.")
        }
        return entries.sortedWith(compareBy<VeritasBrowserFile> { !it.isDirectory }.thenBy {
            it.name.lowercase(
                Locale.getDefault()
            )
        })
    }

    private fun fileTypeFor(name: String, mimeType: String): VeritasBrowserTab? {
        val lowerName = name.lowercase(Locale.getDefault())
        val lowerMime = mimeType.lowercase(Locale.getDefault())
        return when {
            lowerMime.contains("pdf") || lowerName.endsWith(".pdf") -> VeritasBrowserTab.PDF
            lowerMime.contains("wordprocessingml") || lowerName.endsWith(".docx") -> VeritasBrowserTab.DOC
            lowerMime.contains("epub") || lowerName.endsWith(".epub") -> VeritasBrowserTab.BOOKS
            lowerMime.contains("html") || lowerName.endsWith(".html") || lowerName.endsWith(".htm") -> VeritasBrowserTab.HTML
            lowerMime.startsWith("text/") || lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(
                ".csv"
            ) -> VeritasBrowserTab.TXT

            lowerMime.startsWith("image/") || lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(
                ".jpeg"
            ) || lowerName.endsWith(".webp") || lowerName.endsWith(".bmp") || lowerName.endsWith(".tif") || lowerName.endsWith(
                ".tiff"
            ) -> VeritasBrowserTab.OCR

            else -> null
        }
    }

    private fun mimeTypeForFileName(name: String): String {
        val lowerName = name.lowercase(Locale.getDefault())
        return when {
            lowerName.endsWith(".pdf") -> "application/pdf"
            lowerName.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            lowerName.endsWith(".epub") -> "application/epub+zip"
            lowerName.endsWith(".html") || lowerName.endsWith(".htm") -> "text/html"
            lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".csv") -> "text/plain"
            lowerName.endsWith(".png") -> "image/png"
            lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") -> "image/jpeg"
            lowerName.endsWith(".webp") -> "image/webp"
            else -> "application/octet-stream"
        }
    }

    private fun displayNameForRoot(context: Context, uri: Uri): String {
        return runCatching {
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )
            context.contentResolver.query(
                documentUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.stringValue(DocumentsContract.Document.COLUMN_DISPLAY_NAME) else null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Approved folder"
    }

    private fun Cursor.stringValue(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.longValue(columnName: String): Long {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getLong(index) else 0L
    }
}

internal fun readableImportMimeTypes(): Array<String> = arrayOf(
    "text/plain",
    "text/*",
    "text/html",
    "application/pdf",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/epub+zip",
    "application/octet-stream",
    "image/*"
)

@Composable
internal fun VeritasReaderApp(
    initialSharedText: String,
    initialSharedUri: Uri?,
    openVoiceStudioOnStart: Boolean,
    widgetAction: String? = null,
    noteId: String? = null
) {
    val context = LocalContext.current
    val viewModel: ReaderViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val documentRepository = remember(context) { DocumentRepository(context.applicationContext) }
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
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(pending.second.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("Could not open the selected save location.")
            }
            viewModel.updateState {
                it.copy(
                    pendingTextDownload = null,
                    exportMessage = result.fold(
                        onSuccess = { "Edited text saved to phone." },
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

    val activeDocId = uiState.activeDocument?.id
    LaunchedEffect(
        uiState.readerSettings.themeId,
        uiState.readerSettings.themePackId,
        uiState.readerSettings.adaptiveCover,
        activeDocId
    ) {
        VeritasThemeState.themeId = uiState.readerSettings.themeId
        VeritasThemeState.themePackId = uiState.readerSettings.themePackId
        VeritasThemeState.adaptiveCover = uiState.readerSettings.adaptiveCover
        VeritasThemeState.activeDocumentId = activeDocId
    }

    LaunchedEffect(Unit) {
        if (!uiState.handledInitialShare) {
            if (initialSharedText.isNotBlank()) {
                viewModel.updateState { it.copy(draftText = initialSharedText) }
            }
            if (initialSharedUri != null) {
                viewModel.prepareImport(initialSharedUri)
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
                    viewModel.returnToLibrary()
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
                    viewModel.returnToLibrary()
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
            uiState.showTextEditor -> viewModel.dismissTextEditor()
            uiState.showFileBrowser && uiState.fileBrowserBackStack.isNotEmpty() -> viewModel.goUpFileBrowserDirectory()
            uiState.navStack.isNotEmpty() -> viewModel.navigateBack()
            uiState.activeDocument != null -> viewModel.returnToLibrary()
            else -> (context as? ComponentActivity)?.finish()
        }
    }

    VeritasTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            @OptIn(ExperimentalSharedTransitionApi::class)
            SharedTransitionLayout {
                AnimatedContent(
                    targetState = uiState.activeDocument,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    contentKey = { it?.id },
                    label = "reader_transition"
                ) { activeDoc ->
                    if (activeDoc == null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LibraryScreen(
                                uiState = uiState,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@AnimatedContent,
                    onDraftTextChange = { text -> viewModel.updateState { it.copy(draftText = text) } },
                    widgetAction = uiState.pendingWidgetAction ?: widgetAction,
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
                    onDeleteGeneralNote = viewModel::deleteGeneralNote,
                    onGradeFlashcard = viewModel::gradeFlashcard,
                    onDeleteFlashcard = viewModel::deleteFlashcard
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
                            // Keep the card clear of the Add FAB anchored at the bottom end.
                            .fillMaxWidth(0.65f)
                            .padding(bottom = 90.dp)
                            .onGloballyPositioned { OnboardingController.updateBounds("quest_checklist", it) }
                    )
                }
            }
        } else {
            val activeDocument = activeDoc ?: return@AnimatedContent
                val activeMetadata = activeDocument.id?.let { activeId ->
                    uiState.documents.firstOrNull { it.id == activeId }
                }
                if (uiState.showCanvasView && activeMetadata != null) {
                    ActualDocumentView(
                        document = activeMetadata,
                        repository = documentRepository,
                        readingProgress = if (activeDocument.chunks.size <= 1) {
                            0f
                        } else {
                            PlaybackStateStore.currentIndex.toFloat() / activeDocument.chunks.lastIndex.toFloat()
                                .coerceAtLeast(1f)
                        },
                        isPlaying = PlaybackStateStore.isPlaying,
                        statusMessage = PlaybackStateStore.statusMessage,
                        queueCount = PlaybackStateStore.queueCount,
                        rate = PlaybackStateStore.rate,
                        pitch = PlaybackStateStore.pitch,
                        fontSizeSp = uiState.readerSettings.fontSizeSp,
                        canGoPrevious = PlaybackStateStore.currentIndex > 0,
                        canGoNext = PlaybackStateStore.currentIndex < activeDocument.chunks.lastIndex || PlaybackStateStore.queueCount > 0,
                        onPrevious = {
                            viewModel.moveTo(
                                (PlaybackStateStore.currentIndex - 1).coerceAtLeast(0),
                                false
                            )
                        },
                        onPlayPause = { viewModel.playOrPause() },
                        onNext = {
                            viewModel.moveTo(
                                (PlaybackStateStore.currentIndex + 1).coerceAtMost(
                                    activeDocument.chunks.size - 1
                                ),
                                false
                            )
                        },
                        onPageChanged = { pageIndex, pageCount ->
                            // Eyes don't move the voice: while TTS is playing, browsing pages
                            // in Original view must never jump playback (moveTo notifies the
                            // service mid-utterance and it re-reads from the viewed page).
                            if (!PlaybackStateStore.isPlaying) {
                                val lastSentenceIndex = activeDocument.chunks.lastIndex.coerceAtLeast(0)
                                val pageDenominator = (pageCount - 1).coerceAtLeast(1)
                                val targetIndex =
                                    ((pageIndex.toFloat() / pageDenominator.toFloat()) * lastSentenceIndex)
                                        .roundToInt()
                                        .coerceIn(0, lastSentenceIndex)
                                viewModel.moveTo(targetIndex, autoPlay = false)
                            }
                        },
                        onOpenExternal = {
                            openOriginalDocument(
                                context,
                                documentRepository,
                                activeMetadata
                            )
                        },
                        onRateChange = {
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    preferredRate = it
                                )
                            )
                        },
                        onPitchChange = {
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    preferredPitch = it
                                )
                            )
                        },
                        onFontSizeChange = {
                            viewModel.saveReaderSettings(
                                uiState.readerSettings.copy(
                                    fontSizeSp = it
                                )
                            )
                        },
                        onOpenVoiceStudio = { viewModel.updateState { it.copy(showVoiceStudio = true) } },
                        voices = uiState.ttsVoices,
                        voiceSettings = uiState.voiceSettings,
                        onVoiceSelected = { voice ->
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    voiceName = voice.name,
                                    voiceLabel = voice.name,
                                    localeTag = voice.localeTag
                                )
                            )
                        },
                        onClose = { viewModel.updateState { it.copy(showCanvasView = false) } }
                    )
                } else {
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
                            activeDocumentReadingListCount = activeDocument.id?.let { activeId ->
                                uiState.readingListCatalog.listsContaining(activeId)
                                    .count { !it.archived }
                            } ?: 0,
                            voices = uiState.ttsVoices,
                            readerMode = PlaybackStateStore.readerMode
                        ),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
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
                                (PlaybackStateStore.currentIndex - 1).coerceAtLeast(
                                    0
                                ), false
                            )
                        },
                        onNext = {
                            viewModel.moveTo(
                                (PlaybackStateStore.currentIndex + 1).coerceAtMost(
                                    activeDocument.chunks.size - 1
                                ), false
                            )
                        },
                        onRateChange = {
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    preferredRate = it
                                )
                            )
                        },
                        onPitchChange = {
                            viewModel.saveVoiceSettings(
                                uiState.voiceSettings.copy(
                                    preferredPitch = it
                                )
                            )
                        },
                        onFontSizeChange = {
                            viewModel.saveReaderSettings(
                                uiState.readerSettings.copy(
                                    fontSizeSp = it
                                )
                            )
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
                        onOpenPronunciationRules = {
                            viewModel.updateState {
                                it.copy(
                                    showPronunciationRules = true
                                )
                            }
                        },
                        onOpenVoiceStudio = { viewModel.updateState { it.copy(showVoiceStudio = true) } },
                        onOpenNarrationStudio = {
                            viewModel.updateState {
                                it.copy(
                                    showNarrationStudio = true
                                )
                            }
                        },
                        onOpenDocumentNotes = { viewModel.openDocumentNotes() },
                        onOpenCanvas = {
                            val metadata = activeMetadata
                            val file = metadata?.let { documentRepository.originalFile(it) }
                            val isPdfCanvas = metadata != null && (
                                    metadata.originalMimeType.contains("pdf", ignoreCase = true) ||
                                            metadata.originalFileName.endsWith(".pdf", ignoreCase = true) ||
                                            (file != null && file.exists() && runCatching {
                                                file.inputStream().use { input ->
                                                    val bytes = ByteArray(4)
                                                    val read = input.read(bytes)
                                                    read == 4 && bytes[0] == '%'.code.toByte() && bytes[1] == 'P'.code.toByte() && bytes[2] == 'D'.code.toByte() && bytes[3] == 'F'.code.toByte()
                                                }
                                            }.getOrDefault(false))
                                    )
                            if (metadata != null && isPdfCanvas) {
                                context.startActivity(
                                    VeritasPdfViewerActivity.intent(
                                        context,
                                        metadata.id
                                    )
                                )
                            } else {
                                viewModel.updateState { it.copy(showCanvasView = true) }
                            }
                        },
                        onOpenStudyTools = { viewModel.updateState { it.copy(showAiStudyTools = true) } },
                        onOpenTranslationTools = {
                            viewModel.updateState {
                                it.copy(
                                    showTranslationTools = true
                                )
                            }
                        },
                        onOpenSleepTimer = { viewModel.updateState { it.copy(showSleepTimerDialog = true) } },
                        onOpenReadingLists = { viewModel.updateState { it.copy(showReadingLists = true) } },
                        onOpenReadingHistory = { viewModel.updateState { it.copy(showReadingHistory = true) } },
                        onAskCurrentSection = {
                            uiState.activeDocument?.let { document ->
                                shareToAi(
                                    context = context,
                                    document = document,
                                    scope = ShareScope.CURRENT_SECTION,
                                    selection = null,
                                    customPageRange = null,
                                    settings = uiState.askAiSettings
                                )
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
                        onCopySelection = { copyTextToClipboard(context, "Veritas selection", it) },
                        onShareSelection = { sharePlainText(context, "Veritas selection", it) },
                        onGoogleSelection = {
                            viewModel.appendVocabularyWord(it, "Looked up definition / web references.")
                            openGoogleSearch(context, it)
                        },
                        onTranslateSelection = {
                            viewModel.appendVocabularyWord(it, "Looked up translation.")
                            openGoogleTranslate(context, it)
                        },
                        onAskAiSelection = {
                            viewModel.appendVocabularyWord(it, "Asked AI for explanation.")
                            askAiWithSelection(
                                context,
                                uiState.askAiSettings,
                                it
                            )
                        },
                        onEditSpeechSelection = { selection ->
                            viewModel.updateState {
                                it.copy(
                                    showPronunciationRules = true,
                                    newRuleFind = selection.replace(Regex("\\s+"), " ").trim()
                                        .take(120),
                                    newRuleReplaceWith = ""
                                )
                            }
                        },
                        onReadSelection = { sendSelectionSpeechIntent(context, it) },
                        onEditExtractedSelection = { selection ->
                            viewModel.openSelectionTextEditor(
                                selection.sentenceIndexes
                            )
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
                        onAddBookmarkGroup = { indexes, colorHex ->
                            viewModel.addBookmarkGroup(indexes, colorHex)
                        },
                        onShareToAi = { scope, selection, range, noPrompt ->
                            uiState.activeDocument?.let { doc ->
                                val mappedSelection = selection?.let {
                                    ReaderTextSelection(
                                        partIndex = it.partIndex,
                                        start = it.start,
                                        endExclusive = it.endExclusive,
                                        text = it.text,
                                        sentenceIndexes = it.sentenceIndexes
                                    )
                                }
                                shareToAi(context, doc, scope, mappedSelection, range, uiState.askAiSettings, noPrompt)
                            }
                        },
                        showShareToAi = false,
                        onDismissShareToAi = {
                            viewModel.updateState { it.copy(showAiStudyTools = false) }
                        },
                        onReaderModeChange = { PlaybackStateStore.readerMode = it }
                    )
                }
            }
        }
    }
}

        if (uiState.recordMode || uiState.recordAwaitingDecision) {
            FloatingRecordOverlay(
                inProgress = uiState.exportInProgress,
                fileReady = uiState.exportedAudioFile != null,
                awaitingDecision = uiState.recordAwaitingDecision,
                elapsedSeconds = uiState.recordElapsedSeconds,
                onStopRecording = { viewModel.stopRecordSoundFile() },
                onSave = { viewModel.saveRecordedSoundFile() },
                onDiscard = { viewModel.discardRecordedSoundFile() }
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

        var userManualTipTitle by remember { mutableStateOf("") }
        var userManualTipText by remember { mutableStateOf<String?>(null) }

        if (userManualTipText != null) {
            AlertDialog(
                onDismissRequest = { userManualTipText = null },
                title = {
                    Text(
                        text = userManualTipTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = userManualTipText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { userManualTipText = null }
                    ) {
                        Text("Got it")
                    }
                }
            )
        }

        if (uiState.showUserManual) {
            UserManualDialog(
                onDismiss = { viewModel.updateState { it.copy(showUserManual = false) } },
                onNavigateToSetting = { setting ->
                    when (setting) {
                        "reader_settings" -> viewModel.updateState { it.copy(showReaderSettings = true) }
                        "voice_studio" -> viewModel.updateState { it.copy(showVoiceStudio = true) }
                        "narration_studio" -> viewModel.updateState { it.copy(showNarrationStudio = true) }
                        "pronunciation" -> viewModel.updateState { it.copy(showPronunciationRules = true) }
                        "sleep_timer" -> viewModel.updateState { it.copy(showSleepTimerDialog = true) }
                        "pdf_tools" -> viewModel.updateState { it.copy(showPdfImportTools = true) }
                        "history" -> viewModel.updateState { it.copy(showReadingHistory = true) }
                        "reading_lists" -> viewModel.updateState { it.copy(showReadingLists = true) }
                        "sync_center" -> viewModel.updateState { it.copy(showSyncCenter = true) }
                        "ai_center" -> viewModel.updateState { it.copy(showAiCenter = true) }
                        "ask_ai" -> viewModel.updateState { it.copy(showAskAiSettings = true) }
                        "file_browser" -> viewModel.openFileBrowser()
                        "backup_tools" -> viewModel.updateState { it.copy(showBackupTools = true) }
                        "library_options" -> {
                            userManualTipTitle = "Document Actions"
                            userManualTipText = "Tap the three-dot overflow button on any book card in your library to edit metadata, rename files, assign categories, add to custom lists, reset progress, or delete files from storage."
                        }
                        "bulk_edit" -> {
                            userManualTipTitle = "Batch Organization"
                            userManualTipText = "Long-press any document card in your Library to enter multi-select mode. You can then tap other cards to select them and perform bulk actions like category assignment or batch deletion from the top toolbar."
                        }
                        "file_browser_filters" -> {
                            userManualTipTitle = "Browser Sorting & Filters"
                            userManualTipText = "Tap the options menu (three dots) at the top-right of the integrated File Browser to change sorting (name, date, size), filter by file type, or toggle hidden files and folders."
                        }
                        "text_selection" -> {
                            userManualTipTitle = "Interactive Text Selection"
                            userManualTipText = "Double-tap or long-press on any word or sentence in the reader screen to highlight it. Use the selection handles to expand the text range, and access options like copying, notes, dictionary definitions, and TTS narration controls."
                        }
                        "reader_tools" -> {
                            userManualTipTitle = "Reader Tools Menu"
                            userManualTipText = "Tap the top-right tool menu button (three dots) inside the Reader Screen to access bookmarks, text search inside the book, theme settings, and notes export actions."
                        }
                        "study_general" -> {
                            userManualTipTitle = "Study Hub"
                            userManualTipText = "Open the Study screen from the main navigation bar to browse your accumulated vocabularies, highlights, custom bookmarks, and reading history logs in one consolidated workspace."
                        }
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
                        it.copy(
                            ttsEngines = VoiceManager.loadInstalledEngines(
                                context
                            )
                        )
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
                        uiState.readerSettings.copy(
                            fontSizeSp = size
                        )
                    )
                },
                onSpacingChange = { spacing ->
                    viewModel.saveReaderSettings(
                        uiState.readerSettings.copy(
                            sectionSpacingDp = spacing
                        )
                    )
                },
                onThemeChange = { themeId ->
                    viewModel.saveReaderSettings(
                        uiState.readerSettings.copy(
                            themeId = themeId
                        )
                    )
                },
                onThemePackChange = { packId ->
                    viewModel.saveReaderSettings(
                        uiState.readerSettings.copy(
                            themePackId = packId
                        )
                    )
                },
                onToggleSectionNumbers = {
                    viewModel.saveReaderSettings(
                        uiState.readerSettings.copy(
                            showSectionNumbers = !uiState.readerSettings.showSectionNumbers
                        )
                    )
                },
                onToggleAutoPlayQueue = {
                    viewModel.saveReaderSettings(
                        uiState.readerSettings.copy(
                            autoPlayQueue = !uiState.readerSettings.autoPlayQueue
                        )
                    )
                },
                onToggleAdaptiveCover = {
                    viewModel.saveReaderSettings(
                        uiState.readerSettings.copy(
                            adaptiveCover = !uiState.readerSettings.adaptiveCover
                        )
                    )
                }
            )
        }

        if (uiState.showPronunciationRules) {
            PronunciationRulesDialog(
                rules = uiState.pronunciationRules,
                newFind = uiState.newRuleFind,
                newReplaceWith = uiState.newRuleReplaceWith,
                onNewFindChange = { value ->
                    viewModel.updateState {
                        it.copy(
                            newRuleFind = value.take(
                                120
                            )
                        )
                    }
                },
                onNewReplaceChange = { value ->
                    viewModel.updateState {
                        it.copy(
                            newRuleReplaceWith = value.take(
                                120
                            )
                        )
                    }
                },
                onAddRule = { viewModel.addPronunciationRule() },
                onToggleRule = { rule -> viewModel.togglePronunciationRule(rule) },
                onRemoveRule = { rule -> viewModel.removePronunciationRule(rule) },
                onDismiss = { viewModel.updateState { it.copy(showPronunciationRules = false) } }
            )
        }

        if (uiState.showNarrationStudio) {
            NarrationStudioDialog(
                settings = uiState.narrationSettings,
                sampleText = uiState.activeDocument?.chunks?.getOrNull(PlaybackStateStore.currentIndex)
                    .orEmpty(),
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
                            noPrompt = true
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

        if ((uiState.exportInProgress || uiState.exportMessage != null || uiState.exportedAudioFile != null) && !uiState.recordMode && !uiState.recordAwaitingDecision) {
            ExportAudioStatusDialog(
                inProgress = uiState.exportInProgress,
                message = uiState.exportMessage,
                file = uiState.exportedAudioFile,
                onShare = { file -> viewModel.shareExportedAudio(file) },
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
            UpdateAvailableDialog(
                versionName = uiState.updateVersionName,
                changelog = uiState.updateChangelog,
                isDownloading = uiState.isDownloadingUpdate,
                downloadProgress = uiState.updateDownloadProgress,
                downloadError = uiState.updateDownloadError,
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
                onSave = { title, content, color, pinned, isChecklist, imageUrl, audioUrl, reminderAt, closeEditor ->
                    viewModel.saveGeneralNote(title, content, color, pinned, isChecklist, imageUrl, audioUrl, reminderAt, closeEditor)
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

        // --- Extracted dialogs ---
        if (uiState.showPdfImportTools) {
            PdfImportOptionsDialog(
                options = uiState.advancedPdfOptions,
                textOptions = uiState.textImportOptions,
                onOptionsChange = { opt -> viewModel.updateState { it.copy(advancedPdfOptions = opt) } },
                onTextOptionsChange = { opt -> viewModel.updateState { it.copy(textImportOptions = opt) } },
                onPickPdf = {
                    viewModel.updateState { it.copy(showPdfImportTools = false) }
                    viewModel.openFileBrowser()
                },
                onDismiss = { viewModel.updateState { it.copy(showPdfImportTools = false) } }
            )
        }

        if (uiState.showFileBrowser && uiState.pendingImport == null) {
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.refreshFileBrowser()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            FileBrowserDialog(
                roots = uiState.fileBrowserRoots,
                entries = uiState.fileBrowserFiles,
                location = uiState.fileBrowserLocation,
                canGoUp = uiState.fileBrowserBackStack.isNotEmpty(),
                scanning = uiState.fileBrowserScanning,
                message = uiState.fileBrowserMessage,
                allFilesAccessGranted = uiState.fileBrowserAllFilesGranted,
                importing = uiState.importInProgress,
                importingName = uiState.importSourceName,
                onDismiss = { viewModel.updateState { it.copy(showFileBrowser = false) } },
                onPickFolder = { folderPickerLauncher.launch(null) },
                onRequestAllFilesAccess = {
                    openAllFilesAccessSettings(context)
                    viewModel.refreshFileBrowserAccessState()
                },
                onOpenFilePicker = { importFileLauncher.launch(readableImportMimeTypes()) },
                onRefresh = { viewModel.refreshFileBrowser() },
                onGoUp = { viewModel.goUpFileBrowserDirectory() },
                onEnterDirectory = { viewModel.enterFileBrowserDirectory(it) },
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
                onDismiss = { viewModel.updateState { it.copy(showReadingHistory = false) } },
                onOpenDocument = { doc ->
                    viewModel.updateState { it.copy(showReadingHistory = false) }; viewModel.openSavedDocument(
                    doc
                )
                },
                onClearHistory = { viewModel.clearReadingHistory() }
            )
        }

        if (uiState.showDocumentNotes) {
            uiState.activeDocument?.let { document ->
                DocumentNotesDialog(
                    document = document,
                    annotations = uiState.annotations,
                    documentNote = uiState.documentNoteDraft,
                    currentIndex = PlaybackStateStore.currentIndex.coerceIn(
                        0,
                        document.chunks.lastIndex.coerceAtLeast(0)
                    ),
                    onDocumentNoteChange = { draft ->
                        viewModel.updateState {
                            it.copy(
                                documentNoteDraft = draft
                            )
                        }
                    },
                    onSaveDocumentNote = { viewModel.saveDocumentNoteDraft() },
                    onAddCurrentNote = { viewModel.beginSentenceNote(listOf(PlaybackStateStore.currentIndex)) },
                    onJumpToSection = { index ->
                        viewModel.moveTo(index, false)
                        viewModel.updateState { it.copy(showDocumentNotes = false) }
                    },
                    onExportNotes = {
                        viewModel.saveDocumentNoteDraft()
                        viewModel.shareActiveDocumentNotes()
                    },
                    onDismiss = { viewModel.updateState { it.copy(showDocumentNotes = false) } }
                )
            }
        }

        val noteIndexes = uiState.noteTargetIndexes.ifEmpty {
            uiState.noteTargetIndex?.let(::listOf).orEmpty()
        }
        if (noteIndexes.isNotEmpty()) {
            uiState.activeDocument?.let { document ->
                SentenceNoteDialog(
                    document = document,
                    sentenceIndexes = noteIndexes,
                    noteDraft = uiState.noteDraft,
                    onNoteChange = { draft ->
                        viewModel.updateState {
                            it.copy(
                                noteDraft = capWords(
                                    draft,
                                    300
                                )
                            )
                        }
                    },
                    onSave = { viewModel.saveSentenceNote() },
                    onDelete = { viewModel.deleteSentenceNote() },
                    onDismiss = { viewModel.dismissSentenceNote() }
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

        uiState.deleteTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { viewModel.updateState { it.copy(deleteTarget = null) } },
                title = { Text("Delete reading?") },
                text = { Text("This removes ${target.title} from the local library, queue, reading lists, history, bookmarks, and notes.") },
                confirmButton = {
                    Button(onClick = { viewModel.deleteDocument(target) }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.updateState { it.copy(deleteTarget = null) } }) {
                        Text(
                            "Cancel"
                        )
                    }
                }
            )
        }

        uiState.renameTarget?.let { target ->
            AlertDialog(
                onDismissRequest = {
                    viewModel.updateState {
                        it.copy(
                            renameTarget = null,
                            renameDraft = ""
                        )
                    }
                },
                title = { Text("Rename reading") },
                text = {
                    OutlinedTextField(
                        value = uiState.renameDraft,
                        onValueChange = { value -> viewModel.updateState { it.copy(renameDraft = value) } },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.renameDocument(target, uiState.renameDraft) },
                        enabled = uiState.renameDraft.trim().isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.updateState {
                            it.copy(
                                renameTarget = null,
                                renameDraft = ""
                            )
                        }
                    }) { Text("Cancel") }
                }
            )
        }

        uiState.collectionTarget?.let { target ->
            AlertDialog(
                onDismissRequest = {
                    viewModel.updateState {
                        it.copy(
                            collectionTarget = null,
                            collectionDraft = ""
                        )
                    }
                },
                title = { Text("Move to collection") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(target.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        OutlinedTextField(
                            value = uiState.collectionDraft,
                            onValueChange = { value ->
                                viewModel.updateState {
                                    it.copy(
                                        collectionDraft = value
                                    )
                                }
                            },
                            label = { Text("Collection") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setDocumentCollection(
                            target,
                            uiState.collectionDraft
                        )
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.updateState {
                            it.copy(
                                collectionTarget = null,
                                collectionDraft = ""
                            )
                        }
                    }) { Text("Cancel") }
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
                        Text(
                            "Progress: ${target.currentIndex + 1} / ${
                                target.chunkCount.coerceAtLeast(
                                    1
                                )
                            }"
                        )
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
                        Text(
                            "Close"
                        )
                    }
                }
            )
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
                onShareSyncPack = { viewModel.updateState { it.copy(showSyncCenter = false) }; viewModel.shareLibrarySyncPack() },
                onImportSyncPack = {
                    backupImportLauncher.launch(veritasBackupMimeTypes())
                },
                onDismiss = { viewModel.updateState { it.copy(showSyncCenter = false) } }
            )
        }

        if (uiState.showAppHealth) {
            AppHealthDialog(
                documentCount = uiState.documents.size,
                queueCount = uiState.queuedDocuments.size,
                themePackName = VeritasThemePackCatalog.displayName(uiState.readerSettings.themePackId),
                themeName = VeritasThemeCatalog.displayName(uiState.readerSettings.themeId),
                onDismiss = { viewModel.updateState { it.copy(showAppHealth = false) } }
            )
        }

        if (uiState.showTutorial) {
            var isTransitioningStep by remember { mutableStateOf(false) }
            val activeStep = OnboardingController.activeStep
            LaunchedEffect(uiState.showTutorial, uiState.hasCompletedOnboarding) {
                if (uiState.showTutorial && !uiState.hasCompletedOnboarding && OnboardingController.activeStep == null) {
                    viewModel.createWelcomeDocumentSilently()
                    OnboardingController.activeStep = OnboardingStep.WELCOME
                }
            }
            // Voice-assisted tutorial: read each step aloud automatically
            LaunchedEffect(activeStep) {
                if (activeStep != null) {
                    TutorialSpeaker.init(context)
                    TutorialSpeaker.speak("${activeStep.title}. ${activeStep.body}")
                } else {
                    TutorialSpeaker.stop()
                }
            }
            // Shutdown speaker when tutorial is dismissed entirely
            LaunchedEffect(uiState.showTutorial) {
                if (!uiState.showTutorial) {
                    TutorialSpeaker.shutdown()
                }
            }
            // INSIGHTS_PAGE_SPOTLIGHT renders its card inside the insights dialog window;
            // the main overlay would be hidden behind that dialog, so skip it here.
            if (activeStep != null && activeStep != OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT) {
                OnboardingSpotlightOverlay(
                    step = activeStep,
                    userName = uiState.userName,
                    onUserNameChanged = { viewModel.updateUserNameInMemory(it) },
                    isTransitioning = isTransitioningStep,
                    onNext = {
                        if (!isTransitioningStep) {
                            coroutineScope.launch {
                                isTransitioningStep = true
                                try {
                                    TutorialSpeaker.stop() // stop current reading before moving to next step
                                    val nextStep = when (activeStep) {
                                        OnboardingStep.WELCOME -> OnboardingStep.NAME_INPUT
                                        OnboardingStep.NAME_INPUT -> {
                                            viewModel.saveUserName(uiState.userName)
                                            OnboardingStep.FAB_SPOTLIGHT
                                        }
                                        OnboardingStep.FAB_SPOTLIGHT -> OnboardingStep.CHECKLIST_SPOTLIGHT
                                        OnboardingStep.CHECKLIST_SPOTLIGHT -> OnboardingStep.INSIGHTS_SPOTLIGHT
                                        OnboardingStep.INSIGHTS_SPOTLIGHT -> OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT
                                        OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT -> OnboardingStep.DOCUMENT_SPOTLIGHT
                                        OnboardingStep.DOCUMENT_SPOTLIGHT -> {
                                            val targetDoc = uiState.documents.firstOrNull()
                                            if (targetDoc != null) {
                                                viewModel.openSavedDocument(targetDoc)
                                                // Wait for reader screen to load and render the mode toggle
                                                var elapsed = 0
                                                while (viewModel.uiState.value.activeDocument == null && elapsed < 40) {
                                                    delay(50)
                                                    elapsed++
                                                }
                                                elapsed = 0
                                                while (!OnboardingController.componentBounds.containsKey("reader_mode_toggle") && elapsed < 40) {
                                                    delay(50)
                                                    elapsed++
                                                }
                                                OnboardingStep.MODE_TOGGLE_SPOTLIGHT
                                            } else {
                                                OnboardingStep.CONGRATULATIONS
                                            }
                                        }
                                        OnboardingStep.MODE_TOGGLE_SPOTLIGHT -> OnboardingStep.PLAYER_PANEL_SPOTLIGHT
                                        OnboardingStep.PLAYER_PANEL_SPOTLIGHT -> OnboardingStep.READER_TEXT_SPOTLIGHT
                                        OnboardingStep.READER_TEXT_SPOTLIGHT -> {
                                            viewModel.returnToLibrary()
                                            // Wait for library screen to load
                                            var elapsed = 0
                                            while (viewModel.uiState.value.activeDocument != null && elapsed < 40) {
                                                delay(50)
                                                elapsed++
                                            }
                                            OnboardingStep.CONGRATULATIONS
                                        }
                                        OnboardingStep.CONGRATULATIONS -> null
                                    }
                                    if (nextStep == null) {
                                        OnboardingController.activeStep = null
                                        viewModel.completeQuestTour()
                                    } else {
                                        OnboardingController.activeStep = nextStep
                                    }
                                } finally {
                                    isTransitioningStep = false
                                }
                            }
                        }
                    },
                    onBack = {
                        if (!isTransitioningStep) {
                            coroutineScope.launch {
                                isTransitioningStep = true
                                try {
                                    TutorialSpeaker.stop() // stop current reading before moving to prev step
                                    val prevStep = when (activeStep) {
                                        OnboardingStep.WELCOME -> null
                                        OnboardingStep.NAME_INPUT -> OnboardingStep.WELCOME
                                        OnboardingStep.FAB_SPOTLIGHT -> OnboardingStep.NAME_INPUT
                                        OnboardingStep.CHECKLIST_SPOTLIGHT -> OnboardingStep.FAB_SPOTLIGHT
                                        OnboardingStep.INSIGHTS_SPOTLIGHT -> OnboardingStep.CHECKLIST_SPOTLIGHT
                                        OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT -> OnboardingStep.INSIGHTS_SPOTLIGHT
                                        OnboardingStep.DOCUMENT_SPOTLIGHT -> OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT
                                        OnboardingStep.MODE_TOGGLE_SPOTLIGHT -> {
                                            viewModel.returnToLibrary()
                                            // Wait for library screen to load and render the document card
                                            var elapsed = 0
                                            while (viewModel.uiState.value.activeDocument != null && elapsed < 40) {
                                                delay(50)
                                                elapsed++
                                            }
                                            elapsed = 0
                                            while (!OnboardingController.componentBounds.containsKey("document_card_0") && elapsed < 40) {
                                                delay(50)
                                                elapsed++
                                            }
                                            OnboardingStep.DOCUMENT_SPOTLIGHT
                                        }
                                        OnboardingStep.PLAYER_PANEL_SPOTLIGHT -> OnboardingStep.MODE_TOGGLE_SPOTLIGHT
                                        OnboardingStep.READER_TEXT_SPOTLIGHT -> OnboardingStep.PLAYER_PANEL_SPOTLIGHT
                                        OnboardingStep.CONGRATULATIONS -> {
                                            val targetDoc = uiState.documents.firstOrNull()
                                            if (targetDoc != null) {
                                                viewModel.openSavedDocument(targetDoc)
                                                // Wait for reader screen to load and render the reader text view
                                                var elapsed = 0
                                                while (viewModel.uiState.value.activeDocument == null && elapsed < 40) {
                                                    delay(50)
                                                    elapsed++
                                                }
                                                elapsed = 0
                                                while (!OnboardingController.componentBounds.containsKey("reader_text_view") && elapsed < 40) {
                                                    delay(50)
                                                    elapsed++
                                                }
                                                OnboardingStep.READER_TEXT_SPOTLIGHT
                                            } else {
                                                OnboardingStep.DOCUMENT_SPOTLIGHT
                                            }
                                        }
                                    }
                                    OnboardingController.activeStep = prevStep
                                } finally {
                                    isTransitioningStep = false
                                }
                            }
                        }
                    },
                    onDismiss = {
                        coroutineScope.launch {
                            TutorialSpeaker.stop()
                            if (uiState.activeDocument != null) {
                                viewModel.returnToLibrary()
                            }
                            OnboardingController.activeStep = null
                        }
                    }
                )
            }
        }

        if (uiState.showConfetti) {
            ConfettiOverlay(
                onFinished = {
                    viewModel.finishConfettiCelebration()
                }
            )
        }
    }
}

