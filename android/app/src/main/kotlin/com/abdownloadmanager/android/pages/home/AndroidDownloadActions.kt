package com.abdownloadmanager.android.pages.home

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pagemanager.DownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EditDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.FileChecksumDialogManager
import com.abdownloadmanager.shared.pages.home.AbstractDownloadActions
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.xeton.downloader.downloaditem.DownloadJobStatus
import com.xeton.downloader.monitor.CompletedDownloadItemState
import com.xeton.downloader.monitor.IDownloadItemState
import com.xeton.downloader.monitor.statusOrFinished
import com.xeton.downloader.queue.QueueManager
import com.xeton.util.compose.action.buildMenu
import com.xeton.util.compose.action.simpleAction
import com.xeton.util.compose.asStringSource
import com.xeton.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AndroidDownloadActions(
    scope: CoroutineScope,
    downloadSystem: DownloadSystem,
    downloadDialogManager: DownloadDialogManager,
    editDownloadDialogManager: EditDownloadDialogManager,
    fileChecksumDialogManager: FileChecksumDialogManager,
    selections: StateFlow<List<IDownloadItemState>>,
    mainItem: StateFlow<Long?>,
    queueManager: QueueManager,
    categoryManager: CategoryManager,
    openFile: (Long) -> Unit,
    requestDelete: (List<Long>) -> Unit,
    onRequestShareFiles: (ids: List<CompletedDownloadItemState>) -> Unit,
) : AbstractDownloadActions(
    scope = scope,
    downloadSystem = downloadSystem,
    downloadDialogManager = downloadDialogManager,
    editDownloadDialogManager = editDownloadDialogManager,
    fileChecksumDialogManager = fileChecksumDialogManager,
    selections = selections,
    mainItem = mainItem,
    queueManager = queueManager,
    categoryManager = categoryManager,
    openFile = openFile,
    requestDelete = requestDelete,
) {
    val shareAction = simpleAction(
        title = Res.string.share.asStringSource(),
        icon = MyIcons.share,
        checkEnable = selections.mapStateFlow { list ->
            list.any { it.statusOrFinished() is DownloadJobStatus.Finished }
        },
        onActionPerformed = {
            scope.launch {
                onRequestShareFiles(selections.value.filterIsInstance<CompletedDownloadItemState>())
            }
        }
    )
    private val mainOptions = buildMenu {
        +resumeAction
        +pauseAction
        +deleteAction
        +openDownloadDialogAction
    }
    private val extraMenu = buildMenu {
        +openFileAction
        +shareAction
        separator()
        +reDownloadAction
        separator()
        +moveToQueueItems
        +moveToCategoryAction
        separator()
        subMenu(Res.string.copy.asStringSource(), MyIcons.copy) {
            +(copyDownloadLinkAction)
            +(copyDownloadCredentialsAsJsonAction)
            +(copyDownloadCredentialsAsCurlAction)
        }
        +editDownloadAction
        +fileChecksumAction
    }
    val androidMenu = buildMenu {
        mainOptions.forEach {
            +it
        }
        subMenu(
            title = Res.string.more_options.asStringSource(),
            icon = MyIcons.menu,
        ) {
            extraMenu.forEach { +it }
        }
    }
}
