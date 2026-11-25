package com.example.color_or_suit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.color_or_suit.ui.theme.Confetti
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * En custom confetti-animation utan externa beroenden.
 *
 * Partiklar skapas slumpmässigt, simulerar gravitation, rotation och
 * sidleds svängning. Varje partikel har sin egen färg, storlek, hastighet
 * och vinkel – allt drivs av en gemensam tidslinje via [Animatable].
 */
@Composable
fun ConfettiOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = Confetti.particleCount,
    durationMillis: Int = Confetti.durationMillis
) {
    if (!visible) return

    // Slumpa partiklar en gång per uppvisning (rememberKeys förhindrar omskapande
    // vid varje recomposition)
    val particles = remember(visible) {
        List(particleCount) { ConfettiParticle() }
    }

    // Tidslinje 0f -> 1f som driver hela animationen
    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = LinearOutSlowInEasing
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "" }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = progress.value

            for (particle in particles) {
                val time = t * particle.lifetimeSeconds

                // Påverkas av gravitation efter att partikeln passerat toppen
                val gravityFactor = if (time > particle.fallDelay) {
                    ((time - particle.fallDelay) * particle.fallSpeed).let { it * it }
                } else {
                    0f
                }

                // Horisontell svängning (sinus)
                val sway = sin(time * particle.swayFrequency) * particle.swayAmplitude

                val x = size.width * particle.startX + sway
                val y = size.height * particle.startY +
                    particle.initialVelocity * time * size.height +
                    gravityFactor * size.height

                // Roterar konstant
                val rotation = particle.rotationSpeed * time * 360f

                // Avtar mot slutet
                val alpha = (1f - t).coerceIn(0f, 1f)

                // Skippa partiklar som flugit ut ur skärmen
                if (y > size.height + particle.size) continue

                translate(left = x, top = y) {
                    rotate(degrees = rotation, pivot = Offset(particle.size / 2f, particle.size / 2f)) {
                        drawRect(
                            color = particle.color.copy(alpha = alpha),
                            size = Size(particle.size, particle.size * particle.heightRatio)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Beskriver en enskild confetti-partikel.
 */
private data class ConfettiParticle(
    val startX: Float = Random.nextFloat(),
    val startY: Float = -Random.nextFloat() * 0.25f,
    val size: Float = Random.nextFloat() * Confetti.maxSizePx + Confetti.minSizePx,
    val heightRatio: Float = Random.nextFloat() * 0.5f + 0.5f,
    val color: Color = Confetti.palette.random(),
    val initialVelocity: Float = Random.nextFloat() * Confetti.maxVelocity,
    val fallDelay: Float = Random.nextFloat() * Confetti.maxFallDelay,
    val fallSpeed: Float = Random.nextFloat() * Confetti.fallSpeedVariance + Confetti.baseFallSpeed,
    val swayFrequency: Float = Random.nextFloat() * Confetti.maxSwayFrequency,
    val swayAmplitude: Float = Random.nextFloat() * Confetti.maxSwayAmplitude,
    val rotationSpeed: Float = Random.nextFloat() * Confetti.maxRotationSpeed - Confetti.maxRotationSpeed / 2f,
    val lifetimeSeconds: Float = Confetti.lifetimeSeconds
)
