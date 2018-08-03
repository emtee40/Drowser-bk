package com.jarsilio.android.drowser.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.jarsilio.android.drowser.R

class Prefs(context: Context) {
    val IS_ENABLED = context.getString(R.string.pref_enabled_key)
    val SHOW_NOTIFICATION = context.getString(R.string.pref_notification_key)

    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var isEnabled: Boolean
        get() = prefs.getBoolean(IS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(IS_ENABLED, value).apply()

    var showNotification: Boolean
        get() = prefs.getBoolean(SHOW_NOTIFICATION, true)
        set(value) = prefs.edit().putBoolean(SHOW_NOTIFICATION, value).apply()
}