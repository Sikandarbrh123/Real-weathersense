package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<LocationItem>?
)

@JsonClass(generateAdapter = true)
data class LocationItem(
    val id: Long?,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @Json(name = "country_code") val countryCode: String? = null,
    val admin1: String? = null,
    val timezone: String? = null,
    val elevation: Double? = null
) {
    val displayName: String
        get() = buildString {
            append(name)
            if (!admin1.isNullOrBlank()) append(", $admin1")
            if (!country.isNullOrBlank()) append(", $country")
        }
}

object PresetLocations {
    val defaultList = listOf(
        LocationItem(id = 1L, name = "San Francisco", latitude = 37.7749, longitude = -122.4194, country = "United States", admin1 = "California"),
        LocationItem(id = 2L, name = "Tokyo", latitude = 35.6762, longitude = 139.6503, country = "Japan", admin1 = "Tokyo"),
        LocationItem(id = 3L, name = "London", latitude = 51.5074, longitude = -0.1278, country = "United Kingdom", admin1 = "England"),
        LocationItem(id = 4L, name = "New York", latitude = 40.7128, longitude = -74.0060, country = "United States", admin1 = "New York"),
        LocationItem(id = 5L, name = "Paris", latitude = 48.8566, longitude = 2.3522, country = "France", admin1 = "Île-de-France"),
        LocationItem(id = 6L, name = "Sydney", latitude = -33.8688, longitude = 151.2093, country = "Australia", admin1 = "New South Wales"),
        LocationItem(id = 7L, name = "Dubai", latitude = 25.2048, longitude = 55.2708, country = "United Arab Emirates", admin1 = "Dubai"),
        LocationItem(id = 8L, name = "Singapore", latitude = 1.3521, longitude = 103.8198, country = "Singapore", admin1 = "Singapore"),
        LocationItem(id = 9L, name = "Reykjavik", latitude = 64.1466, longitude = -21.9426, country = "Iceland", admin1 = "Capital Region")
    )
}
