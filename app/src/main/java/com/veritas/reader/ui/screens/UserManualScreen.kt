package com.veritas.reader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.veritas.reader.R

data class ManualChapter(
    val title: String,
    val emoji: String,
    val subtitle: String,
    val content: String,
    val ctaText: String? = null,
    val ctaAction: String? = null,
    val flowSteps: List<String> = emptyList(),
    val imageResId: Int? = null
)

data class ManualSection(
    val title: String,
    val emoji: String,
    val description: String,
    val chapters: List<ManualChapter>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManualDialog(
    onDismiss: () -> Unit,
    onNavigateToSetting: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val allSections = remember {
        listOf(
            ManualSection(
                title = "Library & Files",
                emoji = "📚",
                description = "Manage your books, documents, and web imports",
                chapters = listOf(
                    ManualChapter(
                        title = "Library Dashboard",
                        emoji = "📚",
                        subtitle = "Central document library overview",
                        content = "The Library dashboard is your starting point in Veritas. It compiles all your imported books, documents, and web articles. From here, you can view your reading progress per file, see completion status, filter your lists, and tap any item to start reading instantly.",
                        ctaText = "Manage Reading Lists",
                        ctaAction = "reading_lists",
                        flowSteps = listOf(
                            "Launch Veritas to view your Library",
                            "Filter or sort your list using the top toolbar",
                            "Tap on a book card to open the reader"
                        ),
                        imageResId = R.drawable.manual_library_main
                    ),
                    ManualChapter(
                        title = "Document Actions & Metadata",
                        emoji = "⚙️",
                        subtitle = "Edit document metadata and options",
                        content = "Access control options for individual documents using the three-dot overflow button on any book card. From this menu, you can edit titles, modify author names, assign tags, add documents to lists, reset reading progress, or delete files from storage.",
                        ctaText = "Document Options Tip",
                        ctaAction = "library_options",
                        flowSteps = listOf(
                            "Find the document in your Library list",
                            "Tap the three-dot options menu icon",
                            "Choose an action (Rename, Categorize, or Delete)"
                        ),
                        imageResId = R.drawable.manual_library_item_menu
                    ),
                    ManualChapter(
                        title = "Batch Organization",
                        emoji = "🗂️",
                        subtitle = "Manage multiple documents together",
                        content = "Organize a large reading list efficiently using batch mode. By long-pressing any book card, you activate multi-select mode. This allows you to select multiple items to bulk delete, assign categories, or append to folders at once.",
                        ctaText = "Bulk Edit Tip",
                        ctaAction = "bulk_edit",
                        flowSteps = listOf(
                            "Long-press any document card in the Library",
                            "Tap other cards to select multiple items",
                            "Select a bulk action icon from the top bar"
                        ),
                        imageResId = R.drawable.manual_library_batch
                    ),
                    ManualChapter(
                        title = "Adding Content",
                        emoji = "➕",
                        subtitle = "Quick-add drawer options",
                        content = "Tap the floating '+' action button to expand the content addition options. You can choose to import local document files, scrape online articles by pasting a web link, or open a clean canvas in the text editor to write scratch notes.",
                        ctaText = "Open File Browser",
                        ctaAction = "file_browser",
                        flowSteps = listOf(
                            "Tap the '+' floating button in the Library",
                            "Select file import, web URL, or scratch text",
                            "Confirm the selection to add it to your library"
                        ),
                        imageResId = R.drawable.manual_add_sheet
                    ),
                    ManualChapter(
                        title = "Integrated File Browser",
                        emoji = "📂",
                        subtitle = "Scan and import device storage files",
                        content = "Browse your device's directories locally with the integrated file navigator. It automatically highlights and filters compatible document formats such as EPUB, PDF, DOCX, PPTX, and TXT, making imports quick and easy.",
                        ctaText = "Start File Import",
                        ctaAction = "file_browser",
                        flowSteps = listOf(
                            "Choose 'Local File Browser' from the '+' sheet",
                            "Navigate through folders to find your documents",
                            "Tap a file to copy and register it in the Library"
                        ),
                        imageResId = R.drawable.manual_file_browser
                    ),
                    ManualChapter(
                        title = "File Browser Custom Filters",
                        emoji = "🔍",
                        subtitle = "Sort and filter local storage files",
                        content = "Refine lists in the local browser using the options menu. You can sort files by size, date, or name, and toggle the visibility of hidden system folders or files to quickly locate specific digital books or textbooks.",
                        ctaText = "File Sort Tip",
                        ctaAction = "file_browser_filters",
                        flowSteps = listOf(
                            "Open the local file browser screen",
                            "Tap the three-dot menu at the top-right",
                            "Apply sorting rules or filter constraints"
                        ),
                        imageResId = R.drawable.manual_file_browser_menu
                    ),
                    ManualChapter(
                        title = "PDF & Text Extraction Settings",
                        emoji = "📄",
                        subtitle = "Manage layout conversion settings",
                        content = "Fine-tune how documents are parsed during import. Tweak image OCR recognition thresholds, set default character encodings for TXT files, and enable automatic page header or footer cleanup to strip out page numbers and running titles.",
                        ctaText = "Configure PDF Import",
                        ctaAction = "pdf_tools",
                        flowSteps = listOf(
                            "Access PDF Tools from Settings or Import",
                            "Adjust cleanup toggles and OCR settings",
                            "Save preferences to apply to future imports"
                        ),
                        imageResId = R.drawable.manual_import_settings
                    )
                )
            ),
            ManualSection(
                title = "Reader & Display",
                emoji = "📖",
                description = "Customize font, display modes, and PDF layout",
                chapters = listOf(
                    ManualChapter(
                        title = "Reader Screen Text Mode",
                        emoji = "📖",
                        subtitle = "Reflowed layout reading canvas",
                        content = "Open any book in Text Mode to view clean, reflowed paragraphs. Fixed page borders and margins are removed, allowing the text to fit your screen size perfectly. Text matches your theme colors, and double-tapping acts as a shortcut for definitions.",
                        ctaText = "Select Text Tip",
                        ctaAction = "text_selection",
                        flowSteps = listOf(
                            "Open a document card from your Library",
                            "Swipe or scroll to navigate between pages",
                            "Double-tap any word to select it"
                        ),
                        imageResId = R.drawable.manual_reader_screen
                    ),
                    ManualChapter(
                        title = "Original PDF Layout Mode",
                        emoji = "📄",
                        subtitle = "Read fixed-page PDF documents",
                        content = "Switch to Original Mode to view documents exactly as they were formatted. This is ideal for textbooks, multi-column research papers, and books with complex layouts. You can zoom, pan, and tap sentences to trigger speech narration or sync coordinates.",
                        ctaText = "PDF Tools Settings",
                        ctaAction = "pdf_tools",
                        flowSteps = listOf(
                            "Open a PDF document in the reader",
                            "Tap 'ORIGINAL' tab in the top bar",
                            "View pages with full original styling and zoom support"
                        ),
                        imageResId = R.drawable.manual_reader_original
                    ),
                    ManualChapter(
                        title = "Reader Presentation Modes",
                        emoji = "🔄",
                        subtitle = "Toggle TEXT, LISTEN, and ORIGINAL views",
                        content = "Maximize your focus by choosing the ideal presentation mode. 'TEXT' extracts and reflows content for clean reading, 'LISTEN' focuses on audio playback controls, and 'ORIGINAL' displays the document's native fixed format page layout.",
                        ctaText = "Reader Settings",
                        ctaAction = "reader_settings",
                        flowSteps = listOf(
                            "While reading a book, locate the mode selector tabs",
                            "Tap 'TEXT' for clean reflow or 'LISTEN' for audio layout",
                            "Switch back to 'ORIGINAL' for original formatting"
                        ),
                        imageResId = R.drawable.manual_reader_tabs
                    ),
                    ManualChapter(
                        title = "Typography & Theme Controls",
                        emoji = "🎨",
                        subtitle = "Personalize fonts, sizes, and layout",
                        content = "Adjust the reader interface to fit your reading preferences. You can switch background colors (Light, Dark, Sepia, or glassmorphic gradients), customize fonts (including Lexend and OpenDyslexic), and set custom line spacing or page margins.",
                        ctaText = "Configure Appearance",
                        ctaAction = "reader_settings",
                        flowSteps = listOf(
                            "Tweak spacing or font style while reading",
                            "Tap the typography gear icon in the reader bar",
                            "Select a combination that maximizes your comfort"
                        ),
                        imageResId = R.drawable.manual_reader_settings
                    ),
                    ManualChapter(
                        title = "Reader Tools Overflow",
                        emoji = "🛠️",
                        subtitle = "Secondary actions inside the reader",
                        content = "Tap the options overflow in the reader to access extra tools. This menu houses quick bookmark creation, a word search inside the active book, reader progress resets, and options to export your highlighted notes to external Markdown files.",
                        ctaText = "Reader Options Tip",
                        ctaAction = "reader_tools",
                        flowSteps = listOf(
                            "Tap the three-dot tools icon in the reader header",
                            "Choose an action such as search or export notes",
                            "Select the target option to perform the action"
                        ),
                        imageResId = R.drawable.manual_reader_tools_menu
                    ),
                    ManualChapter(
                        title = "Adaptive Book Cover Themes",
                        emoji = "🎨",
                        subtitle = "Extract theme colors dynamically",
                        content = "Veritas automatically extracts vibrant color palettes from your document cover designs using the Android Palette API. When the 'Adaptive Cover' theme option is selected in Reader Settings, the reader UI changes its layout color schemes dynamically to match your book's primary and accent colors.",
                        ctaText = "Adaptive Cover Settings",
                        ctaAction = "reader_settings",
                        flowSteps = listOf(
                            "Open Reader Settings from the reader menu",
                            "Select 'Adaptive Cover' from theme options",
                            "Enjoy a beautifully matched background and UI"
                        )
                    ),
                    ManualChapter(
                        title = "Shared Element Transitions",
                        emoji = "✨",
                        subtitle = "Seamless navigation animations",
                        content = "Navigate from the Library directly into the reader with smooth animations. As you open a book, the cover page grows and repositions smoothly from the book list into the reader header. Closing the reader scales the book back to its original slot in the library grid.",
                        ctaText = "Transition Settings",
                        ctaAction = "library_options",
                        flowSteps = listOf(
                            "Select any book card in the Library",
                            "Observe the cover resizing smoothly into the reader view",
                            "Press back to see the cover transition back"
                        )
                    )
                )
            ),
            ManualSection(
                title = "Audio & Narration",
                emoji = "🎧",
                description = "Speed controls, voice engines, and pronunciation rules",
                chapters = listOf(
                    ManualChapter(
                        title = "Audio Player & Playback Control",
                        emoji = "🎧",
                        subtitle = "Play, pause, speed, and timer dials",
                        content = "Expand the bottom audio bar to control playback. You can set narration speeds from 0.5x to 4.0x, navigate by sentence or chapter, view progress status, and set a sleep timer to pause audio automatically after a set period.",
                        ctaText = "Configure Sleep Timer",
                        ctaAction = "sleep_timer",
                        flowSteps = listOf(
                            "Click the play icon on any sentence",
                            "Expand the mini-player to open the controls",
                            "Adjust the speed slider or set a sleep timer"
                        ),
                        imageResId = R.drawable.manual_audio_mode
                    ),
                    ManualChapter(
                        title = "Voice Studio & Engines",
                        emoji = "🗣️",
                        subtitle = "Manage text-to-speech speech synthesis",
                        content = "Customize your voice engine in the Voice Studio. Select from system speech synthesis engines (such as Google TTS), download regional language packs, and preview accents to find the clearest, most natural voice.",
                        ctaText = "Manage Voice Settings",
                        ctaAction = "voice_studio",
                        flowSteps = listOf(
                            "Open settings and click Voice Studio",
                            "Select your preferred Speech Synthesis Engine",
                            "Choose a specific voice profile from the list"
                        ),
                        imageResId = R.drawable.manual_voice_language
                    ),
                    ManualChapter(
                        title = "Narration Studio Toggles",
                        emoji = "🎙️",
                        subtitle = "Fine-tune speech cadence and pauses",
                        content = "Control speech cadence and pause lengths under Narration Studio. You can adjust the silence delay at punctuation marks (commas, periods, question marks) to create a natural, customized rhythm that fits your listening speed.",
                        ctaText = "Narration Controls",
                        ctaAction = "narration_studio",
                        flowSteps = listOf(
                            "Go to settings and select Narration Studio",
                            "Toggle custom punctuation pause settings",
                            "Define millisecond pause delays for commas or dots"
                        ),
                        imageResId = R.drawable.manual_narration_studio
                    ),
                    ManualChapter(
                        title = "Pronunciation Correction Rules",
                        emoji = "🔤",
                        subtitle = "Define phonetics for complex words",
                        content = "Fix incorrect TTS pronunciations by setting up custom replacement rules. If your device's voice engine mispronounces character names, medical acronyms, or specific technical jargon, add a rule translating the spelling to phonetics.",
                        ctaText = "Configure Pronunciation",
                        ctaAction = "pronunciation",
                        flowSteps = listOf(
                            "Select Pronunciation Rules in settings",
                            "Tap '+' to add a word replacement pair",
                            "Write the target word and its phonetic correction"
                        ),
                        imageResId = R.drawable.manual_pronunciation_rules
                    ),
                    ManualChapter(
                        title = "Sleep Timer Settings",
                        emoji = "⏳",
                        subtitle = "Automatically pause playback",
                        content = "Prevent narration from running all night. The Sleep Timer allows you to choose a countdown duration (from 5 minutes to 1 hour) or stop speaking at the end of the active section. Choose whether it should pause or stop playback upon completion.",
                        ctaText = "Configure Sleep Timer",
                        ctaAction = "sleep_timer",
                        flowSteps = listOf(
                            "Start speech playback inside the reader",
                            "Tap the clock timer icon in the audio bar",
                            "Select your duration limit and click 'Start'"
                        ),
                        imageResId = R.drawable.manual_sleep_timer
                    )
                )
            ),
            ManualSection(
                title = "Study & Progress",
                emoji = "📝",
                description = "Track reading history, streak insights, and notes",
                chapters = listOf(
                    ManualChapter(
                        title = "Study General & Vocabulary",
                        emoji = "📝",
                        subtitle = "Central list of highlights and words",
                        content = "Manage your vocabulary and highlights in the Study Hub. The general tab organizes all your saved words and highlights in order. Tapping any item takes you back to the exact page and chapter where you saved it.",
                        ctaText = "Study Hub Tip",
                        ctaAction = "study_general",
                        flowSteps = listOf(
                            "Open the Study screen from the navigation bar",
                            "Browse your vocabulary list or text highlights",
                            "Tap on a highlight to view it in context"
                        ),
                        imageResId = R.drawable.manual_study_general
                    ),
                    ManualChapter(
                        title = "Reading History Tracking",
                        emoji = "🕒",
                        subtitle = "Trace your past reading sessions",
                        content = "View your detailed reading log in the History tab. Veritas tracks when you open books, progress percentages, and active reading times. Select any entry in the log to instantly jump back and resume reading.",
                        ctaText = "View Reading History",
                        ctaAction = "history",
                        flowSteps = listOf(
                            "Navigate to the Study Hub screen",
                            "Select the History tab at the top",
                            "Tap on a recent session log to open that book"
                        ),
                        imageResId = R.drawable.manual_study_history
                    ),
                    ManualChapter(
                        title = "Reading Insights & Streaks",
                        emoji = "📈",
                        subtitle = "Check weekly statistics and streaks",
                        content = "Review your reading statistics in the Insights tab. This panel tracks your daily and weekly reading time, calculates average reading speed, and counts consecutive reading days to help you build and maintain a consistent habit.",
                        ctaText = "Manage Reading Lists",
                        ctaAction = "reading_lists",
                        flowSteps = listOf(
                            "Select Insights from the menu or drawer",
                            "Examine your reading duration bar chart",
                            "Check your active reading streak and weekly average"
                        ),
                        imageResId = R.drawable.manual_reading_insights
                    ),
                    ManualChapter(
                        title = "Spaced Repetition Flashcards",
                        emoji = "🎴",
                        subtitle = "Anki-style vocabulary/fact reviews",
                        content = "Review vocabulary and key facts using flashcards in the Study Hub. The deck automatically compiles cards and schedules them using the SuperMemo-2 (SM-2) algorithm. Tap 'Review Now' to launch the interactive flipping card UI, rate your memory recall (Again, Hard, Good, Easy), and study efficiently.",
                        ctaText = "Review Flashcards",
                        ctaAction = "study_flashcards",
                        flowSteps = listOf(
                            "Navigate to Study -> Flashcards tab",
                            "Tap 'Review Now' to start a session",
                            "Rate your recall to update the card's interval"
                        )
                    )
                )
            ),
            ManualSection(
                title = "Advanced Tools",
                emoji = "🤖",
                description = "AI handoff, sync, backups, and selection features",
                chapters = listOf(
                    ManualChapter(
                        title = "AI Handoff & Summaries",
                        emoji = "🤖",
                        subtitle = "Summarize text segments using AI helpers",
                        content = "Send highlighted paragraphs or chapters to AI helper tools. These assistants can summarize complex sections, explain difficult concepts, translate foreign phrases, and create question-and-answer study sheets.",
                        ctaText = "Open AI Center",
                        ctaAction = "ai_center",
                        flowSteps = listOf(
                            "Highlight a paragraph in the reader",
                            "Tap the 'AI Handoff' action from the toolbar",
                            "Choose an option (Summarize, Translate, or Explain)"
                        ),
                        imageResId = R.drawable.manual_ai_handoff
                    ),
                    ManualChapter(
                        title = "Interactive Text Selection",
                        emoji = "✍️",
                        subtitle = "Standard copy, select, and read commands",
                        content = "Use the floating text action bar for standard text management. You can copy the selection to your clipboard, select the entire document text, or tap 'Read from here' to resume speech playback starting directly from your highlighted sentence.",
                        ctaText = "Text Selection Tip",
                        ctaAction = "text_selection",
                        flowSteps = listOf(
                            "Double-tap or drag-highlight a sentence in the book",
                            "Wait for the text selection bar to appear",
                            "Tap 'Read from here' to begin audio narration from that point"
                        ),
                        imageResId = R.drawable.manual_text_selection_bar
                    ),
                    ManualChapter(
                        title = "Custom Text Actions Menu",
                        emoji = "⚡",
                        subtitle = "Trigger pronunciations, notes, and AI help",
                        content = "Long-press or select text inside the reflowed reader screen to trigger the custom context menu. This grants fast access to correct mispronunciations, save immediate page bookmarks, add comments/notes, or ask the AI helper to explain the text.",
                        ctaText = "Configure Pronunciation",
                        ctaAction = "pronunciation",
                        flowSteps = listOf(
                            "Long-press a word or sentence in the reader",
                            "Adjust selection handles to highlight text",
                            "Choose an option like 'Fix pronunciation' or 'Ask AI'"
                        ),
                        imageResId = R.drawable.manual_text_context_menu
                    ),
                    ManualChapter(
                        title = "Sync Center Integration",
                        emoji = "🔄",
                        subtitle = "Sync settings and libraries across devices",
                        content = "Keep your digital library in sync across Android and PC versions of Veritas. You can configure cloud sync (WebDAV, Google Drive, or personal endpoints) to automatically upload progress and custom voices on start or exit.",
                        ctaText = "Manage Sync Center",
                        ctaAction = "sync_center",
                        flowSteps = listOf(
                            "Open settings and click Sync Center",
                            "Configure WebDAV or cloud accounts",
                            "Tap 'Sync Now' to manually synchronize your files"
                        ),
                        imageResId = R.drawable.manual_sync_center
                    ),
                    ManualChapter(
                        title = "Backup & Recovery",
                        emoji = "💾",
                        subtitle = "Create backup database archives",
                        content = "Export your settings and reading database to a backup archive to protect against data loss. These `.veritas` archives can be stored on external cloud accounts or local drives, and restored at any time.",
                        ctaText = "Configure Backups",
                        ctaAction = "backup_tools",
                        flowSteps = listOf(
                            "Select Backup & Restore under Settings",
                            "Choose 'Export Backup' and save the archive file",
                            "Use 'Import Backup' to restore your data on a new device"
                        ),
                        imageResId = R.drawable.manual_backup_restore
                    )
                )
            )
        )
    }

    val filteredSections = remember(searchQuery, allSections) {
        if (searchQuery.isBlank()) {
            allSections
        } else {
            allSections.map { section ->
                val matchingChapters = section.chapters.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true)
                }
                section.copy(chapters = matchingChapters)
            }.filter { it.chapters.isNotEmpty() }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
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

                // Sections list
                if (filteredSections.isEmpty()) {
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
                        items(filteredSections) { section ->
                            SectionCard(
                                section = section,
                                searchQuery = searchQuery,
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
fun SectionCard(
    section: ManualSection,
    searchQuery: String,
    onNavigateToSetting: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isExpanded = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
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
                        text = section.emoji,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = section.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    section.chapters.forEach { chapter ->
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

@Composable
fun ChapterCard(
    chapter: ManualChapter,
    onNavigateToSetting: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
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
                Column(modifier = Modifier.weight(1f)) {
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
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
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

                    // Screenshot image if present
                    if (chapter.imageResId != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Image(
                            painter = painterResource(id = chapter.imageResId),
                            contentDescription = "Screenshot of ${chapter.title}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentScale = ContentScale.FillWidth
                        )
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
    }
}
