package com.jarsilio.android.drowser.models

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

class AppItemsViewModel : ViewModel() {
    fun getDrowseCandidates(appItemsDao: AppItemsDao): LiveData<List<AppItem>> {
        return appItemsDao.drowseCandidatesLive
    }

    fun getUserApps(appItemsDao: AppItemsDao): LiveData<List<AppItem>> {
        return appItemsDao.userAppsLive
    }
}