package com.veritas.reader.ui.screens

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritas.reader.ui.ReaderPersona
import com.veritas.reader.ui.ReaderPersonas
import com.veritas.reader.ui.rememberVeritasHaptics

/**
 * Onboarding persona selection screen allowing users to pick their reader profile type.
 */
@Composable
fun OnboardingPersonaSelectionScreen(
    selectedPersona: String,
    onSelectPersona: (ReaderPersona) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "What best describes you?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This information will help guide our development efforts to provide features and improvements that are relevant to you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Choose 1:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2x2 Grid of Persona Squircle Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ReaderPersonas.chunked(2).forEach { rowPersonas ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowPersonas.forEach { persona ->
                        val isSelected = persona.id == selectedPersona
                        PersonaSquircleCard(
                            persona = persona,
                            isSelected = isSelected,
                            onSelect = { onSelectPersona(persona) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Squircle card for a single Reader Persona.
 */
@Composable
fun PersonaSquircleCard(
    persona: ReaderPersona,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberVeritasHaptics()
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.025f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "personaCardScale"
    )

    Card(
        onClick = {
            haptic.select()
            onSelect()
        },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 1.dp
        ),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(175.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                when (persona.id) {
                    "student" -> StudentAnimatedIcon(isSelected = isSelected)
                    "educator" -> EducatorAnimatedIcon(isSelected = isSelected)
                    "professional" -> ProfessionalAnimatedIcon(isSelected = isSelected)
                    else -> BookLoverAnimatedIcon(isSelected = isSelected)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = persona.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = persona.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}

/**
 * Pulsing animated icon for Student persona.
 */
@Composable
fun StudentAnimatedIcon(isSelected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "studentPulse")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "starScale"
    )
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "starAlpha"
    )
    val glassesPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glassesPulse"
    )

    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height

        // Sparkle stars on top
        val star1X = w * 0.75f
        val star1Y = h * 0.2f
        val star2X = w * 0.35f
        val star2Y = h * 0.25f

        drawCircle(
            color = tint.copy(alpha = starAlpha),
            radius = 3.5f * starScale,
            center = Offset(star1X, star1Y)
        )
        drawCircle(
            color = tint.copy(alpha = 1f - starAlpha * 0.5f),
            radius = 2.5f * (2f - starScale),
            center = Offset(star2X, star2Y)
        )

        // Eyeglasses frame
        val leftLensCenter = Offset(w * 0.32f, h * 0.65f)
        val rightLensCenter = Offset(w * 0.68f, h * 0.65f)
        val lensRadius = 12f * (if (isSelected) glassesPulse else 1.0f)

        drawCircle(
            color = tint,
            radius = lensRadius,
            center = leftLensCenter,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
        )
        drawCircle(
            color = tint,
            radius = lensRadius,
            center = rightLensCenter,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
        )

        // Connecting bridge
        drawLine(
            color = tint,
            start = Offset(leftLensCenter.x + lensRadius, leftLensCenter.y - 2f),
            end = Offset(rightLensCenter.x - lensRadius, rightLensCenter.y - 2f),
            strokeWidth = 3.5f
        )

        // Left temple
        drawLine(
            color = tint,
            start = Offset(leftLensCenter.x - lensRadius, leftLensCenter.y),
            end = Offset(leftLensCenter.x - lensRadius - 6f, leftLensCenter.y - 6f),
            strokeWidth = 3f
        )
        // Right temple
        drawLine(
            color = tint,
            start = Offset(rightLensCenter.x + lensRadius, rightLensCenter.y),
            end = Offset(rightLensCenter.x + lensRadius + 6f, rightLensCenter.y - 6f),
            strokeWidth = 3f
        )
    }
}

/**
 * Pulsing animated icon for Educator persona.
 */
