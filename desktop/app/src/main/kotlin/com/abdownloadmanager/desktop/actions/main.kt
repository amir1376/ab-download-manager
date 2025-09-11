package com.abdownloadmanager.desktop.actions

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.SharedConstants
import com.abdownloadmanager.desktop.di.Di
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.ClipboardUtil
import com.abdownloadmanager.desktop.utils.DesktopEntryCreator
import com.abdownloadmanager.desktop.utils.isAppInstalled
import com.abdownloadmanager.desktop.window.Browser
import ir.amirab.util.compose.action.AnAction
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.action.simpleAction
import com.abdownloadmanager.shared.utils.getIcon
import com.abdownloadmanager.shared.utils.getName
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.category.Category
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.activeQueuesFlow
import ir.amirab.downloader.queue.inactiveQueuesFlow
import com.abdownloadmanager.shared.utils.extractors.linkextractor.DownloadCredentialFromStringExtractor
import com.abdownloadmanager.shared.utils.extractors.linkextractor.DownloadCredentialsFromCurl
import ir.amirab.util.URLOpener
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.desktop.PlatformAppActivator
import ir.amirab.util.flow.combineStateFlows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

val newDownloadAction = simpleAction(
    Res.string.new_download.asStringSource(),
    MyIcons.add,
) {
    appComponent.openAddDownloadDialog(listOf(DownloadCredentials.empty()))
}
val newDownloadFromClipboardAction = simpleAction(
    Res.string.import_from_clipboard.asStringSource(),
    MyIcons.paste,
) {
    val contentsInClipboard = ClipboardUtil.read()
    if (contentsInClipboard.isNullOrEmpty()) {
        return@simpleAction
    }
    val curlItems = DownloadCredentialsFromCurl.extract(contentsInClipboard)
    if (curlItems.isNotEmpty()) {
        appComponent.openAddDownloadDialog(curlItems)
        return@simpleAction
    }
    val items: List<DownloadCredentials> = DownloadCredentialFromStringExtractor
        .extract(contentsInClipboard)
        .distinctBy { it.link }
    if (items.isEmpty()) {
        return@simpleAction
    }
    appComponent.openAddDownloadDialog(items)
}
val batchDownloadAction = simpleAction(
    title = Res.string.batch_download.asStringSource(),
    icon = MyIcons.download
) {
    appComponent.openBatchDownload()
}
val stopQueueGroupAction = MenuItem.SubMenu(
    icon = MyIcons.queueStop,
    title = Res.string.stop_queue.asStringSource(),
    items = emptyList()
).apply {
    activeQueuesFlow
        .onEach {
            setItems(it.map {
                stopQueueAction(it)
            })
        }.launchIn(scope)
}


val startQueueGroupAction = MenuItem.SubMenu(
    icon = MyIcons.queueStart,
    title = Res.string.start_queue.asStringSource(),
    items = emptyList()
).apply {
    appComponent.downloadSystem.queueManager
        .inactiveQueuesFlow(scope)
        .onEach {
            setItems(it.map {
                startQueueAction(it)
            })
        }.launchIn(scope)

}


val stopAllAction = simpleAction(
    Res.string.stop_all.asStringSource(),
    MyIcons.stop,
    checkEnable = combineStateFlows(
        downloadSystem.downloadMonitor.activeDownloadCount,
        activeQueuesFlow
    ) { downloadCount, activeQueues ->
        downloadCount > 0 || activeQueues.isNotEmpty()
    }
) {
    scope.launch {
        val activeDownloadIds = downloadSystem.downloadMonitor.activeDownloadListFlow.value.map { it.id }
        appComponent.closeDownloadDialog(*activeDownloadIds.toLongArray())
        downloadSystem.stopAnything()
    }
}

// ui exit
val requestExitAction = simpleAction(
    Res.string.exit.asStringSource(),
    MyIcons.exit,
) {
    scope.launch { appComponent.requestExitApp() }
}


