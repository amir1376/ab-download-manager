package com.abdownloadmanager.desktop.ui

import com.abdownloadmanager.desktop.App
import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.AppEffects
import com.abdownloadmanager.desktop.actions.*
import com.abdownloadmanager.desktop.pages.about.ShowAboutDialog
import com.abdownloadmanager.desktop.pages.addDownload.ShowAddDownloadDialogs
import com.abdownloadmanager.desktop.pages.extenallibs.ShowOpenSourceLibraries
import com.abdownloadmanager.desktop.pages.home.HomeComponent
import com.abdownloadmanager.desktop.pages.home.HomeEffects
import com.abdownloadmanager.desktop.pages.home.HomePage
import com.abdownloadmanager.desktop.pages.newQueue.NewQueueDialog
import com.abdownloadmanager.desktop.pages.queue.QueuesWindow
import com.abdownloadmanager.desktop.pages.settings.SettingWindow
import com.abdownloadmanager.desktop.pages.singleDownloadPage.ShowDownloadDialogs
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.rememberWindowController
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.ABDownloaderTheme
import com.abdownloadmanager.desktop.ui.widget.tray.ComposeTray
import com.abdownloadmanager.desktop.ui.widget.ProvideNotificationManager
import com.abdownloadmanager.desktop.ui.widget.ShowMessageDialogs
import com.abdownloadmanager.desktop.ui.widget.useNotification
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.GlobalAppExceptionHandler
import com.abdownloadmanager.desktop.utils.ProvideGlobalExceptionHandler
import com.abdownloadmanager.desktop.utils.action.buildMenu
import com.abdownloadmanager.desktop.utils.isInDebugMode
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.*
import com.abdownloadmanager.utils.compose.ProvideDebugInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.awt.Dimension

object Ui : KoinComponent {
    val scope: CoroutineScope by inject()
    fun boot(
        appArguments: AppArguments,
        globalAppExceptionHandler: GlobalAppExceptionHandler,
    ) {
        val appComponent: AppComponent = get()
        if (!appArguments.startSilent) {
            appComponent.openHome()
        }
        application {
            ProvideDebugInfo(AppInfo.isInDebugMode()) {
                ProvideNotificationManager {
                    ABDownloaderTheme(
                        theme = appComponent.theme.collectAsState().value,
//                    uiScale = appComponent.uiScale.collectAsState().value
                    ) {
                        ProvideGlobalExceptionHandler(globalAppExceptionHandler) {
                            val trayState = rememberTrayState()
                            HandleEffectsForApp(appComponent)
                            SystemTray(appComponent, trayState)
                            val showHomeSlot = appComponent.showHomeSlot.collectAsState().value
                            showHomeSlot.child?.instance?.let {
                                HomeWindow(it) {
                                    appComponent.closeHome()
                                }
                            }
                            val showSettingSlot = appComponent.showSettingSlot.collectAsState().value
                            showSettingSlot.child?.instance?.let {
                                SettingWindow(it, appComponent::closeSettings)
                            }
                            val showQueuesSlot = appComponent.showQueuesSlot.collectAsState().value
                            showQueuesSlot.child?.instance?.let {
                                QueuesWindow(it)
                            }
                            ShowAddDownloadDialogs(appComponent)
                            ShowDownloadDialogs(appComponent)
                            //TODO Enable Updater
                            //ShowUpdaterDialog(appComponent.updater)
                            ShowAboutDialog(appComponent)
                            NewQueueDialog(appComponent)
                            ShowMessageDialogs(appComponent)
                            ShowOpenSourceLibraries(appComponent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HandleEffectsForApp(appComponent: AppComponent) {
    val notificationManager = useNotification()
    val scope = rememberCoroutineScope()
    HandleEffects(appComponent) {
        when (it) {
            is AppEffects.SimpleNotificationNotification -> {
                scope.launch {
                    withTimeout(5000) {
                        notificationManager.showNotification(it.notificationModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationScope.SystemTray(
    component: AppComponent,
    trayState: TrayState,
) {
    ComposeTray(
        icon = MyIcons.appIcon.rememberPainter(),
        onClick = showDownloadList,
        tooltip = "Ab Download Manager",
        state = trayState,
        menu = remember {
            buildMenu {
                +showDownloadList
                +gotoSettingsAction
                +exitAction
            }
        }
    )
}

@Composable
private fun HomeWindow(
    homeComponent: HomeComponent,
    onCLoseRequest: () -> Unit,
) {
    val size by homeComponent.windowSize.collectAsState()
    val windowState = rememberWindowState(
        size = size,
        position = WindowPosition.Aligned(Alignment.Center)
    )
    val onCloseRequest = onCLoseRequest
    val windowTitle = "AB Download Manager"
    val windowIcon = MyIcons.appIcon
    val windowController = rememberWindowController(
        windowTitle,
        windowIcon.rememberPainter(),
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
                homeComponent.setWindowSize(windowState.size)
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