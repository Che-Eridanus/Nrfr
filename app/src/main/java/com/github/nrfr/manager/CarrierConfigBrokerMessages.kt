package com.github.nrfr.manager

object CarrierConfigBrokerMessages {
    const val SAVE_SUCCESS = "设置已保存"
    const val SAVE_FAILURE = "保存失败，请检查 Shizuku 权限或重试"

    fun brokerResult(success: Boolean, detail: String? = null): String {
        return if (success) SAVE_SUCCESS else failure(detail)
    }

    fun toast(result: String, detail: String?): String {
        return when (result) {
            CarrierConfigBrokerContract.RESULT_SUCCESS -> SAVE_SUCCESS
            CarrierConfigBrokerContract.RESULT_FAILURE -> failure(detail)
            else -> SAVE_FAILURE
        }
    }

    private fun failure(detail: String?): String {
        val message = detail?.trim().orEmpty()
        if (message.isEmpty() || !message.hasChineseText()) {
            return SAVE_FAILURE
        }
        return if (message.startsWith("保存失败")) message else "保存失败: $message"
    }

    private fun String.hasChineseText(): Boolean {
        return any { it in '\u4E00'..'\u9FFF' }
    }
}
