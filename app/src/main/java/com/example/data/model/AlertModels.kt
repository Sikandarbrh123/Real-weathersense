package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Masks
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class WeatherAlertSeverity(val label: String, val color: Color) {
    CRITICAL("Severe Warning", Color(0xFFEF4444)),
    WARNING("Weather Watch", Color(0xFFF97316)),
    ADVISORY("Weather Advisory", Color(0xFF38BDF8))
}

enum class WeatherAlertType(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val defaultSeverity: WeatherAlertSeverity
) {
    SEVERE_STORM(
        title = "Severe Storms & Lightning",
        description = "Convective thunderstorm cells, hail risk, and electrical activity",
        icon = Icons.Default.Thunderstorm,
        defaultSeverity = WeatherAlertSeverity.CRITICAL
    ),
    HEAVY_SNOW(
        title = "Heavy Snow & Blizzards",
        description = "Snowfall accumulation, freezing rain, and icy road hazards",
        icon = Icons.Default.AcUnit,
        defaultSeverity = WeatherAlertSeverity.CRITICAL
    ),
    TEMPERATURE_DROP(
        title = "Rapid Temperature Drop",
        description = "Sudden drop > 5°C (10°F) or freeze threshold warning (< 0°C)",
        icon = Icons.Default.Thermostat,
        defaultSeverity = WeatherAlertSeverity.WARNING
    ),
    EXTREME_HEAT(
        title = "Extreme Heat Wave",
        description = "Dangerous heat index (> 35°C / 95°F) with high thermal stress",
        icon = Icons.Default.WbSunny,
        defaultSeverity = WeatherAlertSeverity.WARNING
    ),
    HIGH_WIND(
        title = "Gale Winds & Microbursts",
        description = "Sustained wind speeds or sudden gusts exceeding 45 km/h",
        icon = Icons.Default.Air,
        defaultSeverity = WeatherAlertSeverity.WARNING
    ),
    RAIN_DOWNPOUR(
        title = "Flash Rain & Downpours",
        description = "Heavy convective precipitation with localized flooding risk",
        icon = Icons.Default.WaterDrop,
        defaultSeverity = WeatherAlertSeverity.ADVISORY
    ),
    AIR_QUALITY(
        title = "Hazardous Air Quality",
        description = "AQI reading above 100 with elevated PM2.5 particulate levels",
        icon = Icons.Default.Masks,
        defaultSeverity = WeatherAlertSeverity.ADVISORY
    )
}

data class WeatherAlertRule(
    val type: WeatherAlertType,
    val isEnabled: Boolean = true,
    val thresholdDescription: String
)

data class ActiveWeatherAlert(
    val id: String,
    val locationName: String,
    val type: WeatherAlertType,
    val severity: WeatherAlertSeverity,
    val title: String,
    val message: String,
    val detectedAt: String,
    val safetyAction: String
)

data class LocationAlertConfig(
    val locationName: String,
    val isMonitored: Boolean = true
)
