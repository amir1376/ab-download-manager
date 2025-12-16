package com.abdownloadmanager.shared.downloaderinui.hls

import com.abdownloadmanager.shared.downloaderinui.BasicDownloadItem
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUi
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.hls.add.HLSDownloadUIChecker
import com.abdownloadmanager.shared.downloaderinui.hls.add.HLSNewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.hls.edit.HLSEditDownloadChecker
import com.abdownloadmanager.shared.downloaderinui.hls.edit.HLSEditDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.http.edit.EditDownloadChecker
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.hls.HLSDownloadCredentials
import ir.amirab.downloader.downloaditem.hls.HLSDownloadItem
import ir.amirab.downloader.downloaditem.hls.HLSDownloadJob
import ir.amirab.downloader.downloaditem.hls.HLSDownloader
import ir.amirab.downloader.downloaditem.hls.HLSResponseInfo
import ir.amirab.downloader.downloaditem.hls.IHLSCredentials
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class HLSDownloaderInUi(
    downloader: HLSDownloader,
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider,
) : DownloaderInUi<
        HLSDownloadCredentials,
        HLSResponseInfo,
        HLSLinkChecker,
        HLSDownloadItem,
        HLSNewDownloadInputs,
        HLSEditDownloadInputs,
        HlsItemToCredentialMapper,
        HLSDownloadJob,
        HLSDownloader
        >(downloader) {
    override fun newDownloadUiChecker(
        initialCredentials: HLSDownloadCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope
    ): HLSDownloadUIChecker {
        return HLSDownloadUIChecker(
            initCredentials = initialCredentials,
            linkCheckerFactory = this,
            initialFolder = initialFolder,
            initialName = initialName,
            downloadSystem = downloadSystem,
            scope = scope,
        )
    }

    override fun acceptDownloadCredentials(item: IDownloadCredentials): Boolean {
        return item is IHLSCredentials
    }

    override fun supportsThisLink(link: String): Boolean {
        return HttpUrlUtils.isValidUrl(link)
    }

    override fun createMinimumCredentials(link: String): HLSDownloadCredentials {
        return HLSDownloadCredentials(link = link)
    }

    override fun createBareDownloadItem(
        credentials: HLSDownloadCredentials,
        basicDownloadItem: BasicDownloadItem
    ): HLSDownloadItem {
        return HLSDownloadItem.createWithCredentials(
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
        downloadJob: HLSDownloadJob,
        speed: Long
    ): ProcessingDownloadItemState {
        return UiProcessingItemForHSLFactory.create(
            downloadJob,
            speed,
        )
    }

    override val name: StringSource = "HLS".asStringSource()

    override fun createLinkChecker(initialCredentials: HLSDownloadCredentials): HLSLinkChecker {
        return HLSLinkChecker(
            credentials = initialCredentials,
            client = downloader.client
        )
    }

    override fun createEditDownloadChecker(
        currentDownloadItem: MutableStateFlow<HLSDownloadItem>,
        editedDownloadItem: MutableStateFlow<HLSDownloadItem>,
        linkChecker: HLSLinkChecker,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope
    ): EditDownloadChecker<HLSDownloadItem, HLSDownloadCredentials, HLSResponseInfo, HLSLinkChecker> {
        return HLSEditDownloadChecker(
            currentDownloadItem = currentDownloadItem,
            editedDownloadItem = editedDownloadItem,
            linkChecker = linkChecker,
            conflictDetector = conflictDetector,
            scope = scope,
        )
    }

    override fun createNewDownloadInputs(
        initialCredentials: HLSDownloadCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope
    ): HLSNewDownloadInputs {
        return HLSNewDownloadInputs(
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
        currentDownloadItem: MutableStateFlow<HLSDownloadItem>,
        editedDownloadItem: MutableStateFlow<HLSDownloadItem>,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope
    ): HLSEditDownloadInputs {
        return HLSEditDownloadInputs(
            currentDownloadItem = currentDownloadItem,
            editedDownloadItem = editedDownloadItem,
            mapper = HlsItemToCredentialMapper(),
            conflictDetector = conflictDetector,
            scope = scope,
            linkCheckerFactory = this,
            editDownloadCheckerFactory = this,
            sizeAndSpeedUnitProvider = sizeAndSpeedUnitProvider,
        )
    }
}

