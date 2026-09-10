package com.wanderwildwood.sokudokei.device

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * One reading from the GPS.
 *
 * [altitudeMetres] is height above the WGS84 ellipsoid, which is what Android gives on
 * this phone. It is not height above sea level: the two differ by tens of metres in most
 * of the world. Android can convert since API 34, and the Kompakt is API 31, so the
 * conversion is not available here and the screen says which one it is showing instead of
 * quietly implying the other.
 */
data class Fix(
    val speedMetresPerSecond: Float,
    val hasSpeed: Boolean,
    val altitudeMetres: Double,
    val hasAltitude: Boolean,
    val latitude: Double,
    val longitude: Double,
    val accuracyMetres: Float,
    val provider: String,
    val timeMillis: Long,
)

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

/** The providers this phone actually offers, whether or not they are switched on. */
fun providers(context: Context): List<String> {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return runCatching { manager.getProviders(false) }.getOrDefault(emptyList())
}

fun providerEnabled(context: Context, provider: String): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
}

fun locationEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return runCatching { manager.isLocationEnabled }.getOrDefault(false)
}

/**
 * A stream of fixes from one named provider.
 *
 * The provider is chosen rather than assumed. GPS is the one that reports a speed worth
 * showing, but a phone may carry others, and which of them answers is a fact about the
 * handset rather than something this app should decide for it.
 *
 * One second between updates. The panel cannot redraw faster than that without smearing,
 * so asking the GPS for more is asking the radio to burn battery producing numbers that
 * are thrown away before anyone sees them.
 */
@SuppressLint("MissingPermission")
fun fixes(context: Context, provider: String): Flow<Fix> = callbackFlow {
    if (provider.isEmpty() || !hasLocationPermission(context)) {
        close()
        return@callbackFlow
    }

    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun send(location: Location?) {
        if (location == null) return
        trySend(
            Fix(
                speedMetresPerSecond = location.speed,
                hasSpeed = location.hasSpeed(),
                altitudeMetres = location.altitude,
                hasAltitude = location.hasAltitude(),
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMetres = if (location.hasAccuracy()) location.accuracy else Float.NaN,
                provider = location.provider ?: provider,
                timeMillis = location.time,
            )
        )
    }

    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) = send(location)

        // Deprecated from API 29 and never called there, but the interface still declares
        // it on the SDK this builds against.
        @Suppress("OVERRIDE_DEPRECATION")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
    }

    val started = runCatching {
        manager.requestLocationUpdates(provider, 1_000L, 0f, listener)
    }.isSuccess

    if (!started) {
        close()
        return@callbackFlow
    }

    awaitClose { manager.removeUpdates(listener) }
}
