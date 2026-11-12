package com.mayaxdev.simnumbersetter.repositories

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.mayaxdev.simnumbersetter.IPhoneNumberSetter
import com.mayaxdev.simnumbersetter.utils.extensions.applySecurity
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

interface ServiceRepository {

    suspend fun <T> runWithService(block: suspend (IPhoneNumberSetter) -> T): T?
    fun <T> runWithServiceIfAvailable(block: (IPhoneNumberSetter) -> T?): T?
    fun unbindServiceIfNeeded()

}

class ServiceRepositoryImpl(private val context: Context): ServiceRepository {

    companion object {
        // Fast 1.5s timeout: if Xposed hook is not intercepting onBind, failover cleanly to direct root
        private const val SERVICE_TIMEOUT_MS = 1500L
    }

    private var serviceInstance: IPhoneNumberSetter? = null
    private var serviceConnection: ServiceConnection? = null
    private val getServiceMutex = Mutex()

    private val serviceIntent = Intent("com.android.phone.TelephonyDebugService").apply {
        `package` = "com.android.phone"
        applySecurity(context)
    }

    override suspend fun <T> runWithService(block: suspend (IPhoneNumberSetter) -> T): T? {
        val service = withContext(Dispatchers.IO) {
            getServiceLocked()
        } ?: return null
        return block(service)
    }

    override fun <T> runWithServiceIfAvailable(block: (IPhoneNumberSetter) -> T?): T? {
        return serviceInstance?.let {
            block(it)
        }
    }

    override fun unbindServiceIfNeeded() {
        serviceConnection?.let {
            try {
                context.unbindService(it)
            }catch (e: IllegalArgumentException){
                //Already unregistered
            }
        }
    }

    private suspend fun getServiceLocked(): IPhoneNumberSetter? {
        return withTimeoutOrNull(SERVICE_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                CoroutineScope(Dispatchers.IO).launch {
                    getServiceMutex.lock()
                    serviceInstance?.let {
                        if (continuation.isActive) continuation.resume(it)
                        getServiceMutex.unlock()
                        return@launch
                    }

                    var isBound = false
                    val conn = object : ServiceConnection {
                        override fun onServiceConnected(component: ComponentName, binder: IBinder) {
                            serviceInstance = IPhoneNumberSetter.Stub.asInterface(binder)
                            binder.pingBinder()
                            serviceConnection = this
                            if (continuation.isActive) {
                                continuation.resume(serviceInstance!!)
                            }
                            getServiceMutex.unlock()
                        }

                        override fun onServiceDisconnected(component: ComponentName) {
                            serviceInstance = null
                            serviceConnection = null
                        }

                        override fun onNullBinding(name: ComponentName?) {
                            // In Android 13 without active Xposed hook, onBind returns null
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                            try {
                                context.unbindService(this)
                            } catch (e: Exception) {}
                            getServiceMutex.unlock()
                        }
                    }

                    continuation.invokeOnCancellation {
                        try {
                            if (isBound) context.unbindService(conn)
                        } catch (e: Exception) {}
                        if (getServiceMutex.isLocked) {
                            getServiceMutex.unlock()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        try {
                            isBound = context.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE)
                            if (!isBound) {
                                if (continuation.isActive) continuation.resume(null)
                                getServiceMutex.unlock()
                            }
                        } catch (t: Throwable) {
                            if (continuation.isActive) continuation.resume(null)
                            getServiceMutex.unlock()
                        }
                    }
                }
            }
        }
    }

}
