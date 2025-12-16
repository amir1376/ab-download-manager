package com.abdownloadmanager.shared.util.category

import com.abdownloadmanager.shared.util.ui.IMyIcons
import ir.amirab.util.compose.IconSource
import java.io.File

class DefaultCategories(
    private val icons: IMyIcons,
    private val getDefaultDownloadFolder: () -> String,
) {

    fun getCategoryOfFileName(name: String): Category? {
        return getDefaultCategories()
            .firstOrNull { it.acceptFileName(name) }
    }

    fun getDefaultCategories(): List<Category> {
        fun IconSource.toUri(): String {
            return requireNotNull(uri) {
                "It seems that we use an icon that does not have uri"
            }
        }

        fun relative(path: String): String {
            return File(getDefaultDownloadFolder(), path).path
        }

        val compressed = Category(
            id = 0,
            name = "Compressed",
            path = relative("Compressed"),
            icon = icons.zipFile.toUri(),
            acceptedFileTypes = listOf(
                "zip",
                "rar",
                "7z",
                "tar",
                "gz",
                "bz2",
                "xz",
                "iso",
                "dmg",
                "tgz",
            ),
        )

        val programs = Category(
            id = 1,
            name = "Programs",
            path = relative("Programs"),
            icon = icons.applicationFile.toUri(),
            acceptedFileTypes = listOf(
                "apk",
                "exe",
                "msi",
                "bat",
                "sh",
                "jar",
                "app",
                "deb",
                "rpm",
                "bin",
            ),
        )
        val videos = Category(
            id = 2,
            name = "Videos",
            path = relative("Videos"),
            icon = icons.videoFile.toUri(),
            acceptedFileTypes = listOf(
                "mp4",
                "avi",
                "mkv",
                "mov",
                "wmv",
                "flv",
                "webm",
                "m4v",
                "3gp",
                "mpeg",
                "ts",
            ),
        )

        val music = Category(
            id = 3,
            name = "Music",
            path = relative("Music"),
            icon = icons.musicFile.toUri(),
            acceptedFileTypes = listOf(
                "mp3",
                "wav",
                "aac",
                "flac",
                "ogg",
                "aiff",
                "wma",
                "m4a",
            ),
        )

        val pictures = Category(
            id = 4,
            name = "Pictures",
            path = relative("Pictures"),
            icon = icons.pictureFile.toUri(),
            acceptedFileTypes = listOf(
                "jpg",
                "jpeg",
                "png",
                "gif",
                "bmp",
                "tiff",
                "tif",
                "svg",
                "webp",
                "heic",
                "ico",
                "raw",
                "psd",
            ),
        )
        val documents = Category(
            id = 5,
            name = "Documents",
            path = relative("Documents"),
            icon = icons.documentFile.toUri(),
            acceptedFileTypes = listOf(
                "doc",
                "docx",
                "pdf",
                "txt",
                "rtf",
                "odt",
                "xls",
                "xlsx",
                "ppt",
                "pptx",
                "csv",
                "epub",
                "pages",
            ),
        )
        return listOf(
            compressed,
            programs,
            videos,
            music,
            pictures,
            documents,
        )
    }

    fun isDefault(categories: List<Category>): Boolean {
        return getDefaultCategories() == categories.map {
            it.copy(items = emptyList())
        }
    }
}
