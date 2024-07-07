package com.abdownloadmanager.desktop.actions

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.di.Di
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.utils.ClipboardUtil
import com.abdownloadmanager.desktop.utils.action.AnAction
import com.abdownloadmanager.desktop.utils.action.MenuItem
import com.abdownloadmanager.desktop.utils.action.simpleAction
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.activeQueuesFlow
import ir.amirab.downloader.queue.inactiveQueuesFlow
import com.abdownloadmanager.utils.extractors.linkextractor.DownloadCredentialFromStringExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.get

private val appComponent = Di.get<AppComponent>()
private val scope = Di.get<CoroutineScope>()
private val downloadSystem = appComponent.downloadSystem

val newDownloadAction = simpleAction(
    "New Download",
    MyIcons.add,
) {
    appComponent.openAddDownloadDialog(listOf(DownloadCredentials.empty()))
}
val newDownloadFromClipboardAction = simpleAction(
    "Import from clipboard",
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

val stopQueueGroupAction = MenuItem.SubMenu(
    icon = MyIcons.stop,
    title = "Stop Queue",
    items = emptyList()
).apply {
    appComponent.downloadSystem.queueManager
        .activeQueuesFlow(scope)
        .onEach {
            setItems(it.map {
                stopQueueAction(it)
            })
        }.launchIn(scope)
}


val startQueueGroupAction = MenuItem.SubMenu(
    icon = MyIcons.resume,
    title = "Start Queue",
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


val stopAction = simpleAction("Stop All", MyIcons.stop) {
    scope.launch {
        downloadSystem.stopAnything()
    }
}.apply {
    downloadSystem.downloadMonitor.activeDownloadCount
        .onEach {
            setEnabled( it > 0)
        }.launchIn(scope)
}

val exitAction = simpleAction(
    "Exit",
    MyIcons.exit,
) {
    appComponent.requestClose()
}
val gotoSettingsAction = simpleAction(
    "Settings",
    MyIcons.settings,
) {
    appComponent.openSettings()
}
val showDownloadList = simpleAction(
    "Show Downloads",
    MyIcons.settings,
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
    title = "About",
    icon = MyIcons.info,
) {
    appComponent.openAbout()
}
val openOpenSourceThirdPartyLibraries = simpleAction(
    title = "View OpenSource Libraries",
    icon = MyIcons.openSource,
) {
    appComponent.openOpenSourceLibraries()
}
val openQueuesAction = simpleAction(
    title = "Open Queues",
    icon = MyIcons.queue
) {
    appComponent.openQueues()
}

fun moveToQueueAction(
    queue: DownloadQueue,
    itemId: List<Long>,
): AnAction {
    return simpleAction(queue.getQueueModel().name) {
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

fun stopQueueAction(
    queue: DownloadQueue,
): AnAction {
    return simpleAction(queue.getQueueModel().name) {
        scope.launch {
            queue.stop()
        }
    }
}

fun startQueueAction(
    queue: DownloadQueue,
): AnAction {
    return simpleAction(queue.getQueueModel().name) {
        scope.launch {
            queue.start()
        }
    }
}

val newQueueAction = simpleAction("New Queue") {
    scope.launch {
        appComponent.openNewQueueDialog()
    }
}