package com.jarsilio.android.drowser.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import com.jarsilio.android.drowser.MainActivity
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.prefs.Prefs
import timber.log.Timber

class DrowserService : Service() {
    private val screenReceiver: ScreenReceiver = ScreenReceiver()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun start() {
        registerScreenReceiver()
    }

    private fun stop() {
        unregisterReceiver(screenReceiver)
    }

    override fun onDestroy() {
        stop()
    }

    override fun onCreate() {
        super.onCreate()
        start()
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (shouldStartForegroundService(this)) {
            startForegroundService()
        }

        return Service.START_STICKY
    }

    private fun startForegroundService() {
        Timber.d("Starting ForegroundService")

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, "persistent")
                .setContentText(getString(R.string.notification_tap_to_open))
                .setShowWhen(false)
                .setContentIntent(notificationPendingIntent)
                .setColor(resources.getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.drowser_notification_icon_white)
                .setOngoing(true)
                .setContentTitle(getString(R.string.notification_drowser_running))
                .setTicker(getString(R.string.notification_drowser_running))

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("persistent", getString(R.string.notification_persistent), NotificationManager.IMPORTANCE_NONE)
            notificationChannel.description = getString(R.string.notification_persistent_channel_description)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        startForeground(FOREGROUND_ID, notificationBuilder.build())
    }

    companion object {
        private const val FOREGROUND_ID = 10001

        const val BATTERY_OPTIMIZATION_REQUEST_CODE = 20002

        fun startService(context: Context) {
            val prefs = Prefs.getInstance(context)
            if (prefs.isEnabled) {
                Timber.i("Starting Drowser")
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    shouldStartForegroundService(context)) {
                    Timber.d("Starting service with context.startForegroundService (Android >= Oreo and battery optimization on)")
                    context.startForegroundService(Intent(context, DrowserService::class.java))
                } else {
                    Timber.d("Starting service with context.startService (Android < Oreo or battery optimization off)")
                    context.startService(Intent(context, DrowserService::class.java))
                }
            } else {
                Timber.i("Not starting Drowser because it's disabled")
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, DrowserService::class.java))
        }

        fun restartService(context: Context) {
            Timber.i("Restarting Drowser")
            stopService(context)
            startService(context)
        }

        fun isIgnoringBatteryOptimizations(context: Context): Boolean {
            val isIgnoringBatteryOptimizations: Boolean
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                isIgnoringBatteryOptimizations = true
            }
            return isIgnoringBatteryOptimizations
        }

        private fun shouldStartForegroundService(context: Context): Boolean {
            return !isIgnoringBatteryOptimizations(context) || Prefs.getInstance(context).showNotification
        }
    }
}
