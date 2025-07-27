package com.abdownloadmanager.shared.utils.ui

import androidx.compose.ui.graphics.vector.ImageVector
import ir.amirab.util.compose.IconSource

context (_: IMyIcons)
fun ImageVector.asIconSource(requiredTint: Boolean = true) = IconSource.VectorIconSource(this, requiredTint)

context (_: IMyIcons)
fun String.asIconSource(requiredTint: Boolean = true) = IconSource.ResourceIconSource(this, requiredTint)

interface IMyIcons {
    val appIcon: IconSource

    val settings: IconSource
    val flag: IconSource
    val fast: IconSource
    val search: IconSource
    val info: IconSource
    val check: IconSource
    val link: IconSource
    val download: IconSource
    val windowMinimize: IconSource
    val windowFloating: IconSource
    val windowMaximize: IconSource
    val windowClose: IconSource
    val exit: IconSource
    val edit: IconSource
    val undo: IconSource
//    val menu: IconSource
//    val menuClose: IconSource

    val openSource: IconSource
    val telegram: IconSource
    val speaker: IconSource
    val group: IconSource

    //browser icons
    val browserMozillaFirefox: IconSource
    val browserGoogleChrome: IconSource
    val browserMicrosoftEdge: IconSource
    val browserOpera: IconSource

    val next: IconSource
    val back: IconSource
    val up: IconSource
    val down: IconSource
    val activeCount: IconSource
    val speed: IconSource
    val resume: IconSource
    val pause: IconSource
    val stop: IconSource
    val queue: IconSource
    val queueStart: IconSource
    val queueStop: IconSource
    val remove: IconSource
    val clear: IconSource
    val add: IconSource
    val paste: IconSource
    val copy: IconSource
    val refresh: IconSource
    val editFolder: IconSource
    val share: IconSource
    val file: IconSource
    val folder: IconSource
    val fileOpen: IconSource
    val folderOpen: IconSource
    val pictureFile: IconSource
    val musicFile: IconSource
    val zipFile: IconSource
    val videoFile: IconSource
    val applicationFile: IconSource
    val documentFile: IconSource
    val otherFile: IconSource
    val lock: IconSource
    val question: IconSource
    val sortUp: IconSource
    val sortDown: IconSource
    val verticalDirection: IconSource
    val appearance: IconSource
    val downloadEngine: IconSource
    val browserIntegration: IconSource
    val network: IconSource
    val language: IconSource
    val externalLink: IconSource

    val earth: IconSource
    val hearth: IconSource
    val dragAndDrop: IconSource
}
