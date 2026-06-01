package com.veritas.reader

import com.veritas.reader.ui.ReaderViewModel
import com.veritas.reader.ui.ReaderUiState
import com.veritas.reader.ui.screens.*

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.ActionMode
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import android.graphics.pdf.PdfRenderer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val viewModel: ReaderViewModel by viewModels()

    var onHardwarePlayPause: (() -> Unit)? = null
    var onHardwareNext: (() -> Unit)? = null
    var onHardwarePrevious: (() -> Unit)? = null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
        val isPageKey = event.keyCode == KeyEvent.KEYCODE_PAGE_DOWN || event.keyCode == KeyEvent.KEYCODE_PAGE_UP
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val incomingAction = intent?.action
        val openVoiceStudioOnStart = intent?.getBooleanExtra(EXTRA_OPEN_VOICE_STUDIO, false) == true
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

        setContent {
            VeritasTheme {
                VeritasReaderApp(
                    initialSharedText = sharedText,
                    initialSharedUri = sharedUri,
                    openVoiceStudioOnStart = openVoiceStudioOnStart
                )
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_VOICE_STUDIO = "com.veritas.reader.extra.OPEN_VOICE_STUDIO"
    }
}

private const val MAIN_ACTIVITY_TAG = "MainActivity"

object VeritasThemeState {
    var themeId by mutableStateOf(VeritasThemeCatalog.DEFAULT_ID)
    var themePackId by mutableStateOf(VeritasThemePackCatalog.DEFAULT_ID)
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Image(
        painter = painterResource(id = R.drawable.veritas_reader_icon),
        contentDescription = "Veritas",
        modifier = modifier
            .size(if (compact) 42.dp else 58.dp)
            .clip(RoundedCornerShape(if (compact) 10.dp else 16.dp)),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun BouncyTextButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "bouncyTextButton"
    )
    TextButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Text(label)
    }
}

@Composable
fun BouncyFilledButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "bouncyFilledButton"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = CircleShape,
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Text(label)
    }
}

@Composable
fun VeritasWordmark(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BrandMark(compact = true)
        Text(
            "eritas",
            style = MaterialTheme.typography.headlineSmall,
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
                if (emphasis) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha()))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

