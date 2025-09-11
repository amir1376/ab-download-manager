package com.abdownloadmanager.desktop.storage

import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.utils.ConfigBaseSettingsByJson
import com.abdownloadmanager.shared.utils.perhostsettings.IPerHostSettingsStorage
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsItem
import kotlinx.coroutines.flow.MutableStateFlow

class PerHostSettingsDatastoreStorage(
    dataStore: DataStore<List<PerHostSettingsItem>>,
) : IPerHostSettingsStorage, ConfigBaseSettingsByJson<List<PerHostSettingsItem>>(dataStore) {
    override val perHostSettingsFlow: MutableStateFlow<List<PerHostSettingsItem>> = data
}
