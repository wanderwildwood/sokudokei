package com.wanderwildwood.sokudokei.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedTest {

    @Test
    fun `metres per second is the identity`() {
        assertEquals(12.0, Speed.MS.from(12f), 1e-9)
    }

    @Test
    fun `one metre per second is 3 point 6 km per hour`() {
        assertEquals(3.6, Speed.KMH.from(1f), 1e-9)
    }

    /** A mile is exactly 1609.344 m, so 1609.344 m/s is exactly 3600 mph. */
    @Test
    fun `a mile per second is 3600 miles per hour`() {
        assertEquals(3600.0, Speed.MPH.from(1609.344f), 1e-3)
    }

    /** A nautical mile is exactly 1852 m. */
    @Test
    fun `1852 metres per second is 3600 knots`() {
        assertEquals(3600.0, Speed.KNOTS.from(1852f), 1e-3)
    }

    @Test
    fun `motorway speed converts the way a driver would recognise`() {
        // 70 mph, the UK limit, is a shade under 31.3 m/s.
        assertEquals(70.0, Speed.MPH.from(31.2928f), 0.01)
    }

    @Test
    fun `units cycle through every value and return`() {
        var unit = Speed.KMH
        repeat(Speed.entries.size) { unit = unit.next() }
        assertEquals(Speed.KMH, unit)
    }
}

class AltitudeTest {

    @Test
    fun `metres is the identity`() {
        assertEquals(500.0, Altitude.METRES.from(500.0), 1e-9)
    }

    /** A foot is exactly 0.3048 m. */
    @Test
    fun `a thousand feet is 304 point 8 metres`() {
        assertEquals(1000.0, Altitude.FEET.from(304.8), 1e-9)
    }

    @Test
    fun `below sea level stays negative`() {
        assertTrue(Altitude.FEET.from(-100.0) < 0)
    }
}

class PressureTest {

    @Test
    fun `hectopascals is the identity`() {
        assertEquals(1013.25, Pressure.HPA.from(1013.25), 1e-9)
    }

    /** Standard sea level pressure is 1013.25 hPa, which is 29.92 inHg. */
    @Test
    fun `standard pressure is the 29 point 92 every pilot knows`() {
        assertEquals(29.92, Pressure.INHG.from(1013.25), 0.005)
    }

    @Test
    fun `inHg is shown to one more decimal than hPa`() {
        assertTrue(Pressure.INHG.decimals > Pressure.HPA.decimals)
    }
}

class CoordinateTest {

    @Test
    fun `a positive longitude reads east`() {
        assertFalse(degreesMinutesSeconds(1.5).negative)
    }

    @Test
    fun `a negative longitude reads west and drops the minus`() {
        val dms = degreesMinutesSeconds(-1.5)
        assertTrue(dms.negative)
        assertEquals("a sign would be saying west twice", Dms(true, 1, 30, 0, 0), dms)
    }

    @Test
    fun `half a degree is thirty minutes`() {
        assertEquals(Dms(false, 51, 30, 0, 0), degreesMinutesSeconds(51.5))
    }

    @Test
    fun `seconds carry from the fraction`() {
        // 0.25 of a minute is 15 seconds.
        assertEquals(Dms(false, 10, 0, 15, 0), degreesMinutesSeconds(10.0 + 15.0 / 3600.0))
    }
}

/**
 * The rounding these coordinates used to lose.
 *
 * Truncating unit by unit pushed every step's error into the one below it, so a value
 * sitting exactly on a boundary came out a tenth short. These pin the boundaries.
 */
class CoordinateRoundingTest {

    @Test
    fun `a whole degree is not one tenth short of itself`() {
        assertEquals(Dms(false, 51, 0, 0, 0), degreesMinutesSeconds(51.0))
    }

    @Test
    fun `a value that used to arrive as 14 point 9 seconds`() {
        assertEquals(Dms(false, 10, 0, 15, 0), degreesMinutesSeconds(10.0 + 15.0 / 3600.0))
    }

    @Test
    fun `tenths carry into seconds`() {
        // 59.98 seconds rounds to 60.0, which must become the next minute, not "59.10".
        assertEquals(Dms(false, 0, 1, 0, 0), degreesMinutesSeconds(59.98 / 3600.0))
    }

    @Test
    fun `seconds carry into minutes and minutes into degrees`() {
        // One hundredth of a second short of a whole degree rounds up through every unit.
        assertEquals(Dms(false, 1, 0, 0, 0), degreesMinutesSeconds(1.0 - 0.01 / 3600.0))
    }

    @Test
    fun `seconds are written to one decimal with a point`() {
        assertEquals("15.0", degreesMinutesSeconds(10.0 + 15.0 / 3600.0).secondsText)
        assertEquals("7.5", degreesMinutesSeconds(7.5 / 3600.0).secondsText)
    }

    @Test
    fun `zero is zero and reads positive`() {
        assertEquals(Dms(false, 0, 0, 0, 0), degreesMinutesSeconds(0.0))
    }
}
