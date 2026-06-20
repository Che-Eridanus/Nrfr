package com.github.nrfr.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrierConfigBrokerRelaunchCommandTest {
    @Test
    fun commandDisablesActivityAnimation() {
        val command = CarrierConfigBrokerRelaunchCommand.build(
            packageName = "com.github.nrfr",
            success = true,
            message = "设置已保存"
        )

        assertTrue(command.contains("--activity-no-animation"))
    }

    @Test
    fun restartingMessageExplainsControlledSaveFlow() {
        assertEquals(
            "正在保存，完成后会自动回到应用",
            CarrierConfigBrokerRestartingException().message
        )
    }

    @Test
    fun userServiceVersionChangesWhenBrokerBehaviorChanges() {
        assertEquals(2, CarrierConfigBrokerContract.USER_SERVICE_VERSION)
    }

    @Test
    fun successToastIgnoresRawBrokerDetails() {
        val message = CarrierConfigBrokerMessages.toast(
            CarrierConfigBrokerContract.RESULT_SUCCESS,
            "Attempt to invoke virtual method 'android.os.TelephonyServiceManager.getCarrierConfigServiceRegisterer()'"
        )

        assertEquals("设置已保存", message)
    }

    @Test
    fun failureToastLocalizesRawBrokerDetails() {
        val message = CarrierConfigBrokerMessages.toast(
            CarrierConfigBrokerContract.RESULT_FAILURE,
            "Attempt to invoke virtual method 'android.os.TelephonyServiceManager.getCarrierConfigServiceRegisterer()'"
        )

        assertEquals("保存失败，请检查 Shizuku 权限或重试", message)
    }
}
