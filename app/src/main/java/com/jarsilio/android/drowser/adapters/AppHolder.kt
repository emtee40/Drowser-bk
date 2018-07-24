package com.jarsilio.android.drowser.adapters

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.models.AppInfo
import com.jarsilio.android.drowser.models.DrowseCandidatesManager
import timber.log.Timber

class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val icon = view.findViewById<View>(R.id.app_icon) as ImageView
    private val appName = view.findViewById<View>(R.id.app_name) as TextView
    private val packageName = view.findViewById<View>(R.id.package_name) as TextView
    private val cardView = view.findViewById<View>(R.id.card_view) as CardView

    fun updateWithAppItem(app: AppInfo) {
        icon.setImageDrawable(app.icon)
        appName.text = app.name
        packageName.text = app.packageName

        // Adding click listener on CardView to open clicked application directly from here .
        cardView.setOnClickListener {
            Timber.d("Clicked on ${app.packageName}.")
            val dm = DrowseCandidatesManager(cardView.context)
            if (dm.isDrowseCandidate(app.packageName)) {
                Toast.makeText(cardView.context,
                        cardView.context.getString(R.string.toast_remove_drowse, app.name),
                        Toast.LENGTH_LONG).show()
                dm.removeDrowseCandidate(app.packageName)
            } else {
                Toast.makeText(cardView.context,
                        cardView.context.getString(R.string.toast_add_drowse, app.name),
                        Toast.LENGTH_LONG).show()
                dm.addDrowseCandidate(app.packageName)
            }
        }

    }
}
