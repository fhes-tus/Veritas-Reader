package com.veritas.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object VeritasThemeState {
    var themeId by mutableStateOf(VeritasThemeCatalog.DEFAULT_ID)
    var themePackId by mutableStateOf(VeritasThemePackCatalog.DEFAULT_ID)
}

object VeritasPackStyle {
    @Composable
    fun currentPackId(): String =
        VeritasThemePackCatalog.normalizePackId(VeritasThemeState.themePackId)

    @Composable
    fun cardShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(34.dp)
        "liquid_glass" -> RoundedCornerShape(42.dp)
        "one_ui" -> RoundedCornerShape(28.dp)
        else -> RoundedCornerShape(18.dp)
    }

    @Composable
    fun compactShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(28.dp)
        "liquid_glass" -> RoundedCornerShape(34.dp)
        "one_ui" -> RoundedCornerShape(18.dp)
        else -> RoundedCornerShape(12.dp)
    }

    @Composable
    fun chipShape(): RoundedCornerShape = when (currentPackId()) {
        "material_you" -> RoundedCornerShape(50)
        "liquid_glass" -> RoundedCornerShape(36.dp)
        "one_ui" -> RoundedCornerShape(16.dp)
        else -> RoundedCornerShape(10.dp)
    }

    @Composable
    fun surfaceAlpha(): Float = when (currentPackId()) {
        "liquid_glass" -> 0.42f
        "one_ui" -> 0.94f
        "material_you" -> 0.88f
        else -> 0.78f
    }

    @Composable
    fun backgroundBrush(colorScheme: ColorScheme): Brush = when (currentPackId()) {
        "liquid_glass" -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.86f),
                colorScheme.primaryContainer.copy(alpha = 0.22f),
                colorScheme.background
            )
        )

        "one_ui" -> Brush.verticalGradient(
            listOf(
                colorScheme.secondaryContainer.copy(alpha = 0.20f),
                colorScheme.background,
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.72f)
            )
        )

        "material_you" -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.76f),
                colorScheme.primaryContainer.copy(alpha = 0.22f),
                colorScheme.tertiaryContainer.copy(alpha = 0.16f),
                colorScheme.background
            )
        )

        else -> Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.primaryContainer.copy(alpha = 0.08f),
                colorScheme.background,
                colorScheme.secondaryContainer.copy(alpha = 0.10f)
            )
        )
    }

    @Composable
    fun label(): String = VeritasThemePackCatalog.displayName(currentPackId())
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false) {
    // Standard text-based brand mark for desktop target to avoid R.drawable dependencies
    Box(
        modifier = modifier
            .size(if (compact) 24.dp else 58.dp)
            .clip(if (compact) MaterialTheme.shapes.extraSmall else MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "V",
            color = MaterialTheme.colorScheme.onPrimary,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleLarge
        )
    }
}

data class VoicePreset(
    val name: String,
    val rate: Float,
    val pitch: Float,
    val summary: String
)

fun voicePresets(): List<VoicePreset> = listOf(
    VoicePreset("Balanced", 1.0f, 1.0f, "Everyday reading with neutral timing."),
    VoicePreset("Study focus", 0.88f, 0.98f, "Slower pacing for dense material and note taking."),
    VoicePreset("Quick scan", 1.35f, 1.02f, "Fast skim for review and familiar documents."),
    VoicePreset("Story warm", 0.96f, 0.92f, "Softer narration for fiction and long listening."),
    VoicePreset("Clear lecture", 1.06f, 1.06f, "Brighter delivery for technical or academic text."),
    VoicePreset("Calm night", 0.82f, 0.90f, "Low, relaxed reading for quiet listening.")
)

fun themePreviewColors(themeId: String): List<Color> {
    return when (VeritasThemeCatalog.normalizeThemeId(themeId)) {
        "light" -> listOf(Color(0xFFF7F9FB), Color(0xFF182442), Color(0xFFD0E1FB))
        "neon" -> listOf(Color(0xFF000000), Color(0xFF00FFFF), Color(0xFF39FF14))
        "solarized_dark" -> listOf(Color(0xFF002B36), Color(0xFF5FC8BF), Color(0xFFD7A84A))
        "tomorrow_night_blue" -> listOf(Color(0xFF071B37), Color(0xFFA9C7FF), Color(0xFFE9B872))
        "dark_high_contrast" -> listOf(Color.Black, Color.White, Color(0xFFFFD400))
        "white_high_contrast" -> listOf(Color.White, Color.Black, Color(0xFF004B65))
        "bw_gradient_light" -> listOf(Color(0xFFFFFFFF), Color(0xFF111111), Color(0xFFE6E6E6))
        "bw_gradient_dark" -> listOf(Color(0xFF000000), Color(0xFFF5F5F5), Color(0xFF242424))
        "blue_high_contrast" -> listOf(Color(0xFF001B3A), Color(0xFFBDE9FF), Color(0xFFFFF176))
        "one_dark_pro" -> listOf(Color(0xFF242A33), Color(0xFF86BFF2), Color(0xFFB7D99A))
        "github_dark" -> listOf(Color(0xFF0D1117), Color(0xFF7BB6FF), Color(0xFF69C779))
        "github_light" -> listOf(Color(0xFFF6F8FA), Color(0xFF075EB8), Color(0xFF1F7A3A))
        "dracula" -> listOf(Color(0xFF252837), Color(0xFFBDA8FF), Color(0xFFFFB3D5))
        "material_you" -> listOf(Color(0xFFF7F2FA), Color(0xFF5A477A), Color(0xFF6F4B57))
        "dark" -> listOf(Color(0xFF111827), Color(0xFF8EDCE6), Color(0xFFE4CCFF))
        else -> listOf(Color(0xFF0B0F14), Color(0xFF82D8E7), Color(0xFFD6C1FF))
    }
}

