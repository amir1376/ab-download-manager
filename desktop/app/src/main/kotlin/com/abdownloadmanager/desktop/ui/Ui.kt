package com.abdownloadmanager.desktop.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.AppEffects
import com.abdownloadmanager.desktop.actions.gotoSettingsAction
import com.abdownloadmanager.desktop.actions.requestExitAction
import com.abdownloadmanager.desktop.actions.showDownloadList
import com.abdownloadmanager.desktop.pages.about.ShowAboutDialog
import com.abdownloadmanager.desktop.pages.addDownload.ShowAddDownloadDialogs
import com.abdownloadmanager.desktop.pages.batchdownload.BatchDownloadWindow
import com.abdownloadmanager.desktop.pages.category.ShowCategoryDialogs
import com.abdownloadmanager.desktop.pages.confirmexit.ConfirmExit
import com.abdownloadmanager.desktop.pages.credits.translators.ShowTranslators
import com.abdownloadmanager.desktop.pages.editdownload.EditDownloadWindow
import com.abdownloadmanager.desktop.pages.enterurl.EnterNewDownloadWindow
import com.abdownloadmanager.desktop.pages.extenallibs.ShowOpenSourceLibraries
import com.abdownloadmanager.desktop.pages.checksum.FileChecksumWindow
import com.abdownloadmanager.desktop.pages.home.HomeWindow
import com.abdownloadmanager.desktop.pages.newQueue.NewQueueDialog
import com.abdownloadmanager.desktop.pages.perhostsettings.PerHostSettingsWindow
import com.abdownloadmanager.desktop.pages.queue.QueuesWindow
import com.abdownloadmanager.desktop.pages.settings.FontManager
import com.abdownloadmanager.desktop.pages.settings.SettingWindow
import com.abdownloadmanager.shared.ui.theme.ThemeManager
import com.abdownloadmanager.desktop.pages.poweractionalert.PowerActionAlert
import com.abdownloadmanager.desktop.pages.singleDownloadPage.ShowDownloadDialogs
import com.abdownloadmanager.desktop.pages.updater.ShowUpdaterDialog
import com.abdownloadmanager.desktop.ui.configurable.comon.CommonConfigurableRenderersForDesktop
import com.abdownloadmanager.desktop.ui.configurable.platform.PlatformConfigurableRenderersForDesktop
import com.abdownloadmanager.desktop.ui.widget.Tray
import com.abdownloadmanager.desktop.ui.widget.ShowMessageDialogs
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.GlobalAppExceptionHandler
import com.abdownloadmanager.desktop.utils.ProvideGlobalExceptionHandler
import com.abdownloadmanager.desktop.utils.isInDebugMode
import com.abdownloadmanager.shared.ui.ProvideCommonSettings
import com.abdownloadmanager.shared.ui.ProvideSizeUnits
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRendererRegistry
import com.abdownloadmanager.shared.ui.theme.ABDownloaderTheme
import com.abdownloadmanager.shared.ui.widget.NotificationManager
import com.abdownloadmanager.shared.ui.widget.ProvideLanguageManager
import com.abdownloadmanager.shared.ui.widget.ProvideNotificationManager
import com.abdownloadmanager.shared.ui.widget.useNotification
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.ui.ProvideDebugInfo
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.desktop.PlatformDockToggler
import ir.amirab.util.desktop.mac.event.MacEventHandler
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isMac
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
        val fontManager: FontManager = get()
        val languageManager: LanguageManager = get()
        val notificationManager: NotificationManager = get()
        themeManager.boot()
        fontManager.boot()
        languageManager.boot()
        if (!appArguments.startSilent) {
            appComponent.openHome()
        }
        if (Platform.isMac()) {
            MacEventHandler.configure(
                onClickIcon = appComponent::activateHomeIfNotOpen,
                onAboutClick = {
                    appComponent.showAboutPage.value = true
                },
                onSettingsClick = appComponent::openSettings,
                onQuit = {
                    scope.launch { appComponent.requestExitApp() }
                }
            )
        }
        application {
            ProvideLocalProviders(
                languageManager = languageManager,
                appComponent = appComponent,
                themeManager = themeManager,
                fontManager = fontManager,
                globalAppExceptionHandler = globalAppExceptionHandler,
                notificationManager = notificationManager,
            ) {
                HandleEffectsForApp(appComponent)
                SystemTray(appComponent)
                val showHomeSlot =
                    appComponent.showHomeSlot.collectAsState().value
                showHomeSlot.child?.instance?.let {
                    HomeWindow(it, appComponent::closeHome)
                }
                val showSettingSlot =
                    appComponent.showSettingSlot.collectAsState().value
                showSettingSlot.child?.instance?.let {
                    SettingWindow(it, appComponent::closeSettings)
                }
                val showQueuesSlot =
                    appComponent.showQueuesSlot.collectAsState().value
                showQueuesSlot.child?.instance?.let {
                    QueuesWindow(it)
                }
                val batchDownloadSlot =
                    appComponent.batchDownloadSlot.collectAsState().value
                batchDownloadSlot.child?.instance?.let {
                    BatchDownloadWindow(it)
                }
                val editDownloadSlot =
                    appComponent.editDownloadSlot.collectAsState().value
                editDownloadSlot.child?.instance?.let {
                    EditDownloadWindow(it)
                }
                EnterNewDownloadWindow(appComponent)
                ShowAddDownloadDialogs(appComponent)
                ShowDownloadDialogs(appComponent)
                ShowCategoryDialogs(appComponent)
                FileChecksumWindow(appComponent)
                ShowUpdaterDialog(appComponent.updater)
                ShowAboutDialog(appComponent)
                NewQueueDialog(appComponent)
                ShowMessageDialogs(appComponent)
                ShowOpenSourceLibraries(appComponent)
                ShowTranslators(appComponent)
                ConfirmExit(appComponent)
                PowerActionAlert(appComponent)
                PerHostSettingsWindow(appComponent)
            }
        }
    }
}

