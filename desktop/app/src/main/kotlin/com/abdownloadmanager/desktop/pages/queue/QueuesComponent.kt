package com.abdownloadmanager.desktop.pages.queue

import com.abdownloadmanager.desktop.actions.newQueueAction
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.desktop.utils.mvi.ContainsEffects
import com.abdownloadmanager.desktop.utils.mvi.supportEffects
import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.navigate
import ir.amirab.downloader.monitor.DownloadMonitor
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
sealed interface QueuesComponentEffects{
    data object ToFront:QueuesComponentEffects
}
class QueuesComponent(
    ctx: ComponentContext,
    val close: () -> Unit,
) : BaseComponent(ctx),
    ContainsEffects<QueuesComponentEffects> by supportEffects(),
    KoinComponent {
    val queueManager: QueueManager by inject()
    val downloadMonitor: DownloadMonitor by inject()
    private val queues = queueManager.queues


    val queuesState by queues.asState(scope)
    var selectedItemIndex by mutableStateOf(0)
    fun getNearest(lastIndex: Int): Int {
        return lastIndex.coerceIn(queuesState.indices)
    }

    val selectedItem by derivedStateOf {
        queuesState.get(getNearest(selectedItemIndex))
    }

    fun onQueueSelected(queueId: Long) {
        val foundIndex = queuesState.indexOfFirst {
            it.id == queueId
        }
        if (foundIndex == -1) {
            return
        }
        selectedItemIndex = foundIndex
    }

    fun addQueue() {
        newQueueAction()
//        scope.launch {
//            queueManager.addQueue("New Queue")
//        }
    }

    fun canDeleteThisQueue(queueId: Long): Boolean {
        return queueManager.canDelete(queueId)
    }

    fun requestDeleteQueue(id: Long) {
        scope.launch {
            queueManager.deleteQueue(id)
        }
    }

    fun bringToFront() {
        sendEffect(QueuesComponentEffects.ToFront)
    }

    init {
        queues.map {
            it.size
        }
            .distinctUntilChanged()
            .onEach {
                selectedItemIndex = getNearest(selectedItemIndex)
            }.launchIn(scope)
    }

    data class QueueInfoNavigationConfig(
        val queueId:Long,
    )
    val queueInfoNavigation = SlotNavigation<QueueInfoNavigationConfig>()
    val queueInfoComponent = childSlot(
        queueInfoNavigation,
        serializer = null,
        initialConfiguration = {
            QueueInfoNavigationConfig(selectedItem.id)
        },
        childFactory = {config,ctx->
            QueueInfoComponent(ctx,config.queueId)
        }
    ).subscribeAsStateFlow()

    init {
        snapshotFlow {
            selectedItem
        }.onEach {q->
            queueInfoNavigation.navigate {
                if (it?.queueId==q.id){
                    it
                }else{
                    QueueInfoNavigationConfig(
                        q.id
                    )
                }
            }
        }.launchIn(scope)

    }

}