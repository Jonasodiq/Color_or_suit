package com.example.color_or_suit.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Central design system tokens for Color or Suit app.
 * All spacing, shapes, elevations, motion specs, and semantic colors defined here.
 */
object DesignSystem {

    // ========================================================================
    // SPACING SCALE (4dp base unit - Material 3)
    // ========================================================================
    object Spacing {
        val xs = 4.dp      // 1×
        val sm = 8.dp      // 2×
        val md = 16.dp     // 4×
        val lg = 24.dp     // 6×
        val xl = 32.dp     // 8×
        val xxl = 48.dp    // 12×
        val xxxl = 64.dp   // 16×

        // Semantic spacing
        val screenPadding = md
        val cardPadding = md
        val buttonGap = sm
        val sectionGap = lg
        val inlineGap = sm
    }

    // ========================================================================
    // SHAPE SYSTEM (Material 3)
    // ========================================================================
    object Shape {
        val extraSmall = RoundedCornerShape(4.dp)
        val small = RoundedCornerShape(8.dp)
        val medium = RoundedCornerShape(12.dp)
        val large = RoundedCornerShape(16.dp)
        val extraLarge = RoundedCornerShape(24.dp)
        val full = RoundedCornerShape(CornerSize(50.dp))

        // Semantic shapes
        val card = large
        val button = medium
        val chip = full
        val dialog = extraLarge
        val suitButton = medium
    }

    // ========================================================================
    // ELEVATION / SHADOW SYSTEM (Material 3)
    // Using Dp values directly since ElevationDefaults is not available in Material3
    // ========================================================================
    object Elevation {
        // Level 0 - flat
        val level0 = 0.dp
        // Level 1 - cards, surfaces
        val level1 = 1.dp
        // Level 2 - raised cards, FAB
        val level2 = 3.dp
        // Level 3 - modal bottom sheets
        val level3 = 6.dp
        // Level 4 - dialogs, popovers
        val level4 = 8.dp
        // Level 5 - navigation drawer
        val level5 = 12.dp

        // Custom elevations for game elements
        val gameCard = 8.dp
        val suitButton = 4.dp
        val floatingScore = 6.dp
    }

    // ========================================================================
    // MOTION / ANIMATION SYSTEM (Material 3)
    // ========================================================================
    object Motion {
        // Duration tokens
        val durationShort = 150
        val durationMedium = 300
        val durationLong = 500
        val durationExtraLong = 700

        // Easing tokens
        val easingStandard: Easing = androidx.compose.animation.core.FastOutSlowInEasing
        val easingEmphasized: Easing = androidx.compose.animation.core.FastOutLinearInEasing
        val easingDecelerate: Easing = androidx.compose.animation.core.LinearOutSlowInEasing
        val easingAccelerate: Easing = androidx.compose.animation.core.FastOutLinearInEasing

        // Spring configs for playful motion
        val springBouncy = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
        val springSmooth = spring<Float>(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )

        // Pre-built tween specs for slide animations (Int pixel offsets)
        val tweenSlide: androidx.compose.animation.core.TweenSpec<Int> = tween<Int>(durationMillis = durationMedium, easing = easingStandard)
        val tweenSlideSlow: androidx.compose.animation.core.TweenSpec<Int> = tween<Int>(durationMillis = durationLong, easing = easingDecelerate)

        // Pre-built tween specs for fade/scale animations (Float)
        val tweenFade: androidx.compose.animation.core.TweenSpec<Float> = tween<Float>(durationMillis = durationMedium, easing = easingStandard)

        // Pre-built tween specs
        val tweenShort: androidx.compose.animation.core.TweenSpec<Float> = tween<Float>(durationMillis = durationShort, easing = easingStandard)
        val tweenMedium: androidx.compose.animation.core.TweenSpec<Float> = tween<Float>(durationMillis = durationMedium, easing = easingStandard)
        val tweenLong: androidx.compose.animation.core.TweenSpec<Float> = tween<Float>(durationMillis = durationLong, easing = easingStandard)
        val tweenCardFlip: androidx.compose.animation.core.TweenSpec<Float> = tween<Float>(durationMillis = 350, easing = easingStandard)
        val tweenScreenTransition: androidx.compose.animation.core.TweenSpec<Float> = tween<Float>(durationMillis = durationMedium, easing = easingDecelerate)

        // Stagger delays for entrance animations
        val staggerDelay = 50L // ms between items
    }

