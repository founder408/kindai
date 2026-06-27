package com.kindai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.kindai.app.ui.design.KindColors

private val LightColorScheme = lightColorScheme(
    primary = KindColors.Primary,
    onPrimary = KindColors.TextOnPrimary,
    primaryContainer = KindColors.PrimaryContainer,
    secondary = KindColors.Secondary,
    onSecondary = KindColors.TextOnPrimary,
    secondaryContainer = KindColors.SecondaryContainer,
    background = KindColors.Background,
    onBackground = KindColors.TextPrimary,
    surface = KindColors.Surface,
    onSurface = KindColors.TextPrimary,
    surfaceVariant = KindColors.SurfaceVariant,
    onSurfaceVariant = KindColors.TextSecondary,
    error = KindColors.Error,
    onError = KindColors.TextOnPrimary,
    outline = KindColors.Border,
    outlineVariant = KindColors.Divider,
)

@Composable
fun KindAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = KindTypography,
        content = content
    )
}
