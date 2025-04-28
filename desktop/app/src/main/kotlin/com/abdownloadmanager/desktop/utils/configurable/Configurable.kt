package com.abdownloadmanager.desktop.utils.configurable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class Configurable<T>(
    val title: StringSource,
    val description: StringSource,
    val backedBy: MutableStateFlow<T>,
    val validate: (T) -> Boolean = { true },
    val describe: (T) -> StringSource,
    val enabled: StateFlow<Boolean> = DefaultEnabledValue,
    val visible: StateFlow<Boolean> = DefaultVisibleValue,
) {
    val stateFlow = backedBy.asStateFlow()
    fun set(value: T): Boolean {
        if (validate(value)) {
            // don't use update function here maybe this is a mappedByTwoWayMutableStateFlow
            // IMPROVE
            backedBy.value = value
            return true
        }
        return false
    }

    @Composable
    abstract fun render(modifier: Modifier)

    companion object {
        val DefaultEnabledValue get() = MutableStateFlow(true)
        val DefaultVisibleValue get() = MutableStateFlow(true)
    }
}


