package com.jarsilio.android.drowser.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.jarsilio.android.drowser.R
import com.jarsilio.android.drowser.models.AppListType
import com.jarsilio.android.drowser.fragments.AppListFragment

class PageAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {
    // Each item is a tab
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AppListFragment.newInstance(AppListType.DROWSE_CANDIDATES)
            1 -> AppListFragment.newInstance(AppListType.USER)
            2 -> AppListFragment.newInstance(AppListType.SYSTEM)
            3 -> AppListFragment.newInstance(AppListType.ALL)
            else -> AppListFragment.newInstance(AppListType.ALL)
        }
    }

    override fun getCount(): Int {
        // Show 4 total tabs.
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.title_tab_will_drowse_apps)
            1 -> context.getString(R.string.title_tab_user_apps)
            2 -> context.getString(R.string.title_tab_system_apps)
            3 -> context.getString(R.string.title_tab_all_apps)
            else -> context.getString(R.string.title_tab_all_apps)
        }
    }
}

