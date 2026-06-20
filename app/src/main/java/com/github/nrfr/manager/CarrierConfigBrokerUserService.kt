package com.github.nrfr.manager

import android.app.IActivityManager
import android.app.IInstrumentationWatcher
import android.app.UiAutomationConnection
import android.content.ComponentName
import android.content.Context
import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import android.os.PersistableBundle
import android.telephony.TelephonyFrameworkInitializer
import android.util.Log
import com.android.internal.telephony.ICarrierConfigLoader
import rikka.shizuku.SystemServiceHelper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class CarrierConfigBrokerUserService() : Binder() {
    constructor(@Suppress("UNUSED_PARAMETER") context: Context) : this()

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (code != CarrierConfigBrokerContract.USER_SERVICE_TRANSACTION_APPLY_CONFIG) {
            return super.onTransact(code, data, reply, flags)
        }

        val arguments = data.readBundle(javaClass.classLoader)
            ?: error("Missing carrier config broker arguments")
        thread(name = "NrfrCarrierConfigBroker") {
            runBroker(arguments)
        }
        reply?.writeNoException()
        return true
    }

    private fun runBroker(arguments: Bundle) {
        val packageName = arguments.getString(CarrierConfigBrokerContract.ARG_PACKAGE_NAME)
            ?: DEFAULT_PACKAGE_NAME
        val outcome = runCatching {
            // Give the foreground app a short moment to show its "saving" toast before
            // Android stops the package to start SDK sandbox instrumentation.
            Thread.sleep(800)
            val request = CarrierConfigBrokerRequest.from(arguments)
            runSandboxInstrumentation(packageName, arguments)
            awaitOverrideVisible(packageName, request.subId, request.overrides)
        }

        val success = outcome.isSuccess
        val message = outcome.exceptionOrNull()?.message
            ?: if (success) "设置已保存" else "保存失败"
        relaunchApp(packageName, success, message)
    }

    private fun runSandboxInstrumentation(packageName: String, arguments: Bundle) {
        val activityManager = IActivityManager.Stub.asInterface(
            SystemServiceHelper.getSystemService(Context.ACTIVITY_SERVICE)
        )
        val watcher = InstrumentationResultWatcher()
        val started = activityManager.startInstrumentation(
            ComponentName(packageName, CarrierConfigBrokerInstrumentation::class.java.name),
            null,
            CarrierConfigBrokerLauncher.instrumentationFlags(useSdkSandbox = true),
            arguments,
            watcher,
            UiAutomationConnection(),
            0,
            null
        )
        check(started) { "无法启动 Android 16 兼容写入代理" }
        watcher.awaitResult()
    }

    private fun awaitOverrideVisible(packageName: String, subId: Int, expected: PersistableBundle?) {
        if (expected == null || expected.isEmpty) return

        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        do {
            val config = getConfigBundleForSubId(packageName, subId)
            if (config != null && expected.keySet().all { key -> config.get(key) == expected.get(key) }) {
                return
            }
            Thread.sleep(100)
        } while (System.nanoTime() < deadline)

        error("系统未返回新的运营商配置")
    }

    private fun getConfigBundleForSubId(packageName: String, subId: Int): PersistableBundle? {
        return ICarrierConfigLoader.Stub.asInterface(
            TelephonyFrameworkInitializer
                .getTelephonyServiceManager()
                .carrierConfigServiceRegisterer
                .get()
        ).getConfigForSubId(subId, packageName)
    }

    private fun relaunchApp(packageName: String, success: Boolean, message: String) {
        val result = if (success) {
            CarrierConfigBrokerContract.RESULT_SUCCESS
        } else {
            CarrierConfigBrokerContract.RESULT_FAILURE
        }
        runCatching {
            val process = ProcessBuilder(
                "/system/bin/am",
                "start",
                "-n",
                "$packageName/.MainActivity",
                "--activity-clear-top",
                "--es",
                CarrierConfigBrokerContract.EXTRA_BROKER_RESULT,
                result,
                "--es",
                CarrierConfigBrokerContract.EXTRA_BROKER_MESSAGE,
                message
            ).redirectErrorStream(true).start()
            process.waitFor(5, TimeUnit.SECONDS)
        }.onFailure {
            Log.e(TAG, "Failed to relaunch app after carrier config broker", it)
        }
    }

    private class InstrumentationResultWatcher : IInstrumentationWatcher.Stub() {
        private val finished = CountDownLatch(1)

        @Volatile
        private var resultCode: Int = 0

        @Volatile
        private var result: Bundle? = null

        override fun instrumentationStatus(className: ComponentName?, resultCode: Int, results: Bundle?) = Unit

        override fun instrumentationFinished(className: ComponentName?, resultCode: Int, results: Bundle?) {
            this.resultCode = resultCode
            result = results
            finished.countDown()
        }

        fun awaitResult() {
            check(finished.await(BROKER_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                "Android 16 兼容写入代理执行超时"
            }
            check(resultCode == 0) {
                result?.getString("error") ?: "Android 16 兼容写入代理执行失败"
            }
        }
    }

    private companion object {
        const val TAG = "NrfrCarrierConfig"
        const val BROKER_TIMEOUT_SECONDS = 15L
        const val DEFAULT_PACKAGE_NAME = "com.github.nrfr"
    }
}
