package com.wanderwildwood.sokudokei.device

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Whether this phone has a barometer at all. The Kompakt does; not every phone does. */
fun hasBarometer(context: Context): Boolean {
    val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    return manager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null
}

/**
 * A stream of ambient pressure readings in hectopascals.
 *
 * Half a second between samples. The sensor will go to 10 Hz and there is no point: air
 * pressure does not change twenty times faster than the screen can draw it, and every
 * extra sample is a repaint of a digit that did not move.
 *
 * Emits nothing at all on a phone with no barometer, so the screen simply never grows the
 * pressure rows rather than showing a permanent dash.
 */
fun pressures(context: Context): Flow<Float> = callbackFlow {
    val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = manager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    if (sensor == null) {
        close()
        return@callbackFlow
    }

    val listener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        override fun onSensorChanged(event: SensorEvent?) {
            val value = event?.values?.firstOrNull() ?: return
            trySend(value)
        }
    }

    manager.registerListener(listener, sensor, 500_000)

    awaitClose { manager.unregisterListener(listener) }
}
