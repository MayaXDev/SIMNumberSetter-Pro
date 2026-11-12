package com.mayaxdev.simnumbersetter.repositories

import android.content.Context

interface PermissionRepository {
    suspend fun grantDumpPermission(): Boolean
}

class PermissionRepositoryImpl(
    private val context: Context,
    private val rootRepository: RootRepository
): PermissionRepository {
    override suspend fun grantDumpPermission(): Boolean {
        // TelephonyDebugService onBind is intercepted directly by Xposed hook,
        // no DUMP permission required on modern Android versions.
        return true
    }
}
