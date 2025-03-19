package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.utils.mvi.HandleEffects
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun SettingWindow(
    settingsComponent: SettingsComponent,
    onRequestCloseWindow: () -> Unit,
) {
    val windowState = rememberWindowState(
        size = settingsComponent.windowSize.value,
        position = WindowPosition.Aligned(Alignment.Center),
    )
    LaunchedEffect(windowState.size) {
        if (!windowState.isMinimized && windowState.placement == WindowPlacement.Floating) {
            settingsComponent.setWindowSize(windowState.size)
        }
    }
    CustomWindow(windowState, {
        onRequestCloseWindow()
    }) {
        HandleEffects(settingsComponent) {
            when (it) {
                SettingPageEffects.BringToFront -> {
                    windowState.isMinimized = false
                    window.toFront()
                }
            }
        }
//        Spacer(Modifier.fillMaxWidth().height(1.dp).background(myColors.surface))
        SettingsPage(settingsComponent, onRequestCloseWindow)
    }
}
