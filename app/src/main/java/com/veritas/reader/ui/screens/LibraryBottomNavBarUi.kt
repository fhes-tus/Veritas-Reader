package com.veritas.reader.ui.screens

import androidx.compose.ui.tooling.preview.Preview
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


import androidx.compose.foundation.layout.RowScope

@Composable
internal fun LibraryBottomNavBar(
    activeNavTab: VeritasHomeTab,
    showNavLabels: Boolean,
    onNavigateToTab: (VeritasHomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
                val scheme = MaterialTheme.colorScheme
                val isDark = scheme.surface.luminance() < 0.5f
                val showNavLabels = showNavLabels
                val barHeight = if (showNavLabels) 66.dp else 60.dp

                // Translucent floating capsule with theme gradient
                val gradientBrush = if (isDark) {
                    Brush.verticalGradient(
                        colors = listOf(
                            blendColors(scheme.surface, scheme.primary, 0.12f).copy(alpha = 0.94f),
                            blendColors(scheme.surface, Color.Black, 0.20f).copy(alpha = 0.96f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            blendColors(scheme.surface, scheme.primaryContainer, 0.35f).copy(alpha = 0.95f),
                            blendColors(scheme.surface, scheme.primary, 0.08f).copy(alpha = 0.97f)
                        )
                    )
                }

                val borderBrush = if (isDark) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            scheme.primary.copy(alpha = 0.32f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.65f),
                            scheme.primary.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.30f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 580.dp)
                            .fillMaxWidth()
                            .height(barHeight),
                        shape = RoundedCornerShape(barHeight / 2),
                        color = Color.Transparent,
                        shadowElevation = if (isDark) 10.dp else 8.dp,
                        tonalElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = gradientBrush,
                                    shape = RoundedCornerShape(barHeight / 2)
                                )
                                .border(
                                    width = 1.dp,
                                    brush = borderBrush,
                                    shape = RoundedCornerShape(barHeight / 2)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BottomNavItem(
                                    selected = activeNavTab == VeritasHomeTab.HOME,
                                    onClick = { onNavigateToTab(VeritasHomeTab.HOME) },
                                    icon = { color, size ->
                                        Icon(
                                            imageVector = if (activeNavTab == VeritasHomeTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home",
                                            tint = color,
                                            modifier = Modifier.size(size)
                                        )
                                    },
                                    label = "Home",
                                    showLabel = showNavLabels
                                )

                                BottomNavItem(
                                    selected = activeNavTab == VeritasHomeTab.LIBRARY,
                                    onClick = { onNavigateToTab(VeritasHomeTab.LIBRARY) },
                                    icon = { color, size ->
                                        Icon(
                                            imageVector = if (activeNavTab == VeritasHomeTab.LIBRARY) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook,
                                            contentDescription = "Library",
                                            tint = color,
                                            modifier = Modifier.size(size)
                                        )
                                    },
                                    label = "Library",
                                    showLabel = showNavLabels
                                )

                                BottomNavItem(
                                    selected = activeNavTab == VeritasHomeTab.NOTES,
                                    onClick = { onNavigateToTab(VeritasHomeTab.NOTES) },
                                    icon = { color, size ->
                                        Icon(
                                            imageVector = if (activeNavTab == VeritasHomeTab.NOTES) Icons.Filled.EditNote else Icons.Outlined.EditNote,
                                            contentDescription = "Notes",
                                            tint = color,
                                            modifier = Modifier.size(size)
                                        )
                                    },
                                    label = "Notes",
                                    showLabel = showNavLabels,
                                    modifier = Modifier.onGloballyPositioned { OnboardingController.updateBounds("notes_tab", it) }
                                )

                                BottomNavItem(
                                    selected = activeNavTab == VeritasHomeTab.STUDY,
                                    onClick = { onNavigateToTab(VeritasHomeTab.STUDY) },
                                    icon = { color, size ->
                                        Icon(
                                            imageVector = if (activeNavTab == VeritasHomeTab.STUDY) Icons.Filled.Layers else Icons.Outlined.Layers,
                                            contentDescription = "Study",
                                            tint = color,
                                            modifier = Modifier.size(size)
                                        )
                                    },
                                    label = "Study",
                                    showLabel = showNavLabels,
                                    modifier = Modifier.onGloballyPositioned { OnboardingController.updateBounds("study_tab", it) }
                                )
                            }
                        }
                    }
                }

}

@Preview(showBackground = true)
@Composable
internal fun LibraryBottomNavBarPreview() {
    MaterialTheme {
        LibraryBottomNavBar(
            activeNavTab = VeritasHomeTab.HOME,
            showNavLabels = true,
            onNavigateToTab = {}
        )
    }
}
