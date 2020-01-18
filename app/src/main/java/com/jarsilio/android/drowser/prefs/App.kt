package com.jarsilio.android.drowser.prefs

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.squareup.leakcanary.LeakCanary

import timber.log.Timber

const val MAX_TAG_LENGTH = 23

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(LongTagTree(packageName))

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }

    inner class LongTagTree(private val packageName: String) : Timber.DebugTree() {
        private fun getMessage(tag: String?, message: String): String {
            val longTagTreeMessage: String
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                // Tag length limitation (<23): Use truncated package name and add class name to message
                longTagTreeMessage = "$tag: $message"
            } else {
                // No tag length limit limitation: Use package name *and* class name
                longTagTreeMessage = message
            }
            return longTagTreeMessage
        }

        private fun getTag(tag: String?): String {
            var longTagTreeTag: String
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                // Tag length limitation (<23): Use truncated package name and add class name to message
                longTagTreeTag = packageName
                if (longTagTreeTag.length > MAX_TAG_LENGTH) {
                    val shortPackageName = packageName.substring(packageName.length - MAX_TAG_LENGTH + 3, packageName.length)
                    longTagTreeTag = "...$shortPackageName"
                }
            } else {
                // No tag length limit limitation: Use package name *and* class name
                longTagTreeTag = "$packageName $tag"
            }

            return longTagTreeTag
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val longTagTreeMessage = getMessage(tag, message)
            val longTagTreeTag = getTag(tag)

            super.log(priority, longTagTreeTag, longTagTreeMessage, t)
        }
    }
}
