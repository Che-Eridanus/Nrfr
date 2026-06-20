package com.github.nrfr.manager

object CarrierConfigBrokerContract {
    const val USER_SERVICE_TAG = "carrier_config_broker"
    const val USER_SERVICE_VERSION = 1
    const val USER_SERVICE_TRANSACTION_APPLY_CONFIG = 1
    const val ARG_SUB_ID = "com.github.nrfr.extra.SUB_ID"
    const val ARG_CLEAR = "com.github.nrfr.extra.CLEAR"
    const val ARG_OVERRIDES = "com.github.nrfr.extra.OVERRIDES"
    const val ARG_PACKAGE_NAME = "com.github.nrfr.extra.PACKAGE_NAME"
    const val EXTRA_BROKER_RESULT = "com.github.nrfr.extra.BROKER_RESULT"
    const val EXTRA_BROKER_MESSAGE = "com.github.nrfr.extra.BROKER_MESSAGE"
    const val RESULT_SUCCESS = "success"
    const val RESULT_FAILURE = "failure"
}
