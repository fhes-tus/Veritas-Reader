package com.veritas.reader.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritas.reader.ui.OnboardingController
import com.veritas.reader.ui.OnboardingStep
import kotlinx.coroutines.delay
import java.util.Random

// --------------------------------------------------------------------
// 1. Confetti Particle System
// --------------------------------------------------------------------

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shapeType: Int // 0: circle, 1: rectangle, 2: triangle
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    var particles by remember { mutableStateOf(emptyList<ConfettiParticle>()) }
    var durationCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val random = Random()
        val colors = listOf(
            Color(0xFFFFC107), // Yellow
            Color(0xFFFF5722), // Orange
            Color(0xFF4CAF50), // Green
            Color(0xFF2196F3), // Blue
            Color(0xFF9C27B0), // Purple
            Color(0xFFE91E63), // Pink
            Color(0xFF00BCD4)  // Cyan
        )
        
        particles = List(120) {
            ConfettiParticle(
                x = 0.1f + random.nextFloat() * 0.8f,
                y = -0.2f - random.nextFloat() * 0.4f,
                speedY = 0.008f + random.nextFloat() * 0.015f,
                speedX = -0.006f + random.nextFloat() * 0.012f,
                color = colors[random.nextInt(colors.size)],
                size = 12f + random.nextFloat() * 18f,
                rotation = random.nextFloat() * 360f,
                rotationSpeed = -6f + random.nextFloat() * 12f,
                shapeType = random.nextInt(3)
            )
        }

        while (durationCount < 240) { // ~4 seconds at 60fps
            withFrameMillis {
                particles = particles.map { p ->
                    val newY = p.y + p.speedY
                    val newX = p.x + p.speedX
                    val newRotation = p.rotation + p.rotationSpeed
                    // Gravity and wind drift simulated
                    p.copy(
                        x = if (newX < -0.05f) 1.05f else if (newX > 1.05f) -0.05f else newX,
                        y = if (newY > 1.1f) -0.1f else newY,
                        rotation = newRotation
                    )
                }
            }
            durationCount++
        }
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { p ->
            val px = p.x * canvasWidth
            val py = p.y * canvasHeight

            if (py in -100f..(canvasHeight + 100f)) {
                withTransform({
                    rotate(p.rotation, pivot = Offset(px, py))
                }) {
                    when (p.shapeType) {
                        1 -> { // Rectangle / Ribbon
                            drawRect(
                                color = p.color,
                                topLeft = Offset(px - p.size, py - p.size / 2f),
                                size = Size(p.size * 2f, p.size)
                            )
                        }
                        2 -> { // Triangle
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(px, py - p.size)
                                lineTo(px - p.size, py + p.size)
                                lineTo(px + p.size, py + p.size)
                                close()
                            }
                            drawPath(path = path, color = p.color)
                        }
                        else -> { // Circle / Dot
                            drawCircle(
                                color = p.color,
                                radius = p.size / 1.5f,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------
// 2. Spotlight Overlay Layout
// --------------------------------------------------------------------

@Composable
fun OnboardingSpotlightOverlay(
    step: OnboardingStep,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val targetBounds = OnboardingController.componentBounds[step.targetKey ?: ""]
    val hasTarget = step.targetKey != null && targetBounds != null
    
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Convert dimensions outside the canvas to be safe
    val paddingPx = with(density) { 8.dp.toPx() }
    val cornerRadiusPx = with(density) { 16.dp.toPx() }
    val cardSpacingPx = with(density) { 20.dp.toPx() }
    val cardHeightEstPx = with(density) { 200.dp.toPx() }
    val minTopMarginPx = with(density) { 10.dp.toPx() }

    // Pulsing cutout glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    // Prevent clicks passing to behind views
                }
            }
    ) {
        // Transparent black layer with a clear cutout
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawIntoCanvas { canvas ->
                canvas.withSaveLayer(
                    bounds = Rect(0f, 0f, canvasWidth, canvasHeight),
                    paint = Paint()
                ) {
                    // Dark dimming overlay
                    drawRect(color = Color.Black.copy(alpha = 0.76f))

                    if (hasTarget) {
                        val rect = targetBounds.inflate(paddingPx)
                        
                        // Clear the shape where the view lies
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = rect.topLeft,
                            size = rect.size,
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                            blendMode = BlendMode.Clear
                        )
                    }
                }
            }
        }

        // Draw glowing neon stroke overlay on top of canvas for visual feedback
        if (hasTarget) {
            val baseRect = targetBounds.inflate(paddingPx)
            val glowWidth = baseRect.width * pulseScale
            val glowHeight = baseRect.height * pulseScale
            val dx = (glowWidth - baseRect.width) / 2
            val dy = (glowHeight - baseRect.height) / 2
            
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (baseRect.left - dx).toDp() },
                        y = with(density) { (baseRect.top - dy).toDp() }
                    )
                    .size(
                        width = with(density) { glowWidth.toDp() },
                        height = with(density) { glowHeight.toDp() }
                    )
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        // Display explanation card
        val isCardBelow = if (hasTarget) {
            targetBounds.center.y < screenHeightPx / 2
        } else {
            true // centered default fallback
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            val targetYOffset = if (hasTarget) {
                if (isCardBelow) {
                    val bottomPx = targetBounds.bottom + cardSpacingPx
                    with(density) { bottomPx.toDp() }
                } else {
                    val topPx = targetBounds.top - cardHeightEstPx
                    with(density) { topPx.coerceAtLeast(minTopMarginPx).toDp() }
                }
            } else {
                // Center it vertically when there is no target
                val estCardHeightDp = 220.dp
                val screenHeightDp = configuration.screenHeightDp.dp
                ((screenHeightDp - estCardHeightDp) / 2f).coerceAtLeast(0.dp)
            }
            val yOffset by animateDpAsState(
                targetValue = targetYOffset,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "yOffset"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = yOffset)
            ) {
                OnboardingInfoCard(
                    step = step,
                    onNext = onNext,
                    onBack = onBack,
                    onDismiss = onDismiss,
                    showLiveTip = step.targetKey != null && targetBounds == null
                )
            }
        }
    }
}

