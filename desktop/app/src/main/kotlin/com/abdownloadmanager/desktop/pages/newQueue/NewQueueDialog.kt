package com.abdownloadmanager.desktop.pages.newQueue

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.rememberWindowController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import ir.amirab.util.desktop.screen.applyUiScale
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun NewQueueDialog(
    appComponent: AppComponent,
) {
    if (appComponent.showCreateQueueDialog.collectAsState().value) {
        CustomWindow(
            state = rememberWindowState(
                initialBoundsProvider = WindowBoundsProvider(
                    sizeProvider = WindowSizeProvider.Fixed(
                        size = DpSize(width = 300.dp, height = 130.dp)
                            .applyUiScale(LocalUiScale.current),
                    ),
                    positionProvider = WindowPositionProvider.CenteredOnScreen
                )
            ),
            resizable = false,
            onRequestToggleMaximize = null,
            onRequestMinimize = null,
            alwaysOnTop = true,
            onCloseRequest = {
                appComponent.closeNewQueueDialog()
            },
            windowController = rememberWindowController(
                title = myStringResource(Res.string.add_new_queue),
            ),
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
