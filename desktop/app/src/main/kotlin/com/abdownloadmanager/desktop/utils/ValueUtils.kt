package com.abdownloadmanager.desktop.utils

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun <T : Any> Value<T>.subscribeAsStateFlow(): StateFlow<T> {
    val stateFlow = MutableStateFlow(this.value)
    subscribe {
        stateFlow.value = it
    }
    return stateFlow
}