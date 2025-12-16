package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.shared.settings.BaseSettingsComponent

@Composable
fun SettingWindow(
    settingsComponent: DesktopSettingsComponent,
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
                is BaseSettingsComponent.Effects.Platform -> {
                    when (it as DesktopSettingsComponent.Effects) {
                        DesktopSettingsComponent.Effects.BringToFront -> {
                            windowState.isMinimized = false
                            window.toFront()
                        }
                    }
                }
            }
        }
//        Spacer(Modifier.fillMaxWidth().height(1.dp).background(myColors.surface))
        SettingsPage(settingsComponent, onRequestCloseWindow)
    }
}
