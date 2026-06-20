package com.github.nrfr.manager

data class CarrierConfigBrokerOutcome(
    val success: Boolean,
    val message: String,
    val verificationError: Throwable? = null
)

object CarrierConfigBrokerApplyFlow {
    fun run(
        applyConfig: () -> Unit,
        verifyConfig: () -> Unit
    ): CarrierConfigBrokerOutcome {
        val applyError = runCatching {
            applyConfig()
        }.exceptionOrNull()

        if (applyError != null) {
            return CarrierConfigBrokerOutcome(
                success = false,
                message = CarrierConfigBrokerMessages.brokerResult(false, applyError.message)
            )
        }

        val verificationError = runCatching {
            verifyConfig()
        }.exceptionOrNull()

        return CarrierConfigBrokerOutcome(
            success = true,
            message = CarrierConfigBrokerMessages.brokerResult(true),
            verificationError = verificationError
        )
    }
}
