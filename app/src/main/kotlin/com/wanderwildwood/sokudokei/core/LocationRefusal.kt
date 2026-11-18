package com.wanderwildwood.sokudokei.core

/**
 * Reads a location request's answer: is the permission blocked for good, and should the app
 * remember a refusal across launches?
 *
 * A dialog dismissed without an answer records no refusal, so "no rationale" after a request
 * looks the same as a blocked permission. Blocked is only believed when the rationale was
 * there before this request and has gone, when an earlier request in this run also came back
 * without a yes, or when a refusal after the rationale was stored in an earlier run.
 */
object LocationRefusal {

    /** Whether the screen should say location is turned off for this app. */
    fun blocked(
        granted: Boolean,
        rationaleAfter: Boolean,
        rationaleBefore: Boolean,
        refusedThisRun: Boolean,
        refusedStored: Boolean,
    ): Boolean = !granted && !rationaleAfter && (rationaleBefore || refusedThisRun || refusedStored)

    /**
     * The stored flag after this answer: cleared by a yes, set by a no that came after the
     * rationale was showing, otherwise unchanged.
     */
    fun stored(granted: Boolean, rationaleBefore: Boolean, refusedStored: Boolean): Boolean = when {
        granted -> false
        rationaleBefore -> true
        else -> refusedStored
    }
}
