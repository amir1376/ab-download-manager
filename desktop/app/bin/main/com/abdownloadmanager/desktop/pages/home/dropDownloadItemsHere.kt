package com.abdownloadmanager.desktop.pages.home

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import ir.amirab.downloader.monitor.IDownloadItemState

internal fun Modifier.dropDownloadItemsHere(
    onDragIn: () -> Unit,
    onDragDone: () -> Unit,
    onItemsDropped: (ids: List<Long>) -> Unit,
): Modifier {
    return composed {
        val onDragIn by rememberUpdatedState(onDragIn)
        val onDragDone by rememberUpdatedState(onDragDone)
        val onItemsDropped by rememberUpdatedState(onItemsDropped)
        dragAndDropTarget(
            shouldStartDragAndDrop = {
                it.awtTransferable.isDataFlavorSupported(DownloadItemListDataFlavor)
            },
            target = remember {
                object : DragAndDropTarget {
                    override fun onEntered(event: DragAndDropEvent) {
                        onDragIn()
                    }

                    override fun onExited(event: DragAndDropEvent) {
                        onDragDone()
                    }

                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        onDragDone()
                        val items = (event.awtTransferable.getTransferData(DownloadItemListDataFlavor) as List<*>)
                            .filterIsInstance<IDownloadItemState>()
                        onItemsDropped(items.map { it.id })
                        return true
                    }
                }
            }
        )
    }
}
