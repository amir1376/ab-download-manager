package com.abdownloadmanager.desktop.ui

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.AppEffects
import com.abdownloadmanager.desktop.actions.*
import com.abdownloadmanager.desktop.pages.about.ShowAboutDialog
import com.abdownloadmanager.desktop.pages.addDownload.ShowAddDownloadDialogs
import com.abdownloadmanager.desktop.pages.extenallibs.ShowOpenSourceLibraries
import com.abdownloadmanager.desktop.pages.newQueue.NewQueueDialog
import com.abdownloadmanager.desktop.pages.queue.QueuesWindow
import com.abdownloadmanager.desktop.pages.settings.SettingWindow
import com.abdownloadmanager.desktop.pages.singleDownloadPage.ShowDownloadDialogs
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.ABDownloaderTheme
import com.abdownloadmanager.desktop.ui.widget.tray.ComposeTray
import ir.amirab.util.compose.action.buildMenu
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import com.abdownloadmanager.desktop.pages.batchdownload.BatchDownloadWindow
import com.abdownloadmanager.desktop.pages.category.ShowCategoryDialogs
import com.abdownloadmanager.desktop.pages.confirmexit.ConfirmExit
import com.abdownloadmanager.desktop.pages.credits.translators.ShowTranslators
import com.abdownloadmanager.desktop.pages.editdownload.EditDownloadWindow
import com.abdownloadmanager.desktop.pages.home.HomeWindow
import com.abdownloadmanager.desktop.pages.settings.ThemeManager
import com.abdownloadmanager.desktop.pages.updater.ShowUpdaterDialog
import com.abdownloadmanager.desktop.ui.widget.*
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.utils.compose.ProvideDebugInfo
import ir.amirab.util.compose.localizationmanager.LanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

object Ui : KoinComponent {
    val scope: CoroutineScope by inject()
    fun boot(
        appArguments: AppArguments,
        globalAppExceptionHandler: GlobalAppExceptionHandler,
    ) {
        val appComponent: AppComponent = get()
        val themeManager: ThemeManager = get()
        val languageManager: LanguageManager = get()
        themeManager.boot()
        languageManager.boot()
        if (!appArguments.startSilent) {
            appComponent.openHome()
        }
        application {
            val theme by themeManager.currentThemeColor.collectAsState()

            ProvideDebugInfo(AppInfo.isInDebugMode()) {
                ProvideLanguageManager(languageManager) {
                    ProvideNotificationManager {
                        ABDownloaderTheme(
                            myColors = theme,
                            uiScale = appComponent.uiScale.collectAsState().value
                        ) {
                            ProvideGlobalExceptionHandler(globalAppExceptionHandler) {
                                ProvideSizeUnits(appComponent) {
                                    val trayState = rememberTrayState()
                                    HandleEffectsForApp(appComponent)
                                    SystemTray(appComponent, trayState)
                                    val showHomeSlot = appComponent.showHomeSlot.collectAsState().value
                                    showHomeSlot.child?.instance?.let {
                                        HomeWindow(it, appComponent::closeHome)
                                    }
                                    val showSettingSlot = appComponent.showSettingSlot.collectAsState().value
                                    showSettingSlot.child?.instance?.let {
                                        SettingWindow(it, appComponent::closeSettings)
                                    }
                                    val showQueuesSlot = appComponent.showQueuesSlot.collectAsState().value
                                    showQueuesSlot.child?.instance?.let {
                                        QueuesWindow(it)
                                    }
                                    val batchDownloadSlot = appComponent.batchDownloadSlot.collectAsState().value
                                    batchDownloadSlot.child?.instance?.let {
                                        BatchDownloadWindow(it)
                                    }
                                    val editDownloadSlot = appComponent.editDownloadSlot.collectAsState().value
                                    editDownloadSlot.child?.instance?.let {
                                        EditDownloadWindow(it)
                                    }
                                    ShowAddDownloadDialogs(appComponent)
                                    ShowDownloadDialogs(appComponent)
                                    ShowCategoryDialogs(appComponent)
                                    ShowUpdaterDialog(appComponent.updater)
                                    ShowAboutDialog(appComponent)
                                    NewQueueDialog(appComponent)
                                    ShowMessageDialogs(appComponent)
                                    ShowOpenSourceLibraries(appComponent)
                                    ShowTranslators(appComponent)
                                    ConfirmExit(appComponent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProvideSizeUnits(
    component: AppComponent,
    content: @Composable () -> Unit,
) {
    ProvideSizeAndSpeedUnit(
        sizeUnitConfig = component.appRepository.sizeUnit.collectAsState().value,
        speedUnitConfig = component.appRepository.speedUnit.collectAsState().value,
        content = content
    )
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
                +requestExitAction
            }
        }
    )
}
