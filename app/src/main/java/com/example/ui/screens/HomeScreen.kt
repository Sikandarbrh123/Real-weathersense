package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AqiHelper
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WeatherCodeParser
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.ui.components.AirQualityCard
import com.example.ui.components.AtmosphereEffectCanvas
import com.example.ui.components.BarometerCard
import com.example.ui.components.UvIndexCard
import com.example.ui.components.WindCompassCard
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SunGold
import com.example.ui.theme.SurfaceCard

@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isGpsDetected by viewModel.isGpsDetected.collectAsState()
    val isCelsius by viewModel.isCelsius.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectCurrentGpsLocation(context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // App bar & detection controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isGpsDetected) Icons.Default.MyLocation else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isGpsDetected) RadarGreen else SkyBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = currentLocation.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = if (isGpsDetected) "Detected via GPS" else (currentLocation.country ?: "Selected Location"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Celsius / Fahrenheit toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .clickable { viewModel.toggleTemperatureUnit() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("unit_toggle_button")
                ) {
                    Text(
                        text = if (isCelsius) "°C" else "°F",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SkyBlueLight
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.testTag("gps_detect_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Detect My Location",
                        tint = if (isGpsDetected) RadarGreen else MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = onNavigateToAlerts,
                    modifier = Modifier.testTag("home_alerts_button")
                ) {
                    Box {
                        Icon(
                            imageVector = if (activeAlerts.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Weather Alerts",
                            tint = if (activeAlerts.isNotEmpty()) Color(0xFFEF4444) else MaterialTheme.colorScheme.onBackground
                        )
                        if (activeAlerts.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNavigateToSearch,
                    modifier = Modifier.testTag("search_location_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Locations",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshWeather() },
                    modifier = Modifier.testTag("refresh_weather_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = SkyBlueLight
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Weather",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SkyBlueLight,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Detecting Atmospheric Conditions...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is WeatherUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Detection Offline",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { viewModel.refreshWeather() },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlueLight),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Retry Detection", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            is WeatherUiState.Success -> {
                val current = state.weather.current
                val condition = current?.let {
                    WeatherCodeParser.parse(it.weatherCode, it.isDay == 1)
                } ?: WeatherCodeParser.parse(0)

                val dailyToday = state.daily.firstOrNull()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    // Hero Atmospheric Card
                    item {
                        AtmosphericHeroCard(
                            condition = condition,
                            currentTemp = current?.temperature ?: 20.0,
                            apparentTemp = current?.apparentTemperature ?: 20.0,
                            tempMax = dailyToday?.tempMax ?: (current?.temperature ?: 20.0),
                            tempMin = dailyToday?.tempMin ?: (current?.temperature ?: 15.0),
                            humidity = current?.humidity ?: 50,
                            windSpeed = current?.windSpeed ?: 12.0,
                            pressure = current?.surfacePressure ?: 1013.0,
                            isCelsius = isCelsius,
                            isDay = (current?.isDay ?: 1) == 1,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Barometric Storm Warning Alert banner if pressure is low or precipitation high or active alerts exist
                    if (activeAlerts.isNotEmpty() || (current?.surfacePressure ?: 1013.0) < 1008.0 || (current?.precipitation ?: 0.0) > 0.0) {
                        item {
                            StormAlertBanner(
                                condition = condition,
                                pressure = current?.surfacePressure ?: 1013.0,
                                precipitation = current?.precipitation ?: 0.0,
                                onClick = onNavigateToAlerts,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // 24-Hour Hourly Forecast
                    item {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = "24-Hour Detection Timeline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.hourly) { item ->
                                    HourlyForecastCard(item = item, isCelsius = isCelsius)
                                }
                            }
                        }
                    }

                    // 7-Day Extended Outlook
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                            Text(
                                text = "7-Day Atmospheric Outlook",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    state.daily.forEachIndexed { index, dayItem ->
                                        DailyForecastRow(
                                            item = dayItem,
                                            isCelsius = isCelsius,
                                            isLast = index == state.daily.lastIndex
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Atmospheric Telemetry Grid
                    item {
                        Text(
                            text = "Atmospheric Telemetry & Sensors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Air Quality
                            val aqi = state.airQuality?.current?.usAqi ?: 32
                            val aqiCat = AqiHelper.fromUsAqi(aqi)
                            AirQualityCard(
                                aqiValue = aqi,
                                pm25 = state.airQuality?.current?.pm25,
                                category = aqiCat,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // UV and Barometer row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                UvIndexCard(
                                    uvValue = dailyToday?.uvIndexMax ?: 4.5,
                                    modifier = Modifier.weight(1f)
                                )
                                BarometerCard(
                                    pressureHpa = current?.surfacePressure ?: 1013.2,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Wind and Solar Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WindCompassCard(
                                    windSpeedKmH = current?.windSpeed ?: 12.0,
                                    windDirectionDeg = current?.windDirection ?: 180.0,
                                    isCelsius = isCelsius,
                                    modifier = Modifier.weight(1f)
                                )

                                SolarCycleCard(
                                    sunrise = dailyToday?.sunrise ?: "06:30 AM",
                                    sunset = dailyToday?.sunset ?: "06:45 PM",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AtmosphericHeroCard(
    condition: com.example.data.model.WeatherCondition,
    currentTemp: Double,
    apparentTemp: Double,
    tempMax: Double,
    tempMin: Double,
    humidity: Int,
    windSpeed: Double,
    pressure: Double,
    isCelsius: Boolean,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val displayCurrent = formatTemp(currentTemp, isCelsius)
    val displayFeels = formatTemp(apparentTemp, isCelsius)
    val displayMax = formatTemp(tempMax, isCelsius)
    val displayMin = formatTemp(tempMin, isCelsius)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_weather_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            condition.primaryColor,
                            condition.secondaryColor.copy(alpha = 0.85f),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
        ) {
            // Live atmospheric visual particle simulation
            AtmosphereEffectCanvas(
                effect = condition.particleEffect,
                isDay = isDay,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LIVE ATMOSPHERE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = displayCurrent,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 68.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = condition.icon,
                            contentDescription = condition.title,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Text(
                    text = condition.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = condition.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // High / Low and Feels Like Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = SunGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Feels like $displayFeels",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = displayMax,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = displayMin,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Environmental Micro-Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricMiniBadge(label = "Humidity", value = "$humidity%")
                    MetricMiniBadge(label = "Wind", value = "%.0f km/h".format(windSpeed))
                    MetricMiniBadge(label = "Pressure", value = "%.0f hPa".format(pressure))
                }
            }
        }
    }
}

@Composable
fun StormAlertBanner(
    condition: com.example.data.model.WeatherCondition,
    pressure: Double,
    precipitation: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("home_storm_alert_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444))
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, Color(0x66EF4444), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFF87171),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Atmospheric Shift Detected",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFCA5A5)
                )
                Text(
                    text = condition.alertAdvice,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun HourlyForecastCard(
    item: HourlyForecastItem,
    isCelsius: Boolean,
    modifier: Modifier = Modifier
) {
    val tempStr = formatTemp(item.temperature, isCelsius)

    Card(
        modifier = modifier
            .width(82.dp)
            .testTag("hourly_card_${item.timeFormatted}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.timeFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                imageVector = item.condition.icon,
                contentDescription = item.condition.title,
                tint = if (item.isDay) SunGold else SkyBlueLight,
                modifier = Modifier.size(26.dp)
            )

            Text(
                text = tempStr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (item.precipitationProb > 10) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "${item.precipitationProb}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
fun DailyForecastRow(
    item: DailyForecastItem,
    isCelsius: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.width(90.dp)) {
                Text(
                    text = item.dayOfWeek,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.dateFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.condition.icon,
                    contentDescription = item.condition.title,
                    tint = SunGold,
                    modifier = Modifier.size(24.dp)
                )

                if (item.precipitationProb > 15) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.precipitationProb}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTemp(item.tempMin, isCelsius),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Visual mini bar
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF38BDF8), SunGold)
                            )
                        )
                )

                Text(
                    text = formatTemp(item.tempMax, isCelsius),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }
    }
}

@Composable
fun SolarCycleCard(
    sunrise: String,
    sunset: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("solar_cycle_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness5,
                    contentDescription = null,
                    tint = SunGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Solar Cycle",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Brightness5,
                            contentDescription = null,
                            tint = SunGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sunrise",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = sunrise,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Brightness6,
                            contentDescription = null,
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sunset",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = sunset,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "Atmospheric daylight index tracker.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricMiniBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

private fun formatTemp(celsius: Double, isCelsius: Boolean): String {
    return if (isCelsius) {
        "${kotlin.math.round(celsius).toInt()}°"
    } else {
        val fahrenheit = (celsius * 9.0 / 5.0) + 32.0
        "${kotlin.math.round(fahrenheit).toInt()}°"
    }
}
