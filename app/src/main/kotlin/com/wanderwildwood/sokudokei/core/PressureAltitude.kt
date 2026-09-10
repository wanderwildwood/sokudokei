package com.wanderwildwood.sokudokei.core

import kotlin.math.ln
import kotlin.math.pow

/**
 * Height from air pressure, by the International Standard Atmosphere.
 *
 * Ported from Blue Square Speedometer, whose author derived the formulae in the source
 * rather than copying a constant off a forum. The derivation, shortened:
 *
 *   p = ρRT and dp = -ρg dh, so dp/p = -g/(RT) dh.
 *   Where temperature falls at a constant lapse rate Γ = -dT/dh, integrating gives
 *   p/p_b = (T/T_b)^(g/RΓ), and solving for height,
 *   h = h_b + T_b · (1 - (p/p_b)^(RΓ/g)) / Γ.
 *   Where temperature is constant, the same integration gives
 *   h = h_b - (R·T/g) · ln(p/p_b).
 *
 * This is pressure altitude: what the height would be if the day matched the standard
 * atmosphere. It is not where you are. On a low-pressure day it reads high by a hundred
 * metres or more, which is why the screen labels it as a separate reading from the GPS
 * altitude rather than reconciling the two.
 */
object PressureAltitude {

    private const val G = 9.80665

    /** Specific gas constant for dry air, per ISO 2533. */
    private const val R = 287.05287

    /**
     * Height in metres for an ambient pressure in hectopascals, or NaN outside the range
     * a phone can be in.
     *
     * Only the troposphere and the isothermal layer above it are modelled — sea level to
     * 20 km. The ISA carries on to 71 km, but a barometer in a pocket cannot read ambient
     * pressure from the stratosphere: even an airliner holds its cabin near 750 hPa.
     * Layers nobody's phone can reach are layers that only need testing, never running.
     */
    fun metresFrom(hectopascals: Double): Double {
        val pascals = hectopascals * 100.0

        // Above roughly sea level pressure by a wide margin — a bad reading, not a place.
        if (pascals > 150_000.0) return Double.NaN

        // Troposphere: 15 °C at sea level, falling 6.5 °C per km, to 11 km.
        if (pascals > 22_632.0) {
            return ((15.0 + 273.15) / 0.0065) *
                (1 - (pascals / 101_325.0).pow(0.0065 * R / G))
        }

        // Isothermal at -56.5 °C from 11 km to 20 km.
        if (pascals > 5_474.9) {
            return 11_000.0 - ((-56.5 + 273.15) * R / G) * ln(pascals / 22_632.0)
        }

        return Double.NaN
    }

    /**
     * The pressure this reading would be if taken at sea level, given a known height.
     *
     * Rearranged from the troposphere case above:
     *   (p/p_b)^(RΓ/g) = 1 - Γ(h - h_b)/T_b, so p_b = p · (1 - Γh/T_b)^(-g/RΓ).
     *
     * This is the number a weather report means by "pressure", and the one worth watching
     * fall before rain. It needs a height to work from, so it stays blank until the GPS
     * has one.
     */
    fun atSeaLevel(hectopascals: Double, altitudeMetres: Double): Double {
        if (hectopascals < 226.32 || altitudeMetres > 11_000.0) return Double.NaN
        return hectopascals * (1 - 0.0065 * altitudeMetres / (15.0 + 273.15))
            .pow(-G / R / 0.0065)
    }
}
