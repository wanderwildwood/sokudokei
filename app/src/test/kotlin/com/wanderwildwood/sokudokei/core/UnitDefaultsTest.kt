package com.wanderwildwood.sokudokei.core

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitDefaultsTest {
    @Test fun americaIsImperial() {
        assertEquals(UnitDefaults(Speed.MPH, Altitude.FEET, Pressure.INHG), UnitDefaults.forCountry("US"))
    }
    @Test fun britainKeepsMilesOnly() {
        assertEquals(UnitDefaults(Speed.MPH, Altitude.METRES, Pressure.HPA), UnitDefaults.forCountry("gb"))
    }
    @Test fun elsewhereIsMetric() {
        assertEquals(UnitDefaults(Speed.KMH, Altitude.METRES, Pressure.HPA), UnitDefaults.forCountry("PL"))
        assertEquals(UnitDefaults(Speed.KMH, Altitude.METRES, Pressure.HPA), UnitDefaults.forCountry(null))
        assertEquals(UnitDefaults(Speed.KMH, Altitude.METRES, Pressure.HPA), UnitDefaults.forCountry(""))
    }
}
