package com.jarsilio.android.drowser

import android.os.Bundle
import android.preference.PreferenceFragment
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.services.DrowserService
import com.jarsilio.android.drowser.services.DrowserService.Companion.BATTERY_OPTIMIZATION_REQUEST_CODE
import com.jarsilio.android.drowser.services.DrowserService.Companion.USAGE_ACCESS_REQUEST_CODE
import timber.log.Timber

class PreferencesActivity : Activity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs.getInstance(this)
        // Display the fragment as the main content.
        fragmentManager.beginTransaction().replace(android.R.id.content, PrefsFragment()).commit()
    }

    override fun onResume() {
        super.onResume()
        prefs.preferencesActivity = this
        prefs.prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.preferencesActivity = null // Avoid leak by not keeping a reference to PreferenceActivity in the Prefs singleton
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
                } else {
                    DrowserService.restartService(this)
                }
            }
            prefs.DROWSE_FOREGROUND_APP -> {
                if (!prefs.drowseForegroundApp && !DrowserService.isUsageAccessAllowed(this)) {
                    startActivityForResult(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS), USAGE_ACCESS_REQUEST_CODE)
                }
            }
            prefs.SHOW_SYSTEM_APPS -> AppsManager(this).updateAppItemsVisibility()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BATTERY_OPTIMIZATION_REQUEST_CODE -> {
                if (!DrowserService.isIgnoringBatteryOptimizations(this)) {
                    Timber.d("The user didn't accept the ignoring of the battery optimization. Forcing show_notification to true")
                    prefs.showNotification = true
                } else {
                    DrowserService.restartService(this)
                }
            }
            USAGE_ACCESS_REQUEST_CODE -> {
                if (!prefs.drowseForegroundApp && !DrowserService.isUsageAccessAllowed(this)) {
                    prefs.drowseForegroundApp = true
                }
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