package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WeatherViewModel
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SunGold
import com.example.ui.theme.SurfaceCard

enum class RadarLayer(val label: String) {
    PRECIPITATION("Rain Reflectivity"),
    WIND_FIELD("Wind Vectors"),
    CLOUDS("Cloud Tops")
}

@Composable
fun RadarScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    var selectedLayer by remember { mutableStateOf(RadarLayer.PRECIPITATION) }
    var selectedRangeKm by remember { mutableStateOf(100) }
    var isSweeping by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("radar_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(RadarGreen.copy(alpha = 0.2f), CircleShape)
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = RadarGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Atmospheric Radar",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Doppler radar sweep centered on ${currentLocation.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { isSweeping = !isSweeping },
                    modifier = Modifier
                        .background(SurfaceCard, CircleShape)
                        .testTag("radar_toggle_play_button")
                ) {
                    Icon(
                        imageVector = if (isSweeping) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle sweep",
                        tint = RadarGreen
                    )
                }
            }
        }

        // Radar Dish Display
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radar_viewport_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadarDishCanvas(
                        isSweeping = isSweeping,
                        layer = selectedLayer,
                        rangeKm = selectedRangeKm,
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A0F1D))
                            .border(1.5.dp, GlassBorder, CircleShape)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Range Switcher
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(25, 50, 100, 200).forEach { range ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedRangeKm == range) RadarGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f))
                                    .border(
                                        width = if (selectedRangeKm == range) 1.dp else 0.5.dp,
                                        color = if (selectedRangeKm == range) RadarGreen else GlassBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRangeKm = range }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("range_chip_$range")
                            ) {
                                Text(
                                    text = "$range km",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRangeKm == range) RadarGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Layer selection chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Telemetry Layers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadarLayer.values().forEach { layer ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selectedLayer == layer) SkyBlueLight.copy(alpha = 0.2f) else SurfaceCard)
                                .border(
                                    width = if (selectedLayer == layer) 1.5.dp else 1.dp,
                                    color = if (selectedLayer == layer) SkyBlueLight else GlassBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedLayer = layer }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = layer.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedLayer == layer) SkyBlueLight else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Radar Reflectivity dBZ Legend
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Reflectivity Intensity (dBZ)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Color Spectrum Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF38BDF8), // Light drizzle
                                        Color(0xFF10B981), // Moderate rain
                                        Color(0xFFFBBF24), // Heavy
                                        Color(0xFFEF4444), // Severe
                                        Color(0xFF8B5CF6)  // Extreme / Hail
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("15 dBZ (Light)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("35 dBZ (Moderate)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("55+ dBZ (Hail/Severe)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun RadarDishCanvas(
    isSweeping: Boolean,
    layer: RadarLayer,
    rangeKm: Int,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "RadarBeam")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f

        // Draw concentric distance range rings
        val rings = listOf(0.25f, 0.5f, 0.75f, 0.98f)
        rings.forEach { ratio ->
            drawCircle(
                color = Color(0x3310B981),
                radius = radius * ratio,
                center = center,
                style = Stroke(width = 1.2f)
            )
        }

        // Crosshairs
        drawLine(
            color = Color(0x3310B981),
            start = Offset(center.x, 0f),
            end = Offset(center.x, size.height),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0x3310B981),
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 1f
        )

        // Draw simulated radar echoes / storm cells
        when (layer) {
            RadarLayer.PRECIPITATION -> {
                // North-east storm cluster
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xDDEF4444), Color(0xAAEF4444), Color(0x55FBBF24), Color.Transparent),
                        center = Offset(center.x + radius * 0.45f, center.y - radius * 0.35f),
                        radius = radius * 0.35f
                    ),
                    center = Offset(center.x + radius * 0.45f, center.y - radius * 0.35f),
                    radius = radius * 0.35f
                )

                // South-west moderate rain band
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x9910B981), Color(0x4438BDF8), Color.Transparent),
                        center = Offset(center.x - radius * 0.38f, center.y + radius * 0.4f),
                        radius = radius * 0.4f
                    ),
                    center = Offset(center.x - radius * 0.38f, center.y + radius * 0.4f),
                    radius = radius * 0.4f
                )
            }
            RadarLayer.WIND_FIELD -> {
                // Wind vector arrows or lines
                for (i in -2..2) {
                    val y = center.y + i * 36f
                    drawLine(
                        color = Color(0x8838BDF8),
                        start = Offset(center.x - radius * 0.7f, y),
                        end = Offset(center.x + radius * 0.6f, y - 18f),
                        strokeWidth = 2f
                    )
                }
            }
            RadarLayer.CLOUDS -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x99FFFFFF), Color(0x4494A3B8), Color.Transparent),
                        center = Offset(center.x + radius * 0.2f, center.y - radius * 0.2f),
                        radius = radius * 0.6f
                    ),
                    center = Offset(center.x + radius * 0.2f, center.y - radius * 0.2f),
                    radius = radius * 0.6f
                )
            }
        }

        // Sweeping radar beam cone
        if (isSweeping) {
            val sweepAngle = 45f
            val startAngle = angle - sweepAngle
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1110B981),
                        Color(0x4410B981),
                        Color(0xCC10B981)
                    ),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = size
            )
        }

        // Center station marker
        drawCircle(
            color = RadarGreen,
            radius = 4f,
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 2f,
            center = center
        )
    }
}
