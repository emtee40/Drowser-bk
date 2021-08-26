package com.jarsilio.android.drowser.services

import android.annotation.TargetApi
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.utils.Utils
import timber.log.Timber

@TargetApi(Build.VERSION_CODES.N)
class TileService : TileService() {
    private val prefs: Prefs by lazy { Prefs.getInstance(this) }

    private var unclicked: Boolean = true
    private var updateTileThread: Thread? = null

    override fun onClick() {
        super.onClick()
        val currentTimeout = Timeout.getTimeout(prefs.disableUntil)
        Timber.d("Tapped on tile: timeout before click: $currentTimeout")
        val newTimeout = if (unclicked && currentTimeout != Timeout.NO_TIMEOUT) {
            Timeout.NO_TIMEOUT // First tap after opening QuickSettings drawer will remove all timeouts if some timeout was set
        } else {
            currentTimeout.next
        }
        unclicked = false

        Timber.d("New timeout: $newTimeout (disable until ${Utils.getReadableDate(prefs.disableUntil)})")

        prefs.disableUntil = newTimeout.disableUntil
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        unclicked = true
        Timber.d("Tile became visible. Starting thread to periodically update qsTile")
        updateTileThread = Thread(
            Runnable {
                try {
                    Timber.v("Updating tile from thread")
                    while (true) {
                        updateTile()
                        Thread.sleep(1000)
                    }
                } catch (e: InterruptedException) {
                    Timber.d("updateTileThread interrupted")
                }
            }
        )
        updateTileThread?.start()
    }

    override fun onStopListening() {
        super.onStopListening()
        Timber.d("Tile became invisible. Interrupting updateTileThread")
        updateTileThread?.interrupt()
        updateTileThread = null
    }

    private fun updateTile() {
        if (qsTile == null) {
            Timber.e("qsTile is null. qsTile is only valid for updates between onStartListening() and onStopListening().")
            return
        } else {
            if (!prefs.isEnabled) {
                qsTile.label = "Disabled"
                qsTile.state = Tile.STATE_INACTIVE
            } else if (prefs.disableUntil < System.currentTimeMillis()) {
                qsTile.label = "Enabled"
                qsTile.state = Tile.STATE_ACTIVE
            } else if (prefs.disableUntil >= Timeout.INFINITY.millis) {
                qsTile.label = "Paused"
                qsTile.state = Tile.STATE_INACTIVE
            } else {
                qsTile.label = "Paused for ${Utils.getTimeoutUntil(prefs.disableUntil)}"
                qsTile.state = Tile.STATE_INACTIVE
            }

            qsTile.updateTile()
        }
    }
}
