package com.wanderwildwood.sokudokei.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationRefusalTest {
    private fun blocked(
        granted: Boolean = false,
        after: Boolean = false,
        before: Boolean = false,
        thisRun: Boolean = false,
        stored: Boolean = false,
    ) = LocationRefusal.blocked(granted, after, before, thisRun, stored)

    @Test fun aDismissedFirstDialogIsNotBlocked() {
        assertFalse(blocked())
    }

    @Test fun aSecondNoAfterTheRationaleIsBlocked() {
        assertTrue(blocked(before = true))
    }

    @Test fun aFreshLaunchAfterAStoredRefusalIsBlockedOnTheFirstTap() {
        assertTrue(blocked(stored = true))
    }

    @Test fun aRationaleStillShowingIsNeverBlocked() {
        assertFalse(blocked(after = true, before = true, thisRun = true, stored = true))
    }

    @Test fun aYesIsNeverBlocked() {
        assertFalse(blocked(granted = true, before = true, thisRun = true, stored = true))
    }

    @Test fun aYesClearsTheStoredFlag() {
        assertFalse(LocationRefusal.stored(granted = true, rationaleBefore = true, refusedStored = true))
    }

    @Test fun aNoAfterTheRationaleSetsIt() {
        assertTrue(LocationRefusal.stored(granted = false, rationaleBefore = true, refusedStored = false))
    }

    @Test fun aNoWithoutTheRationaleLeavesItAsItWas() {
        assertFalse(LocationRefusal.stored(granted = false, rationaleBefore = false, refusedStored = false))
        assertTrue(LocationRefusal.stored(granted = false, rationaleBefore = false, refusedStored = true))
    }
}
