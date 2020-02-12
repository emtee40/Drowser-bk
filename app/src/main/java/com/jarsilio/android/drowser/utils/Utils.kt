package com.jarsilio.android.drowser.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

object Utils {
    fun getReadableDate(epochMilliseconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = epochMilliseconds
        return SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(calendar.time)
    }

    fun getReadableTime(epochMilliseconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = epochMilliseconds
        return SimpleDateFormat("HH:mm:ss").format(calendar.time)
    }

    fun getTimeoutUntil(epochMilliseconds: Long): String {
        val timeoutInMilliseconds = epochMilliseconds - System.currentTimeMillis()
        return String.format("%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds),
            TimeUnit.MILLISECONDS.toSeconds(timeoutInMilliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds))
        )
    }
}
