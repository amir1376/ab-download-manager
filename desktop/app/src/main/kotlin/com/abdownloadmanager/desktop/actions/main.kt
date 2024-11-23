package com.abdownloadmanager.desktop.actions

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.SharedConstants
import com.abdownloadmanager.desktop.di.Di
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.colored.AppIcon
import com.abdownloadmanager.desktop.ui.icons.colored.Telegram
import com.abdownloadmanager.desktop.ui.icons.default.Clipboard
import com.abdownloadmanager.desktop.ui.icons.default.DownSpeed
import com.abdownloadmanager.desktop.ui.icons.default.Exit
import com.abdownloadmanager.desktop.ui.icons.default.Group
import com.abdownloadmanager.desktop.ui.icons.default.Info
import com.abdownloadmanager.desktop.ui.icons.default.Language
import com.abdownloadmanager.desktop.ui.icons.default.OpenSource
import com.abdownloadmanager.desktop.ui.icons.default.Plus
import com.abdownloadmanager.desktop.ui.icons.default.Queue
import com.abdownloadmanager.desktop.ui.icons.default.Resume
import com.abdownloadmanager.desktop.ui.icons.default.Settings
import com.abdownloadmanager.desktop.ui.icons.default.Speaker
import com.abdownloadmanager.desktop.ui.icons.default.Stop
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
    title = Res.string.new_download.asStringSource(),
    icon = AbIcons.Default.Plus,
) {
    appComponent.openAddDownloadDialog(listOf(DownloadCredentials.empty()))
}
val newDownloadFromClipboardAction = simpleAction(
    title = Res.string.import_from_clipboard.asStringSource(),
    icon = AbIcons.Default.Clipboard,
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
    icon = AbIcons.Default.DownSpeed
) {
    appComponent.openBatchDownload()
}
val stopQueueGroupAction = MenuItem.SubMenu(
    icon = AbIcons.Default.Stop,
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
    icon = AbIcons.Default.Resume,
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
    title = Res.string.stop_all.asStringSource(),
    icon = AbIcons.Default.Stop,
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
    title = Res.string.exit.asStringSource(),
    icon = AbIcons.Default.Exit,
) {
    scope.launch { appComponent.requestExitApp() }
}

val browserIntegrations = MenuItem.SubMenu(
    title = Res.string.download_browser_integration.asStringSource(),
    icon = AbIcons.Default.DownSpeed,
    items = buildMenu {
        for (browserExtension in SharedConstants.browserIntegrations) {
            item(
                title = browserExtension.type.getName().asStringSource(),
                image = browserExtension.type.getIcon(),
                onClick = { UrlUtils.openUrl(browserExtension.url) }
            )
        }
    }
)

val gotoSettingsAction = simpleAction(
    title = Res.string.settings.asStringSource(),
    icon = AbIcons.Default.Settings,
) {
    appComponent.openSettings()
}
val showDownloadList = simpleAction(
    title = Res.string.show_downloads.asStringSource(),
    icon = AbIcons.Default.Settings,
) {
    appComponent.openHome()
}

/*val checkForUpdateAction = simpleAction(
    title = "Check For Update",
    icon = MyIcons.refresh,
) {
    appComponent.updater.requestCheckForUpdate()
}*/
val openAboutAction = simpleAction(
    title = Res.string.about.asStringSource(),
    icon = AbIcons.Default.Info,
) {
    appComponent.openAbout()
}
val openOpenSourceThirdPartyLibraries = simpleAction(
    title = Res.string.view_the_open_source_licenses.asStringSource(),
    icon = AbIcons.Default.OpenSource,
) {
    appComponent.openOpenSourceLibraries()
}
val openTranslators = simpleAction(
    title = Res.string.meet_the_translators.asStringSource(),
    icon = AbIcons.Default.Language,
) {
    appComponent.openTranslatorsPage()
}

val supportActionGroup = MenuItem.SubMenu(
    title = Res.string.support_and_community.asStringSource(),
    icon = AbIcons.Default.Group,
    items = buildMenu {
        item(title = Res.string.website.asStringSource(), image = AbIcons.Colored.AppIcon) {
            UrlUtils.openUrl(AppInfo.website)
        }
        item(title = Res.string.source_code.asStringSource(), icon = AbIcons.Default.OpenSource) {
            UrlUtils.openUrl(AppInfo.sourceCode)
        }
        subMenu(title = Res.string.telegram.asStringSource(), image = AbIcons.Colored.Telegram) {
            item(title = Res.string.channel.asStringSource(), icon = AbIcons.Default.Speaker) {
                UrlUtils.openUrl(SharedConstants.telegramChannelUrl)
            }
            item(title = Res.string.group.asStringSource(), icon = AbIcons.Default.Group) {
                UrlUtils.openUrl(SharedConstants.telegramGroupUrl)
            }
        }
    }
)

val openQueuesAction = simpleAction(
    title = Res.string.queues.asStringSource(),
    icon = AbIcons.Default.Queue
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