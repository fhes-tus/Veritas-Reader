package com.veritas.reader


import android.annotation.SuppressLint
import android.content.Context
import android.content.ClipboardManager
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
internal fun AiStudyClipboardBanner(
    detectedClipboardText: String?,
    bannerDismissed: Boolean,
    detectedCards: List<Flashcard>,
    detectedQuiz: List<QuizQuestion>,
    document: ReaderDocument,
    currentPage: Int,
    onImportFlashcards: (String, List<Flashcard>) -> Unit,
    onSaveQuiz: ((QuizSet) -> Unit)?,
    onSetResultPreview: (ModernStudyResultPreview) -> Unit,
    onDismissBanner: () -> Unit
) {
    val context = LocalContext.current
                    // Clipboard Auto-Detection Banner
                    if (detectedClipboardText != null && !bannerDismissed) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Study Content Found on Clipboard!",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    when {
                                        detectedCards.isNotEmpty() -> "✨ Detected ${detectedCards.size} Q&A flashcards ready to import"
                                        detectedQuiz.isNotEmpty() -> "🎯 Detected ${detectedQuiz.size} practice exam questions ready to play"
                                        else -> "📝 Detected study notes / explanation on clipboard"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            when {
                                                detectedCards.isNotEmpty() -> {
                                                    val setName = "${document.title} - Page $currentPage"
                                                    onImportFlashcards(setName, detectedCards)
                                                    onSetResultPreview(ModernStudyResultPreview.Flashcards(setName, detectedCards))
                                                    onDismissBanner()
                                                    Toast.makeText(context, "Imported ${detectedCards.size} flashcards!", Toast.LENGTH_SHORT).show()
                                                }
                                                detectedQuiz.isNotEmpty() -> {
                                                    val newQuiz = QuizSet(
                                                        title = "${document.title} - Page $currentPage Quiz",
                                                        documentId = document.id.orEmpty(),
                                                        questions = detectedQuiz
                                                    )
                                                    onSaveQuiz?.invoke(newQuiz)
                                                    onSetResultPreview(ModernStudyResultPreview.Quiz(newQuiz))
                                                    onDismissBanner()
                                                }
                                                else -> {
                                                    onSetResultPreview(
                                                        ModernStudyResultPreview.NoteText(
                                                            AiPromptType.STUDY_NOTES,
                                                            "Imported Study Notes",
                                                            detectedClipboardText.orEmpty()
                                                        )
                                                    )
                                                    onDismissBanner()
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Review & Import", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onDismissBanner() },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Dismiss")
                                    }
                                }
                            }
                        }
                    }


}


