package com.jarsilio.android.drowser.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class AppItemsViewModel : ViewModel() {
    fun getDrowseCandidates(appItemsDao: AppItemsDao): LiveData<List<AppItem>> {
        return appItemsDao.drowseCandidatesLive
    }

    fun getNonDrowseCandidates(appItemsDao: AppItemsDao): LiveData<List<AppItem>> {
        return appItemsDao.nonDrowseCandidatesLive
    }
}
