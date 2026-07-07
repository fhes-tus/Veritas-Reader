package com.veritas.reader.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.veritas.reader.CoverExtractor
import com.veritas.reader.PlaybackStateStore
import com.veritas.reader.ReaderTrackerSnapshot
import com.veritas.reader.SavedDocument
import com.veritas.reader.VeritasPackStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Home hero card, per the user's sketch:
 *  - full-height cover on the left, shown WHOLE (Fit, never cropped)
 *  - title + live insight line vertically centered beside it, with the clear (x) at
 *    the row's right edge
 *  - progress bar under the title block
 *  - compact stat pills (streak — hidden under 2 days — / this week / done)
 *  - play button anchored bottom-right
 * When TTS is playing the active document the card becomes a live mini-player
 * (pulsing cover, live sentence line). Fixed geometry, cover decoded off-thread.
 */
@Composable
fun VeritasHomeHeroCard(
    tracker: ReaderTrackerSnapshot,
    continueDocument: SavedDocument?,
    gradient: Brush,
    onCardColor: Color,
    weeklyMinutes: Long,
    todayMinutes: Long,
    dailyGoalMinutes: Int = 20,
    onOpen: (SavedDocument) -> Unit,
    onPlayPause: (SavedDocument) -> Unit,
    onClear: (SavedDocument) -> Unit,
    onAddContent: () -> Unit
) {
    val context = LocalContext.current
    val doc = continueDocument

    // ── Async, downsampled cover decode (never on the main thread) ──
    var cover by remember(doc?.id) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(doc?.id) {
        cover = null
        val id = doc?.id ?: return@LaunchedEffect
        cover = withContext(Dispatchers.IO) {
            runCatching {
                val file = CoverExtractor.coverFile(context, id) ?: return@runCatching null
                if (!file.exists()) return@runCatching null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 512) sample *= 2
                BitmapFactory.decodeFile(
                    file.absolutePath,
                    BitmapFactory.Options().apply { inSampleSize = sample }
                )
            }.getOrNull()
        }
    }
    val coverAlpha by animateFloatAsState(
        targetValue = if (cover != null) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "coverFade"
    )

    // ── Live playback state for THIS document ──
    val isPlayingThis = doc != null &&
        PlaybackStateStore.isPlaying && PlaybackStateStore.activeDocumentId == doc.id
    val liveIndex = if (doc != null && PlaybackStateStore.activeDocumentId == doc.id) {
        PlaybackStateStore.currentIndex
    } else {
        doc?.currentIndex ?: 0
    }
    val chunkCount = (doc?.chunkCount ?: 0).coerceAtLeast(1)
    val progressTarget = ((liveIndex + 1).toFloat() / chunkCount.toFloat()).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        targetValue = if (doc != null) progressTarget else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "heroProgress"
    )

    // Subtle breathing pulse on the cover while listening.
    val pulse = rememberInfiniteTransition(label = "coverPulse")
    val coverScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlayingThis) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coverScale"
    )

    // ── Data-earned insight lines (rotating; live sentence line while listening) ──
    val percent = (progressTarget * 100).toInt()
    val insights = remember(doc?.id, percent, todayMinutes, tracker.currentStreak, tracker.documentsCompletedThisMonth, weeklyMinutes, isPlayingThis) {
        buildList {
            if (doc != null && percent in 5..99) add("You're $percent% through — keep going")
            if (todayMinutes > 0) add("${todayMinutes}m read today")
            if (tracker.currentStreak >= 2) add("${tracker.currentStreak}-day streak — keep it alive")
            if (tracker.documentsCompletedThisMonth >= 1) {
                add("${tracker.documentsCompletedThisMonth} book${if (tracker.documentsCompletedThisMonth == 1) "" else "s"} finished this month")
            }
            if (weeklyMinutes > 0) add("${weeklyMinutes}m this week")
            if (isEmpty()) add(if (doc != null) "Pick up where you left off" else "Import or open a document to begin")
        }
    }
    var insightIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(insights.size, isPlayingThis) {
        insightIndex = 0
        if (insights.size > 1 && !isPlayingThis) {
            while (true) {
                delay(6000)
                insightIndex = (insightIndex + 1) % insights.size
            }
        }
    }
    val insightLine = if (isPlayingThis) {
        "Listening • sentence ${liveIndex + 1} of $chunkCount"
    } else {
        insights[insightIndex % insights.size]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = VeritasPackStyle.cardShape(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .clickable { if (doc != null) onOpen(doc) else onAddContent() }
                .padding(14.dp)
        ) {
            // IntrinsicSize.Min lets the cover stretch to the exact height of the right
            // column, so the cover always spans the full card interior.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cover: full card height, whole image visible (Fit — never cropped).
                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = coverScale
                            scaleY = coverScale
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .background(onCardColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = onCardColor.copy(alpha = 0.55f),
                        modifier = Modifier.size(34.dp)
                    )
                    cover?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(coverAlpha),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 172.dp)
                ) {
                    // Center the title block vertically beside the cover.
                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = doc?.title ?: "No book in progress",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.ExtraBold,
                                color = onCardColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                            // Insight line crossfades in place inside a fixed-height slot.
                            Box(modifier = Modifier.height(18.dp), contentAlignment = Alignment.CenterStart) {
                                AnimatedContent(
                                    targetState = insightLine,
                                    transitionSpec = {
                                        (fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 2 })
                                            .togetherWith(fadeOut(tween(250)) + slideOutVertically(tween(250)) { -it / 2 })
                                    },
                                    label = "insightLine"
                                ) { line ->
                                    Text(
                                        text = line,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onCardColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        // Clear (x): mid-right, away from the play corner. Fixed slot.
                        Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                            if (doc != null) {
                                IconButton(onClick = { onClear(doc) }, modifier = Modifier.size(30.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear continue reading",
                                        tint = onCardColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = onCardColor,
                        trackColor = onCardColor.copy(alpha = 0.25f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Stat pills: streak only appears once it's earned (2+ days).
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tracker.currentStreak >= 2) {
                            HeroPill(text = "🔥 ${heroCountUp(tracker.currentStreak)} streak", color = onCardColor)
                        }
                        HeroPill(text = "${heroCountUp(weeklyMinutes.toInt())}m this week", color = onCardColor)
                    }

                    // Daily reading goal: thin progress line under the pills.
                    if (dailyGoalMinutes > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (todayMinutes.toFloat() / dailyGoalMinutes).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = onCardColor,
                            trackColor = onCardColor.copy(alpha = 0.25f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (todayMinutes >= dailyGoalMinutes) "Goal met — ${todayMinutes}m today"
                            else "Today ${todayMinutes} / ${dailyGoalMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = onCardColor.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Play: bottom-right thumb zone.
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(onCardColor.copy(alpha = 0.22f), CircleShape)
                                .clickable { if (doc != null) onPlayPause(doc) else onAddContent() },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = if (doc == null) "add" else if (isPlayingThis) "pause" else "play",
                                transitionSpec = {
                                    (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(tween(150)))
                                        .togetherWith(fadeOut(tween(100)))
                                },
                                label = "playPauseMorph"
                            ) { state ->
                                Icon(
                                    imageVector = when (state) {
                                        "add" -> Icons.Filled.Add
                                        "pause" -> Icons.Filled.Pause
                                        else -> Icons.Filled.PlayArrow
                                    },
                                    contentDescription = when (state) {
                                        "add" -> "Add content"
                                        "pause" -> "Pause"
                                        else -> "Play"
                                    },
                                    tint = onCardColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Rolls a stat from 0 (or its previous value) to the target so the pills count up
// like the Insights streak counter instead of popping in fully formed.
@Composable
private fun heroCountUp(value: Int): Int {
    var target by remember { mutableIntStateOf(0) }
    LaunchedEffect(value) { target = value }
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "heroCountUp"
    )
    return animated
}

@Composable
private fun HeroPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
