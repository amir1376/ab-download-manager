package com.abdownloadmanager.desktop.pages.perhostsettings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.utils.mvi.HandleEffects
import com.abdownloadmanager.shared.utils.rememberChild

@Composable
fun PerHostSettingsWindow(
    appComponent: AppComponent
) {
    val component = appComponent.perHostSettingsSlot.rememberChild()
    if (component != null) {
        val windowState = rememberWindowState(
            size = DpSize(
                600.dp,
                400.dp,
            ),
            position = WindowPosition.Aligned(Alignment.Center)
        )
        CustomWindow(
            state = windowState,
            onCloseRequest = appComponent::closePerHostSettings,
        ) {
            HandleEffects(component) {
                when (it) {
                    PerHostSettingsComponentEffects.BringToFront -> {
                        windowState.isMinimized = false
                        window.toFront()
                    }
                }
            }
            PerHostSettingsPage(component)
        }
    }
}
