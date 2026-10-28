package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.alerts.WeatherAlertManager
import com.example.data.location.LocationDetector
import com.example.data.model.ActiveWeatherAlert
import com.example.data.model.AirQualityResponse
import com.example.data.model.DailyForecastItem
import com.example.data.model.FullWeatherResponse
import com.example.data.model.HourlyForecastItem
import com.example.data.model.LocationItem
import com.example.data.model.PresetLocations
import com.example.data.model.PresetSkyScenarios
import com.example.data.model.SkyScanResult
import com.example.data.model.WeatherAlertRule
import com.example.data.model.WeatherAlertType
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val weather: FullWeatherResponse,
        val hourly: List<HourlyForecastItem>,
        val daily: List<DailyForecastItem>,
        val airQuality: AirQualityResponse?
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

sealed interface SkyScanUiState {
    object Idle : SkyScanUiState
    object Scanning : SkyScanUiState
    data class Result(
        val scanResult: SkyScanResult,
        val customImageUri: Uri? = null
    ) : SkyScanUiState
}

class WeatherViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = WeatherRepository()
    private val alertManager = WeatherAlertManager(application)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow(PresetLocations.defaultList[0])
    val currentLocation: StateFlow<LocationItem> = _currentLocation.asStateFlow()

    private val _isGpsDetected = MutableStateFlow(false)
    val isGpsDetected: StateFlow<Boolean> = _isGpsDetected.asStateFlow()

    private val _isCelsius = MutableStateFlow(true)
    val isCelsius: StateFlow<Boolean> = _isCelsius.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<LocationItem>>(emptyList())
    val searchResults: StateFlow<List<LocationItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _skyScanState = MutableStateFlow<SkyScanUiState>(
        SkyScanUiState.Result(PresetSkyScenarios.presets[0])
    )
    val skyScanState: StateFlow<SkyScanUiState> = _skyScanState.asStateFlow()

    // Alert System State
    private val _alertRules = MutableStateFlow<Map<WeatherAlertType, WeatherAlertRule>>(
        mapOf(
            WeatherAlertType.SEVERE_STORM to WeatherAlertRule(
                type = WeatherAlertType.SEVERE_STORM,
                isEnabled = true,
                thresholdDescription = "Active convective thunderstorm or pressure < 1004 hPa"
            ),
            WeatherAlertType.HEAVY_SNOW to WeatherAlertRule(
                type = WeatherAlertType.HEAVY_SNOW,
                isEnabled = true,
                thresholdDescription = "Heavy snow accumulation, blizzards, freezing road hazards"
            ),
            WeatherAlertType.TEMPERATURE_DROP to WeatherAlertRule(
                type = WeatherAlertType.TEMPERATURE_DROP,
                isEnabled = true,
                thresholdDescription = "Sub-freezing (<= 0°C / 32°F) or rapid diurnal plunge >= 6°C"
            ),
            WeatherAlertType.EXTREME_HEAT to WeatherAlertRule(
                type = WeatherAlertType.EXTREME_HEAT,
                isEnabled = true,
                thresholdDescription = "Ambient temperature >= 34°C or apparent heat index >= 37°C"
            ),
            WeatherAlertType.HIGH_WIND to WeatherAlertRule(
                type = WeatherAlertType.HIGH_WIND,
                isEnabled = true,
                thresholdDescription = "Sustained high wind speeds or microburst gusts >= 40 km/h"
            ),
            WeatherAlertType.RAIN_DOWNPOUR to WeatherAlertRule(
                type = WeatherAlertType.RAIN_DOWNPOUR,
                isEnabled = true,
                thresholdDescription = "Convective downpours >= 2.5 mm/hr with localized flash pooling"
            ),
            WeatherAlertType.AIR_QUALITY to WeatherAlertRule(
                type = WeatherAlertType.AIR_QUALITY,
                isEnabled = true,
                thresholdDescription = "Unhealthy atmospheric particulate reading with AQI >= 101"
            )
        )
    )
    val alertRules: StateFlow<Map<WeatherAlertType, WeatherAlertRule>> = _alertRules.asStateFlow()

    // Location monitoring settings (default all preset locations monitored)
    private val _locationAlertSettings = MutableStateFlow<Map<String, Boolean>>(
        (listOf("Current Location") + PresetLocations.defaultList.map { it.name })
            .associateWith { true }
    )
    val locationAlertSettings: StateFlow<Map<String, Boolean>> = _locationAlertSettings.asStateFlow()

    private val _activeAlerts = MutableStateFlow<List<ActiveWeatherAlert>>(emptyList())
    val activeAlerts: StateFlow<List<ActiveWeatherAlert>> = _activeAlerts.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(alertManager.hasNotificationPermission())
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadWeatherData(_currentLocation.value)
    }

    fun switchTab(index: Int) {
        _activeTab.value = index
    }

    fun toggleTemperatureUnit() {
        _isCelsius.value = !_isCelsius.value
    }

    fun selectLocation(location: LocationItem, fromGps: Boolean = false) {
        _currentLocation.value = location
        _isGpsDetected.value = fromGps
        loadWeatherData(location)
    }

    fun refreshWeather() {
        loadWeatherData(_currentLocation.value, isRefresh = true)
    }

    fun detectCurrentGpsLocation(context: Context) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val detector = LocationDetector(context)
                val location = detector.getCurrentCoordinates()
                if (location != null) {
                    val locationItem = detector.reverseGeocodeLocation(location)
                    _currentLocation.value = locationItem
                    _isGpsDetected.value = true
                    loadWeatherData(locationItem)
                } else {
                    loadWeatherData(_currentLocation.value)
                }
            } catch (e: Exception) {
                loadWeatherData(_currentLocation.value)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(350)
            val results = repository.searchLocations(query.trim())
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    fun scanCustomImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _skyScanState.value = SkyScanUiState.Scanning
            delay(1200)
            val result = repository.analyzeImageOpticalSky(context, imageUri)
            _skyScanState.value = SkyScanUiState.Result(scanResult = result, customImageUri = imageUri)
        }
    }

    fun selectSkyPreset(preset: SkyScanResult) {
        viewModelScope.launch {
            _skyScanState.value = SkyScanUiState.Scanning
            delay(600)
            _skyScanState.value = SkyScanUiState.Result(scanResult = preset, customImageUri = null)
        }
    }

    // Alert Management Methods
    fun updateNotificationPermission(isGranted: Boolean) {
        _hasNotificationPermission.value = isGranted
    }

    fun toggleAlertRule(type: WeatherAlertType) {
        val currentMap = _alertRules.value.toMutableMap()
        val currentRule = currentMap[type] ?: return
        currentMap[type] = currentRule.copy(isEnabled = !currentRule.isEnabled)
        _alertRules.value = currentMap

        // Re-evaluate alerts
        val state = _uiState.value
        if (state is WeatherUiState.Success) {
            evaluateAndNotifyAlerts(
                locationName = _currentLocation.value.name,
                weather = state.weather,
                airQuality = state.airQuality
            )
        }
    }

    fun toggleLocationAlert(locationName: String) {
        val currentMap = _locationAlertSettings.value.toMutableMap()
        val currentValue = currentMap[locationName] ?: true
        currentMap[locationName] = !currentValue
        _locationAlertSettings.value = currentMap
    }

    fun sendTestAlert() {
        val testAlert = alertManager.sendTestAlert(_currentLocation.value.name)
        val currentList = _activeAlerts.value.toMutableList()
        currentList.removeAll { it.id.startsWith("test_alert_") }
        currentList.add(0, testAlert)
        _activeAlerts.value = currentList
    }

    fun dismissAlert(alertId: String) {
        _activeAlerts.value = _activeAlerts.value.filterNot { it.id == alertId }
    }

    private fun evaluateAndNotifyAlerts(
        locationName: String,
        weather: FullWeatherResponse,
        airQuality: AirQualityResponse?
    ) {
        val isMonitored = _locationAlertSettings.value[locationName] ?: true
        if (!isMonitored) return

        val detectedAlerts = alertManager.evaluateWeatherForAlerts(
            locationName = locationName,
            weather = weather,
            airQuality = airQuality,
            rules = _alertRules.value
        )

        val existingIds = _activeAlerts.value.map { it.id }.toSet()
        val newAlerts = detectedAlerts.filter { it.id !in existingIds }

        // Dispatch notifications for newly detected severe warnings
        newAlerts.forEach { alert ->
            alertManager.postNotification(alert)
        }

        // Keep existing test alert or merge
        val testAlerts = _activeAlerts.value.filter { it.id.startsWith("test_alert_") }
        _activeAlerts.value = (testAlerts + detectedAlerts).distinctBy { it.id }
    }

    private fun loadWeatherData(location: LocationItem, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = WeatherUiState.Loading
            }

            try {
                val weather = repository.getFullWeather(location.latitude, location.longitude)
                val hourly = repository.parseHourlyForecast(weather)
                val daily = repository.parseDailyForecast(weather)
                val airQuality = try {
                    repository.getAirQuality(location.latitude, location.longitude)
                } catch (e: Exception) {
                    null
                }

                _uiState.value = WeatherUiState.Success(
                    weather = weather,
                    hourly = hourly,
                    daily = daily,
                    airQuality = airQuality
                )

                // Run alert detection engine
                evaluateAndNotifyAlerts(
                    locationName = location.name,
                    weather = weather,
                    airQuality = airQuality
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    e.localizedMessage ?: "Failed to detect weather data. Please check your network connection."
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
