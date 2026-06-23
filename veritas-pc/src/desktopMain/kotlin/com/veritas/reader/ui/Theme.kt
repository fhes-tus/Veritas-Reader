package com.veritas.reader.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VeritasLightColorScheme = lightColorScheme(
    primary = Color(0xFF7C6FFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0F3FF),
    onPrimaryContainer = Color(0xFF7C6FFF),
    secondary = Color(0xFF5B4FCF),
    secondaryContainer = Color(0xFFDCE2FF),
    onSecondaryContainer = Color(0xFF5B4FCF),
    tertiary = Color(0xFF1A1A2E),
    tertiaryContainer = Color(0xFFEAEAEE),
    onTertiaryContainer = Color(0xFF1A1A2E),
    background = Color(0xFFF4F6FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF4F6FA),
    onSurface = Color(0xFF1A1A2E),
    onSurfaceVariant = Color(0xFF545464),
    outline = Color(0xFFC8C8D0),
    outlineVariant = Color(0xFFEAEAEE),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A)
)

val VeritasDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4E6),
    onPrimary = Color(0xFF002124),
    primaryContainer = Color(0xFF003B42),
    onPrimaryContainer = Color(0xFFB8F4FA),
    secondary = Color(0xFFCAD3D7),
    secondaryContainer = Color(0xFF20272B),
    onSecondaryContainer = Color(0xFFE6EEF2),
    tertiary = Color(0xFFD8B7FF),
    tertiaryContainer = Color(0xFF332245),
    background = Color(0xFF050505),
    surface = Color(0xFF101010),
    surfaceVariant = Color(0xFF1C1C1E),
    onSurface = Color(0xFFEFF7FA),
    onSurfaceVariant = Color(0xFFC2CCD2)
)

@Composable
fun VeritasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VeritasDarkColorScheme else VeritasLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
