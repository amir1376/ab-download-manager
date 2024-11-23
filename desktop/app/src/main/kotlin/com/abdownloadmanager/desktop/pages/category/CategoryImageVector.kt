package com.abdownloadmanager.desktop.pages.category

import androidx.compose.ui.graphics.vector.ImageVector
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.File
import com.abdownloadmanager.desktop.ui.icons.filetype.FileApplication
import com.abdownloadmanager.desktop.ui.icons.filetype.FileDocument
import com.abdownloadmanager.desktop.ui.icons.filetype.FileMusic
import com.abdownloadmanager.desktop.ui.icons.filetype.FilePicture
import com.abdownloadmanager.desktop.ui.icons.filetype.FileVideo
import com.abdownloadmanager.desktop.ui.icons.filetype.FileZip
import com.abdownloadmanager.utils.category.Category
import com.abdownloadmanager.utils.category.CategoryIconType
import com.abdownloadmanager.utils.category.CategoryIconType.ApplicationFile
import com.abdownloadmanager.utils.category.CategoryIconType.DocumentFile
import com.abdownloadmanager.utils.category.CategoryIconType.MusicFile
import com.abdownloadmanager.utils.category.CategoryIconType.Other
import com.abdownloadmanager.utils.category.CategoryIconType.PictureFile
import com.abdownloadmanager.utils.category.CategoryIconType.VideoFile
import com.abdownloadmanager.utils.category.CategoryIconType.ZipFile

fun Category?.toCategoryImageVector(): ImageVector? {
    val type = this?.iconType ?: return null

    return type.toCategoryImageVector()
}

fun CategoryIconType.toCategoryImageVector(): ImageVector {
    return when (this) {
        ZipFile -> AbIcons.FileType.FileZip
        ApplicationFile -> AbIcons.FileType.FileApplication
        VideoFile -> AbIcons.FileType.FileVideo
        MusicFile -> AbIcons.FileType.FileMusic
        PictureFile -> AbIcons.FileType.FilePicture
        DocumentFile -> AbIcons.FileType.FileDocument
        Other -> AbIcons.Default.File
    }
}

fun ImageVector?.toCategoryIconType(): CategoryIconType {
    return when (this) {
        AbIcons.FileType.FileZip -> ZipFile
        AbIcons.FileType.FileApplication -> ApplicationFile
        AbIcons.FileType.FileVideo -> VideoFile
        AbIcons.FileType.FileMusic -> MusicFile
        AbIcons.FileType.FilePicture -> PictureFile
        AbIcons.FileType.FileDocument -> DocumentFile
        else -> error("Unknown icon filetype")
    }
}