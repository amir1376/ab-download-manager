package com.abdownloadmanager.shared.utils

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {
    val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    init {
        componentContext.lifecycle.doOnDestroy {
            scope.cancel()
        }
    }
}