package com.veritas.reader.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.veritas.reader.VeritasPackStyle
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.veritas.reader.ui.rememberVeritasHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritas.reader.ui.OnboardingController
import com.veritas.reader.ui.OnboardingStep
import kotlinx.coroutines.delay
import java.util.Random

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import com.veritas.reader.TutorialSpeaker
import com.veritas.reader.ui.ReaderPersona
import com.veritas.reader.ui.ReaderPersonas
import com.veritas.reader.ui.OnboardingPage
import com.veritas.reader.ui.VoiceAuditionPreset
import com.veritas.reader.ui.VoiceAuditionPresets
import com.veritas.reader.ui.ReadingInterestOption
import com.veritas.reader.ui.ReadingInterestOptions
import com.veritas.reader.ui.AiStudyFocusOption
import com.veritas.reader.ui.AiStudyFocusOptions
import com.veritas.reader.aiAssistantIcon

// --------------------------------------------------------------------
// 0. Revamped Full-Screen Onboarding Flow
// --------------------------------------------------------------------

@Composable
fun RevampedOnboardingFlow(
    initialUserName: String,
    initialReadingInterest: String,
    initialAiAssistant: String,
    onComplete: (name: String, interest: String, aiAssistant: String, speed: Float, pitch: Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = rememberVeritasHaptics()

    var currentPage by rememberSaveable { mutableStateOf(OnboardingPage.WELCOME_HERO) }
    var selectedPersona by rememberSaveable { mutableStateOf("student") }
    var userName by rememberSaveable { mutableStateOf(if (initialUserName.isBlank() || initialUserName.equals("Reader", ignoreCase = true)) "" else initialUserName) }
    var selectedInterest by rememberSaveable { mutableStateOf(initialReadingInterest.ifBlank { "Books & Novels" }) }
    var selectedPreset by remember { mutableStateOf(VoiceAuditionPresets[0]) }
    var selectedAi by rememberSaveable { mutableStateOf(initialAiAssistant.ifBlank { "gemini" }) }
    var selectedStudyFocus by rememberSaveable { mutableStateOf("deep_dive") }
    var isAuditionPlaying by remember { mutableStateOf(false) }
    var isNarrationMuted by rememberSaveable { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        TutorialSpeaker.init(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            TutorialSpeaker.stop()
        }
    }

    // Auto-narrate subtle description for each page
    LaunchedEffect(currentPage, isNarrationMuted) {
        if (!isNarrationMuted && !isAuditionPlaying) {
            TutorialSpeaker.stop()
            TutorialSpeaker.speak(currentPage.spokenDescription)
        } else if (isNarrationMuted) {
            TutorialSpeaker.stop()
        }
    }

    LaunchedEffect(currentPage) {
        if (currentPage == OnboardingPage.READY_CELEBRATION) {
            showConfetti = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar with Step Dots, Audio Narration Toggle & Skip Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OnboardingPage.values().forEach { page ->
                        val isCurrent = page == currentPage
                        val isPassed = page.pageIndex < currentPage.pageIndex
                        val animatedWidth by animateDpAsState(
                            targetValue = if (isCurrent) 24.dp else 8.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(animatedWidth)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isPassed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        )
                    }
                }

                // Top Actions: Subtle Narration Toggle + Skip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Spoken description narration toggle
                    IconButton(
                        onClick = {
                            haptic.toggle(isNarrationMuted)
                            isNarrationMuted = !isNarrationMuted
                            if (!isNarrationMuted) {
                                TutorialSpeaker.speak(currentPage.spokenDescription)
                            } else {
                                TutorialSpeaker.stop()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isNarrationMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isNarrationMuted) "Unmute narration" else "Mute narration",
                            tint = if (isNarrationMuted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Skip / Close Button
                    if (currentPage != OnboardingPage.READY_CELEBRATION) {
                        TextButton(
                            onClick = {
                                haptic.success()
                                TutorialSpeaker.stop()
                                onComplete(
                                    userName.trim().ifBlank { "Reader" },
                                    selectedInterest,
                                    selectedAi,
                                    selectedPreset.speed,
                                    selectedPreset.pitch
                                )
                            }
                        ) {
                            Text(
                                "Skip",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Animated Screen Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState.pageIndex > initialState.pageIndex) {
                            (slideInHorizontally { width -> width / 3 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width / 3 } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width / 3 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width / 3 } + fadeOut()
                            )
                        }
                    },
                    label = "onboarding_page_transition"
                ) { targetPage ->
                    when (targetPage) {
                        OnboardingPage.WELCOME_HERO -> OnboardingWelcomeHeroScreen()
                        OnboardingPage.PERSONA_SELECTION -> OnboardingPersonaSelectionScreen(
                            selectedPersona = selectedPersona,
                            onSelectPersona = { persona ->
                                selectedPersona = persona.id
                                selectedPreset = VoiceAuditionPresets.firstOrNull { it.id == persona.defaultPresetId } ?: VoiceAuditionPresets[0]
                                selectedStudyFocus = persona.defaultAiFocus
                                selectedInterest = when (persona.id) {
                                    "student" -> "PDF & Research"
                                    "educator" -> "Books & Novels"
                                    "professional" -> "Web & Articles"
                                    else -> "Books & Novels"
                                }
                            }
                        )
                        OnboardingPage.NAME_INPUT -> OnboardingNameInputScreen(
                            userName = userName,
                            onUserNameChange = { userName = it },
                            selectedInterest = selectedInterest,
                            onSelectInterest = {
                                haptic.select()
                                selectedInterest = it
                            }
                        )
                        OnboardingPage.VOICE_AUDITION -> OnboardingVoiceAuditionScreen(
                            selectedPreset = selectedPreset,
                            onSelectPreset = {
                                haptic.select()
                                selectedPreset = it
                                TutorialSpeaker.stop()
                                isAuditionPlaying = false
                            },
                            isPlaying = isAuditionPlaying,
                            onTogglePlay = {
                                haptic.toggle(!isAuditionPlaying)
                                if (isAuditionPlaying) {
                                    TutorialSpeaker.stop()
                                    isAuditionPlaying = false
                                } else {
                                    isAuditionPlaying = true
                                    TutorialSpeaker.speakWithTuning(
                                        text = selectedPreset.sampleText,
                                        speed = selectedPreset.speed,
                                        pitch = selectedPreset.pitch
                                    )
                                }
                            }
                        )
                        OnboardingPage.AI_SELECTION -> OnboardingAiSelectionScreen(
                            selectedAi = selectedAi,
                            onSelectAi = {
                                haptic.select()
                                selectedAi = it
                            },
                            selectedFocus = selectedStudyFocus,
                            onSelectFocus = {
                                haptic.select()
                                selectedStudyFocus = it
                            }
                        )
                        OnboardingPage.FEATURE_SHOWCASE -> OnboardingFeatureShowcaseScreen()
                        OnboardingPage.READY_CELEBRATION -> OnboardingReadyCelebrationScreen(
                            userName = userName,
                            personaTitle = ReaderPersonas.firstOrNull { it.id == selectedPersona }?.title ?: "Reader",
                            interest = selectedInterest,
                            voiceTitle = selectedPreset.title,
                            aiTitle = selectedAi.replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }

            // Bottom Navigation Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage != OnboardingPage.WELCOME_HERO && currentPage != OnboardingPage.READY_CELEBRATION) {
                    OutlinedButton(
                        onClick = {
                            TutorialSpeaker.stop()
                            isAuditionPlaying = false
                            val prevIndex = (currentPage.pageIndex - 1).coerceAtLeast(0)
                            currentPage = OnboardingPage.values()[prevIndex]
                        },
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        haptic.success()
                        TutorialSpeaker.stop()
                        isAuditionPlaying = false
                        if (currentPage == OnboardingPage.READY_CELEBRATION) {
                            onComplete(
                                userName.trim().ifBlank { "Reader" },
                                selectedInterest,
                                selectedAi,
                                selectedPreset.speed,
                                selectedPreset.pitch
                            )
                        } else {
                            val nextIndex = (currentPage.pageIndex + 1).coerceAtMost(OnboardingPage.values().lastIndex)
                            currentPage = OnboardingPage.values()[nextIndex]
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    val label = when (currentPage) {
                        OnboardingPage.WELCOME_HERO -> "Get Started"
                        OnboardingPage.READY_CELEBRATION -> "Start Guided Tour"
                        else -> "Continue"
                    }
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (currentPage == OnboardingPage.READY_CELEBRATION) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = label,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (showConfetti) {
            ConfettiOverlay(
                modifier = Modifier.fillMaxSize(),
                onFinished = { showConfetti = false }
            )
        }
    }
}

// --------------------------------------------------------------------
// Sub-screens for Revamped Onboarding
// --------------------------------------------------------------------

@Composable
fun OnboardingWelcomeHeroScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "heroGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Glowing Brand Badge - Sleek App Icon with Pulsing Gold Aura
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(110.dp)
        ) {
            // Pulsating gold glow aura
            Box(
                modifier = Modifier
                    .size(94.dp * glowPulse)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(100)
                    )
            )
            // Sleek icon with subtle theme-dependent gold border
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                com.veritas.reader.BrandMark(compact = false)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Read at the Speed of Thought",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Experience a next-generation reading environment with fluid voice narration, AI-powered study handoffs, and instant PDF canvas layout.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Feature Pillars
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OnboardingHeroPillarRow(
                icon = "🎙️",
                title = "Natural Voice Synthesis",
                desc = "Multi-voice narration with pitch, speed & pronunciation tuning."
            )
            OnboardingHeroPillarRow(
                icon = "🤖",
                title = "6 Official AI Assistants",
                desc = "Instant handoff to ChatGPT, Gemini, Claude, Copilot, Perplexity & Grok."
            )
            OnboardingHeroPillarRow(
                icon = "📑",
                title = "Dual Document Studio",
                desc = "Seamlessly switch between original PDF layouts and responsive text."
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OnboardingHeroPillarRow(
    icon: String,
    title: String,
    desc: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// --------------------------------------------------------------------
// Persona Selection (Nano Banana Inspired 2x2 with Pulsing Micro-Animations)
// --------------------------------------------------------------------

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

// --------------------------------------------------------------------
// Pulsing Animated Persona Icons (Nano Banana Micro-Delight)
// --------------------------------------------------------------------

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

// --------------------------------------------------------------------
// Name Input & Reading Focus Screen
// --------------------------------------------------------------------

@Composable
fun OnboardingNameInputScreen(
    userName: String,
    onUserNameChange: (String) -> Unit,
    selectedInterest: String,
    onSelectInterest: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Make It Yours",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Enter your name so Veritas can welcome you every time you open your library and personalize your study decks.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Name input field
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            placeholder = { Text("e.g., Alex") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Name",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (userName.isNotBlank()) {
            Text(
                text = "🎉 Welcome aboard, ${userName.trim()}! Your profile is personalized.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "✨ We'll use your name on your reading streak heatmap and study session greetings.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "What do you read most?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ReadingInterestOptions.forEach { option ->
                val isSelected = selectedInterest.equals(option.label, ignoreCase = true) || selectedInterest.equals(option.id, ignoreCase = true)
                Card(
                    onClick = { onSelectInterest(option.label) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(option.iconEmoji, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = option.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OnboardingVoiceAuditionScreen(
    selectedPreset: VoiceAuditionPreset,
    onSelectPreset: (VoiceAuditionPreset) -> Unit,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Meet Your Narrator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Choose a voice delivery preset and audition live audio playback.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Preset Chips
        Text(
            text = "Voice Delivery Presets",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            VoiceAuditionPresets.forEach { preset ->
                val isSelected = preset.id == selectedPreset.id
                Card(
                    onClick = { onSelectPreset(preset) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(preset.iconEmoji, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${preset.speed}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Live Audition Player Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Audio Sample",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Live Audition: ${selectedPreset.title}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Equalizer Wave Indicator
                    if (isPlaying) {
                        LiveWaveEqualizer()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "\"${selectedPreset.sampleText}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onTogglePlay,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isPlaying) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play Sample",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPlaying) "Stop Preview" else "Play Audio Sample")
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun LiveWaveEqualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse), label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse), label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse), label = "h3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(20.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).height(h1.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary))
        Box(modifier = Modifier.width(3.dp).height(h2.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.tertiary))
        Box(modifier = Modifier.width(3.dp).height(h3.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun OnboardingAiSelectionScreen(
    selectedAi: String,
    onSelectAi: (String) -> Unit,
    selectedFocus: String,
    onSelectFocus: (String) -> Unit
) {
    val aiAssistants = listOf(
        "gemini" to "Google Gemini",
        "chatgpt" to "ChatGPT",
        "claude" to "Claude",
        "copilot" to "Copilot",
        "perplexity" to "Perplexity",
        "grok" to "xAI Grok"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pair Your AI Study Partner",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Choose your default assistant for instant study handoffs, summaries, quizzes, and vocabulary.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Official AI Assistants",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2-column Grid of 6 AI Assistants
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            aiAssistants.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (id, name) ->
                        val isSelected = selectedAi.equals(id, ignoreCase = true)
                        Card(
                            onClick = { onSelectAi(id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = aiAssistantIcon(id),
                                    contentDescription = name,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Primary Study Focus",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AiStudyFocusOptions.forEach { option ->
                val isSelected = selectedFocus == option.id
                Card(
                    onClick = { onSelectFocus(option.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(option.iconEmoji, fontSize = 20.sp)
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OnboardingFeatureShowcaseScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Reading Superpowers",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Here is what makes reading in Veritas uniquely powerful.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OnboardingFeatureCard(
                icon = "📑",
                badge = "Dual-Engine",
                title = "Original Canvas & Text Studio",
                desc = "Toggle between original visual PDF layout and responsive reflowed text with sentence-by-sentence karaoke highlighting."
            )
            OnboardingFeatureCard(
                icon = "🗣️",
                badge = "Multi-App Handoff",
                title = "Translation & Spoken Read-Out",
                desc = "Send structured translation requests to your priority translation tools and listen to spoken read-out of both original and translated text."
            )
            OnboardingFeatureCard(
                icon = "📊",
                badge = "Private & Local",
                title = "On-Device Reading Insights",
                desc = "Track your daily streaks, monthly time heatmaps, and listening history with zero cloud tracking."
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OnboardingFeatureCard(
    icon: String,
    badge: String,
    title: String,
    desc: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 28.sp)
                SuggestionChip(
                    onClick = {},
                    label = { Text(badge, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

// --------------------------------------------------------------------
// Celebratory Animation Badge (Nano Banana Delight)
// --------------------------------------------------------------------

@Composable
fun CelebrationAnimatedBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebrationAnim")

    val ringScale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Restart),
        label = "ring1"
    )
    val ringAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Restart),
        label = "ringAlpha1"
    )

    val ringScale2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 1000, easing = EaseInOutSine), RepeatMode.Restart),
        label = "ring2"
    )
    val ringAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 1000, easing = EaseInOutSine), RepeatMode.Restart),
        label = "ringAlpha2"
    )

    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bounce"
    )
    val tiltAngle by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "tilt"
    )
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(700, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "sparkle"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(130.dp)
    ) {
        // Canvas for expanding pulse rings and sparkling particle bursts
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val baseRadius = 42f

            // Pulse Ring 1
            drawCircle(
                color = primaryColor.copy(alpha = ringAlpha1),
                radius = baseRadius * ringScale1,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
            )

            // Pulse Ring 2
            drawCircle(
                color = secondaryColor.copy(alpha = ringAlpha2),
                radius = baseRadius * ringScale2,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )

            // Orbiting sparkles
            val sparkleOffsets = listOf(
                Offset(cx - 42f, cy - 32f),
                Offset(cx + 42f, cy - 28f),
                Offset(cx + 38f, cy + 38f),
                Offset(cx - 36f, cy + 36f),
                Offset(cx, cy - 48f)
            )
            sparkleOffsets.forEachIndexed { idx, pos ->
                val alpha = if (idx % 2 == 0) sparkleAlpha else (1f - sparkleAlpha)
                val dotColor = if (idx % 3 == 0) primaryColor else if (idx % 3 == 1) secondaryColor else tertiaryColor
                drawCircle(
                    color = dotColor.copy(alpha = alpha),
                    radius = 3.5f * (0.8f + alpha * 0.4f),
                    center = pos
                )
            }
        }

        // Center bouncing celebration badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = bounceScale
                    scaleY = bounceScale
                    rotationZ = tiltAngle
                }
                .size(72.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Text(
                text = "🎉",
                fontSize = 36.sp
            )
        }
    }
}

@Composable
fun OnboardingReadyCelebrationScreen(
    userName: String,
    personaTitle: String = "Student",
    interest: String,
    voiceTitle: String,
    aiTitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        CelebrationAnimatedBadge()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (userName.isNotBlank()) "You're Ready, ${userName.trim()}!" else "You're Ready to Read!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your personalized profile is configured. We're about to take a quick guided tour through your new reading environment.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Summary Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Configured Reading Profile",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                OnboardingSummaryRow(label = "Reader Name", value = userName.ifBlank { "Veritas Reader" })
                OnboardingSummaryRow(label = "Reader Persona", value = personaTitle)
                OnboardingSummaryRow(label = "Primary Focus", value = interest)
                OnboardingSummaryRow(label = "Voice Preset", value = voiceTitle)
                OnboardingSummaryRow(label = "AI Assistant", value = aiTitle)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OnboardingSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

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
    userName: String,
    onUserNameChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    isTransitioning: Boolean = false
) {
    val targetBounds = OnboardingController.componentBounds[step.targetKey ?: ""]
    
    var lastNonNullBounds by remember { mutableStateOf<Rect?>(null) }
    LaunchedEffect(targetBounds) {
        if (targetBounds != null) {
            lastNonNullBounds = targetBounds
        }
    }

    val baseBounds = targetBounds ?: lastNonNullBounds
    val hasTarget = step.targetKey != null && baseBounds != null
    
    // Animate the cutout alpha to fade out during step transitions
    val cutoutAlpha by animateFloatAsState(
        targetValue = if (isTransitioning) 0f else 1f,
        animationSpec = tween(durationMillis = 250),
        label = "cutoutAlpha"
    )

    // Animate the cutout coordinates to slide smoothly between locations
    val animatedLeft = animateFloatAsState(
        targetValue = if (hasTarget) baseBounds!!.left else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "left"
    )
    val animatedTop = animateFloatAsState(
        targetValue = if (hasTarget) baseBounds!!.top else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "top"
    )
    val animatedRight = animateFloatAsState(
        targetValue = if (hasTarget) baseBounds!!.right else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "right"
    )
    val animatedBottom = animateFloatAsState(
        targetValue = if (hasTarget) baseBounds!!.bottom else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bottom"
    )

    val animBounds = if (hasTarget) {
        Rect(
            left = animatedLeft.value,
            top = animatedTop.value,
            right = animatedRight.value,
            bottom = animatedBottom.value
        )
    } else {
        null
    }

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
        modifier = Modifier.fillMaxSize()
    ) {
        // Fullscreen touch barrier to prevent clicks from bleeding through to underlying home screen tabs and buttons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    // Consume background taps so underlying UI cannot be accidentally pressed
                }
        )

        // Transparent black layer with a clear cutout
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawIntoCanvas { canvas ->
                canvas.withSaveLayer(
                    bounds = Rect(0f, 0f, canvasWidth, canvasHeight),
                    paint = Paint()
                ) {
                    // Dark dimming overlay
                    drawRect(color = Color.Black.copy(alpha = 0.76f))

                    if (animBounds != null) {
                        val rect = animBounds.inflate(paddingPx)
                        
                        // Clear the shape where the view lies
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = rect.topLeft,
                            size = rect.size,
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                            blendMode = BlendMode.Clear
                        )

                        // Fade the cutout to dark when transitioning
                        if (cutoutAlpha < 1f) {
                            drawRoundRect(
                                color = Color.Black.copy(alpha = (1f - cutoutAlpha) * 0.76f),
                                topLeft = rect.topLeft,
                                size = rect.size,
                                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                            )
                        }
                    }
                }
            }
        }

        // Draw glowing neon stroke overlay on top of canvas for visual feedback
        if (animBounds != null) {
            val baseRect = animBounds.inflate(paddingPx)
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
                    .graphicsLayer { alpha = cutoutAlpha }
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
        val isCardBelow = if (animBounds != null) {
            animBounds.center.y < screenHeightPx / 2
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
            val targetYOffset = if (animBounds != null) {
                if (isCardBelow) {
                    val bottomPx = animBounds.bottom + cardSpacingPx
                    with(density) { bottomPx.toDp() }
                } else {
                    val topPx = animBounds.top - cardHeightEstPx
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
                    .graphicsLayer { alpha = cutoutAlpha }
            ) {
                OnboardingInfoCard(
                    step = step,
                    userName = userName,
                    onUserNameChanged = onUserNameChanged,
                    onNext = onNext,
                    onBack = onBack,
                    onDismiss = onDismiss,
                    showLiveTip = step.targetKey != null && targetBounds == null,
                    isTransitioning = isTransitioning
                )
            }
        }
    }
}

@Composable
fun OnboardingInfoCard(
    step: OnboardingStep,
    userName: String,
    onUserNameChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    showLiveTip: Boolean,
    isTransitioning: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 380.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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

            if (step == OnboardingStep.NAME_INPUT) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = userName,
                    onValueChange = onUserNameChanged,
                    label = { Text("Your Name") },
                    placeholder = { Text("Enter your name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                    TextButton(
                        onClick = onBack,
                        enabled = !isTransitioning
                    ) {
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
                    enabled = !isTransitioning,
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
    onDismissQuests: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val haptic = rememberVeritasHaptics()

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
                haptic.toggle(!isExpanded)
                isExpanded = isExpanded == false
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = VeritasPackStyle.cardBorder(MaterialTheme.colorScheme)
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDismissQuests != null) {
                        IconButton(
                            onClick = {
                                haptic.success()
                                onDismissQuests()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Quests",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = {
                            haptic.toggle(!isExpanded)
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
            }

            // Progress bar
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (progress == 1f) Color(0xFF2196F3) else MaterialTheme.colorScheme.primary,
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
                        haptic.success()
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
            tint = if (done) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
