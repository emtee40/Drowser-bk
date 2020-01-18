package com.jarsilio.android.drowser.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.jarsilio.android.drowser.models.AppsManager
import timber.log.Timber
import androidx.core.app.JobIntentService

object Scheduler {
    fun scheduleAlarm(context: Context, time: Long) {
        Timber.d("Canceling previously set alarm")
        cancelAlarm(context)

        Timber.d("Scheduling alarm to drowse apps after pause")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val alarmPendingIntent = getPendingIntent(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager?.set(AlarmManager.RTC_WAKEUP, time, alarmPendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, alarmPendingIntent)
        } else { // between KITKAT and M
            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, time, alarmPendingIntent)
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.cancel(getPendingIntent(context))
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Pause timeout finished. Drowsing apps with JobIntentService")
        AppDrowserJobIntentService.enqueueWork(context, intent)
    }
}

class AppDrowserJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        if (isScreenOn()) {
            Timber.d("Not drowsing apps because screen is on. Letting the natural drowsing course take place (i.e. drowse when the screen goes off)")
        } else {
            AppsManager(this).forceStopApps()
        }
        Scheduler.cancelAlarm(this)
        stopSelf()
    }

    private fun isScreenOn(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager?
        if (powerManager == null) {
            return true // Assume screen is on to not kill any apps the user might be using (this could be impolite, but I prefer to make sure the apps are drowsed)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager.isInteractive
        } else {
            powerManager.isScreenOn
        }
    }

    companion object {
        private val JOB_ID = 1000

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, AppDrowserJobIntentService::class.java, JOB_ID, intent)
        }
    }
}