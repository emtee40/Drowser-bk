package com.jarsilio.android.drowser.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.models.AppInfo

class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val icon = view.findViewById<View>(R.id.app_icon) as ImageView
    private val appName = view.findViewById<View>(R.id.app_name) as TextView
    private val packageName = view.findViewById<View>(R.id.package_name) as TextView

    fun updateWithAppItem(app: AppInfo) {
        icon.setImageDrawable(app.icon)
        appName.text = app.name
        packageName.text = app.packageName
    }
}
