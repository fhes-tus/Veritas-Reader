package com.veritas.reader.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape

import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontFamily
import com.veritas.reader.ui.ReaderUiState
import com.veritas.reader.ui.VeritasUiFont
import com.veritas.reader.ui.VeritasSwitch
import com.veritas.reader.ui.rememberSliderHaptics
import com.veritas.reader.ui.fontFamily
import com.veritas.reader.aiAssistantIcon

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.automirrored.filled.List

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import com.veritas.reader.*
import com.veritas.reader.R

@Composable
fun SettingsHubDialog(
    uiState: ReaderUiState,
    onDismiss: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenVoiceStudio: () -> Unit,
    onOpenNarrationStudio: () -> Unit,
    onOpenPronunciationRules: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenSyncCenter: () -> Unit,
    onOpenAiCenter: () -> Unit,
    onOpenAskAiSettings: () -> Unit,
    onStartRecord: () -> Unit,
    onOpenTextEditor: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenPdfTools: () -> Unit,
    onOpenFileBrowser: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenReadingLists: () -> Unit,
    onOpenUserManual: () -> Unit,
    onOpenStorage: () -> Unit = {},
    onOpenAccessibility: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {}
) {
    val context = LocalContext.current
    val documentCount = uiState.documents.size
    val hasActiveDocument = uiState.activeDocument != null
    val queueCount = uiState.queuedDocuments.size
    uiState.pronunciationRules.size
    uiState.voiceSettings.displayName
    uiState.narrationSettings.enabled
    uiState.readerSettings.autoPlayQueue
    val settingsFeatures = remember(hasActiveDocument, documentCount, queueCount) {
        VeritasFeatureRegistry.resolve(
            VeritasFeatureSurface.SETTINGS_HUB,
            VeritasFeatureContext(
                hasActiveDocument = hasActiveDocument,
                hasSavedDocument = documentCount > 0,
                queueCount = queueCount
            )
        ).associateBy { it.definition.id }
    }

    fun settingsFeature(id: VeritasFeatureId): ResolvedVeritasFeature =
        settingsFeatures.requireResolvedFeature(id)

    var showAboutDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {


                SettingsHubSectionTitle("Voice & Audio")
                SettingsHubGroup(listOf(
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.VOICE_STUDIO),
                        "Voice Studio", "TTS engine, installed voices & pitch presets",
                        Icons.Outlined.RecordVoiceOver, onOpenVoiceStudio
                    ),
                    SettingsRowSpec(
                        "Narration Studio", "Narrator & dialogue delivery controls",
                        Icons.Outlined.TheaterComedy, onOpenNarrationStudio
                    ),
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.PRONUNCIATION_RULES),
                        "Pronunciation", "Word replacements before playback",
                        Icons.Outlined.Translate, onOpenPronunciationRules
                    ),
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.SLEEP_TIMER),
                        "Sleep timer", "Pause or stop reading after chosen time",
                        Icons.Outlined.Timer, onOpenSleepTimer
                    ),
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT),
                        "Record sound file", "Render current reading into audio file",
                        Icons.Outlined.GraphicEq, onStartRecord
                    )
                ))

                SettingsHubSectionTitle("Appearance")
                SettingsHubGroup(listOf(
                    SettingsRowSpec(
                        "Display theme", "Theme packs, colour themes & text size",
                        Icons.Outlined.Palette, onOpenReaderSettings
                    ),
                    SettingsRowSpec(
                        "Accessibility", "Reading goal, reduce motion & contrast",
                        Icons.Outlined.Accessibility, onOpenAccessibility
                    ),
                    SettingsRowSpec(
                        "Background playback",
                        if (isBatteryOptimizationIgnored(LocalContext.current)) "Battery optimization unrestricted (Optimal)" else "Open phone battery optimization settings",
                        Icons.Outlined.PowerSettingsNew,
                        { requestIgnoreBatteryOptimizations(context) }
                    )
                ))

                SettingsHubSectionTitle("AI & Language")
                SettingsHubGroup(listOf(
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.OFFLINE_STUDY_TOOLS),
                        "AI & study mode", "AI app handoff, flashcards & quizzes",
                        Icons.Outlined.AutoAwesome, onOpenAiCenter
                    ),
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.AI_APP_HANDOFF),
                        "Ask AI", "Choose assistant & selected-text prompt",
                        Icons.Outlined.Psychology, onOpenAskAiSettings
                    )
                ))

                SettingsHubSectionTitle("Import Defaults")
                SettingsHubGroup(listOf(
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.PDF_IMPORT_CONTROLS),
                        "PDF and import tools", "Defaults, OCR fallback & page cleanup",
                        Icons.Outlined.PictureAsPdf, onOpenPdfTools
                    ),
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.FILE_BROWSER),
                        "File browser", "Scan approved folders & import local files",
                        Icons.Outlined.Folder, onOpenFileBrowser
                    ),
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.EXTRACTED_TEXT_EDITOR),
                        "Edit extracted text", "Open text editor to correct text",
                        Icons.Outlined.EditNote, onOpenTextEditor
                    )
                ))

                SettingsHubSectionTitle("Data & Backup")
                SettingsHubGroup(listOf(
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.LOCAL_SYNC_PACK),
                        "Sync & Backup Center", "Sync & backup library, flashcards, notes, and progress",
                        Icons.Filled.Sync, onOpenSyncCenter
                    ),
                    featureRowSpec(
                        settingsFeature(VeritasFeatureId.READING_LISTS),
                        "Reading lists", "Create local lists & add saved readings",
                        Icons.AutoMirrored.Filled.List, onOpenReadingLists
                    ),
                    SettingsRowSpec(
                        "Storage", "See what Veritas is using & free up space",
                        Icons.Outlined.Storage, onOpenStorage
                    )
                ))

                SettingsHubSectionTitle("Help")
                SettingsHubGroup(listOf(
                    SettingsRowSpec(
                        "Check for updates",
                        uiState.updateStatusMessage ?: "Check for the latest Veritas Reader version",
                        Icons.Filled.Sync,
                        onCheckForUpdates
                    ),
                    SettingsRowSpec(
                        "User manual", "Interactive guide to Veritas Reader features",
                        Icons.AutoMirrored.Outlined.HelpOutline, onOpenUserManual
                    ),
                    SettingsRowSpec(
                        "Tutorial", "Learn Veritas through guided actions",
                        Icons.Outlined.School, onOpenTutorial
                    ),
                    SettingsRowSpec(
                        "About", "Version, developer contact, social links & feedback",
                        Icons.Outlined.Info, { showAboutDialog = true }
                    )
                ))
                }
            }

            // Onboarding tour card: rendered inside SettingsHubDialog window
            if (com.veritas.reader.ui.OnboardingController.activeStep == com.veritas.reader.ui.OnboardingStep.SETTINGS_SPOTLIGHT) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = com.veritas.reader.ui.OnboardingStep.SETTINGS_SPOTLIGHT.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = com.veritas.reader.ui.OnboardingStep.SETTINGS_SPOTLIGHT.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { com.veritas.reader.ui.OnboardingController.activeStep = com.veritas.reader.ui.OnboardingStep.STUDY_TAB_SPOTLIGHT }) {
                                Text("Back")
                            }
                            Button(onClick = { com.veritas.reader.ui.OnboardingController.activeStep = com.veritas.reader.ui.OnboardingStep.DOCUMENT_SPOTLIGHT }) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(
            uiState = uiState,
            onCheckForUpdates = onCheckForUpdates,
            onDismiss = { showAboutDialog = false }
        )
    }
}

fun formatVeritasBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return when {
        mb >= 1024 -> String.format(Locale.US, "%.2f GB", mb / 1024.0)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
    }
}

