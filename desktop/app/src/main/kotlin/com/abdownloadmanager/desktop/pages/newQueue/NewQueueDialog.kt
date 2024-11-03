package com.abdownloadmanager.desktop.pages.newQueue

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun NewQueueDialog(
    appComponent: AppComponent,
) {
    if (appComponent.showCreateQueueDialog.collectAsState().value){
        CustomWindow(
            state = rememberWindowState(
                size = DpSize(width = 300.dp, height = 130.dp),
                position = WindowPosition.Aligned(Alignment.Center),
            ),
            resizable = false,
            onRequestToggleMaximize = null,
            onRequestMinimize = null,
            alwaysOnTop = true,
            onCloseRequest = {
                appComponent.closeNewQueueDialog()
            }
        ) {
            NewQueue(
                onQueueCreate = {
                    appComponent.closeNewQueueDialog()
                    appComponent.createNewQueue(it)
                },
                onCloseRequest = {
                    appComponent.closeNewQueueDialog()
                }
            )
        }
    }
}