package com.abdownloadmanager.shared.util.mvi

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

interface ContainsScreenState<ScreenState> {
    val state: StateFlow<ScreenState>
    fun setState(state: ScreenState)
    fun setState(updater: (ScreenState) -> ScreenState)
}

class SupportsScreenState<ScreenState>(
    initialState: ScreenState
) : ContainsScreenState<ScreenState> {
    private val _state = MutableStateFlow<ScreenState>(initialState)
    override val state = _state.asStateFlow()

    override fun setState(updater: (ScreenState) -> ScreenState) {
        _state.update(updater)
    }

    override fun setState(state: ScreenState) {
        setState { state }
    }
}


