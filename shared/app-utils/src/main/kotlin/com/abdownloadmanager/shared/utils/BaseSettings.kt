package com.abdownloadmanager.shared.utils

import androidx.datastore.core.DataStore
import arrow.optics.Lens
import ir.amirab.util.flow.mapTwoWayStateFlow
import ir.amirab.util.config.MapConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseStorage<T> : KoinComponent {
    val scope: CoroutineScope by inject()

    protected abstract val inMemoryState: MutableStateFlow<T>
    protected abstract suspend fun saveData(data: T)

    val data get() = inMemoryState

    fun <K> from(lens: Lens<T, K>): MutableStateFlow<K> {
        return inMemoryState.mapTwoWayStateFlow(lens)
    }

    /**
     * call this on upper implementations where [inMemoryState] and [saveData] are implemented
     */
    protected fun startPersistData() {
        inMemoryState
            //first
            .drop(1)
            .debounce(500)
            .onEach { s ->
                saveData(s)
            }.launchIn(scope)
    }
}

abstract class ConfigBaseSettingsByMapConfig<T>(
    private val dataStore: DataStore<MapConfig>,
    private val lens: Lens<MapConfig, T>,
) : BaseStorage<T>(), KoinComponent {
    private val lastFileState = dataStore.data.let {
        runBlocking { it.stateIn(scope) }
    }

    override val inMemoryState = MutableStateFlow(
        lens.get(lastFileState.value)
    )

    override suspend fun saveData(data: T) {
        dataStore.updateData {
            val newData = lens.set(MapConfig(), data)
            newData
        }
    }

    init {
        startPersistData()
    }
}

abstract class ConfigBaseSettingsByJson<T>(
    private val dataStore: DataStore<T>,
) : BaseStorage<T>(), KoinComponent {
    private val lastFileState = dataStore.data.let {
        runBlocking { it.stateIn(scope) }
    }

    override val inMemoryState = MutableStateFlow(lastFileState.value)
    override suspend fun saveData(data: T) {
        dataStore.updateData { data }
    }

    init {
        startPersistData()
    }
}