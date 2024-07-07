package com.abdownloadmanager.desktop.utils

import androidx.datastore.core.DataStore
import arrow.optics.Lens
import ir.amirab.util.flow.mapTwoWayStateFlow
import ir.amirab.util.config.MapConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class ConfigBaseSettings<T>(
    dataStore: DataStore<MapConfig>,
    lens: Lens<MapConfig, T>
) : KoinComponent {
    private val scope: CoroutineScope by inject()
    private val lastFileState = dataStore.data.let {
        runBlocking { it.stateIn(scope) }
    }

    private val inMemoryState = MutableStateFlow(
        lens.get(lastFileState.value)
    )

    init {
        inMemoryState
            //first
            .drop(1)
            .debounce(500)
            .onEach { s ->
                dataStore.updateData {
                    val newData = lens.set(MapConfig(), s)
                    newData
                }
            }.launchIn(scope)
    }

    fun <K> from(lens: Lens<T, K>): MutableStateFlow<K> {
        return inMemoryState
            .mapTwoWayStateFlow(lens)
    }
}