enum class VeritasBrowserTab(val label: String) {
    ALL("ALL"),
    BOOKS("BOOKS"),
    PDF("PDF"),
    DOC("DOC"),
    HTML("HTML"),
    TXT("TXT"),
    OCR("OCR")
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
            return VeritasFileBrowserScanResult(emptyList(), null, listOf("Grant All Files access or approve a folder to browse files."))
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
        val safeCurrent = if (current.absolutePath.startsWith(storageRoot.absolutePath)) current else storageRoot
        if (!safeCurrent.exists()) {
            diagnostics.add("${location.label} no longer exists.")
            return emptyList()
        }
        val children = runCatching { safeCurrent.listFiles()?.toList().orEmpty() }.getOrElse { error ->
            diagnostics.add("Android blocked access to ${location.label}: ${error.message ?: "folder is protected"}.")
            emptyList()
        }
        if (children.isEmpty() && safeCurrent.isDirectory && !safeCurrent.canRead()) {
            diagnostics.add("Android protects this folder. Shared storage can be browsed, but private system/app folders may remain unavailable.")
        }
        val currentEntries = children.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase(Locale.getDefault()) })
            .mapNotNull { child ->
                if (child.name == "." || child.name == "..") return@mapNotNull null
                val relativePath = child.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/')
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
        if (depth > 6 || results.size >= 300 || shouldSkipRecursiveDirectory(current, storageRoot)) return results
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
                    val relativePath = child.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/')
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
        val relative = folder.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/').lowercase(Locale.getDefault())
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
            context.contentResolver.query(childrenUri, childProjection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                val childId = cursor.stringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID) ?: continue
                val name = cursor.stringValue(DocumentsContract.Document.COLUMN_DISPLAY_NAME).orEmpty()
                val mimeType = cursor.stringValue(DocumentsContract.Document.COLUMN_MIME_TYPE).orEmpty()
                val size = cursor.longValue(DocumentsContract.Document.COLUMN_SIZE)
                val modified = cursor.longValue(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    val childPath = if (location.relativePath.isBlank()) name else "${location.relativePath}/$name"
                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                        entries.add(
                            VeritasBrowserFile(
                                uri = DocumentsContract.buildDocumentUriUsingTree(root.uri, childId),
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
                                uri = DocumentsContract.buildDocumentUriUsingTree(root.uri, childId),
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
        return entries.sortedWith(compareBy<VeritasBrowserFile> { !it.isDirectory }.thenBy { it.name.lowercase(Locale.getDefault()) })
    }

    private fun fileTypeFor(name: String, mimeType: String): VeritasBrowserTab? {
        val lowerName = name.lowercase(Locale.getDefault())
        val lowerMime = mimeType.lowercase(Locale.getDefault())
        return when {
            lowerMime.contains("pdf") || lowerName.endsWith(".pdf") -> VeritasBrowserTab.PDF
            lowerMime.contains("wordprocessingml") || lowerName.endsWith(".docx") -> VeritasBrowserTab.DOC
            lowerMime.contains("epub") || lowerName.endsWith(".epub") -> VeritasBrowserTab.BOOKS
            lowerMime.contains("html") || lowerName.endsWith(".html") || lowerName.endsWith(".htm") -> VeritasBrowserTab.HTML
            lowerMime.startsWith("text/") || lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".csv") -> VeritasBrowserTab.TXT
            lowerMime.startsWith("image/") || lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") || lowerName.endsWith(".bmp") || lowerName.endsWith(".tif") || lowerName.endsWith(".tiff") -> VeritasBrowserTab.OCR
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
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
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

@Composable
private fun VeritasReaderApp(
    initialSharedText: String,
    initialSharedUri: Uri?,
    openVoiceStudioOnStart: Boolean
) {
    val context = LocalContext.current
    val viewModel: ReaderViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            viewModel.importDocumentFromUri(
                uri = uri,
                pdfOptions = uiState.advancedPdfOptions,
                textOptions = uiState.textImportOptions
            )
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
            val elapsed = ((System.currentTimeMillis() - uiState.recordStartedAt) / 1000L).coerceAtLeast(0L)
            viewModel.updateState { it.copy(recordElapsedSeconds = elapsed) }
            delay(1_000L)
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.handledInitialShare) {
            if (initialSharedText.isNotBlank()) {
                viewModel.updateState { it.copy(draftText = initialSharedText) }
            }
            if (initialSharedUri != null) {
                viewModel.importDocumentFromUri(initialSharedUri)
            }
            if (openVoiceStudioOnStart) {
                viewModel.updateState { it.copy(showVoiceStudio = true) }
            }
            viewModel.updateState { it.copy(handledInitialShare = true) }
        }
    }

    BackHandler(enabled = true) {
        when {
            uiState.showTextEditor -> viewModel.dismissTextEditor()
            uiState.showCanvasView -> viewModel.updateState { it.copy(showCanvasView = false) }
            uiState.showFileBrowser && uiState.fileBrowserBackStack.isNotEmpty() -> viewModel.goUpFileBrowserDirectory()
            uiState.showFileBrowser -> viewModel.updateState { it.copy(showFileBrowser = false) }
            uiState.showPdfImportTools -> viewModel.updateState { it.copy(showPdfImportTools = false) }
            uiState.showReaderSettings -> viewModel.updateState { it.copy(showReaderSettings = false) }
            uiState.showPronunciationRules -> viewModel.updateState { it.copy(showPronunciationRules = false) }
            uiState.showVoiceStudio -> viewModel.updateState { it.copy(showVoiceStudio = false) }
            uiState.showNarrationStudio -> viewModel.updateState { it.copy(showNarrationStudio = false) }
            uiState.showAiStudyTools -> viewModel.updateState { it.copy(showAiStudyTools = false) }
            uiState.showOfflineStudyTools -> viewModel.updateState { it.copy(showOfflineStudyTools = false) }
            uiState.showAiCenter -> viewModel.updateState { it.copy(showAiCenter = false) }
            uiState.showAskAiSettings -> viewModel.updateState { it.copy(showAskAiSettings = false) }
            uiState.showTranslationTools -> viewModel.updateState { it.copy(showTranslationTools = false) }
            uiState.showSleepTimerDialog -> viewModel.updateState { it.copy(showSleepTimerDialog = false) }
            uiState.showReadingLists -> viewModel.updateState { it.copy(showReadingLists = false) }
            uiState.showReadingHistory -> viewModel.updateState { it.copy(showReadingHistory = false) }
            uiState.showDocumentNotes -> viewModel.updateState { it.copy(showDocumentNotes = false) }
            uiState.showSettingsHub -> viewModel.updateState { it.copy(showSettingsHub = false) }
            uiState.showBackupTools -> viewModel.updateState { it.copy(showBackupTools = false) }
            uiState.showSyncCenter -> viewModel.updateState { it.copy(showSyncCenter = false) }
            uiState.showAppHealth -> viewModel.updateState { it.copy(showAppHealth = false) }
            uiState.showTutorial -> viewModel.finishTutorial()
            uiState.activeDocument != null -> viewModel.returnToLibrary()
            else -> (context as? ComponentActivity)?.finish()
        }
    }

    VeritasTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (uiState.activeDocument == null) {
                LibraryScreen(
                    uiState = uiState,
                    onDraftTextChange = { text -> viewModel.updateState { it.copy(draftText = text) } },
                    onCreateFromDraft = { 
                        if (WebArticleExtractor.looksLikeUrl(uiState.draftText)) viewModel.importWebArticle(uiState.draftText)
                        else viewModel.createAndOpenDocument("Pasted text", uiState.draftText, "Pasted")
                    },
                    onImportWebArticle = { viewModel.importWebArticle(it) },
                    onImportFile = { importFileLauncher.launch(readableImportMimeTypes()) },
                    onAdvancedPdfImport = { viewModel.updateState { it.copy(showPdfImportTools = true) } },
                    onOpenFileBrowser = { viewModel.openFileBrowser() },
                    onOpenReadingLists = { viewModel.updateState { it.copy(showReadingLists = true) } },
                    onOpenReadingHistory = { viewModel.updateState { it.copy(showReadingHistory = true) } },
                    onOpenDocument = { viewModel.openSavedDocument(it) },
                    onOpenDocumentAt = { document, sentenceIndex -> viewModel.openSavedDocument(document, sentenceIndex) },
                    onClearContinueDocument = { viewModel.clearContinueReading(it) },
                    onDeleteDocument = { doc -> viewModel.updateState { it.copy(deleteTarget = doc) } },
                    onToggleQueue = { viewModel.toggleQueue(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onRenameDocument = { doc -> viewModel.updateState { it.copy(renameTarget = doc, renameDraft = doc.title) } },
                    onSetCollection = { doc -> viewModel.updateState { it.copy(collectionTarget = doc, collectionDraft = doc.collection) } },
                    onShowDetails = { doc -> viewModel.updateState { it.copy(detailsTarget = doc) } },
                    isQueued = { doc -> uiState.queuedDocuments.any { it.id == doc.id } },
                    onPlayQueue = { viewModel.playQueue() },
                    onMoveQueueUp = { viewModel.moveQueueItem(it, -1) },
                    onMoveQueueDown = { viewModel.moveQueueItem(it, 1) },
                    onRemoveFromQueue = { viewModel.toggleQueue(it) },
                    onClearQueue = { viewModel.clearQueue() },
                    onBackupRestore = { viewModel.updateState { it.copy(showBackupTools = true) } },
                    onOpenSyncCenter = { viewModel.updateState { it.copy(showSyncCenter = true) } },
                    onOpenAiCenter = { viewModel.updateState { it.copy(showAiCenter = true) } },
                    onOpenSettingsHub = { viewModel.updateState { it.copy(showSettingsHub = true) } },
                    onRefreshMainPage = { viewModel.refreshAll() },
                    onBatchDeleteDocuments = { viewModel.deleteDocuments(it) },
                    onBatchFavoriteDocuments = { viewModel.favoriteDocuments(it) },
                    onBatchQueueDocuments = { viewModel.queueDocuments(it) },
                    onBatchSetCollectionDocuments = { ids, collection -> viewModel.setCollectionForDocuments(ids, collection) },
                    onDeleteAnnotations = { keys -> viewModel.deleteAnnotations(keys) }
                )
            } else {
                val activeDocument = uiState.activeDocument ?: return@Box
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
                            PlaybackStateStore.currentIndex.toFloat() / activeDocument.chunks.lastIndex.toFloat().coerceAtLeast(1f)
                        },
                        isPlaying = PlaybackStateStore.isPlaying,
                        queueCount = PlaybackStateStore.queueCount,
                        rate = PlaybackStateStore.rate,
                        pitch = PlaybackStateStore.pitch,
                        fontSizeSp = uiState.readerSettings.fontSizeSp,
                        onPlayPause = { viewModel.playOrPause() },
                        onPageChanged = { pageIndex, pageCount ->
                            val lastSentenceIndex = activeDocument.chunks.lastIndex.coerceAtLeast(0)
                            val pageDenominator = (pageCount - 1).coerceAtLeast(1)
                            val targetIndex = ((pageIndex.toFloat() / pageDenominator.toFloat()) * lastSentenceIndex)
                                .roundToInt()
                                .coerceIn(0, lastSentenceIndex)
                            viewModel.moveTo(targetIndex, autoPlay = false)
                        },
                        onOpenExternal = { openOriginalDocument(context, documentRepository, activeMetadata) },
                        onRateChange = { viewModel.saveVoiceSettings(uiState.voiceSettings.copy(preferredRate = it)) },
                        onPitchChange = { viewModel.saveVoiceSettings(uiState.voiceSettings.copy(preferredPitch = it)) },
                        onFontSizeChange = { viewModel.saveReaderSettings(uiState.readerSettings.copy(fontSizeSp = it)) },
                        onOpenVoiceStudio = { viewModel.updateState { it.copy(showVoiceStudio = true) } },
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
                        hasCanvas = activeMetadata?.originalFileName?.isNotBlank() == true || activeDocument.chunks.any { it.trim().startsWith("[CANVAS") },
                        sleepTimerDurationMillis = PlaybackStateStore.sleepTimerDurationMillis,
                        sleepTimerEndsAtMillis = PlaybackStateStore.sleepTimerEndsAtMillis,
                        sleepTimerAction = PlaybackStateStore.sleepTimerAction,
                        readingListCount = uiState.readingListCatalog.activeLists.size,
                        activeDocumentReadingListCount = uiState.activeDocument?.id?.let { activeId ->
                            uiState.readingListCatalog.listsContaining(activeId).count { !it.archived }
                        } ?: 0
                    ),
                    listState = rememberLazyListState(),
                    hasCanvas = activeMetadata?.originalFileName?.isNotBlank() == true || activeDocument.chunks.any { it.trim().startsWith("[CANVAS") },
                    onBackToLibrary = { viewModel.returnToLibrary() },
                    onSentenceClick = { viewModel.moveTo(it, false) },
                    onSentenceDoubleTap = { viewModel.moveTo(it, autoPlay = true, forcePlaybackStart = true) },
                    onPlayPause = { viewModel.playOrPause() },
                    onStop = { viewModel.stopAndForgetPlayback() },
                    onPrevious = { viewModel.moveTo((PlaybackStateStore.currentIndex - 1).coerceAtLeast(0), false) },
                    onNext = { viewModel.moveTo((PlaybackStateStore.currentIndex + 1).coerceAtMost(activeDocument.chunks.size - 1), false) },
                    onRateChange = { viewModel.saveVoiceSettings(uiState.voiceSettings.copy(preferredRate = it)) },
                    onPitchChange = { viewModel.saveVoiceSettings(uiState.voiceSettings.copy(preferredPitch = it)) },
                    onFontSizeChange = { viewModel.saveReaderSettings(uiState.readerSettings.copy(fontSizeSp = it)) },
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
                        val metadata = activeMetadata
                        val isPdfCanvas = metadata != null && (
                            metadata.originalMimeType.contains("pdf", ignoreCase = true) ||
                                metadata.originalFileName.endsWith(".pdf", ignoreCase = true)
                            )
                        if (metadata != null && isPdfCanvas) {
                            context.startActivity(VeritasPdfViewerActivity.intent(context, metadata.id))
                        } else {
                            viewModel.updateState { it.copy(showCanvasView = true) }
                        }
                    },
                    onOpenStudyTools = { viewModel.updateState { it.copy(showAiStudyTools = true) } },
                    onOpenTranslationTools = { viewModel.updateState { it.copy(showTranslationTools = true) } },
                    onOpenSleepTimer = { viewModel.updateState { it.copy(showSleepTimerDialog = true) } },
                    onOpenReadingLists = { viewModel.updateState { it.copy(showReadingLists = true) } },
                    onOpenReadingHistory = { viewModel.updateState { it.copy(showReadingHistory = true) } },
                    onAskCurrentSection = {
                        uiState.activeDocument?.let { document ->
                            AiPromptLauncher.launch(
                                context = context,
                                title = document.title,
                                chunks = document.chunks,
                                currentIndex = PlaybackStateStore.currentIndex,
                                type = AiPromptType.EXPLAIN_SECTION,
                                scope = AiPromptScope.CURRENT_SECTION,
                                settings = uiState.askAiSettings
                            )
                            val prompt = AiPromptLauncher.buildPrompt(
                                title = document.title,
                                chunks = document.chunks,
                                currentIndex = PlaybackStateStore.currentIndex,
                                type = AiPromptType.EXPLAIN_SECTION,
                                scope = AiPromptScope.CURRENT_SECTION
                            )
                            viewModel.recordAiPrompt(document.title, AiPromptType.EXPLAIN_SECTION.label, AiPromptScope.CURRENT_SECTION.label, prompt)
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
                    onGoogleSelection = { openGoogleSearch(context, it) },
                    onTranslateSelection = { openGoogleTranslate(context, it) },
                    onAskAiSelection = { askAiWithSelection(context, uiState.askAiSettings, it) },
                    onEditSpeechSelection = { selection ->
                        viewModel.updateState {
                            it.copy(
                                showPronunciationRules = true,
                                newRuleFind = selection.replace(Regex("\\s+"), " ").trim().take(120),
                                newRuleReplaceWith = ""
                            )
                        }
                    },
                    onReadSelection = { sendSelectionSpeechIntent(context, it) },
                    onEditExtractedSelection = { selection -> viewModel.openSelectionTextEditor(selection.sentenceIndexes) },
                    onPlayQueue = { viewModel.playQueue() }
                )
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
                onOpenTutorial = { viewModel.updateState { it.copy(showTutorial = true) } },
                onOpenPdfTools = { viewModel.updateState { it.copy(showPdfImportTools = true) } },
                onOpenFileBrowser = { viewModel.openFileBrowser() },
                onOpenSleepTimer = { viewModel.updateState { it.copy(showSleepTimerDialog = true) } },
                onOpenReadingLists = { viewModel.updateState { it.copy(showReadingLists = true) } },
                onOpenAppHealth = { viewModel.updateState { it.copy(showAppHealth = true) } }
            )
        }

        if (uiState.showVoiceStudio) {
            VoiceStudioDialog(
                settings = uiState.voiceSettings,
                engines = uiState.ttsEngines,
                voices = uiState.ttsVoices,
                loadingVoices = uiState.voiceLoadInProgress,
                onRefreshEngines = { viewModel.updateState { it.copy(ttsEngines = VoiceManager.loadInstalledEngines(context)) } },
                onLoadVoices = { viewModel.loadVoicesForEngine() },
                onUseSystemDefault = { viewModel.saveVoiceSettings(uiState.voiceSettings.copy(enginePackage = "", engineLabel = "System default", voiceName = "", voiceLabel = "System default voice")) },
                onEngineSelected = { engine -> viewModel.saveVoiceSettings(uiState.voiceSettings.copy(enginePackage = engine.packageName, engineLabel = engine.label, voiceName = "", voiceLabel = "System default voice")) },
                onLanguageSelected = { localeTag -> viewModel.saveVoiceSettings(uiState.voiceSettings.copy(localeTag = localeTag, voiceName = "", voiceLabel = "System default voice")) },
                onShowNetworkVoicesChange = { showNetwork -> viewModel.saveVoiceSettings(uiState.voiceSettings.copy(showNetworkVoices = showNetwork)) },
                onVoiceSelected = { voice -> viewModel.saveVoiceSettings(uiState.voiceSettings.copy(voiceName = voice.name, voiceLabel = voice.label, localeTag = voice.localeTag)) },
                onPreviewVoice = { voice -> viewModel.previewVoice(voice) },
                onPresetSelected = { name, rate, pitch -> viewModel.saveVoiceSettings(uiState.voiceSettings.copy(profileName = name, preferredRate = rate, preferredPitch = pitch)) },
                onAddLanguageVoice = { viewModel.openTtsDataInstaller() },
                onOpenSystemTtsSettings = { viewModel.openSystemTtsSettings() },
                onOpenSpeechEdits = { viewModel.updateState { it.copy(showVoiceStudio = false, showPronunciationRules = true) } },
                onOpenNarrationStudio = { viewModel.updateState { it.copy(showVoiceStudio = false, showNarrationStudio = true) } },
                onDismiss = { viewModel.updateState { it.copy(showVoiceStudio = false) } }
            )
        }

        if (uiState.showReaderSettings) {
            ReaderSettingsDialog(
                settings = uiState.readerSettings,
                onDismiss = { viewModel.updateState { it.copy(showReaderSettings = false) } },
                onFontSizeChange = { size -> viewModel.saveReaderSettings(uiState.readerSettings.copy(fontSizeSp = size)) },
                onSpacingChange = { spacing -> viewModel.saveReaderSettings(uiState.readerSettings.copy(sectionSpacingDp = spacing)) },
                onThemeChange = { themeId -> viewModel.saveReaderSettings(uiState.readerSettings.copy(themeId = themeId)) },
                onThemePackChange = { packId -> viewModel.saveReaderSettings(uiState.readerSettings.copy(themePackId = packId)) },
                onToggleSectionNumbers = { viewModel.saveReaderSettings(uiState.readerSettings.copy(showSectionNumbers = !uiState.readerSettings.showSectionNumbers)) },
                onToggleAutoPlayQueue = { viewModel.saveReaderSettings(uiState.readerSettings.copy(autoPlayQueue = !uiState.readerSettings.autoPlayQueue)) }
            )
        }

        if (uiState.showPronunciationRules) {
            PronunciationRulesDialog(
                rules = uiState.pronunciationRules,
                newFind = uiState.newRuleFind,
                newReplaceWith = uiState.newRuleReplaceWith,
                onNewFindChange = { value -> viewModel.updateState { it.copy(newRuleFind = value.take(120)) } },
                onNewReplaceChange = { value -> viewModel.updateState { it.copy(newRuleReplaceWith = value.take(120)) } },
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
                onInstallAssistant = { packageName -> openPlayStoreForPackage(context, packageName) },
                onDismiss = { viewModel.updateState { it.copy(showAskAiSettings = false) } }
            )
        }

        if (uiState.showAiCenter) {
            AiCenterDialog(
                installedAiCount = installedAiOptions(context).size,
                documentCount = uiState.documents.size,
                onOpenAskAiSettings = { viewModel.updateState { it.copy(showAiCenter = false, showAskAiSettings = true) } },
                onOpenStudyTools = { viewModel.updateState { it.copy(showAiCenter = false, showAiStudyTools = true) } },
                onDismiss = { viewModel.updateState { it.copy(showAiCenter = false) } }
            )
        }

        if (uiState.showAiStudyTools && uiState.activeDocument == null) {
            AlertDialog(
                onDismissRequest = { viewModel.updateState { it.copy(showAiStudyTools = false) } },
                title = { Text("AI Study Tools") },
                text = { Text("Open a reading first, then AI Study Tools can prepare the current part or whole document.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.updateState { it.copy(showAiStudyTools = false) } }) { Text("OK") }
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
                onSendToAiApp = { type, customInstruction, scope ->
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
                        title = document.title,
                        chunks = document.chunks,
                        currentIndex = PlaybackStateStore.currentIndex,
                        type = type,
                        customInstruction = customInstruction,
                        scope = scope,
                        settings = uiState.askAiSettings
                    )
                    viewModel.recordAiPrompt(document.title, type.label, scope.label, prompt)
                },
                onSaveTemplate = { title, instruction -> viewModel.saveAiPromptTemplate(title, instruction) },
                onDeleteTemplate = { id -> viewModel.deleteAiPromptTemplate(id) },
                onClearHistory = { viewModel.clearAiPromptHistory() },
                onCopyText = { label, text -> copyTextToClipboard(context, label, text) },
                onSaveAiResultAsNote = { result ->
                    viewModel.updateState { it.copy(noteDraft = result, noteTargetIndexes = listOf(PlaybackStateStore.currentIndex)) }
                    viewModel.saveSentenceNote()
                },
                onOpenOfflineStudyTools = { viewModel.updateState { it.copy(showAiStudyTools = false, showOfflineStudyTools = true) } }
            )
        }
        }

        uiState.activeDocument?.let { document ->
        if (uiState.showOfflineStudyTools) {
            StudyToolsDialog(
                studyPack = StudyAssistant.buildStudyPack(document.title, document.chunks, PlaybackStateStore.currentIndex),
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
                onDismiss = { viewModel.updateState { it.copy(exportMessage = null, exportedAudioFile = null) } }
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

        if (uiState.showFileBrowser) {
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
                        viewModel.importDocumentFromUri(
                            uri = file.uri,
                            pdfOptions = uiState.advancedPdfOptions,
                            textOptions = uiState.textImportOptions,
                            sourceNameHint = file.name
                        )
                    }
                }
            )
        }

        if (uiState.showReadingHistory) {
            ReadingHistoryDialog(
                history = uiState.readingHistory,
                documents = uiState.documents,
                onDismiss = { viewModel.updateState { it.copy(showReadingHistory = false) } },
                onOpenDocument = { doc -> viewModel.updateState { it.copy(showReadingHistory = false) }; viewModel.openSavedDocument(doc) },
                onClearHistory = { viewModel.clearReadingHistory() }
            )
        }

        if (uiState.showDocumentNotes) {
            uiState.activeDocument?.let { document ->
                DocumentNotesDialog(
                    document = document,
                    annotations = uiState.annotations,
                    documentNote = uiState.documentNoteDraft,
                    currentIndex = PlaybackStateStore.currentIndex.coerceIn(0, document.chunks.lastIndex.coerceAtLeast(0)),
                    onDocumentNoteChange = { draft -> viewModel.updateState { it.copy(documentNoteDraft = draft) } },
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
                    onNoteChange = { draft -> viewModel.updateState { it.copy(noteDraft = capWords(draft, 300)) } },
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
                    TextButton(onClick = { viewModel.updateState { it.copy(deleteTarget = null) } }) { Text("Cancel") }
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
                    TextButton(onClick = { viewModel.updateState { it.copy(renameTarget = null, renameDraft = "") } }) { Text("Cancel") }
                }
            )
        }

        uiState.collectionTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { viewModel.updateState { it.copy(collectionTarget = null, collectionDraft = "") } },
                title = { Text("Move to collection") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(target.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        OutlinedTextField(
                            value = uiState.collectionDraft,
                            onValueChange = { value -> viewModel.updateState { it.copy(collectionDraft = value) } },
                            label = { Text("Collection") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.setDocumentCollection(target, uiState.collectionDraft) }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.updateState { it.copy(collectionTarget = null, collectionDraft = "") } }) { Text("Cancel") }
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
                        Text(target.preview, maxLines = 4, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.updateState { it.copy(detailsTarget = null) } }) { Text("Close") }
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
            TutorialDialog(
                initialName = uiState.userName,
                onDismiss = { name -> viewModel.finishOnboarding(name) },
                onImport = { name -> viewModel.finishOnboarding(name); viewModel.updateState { it.copy(showPdfImportTools = true) } },
                onVoice = { name -> viewModel.finishOnboarding(name); viewModel.updateState { it.copy(showVoiceStudio = true) } },
                onThemes = { name -> viewModel.finishOnboarding(name); viewModel.updateState { it.copy(showReaderSettings = true) } }
            )
        }
    }
}

