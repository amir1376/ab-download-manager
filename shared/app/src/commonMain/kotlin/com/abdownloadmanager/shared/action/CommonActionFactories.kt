package com.abdownloadmanager.shared.action

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pagemanager.AboutPageManager
import com.abdownloadmanager.shared.pagemanager.AddDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.BatchDownloadPageManager
import com.abdownloadmanager.shared.pagemanager.EnterNewURLDialogManager
import com.abdownloadmanager.shared.pagemanager.ExitApplicationRequestManager
import com.abdownloadmanager.shared.pagemanager.NewQueuePageManager
import com.abdownloadmanager.shared.pagemanager.OpenSourceLibrariesPageManager
import com.abdownloadmanager.shared.pagemanager.PerHostSettingsPageManager
import com.abdownloadmanager.shared.pagemanager.QueuePageManager
import com.abdownloadmanager.shared.pagemanager.SettingsPageManager
import com.abdownloadmanager.shared.pagemanager.TranslatorsPageManager
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.updater.UpdateComponent
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.SharedConstants
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.extractors.linkextractor.DownloadCredentialFromStringExtractor
import com.abdownloadmanager.shared.util.extractors.linkextractor.DownloadCredentialsFromCurl
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.queue.inactiveQueuesFlow
import ir.amirab.util.URLOpener
import ir.amirab.util.compose.action.AnAction
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.action.simpleAction
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.combineStateFlows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


fun createNewDownloadAction(
    enterNewURLDialogManager: EnterNewURLDialogManager,
): AnAction {
    return simpleAction(
        Res.string.new_download.asStringSource(),
        MyIcons.add,
    ) {
        enterNewURLDialogManager.openEnterNewURLWindow()
    }
}
fun createDownloadFromClipboardAction(
    addDownloadDialogManager: AddDownloadDialogManager,
): AnAction {
    return simpleAction(
        Res.string.import_from_clipboard.asStringSource(),
        MyIcons.paste,
    ) {
        val contentsInClipboard = ClipboardUtil.read()
        if (contentsInClipboard.isNullOrEmpty()) {
            return@simpleAction
        }
        val curlItems = DownloadCredentialsFromCurl.extract(contentsInClipboard)
        if (curlItems.isNotEmpty()) {
            addDownloadDialogManager.openAddDownloadDialog(
                curlItems.map {
                    AddDownloadCredentialsInUiProps(it)
                }
            )
            return@simpleAction
        }
        val items: List<IDownloadCredentials> = DownloadCredentialFromStringExtractor
            .extract(contentsInClipboard)
            .distinctBy { it.link }
        if (items.isEmpty()) {
            return@simpleAction
        }
        addDownloadDialogManager.openAddDownloadDialog(items.map {
            AddDownloadCredentialsInUiProps(it)
        })
    }
}

fun createOpenBatchDownloadAction(
    batchDownloadPageManager: BatchDownloadPageManager
): AnAction {
    return simpleAction(
        title = Res.string.batch_download.asStringSource(),
        icon = MyIcons.download
    ) {
        batchDownloadPageManager.openBatchDownloadPage()
    }
}


fun createStopQueueGroupAction(
    scope: CoroutineScope,
    activeQueuesFlow: StateFlow<List<DownloadQueue>>
): MenuItem.SubMenu {
    return MenuItem.SubMenu(
        icon = MyIcons.queueStop,
        title = Res.string.stop_queue.asStringSource(),
        items = emptyList()
    ).apply {
        activeQueuesFlow
            .onEach {
                setItems(it.map {
                    createStopQueueAction(scope, it)
                })
            }.launchIn(scope)
    }
}
fun createStartQueueGroupAction(
    scope: CoroutineScope,
    queueManager: QueueManager,
): MenuItem.SubMenu {
    return MenuItem.SubMenu(
        icon = MyIcons.queueStart,
        title = Res.string.start_queue.asStringSource(),
        items = emptyList()
    ).apply {
        queueManager
            .inactiveQueuesFlow(scope)
            .onEach {
                setItems(it.map {
                    createStartQueueAction(scope, it)
                })
            }.launchIn(scope)
    }
}

