package com.veritas.reader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.veritas.reader.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderToolsSheet(
    onDismiss: () -> Unit,
    onOpenVoiceStudio: () -> Unit,
    onOpenNarrationStudio: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenAskAi: () -> Unit,
    
    // Other settings
    showSearch: Boolean,
    showBookmarks: Boolean,
    hasCanvas: Boolean,
    noteCount: Int,
    isQueued: Boolean,
    queueCount: Int,
    askAiSettings: AskAiSettings,
    aiAssistantOptions: List<AiAssistantOption>,
    
    onToggleSearch: () -> Unit,
    onToggleBookmarks: () -> Unit,
    onOpenDocumentNotes: () -> Unit,
    onOpenCanvas: () -> Unit,
    onOpenStudyTools: () -> Unit,
    onOpenTranslationTools: () -> Unit,
    readingListCount: Int,
    activeDocumentReadingListCount: Int,
    onOpenReadingLists: () -> Unit,
    onOpenReadingHistory: () -> Unit,
    onSelectAskAiAssistant: (AiAssistantOption, String) -> Unit,
    onOpenTextEditor: () -> Unit,
    onStartRecord: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenPronunciationRules: () -> Unit,
    onExportAudio: () -> Unit,
    onExportStudyGuidePdf: () -> Unit = {},
    onToggleQueue: () -> Unit,
    onPlayQueue: () -> Unit,
    
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAllSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showAiChooser by remember { mutableStateOf(false) }

    val readerFeatures = remember(queueCount) {
        VeritasFeatureRegistry.resolve(
            VeritasFeatureSurface.READER_OVERFLOW,
            VeritasFeatureContext(
                hasActiveDocument = true,
                hasSavedDocument = true,
                queueCount = queueCount
            )
        ).associateBy { it.definition.id }
    }

    fun readerFeature(id: VeritasFeatureId): ResolvedVeritasFeature =
        readerFeatures.getValue(id)

    fun choose(action: () -> Unit) {
        onDismiss()
        action()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        if (!showAllSettings) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reader Tools",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToolItem(icon = Icons.Outlined.RecordVoiceOver, label = "Voice", onClick = { choose(onOpenVoiceStudio) })
                    ToolItem(icon = Icons.Outlined.TheaterComedy, label = "Narration", onClick = { choose(onOpenNarrationStudio) })
                    ToolItem(icon = Icons.Outlined.Timer, label = "Timer", onClick = { choose(onOpenSleepTimer) })
                }
                Spacer(modifier = Modifier.size(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToolItem(icon = Icons.Outlined.AutoAwesome, label = "Ask AI", onClick = { choose(onOpenAskAi) })
                    ToolItem(icon = Icons.Outlined.Settings, label = "All Settings", onClick = { showAllSettings = true })
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAllSettings = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "All Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // READ SECTION
                Text(
                    text = "Read",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                SettingsItem(
                    title = if (showSearch) "Hide search" else "Search document",
                    leadingIcon = Icons.Outlined.Search,
                    onClick = { choose(onToggleSearch) }
                )

                SettingsItem(
                    title = "Original View",
                    leadingIcon = Icons.Outlined.Description,
                    enabled = hasCanvas,
                    onClick = { choose(onOpenCanvas) }
                )

                val readingListsFeature = readerFeature(VeritasFeatureId.READING_LISTS)
                SettingsItem(
                    title = "Reading lists ($activeDocumentReadingListCount/$readingListCount)",
                    leadingIcon = Icons.Outlined.CollectionsBookmark,
                    enabled = readingListsFeature.enabled,
                    subtitle = if (!readingListsFeature.enabled) readingListsFeature.disabledReason else null,
                    onClick = { choose(onOpenReadingLists) }
                )

                SettingsItem(
                    title = if (isQueued) "Remove from Queue" else "Add to Queue",
                    leadingIcon = if (isQueued) Icons.AutoMirrored.Filled.PlaylistAddCheck else Icons.AutoMirrored.Filled.PlaylistAdd,
                    onClick = { choose(onToggleQueue) }
                )

                SettingsItem(
                    title = "Play Queue ($queueCount)",
                    leadingIcon = Icons.AutoMirrored.Filled.PlaylistPlay,
                    enabled = queueCount > 0,
                    onClick = { choose(onPlayQueue) }
                )

                val readingHistoryFeature = readerFeature(VeritasFeatureId.READING_HISTORY)
                SettingsItem(
                    title = "Reading history",
                    leadingIcon = Icons.Outlined.History,
                    enabled = readingHistoryFeature.enabled,
                    subtitle = if (!readingHistoryFeature.enabled) readingHistoryFeature.disabledReason else null,
                    onClick = { choose(onOpenReadingHistory) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // NOTES AND BOOKMARKS SECTION
                Text(
                    text = "Notes and bookmarks",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                SettingsItem(
                    title = if (showBookmarks) "Hide bookmarks" else "Bookmarks",
                    leadingIcon = Icons.Outlined.Bookmark,
                    onClick = { choose(onToggleBookmarks) }
                )

                val documentNotesFeature = readerFeature(VeritasFeatureId.BOOKMARKS_AND_NOTES)
                SettingsItem(
                    title = "Booknotes${if (noteCount > 0) " • $noteCount sentence${if (noteCount == 1) "" else "s"}" else ""}",
                    leadingIcon = Icons.Outlined.EditNote,
                    enabled = documentNotesFeature.enabled,
                    subtitle = if (!documentNotesFeature.enabled) documentNotesFeature.disabledReason else null,
                    onClick = { choose(onOpenDocumentNotes) }
                )

                SettingsItem(
                    title = "Export Study Guide PDF",
                    leadingIcon = Icons.AutoMirrored.Outlined.MenuBook,
                    subtitle = "Styled A4 PDF with booknotes & highlights",
                    onClick = { choose(onExportStudyGuidePdf) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // STUDY SECTION
                Text(
                    text = "Study",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                SettingsItem(
                    title = "AI Assistant: ${askAiSettings.assistantLabel}",
                    leadingIcon = aiAssistantIcon(askAiSettings.assistantId),
                    onClick = { showAiChooser = !showAiChooser }
                )

                if (showAiChooser) {
                    aiAssistantOptions.filter { it.packageName.isNotBlank() }.forEach { option ->
                        val installedPackage = installedPackageForOption(context, option)
                        val isSelected = askAiSettings.assistantId == option.id
                        SettingsSubItem(
                            title = "${if (isSelected) "✓ " else ""}${option.label}${if (installedPackage == null) " • install" else ""}",
                            leadingIcon = aiAssistantIcon(option.id),
                            onClick = {
                                if (installedPackage != null) {
                                    choose { onSelectAskAiAssistant(option, installedPackage) }
                                } else {
                                    openPlayStoreForPackage(context, option.packageName)
                                }
                            }
                        )
                    }
                }

                val studyToolsFeature = readerFeature(VeritasFeatureId.OFFLINE_STUDY_TOOLS)
                SettingsItem(
                    title = "AI Study tools",
                    leadingIcon = Icons.Outlined.School,
                    enabled = studyToolsFeature.enabled,
                    subtitle = if (!studyToolsFeature.enabled) studyToolsFeature.disabledReason else null,
                    onClick = { choose(onOpenStudyTools) }
                )

                val translationFeature = readerFeature(VeritasFeatureId.TRANSLATION_HANDOFF)
                SettingsItem(
                    title = "Translation handoff",
                    leadingIcon = Icons.Outlined.Translate,
                    enabled = translationFeature.enabled,
                    subtitle = if (!translationFeature.enabled) translationFeature.disabledReason else null,
                    onClick = { choose(onOpenTranslationTools) }
                )

                val textEditorFeature = readerFeature(VeritasFeatureId.EXTRACTED_TEXT_EDITOR)
                SettingsItem(
                    title = "Edit extracted text",
                    leadingIcon = Icons.Outlined.EditNote,
                    enabled = textEditorFeature.enabled,
                    subtitle = if (!textEditorFeature.enabled) textEditorFeature.disabledReason else null,
                    onClick = { choose(onOpenTextEditor) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // VOICE AND SETTINGS SECTION
                Text(
                    text = "Voice and settings",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val pronunciationFeature = readerFeature(VeritasFeatureId.PRONUNCIATION_RULES)
                SettingsItem(
                    title = "Pronunciation rules",
                    leadingIcon = Icons.Outlined.Spellcheck,
                    enabled = pronunciationFeature.enabled,
                    subtitle = if (!pronunciationFeature.enabled) pronunciationFeature.disabledReason else null,
                    onClick = { choose(onOpenPronunciationRules) }
                )

                SettingsItem(
                    title = "Reader appearance",
                    leadingIcon = Icons.Outlined.Palette,
                    onClick = { choose(onOpenReaderSettings) }
                )

                val recordSoundFeature = readerFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT)
                SettingsItem(
                    title = "Record sound file",
                    leadingIcon = Icons.Outlined.GraphicEq,
                    enabled = recordSoundFeature.enabled,
                    subtitle = if (!recordSoundFeature.enabled) recordSoundFeature.disabledReason else null,
                    onClick = { choose(onStartRecord) }
                )

                val exportAudioFeature = readerFeature(VeritasFeatureId.QUEUE_AUDIO_EXPORT)
                SettingsItem(
                    title = "Export audio",
                    leadingIcon = Icons.Outlined.FileDownload,
                    enabled = exportAudioFeature.enabled,
                    subtitle = if (!exportAudioFeature.enabled) exportAudioFeature.disabledReason else null,
                    onClick = { choose(onExportAudio) }
                )
            }
        }
    }
}

@Composable
private fun ToolItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .size(80.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(14.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSubItem(
    title: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(start = 40.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
