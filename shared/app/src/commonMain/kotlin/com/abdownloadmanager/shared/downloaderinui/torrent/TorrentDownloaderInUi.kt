package com.abdownloadmanager.shared.downloaderinui.torrent

import com.abdownloadmanager.shared.downloaderinui.BasicDownloadItem
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUi
import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.torrent.add.TorrentNewDownloadUIChecker
import com.abdownloadmanager.shared.downloaderinui.torrent.add.TorrentNewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.torrent.edit.TorrentEditDownloadChecker
import com.abdownloadmanager.shared.downloaderinui.torrent.edit.TorrentEditDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadChecker
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloadCredentials
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloadItem
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloadJob
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloader
import ir.amirab.downloader.downloaditem.torrent.TorrentResponseInfo
import ir.amirab.downloader.downloaditem.torrent.TorrentLinks
import ir.amirab.downloader.downloaditem.torrent.ITorrentCredentials
import ir.amirab.downloader.monitor.ProcessingDownloadItemFactoryInputs
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class TorrentDownloaderInUi(
    downloader: TorrentDownloader,
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider,
) : DownloaderInUi<
        TorrentDownloadCredentials,
        TorrentResponseInfo,
        DownloadSize.Duration,
        TorrentLinkChecker,
        TorrentDownloadItem,
        TorrentNewDownloadInputs,
        TorrentEditDownloadInputs,
        TorrentItemToCredentialMapper,
        TorrentDownloadJob,
        TorrentDownloader
        >(downloader) {
    override fun newDownloadUiChecker(
        initialCredentials: TorrentDownloadCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope
    ): TorrentNewDownloadUIChecker {
        return TorrentNewDownloadUIChecker(
            initCredentials = initialCredentials,
            linkCheckerFactory = this,
            initialFolder = initialFolder,
            initialName = initialName,
            downloadSystem = downloadSystem,
            scope = scope,
        )
    }

    override fun acceptDownloadCredentials(item: IDownloadCredentials): Boolean {
        return item is ITorrentCredentials
    }

    override fun supportsThisLink(link: String): Boolean {
        return TorrentLinks.isSupported(link)
    }

    override fun createMinimumCredentials(link: String): TorrentDownloadCredentials {
        return TorrentDownloadCredentials(link = link)
    }

    override fun createBareDownloadItem(
        credentials: TorrentDownloadCredentials,
        basicDownloadItem: BasicDownloadItem
    ): TorrentDownloadItem {
        return TorrentDownloadItem.createWithCredentials(
            id = -1,
            credentials = credentials,
            folder = basicDownloadItem.folder,
            name = basicDownloadItem.name,
            contentLength = basicDownloadItem.contentLength,
            preferredConnectionCount = basicDownloadItem.preferredConnectionCount,
            speedLimit = basicDownloadItem.speedLimit,
            fileChecksum = basicDownloadItem.fileChecksum,
        )
    }

    override fun createProcessingDownloadItemState(
        props: ProcessingDownloadItemFactoryInputs<TorrentDownloadJob>
    ): ProcessingDownloadItemState {
        return UiProcessingItemForTorrentFactory.create(
            props,
        )
    }

    override val name: StringSource = "Torrent".asStringSource()

    override fun createLinkChecker(initialCredentials: TorrentDownloadCredentials): TorrentLinkChecker {
        return TorrentLinkChecker(
            credentials = initialCredentials,
            client = downloader.client
        )
    }

    override fun createEditDownloadChecker(
        currentDownloadItem: MutableStateFlow<TorrentDownloadItem>,
        editedDownloadItem: MutableStateFlow<TorrentDownloadItem>,
        linkChecker: TorrentLinkChecker,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope
    ): EditDownloadChecker<TorrentDownloadItem, TorrentDownloadCredentials, TorrentResponseInfo, DownloadSize.Duration, TorrentLinkChecker> {
        return TorrentEditDownloadChecker(
            currentDownloadItem = currentDownloadItem,
            editedDownloadItem = editedDownloadItem,
            linkChecker = linkChecker,
            conflictDetector = conflictDetector,
            scope = scope,
        )
    }

    override fun createNewDownloadInputs(
        initialCredentials: TorrentDownloadCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope
    ): TorrentNewDownloadInputs {
        return TorrentNewDownloadInputs(
            newDownloadUiChecker(
                initialCredentials = initialCredentials,
                initialFolder = initialFolder,
                initialName = initialName,
                downloadSystem = downloadSystem,
                scope = scope,
            ),
            sizeAndSpeedUnitProvider,
            scope,
        )
    }

    override fun createEditDownloadInputs(
        currentDownloadItem: MutableStateFlow<TorrentDownloadItem>,
        editedDownloadItem: MutableStateFlow<TorrentDownloadItem>,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope
    ): TorrentEditDownloadInputs {
        return TorrentEditDownloadInputs(
            currentDownloadItem = currentDownloadItem,
            editedDownloadItem = editedDownloadItem,
            mapper = TorrentItemToCredentialMapper(),
            conflictDetector = conflictDetector,
            scope = scope,
            linkCheckerFactory = this,
            editDownloadCheckerFactory = this,
            sizeAndSpeedUnitProvider = sizeAndSpeedUnitProvider,
        )
    }
}

