package com.jarsilio.android.drowser

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.services.DrowserService
import timber.log.Timber

class PreferencesActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefs: Prefs

    private lateinit var batteryOptimizationActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var usageAccessActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = Prefs.getInstance(this)

        setContentView(R.layout.activity_settings)
        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
        }

        registerActivityResultLaunchers()
    }

    override fun onResume() {
        super.onResume()
        prefs.prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun registerActivityResultLaunchers() {
        batteryOptimizationActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!DrowserService.isIgnoringBatteryOptimizations(this)) {
                Timber.d("The user didn't accept the ignoring of the battery optimization. Forcing show_notification to true")
                prefs.showNotification = true
            } else {
                DrowserService.restartService(this)
            }
        }

        usageAccessActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!prefs.drowseForegroundApp && !DrowserService.isUsageAccessAllowed(this)) {
                prefs.drowseForegroundApp = true
            }
        }
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
                    batteryOptimizationActivityResultLauncher.launch(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
                } else {
                    DrowserService.restartService(this)
                }
            }
            prefs.DROWSE_FOREGROUND_APP -> {
                if (!prefs.drowseForegroundApp && !DrowserService.isUsageAccessAllowed(this)) {
                    usageAccessActivityResultLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            }
            prefs.SHOW_SYSTEM_APPS -> AppsManager(this).updateAppItemsVisibility()
            prefs.SHOW_DISABLED_APPS -> AppsManager(this).updateAppItemsVisibility()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        val prefs by lazy { Prefs.getInstance(requireContext()) }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }

        override fun onResume() {
            super.onResume()
            prefs.fragment = this
        }
    }
}
