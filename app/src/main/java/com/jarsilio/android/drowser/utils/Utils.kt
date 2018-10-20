package com.jarsilio.android.drowser.utils

import java.text.SimpleDateFormat
import java.util.Calendar

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
}