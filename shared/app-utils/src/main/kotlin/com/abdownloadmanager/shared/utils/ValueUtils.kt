package com.abdownloadmanager.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.arkivanov.decompose.router.slot.ChildSlot
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

@Composable
fun <C : Any, T : Any> StateFlow<ChildSlot<C, T>>.rememberChild(): T? {
    return collectAsState().value.child?.instance
}
