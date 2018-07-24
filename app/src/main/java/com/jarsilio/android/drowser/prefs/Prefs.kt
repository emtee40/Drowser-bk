package com.jarsilio.android.drowser.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.jarsilio.android.drowser.R

class Prefs(context: Context) {
    val DROWSE_CANDIDATES_PACKAGES = context.getString(R.string.pref_drowse_candidates_key)
    val IS_ENABLED = context.getString(R.string.pref_enabled_key)
    val SHOW_NOTIFICATION = context.getString(R.string.pref_notification_key)

    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var isEnabled: Boolean
        get() = prefs.getBoolean(IS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(IS_ENABLED, value).apply()

    var drowseCandidates: String
        get() = prefs.getString(DROWSE_CANDIDATES_PACKAGES, "")
        set(value) = prefs.edit().putString(DROWSE_CANDIDATES_PACKAGES, value).apply()

    var showNotification: Boolean
        get() = prefs.getBoolean(SHOW_NOTIFICATION, true)
        set(value) = prefs.edit().putBoolean(SHOW_NOTIFICATION, value).apply()
}