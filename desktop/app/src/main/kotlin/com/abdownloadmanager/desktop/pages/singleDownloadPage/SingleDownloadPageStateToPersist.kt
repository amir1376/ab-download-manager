package com.abdownloadmanager.desktop.pages.singleDownloadPage

import arrow.optics.Lens
import arrow.optics.optics
import ir.amirab.util.config.MapConfig
import ir.amirab.util.config.booleanKeyOf
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

@optics
@Serializable
data class SingleDownloadPageStateToPersist(
    val showPartInfo: Boolean = false,
) {
    class ConfigLens(prefix: String) : Lens<MapConfig, SingleDownloadPageStateToPersist>,
        KoinComponent {
        class Keys(prefix: String) {
            val showPartInfo = booleanKeyOf("${prefix}showPartInfo")
        }

        private val keys = Keys(prefix)
        override fun get(source: MapConfig): SingleDownloadPageStateToPersist {
            val default by lazy { SingleDownloadPageStateToPersist() }
            return SingleDownloadPageStateToPersist(
                showPartInfo = source.get(keys.showPartInfo) ?: default.showPartInfo,
            )
        }

        override fun set(source: MapConfig, focus: SingleDownloadPageStateToPersist): MapConfig {
            source.put(keys.showPartInfo, focus.showPartInfo)
            return source
        }
    }

    companion object
}