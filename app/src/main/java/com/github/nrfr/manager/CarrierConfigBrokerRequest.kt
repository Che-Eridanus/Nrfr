package com.github.nrfr.manager

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.telephony.SubscriptionManager

data class CarrierConfigBrokerRequest(
    val subId: Int,
    val overrides: PersistableBundle?
) {
    companion object {
        fun from(arguments: Bundle): CarrierConfigBrokerRequest {
            val subId = arguments.getInt(
                CarrierConfigBrokerContract.ARG_SUB_ID,
                SubscriptionManager.INVALID_SUBSCRIPTION_ID
            )
            require(SubscriptionManager.isValidSubscriptionId(subId)) {
                "Invalid subscription id: $subId"
            }

            val overrides = if (arguments.getBoolean(CarrierConfigBrokerContract.ARG_CLEAR)) {
                null
            } else {
                arguments.getPersistableBundleCompat(CarrierConfigBrokerContract.ARG_OVERRIDES)
                    ?: PersistableBundle()
            }

            return CarrierConfigBrokerRequest(subId, overrides)
        }

        @Suppress("DEPRECATION")
        private fun Bundle.getPersistableBundleCompat(key: String): PersistableBundle? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelable(key, PersistableBundle::class.java)
            } else {
                getParcelable(key)
            }
        }
    }
}
