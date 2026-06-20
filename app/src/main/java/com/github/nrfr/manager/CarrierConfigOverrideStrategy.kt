package com.github.nrfr.manager

object CarrierConfigOverrideStrategy {
    private const val ANDROID_16_SDK = 36
    private const val FIRST_BLOCKING_PATCH_YEAR_MONTH = 202510

    fun shouldUseBroker(sdkInt: Int, securityPatch: String?): Boolean {
        if (sdkInt >= ANDROID_16_SDK) {
            return true
        }

        val yearMonth = parsePatchYearMonth(securityPatch) ?: return false
        return yearMonth >= FIRST_BLOCKING_PATCH_YEAR_MONTH
    }

    private fun parsePatchYearMonth(securityPatch: String?): Int? {
        if (securityPatch == null || securityPatch.length < 7) {
            return null
        }

        val year = securityPatch.substring(0, 4).toIntOrNull() ?: return null
        val month = securityPatch.substring(5, 7).toIntOrNull() ?: return null
        if (securityPatch[4] != '-' || month !in 1..12) {
            return null
        }

        return year * 100 + month
    }
}
