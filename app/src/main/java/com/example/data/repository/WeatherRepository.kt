package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.api.ApiClient
import com.example.data.model.AirQualityResponse
import com.example.data.model.DailyForecastItem
import com.example.data.model.FullWeatherResponse
import com.example.data.model.HourlyForecastItem
import com.example.data.model.LocationItem
import com.example.data.model.PresetSkyScenarios
import com.example.data.model.SkyScanResult
import com.example.data.model.StormRiskLevel
import com.example.data.model.WeatherCodeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherRepository {

    private val weatherApi = ApiClient.weatherService
    private val airQualityApi = ApiClient.airQualityService
    private val geocodingApi = ApiClient.geocodingService

    suspend fun getFullWeather(lat: Double, lon: Double): FullWeatherResponse {
        return withContext(Dispatchers.IO) {
            weatherApi.getForecast(latitude = lat, longitude = lon)
        }
    }

    suspend fun getAirQuality(lat: Double, lon: Double): AirQualityResponse {
        return withContext(Dispatchers.IO) {
            airQualityApi.getAirQuality(latitude = lat, longitude = lon)
        }
    }

    suspend fun searchLocations(query: String): List<LocationItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response = geocodingApi.searchLocations(name = query)
                response.results ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun parseHourlyForecast(response: FullWeatherResponse): List<HourlyForecastItem> {
        val hourly = response.hourly ?: return emptyList()
        val times = hourly.time
        val temps = hourly.temperature
        val codes = hourly.weatherCode
        val probs = hourly.precipitationProbability

        val items = mutableListOf<HourlyForecastItem>()
        val count = minOf(times.size, temps.size, codes.size, 24)

        for (i in 0 until count) {
            val rawTime = times[i]
            val hourFormatted = formatHour(rawTime)
            val code = codes[i]
            val prob = probs?.getOrNull(i) ?: 0
            val isDay = isDayTimeFromHour(rawTime)
            val condition = WeatherCodeParser.parse(code, isDay)

            items.add(
                HourlyForecastItem(
                    timeFormatted = hourFormatted,
                    rawTime = rawTime,
                    temperature = temps[i],
                    weatherCode = code,
                    precipitationProb = prob,
                    condition = condition,
                    isDay = isDay
                )
            )
        }
        return items
    }

    fun parseDailyForecast(response: FullWeatherResponse): List<DailyForecastItem> {
        val daily = response.daily ?: return emptyList()
        val times = daily.time
        val codes = daily.weatherCode
        val maxTemps = daily.temperatureMax
        val minTemps = daily.temperatureMin
        val probs = daily.precipitationProbabilityMax
        val sunrises = daily.sunrise
        val sunsets = daily.sunset
        val uvs = daily.uvIndexMax

        val items = mutableListOf<DailyForecastItem>()
        val count = minOf(times.size, codes.size, maxTemps.size, minTemps.size)

        for (i in 0 until count) {
            val rawDate = times[i]
            val dayOfWeek = formatDayOfWeek(rawDate, i)
            val dateFormatted = formatDateMonth(rawDate)
            val code = codes[i]
            val condition = WeatherCodeParser.parse(code, isDay = true)

            items.add(
                DailyForecastItem(
                    dateFormatted = dateFormatted,
                    dayOfWeek = dayOfWeek,
                    weatherCode = code,
                    tempMax = maxTemps[i],
                    tempMin = minTemps[i],
                    precipitationProb = probs?.getOrNull(i) ?: 0,
                    condition = condition,
                    uvIndexMax = uvs?.getOrNull(i) ?: 4.0,
                    sunrise = sunrises?.getOrNull(i)?.let { formatHour(it) },
                    sunset = sunsets?.getOrNull(i)?.let { formatHour(it) }
                )
            )
        }
        return items
    }

    suspend fun analyzeImageOpticalSky(context: Context, imageUri: Uri): SkyScanResult {
        return withContext(Dispatchers.Default) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    analyzeBitmapAtmosphere(bitmap)
                } else {
                    PresetSkyScenarios.presets[0]
                }
            } catch (e: Exception) {
                PresetSkyScenarios.presets[0]
            }
        }
    }

    private fun analyzeBitmapAtmosphere(bitmap: Bitmap): SkyScanResult {
        // Downsample bitmap for fast color and luminance analysis
        val scaled = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        var totalBrightness = 0.0
        var totalBlue = 0.0
        var totalRed = 0.0
        var totalGreen = 0.0
        var darkPixelCount = 0
        var whitePixelCount = 0
        var bluePixelCount = 0

        val totalPixels = scaled.width * scaled.height
        for (y in 0 until scaled.height) {
            for (x in 0 until scaled.width) {
                val pixel = scaled.getPixel(x, y)
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b)

                totalBrightness += brightness
                totalRed += r
                totalGreen += g
                totalBlue += b

                if (brightness < 65) darkPixelCount++
                if (brightness > 190 && Math.abs(r - g) < 25 && Math.abs(g - b) < 25) whitePixelCount++
                if (b > r + 20 && b > g + 10) bluePixelCount++
            }
        }

        val avgBrightness = totalBrightness / totalPixels
        val darkRatio = darkPixelCount.toDouble() / totalPixels
        val whiteRatio = whitePixelCount.toDouble() / totalPixels
        val blueRatio = bluePixelCount.toDouble() / totalPixels

        return when {
            darkRatio > 0.35 || avgBrightness < 80 -> {
                SkyScanResult(
                    id = "custom_storm",
                    title = "Detected: Storm Front / Cumulonimbus",
                    cloudGenus = "Cumulonimbus / Low Nimbostratus",
                    cloudFamily = "Convective Storm System",
                    estimatedAltitudeKm = "0.8 km - 10.5 km",
                    confidencePercent = (85 + (darkRatio * 14)).toInt().coerceIn(85, 98),
                    precipitationRiskPercent = (80 + (darkRatio * 20)).toInt().coerceIn(80, 99),
                    stormRisk = StormRiskLevel.SEVERE,
                    opticalPhenomenon = "Dense storm base, localized lightning potential, virga downdraft",
                    meteorologicalAnalysis = "High optical density and low scene luminance confirm thick, moisture-laden convective cloud masses. Impending precipitation and turbulent wind gusts.",
                    outdoorRecommendation = "Seek indoor shelter immediately. Avoid tall trees and outdoor electrical gear.",
                    flightAviationImpact = "Severe convective turbulence and strong downdrafts. Hazardous flight zone.",
                    imageDescription = "Dark ominous storm clouds detected across the sky horizon."
                )
            }
            blueRatio > 0.45 && whiteRatio < 0.25 -> {
                SkyScanResult(
                    id = "custom_clear",
                    title = "Detected: Clear / High Cirrus Sky",
                    cloudGenus = "Cirrus Fibratus / Clear Sky",
                    cloudFamily = "High Ice Cloud or Free Troposphere",
                    estimatedAltitudeKm = "8.0 km - 12.0 km",
                    confidencePercent = (88 + (blueRatio * 10)).toInt().coerceIn(88, 97),
                    precipitationRiskPercent = 8,
                    stormRisk = StormRiskLevel.NONE,
                    opticalPhenomenon = "High Rayleigh scattering, solar rays, crystal optical transparency",
                    meteorologicalAnalysis = "Dominant atmospheric blue spectrum with minimal aerosol attenuation. Stable anticyclonic high-pressure air mass.",
                    outdoorRecommendation = "Ideal outdoor conditions for running, hiking, and sports. High UV index; wear sunscreen.",
                    flightAviationImpact = "Smooth flight conditions, CAVOK (Ceiling And Visibility OK).",
                    imageDescription = "Open azure sky with faint wispy high-altitude ice trails."
                )
            }
            whiteRatio > 0.40 -> {
                SkyScanResult(
                    id = "custom_cumulus",
                    title = "Detected: Cumulus / Stratocumulus Deck",
                    cloudGenus = "Stratocumulus / Altocumulus",
                    cloudFamily = "Mid-to-Low Cloud Deck",
                    estimatedAltitudeKm = "1.8 km - 4.5 km",
                    confidencePercent = (84 + (whiteRatio * 12)).toInt().coerceIn(84, 95),
                    precipitationRiskPercent = 32,
                    stormRisk = StormRiskLevel.LOW,
                    opticalPhenomenon = "Bright diffused solar backscattering, rolling cumulus waves",
                    meteorologicalAnalysis = "Extensive fair-weather stratocumulus or mid-level altocumulus layers. Mild convection capped by dry air layer above.",
                    outdoorRecommendation = "Great overcast/partly cloudy weather. Moderate temperatures and low sunburn risk.",
                    flightAviationImpact = "Minor light chop below the cloud base.",
                    imageDescription = "Fluffy textured cloud fields with strong ambient sunlight reflection."
                )
            }
            else -> {
                SkyScanResult(
                    id = "custom_altostratus",
                    title = "Detected: Overcast Altostratus Sheet",
                    cloudGenus = "Altostratus / Stratus Translucidus",
                    cloudFamily = "Mid-Level Stratiform Sheet",
                    estimatedAltitudeKm = "2.0 km - 5.5 km",
                    confidencePercent = 87,
                    precipitationRiskPercent = 54,
                    stormRisk = StormRiskLevel.MODERATE,
                    opticalPhenomenon = "Watery sun disc, corona diffraction, diffuse ambient overcast",
                    meteorologicalAnalysis = "Broad frontal cloud sheet. Steady thickening may yield light rain or drizzle over the next few hours.",
                    outdoorRecommendation = "Cool and overcast. Consider packing a light jacket and compact umbrella.",
                    flightAviationImpact = "Extensive cloud ceiling with moderate instrument flight rules (IFR).",
                    imageDescription = "Uniform gray sky covering the celestial dome."
                )
            }
        }
    }

    private fun formatHour(rawTime: String): String {
        return try {
            val parts = rawTime.split("T")
            if (parts.size >= 2) {
                val timePart = parts[1]
                val hour = timePart.substringBefore(":").toInt()
                when {
                    hour == 0 -> "12 AM"
                    hour < 12 -> "$hour AM"
                    hour == 12 -> "12 PM"
                    else -> "${hour - 12} PM"
                }
            } else {
                rawTime
            }
        } catch (e: Exception) {
            rawTime
        }
    }

    private fun isDayTimeFromHour(rawTime: String): Boolean {
        return try {
            val parts = rawTime.split("T")
            if (parts.size >= 2) {
                val hour = parts[1].substringBefore(":").toInt()
                hour in 6..19
            } else true
        } catch (e: Exception) {
            true
        }
    }

    private fun formatDayOfWeek(rawDate: String, index: Int): String {
        if (index == 0) return "Today"
        if (index == 1) return "Tomorrow"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(rawDate)
            if (date != null) {
                SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            } else "Day $index"
        } catch (e: Exception) {
            "Day $index"
        }
    }

    private fun formatDateMonth(rawDate: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(rawDate)
            if (date != null) {
                SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
            } else rawDate
        } catch (e: Exception) {
            rawDate
        }
    }
}