@Composable
fun StorageDialog(
    breakdown: StorageBreakdown?,
    candidates: List<Pair<SavedDocument, Long>>,
    cleanupMessage: String?,
    onSmartCleanup: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Storage") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (breakdown == null) {
                    Text("Measuring…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(
                        "Veritas is using ${formatVeritasBytes(breakdown.totalBytes)} across ${breakdown.documentCount} document${if (breakdown.documentCount == 1) "" else "s"}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Original books & files: ${formatVeritasBytes(breakdown.originalsBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Extracted text: ${formatVeritasBytes(breakdown.textBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Covers & thumbnails: ${formatVeritasBytes(breakdown.coversBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Database & storage index: ${formatVeritasBytes(breakdown.databaseBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Temporary cache & audio: ${formatVeritasBytes(breakdown.cacheBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                    val reclaimable = candidates.sumOf { it.second }
                    Button(
                        onClick = onSmartCleanup,
                        enabled = candidates.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (candidates.isEmpty()) "Nothing to clean up"
                            else "Smart Cleanup — free ${formatVeritasBytes(reclaimable)}"
                        )
                    }
                    Text(
                        if (candidates.isEmpty())
                            "Smart Cleanup removes original files of fully-read or 90+ day dormant, non-favorite documents. Text, progress, notes, and highlights always stay."
                        else
                            "Removes the original files of ${candidates.size} fully-read or dormant document${if (candidates.size == 1) "" else "s"} (favorites are never touched). Reading text, progress, notes, highlights and cover art stay — only Original View is lost until a re-import.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!cleanupMessage.isNullOrBlank()) {
                        Text(cleanupMessage, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    )
}

@Composable
fun SettingsHubSectionTitle(title: String) {
    val currentLocale = LocalConfiguration.current.locales[0]
    Text(
        title.uppercase(currentLocale),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        // Structure, not action: section headers stay neutral so the accent colour
        // means something when it does appear.
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
    )
}

/** One row of the settings hub, described so a group can lay several out together. */
data class SettingsRowSpec(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val disabledReason: String? = null
)

fun featureRowSpec(
    feature: ResolvedVeritasFeature,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) = SettingsRowSpec(title, subtitle, icon, onClick, feature.enabled, feature.disabledReason)

/**
 * A section's rows sharing a single card, separated by hairlines.
 *
 * Previously every row was its own bordered card with an 8dp gap, which cost
 * 74dp per item and let only ~9 rows onto a screen. Grouping drops that to 56dp.
 */
@Composable
fun SettingsHubGroup(rows: List<SettingsRowSpec>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
        ),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        // Aligns with where the title text starts: 14 padding + 42 chip + 14 gap.
                        modifier = Modifier.padding(start = 70.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                    )
                }
                SettingsHubRow(row)
            }
        }
    }
}

@Composable
fun SettingsHubRow(row: SettingsRowSpec) {
    val enabled = row.enabled
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { row.onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    },
                    shape = VeritasPackStyle.compactShape()
                )
                .then(
                    Modifier.border(
                        VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
                        VeritasPackStyle.compactShape()
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = row.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = if (enabled) 1f else 0.45f),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            // titleMedium/bodyMedium now carry the 15/13sp scale app-wide, set in
            // veritasTypography() — no local override needed.
            Text(
                text = row.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = row.disabledReason ?: row.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                // Long subtitles wrap rather than truncate now that rows are not
                // fighting for height.
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.85f else 0.5f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.55f else 0.25f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Typeface picker. Every row is set in the face it offers — a font list rendered
 * in one typeface tells you nothing about the others.
 */
@Composable
fun VeritasFontPicker(
    selectedFontId: String,
    onUiFontChange: (String) -> Unit
) {
    val selected = VeritasUiFont.fromId(selectedFontId)
    Column(modifier = Modifier.fillMaxWidth()) {
        VeritasUiFont.entries.forEachIndexed { index, font ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                )
            }
            val isSelected = font == selected
            // fontFamily() is null for SYSTEM, and a null fontFamily on Text means
            // "inherit" — which made the System default row render in whichever face
            // was currently active instead of the platform one it actually offers.
            val family = font.fontFamily() ?: FontFamily.Default
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUiFontChange(font.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = font.label,
                        fontFamily = family,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        // A pangram-ish specimen: enough letterforms to tell the
                        // faces apart at a glance.
                        text = "The quick brown fox jumps over",
                        fontFamily = family,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = font.note,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                RadioButton(selected = isSelected, onClick = { onUiFontChange(font.id) })
            }
        }
    }
}

@Composable
fun VeritasThemePackPicker(
    selectedPackId: String,
    onThemePackChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VeritasThemePackCatalog.packOptions.chunked(2).forEach { rowPacks ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowPacks.forEach { (packId, label) ->
                    val selected = VeritasThemePackCatalog.normalizePackId(selectedPackId) == packId
                    val modifier = Modifier.weight(1f)
                    if (selected) {
                        Button(
                            onClick = { onThemePackChange(packId) },
                            shape = VeritasPackStyle.chipShape(),
                            modifier = modifier
                        ) {
                            PackChoiceContent(label = label, packId = packId, selected = true)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onThemePackChange(packId) },
                            shape = VeritasPackStyle.chipShape(),
                            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
                            modifier = modifier
                        ) {
                            PackChoiceContent(label = label, packId = packId, selected = false)
                        }
                    }
                }
                if (rowPacks.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PackPalettePreview(packId: String) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    
    val shape = when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "material_you" -> RoundedCornerShape(6.dp)
        "liquid_glass" -> RoundedCornerShape(10.dp)
        "one_ui" -> RoundedCornerShape(3.dp)
        else -> RoundedCornerShape(1.dp)
    }
    
    val alpha = when (VeritasThemePackCatalog.normalizePackId(packId)) {
        "liquid_glass" -> 0.62f
        else -> 1.0f
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(primary.copy(alpha = alpha), shape)
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(secondary.copy(alpha = alpha), shape)
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(tertiary.copy(alpha = alpha), shape)
        )
    }
}

@Composable
fun PackChoiceContent(label: String, packId: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        PackPalettePreview(packId = packId)
    }
}

/**
 * Live theme preview: a miniature of the actual app UI rendered inside a nested
 * MaterialTheme using the SELECTED theme's real ColorScheme and the pack's shapes/
 * background brush — so it looks exactly like the app will, not an approximation.
 */