// ui exit
fun createRequestExitAction(
    scope: CoroutineScope,
    exitAppRequestManager: ExitApplicationRequestManager
): AnAction {
    return simpleAction(
        Res.string.exit.asStringSource(),
        MyIcons.exit,
    ) {
        scope.launch { exitAppRequestManager.requestExitApp() }
    }
}


fun createPerHostSettingsPage(
    perHostSettingsPageManager: PerHostSettingsPageManager
): AnAction {
    return simpleAction(
        Res.string.settings_per_host_settings.asStringSource(),
        MyIcons.earth,
    ) {
        perHostSettingsPageManager.openPerHostSettings(null)
    }
}


fun createOpenSettingsAction(
    settingsPageManager: SettingsPageManager
): AnAction {
    return simpleAction(
        Res.string.settings.asStringSource(),
        MyIcons.settings,
    ) {
        settingsPageManager.openSettings()
    }
}

fun createCheckForUpdateAction(
    updaterComponent: UpdateComponent
): AnAction {
    return simpleAction(
        title = Res.string.update_check_for_update.asStringSource(),
        icon = MyIcons.refresh,
        checkEnable = MutableStateFlow(
            updaterComponent.isUpdateSupported()
        )
    ) {
        updaterComponent.requestCheckForUpdate()
    }
}


fun createOpenAboutPage(aboutPageManager: AboutPageManager): AnAction {
    return simpleAction(
        title = Res.string.about.asStringSource(),
        icon = MyIcons.info,
    ) {
        aboutPageManager.openAboutPage()
    }
}

fun createOpenOpenSourceThirdPartyLibrariesPage(
    openSourceLibrariesPageManager: OpenSourceLibrariesPageManager,
): AnAction {
    return simpleAction(
        title = Res.string.view_the_open_source_licenses.asStringSource(),
        icon = MyIcons.openSource,
    ) {
        openSourceLibrariesPageManager.openOpenSourceLibrariesPage()
    }
}

fun createOpenTranslatorsPageAction(
    opeTranslatorsPageManager: TranslatorsPageManager,
): AnAction {
    return simpleAction(
        title = Res.string.meet_the_translators.asStringSource(),
        icon = MyIcons.language,
    ) {
        opeTranslatorsPageManager.openTranslatorsPage()
    }
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
            URLOpener.openUrl(SharedConstants.projectWebsite)
        }
        item(Res.string.source_code.asStringSource(), MyIcons.openSource) {
            URLOpener.openUrl(SharedConstants.projectSourceCode)
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

fun createOpenQueuesAction(
    queuePageManager: QueuePageManager
): AnAction {
    return simpleAction(
        title = Res.string.queues.asStringSource(),
        icon = MyIcons.queue
    ) {
        queuePageManager.openQueues()
    }
}


fun createMoveToQueueAction(
    scope: CoroutineScope,
    downloadSystem: DownloadSystem,
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
    scope: CoroutineScope,
    downloadSystem: DownloadSystem,
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

fun createStopQueueAction(
    scope: CoroutineScope,
    queue: DownloadQueue,
): AnAction {
    return simpleAction(queue.getQueueModel().name.asStringSource()) {
        scope.launch {
            queue.stop()
        }
    }
}

fun createStartQueueAction(
    scope: CoroutineScope,
    queue: DownloadQueue,
): AnAction {
    return simpleAction(queue.getQueueModel().name.asStringSource()) {
        scope.launch {
            queue.start()
        }
    }
}

fun createNewQueueAction(
    scope: CoroutineScope,
    queuePageManager: NewQueuePageManager,
): AnAction {
    return simpleAction(Res.string.add_new_queue.asStringSource()) {
        scope.launch {
            queuePageManager.openNewQueueDialog()
        }
    }
}
fun createStopAllAction(
    scope: CoroutineScope,
    downloadSystem: DownloadSystem,
    extraJobs: () -> Unit,
    activeQueuesFlow: StateFlow<List<DownloadQueue>>
): AnAction {
    return simpleAction(
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
            downloadSystem.stopAnything()
            extraJobs()
        }
    }
}
