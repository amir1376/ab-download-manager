package com.abdownloadmanager.desktop.pages.checksum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.pages.checksum.BaseFileChecksumComponent
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun FileChecksumWindow(
    component: AppComponent
) {
    component.openedFileChecksumDialog.collectAsState().value.child?.instance?.let {
        FileChecksumWindow(it)
    }
}

@Composable
fun FileChecksumWindow(
    component: DesktopFileChecksumComponent
) {
    val uiScale = LocalUiScale.current
    val state = rememberWindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        size = DpSize(900.dp, 400.dp).applyUiScale(uiScale)
    )
    CustomWindow(
        state = state,
        onCloseRequest = component::onRequestClose
    ) {
        HandleEffects(component) {
            when (it) {
                is BaseFileChecksumComponent.Effects.Platform -> {
                    when (it as DesktopFileChecksumComponent.Effects) {
                        DesktopFileChecksumComponent.Effects.BringToFront -> {
                            state.isMinimized = false
                            window.toFront()
                        }
                    }
                }
            }
        }
        FileChecksumPage(component)
    }
}
