package com.wanderwildwood.sokudokei.core

/**
 * The units a reading can be said in, and the factors that get it there.
 *
 * The conversion factors and their sources come from Blue Square Speedometer, whose author
 * chased each one to a standards document rather than a search result. They are kept here
 * with the reasoning attached, because a bare `1.609344` in a file is a number nobody can
 * check a year later.
 */

/** How fast. */
enum class Speed(val label: String) {
    KMH("km/h"),
    MPH("mph"),
    KNOTS("knots"),
    MS("m/s"),
    ;

    /**
     * Metres per second, as Android reports speed, converted for display.
     *
     * 1 yard = 0.9144 m and 1 mile = 1760 yards, both by the 1959 international yard and
     * pound agreement, giving a mile of exactly 1609.344 m. A nautical mile is exactly
     * 1852 m, and a knot is one of those per hour.
     */
    fun from(metresPerSecond: Float): Double = when (this) {
        KMH -> metresPerSecond * 3.6
        MPH -> metresPerSecond * 3.6 / 1.609344
        KNOTS -> metresPerSecond * 3.6 / 1.852
        MS -> metresPerSecond.toDouble()
    }

    fun next(): Speed = entries[(ordinal + 1) % entries.size]
}

/** How high. */
enum class Altitude(val label: String) {
    METRES("m"),
    FEET("ft"),
    ;

    /** A foot is exactly 0.3048 m, again by the 1959 agreement. */
    fun from(metres: Double): Double = when (this) {
        METRES -> metres
        FEET -> metres / 0.3048
    }

    fun next(): Altitude = entries[(ordinal + 1) % entries.size]
}

/** How heavy the air. */
enum class Pressure(val label: String) {
    HPA("hPa"),
    INHG("inHg"),
    ;

    /**
     * 1 inHg = 3386.389 Pa, so 33.86389 hPa.
     *
     * Wikipedia and NIST SP 811 both give 3386.389 Pa; the Japanese Measurement Act rounds
     * it to 3386.39. At the five digits ever shown here the difference cannot appear.
     */
    fun from(hectopascals: Double): Double = when (this) {
        HPA -> hectopascals
        INHG -> hectopascals / 33.86389
    }

    /** inHg wants a further decimal to say the same amount of truth as hPa. */
    val decimals: Int get() = when (this) {
        HPA -> 1
        INHG -> 2
    }

    fun next(): Pressure = entries[(ordinal + 1) % entries.size]
}

/**
 * A latitude or longitude, as degrees, minutes and seconds.
 *
 * Decimal degrees would be shorter, but a coordinate is read off this screen to be said
 * out loud or written on paper, and nobody reads 51.4778 aloud.
 */
fun degreesMinutesSeconds(degrees: Double, positive: String, negative: String): String {
    val axis = if (degrees < 0) negative else positive
    val magnitude = if (degrees < 0) -degrees else degrees

    // Reduced to whole tenths of a second first, then split up.
    //
    // Subtracting and multiplying down the units instead - degrees, then minutes, then
    // seconds - truncates the rounding error of every step into the step below it, and
    // the error always runs the same way. A coordinate exactly 15 seconds past the minute
    // arrives as 14.999999999998 and prints as 14.9, one tenth short, every time. Doing
    // the rounding once at the bottom and carrying upwards cannot drift.
    var total = Math.round(magnitude * 3600.0 * 10.0)

    val tenths = total % 10
    total /= 10
    val seconds = total % 60
    total /= 60
    val minutes = total % 60
    val whole = total / 60

    return "$axis $whole° $minutes′ $seconds.$tenths″"
}
