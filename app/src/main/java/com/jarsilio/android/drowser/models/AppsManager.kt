package com.jarsilio.android.drowser.models

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import eu.chainfire.libsuperuser.Shell
import timber.log.Timber

class AppsManager(private val context: Context) {
    private val SERVICE_RECORD_MATCH = Regex("\\s*\\* ServiceRecord\\{.+ (.+)\\}.*") // Example:  * ServiceRecord{aad95cb u0 com.whatsapp/.gcm.RegistrationIntentService}
    private val appItemsDao = AppDatabase.getInstance(context.applicationContext).appItemsDao()

    fun forceStopApps() {
        if (!Shell.SU.available()) {
            Timber.e("Root access not granted (or device nor rooted)")
            return
        }

        Timber.d("Force-stopping all candidate apps")
        val commands: MutableList<String> = mutableListOf()
        val appItemsDao = AppDatabase.getInstance(context).appItemsDao()

        Thread(Runnable {
            Timber.v("Preparing shell commands:")
            for (appItem in appItemsDao.drowseCandidates) { // in separate thread because of database access
                val command = "am force-stop ${appItem.packageName}"
                commands.add(command)
                Timber.v("-> $command")
            }
            
            Timber.d("Running shell commands as root")
            Shell.SU.run(commands)
            Timber.d("Done")
        }).start()
    }

    fun getActiveServices(packageName: String): List<String> {
        val activeServices: MutableList<String> = mutableListOf<String>()
        val dumpsysServicesOutput = Shell.SU.run("dumpsys activity services $packageName")
        for (line in dumpsysServicesOutput) {
            if (line.matches(SERVICE_RECORD_MATCH)) {
                val service = line.replace(SERVICE_RECORD_MATCH, "$1")
                Timber.d("Service: $service")
                activeServices.add(service)
            }
        }
        return activeServices.toList()
    }

    fun getActiveServicesForAllApps(): List<String> {
        return getActiveServices("")
    }

    fun initDatabase() {
        Thread(Runnable {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
            for (resolveInfo in resolveInfoList) {
                val packageName = resolveInfo.activityInfo.applicationInfo.packageName
                val name = getAppName(packageName)
                val isSystem = isSystemPackage(resolveInfo)
                val isDrowseCandidate = false

                val appItem = AppItem(packageName, name, isSystem, isDrowseCandidate)
                Timber.d("Inserting $appItem if it does not already exist")
                appItemsDao.insertIfNotExists(appItem)
            }
        }).start()
    }

    private fun getAppName(packageName: String): String {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(applicationInfo) as String
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            "Untitled app" // TODO: Use a string from strings.xml for this
        }
    }

    private fun isSystemPackage(resolveInfo: ResolveInfo): Boolean {
        return resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }
}
