package com.jarsilio.android.drowser.adapters

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jarsilio.android.drowser.models.AppInfo
import com.jarsilio.android.drowser.R

class AppAdapter(private val apps: List<AppInfo>) : RecyclerView.Adapter<AppHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        val appItem = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_app_item, parent, false) as CardView
        return AppHolder(appItem)
    }

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        holder.updateWithAppItem(apps[position])
    }

    override fun getItemCount(): Int {
        return apps.size
    }

}
