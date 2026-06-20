package com.github.nrfr.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrierConfigBrokerApplyFlowTest {
    @Test
    fun verificationFailureAfterApplyStillReportsSuccess() {
        val outcome = CarrierConfigBrokerApplyFlow.run(
            applyConfig = {},
            verifyConfig = {
                error("Attempt to invoke virtual method 'android.os.TelephonyServiceManager.getCarrierConfigServiceRegisterer()'")
            }
        )

        assertTrue(outcome.success)
        assertEquals("设置已保存", outcome.message)
    }
}
