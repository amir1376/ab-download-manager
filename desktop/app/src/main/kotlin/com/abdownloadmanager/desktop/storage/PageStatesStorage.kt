package com.abdownloadmanager.desktop.storage

import com.abdownloadmanager.desktop.pages.home.HomePageStateToPersist
import androidx.datastore.core.DataStore
import arrow.optics.optics
import com.abdownloadmanager.desktop.pages.settings.SettingPageStateToPersist
import com.abdownloadmanager.desktop.pages.singleDownloadPage.SingleDownloadPageStateStorage
import com.abdownloadmanager.desktop.pages.singleDownloadPage.SingleDownloadPageStateToPersist
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import kotlinx.serialization.Serializable

@optics
@Serializable
data class CommonData(
    val lastSavedLocations: List<String> = emptyList(),
) {
    companion object
}

@optics
@Serializable
data class PageStatesModel(
    val home: HomePageStateToPersist = HomePageStateToPersist(),
    val settings: SettingPageStateToPersist = SettingPageStateToPersist(),
    val downloadPage: SingleDownloadPageStateToPersist = SingleDownloadPageStateToPersist(),
    val global: CommonData = CommonData(),
) {
    companion object {
        val default get() = PageStatesModel()
    }
}

class PageStatesStorage(
    settings: DataStore<PageStatesModel>,
) : ConfigBaseSettingsByJson<PageStatesModel>(settings),
    ILastSavedLocationsStorage,
    SingleDownloadPageStateStorage {
    override val lastUsedSaveLocations = from(PageStatesModel.global.lastSavedLocations)
    override val singleDownloadPageState = from(PageStatesModel.downloadPage)
    val homePageStorage = from(PageStatesModel.home)
    val settingsPageStorage = from(PageStatesModel.settings)
}
