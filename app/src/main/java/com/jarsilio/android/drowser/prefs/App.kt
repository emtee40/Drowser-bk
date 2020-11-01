package com.jarsilio.android.drowser.prefs

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.jarsilio.android.common.logging.LongTagTree
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(LongTagTree(applicationContext))
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
