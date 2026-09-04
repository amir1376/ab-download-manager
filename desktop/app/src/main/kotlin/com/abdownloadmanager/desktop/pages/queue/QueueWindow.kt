package com.abdownloadmanager.desktop.pages.queue

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.rememberChild

@Composable
fun QueuesWindow(appComponent: AppComponent) {
    appComponent.showQueuesSlot.rememberChild()?.let {
        QueuesWindow(it)
    }
}


@Composable
private fun QueuesWindow(queuesComponent: QueuesComponent) {
    val state = rememberWindowState()
    CustomWindow(
        state = state,
        onCloseRequest = queuesComponent.close
    ) {
        HandleEffects(queuesComponent) {
            if (it == QueuesComponentEffects.ToFront) {
                state.requestMinimized(false)
                window.toFront()
            }
        }
        QueuePage(queuesComponent)
    }
}
