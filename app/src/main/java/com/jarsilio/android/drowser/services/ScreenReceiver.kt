package com.jarsilio.android.drowser.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jarsilio.android.drowser.models.AppsManager

import timber.log.Timber

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            Timber.d("Screen off. Drowsing (force-stopping) candidate apps")
            AppsManager(context).forceStopApps()
        }
    }
}