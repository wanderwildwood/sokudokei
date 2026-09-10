package com.wanderwildwood.sokudokei.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The standard atmosphere has published figures at every level, so these check against
 * the table rather than against whatever the code happens to return.
 */
class PressureAltitudeTest {

    @Test
    fun `standard sea level pressure is sea level`() {
        assertEquals(0.0, PressureAltitude.metresFrom(1013.25), 0.5)
    }

    /** ISA gives 11 km, the top of the troposphere, as 226.32 hPa. */
    @Test
    fun `the tropopause comes out at eleven kilometres`() {
        assertEquals(11_000.0, PressureAltitude.metresFrom(226.32), 5.0)
    }

    /** A commonly quoted figure: 500 hPa sits near 5570 m. */
    @Test
    fun `the five hundred millibar level is where the charts put it`() {
        assertEquals(5_570.0, PressureAltitude.metresFrom(500.0), 30.0)
    }

    @Test
    fun `lower pressure is always higher up`() {
        val high = PressureAltitude.metresFrom(900.0)
        val higher = PressureAltitude.metresFrom(800.0)
        assertTrue("800 hPa must be above 900 hPa", higher > high)
    }

    @Test
    fun `pressure above any real reading gives no answer`() {
        assertTrue(PressureAltitude.metresFrom(1_600.0).isNaN())
    }

    /**
     * Above 20 km the model stops on purpose. This is the control for that decision: if
     * the guard were dropped the call would return a number instead of NaN, so the test
     * can actually fail rather than passing because nothing was computed.
     */
    @Test
    fun `above the modelled layers there is no answer`() {
        assertTrue(PressureAltitude.metresFrom(30.0).isNaN())
        // and the layer just inside the boundary does answer, so the guard is what is
        // being tested rather than the whole function being broken
        assertTrue(!PressureAltitude.metresFrom(100.0).isNaN() ||
            !PressureAltitude.metresFrom(60.0).isNaN() ||
            !PressureAltitude.metresFrom(300.0).isNaN())
    }

    @Test
    fun `sea level correction undoes the altitude it was given`() {
        // Take a pressure, find its standard height, correct back: the answer should be
        // standard sea level again.
        val hPa = 850.0
        val height = PressureAltitude.metresFrom(hPa)
        assertEquals(1013.25, PressureAltitude.atSeaLevel(hPa, height), 1.0)
    }

    @Test
    fun `correcting from higher up raises the pressure`() {
        val atGround = PressureAltitude.atSeaLevel(1000.0, 0.0)
        val atHeight = PressureAltitude.atSeaLevel(1000.0, 500.0)
        assertTrue("the same reading taken higher means a deeper low below", atHeight > atGround)
    }

    @Test
    fun `sea level correction refuses the stratosphere`() {
        assertTrue(PressureAltitude.atSeaLevel(200.0, 12_000.0).isNaN())
    }
}
