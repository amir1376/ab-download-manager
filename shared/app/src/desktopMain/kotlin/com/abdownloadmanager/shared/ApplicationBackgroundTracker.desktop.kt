package com.abdownloadmanager.shared

import java.awt.KeyboardFocusManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.beans.PropertyChangeListener

object DesktopApplicationBackgroundTracker :
    IApplicationBackgroundTracker,
    AutoCloseable {

    private val focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()

    private val _isInBackgroundFlow = MutableStateFlow(focusManager.activeWindow == null)
    override val isInBackgroundFlow: StateFlow<Boolean> = _isInBackgroundFlow.asStateFlow()

    private val activeWindowListener = PropertyChangeListener {
        _isInBackgroundFlow.value = checkIsInBackground()
    }

    private fun registerListeners() {
        focusManager.addPropertyChangeListener(
            "activeWindow",
            activeWindowListener
        )
    }

    private fun unregisterListeners() {
        focusManager.removePropertyChangeListener(
            "activeWindow",
            activeWindowListener
        )
    }

    private fun checkIsInBackground(): Boolean {
        return focusManager.activeWindow == null
    }

    override fun close() {
        unregisterListeners()
    }

    init {
        registerListeners()

        // Recheck after registering the listener to avoid missing
        // a focus change during initialization.
        _isInBackgroundFlow.value = checkIsInBackground()
    }
}

actual fun platformApplicationBackgroundTracker(): IApplicationBackgroundTracker = DesktopApplicationBackgroundTracker
