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



@Composable
internal fun MainOnboardingAndTourHost(
    viewModel: ReaderViewModel,
    uiState: ReaderUiState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

        if (uiState.showTutorial && !uiState.hasCompletedOnboarding && OnboardingController.activeStep == null) {
            RevampedOnboardingFlow(
                initialUserName = uiState.userName,
                initialReadingInterest = uiState.readingInterest,
                initialAiAssistant = uiState.askAiSettings.assistantId,
                onComplete = { name, interest, aiAssistant, speed, pitch ->
                    viewModel.createWelcomeDocumentSilently()
                    viewModel.completeRevampedOnboarding(name, interest, aiAssistant, speed, pitch)
                    OnboardingController.activeStep = OnboardingStep.WELCOME
                },
                onDismiss = {
                    viewModel.createWelcomeDocumentSilently()
                    viewModel.completeRevampedOnboarding(
                        name = uiState.userName.ifBlank { "Reader" },
                        interest = uiState.readingInterest,
                        aiAssistant = uiState.askAiSettings.assistantId,
                        speedRate = uiState.voiceSettings.preferredRate,
                        pitchRate = uiState.voiceSettings.preferredPitch
                    )
                    OnboardingController.activeStep = OnboardingStep.WELCOME
                }
            )
        } else if (OnboardingController.activeStep != null) {
            var isTransitioningStep by remember { mutableStateOf(false) }
            val activeStep = OnboardingController.activeStep
            // Voice-assisted tutorial: read each step aloud automatically
            LaunchedEffect(activeStep) {
                if (activeStep != null) {
                    TutorialSpeaker.init(context)
                    TutorialSpeaker.speak("${activeStep.title}. ${activeStep.body}")
                } else {
                    TutorialSpeaker.stop()
                }
                if (activeStep == OnboardingStep.SETTINGS_SPOTLIGHT) {
                    viewModel.updateState { it.copy(showSettingsHub = true) }
                } else if (activeStep != null && uiState.showSettingsHub) {
                    viewModel.updateState { it.copy(showSettingsHub = false) }
                }
            }
            // Shutdown speaker when tour is completed or dismissed
            LaunchedEffect(activeStep == null) {
                if (activeStep == null) {
                    TutorialSpeaker.shutdown()
                }
            }
            // INSIGHTS_PAGE_SPOTLIGHT, SETTINGS_SPOTLIGHT, and CLASSICS_SPOTLIGHT render their tour cards inside their own dialog windows
            if (activeStep != null && activeStep != OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT && activeStep != OnboardingStep.SETTINGS_SPOTLIGHT && activeStep != OnboardingStep.CLASSICS_SPOTLIGHT) {
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
                                        OnboardingStep.WELCOME -> OnboardingStep.FAB_SPOTLIGHT
                                        OnboardingStep.NAME_INPUT -> {
                                            viewModel.saveUserName(uiState.userName)
                                            OnboardingStep.FAB_SPOTLIGHT
                                        }
                                        OnboardingStep.FAB_SPOTLIGHT -> OnboardingStep.CHECKLIST_SPOTLIGHT
                                        OnboardingStep.CHECKLIST_SPOTLIGHT -> {
                                            viewModel.updateState { it.copy(showClassicsCatalog = true) }
                                            OnboardingStep.CLASSICS_SPOTLIGHT
                                        }
                                        OnboardingStep.CLASSICS_SPOTLIGHT -> {
                                            viewModel.updateState { it.copy(showClassicsCatalog = false) }
                                            OnboardingStep.INSIGHTS_SPOTLIGHT
                                        }
                                        OnboardingStep.INSIGHTS_SPOTLIGHT -> OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT
                                        OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT -> OnboardingStep.NOTES_TAB_SPOTLIGHT
                                        OnboardingStep.NOTES_TAB_SPOTLIGHT -> OnboardingStep.STUDY_TAB_SPOTLIGHT
                                        OnboardingStep.STUDY_TAB_SPOTLIGHT -> OnboardingStep.SETTINGS_SPOTLIGHT
                                        OnboardingStep.SETTINGS_SPOTLIGHT -> OnboardingStep.DOCUMENT_SPOTLIGHT
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
                                        OnboardingStep.FAB_SPOTLIGHT -> OnboardingStep.WELCOME
                                        OnboardingStep.CHECKLIST_SPOTLIGHT -> OnboardingStep.FAB_SPOTLIGHT
                                        OnboardingStep.CLASSICS_SPOTLIGHT -> {
                                            viewModel.updateState { it.copy(showClassicsCatalog = false) }
                                            OnboardingStep.CHECKLIST_SPOTLIGHT
                                        }
                                        OnboardingStep.INSIGHTS_SPOTLIGHT -> {
                                            viewModel.updateState { it.copy(showClassicsCatalog = true) }
                                            OnboardingStep.CLASSICS_SPOTLIGHT
                                        }
                                        OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT -> OnboardingStep.INSIGHTS_SPOTLIGHT
                                        OnboardingStep.NOTES_TAB_SPOTLIGHT -> OnboardingStep.INSIGHTS_PAGE_SPOTLIGHT
                                        OnboardingStep.STUDY_TAB_SPOTLIGHT -> OnboardingStep.NOTES_TAB_SPOTLIGHT
                                        OnboardingStep.SETTINGS_SPOTLIGHT -> OnboardingStep.STUDY_TAB_SPOTLIGHT
                                        OnboardingStep.DOCUMENT_SPOTLIGHT -> OnboardingStep.SETTINGS_SPOTLIGHT
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
                            if (uiState.showClassicsCatalog) {
                                viewModel.updateState { it.copy(showClassicsCatalog = false) }
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

