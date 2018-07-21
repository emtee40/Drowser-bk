package com.jarsilio.android.drowser.models

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.utils.Root
import timber.log.Timber

class AppInfo(val name: String, val packageName: String, val icon: Drawable?, val isSystem: Boolean) {
    val lowercaseName = name.toLowerCase() // for alphabetical sorting
}

enum class AppListType {
    DROWSE_CANDIDATES,
    USER,
    SYSTEM,
    ALL
}

class AppsManager(private val context: Context) {
    private val SERVICE_RECORD_MATCH = Regex("\\s*\\* ServiceRecord\\{.+ (.+)\\}.*") // Example:  * ServiceRecord{aad95cb u0 com.whatsapp/.gcm.RegistrationIntentService}

    private val drowseCandidatesManager = DrowseCandidatesManager(context)

    fun forceStopApp(packageName: String) {
        if (packageName == "") {
            Timber.d("Not force-stopping app with empty package name")
        } else {
            Timber.d("Force-stopping app: $packageName")
            Root.execute("am force-stop $packageName")
        }
    }

    fun forceStopApps() {
        Timber.d("Prefs(context).drowseCandidates: ${Prefs(context).drowseCandidates}")
        Timber.d("drowseCandidatesManager.drowseCandidates: ${drowseCandidatesManager.drowseCandidates}")
        Timber.d("Force-stopping all candidate apps')")
        for (app in drowseCandidatesManager.drowseCandidates) {
            forceStopApp(app)
        }
    }

    fun getActiveServices(packageName: String): List<String> {
        val activeServices: MutableList<String> = mutableListOf<String>()
        val dumpsysServicesOutput = Root.execute("dumpsys activity services $packageName").stdout
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

    fun getUserApps(): List<AppInfo> {
        val userApps: MutableList<AppInfo> = mutableListOf<AppInfo>()
        for (app in getAllApps()) {
            if (!app.isSystem) {
                userApps.add(app)
            }
        }
        return userApps.toList()
    }

    fun getApp(packageName: String): AppInfo? {
        var appInfo: AppInfo? = null
        for (app in getAllApps()) {
            if (app.packageName == packageName) {
                appInfo = app
                break
            }
        }
        return appInfo
    }

    fun getDrowseCandidates(): List<AppInfo> {
        val drowseCandidatesPackageNames = DrowseCandidatesManager(context).drowseCandidates
        val drowseCandidates: MutableList<AppInfo> = mutableListOf<AppInfo>()
        for (app in getAllApps()) {
            for (drowseCandidatePackageName in drowseCandidatesPackageNames) {
                if (app.packageName == drowseCandidatePackageName) {
                    drowseCandidates.add(app)
                    break
                }
            }
        }
        return drowseCandidates.toList()
    }

    fun getSystemApps(): List<AppInfo> {
        val userApps: MutableList<AppInfo> = mutableListOf<AppInfo>()
        for (app in getAllApps()) {
            if (app.isSystem) {
                userApps.add(app)
            }
        }
        return userApps.toList()
    }

    fun getAllApps(): List<AppInfo> {
        val allApps: MutableList<AppInfo> = mutableListOf<AppInfo>()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resolveInfoList) {
            val packageName = resolveInfo.activityInfo.applicationInfo.packageName
            val name = getAppName(packageName)
            val icon = getAppIcon(packageName)
            val isSystem = isSystemPackage(resolveInfo)

            allApps.add(AppInfo(name, packageName, icon, isSystem))
        }

        return allApps.sortedWith(compareBy({ it.lowercaseName })).toList()
    }

    private fun getAppName(packageName: String): String {
        var name = ""
        val packageManager = context.packageManager
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            name = packageManager.getApplicationLabel(applicationInfo) as String
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }

        return name
    }

    private fun getAppIcon(packageName: String): Drawable? {
        var drawable: Drawable?
        try {
            drawable = context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        }

        return drawable
    }

    private fun isSystemPackage(resolveInfo: ResolveInfo): Boolean {
        return resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }
}

class DrowseCandidatesManager(context: Context) {
    private val prefs = Prefs(context)

    val drowseCandidates: List<String>
        get() = prefs.drowseCandidates.split(";")

    fun addDrowseCandidate(packageName: String) {
        val pref: String = prefs.drowseCandidates
        if (!pref.matches(Regex(".*$packageName(;.+|\$)"))) {
            Timber.v("Adding '$packageName' to drowse candidate list. It will be force-stopped the next time the screen is turned off")
            if (pref != "") {
                prefs.drowseCandidates += ";"
            }
            prefs.drowseCandidates += "$packageName"
        } else {
            Timber.v("Not adding '$packageName'. It is already in the list")
        }
    }

    fun removeDrowseCandidate(packageName: String) {
        Timber.d("Removing drowse candidate '$packageName'. It will be not be force-stopped the next time the screen is turned off. You will have to start it manually again if it has already been force-stopped")
        if (prefs.drowseCandidates.contains("$packageName;")) {
            prefs.drowseCandidates = prefs.drowseCandidates.replace("$packageName;", "")
        }
    }

    fun clearDrowseCandidates() {
        prefs.drowseCandidates = ""
    }
}