package com.jarsilio.android.drowser

import android.os.Bundle
import android.preference.PreferenceFragment
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.services.DrowserService
import com.jarsilio.android.drowser.services.DrowserService.Companion.BATTERY_OPTIMIZATION_REQUEST_CODE
import timber.log.Timber

class PreferencesActivity : Activity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs.getInstance(this)
        prefs.prefs.registerOnSharedPreferenceChangeListener(this)
        // Display the fragment as the main content.
        fragmentManager.beginTransaction().replace(android.R.id.content, PrefsFragment()).commit()
    }

    override fun onResume() {
        super.onResume()
        prefs.prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.d("Changed preference: $key")
        when (key) {
            prefs.IS_ENABLED -> {
                if (prefs.isEnabled) {
                    DrowserService.startService(this)
                } else {
                    DrowserService.stopService(this)
                }
            }
            prefs.SHOW_NOTIFICATION -> {
                if (!prefs.showNotification && !DrowserService.isIgnoringBatteryOptimizations(this)) {
                    Timber.d("Requesting to ignore battery optimizations")
                    startActivityForResult(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:$packageName")), BATTERY_OPTIMIZATION_REQUEST_CODE)
                }
                DrowserService.restartService(this)
            }
        }
    }

    class PrefsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences)
        }
    }
}