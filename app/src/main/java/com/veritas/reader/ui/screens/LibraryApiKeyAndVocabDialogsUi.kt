package com.veritas.reader.ui.screens

import android.graphics.BitmapFactory
import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.scale
import java.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.veritas.reader.VeritasPackStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.animation.animateContentSize
import java.util.UUID
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import com.veritas.reader.ui.rememberVeritasHaptics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.widget.Toast
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.core.content.edit
import androidx.compose.ui.layout.onGloballyPositioned
import com.veritas.reader.ui.OnboardingController
import com.veritas.reader.ui.OnboardingStep
import com.veritas.reader.*
import com.veritas.reader.ui.ReaderUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.ui.res.stringResource
import com.veritas.reader.R
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb



@Composable
internal fun GeminiApiKeyDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedProvider by remember { mutableStateOf(GeminiStudyService.getProvider(context)) }
    var keyDraft by remember { mutableStateOf(GeminiStudyService.getApiKey(context)) }
    var modelDraft by remember { mutableStateOf(GeminiStudyService.getModel(context).ifBlank { selectedProvider.defaultModel }) }
    var endpointDraft by remember { mutableStateOf(GeminiStudyService.getCustomEndpoint(context).ifBlank { selectedProvider.defaultEndpoint }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("AI Provider & Key Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Connect any AI model to generate flashcards, quizzes, and study summaries directly in Veritas Reader.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Model",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (selectedProvider.recommendedModels.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedProvider.recommendedModels.forEach { recModel ->
                            FilterChip(
                                selected = modelDraft.trim() == recModel,
                                onClick = { modelDraft = recModel },
                                leadingIcon = if (modelDraft.trim() == recModel) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else null,
                                label = { Text(recModel, fontSize = 11.sp) },
                                shape = RoundedCornerShape(50)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = modelDraft,
                    onValueChange = { modelDraft = it },
                    label = { Text("Model Name / ID") },
                    placeholder = { Text(selectedProvider.defaultModel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Provider",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        selectedProvider.label,
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
                    AiProvider.entries.forEach { provider ->
                        val icon = when (provider) {
                            AiProvider.GEMINI -> IconGemini
                            AiProvider.OPENAI -> IconChatGPT
                            AiProvider.ANTHROPIC -> IconClaude
                            AiProvider.GROQ -> Icons.Outlined.Bolt
                            AiProvider.OPENROUTER -> Icons.Filled.Language
                            AiProvider.CUSTOM -> Icons.Filled.Settings
                        }
                        FilterChip(
                            selected = selectedProvider == provider,
                            onClick = {
                                selectedProvider = provider
                                modelDraft = provider.defaultModel
                                endpointDraft = provider.defaultEndpoint
                            },
                            label = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = provider.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                OutlinedTextField(
                    value = keyDraft,
                    onValueChange = { keyDraft = it },
                    label = { Text("${selectedProvider.label} API Key") },
                    placeholder = { Text("Add API key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (selectedProvider == AiProvider.CUSTOM || selectedProvider == AiProvider.OPENROUTER) {
                    OutlinedTextField(
                        value = endpointDraft,
                        onValueChange = { endpointDraft = it },
                        label = { Text("API Endpoint URL") },
                        placeholder = { Text(selectedProvider.defaultEndpoint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text(
                    when (selectedProvider) {
                        AiProvider.GEMINI -> "Free tier available at aistudio.google.com."
                        AiProvider.OPENAI -> "Obtain keys from platform.openai.com."
                        AiProvider.ANTHROPIC -> "Obtain keys from console.anthropic.com."
                        AiProvider.GROQ -> "Ultra-fast inference at console.groq.com."
                        AiProvider.OPENROUTER -> "Access hundreds of models at openrouter.ai."
                        AiProvider.CUSTOM -> "Any standard OpenAI-compatible API base URL."
                    } + " Keys are saved locally on your device only.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    GeminiStudyService.saveProvider(context, selectedProvider)
                    GeminiStudyService.saveApiKey(context, keyDraft.trim())
                    GeminiStudyService.saveModel(context, modelDraft.trim())
                    if (selectedProvider == AiProvider.CUSTOM || selectedProvider == AiProvider.OPENROUTER) {
                        GeminiStudyService.saveCustomEndpoint(context, endpointDraft.trim())
                    }
                    Toast.makeText(
                        context,
                        if (keyDraft.isNotBlank()) "${selectedProvider.label} API key saved!" else "API key cleared",
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
internal fun VocabularyEntryRow(
    entry: VocabularyEntry,
    document: SavedDocument,
    onOpenDocumentAt: (SavedDocument, Int) -> Unit,
    onRemoveVocabularyWord: (String, String) -> Unit
) {
    val context = LocalContext.current
    var itemMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Vocabulary Word?") },
            text = { Text("Are you sure you want to remove \"${entry.word}\" from your vocabulary list?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onRemoveVocabularyWord(document.id, entry.word)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.word, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                if (!entry.pronunciation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.pronunciation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            Text(entry.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!entry.contextSentence.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "\"${entry.contextSentence}\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = entry.source,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenDocumentAt(document, entry.sentenceIndex) }
            )
        }

        Box {
            IconButton(onClick = { itemMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Entry Actions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = itemMenuExpanded,
                onDismissRequest = { itemMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Share as text") },
                    onClick = {
                        itemMenuExpanded = false
                        shareVocabularyAsWords(
                            context = context,
                            bookTitle = document.title,
                            entry = entry
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Share as image") },
                    onClick = {
                        itemMenuExpanded = false
                        shareVocabularyAsImage(
                            context = context,
                            bookTitle = document.title,
                            authorName = "",
                            entry = entry
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Image, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Copy") },
                    onClick = {
                        itemMenuExpanded = false
                        val vocabText = buildString {
                            append(entry.word)
                            if (!entry.pronunciation.isNullOrBlank()) append(" (${entry.pronunciation})")
                            append("\n").append(entry.explanation)
                            if (!entry.contextSentence.isNullOrBlank()) append("\n\"${entry.contextSentence}\"")
                        }
                        copyTextToClipboard(context, "Vocabulary Word", vocabText)
                    },
                    leadingIcon = { Icon(Icons.Filled.ContentPaste, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        itemMenuExpanded = false
                        showDeleteConfirm = true
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
            }
        }
    }
}


