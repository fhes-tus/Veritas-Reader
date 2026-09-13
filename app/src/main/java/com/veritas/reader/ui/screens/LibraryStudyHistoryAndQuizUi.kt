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
import androidx.compose.material.icons.outlined.AutoAwesome
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

import androidx.compose.foundation.lazy.LazyListState

import androidx.compose.foundation.lazy.LazyListScope

internal fun LazyListScope.studyQuizSection(
    quizzes: List<QuizSet>,
    onOpenAiStudyTools: () -> Unit,
    onShowPasteQuiz: () -> Unit,
    onShowQuizLabMetrics: () -> Unit,
    onPlayQuiz: (QuizSet) -> Unit,
    onDeleteQuiz: (QuizSet) -> Unit,
    onGoToLibrary: () -> Unit
) {
                    val quizzes = quizzes
                    if (quizzes.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.EditNote,
                                title = "No quizzes yet",
                                description = "Take a quiz to test your memory and retention. Create a quiz directly with AI or paste a quiz from ChatGPT, Claude, or Gemini.",
                                onGoToLibrary = { onGoToLibrary() },
                                primaryActionLabel = "Open AI Hub",
                                onPrimaryAction = onOpenAiStudyTools
                            )
                        }
                        item {
                            Button(
                                onClick = { onShowPasteQuiz() },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = VeritasPackStyle.chipShape()
                            ) {
                                Text("Paste Quiz → Start Learning")
                            }
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Saved Quizzes (${quizzes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    OutlinedButton(
                                        onClick = { onShowQuizLabMetrics() },
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("Quiz Lab 📊", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                FilledTonalButton(
                                    onClick = { onShowPasteQuiz() },
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Quiz", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        items(quizzes, key = { it.id }) { quiz ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = VeritasPackStyle.compactShape(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())),
                                border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("📝", fontSize = 20.sp)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(quiz.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("${quiz.totalQuestions} Questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (quiz.bestScore >= 0) {
                                                val calibratedBest = if (quiz.bestScore > quiz.totalQuestions && quiz.totalQuestions > 0) {
                                                    ((quiz.bestScore.toFloat() / 100f) * quiz.totalQuestions.toFloat()).roundToInt().coerceIn(0, quiz.totalQuestions)
                                                } else {
                                                    quiz.bestScore.coerceIn(0, quiz.totalQuestions)
                                                }
                                                val isMastered = calibratedBest == quiz.totalQuestions
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isMastered) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                    modifier = Modifier.padding(start = 4.dp)
                                                ) {
                                                    Text(
                                                        text = if (isMastered) "Mastered 🏆" else "Best: $calibratedBest/${quiz.totalQuestions}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isMastered) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = { onPlayQuiz(quiz) },
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("Play", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(onClick = { onDeleteQuiz(quiz) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete quiz", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

}

internal fun LazyListScope.studyHistorySection(
    readingHistory: List<ReadingHistoryEntry>,
    documents: List<SavedDocument>,
    onClearReadingHistory: () -> Unit,
    onRemoveReadingHistoryEntry: (String) -> Unit,
    onOpenDocumentAt: (SavedDocument, Int) -> Unit,
    onGoToLibrary: () -> Unit,
    onImportFile: () -> Unit
) {
                    val readingHistory = readingHistory
                    if (readingHistory.isEmpty()) {
                        item {
                            StudyEmptyState(
                                icon = Icons.Outlined.History,
                                title = "No reading history yet",
                                description = "Documents you read will show up here.",
                                onGoToLibrary = { onGoToLibrary() },
                                onImportFile = onImportFile
                            )
                        }
                    } else {
                        item {
                            var confirmClearHistory by remember { mutableStateOf(false) }
                            if (confirmClearHistory) {
                                AlertDialog(
                                    onDismissRequest = { confirmClearHistory = false },
                                    title = { Text(stringResource(R.string.clear_history_title)) },
                                    text = { Text(stringResource(R.string.clear_history_message)) },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            confirmClearHistory = false
                                            onClearReadingHistory()
                                        }) { Text(stringResource(R.string.action_clear), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { confirmClearHistory = false }) {
                                            Text(stringResource(R.string.action_cancel))
                                        }
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recent history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { confirmClearHistory = true }) {
                                    Text("Clear all", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        readingHistory.forEach { historyEntry ->
                            val doc = documents.firstOrNull { it.id == historyEntry.documentId }
                            item(key = "history-entry-${historyEntry.documentId}-${historyEntry.openedAt}") {
                                val isRemoved = doc == null
                                val progress = if (historyEntry.chunkCount > 0)
                                    (historyEntry.currentIndex.toFloat() / historyEntry.chunkCount).coerceIn(0f, 1f)
                                else 0f

                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { newVal ->
                                        if (newVal == SwipeToDismissBoxValue.EndToStart) {
                                            onRemoveReadingHistoryEntry(historyEntry.documentId)
                                            true
                                        } else false
                                    }
                                )

                                // Buzz the moment the swipe passes the point of no return, so the
                                // commit is felt before the finger lifts — this delete has no undo.
                                val swipeHaptics = rememberVeritasHaptics()
                                LaunchedEffect(dismissState.targetValue) {
                                    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) swipeHaptics.threshold()
                                }

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 4.dp)
                                                .clip(VeritasPackStyle.compactShape())
                                                .background(MaterialTheme.colorScheme.errorContainer),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remove from history",
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(end = 20.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.animateItem()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (!isRemoved) {
                                                    Modifier.clickable {
                                                        onOpenDocumentAt(doc!!, historyEntry.currentIndex)
                                                    }
                                                } else Modifier
                                            ),
                                        shape = VeritasPackStyle.compactShape(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isRemoved) {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            } else {
                                                MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
                                            }
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isRemoved) {
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            }
                                        )
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Text(
                                                        text = historyEntry.title,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isRemoved) {
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        },
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = if (isRemoved) "Removed" else "Sentence ${historyEntry.currentIndex + 1} of ${historyEntry.chunkCount}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (isRemoved) {
                                                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        }
                                                    )
                                                    val locale = LocalConfiguration.current.locales[0]
                                                    val openedTime = remember(historyEntry.openedAt, locale) {
                                                        SimpleDateFormat("dd MMM, HH:mm", locale).format(Date(historyEntry.openedAt))
                                                    }
                                                    Text(
                                                        text = "Opened $openedTime",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isRemoved) 0.3f else 0.5f)
                                                    )
                                                }

                                                if (isRemoved) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "Removed",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Filled.ChevronRight,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }

                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                                color = if (isRemoved) {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

}
