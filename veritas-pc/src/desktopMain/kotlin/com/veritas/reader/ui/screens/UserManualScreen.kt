package com.veritas.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class ManualChapter(
    val title: String,
    val emoji: String,
    val subtitle: String,
    val content: String,
    val ctaText: String? = null,
    val ctaAction: String? = null,
    val flowSteps: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManualDialog(
    onDismiss: () -> Unit,
    onNavigateToSetting: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val allChapters = remember {
        listOf(
            ManualChapter(
                title = "Welcome to Veritas",
                emoji = "🚀",
                subtitle = "Getting started and core philosophy",
                content = "Veritas Reader is a high-performance reading and audio learning application designed to maximize comprehension and retention. By combining advanced Text-to-Speech (TTS), distraction-free text presentation, and integrated study tools, Veritas transforms static texts into dynamic learning experiences.",
                ctaText = "Configure Appearance",
                ctaAction = "reader_settings"
            ),
            ManualChapter(
                title = "Importing Documents",
                emoji = "📥",
                subtitle = "Adding content to your library",
                content = "Veritas supports importing EPUB, PDF, DOCX, PPTX, TXT files, and Web articles. You can import files from local storage, Google Drive, or shared system links. Web articles are scraped automatically to extract clean content without ads or popups.",
                flowSteps = listOf(
                    "Open File Browser in the app",
                    "Scan directories or tap '+' to import files",
                    "Select extraction options (like OCR for scanned PDFs)"
                ),
                ctaText = "Configure PDF Import",
                ctaAction = "pdf_tools"
            ),
            ManualChapter(
                title = "Listening & Controls",
                emoji = "🎧",
                subtitle = "Fine-tuned TTS narration",
                content = "Control playback using the inline player or system notification controls. Adjust narration speed (rate) and pitch. Use the sleep timer to automatically pause playback after a set duration, such as when reading before sleep.",
                ctaText = "Configure Sleep Timer",
                ctaAction = "sleep_timer"
            ),
            ManualChapter(
                title = "PDF Mode",
                emoji = "📄",
                subtitle = "Reading PDFs efficiently",
                content = "Veritas provides a specialized PDF extractor. It parses the document layout, reflows text for clean reading, and utilizes built-in optical character recognition (OCR) to convert scanned images or non-selectable PDF pages into editable, readable text.",
                ctaText = "PDF Import Tools",
                ctaAction = "pdf_tools"
            ),
            ManualChapter(
                title = "Annotations",
                emoji = "📌",
                subtitle = "Bookmarks, highlights, and notes",
                content = "Highlight key sentences and add notes. Bookmarks help you quickly jump to specific parts of a book, while notes store your thoughts and insights directly attached to sentences. Pinned notes can even be displayed on your device home screen via widgets.",
                ctaText = "Configure Pronunciation",
                ctaAction = "pronunciation"
            ),
            ManualChapter(
                title = "Study Tab",
                emoji = "📚",
                subtitle = "Study tools, history, and search",
                content = "The Study tab organizes your vocabulary, annotations, history, and reading lists. Use lists to organize documents into folders. Check your reading history to trace past activity and resume reading immediately.",
                ctaText = "View Reading History",
                ctaAction = "history"
            ),
            ManualChapter(
                title = "Reading Insights",
                emoji = "📈",
                subtitle = "Streaks and reading statistics",
                content = "Veritas tracks your daily reading sessions. The weekly reading time graph displays your usage for the current week, helping you maintain a consistent reading streak and meet your personal goals.",
                ctaText = "Manage Reading Lists",
                ctaAction = "reading_lists"
            ),
            ManualChapter(
                title = "AI Features",
                emoji = "🤖",
                subtitle = "AI-powered summaries and vocabulary help",
                content = "Veritas integrates with local AI models and assistants to summarize paragraphs, clarify difficult terms, and translate foreign phrases. You can customize prompt templates for your specific study routines.",
                ctaText = "Open AI Center",
                ctaAction = "ai_center"
            ),
            ManualChapter(
                title = "Glance Widgets",
                emoji = "📱",
                subtitle = "Home screen widgets suite",
                content = "Add Veritas widgets to your home screen to check your reading progress, capture quick notes, control playback, view pinned notes, or view your study stats dashboard at a glance without opening the app.",
                ctaText = "Manage Voice Settings",
                ctaAction = "voice_studio"
            ),
            ManualChapter(
                title = "Customisation",
                emoji = "🎨",
                subtitle = "Theme packs and personalization",
                content = "Make Veritas look and sound exactly how you want. Choose from built-in theme packs, configure custom fonts and line heights, and set up custom pronunciation rules to correct TTS mispronunciations of names or terminology.",
                ctaText = "Configure Reader Appearance",
                ctaAction = "reader_settings"
            )
        )
    }

    val filteredChapters = remember(searchQuery, allChapters) {
        if (searchQuery.isBlank()) {
            allChapters
        } else {
            allChapters.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.subtitle.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Manual",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search chapters...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chapters list
                if (filteredChapters.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching chapters found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredChapters) { chapter ->
                            ChapterCard(
                                chapter = chapter,
                                onNavigateToSetting = onNavigateToSetting
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterCard(
    chapter: ManualChapter,
    onNavigateToSetting: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chapter.emoji,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = chapter.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            Text(
                text = chapter.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            // Flow steps if present
            if (chapter.flowSteps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Workflow",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    chapter.flowSteps.forEachIndexed { index, step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(
                                text = "${index + 1}. ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action button if present
            if (chapter.ctaText != null && chapter.ctaAction != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { onNavigateToSetting(chapter.ctaAction) },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = chapter.ctaText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
