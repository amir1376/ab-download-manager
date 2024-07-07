package com.abdownloadmanager.desktop.utils.externaldraggable

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.DataFlavor.selectBestTextFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import java.io.File

internal fun Transferable.dragData(): AvailableDragData {
    val o = mutableListOf<DragData>()
    if (isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        o.add(DragDataFilesListImpl(this))
    }
    if (isDataFlavorSupported(DataFlavor.imageFlavor)) {
        o.add(DragDataImageImpl(this))
    }
    selectBestTextFlavor(transferDataFlavors)?.let {
        o.add(DragDataTextImpl(it, this))
    }
    return AvailableDragData(o)
}


private class DragDataFilesListImpl(
    private val transferable: Transferable
) : DragData.FilesList {
    override fun readFiles(): List<String> {
        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
        return files.filterIsInstance<File>().map { it.toURI().toString() }
    }
}

private class DragDataImageImpl(
    private val transferable: Transferable
) : DragData.Image {
    override fun readImage(): Painter {
        return (transferable.getTransferData(DataFlavor.imageFlavor) as Image).painter()
    }

    private fun Image.painter(): Painter {
        if (this is BufferedImage) {
            return this.toPainter()
        }
        val bufferedImage =
                BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB)

        val g2 = bufferedImage.createGraphics()
        try {
            g2.drawImage(this, 0, 0, null)
        } finally {
            g2.dispose()
        }

        return bufferedImage.toPainter()
    }
}

private class DragDataTextImpl(
    private val bestTextFlavor: DataFlavor,
    private val transferable: Transferable
) : DragData.Text {
    override val bestMimeType: String = bestTextFlavor.mimeType

    override fun readText(): String {
        val reader = bestTextFlavor.getReaderForText(transferable)
        return reader.readText()
    }
}