@Composable
fun ThemePreviewCard(themePackId: String, themeId: String, vibrantHero: Boolean = false) {
    val context = LocalContext.current
    val normalizedPack = VeritasThemePackCatalog.normalizePackId(themePackId)
    val scheme = veritasColorScheme(themeId, context)
    // Mirror the hero-card style toggle: vibrant = accent poster gradient with a
    // luminance-picked text colour; subtle = container tones.
    val heroGradient = if (vibrantHero) {
        val hsl = FloatArray(3)
        android.graphics.Color.colorToHSV(scheme.primary.toArgb(), hsl)
        Brush.linearGradient(
            listOf(
                Color(android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], (hsl[1] * 0.7f).coerceIn(0f, 1f), (hsl[2] * 1.15f).coerceIn(0f, 1f)))),
                Color(android.graphics.Color.HSVToColor(floatArrayOf((hsl[0] + 15f) % 360f, hsl[1].coerceIn(0f, 1f), (hsl[2] * 0.85f).coerceIn(0f, 1f))))
            )
        )
    } else {
        Brush.linearGradient(listOf(scheme.primaryContainer, blendColors(scheme.primaryContainer, scheme.surface, 0.5f)))
    }
    val heroOnColor = if (vibrantHero) {
        if (scheme.primary.luminance() > 0.35f) Color(0xFF1A1A2E) else Color.White
    } else scheme.onPrimaryContainer
    val cardCorner = when (normalizedPack) {
        "material_you" -> 22.dp
        "liquid_glass" -> 26.dp
        "one_ui" -> 18.dp
        else -> 16.dp
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Live preview",
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium
        )
        MaterialTheme(colorScheme = scheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(226.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(VeritasPackStyle.backgroundBrush(scheme))
                    .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Top bar with app icon + wordmark + settings dot
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.veritas_reader_icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Veritas", fontWeight = FontWeight.Black, color = scheme.onBackground, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        Box(modifier = Modifier.size(22.dp).background(scheme.surfaceVariant, CircleShape))
                    }
                    // Hero card — reflects the vibrant/subtle toggle, like Home
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp)
                            .clip(RoundedCornerShape(cardCorner))
                            .background(heroGradient)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "Lorem ipsum dolor",
                                color = heroOnColor,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "sit amet consectetur",
                                color = heroOnColor.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd).size(30.dp).background(if (vibrantHero) heroOnColor.copy(alpha = 0.25f) else scheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(11.dp).background(if (vibrantHero) heroOnColor else scheme.onPrimary, CircleShape))
                        }
                    }
                    // Two content rows on real surface, with sample text
                    val sampleRows = listOf(
                        "Dolor sit amet" to "consectetur elit",
                        "Adipiscing tempor" to "incididunt labore"
                    )
                    sampleRows.forEach { (title, sub) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape((cardCorner.value - 4).coerceAtLeast(6f).dp))
                                .background(scheme.surface.copy(alpha = 0.85f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(22.dp).background(scheme.secondaryContainer, RoundedCornerShape(6.dp)))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(title, color = scheme.onSurface, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(sub, color = scheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    // Bottom nav — active tab in primary, others muted
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        repeat(3) { i ->
                            Box(modifier = Modifier.size(if (i == 0) 24.dp else 18.dp).background(if (i == 0) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape))
                        }
                    }
                }
            }
        }
        Text(
            "${VeritasThemePackCatalog.displayName(normalizedPack)} · ${VeritasThemeCatalog.displayName(themeId)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun VeritasThemePicker(
    selectedThemeId: String,
    onThemeChange: (String) -> Unit
) {
    val normalizedSelected = VeritasThemeCatalog.normalizeThemeId(selectedThemeId)

    val col1Themes = listOf(
        "system" to "System Default",
        "light" to "Light",
        "github_light" to "GitHub Light",
        "bw_gradient_light" to "B/W Gradient Light",
        "blue_high_contrast" to "Blue High Contrast",
        "one_dark_pro" to "One Dark Pro"
    )

    val col2Themes = listOf(
        "dark" to "Dark",
        "midnight_dark" to "Midnight Dark",
        "github_dark" to "GitHub Dark",
        "bw_gradient_dark" to "B/W Gradient Dark",
        "dracula" to "Dracula",
        "neon" to "Neon"
    )

    val rowCount = maxOf(col1Themes.size, col2Themes.size)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until rowCount) {
            val item1 = col1Themes.getOrNull(i)
            val item2 = col2Themes.getOrNull(i)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (item1 != null) {
                    val (id1, label1) = item1
                    val selected1 = normalizedSelected == id1
                    val colors1 = themePreviewColors(id1)
                    if (selected1) {
                        Button(
                            onClick = { onThemeChange(id1) },
                            shape = VeritasPackStyle.chipShape(),
                            modifier = Modifier.weight(1f)
                        ) {
                            ThemeChoiceContent(label = label1, previewColors = colors1, selected = true)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onThemeChange(id1) },
                            shape = VeritasPackStyle.chipShape(),
                            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
                            modifier = Modifier.weight(1f)
                        ) {
                            ThemeChoiceContent(label = label1, previewColors = colors1, selected = false)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (item2 != null) {
                    val (id2, label2) = item2
                    val selected2 = normalizedSelected == id2
                    val colors2 = themePreviewColors(id2)
                    if (selected2) {
                        Button(
                            onClick = { onThemeChange(id2) },
                            shape = VeritasPackStyle.chipShape(),
                            modifier = Modifier.weight(1f)
                        ) {
                            ThemeChoiceContent(label = label2, previewColors = colors2, selected = true)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onThemeChange(id2) },
                            shape = VeritasPackStyle.chipShape(),
                            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
                            modifier = Modifier.weight(1f)
                        ) {
                            ThemeChoiceContent(label = label2, previewColors = colors2, selected = false)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ThemeChoiceContent(label: String, previewColors: List<Color>, selected: Boolean) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            previewColors.forEach { color ->
                Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
            }
        }
    }
}

/** Full-screen top bar with a back action for settings sub-pages. */
@Composable
private fun FullScreenSettingsScaffold(
    title: String,
    onBack: () -> Unit,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    // decorFitsSystemWindows = false lets the surface draw edge-to-edge behind the
    // status/nav bars so the background matches the rest of the app's full-screen look
    // (content is inset by statusBarsPadding/navigationBarsPadding below).
    Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .background(VeritasPackStyle.backgroundBrush(MaterialTheme.colorScheme))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun ReaderSettingsDialog(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onSpacingChange: (Int) -> Unit,
    onThemeChange: (String) -> Unit,
    onThemePackChange: (String) -> Unit,
    onToggleVibrantHero: () -> Unit,
    onToggleAutoPlayQueue: () -> Unit,
    onUiFontChange: (String) -> Unit = {}
) {
    var themePacksExpanded by remember { mutableStateOf(false) }
    var colourThemesExpanded by remember { mutableStateOf(false) }
    var typefaceExpanded by remember { mutableStateOf(false) }

    FullScreenSettingsScaffold(title = "Display & theme", onBack = onDismiss) {
        // Theme Packs Accordion
        SettingsHubSectionTitle("Theme packs")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { themePacksExpanded = !themePacksExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Theme pack: ${VeritasThemePackCatalog.displayName(settings.themePackId)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "UI styling, shapes, and surface finish",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { themePacksExpanded = !themePacksExpanded }) {
                        Icon(
                            if (themePacksExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                            contentDescription = if (themePacksExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (themePacksExpanded) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    VeritasThemePackPicker(
                        selectedPackId = settings.themePackId,
                        onThemePackChange = onThemePackChange
                    )
                }
            }
        }

        // Colour Themes Accordion
        SettingsHubSectionTitle("Colour themes")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { colourThemesExpanded = !colourThemesExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Colour theme: ${VeritasThemeCatalog.displayName(settings.themeId)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Color palette and document canvas tone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { colourThemesExpanded = !colourThemesExpanded }) {
                        Icon(
                            if (colourThemesExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                            contentDescription = if (colourThemesExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (colourThemesExpanded) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    VeritasThemePicker(
                        selectedThemeId = settings.themeId,
                        onThemeChange = onThemeChange
                    )
                }
            }
        }

        ThemePreviewCard(
            themePackId = settings.themePackId,
            themeId = settings.themeId,
            vibrantHero = settings.vibrantHero
        )

        // Typeface Accordion
        SettingsHubSectionTitle("Typeface")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { typefaceExpanded = !typefaceExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Selected Font: ${settings.uiFontId.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Reader typography & font family",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { typefaceExpanded = !typefaceExpanded }) {
                        Icon(
                            if (typefaceExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                            contentDescription = if (typefaceExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (typefaceExpanded) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    VeritasFontPicker(
                        selectedFontId = settings.uiFontId,
                        onUiFontChange = onUiFontChange
                    )
                }
            }
        }

        // Text Sizing & Spacing Section
        SettingsHubSectionTitle("Text sizing & spacing")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Text size",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${settings.fontSizeSp} sp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                VeritasRoundSlider(
                    value = settings.fontSizeSp.toFloat(),
                    onValueChange = { onFontSizeChange(it.toInt().coerceIn(14, 28)) },
                    valueRange = 14f..28f,
                    steps = 13
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Paragraph spacing",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${settings.sectionSpacingDp} dp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                VeritasRoundSlider(
                    value = settings.sectionSpacingDp.toFloat(),
                    onValueChange = { onSpacingChange(it.toInt().coerceIn(6, 24)) },
                    valueRange = 6f..24f,
                    steps = 17
                )
            }
        }

        // Preferences Section
        SettingsHubSectionTitle("Display preferences")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibrant hero card", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Bold accent gradient on the home card", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.vibrantHero, onCheckedChange = { onToggleVibrantHero() })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-play queue", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Automatically play next queued book", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.autoPlayQueue, onCheckedChange = { onToggleAutoPlayQueue() })
                }
            }
        }
    }
}

@Composable
fun AccessibilitySettingsDialog(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onThemeChange: (String) -> Unit,
    onToggleContrastTheme: (String, String?) -> Unit = { theme, _ -> onThemeChange(theme) },
    onToggleAdaptiveCover: () -> Unit,
    onToggleSectionNumbers: () -> Unit,
    onGoalMinutesChange: (Int) -> Unit,
    onToggleStreakReminder: () -> Unit,
    onToggleReduceMotion: () -> Unit,
    onToggleBionicReading: () -> Unit = {},
    onToggleShakeToExtend: () -> Unit = {},
    onToggleCollapsibleBars: () -> Unit = {}
) {
    FullScreenSettingsScaffold(title = "Accessibility", onBack = onDismiss) {
        // Reading focus & comprehension
        SettingsHubSectionTitle("Focus & comprehension")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Collapsible reader bars", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Single tap on reading canvas hides/shows top and bottom bars. Toggle off to keep bars permanently pinned.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.collapsibleReaderBars, onCheckedChange = { onToggleCollapsibleBars() })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bionic reading mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Bold leading letters to guide eye fixation and speed comprehension", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.bionicReading, onCheckedChange = { onToggleBionicReading() })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shake to extend sleep timer", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Gently shake phone in final minute to add 10 min without unlocking", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.shakeToExtendSleepTimer, onCheckedChange = { onToggleShakeToExtend() })
                }
            }
        }

        // Reading goal
        SettingsHubSectionTitle("Daily reading goal")
        val goalOn = settings.dailyGoalMinutes > 0
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Set daily reading goal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Shows daily progress ring on the home card", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(
                        checked = goalOn,
                        onCheckedChange = { on -> onGoalMinutesChange(if (on) 20 else 0) }
                    )
                }
                if (goalOn) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily target", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${settings.dailyGoalMinutes} min", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = settings.dailyGoalMinutes.coerceIn(5, 180).toFloat(),
                        onValueChange = rememberSliderHaptics(settings.dailyGoalMinutes.coerceIn(5, 180).toFloat(), 5f..180f, 34) { onGoalMinutesChange(it.toInt().coerceIn(5, 180)) },
                        valueRange = 5f..180f,
                        steps = 34
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Streak reminder", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Evening nudge when daily goal is at risk", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        VeritasSwitch(checked = settings.streakReminderEnabled, onCheckedChange = { onToggleStreakReminder() })
                    }
                }
            }
        }

        SettingsHubSectionTitle("Motion & contrast")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reduce motion", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Disable decorative animations and pulses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.reduceMotion, onCheckedChange = { onToggleReduceMotion() })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("High-contrast quick presets", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    
                    val isDarkContrast = settings.themeId == "dark_high_contrast"
                    val isLightContrast = settings.themeId == "white_high_contrast"
                    val isHighContrast = isDarkContrast || isLightContrast

                    fun toggleContrast(targetThemeId: String) {
                        if (settings.themeId == targetThemeId) {
                            // Toggle OFF: restore previous theme before high contrast was activated
                            val restoreTheme = settings.previousThemeId?.takeIf {
                                it.isNotBlank() && it != "dark_high_contrast" && it != "white_high_contrast" && it != "blue_high_contrast"
                            } ?: if (targetThemeId == "dark_high_contrast") "dark" else "light"
                            onToggleContrastTheme(restoreTheme, null)
                        } else {
                            // Toggle ON: remember current theme (if not already high contrast) and switch
                            val savedPrev = if (!isHighContrast) settings.themeId else settings.previousThemeId
                            onToggleContrastTheme(targetThemeId, savedPrev)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isDarkContrast) {
                            Button(
                                onClick = { toggleContrast("dark_high_contrast") },
                                modifier = Modifier.weight(1f),
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text("Contrast Dark", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { toggleContrast("dark_high_contrast") },
                                modifier = Modifier.weight(1f),
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text("Contrast Dark")
                            }
                        }

                        if (isLightContrast) {
                            Button(
                                onClick = { toggleContrast("white_high_contrast") },
                                modifier = Modifier.weight(1f),
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text("Contrast Light", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { toggleContrast("white_high_contrast") },
                                modifier = Modifier.weight(1f),
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text("Contrast Light")
                            }
                        }
                    }
                }
            }
        }

        SettingsHubSectionTitle("Appearance extras")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Adaptive cover theme", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Blend active book cover palette into theme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.adaptiveCover, onCheckedChange = { onToggleAdaptiveCover() })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show section labels", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Display indices for study and search jumps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.showSectionNumbers, onCheckedChange = { onToggleSectionNumbers() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeritasRoundSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val trackHeight = 8.dp
    val thumbSize = 12.dp
    Slider(
        value = value,
        onValueChange = rememberSliderHaptics(value, valueRange, steps, onValueChange),
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        modifier = modifier,
        thumb = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        CircleShape
                    )
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(trackHeight)
            )
        }
    )
}

