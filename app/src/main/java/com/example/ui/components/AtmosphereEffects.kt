package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.ParticleEffect
import kotlin.random.Random

@Composable
fun AtmosphereEffectCanvas(
    effect: ParticleEffect,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "AtmosphereTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    when (effect) {
        ParticleEffect.RAIN -> RainCanvas(phase = phase, modifier = modifier)
        ParticleEffect.SNOW -> SnowCanvas(phase = phase, modifier = modifier)
        ParticleEffect.SUN_BEAMS -> SunBeamsCanvas(phase = phase, modifier = modifier)
        ParticleEffect.DRIFTING_CLOUDS -> CloudsCanvas(phase = phase, modifier = modifier)
        ParticleEffect.NONE -> {
            if (!isDay) {
                StarryCanvas(phase = phase, modifier = modifier)
            }
        }
    }
}

@Composable
private fun RainCanvas(phase: Float, modifier: Modifier = Modifier) {
    val drops = remember {
        List(40) {
            Drop(
                xNorm = Random.nextFloat(),
                speed = 0.8f + Random.nextFloat() * 0.7f,
                length = 15f + Random.nextFloat() * 25f,
                alpha = 0.3f + Random.nextFloat() * 0.5f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drops.forEach { drop ->
            val yProgress = (phase * drop.speed + drop.xNorm) % 1.0f
            val startX = drop.xNorm * w
            val startY = yProgress * h
            val endX = startX - 4f
            val endY = startY + drop.length

            drawLine(
                color = Color(0xFF60A5FA).copy(alpha = drop.alpha),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
private fun SnowCanvas(phase: Float, modifier: Modifier = Modifier) {
    val flakes = remember {
        List(35) {
            Flake(
                xNorm = Random.nextFloat(),
                speed = 0.4f + Random.nextFloat() * 0.4f,
                radius = 2.5f + Random.nextFloat() * 3.5f,
                alpha = 0.4f + Random.nextFloat() * 0.5f,
                wobbleSpeed = 1f + Random.nextFloat() * 2f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        flakes.forEach { flake ->
            val yProgress = (phase * flake.speed + flake.xNorm) % 1.0f
            val sway = kotlin.math.sin((phase * flake.wobbleSpeed * 2 * Math.PI).toFloat()) * 12f
            val x = (flake.xNorm * w + sway).coerceIn(0f, w)
            val y = yProgress * h

            drawCircle(
                color = Color.White.copy(alpha = flake.alpha),
                radius = flake.radius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun SunBeamsCanvas(phase: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val pulse = 0.85f + 0.15f * kotlin.math.sin(phase * 2 * Math.PI.toFloat())

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x55FBBF24),
                    Color(0x22FBBF24),
                    Color.Transparent
                ),
                center = Offset(w * 0.85f, h * 0.2f),
                radius = w * 0.65f * pulse
            )
        )
    }
}

@Composable
private fun CloudsCanvas(phase: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val shiftX = (phase * 60f) % w

        // Atmospheric soft haze circles
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            center = Offset((w * 0.2f + shiftX) % w, h * 0.35f),
            radius = w * 0.35f
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.06f),
            center = Offset((w * 0.7f + shiftX * 0.7f) % w, h * 0.45f),
            radius = w * 0.4f
        )
    }
}

@Composable
private fun StarryCanvas(phase: Float, modifier: Modifier = Modifier) {
    val stars = remember {
        List(40) {
            Star(
                xNorm = Random.nextFloat(),
                yNorm = Random.nextFloat() * 0.7f,
                radius = 1.2f + Random.nextFloat() * 2f,
                baseAlpha = 0.3f + Random.nextFloat() * 0.6f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        stars.forEachIndexed { i, star ->
            val twinkle = (kotlin.math.sin(phase * 4 * Math.PI.toFloat() + i) + 1f) / 2f
            val alpha = (star.baseAlpha * (0.5f + 0.5f * twinkle)).coerceIn(0.1f, 1f)
            drawCircle(
                color = Color(0xFFF1F5F9).copy(alpha = alpha),
                radius = star.radius,
                center = Offset(star.xNorm * w, star.yNorm * h)
            )
        }
    }
}

private data class Drop(val xNorm: Float, val speed: Float, val length: Float, val alpha: Float)
private data class Flake(val xNorm: Float, val speed: Float, val radius: Float, val alpha: Float, val wobbleSpeed: Float)
private data class Star(val xNorm: Float, val yNorm: Float, val radius: Float, val baseAlpha: Float)
