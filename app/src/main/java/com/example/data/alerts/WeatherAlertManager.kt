package com.example.data.alerts

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.ActiveWeatherAlert
import com.example.data.model.AirQualityResponse
import com.example.data.model.FullWeatherResponse
import com.example.data.model.WeatherAlertRule
import com.example.data.model.WeatherAlertSeverity
import com.example.data.model.WeatherAlertType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherAlertManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "weather_alerts_channel"
        const val CHANNEL_NAME = "Severe Weather & Temperature Alerts"
    }

    init {
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for severe storms, heavy snow, and rapid temperature changes."
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun postNotification(alert: ActiveWeatherAlert) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ ${alert.title}")
            .setContentText("${alert.locationName}: ${alert.message}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${alert.message}\n\n📍 Location: ${alert.locationName}\n🛡️ Advisory: ${alert.safetyAction}")
            )
            .setPriority(
                when (alert.severity) {
                    WeatherAlertSeverity.CRITICAL -> NotificationCompat.PRIORITY_MAX
                    WeatherAlertSeverity.WARNING -> NotificationCompat.PRIORITY_HIGH
                    WeatherAlertSeverity.ADVISORY -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(alert.id.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Permission revoked
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun sendTestAlert(locationName: String): ActiveWeatherAlert {
        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val testAlert = ActiveWeatherAlert(
            id = "test_alert_${System.currentTimeMillis()}",
            locationName = locationName,
            type = WeatherAlertType.SEVERE_STORM,
            severity = WeatherAlertSeverity.CRITICAL,
            title = "Severe Weather Alert System Active",
            message = "Simulated convective storm warning. Rapid atmospheric pressure drop detected with lightning risk.",
            detectedAt = now,
            safetyAction = "Seek sturdy indoor shelter and avoid open high-elevation areas.",
        )
        postNotification(testAlert)
        return testAlert
    }

    fun evaluateWeatherForAlerts(
        locationName: String,
        weather: FullWeatherResponse,
        airQuality: AirQualityResponse?,
        rules: Map<WeatherAlertType, WeatherAlertRule>
    ): List<ActiveWeatherAlert> {
        val alerts = mutableListOf<ActiveWeatherAlert>()
        val current = weather.current ?: return emptyList()
        val daily = weather.daily?.let {
            if (it.temperatureMax.isNotEmpty()) it else null
        }
        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        // 1. Severe Storm Alert Check
        if (rules[WeatherAlertType.SEVERE_STORM]?.isEnabled == true) {
            val code = current.weatherCode
            val isThunderstorm = code in listOf(95, 96, 99)
            val isSeverePressureDrop = current.surfacePressure < 1004.0

            if (isThunderstorm || isSeverePressureDrop) {
                alerts.add(
                    ActiveWeatherAlert(
                        id = "storm_${locationName}_${code}",
                        locationName = locationName,
                        type = WeatherAlertType.SEVERE_STORM,
                        severity = WeatherAlertSeverity.CRITICAL,
                        title = "Severe Storm Warning: $locationName",
                        message = if (isThunderstorm) {
                            "Intense convective thunderstorm cells detected in immediate vicinity with lightning hazard."
                        } else {
                            "Barometric pressure has plummeted to %.1f hPa. High probability of imminent severe squalls.".format(current.surfacePressure)
                        },
                        detectedAt = now,
                        safetyAction = "Move indoors immediately. Unplug sensitive electronics and stay away from windows."
                    )
                )
            }
        }

        // 2. Heavy Snow & Blizzard Alert Check
        if (rules[WeatherAlertType.HEAVY_SNOW]?.isEnabled == true) {
            val code = current.weatherCode
            val isSnow = code in listOf(71, 73, 75, 77, 85, 86)
            if (isSnow) {
                alerts.add(
                    ActiveWeatherAlert(
                        id = "snow_${locationName}_${code}",
                        locationName = locationName,
                        type = WeatherAlertType.HEAVY_SNOW,
                        severity = WeatherAlertSeverity.CRITICAL,
                        title = "Heavy Snow & Winter Hazard: $locationName",
                        message = "Active snowfall and sub-freezing surface conditions detected. Significant road friction reduction.",
                        detectedAt = now,
                        safetyAction = "Equip thermal insulation gear, avoid non-essential travel, and watch for black ice."
                    )
                )
            }
        }

        // 3. Significant Temperature Drop / Freeze Check
        if (rules[WeatherAlertType.TEMPERATURE_DROP]?.isEnabled == true) {
            val currentTemp = current.temperature
            val maxTempToday = daily?.temperatureMax?.firstOrNull() ?: currentTemp
            val tempSpread = maxTempToday - currentTemp

            if (currentTemp <= 0.0) {
                alerts.add(
                    ActiveWeatherAlert(
                        id = "freeze_${locationName}",
                        locationName = locationName,
                        type = WeatherAlertType.TEMPERATURE_DROP,
                        severity = WeatherAlertSeverity.WARNING,
                        title = "Freezing Temperature Warning: $locationName",
                        message = "Current temperature has fallen to %.1f°C (%.1f°F). Frost and freeze risk for exposed vegetation and pipes.".format(
                            currentTemp,
                            currentTemp * 9.0 / 5.0 + 32.0
                        ),
                        detectedAt = now,
                        safetyAction = "Protect sensitive domestic plants, insulate outdoor water pipes, and dress warmly."
                    )
                )
            } else if (tempSpread >= 6.0) {
                alerts.add(
                    ActiveWeatherAlert(
                        id = "temp_drop_${locationName}",
                        locationName = locationName,
                        type = WeatherAlertType.TEMPERATURE_DROP,
                        severity = WeatherAlertSeverity.WARNING,
                        title = "Sharp Temperature Drop: $locationName",
                        message = "Atmospheric shift has driven temperatures down by %.1f°C since peak daylight.".format(tempSpread),
                        detectedAt = now,
                        safetyAction = "Prepare an extra thermal layer for sudden evening cooling."
                    )
                )
            }
        }

        // 4. Extreme Heat Wave Alert
        if (rules[WeatherAlertType.EXTREME_HEAT]?.isEnabled == true) {
            val currentTemp = current.temperature
            val feelsLike = current.apparentTemperature
            if (currentTemp >= 34.0 || feelsLike >= 37.0) {
                alerts.add(
                    ActiveWeatherAlert(
                        id = "heat_${locationName}",
                        locationName = locationName,
                        type = WeatherAlertType.EXTREME_HEAT,
                        severity = WeatherAlertSeverity.WARNING,
                        title = "Extreme Heat Advisory: $locationName",
                        message = "Dangerous thermal stress: Ambient %.1f°C feels like %.1f°C. High risk of dehydration and heat exhaustion.".format(
                            currentTemp,
                            feelsLike
                        ),
                        detectedAt = now,
                        safetyAction = "Stay well hydrated, remain in air-conditioned environments, and restrict midday outdoor exertion."
                    )
                )
            }
        }

        // 5. Gale Wind Squall Alert
        if (rules[WeatherAlertType.HIGH_WIND]?.isEnabled == true) {
            val windSpeed = current.windSpeed
            if (windSpeed >= 40.0) {
                alerts.add(
                    ActiveWeatherAlert(
                        id = "wind_${locationName}",
                        locationName = locationName,
                        type = WeatherAlertType.HIGH_WIND,
                        severity = WeatherAlertSeverity.WARNING,
                        title = "High Wind Squall Alert: $locationName",
                        message = "Sustained high wind velocity detected at %.1f km/h. Risk of displaced outdoor objects and tree branch debris.".format(
                            windSpeed
                        ),
                        detectedAt = now,
                        safetyAction = "Secure patio furniture, maintain firm vehicle steering, and keep clear of tall scaffolding."
                    )
                )
            }
        }

        // 6. Flash Rain Downpour Alert
        if (rules[WeatherAlertType.RAIN_DOWNPOUR]?.isEnabled == true) {
            val precip = current.precipitation
            val code = current.weatherCode
            if (precip >= 2.5 || code in listOf(65, 82)) {
                alerts.add(
                    ActiveWeatherAlert(
                        id = "rain_${locationName}",
                        locationName = locationName,
                        type = WeatherAlertType.RAIN_DOWNPOUR,
                        severity = WeatherAlertSeverity.ADVISORY,
                        title = "Heavy Rain Downpour: $locationName",
                        message = "Convective downpour active with precipitation rate of %.1f mm. Localized pooling and reduced visibility.".format(
                            precip
                        ),
                        detectedAt = now,
                        safetyAction = "Drive with reduced speed, maintain increased stopping distance, and carry waterproof gear."
                    )
                )
            }
        }

        // 7. Hazardous Air Quality Alert
        if (rules[WeatherAlertType.AIR_QUALITY]?.isEnabled == true) {
            val aqi = airQuality?.current?.usAqi ?: 0
            if (aqi >= 101) {
                alerts.add(
                    ActiveWeatherAlert(
                        id = "aqi_${locationName}",
                        locationName = locationName,
                        type = WeatherAlertType.AIR_QUALITY,
                        severity = WeatherAlertSeverity.ADVISORY,
                        title = "Elevated Air Pollution: $locationName",
                        message = "Air Quality Index has reached $aqi (Unhealthy). Elevated PM2.5 particulate concentrations detected.",
                        detectedAt = now,
                        safetyAction = "Sensitive groups and children should avoid prolonged outdoor physical exertion."
                    )
                )
            }
        }

        return alerts
    }
}
