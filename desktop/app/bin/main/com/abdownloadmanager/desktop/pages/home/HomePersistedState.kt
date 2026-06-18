package com.abdownloadmanager.desktop.pages.home

import com.abdownloadmanager.shared.ui.widget.table.customtable.TableState
import arrow.optics.Lens
import ir.amirab.util.config.floatKeyOf
import ir.amirab.util.config.getDecoded
import ir.amirab.util.config.keyOfEncoded
import ir.amirab.util.config.putEncodedNullable
import ir.amirab.util.config.MapConfig
import ir.amirab.util.config.booleanKeyOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@Serializable
data class HomePageStateToPersist(
    val downloadListState: TableState.SerializableTableState? = null,
    val windowSize: Pair<Float, Float> = 1000f to 500f,
    val isMaximized: Boolean = false,
    val categoriesWidth: Float = 185f,
) {
    class ConfigLens(prefix: String) : Lens<MapConfig, HomePageStateToPersist>,
        KoinComponent {
        private val json: Json by inject()

        class Keys(prefix: String) {
            val windowWidth = floatKeyOf("${prefix}window.width")
            val windowHeight = floatKeyOf("${prefix}window.height")
            val isMaximized = booleanKeyOf("${prefix}window.isMaximized")
            val categoriesWidth = floatKeyOf("${prefix}categories.width")
            val downloadListTableState = keyOfEncoded<TableState.SerializableTableState>("${prefix}downloadListState")
        }

        private val keys = Keys(prefix)
        override fun get(source: MapConfig): HomePageStateToPersist {
            val default by lazy { HomePageStateToPersist() }
            return with(json) {
                HomePageStateToPersist(
                    downloadListState = source.getDecoded(keys.downloadListTableState),
                    categoriesWidth = source.get(keys.categoriesWidth) ?: default.categoriesWidth,
                    windowSize = run {
                        val width = source.get(keys.windowWidth)
                        val height = source.get(keys.windowHeight)
                        if (height != null && width != null) {
                            width to height
                        } else {
                            default.windowSize
                        }
                    },
                    isMaximized = source.get(keys.isMaximized) ?: default.isMaximized,
                )
            }
        }

        override fun set(source: MapConfig, focus: HomePageStateToPersist): MapConfig {
            with(json) {
                source.put(keys.windowWidth, focus.windowSize.first)
                source.put(keys.windowHeight, focus.windowSize.second)
                source.put(keys.isMaximized, focus.isMaximized)
                source.put(keys.categoriesWidth, focus.categoriesWidth)
                source.putEncodedNullable(keys.downloadListTableState, focus.downloadListState)
            }
            return source
        }
    }
}
