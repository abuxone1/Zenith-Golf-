package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ZenithThemeColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryGreen: Color,
    val deepForestGreen: Color,
    val accentGold: Color,
    val onGold: Color,
    val accentSuccess: Color,
    val accentInfo: Color,
    val forestCard: Color,
    val onForestCard: Color,
    val onForestSecondary: Color,
    val error: Color
)

private val LightZenithColors = ZenithThemeColors(
    isDark = false,
    background = ZenithSoftLightBg,
    surface = ZenithLightSurface,
    surfaceElevated = ZenithLightSurfaceElevated,
    border = ZenithLightBorder,
    textPrimary = ZenithLightTextPrimary,
    textSecondary = ZenithLightTextSecondary,
    textMuted = ZenithLightTextMuted,
    primaryGreen = ZenithChampionshipGreen,
    deepForestGreen = ZenithDeepForestGreen,
    accentGold = Color(0xFFAD8727),
    onGold = ZenithPureWhite,
    accentSuccess = Color(0xFF0C7C47),
    accentInfo = Color(0xFF0B6E8C),
    forestCard = ZenithChampionshipGreen,
    onForestCard = ZenithPureWhite,
    onForestSecondary = Color(0xFFD0E3D7),
    error = Color(0xFFC62828)
)

private val DarkZenithColors = ZenithThemeColors(
    isDark = true,
    background = ZenithDarkInterfaceBg,
    surface = ZenithDarkSurface,
    surfaceElevated = ZenithDarkSurfaceElevated,
    border = ZenithDarkBorder,
    textPrimary = ZenithPureWhite,
    textSecondary = ZenithTextSecondary,
    textMuted = ZenithTextMuted,
    primaryGreen = ZenithChampionshipGreen,
    deepForestGreen = ZenithDeepForestGreen,
    accentGold = ZenithPremiumGold,
    onGold = Color(0xFF1A1200),
    accentSuccess = ZenithNeonGreen,
    accentInfo = ZenithCyan,
    forestCard = ZenithDeepForestGreen,
    onForestCard = ZenithPureWhite,
    onForestSecondary = ZenithTextSecondary,
    error = ZenithHazardRed
)

val LocalZenithColors = staticCompositionLocalOf { DarkZenithColors }

object ZenithTheme {
    val colors: ZenithThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalZenithColors.current
}

private val ZenithLightColorScheme = lightColorScheme(
    primary = ZenithChampionshipGreen,
    onPrimary = ZenithPureWhite,
    primaryContainer = Color(0xFFD5E8DC),
    onPrimaryContainer = ZenithDeepForestGreen,
    secondary = ZenithPremiumGold,
    onSecondary = Color(0xFF1E1600),
    secondaryContainer = Color(0xFFF7ECC9),
    onSecondaryContainer = Color(0xFF3E2E00),
    tertiary = ZenithCyanDark,
    onTertiary = ZenithPureWhite,
    background = ZenithSoftLightBg,
    onBackground = ZenithLightTextPrimary,
    surface = ZenithLightSurface,
    onSurface = ZenithLightTextPrimary,
    surfaceVariant = ZenithLightSurfaceElevated,
    onSurfaceVariant = ZenithLightTextSecondary,
    outline = ZenithLightBorder,
    error = Color(0xFFC62828)
)

private val ZenithDarkColorScheme = darkColorScheme(
    primary = ZenithNeonGreen,
    onPrimary = Color(0xFF003816),
    primaryContainer = ZenithChampionshipGreen,
    onPrimaryContainer = ZenithPureWhite,
    secondary = ZenithCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = ZenithCyanDark,
    onSecondaryContainer = Color(0xFFA5F3FC),
    tertiary = ZenithPremiumGold,
    onTertiary = Color(0xFF1E1600),
    tertiaryContainer = ZenithGoldDark,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = ZenithDarkInterfaceBg,
    onBackground = ZenithPureWhite,
    surface = ZenithDarkSurface,
    onSurface = ZenithPureWhite,
    surfaceVariant = ZenithDarkSurfaceElevated,
    onSurfaceVariant = ZenithTextSecondary,
    outline = ZenithDarkBorder,
    error = ZenithHazardRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val zenithColors = if (darkTheme) DarkZenithColors else LightZenithColors
    val materialScheme = if (darkTheme) ZenithDarkColorScheme else ZenithLightColorScheme

    CompositionLocalProvider(LocalZenithColors provides zenithColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
