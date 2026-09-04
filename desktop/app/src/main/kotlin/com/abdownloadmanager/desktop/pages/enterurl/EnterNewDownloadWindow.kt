package com.abdownloadmanager.desktop.pages.enterurl

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.rememberChild
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun EnterNewDownloadWindow(
    appComponent: AppComponent
) {
    val child = appComponent.enterNewURLWindowSlot.rememberChild()
    child?.let {
        EnterNewDownloadWindow(child)
    }
}

@Composable
private fun EnterNewDownloadWindow(
    component: DesktopEnterNewURLComponent,
) {
    val windowState = rememberWindowState(
        initialBoundsProvider = WindowBoundsProvider(
            sizeProvider = WindowSizeProvider.Fixed(
                size = DpSize(400.dp, 150.dp)
                    .applyUiScale(LocalUiScale.current),
            ),
            positionProvider = WindowPositionProvider.CenteredOnScreen
        )
    )
    CustomWindow(
        state = windowState,
        onCloseRequest = component::close
    ) {
        WindowTitle(
            myStringResource(Res.string.new_download)
        )
        HandleEffects(component) {
            when (it) {
                DesktopEnterNewURLComponent.Effects.BringToFront -> {
                    windowState.requestMinimized(false)
                    window.toFront()
                }
                else -> {}
            }
        }
        EnterNewURLPage(
            component,
        )
    }
}

