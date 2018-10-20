package com.jarsilio.android.drowser.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.utils.Utils

import timber.log.Timber

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            val prefs = Prefs(context)

            Timber.d("Screen off")

            if (prefs.disableUntil < System.currentTimeMillis()) {
                Timber.d("Drowsing (force-stopping) candidate apps.")
                AppsManager(context).forceStopApps()
            } else {
                Timber.d("Drowser temporarily disabled (until ${Utils.getReadableDate(prefs.disableUntil)}), not drowsing apps.")
            }
        }
    }
}