package com.abdownloadmanager.shared.pages.home.queue

import androidx.compose.runtime.Stable
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.downloader.db.QueueModel
import ir.amirab.downloader.queue.DefaultQueueInfo
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.action.simpleAction
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Stable
class QueueActions(
    private val scope: CoroutineScope,
    private val queueManager: QueueManager,
    val mainQueueModel: QueueModel?,
    private val requestDelete: (QueueModel) -> Unit,
    private val requestEdit: (QueueModel) -> Unit,
    private val requestClearItems: (QueueModel) -> Unit,
    private val onRequestNewQueue: () -> Unit,
) {
    private val mainItemExists = MutableStateFlow(mainQueueModel != null)

    fun downloadQueueOrNull(): DownloadQueue? {
        val qId = mainQueueModel?.id ?: return null
        return runCatching {
            queueManager.getQueue(qId)
        }.getOrNull()
    }

    private inline fun useItem(
        block: (QueueModel) -> Unit,
    ) {
        mainQueueModel?.let(block)
    }

    val deleteAction = simpleAction(
        title = Res.string.delete.asStringSource(),
        icon = MyIcons.remove,
        checkEnable = MutableStateFlow(run {
            val item = mainQueueModel ?: return@run false
            item.id != DefaultQueueInfo.ID
        }),
        onActionPerformed = {
            scope.launch {
                useItem {
                    requestDelete(it)
                }
            }
        },
    )
    val editAction = simpleAction(
        title = Res.string.edit.asStringSource(),
        icon = MyIcons.settings,
        checkEnable = mainItemExists,
        onActionPerformed = {
            scope.launch {
                useItem {
                    requestEdit(it)
                }
            }
        },
    )
    val clearItems = simpleAction(
        title = Res.string.clear_queue_items.asStringSource(),
        icon = MyIcons.clear,
        checkEnable = mainItemExists,
        onActionPerformed = {
            scope.launch {
                useItem {
                    requestClearItems(it)
                }
            }
        },
    )

    val addQueueAction = simpleAction(
        title = Res.string.add_new_queue.asStringSource(),
        icon = MyIcons.add,
        onActionPerformed = {
            scope.launch {
                onRequestNewQueue()
            }
        },
    )

    val start = simpleAction(
        title = Res.string.start_queue.asStringSource(),
        icon = MyIcons.queueStart,
        checkEnable = run {
            downloadQueueOrNull()?.activeFlow?.mapStateFlow { !it }
                ?: MutableStateFlow(false)
        },
        onActionPerformed = {
            scope.launch {
                downloadQueueOrNull()?.start()
            }
        },
    )
    val stop = simpleAction(
        title = Res.string.stop_queue.asStringSource(),
        icon = MyIcons.queueStop,
        checkEnable = run {
            downloadQueueOrNull()?.activeFlow
                ?: MutableStateFlow(false)
        },
        onActionPerformed = {
            scope.launch {
                downloadQueueOrNull()?.stop()
            }
        },
    )

    val menu: List<MenuItem> = buildMenu {
        +start
        +stop
        separator()
        +editAction
        +deleteAction
        +clearItems
        separator()
        +addQueueAction
    }
}
