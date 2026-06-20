package com.github.nrfr.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CarrierConfigWriteDispatcher {
    suspend fun <T> run(block: () -> T): T = withContext(Dispatchers.IO) {
        block()
    }
}
