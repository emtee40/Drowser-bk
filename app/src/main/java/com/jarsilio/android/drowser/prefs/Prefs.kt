package com.jarsilio.android.drowser.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.CheckBoxPreference
import android.preference.PreferenceManager
import android.preference.SwitchPreference
import com.jarsilio.android.drowser.PreferencesActivity
import com.jarsilio.android.drowser.PreferencesActivity.PrefsFragment
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.models.SingletonHolder

class Prefs private constructor(context: Context) {
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var preferencesActivity: PreferencesActivity? = null

    /* User prefs */
    val IS_ENABLED: String = context.getString(R.string.pref_enabled_key)
    val SHOW_NOTIFICATION: String = context.getString(R.string.pref_notification_key)

    var isEnabled: Boolean
        get() = prefs.getBoolean(IS_ENABLED, true)
        set(value) = setBooleanPreference(IS_ENABLED, value)

    var showNotification: Boolean
        get() = prefs.getBoolean(SHOW_NOTIFICATION, true)
        set(value) = setBooleanPreference(SHOW_NOTIFICATION, value)

    /* Automagic prefs: these are not real prefs. They are just set by the app to remember stuff */
    val REQUEST_ROOT_ACCESS: String = context.getString(R.string.pref_request_root_access_key)
    val DISABLE_UNTIL: String = context.getString(R.string.pref_disable_until_key)
    val LAST_DISABLE_UNTIL_USER_CHOICE: String = context.getString(R.string.pref_last_disable_until_user_choice_key)

    var requestRootAccess: Boolean
        get() = prefs.getBoolean(REQUEST_ROOT_ACCESS, true)
        set(value) = prefs.edit().putBoolean(REQUEST_ROOT_ACCESS, value).apply()

    var disableUntil: Long
        get() = prefs.getLong(DISABLE_UNTIL, 0)
        set(value) = prefs.edit().putLong(DISABLE_UNTIL, value).apply()

    var lastDisableUntilUserChoide: Int
        get() = prefs.getInt(LAST_DISABLE_UNTIL_USER_CHOICE, 2)
        set(value) = prefs.edit().putInt(LAST_DISABLE_UNTIL_USER_CHOICE, value).apply()

    private fun setBooleanPreference(key: String, value: Boolean) {
        // This changes the GUI, but it needs the PreferencesActivity to have started
        val prefsFragment = preferencesActivity?.fragmentManager?.findFragmentById(android.R.id.content) as PrefsFragment?
        val preference = prefsFragment?.findPreference(key)
        if (preference is CheckBoxPreference) {
            preference.isChecked = value
        } else if (preference is SwitchPreference) {
            preference.isChecked = value
        }
        // This doesn't change the GUI
        prefs.edit().putBoolean(key, value).apply()
    }

    companion object : SingletonHolder<Prefs, Context>(::Prefs)
}