package com.veritas.reader

import android.annotation.SuppressLint
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.automirrored.outlined.Article
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
import com.veritas.reader.ui.screens.GeminiApiKeyDialog
import com.veritas.reader.ui.screens.FlashcardViewerDialog
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
    onCancel: (() -> Unit)? = null,
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
            if (inProgress && onCancel != null) {
                TextButton(onClick = onCancel) { Text("Cancel") }
            } else {
                TextButton(onClick = onDismiss, enabled = !inProgress) { Text("Close") }
            }
        }
    )
}

@Composable
internal fun AiAppStudyDialog(
    document: ReaderDocument,
    currentIndex: Int,
    templates: List<AiPromptTemplate> = emptyList(),
    history: List<AiPromptHistoryEntry> = emptyList(),
    askAiSettings: AskAiSettings? = null,
    onUpdateAskAiSettings: ((AskAiSettings) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSendToAiApp: (AiPromptType, String, AiPromptScope, IntRange?) -> Unit,
    onSaveTemplate: (String, String) -> Unit = { _, _ -> },
    onDeleteTemplate: (String) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onCopyText: (String, String) -> Unit,
    onSaveAiResultAsNote: (String) -> Unit,
    onImportFlashcards: (String, List<Flashcard>) -> Unit,
    onGenerateInAppFlashcards: ((String, Int, (Boolean, String) -> Unit) -> Unit)? = null,
    onGenerateInAppQuiz: ((String, Int, (Boolean, String, QuizSet?) -> Unit) -> Unit)? = null,
    onGenerateInAppSummary: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onGenerateInAppExplanation: ((String, String, (Boolean, String) -> Unit) -> Unit)? = null,
    onGenerateInAppStudyGuide: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onSaveQuiz: ((QuizSet) -> Unit)? = null,
    onRecordQuizScore: ((String, Int) -> Unit)? = null,
    onRateFlashcard: ((String, String) -> Unit)? = null,
    onOpenStudyHub: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val safeIndex = if (document.chunks.isEmpty()) 0 else currentIndex.coerceIn(0, document.chunks.lastIndex)
    val currentPage = remember(document, currentIndex) {
        val model = ReaderTextModelCache.get(document.id, document.rawText, document.pageCount)
        val sentence = model.sentences.getOrNull(currentIndex)
        sentence?.pageNumber?.coerceAtLeast(1) ?: 1
    }

    var selectedScope by remember { mutableStateOf(AiPromptScope.CURRENT_PAGE) }
    var customStartPage by remember { mutableStateOf(currentPage.toString()) }
    var customEndPage by remember { mutableStateOf(minOf(currentPage + 2, document.pageCount.coerceAtLeast(1)).toString()) }

    var showGeminiSetup by remember { mutableStateOf(false) }
    var showAssistantChooser by remember { mutableStateOf(false) }
    var showPasteFlashcards by remember { mutableStateOf(false) }
    var showPasteQuiz by remember { mutableStateOf(false) }
    var showManualTools by remember { mutableStateOf(false) }
    var manualInputDraft by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatingStatus by remember { mutableStateOf("Generating with AI...") }

    var activeResultPreview by remember { mutableStateOf<ModernStudyResultPreview?>(null) }
    var activeQuizQuestions by remember { mutableStateOf<List<QuizQuestion>?>(null) }
    var activeQuizId by remember { mutableStateOf<String?>(null) }
    var activeStudyDeckCards by remember { mutableStateOf<Pair<String, List<Flashcard>>?>(null) }

    // Clipboard auto-detection
    var detectedClipboardText by remember { mutableStateOf<String?>(null) }
    var detectedCards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var detectedQuiz by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var bannerDismissed by remember { mutableStateOf(false) }

    fun checkClipboard() {
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString().orEmpty().trim()
            if (text.length > 20 && text != detectedClipboardText) {
                val quiz = AiResultParser.parseQuiz(text)
                val isLikelyQuiz = quiz.isNotEmpty() || AiResultParser.isLikelyQuiz(text)
                val cards = if (isLikelyQuiz) emptyList() else AiResultParser.parseFlashcards(text)
                if (cards.isNotEmpty() || quiz.isNotEmpty() || (text.length > 50 && !text.startsWith("You are an expert"))) {
                    detectedClipboardText = text
                    detectedCards = cards
                    detectedQuiz = quiz
                    bannerDismissed = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        checkClipboard()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                checkClipboard()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val hasGeminiKey = remember(context, showGeminiSetup) {
        GeminiStudyService.hasApiKey(context)
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

    val currentRange = remember(selectedScope, customStartPage, customEndPage, document.pageCount) {
        if (selectedScope == AiPromptScope.CUSTOM_PAGE_RANGE) {
            val start = customStartPage.toIntOrNull() ?: 1
            val end = customEndPage.toIntOrNull() ?: document.pageCount
            minOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))..maxOf(start, end).coerceIn(1, document.pageCount.coerceAtLeast(1))
        } else null
    }

    val scopeSentences = remember(document, currentIndex, selectedScope, currentRange) {
        AiPromptLauncher.getSelectedSentences(document, currentIndex, selectedScope, currentRange)
    }

    val scopeText = remember(scopeSentences) {
        AiPromptLauncher.extractTextForScope(document, currentIndex, selectedScope, currentRange)
    }

    val scopeWordCount = remember(scopeSentences) {
        scopeSentences.sumOf { it.text.split(Regex("\\s+")).size }
    }

    if (showGeminiSetup) {
        GeminiApiKeyDialog(onDismiss = { showGeminiSetup = false })
    }

    if (showPasteFlashcards) {
        PasteFlashcardsDialog(
            onImport = { name, cards ->
                onImportFlashcards(name, cards)
                showPasteFlashcards = false
                activeResultPreview = ModernStudyResultPreview.Flashcards(name, cards)
            },
            onDismiss = { showPasteFlashcards = false }
        )
    }

    if (showPasteQuiz) {
        PasteQuizDialog(onDismiss = { showPasteQuiz = false })
    }

    activeQuizQuestions?.let { questions ->
        QuizPlayerDialog(
            questions = questions,
            quizTitle = "${document.title} Quiz",
            onSaveScore = { score ->
                activeQuizId?.let { id -> onRecordQuizScore?.invoke(id, score) }
            },
            onDismiss = { activeQuizQuestions = null }
        )
    }

    activeStudyDeckCards?.let { (deckTitle, cards) ->
        FlashcardViewerDialog(
            setName = deckTitle,
            cards = cards.mapIndexed { idx, card ->
                FlashcardProgress(
                    id = "${document.id.orEmpty()}_card_$idx",
                    documentId = document.id.orEmpty(),
                    front = card.front,
                    back = card.back,
                    setName = deckTitle
                )
            },
            onRate = { cardId, rating ->
                onRateFlashcard?.invoke(cardId, rating)
            },
            onDeleteCard = {},
            onDismiss = { activeStudyDeckCards = null }
        )
    }

    fun handleExternalHandoff(type: AiPromptType) {
        val prompt = AiPromptLauncher.buildPrompt(
            title = document.title,
            chunks = document.chunks,
            currentIndex = PlaybackStateStore.currentIndex,
            type = type,
            scope = selectedScope
        )
        onSendToAiApp(type, "", selectedScope, currentRange)
        Toast.makeText(context, "Prompt ready in $defaultAiName. Return here when done!", Toast.LENGTH_LONG).show()
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
            ) {
                // Top Header Bar
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 1.dp,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column {
                                Text(
                                    "AI Study Studio",
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (onOpenStudyHub != null) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.clickable {
                                        onDismiss()
                                        onOpenStudyHub()
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            "Study Hub",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    checkClipboard()
                                    Toast.makeText(context, "Checked clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh Clipboard",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Engine Status & Selector Pill
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (hasGeminiKey) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (hasGeminiKey) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.clickable {
                                    if (!hasGeminiKey) showGeminiSetup = true else showAssistantChooser = true
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val activeProvider = remember(hasGeminiKey, showGeminiSetup) { GeminiStudyService.getProvider(context) }
                                    Icon(
                                        imageVector = if (hasGeminiKey) Icons.Outlined.AutoAwesome else aiAssistantIcon(defaultAiId),
                                        contentDescription = null,
                                        tint = if (hasGeminiKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        if (hasGeminiKey) "${activeProvider.label} In-App" else defaultAiName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasGeminiKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Scope Selector Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "STUDY FOCUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${scopeSentences.size} sentences • ~$scopeWordCount words",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val scopes = listOf(
                                    AiPromptScope.CURRENT_PAGE to "Page $currentPage",
                                    AiPromptScope.CURRENT_SENTENCE to "Sentence ${(safeIndex + 1)}",
                                    AiPromptScope.CURRENT_SECTION to "Section",
                                    AiPromptScope.WHOLE_DOCUMENT to "Whole Book",
                                    AiPromptScope.CUSTOM_PAGE_RANGE to "Pages..."
                                )
                                scopes.forEach { (scope, label) ->
                                    val isSelected = selectedScope == scope
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedScope = scope },
                                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }

                            if (selectedScope == AiPromptScope.CUSTOM_PAGE_RANGE) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = customStartPage,
                                        onValueChange = { customStartPage = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("From Page", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = customEndPage,
                                        onValueChange = { customEndPage = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("To Page", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick External AI App Hand-off Chips
                    QuickExternalAiChips(
                        context = context,
                        document = document,
                        scopeText = scopeText,
                        safeIndex = safeIndex,
                        selectedScope = selectedScope
                    )

                    // Clipboard Auto-Detection Banner
                    AiStudyClipboardBanner(
                        detectedClipboardText = detectedClipboardText,
                        bannerDismissed = bannerDismissed,
                        detectedCards = detectedCards,
                        detectedQuiz = detectedQuiz,
                        document = document,
                        currentPage = currentPage,
                        onImportFlashcards = onImportFlashcards,
                        onSaveQuiz = onSaveQuiz,
                        onSetResultPreview = { activeResultPreview = it },
                        onDismissBanner = { bannerDismissed = true }
                    )
                    // In-Progress Loading Card
                    if (isGenerating) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        generatingStatus,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Analyzing with Google Gemini...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { isGenerating = false }) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }

                    // Interactive Results Preview (If Any)
                    activeResultPreview?.let { preview ->
                        ModernStudyResultPreviewCard(
                            preview = preview,
                            onClose = { activeResultPreview = null },
                            onStudyCards = { setName, cards ->
                                activeStudyDeckCards = setName to cards
                            },
                            onStartQuiz = { quiz ->
                                onSaveQuiz?.invoke(quiz)
                                activeQuizId = quiz.id
                                activeQuizQuestions = quiz.questions
                            },
                            onSaveNote = { text ->
                                onSaveAiResultAsNote(text)
                                Toast.makeText(context, "Saved note to sentence ${(safeIndex + 1)}!", Toast.LENGTH_SHORT).show()
                            },
                            onCopy = { label, text -> onCopyText(label, text) }
                        )
                    }


                    // 5 Core Modern Study Tools
                    AiStudyCoreToolsCards(
                        document = document,
                        currentPage = currentPage,
                        safeIndex = safeIndex,
                        scopeText = scopeText,
                        selectedScope = selectedScope,
                        hasGeminiKey = hasGeminiKey,
                        defaultAiName = defaultAiName,
                        onImportFlashcards = onImportFlashcards,
                        onSaveQuiz = onSaveQuiz,
                        onCopyText = onCopyText,
                        onSaveAiResultAsNote = onSaveAiResultAsNote,
                        onGenerateInAppFlashcards = onGenerateInAppFlashcards,
                        onGenerateInAppQuiz = onGenerateInAppQuiz,
                        onGenerateInAppSummary = onGenerateInAppSummary,
                        onGenerateInAppExplanation = onGenerateInAppExplanation,
                        onGenerateInAppStudyGuide = onGenerateInAppStudyGuide,
                        onSetGenerating = { generating, status ->
                            isGenerating = generating
                            if (status.isNotBlank()) generatingStatus = status
                        },
                        onSetResultPreview = { activeResultPreview = it },
                        onExternalHandoff = { handleExternalHandoff(it) }
                    )

                    // Manual Importer & Advanced Accordion
                    AiStudyManualAccordion(
                        document = document,
                        onImportFlashcards = onImportFlashcards,
                        onSaveQuiz = onSaveQuiz,
                        onSaveAiResultAsNote = onSaveAiResultAsNote,
                        onSetResultPreview = { activeResultPreview = it },
                        onShowPasteFlashcards = { showPasteFlashcards = true },
                        onShowPasteQuiz = { showPasteQuiz = true },
                        onShowGeminiSetup = { showGeminiSetup = true }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickExternalAiChips(
    context: Context,
    document: ReaderDocument,
    scopeText: String,
    safeIndex: Int,
    selectedScope: AiPromptScope
) {
    val aiHandoffs = remember {
        listOf(
            Triple("Gemini", "com.google.android.apps.bard", "https://gemini.google.com"),
            Triple("ChatGPT", "com.openai.chatgpt", "https://chatgpt.com"),
            Triple("Claude", "com.anthropic.claude", "https://claude.ai"),
            Triple("Grok", "ai.groq", "https://x.ai"),
            Triple("Perplexity", "ai.perplexity.app.android", "https://www.perplexity.ai"),
            Triple("Copilot", "com.microsoft.copilot", "https://copilot.microsoft.com")
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Quick External AI Hand-off",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Auto-copies prompt",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aiHandoffs.forEach { (name, pkg, webUrl) ->
                    AssistChip(
                        onClick = {
                            val prompt = AiPromptLauncher.buildPrompt(
                                title = document.title,
                                chunks = document.chunks,
                                currentIndex = safeIndex,
                                type = AiPromptType.STUDY_NOTES,
                                scope = selectedScope
                            ) + "\n\n" + scopeText
                            launchExternalAiHandoff(context, name, pkg, webUrl, prompt)
                        },
                        label = { Text(name, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                imageVector = aiAssistantIcon(name),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Unspecified
                            )
                        },
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }
    }
}

private fun launchExternalAiHandoff(
    context: Context,
    appName: String,
    packageName: String,
    webUrl: String,
    prompt: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Veritas AI Prompt", prompt))

    var launched = false
    if (packageName.isNotBlank()) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
                Toast.makeText(context, "Prompt copied! Opening $appName...", Toast.LENGTH_SHORT).show()
                launched = true
            } catch (_: Exception) {}
        }
    }

    if (!launched) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            Toast.makeText(context, "Prompt copied! Opening $appName in browser...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }
}


