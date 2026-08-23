package com.abdownloadmanager.desktop.pages.home

import com.abdownloadmanager.shared.ui.widget.table.customtable.TableState
import kotlinx.serialization.Serializable


@Serializable
data class HomePageStateToPersist(
    val downloadListState: TableState.SerializableTableState? = null,
    val windowSize: Pair<Float, Float> = 1000f to 500f,
    val isMaximized: Boolean = false,
    val categoriesWidth: Float = 185f,
)