    // ========================================================================
    // TYPOGRAPHY SCALE ADDITIONS (extends Type.kt Typography)
    // ========================================================================
    object TypeScale {
        // Additional text styles not in Material Typography
        val scoreDisplay = androidx.compose.ui.text.TextStyle(
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1.5).sp
        )
        val suitLabel = androidx.compose.ui.text.TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        val statLabel = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
        val statValue = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }

    // ========================================================================
    // TOUCH TARGET SIZES (Accessibility - Material 3 minimum 48dp, recommended 56dp)
    // ========================================================================
    object TouchTarget {
        val minimum = 48.dp
        val comfortable = 56.dp
        val spacious = 64.dp

        val iconButton = comfortable
        val suitButton = 56.dp
        val actionButton = comfortable
        val fab = 56.dp
    }

    // ========================================================================
    // Z-INDEX LAYERS
    // ========================================================================
    object ZIndex {
        val background = 0f
        val content = 1f
        val card = 10f
        val floatingScore = 20f
        val stickyHeader = 30f
        val modal = 100f
        val dialog = 200f
        val toast = 300f
    }

    // ========================================================================
    // BREAKPOINTS (for responsive layouts)
    // ========================================================================
    object Breakpoints {
        val compact = 600.dp   // phones
        val medium = 840.dp    // large phones / small tablets
        val expanded = 1200.dp // tablets / desktop
    }

    // ========================================================================
    // SEMANTIC COLOR ROLES (mapped to Material 3 color scheme)
    // Usage: DesignSystem.SemanticColors.primaryContainer etc.
    // ========================================================================
    object SemanticColors {
        // Game-specific semantic colors
        @Composable
        fun correctColor() = androidx.compose.material3.MaterialTheme.colorScheme.primary

        @Composable
        fun correctColorStrong() = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer

        @Composable
        fun errorColor() = androidx.compose.material3.MaterialTheme.colorScheme.error

        @Composable
        fun errorColorContainer() = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer

        @Composable
        fun warningColor() = androidx.compose.material3.MaterialTheme.colorScheme.tertiary

        @Composable
        fun surfaceVariant() = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant

        @Composable
        fun onSurfaceVariant() = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

        @Composable
        fun outline() = androidx.compose.material3.MaterialTheme.colorScheme.outline

        @Composable
        fun outlineVariant() = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
    }

    // ========================================================================
    // GAME-SPECIFIC CONSTANTS
    // ========================================================================
    object Game {
        val cardWidth = 140.dp
        val cardHeight = 210.dp
        val cardAspectRatio = 2f / 3f

        val suitIconSize = 32.dp
        val suitButtonSize = TouchTarget.suitButton

        val scoreAnimationHeight = 80.dp
        val scoreAnimationDuration = 1200

        val flipAnimationDuration = 350
        val flipDelayBeforeFlipBack = 1000

        // Difficulty thresholds (can be tuned via A/B testing)
        object Difficulty {
            // Easy: Win 15-18, Lose < -3
            val easyWinMin = 15
            val easyWinMax = 18
            val easyLoseThreshold = -3

            // Medium: Win exactly 21, Lose < 0 or > 21
            val mediumWinExact = 21
            val mediumLoseMin = 0
            val mediumLoseMax = 21

            // Hard: Win exactly 31, Lose < 0 or > 31
            val hardWinExact = 31
            val hardLoseMin = 0
            val hardLoseMax = 31
        }
    }
}

// ========================================================================
// CONVENIENCE EXTENSIONS
// ========================================================================

fun Modifier.screenPadding() = padding(DesignSystem.Spacing.screenPadding)
fun Modifier.cardPadding() = padding(DesignSystem.Spacing.cardPadding)
fun Modifier.sectionGap() = padding(bottom = DesignSystem.Spacing.sectionGap)
fun Modifier.inlineGap() = padding(end = DesignSystem.Spacing.inlineGap)

// ========================================================================
// CONFETTI / CELEBRATION (top-level object for easy access)
// ========================================================================
object Confetti {
    val particleCount = 120
    val durationMillis = 2500
    val lifetimeSeconds = 2.5f
    val minSizePx = 4f
    val maxSizePx = 12f
    val baseFallSpeed = 0.3f
    val fallSpeedVariance = 0.5f
    val maxFallDelay = 0.3f
    val maxVelocity = 0.8f
    val maxSwayFrequency = 4f
    val maxSwayAmplitude = 30f
    val maxRotationSpeed = 4f

    // Färgpalett – primär + sekundär + komplement + accent
    val palette = listOf(
        Color(0xFF6200EE), // Primary purple
        Color(0xFF03DAC5), // Secondary teal
        Color(0xFFFF0266), // Error/Accent pink
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFF2196F3), // Blue
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0)  // Deep purple
    )
}