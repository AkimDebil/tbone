package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.settings.AppTheme

private fun createM3DarkColorScheme(colors: VirtuosoColors) = darkColorScheme(
    primary = colors.primary,
    onPrimary = colors.background,
    primaryContainer = colors.surfaceVariant,
    onPrimaryContainer = colors.primaryLight,
    secondary = colors.accent,
    onSecondary = colors.background,
    secondaryContainer = colors.surface,
    onSecondaryContainer = colors.primaryLight,
    tertiary = colors.tertiary,
    onTertiary = colors.background,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.surface,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surfaceVariant,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.border,
    outlineVariant = colors.surfaceVariant,
    error = colors.error
)

private fun createM3LightColorScheme(colors: VirtuosoColors) = lightColorScheme(
    primary = colors.primary,
    onPrimary = LightSurface,
    primaryContainer = colors.surfaceVariant,
    onPrimaryContainer = colors.primary,
    secondary = colors.accent,
    onSecondary = LightSurface,
    secondaryContainer = colors.surfaceVariant,
    onSecondaryContainer = colors.textPrimary,
    tertiary = colors.tertiary,
    onTertiary = LightSurface,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.surface,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surfaceVariant,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.border,
    outlineVariant = colors.surfaceVariant,
    error = colors.error
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.CONCERT_DARK,
    content: @Composable () -> Unit
) {
    val virtuosoColors = getThemeColors(appTheme)
    val colorScheme = if (virtuosoColors.isDark) {
        createM3DarkColorScheme(virtuosoColors)
    } else {
        createM3LightColorScheme(virtuosoColors)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = virtuosoColors.background.toArgb()
                window.navigationBarColor = virtuosoColors.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !virtuosoColors.isDark
                insetsController.isAppearanceLightNavigationBars = !virtuosoColors.isDark
            }
        }
    }

    CompositionLocalProvider(LocalVirtuosoColors provides virtuosoColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
