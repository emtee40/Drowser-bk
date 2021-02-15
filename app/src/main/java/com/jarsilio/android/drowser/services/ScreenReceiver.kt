package com.jarsilio.android.drowser.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.utils.Utils
import timber.log.Timber

class ScreenReceiver : BroadcastReceiver() {
    var drowserThread: Thread? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            val prefs = Prefs.getInstance(context)

            Timber.d("Screen off")

            if (prefs.disableUntil < System.currentTimeMillis()) {
                Timber.d("Drowsing (force-stopping) candidate apps.")

                drowserThread = Thread {
                    if (prefs.drowseDelay > 0) {
                        Timber.d("Going to sleep for ${prefs.drowseDelay / 1000} seconds before drowsing apps...")
                        try {
                            Thread.sleep(prefs.drowseDelay)
                        } catch (e: InterruptedException) {
                            Timber.d("drowserThread (Thread.sleep) interrupted. Hopefully because we interrupted it ourselves.")
                            return@Thread
                        }
                    }
                    AppsManager(context).forceStopApps()
                }
                drowserThread?.start()
            } else {
                Timber.d("Drowser temporarily disabled (until ${Utils.getReadableDate(prefs.disableUntil)}), not drowsing apps.")
            }
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            Timber.d("Screen on")

            if (drowserThread != null) {
                Timber.d("Canceling app drowsing (interrupting drowser thread)")
            }

            drowserThread?.interrupt()
            drowserThread = null
        }
    }
}
