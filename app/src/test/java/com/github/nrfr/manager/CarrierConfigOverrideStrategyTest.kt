package com.github.nrfr.manager

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrierConfigOverrideStrategyTest {
    @Test
    fun usesBrokerForAndroid16AndNewer() {
        assertTrue(CarrierConfigOverrideStrategy.shouldUseBroker(36, "2025-06-01"))
        assertTrue(CarrierConfigOverrideStrategy.shouldUseBroker(37, "2026-06-01"))
    }

    @Test
    fun usesBrokerForOctober2025SecurityPatchAndNewer() {
        assertFalse(CarrierConfigOverrideStrategy.shouldUseBroker(35, "2025-09-05"))
        assertTrue(CarrierConfigOverrideStrategy.shouldUseBroker(35, "2025-10-01"))
        assertTrue(CarrierConfigOverrideStrategy.shouldUseBroker(35, "2026-01-01"))
    }

    @Test
    fun keepsDirectPathForOlderOrUnknownPatchLevelsBelowAndroid16() {
        assertFalse(CarrierConfigOverrideStrategy.shouldUseBroker(35, "2025"))
        assertFalse(CarrierConfigOverrideStrategy.shouldUseBroker(35, ""))
        assertFalse(CarrierConfigOverrideStrategy.shouldUseBroker(35, null))
    }
}
