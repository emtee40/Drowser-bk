package com.jarsilio.android.drowser.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jarsilio.android.drowser.models.AppListType
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.adapters.AppAdapter

class AppListFragment() : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_app_list, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recycler_view) as RecyclerView
        val listType = arguments!!.getSerializable(ARG_LIST_TYPE) as AppListType
        val appsManager = AppsManager(context!!)

        var apps = when (listType) {
            AppListType.DROWSE_CANDIDATES -> appsManager.getDrowseCandidates()
            AppListType.USER -> appsManager.getUserApps()
            AppListType.SYSTEM -> appsManager.getSystemApps()
            AppListType.ALL -> appsManager.getAllApps()
        }

        recyclerView.adapter = AppAdapter(apps)
        recyclerView.layoutManager = LinearLayoutManager(context)
        return rootView
    }

    companion object {
        const val ARG_LIST_TYPE = "LIST_TYPE"

        fun newInstance(listType: AppListType): AppListFragment {
            val fragment = AppListFragment()
            val args = Bundle()
            args.putSerializable(ARG_LIST_TYPE, listType)

            fragment.arguments = args
            return fragment
        }
    }
}
