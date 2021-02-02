package com.jarsilio.android.drowser.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.jarsilio.android.drowser.PreferencesActivity
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.models.SingletonHolder
import com.jarsilio.android.drowser.services.Scheduler

class Prefs private constructor(private val context: Context) {
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var fragment: PreferencesActivity.SettingsFragment? = null

    /* User prefs */
    val IS_ENABLED: String = context.getString(R.string.pref_enabled_key)
    val SHOW_NOTIFICATION: String = context.getString(R.string.pref_notification_key)
    val DROWSE_FOREGROUND_APP: String = context.getString(R.string.pref_drowse_foreground_app_key)
    val DROWSE_DELAY: String = context.getString(R.string.pref_drowse_delay_key)
    val SHOW_SYSTEM_APPS: String = context.getString(R.string.pref_drowse_show_system_apps_key)

    var isEnabled: Boolean
        get() = prefs.getBoolean(IS_ENABLED, true)
        set(value) = setBooleanPreference(IS_ENABLED, value)

    var showNotification: Boolean
        get() = prefs.getBoolean(SHOW_NOTIFICATION, true)
        set(value) = setBooleanPreference(SHOW_NOTIFICATION, value)

    var drowseForegroundApp: Boolean
        get() = prefs.getBoolean(DROWSE_FOREGROUND_APP, true)
        set(value) = setBooleanPreference(DROWSE_FOREGROUND_APP, value)

    var drowseDelay: Long
        get() = prefs.getString(DROWSE_DELAY, "0")!!.toLong()
        set(value) = prefs.edit().putString(DROWSE_DELAY, value.toString()).apply()

    var showSystemApps: Boolean
        get() = prefs.getBoolean(SHOW_SYSTEM_APPS, false)
        set(value) = setBooleanPreference(SHOW_SYSTEM_APPS, value)

    /* Automagic prefs: these are not real prefs. They are just set by the app to remember stuff */
    val REQUEST_ROOT_ACCESS: String = context.getString(R.string.pref_request_root_access_key)
    val DISABLE_UNTIL: String = context.getString(R.string.pref_disable_until_key)
    val LAST_DISABLE_UNTIL_USER_CHOICE: String = context.getString(R.string.pref_last_disable_until_user_choice_key)

    var requestRootAccess: Boolean
        get() = prefs.getBoolean(REQUEST_ROOT_ACCESS, true)
        set(value) = prefs.edit().putBoolean(REQUEST_ROOT_ACCESS, value).apply()

    var disableUntil: Long
        get() = prefs.getLong(DISABLE_UNTIL, 0)
        set(value) {
            prefs.edit().putLong(DISABLE_UNTIL, value).apply()
            Scheduler.scheduleAlarm(context, value)
        }

    var lastDisableUntilUserChoice: Int
        get() = prefs.getInt(LAST_DISABLE_UNTIL_USER_CHOICE, 2)
        set(value) = prefs.edit().putInt(LAST_DISABLE_UNTIL_USER_CHOICE, value).apply()

    private fun setBooleanPreference(key: String, value: Boolean) {
        // This changes the GUI, but it needs the PreferencesActivity to have started
        val preference = fragment?.findPreference<Preference>(key)
        if (preference is SwitchPreferenceCompat) {
            preference.isChecked = value
        }
        // This doesn't change the GUI
        prefs.edit().putBoolean(key, value).apply()
    }

    companion object : SingletonHolder<Prefs, Context>(::Prefs)
}
