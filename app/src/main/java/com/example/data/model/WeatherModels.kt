package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FullWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val elevation: Double?,
    val current: CurrentWeatherData?,
    val hourly: HourlyWeatherData?,
    val daily: DailyWeatherData?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherData(
    val time: String,
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "relative_humidity_2m") val humidity: Int,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    @Json(name = "is_day") val isDay: Int,
    val precipitation: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "cloud_cover") val cloudCover: Int,
    @Json(name = "pressure_msl") val pressureMsl: Double,
    @Json(name = "surface_pressure") val surfacePressure: Double,
    @Json(name = "wind_speed_10m") val windSpeed: Double,
    @Json(name = "wind_direction_10m") val windDirection: Double
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherData(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature: List<Double>,
    @Json(name = "relative_humidity_2m") val humidity: List<Int>?,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "wind_speed_10m") val windSpeed: List<Double>?,
    @Json(name = "uv_index") val uvIndex: List<Double>?
)

@JsonClass(generateAdapter = true)
data class DailyWeatherData(
    val time: List<String>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>,
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>?,
    @Json(name = "apparent_temperature_min") val apparentTemperatureMin: List<Double>?,
    val sunrise: List<String>?,
    val sunset: List<String>?,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>?,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>?,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?
)

data class HourlyForecastItem(
    val timeFormatted: String,
    val rawTime: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProb: Int,
    val condition: WeatherCondition,
    val isDay: Boolean
)

data class DailyForecastItem(
    val dateFormatted: String,
    val dayOfWeek: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProb: Int,
    val condition: WeatherCondition,
    val uvIndexMax: Double,
    val sunrise: String?,
    val sunset: String?
)

enum class ParticleEffect {
    NONE,
    RAIN,
    SNOW,
    SUN_BEAMS,
    DRIFTING_CLOUDS
}

data class WeatherCondition(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val particleEffect: ParticleEffect,
    val alertAdvice: String
)

object WeatherCodeParser {
    fun parse(code: Int, isDay: Boolean = true): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition(
                title = if (isDay) "Clear Sky" else "Clear Night",
                description = if (isDay) "Sunny with clear atmospheric visibility" else "Starry and crisp visibility",
                icon = Icons.Default.WbSunny,
                primaryColor = if (isDay) Color(0xFF0284C7) else Color(0xFF0F172A),
                secondaryColor = if (isDay) Color(0xFF38BDF8) else Color(0xFF1E293B),
                particleEffect = if (isDay) ParticleEffect.SUN_BEAMS else ParticleEffect.NONE,
                alertAdvice = "Optimal visibility. UV protection recommended during peak solar hours."
            )
            1 -> WeatherCondition(
                title = "Mainly Clear",
                description = "Light cirrus clouds with abundant sunshine",
                icon = Icons.Default.WbSunny,
                primaryColor = Color(0xFF0369A1),
                secondaryColor = Color(0xFF38BDF8),
                particleEffect = ParticleEffect.SUN_BEAMS,
                alertAdvice = "Great conditions for outdoor activities."
            )
            2 -> WeatherCondition(
                title = "Partly Cloudy",
                description = "Scattered cumulus clouds across the sky",
                icon = Icons.Default.WbCloudy,
                primaryColor = Color(0xFF0E7490),
                secondaryColor = Color(0xFF22D3EE),
                particleEffect = ParticleEffect.DRIFTING_CLOUDS,
                alertAdvice = "Mild solar radiation with intermittent shade."
            )
            3 -> WeatherCondition(
                title = "Overcast",
                description = "Dense cloud blanket covering the atmosphere",
                icon = Icons.Default.Cloud,
                primaryColor = Color(0xFF334155),
                secondaryColor = Color(0xFF64748B),
                particleEffect = ParticleEffect.DRIFTING_CLOUDS,
                alertAdvice = "Diffused ambient lighting. Stable atmospheric pressure."
            )
            45, 48 -> WeatherCondition(
                title = if (code == 45) "Foggy" else "Depositing Rime Fog",
                description = "Reduced horizontal visibility below 1000m",
                icon = Icons.Default.Air,
                primaryColor = Color(0xFF475569),
                secondaryColor = Color(0xFF94A3B8),
                particleEffect = ParticleEffect.DRIFTING_CLOUDS,
                alertAdvice = "Low visibility alert: Exercise caution while commuting."
            )
            51, 53, 55 -> WeatherCondition(
                title = "Drizzle",
                description = "Fine microscopic rain droplets aloft",
                icon = Icons.Default.WaterDrop,
                primaryColor = Color(0xFF1E3A8A),
                secondaryColor = Color(0xFF60A5FA),
                particleEffect = ParticleEffect.RAIN,
                alertAdvice = "Damp surfaces. Light waterproof outer layer advised."
            )
            61, 63, 65 -> WeatherCondition(
                title = if (code == 65) "Heavy Rain" else "Rainfall",
                description = "Sustained atmospheric precipitation",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF1E293B),
                secondaryColor = Color(0xFF3B82F6),
                particleEffect = ParticleEffect.RAIN,
                alertAdvice = "Precipitation active. Carry an umbrella and expect slick roads."
            )
            71, 73, 75, 77 -> WeatherCondition(
                title = "Snowfall",
                description = "Atmospheric crystallization and snow accumulation",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF1E293B),
                secondaryColor = Color(0xFFE2E8F0),
                particleEffect = ParticleEffect.SNOW,
                alertAdvice = "Freezing temperatures. Thermal insulation required."
            )
            80, 81, 82 -> WeatherCondition(
                title = "Rain Showers",
                description = "Intermittent convective precipitation bursts",
                icon = Icons.Default.WaterDrop,
                primaryColor = Color(0xFF0F172A),
                secondaryColor = Color(0xFF2563EB),
                particleEffect = ParticleEffect.RAIN,
                alertAdvice = "Sudden downpours possible over short intervals."
            )
            85, 86 -> WeatherCondition(
                title = "Snow Showers",
                description = "Flurries and sudden snow bursts",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF0F172A),
                secondaryColor = Color(0xFF93C5FD),
                particleEffect = ParticleEffect.SNOW,
                alertAdvice = "Reduced traction and rapid temperature drops."
            )
            95, 96, 99 -> WeatherCondition(
                title = "Thunderstorm",
                description = "Severe electrical convective storm with lightning risk",
                icon = Icons.Default.Thunderstorm,
                primaryColor = Color(0xFF0F172A),
                secondaryColor = Color(0xFF7C3AED),
                particleEffect = ParticleEffect.RAIN,
                alertAdvice = "Severe weather warning! Stay indoors away from exposed metallic areas."
            )
            else -> WeatherCondition(
                title = "Fair Skies",
                description = "Typical seasonal weather conditions",
                icon = Icons.Default.WbSunny,
                primaryColor = Color(0xFF0369A1),
                secondaryColor = Color(0xFF38BDF8),
                particleEffect = ParticleEffect.NONE,
                alertAdvice = "Pleasant ambient atmosphere."
            )
        }
    }
}
