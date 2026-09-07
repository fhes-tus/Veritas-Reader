package com.veritas.desktop.ui.workstation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritas.desktop.audio.DesktopPlaybackController
import com.veritas.desktop.models.*
import com.veritas.desktop.ui.components.DesktopPlaybackBar
import com.veritas.desktop.ui.components.DesktopTopBar
import com.veritas.desktop.ui.screens.*

@Composable
fun WorkstationLayout(
    documents: List<DesktopDocument>,
    activeDocument: DesktopDocument?,
    playbackController: DesktopPlaybackController,
    bookmarks: List<Bookmark>,
    annotations: List<TextAnnotation>,
    richNotes: List<RichNote>,
    habitTracker: HabitTracker,
    onSelectDocument: (DesktopDocument) -> Unit,
    onImportFile: () -> Unit,
    onPasteText: () -> Unit,
    onDeleteDocument: (DesktopDocument) -> Unit,
    onToggleFavorite: (DesktopDocument) -> Unit,
    onToggleBookmark: (Int, String) -> Unit,
    onAddAnnotation: (Int, String) -> Unit,
    onDeleteAnnotation: (String) -> Unit,
    onSaveRichNote: (RichNote) -> Unit,
    onDeleteRichNote: (String) -> Unit,
    onLaunchRsvp: (Int) -> Unit,
    onSwitchToFloater: () -> Unit,
    onSelectTheme: (DesktopThemeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by playbackController.state.collectAsState()

    var activeTab by remember { mutableStateOf(WorkstationTab.HOME) }
    var isStudyStudioOpen by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Bar
        DesktopTopBar(
            documentTitle = if (activeTab == WorkstationTab.READER) activeDocument?.title else null,
            documentSource = if (activeTab == WorkstationTab.READER) activeDocument?.sourceLabel else null,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            showSearch = showSearch,
            onToggleSearch = {
                showSearch = !showSearch
                if (!showSearch) searchQuery = ""
            },
            currentTheme = state.readerSettings.themeType,
            onSelectTheme = onSelectTheme,
            onSwitchToFloater = onSwitchToFloater,
            onOpenSettings = { isStudyStudioOpen = true },
            onToggleSidebar = { /* Handled via Navigation Rail */ },
            onToggleStudyStudio = { isStudyStudioOpen = !isStudyStudioOpen },
            isSidebarOpen = true,
            isStudyStudioOpen = isStudyStudioOpen
        )

        // Main Center Workstation Stage with Left Navigation Rail
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Left Navigation Rail
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxHeight().width(76.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                NavigationRailItem(
                    selected = activeTab == WorkstationTab.HOME,
                    onClick = { activeTab = WorkstationTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp) }
                )

                NavigationRailItem(
                    selected = activeTab == WorkstationTab.LIBRARY,
                    onClick = { activeTab = WorkstationTab.LIBRARY },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = "Bookshelf") },
                    label = { Text("Library", fontSize = 10.sp) }
                )

                NavigationRailItem(
                    selected = activeTab == WorkstationTab.READER,
                    onClick = { activeTab = WorkstationTab.READER },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Reader") },
                    label = { Text("Reader", fontSize = 10.sp) }
                )

                NavigationRailItem(
                    selected = activeTab == WorkstationTab.NOTES,
                    onClick = { activeTab = WorkstationTab.NOTES },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Notes") },
                    label = { Text("Notes", fontSize = 10.sp) }
                )

                NavigationRailItem(
                    selected = activeTab == WorkstationTab.STUDY_ANALYTICS,
                    onClick = { activeTab = WorkstationTab.STUDY_ANALYTICS },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Insights") },
                    label = { Text("Insights", fontSize = 10.sp) }
                )
            }

            VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Main Active View Container
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                when (activeTab) {
                    WorkstationTab.HOME -> {
                        DesktopHomeDashboard(
                            continueDocument = activeDocument ?: documents.firstOrNull(),
                            recentDocuments = documents,
                            isPlaying = state.isPlaying,
                            activeDocumentId = state.activeDocument?.id,
                            currentIndex = state.currentIndex,
                            habitTracker = habitTracker,
                            onOpenDocument = { doc ->
                                onSelectDocument(doc)
                                activeTab = WorkstationTab.READER
                            },
                            onTogglePlay = { doc ->
                                if (state.activeDocument?.id != doc.id) {
                                    playbackController.loadDocument(doc)
                                }
                                playbackController.togglePlay()
                            },
                            onClearContinue = { /* Optional clear */ },
                            onImportFile = onImportFile,
                            onPasteText = onPasteText,
                            onOpenBookshelf = { activeTab = WorkstationTab.LIBRARY }
                        )
                    }

                    WorkstationTab.LIBRARY -> {
                        DesktopBookshelfView(
                            documents = documents,
                            onOpenDocument = { doc ->
                                onSelectDocument(doc)
                                activeTab = WorkstationTab.READER
                            },
                            onToggleFavorite = onToggleFavorite,
                            onDeleteDocument = onDeleteDocument,
                            onImportFile = onImportFile
                        )
                    }

                    WorkstationTab.READER -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            ReadingCanvas(
                                document = activeDocument,
                                currentIndex = state.currentIndex,
                                isPlaying = state.isPlaying,
                                settings = state.readerSettings,
                                searchQuery = searchQuery,
                                bookmarks = bookmarks,
                                annotations = annotations,
                                onSentenceClick = { idx -> playbackController.jumpToSentence(idx, autoPlay = false) },
                                onSentenceDoubleTap = { idx -> playbackController.jumpToSentence(idx, autoPlay = true) },
                                onToggleBookmark = onToggleBookmark,
                                onAddAnnotation = onAddAnnotation,
                                onLaunchRsvp = onLaunchRsvp,
                                onBackToLibrary = { activeTab = WorkstationTab.LIBRARY },
                                modifier = Modifier.weight(1f)
                            )

                            // Right: Collapsible Study Studio & Inspector
                            AnimatedVisibility(
                                visible = isStudyStudioOpen,
                                enter = slideInHorizontally(initialOffsetX = { it }),
                                exit = slideOutHorizontally(targetOffsetX = { it })
                            ) {
                                StudyStudio(
                                    state = state,
                                    annotations = annotations,
                                    onAddAnnotation = { _, note -> onAddAnnotation(state.currentIndex, note) },
                                    onDeleteAnnotation = onDeleteAnnotation,
                                    onSelectVoice = { playbackController.setVoice(it) },
                                    onSetSpeed = { playbackController.setSpeed(it) },
                                    onSetPitch = { /* Pitch */ },
                                    onSetSleepTimer = { playbackController.setSleepTimer(it) },
                                    onUpdateReaderSettings = { playbackController.updateReaderSettings(it) },
                                    onCloseStudio = { isStudyStudioOpen = false }
                                )
                            }
                        }
                    }

                    WorkstationTab.NOTES -> {
                        DesktopNotesStudio(
                            notes = richNotes,
                            onSaveNote = onSaveRichNote,
                            onDeleteNote = onDeleteRichNote
                        )
                    }

                    WorkstationTab.STUDY_ANALYTICS -> {
                        DesktopInsightsView(
                            habitTracker = habitTracker,
                            documents = documents
                        )
                    }
                }
            }
        }

        // Bottom Playback HUD Bar
        DesktopPlaybackBar(
            state = state,
            onTogglePlay = { playbackController.togglePlay() },
            onNext = { playbackController.nextSentence() },
            onPrevious = { playbackController.previousSentence() },
            onSetSpeed = { playbackController.setSpeed(it) },
            onOpenVoiceMenu = { isStudyStudioOpen = true }
        )
    }
}