@Composable
internal fun AiStudyCoreToolsCards(
    document: ReaderDocument,
    currentPage: Int,
    safeIndex: Int,
    scopeText: String,
    selectedScope: AiPromptScope,
    hasGeminiKey: Boolean,
    defaultAiName: String,
    onImportFlashcards: (String, List<Flashcard>) -> Unit,
    onSaveQuiz: ((QuizSet) -> Unit)?,
    onCopyText: (String, String) -> Unit,
    onSaveAiResultAsNote: (String) -> Unit,
    onGenerateInAppFlashcards: ((String, Int, (Boolean, String) -> Unit) -> Unit)?,
    onGenerateInAppQuiz: ((String, Int, (Boolean, String, QuizSet?) -> Unit) -> Unit)?,
    onGenerateInAppSummary: ((String, (Boolean, String) -> Unit) -> Unit)?,
    onGenerateInAppExplanation: ((String, String, (Boolean, String) -> Unit) -> Unit)?,
    onGenerateInAppStudyGuide: ((String, (Boolean, String) -> Unit) -> Unit)?,
    onSetGenerating: (Boolean, String) -> Unit,
    onSetResultPreview: (ModernStudyResultPreview) -> Unit,
    onExternalHandoff: (AiPromptType) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

                    // 5 Core Modern Study Tools
                    ModernToolActionCard(
                        title = "AI Flashcard Deck",
                        badge = "SM-2 Active Recall",
                        description = "Extracts definitions, formulas and key facts into interactive spaced repetition cards.",
                        icon = Icons.Outlined.Style,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        primaryActionLabel = if (hasGeminiKey) "Generate Flashcards" else "Open in $defaultAiName",
                        isPrimaryInApp = hasGeminiKey,
                        onPrimaryAction = {
                            if (hasGeminiKey) {
                                onSetGenerating(true, "Synthesizing flashcards with Gemini...")
                                if (onGenerateInAppFlashcards != null) {
                                    onGenerateInAppFlashcards(scopeText, 8) { success, msg ->
                                        onSetGenerating(false, "")
                                        if (success) {
                                            val cards = AiResultParser.parseFlashcards(msg)
                                            val setName = "${document.title} - Page $currentPage"
                                            onSetResultPreview(ModernStudyResultPreview.Flashcards(setName, cards))
                                            Toast.makeText(context, "Flashcards ready!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        val res = GeminiStudyService.generateFlashcards(
                                            apiKey = GeminiStudyService.getApiKey(context),
                                            documentTitle = document.title,
                                            textContext = scopeText,
                                            cardCount = 8
                                        )
                                        onSetGenerating(false, "")
                                        res.onSuccess { cards ->
                                            val setName = "${document.title} - Page $currentPage"
                                            onImportFlashcards(setName, cards)
                                            onSetResultPreview(ModernStudyResultPreview.Flashcards(setName, cards))
                                            Toast.makeText(context, "Generated ${cards.size} flashcards!", Toast.LENGTH_SHORT).show()
                                        }.onFailure { err ->
                                            Toast.makeText(context, err.message ?: "Generation failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } else {
                                onExternalHandoff(AiPromptType.FLASHCARDS)
                            }
                        },
                        onCopyPrompt = {
                            val prompt = AiPromptLauncher.buildPrompt(
                                title = document.title,
                                chunks = document.chunks,
                                currentIndex = safeIndex,
                                type = AiPromptType.FLASHCARDS,
                                scope = selectedScope
                            )
                            onCopyText("Vern Flashcard Prompt", prompt)
                        },
                        onExternalLaunch = { onExternalHandoff(AiPromptType.FLASHCARDS) }
                    )

                    ModernToolActionCard(
                        title = "Practice Exam Quiz",
                        badge = "Interactive Studio",
                        description = "Generates multiple-choice exam questions with instant scoring and detailed explanations.",
                        icon = Icons.Outlined.Quiz,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        primaryActionLabel = if (hasGeminiKey) "Create Practice Quiz" else "Open in $defaultAiName",
                        isPrimaryInApp = hasGeminiKey,
                        onPrimaryAction = {
                            if (hasGeminiKey) {
                                onSetGenerating(true, "Formulating exam questions with Gemini...")
                                if (onGenerateInAppQuiz != null) {
                                    onGenerateInAppQuiz(scopeText, 5) { success, msg, quiz ->
                                        onSetGenerating(false, "")
                                        if (success && quiz != null) {
                                            onSaveQuiz?.invoke(quiz)
                                            onSetResultPreview(ModernStudyResultPreview.Quiz(quiz))
                                            Toast.makeText(context, "Quiz ready!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        val res = GeminiStudyService.generateQuiz(
                                            apiKey = GeminiStudyService.getApiKey(context),
                                            documentTitle = document.title,
                                            textContext = scopeText,
                                            questionCount = 5
                                        )
                                        onSetGenerating(false, "")
                                        res.onSuccess { questions ->
                                            val newQuiz = QuizSet(
                                                title = "${document.title} - Page $currentPage Quiz",
                                                documentId = document.id.orEmpty(),
                                                questions = questions
                                            )
                                            onSaveQuiz?.invoke(newQuiz)
                                            onSetResultPreview(ModernStudyResultPreview.Quiz(newQuiz))
                                            Toast.makeText(context, "Created ${questions.size} questions!", Toast.LENGTH_SHORT).show()
                                        }.onFailure { err ->
                                            Toast.makeText(context, err.message ?: "Quiz creation failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } else {
                                onExternalHandoff(AiPromptType.QUIZ)
                            }
                        },
                        onCopyPrompt = {
                            val prompt = AiPromptLauncher.buildPrompt(
                                title = document.title,
                                chunks = document.chunks,
                                currentIndex = safeIndex,
                                type = AiPromptType.QUIZ,
                                scope = selectedScope
                            )
                            onCopyText("Vern Quiz Prompt", prompt)
                        },
                        onExternalLaunch = { onExternalHandoff(AiPromptType.QUIZ) }
                    )

                    ModernToolActionCard(
                        title = "Executive Summary",
                        badge = "BLUF Framework",
                        description = "High-yield summary with core thesis, 4-6 bullet takeaways with citations, and conclusions.",
                        icon = Icons.Outlined.Summarize,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        primaryActionLabel = if (hasGeminiKey) "Synthesize Summary" else "Open in $defaultAiName",
                        isPrimaryInApp = hasGeminiKey,
                        onPrimaryAction = {
                            if (hasGeminiKey) {
                                onSetGenerating(true, "Synthesizing executive summary...")
                                if (onGenerateInAppSummary != null) {
                                    onGenerateInAppSummary(scopeText) { success, result ->
                                        onSetGenerating(false, "")
                                        if (success) {
                                            onSetResultPreview(ModernStudyResultPreview.NoteText(
                                                AiPromptType.SUMMARY,
                                                "Executive Summary (Page $currentPage)",
                                                result
                                            ))
                                        } else {
                                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        val res = GeminiStudyService.generateStudySummary(
                                            apiKey = GeminiStudyService.getApiKey(context),
                                            documentTitle = document.title,
                                            textContext = scopeText
                                        )
                                        onSetGenerating(false, "")
                                        res.onSuccess { summary ->
                                            onSetResultPreview(ModernStudyResultPreview.NoteText(
                                                AiPromptType.SUMMARY,
                                                "Executive Summary (Page $currentPage)",
                                                summary
                                            ))
                                        }.onFailure { err ->
                                            Toast.makeText(context, err.message ?: "Summary failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } else {
                                onExternalHandoff(AiPromptType.SUMMARY)
                            }
                        },
                        onCopyPrompt = {
                            val prompt = AiPromptLauncher.buildPrompt(
                                title = document.title,
                                chunks = document.chunks,
                                currentIndex = safeIndex,
                                type = AiPromptType.SUMMARY,
                                scope = selectedScope
                            )
                            onCopyText("Vern Summary Prompt", prompt)
                        },
                        onExternalLaunch = { onExternalHandoff(AiPromptType.SUMMARY) }
                    )

                    ModernToolActionCard(
                        title = "Explain & Simplify",
                        badge = "Feynman Technique",
                        description = "Deconstructs difficult passages into plain English with relatable analogies and definitions.",
                        icon = Icons.Outlined.School,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        primaryActionLabel = if (hasGeminiKey) "Explain Passage" else "Open in $defaultAiName",
                        isPrimaryInApp = hasGeminiKey,
                        onPrimaryAction = {
                            if (hasGeminiKey) {
                                onSetGenerating(true, "Deconstructing concepts...")
                                val targetPassage = document.chunks.getOrNull(safeIndex).orEmpty()
                                if (onGenerateInAppExplanation != null) {
                                    onGenerateInAppExplanation(scopeText, targetPassage) { success, result ->
                                        onSetGenerating(false, "")
                                        if (success) {
                                            onSetResultPreview(ModernStudyResultPreview.NoteText(
                                                AiPromptType.EXPLAIN_SECTION,
                                                "Feynman Explanation",
                                                result
                                            ))
                                        } else {
                                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        val res = GeminiStudyService.generateExplanation(
                                            apiKey = GeminiStudyService.getApiKey(context),
                                            documentTitle = document.title,
                                            textContext = scopeText,
                                            targetPassage = targetPassage
                                        )
                                        onSetGenerating(false, "")
                                        res.onSuccess { explanation ->
                                            onSetResultPreview(ModernStudyResultPreview.NoteText(
                                                AiPromptType.EXPLAIN_SECTION,
                                                "Feynman Explanation",
                                                explanation
                                            ))
                                        }.onFailure { err ->
                                            Toast.makeText(context, err.message ?: "Explanation failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } else {
                                onExternalHandoff(AiPromptType.EXPLAIN_SECTION)
                            }
                        },
                        onCopyPrompt = {
                            val prompt = AiPromptLauncher.buildPrompt(
                                title = document.title,
                                chunks = document.chunks,
                                currentIndex = safeIndex,
                                type = AiPromptType.EXPLAIN_SECTION,
                                scope = selectedScope
                            )
                            onCopyText("Vern Explainer Prompt", prompt)
                        },
                        onExternalLaunch = { onExternalHandoff(AiPromptType.EXPLAIN_SECTION) }
                    )

                    ModernToolActionCard(
                        title = "Study Guide & Cheatsheet",
                        badge = "Structured Framework",
                        description = "Generates organized headings, definitions glossary, core formulas and self-review checklist.",
                        icon = Icons.Outlined.Description,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        primaryActionLabel = if (hasGeminiKey) "Build Cheatsheet" else "Open in $defaultAiName",
                        isPrimaryInApp = hasGeminiKey,
                        onPrimaryAction = {
                            if (hasGeminiKey) {
                                onSetGenerating(true, "Building study cheatsheet...")
                                if (onGenerateInAppStudyGuide != null) {
                                    onGenerateInAppStudyGuide(scopeText) { success, result ->
                                        onSetGenerating(false, "")
                                        if (success) {
                                            onSetResultPreview(ModernStudyResultPreview.NoteText(
                                                AiPromptType.STUDY_NOTES,
                                                "Study Cheatsheet",
                                                result
                                            ))
                                        } else {
                                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        val res = GeminiStudyService.generateStudyGuide(
                                            apiKey = GeminiStudyService.getApiKey(context),
                                            documentTitle = document.title,
                                            textContext = scopeText
                                        )
                                        onSetGenerating(false, "")
                                        res.onSuccess { guide ->
                                            onSetResultPreview(ModernStudyResultPreview.NoteText(
                                                AiPromptType.STUDY_NOTES,
                                                "Study Cheatsheet",
                                                guide
                                            ))
                                        }.onFailure { err ->
                                            Toast.makeText(context, err.message ?: "Cheatsheet build failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } else {
                                onExternalHandoff(AiPromptType.STUDY_NOTES)
                            }
                        },
                        onCopyPrompt = {
                            val prompt = AiPromptLauncher.buildPrompt(
                                title = document.title,
                                chunks = document.chunks,
                                currentIndex = safeIndex,
                                type = AiPromptType.STUDY_NOTES,
                                scope = selectedScope
                            )
                            onCopyText("Vern Study Guide Prompt", prompt)
                        },
                        onExternalLaunch = { onExternalHandoff(AiPromptType.STUDY_NOTES) }
                    )


}


@Composable
internal fun AiStudyManualAccordion(
    document: ReaderDocument,
    onImportFlashcards: (String, List<Flashcard>) -> Unit,
    onSaveQuiz: ((QuizSet) -> Unit)?,
    onSaveAiResultAsNote: (String) -> Unit,
    onSetResultPreview: (ModernStudyResultPreview) -> Unit,
    onShowPasteFlashcards: () -> Unit,
    onShowPasteQuiz: () -> Unit,
    onShowGeminiSetup: () -> Unit
) {
    val context = LocalContext.current
    var showManualTools by remember { mutableStateOf(false) }
    var manualInputDraft by remember { mutableStateOf("") }

                    // Manual Importer & Advanced Accordion
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showManualTools = !showManualTools },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Manual Importers & AI Settings",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (showManualTools) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (showManualTools) {
                                OutlinedTextField(
                                    value = manualInputDraft,
                                    onValueChange = { manualInputDraft = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    label = { Text("Paste AI response here") },
                                    placeholder = { Text("Paste flashcards, quiz, or study notes...") },
                                    shape = RoundedCornerShape(12.dp)
                                )

                                val parsedCards = remember(manualInputDraft) { AiResultParser.parseFlashcards(manualInputDraft) }
                                val parsedQuiz = remember(manualInputDraft) { AiResultParser.parseQuiz(manualInputDraft) }

                                if (manualInputDraft.isNotBlank()) {
                                    Text(
                                        when {
                                            parsedCards.isNotEmpty() -> "✓ Recognized ${parsedCards.size} Flashcards"
                                            parsedQuiz.isNotEmpty() -> "✓ Recognized ${parsedQuiz.size} Quiz Questions"
                                            else -> "✓ Recognized Study Notes / Summary"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                when {
                                                    parsedCards.isNotEmpty() -> {
                                                        val setName = "${document.title} - Imported"
                                                        onImportFlashcards(setName, parsedCards)
                                                        onSetResultPreview(ModernStudyResultPreview.Flashcards(setName, parsedCards))
                                                        manualInputDraft = ""
                                                    }
                                                    parsedQuiz.isNotEmpty() -> {
                                                        val newQuiz = QuizSet(
                                                            title = "${document.title} - Imported Quiz",
                                                            documentId = document.id.orEmpty(),
                                                            questions = parsedQuiz
                                                        )
                                                        onSaveQuiz?.invoke(newQuiz)
                                                        onSetResultPreview(ModernStudyResultPreview.Quiz(newQuiz))
                                                        manualInputDraft = ""
                                                    }
                                                    else -> {
                                                        onSaveAiResultAsNote(manualInputDraft)
                                                        manualInputDraft = ""
                                                        Toast.makeText(context, "Saved as sentence note!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text("Import Recognized Content")
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { onShowPasteFlashcards() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Card Importer", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onShowPasteQuiz() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Quiz Importer", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onShowGeminiSetup() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Gemini Key", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }


}

