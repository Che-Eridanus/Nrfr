package com.github.nrfr.manager

import android.app.ActivityManager

object CarrierConfigBrokerLauncher {
    fun instrumentationFlags(useSdkSandbox: Boolean): Int =
        if (useSdkSandbox) {
            ActivityManager.INSTR_FLAG_DISABLE_HIDDEN_API_CHECKS or
                ActivityManager.INSTR_FLAG_INSTRUMENT_SDK_SANDBOX
        } else {
            ActivityManager.INSTR_FLAG_DISABLE_HIDDEN_API_CHECKS or
                ActivityManager.INSTR_FLAG_NO_RESTART
        }
}
