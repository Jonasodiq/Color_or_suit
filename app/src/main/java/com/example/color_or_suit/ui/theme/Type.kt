package com.example.color_or_suit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.color_or_suit.R

// Använd Async loading strategy så att appen ALDRIG kraschar om Play Services inte har fonten lokalt,
// och lägg till Serif som fallback-font.
val KavoonFontFamily = FontFamily(
    Font(
        resId = R.font.kavoon,
        weight = FontWeight.Normal,
        loadingStrategy = FontLoadingStrategy.Async
    )
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = KavoonFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = KavoonFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
)
