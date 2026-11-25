package com.example.color_or_suit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ColorOrSuitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Use dynamic color schemes (Material You) with brand fallback
    val colorScheme = if (darkTheme) {
        dynamicDarkColorScheme()
    } else {
        dynamicLightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Expose shapes for use in components
val Shapes = androidx.compose.material3.Shapes(
    extraSmall = DesignSystem.Shape.extraSmall,
    small = DesignSystem.Shape.small,
    medium = DesignSystem.Shape.medium,
    large = DesignSystem.Shape.large,
    extraLarge = DesignSystem.Shape.extraLarge
)
