package com.jarsilio.android.drowser

import java.io.DataOutputStream
import java.io.IOException

import timber.log.Timber

object Root {
    fun forceStopApp(packageName: String) {
        Timber.d("Force-stopping app: $packageName");
        executeAsRoot("am force-stop $packageName");
    }

    fun requestSuPermission(): Boolean {
        return executeAsRoot("")
    }

    private fun executeAsRoot(command: String): Boolean {
        Timber.d("Trying to execute '$command' as root")
        var accessGranted = false
        try {
            val suProcess = Runtime.getRuntime().exec("su")

            val dataOutputStream = DataOutputStream(suProcess.outputStream)
            dataOutputStream.writeBytes(command + "\n")
            dataOutputStream.writeBytes("exit\n")
            dataOutputStream.flush()

            val suProcessReturnValue = suProcess.waitFor()
            if (suProcessReturnValue == 0) {
                Timber.v("Root access granted (return value $suProcessReturnValue)")
                accessGranted = true
            } else {
                Timber.v("Root access denied (return value $suProcessReturnValue)")
            }

        } catch (e: IOException) {
            Timber.e("Couldn't get root access while executing '$command'", e)
        } catch (e: SecurityException) {
            Timber.e("Couldn't get root access while executing '$command'", e)
        } catch (e: InterruptedException) {
            Timber.e("Error trying to get root access", e)
        }

        return accessGranted
    }
}
