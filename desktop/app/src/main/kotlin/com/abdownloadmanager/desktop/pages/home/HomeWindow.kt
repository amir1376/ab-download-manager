package com.abdownloadmanager.desktop.pages.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.actions.LocalShortCutManager
import com.abdownloadmanager.desktop.actions.handle
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.rememberWindowController
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.colored.AppIcon
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import java.awt.Dimension

@Composable
fun HomeWindow(
    homeComponent: HomeComponent,
    onCLoseRequest: () -> Unit,
) {
    val size by homeComponent.windowSize.collectAsState()
    val windowState = rememberWindowState(
        size = size,
        position = WindowPosition.Aligned(Alignment.Center)
    )
    val onCloseRequest = onCLoseRequest
    val windowController = rememberWindowController(
        title = AppInfo.name,
        icon = AbIcons.Colored.AppIcon,
    )

    CompositionLocalProvider(
        LocalShortCutManager provides homeComponent.shortcutManager
    ) {
        CustomWindow(
            state = windowState,
            onCloseRequest = onCloseRequest,
            windowController = windowController,
            onKeyEvent = {
                homeComponent.shortcutManager.handle(it)
            }
        ) {
            LaunchedEffect(windowState.size) {
                if (!windowState.isMinimized && windowState.placement == WindowPlacement.Floating) {
                    homeComponent.setWindowSize(windowState.size)
                }
            }
            window.minimumSize = Dimension(
                400, 400
            )
            HandleEffects(homeComponent) {
                when (it) {
                    HomeEffects.BringToFront -> {
                        windowState.isMinimized = false
                        window.toFront()
                    }

                    else -> {}
                }
            }
            BoxWithConstraints {
                HomePage(homeComponent)
            }
        }
    }
}