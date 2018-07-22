package com.jarsilio.android.drowser.prefs

import android.content.Context
import android.preference.PreferenceManager

class Prefs(context: Context) {
    val DROWSE_CANDIDATES_PACKAGES = "drowse_candidates_packages"
    val IS_ENABLED = "is_enabled"

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var isEnabled: Boolean
        get() = prefs.getBoolean(IS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(IS_ENABLED, value).apply()

    var drowseCandidates: String
        get() = prefs.getString(DROWSE_CANDIDATES_PACKAGES, "")
        set(value) = prefs.edit().putString(DROWSE_CANDIDATES_PACKAGES, value).apply()
}