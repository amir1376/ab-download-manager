package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.isMinimizedOrNull
import com.abdownloadmanager.desktop.window.custom.placementOrNull
import com.abdownloadmanager.desktop.window.custom.sizeOrNull
import com.abdownloadmanager.shared.settings.BaseSettingsComponent
import com.abdownloadmanager.shared.util.rememberChild

@Composable
fun SettingWindow(
    appComponent: AppComponent,
) {
    appComponent.showSettingSlot.rememberChild()?.let {
        SettingWindow(it, appComponent::closeSettings)
    }
}

@Composable
private fun SettingWindow(
    settingsComponent: DesktopSettingsComponent,
    onRequestCloseWindow: () -> Unit,
) {
    val windowState = rememberWindowState(
        initialBoundsProvider = WindowBoundsProvider(
            sizeProvider = WindowSizeProvider.Fixed(
                size = settingsComponent.windowSize.value,
            ),
            positionProvider = WindowPositionProvider.CenteredOnScreen
        )
    )
    LaunchedEffect(windowState.sizeOrNull) {
        if (windowState.isMinimizedOrNull == false && windowState.placementOrNull == WindowPlacement.Floating) {
            windowState.sizeOrNull?.let {
                settingsComponent.setWindowSize(it)
            }
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
                            windowState.requestMinimized(false)
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
