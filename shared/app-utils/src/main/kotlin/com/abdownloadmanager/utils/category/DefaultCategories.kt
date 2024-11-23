package com.abdownloadmanager.utils.category

import com.abdownloadmanager.utils.category.CategoryIconType.*
import java.io.File

class DefaultCategories(private val getDefaultDownloadFolder: () -> String) {

    fun getCategoryOfFileName(name: String): Category? {
        return getDefaultCategories()
            .firstOrNull { it.acceptFileName(name) }
    }

    fun getDefaultCategories(): List<Category> {
        fun relative(path: String): String {
            return File(getDefaultDownloadFolder(), path).path
        }

        val compressed = Category(
            id = 0,
            name = "Compressed",
            path = relative("Compressed"),
            iconType = ZipFile,
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
            iconType = ApplicationFile,
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
            iconType = VideoFile,
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
            ),
        )

        val music = Category(
            id = 3,
            name = "Music",
            path = relative("Music"),
            iconType = MusicFile,
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
            iconType = PictureFile,
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
            iconType = DocumentFile,
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
