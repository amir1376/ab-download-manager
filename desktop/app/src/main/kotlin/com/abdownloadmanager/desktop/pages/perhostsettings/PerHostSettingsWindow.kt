package com.abdownloadmanager.desktop.pages.perhostsettings

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.pages.perhostsettings.BasePerHostSettingsComponent
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.rememberChild
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun PerHostSettingsWindow(
    appComponent: AppComponent
) {
    val component = appComponent.perHostSettingsSlot.rememberChild()
    if (component != null) {
        val windowState = rememberWindowState(
            initialBoundsProvider = WindowBoundsProvider(
                sizeProvider = WindowSizeProvider.Fixed(
                    DpSize(
                        600.dp,
                        400.dp,
                    )
                        .applyUiScale(LocalUiScale.current)
                ),
                positionProvider = WindowPositionProvider.CenteredOnScreen
            )
        )
        CustomWindow(
            state = windowState,
            onCloseRequest = appComponent::closePerHostSettings,
        ) {
            HandleEffects(component) {
                when (it) {
                    is BasePerHostSettingsComponent.Effects.Platform -> {
                        when (it as DesktopPerHostSettingsComponent.Effects) {
                            DesktopPerHostSettingsComponent.Effects.BringToFront -> {
                                windowState.requestMinimized(false)
                                window.toFront()
                            }
                        }
                    }
                }
            }
            PerHostSettingsPage(component)
        }
    }
}
