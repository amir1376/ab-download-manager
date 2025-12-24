package com.abdownloadmanager.shared.util.ui.icon

import com.abdownloadmanager.resources.icons.ABDMIcons
import com.abdownloadmanager.resources.icons.*
import com.abdownloadmanager.shared.util.ui.BaseMyColors
import ir.amirab.util.compose.IconSource

object MyIcons : BaseMyColors() {
    override val appIcon = ABDMIcons.AppIcon.asIconSource("appIcon", false)

    override val settings = ABDMIcons.Settings.asIconSource("settings")
    override val flag = ABDMIcons.Flag.asIconSource("flag")
    override val fast = ABDMIcons.Fast.asIconSource("fast")
    override val search = ABDMIcons.Search.asIconSource("search")
    override val info = ABDMIcons.Info.asIconSource("info")
    override val check = ABDMIcons.Check.asIconSource("check")
    override val link = ABDMIcons.AddLink.asIconSource("link")
    override val download = ABDMIcons.DownSpeed.asIconSource("download")
    override val permission = ABDMIcons.Permission.asIconSource("permission")

    override val windowMinimize = ABDMIcons.WindowMinimize.asIconSource("windowMinimize")
    override val windowFloating = ABDMIcons.WindowFloating.asIconSource("windowFloating")
    override val windowMaximize = ABDMIcons.WindowMaximize.asIconSource("windowMaximize")
    override val windowClose = ABDMIcons.WindowClose.asIconSource("windowClose")

    override val exit = ABDMIcons.Exit.asIconSource("exit")
    override val edit = ABDMIcons.Edit.asIconSource("edit")
    override val undo = ABDMIcons.Undo.asIconSource("undo")

    override val openSource = ABDMIcons.OpenSource.asIconSource("openSource")
    override val telegram = ABDMIcons.Telegram.asIconSource("telegram", false)
    override val speaker = ABDMIcons.Speaker.asIconSource("speaker")
    override val group = ABDMIcons.Group.asIconSource("group")

    override val browserMozillaFirefox = ABDMIcons.BrowserMozillaFirefox.asIconSource("browserMozillaFirefox", false)
    override val browserGoogleChrome = ABDMIcons.BrowserGoogleChrome.asIconSource("browserGoogleChrome", false)
    override val browserMicrosoftEdge = ABDMIcons.BrowserMicrosoftEdge.asIconSource("browserMicrosoftEdge", false)
    override val browserOpera = ABDMIcons.BrowserOpera.asIconSource("browserOpera", false)

    override val next = ABDMIcons.Next.asIconSource("next")
    override val back = ABDMIcons.Back.asIconSource("back")
    override val up = ABDMIcons.Up.asIconSource("up")
    override val down = ABDMIcons.Down.asIconSource("down")

    override val activeCount = ABDMIcons.List.asIconSource("activeCount")
    override val speed = ABDMIcons.DownSpeed.asIconSource("speed")

    override val resume = ABDMIcons.Resume.asIconSource("resume")
    override val pause = ABDMIcons.Pause.asIconSource("pause")
    override val stop = ABDMIcons.Stop.asIconSource("stop")

    override val queue = ABDMIcons.Queue.asIconSource("queue")
    override val queueStart = ABDMIcons.QueueStart.asIconSource("queueStart")
    override val queueStop = ABDMIcons.QueueStop.asIconSource("queueStop")

    override val remove = ABDMIcons.Delete.asIconSource("remove")
    override val clear = ABDMIcons.Clear.asIconSource("clear")
    override val add = ABDMIcons.Plus.asIconSource("add")
    override val minus = ABDMIcons.Minus.asIconSource("add")
    override val paste = ABDMIcons.Clipboard.asIconSource("paste")

    override val copy = ABDMIcons.Copy.asIconSource("copy")
    override val refresh = ABDMIcons.Refresh.asIconSource("refresh")
    override val editFolder = ABDMIcons.Folder.asIconSource("editFolder")

    override val share = ABDMIcons.Share.asIconSource("share")
    override val file = ABDMIcons.File.asIconSource("file")
    override val folder = ABDMIcons.Folder.asIconSource("folder")

    override val fileOpen = file
    override val folderOpen = folder
    override val pictureFile = ABDMIcons.FilePicture.asIconSource("fileOpen")
    override val musicFile = ABDMIcons.FileMusic.asIconSource("folderOpen")
    override val zipFile = ABDMIcons.FileZip.asIconSource("pictureFile")
    override val videoFile = ABDMIcons.FileVideo.asIconSource("musicFile")
    override val applicationFile = ABDMIcons.FileApplication.asIconSource("zipFile")
    override val documentFile = ABDMIcons.FileDocument.asIconSource("videoFile")
    override val otherFile = ABDMIcons.FileUnknown.asIconSource("applicationFile")

    override val lock = ABDMIcons.Lock.asIconSource("lock")
    override val question = ABDMIcons.QuestionMark.asIconSource("question")

    override val grip = ABDMIcons.Grip.asIconSource("grip")
    override val sortUp = ABDMIcons.Sort123.asIconSource("sortUp")
    override val sortDown = ABDMIcons.Sort321.asIconSource("sortDown")
    override val verticalDirection = ABDMIcons.VerticalDirection.asIconSource("verticalDirection")

    override val browserIntegration = ABDMIcons.Earth.asIconSource("browserIntegration")
    override val appearance = ABDMIcons.Colors.asIconSource("appearance")
    override val downloadEngine = ABDMIcons.DownSpeed.asIconSource("downloadEngine")
    override val network = ABDMIcons.Network.asIconSource("network")
    override val language = ABDMIcons.Language.asIconSource("language")

    override val externalLink = ABDMIcons.ExternalLink.asIconSource("externalLink")
    override val earth = ABDMIcons.Earth.asIconSource("earth")
    override val hearth = ABDMIcons.Hearth.asIconSource("hearth")
    override val dragAndDrop = ABDMIcons.DragAndDrop.asIconSource("dragAndDrop")


    override val selectAll = ABDMIcons.SelectAll.asIconSource("selectAll")
    override val selectInside = ABDMIcons.SelectInside.asIconSource("selectInside")
    override val selectInvert = ABDMIcons.SelectInvert.asIconSource("selectInvert")

    override val menu = ABDMIcons.Menu.asIconSource("menu")

    override val close: IconSource = ABDMIcons.Clear.asIconSource("close")

    override val data: IconSource = ABDMIcons.Data.asIconSource("alphabet")
    override val alphabet: IconSource = ABDMIcons.Alphabet.asIconSource("alphabet")
    override val clock: IconSource = ABDMIcons.Clock.asIconSource("clock")
}
