package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.utils.mvi.HandleEffects
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun SettingWindow(
    settingsComponent: SettingsComponent,
    onRequestCloseWindow: () -> Unit,
) {
    val state = rememberWindowState(
        size = DpSize(width = 800.dp, height = 400.dp),
        position = WindowPosition.Aligned(Alignment.Center),
    )
    CustomWindow(state, {
        onRequestCloseWindow()
    }) {
        HandleEffects(settingsComponent) {
            when (it) {
                SettingPageEffects.BringToFront -> {
                    state.isMinimized = false
                    window.toFront()
                }
            }
        }
//        Spacer(Modifier.fillMaxWidth().height(1.dp).background(myColors.surface))
        SettingsPage(settingsComponent, onRequestCloseWindow)
    }
}