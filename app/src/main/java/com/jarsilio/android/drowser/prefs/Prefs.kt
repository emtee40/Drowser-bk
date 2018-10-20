package com.jarsilio.android.drowser.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.jarsilio.android.drowser.R

class Prefs(context: Context) {
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /* User prefs */
    val IS_ENABLED: String = context.getString(R.string.pref_enabled_key)
    val SHOW_NOTIFICATION: String = context.getString(R.string.pref_notification_key)

    var isEnabled: Boolean
        get() = prefs.getBoolean(IS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(IS_ENABLED, value).apply()

    var showNotification: Boolean
        get() = prefs.getBoolean(SHOW_NOTIFICATION, true)
        set(value) = prefs.edit().putBoolean(SHOW_NOTIFICATION, value).apply()

    /* Automagic prefs: these are not real prefs. They are just set by the app to remember stuff */
    val REQUEST_ROOT_ACCESS: String = context.getString(R.string.pref_request_root_access_key)

    var requestRootAccess: Boolean
        get() = prefs.getBoolean(REQUEST_ROOT_ACCESS, true)
        set(value) = prefs.edit().putBoolean(REQUEST_ROOT_ACCESS, value).apply()
}