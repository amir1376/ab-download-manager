package com.abdownloadmanager.shared.downloaderinui.ytdlp.add

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.ytdlp.YtdlpLinkChecker
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadCredentials
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadItem
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpResponseInfo
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.combineStateFlows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class YtdlpNewDownloadInputs(
    downloadUiChecker: YtdlpNewDownloadUiChecker,
    scope: CoroutineScope,
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider,
) : NewDownloadInputs<
        YtdlpDownloadItem,
        YtdlpDownloadCredentials,
        YtdlpResponseInfo,
        DownloadSize.Bytes,
        YtdlpLinkChecker,
        >(
    downloadUiChecker
) {
    override val downloadItem: StateFlow<YtdlpDownloadItem> = combineStateFlows(
        this.credentials,
        this.folder,
        this.name,
        this.downloadSize,
    ) { credentials, folder, name, length ->
        YtdlpDownloadItem(
            id = -1,
            folder = folder,
            name = name,
            link = credentials.link,
            contentLength = length?.bytes ?: -1,
            dateAdded = openedTime,
            startTime = null,
            completeTime = null,
            status = DownloadStatus.Added,
            preferredConnectionCount = null,
            speedLimit = 0,
            fileChecksum = null
        )
    }

    override val downloadJobConfig: StateFlow<DownloadJobExtraConfig?> = MutableStateFlow(null)

    override fun applyHostSettingsToExtraConfig(extraConfig: com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsItem) {
    }

    override val configurableList: List<com.abdownloadmanager.shared.ui.configurable.Configurable<*>> = emptyList()

    override fun downloadSizeToStringSource(downloadSize: DownloadSize.Bytes): StringSource {
        return downloadSize.asStringSource(sizeAndSpeedUnitProvider.sizeUnit.value)
    }
}
