package com.abdownloadmanager.desktop.ui.icon

import com.abdownloadmanager.utils.compose.IMyIcons
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.utils.compose.asIconSource

object MyIcons : IMyIcons {
    override val appIcon: IconSource get() = "icons/app_icon.svg".asIconSource(false)

    override val settings get() = "icons/settings.svg".asIconSource()
    override val search get() = "icons/search.svg".asIconSource()
    override val info get() = "icons/info.svg".asIconSource()
    override val check get() = "icons/check.svg".asIconSource()
    override val link get() = "icons/add_link.svg".asIconSource()
    override val download get() = "icons/down_speed.svg".asIconSource()

    override val windowMinimize get() = "icons/window_minimize.svg".asIconSource()
    override val windowFloating get() = "icons/window_floating.svg".asIconSource()
    override val windowMaximize get() = "icons/window_maximize.svg".asIconSource()
    override val windowClose get() = "icons/window_close.svg".asIconSource()

    override val exit get() = "icons/exit.svg".asIconSource()
    override val undo get() = "icons/undo.svg".asIconSource()

    override val openSource: IconSource get() = "icons/open_source.svg".asIconSource()
    override val telegram: IconSource get() = "icons/telegram.svg".asIconSource(false)
    override val speaker: IconSource get() = "icons/speaker.svg".asIconSource()
    override val group: IconSource get() = "icons/group.svg".asIconSource()


    override val browserMozillaFirefox: IconSource get() = "icons/browser_mozilla_firefox.svg".asIconSource(false)
    override val browserGoogleChrome: IconSource get() = "icons/browser_google_chrome.svg".asIconSource(false)
    override val browserMicrosoftEdge: IconSource get() = "icons/browser_microsoft_edge.svg".asIconSource(false)
    override val browserOpera: IconSource get() = "icons/browser_opera.svg".asIconSource(false)

//    override val menu get() = TablerIcons.Menu.asIconSource()
//    override val menuClose get() = TablerIcons.X.asIconSource()


    override val next get() = "icons/next.svg".asIconSource()
//    override val back get() = TablerIcons.ChevronLeft.asIconSource()
    override val back get() = "icons/back.svg".asIconSource()
    override val up get() = "icons/up.svg".asIconSource()
    override val down get() = "icons/down.svg".asIconSource()

    override val activeCount get() = "icons/list.svg".asIconSource()
    override val speed get() = "icons/down_speed.svg".asIconSource()


    override val resume get() = "icons/resume.svg".asIconSource()
    override val pause get() = "icons/pause.svg".asIconSource()
    override val stop get() = "icons/stop.svg".asIconSource()

    override val queue get() = "icons/queue.svg".asIconSource()

    override val remove get() = "icons/delete.svg".asIconSource()
    override val clear get() = "icons/clear.svg".asIconSource()
    override val add get() = "icons/plus.svg".asIconSource()
    override val paste get() = "icons/clipboard.svg".asIconSource()

    override val copy get() = "icons/copy.svg".asIconSource()
    override val refresh get() = "icons/refresh.svg".asIconSource()
    override val editFolder get() = "icons/folder.svg".asIconSource()

    override val share get() = "icons/share.svg".asIconSource()
    override val file get() = "icons/file.svg".asIconSource()
    override val folder get() = "icons/folder.svg".asIconSource()

    override val fileOpen get() = file
    override val folderOpen get() = folder
    override val pictureFile get() = "icons/file_picture.svg".asIconSource()
    override val musicFile get() = "icons/file_music.svg".asIconSource()
    override val zipFile get() = "icons/file_zip.svg".asIconSource()
    override val videoFile get() = "icons/file_video.svg".asIconSource()
    override val applicationFile get() = "icons/file_application.svg".asIconSource()
    override val documentFile get() = "icons/file_document.svg".asIconSource()
    override val otherFile get() = "icons/file_unknown.svg".asIconSource()

    override val lock get() = "icons/lock.svg".asIconSource()

    override val question get() = "icons/question_mark.svg".asIconSource()

    override val sortUp get() = "icons/sort_321.svg".asIconSource()
    override val sortDown get() = "icons/sort_123.svg".asIconSource()
    override val verticalDirection get() = "icons/vertical_direction.svg".asIconSource()

    override val browserIntegration: IconSource get() = "icons/earth.svg".asIconSource()
    override val appearance: IconSource get() = "icons/color.svg".asIconSource()
    override val downloadEngine: IconSource get() = "icons/down_speed.svg".asIconSource()
    override val network: IconSource get() = "icons/network.svg".asIconSource()

    override val externalLink: IconSource get() = "icons/external_link.svg".asIconSource()
}
