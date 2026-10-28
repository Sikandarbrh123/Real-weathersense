package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.alerts.WeatherAlertManager
import com.example.data.model.AqiCategory
import com.example.data.model.AqiHelper
import com.example.data.model.CurrentWeatherData
import com.example.data.model.FullWeatherResponse
import com.example.data.model.WeatherAlertRule
import com.example.data.model.WeatherAlertSeverity
import com.example.data.model.WeatherAlertType
import com.example.data.model.WeatherCodeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WeatherSense", appName)
  }

  @Test
  fun `verify weather code parser for clear sky and thunderstorm`() {
    val clear = WeatherCodeParser.parse(0, isDay = true)
    assertEquals("Clear Sky", clear.title)

    val storm = WeatherCodeParser.parse(95, isDay = true)
    assertEquals("Thunderstorm", storm.title)
    assertNotNull(storm.alertAdvice)
  }

  @Test
  fun `verify aqi helper categorization`() {
    assertEquals(AqiCategory.GOOD, AqiHelper.fromUsAqi(30))
    assertEquals(AqiCategory.MODERATE, AqiHelper.fromUsAqi(75))
    assertEquals(AqiCategory.UNHEALTHY_SENSITIVE, AqiHelper.fromUsAqi(120))
    assertEquals(AqiCategory.UNHEALTHY, AqiHelper.fromUsAqi(180))
    assertEquals(AqiCategory.VERY_UNHEALTHY, AqiHelper.fromUsAqi(250))
    assertEquals(AqiCategory.HAZARDOUS, AqiHelper.fromUsAqi(350))
  }

  @Test
  fun `verify weather alert manager channel and alert evaluation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val alertManager = WeatherAlertManager(context)

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = notificationManager.getNotificationChannel(WeatherAlertManager.CHANNEL_ID)
    assertNotNull(channel)
    assertEquals(WeatherAlertManager.CHANNEL_NAME, channel.name)

    // Test alert evaluation for severe thunderstorm
    val fakeWeather = FullWeatherResponse(
        latitude = 37.77,
        longitude = -122.41,
        timezone = "auto",
        elevation = 10.0,
        current = CurrentWeatherData(
            time = "2026-10-28T14:00",
            temperature = 18.0,
            humidity = 88,
            apparentTemperature = 17.5,
            isDay = 1,
            precipitation = 15.0,
            weatherCode = 95, // Thunderstorm
            cloudCover = 100,
            pressureMsl = 1002.0,
            surfacePressure = 1001.0,
            windSpeed = 48.0,
            windDirection = 220.0
        ),
        hourly = null,
        daily = null
    )

    val rules = mapOf(
        WeatherAlertType.SEVERE_STORM to WeatherAlertRule(WeatherAlertType.SEVERE_STORM, true, ""),
        WeatherAlertType.HIGH_WIND to WeatherAlertRule(WeatherAlertType.HIGH_WIND, true, "")
    )

    val alerts = alertManager.evaluateWeatherForAlerts("San Francisco", fakeWeather, null, rules)
    assertTrue("Should detect severe storm alert", alerts.any { it.type == WeatherAlertType.SEVERE_STORM })
    assertTrue("Should detect high wind alert", alerts.any { it.type == WeatherAlertType.HIGH_WIND })
    assertEquals(WeatherAlertSeverity.CRITICAL, alerts.first { it.type == WeatherAlertType.SEVERE_STORM }.severity)
  }
}
