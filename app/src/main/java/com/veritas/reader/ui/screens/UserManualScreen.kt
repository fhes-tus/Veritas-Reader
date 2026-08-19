package com.veritas.reader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.veritas.reader.R
import com.veritas.reader.VeritasPackStyle

data class ManualChapter(
    val title: String,
    val icon: ImageVector,
    val subtitle: String,
    val content: String,
    val ctaText: String? = null,
    val ctaAction: String? = null,
    val flowSteps: List<String> = emptyList(),
    val imageResId: Int? = null,
    val tags: List<String> = emptyList()
)

data class ManualSection(
    val title: String,
    val icon: ImageVector,
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
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                description = "Manage your books, documents, and web imports",
                chapters = listOf(
                    ManualChapter(
                        title = "Library Dashboard",
                        icon = Icons.Outlined.CollectionsBookmark,
                        subtitle = "Central document library overview",
                        content = "The Library dashboard is your starting point in Veritas. It compiles all your imported books, documents, and web articles. View reading progress per file, see completion status, filter your lists, and tap any item to start reading instantly.",
                        ctaText = "Manage Reading Lists",
                        ctaAction = "reading_lists",
                        flowSteps = listOf(
                            "Launch Veritas to view your Library",
                            "Filter or sort your list using the top toolbar",
                            "Tap on a book card to open the reader"
                        ),
                        imageResId = R.drawable.manual_library_main,
                        tags = listOf("dashboard", "library", "books", "documents", "articles", "lists", "shelf")
                    ),
                    ManualChapter(
                        title = "Document Actions & Metadata",
                        icon = Icons.Outlined.MoreVert,
                        subtitle = "Edit document titles, authors, and options",
                        content = "Access control options for individual documents using the three-dot overflow button on any book card. Edit titles, modify author names, assign tags, add documents to reading lists, reset reading progress, or delete files from storage.",
                        ctaText = "Document Options Tip",
                        ctaAction = "library_options",
                        flowSteps = listOf(
                            "Find the document in your Library list",
                            "Tap the three-dot options menu icon",
                            "Choose an action (Rename, Categorize, or Delete)"
                        ),
                        imageResId = R.drawable.manual_library_item_menu,
                        tags = listOf("rename", "author", "metadata", "delete", "options", "edit", "tags", "categories")
                    ),
                    ManualChapter(
                        title = "Batch Organization",
                        icon = Icons.Outlined.Checklist,
                        subtitle = "Manage multiple documents together",
                        content = "Organize a large reading list efficiently using batch mode. By long-pressing any book card, you activate multi-select mode. Select multiple items to bulk delete, assign categories, or append to folders at once.",
                        ctaText = "Bulk Edit Tip",
                        ctaAction = "bulk_edit",
                        flowSteps = listOf(
                            "Long-press any document card in the Library",
                            "Tap other cards to select multiple items",
                            "Select a bulk action icon from the top bar"
                        ),
                        imageResId = R.drawable.manual_library_batch,
                        tags = listOf("batch", "bulk", "multi-select", "select all", "delete multiple", "organize")
                    ),
                    ManualChapter(
                        title = "Adding Content",
                        icon = Icons.Outlined.AddCircleOutline,
                        subtitle = "Quick-add drawer options",
                        content = "Tap the floating '+' action button to expand content addition options. Import local document files, scrape online articles by pasting a web link, or open a clean canvas in the text editor to write scratch notes.",
                        ctaText = "Open File Browser",
                        ctaAction = "file_browser",
                        flowSteps = listOf(
                            "Tap the '+' floating button in the Library",
                            "Select file import, web URL, or scratch text",
                            "Confirm the selection to add it to your library"
                        ),
                        imageResId = R.drawable.manual_add_sheet,
                        tags = listOf("add", "import", "plus", "url", "web", "scrape", "link", "new")
                    ),
                    ManualChapter(
                        title = "Integrated File Browser",
                        icon = Icons.Outlined.Folder,
                        subtitle = "Scan and import device storage files",
                        content = "Browse your device's directories locally with the integrated file navigator. It automatically highlights and filters compatible document formats such as EPUB, PDF, DOCX, PPTX, and TXT, making imports quick and reliable.",
                        ctaText = "Start File Import",
                        ctaAction = "file_browser",
                        flowSteps = listOf(
                            "Choose 'Local File Browser' from the '+' sheet",
                            "Navigate through folders to find your documents",
                            "Tap a file to copy and register it in the Library"
                        ),
                        imageResId = R.drawable.manual_file_browser,
                        tags = listOf("file browser", "storage", "directory", "folder", "epub", "pdf", "docx", "txt", "import")
                    ),
                    ManualChapter(
                        title = "File Browser Custom Filters",
                        icon = Icons.Outlined.FilterList,
                        subtitle = "Sort and filter local storage files",
                        content = "Refine lists in the local browser using the options menu. Sort files by size, date, or name, and toggle the visibility of hidden system folders or files to quickly locate specific digital books or textbooks.",
                        ctaText = "File Sort Tip",
                        ctaAction = "file_browser_filters",
                        flowSteps = listOf(
                            "Open the local file browser screen",
                            "Tap the three-dot menu at the top-right",
                            "Apply sorting rules or filter constraints"
                        ),
                        imageResId = R.drawable.manual_file_browser_menu,
                        tags = listOf("sort", "filter", "hidden files", "file size", "alphabetical", "directory filter")
                    ),
                    ManualChapter(
                        title = "PDF & Text Extraction Settings",
                        icon = Icons.Outlined.Description,
                        subtitle = "Manage layout conversion settings",
                        content = "Fine-tune how documents are parsed during import. Tweak image OCR recognition thresholds, set default character encodings for TXT files, and enable automatic page header or footer cleanup to strip out page numbers and running titles.",
                        ctaText = "Configure PDF Import",
                        ctaAction = "pdf_tools",
                        flowSteps = listOf(
                            "Access PDF Tools from Settings or Import",
                            "Adjust cleanup toggles and OCR settings",
                            "Save preferences to apply to future imports"
                        ),
                        imageResId = R.drawable.manual_import_settings,
                        tags = listOf("pdf", "ocr", "extraction", "encoding", "header", "footer", "cleanup", "page numbers")
                    )
                )
            ),
            ManualSection(
                title = "Reader & Display",
                icon = Icons.Outlined.AutoStories,
                description = "Customize font, display modes, themes, and PDF layout",
                chapters = listOf(
                    ManualChapter(
                        title = "Reader Screen Text Mode",
                        icon = Icons.Outlined.Article,
                        subtitle = "Reflowed layout reading canvas",
                        content = "Open any book in Text Mode to view clean, reflowed paragraphs. Fixed page borders and margins are removed, allowing text to fit your screen size perfectly. Text matches your theme colors, and double-tapping acts as a shortcut for definitions.",
                        ctaText = "Select Text Tip",
                        ctaAction = "text_selection",
                        flowSteps = listOf(
                            "Open a document card from your Library",
                            "Swipe or scroll to navigate between pages",
                            "Double-tap any word to select it"
                        ),
                        imageResId = R.drawable.manual_reader_screen,
                        tags = listOf("reflow", "text mode", "canvas", "paragraphs", "font size", "margin", "padding")
                    ),
                    ManualChapter(
                        title = "Original PDF Layout Mode",
                        icon = Icons.Outlined.PictureInPicture,
                        subtitle = "Read fixed-page PDF documents",
                        content = "Switch to Original Mode to view documents exactly as formatted. Ideal for textbooks, multi-column research papers, and books with complex visual layouts. Zoom, pan, and tap sentences to trigger speech narration or sync coordinates.",
                        ctaText = "PDF Tools Settings",
                        ctaAction = "pdf_tools",
                        flowSteps = listOf(
                            "Open a PDF document in the reader",
                            "Tap 'ORIGINAL' tab in the top bar",
                            "View pages with full original styling and zoom support"
                        ),
                        imageResId = R.drawable.manual_reader_original,
                        tags = listOf("pdf", "original", "fixed layout", "columns", "zoom", "pan", "textbook")
                    ),
                    ManualChapter(
                        title = "Reader Presentation Modes",
                        icon = Icons.Outlined.ViewCarousel,
                        subtitle = "Toggle TEXT, LISTEN, and ORIGINAL views",
                        content = "Maximize focus by choosing the ideal presentation mode. 'TEXT' extracts and reflows content for clean reading, 'LISTEN' focuses on audio playback controls, and 'ORIGINAL' displays the document's native fixed format page layout.",
                        ctaText = "Reader Settings",
                        ctaAction = "reader_settings",
                        flowSteps = listOf(
                            "While reading a book, locate the mode selector tabs",
                            "Tap 'TEXT' for clean reflow or 'LISTEN' for audio layout",
                            "Switch back to 'ORIGINAL' for original formatting"
                        ),
                        imageResId = R.drawable.manual_reader_tabs,
                        tags = listOf("presentation", "listen mode", "text mode", "original mode", "tabs", "view")
                    ),
                    ManualChapter(
                        title = "Typography & Theme Controls",
                        icon = Icons.Outlined.Palette,
                        subtitle = "Personalize fonts, sizes, and layout",
                        content = "Adjust the reader interface to fit your reading preferences. Switch background colors (Light, Dark, Sepia, or glassmorphic theme packs), customize fonts (including Lexend and OpenDyslexic), and set custom line spacing or page margins.",
                        ctaText = "Configure Appearance",
                        ctaAction = "reader_settings",
                        flowSteps = listOf(
                            "Tweak spacing or font style while reading",
                            "Tap the typography gear icon in the reader bar",
                            "Select a combination that maximizes your comfort"
                        ),
                        imageResId = R.drawable.manual_reader_settings,
                        tags = listOf("font", "theme", "dark mode", "sepia", "lexend", "opendyslexic", "line spacing", "margins", "appearance")
                    ),
                    ManualChapter(
                        title = "Reader Tools Overflow",
                        icon = Icons.Outlined.Tune,
                        subtitle = "Secondary actions inside the reader",
                        content = "Tap the options overflow in the reader to access extra tools. This menu houses quick bookmark creation, word search inside the active book, reader progress resets, and options to export highlighted notes to external Markdown files.",
                        ctaText = "Reader Options Tip",
                        ctaAction = "reader_tools",
                        flowSteps = listOf(
                            "Tap the three-dot tools icon in the reader header",
                            "Choose an action such as search or export notes",
                            "Select the target option to perform the action"
                        ),
                        imageResId = R.drawable.manual_reader_tools_menu,
                        tags = listOf("bookmark", "search text", "export notes", "markdown", "reset progress")
                    ),
                    ManualChapter(
                        title = "Adaptive Book Cover Themes",
                        icon = Icons.Outlined.ColorLens,
                        subtitle = "Extract theme colors dynamically",
                        content = "Veritas automatically extracts vibrant color palettes from your document cover designs. When the 'Adaptive Cover' theme option is selected in Reader Settings, the reader UI changes its layout color schemes dynamically to match your book's primary and accent colors.",
                        ctaText = "Adaptive Cover Settings",
                        ctaAction = "reader_settings",
                        flowSteps = listOf(
                            "Open Reader Settings from the reader menu",
                            "Select 'Adaptive Cover' from theme options",
                            "Enjoy a beautifully matched background and UI"
                        ),
                        tags = listOf("cover", "adaptive theme", "dynamic color", "palette", "accent color")
                    )
                )
            ),
            ManualSection(
                title = "Audio & Narration",
                icon = Icons.Outlined.RecordVoiceOver,
                description = "Speed controls, voice engines, speech synthesis, and sleep timer",
                chapters = listOf(
                    ManualChapter(
                        title = "Audio Player & Playback Control",
                        icon = Icons.Outlined.GraphicEq,
                        subtitle = "Play, pause, speed, and timer dials",
                        content = "Expand the bottom audio bar to control playback. Set narration speeds from 0.5x to 4.0x, navigate by sentence or chapter, view progress status, and set a sleep timer to pause audio automatically after a set period.",
                        ctaText = "Configure Sleep Timer",
                        ctaAction = "sleep_timer",
                        flowSteps = listOf(
                            "Click the play icon on any sentence",
                            "Expand the mini-player to open controls",
                            "Adjust speed slider or set a sleep timer"
                        ),
                        imageResId = R.drawable.manual_audio_mode,
                        tags = listOf("audio", "tts", "play", "pause", "speed", "playback", "listen", "narrate", "loud")
                    ),
                    ManualChapter(
                        title = "Voice Studio & Engines",
                        icon = Icons.Outlined.RecordVoiceOver,
                        subtitle = "Manage text-to-speech speech synthesis",
                        content = "Customize your voice engine in the Voice Studio. Select from system speech synthesis engines (such as Google TTS), download regional language packs, and preview accents to find the clearest, most natural voice.",
                        ctaText = "Manage Voice Settings",
                        ctaAction = "voice_studio",
                        flowSteps = listOf(
                            "Open settings and click Voice Studio",
                            "Select your preferred Speech Synthesis Engine",
                            "Choose a specific voice profile from the list"
                        ),
                        imageResId = R.drawable.manual_voice_language,
                        tags = listOf("voice", "engine", "tts", "google tts", "accent", "language", "speech synthesis", "speak")
                    ),
                    ManualChapter(
                        title = "Full-Cast Narration Studio",
                        icon = Icons.Outlined.Tune,
                        subtitle = "Multi-voice character speech and pitch control",
                        content = "Assign distinct TTS voices, pitch multipliers, and speeds to different book characters under Narration Studio. Smart dialogue analysis automatically detects character quotes and speaks them in their assigned voice.",
                        ctaText = "Narration Controls",
                        ctaAction = "narration_studio",
                        flowSteps = listOf(
                            "Go to settings and select Narration Studio",
                            "Enable Full-cast multi-voice mode",
                            "Create character profiles and customize pitch/speed"
                        ),
                        imageResId = R.drawable.manual_narration_studio,
                        tags = listOf("full-cast", "character", "multi-voice", "pitch", "narration", "dialogue", "speech")
                    ),
                    ManualChapter(
                        title = "Pronunciation Correction Rules",
                        icon = Icons.Outlined.Spellcheck,
                        subtitle = "Define phonetics for complex words",
                        content = "Fix incorrect TTS pronunciations by setting up custom replacement rules. If your device's voice engine mispronounces character names, medical acronyms, or specific technical jargon, add a rule translating the spelling to phonetics.",
                        ctaText = "Configure Pronunciation",
                        ctaAction = "pronunciation",
                        flowSteps = listOf(
                            "Select Pronunciation Rules in settings",
                            "Tap '+' to add a word replacement pair",
                            "Write target word and its phonetic correction"
                        ),
                        imageResId = R.drawable.manual_pronunciation_rules,
                        tags = listOf("pronunciation", "phonetics", "acronyms", "mispronounce", "dictionary", "replace word")
                    ),
                    ManualChapter(
                        title = "Sleep Timer Settings",
                        icon = Icons.Outlined.Timer,
                        subtitle = "Automatically pause playback",
                        content = "Prevent narration from running all night. The Sleep Timer allows choosing a countdown duration (from 5 minutes to 1 hour) or stopping speech at the end of the active section. Choose whether it should pause or stop playback upon completion.",
                        ctaText = "Configure Sleep Timer",
                        ctaAction = "sleep_timer",
                        flowSteps = listOf(
                            "Start speech playback inside the reader",
                            "Tap clock timer icon in the audio bar",
                            "Select duration limit and click 'Start'"
                        ),
                        imageResId = R.drawable.manual_sleep_timer,
                        tags = listOf("sleep timer", "timer", "countdown", "night", "auto pause", "stop audio")
                    )
                )
            ),
            ManualSection(
                title = "Study & Progress",
                icon = Icons.Outlined.AutoAwesome,
                description = "Track reading history, streak insights, study cards, and notes",
                chapters = listOf(
                    ManualChapter(
                        title = "Study General & Vocabulary",
                        icon = Icons.Outlined.BookmarkBorder,
                        subtitle = "Central list of highlights and words",
                        content = "Manage vocabulary and highlights in the Study Hub. The general tab organizes all saved words and highlights in order. Tapping any item takes you back to the exact page and chapter where you saved it.",
                        ctaText = "Study Hub Tip",
                        ctaAction = "study_general",
                        flowSteps = listOf(
                            "Open Study screen from navigation bar",
                            "Browse vocabulary list or text highlights",
                            "Tap on a highlight to view it in context"
                        ),
                        imageResId = R.drawable.manual_study_general,
                        tags = listOf("study", "vocabulary", "highlights", "notes", "bookmarks", "words", "saved")
                    ),
                    ManualChapter(
                        title = "Reading History Tracking",
                        icon = Icons.Outlined.History,
                        subtitle = "Trace past reading sessions",
                        content = "View detailed reading logs in the History tab. Veritas tracks when you open books, progress percentages, and active reading times. Select any entry in the log to instantly jump back and resume reading.",
                        ctaText = "View Reading History",
                        ctaAction = "history",
                        flowSteps = listOf(
                            "Navigate to Study Hub screen",
                            "Select History tab at top",
                            "Tap a recent session log to open that book"
                        ),
                        imageResId = R.drawable.manual_study_history,
                        tags = listOf("history", "recent", "log", "reading session", "timeline", "resume")
                    ),
                    ManualChapter(
                        title = "Reading Insights & Streaks",
                        icon = Icons.Outlined.Insights,
                        subtitle = "Check weekly statistics and streaks",
                        content = "Review reading statistics in the Insights tab. This panel tracks daily and weekly reading time, calculates average reading speed, and counts consecutive reading days to help build and maintain a consistent habit.",
                        ctaText = "Manage Reading Lists",
                        ctaAction = "reading_lists",
                        flowSteps = listOf(
                            "Select Insights from menu or drawer",
                            "Examine reading duration bar chart",
                            "Check active reading streak and weekly average"
                        ),
                        imageResId = R.drawable.manual_reading_insights,
                        tags = listOf("insights", "stats", "analytics", "streak", "daily goal", "chart", "speed", "time")
                    ),
                    ManualChapter(
                        title = "Spaced Repetition Flashcards",
                        icon = Icons.Outlined.Psychology,
                        subtitle = "Anki-style vocabulary & fact reviews",
                        content = "Review vocabulary and key facts using flashcards in the Study Hub. The deck automatically compiles cards and schedules them using the SuperMemo-2 (SM-2) algorithm. Tap 'Review Now' to launch interactive flipping cards and rate memory recall.",
                        ctaText = "Review Flashcards",
                        ctaAction = "study_flashcards",
                        flowSteps = listOf(
                            "Navigate to Study -> Flashcards tab",
                            "Tap 'Review Now' to start a session",
                            "Rate your recall to update the card's interval"
                        ),
                        tags = listOf("flashcards", "anki", "sm2", "spaced repetition", "quiz", "memorize", "card review", "deck")
                    )
                )
            ),
            ManualSection(
                title = "Advanced Tools & Cloud",
                icon = Icons.Outlined.Settings,
                description = "AI helpers, sync center, backups, and custom selection tools",
                chapters = listOf(
                    ManualChapter(
                        title = "AI Handoff & Summaries",
                        icon = Icons.Outlined.AutoAwesome,
                        subtitle = "Summarize text segments using AI helpers",
                        content = "Send highlighted paragraphs or chapters to AI helper tools. These assistants summarize complex sections, explain difficult concepts, translate foreign phrases, and create question-and-answer study sheets.",
                        ctaText = "Open AI Center",
                        ctaAction = "ai_center",
                        flowSteps = listOf(
                            "Highlight a paragraph in the reader",
                            "Tap 'AI Handoff' action from toolbar",
                            "Choose an option (Summarize, Translate, or Explain)"
                        ),
                        imageResId = R.drawable.manual_ai_handoff,
                        tags = listOf("ai", "summary", "translate", "explain", "assistant", "chat", "smart", "prompt")
                    ),
                    ManualChapter(
                        title = "Interactive Text Selection",
                        icon = Icons.Outlined.ContentCut,
                        subtitle = "Standard copy, select, and read commands",
                        content = "Use the floating text action bar for standard text management. Copy selection to clipboard, select entire document text, or tap 'Read from here' to resume speech playback starting directly from your highlighted sentence.",
                        ctaText = "Text Selection Tip",
                        ctaAction = "text_selection",
                        flowSteps = listOf(
                            "Double-tap or drag-highlight a sentence in the book",
                            "Wait for text selection bar to appear",
                            "Tap 'Read from here' to begin audio narration from that point"
                        ),
                        imageResId = R.drawable.manual_text_selection_bar,
                        tags = listOf("copy", "select text", "highlight", "read from here", "clipboard", "selection bar")
                    ),
                    ManualChapter(
                        title = "Sync Center Integration",
                        icon = Icons.Outlined.Sync,
                        subtitle = "Sync settings and libraries across devices",
                        content = "Keep your digital library in sync across devices. Configure cloud sync (WebDAV, Google Drive, or personal endpoints) to automatically upload progress, bookmarks, and custom voices on start or exit.",
                        ctaText = "Manage Sync Center",
                        ctaAction = "sync_center",
                        flowSteps = listOf(
                            "Open settings and click Sync Center",
                            "Configure WebDAV or cloud accounts",
                            "Tap 'Sync Now' to manually synchronize files"
                        ),
                        imageResId = R.drawable.manual_sync_center,
                        tags = listOf("sync", "cloud", "google drive", "webdav", "cross device", "backup", "library sync")
                    ),
                    ManualChapter(
                        title = "Backup & Recovery",
                        icon = Icons.Outlined.Backup,
                        subtitle = "Create backup database archives",
                        content = "Export settings and reading database to a backup archive to protect against data loss. These `.veritas` archives can be stored on external cloud accounts or local drives, and restored at any time.",
                        ctaText = "Configure Backups",
                        ctaAction = "backup_tools",
                        flowSteps = listOf(
                            "Select Backup & Restore under Settings",
                            "Choose 'Export Backup' and save archive file",
                            "Use 'Import Backup' to restore data on a new device"
                        ),
                        imageResId = R.drawable.manual_backup_restore,
                        tags = listOf("backup", "export database", "restore", "veritas archive", "recovery", "data loss")
                    )
                )
            )
        )
    }

    // Smart Fuzzy & Intent Search Matcher
    val filteredSections = remember(searchQuery, allSections) {
        val rawQuery = searchQuery.trim().lowercase()
        if (rawQuery.isBlank()) {
            allSections
        } else {
            val queryTokens = rawQuery.split(Regex("\\s+")).filter { it.length > 1 }

            // Synonym dictionary mapping user intent words to semantic tags
            val semanticSynonyms = mapOf(
                "speak" to listOf("tts", "speech", "voice", "audio", "narrate", "listen", "read loud", "sound", "cadence"),
                "read" to listOf("reader", "book", "text", "page", "font", "display", "mode", "reflow", "original"),
                "dark" to listOf("theme", "mode", "color", "background", "black", "palette", "night", "sepia"),
                "quiz" to listOf("flashcard", "card", "anki", "sm2", "study", "memory", "review", "vocab", "deck"),
                "pdf" to listOf("document", "file", "ocr", "layout", "original", "import", "scan"),
                "save" to listOf("backup", "sync", "export", "restore", "drive", "webdav", "archive"),
                "fast" to listOf("speed", "rate", "pitch", "narration", "cadence", "pause")
            )

            allSections.mapNotNull { section ->
                val matchingChapters = section.chapters.filter { chapter ->
                    val targetText = (chapter.title + " " + chapter.subtitle + " " + chapter.content + " " + chapter.tags.joinToString(" ") + " " + section.title + " " + section.description).lowercase()

                    // Direct substring match
                    if (targetText.contains(rawQuery)) return@filter true

                    // Token matching with semantic synonyms
                    if (queryTokens.isNotEmpty()) {
                        queryTokens.all { token ->
                            targetText.contains(token) || semanticSynonyms[token]?.any { syn -> targetText.contains(syn) } == true
                        }
                    } else false
                }
                if (matchingChapters.isNotEmpty()) section.copy(chapters = matchingChapters) else null
            }
        }
    }

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
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "User Manual",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar with Theme Pack Awareness
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search features (e.g. read out loud, dark mode, pdf)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = VeritasPackStyle.cardShape(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha()),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha()),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sections List
                if (filteredSections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching manual sections found.\nTry searching for 'voice', 'dark mode', 'pdf', or 'backup'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = VeritasPackStyle.surfaceAlpha())
        ),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val cardBorder = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = VeritasPackStyle.compactShape()
                        )
                        .then(if (cardBorder != null) Modifier.border(cardBorder, VeritasPackStyle.compactShape()) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
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
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Title block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = VeritasPackStyle.compactShape()
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = chapter.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
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
                    modifier = Modifier.size(22.dp)
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

                    // Workflow steps if present
                    if (chapter.flowSteps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(VeritasPackStyle.cardShape())
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Workflow Steps",
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
                                .clip(VeritasPackStyle.cardShape()),
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
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = VeritasPackStyle.chipShape(),
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
