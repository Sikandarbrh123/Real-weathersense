package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AirQualityResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentAirQuality?
)

@JsonClass(generateAdapter = true)
data class CurrentAirQuality(
    val time: String,
    @Json(name = "european_aqi") val europeanAqi: Int?,
    @Json(name = "us_aqi") val usAqi: Int?,
    val pm10: Double?,
    @Json(name = "pm2_5") val pm25: Double?,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double?,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double?,
    @Json(name = "sulphur_dioxide") val sulphurDioxide: Double?,
    val ozone: Double?
)

enum class AqiCategory(
    val label: String,
    val rangeText: String,
    val color: Color,
    val healthAdvice: String
) {
    GOOD("Good", "0-50", Color(0xFF10B981), "Air quality is satisfactory and poses little or no risk."),
    MODERATE("Moderate", "51-100", Color(0xFFFBBF24), "Acceptable air quality; sensitive individuals should monitor."),
    UNHEALTHY_SENSITIVE("Unhealthy for Sensitive", "101-150", Color(0xFFF97316), "Members of sensitive groups may experience health effects."),
    UNHEALTHY("Unhealthy", "151-200", Color(0xFFEF4444), "Everyone may begin to experience health effects; limit strenuous outdoor effort."),
    VERY_UNHEALTHY("Very Unhealthy", "201-300", Color(0xFF8B5CF6), "Health alert: The risk of health effects is increased for everyone."),
    HAZARDOUS("Hazardous", "300+", Color(0xFF7F1D1D), "Health warnings of emergency conditions. Stay indoors.")
}

object AqiHelper {
    fun fromUsAqi(value: Int?): AqiCategory {
        val aqi = value ?: 25
        return when {
            aqi <= 50 -> AqiCategory.GOOD
            aqi <= 100 -> AqiCategory.MODERATE
            aqi <= 150 -> AqiCategory.UNHEALTHY_SENSITIVE
            aqi <= 200 -> AqiCategory.UNHEALTHY
            aqi <= 300 -> AqiCategory.VERY_UNHEALTHY
            else -> AqiCategory.HAZARDOUS
        }
    }
}
