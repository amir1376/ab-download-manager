package com.abdownloadmanager.shared.downloaderinui.http

import com.abdownloadmanager.shared.downloaderinui.DownloaderInUi
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpDownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpLinkChecker
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpNewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.http.edit.HttpEditDownloadChecker
import com.abdownloadmanager.shared.downloaderinui.http.edit.HttpEditDownloadInputs
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.utils.DownloadSystem
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadJob
import ir.amirab.downloader.downloaditem.http.HttpDownloader
import ir.amirab.downloader.downloaditem.http.IHttpDownloadCredentials
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.UiPart
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class HttpDownloaderInUi(
    httpDownloader: HttpDownloader,
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider,
) : DownloaderInUi<HttpDownloadCredentials, HttpResponseInfo, HttpLinkChecker, HttpDownloadItem, HttpNewDownloadInputs, HttpEditDownloadInputs, HttpCredentialsToItemMapper, HttpDownloadJob, HttpDownloader>(
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

    override fun acceptCredentials(credentials: IDownloadCredentials): Boolean {
        return credentials is IHttpDownloadCredentials
    }

    override fun acceptDownloadItem(item: IDownloadItem): Boolean {
        return item is HttpDownloadItem
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
        downloadJob: HttpDownloadJob,
        speed: Long
    ): ProcessingDownloadItemState {
        val downloadItem = downloadJob.downloadItem
        val downloadJobStatus = downloadJob.status.value
        val parts = downloadJob.getParts()
        return ProcessingDownloadItemState(
            id = downloadItem.id,
            folder = downloadItem.folder,
            name = downloadItem.name,
            contentLength = downloadItem.contentLength ?: -1,
            dateAdded = downloadItem.dateAdded,
            startTime = downloadItem.startTime ?: -1,
            completeTime = downloadItem.completeTime ?: -1,
            status = downloadJobStatus,
            saveLocation = downloadItem.name,
            parts = parts.map {
                UiPart.fromPart(it)
            },
            speed = speed,
            supportResume = downloadJob.supportsConcurrent,
            downloadLink = downloadItem.link
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
