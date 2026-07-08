package com.veritas.reader.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape

import com.veritas.reader.ui.ReaderUiState

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility

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
    onOpenAccessibility: () -> Unit = {}
) {
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {


                SettingsHubSectionTitle("Voice & Audio")
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.VOICE_STUDIO),
                    title = "Voice Studio",
                    subtitle = "TTS engine, installed voices, rate/pitch presets",
                    icon = "🎙",
                    onClick = onOpenVoiceStudio
                )
                SettingsHubRow("Narration Studio", "Narrator/dialogue delivery controls", "🎭", onOpenNarrationStudio)
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.PRONUNCIATION_RULES),
                    title = "Pronunciation",
                    subtitle = "Word replacements before playback/export",
                    icon = "Ab",
                    onClick = onOpenPronunciationRules
                )
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.SLEEP_TIMER),
                    title = "Sleep timer",
                    subtitle = "Pause or stop the current reading after a chosen time",
                    icon = "⏱",
                    onClick = onOpenSleepTimer
                )
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT),
                    title = "Record sound file",
                    subtitle = "Render the current reading into audio, then save or discard",
                    icon = "●",
                    onClick = onStartRecord
                )

                SettingsHubSectionTitle("Appearance")
                SettingsHubRow("Display theme", "Theme packs, colour themes, text size, spacing, section labels", "🎨", onOpenReaderSettings)
                SettingsHubRow("Accessibility", "Reading goal, reduce motion, high contrast, hero & cover styles", "", onOpenAccessibility, iconVector = Icons.Filled.Accessibility)

                SettingsHubSectionTitle("AI & Language")
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.OFFLINE_STUDY_TOOLS),
                    title = "AI & study mode",
                    subtitle = "Installed AI app handoff, flashcards, and quizzes",
                    icon = "AI",
                    onClick = onOpenAiCenter
                )
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.AI_APP_HANDOFF),
                    title = "Ask AI",
                    subtitle = "Choose your assistant and custom selected-text prompt",
                    icon = "AI",
                    onClick = onOpenAskAiSettings
                )

                SettingsHubSectionTitle("Import Defaults")
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.PDF_IMPORT_CONTROLS),
                    title = "PDF and import tools",
                    subtitle = "Defaults, OCR fallback, page cleanup, canvas view preparation",
                    icon = "PDF",
                    onClick = onOpenPdfTools
                )
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.FILE_BROWSER),
                    title = "File browser",
                    subtitle = "Scan approved folders and import supported local files",
                    icon = "DIR",
                    onClick = onOpenFileBrowser
                )
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.EXTRACTED_TEXT_EDITOR),
                    title = "Edit extracted text",
                    subtitle = "Open the extracted text editor and save corrected text",
                    icon = "T+",
                    onClick = onOpenTextEditor
                )

                SettingsHubSectionTitle("Data & Backup")
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.LOCAL_SYNC_PACK),
                    title = "Backup / restore",
                    subtitle = "Export or import a local archive",
                    icon = "⇄",
                    onClick = onOpenBackupRestore
                )
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.LOCAL_SYNC_PACK),
                    title = "Sync Center",
                    subtitle = "Export, share, and safely merge sync packs",
                    icon = "⟳",
                    onClick = onOpenSyncCenter
                )
                FeatureSettingsHubRow(
                    feature = settingsFeature(VeritasFeatureId.READING_LISTS),
                    title = "Reading lists",
                    subtitle = "Create local lists and add saved readings without accounts",
                    icon = "List",
                    onClick = onOpenReadingLists
                )

                SettingsHubRow("Storage", "See what Veritas is using and free up space", "◫", onOpenStorage)

                SettingsHubSectionTitle("Help")
                SettingsHubRow("User manual", "Interactive guide to Veritas Reader features & tips", "📖", onOpenUserManual)
                SettingsHubRow("Tutorial", "Learn Veritas through guided actions", "i", onOpenTutorial)
                }
            }
        }
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
                        "Veritas is using ${formatVeritasBytes(breakdown.totalBytes)} for ${breakdown.documentCount} documents",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Original files: ${formatVeritasBytes(breakdown.originalsBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Covers: ${formatVeritasBytes(breakdown.coversBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Extracted text: ${formatVeritasBytes(breakdown.textBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)

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
                            "Removes the original files of ${candidates.size} fully-read or dormant document${if (candidates.size == 1) "" else "s"} (favorites are never touched). Reading text, progress, notes, and highlights stay — only Original View is lost until a re-import.",
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
    val currentLocale = Locale.getDefault()
    Text(
        title.uppercase(currentLocale),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun FeatureSettingsHubRow(
    feature: ResolvedVeritasFeature,
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit
) {
    SettingsHubRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick,
        enabled = feature.enabled,
        disabledReason = feature.disabledReason
    )
}

@Composable
fun SettingsHubRow(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    disabledReason: String? = null,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                if (iconVector != null) {
                    Icon(iconVector, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                } else {
                    Text(icon, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Black,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    disabledReason ?: subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.35f)
            )
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
                        Button(onClick = { onThemePackChange(packId) }, modifier = modifier) {
                            PackChoiceContent(label = label, packId = packId, selected = true)
                        }
                    } else {
                        OutlinedButton(onClick = { onThemePackChange(packId) }, modifier = modifier) {
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
        Text("Live preview", fontWeight = FontWeight.Black)
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
    // High-contrast themes are surfaced under Accessibility, not here.
    val pickerThemes = VeritasThemeCatalog.themeOptions.filterNot {
        it.first == "dark_high_contrast" || it.first == "white_high_contrast"
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pickerThemes.chunked(2).forEach { rowThemes ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowThemes.forEach { (themeId, label) ->
                    val selected = VeritasThemeCatalog.normalizeThemeId(selectedThemeId) == themeId
                    val previewColors = themePreviewColors(themeId)
                    val buttonModifier = Modifier.weight(1f)
                    if (selected) {
                        Button(onClick = { onThemeChange(themeId) }, modifier = buttonModifier) {
                            ThemeChoiceContent(label = label, previewColors = previewColors, selected = true)
                        }
                    } else {
                        OutlinedButton(onClick = { onThemeChange(themeId) }, modifier = buttonModifier) {
                            ThemeChoiceContent(label = label, previewColors = previewColors, selected = false)
                        }
                    }
                }
                if (rowThemes.size == 1) Spacer(modifier = Modifier.weight(1f))
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
    content: @Composable ColumnScope.() -> Unit
) {
    // decorFitsSystemWindows = false lets the surface draw edge-to-edge behind the
    // status/nav bars so the background matches the rest of the app's full-screen look
    // (content is inset by statusBarsPadding/navigationBarsPadding below).
    Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
    onToggleAutoPlayQueue: () -> Unit
) {
    FullScreenSettingsScaffold(title = "Display & theme", onBack = onDismiss) {
        Text("Theme pack: ${VeritasThemePackCatalog.displayName(settings.themePackId)}", fontWeight = FontWeight.SemiBold)
        VeritasThemePackPicker(
            selectedPackId = settings.themePackId,
            onThemePackChange = onThemePackChange
        )
        Text("Colour theme: ${VeritasThemeCatalog.displayName(settings.themeId)}", fontWeight = FontWeight.SemiBold)
        VeritasThemePicker(
            selectedThemeId = settings.themeId,
            onThemeChange = onThemeChange
        )
        ThemePreviewCard(
            themePackId = settings.themePackId,
            themeId = settings.themeId,
            vibrantHero = settings.vibrantHero
        )
        HorizontalDivider()
        Text("Text size: ${settings.fontSizeSp}sp", fontWeight = FontWeight.SemiBold)
        Slider(
            value = settings.fontSizeSp.toFloat(),
            onValueChange = { onFontSizeChange(it.toInt().coerceIn(14, 28)) },
            valueRange = 14f..28f,
            steps = 13
        )
        Text("Section text spacing: ${settings.sectionSpacingDp}dp", fontWeight = FontWeight.SemiBold)
        Slider(
            value = settings.sectionSpacingDp.toFloat(),
            onValueChange = { onSpacingChange(it.toInt().coerceIn(6, 24)) },
            valueRange = 6f..24f,
            steps = 17
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Vibrant hero card", fontWeight = FontWeight.SemiBold)
                Text("Bold accent-coloured home card instead of the subtle themed one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = settings.vibrantHero, onCheckedChange = { onToggleVibrantHero() })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Auto-play queue", fontWeight = FontWeight.SemiBold)
                Text("Continue into the next queued item.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = settings.autoPlayQueue, onCheckedChange = { onToggleAutoPlayQueue() })
        }
    }
}

@Composable
fun AccessibilitySettingsDialog(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onThemeChange: (String) -> Unit,
    onToggleAdaptiveCover: () -> Unit,
    onToggleSectionNumbers: () -> Unit,
    onGoalMinutesChange: (Int) -> Unit,
    onToggleStreakReminder: () -> Unit,
    onToggleReduceMotion: () -> Unit
) {
    FullScreenSettingsScaffold(title = "Accessibility", onBack = onDismiss) {
        // Reading goal — opt-in. The switch off means "no goal" (minutes = 0), which
        // hides the hero goal bar and the reminder entirely.
        Text("Reading goal", fontWeight = FontWeight.Bold)
        val goalOn = settings.dailyGoalMinutes > 0
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Set a daily reading goal", fontWeight = FontWeight.SemiBold)
                Text("Optional. Shows a progress ring on the home card.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = goalOn,
                onCheckedChange = { on -> onGoalMinutesChange(if (on) 20 else 0) }
            )
        }
        if (goalOn) {
            Text("Daily target: ${settings.dailyGoalMinutes} min", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = settings.dailyGoalMinutes.coerceIn(5, 180).toFloat(),
                onValueChange = { onGoalMinutesChange(it.toInt().coerceIn(5, 180)) },
                valueRange = 5f..180f,
                steps = 34
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Streak reminder", fontWeight = FontWeight.SemiBold)
                    Text("Evening nudge when today's goal isn't met and a streak is at risk.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.streakReminderEnabled, onCheckedChange = { onToggleStreakReminder() })
            }
        }

        HorizontalDivider()
        Text("Motion & contrast", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Reduce motion", fontWeight = FontWeight.SemiBold)
                Text("Skip decorative animations like card entrances and pulses.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = settings.reduceMotion, onCheckedChange = { onToggleReduceMotion() })
        }
        Text("High-contrast theme", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onThemeChange("dark_high_contrast") }, modifier = Modifier.weight(1f)) {
                Text("Contrast dark")
            }
            OutlinedButton(onClick = { onThemeChange("white_high_contrast") }, modifier = Modifier.weight(1f)) {
                Text("Contrast light")
            }
        }

        HorizontalDivider()
        Text("Appearance extras", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Adaptive cover theme", fontWeight = FontWeight.SemiBold)
                Text("Blend active book cover colours into the selected theme.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = settings.adaptiveCover, onCheckedChange = { onToggleAdaptiveCover() })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Show section labels", fontWeight = FontWeight.SemiBold)
                Text("Useful for study, notes, and search jumps.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = settings.showSectionNumbers, onCheckedChange = { onToggleSectionNumbers() })
        }
    }
}

@Composable
fun AskAiSettingsDialog(
    settings: AskAiSettings,
    onSettingsChange: (AskAiSettings) -> Unit,
    onInstallAssistant: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var promptDraft by remember(settings.promptTemplate) { mutableStateOf(settings.promptTemplate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onSettingsChange(settings.copy(promptTemplate = promptDraft))
                    onDismiss()
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Ask AI") },
        text = {
            Column(
                modifier = Modifier.height(520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Choose the assistant Veritas should prefer for selected text. If it is not installed, Veritas opens Play Store.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                aiAssistantOptions.forEach { option ->
                    val selected = settings.assistantId == option.id
                    val installedPackage = installedPackageForOption(context, option)
                    val installed = option.packageName.isBlank() || installedPackage != null
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
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
                        },
                        shape = MaterialTheme.shapes.small,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (selected) "✓" else "AI", fontWeight = FontWeight.Black, modifier = Modifier.width(34.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(option.label, fontWeight = FontWeight.Bold)
                                Text(if (installed) "Ready" else "Install from Play Store", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!installed) TextButton(onClick = { onInstallAssistant(option.packageName) }) { Text("Install") }
                        }
                    }
                }
                OutlinedTextField(
                    value = promptDraft,
                    onValueChange = { promptDraft = it },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    label = { Text("Prompt template") },
                    placeholder = { Text("Use {selection} where the selected text should appear") }
                )
            }
        }
    )
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
    onPresetSelected: (String, Float, Float) -> Unit,
    onAddLanguageVoice: () -> Unit,
    onOpenSystemTtsSettings: () -> Unit,
    onOpenSpeechEdits: () -> Unit,
    onOpenNarrationStudio: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentLocale = Locale.getDefault()
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
    val selectedLanguageTag = when {
        settings.localeTag in languageTags -> settings.localeTag
        languageTags.isNotEmpty() -> languageTags.first()
        else -> settings.localeTag
    }
    val languageVoices = remember(visibleVoices, selectedLanguageTag) {
        if (selectedLanguageTag.isBlank()) visibleVoices else visibleVoices.filter { it.localeTag == selectedLanguageTag }
    }
    val selectedVoice = voices.firstOrNull { it.name == settings.voiceName }

    LaunchedEffect(Unit) {
        onRefreshEngines()
        if (voices.isEmpty()) onLoadVoices()
    }

    FullScreenSettingsScaffold(title = "Voice and language", onBack = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select how to manage voices:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box {
                    TextButton(onClick = { managerMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Use Veritas voice manager", modifier = Modifier.weight(1f))
                        Text("▾")
                    }
                    DropdownMenu(expanded = managerMenuExpanded, onDismissRequest = { managerMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Use Veritas voice manager") },
                            onClick = { managerMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Use Android system default") },
                            onClick = {
                                managerMenuExpanded = false
                                onUseSystemDefault()
                            }
                        )
                    }
                }

                Text("Select a language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Only languages reported by installed Android speech modules are shown.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Box {
                    TextButton(
                        onClick = { languageMenuExpanded = true },
                        enabled = visibleVoices.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(voiceLanguageLabel(selectedLanguageTag, currentLocale), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("▾")
                    }
                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false },
                        modifier = Modifier.width(320.dp).heightIn(max = 360.dp)
                    ) {
                        languageTags.forEach { localeTag ->
                            DropdownMenuItem(
                                text = { Text(voiceLanguageLabel(localeTag, currentLocale), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = {
                                    languageMenuExpanded = false
                                    onLanguageSelected(localeTag)
                                }
                            )
                        }
                    }
                }

                Text("Choose a voice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (selectedVoice != null) {
                    Text(voiceProviderLabel(selectedVoice, currentLocale), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box {
                    TextButton(
                        onClick = { voiceMenuExpanded = true },
                        enabled = languageVoices.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedVoice?.name ?: settings.displayName,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("▾")
                    }
                    DropdownMenu(
                        expanded = voiceMenuExpanded,
                        onDismissRequest = { voiceMenuExpanded = false },
                        modifier = Modifier.width(340.dp).heightIn(max = 420.dp)
                    ) {
                        languageVoices.forEach { voice ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(voice.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            voiceProviderLabel(voice, currentLocale),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                trailingIcon = {
                                    TextButton(onClick = { onPreviewVoice(voice) }) { Text("Play") }
                                },
                                onClick = {
                                    voiceMenuExpanded = false
                                    onVoiceSelected(voice)
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = settings.showNetworkVoices,
                        onCheckedChange = onShowNetworkVoicesChange
                    )
                    Text("Show also voices that need network connection")
                }

                if (loadingVoices) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp))
                        Text("Loading installed speech modules...")
                    }
                }
                if (visibleVoices.isEmpty() && !loadingVoices) {
                    Text(
                        if (voices.any { it.requiresNetwork }) {
                            "Only network voices were reported. Enable network voices above or add an offline voice."
                        } else {
                            "No installed voices were reported by this TTS engine."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    TextButton(onClick = { profileMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Voice profile: ${settings.profileName}", modifier = Modifier.weight(1f))
                        Text("▾")
                    }
                    DropdownMenu(expanded = profileMenuExpanded, onDismissRequest = { profileMenuExpanded = false }) {
                        voicePresets().forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(preset.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${preset.summary} ${"%.2f".format(preset.rate)}x, pitch ${"%.2f".format(preset.pitch)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    profileMenuExpanded = false
                                    onPresetSelected(preset.name, preset.rate, preset.pitch)
                                }
                            )
                        }
                    }
                }

                if (engines.isNotEmpty()) {
                    Box {
                        TextButton(onClick = { engineMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(settings.engineLabel.ifBlank { "System default" }, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("▾")
                        }
                        DropdownMenu(
                            expanded = engineMenuExpanded,
                            onDismissRequest = { engineMenuExpanded = false },
                            modifier = Modifier.width(320.dp).heightIn(max = 360.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("System default") },
                                onClick = {
                                    engineMenuExpanded = false
                                    onUseSystemDefault()
                                }
                            )
                            engines.forEach { engine ->
                                DropdownMenuItem(
                                    text = { Text(engine.label, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        engineMenuExpanded = false
                                        onEngineSelected(engine)
                                        onLoadVoices()
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("USE SELECTED VOICE")
                }
                OutlinedButton(onClick = onAddLanguageVoice, modifier = Modifier.fillMaxWidth()) {
                    Text("ADD A LANGUAGE/VOICE")
                }
                OutlinedButton(onClick = onOpenSystemTtsSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("SYSTEM TTS SETTINGS")
                }
                OutlinedButton(onClick = onOpenSpeechEdits, modifier = Modifier.fillMaxWidth()) {
                    Text("EDIT SPEECH")
                }
                OutlinedButton(onClick = onOpenNarrationStudio, modifier = Modifier.fillMaxWidth()) {
                    Text("VOICE DEFINITION FOR ANNOTATIONS")
                }
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
    onSettingsChange: (NarrationSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sample = sampleText.ifBlank { "\"This is a sample dialogue line,\" she said. The narrator continues with a calmer sentence." }
    val sampleLabel = NarrationAnalyzer.labelFor(sample, settings)
    val sampleRate = NarrationAnalyzer.effectiveRate(1.0f, settings, sample)
    val samplePitch = NarrationAnalyzer.effectivePitch(1.0f, settings, sample)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Narration studio") },
        text = {
            Column(
                modifier = Modifier
                    .height(560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(if (settings.enabled) "🎭 Narration mode on" else "🎭 Narration mode off", fontWeight = FontWeight.Black)
                        Text(
                            "Automatically changes delivery for dialogue-like sentences without needing a separate audiobook file.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable narration mode", fontWeight = FontWeight.SemiBold)
                        Text("Uses slightly different speed/pitch for narrator and dialogue sentences.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = settings.enabled, onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) })
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Detect dialogue", fontWeight = FontWeight.SemiBold)
                        Text("Looks for quoted speech, dash-dialogue, and common speaker tags.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.dialogueDetection,
                        onCheckedChange = { onSettingsChange(settings.copy(dialogueDetection = it)) },
                        enabled = settings.enabled
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show dialogue badges", fontWeight = FontWeight.SemiBold)
                        Text("Marks likely dialogue sentences with 🎭 in the reader.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.showDialogueBadges,
                        onCheckedChange = { onSettingsChange(settings.copy(showDialogueBadges = it)) },
                        enabled = settings.enabled
                    )
                }

                HorizontalDivider()
                Text("Narrator delivery", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Narrator speed: ${"%.2f".format(settings.narratorRateMultiplier)}×", fontWeight = FontWeight.SemiBold)
                Slider(
                    value = settings.narratorRateMultiplier,
                    onValueChange = { onSettingsChange(settings.copy(narratorRateMultiplier = it.coerceIn(0.75f, 1.25f))) },
                    valueRange = 0.75f..1.25f,
                    enabled = settings.enabled
                )
                Text("Narrator pitch: ${"%.2f".format(settings.narratorPitchMultiplier)}", fontWeight = FontWeight.SemiBold)
                Slider(
                    value = settings.narratorPitchMultiplier,
                    onValueChange = { onSettingsChange(settings.copy(narratorPitchMultiplier = it.coerceIn(0.80f, 1.25f))) },
                    valueRange = 0.80f..1.25f,
                    enabled = settings.enabled
                )

                HorizontalDivider()
                Text("Dialogue delivery", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Dialogue speed: ${"%.2f".format(settings.dialogueRateMultiplier)}×", fontWeight = FontWeight.SemiBold)
                Slider(
                    value = settings.dialogueRateMultiplier,
                    onValueChange = { onSettingsChange(settings.copy(dialogueRateMultiplier = it.coerceIn(0.75f, 1.25f))) },
                    valueRange = 0.75f..1.25f,
                    enabled = settings.enabled
                )
                Text("Dialogue pitch: ${"%.2f".format(settings.dialoguePitchMultiplier)}", fontWeight = FontWeight.SemiBold)
                Slider(
                    value = settings.dialoguePitchMultiplier,
                    onValueChange = { onSettingsChange(settings.copy(dialoguePitchMultiplier = it.coerceIn(0.80f, 1.25f))) },
                    valueRange = 0.80f..1.25f,
                    enabled = settings.enabled
                )

                HorizontalDivider()
                Text("Preview classification", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(sample.take(260), maxLines = 5, overflow = TextOverflow.Ellipsis)
                        Text("Detected as: $sampleLabel • effective ${"%.2f".format(sampleRate)}× • pitch ${"%.2f".format(samplePitch)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(
                    "This is a lightweight narration mode. It does not yet assign separate character voices; that would require a later character-mapping system.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
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
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Pronunciation rules") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Replace awkward TTS readings before the sentence is spoken.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = newFind,
                    onValueChange = onNewFindChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Find") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = newReplaceWith,
                    onValueChange = onNewReplaceChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Say instead") },
                    singleLine = true
                )
                Button(onClick = onAddRule, enabled = newFind.isNotBlank()) { Text("Add rule") }
                if (rules.isEmpty()) {
                    Text("No rules yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    rules.take(6).forEach { rule ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${rule.find} → ${rule.replaceWith}", fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onToggleRule(rule) }) {
                                        Text(if (rule.enabled) "Enabled" else "Disabled")
                                    }
                                    TextButton(onClick = { onRemoveRule(rule) }) { Text("Remove") }
                                }
                            }
                        }
                    }
                    if (rules.size > 6) {
                        Text("Showing 6 of ${rules.size} rules.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    )
}

