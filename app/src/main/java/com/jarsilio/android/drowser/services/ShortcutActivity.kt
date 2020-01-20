package com.jarsilio.android.drowser.services

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.models.AppsManager
import timber.log.Timber


class ShortcutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            DROWSE_NOW_ACTION -> {
                Timber.d("User tapped on 'Drowse now' shortcut. Drowsing apps...")
                Toast.makeText(this, getString(R.string.snackbar_zzz), Toast.LENGTH_SHORT).show()
                AppsManager(applicationContext).forceStopApps()
            }
        }

        finish()
    }

    companion object {
        const val DROWSE_NOW_ACTION = "com.jarsilio.android.drowser.action.DROWSE_NOW"
    }
}
