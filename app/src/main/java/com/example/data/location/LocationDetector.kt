package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.data.model.LocationItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class LocationDetector(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    suspend fun getCurrentCoordinates(): Location? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { cont ->
            try {
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            cont.resume(location)
                        } else {
                            // Fallback to lastLocation
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                cont.resume(lastLoc)
                            }.addOnFailureListener {
                                cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            } catch (e: SecurityException) {
                cont.resume(null)
            } catch (e: Exception) {
                cont.resume(null)
            }
        }
    }

    fun reverseGeocodeLocation(location: Location): LocationItem {
        val lat = location.latitude
        val lon = location.longitude
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val cityName = addr.locality
                    ?: addr.subAdminArea
                    ?: addr.adminArea
                    ?: "Detected Location"
                LocationItem(
                    id = System.currentTimeMillis(),
                    name = cityName,
                    latitude = lat,
                    longitude = lon,
                    country = addr.countryName,
                    admin1 = addr.adminArea
                )
            } else {
                LocationItem(
                    id = System.currentTimeMillis(),
                    name = "Current Location",
                    latitude = lat,
                    longitude = lon,
                    country = "GPS Detected"
                )
            }
        } catch (e: Exception) {
            LocationItem(
                id = System.currentTimeMillis(),
                name = "Current Location",
                latitude = lat,
                longitude = lon,
                country = "GPS Detected"
            )
        }
    }
}
