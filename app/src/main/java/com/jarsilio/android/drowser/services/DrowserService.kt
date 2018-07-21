package com.jarsilio.android.drowser.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.jarsilio.android.drowser.MainActivity
import com.jarsilio.android.drowser.R
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
        if (intent != null) {
            Timber.d("onStartCommand called")
            val action = intent.action
            if (action != null) {
                Timber.d("Received Start Foreground Intent: %s", action)
                when (action) {
                    RESUME_ACTION -> {
                        Timber.d("Resuming service")
                        //settings!!.isPaused = false
                    }
                    PAUSE_ACTION -> {
                        Timber.d("Pausing service")
                        //settings!!.isPaused = true
                    }
                    DISABLE_ACTION -> {
                        Timber.d("Disabling (completely stopping) service")
                        //settings!!.isPaused = false
                        //settings!!.isServiceEnabled = false
                        stopSelf()
                    }
                }
            }
        } else {
            Timber.e("onStartCommand called with a null Intent. Probably it was killed by the system and it gave us nothing to work with. Starting (or maybe pausing) anyway")
        }

        if (shouldStartForegroundService(this)) {
            startForegroundService()
        }

        return Service.START_STICKY
    }

    private fun startForegroundService() {
        Timber.d("Starting ForegroundService")
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val resumeIntent = Intent(this, DrowserService::class.java)
        resumeIntent.action = RESUME_ACTION
        val resumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, 0)

        val pauseIntent = Intent(this, DrowserService::class.java)
        pauseIntent.action = PAUSE_ACTION
        val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0)

        val disableIntent = Intent(this, DrowserService::class.java)
        disableIntent.action = DISABLE_ACTION
        val disablePendingIntent = PendingIntent.getService(this, 0, disableIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("persistent", "WaveUp persistent notification", NotificationManager.IMPORTANCE_NONE)
            notificationChannel.description = "This notification is used to keep WaveUp alive in the background. You can switch it off if you wish."
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "persistent")
                .setContentText("Tap to open")
                .setShowWhen(false)
                .setContentIntent(notificationPendingIntent)
                .setColor(resources.getColor(R.color.colorAccent))
                .setOngoing(true)

        if (true) {
            notificationBuilder.setContentTitle("Drowser running")
                    .setTicker("Drowser running")
        } else {
            notificationBuilder.setContentTitle("Drowser not running")
                    .setTicker("Drowser not running")
        }

        // We should only reach this code if it *is* enabled so no need to check that
        if (false) {
            notificationBuilder.addAction(0, "Resume", resumePendingIntent)
        } else {
            notificationBuilder.addAction(0, "Pause", pausePendingIntent)
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder.priority = Notification.PRIORITY_MIN
        }

        notificationBuilder.addAction(0, "Disable", disablePendingIntent)
        startForeground(FOREGROUND_ID, notificationBuilder.build())
    }

    companion object {
        private val FOREGROUND_ID = 1001

        private val MAIN_ACTION = "MAIN_ACTION"
        private val DISABLE_ACTION = "DISABLE_ACTION"
        private val RESUME_ACTION = "RESUME_ACTION"
        private val PAUSE_ACTION = "PAUSE_ACTION"

        fun shouldStartForegroundService(context: Context): Boolean {
            //val settings = Settings.getInstance(context)
            //return !settings!!.isIgnoringBatteryOptimizations || settings.isShowNotification
            return true
        }
    }
}
