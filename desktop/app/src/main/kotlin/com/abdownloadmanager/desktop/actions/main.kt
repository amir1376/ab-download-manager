package com.abdownloadmanager.desktop.actions

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.SharedConstants
import com.abdownloadmanager.desktop.di.Di
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.ClipboardUtil
import ir.amirab.util.compose.action.AnAction
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.action.simpleAction
import com.abdownloadmanager.desktop.utils.getIcon
import com.abdownloadmanager.desktop.utils.getName
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.category.Category
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.activeQueuesFlow
import ir.amirab.downloader.queue.inactiveQueuesFlow
import com.abdownloadmanager.utils.extractors.linkextractor.DownloadCredentialFromStringExtractor
import ir.amirab.util.UrlUtils
import ir.amirab.util.compose.asStringSource
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
    val items = DownloadCredentialFromStringExtractor
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
    icon = MyIcons.stop,
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
    icon = MyIcons.resume,
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
        downloadSystem.stopAnything()
    }
}.apply {
}

// ui exit
val requestExitAction = simpleAction(
    Res.string.exit.asStringSource(),
    MyIcons.exit,
) {
    scope.launch { appComponent.requestExitApp() }
}

val browserIntegrations = MenuItem.SubMenu(
    title = Res.string.download_browser_integration.asStringSource(),
    icon = MyIcons.download,
    items = buildMenu {
        for (browserExtension in SharedConstants.browserIntegrations) {
            item(
                title = browserExtension.type.getName().asStringSource(),
                icon = browserExtension.type.getIcon(),
                onClick = { UrlUtils.openUrl(browserExtension.url) }
            )
        }
    }
)

val gotoSettingsAction = simpleAction(
    Res.string.settings.asStringSource(),
    MyIcons.settings,
) {
    appComponent.openSettings()
}
val showDownloadList = simpleAction(
    Res.string.show_downloads.asStringSource(),
    MyIcons.settings,
) {
    appComponent.openHome()
}

val checkForUpdateAction = simpleAction(
    title = Res.string.update_check_for_update.asStringSource(),
    icon = MyIcons.refresh,
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

val supportActionGroup = MenuItem.SubMenu(
    title = Res.string.support_and_community.asStringSource(),
    icon = MyIcons.group,
    items = buildMenu {
        item(Res.string.website.asStringSource(), MyIcons.appIcon) {
            UrlUtils.openUrl(AppInfo.website)
        }
        item(Res.string.source_code.asStringSource(), MyIcons.openSource) {
            UrlUtils.openUrl(AppInfo.sourceCode)
        }
        subMenu(Res.string.telegram.asStringSource(), MyIcons.telegram) {
            item(Res.string.channel.asStringSource(), MyIcons.speaker) {
                UrlUtils.openUrl(SharedConstants.telegramChannelUrl)
            }
            item(Res.string.group.asStringSource(), MyIcons.group) {
                UrlUtils.openUrl(SharedConstants.telegramGroupUrl)
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