private fun getAiOptionIcon(optionId: String): ImageVector = aiAssistantIcon(optionId)

@Composable
fun AskAiSettingsDialog(
    settings: AskAiSettings,
    onSettingsChange: (AskAiSettings) -> Unit,
    onInstallAssistant: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var promptDraft by remember(settings.promptTemplate) { mutableStateOf(settings.promptTemplate) }

    FullScreenSettingsScaffold(title = "Ask AI", onBack = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "AI Assistant Preference",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Choose the assistant Veritas should launch when you select text in the reader. If your chosen app is not installed, Veritas can redirect you to the Play Store.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select Preferred AI App",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        aiAssistantOptions.forEach { option ->
            val selected = settings.assistantId == option.id
            val installedPackage = installedPackageForOption(context, option)
            val installed = option.packageName.isBlank() || installedPackage != null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (installed || option.packageName.isBlank()) {
                            onSettingsChange(
                                settings.copy(
                                    assistantId = option.id,
                                    assistantLabel = option.label,
                                    packageName = installedPackage ?: option.packageName,
                                    promptTemplate = promptDraft
                                )
                            )
                        } else {
                            onInstallAssistant(option.packageName)
                        }
                    }
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cardBorder = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = VeritasPackStyle.compactShape()
                        )
                        .then(Modifier.border(cardBorder, VeritasPackStyle.compactShape())),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAiOptionIcon(option.id),
                        contentDescription = option.label,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (installed) "Ready to launch" else "Install from Play Store",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (!installed) {
                    TextButton(onClick = { onInstallAssistant(option.packageName) }) {
                        Text("Install", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Custom Prompt Template",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = promptDraft,
            onValueChange = {
                promptDraft = it
                onSettingsChange(settings.copy(promptTemplate = it))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = VeritasPackStyle.cardShape(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            label = { Text("Prompt Template") },
            placeholder = { Text("Use {selection} where the selected text should appear") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                onSettingsChange(settings.copy(promptTemplate = promptDraft))
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.chipShape()
        ) {
            Text("Save AI Settings", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VoiceStudioDialog(
    settings: VoiceSettings,
    engines: List<TtsEngineOption>,
    voices: List<TtsVoiceOption>,
    loadingVoices: Boolean,
    onRefreshEngines: () -> Unit,
    onLoadVoices: () -> Unit,
    onUseSystemDefault: () -> Unit,
    onEngineSelected: (TtsEngineOption) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onShowNetworkVoicesChange: (Boolean) -> Unit,
    onVoiceSelected: (TtsVoiceOption) -> Unit,
    onPreviewVoice: (TtsVoiceOption) -> Unit,
    onPreviewActiveVoiceWithPreset: () -> Unit = {},
    onPresetSelected: (String, Float, Float) -> Unit,
    onAddLanguageVoice: () -> Unit,
    onOpenSystemTtsSettings: () -> Unit,
    onOpenSpeechEdits: () -> Unit,
    onOpenNarrationStudio: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentLocale = LocalConfiguration.current.locales[0]
    var managerMenuExpanded by remember { mutableStateOf(false) }
    var engineMenuExpanded by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var voiceMenuExpanded by remember { mutableStateOf(false) }
    var profileMenuExpanded by remember { mutableStateOf(false) }
    val visibleVoices = remember(voices, settings.showNetworkVoices, currentLocale) {
        voices
            .filter { settings.showNetworkVoices || !it.requiresNetwork }
            .sortedWith { a, b ->
                val aLoc = a.localeTag
                val bLoc = b.localeTag
                val aEn = aLoc.startsWith("en", ignoreCase = true)
                val bEn = bLoc.startsWith("en", ignoreCase = true)
                val locCompare = when {
                    aEn && !bEn -> -1
                    !aEn && bEn -> 1
                    aEn && bEn -> {
                        val aUS = aLoc.equals("en-US", ignoreCase = true) || aLoc.equals("en_US", ignoreCase = true)
                        val bUS = bLoc.equals("en-US", ignoreCase = true) || bLoc.equals("en_US", ignoreCase = true)
                        val aGB = aLoc.equals("en-GB", ignoreCase = true) || aLoc.equals("en_GB", ignoreCase = true)
                        val bGB = bLoc.equals("en-GB", ignoreCase = true) || bLoc.equals("en_GB", ignoreCase = true)
                        when {
                            aUS && !bUS -> -1
                            !aUS && bUS -> 1
                            aGB && !bGB -> -1
                            !aGB && bGB -> 1
                            else -> aLoc.compareTo(bLoc, ignoreCase = true)
                        }
                    }
                    else -> aLoc.compareTo(bLoc, ignoreCase = true)
                }
                if (locCompare != 0) locCompare
                else a.name.compareTo(b.name, ignoreCase = true)
            }
    }
    val languageTags = remember(visibleVoices) {
        visibleVoices.map { it.localeTag }.distinct().sortedWith { a, b ->
            val aEn = a.startsWith("en", ignoreCase = true)
            val bEn = b.startsWith("en", ignoreCase = true)
            when {
                aEn && !bEn -> -1
                !aEn && bEn -> 1
                aEn && bEn -> {
                    val aUS = a.equals("en-US", ignoreCase = true) || a.equals("en_US", ignoreCase = true)
                    val bUS = b.equals("en-US", ignoreCase = true) || b.equals("en_US", ignoreCase = true)
                    val aGB = a.equals("en-GB", ignoreCase = true) || a.equals("en_GB", ignoreCase = true)
                    val bGB = b.equals("en-GB", ignoreCase = true) || b.equals("en_GB", ignoreCase = true)
                    when {
                        aUS && !bUS -> -1
                        !aUS && bUS -> 1
                        aGB && !bGB -> -1
                        !aGB && bGB -> 1
                        else -> a.compareTo(b, ignoreCase = true)
                    }
                }
                else -> a.compareTo(b, ignoreCase = true)
            }
        }
    }
    var selectedLanguageTag by remember(settings.localeTag, languageTags) {
        mutableStateOf(
            when {
                settings.localeTag.isNotBlank() && settings.localeTag in languageTags -> settings.localeTag
                languageTags.contains(currentLocale.toLanguageTag()) -> currentLocale.toLanguageTag()
                languageTags.isNotEmpty() -> languageTags.first()
                else -> settings.localeTag
            }
        )
    }

    val languageVoices = remember(visibleVoices, selectedLanguageTag) {
        if (selectedLanguageTag.isBlank()) visibleVoices else visibleVoices.filter { it.localeTag == selectedLanguageTag }
    }
    val selectedVoice = voices.firstOrNull { it.name == settings.voiceName }

    val activeVoiceDisplayName = remember(selectedVoice, settings) {
        if (selectedVoice != null) {
            selectedVoice.label.ifBlank {
                VoiceNamingRegistry.resolveHumanName(selectedVoice.name, selectedVoice.localeTag, selectedVoice.requiresNetwork)
            }
        } else if (settings.voiceName.isNotBlank()) {
            VoiceNamingRegistry.resolveHumanName(settings.voiceName, settings.localeTag, false)
        } else {
            settings.displayName.ifBlank { "System default voice" }
        }
    }

    var voiceSearchQuery by remember { mutableStateOf("") }
    val filteredLanguageVoices = remember(languageVoices, voiceSearchQuery) {
        if (voiceSearchQuery.isBlank()) languageVoices
        else languageVoices.filter {
            it.name.contains(voiceSearchQuery, ignoreCase = true) ||
            it.label.contains(voiceSearchQuery, ignoreCase = true) ||
            it.localeTag.contains(voiceSearchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(settings.enginePackage) {
        onRefreshEngines()
        onLoadVoices()
    }

    FullScreenSettingsScaffold(title = "Voice and language", onBack = onDismiss) {
        // Active Voice Highlight Card
        SettingsHubSectionTitle("Active voice")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("CURRENT VOICE MODEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            activeVoiceDisplayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            voiceLanguageLabel(selectedLanguageTag, currentLocale),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    if (selectedVoice != null || settings.displayName.isNotBlank()) {
                        FilledTonalButton(onClick = { onPreviewActiveVoiceWithPreset() }) {
                            Icon(Icons.Outlined.GraphicEq, contentDescription = "Play Sample", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sample")
                        }
                    }
                }
            }
        }

        // Voice Presets Gallery
        SettingsHubSectionTitle("Voice presets")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            voicePresets().forEach { preset ->
                val isSelected = settings.profileName.equals(preset.name, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { onPresetSelected(preset.name, preset.rate, preset.pitch) },
                    label = {
                        Text(
                            preset.name,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        HorizontalDivider()

        // Search & Language Controls
        SettingsHubSectionTitle("Language & voice library")

        OutlinedTextField(
            value = voiceSearchQuery,
            onValueChange = { voiceSearchQuery = it },
            placeholder = { Text("Search voices or languages...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = if (voiceSearchQuery.isNotBlank()) {
                { TextButton(onClick = { voiceSearchQuery = "" }) { Text("Clear", color = MaterialTheme.colorScheme.primary) } }
            } else null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        val currentEngineLabel = when {
            settings.enginePackage == VoiceManager.VERITAS_STUDIO -> "Veritas Studio"
            settings.enginePackage == VoiceManager.VERITAS_LITE -> "Veritas Lite"
            settings.engineLabel.isNotBlank() && settings.engineLabel != "System default" -> settings.engineLabel
            settings.enginePackage.isNotBlank() -> settings.enginePackage
            else -> "System Default"
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { languageMenuExpanded = true },
                    enabled = visibleVoices.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        voiceLanguageLabel(selectedLanguageTag, currentLocale),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false },
                    modifier = Modifier.width(320.dp).heightIn(max = 360.dp)
                ) {
                    languageTags.forEach { localeTag ->
                        DropdownMenuItem(
                            text = { Text(voiceLanguageLabel(localeTag, currentLocale), maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                languageMenuExpanded = false
                                selectedLanguageTag = localeTag
                                onLanguageSelected(localeTag)
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { managerMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        currentEngineLabel,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(
                    expanded = managerMenuExpanded,
                    onDismissRequest = { managerMenuExpanded = false },
                    modifier = Modifier.width(280.dp).heightIn(max = 360.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Use System Default", color = MaterialTheme.colorScheme.onSurface)
                                if (settings.enginePackage.isBlank()) {
                                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        onClick = {
                            managerMenuExpanded = false
                            onUseSystemDefault()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Use Veritas Studio", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                if (VoiceManager.isVeritasEngine(settings.enginePackage)) {
                                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        onClick = {
                            managerMenuExpanded = false
                            onEngineSelected(TtsEngineOption(VoiceManager.VERITAS_STUDIO, "Veritas Studio"))
                        }
                    )
                    if (engines.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        engines.forEach { engine ->
                            val isSelected = settings.enginePackage == engine.packageName
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(engine.label, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                                        if (isSelected) {
                                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    managerMenuExpanded = false
                                    onEngineSelected(engine)
                                    onLoadVoices()
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = settings.showNetworkVoices,
                onCheckedChange = onShowNetworkVoicesChange
            )
            Text("Include online/network voices", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }

        if (loadingVoices) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Text("Loading voice models...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        // Downloading a voice from its own row needs these locally; the progress map
        // is observed so the row flips to a preview button when the install finishes.
        val voiceRowContext = androidx.compose.ui.platform.LocalContext.current
        val voiceRowScope = androidx.compose.runtime.rememberCoroutineScope()
        val voiceDownloadStates by com.veritas.reader.tts.VoiceModelManager.downloadState.collectAsState()

        // Voice List Cards
        filteredLanguageVoices.forEach { voice ->
            val isCurrent = voice.name == settings.voiceName
            val offlineVoice = com.veritas.reader.tts.VoiceModelManager.availableVoices
                .firstOrNull { it.id == voice.name }
            val needsDownload = offlineVoice != null &&
                !com.veritas.reader.tts.VoiceModelManager.isVoiceInstalled(voiceRowContext, voice.name)
            val voiceDownload = voiceDownloadStates[voice.name]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !needsDownload) { onVoiceSelected(voice) },
                shape = VeritasPackStyle.cardShape(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = if (isCurrent) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                voice.label.ifBlank { voice.name },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isCurrent) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Text("Active", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        Text(
                            voiceProviderLabel(voice, currentLocale),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (needsDownload && offlineVoice != null) {
                        when (val state = voiceDownload) {
                            is com.veritas.reader.tts.DownloadState.Downloading ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("${state.percent}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            else -> TextButton(onClick = {
                                voiceRowScope.launch {
                                    com.veritas.reader.tts.VoiceModelManager.downloadVoice(voiceRowContext, offlineVoice)
                                }
                            }) {
                                Text(
                                    "Get · ${offlineVoice.downloadSizeMb.toInt()}MB",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { onPreviewVoice(voice) }) {
                            Icon(Icons.Outlined.GraphicEq, contentDescription = "Preview voice", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        if (filteredLanguageVoices.isEmpty() && !loadingVoices) {
            Text(
                "No voices found matching search filter.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (VoiceManager.isVeritasEngine(settings.enginePackage)) {
            SettingsHubSectionTitle("Veritas voice models")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = VeritasPackStyle.cardShape(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
            ) {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val downloadStates by com.veritas.reader.tts.VoiceModelManager.downloadState.collectAsState()
                var refreshTrigger by remember { mutableStateOf(0) }

                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Download offline neural voice packages for studio-quality human cadence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    com.veritas.reader.tts.VoiceModelManager.availablePackages.forEach { pkg ->
                        val isInstalled = remember(refreshTrigger) {
                            com.veritas.reader.tts.VoiceModelManager.isVoiceInstalled(context, pkg.targetVoiceId)
                        }
                        val targetVoice = com.veritas.reader.tts.VoiceModelManager.availableVoices.firstOrNull { it.id == pkg.targetVoiceId }
                        val state = downloadStates[pkg.targetVoiceId] ?: com.veritas.reader.tts.DownloadState.Idle

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pkg.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    pkg.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (state is com.veritas.reader.tts.DownloadState.Downloading) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { state.percent / 100f },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text("Downloading ${state.percent}%...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                } else if (state is com.veritas.reader.tts.DownloadState.Failed) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(state.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (isInstalled) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Installed", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text("Installed", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = {
                                        com.veritas.reader.tts.VoiceModelManager.deleteVoice(context, pkg.targetVoiceId)
                                        refreshTrigger++
                                        onRefreshEngines()
                                        onLoadVoices()
                                    }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete voice model", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }
                            } else if (state is com.veritas.reader.tts.DownloadState.Downloading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Button(
                                    onClick = {
                                        if (targetVoice != null) {
                                            coroutineScope.launch {
                                                val ok = com.veritas.reader.tts.VoiceModelManager.downloadVoice(context, targetVoice)
                                                if (ok) {
                                                    refreshTrigger++
                                                    onRefreshEngines()
                                                    onLoadVoices()
                                                }
                                            }
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Download", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Navigation Studio Shortcuts
        SettingsHubSectionTitle("Narration shortcuts")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onOpenNarrationStudio,
                modifier = Modifier.weight(1f)
            ) {
                Text("Narration Studio", color = MaterialTheme.colorScheme.onSurface)
            }
            OutlinedButton(
                onClick = onOpenSpeechEdits,
                modifier = Modifier.weight(1f)
            ) {
                Text("Speech Edits", color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Use Selected Voice")
        }
        OutlinedButton(onClick = onAddLanguageVoice, modifier = Modifier.fillMaxWidth()) {
            Text("Add Language / Voice", color = MaterialTheme.colorScheme.onSurface)
        }
        OutlinedButton(onClick = onOpenSystemTtsSettings, modifier = Modifier.fillMaxWidth()) {
            Text("System TTS Settings", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun voiceLanguageLabel(localeTag: String, displayLocale: Locale): String {
    if (localeTag.isBlank()) return "Default language"
    val locale = Locale.forLanguageTag(localeTag)
    return locale.getDisplayName(displayLocale).ifBlank { localeTag }
}

private fun voiceProviderLabel(voice: TtsVoiceOption, displayLocale: Locale): String {
    val language = voiceLanguageLabel(voice.localeTag, displayLocale)
    val network = if (voice.requiresNetwork) "Network voice" else "Offline voice"
    return "$language, $network, quality ${voice.quality}, latency ${voice.latency}"
}

@Composable
fun NarrationStudioDialog(
    settings: NarrationSettings,
    sampleText: String,
    availableVoices: List<TtsVoiceOption> = emptyList(),
    onSettingsChange: (NarrationSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sample = sampleText.ifBlank { "\"This is a sample dialogue line,\" she said. The narrator continues with a calmer sentence." }
    val sampleLabel = NarrationAnalyzer.labelFor(sample, settings)
    val sampleRate = NarrationAnalyzer.effectiveRate(1.0f, settings, sample)
    val samplePitch = NarrationAnalyzer.effectivePitch(1.0f, settings, sample)

    val context = LocalContext.current
    var loadedVoiceList by remember(availableVoices) { mutableStateOf(availableVoices) }

    LaunchedEffect(Unit) {
        if (loadedVoiceList.isEmpty()) {
            val list = runCatching { VoiceManager.loadVoices(context, "") }.getOrDefault(emptyList())
            if (list.isNotEmpty()) {
                loadedVoiceList = list
            }
        }
    }

    FullScreenSettingsScaffold(title = "Narration studio", onBack = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Outlined.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        if (settings.enabled) "Narration mode active" else "Narration mode inactive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    "Dynamically adjusts speech delivery and character voices for spoken dialogue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        SettingsHubSectionTitle("Dialogue detection")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable narration mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Custom character voices & pacing for dialogue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(checked = settings.enabled, onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Detect dialogue quotes", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Auto-detect quotes, dash dialogue, and speech tags", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(
                        checked = settings.dialogueDetection,
                        onCheckedChange = { onSettingsChange(settings.copy(dialogueDetection = it)) },
                        enabled = settings.enabled
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show dialogue indicators", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Display badges for spoken dialogue in reader", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(
                        checked = settings.showDialogueBadges,
                        onCheckedChange = { onSettingsChange(settings.copy(showDialogueBadges = it)) },
                        enabled = settings.enabled
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Full-cast multi-voice mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Assign unique voice models per character", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VeritasSwitch(
                        checked = settings.fullCastEnabled,
                        onCheckedChange = { onSettingsChange(settings.copy(fullCastEnabled = it)) },
                        enabled = settings.enabled
                    )
                }
            }
        }

        SettingsHubSectionTitle("Character voice profiles")

        var newCharacterName by remember { mutableStateOf("") }
        var showAddCharDialog by remember { mutableStateOf(false) }

        if (showAddCharDialog) {
            AlertDialog(
                onDismissRequest = { showAddCharDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Add character profile")
                    }
                },
                text = {
                    OutlinedTextField(
                        value = newCharacterName,
                        onValueChange = { newCharacterName = it },
                        label = { Text("Character name (e.g. Alice)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCharacterName.isNotBlank()) {
                                val newChar = BookCharacter(
                                    id = java.util.UUID.randomUUID().toString(),
                                    name = newCharacterName.trim(),
                                    genderLabel = "Neutral"
                                )
                                onSettingsChange(settings.copy(characterProfiles = settings.characterProfiles + newChar))
                                newCharacterName = ""
                                showAddCharDialog = false
                            }
                        },
                        shape = RoundedCornerShape(50)
                    ) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCharDialog = false }, shape = RoundedCornerShape(50)) { Text("Cancel") }
                }
            )
        }

        settings.characterProfiles.forEach { char ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = VeritasPackStyle.cardShape(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            char.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val previewText = when (char.id) {
                                    "narrator" -> "This is how the narrator speaks descriptive scenes and exposition."
                                    "dialogue" -> "\"Hello there!\" exclaimed the speaker with emotion."
                                    else -> "\"My name is ${char.name}, and this is my character voice.\""
                                }
                                VoiceManager.previewVoice(
                                    context = context,
                                    enginePackage = "",
                                    voiceName = char.voiceName.orEmpty(),
                                    text = previewText,
                                    pitch = char.pitchMultiplier
                                )
                            },
                            enabled = settings.enabled
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Preview voice",
                                tint = if (settings.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                        if (char.id != "narrator" && char.id != "dialogue") {
                            IconButton(
                                onClick = {
                                    onSettingsChange(settings.copy(characterProfiles = settings.characterProfiles.filterNot { it.id == char.id }))
                                }
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Remove character", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    var voiceMenuExpanded by remember { mutableStateOf(false) }
                    val currentVoiceLabel = loadedVoiceList.find { it.name == char.voiceName }?.label
                        ?: if (char.voiceName.isNullOrBlank()) "System Default Voice" else char.voiceName

                    Text("Assigned Voice Model:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box {
                        OutlinedButton(
                            onClick = { voiceMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = settings.enabled,
                            shape = VeritasPackStyle.chipShape()
                        ) {
                            Text(currentVoiceLabel!!, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(
                            expanded = voiceMenuExpanded,
                            onDismissRequest = { voiceMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("System Default Voice") },
                                onClick = {
                                    val updated = settings.characterProfiles.map { if (it.id == char.id) it.copy(voiceName = null) else it }
                                    onSettingsChange(settings.copy(characterProfiles = updated))
                                    voiceMenuExpanded = false
                                }
                            )
                            loadedVoiceList.forEach { voiceOpt ->
                                DropdownMenuItem(
                                    text = { Text(voiceOpt.label) },
                                    onClick = {
                                        val updated = settings.characterProfiles.map { if (it.id == char.id) it.copy(voiceName = voiceOpt.name) else it }
                                        onSettingsChange(settings.copy(characterProfiles = updated))
                                        voiceMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val archetypes = listOf(
                            Triple("🎙️ Natural", 1.00f, 1.00f),
                            Triple("📖 Deep/Warm", 0.85f, 0.95f),
                            Triple("💬 Bright/High", 1.20f, 1.05f),
                            Triple("🎓 Scholar", 0.90f, 0.90f)
                        )
                        archetypes.forEach { (label, p, r) ->
                            FilterChip(
                                selected = (kotlin.math.abs(char.pitchMultiplier - p) < 0.025f && kotlin.math.abs(char.rateMultiplier - r) < 0.025f),
                                onClick = {
                                    val updated = settings.characterProfiles.map {
                                        if (it.id == char.id) it.copy(pitchMultiplier = p, rateMultiplier = r) else it
                                    }
                                    onSettingsChange(settings.copy(characterProfiles = updated))
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                enabled = settings.enabled,
                                shape = VeritasPackStyle.chipShape()
                            )
                        }
                    }

                    val pitchOffsetPercent = ((char.pitchMultiplier - 1.0f) * 100).toInt()
                    val pitchLabel = when {
                        char.pitchMultiplier >= 1.15f -> "Bright / Crisp High"
                        char.pitchMultiplier > 1.03f -> "Slightly High"
                        char.pitchMultiplier <= 0.85f -> "Rich Deep / Bass"
                        char.pitchMultiplier < 0.97f -> "Warm Deep"
                        else -> "Natural / Normal"
                    }
                    Text("Pitch Tone: ${"%.2f".format(char.pitchMultiplier)}x ($pitchLabel)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    VeritasRoundSlider(
                        value = char.pitchMultiplier,
                        onValueChange = { newP ->
                            val updated = settings.characterProfiles.map { if (it.id == char.id) it.copy(pitchMultiplier = newP) else it }
                            onSettingsChange(settings.copy(characterProfiles = updated))
                        },
                        valueRange = 0.70f..1.35f,
                        enabled = settings.enabled
                    )

                    val rateOffsetPercent = ((char.rateMultiplier - 1.0f) * 100).toInt()
                    val rateLabel = when {
                        rateOffsetPercent > 0 -> "+$rateOffsetPercent% faster"
                        rateOffsetPercent < 0 -> "$rateOffsetPercent% slower"
                        else -> "Standard pacing"
                    }
                    Text("Speech Pace: ${"%.2f".format(char.rateMultiplier)}x ($rateLabel)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    VeritasRoundSlider(
                        value = char.rateMultiplier,
                        onValueChange = { newR ->
                            val updated = settings.characterProfiles.map { if (it.id == char.id) it.copy(rateMultiplier = newR) else it }
                            onSettingsChange(settings.copy(characterProfiles = updated))
                        },
                        valueRange = 0.85f..1.20f,
                        enabled = settings.enabled
                    )
                }
            }
        }

        Button(
            onClick = { showAddCharDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = settings.enabled,
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Character Profile")
        }

        SettingsHubSectionTitle("Preview classification & spoken audio")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VeritasPackStyle.cardShape(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(sample.take(260), maxLines = 5, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            "Detected as: $sampleLabel • ${"%.2f".format(sampleRate)}× • pitch ${"%.2f".format(samplePitch)}",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            VoiceManager.previewVoice(
                                context = context,
                                enginePackage = "",
                                voiceName = "",
                                text = sample,
                                pitch = samplePitch
                            )
                        }
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play classification preview", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun PronunciationRulesDialog(
    rules: List<PronunciationRule>,
    newFind: String,
    newReplaceWith: String,
    onNewFindChange: (String) -> Unit,
    onNewReplaceChange: (String) -> Unit,
    onAddRule: () -> Unit,
    onToggleRule: (PronunciationRule) -> Unit,
    onRemoveRule: (PronunciationRule) -> Unit,
    onDismiss: () -> Unit
) {
    var testInput by remember { mutableStateOf("Test pronunciation replacements here.") }
    val testOutput = remember(testInput, rules) {
        var output = testInput
        rules.filter { it.enabled && it.find.isNotBlank() }.forEach { rule ->
            val escapedFind = Regex.escape(rule.find)
            val startsWithWordChar = rule.find.firstOrNull()?.let { it.isLetterOrDigit() || it == '_' } == true
            val endsWithWordChar = rule.find.lastOrNull()?.let { it.isLetterOrDigit() || it == '_' } == true
            val prefix = if (startsWithWordChar) "\\b" else ""
            val suffix = if (endsWithWordChar) "\\b" else ""
            val pattern = "$prefix$escapedFind$suffix"
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            output = regex.replace(output) { matchResult ->
                val matchedText = matchResult.value
                val replacement = rule.replaceWith
                when {
                    replacement.isEmpty() -> ""
                    matchedText.all { it.isUpperCase() } -> replacement.uppercase(java.util.Locale.getDefault())
                    matchedText.firstOrNull()?.isUpperCase() == true -> {
                        replacement.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                    }
                    else -> replacement
                }
            }
        }
        output
    }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Done") } },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Spellcheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Pronunciation rules")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Replace awkward TTS readings before sentences are spoken.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Add new rule section
                OutlinedTextField(
                    value = newFind,
                    onValueChange = onNewFindChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Find word or phrase") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = newReplaceWith,
                    onValueChange = onNewReplaceChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Say instead (phonetic)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = onAddRule,
                    enabled = newFind.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add pronunciation rule")
                }

                // Spoken Reading Preview Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Reading Preview",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    if (testOutput.isNotBlank()) {
                                        VoiceManager.previewVoice(
                                            context = context,
                                            enginePackage = "",
                                            voiceName = "",
                                            text = testOutput
                                        )
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "Hear preview",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        OutlinedTextField(
                            value = testInput,
                            onValueChange = { testInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Type test sentence") },
                            maxLines = 2,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Spoken output:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    if (testOutput.isNotBlank()) {
                                        VoiceManager.previewVoice(
                                            context = context,
                                            enginePackage = "",
                                            voiceName = "",
                                            text = testOutput
                                        )
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play Audio", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                testOutput,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Text(
                    "Active rules (${rules.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (rules.isEmpty()) {
                    Text("No custom rules configured yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    rules.forEach { rule ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "${rule.find} → ${rule.replaceWith}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        if (rule.enabled) "Active rule" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (rule.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    VeritasSwitch(
                                        checked = rule.enabled,
                                        onCheckedChange = { onToggleRule(rule) },
                                        modifier = Modifier.scale(0.78f)
                                    )
                                    IconButton(
                                        onClick = { onRemoveRule(rule) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Remove rule",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

private fun openEmail(context: Context, email: String, subject: String = "") {
    runCatching {
        val uriText = "mailto:$email" + if (subject.isNotBlank()) "?subject=${Uri.encode(subject)}" else ""
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriText))
        context.startActivity(intent)
    }
}

private fun isBatteryOptimizationIgnored(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager ?: return true
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    runCatching {
        val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    }.recoverCatching {
        val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }.recoverCatching {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

val IconGithub: ImageVector
    get() = ImageVector.Builder(
        name = "Github",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 0f)
            curveTo(5.37f, 0f, 0f, 5.37f, 0f, 12f)
            curveTo(0f, 17.31f, 3.435f, 21.795f, 8.205f, 23.385f)
            curveTo(8.805f, 23.495f, 9.025f, 23.125f, 9.025f, 22.805f)
            curveTo(9.025f, 22.515f, 9.015f, 21.755f, 9.01f, 20.735f)
            curveTo(5.675f, 21.46f, 4.97f, 19.125f, 4.97f, 19.125f)
            curveTo(4.425f, 17.735f, 3.645f, 17.365f, 3.645f, 17.365f)
            curveTo(2.555f, 16.62f, 3.73f, 16.635f, 3.73f, 16.635f)
            curveTo(4.935f, 16.72f, 5.57f, 17.87f, 5.57f, 17.87f)
            curveTo(6.64f, 19.705f, 8.38f, 19.175f, 9.065f, 18.87f)
            curveTo(9.175f, 18.095f, 9.485f, 17.565f, 9.825f, 17.265f)
            curveTo(7.16f, 16.965f, 4.355f, 15.935f, 4.355f, 11.335f)
            curveTo(4.355f, 10.025f, 4.825f, 8.955f, 5.595f, 8.115f)
            curveTo(5.47f, 7.815f, 5.06f, 6.595f, 5.715f, 4.955f)
            curveTo(5.715f, 4.955f, 6.72f, 4.635f, 9.01f, 6.185f)
            curveTo(9.965f, 5.92f, 10.99f, 5.788f, 12.01f, 5.783f)
            curveTo(13.03f, 5.788f, 14.055f, 5.92f, 15.01f, 6.185f)
            curveTo(17.3f, 4.635f, 18.305f, 4.955f, 18.305f, 4.955f)
            curveTo(18.96f, 6.595f, 18.55f, 7.815f, 18.425f, 8.115f)
            curveTo(19.2f, 8.955f, 19.665f, 10.025f, 19.665f, 11.335f)
            curveTo(19.665f, 15.945f, 16.855f, 16.96f, 14.18f, 17.255f)
            curveTo(14.61f, 17.625f, 15f, 18.355f, 15f, 19.475f)
            curveTo(15f, 21.075f, 14.985f, 22.365f, 14.985f, 22.805f)
            curveTo(14.985f, 23.13f, 15.2f, 23.505f, 15.81f, 23.385f)
            curveTo(20.57f, 21.79f, 24f, 17.31f, 24f, 12f)
            curveTo(24f, 5.37f, 18.63f, 0f, 12f, 0f)
            close()
        }
    }.build()

val IconX: ImageVector
    get() = ImageVector.Builder(
        name = "X",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(18.244f, 2.25f)
            lineTo(21.552f, 2.25f)
            lineTo(14.325f, 10.51f)
            lineTo(22.827f, 21.75f)
            lineTo(16.17f, 21.75f)
            lineTo(10.956f, 14.933f)
            lineTo(4.99f, 21.75f)
            lineTo(1.68f, 21.75f)
            lineTo(9.41f, 12.915f)
            lineTo(1.254f, 2.25f)
            lineTo(8.08f, 2.25f)
            lineTo(12.793f, 8.481f)
            close()
            moveTo(17.083f, 19.77f)
            lineTo(18.916f, 19.77f)
            lineTo(7.084f, 4.126f)
            lineTo(5.117f, 4.126f)
            close()
        }
    }.build()

val IconTelegram: ImageVector
    get() = ImageVector.Builder(
        name = "Telegram",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(11.944f, 0.002f)
            curveTo(5.347f, 0.002f, 0f, 5.349f, 0f, 11.946f)
            curveTo(0f, 18.543f, 5.347f, 23.89f, 11.944f, 23.89f)
            curveTo(18.541f, 23.89f, 23.888f, 18.543f, 23.888f, 11.946f)
            curveTo(23.888f, 5.349f, 18.541f, 0.002f, 11.944f, 0.002f)
            close()
            moveTo(17.848f, 7.915f)
            lineTo(15.864f, 17.268f)
            curveTo(15.714f, 17.931f, 15.321f, 18.096f, 14.764f, 17.784f)
            lineTo(11.741f, 15.556f)
            lineTo(10.282f, 16.96f)
            curveTo(10.12f, 17.122f, 9.985f, 17.257f, 9.673f, 17.257f)
            lineTo(9.89f, 14.175f)
            lineTo(15.498f, 9.106f)
            curveTo(15.742f, 8.889f, 15.444f, 8.769f, 15.119f, 8.986f)
            lineTo(8.188f, 13.35f)
            lineTo(5.199f, 12.416f)
            curveTo(4.549f, 12.213f, 4.536f, 11.766f, 5.336f, 11.454f)
            lineTo(17.037f, 6.945f)
            curveTo(17.579f, 6.742f, 18.053f, 7.067f, 17.848f, 7.915f)
            close()
        }
    }.build()

val IconWhatsapp: ImageVector
    get() = ImageVector.Builder(
        name = "Whatsapp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.012f, 0f)
            curveTo(5.385f, 0f, 0.005f, 5.378f, 0.005f, 12.007f)
            curveTo(0.005f, 14.127f, 0.559f, 16.202f, 1.611f, 18.026f)
            lineTo(0f, 23.908f)
            lineTo(6.032f, 22.327f)
            curveTo(7.794f, 23.287f, 9.878f, 23.792f, 12.007f, 23.792f)
            curveTo(18.636f, 23.792f, 24.015f, 18.413f, 24.015f, 11.984f)
            curveTo(24.015f, 5.554f, 18.636f, 0f, 12.012f, 0f)
            close()
            moveTo(12.012f, 21.803f)
            curveTo(10.207f, 21.803f, 8.435f, 21.318f, 6.883f, 20.4f)
            lineTo(6.52f, 20.184f)
            lineTo(2.934f, 21.124f)
            lineTo(3.89f, 17.627f)
            lineTo(3.652f, 17.249f)
            curveTo(2.645f, 15.647f, 2.112f, 13.847f, 2.112f, 12.007f)
            curveTo(2.112f, 6.549f, 6.554f, 2.107f, 12.012f, 2.107f)
            curveTo(17.47f, 2.107f, 21.912f, 6.549f, 21.912f, 12.007f)
            curveTo(21.912f, 17.465f, 17.47f, 21.803f, 12.012f, 21.803f)
            close()
            moveTo(17.424f, 14.475f)
            curveTo(17.127f, 14.327f, 15.666f, 13.609f, 15.393f, 13.511f)
            curveTo(15.121f, 13.411f, 14.923f, 13.361f, 14.725f, 13.659f)
            curveTo(14.527f, 13.956f, 13.958f, 14.624f, 13.785f, 14.822f)
            curveTo(13.612f, 15.02f, 13.438f, 15.045f, 13.141f, 14.896f)
            curveTo(12.844f, 14.747f, 11.889f, 14.433f, 10.757f, 13.424f)
            curveTo(9.873f, 12.636f, 9.277f, 11.662f, 9.104f, 11.365f)
            curveTo(8.931f, 11.068f, 9.085f, 10.907f, 9.234f, 10.759f)
            curveTo(9.368f, 10.626f, 9.531f, 10.413f, 9.679f, 10.239f)
            curveTo(9.827f, 10.065f, 9.877f, 9.942f, 9.976f, 9.744f)
            curveTo(10.075f, 9.546f, 10.025f, 9.373f, 9.951f, 9.224f)
            curveTo(9.877f, 9.075f, 9.283f, 7.614f, 9.035f, 7.02f)
            curveTo(8.795f, 6.442f, 8.552f, 6.519f, 8.374f, 6.51f)
            curveTo(8.205f, 6.502f, 8.007f, 6.502f, 7.809f, 6.502f)
            curveTo(7.611f, 6.502f, 7.314f, 6.576f, 7.066f, 6.848f)
            curveTo(6.818f, 7.12f, 6.125f, 7.768f, 6.125f, 9.08f)
            curveTo(6.125f, 10.392f, 7.091f, 11.654f, 7.227f, 11.837f)
            curveTo(7.363f, 12.02f, 9.127f, 14.729f, 11.832f, 15.895f)
            curveTo(12.476f, 16.173f, 12.973f, 16.338f, 13.365f, 16.462f)
            curveTo(14.012f, 16.668f, 14.601f, 16.639f, 15.066f, 16.569f)
            curveTo(15.584f, 16.491f, 16.661f, 15.916f, 16.884f, 15.297f)
            curveTo(17.107f, 14.678f, 17.107f, 14.158f, 17.04f, 14.045f)
            curveTo(16.973f, 13.932f, 16.775f, 13.865f, 16.478f, 13.716f)
            close()
        }
    }.build()

@Composable
private fun SocialIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun AboutOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val cardBorder = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = VeritasPackStyle.compactShape()
                )
                .then(Modifier.border(cardBorder, VeritasPackStyle.compactShape())),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun OpenSourceLicensesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Open Source Licenses", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Veritas Reader is built using the following open source libraries and tools:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                val licenses = listOf(
                    "Jetpack Compose" to "Apache License 2.0",
                    "Kotlin Standard Library" to "Apache License 2.0",
                    "AndroidX Core & Lifecycle" to "Apache License 2.0",
                    "Material Components for Android" to "Apache License 2.0",
                    "KotlinX Coroutines" to "Apache License 2.0",
                    "Google Gson" to "Apache License 2.0"
                )
                licenses.forEach { (lib, lic) ->
                    Column {
                        Text(lib, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(lic, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    )
}

@Composable
fun AboutDialog(
    uiState: ReaderUiState? = null,
    onCheckForUpdates: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showLicensesDialog by remember { mutableStateOf(false) }

    FullScreenSettingsScaffold(title = "About", onBack = onDismiss, scrollable = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: App Logo (max 2dp border) + Title + Version
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.veritas_reader_icon),
                        contentDescription = "Veritas Reader Logo",
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }

                val appVersionText = remember(context) {
                    try {
                        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        val vName = pInfo.versionName ?: com.veritas.reader.BuildConfig.VERSION_NAME
                        val vCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            pInfo.longVersionCode
                        } else {
                            @Suppress("DEPRECATION")
                            pInfo.versionCode.toLong()
                        }
                        "Version $vName (Build $vCode)"
                    } catch (_: Exception) {
                        "Version ${com.veritas.reader.BuildConfig.VERSION_NAME}"
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Veritas Reader",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = appVersionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    OutlinedButton(
                        onClick = onCheckForUpdates,
                        enabled = uiState?.isCheckingForUpdates != true,
                        shape = VeritasPackStyle.chipShape(),
                        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (uiState?.isCheckingForUpdates == true) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checking...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState?.updateStatusMessage ?: "Check for updates",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Action Buttons (Donate & Contact)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { openEmail(context, "myreader.veritas@gmail.com", "Donate to Veritas Reader") },
                    shape = VeritasPackStyle.chipShape(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            VeritasPackStyle.chipShape()
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Donate", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { openEmail(context, "myreader.veritas@gmail.com", "Veritas Reader Feedback & Support") },
                    shape = VeritasPackStyle.chipShape(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            VeritasPackStyle.chipShape()
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mail,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Contact", fontWeight = FontWeight.Bold)
                }
            }

            // Monochrome Social App Icons Row (GitHub, X, Telegram, WhatsApp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialIconButton(
                    icon = IconGithub,
                    label = "GitHub",
                    onClick = { openUrl(context, "https://github.com/fhes-tus/Veritas-Reader") }
                )
                SocialIconButton(
                    icon = IconX,
                    label = "X",
                    onClick = { openUrl(context, "https://x.com/_1st2us") }
                )
                SocialIconButton(
                    icon = IconTelegram,
                    label = "Telegram",
                    onClick = { openUrl(context, "https://t.me/myreader_veritas") }
                )
                SocialIconButton(
                    icon = IconWhatsapp,
                    label = "WhatsApp",
                    onClick = { openUrl(context, "https://wa.me/mr.Gyan_0") }
                )
            }

            // About Veritas Reader Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = VeritasPackStyle.cardShape(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                ),
                border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "About Veritas Reader",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Veritas Reader is a modern, privacy-focused reading engine and document studio for Android. Built for readers, researchers, and students, Veritas brings together intelligent text-to-speech narration, custom voice controls, smart study cards, and reading insights — all with zero tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            // Tight Option Cards List (Submit Issue & Open Source Licenses)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AboutOptionRow(
                    title = "Submit issue or feedback",
                    subtitle = "Help us improve this application on GitHub",
                    icon = Icons.Outlined.BugReport,
                    onClick = { openUrl(context, "https://github.com/fhes-tus/Veritas-Reader/issues") }
                )
                AboutOptionRow(
                    title = "Open source licenses",
                    subtitle = "View all the libraries used to build Veritas Reader",
                    icon = Icons.Outlined.Info,
                    onClick = { showLicensesDialog = true }
                )
            }
        }
    }

    if (showLicensesDialog) {
        OpenSourceLicensesDialog(onDismiss = { showLicensesDialog = false })
    }
}