@Composable
fun OnboardingInfoCard(
    step: OnboardingStep,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    showLiveTip: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 380.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            if (step == OnboardingStep.WELCOME) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    com.veritas.reader.BrandMark(compact = false)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Tour",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = step.body,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (showLiveTip) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tip",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tip: Open a reading to see this live in the reader!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                if (step != OnboardingStep.WELCOME) {
                    TextButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Next / Finish button
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(50)
                ) {
                    val label = if (step == OnboardingStep.CONGRATULATIONS) "Finish" else "Next"
                    Text(label, style = MaterialTheme.typography.labelMedium)
                    if (step != OnboardingStep.CONGRATULATIONS) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------
// 3. Quest Checklist Composable
// --------------------------------------------------------------------

@Composable
fun OnboardingQuestChecklist(
    questTourDone: Boolean,
    questImportDone: Boolean,
    questSpeedDone: Boolean,
    questBookmarkDone: Boolean,
    onStartTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val isTourActive = OnboardingController.activeStep != null
    LaunchedEffect(isTourActive) {
        if (isTourActive) {
            isExpanded = false
        }
    }

    val completedCount = listOf(questTourDone, questImportDone, questSpeedDone, questBookmarkDone).count { done -> done }
    val progress = completedCount / 4f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isExpanded = isExpanded == false
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(16.dp)
        ) {
            // Header summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Quests",
                        tint = if (progress == 1f) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = if (progress == 1f) "All Quests Complete! 🎉" else "Onboarding Quests",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedCount of 4 missions completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = isExpanded == false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress bar
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (progress == 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Checklist contents
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                QuestItemRow(
                    title = "Take the guided tour",
                    desc = "Learn the layout of the app with our quick guide.",
                    done = questTourDone
                )
                QuestItemRow(
                    title = "Import your first reading",
                    desc = "Add any PDF, EPUB, docx, or link using the + button.",
                    done = questImportDone
                )
                QuestItemRow(
                    title = "Tune voice speed or pitch",
                    desc = "Adjust playback parameters in the playback studio.",
                    done = questSpeedDone
                )
                QuestItemRow(
                    title = "Bookmark a sentence",
                    desc = "Long press text in the reader to save a bookmark.",
                    done = questBookmarkDone
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStartTour()
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Guided Tour",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (questTourDone) "Restart Guided Tour" else "Start Guided Tour",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun QuestItemRow(
    title: String,
    desc: String,
    done: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (done) "Completed" else "Incomplete",
            tint = if (done) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (done) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
