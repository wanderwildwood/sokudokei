package com.wanderwildwood.sokudokei.meter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wanderwildwood.sokudokei.core.Altitude
import com.wanderwildwood.sokudokei.core.Pressure
import com.wanderwildwood.sokudokei.core.PressureAltitude
import com.wanderwildwood.sokudokei.core.Speed
import com.wanderwildwood.sokudokei.device.Fix
import com.wanderwildwood.sokudokei.device.fixes
import com.wanderwildwood.sokudokei.device.hasBarometer
import com.wanderwildwood.sokudokei.device.hasLocationPermission
import com.wanderwildwood.sokudokei.device.locationEnabled
import com.wanderwildwood.sokudokei.device.pressures
import com.wanderwildwood.sokudokei.device.providerEnabled
import com.wanderwildwood.sokudokei.device.providers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Why there is no reading, when there is no reading. */
enum class Trouble { NONE, NO_PERMISSION, LOCATION_OFF, NO_PROVIDER, PROVIDER_OFF }

data class MeterState(
    val fix: Fix? = null,
    val hectopascals: Double = Double.NaN,
    val speedUnit: Speed = Speed.MPH,
    val altitudeUnit: Altitude = Altitude.FEET,
    val pressureUnit: Pressure = Pressure.INHG,
    val provider: String = "",
    val providers: List<String> = emptyList(),
    val hasBarometer: Boolean = false,
    val trouble: Trouble = Trouble.NONE,
) {
    /** Pressure altitude, which needs only the barometer. */
    val pressureAltitudeMetres: Double
        get() = if (hectopascals.isNaN()) Double.NaN
        else PressureAltitude.metresFrom(hectopascals)

    /**
     * Sea level pressure, which needs the GPS height as well and stays blank without it.
     * Saying nothing is the honest answer; a number derived from a height we do not have
     * would look exactly as confident as one we do.
     */
    val seaLevelHectopascals: Double
        get() {
            val height = fix?.altitudeMetres ?: return Double.NaN
            if (hectopascals.isNaN() || fix.hasAltitude.not()) return Double.NaN
            return PressureAltitude.atSeaLevel(hectopascals, height)
        }
}

class MeterViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = Preferences(application)

    private val _state = MutableStateFlow(
        MeterState(
            speedUnit = preferences.speed,
            altitudeUnit = preferences.altitude,
            pressureUnit = preferences.pressure,
            provider = preferences.provider,
            hasBarometer = hasBarometer(application),
        )
    )
    val state: StateFlow<MeterState> = _state.asStateFlow()

    private var locationJob: Job? = null
    private var pressureJob: Job? = null

    /**
     * Called every time the screen comes back, because all four of these can change while
     * the app is in the background: the permission in Settings, the location switch in the
     * shade, the provider list when a mock provider is installed.
     */
    fun refresh() {
        val context = getApplication<Application>()
        val available = providers(context)

        // A provider that has gone away is not a provider. Dropping it here is what makes
        // the screen offer the picker again rather than waiting on a name that is dead.
        var chosen = preferences.provider
        if (chosen.isNotEmpty() && chosen !in available) {
            chosen = ""
            preferences.provider = ""
        }

        // The GPS is what a speedometer wants, so take it unasked the first time rather
        // than opening with a picker over a list most people will not recognise.
        if (chosen.isEmpty() && "gps" in available) {
            chosen = "gps"
            preferences.provider = chosen
        }

        _state.update {
            it.copy(
                provider = chosen,
                providers = available,
                speedUnit = preferences.speed,
                altitudeUnit = preferences.altitude,
                pressureUnit = preferences.pressure,
                trouble = trouble(chosen),
            )
        }

        startLocation(chosen)
        startPressure()
    }

    /** Stops both sensors. Neither has any business running behind another screen. */
    fun pause() {
        locationJob?.cancel()
        locationJob = null
        pressureJob?.cancel()
        pressureJob = null
    }

    fun permissionAnswered(granted: Boolean) {
        if (granted) refresh() else _state.update { it.copy(trouble = Trouble.NO_PERMISSION) }
    }

    fun nextSpeedUnit() {
        preferences.speed = preferences.speed.next()
        _state.update { it.copy(speedUnit = preferences.speed) }
    }

    fun nextAltitudeUnit() {
        preferences.altitude = preferences.altitude.next()
        _state.update { it.copy(altitudeUnit = preferences.altitude) }
    }

    fun nextPressureUnit() {
        preferences.pressure = preferences.pressure.next()
        _state.update { it.copy(pressureUnit = preferences.pressure) }
    }

    fun chooseProvider(provider: String) {
        preferences.provider = provider
        _state.update { it.copy(provider = provider, fix = null, trouble = trouble(provider)) }
        startLocation(provider)
    }

    private fun trouble(provider: String): Trouble {
        val context = getApplication<Application>()
        return when {
            !hasLocationPermission(context) -> Trouble.NO_PERMISSION
            !locationEnabled(context) -> Trouble.LOCATION_OFF
            provider.isEmpty() -> Trouble.NO_PROVIDER
            !providerEnabled(context, provider) -> Trouble.PROVIDER_OFF
            else -> Trouble.NONE
        }
    }

    private fun startLocation(provider: String) {
        locationJob?.cancel()
        val context = getApplication<Application>()
        if (provider.isEmpty() || !hasLocationPermission(context)) return

        locationJob = viewModelScope.launch {
            fixes(context, provider).collect { fix ->
                _state.update { it.copy(fix = fix, trouble = Trouble.NONE) }
            }
        }
    }

    private fun startPressure() {
        pressureJob?.cancel()
        val context = getApplication<Application>()
        if (!hasBarometer(context)) return

        pressureJob = viewModelScope.launch {
            pressures(context).collect { hPa ->
                _state.update { it.copy(hectopascals = hPa.toDouble()) }
            }
        }
    }
}
