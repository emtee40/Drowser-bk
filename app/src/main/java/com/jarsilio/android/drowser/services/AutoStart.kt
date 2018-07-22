package com.jarsilio.android.drowser.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jarsilio.android.drowser.prefs.Prefs
import timber.log.Timber

class AutoStart : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = Prefs(context)
        if (prefs.isEnabled) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                Timber.d("Received ACTION_BOOT_COMPLETED.")
                startService(context)
            } else if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                Timber.d("Received ACTION_MY_PACKAGE_REPLACED.")
                startService(context)
            }
        }
    }

    private fun startService(context: Context) {
        Timber.i("Starting Drowser")
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && DrowserService.shouldStartForegroundService(context)) {
            context.startForegroundService(Intent(context, DrowserService::class.java))
        } else {
            context.startService(Intent(context, DrowserService::class.java))
        }
    }
}
