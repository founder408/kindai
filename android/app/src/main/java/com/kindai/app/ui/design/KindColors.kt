package com.kindai.app.ui.design

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object KindColors {
    // Primary palette - Deep professional blue
    val Primary = Color(0xFF2563EB)
    val PrimaryDark = Color(0xFF1D4ED8)
    val PrimaryLight = Color(0xFFEFF6FF)
    val PrimaryContainer = Color(0xFFDBEAFE)
    val PrimarySoft = Color(0xFF93C5FD)

    // Secondary / Medical green
    val Secondary = Color(0xFF10B981)
    val SecondaryDark = Color(0xFF059669)
    val SecondaryLight = Color(0xFFECFDF5)
    val SecondaryContainer = Color(0xFFD1FAE5)

    // Accent cyan
    val Accent = Color(0xFF06B6D4)
    val AccentDark = Color(0xFF0891B2)
    val AccentLight = Color(0xFFECFEFF)

    // AI Purple
    val AiPurple = Color(0xFF7C3AED)
    val AiPurpleDark = Color(0xFF6D28D9)
    val AiPurpleLight = Color(0xFFF5F3FF)

    // Surfaces
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F5F9)
    val SurfaceElevated = Color(0xFFFFFFFF)
    val CardSurface = Color(0xFFFFFFFF)

    // Text
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val TextTertiary = Color(0xFF94A3B8)
    val TextOnPrimary = Color(0xFFFFFFFF)
    val TextOnDark = Color(0xFFFFFFFF)

    // Risk levels
    val RiskLow = Color(0xFF10B981)
    val RiskLowBg = Color(0xFFECFDF5)
    val RiskMedium = Color(0xFFF59E0B)
    val RiskMediumBg = Color(0xFFFFFBEB)
    val RiskHigh = Color(0xFFEF4444)
    val RiskHighBg = Color(0xFFFEF2F2)
    val RiskEmergency = Color(0xFF991B1B)
    val RiskEmergencyBg = Color(0xFFFEE2E2)

    // Functional
    val Success = Color(0xFF10B981)
    val SuccessBg = Color(0xFFECFDF5)
    val Warning = Color(0xFFF59E0B)
    val WarningBg = Color(0xFFFFFBEB)
    val Error = Color(0xFFEF4444)
    val ErrorBg = Color(0xFFFEF2F2)
    val Info = Color(0xFF3B82F6)
    val InfoBg = Color(0xFFEFF6FF)

    // Dividers & borders
    val Divider = Color(0xFFE2E8F0)
    val Border = Color(0xFFCBD5E1)
    val BorderFocused = Primary
    val BorderLight = Color(0xFFF1F5F9)

    // Overlay
    val Overlay = Color(0x80000000)
    val Shimmer = Color(0xFFE2E8F0)

    // Bottom nav
    val NavActive = Primary
    val NavInactive = Color(0xFF94A3B8)
    val NavBackground = Color(0xFFFFFFFF)

    // Elderly mode
    val ElderlyPrimary = Color(0xFF1D4ED8)
    val ElderlyBackground = Color(0xFFF0F5FF)
    val ElderlySOS = Color(0xFFDC2626)
    val ElderlyWarm = Color(0xFFF97316)
}

object KindGradients {
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
    )
    val PrimaryHorizontal = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
    )
    val HeaderGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E40AF), Color(0xFF2563EB))
    )
    val HeaderGradientLarge = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E3A8A), Color(0xFF1E40AF), Color(0xFF2563EB))
    )
    val CardGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
    )
    val AiGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF7C3AED), Color(0xFF6366F1))
    )
    val AiGradientSubtle = Brush.linearGradient(
        colors = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE))
    )
    val SuccessGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF10B981), Color(0xFF059669))
    )
    val DangerGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
    )
    val WarmGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFF97316), Color(0xFFEA580C))
    )
    val SplashGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E3A8A), Color(0xFF1E40AF), Color(0xFF3B82F6))
    )
    val CyanGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF06B6D4), Color(0xFF0891B2))
    )
    val GlassWhite = Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
    )
}
