package com.abdownloadmanager.shared.downloaderinui.ytdlp

import com.abdownloadmanager.shared.downloaderinui.BasicDownloadItem
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUi
import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.ytdlp.add.YtdlpNewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.ytdlp.add.YtdlpNewDownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.ytdlp.edit.YtdlpEditDownloadChecker
import com.abdownloadmanager.shared.downloaderinui.ytdlp.edit.YtdlpEditDownloadInputs
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadItem
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadCredentials
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadJob
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloader
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpResponseInfo
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemFactoryInputs
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.RangeBasedProcessingDownloadItemState
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class YtdlpDownloaderInUi(
    ytdlpDownloader: YtdlpDownloader,
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider,
) : DownloaderInUi<
        YtdlpDownloadCredentials,
        YtdlpResponseInfo,
        DownloadSize.Bytes,
        YtdlpLinkChecker,
        YtdlpDownloadItem,
        YtdlpNewDownloadInputs,
        YtdlpEditDownloadInputs,
        YtdlpCredentialsToItemMapper,
        YtdlpDownloadJob,
        YtdlpDownloader,
        >(
    downloader = ytdlpDownloader
) {
    override fun createLinkChecker(initialCredentials: YtdlpDownloadCredentials): YtdlpLinkChecker {
        return YtdlpLinkChecker(initialCredentials)
    }

    override fun newDownloadUiChecker(
        initialCredentials: YtdlpDownloadCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope
    ): YtdlpNewDownloadUiChecker {
        return YtdlpNewDownloadUiChecker(
            initialCredentials = initialCredentials,
            linkCheckerFactory = this,
            initialFolder = initialFolder,
            initialName = initialName,
            downloadSystem = downloadSystem,
            scope = scope
        )
    }

    override fun createNewDownloadInputs(
        initialCredentials: YtdlpDownloadCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope
    ): YtdlpNewDownloadInputs {
        val uiChecker = newDownloadUiChecker(
            initialCredentials,
            initialFolder,
            initialName,
            downloadSystem,
            scope
        )
        return YtdlpNewDownloadInputs(
            downloadUiChecker = uiChecker,
            scope = scope,
            sizeAndSpeedUnitProvider = sizeAndSpeedUnitProvider
        )
    }

    override fun createEditDownloadChecker(
        currentDownloadItem: MutableStateFlow<YtdlpDownloadItem>,
        editedDownloadItem: MutableStateFlow<YtdlpDownloadItem>,
        linkChecker: YtdlpLinkChecker,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope,
    ): YtdlpEditDownloadChecker {
        return YtdlpEditDownloadChecker(
            currentDownloadItem = currentDownloadItem,
            editedDownloadItem = editedDownloadItem,
            conflictDetector = conflictDetector,
            scope = scope,
            linkChecker = linkChecker
        )
    }

    override fun createEditDownloadInputs(
        currentDownloadItem: MutableStateFlow<YtdlpDownloadItem>,
        editedDownloadItem: MutableStateFlow<YtdlpDownloadItem>,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope
    ): YtdlpEditDownloadInputs {
        return YtdlpEditDownloadInputs(
            currentDownloadItem = currentDownloadItem,
            editedDownloadItem = editedDownloadItem,
            sizeAndSpeedUnitProvider = sizeAndSpeedUnitProvider,
            mapper = YtdlpCredentialsToItemMapper,
            conflictDetector = conflictDetector,
            scope = scope,
            linkCheckerFactory = this,
            editDownloadCheckerFactory = this
        )
    }

    override fun acceptDownloadCredentials(item: IDownloadCredentials): Boolean {
        return item is YtdlpDownloadCredentials
    }

    override fun supportsThisLink(link: String): Boolean {
        val lower = link.lowercase()
        return lower.contains("youtube.com") || lower.contains("youtu.be") ||
               lower.contains("facebook.com") || lower.contains("fb.watch") ||
               lower.contains("fb.com")
    }

    override fun createMinimumCredentials(link: String): YtdlpDownloadCredentials {
        return YtdlpDownloadCredentials(link = link)
    }

    override fun createBareDownloadItem(
        credentials: YtdlpDownloadCredentials,
        basicDownloadItem: BasicDownloadItem
    ): YtdlpDownloadItem {
        return YtdlpDownloadItem.createWithCredentials(
            id = -1,
            credentials = credentials,
            folder = basicDownloadItem.folder,
            name = basicDownloadItem.name,
            dateAdded = System.currentTimeMillis()
        )
    }

    override fun createProcessingDownloadItemState(props: ProcessingDownloadItemFactoryInputs<YtdlpDownloadJob>): ProcessingDownloadItemState {
        val job = props.downloadJob
        val item = job.downloadItem
        return RangeBasedProcessingDownloadItemState(
            id = item.id,
            folder = item.folder,
            name = item.name,
            contentLength = item.contentLength,
            dateAdded = item.dateAdded,
            startTime = item.startTime ?: -1,
            completeTime = item.completeTime ?: -1,
            status = job.status.value,
            saveLocation = item.name,
            parts = emptyList(),
            speed = props.speed,
            supportResume = true,
            downloadLink = item.link,
            isWaiting = props.isWaiting
        )
    }

    override val name: StringSource = "yt-dlp Video Downloader".asStringSource()
}
