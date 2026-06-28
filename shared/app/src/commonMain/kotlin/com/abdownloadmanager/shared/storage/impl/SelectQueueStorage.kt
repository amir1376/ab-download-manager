package com.abdownloadmanager.shared.storage.impl

import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.storage.ISelectQueueStorage
import com.abdownloadmanager.shared.storage.SelectQueueSettings
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import kotlinx.coroutines.flow.MutableStateFlow

class SelectQueueStorage(
    dataStore: DataStore<SelectQueueSettings>
) : ConfigBaseSettingsByJson<SelectQueueSettings>(dataStore), ISelectQueueStorage {
    override val selectQueueSettings: MutableStateFlow<SelectQueueSettings> = data
}
