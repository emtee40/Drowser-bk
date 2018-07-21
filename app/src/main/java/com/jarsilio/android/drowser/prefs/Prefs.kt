package com.jarsilio.android.drowser.prefs

import android.content.Context
import android.preference.PreferenceManager

class Prefs(context: Context) {
    val DROWSE_CANDIDATES_PACKAGES = "drowse_candidates_packages"
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var drowseCandidates: String
        get() = prefs.getString(DROWSE_CANDIDATES_PACKAGES, "")
        set(value) = prefs.edit().putString(DROWSE_CANDIDATES_PACKAGES, value).apply()
}