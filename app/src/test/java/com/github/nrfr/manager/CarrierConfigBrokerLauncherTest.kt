package com.github.nrfr.manager

import android.app.ActivityManager
import org.junit.Assert.assertEquals
import org.junit.Test

class CarrierConfigBrokerLauncherTest {
    @Test
    fun instrumentationFlagsAvoidRestartingTheRunningApp() {
        val expected = ActivityManager.INSTR_FLAG_DISABLE_HIDDEN_API_CHECKS or
            ActivityManager.INSTR_FLAG_NO_RESTART

        assertEquals(expected, CarrierConfigBrokerLauncher.instrumentationFlags(useSdkSandbox = false))
    }

    @Test
    fun instrumentationFlagsCanTargetSdkSandbox() {
        val expected = ActivityManager.INSTR_FLAG_DISABLE_HIDDEN_API_CHECKS or
            ActivityManager.INSTR_FLAG_INSTRUMENT_SDK_SANDBOX

        assertEquals(expected, CarrierConfigBrokerLauncher.instrumentationFlags(useSdkSandbox = true))
    }
}
