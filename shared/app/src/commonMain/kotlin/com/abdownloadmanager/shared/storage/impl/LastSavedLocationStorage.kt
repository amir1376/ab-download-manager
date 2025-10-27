package com.abdownloadmanager.shared.storage.impl

import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import kotlinx.coroutines.flow.MutableStateFlow

class LastSavedLocationStorage(
    dataStore: DataStore<List<String>>
) : ConfigBaseSettingsByJson<List<String>>(dataStore), ILastSavedLocationsStorage {
    override val lastUsedSaveLocations: MutableStateFlow<List<String>> = data
}
