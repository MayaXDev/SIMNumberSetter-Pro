package com.mayaxdev.simnumbersetter.utils.extensions

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mayaxdev.simnumbersetter.BuildConfig

private const val KEY_PENDING_INTENT = "SECURITY_PENDING_INTENT"
private const val PENDING_INTENT_REQUEST_CODE = 1001

fun Intent.applySecurity(context: Context) {
    putExtra(
        KEY_PENDING_INTENT, PendingIntent.getActivity(
        context,
        PENDING_INTENT_REQUEST_CODE,
        Intent(),
        PendingIntent.FLAG_IMMUTABLE
    ))
}

fun Intent.checkSecurity(): Boolean {
    return try {
        setExtrasClassLoader(PendingIntent::class.java.classLoader)
        val pendingIntent = getParcelableExtra<PendingIntent>(KEY_PENDING_INTENT) ?: return false
        pendingIntent.creatorPackage == BuildConfig.APPLICATION_ID
    } catch (t: Throwable) {
        false
    }
}
