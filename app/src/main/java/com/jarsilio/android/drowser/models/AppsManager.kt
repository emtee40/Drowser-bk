package com.jarsilio.android.drowser.models

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.services.DrowserService
import eu.chainfire.libsuperuser.Shell
import timber.log.Timber

class AppsManager(private val context: Context) {
    private val SERVICE_RECORD_MATCH = Regex("\\s*\\* ServiceRecord\\{.+ (.+)\\}.*") // Example:  * ServiceRecord{aad95cb u0 com.whatsapp/.gcm.RegistrationIntentService}
    private val appItemsDao = AppDatabase.getInstance(context.applicationContext).appItemsDao()
    private val prefs = Prefs.getInstance(context)

    private fun getForegroundPackageName(): String {
        var lastUsedPackage = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("WrongConstant")
            val usageStatsManager = context.getSystemService("usagestats") as UsageStatsManager
            val now = System.currentTimeMillis()
            val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 1000, now)
            var lastTimeUsed: Long = 0
            for (usageStats in usageStatsList) {
                if (usageStats.lastTimeUsed > lastTimeUsed) {
                    lastTimeUsed = usageStats.lastTimeUsed
                    lastUsedPackage = usageStats.packageName
                }
            }
        } else {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks = activityManager.runningAppProcesses
            lastUsedPackage = tasks[0].processName
        }
        return lastUsedPackage
    }

    fun forceStopApps() {
        if (!Shell.SU.available()) {
            Timber.e("Root access not granted (or device nor rooted). Stopping DrowserService until root access is granted. Will request root access once the user opens the app again.")
            prefs.requestRootAccess = true
            DrowserService.stopService(context)
            return
        }

        Timber.d("Force-stopping all candidate apps")
        val commands: MutableList<String> = mutableListOf()
        val appItemsDao = AppDatabase.getInstance(context).appItemsDao()

        Thread(
            Runnable {
                Timber.v("Preparing shell commands:")
                val foregroundApp = getForegroundPackageName()
                if (!prefs.drowseForegroundApp) {
                    Timber.d("App running in foreground: $foregroundApp")
                }
                for (appItem in appItemsDao.drowseCandidates) { // in separate thread because of database access
                    if (!prefs.drowseForegroundApp && appItem.packageName == foregroundApp) {
                        Timber.d("-> Not force-stopping $foregroundApp because 'Stop foreground app' option is disabled.")
                        continue
                    }
                    val command = "am force-stop ${appItem.packageName}"
                    commands.add(command)
                    Timber.v("-> $command")
                }

                Timber.d("Running shell commands as root")
                Shell.SU.run(commands)
                Timber.d("Done")
            }
        ).start()
    }

    fun updateAppItemsDatabase() {
        Thread(
            Runnable {
                addNewAppItemsToDatabase()
                updateAppItemsVisibility()
                removeObsoleteAppItemsFromDatabase()
            }
        ).start()
    }

    private fun addNewAppItemsToDatabase() {
        Timber.d("Adding new apps to database")
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES)

        for (packageInfo in packages) {
            val applicationInfo = packageInfo.applicationInfo ?: continue

            val packageName = applicationInfo.packageName
            val appAlreadyInDatabase = appItemsDao.loadByPackageName(packageName) != null

            if (appAlreadyInDatabase || packageName == context.packageName) {
                continue
            }

            val name = packageManager.getApplicationLabel(applicationInfo).toString()
            val isDrowseCandidate = false
            val show = true // updateAppItemsVisibility will take care of showing it or not

            val appItem = AppItem(packageName, name, isDrowseCandidate, show)
            Timber.v("-> $appItem")
            appItemsDao.insertIfNotExists(appItem) // If not exists because there might be apps that expose more than one launcher
        }
    }

    fun updateAppItemsVisibility() {
        Thread {
            for (appItem in appItemsDao.all) {
                val hideBecauseSystemApp = isSystemApp(appItem) && !prefs.showSystemApps
                val hideBecauseDisabledApp = !isAppEnabled(appItem) && !prefs.showDisabledApps
                val hideBecauseNotInstalled = !isAppInstalled(appItem)

                val show = !hideBecauseSystemApp && !hideBecauseDisabledApp && !hideBecauseNotInstalled
                appItemsDao.showApp(appItem.packageName, show)
            }
        }.start()
    }

    private fun removeObsoleteAppItemsFromDatabase() {
        Timber.d("Removing obsolete apps from database (probably uninstalled)")
        for (appItem in appItemsDao.all) {
            if (!isAppInstalled(appItem)) {
                Timber.v("-> $appItem")
                if (appItem.isDrowseCandidate) {
                    Timber.d("    Not removing $appItem from database because it is a drowse candidate")
                } else {
                    appItemsDao.delete(appItem)
                }
            }
        }
    }

    private fun isSystemApp(appItem: AppItem): Boolean {
        return try {
            context.packageManager.getApplicationInfo(appItem.packageName, 0).flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun isAppInstalled(appItem: AppItem): Boolean {
        return try {
            context.packageManager.getApplicationInfo(appItem.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun isAppEnabled(appItem: AppItem): Boolean {
        return try {
            context.packageManager.getApplicationInfo(appItem.packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            true
        }
    }
}
