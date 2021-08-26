package com.jarsilio.android.drowser.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
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
            toggleIsDrowseCandidate(cardView, appItem)
        }
    }

    private fun toggleIsDrowseCandidate(view: View, appItem: AppItem) {
        Thread(
            Runnable {
                dao.setDrowseCandidate(appItem.packageName, !appItem.isDrowseCandidate)
            }
        ).start()

        val snackBarMessage: String
        val debugMessage: String
        val undoDebugMessage: String

        if (!appItem.isDrowseCandidate) {
            debugMessage = "Added ${appItem.packageName} to drowse candidate list"
            snackBarMessage = view.context.getString(R.string.added_app, appItem.name)
            undoDebugMessage = "Undid adding ${appItem.packageName} to drowse candidate list"
        } else {
            debugMessage = "Removed ${appItem.packageName} from drowse candidate list"
            snackBarMessage = view.context.getString(R.string.removed_app, appItem.name)
            undoDebugMessage = "Undid removing ${appItem.packageName} from drowse candidate list"
        }

        Timber.d(debugMessage)

        Snackbar.make(view, snackBarMessage, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                Timber.d(undoDebugMessage)
                Thread(
                    Runnable {
                        dao.setDrowseCandidate(appItem.packageName, appItem.isDrowseCandidate)
                    }
                ).start()
            }.show()
    }
}

class AppItemDiffCallback : DiffUtil.ItemCallback<AppItem>() {
    override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
        return oldItem == newItem
    }
}
