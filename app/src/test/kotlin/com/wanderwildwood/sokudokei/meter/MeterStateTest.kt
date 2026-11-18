package com.wanderwildwood.sokudokei.meter

import com.wanderwildwood.sokudokei.device.Fix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks what the screen reads from a stale fix. The step that marks a fix stale after five
 * seconds without a newer one (MeterViewModel's location job) runs on a coroutine delay and
 * is checked by the phone test, not here.
 */
class MeterStateTest {

    private val fix = Fix(
        speedMetresPerSecond = 12f,
        hasSpeed = true,
        altitudeMetres = 150.0,
        hasAltitude = true,
        latitude = 10.0,
        longitude = 20.0,
        accuracyMetres = 5f,
        provider = "network",
        timeMillis = 0L,
    )

    @Test
    fun `a fresh fix shows its speed`() {
        assertEquals(12f, MeterState(fix = fix).speedMetresPerSecond!!, 1e-6f)
    }

    @Test
    fun `a stale fix shows no speed`() {
        assertNull(MeterState(fix = fix, speedStale = true).speedMetresPerSecond)
    }

    @Test
    fun `a stale fix still gives the sea level pressure`() {
        val state = MeterState(fix = fix, speedStale = true, hectopascals = 1000.0)
        assertFalse(state.seaLevelHectopascals.isNaN())
    }

    @Test
    fun `a fix without speed shows no speed`() {
        assertNull(MeterState(fix = fix.copy(hasSpeed = false)).speedMetresPerSecond)
    }

    @Test
    fun `no fix shows no speed`() {
        assertNull(MeterState().speedMetresPerSecond)
    }
}