@Composable
fun EducatorAnimatedIcon(isSelected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "educatorPulse")
    val p1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "p1"
    )
    val p2 by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "p2"
    )

    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val blockSize = 11f
        val gap = 4f
        val centerX = w / 2f
        val centerY = h / 2f

        // 4 geometric blocks (like the reference icon)
        // Top-left
        drawRoundRect(
            color = tint,
            topLeft = Offset(centerX - blockSize * (if (isSelected) p1 else 1f) - gap, centerY - blockSize * (if (isSelected) p1 else 1f) - gap),
            size = Size(blockSize * (if (isSelected) p1 else 1f), blockSize * (if (isSelected) p1 else 1f)),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Top-right
        drawRoundRect(
            color = tint,
            topLeft = Offset(centerX + gap, centerY - blockSize * (if (isSelected) p2 else 1f) - gap),
            size = Size(blockSize * (if (isSelected) p2 else 1f), blockSize * (if (isSelected) p2 else 1f)),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Bottom-left
        drawRoundRect(
            color = tint,
            topLeft = Offset(centerX - blockSize * (if (isSelected) p2 else 1f) - gap, centerY + gap),
            size = Size(blockSize * (if (isSelected) p2 else 1f), blockSize * (if (isSelected) p2 else 1f)),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Bottom-right
        drawRoundRect(
            color = tint,
            topLeft = Offset(centerX + gap, centerY + gap),
            size = Size(blockSize * (if (isSelected) p1 else 1f), blockSize * (if (isSelected) p1 else 1f)),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Center micro-connector dot
        drawCircle(
            color = tint.copy(alpha = 0.7f),
            radius = 2.5f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * Pulsing animated icon for Professional persona.
 */
@Composable
fun ProfessionalAnimatedIcon(isSelected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "proClock")
    val handAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "clockHand"
    )
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "badgePulse"
    )

    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = 16f * (if (isSelected) badgePulse else 1f)

        // Rounded trapezoid/badge frame
        drawRoundRect(
            color = tint,
            topLeft = Offset(cx - r - 4f, cy - r - 4f),
            size = Size((r + 4f) * 2f, (r + 4f) * 2f),
            cornerRadius = CornerRadius(10f, 10f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
        )

        // Center clock hub
        drawCircle(
            color = tint,
            radius = 3.5f,
            center = Offset(cx, cy)
        )

        // Hour hand (fixed)
        drawLine(
            color = tint,
            start = Offset(cx, cy),
            end = Offset(cx, cy - 8f),
            strokeWidth = 3.5f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Minute hand (rotating)
        val angleRad = Math.toRadians((if (isSelected) handAngle else 90f).toDouble())
        val handLen = 10f
        drawLine(
            color = tint,
            start = Offset(cx, cy),
            end = Offset(cx + (handLen * Math.cos(angleRad)).toFloat(), cy + (handLen * Math.sin(angleRad)).toFloat()),
            strokeWidth = 2.5f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

/**
 * Pulsing animated icon for Book Lover persona.
 */
@Composable
fun BookLoverAnimatedIcon(isSelected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "flowerPulse")
    val flowerRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "flowerRot"
    )
    val petalScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "petalScale"
    )

    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val bgColor = MaterialTheme.colorScheme.background

    Canvas(modifier = Modifier.size(48.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val petalCount = 8
        val scale = if (isSelected) petalScale else 1f

        withTransform({
            rotate(if (isSelected) flowerRotation else 0f, pivot = Offset(cx, cy))
        }) {
            for (i in 0 until petalCount) {
                val angle = (i * 360f / petalCount)
                withTransform({
                    rotate(angle, pivot = Offset(cx, cy))
                }) {
                    drawOval(
                        color = tint,
                        topLeft = Offset(cx - 3.5f * scale, cy - 18f * scale),
                        size = Size(7f * scale, 16f * scale)
                    )
                }
            }
            // Center ring
            drawCircle(
                color = bgColor,
                radius = 5f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = tint,
                radius = 5f,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }
}
