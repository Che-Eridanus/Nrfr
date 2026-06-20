package com.github.nrfr.manager

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CarrierConfigWriteDispatcherTest {
    @Test
    fun writeRunsOffCallerThread() = runBlocking {
        val callerThreadId = Thread.currentThread().id
        var writeThreadId = callerThreadId

        CarrierConfigWriteDispatcher.run {
            writeThreadId = Thread.currentThread().id
        }

        assertNotEquals(callerThreadId, writeThreadId)
    }
}
