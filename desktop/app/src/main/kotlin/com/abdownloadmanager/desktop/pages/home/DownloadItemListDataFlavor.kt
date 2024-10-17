package com.abdownloadmanager.desktop.pages.home

import ir.amirab.downloader.monitor.IDownloadItemState
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File

val DownloadItemListDataFlavor = DataFlavor(
    IDownloadItemState::class.java,
    "Download Item"
)

class DownloadItemTransferable(
    val items: List<IDownloadItemState>,
) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(
            DataFlavor.javaFileListFlavor,
            DataFlavor.stringFlavor,
            DownloadItemListDataFlavor,
        )
    }

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
        return (flavor in arrayOf(
            DataFlavor.javaFileListFlavor,
            DataFlavor.stringFlavor,
            DownloadItemListDataFlavor,
        ))
    }

    override fun getTransferData(flavor: DataFlavor?): Any {
        return when (flavor) {
            DataFlavor.javaFileListFlavor -> items.map {
                File(it.folder, it.name)
            }

            DataFlavor.stringFlavor -> items.map {
                it.downloadLink
            }.joinToString("\n")

            DownloadItemListDataFlavor -> items
            else -> throw UnsupportedFlavorException(flavor)
        }
    }
}