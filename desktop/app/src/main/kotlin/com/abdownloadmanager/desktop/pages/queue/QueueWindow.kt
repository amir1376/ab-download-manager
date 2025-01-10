package com.abdownloadmanager.desktop.pages.queue

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.utils.mvi.HandleEffects
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.rememberWindowState

@Composable
fun QueuesWindow(queuesComponent: QueuesComponent) {
    val state = rememberWindowState()
    CustomWindow(
        state = state,
        onCloseRequest = queuesComponent.close
    ) {
        HandleEffects(queuesComponent) {
            if (it == QueuesComponentEffects.ToFront) {
                state.isMinimized = false
                window.toFront()
            }
        }
        QueuePage(queuesComponent)
    }
}