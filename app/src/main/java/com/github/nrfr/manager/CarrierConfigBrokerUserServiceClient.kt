package com.github.nrfr.manager

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import rikka.shizuku.Shizuku

object CarrierConfigBrokerUserServiceClient {
    private const val CONNECTION_TIMEOUT_SECONDS = 5L

    fun start(context: Context, arguments: Bundle) {
        val serviceArgs = Shizuku.UserServiceArgs(
            ComponentName(context, CarrierConfigBrokerUserService::class.java)
        )
            .daemon(false)
            .processNameSuffix(CarrierConfigBrokerContract.USER_SERVICE_TAG)
            .tag(CarrierConfigBrokerContract.USER_SERVICE_TAG)
            .version(CarrierConfigBrokerContract.USER_SERVICE_VERSION)
            .debuggable(false)

        val connected = CountDownLatch(1)
        val service = AtomicReference<IBinder?>()
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service.set(binder)
                connected.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        Shizuku.bindUserService(serviceArgs, connection)
        check(connected.await(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            "无法连接 Shizuku 写入服务"
        }
        val binder = service.get() ?: error("Shizuku 写入服务未返回 Binder")

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeBundle(arguments)
            binder.transact(
                CarrierConfigBrokerContract.USER_SERVICE_TRANSACTION_APPLY_CONFIG,
                data,
                reply,
                0
            )
            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()
            Shizuku.unbindUserService(serviceArgs, connection, false)
        }

        throw CarrierConfigBrokerRestartingException()
    }
}

class CarrierConfigBrokerRestartingException : IllegalStateException(
    "正在通过 Android 16 兼容模式保存，完成后会自动回到应用"
)
