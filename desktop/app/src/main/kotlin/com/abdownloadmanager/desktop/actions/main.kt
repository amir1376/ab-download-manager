package com.abdownloadmanager.desktop.actions

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.shared.util.SharedConstants
import com.abdownloadmanager.desktop.di.Di
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.DesktopEntryCreator
import com.abdownloadmanager.desktop.utils.isAppInstalled
import com.abdownloadmanager.desktop.window.Browser
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.action.simpleAction
import com.abdownloadmanager.shared.util.getIcon
import com.abdownloadmanager.shared.util.getName
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.action.createCheckForUpdateAction
import com.abdownloadmanager.shared.action.createDownloadFromClipboardAction
import com.abdownloadmanager.shared.action.createNewDownloadAction
import com.abdownloadmanager.shared.action.createNewQueueAction
import com.abdownloadmanager.shared.action.createOpenAboutPage
import com.abdownloadmanager.shared.action.createOpenBatchDownloadAction
import com.abdownloadmanager.shared.action.createOpenOpenSourceThirdPartyLibrariesPage
import com.abdownloadmanager.shared.action.createOpenQueuesAction
import com.abdownloadmanager.shared.action.createOpenSettingsAction
import com.abdownloadmanager.shared.action.createOpenTranslatorsPageAction
import com.abdownloadmanager.shared.action.createPerHostSettingsPage
import com.abdownloadmanager.shared.action.createRequestExitAction
import com.abdownloadmanager.shared.action.createStartQueueGroupAction
import com.abdownloadmanager.shared.action.createStopQueueGroupAction
import ir.amirab.downloader.queue.activeQueuesFlow
import ir.amirab.util.URLOpener
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.desktop.PlatformAppActivator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.koin.core.component.get

private val appComponent = Di.get<AppComponent>()
private val scope = Di.get<CoroutineScope>()
private val downloadSystem = appComponent.downloadSystem

private val activeQueuesFlow = downloadSystem
    .queueManager
    .activeQueuesFlow(scope)
    .stateIn(
        scope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

// desktop
val stopAllAction = createDesktopStopAllAction(scope, downloadSystem, appComponent, activeQueuesFlow)
val newDownloadAction = createNewDownloadAction(appComponent)
val newDownloadFromClipboardAction = createDownloadFromClipboardAction(appComponent)
val createDesktopEntryAction = simpleAction(
    Res.string.create_desktop_entry.asStringSource(),
    MyIcons.applicationFile,
    checkEnable = MutableStateFlow(AppInfo.isAppInstalled())
) {
    DesktopEntryCreator.createLinuxDesktopEntry()
}
val showDownloadList = simpleAction(
    Res.string.show_downloads.asStringSource(),
    MyIcons.download,
) {
    PlatformAppActivator.active()
    appComponent.openHome()
}
val browserIntegrations = MenuItem.SubMenu(
    title = Res.string.download_browser_integration.asStringSource(),
    icon = MyIcons.download,
    items = buildMenu {
        for (browserExtension in SharedConstants.browserIntegrations) {
            item(
                title = browserExtension.type.getName().asStringSource(),
                icon = browserExtension.type.getIcon(),
                onClick = {
                    val browser = Browser.getBrowserByType(browserExtension.type)
                    val success = browser?.openLink(browserExtension.url) == true
                    if (!success) {
                        URLOpener.openUrl(browserExtension.url)
                    }
                }
            )
        }
    }
)


// commonUsage but with desktop implementations
val newQueueAction = createNewQueueAction(scope, appComponent)
val openQueuesAction = createOpenQueuesAction(appComponent)
val openTranslators = createOpenTranslatorsPageAction(appComponent)
val openAboutAction = createOpenAboutPage(appComponent)
val checkForUpdateAction = createCheckForUpdateAction(appComponent.updater)
val gotoSettingsAction = createOpenSettingsAction(appComponent)
val perHostSettings = createPerHostSettingsPage(appComponent)
val requestExitAction = createRequestExitAction(scope, appComponent)
val startQueueGroupAction = createStartQueueGroupAction(scope, appComponent.downloadSystem.queueManager)
val stopQueueGroupAction = createStopQueueGroupAction(scope, activeQueuesFlow)
val batchDownloadAction = createOpenBatchDownloadAction(appComponent)
val openOpenSourceThirdPartyLibraries = createOpenOpenSourceThirdPartyLibrariesPage(appComponent)
