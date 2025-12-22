package com.abdownloadmanager.shared.util

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.coroutines.withLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

abstract class BaseComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {
    val scope = coroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    fun <T> Flow<T>.withResumedLifecycle(): Flow<T> {
        return withLifecycle(
            lifecycle = lifecycle,
            minActiveState = Lifecycle.State.STARTED,
        )
    }
}
