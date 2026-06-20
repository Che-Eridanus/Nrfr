package com.github.nrfr.manager

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.app.Instrumentation
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.os.Process
import android.system.Os
import android.telephony.CarrierConfigManager
import android.util.Log
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class CarrierConfigBrokerInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)

        val result = Bundle()
        try {
            requireNotNull(arguments) { "Missing carrier config arguments" }
            val request = CarrierConfigBrokerRequest.from(arguments)

            if (Process.isSdkSandbox()) {
                applyConfigWithUiAutomation(request.subId, request.overrides)
            } else {
                applyConfigWithShellDelegate(request.subId, request.overrides)
            }
            finish(0, result)
        } catch (e: Exception) {
            Log.e(TAG, "Carrier config broker failed", e)
            result.putString("error", e.message ?: e.javaClass.name)
            finish(1, result)
        }
    }

    @SuppressLint("MissingPermission")
    private fun applyConfigWithUiAutomation(subId: Int, overrides: PersistableBundle?) {
        val automation = uiAutomation ?: error("Missing UiAutomation connection")
        automation.adoptShellPermissionIdentity()
        try {
            applyConfig(subId, overrides)
        } finally {
            automation.dropShellPermissionIdentity()
        }
    }

    @SuppressLint("MissingPermission")
    private fun applyConfigWithShellDelegate(subId: Int, overrides: PersistableBundle?) {
        val activityManager = IActivityManager.Stub.asInterface(
            ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.ACTIVITY_SERVICE))
        )
        activityManager.startDelegateShellPermissionIdentity(Os.getuid(), null)
        try {
            applyConfig(subId, overrides)
        } finally {
            activityManager.stopDelegateShellPermissionIdentity()
        }
    }

    @SuppressLint("MissingPermission")
    private fun applyConfig(subId: Int, overrides: PersistableBundle?) {
        val carrierConfigManager = context.getSystemService(CarrierConfigManager::class.java)
        carrierConfigManager.overrideConfig(subId, overrides, true)
    }

    private companion object {
        const val TAG = "NrfrCarrierConfig"
    }
}