@Composable
private fun SentenceNoteDialog(
    document: ReaderDocument,
    sentenceIndexes: List<Int>,
    noteDraft: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val indexes = sentenceIndexes
        .filter { it in document.chunks.indices }
        .distinct()
        .sorted()
    val title = if (indexes.size == 1) {
        "Sentence ${indexes.first() + 1} note"
    } else {
        "${indexes.size} sentence note"
    }
    val wordCount = noteDraft.trim().split(Regex("\\s+")).count { it.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(document.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    label = { Text("Sentence note") },
                    placeholder = { Text("Write the note to attach to this sentence") },
                    minLines = 6,
                    maxLines = 10
                )
                Text("$wordCount / 300 words", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                indexes.take(5).forEach { index ->
                    val excerpt = document.chunks.getOrNull(index).orEmpty().replace(Regex("\\s+"), " ").trim()
                    if (excerpt.isNotBlank()) {
                        Text(
                            "Sentence ${index + 1}: $excerpt",
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (indexes.size > 5) {
                    Text("+ ${indexes.size - 5} more selected sentences", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = noteDraft.trim().isNotBlank()) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Delete") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun FileBrowserDialog(
    roots: List<VeritasBrowserRoot>,
    entries: List<VeritasBrowserFile>,
    location: VeritasBrowserLocation?,
    canGoUp: Boolean,
    scanning: Boolean,
    message: String?,
    allFilesAccessGranted: Boolean,
    importing: Boolean,
    importingName: String,
    onDismiss: () -> Unit,
    onPickFolder: () -> Unit,
    onRequestAllFilesAccess: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onRefresh: () -> Unit,
    onGoUp: () -> Unit,
    onEnterDirectory: (VeritasBrowserFile) -> Unit,
    onRemoveAllAccess: () -> Unit,
    onImportFile: (VeritasBrowserFile) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(VeritasBrowserTab.ALL) }
    var sortMode by remember { mutableStateOf(VeritasBrowserSort.NAME) }
    var sortAscending by remember { mutableStateOf(true) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    val browserFeatures = remember(roots, allFilesAccessGranted) {
        VeritasFeatureRegistry.resolve(
            VeritasFeatureSurface.FILE_BROWSER_OVERFLOW,
            VeritasFeatureContext(hasFileBrowserSession = roots.isNotEmpty() || allFilesAccessGranted)
        ).associateBy { it.definition.id }
    }
    val visibleEntries = remember(entries, query, selectedTab, sortMode, sortAscending) {
        val needle = query.trim()
        val filtered = entries
            .filter { selectedTab == VeritasBrowserTab.ALL || it.isDirectory || it.type == selectedTab }
            .filter { file ->
                needle.isBlank() ||
                    file.name.contains(needle, ignoreCase = true) ||
                    file.rootLabel.contains(needle, ignoreCase = true) ||
                    file.relativePath.contains(needle, ignoreCase = true)
            }
        val comparator = when (sortMode) {
            VeritasBrowserSort.NAME -> compareBy<VeritasBrowserFile> { it.name.lowercase(Locale.getDefault()) }
            VeritasBrowserSort.DATE -> compareBy { it.modifiedAt }
            VeritasBrowserSort.SIZE -> compareBy { it.sizeBytes }
            VeritasBrowserSort.PATH -> compareBy { it.relativePath.lowercase(Locale.getDefault()) }
        }
        val sorted = if (sortAscending) filtered.sortedWith(comparator) else filtered.sortedWith(comparator.reversed())
        sorted.sortedBy { it.isDirectory }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("‹", style = MaterialTheme.typography.headlineSmall) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("File browser", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text(
                            when {
                                allFilesAccessGranted -> "All Files access • ${location?.label ?: "Phone storage"}"
                                roots.isEmpty() -> "No folders approved"
                                else -> location?.label ?: "${roots.size} approved folder${if (roots.size == 1) "" else "s"}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box {
                        TextButton(onClick = { showMoreMenu = true }, enabled = !importing, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("⋮", style = MaterialTheme.typography.titleLarge) }
                        DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Import with file picker") },
                                enabled = !importing,
                                onClick = {
                                    showMoreMenu = false
                                    onOpenFilePicker()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Go up") },
                                enabled = canGoUp && !scanning,
                                onClick = {
                                    showMoreMenu = false
                                    onGoUp()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Refresh files") },
                                enabled = (roots.isNotEmpty() || allFilesAccessGranted) && !scanning,
                                onClick = {
                                    showMoreMenu = false
                                    onRefresh()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (allFilesAccessGranted) "All files access granted" else "Grant all files access") },
                                enabled = !allFilesAccessGranted,
                                onClick = {
                                    showMoreMenu = false
                                    onRequestAllFilesAccess()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Folders to scan") },
                                onClick = {
                                    showMoreMenu = false
                                    onPickFolder()
                                }
                            )
                            FeatureDropdownMenuItem(
                                feature = browserFeatures.requireResolvedFeature(VeritasFeatureId.FILE_BROWSER_SORTING),
                                label = "Sort files",
                                onClick = {
                                    showMoreMenu = false
                                    showSortDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove all files access") },
                                enabled = roots.isNotEmpty(),
                                onClick = {
                                    showMoreMenu = false
                                    onRemoveAllAccess()
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    leadingIcon = { Text("⌕", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("Search files...") },
                    singleLine = true,
                    shape = RoundedCornerShape(50)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VeritasBrowserTab.entries.forEach { tab ->
                        val count = if (tab == VeritasBrowserTab.ALL) entries.count { !it.isDirectory } else entries.count { !it.isDirectory && it.type == tab }
                        if (selectedTab == tab) {
                            Button(onClick = { selectedTab = tab }, contentPadding = ButtonDefaults.ContentPadding) {
                                Text("${tab.label} $count")
                            }
                        } else {
                            OutlinedButton(onClick = { selectedTab = tab }, contentPadding = ButtonDefaults.ContentPadding) {
                                Text("${tab.label} $count")
                            }
                        }
                    }
                }

                message?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (importing) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Text(
                            "Importing ${importingName.ifBlank { "selected file" }}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                when {
                    roots.isEmpty() && !allFilesAccessGranted -> FileBrowserEmptyState(
                        onPickFolder = onPickFolder,
                        onRequestAllFilesAccess = onRequestAllFilesAccess
                    )
                    scanning -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator()
                            Text("Opening folder.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    visibleEntries.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No files or folders match this view.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        val folders = visibleEntries.filter { it.isDirectory }
                        val files = visibleEntries.filter { !it.isDirectory }
                        if (files.isNotEmpty()) {
                            item("files-header") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text("Documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                    Text("PDF, DOCX, TXT, EPUB, HTML", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        items(files, key = { it.uri.toString() }) { file ->
                            FileBrowserFileRow(
                                file = file,
                                importing = importing,
                                onOpenDirectory = { onEnterDirectory(file) },
                                onImport = { onImportFile(file) }
                            )
                        }
                        if (folders.isNotEmpty()) {
                            item("folders-header") {
                                Text("Folders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
                            }
                        }
                        items(folders, key = { it.uri.toString() }) { file ->
                            FileBrowserFileRow(
                                file = file,
                                importing = importing,
                                onOpenDirectory = { onEnterDirectory(file) },
                                onImport = { onImportFile(file) }
                            )
                        }
                    }
                }
            }
                Button(
                    onClick = onOpenFilePicker,
                    enabled = !importing,
                    modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(22.dp),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text("＋ Import")
                }
                if (importing) {
                    ImportProgressOverlay(importingName.ifBlank { "selected file" })
                }
            }
        }
    }

    if (showSortDialog) {
        FileBrowserSortDialog(
            sortMode = sortMode,
            sortAscending = sortAscending,
            onSortModeChange = { sortMode = it },
            onSortAscendingChange = { sortAscending = it },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
private fun FileBrowserEmptyState(
    onPickFolder: () -> Unit,
    onRequestAllFilesAccess: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f))
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("No file access yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(
                    "Grant All Files access for broad phone storage browsing, or choose specific folders to scan.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onRequestAllFilesAccess, modifier = Modifier.fillMaxWidth()) { Text("Grant all files access") }
                Button(onClick = onPickFolder, modifier = Modifier.fillMaxWidth()) { Text("Folders to scan") }
            }
        }
    }
}

@Composable
private fun ImportProgressOverlay(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator()
                Text("Importing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(
                    title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Extracting readable text and preserving the original file.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FileBrowserFileRow(
    file: VeritasBrowserFile,
    importing: Boolean,
    onOpenDirectory: () -> Unit,
    onImport: () -> Unit
) {
    val enabled = if (file.isDirectory) file.targetLocation != null else file.isSupported && !importing
    val action = if (file.isDirectory) onOpenDirectory else onImport
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { action() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (file.isSupported || file.isDirectory) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
            }
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(
                    if (file.isDirectory) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(14.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (file.isDirectory) "DIR" else if (file.isSupported) file.type.label else "FILE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = if (file.isDirectory) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(file.name, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(fileBrowserFolderLine(file), maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    when {
                        file.isDirectory && file.targetLocation != null -> "Folder • ${formatBrowserModified(file.modifiedAt)}"
                        file.isDirectory -> "Protected folder • Android may block this path"
                        file.isSupported -> "${formatBrowserFileSize(file.sizeBytes)} • ${formatBrowserModified(file.modifiedAt)}"
                        else -> "Unsupported file • ${formatBrowserFileSize(file.sizeBytes)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (file.isDirectory) {
                TextButton(onClick = onOpenDirectory, enabled = enabled) { Text("Open") }
            } else {
                TextButton(onClick = onImport, enabled = enabled) { Text(if (file.isSupported) "Import" else "Unsupported") }
            }
        }
    }
}

@Composable
private fun FileBrowserSortDialog(
    sortMode: VeritasBrowserSort,
    sortAscending: Boolean,
    onSortModeChange: (VeritasBrowserSort) -> Unit,
    onSortAscendingChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        title = { Text("Sort files by") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VeritasBrowserSort.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSortModeChange(option) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = sortMode == option, onClick = { onSortModeChange(option) })
                        Text(option.label, modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Order:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSortAscendingChange(true) }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = sortAscending, onClick = { onSortAscendingChange(true) })
                    Text("Ascending", modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSortAscendingChange(false) }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = !sortAscending, onClick = { onSortAscendingChange(false) })
                    Text("Descending", modifier = Modifier.weight(1f))
                }
            }
        }
    )
}

private fun fileBrowserFolderLine(file: VeritasBrowserFile): String {
    val folderPath = file.relativePath.substringBeforeLast('/', missingDelimiterValue = "")
    return if (folderPath.isBlank()) file.rootLabel else "${file.rootLabel}/$folderPath"
}

private fun formatBrowserFileSize(bytes: Long): String {
    if (bytes <= 0L) return "Unknown size"
    val units = listOf("B", "kB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return if (unitIndex == 0) {
        "${bytes} ${units[unitIndex]}"
    } else {
        String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
    }
}

private fun formatBrowserModified(timestamp: Long): String =
    if (timestamp > 0L) formatUpdated(timestamp) else "Unknown date"

@Composable
private fun ReadingHistoryDialog(
    history: List<ReadingHistoryEntry>,
    documents: List<SavedDocument>,
    onDismiss: () -> Unit,
    onOpenDocument: (SavedDocument) -> Unit,
    onClearHistory: () -> Unit
) {
    val docsById = remember(documents) { documents.associateBy { it.id } }
    val visibleHistory = remember(history, documents) {
        history.mapNotNull { entry ->
            docsById[entry.documentId]?.let { document -> entry to document }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = {
            TextButton(onClick = onClearHistory, enabled = visibleHistory.isNotEmpty()) { Text("Clear") }
        },
        title = { Text("Reading history") },
        text = {
            if (visibleHistory.isEmpty()) {
                Text("Open a reading and it will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(visibleHistory, key = { (entry, _) -> entry.documentId }) { (entry, document) ->
                        ReadingHistoryRow(
                            entry = entry,
                            document = document,
                            onOpen = { onOpenDocument(document) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ReadingHistoryRow(
    entry: ReadingHistoryEntry,
    document: SavedDocument,
    onOpen: () -> Unit
) {
    val safeChunkCount = entry.chunkCount.coerceAtLeast(document.chunkCount).coerceAtLeast(1)
    val safeIndex = entry.currentIndex.coerceIn(0, safeChunkCount - 1)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(document.sourceLabel.take(3).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(document.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(
                    "${document.sourceLabel} • sentence ${safeIndex + 1}/$safeChunkCount • ${progressPercent(document)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Opened ${formatUpdated(entry.openedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onOpen) { Text("Open") }
        }
    }
}








fun Map<VeritasFeatureId, ResolvedVeritasFeature>.requireResolvedFeature(
    id: VeritasFeatureId
): ResolvedVeritasFeature = getValue(id)

data class ReaderTextSelection(
    val partIndex: Int,
    val start: Int,
    val endExclusive: Int,
    val text: String,
    val sentenceIndexes: List<Int>
) {
    val firstSentenceIndex: Int
        get() = sentenceIndexes.firstOrNull() ?: 0

    val endSentenceIndexExclusive: Int
        get() = (sentenceIndexes.lastOrNull() ?: firstSentenceIndex) + 1
}

@Composable
private fun PdfImportOptionsDialog(
    options: PdfImportOptions,
    textOptions: TextImportOptions,
    onOptionsChange: (PdfImportOptions) -> Unit,
    onTextOptionsChange: (TextImportOptions) -> Unit,
    onPickPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    var startPageDraft by remember(options.startPage) { mutableStateOf(options.startPage?.toString().orEmpty()) }
    var endPageDraft by remember(options.endPage) { mutableStateOf(options.endPage?.toString().orEmpty()) }
    var modeExpanded by remember { mutableStateOf(false) }
    var encodingExpanded by remember { mutableStateOf(false) }
    val extractionModes = remember {
        listOf(
            "HTML with images",
            "Plain text",
            "Prefer OCR when text is poor",
            "Force OCR"
        )
    }
    val selectedEncoding = TextImportEncodingCatalog.byId(textOptions.encodingId)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PDF text import settings") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Control how PDFs and text files are imported before they enter the reader.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = startPageDraft,
                            onValueChange = { value ->
                                startPageDraft = value.filter { it.isDigit() }.take(5)
                                onOptionsChange(options.copy(startPage = startPageDraft.toIntOrNull()?.takeIf { it > 0 }))
                            },
                            label = { Text("Start page") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endPageDraft,
                            onValueChange = { value ->
                                endPageDraft = value.filter { it.isDigit() }.take(5)
                                onOptionsChange(options.copy(endPage = endPageDraft.toIntOrNull()?.takeIf { it > 0 }))
                            },
                            label = { Text("End page") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Column {
                        Text("Extraction mode", fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(onClick = { modeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(options.extractionMode.ifBlank { "HTML with images" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            DropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }) {
                                extractionModes.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode) },
                                        onClick = {
                                            modeExpanded = false
                                            onOptionsChange(
                                                options.copy(
                                                    extractionMode = mode,
                                                    forceOcr = mode == "Force OCR",
                                                    preferOcrWhenLowText = mode != "Plain text"
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Column {
                        Text("Text encoding", fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(onClick = { encodingExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedEncoding.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            DropdownMenu(expanded = encodingExpanded, onDismissRequest = { encodingExpanded = false }) {
                                TextImportEncodingCatalog.options.forEach { encoding ->
                                    DropdownMenuItem(
                                        text = { Text(encoding.label) },
                                        onClick = {
                                            encodingExpanded = false
                                            onTextOptionsChange(textOptions.copy(encodingId = encoding.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    PdfImportToggleRow(
                        title = "Clean repeated headers and footers",
                        checked = options.cleanupRepeatedLines,
                        onCheckedChange = { onOptionsChange(options.copy(cleanupRepeatedLines = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Remove page numbers",
                        checked = options.removePageNumbers,
                        onCheckedChange = { onOptionsChange(options.copy(removePageNumbers = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Repair hyphenated line breaks",
                        checked = options.repairHyphenation,
                        onCheckedChange = { onOptionsChange(options.copy(repairHyphenation = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Include page markers",
                        checked = options.includePageMarkers,
                        onCheckedChange = { onOptionsChange(options.copy(includePageMarkers = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Match original layout where possible",
                        checked = options.markPdfLinesForCanvas,
                        onCheckedChange = { onOptionsChange(options.copy(markPdfLinesForCanvas = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Remove top page noise",
                        checked = options.removeTopPageNoise,
                        onCheckedChange = { onOptionsChange(options.copy(removeTopPageNoise = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Remove bottom page noise",
                        checked = options.removeBottomPageNoise,
                        onCheckedChange = { onOptionsChange(options.copy(removeBottomPageNoise = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Manual crop before extract",
                        checked = options.manualCropBeforeExtract,
                        onCheckedChange = { onOptionsChange(options.copy(manualCropBeforeExtract = it)) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = options.minWordGap,
                        onValueChange = { value ->
                            onOptionsChange(options.copy(minWordGap = value.filter { it.isDigit() || it == '.' }.take(6).ifBlank { "0.1" }))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Minimum word gap") },
                        singleLine = true
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Separate words when font changes",
                        checked = options.separateWordsOnFontChange,
                        onCheckedChange = { onOptionsChange(options.copy(separateWordsOnFontChange = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Prefer OCR when PDF text is weak",
                        checked = options.preferOcrWhenLowText,
                        onCheckedChange = { onOptionsChange(options.copy(preferOcrWhenLowText = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Force OCR for PDF import",
                        checked = options.forceOcr,
                        onCheckedChange = { onOptionsChange(options.copy(forceOcr = it)) }
                    )
                }
                item {
                    PdfImportToggleRow(
                        title = "Force fresh extraction",
                        checked = options.forceFreshExtraction,
                        onCheckedChange = { onOptionsChange(options.copy(forceFreshExtraction = it)) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onPickPdf) { Text("Open file browser") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun PdfImportToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun Menu.addReaderSelectionFeature(
    itemId: Int,
    order: Int,
    title: String,
    feature: ResolvedVeritasFeature,
    showAsAction: Int? = null
) {
    val item = add(0, itemId, order, title)
    item.isEnabled = feature.enabled
    if (showAsAction != null) {
        item.setShowAsAction(showAsAction)
    }
}

private const val READER_SELECTION_READ_FROM_HERE = 6101
private const val READER_SELECTION_NOTE = 6103
private const val READER_SELECTION_BOOKMARK = 6104

@Composable
private fun SyncCenterDialog(
    documentCount: Int,
    annotationCount: Int,
    queueCount: Int,
    pronunciationRuleCount: Int,
    inProgress: Boolean,
    message: String?,
    onExportSyncPack: () -> Unit,
    onShareSyncPack: () -> Unit,
    onImportSyncPack: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Sync") },
        text = {
            Column(
                modifier = Modifier.height(560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⇅", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Manual sync pack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                Text("Move your library between devices now. Drive login comes later.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            "Export a sync file, share it through Google Drive, WhatsApp, Files, Nearby Share, or any app, then import it on another device. Import merges safely and does not delete local readings.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("What the sync file includes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                        SyncInfoRow("Saved readings", documentCount.toString())
                        SyncInfoRow("Bookmarks and notes", annotationCount.toString())
                        SyncInfoRow("Listen Later queue", queueCount.toString())
                        SyncInfoRow("Pronunciation rules", pronunciationRuleCount.toString())
                        Text(
                            "It also includes progress, collections, favorites, reader settings, voice settings, narration settings, AI prompt templates, and AI prompt history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Manual sync flow", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                        Button(onClick = onExportSyncPack, modifier = Modifier.fillMaxWidth(), enabled = documentCount > 0) {
                            Text("Export sync file")
                        }
                        OutlinedButton(onClick = onShareSyncPack, modifier = Modifier.fillMaxWidth(), enabled = documentCount > 0) {
                            Text("Share to Drive / WhatsApp / Files")
                        }
                        OutlinedButton(onClick = onImportSyncPack, modifier = Modifier.fillMaxWidth()) {
                            Text("Import sync file from another device")
                        }
                        BackupStatusBlock(inProgress = inProgress, message = message)
                        Text(
                            "Safe merge is always used in this beta. Existing local readings are kept unless you delete them manually.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Future cloud sync", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                        Text(
                            "Google Drive sign-in, automatic upload, and restore controls are intentionally left out of this beta until OAuth and conflict handling are ready.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SyncInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        SoftChip(value)
    }
}

@Composable
private fun BackupRestoreDialog(
    documentCount: Int,
    annotationCount: Int,
    queueCount: Int,
    inProgress: Boolean,
    message: String?,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Backup & restore") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Export a portable Veritas backup, or import a backup into this library. Import adds restored readings without deleting the current library.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Current library", fontWeight = FontWeight.Black)
                        Text("$documentCount readings • $annotationCount bookmarks/notes • $queueCount queued", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Button(onClick = onExport, modifier = Modifier.fillMaxWidth(), enabled = documentCount > 0) {
                    Text("Export library backup")
                }
                OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                    Text("Import backup file")
                }
                BackupStatusBlock(inProgress = inProgress, message = message)
                Text(
                    "Backup includes saved text, progress, queue, reading lists, bookmarks, document notes, sentence notes, pronunciation rules, reader settings, and voice settings.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
private fun BackupStatusBlock(
    inProgress: Boolean,
    message: String?
) {
    if (!inProgress && message.isNullOrBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (inProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Working on backup file...", style = MaterialTheme.typography.bodySmall)
            }
            if (!message.isNullOrBlank()) {
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TranslationToolsDialog(
    document: ReaderDocument,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSend: (String, TranslationLauncher.Mode) -> Unit
) {
    var targetLanguage by remember { mutableStateOf("English") }
    val currentPreview = document.chunks.getOrNull(currentIndex).orEmpty().take(180)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("⇄") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Use the translation or AI apps already installed on this phone. Veritas prepares the prompt and opens the Android share sheet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = targetLanguage,
                    onValueChange = { targetLanguage = it },
                    label = { Text("Target language") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (currentPreview.isNotBlank()) {
                    Text(
                        "Current sentence: $currentPreview${if (document.chunks.getOrNull(currentIndex).orEmpty().length > 180) "…" else ""}",
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { onSend(targetLanguage, TranslationLauncher.Mode.CURRENT_SECTION) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Translate current sentence") }
                OutlinedButton(
                    onClick = { onSend(targetLanguage, TranslationLauncher.Mode.DOCUMENT) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Translate full document") }
                OutlinedButton(
                    onClick = { onSend(targetLanguage, TranslationLauncher.Mode.BILINGUAL_SECTION) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Bilingual current sentence") }
                OutlinedButton(
                    onClick = { onSend(targetLanguage, TranslationLauncher.Mode.BILINGUAL_DOCUMENT) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Bilingual full document") }
            }
        }
    )
}


@Composable
private fun AiFreeModeDialog(
    documentCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("AI & study mode") },
        text = {
            Column(
                modifier = Modifier.height(520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Free AI approach", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text("Veritas does not bundle a large offline model and does not require your OpenAI API key. It uses installed AI apps on the phone plus lightweight offline study tools.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                StudyCard(title = "1. AI app handoff") {
                    Text("Open a document, tap Reader tools → AI, choose a task, then send the prepared prompt to ChatGPT, Gemini, Claude, Copilot, Perplexity, or another installed app.")
                }
                StudyCard(title = "2. Offline study tools") {
                    Text("For no-internet revision, Veritas can create simple summaries, key points, terms, flashcards, and quizzes using local document logic. This is smaller but less powerful than cloud AI.")
                }
                StudyCard(title = "3. Base app stays lighter") {
                    Text("No heavy local AI model is bundled in the base app. A real offline model can be optional later as a separate downloadable pack.")
                }
                Text("Current library: $documentCount saved reading${if (documentCount == 1) "" else "s"}.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun AiCenterDialog(
    installedAiCount: Int,
    documentCount: Int,
    onOpenAskAiSettings: () -> Unit,
    onOpenStudyTools: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("AI tools") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Use installed AI apps or local study tools without adding paid APIs or account-gated services inside Veritas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("$installedAiCount compatible AI app${if (installedAiCount == 1) "" else "s"} detected.")
                Text("$documentCount reading${if (documentCount == 1) "" else "s"} available for study workflows.")
                Button(onClick = onOpenStudyTools, modifier = Modifier.fillMaxWidth()) {
                    Text("Open AI Study Tools")
                }
                OutlinedButton(onClick = onOpenAskAiSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Ask AI app settings")
                }
            }
        }
    )
}

@Composable
private fun ExportAudioStatusDialog(
    inProgress: Boolean,
    message: String?,
    file: File?,
    onShare: (File) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!inProgress) onDismiss() },
        title = { Text("Export audio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (inProgress) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Creating audio from this reading...")
                    }
                }
                Text(message ?: "Preparing export...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (file != null) {
                    Text(file.name, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            if (file != null) {
                Button(onClick = { onShare(file) }) { Text("Share") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !inProgress) { Text("Close") }
        }
    )
}

@Composable
private fun AiAppStudyDialog(
    document: ReaderDocument,
    currentIndex: Int,
    templates: List<AiPromptTemplate>,
    history: List<AiPromptHistoryEntry>,
    onDismiss: () -> Unit,
    onSendToAiApp: (AiPromptType, String, AiPromptScope) -> Unit,
    onSaveTemplate: (String, String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onClearHistory: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onSaveAiResultAsNote: (String) -> Unit,
    onOpenOfflineStudyTools: () -> Unit
) {
    var customPrompt by remember { mutableStateOf("") }
    var templateTitle by remember { mutableStateOf("Custom study prompt") }
    var aiResultDraft by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Tasks") }
    val safeIndex = if (document.chunks.isEmpty()) 0 else currentIndex.coerceIn(0, document.chunks.lastIndex)
    val estimatedTextLength = document.chunks.sumOf { it.length }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("AI handoff") },
        text = {
            Column(
                modifier = Modifier
                    .height(560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Use installed AI apps for free-tier summaries and study help. Veritas prepares the prompt, then you choose ChatGPT, Gemini, Claude, Copilot, Perplexity, or another app.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(document.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                        Text(
                            "${document.sourceLabel} • ${document.chunks.size} sentences • current ${safeIndex + 1} • about $estimatedTextLength characters",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    listOf("Tasks", "Templates", "History", "Result → note").forEach { tab ->
                        val selected = selectedTab == tab
                        if (selected) {
                            Button(onClick = { selectedTab = tab }) { Text(tab) }
                        } else {
                            OutlinedButton(onClick = { selectedTab = tab }) { Text(tab) }
                        }
                    }
                }

                when (selectedTab) {
                    "Tasks" -> {
                        Button(
                            onClick = { onSendToAiApp(AiPromptType.SUMMARY, "", AiPromptScope.CURRENT_SECTION) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Summarize current sentence") }

                        OutlinedButton(
                            onClick = { onSendToAiApp(AiPromptType.SUMMARY, "", AiPromptScope.WHOLE_DOCUMENT) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Summarize whole document") }

                        OutlinedButton(
                            onClick = { onSendToAiApp(AiPromptType.SECTION_BY_SECTION, "", AiPromptScope.CURRENT_SECTION) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Long document: summarize this sentence") }

                        OutlinedButton(
                            onClick = { onSendToAiApp(AiPromptType.KEY_POINTS, "", AiPromptScope.WHOLE_DOCUMENT) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Extract key points") }

                        OutlinedButton(
                            onClick = { onSendToAiApp(AiPromptType.EXPLAIN_SECTION, "", AiPromptScope.CURRENT_SECTION) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Explain current sentence") }

                        OutlinedButton(
                            onClick = { onSendToAiApp(AiPromptType.STUDY_NOTES, "", AiPromptScope.WHOLE_DOCUMENT) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Create study notes") }

                        OutlinedButton(
                            onClick = { onSendToAiApp(AiPromptType.SIMPLIFY, "", AiPromptScope.WHOLE_DOCUMENT) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Simplify difficult text") }

                        OutlinedButton(
                            onClick = { onSendToAiApp(AiPromptType.QUIZ, "", AiPromptScope.WHOLE_DOCUMENT) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Create revision quiz") }

                        OutlinedButton(
                            onClick = { onSendToAiApp(AiPromptType.FLASHCARDS, "", AiPromptScope.WHOLE_DOCUMENT) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Create flashcards") }

                        TextButton(onClick = onOpenOfflineStudyTools, modifier = Modifier.fillMaxWidth()) {
                            Text("Use offline study tools instead")
                        }
                    }
                    "Templates" -> {
                        OutlinedTextField(
                            value = templateTitle,
                            onValueChange = { templateTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Template title") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customPrompt,
                            onValueChange = { customPrompt = it },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            label = { Text("Custom instruction") },
                            placeholder = { Text("Example: Explain this like I am preparing for an exam, then give likely questions.") }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onSendToAiApp(AiPromptType.CUSTOM, customPrompt, AiPromptScope.WHOLE_DOCUMENT) },
                                enabled = customPrompt.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Send") }
                            OutlinedButton(
                                onClick = { onSaveTemplate(templateTitle, customPrompt) },
                                enabled = customPrompt.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Save") }
                        }
                        HorizontalDivider()
                        if (templates.isEmpty()) {
                            Text("Saved custom prompts will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            templates.forEach { template ->
                                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(template.title, fontWeight = FontWeight.Black)
                                        Text(template.instruction, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            TextButton(onClick = { onSendToAiApp(AiPromptType.CUSTOM, template.instruction, AiPromptScope.WHOLE_DOCUMENT) }) { Text("Use") }
                                            TextButton(onClick = { onCopyText("Veritas AI template", template.instruction) }) { Text("Copy") }
                                            TextButton(onClick = { onDeleteTemplate(template.id) }) { Text("Delete") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "History" -> {
                        if (history.isEmpty()) {
                            Text("Prompts you send to AI apps will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = onClearHistory) { Text("Clear history") }
                            }
                            history.take(12).forEach { item ->
                                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${item.promptType} • ${item.scope}", fontWeight = FontWeight.Black)
                                        Text(item.documentTitle, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(item.promptPreview, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
                                        Text(formatUpdated(item.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        TextButton(onClick = { onCopyText("Veritas AI prompt preview", item.promptPreview) }) { Text("Copy preview") }
                                    }
                                }
                            }
                        }
                    }
                    "Result → note" -> {
                        Text(
                            "After the AI app replies, copy its answer, return here, paste it below, and save it as a note on the current sentence.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = aiResultDraft,
                            onValueChange = { aiResultDraft = it },
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            label = { Text("Paste AI result") },
                            placeholder = { Text("Paste summary, explanation, quiz answer, or study notes here…") }
                        )
                        Button(
                            onClick = {
                                onSaveAiResultAsNote(aiResultDraft)
                                aiResultDraft = ""
                            },
                            enabled = aiResultDraft.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save result to current-sentence note") }
                    }
                }

                Text(
                    "For long documents, use the sentence-by-sentence button repeatedly as you move through the reader. Whole-document prompts may be shortened by Android share limits.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
private fun StudyToolsDialog(
    studyPack: StudyPack,
    onDismiss: () -> Unit
) {
    var tab by remember { mutableStateOf("Summary") }
    val tabs = listOf("Summary", "Points", "Terms", "Cards", "Quiz", "Sentence")
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Study tools") },
        text = {
            Column(
                modifier = Modifier
                    .height(500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Offline study help generated from this document. It is meant for revision, not as a final authority.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.take(3).forEach { item ->
                        if (tab == item) Button(onClick = { tab = item }) { Text(item) }
                        else OutlinedButton(onClick = { tab = item }) { Text(item) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.drop(3).forEach { item ->
                        if (tab == item) Button(onClick = { tab = item }) { Text(item) }
                        else OutlinedButton(onClick = { tab = item }) { Text(item) }
                    }
                }

                when (tab) {
                    "Summary" -> StudyListBlock(
                        title = "Document summary",
                        emptyText = "No summary could be generated.",
                        items = studyPack.summary
                    )
                    "Points" -> StudyListBlock(
                        title = "Key points",
                        emptyText = "No key points could be generated.",
                        items = studyPack.keyPoints
                    )
                    "Terms" -> StudyListBlock(
                        title = "Key terms",
                        emptyText = "No key terms could be detected.",
                        items = studyPack.keyTerms
                    )
                    "Cards" -> {
                        Text("Flashcards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (studyPack.flashcards.isEmpty()) {
                            Text("No flashcards could be generated.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            studyPack.flashcards.forEachIndexed { index, card ->
                                StudyCard(title = "Card ${index + 1}") {
                                    Text(card.front, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(card.back, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    "Quiz" -> {
                        Text("Quick quiz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (studyPack.quiz.isEmpty()) {
                            Text("No quiz could be generated.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            studyPack.quiz.forEachIndexed { index, question ->
                                StudyCard(title = "Question ${index + 1}") {
                                    Text(question.question, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    question.options.forEach { option ->
                                        Text("• $option")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Answer: ${question.answer}", fontWeight = FontWeight.Bold)
                                    Text(question.explanation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    "Sentence" -> StudyListBlock(
                        title = "Current sentence explained",
                        emptyText = "No sentence explanation could be generated.",
                        items = studyPack.currentSectionExplanation
                    )
                }
            }
        }
    )
}

@Composable
private fun StudyListBlock(
    title: String,
    emptyText: String,
    items: List<String>
) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    if (items.isEmpty()) {
        Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        items.forEachIndexed { index, item ->
            StudyCard(title = "${index + 1}") {
                Text(item, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun StudyCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
            content()
        }
    }
}


data class VoicePreset(
    val name: String,
    val rate: Float,
    val pitch: Float,
    val summary: String
)

fun voicePresets(): List<VoicePreset> = listOf(
    VoicePreset("Balanced", 1.0f, 1.0f, "Everyday reading with neutral timing."),
    VoicePreset("Study focus", 0.88f, 0.98f, "Slower pacing for dense material and note taking."),
    VoicePreset("Quick scan", 1.35f, 1.02f, "Fast skim for review and familiar documents."),
    VoicePreset("Story warm", 0.96f, 0.92f, "Softer narration for fiction and long listening."),
    VoicePreset("Clear lecture", 1.06f, 1.06f, "Brighter delivery for technical or academic text."),
    VoicePreset("Calm night", 0.82f, 0.90f, "Low, relaxed reading for quiet listening.")
)







private fun textEditorDownloadName(document: ReaderDocument, target: VeritasTextEditTarget): String {
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

private fun countSearchOccurrences(source: String, query: String): Int {
    val needle = query.trim()
    if (source.isBlank() || needle.isBlank()) return 0
    var count = 0
    var cursor = 0
    while (cursor <= source.length - needle.length) {
        val found = source.indexOf(needle, startIndex = cursor, ignoreCase = true)
        if (found < 0) break
        count++
        cursor = found + needle.length.coerceAtLeast(1)
        if (count >= 500) break
    }
    return count
}

private fun openOriginalDocument(
    context: Context,
    repository: DocumentRepository,
    document: SavedDocument
) {
    val original = repository.originalFile(document)
    if (original == null) {
        Toast.makeText(context, "No stored original is available for this reading.", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = runCatching {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", original)
    }.getOrNull()
    if (uri == null) {
        Toast.makeText(context, "Could not prepare the original file.", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, document.originalMimeType.ifBlank { "application/octet-stream" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Open original document"))
    }.onFailure {
        Toast.makeText(context, "No app can open this original document.", Toast.LENGTH_SHORT).show()
    }
}

private fun openAllFilesAccessSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        Toast.makeText(context, "All Files access is already available on this Android version.", Toast.LENGTH_SHORT).show()
        return
    }
    val appIntent = Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        "package:${context.packageName}".toUri()
    )
    val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    runCatching {
        context.startActivity(appIntent)
    }.onFailure {
        runCatching { context.startActivity(fallback) }
            .onFailure {
                Toast.makeText(context, "Could not open All Files access settings.", Toast.LENGTH_LONG).show()
            }
    }
}

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

@Composable
private fun TextEditorDialog(
    document: ReaderDocument,
    currentIndex: Int,
    text: String,
    target: VeritasTextEditTarget,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDownloadToPhone: () -> Unit,
    onDismiss: () -> Unit
) {
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var editorValue by remember(target) { mutableStateOf(TextFieldValue(text, selection = TextRange(text.length))) }
    var undoStack by remember(target) { mutableStateOf<List<TextFieldValue>>(emptyList()) }
    var redoStack by remember(target) { mutableStateOf<List<TextFieldValue>>(emptyList()) }
    val scopeLabel = when (target) {
        is VeritasTextEditTarget.SentenceRange -> target.label
        is VeritasTextEditTarget.Part -> target.label
    }
    val searchMatches = remember(editorValue.text, searchQuery) { countSearchOccurrences(editorValue.text, searchQuery) }

    LaunchedEffect(text, target) {
        if (text != editorValue.text) {
            val safeSelection = TextRange(text.length)
            editorValue = TextFieldValue(text, selection = safeSelection)
        }
    }

    fun commitValue(next: TextFieldValue) {
        if (next.text == editorValue.text && next.selection == editorValue.selection) {
            editorValue = next
            return
        }
        undoStack = (undoStack + editorValue).takeLast(80)
        redoStack = emptyList()
        editorValue = next
        onTextChange(next.text)
    }

    fun replaceSelection(prefix: String, suffix: String = prefix, placeholder: String = "") {
        val value = editorValue
        val start = value.selection.min.coerceIn(0, value.text.length)
        val end = value.selection.max.coerceIn(0, value.text.length)
        val selected = value.text.substring(start, end).ifBlank { placeholder }
        val replacement = "$prefix$selected$suffix"
        val nextText = value.text.replaceRange(start, end, replacement)
        val cursorStart = start + prefix.length
        val cursorEnd = cursorStart + selected.length
        commitValue(TextFieldValue(nextText, selection = TextRange(cursorStart, cursorEnd)))
    }

    fun findNextSearchMatch() {
        val needle = searchQuery.trim()
        if (needle.isBlank()) return
        val start = editorValue.selection.max.coerceIn(0, editorValue.text.length)
        val first = editorValue.text.indexOf(needle, startIndex = start, ignoreCase = true)
        val match = if (first >= 0) first else editorValue.text.indexOf(needle, ignoreCase = true)
        if (match >= 0) {
            editorValue = editorValue.copy(selection = TextRange(match, match + needle.length))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) { Text("←", style = MaterialTheme.typography.titleLarge) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Edit text", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text(document.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showSearch = !showSearch }) { Text("⌕") }
                    TextButton(onClick = onDownloadToPhone, enabled = editorValue.text.isNotBlank()) { Text("⇩") }
                    Button(onClick = onSave, enabled = editorValue.text.isNotBlank()) { Text("Save") }
                }
                if (showSearch) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it.take(120) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Search in edited text") }
                        )
                        Text("$searchMatches", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = ::findNextSearchMatch, enabled = searchMatches > 0) { Text("Next") }
                    }
                }
                Text(
                    "Editing $scopeLabel • current sentence ${currentIndex + 1}/${document.chunks.size.coerceAtLeast(1)}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                BasicTextField(
                    value = editorValue,
                    onValueChange = { next ->
                        commitValue(next)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = (MaterialTheme.typography.bodyLarge.fontSize.value + 8).sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.94f))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = { showSearch = !showSearch }) { Text("⌕", color = MaterialTheme.colorScheme.inverseOnSurface) }
                    TextButton(onClick = { replaceSelection("<p>\n", "\n</p>", "Paragraph") }) { Text("<p>", color = MaterialTheme.colorScheme.inverseOnSurface) }
                    TextButton(onClick = { replaceSelection("_") }, enabled = editorValue.text.isNotBlank()) { Text("I", color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold) }
                    TextButton(onClick = { replaceSelection("**") }, enabled = editorValue.text.isNotBlank()) { Text("B", color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Black) }
                    TextButton(onClick = { replaceSelection("<u>", "</u>") }, enabled = editorValue.text.isNotBlank()) { Text("U", color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold) }
                    TextButton(
                        onClick = {
                            val previous = undoStack.lastOrNull() ?: return@TextButton
                            undoStack = undoStack.dropLast(1)
                            redoStack = (redoStack + editorValue).takeLast(80)
                            editorValue = previous
                            onTextChange(previous.text)
                        },
                        enabled = undoStack.isNotEmpty()
                    ) { Text("↶", color = MaterialTheme.colorScheme.inverseOnSurface) }
                    TextButton(
                        onClick = {
                            val next = redoStack.lastOrNull() ?: return@TextButton
                            redoStack = redoStack.dropLast(1)
                            undoStack = (undoStack + editorValue).takeLast(80)
                            editorValue = next
                            onTextChange(next.text)
                        },
                        enabled = redoStack.isNotEmpty()
                    ) { Text("↷", color = MaterialTheme.colorScheme.inverseOnSurface) }
                    TextButton(onClick = { replaceSelection("`") }, enabled = editorValue.text.isNotBlank()) { Text("</>", color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold) }
                    TextButton(onClick = onDownloadToPhone, enabled = editorValue.text.isNotBlank()) { Text("▣", color = MaterialTheme.colorScheme.inverseOnSurface) }
                }
            }
        }
    }
}

@Composable
private fun TutorialDialog(
    initialName: String,
    onDismiss: (String) -> Unit,
    onImport: (String) -> Unit,
    onVoice: (String) -> Unit,
    onThemes: (String) -> Unit
) {
    val context = LocalContext.current
    var nameDraft by rememberSaveable { mutableStateOf(initialName) }
    var ttsReady by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val steps = remember {
        listOf(
            TutorialFrame("1", "Welcome to Veritas Reader", "Transform your research, documents, and reading materials into focused audio experiences.", "V", null),
            TutorialFrame("2", "What should we call you?", "Your name personalizes the dashboard and reading experience.", "Aa", null),
            TutorialFrame("3", "Add a reading", "Import a document, paste text, or share a web page into Veritas.", "＋", { onImport(nameDraft) }),
            TutorialFrame("4", "Read your way", "Switch between extracted text and actual document pages when you need the original layout.", "▣", null),
            TutorialFrame("5", "Listen", "Pick voices, tune playback, and control reading from the bottom player or notification.", "▶", { onVoice(nameDraft) }),
            TutorialFrame("6", "Mark and remember", "Bookmark a sentence to highlight it, add notes, translate, search, or ask AI.", "★", null),
            TutorialFrame("7", "Make it yours", "Choose theme packs, text size, spacing, widgets, and sync options.", "Aa", { onThemes(nameDraft) }),
            TutorialFrame("8", "Ready to read?", "Your calm reading environment is configured. Open the dashboard and import your first file.", "✓", null)
        )
    }
    var stepIndex by remember { mutableIntStateOf(0) }
    val pulse by animateFloatAsState(
        targetValue = if (stepIndex % 2 == 0) 1.08f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tutorialPulse"
    )
    DisposableEffect(context) {
        val engine = TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }
    LaunchedEffect(stepIndex, ttsReady, nameDraft) {
        if (ttsReady) {
            val greeting = nameDraft.trim().ifBlank { "reader" }
            val frame = steps[stepIndex]
            val spoken = if (stepIndex == 0) {
                "Welcome to Veritas Reader. Transform your research, documents, and reading materials into focused audio experiences."
            } else if (stepIndex == 1) {
                "What should we call you? This name will appear on your dashboard."
            } else if (stepIndex == steps.lastIndex) {
                "Ready to read, $greeting. Your setup is complete."
            } else {
                "${frame.title}. ${frame.body}"
            }
            tts?.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "veritas-onboarding-$stepIndex")
        }
    }
    Dialog(
        onDismissRequest = { onDismiss(nameDraft) },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Veritas setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onDismiss(nameDraft) }) { Text("Skip") }
                }
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    if (stepIndex == 0) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(112.dp)
                                    .graphicsLayer(scaleX = pulse, scaleY = pulse)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                BrandMark(compact = true)
                            }
                        }
                        Text(
                            "Welcome to Veritas Reader",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Transform your research, documents, and reading materials into high-quality, focused audio experiences.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (stepIndex == 1) {
                        Text("What should we call you?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        OutlinedTextField(
                            value = nameDraft,
                            onValueChange = { nameDraft = it.take(48) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your preferred name") },
                            placeholder = { Text("Name for the dashboard welcome") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Text(
                            "This is used to personalize your dashboard and reading experience.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (stepIndex >= 2) {
                        TutorialStage(
                            frame = steps[stepIndex],
                            progress = (stepIndex + 1).toFloat() / steps.size.toFloat(),
                            pulse = pulse
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .width(if (index == stepIndex) 28.dp else 8.dp)
                                    .height(8.dp)
                                    .background(
                                        if (index == stepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    )
                                    .clickable { stepIndex = index }
                            )
                        }
                    }
                    steps.drop(2).dropLast(1).forEachIndexed { offset, frame ->
                        val index = offset + 2
                        TutorialStep(
                            number = frame.number,
                            title = frame.title,
                            body = frame.body,
                            action = frame.action,
                            selected = index == stepIndex,
                            onSelect = { stepIndex = index }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { stepIndex = (stepIndex - 1).coerceAtLeast(0) },
                        enabled = stepIndex > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) { Text("Back") }
                    Button(
                        onClick = {
                            if (stepIndex >= steps.lastIndex) onDismiss(nameDraft) else stepIndex += 1
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(if (stepIndex >= steps.lastIndex) "Go to Dashboard" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialStage(frame: TutorialFrame, progress: Float, pulse: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .graphicsLayer(scaleX = pulse, scaleY = pulse)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(frame.icon, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                        .fillMaxWidth(0.62f)
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            Text(frame.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text(frame.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (frame.action != null) {
                TextButton(onClick = frame.action) { Text("Open") }
            }
        }
    }
}

@Composable
private fun TutorialStep(number: String, title: String, body: String, action: (() -> Unit)?, selected: Boolean, onSelect: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tutorialStepBounce"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        )
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(number, fontWeight = FontWeight.Black) }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Black)
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (action != null) {
                TextButton(onClick = action) { Text("Open") }
            }
        }
    }
}

private data class TutorialFrame(
    val number: String,
    val title: String,
    val body: String,
    val icon: String,
    val action: (() -> Unit)?
)

@Composable
private fun FloatingRecordOverlay(
    inProgress: Boolean,
    fileReady: Boolean,
    awaitingDecision: Boolean,
    elapsedSeconds: Long,
    onStopRecording: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val elapsed = formatRecordElapsed(elapsedSeconds)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(22.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
            shape = RoundedCornerShape(if (awaitingDecision) 24.dp else 18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            if (awaitingDecision) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecordPillDot(inProgress = inProgress)
                    if (fileReady) {
                        TextButton(onClick = onSave) { Text("Save") }
                        TextButton(onClick = onDiscard) { Text("Discard") }
                    } else {
                        Text(
                            if (inProgress) "Finishing…" else "No audio yet",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = onDiscard) { Text("Discard") }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .clickable { onStopRecording() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecordPillDot(
                        inProgress = inProgress,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        elapsed,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordPillDot(
    inProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(if (inProgress) Color(0xFFE53935) else MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (inProgress) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(Color.White, CircleShape)
            )
        } else {
            Text("✓", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatRecordElapsed(seconds: Long): String {
    val safeSeconds = seconds.coerceAtLeast(0L)
    val minutes = safeSeconds / 60L
    val remainingSeconds = safeSeconds % 60L
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Composable
private fun AppHealthDialog(
    documentCount: Int,
    queueCount: Int,
    themePackName: String,
    themeName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("App health") },
        text = {
            Column(
                modifier = Modifier.height(560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Veritas Reader beta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text("Version: 0.3.7-beta-release-hardening", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Library: $documentCount reading${if (documentCount == 1) "" else "s"} • $queueCount queued", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Theme: $themePackName / $themeName", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                ReadinessBlock(
                    title = "Permissions review",
                    items = listOf(
                        "Notifications are only used for media playback controls.",
                        "Foreground service is used for reading aloud while the app is outside the screen.",
                        "Document access uses Android file pickers and shared files; no broad storage permission is required.",
                        "Internet is not required for core reading. AI handoff uses installed apps through Android sharing."
                    )
                )

                ReadinessBlock(
                    title = "Before sharing an APK",
                    items = listOf(
                        "Run the final QA checklist on a physical phone.",
                        "Test import, playback, background controls, OCR, export, sync pack merge, notes export, and AI handoff.",
                        "Make a backup copy of this project folder once it passes.",
                        "Use a debug APK for private testing only. Use a signed release APK/AAB for wider distribution."
                    )
                )

                ReadinessBlock(
                    title = "Current limits",
                    items = listOf(
                        "Google Drive login is planned for a later OAuth pass.",
                        "Long scanned PDFs and WAV exports may be slow on low-memory phones.",
                        "Canvas/source view requires newly imported originals to be preserved.",
                        "AI features depend on installed AI apps or the small offline helper."
                    )
                )
            }
        }
    )
}

@Composable
private fun ReadinessBlock(title: String, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
            items.forEach { item ->
                Text("• $item", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}



object VeritasPackStyle {
    @Composable
    fun currentPackId(): String = VeritasThemePackCatalog.normalizePackId(VeritasThemeState.themePackId)

    @Composable
    fun cardShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(34.dp)
        "liquid_glass" -> RoundedCornerShape(42.dp)
        "one_ui" -> RoundedCornerShape(28.dp)
        else -> RoundedCornerShape(18.dp)
    }

    @Composable
    fun compactShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(28.dp)
        "liquid_glass" -> RoundedCornerShape(34.dp)
        "one_ui" -> RoundedCornerShape(18.dp)
        else -> RoundedCornerShape(12.dp)
    }

    @Composable
    fun chipShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(50)
        "liquid_glass" -> RoundedCornerShape(36.dp)
        "one_ui" -> RoundedCornerShape(16.dp)
        else -> RoundedCornerShape(10.dp)
    }

    @Composable
    fun surfaceAlpha(): Float = when (currentPackId()) {
        "liquid_glass" -> 0.42f
        "one_ui" -> 0.94f
        "material_you" -> 0.88f
        else -> 0.78f
    }

    @Composable
    fun backgroundBrush(colorScheme: ColorScheme): Brush = when (currentPackId()) {
        "liquid_glass" -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.86f),
                colorScheme.primaryContainer.copy(alpha = 0.22f),
                colorScheme.background
            )
        )
        "one_ui" -> Brush.verticalGradient(
            listOf(
                colorScheme.secondaryContainer.copy(alpha = 0.20f),
                colorScheme.background,
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.72f)
            )
        )
        "material_you" -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.76f),
                colorScheme.primaryContainer.copy(alpha = 0.22f),
                colorScheme.tertiaryContainer.copy(alpha = 0.16f),
                colorScheme.background
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.primaryContainer.copy(alpha = 0.08f),
                colorScheme.background,
                colorScheme.secondaryContainer.copy(alpha = 0.10f)
            )
        )
    }

    @Composable
    fun label(): String = VeritasThemePackCatalog.displayName(currentPackId())
}



fun packPreviewSymbols(packId: String): String = when (VeritasThemePackCatalog.normalizePackId(packId)) {
    "material_you" -> "rounded • adaptive • soft"
    "liquid_glass" -> "translucent • floating • glossy"
    "one_ui" -> "large • reachable • calm"
    else -> "media • compact • bold"
}




fun themePreviewColors(themeId: String): List<Color> {
    return when (VeritasThemeCatalog.normalizeThemeId(themeId)) {
        "light" -> listOf(Color(0xFFF7F9FB), Color(0xFF182442), Color(0xFFD0E1FB))
        "neon" -> listOf(Color(0xFF080D12), Color(0xFF7DD3FC), Color(0xFFA7F3D0))
        "solarized_dark" -> listOf(Color(0xFF002B36), Color(0xFF5FC8BF), Color(0xFFD7A84A))
        "tomorrow_night_blue" -> listOf(Color(0xFF071B37), Color(0xFFA9C7FF), Color(0xFFE9B872))
        "dark_high_contrast" -> listOf(Color.Black, Color.White, Color(0xFFFFD400))
        "white_high_contrast" -> listOf(Color.White, Color.Black, Color(0xFF004B65))
        "bw_gradient_light" -> listOf(Color(0xFFFFFFFF), Color(0xFF111111), Color(0xFFE6E6E6))
        "bw_gradient_dark" -> listOf(Color(0xFF000000), Color(0xFFF5F5F5), Color(0xFF242424))
        "blue_high_contrast" -> listOf(Color(0xFF001B3A), Color(0xFFBDE9FF), Color(0xFFFFF176))
        "one_dark_pro" -> listOf(Color(0xFF242A33), Color(0xFF86BFF2), Color(0xFFB7D99A))
        "github_dark" -> listOf(Color(0xFF0D1117), Color(0xFF7BB6FF), Color(0xFF69C779))
        "github_light" -> listOf(Color(0xFFF6F8FA), Color(0xFF075EB8), Color(0xFF1F7A3A))
        "dracula" -> listOf(Color(0xFF252837), Color(0xFFBDA8FF), Color(0xFFFFB3D5))
        "material_you" -> listOf(Color(0xFFF7F2FA), Color(0xFF5A477A), Color(0xFF6F4B57))
        "dark" -> listOf(Color(0xFF111827), Color(0xFF8EDCE6), Color(0xFFE4CCFF))
        else -> listOf(Color(0xFF0B0F14), Color(0xFF82D8E7), Color(0xFFD6C1FF))
    }
}



@Composable
fun AnnotationPill(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}






@Composable
private fun VeritasTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val selectedTheme = VeritasThemeCatalog.normalizeThemeId(VeritasThemeState.themeId)
    val selectedPack = VeritasThemePackCatalog.normalizePackId(VeritasThemeState.themePackId)
    val colorScheme = veritasPackColorScheme(
        base = veritasColorScheme(selectedTheme, context),
        packId = selectedPack
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        shapes = veritasPackShapes(selectedPack),
        content = content
    )
}

private fun veritasPackShapes(packId: String): Shapes {
    return when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "material_you" -> Shapes(
            extraSmall = RoundedCornerShape(14.dp),
            small = RoundedCornerShape(20.dp),
            medium = RoundedCornerShape(28.dp),
            large = RoundedCornerShape(36.dp),
            extraLarge = RoundedCornerShape(44.dp)
        )
        "liquid_glass" -> Shapes(
            extraSmall = RoundedCornerShape(18.dp),
            small = RoundedCornerShape(26.dp),
            medium = RoundedCornerShape(34.dp),
            large = RoundedCornerShape(44.dp),
            extraLarge = RoundedCornerShape(52.dp)
        )
        "one_ui" -> Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(22.dp),
            large = RoundedCornerShape(30.dp),
            extraLarge = RoundedCornerShape(38.dp)
        )
        else -> Shapes(
            extraSmall = RoundedCornerShape(6.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(26.dp)
        )
    }
}

private fun veritasPackColorScheme(base: ColorScheme, packId: String): ColorScheme {
    return when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "liquid_glass" -> base.copy(
            surface = base.surface.copy(alpha = 0.74f),
            surfaceVariant = base.surfaceVariant.copy(alpha = 0.58f),
            primaryContainer = base.primaryContainer.copy(alpha = 0.84f),
            secondaryContainer = base.secondaryContainer.copy(alpha = 0.72f),
            tertiaryContainer = base.tertiaryContainer.copy(alpha = 0.68f)
        )
        "one_ui" -> base.copy(
            surface = base.surface,
            surfaceVariant = base.secondaryContainer.copy(alpha = 0.94f),
            primaryContainer = base.primaryContainer.copy(alpha = 0.98f),
            tertiaryContainer = base.tertiaryContainer.copy(alpha = 0.90f)
        )
        "material_you" -> base.copy(
            surface = base.surface.copy(alpha = 0.96f),
            surfaceVariant = base.primaryContainer.copy(alpha = 0.46f),
            secondaryContainer = base.tertiaryContainer.copy(alpha = 0.90f)
        )
        else -> base
    }
}

@Composable
private fun veritasColorScheme(themeId: String, context: Context): ColorScheme {
    return when (VeritasThemeCatalog.normalizeThemeId(themeId)) {
        "light" -> lightColorScheme(
            primary = Color(0xFF182442),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF2E3A59),
            onPrimaryContainer = Color(0xFF98A4C9),
            secondary = Color(0xFF505F76),
            secondaryContainer = Color(0xFFD0E1FB),
            onSecondaryContainer = Color(0xFF54647A),
            tertiary = Color(0xFF1D2538),
            tertiaryContainer = Color(0xFF323A4F),
            onTertiaryContainer = Color(0xFF9DA4BD),
            background = Color(0xFFF7F9FB),
            surface = Color(0xFFF7F9FB),
            surfaceVariant = Color(0xFFE0E3E5),
            onSurface = Color(0xFF191C1E),
            onSurfaceVariant = Color(0xFF45464E),
            outline = Color(0xFF75777E),
            outlineVariant = Color(0xFFC6C6CE),
            error = Color(0xFFBA1A1A),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF93000A)
        )
        "neon" -> darkColorScheme(
            primary = Color(0xFF7DD3FC),
            onPrimary = Color(0xFF082030),
            primaryContainer = Color(0xFF12344A),
            onPrimaryContainer = Color(0xFFD8F2FF),
            secondary = Color(0xFFA7F3D0),
            secondaryContainer = Color(0xFF123829),
            onSecondaryContainer = Color(0xFFE0FFF1),
            tertiary = Color(0xFFFDE68A),
            tertiaryContainer = Color(0xFF4A3A10),
            background = Color(0xFF080D12),
            surface = Color(0xFF101820),
            surfaceVariant = Color(0xFF1D2A34),
            onSurface = Color(0xFFEAF2F7),
            onSurfaceVariant = Color(0xFFC2CED6)
        )
        "solarized_dark" -> darkColorScheme(
            primary = Color(0xFF2AA198),
            onPrimary = Color(0xFF002B36),
            primaryContainer = Color(0xFF004D52),
            onPrimaryContainer = Color(0xFFB7FFF8),
            secondary = Color(0xFFB58900),
            secondaryContainer = Color(0xFF4A3900),
            onSecondaryContainer = Color(0xFFFFE28A),
            tertiary = Color(0xFF6C71C4),
            tertiaryContainer = Color(0xFF2C2F60),
            background = Color(0xFF002B36),
            surface = Color(0xFF073642),
            surfaceVariant = Color(0xFF124652),
            onSurface = Color(0xFFEEE8D5),
            onSurfaceVariant = Color(0xFFB9C4BE)
        )
        "tomorrow_night_blue" -> darkColorScheme(
            primary = Color(0xFF8AB4F8),
            onPrimary = Color(0xFF071B37),
            primaryContainer = Color(0xFF1B3A63),
            onPrimaryContainer = Color(0xFFD7E7FF),
            secondary = Color(0xFFB7C7E6),
            secondaryContainer = Color(0xFF223755),
            onSecondaryContainer = Color(0xFFE3ECFF),
            tertiary = Color(0xFFFFB86C),
            tertiaryContainer = Color(0xFF573100),
            background = Color(0xFF002451),
            surface = Color(0xFF0B2D5C),
            surfaceVariant = Color(0xFF173A69),
            onSurface = Color(0xFFEAF1FF),
            onSurfaceVariant = Color(0xFFC6D1E5)
        )
        "dark_high_contrast" -> darkColorScheme(
            primary = Color(0xFFFFFFFF),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFFE2E2E2),
            onPrimaryContainer = Color.Black,
            secondary = Color(0xFFFFD400),
            secondaryContainer = Color(0xFF3A3000),
            onSecondaryContainer = Color(0xFFFFFFD1),
            tertiary = Color(0xFF00E5FF),
            tertiaryContainer = Color(0xFF003D44),
            background = Color(0xFF000000),
            surface = Color(0xFF0A0A0A),
            surfaceVariant = Color(0xFF1D1D1D),
            onSurface = Color(0xFFFFFFFF),
            onSurfaceVariant = Color(0xFFE0E0E0)
        )
        "white_high_contrast" -> lightColorScheme(
            primary = Color(0xFF000000),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDADADA),
            onPrimaryContainer = Color.Black,
            secondary = Color(0xFF004B65),
            secondaryContainer = Color(0xFFC8EFFF),
            onSecondaryContainer = Color(0xFF001E2B),
            tertiary = Color(0xFF6B3A00),
            tertiaryContainer = Color(0xFFFFDDB5),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFEFEF),
            onSurface = Color(0xFF000000),
            onSurfaceVariant = Color(0xFF202020)
        )
        "bw_gradient_light" -> lightColorScheme(
            primary = Color(0xFF111111),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE7E7E7),
            onPrimaryContainer = Color(0xFF111111),
            secondary = Color(0xFF4A4A4A),
            secondaryContainer = Color(0xFFF0F0F0),
            onSecondaryContainer = Color(0xFF161616),
            tertiary = Color(0xFF707070),
            tertiaryContainer = Color(0xFFE0E0E0),
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEDEDED),
            onSurface = Color(0xFF101010),
            onSurfaceVariant = Color(0xFF3F3F3F)
        )
        "bw_gradient_dark" -> darkColorScheme(
            primary = Color(0xFFF2F2F2),
            onPrimary = Color(0xFF090909),
            primaryContainer = Color(0xFF2D2D2D),
            onPrimaryContainer = Color(0xFFF6F6F6),
            secondary = Color(0xFFC7C7C7),
            secondaryContainer = Color(0xFF1E1E1E),
            onSecondaryContainer = Color(0xFFEDEDED),
            tertiary = Color(0xFF9A9A9A),
            tertiaryContainer = Color(0xFF252525),
            background = Color(0xFF050505),
            surface = Color(0xFF111111),
            surfaceVariant = Color(0xFF242424),
            onSurface = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFFC9C9C9)
        )
        "blue_high_contrast" -> darkColorScheme(
            primary = Color(0xFFBDE9FF),
            onPrimary = Color(0xFF001E30),
            primaryContainer = Color(0xFF00517A),
            onPrimaryContainer = Color(0xFFE9F7FF),
            secondary = Color(0xFFFFF176),
            secondaryContainer = Color(0xFF4A4500),
            onSecondaryContainer = Color(0xFFFFFBD0),
            tertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF263B67),
            background = Color(0xFF001B3A),
            surface = Color(0xFF002857),
            surfaceVariant = Color(0xFF003B7A),
            onSurface = Color(0xFFFFFFFF),
            onSurfaceVariant = Color(0xFFD9E9FF)
        )
        "one_dark_pro" -> darkColorScheme(
            primary = Color(0xFF61AFEF),
            onPrimary = Color(0xFF071D2E),
            primaryContainer = Color(0xFF18344D),
            onPrimaryContainer = Color(0xFFD2ECFF),
            secondary = Color(0xFF98C379),
            secondaryContainer = Color(0xFF243B19),
            onSecondaryContainer = Color(0xFFDFFFD0),
            tertiary = Color(0xFFC678DD),
            tertiaryContainer = Color(0xFF3C1F46),
            background = Color(0xFF21252B),
            surface = Color(0xFF282C34),
            surfaceVariant = Color(0xFF343A45),
            onSurface = Color(0xFFABB2BF),
            onSurfaceVariant = Color(0xFFC8CCD4)
        )
        "github_dark" -> darkColorScheme(
            primary = Color(0xFF58A6FF),
            onPrimary = Color(0xFF001F3D),
            primaryContainer = Color(0xFF0D419D),
            onPrimaryContainer = Color(0xFFDBEDFF),
            secondary = Color(0xFF3FB950),
            secondaryContainer = Color(0xFF033A16),
            onSecondaryContainer = Color(0xFFD2FFD9),
            tertiary = Color(0xFFFFA657),
            tertiaryContainer = Color(0xFF4A2500),
            background = Color(0xFF0D1117),
            surface = Color(0xFF161B22),
            surfaceVariant = Color(0xFF21262D),
            onSurface = Color(0xFFC9D1D9),
            onSurfaceVariant = Color(0xFF8B949E)
        )
        "github_light" -> lightColorScheme(
            primary = Color(0xFF0969DA),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDDF4FF),
            onPrimaryContainer = Color(0xFF032F62),
            secondary = Color(0xFF1A7F37),
            secondaryContainer = Color(0xFFDAFBE1),
            onSecondaryContainer = Color(0xFF003D16),
            tertiary = Color(0xFF9A6700),
            tertiaryContainer = Color(0xFFFFF8C5),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF6F8FA),
            surfaceVariant = Color(0xFFEAEFF4),
            onSurface = Color(0xFF24292F),
            onSurfaceVariant = Color(0xFF57606A)
        )
        "dracula" -> darkColorScheme(
            primary = Color(0xFFBDA8FF),
            onPrimary = Color(0xFF22163B),
            primaryContainer = Color(0xFF3B2B5C),
            onPrimaryContainer = Color(0xFFF0E8FF),
            secondary = Color(0xFF8BE9B2),
            secondaryContainer = Color(0xFF163A27),
            onSecondaryContainer = Color(0xFFD8FFE6),
            tertiary = Color(0xFFFFB3D5),
            tertiaryContainer = Color(0xFF4B2136),
            background = Color(0xFF252837),
            surface = Color(0xFF2D3142),
            surfaceVariant = Color(0xFF393E52),
            onSurface = Color(0xFFF5F4EE),
            onSurfaceVariant = Color(0xFFD5D6E8)
        )
        "material_you" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                lightColorScheme(
                    primary = Color(0xFF6750A4),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFEADDFF),
                    onPrimaryContainer = Color(0xFF21005D),
                    secondary = Color(0xFF625B71),
                    secondaryContainer = Color(0xFFE8DEF8),
                    onSecondaryContainer = Color(0xFF1D192B),
                    tertiary = Color(0xFF7D5260),
                    tertiaryContainer = Color(0xFFFFD8E4),
                    background = Color(0xFFFFFBFE),
                    surface = Color(0xFFFFFBFE),
                    surfaceVariant = Color(0xFFE7E0EC),
                    onSurface = Color(0xFF1C1B1F),
                    onSurfaceVariant = Color(0xFF49454F)
                )
            }
        }
        "dark" -> darkColorScheme(
            primary = Color(0xFF8EDCE6),
            onPrimary = Color(0xFF08333A),
            primaryContainer = Color(0xFF164B54),
            onPrimaryContainer = Color(0xFFD7F8FC),
            secondary = Color(0xFFC9D6DF),
            secondaryContainer = Color(0xFF29333B),
            onSecondaryContainer = Color(0xFFE6EEF3),
            tertiary = Color(0xFFE4CCFF),
            tertiaryContainer = Color(0xFF42305F),
            background = Color(0xFF111827),
            surface = Color(0xFF171F2B),
            surfaceVariant = Color(0xFF273140),
            onSurface = Color(0xFFE9EEF5),
            onSurfaceVariant = Color(0xFFC4CED9)
        )
        else -> darkColorScheme(
            primary = Color(0xFF00D4E6),
            onPrimary = Color(0xFF002124),
            primaryContainer = Color(0xFF003B42),
            onPrimaryContainer = Color(0xFFB8F4FA),
            secondary = Color(0xFFCAD3D7),
            secondaryContainer = Color(0xFF20272B),
            onSecondaryContainer = Color(0xFFE6EEF2),
            tertiary = Color(0xFFD8B7FF),
            tertiaryContainer = Color(0xFF332245),
            background = Color(0xFF050505),
            surface = Color(0xFF101010),
            surfaceVariant = Color(0xFF1C1C1E),
            onSurface = Color(0xFFEFF7FA),
            onSurfaceVariant = Color(0xFFC2CCD2)
        )
    }
}
