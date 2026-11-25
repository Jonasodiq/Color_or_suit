package com.example.color_or_suit.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ========================================================================
// BRAND COLORS (used as seeds for dynamic color, or as fallbacks)
// ========================================================================
val BrandBluePrimary = Color(0xFF279FFF)
val BrandBlueDark = Color(0xFF40B0FF)
val BrandBlueContainer = Color(0xFFD6E8FF)

// Game-specific colors (these stay constant regardless of theme)
val CardRed = Color(0xFFFF0000)
val CardBlack = Color(0xFF000000)

// Legacy static backgrounds (fallback when dynamic color unavailable)
val LightBackground = Color(0xFFC3F0F0)
val DarkBackground = Color(0xFF121B22)

val CardBackground = Color(0xFFFFFFFF)
val CardBackgroundDark = Color(0xFF1E2A32)

// ========================================================================
// DYNAMIC COLOR SCHEMES (Material You / Material 3)
// Available on Android 12+ (API 31+)
// ========================================================================

/**
 * Returns a light color scheme based on the user's wallpaper (Material You).
 * Falls back to brand-based scheme if dynamic color is unavailable.
 */
@Composable
fun dynamicLightColorScheme(): androidx.compose.material3.ColorScheme {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(context)
    } else {
        lightColorSchemeFromBrand()
    }
}

/**
 * Returns a dark color scheme based on the user's wallpaper (Material You).
 * Falls back to brand-based scheme if dynamic color is unavailable.
 */
@Composable
fun dynamicDarkColorScheme(): androidx.compose.material3.ColorScheme {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        darkColorSchemeFromBrand()
    }
}

/**
 * Brand-based light color scheme (fallback for API < 31 or when dynamic color disabled)
 */
private fun lightColorSchemeFromBrand(): androidx.compose.material3.ColorScheme {
    return androidx.compose.material3.lightColorScheme(
        primary = BrandBluePrimary,
        primaryContainer = BrandBlueContainer,
        secondary = BrandBluePrimary.copy(alpha = 0.8f),
        tertiary = Color(0xFF006874),
        background = LightBackground,
        surface = CardBackground,
        surfaceVariant = Color(0xFFD6E4E9),
        error = Color(0xFFBA1A1A),
        onPrimary = Color.White,
        onPrimaryContainer = Color(0xFF001D33),
        onSecondary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        onSurfaceVariant = Color(0xFF3F4347),
        onError = Color.White,
        outline = Color(0xFF6F797C),
        outlineVariant = Color(0xFFBEC8CB),
        scrim = Color.Black,
        inverseSurface = DarkBackground,
        inversePrimary = BrandBlueDark,
        surfaceTint = BrandBluePrimary
    )
}

/**
 * Brand-based dark color scheme (fallback for API < 31 or when dynamic color disabled)
 */
private fun darkColorSchemeFromBrand(): androidx.compose.material3.ColorScheme {
    return androidx.compose.material3.darkColorScheme(
        primary = BrandBlueDark,
        primaryContainer = Color(0xFF00335A),
        secondary = BrandBlueDark.copy(alpha = 0.8f),
        tertiary = Color(0xFF4EC9F0),
        background = DarkBackground,
        surface = CardBackgroundDark,
        surfaceVariant = Color(0xFF3F4347),
        error = Color(0xFFFFB4AB),
        onPrimary = Color.Black,
        onPrimaryContainer = Color(0xFFD6E8FF),
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = Color(0xFFBEC8CB),
        onError = Color.Black,
        outline = Color(0xFF899295),
        outlineVariant = Color(0xFF3F4347),
        scrim = Color.Black,
        inverseSurface = LightBackground,
        inversePrimary = BrandBluePrimary,
        surfaceTint = BrandBlueDark
    )
}
