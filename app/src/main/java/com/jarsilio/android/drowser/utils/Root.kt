package com.jarsilio.android.drowser.utils

import java.io.DataOutputStream

import timber.log.Timber
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.InputStreamReader

class SuExecResult(val suGranted: Boolean, val stdout: List<String>)

object Root {

    fun isRootAccess(): Boolean {
        return execute("").suGranted
    }

    fun execute(command: String): SuExecResult {
        Timber.d("Executing '$command' in shell as root")
        val su = Runtime.getRuntime().exec("su")

        val dataOutputStream = DataOutputStream(su.outputStream)
        dataOutputStream.writeBytes("$command\n")
        dataOutputStream.writeBytes("exit\n")
        dataOutputStream.flush()

        // Read command output
        val dataInputStream = DataInputStream(su.inputStream)
        val bufferedReader = BufferedReader(InputStreamReader(dataInputStream))
        val output = bufferedReader.readLines()

        val exitValue = su.waitFor()
        if (exitValue != 0) {
            Timber.e("Failed to run $command!")
        }
        return SuExecResult(exitValue == 0, output)
    }
}
