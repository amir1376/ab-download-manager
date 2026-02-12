package com.abdownloadmanager.shared.downloaderinui.http

import com.abdownloadmanager.shared.downloaderinui.BasicDownloadItem
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUi
import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpDownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpLinkChecker
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpNewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.http.edit.HttpEditDownloadChecker
import com.abdownloadmanager.shared.downloaderinui.http.edit.HttpEditDownloadInputs
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadJob
import ir.amirab.downloader.downloaditem.http.HttpDownloader
import ir.amirab.downloader.downloaditem.http.IHttpDownloadCredentials
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemFactoryInputs
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.RangeBasedProcessingDownloadItemState
import ir.amirab.downloader.monitor.UiRangedPart
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class HttpDownloaderInUi(
    httpDownloader: HttpDownloader,
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider,
) : DownloaderInUi<HttpDownloadCredentials, HttpResponseInfo, DownloadSize.Bytes, HttpLinkChecker, HttpDownloadItem, HttpNewDownloadInputs, HttpEditDownloadInputs, HttpCredentialsToItemMapper, HttpDownloadJob, HttpDownloader>(
    downloader = httpDownloader
) {
    override fun createLinkChecker(initialCredentials: HttpDownloadCredentials): HttpLinkChecker {
        return HttpLinkChecker(
            initialCredentials,
            downloader.httpDownloaderClient,
        )
    }

    override fun newDownloadUiChecker(
        initialCredentials: HttpDownloadCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope,
    ): HttpDownloadUiChecker {
        return HttpDownloadUiChecker(
            initialCredentials = initialCredentials,
            linkCheckerFactory = this,
            initialFolder = initialFolder,
            initialName = initialName,
            downloadSystem = downloadSystem,
            scope = scope,
        )
    }

    override fun createNewDownloadInputs(
        initialCredentials: HttpDownloadCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope
    ): HttpNewDownloadInputs {
        val downloadUiChecker = newDownloadUiChecker(
            initialCredentials,
            initialFolder,
            initialName,
            downloadSystem,
            scope,
        )
        return HttpNewDownloadInputs(
            downloadUiChecker = downloadUiChecker,
            scope = scope,
            sizeAndSpeedUnitProvider = sizeAndSpeedUnitProvider
        )
    }

    override fun createEditDownloadInputs(
        currentDownloadItem: MutableStateFlow<HttpDownloadItem>,
        editedDownloadItem: MutableStateFlow<HttpDownloadItem>,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope
    ): HttpEditDownloadInputs {
        return HttpEditDownloadInputs(
            currentDownloadItem = currentDownloadItem,
            editedDownloadItem = editedDownloadItem,
            sizeAndSpeedUnitProvider = sizeAndSpeedUnitProvider,
            mapper = HttpCredentialsToItemMapper,
            conflictDetector = conflictDetector,
            scope = scope,
            linkCheckerFactory = this,
            editDownloadCheckerFactory = this,
        )
    }

    override fun acceptDownloadCredentials(item: IDownloadCredentials): Boolean {
        return item is IHttpDownloadCredentials
    }

    override fun supportsThisLink(link: String): Boolean {
        return HttpUrlUtils.isValidUrl(link)
    }

    override fun createMinimumCredentials(link: String): HttpDownloadCredentials {
        return HttpDownloadCredentials(link = link)
    }

    override fun createProcessingDownloadItemState(
        props: ProcessingDownloadItemFactoryInputs<HttpDownloadJob>
    ): ProcessingDownloadItemState {
        val downloadJob = props.downloadJob
        val downloadItem = downloadJob.downloadItem
        val downloadJobStatus = downloadJob.status.value
        val parts = downloadJob.getParts()
        val contentLength = downloadItem.contentLength
        return RangeBasedProcessingDownloadItemState(
            id = downloadItem.id,
            folder = downloadItem.folder,
            name = downloadItem.name,
            contentLength = contentLength,
            dateAdded = downloadItem.dateAdded,
            startTime = downloadItem.startTime ?: -1,
            completeTime = downloadItem.completeTime ?: -1,
            status = downloadJobStatus,
            saveLocation = downloadItem.name,
            parts = parts.map {
                UiRangedPart.fromPart(
                    part = it,
                    totalLength = contentLength,
                )
            },
            speed = props.speed,
            supportResume = downloadJob.supportsConcurrent,
            downloadLink = downloadItem.link,
            isWaiting = props.isWaiting,
        )
    }

    override fun createBareDownloadItem(
        credentials: HttpDownloadCredentials,
        basicDownloadItem: BasicDownloadItem
    ): HttpDownloadItem {
        return HttpDownloadItem.createWithCredentials(
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

    override val name: StringSource = "HTTP".asStringSource()
    override fun createEditDownloadChecker(
        currentDownloadItem: MutableStateFlow<HttpDownloadItem>,
        editedDownloadItem: MutableStateFlow<HttpDownloadItem>,
        linkChecker: HttpLinkChecker,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope
    ): HttpEditDownloadChecker {
        return HttpEditDownloadChecker(
            currentDownloadItem = currentDownloadItem,
            editedDownloadItem = editedDownloadItem,
            linkChecker = linkChecker,
            conflictDetector = conflictDetector,
            scope = scope,
        )
    }
}
