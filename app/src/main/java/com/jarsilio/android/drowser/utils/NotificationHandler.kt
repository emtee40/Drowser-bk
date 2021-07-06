package com.jarsilio.android.drowser.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jarsilio.android.common.extensions.isOreoOrNewer
import com.jarsilio.android.drowser.R
import timber.log.Timber

const val NOTIFICATION_CHANNEL_ID = "no_root_notification_channel_id"
const val NOTIFICATION_ID = 43

object NotificationHandler {

    fun showNoRootNotification(context: Context) {
        @TargetApi(Build.VERSION_CODES.O)
        if (isOreoOrNewer) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.root_required_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = context.getString(R.string.root_required_notification_channel_description)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle(context.getString(R.string.root_required_notification_title))
            setContentText(context.getString(R.string.root_required_notification_description))
            setSmallIcon(R.drawable.drowser_notification_icon_white)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

        with(NotificationManagerCompat.from(context)) {
            Timber.d("Showing missing root notification")
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    fun dismissNoRootNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            Timber.d("Dismissing missing root notification (if it was shown)")
            cancel(NOTIFICATION_ID)
        }
    }
}
