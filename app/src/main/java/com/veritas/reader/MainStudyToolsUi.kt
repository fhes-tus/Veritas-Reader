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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.FilterChip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.BasicTextField
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

@Composable
internal fun BackupRestoreDialog(
    documentCount: Int,
    annotationCount: Int,
    queueCount: Int,
    inProgress: Boolean,
    message: String?,
    fullBackupEstimateBytes: Long = 0L,
    autoBackupEnabled: Boolean = true,
    onToggleAutoBackup: () -> Unit = {},
    onExport: () -> Unit,
    onExportFull: () -> Unit = {},
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    SyncAndBackupCenterDialog(
        documentCount = documentCount,
        annotationCount = annotationCount,
        queueCount = queueCount,
        pronunciationRuleCount = 0,
        inProgress = inProgress,
        message = message,
        onExportSyncPack = onExport,
        onShareSyncPack = onExport,
        onImportSyncPack = onImport,
        onDismiss = onDismiss,
        fullBackupEstimateBytes = fullBackupEstimateBytes,
        autoBackupEnabled = autoBackupEnabled,
        onToggleAutoBackup = onToggleAutoBackup,
        onExportFull = onExportFull
    )
}

@Composable
internal fun BackupStatusBlock(
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
internal fun TranslationToolsDialog(
    document: ReaderDocument,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSend: (String, TranslationLauncher.Mode) -> Unit
) {
    val context = LocalContext.current
    var targetLanguage by remember { mutableStateOf("English") }
    var selectedMode by remember { mutableStateOf(TranslationLauncher.Mode.CURRENT_SECTION) }
    var translatedTextInput by remember { mutableStateOf("") }
    val currentSentence = remember(document, currentIndex) {
        document.chunks.getOrNull(currentIndex).orEmpty().trim()
    }
    val popularLanguages = listOf("English", "Spanish", "French", "German", "Chinese", "Japanese", "Arabic", "Portuguese", "Italian", "Russian")

    val priorityApps = remember {
        listOf(
            Triple("Google", "com.google.android.apps.translate", Icons.Outlined.Translate),
            Triple("ChatGPT", "com.openai.chatgpt", IconChatGPT),
            Triple("Gemini", "com.google.android.apps.bard", IconGemini),
            Triple("Claude", "com.anthropic.claude", IconClaude),
            Triple("Copilot", "com.microsoft.copilot", IconCopilot),
            Triple("Perplexity", "ai.perplexity.app.android", IconPerplexity),
            Triple("Grok", "com.x.android", IconGrok)
        )
    }

    fun executeHandoff(targetPackage: String?) {
        TranslationLauncher.launchTarget(
            context = context,
            targetPackage = targetPackage,
            title = document.title,
            chunks = document.chunks,
            currentIndex = currentIndex,
            targetLanguage = targetLanguage,
            mode = selectedMode
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Done") } },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Translation handoff")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Send structured translation prompts to priority translator & AI apps, or listen to audio read-out of original and translated text.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Priority Apps Quick Actions
                Text(
                    "Priority translation targets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorityApps.forEach { (label, pkg, icon) ->
                        OutlinedButton(
                            onClick = { executeHandoff(pkg) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Button(
                        onClick = { executeHandoff(null) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("All Apps", style = MaterialTheme.typography.labelMedium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Audio Read-Out Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                "Spoken Audio Read-Out",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Play Original Sentence
                        if (currentSentence.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Original Sentence ${currentIndex + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        currentSentence.take(90) + if (currentSentence.length > 90) "…" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        VoiceManager.previewVoice(
                                            context = context,
                                            enginePackage = "",
                                            voiceName = "",
                                            text = currentSentence
                                        )
                                    }
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Read Original", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                        // Play Spoken Translation
                        Text(
                            "Translated Text (type or paste to listen)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = translatedTextInput,
                            onValueChange = { translatedTextInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Paste translated result here...") },
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (translatedTextInput.isNotBlank()) {
                                    VoiceManager.previewVoice(
                                        context = context,
                                        enginePackage = "",
                                        voiceName = "",
                                        text = translatedTextInput
                                    )
                                }
                            },
                            enabled = translatedTextInput.isNotBlank(),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Read Out Translation")
                        }
                    }
                }

                Text(
                    "Target language",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Quick Language Selection Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularLanguages.forEach { lang ->
                        val isSelected = targetLanguage.equals(lang, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { targetLanguage = lang },
                            label = { Text(lang) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                OutlinedTextField(
                    value = targetLanguage,
                    onValueChange = { targetLanguage = it },
                    label = { Text("Language name or code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(Icons.Outlined.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )

                Text(
                    "Translation scope & mode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Mode 1: Current Sentence
                TranslationModeCard(
                    title = "Translate current sentence",
                    description = "Accurate translation of sentence ${currentIndex + 1} into $targetLanguage.",
                    icon = Icons.Outlined.Translate,
                    isSelected = selectedMode == TranslationLauncher.Mode.CURRENT_SECTION,
                    onClick = {
                        selectedMode = TranslationLauncher.Mode.CURRENT_SECTION
                        executeHandoff(null)
                    }
                )

                // Mode 2: Full Document
                TranslationModeCard(
                    title = "Translate full document",
                    description = "Extract and send all ${document.chunks.size} sentences formatted for translation.",
                    icon = Icons.Outlined.Article,
                    isSelected = selectedMode == TranslationLauncher.Mode.DOCUMENT,
                    onClick = {
                        selectedMode = TranslationLauncher.Mode.DOCUMENT
                        executeHandoff(null)
                    }
                )

                // Mode 3: Bilingual Sentence
                TranslationModeCard(
                    title = "Bilingual current sentence",
                    description = "Side-by-side parallel text comparing original with $targetLanguage.",
                    icon = Icons.Outlined.Language,
                    isSelected = selectedMode == TranslationLauncher.Mode.BILINGUAL_SECTION,
                    onClick = {
                        selectedMode = TranslationLauncher.Mode.BILINGUAL_SECTION
                        executeHandoff(null)
                    }
                )

                // Mode 4: Bilingual Full Document
                TranslationModeCard(
                    title = "Bilingual full document",
                    description = "Structured interlinear lines for the entire document.",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    isSelected = selectedMode == TranslationLauncher.Mode.BILINGUAL_DOCUMENT,
                    onClick = {
                        selectedMode = TranslationLauncher.Mode.BILINGUAL_DOCUMENT
                        executeHandoff(null)
                    }
                )
            }
        }
    )
}

@Composable
private fun TranslationModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
internal fun AiFreeModeDialog(
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            "Veritas does not bundle a large offline model and does not require an API key. It prepares prompts for the AI apps already installed on this phone.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                InfoStepCard(title = "1. AI app handoff") {
                    Text("Open a document, tap Reader tools → AI, choose a task, then send the prepared prompt to ChatGPT, Gemini, Claude, Copilot, Perplexity, or another installed app. The prompt is also copied to your clipboard.")
                }
                InfoStepCard(title = "2. Paste the reply back") {
                    Text("Flashcard and quiz replies can be pasted straight back into Veritas — cards join your spaced-repetition deck and quizzes become an in-app scored test.")
                }
                InfoStepCard(title = "3. Base app stays lighter") {
                    Text("No heavy local AI model is bundled in the base app. A real offline model can be optional later as a separate downloadable pack.")
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
internal fun AiCenterDialog(
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
internal fun ExportAudioStatusDialog(
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                Button(onClick = { onShare(file) }) { Text("Share") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !inProgress) { Text("Close") }
        }
    )
}

@Composable
internal fun ScopeSelector(
    selectedScope: AiPromptScope,
    onScopeSelected: (AiPromptScope) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Scope", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiPromptScope.values().forEach { scope ->
                val isSelected = selectedScope == scope
                val label = when (scope) {
                    AiPromptScope.CURRENT_SENTENCE -> "Sentence"
                    AiPromptScope.CURRENT_SECTION -> "Section"
                    AiPromptScope.CUSTOM_PAGE_RANGE -> "Page range"
                    AiPromptScope.WHOLE_DOCUMENT -> "Whole doc"
                }
                if (isSelected) {
                    Button(
                        onClick = { onScopeSelected(scope) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 32.dp)
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onScopeSelected(scope) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 32.dp)
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PageRangeInputs(
    startVal: String,
    onStartChange: (String) -> Unit,
    endVal: String,
    onEndChange: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = startVal,
            onValueChange = { onStartChange(it.filter { char -> char.isDigit() }) },
            label = { Text("From Page", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = endVal,
            onValueChange = { onEndChange(it.filter { char -> char.isDigit() }) },
            label = { Text("To Page", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
internal fun TaskExpandableCard(
    title: String,
    taskKey: String,
    expandedTask: String?,
    onToggleExpand: (String?) -> Unit,
    prompt: String,
    onPromptChange: (String) -> Unit,
    icon: ImageVector? = null,
    badge: String? = null,
    showScopeSelector: Boolean = false,
    selectedScope: AiPromptScope = AiPromptScope.WHOLE_DOCUMENT,
    onScopeSelected: (AiPromptScope) -> Unit = {},
    startPage: String = "",
    onStartPageChange: (String) -> Unit = {},
    endPage: String = "",
    onEndPageChange: (String) -> Unit = {},
    onCopyPrompt: (() -> Unit)? = null,
    onSend: () -> Unit
) {
    val isExpanded = expandedTask == taskKey
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand(if (isExpanded) null else taskKey) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = BorderStroke(
            1.dp,
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Surface(
                            shape = CircleShape,
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (badge != null) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                if (showScopeSelector) {
                    ScopeSelector(
                        selectedScope = selectedScope,
                        onScopeSelected = onScopeSelected
                    )
                }

                if (selectedScope == AiPromptScope.CUSTOM_PAGE_RANGE || (!showScopeSelector && taskKey == "long_doc")) {
                    PageRangeInputs(
                        startVal = startPage,
                        onStartChange = onStartPageChange,
                        endVal = endPage,
                        onEndChange = onEndPageChange
                    )
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    label = { Text("Prompt instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (onCopyPrompt != null) {
                        OutlinedButton(
                            onClick = onCopyPrompt,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Copy Prompt")
                        }
                    }
                    Button(
                        onClick = onSend,
                        modifier = Modifier.weight(if (onCopyPrompt != null) 1.2f else 1f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Send to AI")
                    }
                }
            }
        }
    }
}

@Composable
internal fun AiAppStudyDialog(
    document: ReaderDocument,
    currentIndex: Int,
    templates: List<AiPromptTemplate>,
    history: List<AiPromptHistoryEntry>,
    askAiSettings: AskAiSettings? = null,
    onUpdateAskAiSettings: ((AskAiSettings) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSendToAiApp: (AiPromptType, String, AiPromptScope, IntRange?) -> Unit,
    onSaveTemplate: (String, String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onClearHistory: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onSaveAiResultAsNote: (String) -> Unit,
    onImportFlashcards: (String, List<Flashcard>) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var customPrompt by remember { mutableStateOf("") }
    var templateTitle by remember { mutableStateOf("Custom study prompt") }
    var aiResultDraft by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Tasks") }
    var showPasteFlashcards by remember { mutableStateOf(false) }
    var showPasteQuiz by remember { mutableStateOf(false) }
    var activeQuizQuestions by remember { mutableStateOf<List<QuizQuestion>?>(null) }

    if (showPasteFlashcards) {
        PasteFlashcardsDialog(
            onImport = { name, cards -> onImportFlashcards(name, cards); showPasteFlashcards = false },
            onDismiss = { showPasteFlashcards = false }
        )
    }
    if (showPasteQuiz) {
        PasteQuizDialog(onDismiss = { showPasteQuiz = false })
    }
    activeQuizQuestions?.let { questions ->
        QuizPlayerDialog(questions = questions, onDismiss = { activeQuizQuestions = null })
    }

    val expandedTask = remember { mutableStateOf<String?>(null) }

    val summarizeWholeDocPrompt = remember { mutableStateOf("Summarize the whole document clearly. Give a short overview first, then the main points, then any important conclusions or action items.") }
    val longDocSummaryPrompt = remember { mutableStateOf("This is part of a long-document workflow. Summarize this section or page range only, give 3-6 key points, define difficult terms, and end with a short note saying what the user should send next.") }
    val extractKeypointsPrompt = remember { mutableStateOf("Extract the key points from this document. Group related ideas together and keep the wording clear for revision.") }
    val explainSentencePrompt = remember { mutableStateOf("Explain the current section in simple language. Identify the main idea, difficult terms, and why the section matters in the document.") }
    val studyNotesPrompt = remember { mutableStateOf("Turn this document into organized study notes. Use headings, bullet points, definitions, examples, likely exam areas, and a short final revision checklist.") }
    val simplifyPrompt = remember { mutableStateOf("Rewrite and explain the document in simpler language without removing important meaning. Define difficult words and give short examples where useful.") }
    val quizPrompt = remember { mutableStateOf("Create an exam-style multiple-choice revision quiz from this document. Format every question EXACTLY like this, with one blank line between questions:\nQ: <question>\nA) <option>\nB) <option>\nC) <option>\nD) <option>\nAnswer: <letter>\nExplanation: <one short sentence>") }
    val flashcardsPrompt = remember { mutableStateOf("Create flashcards from this document. Focus on definitions, processes, comparisons, and important facts. Reply as plain text only (no tables, no attachments) with every card formatted EXACTLY like this, one blank line between cards:\nQ: <question>\nA: <concise answer>") }
    val translatePrompt = remember { mutableStateOf("Translate this document accurately while preserving technical terms. Include a short glossary table of key concepts at the end.") }

    val extractKeypointsScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }
    val studyNotesScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }
    val simplifyScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }
    val quizScope = remember { mutableStateOf(AiPromptScope.CUSTOM_PAGE_RANGE) }
    val flashcardsScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }
    val translateScope = remember { mutableStateOf(AiPromptScope.WHOLE_DOCUMENT) }

    val longDocStartPage = remember { mutableStateOf("1") }
    val longDocEndPage = remember { mutableStateOf(document.pageCount.coerceAtLeast(1).toString()) }
    val extractKeypointsStartPage = remember { mutableStateOf("1") }
    val extractKeypointsEndPage = remember { mutableStateOf(document.pageCount.coerceAtLeast(1).toString()) }
    val studyNotesStartPage = remember { mutableStateOf("1") }
    val studyNotesEndPage = remember { mutableStateOf(document.pageCount.coerceAtLeast(1).toString()) }
    val simplifyStartPage = remember { mutableStateOf("1") }
    val simplifyEndPage = remember { mutableStateOf(document.pageCount.coerceAtLeast(1).toString()) }
    val quizStartPage = remember { mutableStateOf("1") }
    val quizEndPage = remember { mutableStateOf(document.pageCount.coerceAtLeast(1).toString()) }
    val flashcardsStartPage = remember { mutableStateOf("1") }
    val flashcardsEndPage = remember { mutableStateOf(document.pageCount.coerceAtLeast(1).toString()) }
    val translateStartPage = remember { mutableStateOf("1") }
    val translateEndPage = remember { mutableStateOf(document.pageCount.coerceAtLeast(1).toString()) }

    val safeIndex =
        if (document.chunks.isEmpty()) 0 else currentIndex.coerceIn(0, document.chunks.lastIndex)
    val estimatedTextLength = document.chunks.sumOf { it.length }
    val currentPage = remember(document, currentIndex) {
        val model = ReaderTextModelCache.get(document.id, document.rawText, document.pageCount)
        val sentence = model.sentences.getOrNull(currentIndex)
        sentence?.pageNumber?.coerceAtLeast(1) ?: 1
    }

    val installedAiList = remember(context) { installedAiOptions(context) }
    val defaultAiId = remember(askAiSettings, installedAiList) {
        if (askAiSettings == null || askAiSettings.assistantId.isBlank() || askAiSettings.assistantId == "chooser") {
            if (installedAiList.isNotEmpty()) installedAiList.first().first.id else "chooser"
        } else {
            askAiSettings.assistantId
        }
    }
    val defaultAiName = remember(askAiSettings, installedAiList) {
        if (askAiSettings == null || askAiSettings.assistantId.isBlank() || askAiSettings.assistantId == "chooser") {
            if (installedAiList.isNotEmpty()) installedAiList.first().first.label else "Share Menu"
        } else {
            aiAssistantOptions.firstOrNull { it.id == askAiSettings.assistantId }?.label ?: "Share Menu"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
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
            ) {
                // Top App Bar
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column {
                                    Text(
                                        "AI Study Handoff",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "${document.title} • Page $currentPage of ${document.pageCount.coerceAtLeast(1)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Quick target assistant pill
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { selectedTab = "AI Apps" }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = aiAssistantIcon(defaultAiId),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        defaultAiName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Tab Bar Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            val tabs = listOf("Tasks", "AI Apps", "Templates", "History", "Smart Importer")
                            tabs.forEach { tab ->
                                val selected = selectedTab == tab
                                if (selected) {
                                    Button(
                                        onClick = { selectedTab = tab },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(tab, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { selectedTab = tab },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(tab, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                // Main Tab Content Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Document Overview Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        document.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${document.sourceLabel.ifBlank { "Document" }} • ${document.chunks.size} sentences • ${document.pageCount} pages • ~$estimatedTextLength characters",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        when (selectedTab) {
                            "Tasks" -> {
                                Text(
                                    "Select a study goal below. Veritas prepares the text with page coordinates, copies the prompt, and opens your chosen AI assistant for instant analysis.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TaskExpandableCard(
                                    title = "Summarize whole document",
                                    badge = "Executive overview, key points & takeaways",
                                    icon = Icons.Outlined.Summarize,
                                    taskKey = "summarize_whole_doc",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = summarizeWholeDocPrompt.value,
                                    onPromptChange = { summarizeWholeDocPrompt.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Summary Prompt", summarizeWholeDocPrompt.value) },
                                    onSend = {
                                        onSendToAiApp(
                                            AiPromptType.SUMMARY,
                                            summarizeWholeDocPrompt.value,
                                            AiPromptScope.WHOLE_DOCUMENT,
                                            null
                                        )
                                    }
                                )

                                TaskExpandableCard(
                                    title = "Section / Page-by-page summary",
                                    badge = "Targeted range summary with term definitions",
                                    icon = Icons.Outlined.Article,
                                    taskKey = "long_doc",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = longDocSummaryPrompt.value,
                                    onPromptChange = { longDocSummaryPrompt.value = it },
                                    startPage = longDocStartPage.value,
                                    onStartPageChange = { longDocStartPage.value = it },
                                    endPage = longDocEndPage.value,
                                    onEndPageChange = { longDocEndPage.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Page Summary Prompt", longDocSummaryPrompt.value) },
                                    onSend = {
                                        val start = longDocStartPage.value.toIntOrNull() ?: 1
                                        val end = longDocEndPage.value.toIntOrNull() ?: document.pageCount
                                        val min = minOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
                                        val max = maxOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
                                        onSendToAiApp(
                                            AiPromptType.SECTION_BY_SECTION,
                                            longDocSummaryPrompt.value,
                                            AiPromptScope.CUSTOM_PAGE_RANGE,
                                            min..max
                                        )
                                    }
                                )

                                TaskExpandableCard(
                                    title = "Extract key points & core ideas",
                                    badge = "Bulleted facts, arguments and findings",
                                    icon = Icons.Outlined.Bolt,
                                    taskKey = "extract_keypoints",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = extractKeypointsPrompt.value,
                                    onPromptChange = { extractKeypointsPrompt.value = it },
                                    showScopeSelector = true,
                                    selectedScope = extractKeypointsScope.value,
                                    onScopeSelected = { extractKeypointsScope.value = it },
                                    startPage = extractKeypointsStartPage.value,
                                    onStartPageChange = { extractKeypointsStartPage.value = it },
                                    endPage = extractKeypointsEndPage.value,
                                    onEndPageChange = { extractKeypointsEndPage.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Keypoints Prompt", extractKeypointsPrompt.value) },
                                    onSend = {
                                        val range = if (extractKeypointsScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                            val start = extractKeypointsStartPage.value.toIntOrNull() ?: 1
                                            val end = extractKeypointsEndPage.value.toIntOrNull() ?: document.pageCount
                                            minOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))..maxOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
                                        } else null
                                        onSendToAiApp(
                                            AiPromptType.KEY_POINTS,
                                            extractKeypointsPrompt.value,
                                            extractKeypointsScope.value,
                                            range
                                        )
                                    }
                                )

                                TaskExpandableCard(
                                    title = "Explain current sentence & section",
                                    badge = "In-depth explanation with context & difficult words",
                                    icon = Icons.Outlined.School,
                                    taskKey = "explain_sentence",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = explainSentencePrompt.value,
                                    onPromptChange = { explainSentencePrompt.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Sentence Prompt", explainSentencePrompt.value) },
                                    onSend = {
                                        onSendToAiApp(
                                            AiPromptType.EXPLAIN_SECTION,
                                            explainSentencePrompt.value,
                                            AiPromptScope.CURRENT_SENTENCE,
                                            null
                                        )
                                    }
                                )

                                TaskExpandableCard(
                                    title = "Create organized study notes",
                                    badge = "Headings, formulas, definitions & review checklist",
                                    icon = Icons.Outlined.Description,
                                    taskKey = "study_notes",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = studyNotesPrompt.value,
                                    onPromptChange = { studyNotesPrompt.value = it },
                                    showScopeSelector = true,
                                    selectedScope = studyNotesScope.value,
                                    onScopeSelected = { studyNotesScope.value = it },
                                    startPage = studyNotesStartPage.value,
                                    onStartPageChange = { studyNotesStartPage.value = it },
                                    endPage = studyNotesEndPage.value,
                                    onEndPageChange = { studyNotesEndPage.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Study Notes Prompt", studyNotesPrompt.value) },
                                    onSend = {
                                        val range = if (studyNotesScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                            val start = studyNotesStartPage.value.toIntOrNull() ?: 1
                                            val end = studyNotesEndPage.value.toIntOrNull() ?: document.pageCount
                                            minOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))..maxOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
                                        } else null
                                        onSendToAiApp(
                                            AiPromptType.STUDY_NOTES,
                                            studyNotesPrompt.value,
                                            studyNotesScope.value,
                                            range
                                        )
                                    }
                                )

                                TaskExpandableCard(
                                    title = "Simplify difficult text & jargon",
                                    badge = "Rewritten in clear, straightforward language",
                                    icon = Icons.Outlined.AutoAwesome,
                                    taskKey = "simplify_text",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = simplifyPrompt.value,
                                    onPromptChange = { simplifyPrompt.value = it },
                                    showScopeSelector = true,
                                    selectedScope = simplifyScope.value,
                                    onScopeSelected = { simplifyScope.value = it },
                                    startPage = simplifyStartPage.value,
                                    onStartPageChange = { simplifyStartPage.value = it },
                                    endPage = simplifyEndPage.value,
                                    onEndPageChange = { simplifyEndPage.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Simplify Prompt", simplifyPrompt.value) },
                                    onSend = {
                                        val range = if (simplifyScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                            val start = simplifyStartPage.value.toIntOrNull() ?: 1
                                            val end = simplifyEndPage.value.toIntOrNull() ?: document.pageCount
                                            minOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))..maxOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
                                        } else null
                                        onSendToAiApp(
                                            AiPromptType.SIMPLIFY,
                                            simplifyPrompt.value,
                                            simplifyScope.value,
                                            range
                                        )
                                    }
                                )

                                TaskExpandableCard(
                                    title = "Create practice exam quiz",
                                    badge = "Multiple choice revision with answers & explanations",
                                    icon = Icons.Outlined.Quiz,
                                    taskKey = "quiz",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = quizPrompt.value,
                                    onPromptChange = { quizPrompt.value = it },
                                    showScopeSelector = true,
                                    selectedScope = quizScope.value,
                                    onScopeSelected = { quizScope.value = it },
                                    startPage = quizStartPage.value,
                                    onStartPageChange = { quizStartPage.value = it },
                                    endPage = quizEndPage.value,
                                    onEndPageChange = { quizEndPage.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Quiz Prompt", quizPrompt.value) },
                                    onSend = {
                                        val range = if (quizScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                            val start = quizStartPage.value.toIntOrNull() ?: 1
                                            val end = quizEndPage.value.toIntOrNull() ?: document.pageCount
                                            minOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))..maxOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
                                        } else null
                                        onSendToAiApp(
                                            AiPromptType.QUIZ,
                                            quizPrompt.value,
                                            quizScope.value,
                                            range
                                        )
                                    }
                                )

                                TaskExpandableCard(
                                    title = "Generate study flashcards",
                                    badge = "Q&A flashcards ready for Veritas study mode",
                                    icon = Icons.Outlined.Style,
                                    taskKey = "flashcards",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = flashcardsPrompt.value,
                                    onPromptChange = { flashcardsPrompt.value = it },
                                    showScopeSelector = true,
                                    selectedScope = flashcardsScope.value,
                                    onScopeSelected = { flashcardsScope.value = it },
                                    startPage = flashcardsStartPage.value,
                                    onStartPageChange = { flashcardsStartPage.value = it },
                                    endPage = flashcardsEndPage.value,
                                    onEndPageChange = { flashcardsEndPage.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Flashcards Prompt", flashcardsPrompt.value) },
                                    onSend = {
                                        val range = if (flashcardsScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                            val start = flashcardsStartPage.value.toIntOrNull() ?: 1
                                            val end = flashcardsEndPage.value.toIntOrNull() ?: document.pageCount
                                            minOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))..maxOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
                                        } else null
                                        onSendToAiApp(
                                            AiPromptType.FLASHCARDS,
                                            flashcardsPrompt.value,
                                            flashcardsScope.value,
                                            range
                                        )
                                    }
                                )

                                TaskExpandableCard(
                                    title = "Translate & build terminology glossary",
                                    badge = "Language translation with key technical term definitions",
                                    icon = Icons.Outlined.Translate,
                                    taskKey = "translate_glossary",
                                    expandedTask = expandedTask.value,
                                    onToggleExpand = { expandedTask.value = it },
                                    prompt = translatePrompt.value,
                                    onPromptChange = { translatePrompt.value = it },
                                    showScopeSelector = true,
                                    selectedScope = translateScope.value,
                                    onScopeSelected = { translateScope.value = it },
                                    startPage = translateStartPage.value,
                                    onStartPageChange = { translateStartPage.value = it },
                                    endPage = translateEndPage.value,
                                    onEndPageChange = { translateEndPage.value = it },
                                    onCopyPrompt = { onCopyText("Veritas Translation Prompt", translatePrompt.value) },
                                    onSend = {
                                        val range = if (translateScope.value == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                            val start = translateStartPage.value.toIntOrNull() ?: 1
                                            val end = translateEndPage.value.toIntOrNull() ?: document.pageCount
                                            minOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))..maxOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
                                        } else null
                                        onSendToAiApp(
                                            AiPromptType.CUSTOM,
                                            translatePrompt.value,
                                            translateScope.value,
                                            range
                                        )
                                    }
                                )
                            }

                            "AI Apps" -> {
                                Text(
                                    "Choose which AI assistant Veritas will automatically target when sending study prompts, or launch installed AI apps directly.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                aiAssistantOptions.forEach { option ->
                                    val isInstalled = remember(option, context) {
                                        if (option.id == "chooser") true
                                        else installedPackageForOption(context, option)?.isNotBlank() == true
                                    }
                                    val isCurrent = remember(option, askAiSettings) {
                                        if (askAiSettings == null || askAiSettings.assistantId.isBlank()) {
                                            option.id == "chooser"
                                        } else {
                                            option.id == askAiSettings.assistantId
                                        }
                                    }

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = isInstalled) {
                                                val newSettings = (askAiSettings ?: AskAiSettings()).copy(
                                                    assistantId = option.id,
                                                    assistantLabel = option.label,
                                                    packageName = option.packageName
                                                )
                                                onUpdateAskAiSettings?.invoke(newSettings)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrent)
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        ),
                                        border = BorderStroke(
                                            if (isCurrent) 1.5.dp else 1.dp,
                                            if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = aiAssistantIcon(option.id),
                                                            contentDescription = null,
                                                            tint = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(
                                                            option.label,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                        if (isCurrent) {
                                                            Surface(
                                                                shape = RoundedCornerShape(50),
                                                                color = MaterialTheme.colorScheme.primary,
                                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                                            ) {
                                                                Text(
                                                                    "Active Target",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        when {
                                                            option.id == "chooser" -> "Opens Android sharing chooser every time"
                                                            isInstalled -> "Installed on this device"
                                                            else -> "Not installed on device"
                                                        },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                if (isInstalled) {
                                                    if (option.packageName.isNotBlank()) {
                                                        IconButton(
                                                            onClick = {
                                                                val pkg = installedPackageForOption(context, option) ?: option.packageName
                                                                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                                                                if (launchIntent != null) {
                                                                    context.startActivity(launchIntent)
                                                                } else {
                                                                    openPlayStoreForPackage(context, option.packageName)
                                                                }
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                                contentDescription = "Open App",
                                                                modifier = Modifier.size(17.dp),
                                                                tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                } else if (option.packageName.isNotBlank()) {
                                                    OutlinedButton(
                                                        onClick = { openPlayStoreForPackage(context, option.packageName) },
                                                        shape = RoundedCornerShape(50),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Install", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "Templates" -> {
                                Text(
                                    "Save customized prompts for quick re-use across all your books and study documents.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("Create New Template", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        OutlinedTextField(
                                            value = templateTitle,
                                            onValueChange = { templateTitle = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Template Name") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        OutlinedTextField(
                                            value = customPrompt,
                                            onValueChange = { customPrompt = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp),
                                            label = { Text("Prompt Instructions") },
                                            placeholder = { Text("e.g. Act as a tutor and quiz me step-by-step on this material...") },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    onSaveTemplate(templateTitle, customPrompt)
                                                    customPrompt = ""
                                                    templateTitle = "Custom study prompt"
                                                },
                                                enabled = customPrompt.isNotBlank(),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Save")
                                            }
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
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Send Now")
                                            }
                                        }
                                    }
                                }

                                Text("Saved Custom Templates", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                                if (templates.isEmpty()) {
                                    Text(
                                        "No custom templates saved yet. Create one above or use the built-in study tasks.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else {
                                    templates.forEach { template ->
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(template.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                Text(
                                                    template.instruction,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Row(
                                                    horizontalArrangement = Arrangement.End,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    TextButton(onClick = { onDeleteTemplate(template.id) }) {
                                                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Delete")
                                                    }
                                                    TextButton(onClick = { onCopyText("Veritas AI template", template.instruction) }) {
                                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Copy")
                                                    }
                                                    Button(
                                                        onClick = {
                                                            onSendToAiApp(
                                                                AiPromptType.CUSTOM,
                                                                template.instruction,
                                                                AiPromptScope.WHOLE_DOCUMENT,
                                                                null
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(50)
                                                    ) {
                                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Use")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "History" -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Recent AI Prompts",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (history.isNotEmpty()) {
                                        TextButton(onClick = onClearHistory) {
                                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Clear history")
                                        }
                                    }
                                }

                                if (history.isEmpty()) {
                                    Text(
                                        "Prompts and questions you hand off to AI assistants will be tracked here for quick re-use.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    history.forEach { item ->
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    ) {
                                                        Text(
                                                            "${item.promptType} • ${item.scope}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Text(
                                                        formatUpdated(item.createdAt),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Text(
                                                    item.documentTitle,
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                                Text(
                                                    item.promptPreview,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 5,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Row(
                                                    horizontalArrangement = Arrangement.End,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    TextButton(onClick = { onCopyText("Veritas AI prompt", item.promptPreview) }) {
                                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Copy")
                                                    }
                                                    Button(
                                                        onClick = {
                                                            onSendToAiApp(
                                                                AiPromptType.CUSTOM,
                                                                item.promptPreview,
                                                                AiPromptScope.WHOLE_DOCUMENT,
                                                                null
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(50)
                                                    ) {
                                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Re-send")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "Smart Importer" -> {
                                Text(
                                    "When the AI assistant replies, copy its text, return here, and paste it below. Veritas automatically detects whether it's flashcards, a quiz, or study notes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val parsedCards = remember(aiResultDraft) { AiResultParser.parseFlashcards(aiResultDraft) }
                                val parsedQuiz = remember(aiResultDraft) { AiResultParser.parseQuiz(aiResultDraft) }

                                OutlinedTextField(
                                    value = aiResultDraft,
                                    onValueChange = { aiResultDraft = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    label = { Text("Paste AI reply here") },
                                    placeholder = { Text("Paste summary, explanation, Q&A flashcards, or exam quiz...") },
                                    shape = RoundedCornerShape(14.dp)
                                )

                                // Auto detection badge
                                if (aiResultDraft.isNotBlank()) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.AutoAwesome,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                when {
                                                    parsedCards.isNotEmpty() && parsedQuiz.isNotEmpty() ->
                                                        "Detected: ${parsedCards.size} Flashcard(s) & ${parsedQuiz.size} Quiz question(s)"
                                                    parsedCards.isNotEmpty() ->
                                                        "Detected: ${parsedCards.size} Flashcard(s) (Q/A format)"
                                                    parsedQuiz.isNotEmpty() ->
                                                        "Detected: ${parsedQuiz.size} Quiz question(s) (Multiple-choice format)"
                                                    else ->
                                                        "Detected: Study Note / Text Explanation"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                // Quick Action Buttons
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (parsedCards.isNotEmpty()) {
                                        Button(
                                            onClick = {
                                                onImportFlashcards(document.title, parsedCards)
                                                aiResultDraft = ""
                                                Toast.makeText(context, "Imported ${parsedCards.size} flashcards!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Outlined.Style, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Import ${parsedCards.size} Flashcard${if (parsedCards.size == 1) "" else "s"}")
                                        }
                                    }

                                    if (parsedQuiz.isNotEmpty()) {
                                        Button(
                                            onClick = {
                                                activeQuizQuestions = parsedQuiz
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Outlined.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Take Interactive Quiz (${parsedQuiz.size} Questions)")
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onSaveAiResultAsNote(aiResultDraft)
                                            aiResultDraft = ""
                                            Toast.makeText(context, "Saved note to sentence ${safeIndex + 1}!", Toast.LENGTH_SHORT).show()
                                        },
                                        enabled = aiResultDraft.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Save Result to Current Sentence Note")
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedButton(
                                            onClick = { showPasteFlashcards = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text("Custom Flashcard Importer")
                                        }
                                        OutlinedButton(
                                            onClick = { showPasteQuiz = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text("Custom Quiz Importer")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun InfoStepCard(
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

@Composable
internal fun PasteFlashcardsDialog(
    onImport: (String, List<Flashcard>) -> Unit,
    onDismiss: () -> Unit
) {
    var pasted by remember { mutableStateOf("") }
    var setName by remember { mutableStateOf("") }
    val parsed = remember(pasted) { AiResultParser.parseFlashcards(pasted) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add flashcard set") },
        confirmButton = {
            Button(onClick = { onImport(setName, parsed) }, enabled = parsed.isNotEmpty()) {
                Text(if (parsed.isEmpty()) "Import" else "Import ${parsed.size} card${if (parsed.size == 1) "" else "s"}")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = setName,
                    onValueChange = { setName = it },
                    singleLine = true,
                    label = { Text("Set name (optional)") },
                    placeholder = { Text("Untitled set") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Paste the AI app's reply below. Cards are detected from Q:/A: (or Front:/Back:) pairs.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = pasted,
                    onValueChange = { pasted = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    label = { Text("AI reply") }
                )
                Text(
                    when {
                        pasted.isBlank() -> "Waiting for pasted text…"
                        parsed.isEmpty() -> "No cards recognized yet — check the Q:/A: format."
                        else -> "Found ${parsed.size} card${if (parsed.size == 1) "" else "s"}. First: “${parsed.first().front.take(60)}”"
                    },
                    color = if (parsed.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
internal fun PasteQuizDialog(onDismiss: () -> Unit) {
    var pasted by remember { mutableStateOf("") }
    var quiz by remember { mutableStateOf<List<QuizQuestion>?>(null) }
    val parsed = remember(pasted) { AiResultParser.parseQuiz(pasted) }

    quiz?.let { questions ->
        QuizPlayerDialog(questions = questions, onDismiss = onDismiss)
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Take a pasted quiz") },
        confirmButton = {
            Button(onClick = { quiz = parsed }, enabled = parsed.isNotEmpty()) {
                Text(if (parsed.isEmpty()) "Start quiz" else "Start quiz (${parsed.size})")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Paste the AI app's quiz reply below (Q: / A) B) C) D) / Answer: format).",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = pasted,
                    onValueChange = { pasted = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    label = { Text("AI reply") }
                )
                Text(
                    when {
                        pasted.isBlank() -> "Waiting for pasted text…"
                        parsed.isEmpty() -> "No questions recognized yet — check the format."
                        else -> "Found ${parsed.size} question${if (parsed.size == 1) "" else "s"}."
                    },
                    color = if (parsed.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
internal fun QuizPlayerDialog(
    questions: List<QuizQuestion>,
    onDismiss: () -> Unit
) {
    var index by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (finished) "Quiz complete" else "Question ${index + 1} of ${questions.size}") },
        confirmButton = {
            when {
                finished -> Button(onClick = onDismiss) { Text("Done") }
                selected != null -> Button(onClick = {
                    if (index + 1 >= questions.size) finished = true
                    else {
                        index++
                        selected = null
                    }
                }) { Text(if (index + 1 >= questions.size) "See score" else "Next") }
                else -> TextButton(onClick = onDismiss) { Text("Quit") }
            }
        },
        text = {
            if (finished) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "You scored $score of ${questions.size}.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when {
                            score == questions.size -> "Perfect — every answer right."
                            score >= questions.size * 3 / 4 -> "Strong revision — nearly there."
                            score >= questions.size / 2 -> "Good base. Re-read the sections behind the misses."
                            else -> "Worth another pass through the document before retrying."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val question = questions[index]
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(question.question, fontWeight = FontWeight.SemiBold)
                    question.options.forEach { option ->
                        val isPicked = selected == option
                        val isCorrect = option == question.answer
                        val showState = selected != null
                        OutlinedButton(
                            onClick = {
                                if (selected == null) {
                                    selected = option
                                    if (isCorrect) score++
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = when {
                                    showState && isCorrect -> Color(0xFF10B981)
                                    showState && isPicked -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        ) {
                            Text(
                                text = when {
                                    showState && isCorrect -> "✓ $option"
                                    showState && isPicked -> "✗ $option"
                                    else -> option
                                },
                                fontWeight = if (showState && (isCorrect || isPicked)) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    if (selected != null && question.explanation.isNotBlank()) {
                        Text(
                            question.explanation,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}

data class VoicePreset(
    val name: String,
    val rate: Float,
    val pitch: Float,
    val summary: String
)

fun voicePresets(): List<VoicePreset> = listOf(
    VoicePreset("Balanced", 1.0f, 1.0f, "Natural everyday audio reading with standard timing."),
    VoicePreset("Study focus", 0.92f, 0.99f, "Deliberate pacing with grounded tone for deep comprehension."),
    VoicePreset("Quick scan", 1.25f, 1.01f, "Fast energetic review with crisp consonant clarity."),
    VoicePreset("Storyteller", 0.95f, 0.97f, "Warm, chest-resonant narration for fiction and novels."),
    VoicePreset("Lecture bright", 1.10f, 1.03f, "Confident, articulate delivery for academic and business text."),
    VoicePreset("Bedtime calm", 0.85f, 0.98f, "Slow, soothing, mellow tone for quiet night-time listening.")
)


internal fun textEditorDownloadName(
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

internal fun countSearchOccurrences(source: String, query: String): Int {
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

internal fun openOriginalDocument(
    context: Context,
    repository: DocumentRepository,
    document: SavedDocument
) {
    val original = repository.originalFile(document)
    if (original == null) {
        Toast.makeText(
            context,
            "No stored original is available for this reading.",
            Toast.LENGTH_SHORT
        ).show()
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
        Toast.makeText(context, "No app can open this original document.", Toast.LENGTH_SHORT)
            .show()
    }
}

internal fun openAllFilesAccessSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        Toast.makeText(
            context,
            "All Files access is already available on this Android version.",
            Toast.LENGTH_SHORT
        ).show()
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
                Toast.makeText(
                    context,
                    "Could not open All Files access settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}

internal fun veritasBackupFileName(prefix: String): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "${prefix}_$timestamp.json"
}

internal fun veritasBackupZipFileName(prefix: String): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "${prefix}_$timestamp.zip"
}

internal fun veritasBackupMimeTypes(): Array<String> = arrayOf(
    "application/json",
    "text/json",
    "text/plain",
    "application/zip",
    "application/octet-stream"
)

@Composable
internal fun TextEditorDialog(
    document: ReaderDocument,
    currentIndex: Int,
    text: String,
    target: VeritasTextEditTarget,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDownloadToPhone: () -> Unit,
    onDismiss: () -> Unit
) {
    val model = remember(document.id, document.rawText) {
        ReaderTextModelCache.get(document.id, document.rawText, document.pageCount)
    }
    val parts = remember(model) { model.parts }
    val totalPages = remember(parts) { parts.size.coerceAtLeast(1) }

    val initialPartIndex = remember(target, currentIndex, parts) {
        when (target) {
            is VeritasTextEditTarget.Part -> target.partIndex.coerceIn(0, totalPages - 1)
            is VeritasTextEditTarget.SentenceRange -> {
                val p = model.partForSentence(target.startSentenceIndex)
                p?.index?.coerceIn(0, totalPages - 1) ?: 0
            }
        }
    }

    var currentPartIndex by remember { mutableIntStateOf(initialPartIndex) }
    var showSearch by remember { mutableStateOf(false) }
    var showReplace by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }

    var editorValue by remember(currentPartIndex) {
        val initialText = if (currentPartIndex == initialPartIndex && text.isNotBlank()) {
            text
        } else {
            parts.getOrNull(currentPartIndex)?.text.orEmpty()
        }
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    var undoStack by remember(currentPartIndex) { mutableStateOf<List<TextFieldValue>>(emptyList()) }
    var redoStack by remember(currentPartIndex) { mutableStateOf<List<TextFieldValue>>(emptyList()) }

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

    fun applyPrefixToLines(linePrefix: String) {
        val value = editorValue
        val start = value.selection.min.coerceIn(0, value.text.length)
        val end = value.selection.max.coerceIn(0, value.text.length)
        val selected = value.text.substring(start, end)
        val transformed = if (selected.contains("\n")) {
            selected.lines().joinToString("\n") { if (it.isNotBlank()) "$linePrefix $it" else it }
        } else {
            "$linePrefix $selected"
        }
        val nextText = value.text.replaceRange(start, end, transformed)
        commitValue(TextFieldValue(nextText, selection = TextRange(start, start + transformed.length)))
    }

    fun cleanAndNormalizeText() {
        val clean = editorValue.text
            .replace(Regex("(\\w+)-\\n(\\w+)"), "$1$2") // fix broken hyphenated linebreaks
            .replace(Regex("[ \\t]+"), " ")             // fix multiple spaces
            .replace(Regex("\\n{3,}"), "\n\n")          // fix excessive newlines
            .trim()
        commitValue(TextFieldValue(clean, selection = TextRange(clean.length)))
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

    fun replaceCurrentMatch() {
        val needle = searchQuery.trim()
        if (needle.isBlank()) return
        val value = editorValue
        val start = value.selection.min.coerceIn(0, value.text.length)
        val end = value.selection.max.coerceIn(0, value.text.length)
        val selected = value.text.substring(start, end)
        if (selected.equals(needle, ignoreCase = true)) {
            val nextText = value.text.replaceRange(start, end, replaceQuery)
            commitValue(TextFieldValue(nextText, selection = TextRange(start + replaceQuery.length)))
            findNextSearchMatch()
        } else {
            findNextSearchMatch()
        }
    }

    fun replaceAllMatches() {
        val needle = searchQuery.trim()
        if (needle.isBlank()) return
        val nextText = editorValue.text.replace(needle, replaceQuery, ignoreCase = true)
        commitValue(TextFieldValue(nextText, selection = TextRange(nextText.length)))
    }

    val wordCount = remember(editorValue.text) {
        editorValue.text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }
    val charCount = editorValue.text.length
    val searchMatches = remember(editorValue.text, searchQuery) {
        countSearchOccurrences(editorValue.text, searchQuery)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
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
                    .imePadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            "Edit extracted text",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            document.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showSearch = !showSearch; if (showSearch) showReplace = false },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = if (showSearch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showReplace = !showReplace; if (showReplace) showSearch = false },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Filled.FindReplace,
                            contentDescription = "Find & Replace",
                            tint = if (showReplace) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDownloadToPhone,
                        enabled = editorValue.text.isNotBlank(),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = "Export Text", modifier = Modifier.size(20.dp))
                    }
                    Button(
                        onClick = onSave,
                        enabled = editorValue.text.isNotBlank(),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Per-Page Navigator Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (currentPartIndex > 0) {
                                    currentPartIndex--
                                }
                            },
                            enabled = currentPartIndex > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Previous Page")
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "Page ${currentPartIndex + 1} of $totalPages",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (currentPartIndex < totalPages - 1) {
                                    currentPartIndex++
                                }
                            },
                            enabled = currentPartIndex < totalPages - 1
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next Page")
                        }
                    }
                }

                // Search Panel
                if (showSearch) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it.take(120) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Find in page") },
                                shape = RoundedCornerShape(8.dp)
                            )
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    "$searchMatches found",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            Button(
                                onClick = ::findNextSearchMatch,
                                enabled = searchMatches > 0,
                                shape = RoundedCornerShape(50)
                            ) { Text("Next") }
                        }
                    }
                }

                // Replace Panel
                if (showReplace) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it.take(120) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    label = { Text("Find") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = replaceQuery,
                                    onValueChange = { replaceQuery = it.take(120) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    label = { Text("Replace with") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = ::replaceCurrentMatch, enabled = searchQuery.isNotBlank()) {
                                    Text("Replace Next")
                                }
                                Button(onClick = ::replaceAllMatches, enabled = searchQuery.isNotBlank(), shape = RoundedCornerShape(50)) {
                                    Text("Replace All")
                                }
                            }
                        }
                    }
                }

                // Word count banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$wordCount words • $charCount characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Rich Text Editor Area
                BasicTextField(
                    value = editorValue,
                    onValueChange = { next -> commitValue(next) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = (MaterialTheme.typography.bodyLarge.fontSize.value + 8).sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Material 3 Rich Editing Toolbar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val previous = undoStack.lastOrNull() ?: return@IconButton
                                undoStack = undoStack.dropLast(1)
                                redoStack = (redoStack + editorValue).takeLast(80)
                                editorValue = previous
                                onTextChange(previous.text)
                            },
                            enabled = undoStack.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                        }

                        IconButton(
                            onClick = {
                                val next = redoStack.lastOrNull() ?: return@IconButton
                                redoStack = redoStack.dropLast(1)
                                undoStack = (undoStack + editorValue).takeLast(80)
                                editorValue = next
                                onTextChange(next.text)
                            },
                            enabled = redoStack.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        IconButton(onClick = { replaceSelection("**") }) {
                            Icon(Icons.Filled.FormatBold, contentDescription = "Bold")
                        }

                        IconButton(onClick = { replaceSelection("_") }) {
                            Icon(Icons.Filled.FormatItalic, contentDescription = "Italic")
                        }

                        IconButton(onClick = { replaceSelection("<u>", "</u>") }) {
                            Icon(Icons.Filled.FormatUnderlined, contentDescription = "Underline")
                        }

                        IconButton(onClick = { applyPrefixToLines("##") }) {
                            Icon(Icons.Filled.Title, contentDescription = "Heading")
                        }

                        IconButton(onClick = { applyPrefixToLines("•") }) {
                            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Bulleted List")
                        }

                        IconButton(onClick = { applyPrefixToLines("1.") }) {
                            Icon(Icons.Filled.FormatListNumbered, contentDescription = "Numbered List")
                        }

                        IconButton(onClick = { applyPrefixToLines(">") }) {
                            Icon(Icons.Filled.FormatQuote, contentDescription = "Quote")
                        }

                        IconButton(onClick = ::cleanAndNormalizeText) {
                            Icon(Icons.Filled.CleaningServices, contentDescription = "Clean whitespace & formatting")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TutorialDialog(
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
                "Pick voices, adjust speed/pitch, and control playback from the expandable panel or system notification.",
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
    var stepIndex by remember { mutableIntStateOf(0) }
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
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
internal fun TutorialStage(frame: TutorialFrame, progress: Float, pulse: Float) {
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
internal fun TutorialStep(
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

internal data class TutorialFrame(
    val number: String,
    val title: String,
    val body: String,
    val icon: String,
    val action: (() -> Unit)?
)

@Composable
internal fun FloatingRecordOverlay(
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
internal fun RecordPillDot(
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

internal fun formatRecordElapsed(seconds: Long): String {
    val safeSeconds = seconds.coerceAtLeast(0L)
    val minutes = safeSeconds / 60L
    val remainingSeconds = safeSeconds % 60L
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Composable
internal fun AppHealthDialog(
    documentCount: Int,
    queueCount: Int,
    themePackName: String,
    themeName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Close") } },
        title = { Text("App health") },
        text = {
            Column(
                modifier = Modifier
                    .height(560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Veritas Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        val context = LocalContext.current
                        val appVersion = remember(context) {
                            runCatching {
                                context.packageManager.getPackageInfo(context.packageName, 0).versionName
                            }.getOrNull() ?: "2.0.0"
                        }
                        Text(
                            "Version: $appVersion",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Library: $documentCount reading${if (documentCount == 1) "" else "s"} • $queueCount queued",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Theme: $themePackName / $themeName",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
internal fun ReadinessBlock(title: String, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
            items.forEach { item ->
                Text(
                    "• $item",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


object VeritasPackStyle {
    @Composable
    fun currentPackId(): String =
        VeritasThemePackCatalog.normalizePackId(VeritasThemeState.themePackId)

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
        "liquid_glass" -> 0.85f
        "one_ui" -> 0.94f
        "material_you" -> 0.88f
        else -> 0.78f
    }

    @Composable
    fun bottomNavShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        "one_ui" -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        else -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    }

    @Composable
    fun bottomNavPadding(): androidx.compose.ui.unit.Dp = 0.dp

    // Liquid Glass & Liquid Material get a real glass edge: a catch-light that's brightest along the
    // top rim and fades to a faint primary tint — the cue that sells "glossy pane".
    // Other packs keep the standard hairline outline.
    @Composable
    fun cardBorder(colorScheme: ColorScheme): BorderStroke = when (currentPackId()) {
        "liquid_glass" -> BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.60f),
                    Color.White.copy(alpha = 0.12f),
                    colorScheme.primary.copy(alpha = 0.25f)
                )
            )
        )
        else -> BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f))
    }

    @Composable
    fun backgroundBrush(colorScheme: ColorScheme): Brush = when (currentPackId()) {
        "liquid_glass" -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.primaryContainer.copy(alpha = 0.14f),
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


fun packPreviewSymbols(packId: String): String =
    when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "material_you" -> "rounded • adaptive • soft"
        "liquid_glass" -> "translucent • floating • glossy"
        "one_ui" -> "large • reachable • calm"
        else -> "media • compact • bold"
    }


// Swatches are the theme's REAL scheme values — [background, primary, key accent] —
// so what the picker shows is exactly what the app paints once the theme is applied.
fun themePreviewColors(themeId: String): List<Color> {
    return when (VeritasThemeCatalog.normalizeThemeId(themeId)) {
        "light" -> listOf(Color(0xFFFAF7F2), Color(0xFFC07318), Color(0xFF52695C))
        "neon" -> listOf(Color(0xFF08090C), Color(0xFF00E5FF), Color(0xFF00E676))
        "solarized_dark" -> listOf(Color(0xFF002B36), Color(0xFF2AA198), Color(0xFFB58900))
        "tomorrow_night_blue" -> listOf(Color(0xFF002451), Color(0xFFBBDAFF), Color(0xFFFFC58F))
        "dark_high_contrast" -> listOf(Color.Black, Color.White, Color(0xFFFFD400))
        "white_high_contrast" -> listOf(Color.White, Color.Black, Color(0xFF004B65))
        "bw_gradient_light" -> listOf(Color(0xFFFAFAFA), Color(0xFF111111), Color(0xFF707070))
        "bw_gradient_dark" -> listOf(Color(0xFF050505), Color(0xFFF2F2F2), Color(0xFF9A9A9A))
        "blue_high_contrast" -> listOf(Color(0xFF001B3A), Color(0xFFBDE9FF), Color(0xFFFFF176))
        "one_dark_pro" -> listOf(Color(0xFF21252B), Color(0xFF61AFEF), Color(0xFF98C379))
        "github_dark" -> listOf(Color(0xFF0D1117), Color(0xFF58A6FF), Color(0xFF3FB950))
        "github_light" -> listOf(Color(0xFFFFFFFF), Color(0xFF0969DA), Color(0xFF1A7F37))
        "dracula" -> listOf(Color(0xFF282A36), Color(0xFFBD93F9), Color(0xFFFF79C6))
        "material_you" -> listOf(Color(0xFFFFFBFE), Color(0xFF6750A4), Color(0xFF7D5260))
        "midnight_dark" -> listOf(Color(0xFF0F172A), Color(0xFFA79BFF), Color(0xFF9BD8E0))
        "dark" -> listOf(Color(0xFF151515), Color(0xFFE5A93C), Color(0xFF81B29A))
        "system" -> listOf(Color(0xFFFAF7F2), Color(0xFF151515), Color(0xFFC07318))
        else -> listOf(Color(0xFF151515), Color(0xFFE5A93C), Color(0xFF81B29A))
    }
}


@Composable
fun AnnotationPill(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AnnotationPill(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}


internal fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
    val r = color1.red * (1f - ratio) + color2.red * ratio
    val g = color1.green * (1f - ratio) + color2.green * ratio
    val b = color1.blue * (1f - ratio) + color2.blue * ratio
    return Color(r, g, b, 1f)
}

internal fun adaptColorScheme(base: ColorScheme, palette: androidx.palette.graphics.Palette, isLight: Boolean): ColorScheme {
    val dominantColor = Color(palette.getDominantColor(base.primary.toArgb()))
    val vibrantColor = Color(palette.getVibrantColor(base.primary.toArgb()))
    val darkVibrant = Color(palette.getDarkVibrantColor(base.primary.toArgb()))
    val lightVibrant = Color(palette.getLightVibrantColor(base.primary.toArgb()))
    val muted = Color(palette.getMutedColor(base.secondary.toArgb()))

    return if (!isLight) {
        val bgTint = blendColors(dominantColor, base.background, 0.88f)
        val surfTint = blendColors(dominantColor, base.surface, 0.88f)
        val surfVarTint = blendColors(dominantColor, base.surfaceVariant, 0.88f)
        
        base.copy(
            primary = if (vibrantColor != base.primary) vibrantColor else lightVibrant,
            primaryContainer = blendColors(dominantColor, base.primaryContainer, 0.7f),
            secondary = if (muted != base.secondary) muted else dominantColor,
            secondaryContainer = blendColors(dominantColor, base.secondaryContainer, 0.8f),
            background = bgTint,
            surface = surfTint,
            surfaceVariant = surfVarTint
        )
    } else {
        val bgTint = blendColors(dominantColor, base.background, 0.93f)
        val surfTint = blendColors(dominantColor, base.surface, 0.95f)
        val surfVarTint = blendColors(dominantColor, base.surfaceVariant, 0.93f)
        
        base.copy(
            primary = if (vibrantColor != base.primary) vibrantColor else dominantColor,
            primaryContainer = blendColors(dominantColor, base.primaryContainer, 0.85f),
            secondary = if (muted != base.secondary) muted else dominantColor,
            secondaryContainer = blendColors(dominantColor, base.secondaryContainer, 0.9f),
            background = bgTint,
            surface = surfTint,
            surfaceVariant = surfVarTint
        )
    }
}

@Composable
internal fun VeritasTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val selectedTheme = VeritasThemeCatalog.normalizeThemeId(VeritasThemeState.themeId)
    val selectedPack = VeritasThemePackCatalog.normalizePackId(VeritasThemeState.themePackId)
    val reduceMotion = VeritasThemeState.reduceMotion
    
    val resolvedTheme = if (selectedTheme == "system") {
        if (isSystemInDarkTheme()) "dark" else "light"
    } else {
        selectedTheme
    }

    val isLight = when (resolvedTheme) {
        "light", "white_high_contrast", "bw_gradient_light", "github_light" -> true
        else -> false
    }

    val activeDocId = VeritasThemeState.activeDocumentId
    val adaptiveCover = VeritasThemeState.adaptiveCover
    var coverPalette by remember(activeDocId) { mutableStateOf<androidx.palette.graphics.Palette?>(null) }
    
    LaunchedEffect(activeDocId, adaptiveCover) {
        if (adaptiveCover && activeDocId != null) {
            val palette = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val file = CoverExtractor.coverFile(context, activeDocId)
                if (file != null && file.exists()) {
                    runCatching {
                        val bmp = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                        if (bmp != null) androidx.palette.graphics.Palette.from(bmp).generate() else null
                    }.getOrNull()
                } else null
            }
            coverPalette = palette
        } else {
            coverPalette = null
        }
    }

    val baseColorScheme = veritasColorScheme(resolvedTheme, context)
    val palette = coverPalette
    
    val blendedColorScheme = if (adaptiveCover && palette != null) {
        adaptColorScheme(baseColorScheme, palette, isLight)
    } else {
        baseColorScheme
    }

    val colorScheme = veritasPackColorScheme(
        base = blendedColorScheme,
        packId = selectedPack
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.primaryContainer.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = colorScheme.primaryContainer.toArgb()
                val isLightContainer = colorScheme.primaryContainer.luminance() > 0.45f
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = isLightContainer
                insetsController.isAppearanceLightNavigationBars = isLightContainer
            }
        }
    }

    val uiFont = com.veritas.reader.ui.VeritasUiFont.fromId(VeritasThemeState.uiFontId)
    androidx.compose.runtime.CompositionLocalProvider(
        com.veritas.reader.ui.LocalVeritasMotion provides
            remember(reduceMotion) { com.veritas.reader.ui.VeritasMotionScheme(reduceMotion) }
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = remember(uiFont) { com.veritas.reader.ui.veritasTypography(uiFont) },
            shapes = veritasPackShapes(selectedPack),
            content = content
        )
    }
}

internal fun veritasPackShapes(packId: String): Shapes {
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

internal fun veritasPackColorScheme(base: ColorScheme, packId: String): ColorScheme {
    val isDark = base.background.luminance() < 0.3f
    return when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "liquid_glass" -> base.copy(
            surface = base.surface.copy(alpha = if (isDark) 0.28f else 0.38f),
            surfaceVariant = base.surfaceVariant.copy(alpha = if (isDark) 0.20f else 0.28f),
            primaryContainer = base.primaryContainer.copy(alpha = if (isDark) 0.65f else 0.80f),
            secondaryContainer = base.secondaryContainer.copy(alpha = if (isDark) 0.55f else 0.70f),
            tertiaryContainer = base.tertiaryContainer.copy(alpha = if (isDark) 0.50f else 0.65f)
        )

        "one_ui" -> base.copy(
            surface = base.surface,
            surfaceVariant = base.secondaryContainer.copy(alpha = 0.94f),
            primaryContainer = base.primaryContainer.copy(alpha = 0.98f),
            tertiaryContainer = base.tertiaryContainer.copy(alpha = 0.90f)
        )

        "material_you" -> base.copy(
            surface = base.surface.copy(alpha = 0.90f),
            surfaceVariant = base.surfaceVariant.copy(alpha = 0.35f),
            primaryContainer = base.primaryContainer.copy(alpha = 0.50f),
            secondaryContainer = base.secondaryContainer.copy(alpha = 0.45f),
            tertiaryContainer = base.tertiaryContainer.copy(alpha = 0.40f)
        )

        else -> base
    }
}

internal fun veritasColorScheme(themeId: String, context: Context): ColorScheme {
    return when (VeritasThemeCatalog.normalizeThemeId(themeId)) {
        "light" -> lightColorScheme(
            primary = Color(0xFFC07318),          // Rich Darker Amber Gold
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFDE8CC),
            onPrimaryContainer = Color(0xFF3B1E00),
            secondary = Color(0xFF8C5E3C),        // Warm Terracotta
            secondaryContainer = Color(0xFFF5ECE4),
            onSecondaryContainer = Color(0xFF321A0C),
            tertiary = Color(0xFF52695C),         // Soft Sage
            tertiaryContainer = Color(0xFFDCEDE4),
            onTertiaryContainer = Color(0xFF102117),
            background = Color(0xFFFAF7F2),       // Warm Cream Paper
            surface = Color(0xFFFFFFFF),          // Crisp White Card
            surfaceVariant = Color(0xFFF0EAE1),   // Warm Linen Container / Chip
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F3EC),
            surfaceContainer = Color(0xFFF2ECE3),
            surfaceContainerHigh = Color(0xFFEBE4D9),
            surfaceContainerHighest = Color(0xFFE3DCCE),
            onSurface = Color(0xFF1E1B18),        // Warm Espresso Charcoal Text
            onSurfaceVariant = Color(0xFF6E675E), // Warm Neutral Muted Gray
            outline = Color(0xFFD6CEC4),
            outlineVariant = Color(0xFFE8E1D7),
            error = Color(0xFFBA1A1A),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF93000A)
        )

        "neon" -> darkColorScheme(
            primary = Color(0xFF00E5FF),          // Vibrant Luminous Electric Cyan
            onPrimary = Color(0xFF001B20),
            primaryContainer = Color(0xFF003844),
            onPrimaryContainer = Color(0xFFB8F5FF),
            secondary = Color(0xFF00E676),        // Cyber Emerald Mint
            onSecondary = Color(0xFF00220E),
            secondaryContainer = Color(0xFF003D1A),
            onSecondaryContainer = Color(0xFF82FFBC),
            tertiary = Color(0xFFFF2A85),         // Cyber Neon Coral / Pink
            onTertiary = Color(0xFF3B0018),
            tertiaryContainer = Color(0xFF5C0028),
            onTertiaryContainer = Color(0xFFFFB2D1),
            background = Color(0xFF08090C),       // Deep OLED Obsidian
            onBackground = Color(0xFFF0F6FC),
            surface = Color(0xFF10141B),          // Elevated Cyber Slate
            onSurface = Color(0xFFF0F6FC),
            surfaceVariant = Color(0xFF181F29),   // Smooth Cyber Container / Chip
            surfaceContainerLowest = Color(0xFF040507),
            surfaceContainerLow = Color(0xFF0C0F14),
            surfaceContainer = Color(0xFF10141B),
            surfaceContainerHigh = Color(0xFF161C24),
            surfaceContainerHighest = Color(0xFF1D242F),
            onSurfaceVariant = Color(0xFF8BA2B8), // Readable Neutral Cyber Gray
            outline = Color(0xFF263242),
            outlineVariant = Color(0xFF181F29)
        )

        "solarized_dark" -> darkColorScheme(
            primary = Color(0xFF2AA198),          // Teal
            onPrimary = Color(0xFF002B36),
            primaryContainer = Color(0xFF073642),
            onPrimaryContainer = Color(0xFF93A1A1),
            secondary = Color(0xFF268BD2),        // Blue
            secondaryContainer = Color(0xFF002B36),
            onSecondaryContainer = Color(0xFF2AA198),
            tertiary = Color(0xFFB58900),         // Yellow/Gold
            tertiaryContainer = Color(0xFF073642),
            onTertiaryContainer = Color(0xFFFDF6E3),
            background = Color(0xFF002B36),
            onBackground = Color(0xFFEEE8D5),
            surface = Color(0xFF073642),
            onSurface = Color(0xFFEEE8D5),        // High Contrast Solarized Base3
            surfaceVariant = Color(0xFF0A3F4E),
            surfaceContainerLowest = Color(0xFF00212B),
            surfaceContainerLow = Color(0xFF002B36),
            surfaceContainer = Color(0xFF073642),
            surfaceContainerHigh = Color(0xFF0B414F),
            surfaceContainerHighest = Color(0xFF0F4D5D),
            onSurfaceVariant = Color(0xFF93A1A1), // Solarized Base1
            outline = Color(0xFF586E75),
            outlineVariant = Color(0xFF0A3F4E)
        )

        "tomorrow_night_blue" -> darkColorScheme(
            primary = Color(0xFFBBDAFF),
            onPrimary = Color(0xFF002451),
            primaryContainer = Color(0xFF002047),
            onPrimaryContainer = Color(0xFFEEFFFF),
            secondary = Color(0xFFEBBBFF),
            secondaryContainer = Color(0xFF002451),
            onSecondaryContainer = Color(0xFFEBBBFF),
            tertiary = Color(0xFFFFC58F),
            tertiaryContainer = Color(0xFF00346B),
            onTertiaryContainer = Color(0xFFFFE5CC),
            background = Color(0xFF002451),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF002F6C),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF003C7A),
            surfaceContainerLowest = Color(0xFF001B3E),
            surfaceContainerLow = Color(0xFF002451),
            surfaceContainer = Color(0xFF002F6C),
            surfaceContainerHigh = Color(0xFF00387B),
            surfaceContainerHighest = Color(0xFF00438E),
            onSurfaceVariant = Color(0xFFB2CCD6),
            outline = Color(0xFF395F91),
            outlineVariant = Color(0xFF003C7A)
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
            onTertiaryContainer = Color(0xFFB8F5FF),
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF0A0A0A),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF1D1D1D),
            surfaceContainerLowest = Color(0xFF000000),
            surfaceContainerLow = Color(0xFF050505),
            surfaceContainer = Color(0xFF0A0A0A),
            surfaceContainerHigh = Color(0xFF141414),
            surfaceContainerHighest = Color(0xFF1D1D1D),
            onSurfaceVariant = Color(0xFFE0E0E0),
            outline = Color(0xFFFFFFFF),
            outlineVariant = Color(0xFF666666)
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
            onTertiaryContainer = Color(0xFF2C1500),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFEFEF),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F7F7),
            surfaceContainer = Color(0xFFEFEFEF),
            surfaceContainerHigh = Color(0xFFE5E5E5),
            surfaceContainerHighest = Color(0xFFDADADA),
            onSurface = Color(0xFF000000),
            onSurfaceVariant = Color(0xFF202020),
            outline = Color(0xFF000000),
            outlineVariant = Color(0xFF333333)
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
            onTertiaryContainer = Color(0xFF111111),
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEDEDED),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F7F7),
            surfaceContainer = Color(0xFFF0F0F0),
            surfaceContainerHigh = Color(0xFFE8E8E8),
            surfaceContainerHighest = Color(0xFFDFDFDF),
            onSurface = Color(0xFF101010),
            onSurfaceVariant = Color(0xFF3F3F3F),
            outline = Color(0xFFB0B0B0),
            outlineVariant = Color(0xFFEDEDED)
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
            onTertiaryContainer = Color(0xFFE0E0E0),
            background = Color(0xFF050505),
            onBackground = Color(0xFFF5F5F5),
            surface = Color(0xFF111111),
            onSurface = Color(0xFFF5F5F5),
            surfaceVariant = Color(0xFF242424),
            surfaceContainerLowest = Color(0xFF000000),
            surfaceContainerLow = Color(0xFF0B0B0B),
            surfaceContainer = Color(0xFF111111),
            surfaceContainerHigh = Color(0xFF1A1A1A),
            surfaceContainerHighest = Color(0xFF242424),
            onSurfaceVariant = Color(0xFFC9C9C9),
            outline = Color(0xFF404040),
            outlineVariant = Color(0xFF242424)
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
            onTertiaryContainer = Color(0xFFE0ECFF),
            background = Color(0xFF001B3A),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF002857),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF003B7A),
            surfaceContainerLowest = Color(0xFF00142C),
            surfaceContainerLow = Color(0xFF001E40),
            surfaceContainer = Color(0xFF002857),
            surfaceContainerHigh = Color(0xFF00336E),
            surfaceContainerHighest = Color(0xFF003F88),
            onSurfaceVariant = Color(0xFFD9E9FF),
            outline = Color(0xFF4B7BB0),
            outlineVariant = Color(0xFF003B7A)
        )

        "one_dark_pro" -> darkColorScheme(
            primary = Color(0xFF61AFEF),
            onPrimary = Color(0xFF21252B),
            primaryContainer = Color(0xFF1D222A),
            onPrimaryContainer = Color(0xFF61AFEF),
            secondary = Color(0xFF98C379),
            secondaryContainer = Color(0xFF21252B),
            onSecondaryContainer = Color(0xFF98C379),
            tertiary = Color(0xFFC678DD),
            tertiaryContainer = Color(0xFF282C34),
            onTertiaryContainer = Color(0xFFECCBFF),
            background = Color(0xFF21252B),
            onBackground = Color(0xFFE5E9F0),
            surface = Color(0xFF282C34),
            onSurface = Color(0xFFE5E9F0),        // Lighter high contrast text
            surfaceVariant = Color(0xFF353B45),
            surfaceContainerLowest = Color(0xFF1A1D22),
            surfaceContainerLow = Color(0xFF21252B),
            surfaceContainer = Color(0xFF282C34),
            surfaceContainerHigh = Color(0xFF2F343E),
            surfaceContainerHighest = Color(0xFF353B45),
            onSurfaceVariant = Color(0xFFABB2BF), // Secondary text
            outline = Color(0xFF4B5263),
            outlineVariant = Color(0xFF353B45)
        )

        "github_dark" -> darkColorScheme(
            primary = Color(0xFF58A6FF),
            onPrimary = Color(0xFF0D1117),
            primaryContainer = Color(0xFF124391),
            onPrimaryContainer = Color(0xFFF0F6FC),
            secondary = Color(0xFF3FB950),
            secondaryContainer = Color(0xFF0D1117),
            onSecondaryContainer = Color(0xFF3FB950),
            tertiary = Color(0xFFFFA657),
            tertiaryContainer = Color(0xFF161B22),
            onTertiaryContainer = Color(0xFFFFD1A9),
            background = Color(0xFF0D1117),
            onBackground = Color(0xFFF0F6FC),
            surface = Color(0xFF161B22),
            onSurface = Color(0xFFF0F6FC),        // GitHub fg.default
            surfaceVariant = Color(0xFF21262D),
            surfaceContainerLowest = Color(0xFF090D12),
            surfaceContainerLow = Color(0xFF11161D),
            surfaceContainer = Color(0xFF161B22),
            surfaceContainerHigh = Color(0xFF1C2129),
            surfaceContainerHighest = Color(0xFF21262D),
            onSurfaceVariant = Color(0xFFC9D1D9), // GitHub fg.muted
            outline = Color(0xFF30363D),
            outlineVariant = Color(0xFF21262D)
        )

        "github_light" -> lightColorScheme(
            primary = Color(0xFF0969DA),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDDF4FF),
            onPrimaryContainer = Color(0xFF0969DA),
            secondary = Color(0xFF1A7F37),
            secondaryContainer = Color(0xFFFFFFFF),
            onSecondaryContainer = Color(0xFF1A7F37),
            tertiary = Color(0xFF9A6700),
            tertiaryContainer = Color(0xFFF6F8FA),
            onTertiaryContainer = Color(0xFF5C3D00),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF6F8FA),
            surfaceVariant = Color(0xFFEAEFF4),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF6F8FA),
            surfaceContainer = Color(0xFFEFF2F5),
            surfaceContainerHigh = Color(0xFFE6EAEF),
            surfaceContainerHighest = Color(0xFFDDE2E8),
            onSurface = Color(0xFF24292F),
            onSurfaceVariant = Color(0xFF57606A),
            outline = Color(0xFFD0D7DE),
            outlineVariant = Color(0xFFEAEFF4)
        )

        "dracula" -> darkColorScheme(
            primary = Color(0xFFBD93F9),          // Purple
            onPrimary = Color(0xFF282A36),
            primaryContainer = Color(0xFF2F3142),
            onPrimaryContainer = Color(0xFFF8F8F2),
            secondary = Color(0xFF50FA7B),        // Green
            secondaryContainer = Color(0xFF282A36),
            onSecondaryContainer = Color(0xFF50FA7B),
            tertiary = Color(0xFFFF79C6),         // Pink
            tertiaryContainer = Color(0xFF44475A),
            onTertiaryContainer = Color(0xFFFFB2D1),
            background = Color(0xFF282A36),
            onBackground = Color(0xFFF8F8F2),
            surface = Color(0xFF1E1F29),
            onSurface = Color(0xFFF8F8F2),
            surfaceVariant = Color(0xFF44475A),
            surfaceContainerLowest = Color(0xFF191A23),
            surfaceContainerLow = Color(0xFF21222C),
            surfaceContainer = Color(0xFF282A36),
            surfaceContainerHigh = Color(0xFF343746),
            surfaceContainerHighest = Color(0xFF44475A),
            onSurfaceVariant = Color(0xFF8E9AC9),
            outline = Color(0xFF6272A4),
            outlineVariant = Color(0xFF343746)
        )

        "material_you" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val isSystemDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
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

        "midnight_dark" -> darkColorScheme(
            primary = Color(0xFFA79BFF),
            onPrimary = Color(0xFF221656),
            primaryContainer = Color(0xFF261D50),
            onPrimaryContainer = Color(0xFFE6DFFF),
            secondary = Color(0xFFC9D6DF),
            secondaryContainer = Color(0xFF29333B),
            onSecondaryContainer = Color(0xFFE6EEF3),
            tertiary = Color(0xFF9BD8E0),
            tertiaryContainer = Color(0xFF164B54),
            onTertiaryContainer = Color(0xFFB8F5FF),
            background = Color(0xFF0F172A),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF1E293B),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF334155),
            surfaceContainerLowest = Color(0xFF0A0F1D),
            surfaceContainerLow = Color(0xFF141E33),
            surfaceContainer = Color(0xFF1E293B),
            surfaceContainerHigh = Color(0xFF28354A),
            surfaceContainerHighest = Color(0xFF334155),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF475569),
            outlineVariant = Color(0xFF1E293B)
        )

        else -> darkColorScheme(
            primary = Color(0xFFE5A93C),          // Luminous Amber Gold (Dark counterpart of #C07318)
            onPrimary = Color(0xFF2C1E00),
            primaryContainer = Color(0xFF3D2A10),
            onPrimaryContainer = Color(0xFFFDE8CC),
            secondary = Color(0xFFD4A373),        // Warm Terracotta Sand (Dark counterpart of #8C5E3C)
            onSecondary = Color(0xFF3C2000),
            secondaryContainer = Color(0xFF3B2A1E),
            onSecondaryContainer = Color(0xFFF5ECE4),
            tertiary = Color(0xFF81B29A),         // Soft Sage (Dark counterpart of #52695C)
            onTertiary = Color(0xFF003828),
            tertiaryContainer = Color(0xFF1B2B24),
            onTertiaryContainer = Color(0xFFDCEDE4),
            background = Color(0xFF151515),       // User Requested #151515 Dark Mode Background
            onBackground = Color(0xFFF5F2EB),
            surface = Color(0xFF1E1E1E),          // Elevated Warm Dark Card matching #151515
            onSurface = Color(0xFFF5F2EB),        // Warm Cream Off-White
            surfaceVariant = Color(0xFF282828),   // Dark Chip container
            surfaceContainerLowest = Color(0xFF111111),
            surfaceContainerLow = Color(0xFF181818),
            surfaceContainer = Color(0xFF1E1E1E),
            surfaceContainerHigh = Color(0xFF242424),
            surfaceContainerHighest = Color(0xFF2C2C2C),
            onSurfaceVariant = Color(0xFFA8A29E), // Muted Neutral
            outline = Color(0xFF424242),
            outlineVariant = Color(0xFF2C2C2C)
        )
    }
}

@Composable
fun VeritasImportPreviewDialog(
    pendingImport: VeritasPendingImport,
    onConfirm: (String, PdfImportOptions, TextImportOptions, PptxImportOptions) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(cleanDocumentTitle(pendingImport.name)) }
    var pdfOptions by remember { mutableStateOf(pendingImport.pdfOptions) }
    var textOptions by remember { mutableStateOf(pendingImport.textOptions) }
    var pptxOptions by remember { mutableStateOf(pendingImport.pptxOptions) }

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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

                if (pendingImport.isPptx) {
                    item {
                        Text("Slides options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Speaker notes", fontWeight = FontWeight.SemiBold)
                                Text("Read presenter notes after each slide.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            VeritasSwitch(
                                checked = pptxOptions.includeSpeakerNotes,
                                onCheckedChange = { pptxOptions = pptxOptions.copy(includeSpeakerNotes = it) }
                            )
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-punctuate", fontWeight = FontWeight.SemiBold)
                                Text("Add end punctuation to unpunctuated lines for smoother listening.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            VeritasSwitch(
                                checked = pptxOptions.autoPunctuate,
                                onCheckedChange = { pptxOptions = pptxOptions.copy(autoPunctuate = it) }
                            )
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Read text in slide images", fontWeight = FontWeight.SemiBold)
                                Text("OCR screenshots and figures. Large decks import slower.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            VeritasSwitch(
                                checked = pptxOptions.ocrSlideImages,
                                onCheckedChange = { pptxOptions = pptxOptions.copy(ocrSlideImages = it) }
                            )
                        }
                    }
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
                    onConfirm(title.ifBlank { pendingImport.name }, pdfOptions, textOptions, pptxOptions)
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
