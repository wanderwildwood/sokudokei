package com.wanderwildwood.sokudokei.meter

import android.content.Context
import com.wanderwildwood.sokudokei.core.Altitude
import com.wanderwildwood.sokudokei.core.Pressure
import com.wanderwildwood.sokudokei.core.Speed

/**
 * The four things there are to set.
 *
 * Three units and the provider they are read from. Nothing else about this app is
 * adjustable, which is why they sit on one flat list rather than behind doors.
 */
class Preferences(context: Context) {

    private val store = context.getSharedPreferences("speedometer", Context.MODE_PRIVATE)

    var speed: Speed
        get() = read(SPEED, Speed.entries, Speed.MPH)
        set(value) = write(SPEED, value.name)

    var altitude: Altitude
        get() = read(ALTITUDE, Altitude.entries, Altitude.FEET)
        set(value) = write(ALTITUDE, value.name)

    var pressure: Pressure
        get() = read(PRESSURE, Pressure.entries, Pressure.INHG)
        set(value) = write(PRESSURE, value.name)

    /** Empty until one is chosen, which is what makes the settings row say "Not set". */
    var provider: String
        get() = store.getString(PROVIDER, "") ?: ""
        set(value) = write(PROVIDER, value)

    /**
     * A stored name that no longer matches anything — an enum value renamed between
     * versions — falls back to the default rather than crashing on `valueOf`.
     */
    private fun <T : Enum<T>> read(key: String, values: List<T>, fallback: T): T {
        val stored = store.getString(key, null) ?: return fallback
        return values.firstOrNull { it.name == stored } ?: fallback
    }

    private fun write(key: String, value: String) =
        store.edit().putString(key, value).apply()

    private companion object {
        const val SPEED = "speed"
        const val ALTITUDE = "altitude"
        const val PRESSURE = "pressure"
        const val PROVIDER = "provider"
    }
}
