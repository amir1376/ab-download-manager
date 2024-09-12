package com.abdownloadmanager.desktop.storage

import com.abdownloadmanager.desktop.pages.home.HomePageStateToPersist
import com.abdownloadmanager.desktop.utils.*
import androidx.datastore.core.DataStore
import arrow.optics.Lens
import arrow.optics.optics
import com.abdownloadmanager.desktop.pages.singleDownloadPage.SingleDownloadPageStateToPersist
import ir.amirab.util.config.getDecoded
import ir.amirab.util.config.keyOfEncoded
import ir.amirab.util.config.putEncoded
import ir.amirab.util.config.MapConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@optics
@Serializable
data class CommonData(
    val lastSavedLocations: List<String> = emptyList(),
) {
    companion object
    class ConfigLens(prefix: String) : Lens<MapConfig, CommonData>, KoinComponent {
        class Keys(prefix: String) {
            val lastSavedLocations = keyOfEncoded<List<String>>("${prefix}lastSavedLocations")
        }

        private val json: Json by inject()
        private val keys = Keys(prefix)
        override fun get(source: MapConfig): CommonData {
            return with(json) {
                CommonData(
                    lastSavedLocations = source.getDecoded(keys.lastSavedLocations) ?: emptyList()
                )
            }
        }

        override fun set(source: MapConfig, focus: CommonData): MapConfig {
            return with(json) {
                source.putEncoded(keys.lastSavedLocations, focus.lastSavedLocations)
                source
            }
        }
    }
}

@optics
@Serializable
data class PageStatesModel(
    val home: HomePageStateToPersist = HomePageStateToPersist(),
    val downloadPage: SingleDownloadPageStateToPersist = SingleDownloadPageStateToPersist(),
    val global: CommonData = CommonData(),
) {
    companion object {
        val default get() = PageStatesModel()
    }

    object ConfigLens : Lens<MapConfig, PageStatesModel>, KoinComponent {
        private val json: Json by inject()

        object Child {
            val common = CommonData.ConfigLens("global.")
            val downloadPage = SingleDownloadPageStateToPersist.ConfigLens("downloadPage.")
            val home = HomePageStateToPersist.ConfigLens("home.")
        }

        override fun get(source: MapConfig): PageStatesModel {
            return PageStatesModel(
                home = Child.home.get(source),
                downloadPage = Child.downloadPage.get(source),
                global = Child.common.get(source)
            )
        }

        override fun set(source: MapConfig, focus: PageStatesModel): MapConfig {
            Child.home.set(source, focus.home)
            Child.downloadPage.set(source, focus.downloadPage)
            Child.common.set(source, focus.global)
            return source
        }
    }
}

class PageStatesStorage(
    settings: DataStore<MapConfig>,
) : ConfigBaseSettings<PageStatesModel>(settings, PageStatesModel.ConfigLens) {
    val lastUsedSaveLocations = from(PageStatesModel.global.lastSavedLocations)
    val downloadPage = from(PageStatesModel.downloadPage)
    val homePageStorage = from(PageStatesModel.home)
}