package com.jarsilio.android.drowser.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jarsilio.android.drowser.prefs.Prefs
import timber.log.Timber

class AutoStart : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = Prefs(context)
        if (prefs.isEnabled) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                Timber.d("Received ACTION_BOOT_COMPLETED.")
                DrowserService.startService(context)
            } else if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                Timber.d("Received ACTION_MY_PACKAGE_REPLACED.")
                DrowserService.startService(context)
            }
        }
    }
}
