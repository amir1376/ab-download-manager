package com.abdownloadmanager.desktop.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.abdownloadmanager.shared.utils.FileIconProvider
import com.abdownloadmanager.shared.utils.ui.IMyIcons
import ir.amirab.util.compose.IconSource
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.filechooser.FileSystemView

class OSFileIconProvider(
    private val icons: IMyIcons
) : FileIconProvider {
    private val registeredIcons = mutableMapOf<String, ImageBitmap>()
    private val lock = Any()
    private fun getIconOfFileExtension(
        extension: String,
    ): ImageBitmap? {
        val imageBitmap = registeredIcons[extension]
        if (imageBitmap != null) {
            return imageBitmap
        } else {
            synchronized(lock) {
                val bitmapFoundInSync = registeredIcons[extension]
                if (bitmapFoundInSync != null) {
                    return bitmapFoundInSync
                }
                val w = 24
                val h = 24
                return runCatching {
                    val fileSystemView = FileSystemView.getFileSystemView()
                    val file = File.createTempFile("file", "file.$extension")
                    val icon = fileSystemView.getSystemIcon(file, w, h)
                    bufferedImageToImageBitmap(iconToImage(icon))
                }.onSuccess {
                    registeredIcons[extension] = it
                }.getOrNull()
            }
        }
    }

    private fun iconToImage(icon: javax.swing.Icon): BufferedImage {
        val width = icon.iconWidth
        val height = icon.iconHeight
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        icon.paintIcon(null, graphics, 0, 0)
        graphics.dispose()
        return image
    }

    private fun bufferedImageToImageBitmap(bufferedImage: BufferedImage): ImageBitmap {
        return bufferedImage.toComposeImageBitmap()
    }

    override fun getIcon(fileName: String): IconSource {
        val extension = fileName.substringAfterLast('.', "")
        val imageBitmap = getIconOfFileExtension(extension)
            ?: return icons.file
        return IconSource.PainterIconSource(
            BitmapPainter(imageBitmap),
            false
        )
    }

    @Composable
    override fun rememberIcon(fileName: String): IconSource {
        return getIcon(fileName)
    }
}
