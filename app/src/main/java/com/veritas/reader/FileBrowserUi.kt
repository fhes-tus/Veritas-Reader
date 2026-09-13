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
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.History
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PowerSettingsNew
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
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import com.veritas.reader.ui.VeritasSwitch
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
import com.veritas.reader.ui.rememberVeritasHaptics
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


@Composable
internal fun FileBrowserDialog(
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
    onImportFile: (VeritasBrowserFile) -> Unit,
    onImportMultipleFiles: (List<VeritasBrowserFile>, Boolean) -> Unit,
    onDeleteFiles: (List<VeritasBrowserFile>) -> Unit = {}
) {
    val selectedFiles = remember { mutableStateListOf<VeritasBrowserFile>() }
    // Deleting reaches the user's own storage and cannot be undone, so nothing is removed
    // until this is confirmed with the files named.
    var pendingDelete by remember { mutableStateOf<List<VeritasBrowserFile>>(emptyList()) }
    
    FileBrowserDeleteConfirmationDialog(
        doomedFiles = pendingDelete,
        onConfirmDelete = {
            onDeleteFiles(pendingDelete)
            selectedFiles.clear()
            pendingDelete = emptyList()
        },
        onDismiss = { pendingDelete = emptyList() }
    )

    LaunchedEffect(location) {
        selectedFiles.clear()
    }

    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(VeritasBrowserTab.ALL) }
    var sortMode by remember { mutableStateOf(VeritasBrowserSort.NAME) }
    var sortAscending by remember { mutableStateOf(true) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val browserPrefs = remember { context.getSharedPreferences("veritas_library_settings", Context.MODE_PRIVATE) }
    var viewMode by remember {
        mutableStateOf(
            runCatching {
                LibraryViewMode.valueOf(
                    browserPrefs.getString("file_view_mode", LibraryViewMode.TILES.name) ?: LibraryViewMode.TILES.name
                )
            }.getOrDefault(LibraryViewMode.TILES)
        )
    }
    var showSortDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showViewMenu by remember { mutableStateOf(false) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val columnCount = remember(configuration) {
        when {
            configuration.screenWidthDp >= 840 -> 4
            configuration.screenWidthDp >= 600 -> 3
            else -> 2
        }
    }
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
        val sorted =
            if (sortAscending) filtered.sortedWith(comparator) else filtered.sortedWith(comparator.reversed())
        sorted.sortedBy { it.isDirectory }
    }

    val folders = remember(visibleEntries, canGoUp, query) {
        val list = visibleEntries.filter { it.isDirectory }
        if (canGoUp && query.isBlank()) {
            listOf(
                VeritasBrowserFile(
                    uri = Uri.parse("veritas://parent_directory"),
                    name = ".. (Go up)",
                    mimeType = "",
                    sizeBytes = 0L,
                    modifiedAt = 0L,
                    rootLabel = "",
                    relativePath = "",
                    isDirectory = true,
                    isSupported = true,
                    targetLocation = VeritasBrowserLocation(rootLabel = "Parent")
                )
            ) + list
        } else {
            list
        }
    }
    val files = remember(visibleEntries) {
        visibleEntries.filter { !it.isDirectory }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme))
        ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    if (selectedFiles.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = { selectedFiles.clear() }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear selection",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${selectedFiles.size} selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { pendingDelete = selectedFiles.toList() }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = if (selectedFiles.size == 1) {
                                        "Delete file"
                                    } else {
                                        "Delete ${selectedFiles.size} files"
                                    },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    onImportMultipleFiles(selectedFiles.toList(), false)
                                    selectedFiles.clear()
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Batch Import", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    onImportMultipleFiles(selectedFiles.toList(), true)
                                    selectedFiles.clear()
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Batch Queue", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (canGoUp) {
                                            onGoUp()
                                        } else {
                                            onDismiss()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        "File browser",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        when {
                                            allFilesAccessGranted -> "All Files access • ${location?.label ?: "Phone storage"}"
                                            roots.isEmpty() -> "No folders approved"
                                            else -> location?.label
                                                ?: "${roots.size} approved folder${if (roots.size == 1) "" else "s"}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Box {
                                IconButton(
                                    onClick = { showMoreMenu = true },
                                    enabled = !importing
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "More options",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }) {
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
                                        feature = browserFeatures.requireResolvedFeature(
                                            VeritasFeatureId.FILE_BROWSER_SORTING
                                        ),
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
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .padding(horizontal = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "Search files...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VeritasBrowserTab.entries.forEach { tab ->
                            val count =
                                if (tab == VeritasBrowserTab.ALL) entries.count { !it.isDirectory } else entries.count { !it.isDirectory && it.type == tab }
                            if (selectedTab == tab) {
                                Button(
                                    onClick = { selectedTab = tab },
                                    shape = VeritasPackStyle.chipShape(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${tab.label} $count")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedTab = tab },
                                    shape = VeritasPackStyle.chipShape(),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${tab.label} $count")
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${visibleEntries.count { !it.isDirectory }} files, ${visibleEntries.count { it.isDirectory }} folders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )

                        // Sort Dropdown Chip
                        Box {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                                    .clickable { showSortMenu = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${sortMode.label} ${if (sortAscending) "▲" else "▼"}",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                VeritasBrowserSort.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text("Sort by ${mode.label}") },
                                        onClick = {
                                            sortMode = mode
                                            showSortMenu = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(if (sortAscending) "Descending order" else "Ascending order") },
                                    onClick = {
                                        sortAscending = !sortAscending
                                        showSortMenu = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // View Menu (View mode / Refresh)
                        Box {
                            IconButton(onClick = { showViewMenu = true }) {
                                Text(
                                    text = viewMode.icon,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(expanded = showViewMenu, onDismissRequest = { showViewMenu = false }) {
                                LibraryViewMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text("${mode.icon} ${mode.label}", color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            viewMode = mode
                                            browserPrefs.edit().putString("file_view_mode", mode.name).apply()
                                            showViewMenu = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Refresh files") },
                                    onClick = {
                                        onRefresh()
                                        showViewMenu = false
                                    }
                                )
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
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

                        scanning -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    "Opening folder.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        visibleEntries.isEmpty() -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No files or folders match this view.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        else -> LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {

                            if (files.isNotEmpty()) {
                                item("files-header") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            "Documents",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "PDF, DOCX, PPTX, TXT, EPUB, HTML",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (viewMode == LibraryViewMode.TILES) {
                                val chunkedFiles = files.chunked(columnCount)
                                items(chunkedFiles.size) { rowIndex ->
                                    val rowFiles = chunkedFiles[rowIndex]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowFiles.forEach { file ->
                                            val isSelected = selectedFiles.any { it.uri == file.uri }
                                            FileBrowserFileTileCard(
                                                file = file,
                                                importing = importing,
                                                onOpenDirectory = { onEnterDirectory(file) },
                                                onImport = { onImportFile(file) },
                                                isSelected = isSelected,
                                                onSelectedChange = { checked ->
                                                    if (checked) {
                                                        if (selectedFiles.none { it.uri == file.uri }) selectedFiles.add(file)
                                                    } else {
                                                        selectedFiles.removeAll { it.uri == file.uri }
                                                    }
                                                },
                                                selectionMode = selectedFiles.isNotEmpty(),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        val remainder = columnCount - rowFiles.size
                                        repeat(remainder) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                items(files, key = { it.uri.toString() }) { file ->
                                    val isSelected = selectedFiles.any { it.uri == file.uri }
                                    FileBrowserFileRow(
                                        file = file,
                                        viewMode = viewMode,
                                        importing = importing,
                                        onOpenDirectory = { onEnterDirectory(file) },
                                        onImport = { onImportFile(file) },
                                        isSelected = isSelected,
                                        onSelectedChange = { checked ->
                                            if (checked) {
                                                if (selectedFiles.none { it.uri == file.uri }) selectedFiles.add(file)
                                            } else {
                                                selectedFiles.removeAll { it.uri == file.uri }
                                            }
                                        },
                                        selectionMode = selectedFiles.isNotEmpty()
                                    )
                                }
                            }
                            if (folders.isNotEmpty()) {
                                item("folders-header") {
                                    Text(
                                        "Folders",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }
                            }
                            if (viewMode == LibraryViewMode.TILES) {
                                val chunkedFolders = folders.chunked(columnCount)
                                items(chunkedFolders.size) { rowIndex ->
                                    val rowFolders = chunkedFolders[rowIndex]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowFolders.forEach { folder ->
                                            FileBrowserFileTileCard(
                                                file = folder,
                                                importing = importing,
                                                onOpenDirectory = {
                                                    if (folder.name == ".. (Go up)") {
                                                        onGoUp()
                                                    } else {
                                                        onEnterDirectory(folder)
                                                    }
                                                },
                                                onImport = { onImportFile(folder) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        val remainder = columnCount - rowFolders.size
                                        repeat(remainder) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                items(folders, key = { it.uri.toString() }) { folder ->
                                    FileBrowserFileRow(
                                        file = folder,
                                        viewMode = viewMode,
                                        importing = importing,
                                        onOpenDirectory = {
                                            if (folder.name == ".. (Go up)") {
                                                onGoUp()
                                            } else {
                                                onEnterDirectory(folder)
                                            }
                                        },
                                        onImport = { onImportFile(folder) }
                                    )
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = onOpenFilePicker,
                    enabled = !importing,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(22.dp),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text("＋ Import")
                }
                if (importing) {
                    ImportProgressOverlay(importingName.ifBlank { "selected file" })
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
        }
    }

