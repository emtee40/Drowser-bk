package com.jarsilio.android.drowser.adapters

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.models.AppDatabase
import com.jarsilio.android.drowser.models.AppItem
import timber.log.Timber

class AppItemListAdapter : ListAdapter<AppItem, AppItemHolder>(AppItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemHolder {
        val appItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_app_item, parent, false) as CardView
        return AppItemHolder(appItem)
    }

    override fun onBindViewHolder(holder: AppItemHolder, position: Int) {
        holder.updateWithAppItem(getItem(position))
    }
}

class AppItemHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val icon = view.findViewById<View>(R.id.app_icon) as ImageView
    private val appName = view.findViewById<View>(R.id.app_name) as TextView
    private val packageName = view.findViewById<View>(R.id.package_name) as TextView
    private val cardView = view.findViewById<View>(R.id.card_view) as CardView
    private val dao = AppDatabase.getInstance(view.context).appItemsDao()

    fun updateWithAppItem(appItem: AppItem) {
        icon.setImageDrawable(appItem.getIcon(cardView.context))
        appName.text = appItem.name
        packageName.text = appItem.packageName

        // Adding click listener on CardView to open clicked application directly from here .
        cardView.setOnClickListener {
            Timber.d("Clicked on ${appItem.packageName}.")

            if (appItem.isDrowseCandidate) {
                Timber.d("${appItem.packageName} is not a drowse candidate anymore")
                Thread(Runnable {
                    dao.setDrowseCandidate(appItem.packageName, false)
                }).start()
            } else {
                Timber.d("${appItem.packageName} is now a drowse candidate")
                Thread(Runnable {
                    dao.setDrowseCandidate(appItem.packageName, true)
                }).start()
            }
        }
    }
}

class AppItemDiffCallback : DiffUtil.ItemCallback<AppItem>() {
    override fun areItemsTheSame(oldItem: AppItem?, newItem: AppItem?): Boolean {
        return oldItem?.packageName == newItem?.packageName
    }

    override fun areContentsTheSame(oldItem: AppItem?, newItem: AppItem?): Boolean {
        return oldItem == newItem
    }
}
