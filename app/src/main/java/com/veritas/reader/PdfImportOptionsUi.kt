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
internal fun PdfImportOptionsDialog(
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

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            // imePadding: this page has page-number inputs; with edge-to-edge the soft
            // keyboard must not cover them.
            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("PDF & import tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Done") }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onPickPdf, shape = RoundedCornerShape(50)) { Text("Open file browser") }
                }
            }
        }
    }
}

@Composable
internal fun PdfImportToggleRow(
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
        VeritasSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


