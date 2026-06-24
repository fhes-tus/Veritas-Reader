package com.veritas.reader.ui.screens

import android.content.Context
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.veritas.reader.*
import com.veritas.reader.ui.ReaderUiState
import com.veritas.reader.ui.VeritasPendingImport
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ImportProgressOverlay(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    "Importing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
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

private fun getFileColorAndIcon(file: VeritasBrowserFile): Triple<String, Color, Color> {
    if (file.isDirectory) {
        return Triple("📁", Color(0xFFF2994A), Color(0xFFFFF7F0))
    }
    return when (file.type) {
        VeritasBrowserTab.PDF -> Triple("📄", Color(0xFFE24B4A), Color(0xFFFFF0F0))
        VeritasBrowserTab.DOC -> Triple("📘", Color(0xFF7C6FFF), Color(0xFFF0F3FF))
        VeritasBrowserTab.BOOKS -> Triple("📕", Color(0xFF1D9E75), Color(0xFFF0FAF5))
        VeritasBrowserTab.HTML -> Triple("🌐", Color(0xFF2F80ED), Color(0xFFEBF3FF))
        VeritasBrowserTab.TXT -> Triple("📝", Color(0xFF888888), Color(0xFFF5F5F5))
        else -> Triple("📄", Color(0xFF888888), Color(0xFFF5F5F5))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileBrowserFileRow(
    file: VeritasBrowserFile,
    viewMode: LibraryViewMode,
    importing: Boolean,
    onOpenDirectory: () -> Unit,
    onImport: () -> Unit,
    isSelected: Boolean? = null,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    selectionMode: Boolean = false
) {
    val enabled = if (file.isDirectory) file.targetLocation != null else file.isSupported && !importing
    val action = if (file.isDirectory) onOpenDirectory else onImport

    val padding = when (viewMode) {
        LibraryViewMode.SMALL -> 6.dp
        LibraryViewMode.LIST -> 8.dp
        LibraryViewMode.MEDIUM -> 12.dp
        LibraryViewMode.DETAILS -> 14.dp
        else -> 10.dp
    }
    val coverSize = when (viewMode) {
        LibraryViewMode.SMALL -> 36.dp
        LibraryViewMode.LIST -> 46.dp
        LibraryViewMode.MEDIUM -> 58.dp
        LibraryViewMode.DETAILS -> 72.dp
        else -> 54.dp
    }
    val titleStyle = when (viewMode) {
        LibraryViewMode.SMALL -> MaterialTheme.typography.bodyMedium
        LibraryViewMode.LIST -> MaterialTheme.typography.bodyLarge
        LibraryViewMode.MEDIUM -> MaterialTheme.typography.titleSmall
        LibraryViewMode.DETAILS -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.bodyLarge
    }
    val showDetails = viewMode == LibraryViewMode.DETAILS || viewMode == LibraryViewMode.LIST
    val shape = if (viewMode == LibraryViewMode.SMALL || viewMode == LibraryViewMode.LIST) MaterialTheme.shapes.medium else MaterialTheme.shapes.large

    val (emoji, tint, bg) = getFileColorAndIcon(file)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    if (selectionMode && !file.isDirectory) {
                        if (isSelected == true) onSelectedChange?.invoke(false)
                        else onSelectedChange?.invoke(true)
                    } else {
                        action()
                    }
                },
                onLongClick = {
                    if (!file.isDirectory && onSelectedChange != null) {
                        onSelectedChange(true)
                    }
                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectionMode && isSelected != null && onSelectedChange != null && !file.isDirectory) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        onSelectedChange(checked)
                    },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(coverSize)
                    .background(bg, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = if (viewMode == LibraryViewMode.SMALL) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleLarge,
                    color = tint
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = file.name,
                    maxLines = if (showDetails) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    style = titleStyle
                )
                if (showDetails) {
                    Text(
                        text = fileBrowserFolderLine(file),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = when {
                        file.isDirectory && file.targetLocation != null -> "Folder • ${formatBrowserModified(file.modifiedAt)}"
                        file.isDirectory -> "Protected folder"
                        file.isSupported -> "${formatBrowserFileSize(file.sizeBytes)} • ${formatBrowserModified(file.modifiedAt)}"
                        else -> "Unsupported file • ${formatBrowserFileSize(file.sizeBytes)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val buttonEnabled = enabled && (!selectionMode || file.isDirectory)
            Box(
                modifier = Modifier
                    .background(
                        if (buttonEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(50)
                    )
                    .clickable(enabled = buttonEnabled) {
                        if (file.isDirectory) onOpenDirectory() else onImport()
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (file.isDirectory) "Open" else if (file.isSupported) "Import" else "Unsupported",
                    color = if (buttonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileBrowserFileTileCard(
    file: VeritasBrowserFile,
    importing: Boolean,
    onOpenDirectory: () -> Unit,
    onImport: () -> Unit,
    isSelected: Boolean? = null,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    selectionMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val enabled = if (file.isDirectory) file.targetLocation != null else file.isSupported && !importing
    val action = if (file.isDirectory) onOpenDirectory else onImport

    val (emoji, tint, bg) = getFileColorAndIcon(file)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    if (selectionMode && !file.isDirectory) {
                        if (isSelected == true) onSelectedChange?.invoke(false)
                        else onSelectedChange?.invoke(true)
                    } else {
                        action()
                    }
                },
                onLongClick = {
                    if (!file.isDirectory && onSelectedChange != null) {
                        onSelectedChange(true)
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .background(bg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium,
                        color = tint
                    )
                }
                if (selectionMode && isSelected != null && onSelectedChange != null && !file.isDirectory) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            onSelectedChange(checked)
                        },
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    )
                }
            }
            Text(
                text = file.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    file.isDirectory && file.targetLocation != null -> "Folder • ${formatBrowserModified(file.modifiedAt)}"
                    file.isDirectory -> "Protected folder"
                    file.isSupported -> "${formatBrowserFileSize(file.sizeBytes)} • ${formatBrowserModified(file.modifiedAt)}"
                    else -> "Unsupported file"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val buttonEnabled = enabled && (!selectionMode || file.isDirectory)
                Box(
                    modifier = Modifier
                        .background(
                            if (buttonEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(50)
                        )
                        .clickable(enabled = buttonEnabled) {
                            if (file.isDirectory) onOpenDirectory() else onImport()
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (file.isDirectory) "Open" else if (file.isSupported) "Import" else "Unsupported",
                        color = if (buttonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FileBrowserSortDialog(
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortModeChange(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortMode == option,
                            onClick = { onSortModeChange(option) })
                        Text(option.label, modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Order:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortAscendingChange(true) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = sortAscending, onClick = { onSortAscendingChange(true) })
                    Text("Ascending", modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortAscendingChange(false) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !sortAscending,
                        onClick = { onSortAscendingChange(false) })
                    Text("Descending", modifier = Modifier.weight(1f))
                }
            }
        }
    )
}

fun fileBrowserFolderLine(file: VeritasBrowserFile): String {
    val folderPath = file.relativePath.substringBeforeLast('/', missingDelimiterValue = "")
    return if (folderPath.isBlank()) file.rootLabel else "${file.rootLabel}/$folderPath"
}

fun formatBrowserFileSize(bytes: Long): String {
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

fun formatBrowserModified(timestamp: Long): String =
    if (timestamp > 0L) formatUpdated(timestamp) else "Unknown date"

@Composable
fun ReadingHistoryDialog(
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
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Close") } },
        dismissButton = {
            OutlinedButton(
                onClick = onClearHistory,
                enabled = visibleHistory.isNotEmpty(),
                shape = RoundedCornerShape(50)
            ) { Text("Clear") }
        },
        title = { Text("Reading history") },
        text = {
            if (visibleHistory.isEmpty()) {
                Text(
                    "Open a reading and it will appear here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        visibleHistory,
                        key = { (entry, _) -> entry.documentId }) { (entry, document) ->
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    document.sourceLabel.take(3).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    document.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${document.sourceLabel} • sentence ${safeIndex + 1}/$safeChunkCount • ${
                        progressPercent(
                            document
                        )
                    }%",
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

@Composable
fun PdfImportOptionsDialog(
    options: PdfImportOptions,
    textOptions: TextImportOptions,
    onOptionsChange: (PdfImportOptions) -> Unit,
    onTextOptionsChange: (TextImportOptions) -> Unit,
    onPickPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    var startPageDraft by remember(options.startPage) {
        mutableStateOf(
            options.startPage?.toString().orEmpty()
        )
    }
    var endPageDraft by remember(options.endPage) {
        mutableStateOf(
            options.endPage?.toString().orEmpty()
        )
    }
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
                                onOptionsChange(
                                    options.copy(
                                        startPage = startPageDraft.toIntOrNull()?.takeIf { it > 0 })
                                )
                            },
                            label = { Text("Start page") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endPageDraft,
                            onValueChange = { value ->
                                endPageDraft = value.filter { it.isDigit() }.take(5)
                                onOptionsChange(
                                    options.copy(
                                        endPage = endPageDraft.toIntOrNull()?.takeIf { it > 0 })
                                )
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
                            OutlinedButton(
                                onClick = { modeExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    options.extractionMode.ifBlank { "HTML with images" },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = modeExpanded,
                                onDismissRequest = { modeExpanded = false }) {
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
                            OutlinedButton(
                                onClick = { encodingExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    selectedEncoding.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = encodingExpanded,
                                onDismissRequest = { encodingExpanded = false }) {
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
                            onOptionsChange(options.copy(minWordGap = value.filter { it.isDigit() || it == '.' }
                                .take(6).ifBlank { "0.1" }))
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

@Composable
fun SyncCenterDialog(
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
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Close") } },
        title = { Text("Sync") },
        text = {
            Column(
                modifier = Modifier
                    .height(560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.shapes.medium
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "⇅",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Manual sync pack",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "Move your library between devices now.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            "Export a sync file, copy or move it to another device, then import it. Import merges safely and does not delete local readings.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "What the sync file includes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black
                        )
                        SyncInfoRow("Saved readings", documentCount.toString())
                        SyncInfoRow("Bookmarks and notes", annotationCount.toString())
                        SyncInfoRow("Queue", queueCount.toString())
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Manual sync flow",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black
                        )
                        Button(
                            onClick = onExportSyncPack,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = documentCount > 0,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Export sync file")
                        }
                        OutlinedButton(
                            onClick = onImportSyncPack,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Import sync file from another device")
                        }
                        BackupStatusBlock(inProgress = inProgress, message = message)
                        Text(
                            "Safe merge is always used. Existing local readings are kept unless you delete them manually.",
                            style = MaterialTheme.typography.bodySmall,
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
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SoftChip(value)
    }
}

@Composable
private fun BackupStatusBlock(
    inProgress: Boolean,
    message: String?
) {
    if (!inProgress && message.isNullOrBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
fun BackupRestoreDialog(
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
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Close") } },
        title = { Text("Backup & restore") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Export a portable Veritas backup, or import a backup into this library. Import adds restored readings without deleting the current library.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Current library", fontWeight = FontWeight.Black)
                        Text(
                            "$documentCount readings • $annotationCount bookmarks/notes • $queueCount queued",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = documentCount > 0,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Export library backup")
                }
                OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)) {
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
fun TranslationToolsDialog(
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
        title = { Text("Translation tools") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Veritas prepares the prompt and copies it to the clipboard. You can paste it into your translator app.",
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
                        "Current sentence: $currentPreview${
                            if (document.chunks.getOrNull(
                                    currentIndex
                                ).orEmpty().length > 180
                            ) "…" else ""
                        }",
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
                    onClick = {
                        onSend(
                            targetLanguage,
                            TranslationLauncher.Mode.BILINGUAL_SECTION
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Bilingual current sentence") }
                OutlinedButton(
                    onClick = {
                        onSend(
                            targetLanguage,
                            TranslationLauncher.Mode.BILINGUAL_DOCUMENT
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Bilingual full document") }
            }
        }
    )
}

@Composable
fun AiFreeModeDialog(
    documentCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("AI & study mode") },
        text = {
            Column(
                modifier = Modifier
                    .height(520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Free AI approach",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Veritas copies prompts to your clipboard for easy pasting into your web AI of choice.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StudyCard(title = "1. AI app handoff") {
                    Text("Open a document, tap Reader tools → AI, choose a task, then send the prepared prompt to ChatGPT, Gemini, Claude, Copilot, Perplexity, or another app.")
                }
                StudyCard(title = "2. Offline study tools") {
                    Text("For no-internet revision, Veritas can create simple summaries, key points, terms, flashcards, and quizzes using local document logic. This is smaller but less powerful than cloud AI.")
                }
                Text(
                    "Current library: $documentCount saved reading${if (documentCount == 1) "" else "s"}.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun AiCenterDialog(
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
                    "Use local study tools or prepared clipboard handoff prompts without adding paid APIs or account-gated services inside Veritas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
fun ExportAudioStatusDialog(
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Creating audio from this reading...")
                    }
                }
                Text(
                    message ?: "Preparing export...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (file != null) {
                    Text(file.name, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            if (file != null) {
                Button(onClick = { onShare(file) }) { Text("Share / Open") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !inProgress) { Text("Close") }
        }
    )
}

@Composable
fun AiAppStudyDialog(
    document: ReaderDocument,
    currentIndex: Int,
    templates: List<AiPromptTemplate>,
    history: List<AiPromptHistoryEntry>,
    onDismiss: () -> Unit,
    onSendToAiApp: (AiPromptType, String, AiPromptScope, IntRange?) -> Unit,
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
    var showLongDocPageRange by remember { mutableStateOf(false) }
    var longDocStartPage by remember { mutableStateOf("") }
    var longDocEndPage by remember { mutableStateOf("") }
    var showQuizPageRange by remember { mutableStateOf(false) }
    var quizStartPage by remember { mutableStateOf("") }
    var quizEndPage by remember { mutableStateOf("") }
    val safeIndex =
        if (document.chunks.isEmpty()) 0 else currentIndex.coerceIn(0, document.chunks.lastIndex)
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
                    "Veritas prepares the prompt and copies it to your clipboard. You can paste it into your AI assistant.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            document.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
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
                            onClick = {
                                onSendToAiApp(
                                    AiPromptType.SUMMARY,
                                    "",
                                    AiPromptScope.CURRENT_SECTION,
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Summarize current sentence") }

                        OutlinedButton(
                            onClick = {
                                onSendToAiApp(
                                    AiPromptType.SUMMARY,
                                    "",
                                    AiPromptScope.WHOLE_DOCUMENT,
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Summarize whole document") }

                        OutlinedButton(
                            onClick = {
                                showLongDocPageRange = !showLongDocPageRange
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Long document: page-to-page summary") }

                        if (showLongDocPageRange) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = longDocStartPage,
                                        onValueChange = { longDocStartPage = it.filter { char -> char.isDigit() } },
                                        label = { Text("From") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = longDocEndPage,
                                        onValueChange = { longDocEndPage = it.filter { char -> char.isDigit() } },
                                        label = { Text("To") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Button(
                                    onClick = {
                                        val start = longDocStartPage.toIntOrNull() ?: 1
                                        val end = longDocEndPage.toIntOrNull() ?: 1
                                        val min = minOf(start, end).coerceIn(1, document.pageCount)
                                        val max = maxOf(start, end).coerceIn(1, document.pageCount)
                                        onSendToAiApp(
                                            AiPromptType.SECTION_BY_SECTION,
                                            "",
                                            AiPromptScope.CUSTOM_PAGE_RANGE,
                                            min..max
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Send Pages ${longDocStartPage.ifBlank { "1" }}-${longDocEndPage.ifBlank { document.pageCount.toString() }} to AI")
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onSendToAiApp(
                                    AiPromptType.KEY_POINTS,
                                    "",
                                    AiPromptScope.WHOLE_DOCUMENT,
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Extract key points") }

                        OutlinedButton(
                            onClick = {
                                onSendToAiApp(
                                    AiPromptType.EXPLAIN_SECTION,
                                    "",
                                    AiPromptScope.CURRENT_SECTION,
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Explain current sentence") }

                        OutlinedButton(
                            onClick = {
                                onSendToAiApp(
                                    AiPromptType.STUDY_NOTES,
                                    "",
                                    AiPromptScope.WHOLE_DOCUMENT,
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Create study notes") }

                        OutlinedButton(
                            onClick = {
                                onSendToAiApp(
                                    AiPromptType.SIMPLIFY,
                                    "",
                                    AiPromptScope.WHOLE_DOCUMENT,
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Simplify difficult text") }

                        OutlinedButton(
                            onClick = {
                                showQuizPageRange = !showQuizPageRange
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Create page-to-page quiz") }

                        if (showQuizPageRange) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = quizStartPage,
                                        onValueChange = { quizStartPage = it.filter { char -> char.isDigit() } },
                                        label = { Text("From") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = quizEndPage,
                                        onValueChange = { quizEndPage = it.filter { char -> char.isDigit() } },
                                        label = { Text("To") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Button(
                                    onClick = {
                                        val start = quizStartPage.toIntOrNull() ?: 1
                                        val end = quizEndPage.toIntOrNull() ?: 1
                                        val min = minOf(start, end).coerceIn(1, document.pageCount)
                                        val max = maxOf(start, end).coerceIn(1, document.pageCount)
                                        onSendToAiApp(
                                            AiPromptType.QUIZ,
                                            "",
                                            AiPromptScope.CUSTOM_PAGE_RANGE,
                                            min..max
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Generate Quiz for Pages ${quizStartPage.ifBlank { "1" }}-${quizEndPage.ifBlank { document.pageCount.toString() }}")
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onSendToAiApp(
                                    AiPromptType.FLASHCARDS,
                                    "",
                                    AiPromptScope.WHOLE_DOCUMENT,
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Create flashcards") }

                        TextButton(
                            onClick = onOpenOfflineStudyTools,
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            label = { Text("Custom instruction") },
                            placeholder = { Text("Example: Explain this like I am preparing for an exam.") }
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    onSendToAiApp(
                                        AiPromptType.CUSTOM,
                                        customPrompt,
                                        AiPromptScope.WHOLE_DOCUMENT,
                                        null
                                    )
                                },
                                enabled = customPrompt.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Copy Prompt") }
                            OutlinedButton(
                                onClick = { onSaveTemplate(templateTitle, customPrompt) },
                                enabled = customPrompt.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Save Template") }
                        }
                        HorizontalDivider()
                        if (templates.isEmpty()) {
                            Text(
                                "Saved custom prompts will appear here.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            templates.forEach { template ->
                                Card(
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(template.title, fontWeight = FontWeight.Black)
                                        Text(
                                            template.instruction,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            TextButton(onClick = {
                                                onSendToAiApp(
                                                    AiPromptType.CUSTOM,
                                                    template.instruction,
                                                    AiPromptScope.WHOLE_DOCUMENT,
                                                    null
                                                )
                                            }) { Text("Use") }
                                            TextButton(onClick = {
                                                onCopyText(
                                                    "Veritas AI template",
                                                    template.instruction
                                                )
                                            }) { Text("Copy") }
                                            TextButton(onClick = { onDeleteTemplate(template.id) }) {
                                                Text(
                                                    "Delete"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "History" -> {
                        if (history.isEmpty()) {
                            Text(
                                "Prompts you send to AI apps will appear here.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = onClearHistory) { Text("Clear history") }
                            }
                            history.take(12).forEach { item ->
                                Card(
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "${item.promptType} • ${item.scope}",
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            item.documentTitle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            item.promptPreview,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            formatUpdated(item.createdAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        TextButton(onClick = {
                                            onCopyText(
                                                "Veritas AI prompt preview",
                                                item.promptPreview
                                              )
                                        }) { Text("Copy preview") }
                                    }
                                }
                            }
                        }
                    }

                    "Result → note" -> {
                        Text(
                            "After the AI replies, copy its answer, return here, paste it below, and save it as a note on the current sentence.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = aiResultDraft,
                            onValueChange = { aiResultDraft = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            label = { Text("Paste AI result") },
                            placeholder = { Text("Paste summary or study notes here…") }
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
            }
        }
    )
}

@Composable
fun StudyToolsDialog(
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
                    "Offline study help generated from this document. It is meant for revision.",
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
                        Text(
                            "Flashcards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (studyPack.flashcards.isEmpty()) {
                            Text(
                                "No flashcards could be generated.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            studyPack.flashcards.forEachIndexed { index, card ->
                                StudyCard(title = "Card ${index + 1}") {
                                    Text(card.front, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        card.back,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    "Quiz" -> {
                        Text(
                            "Quick quiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (studyPack.quiz.isEmpty()) {
                            Text(
                                "No quiz could be generated.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                                    Text(
                                        question.explanation,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
            content()
        }
    }
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

@Composable
fun TextEditorDialog(
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
    var editorValue by remember(target) {
        mutableStateOf(
            TextFieldValue(
                text,
                selection = TextRange(text.length)
            )
        )
    }
    var undoStack by remember(target) { mutableStateOf<List<TextFieldValue>>(emptyList()) }
    var redoStack by remember(target) { mutableStateOf<List<TextFieldValue>>(emptyList()) }
    val scopeLabel = when (target) {
        is VeritasTextEditTarget.SentenceRange -> target.label
        is VeritasTextEditTarget.Part -> target.label
    }
    val searchMatches = remember(editorValue.text, searchQuery) {
        countSearchOccurrences(
            editorValue.text,
            searchQuery
        )
    }

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
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "←",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Edit text",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            document.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showSearch = !showSearch }) { Text("⌕") }
                    TextButton(
                        onClick = onDownloadToPhone,
                        enabled = editorValue.text.isNotBlank()
                    ) { Text("⇩") }
                    Button(
                        onClick = onSave,
                        enabled = editorValue.text.isNotBlank()
                    ) { Text("Save") }
                }
                if (showSearch) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        TextButton(
                            onClick = ::findNextSearchMatch,
                            enabled = searchMatches > 0
                        ) { Text("Next") }
                    }
                }
                Text(
                    "Editing $scopeLabel • current sentence ${currentIndex + 1}/${
                        document.chunks.size.coerceAtLeast(
                            1
                        )
                    }",
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
                        .background(MaterialTheme.colorScheme.inverseSurface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = { showSearch = !showSearch }) {
                        Text(
                            "⌕",
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                    TextButton(onClick = {
                        replaceSelection(
                            "<p>\n",
                            "\n</p>",
                            "Paragraph"
                        )
                    }) { Text("<p>", color = MaterialTheme.colorScheme.inverseOnSurface) }
                    TextButton(
                        onClick = { replaceSelection("_") },
                        enabled = editorValue.text.isNotBlank()
                    ) {
                        Text(
                            "I",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { replaceSelection("**") },
                        enabled = editorValue.text.isNotBlank()
                    ) {
                        Text(
                            "B",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Black
                        )
                    }
                    TextButton(
                        onClick = { replaceSelection("<u>", "</u>") },
                        enabled = editorValue.text.isNotBlank()
                    ) {
                        Text(
                            "U",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                }
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
private fun TutorialStage(frame: TutorialFrame, progress: Float, pulse: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .graphicsLayer(scaleX = pulse, scaleY = pulse)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        frame.icon,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                        .fillMaxWidth(0.62f)
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
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
            Text(
                frame.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
            Text(frame.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (frame.action != null) {
                Button(onClick = frame.action, shape = RoundedCornerShape(50)) { Text("Open") }
            }
        }
    }
}

@Composable
private fun TutorialStep(
    number: String,
    title: String,
    body: String,
    action: (() -> Unit)?,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tutorialStepBounce"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(number, fontWeight = FontWeight.Black) }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, fontWeight = FontWeight.Black)
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (action != null) {
                Button(onClick = action, shape = RoundedCornerShape(50)) { Text("Open") }
            }
        }
    }
}

@Composable
fun TutorialDialog(
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
            TutorialFrame(
                "1",
                "Welcome to Veritas Reader",
                "Transform your research, documents, and reading materials into focused audio experiences.",
                "📖",
                null
            ),
            TutorialFrame(
                "2",
                "What should we call you?",
                "Your name personalizes the Home tab and reading experience.",
                "👤",
                null
            ),
            TutorialFrame(
                "3",
                "Add a reading",
                "Import PDFs, EPUBs, Word documents, text files, images, or paste texts and links.",
                "➕",
                { onImport(nameDraft) }),
            TutorialFrame(
                "4",
                "Read your way",
                "Switch between Extracted Text, Listen Mode, and the Original PDF/Image layouts.",
                "📄",
                null
            ),
            TutorialFrame(
                "5",
                "Listen & synthesis",
                "Pick voices, adjust speed/pitch, and control playback.",
                "🎧",
                { onVoice(nameDraft) }),
            TutorialFrame(
                "6",
                "Mark & remember",
                "Bookmark sentences to highlight them, add study notes, translate text, search, and fix pronunciation.",
                "🔖",
                null
            ),
            TutorialFrame(
                "7",
                "Make it yours",
                "Choose from 10+ premium color themes, set up widget shortcuts, and export standard WAV audio files.",
                "🎨",
                { onThemes(nameDraft) }),
            TutorialFrame(
                "8",
                "Ready to read?",
                "Your calm reading environment is configured. Open the library and import your first document.",
                "🚀",
                null
            )
        )
    }
    var stepIndex by remember { mutableStateOf(0) }
    val pulse by animateFloatAsState(
        targetValue = if (stepIndex % 2 == 0) 1.08f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Veritas setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onDismiss(nameDraft) }) { Text("Skip") }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
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
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.shapes.large
                                    ),
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
                        Text(
                            "What should we call you?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        OutlinedTextField(
                            value = nameDraft,
                            onValueChange = { nameDraft = it.take(48) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your preferred name") },
                            placeholder = { Text("Name for the Home tab welcome") },
                            singleLine = true,
                            shape = RoundedCornerShape(50)
                        )
                        Text(
                            "This is used to personalize your Home tab and reading experience.",
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
                        Text(if (stepIndex >= steps.lastIndex) "Go to Home" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
fun VeritasImportPreviewDialog(
    pendingImport: VeritasPendingImport,
    onConfirm: (String, PdfImportOptions, TextImportOptions) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(pendingImport.name.substringBeforeLast(".")) }
    var pdfOptions by remember { mutableStateOf(pendingImport.pdfOptions) }
    var textOptions by remember { mutableStateOf(pendingImport.textOptions) }

    var startPageDraft by remember { mutableStateOf(pdfOptions.startPage?.toString().orEmpty()) }
    var endPageDraft by remember { mutableStateOf(pdfOptions.endPage?.toString().orEmpty()) }

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

    val sizeKB = pendingImport.sizeBytes / 1024.0
    val sizeMB = sizeKB / 1024.0
    val sizeText = if (sizeMB >= 1.0) {
        String.format(Locale.US, "%.2f MB", sizeMB)
    } else {
        String.format(Locale.US, "%.1f KB", sizeKB)
    }

    val pageCount = pendingImport.pageCount
    val isPageRangeInvalid = pendingImport.isPdf && pageCount > 0 && run {
        val start = startPageDraft.toIntOrNull()
        val end = endPageDraft.toIntOrNull()
        start == null || end == null || start < 1 || end > pageCount || start > end
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Import settings") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "File size: $sizeText" + if (pendingImport.isPdf && pageCount > 0) " • Total pages: $pageCount" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Document title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (pendingImport.isPdf) {
                    item {
                        Text("PDF options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = startPageDraft,
                                onValueChange = { value ->
                                    startPageDraft = value.filter { it.isDigit() }.take(5)
                                    val startVal = startPageDraft.toIntOrNull()
                                    pdfOptions = pdfOptions.copy(startPage = startVal)
                                },
                                label = { Text("Start page") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endPageDraft,
                                onValueChange = { value ->
                                    endPageDraft = value.filter { it.isDigit() }.take(5)
                                    val endVal = endPageDraft.toIntOrNull()
                                    pdfOptions = pdfOptions.copy(endPage = endVal)
                                },
                                label = { Text("End page") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (isPageRangeInvalid && pageCount > 0) {
                        item {
                            Text(
                                text = "Warning: Page range must be between 1 and $pageCount.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    item {
                        Column {
                            Text("Extraction mode", fontWeight = FontWeight.Bold)
                            Box {
                                OutlinedButton(
                                    onClick = { modeExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        pdfOptions.extractionMode.ifBlank { "HTML with images" },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                DropdownMenu(
                                    expanded = modeExpanded,
                                    onDismissRequest = { modeExpanded = false }
                                ) {
                                    extractionModes.forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode) },
                                            onClick = {
                                                modeExpanded = false
                                                pdfOptions = pdfOptions.copy(
                                                    extractionMode = mode,
                                                    forceOcr = mode == "Force OCR",
                                                    preferOcrWhenLowText = mode != "Plain text"
                                                )
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
                            checked = pdfOptions.cleanupRepeatedLines,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(cleanupRepeatedLines = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Remove page numbers",
                            checked = pdfOptions.removePageNumbers,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(removePageNumbers = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Repair hyphenated line breaks",
                            checked = pdfOptions.repairHyphenation,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(repairHyphenation = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Include page markers",
                            checked = pdfOptions.includePageMarkers,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(includePageMarkers = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Remove top page noise",
                            checked = pdfOptions.removeTopPageNoise,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(removeTopPageNoise = it) }
                        )
                    }

                    item {
                        PdfImportToggleRow(
                            title = "Remove bottom page noise",
                            checked = pdfOptions.removeBottomPageNoise,
                            onCheckedChange = { pdfOptions = pdfOptions.copy(removeBottomPageNoise = it) }
                        )
                    }
                } else if (pendingImport.mimeType.contains("text") || pendingImport.mimeType.contains("html") || pendingImport.name.endsWith(".txt") || pendingImport.name.endsWith(".html") || pendingImport.name.endsWith(".htm")) {
                    item {
                        Text("Text Options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }

                    item {
                        Column {
                            Text("Text encoding", fontWeight = FontWeight.Bold)
                            Box {
                                OutlinedButton(
                                    onClick = { encodingExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        selectedEncoding.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                DropdownMenu(
                                    expanded = encodingExpanded,
                                    onDismissRequest = { encodingExpanded = false }
                                ) {
                                    TextImportEncodingCatalog.options.forEach { encoding ->
                                        DropdownMenuItem(
                                            text = { Text(encoding.label) },
                                            onClick = {
                                                encodingExpanded = false
                                                textOptions = textOptions.copy(encodingId = encoding.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(title.ifBlank { pendingImport.name }, pdfOptions, textOptions)
                },
                enabled = !isPageRangeInvalid
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FloatingRecordOverlay(
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
            .padding(22.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                },
            shape = if (awaitingDecision) MaterialTheme.shapes.large else MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
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
            .background(
                if (inProgress) Color(0xFFE53935) else MaterialTheme.colorScheme.primary,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (inProgress) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(Color.White, CircleShape)
            )
        } else {
            Text(
                "✓",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall
            )
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
fun FileBrowserDialog(
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
    onImportMultipleFiles: (List<VeritasBrowserFile>, Boolean) -> Unit
) {
    val selectedFiles = remember { mutableStateListOf<VeritasBrowserFile>() }

    LaunchedEffect(location) {
        selectedFiles.clear()
    }

    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(VeritasBrowserTab.ALL) }
    var sortMode by remember { mutableStateOf(VeritasBrowserSort.NAME) }
    var sortAscending by remember { mutableStateOf(true) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var viewMode by remember { mutableStateOf(LibraryViewMode.TILES) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showViewMenu by remember { mutableStateOf(false) }

    val columnCount = 3

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
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
                                        location?.label ?: "Browse files",
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
                                        enabled = !scanning,
                                        onClick = {
                                            showMoreMenu = false
                                            onRefresh()
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
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text("${tab.emoji} ${tab.label} $count")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedTab = tab },
                                    shape = VeritasPackStyle.chipShape(),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text("${tab.emoji} ${tab.label} $count")
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

                        Box {
                            IconButton(onClick = { showViewMenu = true }) {
                                Text(viewMode.icon, style = MaterialTheme.typography.titleMedium)
                            }
                            DropdownMenu(expanded = showViewMenu, onDismissRequest = { showViewMenu = false }) {
                                LibraryViewMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text("${mode.icon} ${mode.label}") },
                                        onClick = {
                                            viewMode = mode
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
                        scanning -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            val folders = visibleEntries.filter { it.isDirectory }
                            val files = visibleEntries.filter { !it.isDirectory }
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
                                            "PDF, DOCX, TXT, EPUB, HTML",
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
                                                onOpenDirectory = { onEnterDirectory(folder) },
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
                                        onOpenDirectory = { onEnterDirectory(folder) },
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
                        .padding(22.dp),
                    shape = MaterialTheme.shapes.medium,
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
}

@Composable
fun SentenceNoteDialog(
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
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    document.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    label = { Text("Sentence note") },
                    placeholder = { Text("Write the note to attach to this sentence") },
                    minLines = 6,
                    maxLines = 10,
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    "$wordCount / 300 words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                indexes.take(5).forEach { index ->
                    val excerpt =
                        document.chunks.getOrNull(index).orEmpty().replace(Regex("\\s+"), " ")
                            .trim()
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
                    Text(
                        "+ ${indexes.size - 5} more selected sentences",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = noteDraft.trim().isNotBlank(), shape = RoundedCornerShape(50)) { Text("Save") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDelete, shape = RoundedCornerShape(50)) { Text("Delete") }
                TextButton(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Cancel") }
            }
        }
    )
}