val perHostSettings = simpleAction(
    Res.string.settings_per_host_settings.asStringSource(),
    MyIcons.earth,
) {
    scope.launch { appComponent.openPerHostSettings(null) }
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

val createDesktopEntryAction = simpleAction(
    Res.string.create_desktop_entry.asStringSource(),
    MyIcons.applicationFile,
    checkEnable = MutableStateFlow(AppInfo.isAppInstalled())
) {
    DesktopEntryCreator.createLinuxDesktopEntry()
}

val gotoSettingsAction = simpleAction(
    Res.string.settings.asStringSource(),
    MyIcons.settings,
) {
    appComponent.openSettings()
}
val showDownloadList = simpleAction(
    Res.string.show_downloads.asStringSource(),
    MyIcons.download,
) {
    PlatformAppActivator.active()
    appComponent.openHome()
}

val checkForUpdateAction = simpleAction(
    title = Res.string.update_check_for_update.asStringSource(),
    icon = MyIcons.refresh,
    checkEnable = MutableStateFlow(
        appComponent.updater.isUpdateSupported()
    )
) {
    appComponent.updater.requestCheckForUpdate()
}
val openAboutAction = simpleAction(
    title = Res.string.about.asStringSource(),
    icon = MyIcons.info,
) {
    appComponent.openAbout()
}
val openOpenSourceThirdPartyLibraries = simpleAction(
    title = Res.string.view_the_open_source_licenses.asStringSource(),
    icon = MyIcons.openSource,
) {
    appComponent.openOpenSourceLibraries()
}
val openTranslators = simpleAction(
    title = Res.string.meet_the_translators.asStringSource(),
    icon = MyIcons.language,
) {
    appComponent.openTranslatorsPage()
}

val donate = simpleAction(
    title = Res.string.donate.asStringSource(),
    icon = MyIcons.hearth,
) {
    URLOpener.openUrl(SharedConstants.donateLink)
}

val supportActionGroup = MenuItem.SubMenu(
    title = Res.string.support_and_community.asStringSource(),
    icon = MyIcons.group,
    items = buildMenu {
        item(Res.string.website.asStringSource(), MyIcons.appIcon) {
            URLOpener.openUrl(AppInfo.website)
        }
        item(Res.string.source_code.asStringSource(), MyIcons.openSource) {
            URLOpener.openUrl(AppInfo.sourceCode)
        }
        subMenu(Res.string.telegram.asStringSource(), MyIcons.telegram) {
            item(Res.string.channel.asStringSource(), MyIcons.speaker) {
                URLOpener.openUrl(SharedConstants.telegramChannelUrl)
            }
            item(Res.string.group.asStringSource(), MyIcons.group) {
                URLOpener.openUrl(SharedConstants.telegramGroupUrl)
            }
        }
    }
)

val openQueuesAction = simpleAction(
    title = Res.string.queues.asStringSource(),
    icon = MyIcons.queue
) {
    appComponent.openQueues()
}

fun moveToQueueAction(
    queue: DownloadQueue,
    itemId: List<Long>,
): AnAction {
    return simpleAction(queue.getQueueModel().name.asStringSource()) {
        scope.launch {
            downloadSystem
                .queueManager
                .addToQueue(
                    queueId = queue.id,
                    downloadIds = itemId,
                )
        }
    }
}

fun createMoveToCategoryAction(
    category: Category,
    itemIds: List<Long>,
): AnAction {
    return simpleAction(category.name.asStringSource()) {
        scope.launch {
            downloadSystem
                .categoryManager
                .addItemsToCategory(
                    categoryId = category.id,
                    itemIds = itemIds,
                )
        }
    }
}

fun stopQueueAction(
    queue: DownloadQueue,
): AnAction {
    return simpleAction(queue.getQueueModel().name.asStringSource()) {
        scope.launch {
            queue.stop()
        }
    }
}

fun startQueueAction(
    queue: DownloadQueue,
): AnAction {
    return simpleAction(queue.getQueueModel().name.asStringSource()) {
        scope.launch {
            queue.start()
        }
    }
}

val newQueueAction = simpleAction(Res.string.add_new_queue.asStringSource()) {
    scope.launch {
        appComponent.openNewQueueDialog()
    }
}
