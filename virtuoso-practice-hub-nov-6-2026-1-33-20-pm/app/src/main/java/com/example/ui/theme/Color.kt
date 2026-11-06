package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.settings.AppTheme

// Concert Dark Palette (Deep Charcoal & Warm Brass/Gold)
val GoldPrimary = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF3E5AB)
val GoldDark = Color(0xFFA67C1E)
val BrassAccent = Color(0xFFE5A93C)
val AmberWood = Color(0xFFC67D34)

val CharcoalDarkest = Color(0xFF101012)
val CharcoalDark = Color(0xFF161619)
val CharcoalSurface = Color(0xFF1E1E22)
val CharcoalSurfaceVariant = Color(0xFF28282E)
val CharcoalBorder = Color(0xFF383842)

val TextPrimary = Color(0xFFF4F4F6)
val TextSecondary = Color(0xFFB4B4BC)
val TextMuted = Color(0xFF7E7E8A)

val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFE57373)

// Studio Light Palette (Warm Paper & Dark Charcoal Typography)
val LightBg = Color(0xFFF8F6F0)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEFECE4)
val LightBorder = Color(0xFFDDD8CE)
val LightGoldPrimary = Color(0xFFB8860B) // Dark goldenrod
val LightBrass = Color(0xFFC88214)
val LightTextPrimary = Color(0xFF1A1A1E)
val LightTextSecondary = Color(0xFF5A5A66)
val LightTextMuted = Color(0xFF888894)

// Neon Brass Palette (Obsidian Black & Glowing Molten Amber)
val NeonBg = Color(0xFF0A0A0C)
val NeonSurface = Color(0xFF141418)
val NeonSurfaceVariant = Color(0xFF201E28)
val NeonBorder = Color(0xFF422C1A)
val NeonPrimary = Color(0xFFFF8C00) // Deep molten amber
val NeonSecondary = Color(0xFFFFB347)
val NeonTextPrimary = Color(0xFFFFF6EE)
val NeonTextSecondary = Color(0xFFD8C0AC)
val NeonTextMuted = Color(0xFF8C7868)

/**
 * Dynamic theme colors accessed via LocalVirtuosoColors.current
 */
data class VirtuosoColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val primary: Color,
    val primaryLight: Color,
    val accent: Color,
    val tertiary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val success: Color = SuccessGreen,
    val error: Color = ErrorRed,
    val isDark: Boolean
)

val ConcertDarkColors = VirtuosoColors(
    background = CharcoalDarkest,
    surface = CharcoalSurface,
    surfaceVariant = CharcoalSurfaceVariant,
    border = CharcoalBorder,
    primary = GoldPrimary,
    primaryLight = GoldLight,
    accent = BrassAccent,
    tertiary = AmberWood,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    isDark = true
)

val StudioLightColors = VirtuosoColors(
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    border = LightBorder,
    primary = LightGoldPrimary,
    primaryLight = Color(0xFFD4AF37),
    accent = LightBrass,
    tertiary = Color(0xFF9E6510),
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    isDark = false
)

val NeonBrassColors = VirtuosoColors(
    background = NeonBg,
    surface = NeonSurface,
    surfaceVariant = NeonSurfaceVariant,
    border = NeonBorder,
    primary = NeonPrimary,
    primaryLight = NeonSecondary,
    accent = BrassAccent,
    tertiary = Color(0xFFFF5722),
    textPrimary = NeonTextPrimary,
    textSecondary = NeonTextSecondary,
    textMuted = NeonTextMuted,
    isDark = true
)

fun getThemeColors(theme: AppTheme): VirtuosoColors {
    return when (theme) {
        AppTheme.CONCERT_DARK -> ConcertDarkColors
        AppTheme.STUDIO_LIGHT -> StudioLightColors
        AppTheme.NEON_BRASS -> NeonBrassColors
    }
}

val LocalVirtuosoColors = compositionLocalOf { ConcertDarkColors }
