package com.abdownloadmanager.desktop.pages.updater

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.rememberWindowController
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import com.abdownloadmanager.shared.pages.updater.RenderUpdateNotifications
import com.abdownloadmanager.shared.pages.updater.UpdateComponent
import ir.amirab.util.desktop.screen.applyUiScale
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun ShowUpdaterDialog(updaterComponent: UpdateComponent) {
    val showUpdate = updaterComponent.showNewUpdate.collectAsState().value
    val newVersion = updaterComponent.newVersionData.collectAsState().value
    val closeUpdatePage = {
        updaterComponent.requestClose()
    }
    RenderUpdateNotifications(updaterComponent)
    if (showUpdate && newVersion != null) {
        val uiScale = LocalUiScale.current
        CustomWindow(
            state = rememberWindowState(
                initialBoundsProvider = WindowBoundsProvider(
                    sizeProvider = WindowSizeProvider.Fixed(
                        size = DpSize(500.dp, 400.dp).applyUiScale(uiScale),
                    ),
                    positionProvider = WindowPositionProvider.CenteredOnScreen
                )
            ),
            onCloseRequest = closeUpdatePage,
            windowController = rememberWindowController(
                title = myStringResource(Res.string.update_updater),
            ),
        ) {
            NewUpdatePage(
                newVersionInfo = newVersion,
                currentVersion = updaterComponent.currentVersion,
                cancel = closeUpdatePage,
                update = {
                    updaterComponent.performUpdate()
                    closeUpdatePage()
                }
            )
        }
    }
}
