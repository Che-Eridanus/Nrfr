package com.github.nrfr.manager

object CarrierConfigBrokerRelaunchCommand {
    fun build(packageName: String, success: Boolean, message: String): List<String> {
        val result = if (success) {
            CarrierConfigBrokerContract.RESULT_SUCCESS
        } else {
            CarrierConfigBrokerContract.RESULT_FAILURE
        }
        return listOf(
            "/system/bin/am",
            "start",
            "--activity-no-animation",
            "-n",
            "$packageName/.MainActivity",
            "--activity-clear-top",
            "--es",
            CarrierConfigBrokerContract.EXTRA_BROKER_RESULT,
            result,
            "--es",
            CarrierConfigBrokerContract.EXTRA_BROKER_MESSAGE,
            message
        )
    }
}
