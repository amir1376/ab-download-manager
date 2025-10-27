package com.abdownloadmanager.android.storage

import androidx.datastore.core.DataStore
import com.abdownloadmanager.android.pages.home.HomePageStateToPersist
import com.abdownloadmanager.android.pages.home.sortBy
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson

class HomePageStorage(
    dataStore: DataStore<HomePageStateToPersist>,
) : ConfigBaseSettingsByJson<HomePageStateToPersist>(
    dataStore = dataStore,
) {
    val sortBy = from(HomePageStateToPersist.sortBy)
}