@Composable
private fun ProvideLocalProviders(
    languageManager: LanguageManager,
    themeManager: ThemeManager,
    fontManager: FontManager,
    appComponent: AppComponent,
    notificationManager: NotificationManager,
    globalAppExceptionHandler: GlobalAppExceptionHandler,
    content: @Composable () -> Unit
) {
    val theme by themeManager.currentThemeColor.collectAsState()
    val fontFamily by fontManager.currentFontFamily.collectAsState()
    val configurableRendererRegistry = remember {
        ConfigurableRendererRegistry {
            listOf(
                PlatformConfigurableRenderersForDesktop,
                CommonConfigurableRenderersForDesktop,
            ).forEach {
                it.getAllRenderers().forEach { (key, renderer) ->
                    this.register(key, renderer)
                }
            }
        }
    }
    ProvideDebugInfo(AppInfo.isInDebugMode()) {
        ProvideLanguageManager(languageManager) {
            ProvideCommonSettings(
                appSettings = appComponent.appSettings,
                configurableRendererRegistry = configurableRendererRegistry,
                iconProvider = appComponent.iconFromUriResolver
            ) {
                ProvideNotificationManager(notificationManager) {
                    ABDownloaderTheme(
                        myColors = theme,
                        fontFamily = fontFamily,
                        uiScale = appComponent.uiScale.collectAsState().value
                    ) {
                        ProvideGlobalExceptionHandler(globalAppExceptionHandler) {
                            ProvideSizeUnits(appComponent.appRepository) {
                                content()
                            }
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
) {
    val useSystemTray by component.useSystemTray.collectAsState()
    if (useSystemTray) {
        LaunchedEffect(Unit) { PlatformDockToggler.hide() }
        val menu = remember {
            buildMenu {
                +showDownloadList
                +gotoSettingsAction
                +requestExitAction
            }
        }
        Tray(
            icon = MyIcons.appIcon,
            tooltip = AppInfo.displayName,
            primaryAction = { showDownloadList.onClick() },
            menu = menu,
        )
    } else {
        LaunchedEffect(Unit) { PlatformDockToggler.show() }
    }
}
