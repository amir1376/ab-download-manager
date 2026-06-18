package com.abdownloadmanager.desktop.pages.settings

import arrow.optics.Lens
import com.abdownloadmanager.desktop.pages.home.HomePageStateToPersist
import ir.amirab.util.config.*
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

@Serializable
data class SettingPageStateToPersist(
    val windowSize: Pair<Float, Float> = 800f to 400f
) {
    class ConfigLens(prefix: String) : Lens<MapConfig, SettingPageStateToPersist>,
        KoinComponent {

        class Keys(prefix: String) {
            val windowWidth = floatKeyOf("${prefix}window.width")
            val windowHeight = floatKeyOf("${prefix}window.height")
        }

        private val keys = Keys(prefix)
        override fun get(source: MapConfig): SettingPageStateToPersist {
            val default by lazy { HomePageStateToPersist() }
            return SettingPageStateToPersist(
                windowSize = run {
                    val width = source.get(keys.windowWidth)
                    val height = source.get(keys.windowHeight)
                    if (height != null && width != null) {
                        width to height
                    } else {
                        default.windowSize
                    }
                }
            )
        }

        override fun set(source: MapConfig, focus: SettingPageStateToPersist): MapConfig {
            source.put(keys.windowWidth, focus.windowSize.first)
            source.put(keys.windowHeight, focus.windowSize.second)
            return source
        }
    }
}
