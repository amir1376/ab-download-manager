package com.abdownloadmanager.shared.downloaderinui.add

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.DownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsItem
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

abstract class NewDownloadInputs<
        TDownloadItem : IDownloadItem,
        TCredentials : IDownloadCredentials,
        TResponseInfoType : IResponseInfo,
        TDownloadSize : DownloadSize,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfoType, TDownloadSize>,
        >(
    val downloadUiChecker: DownloadUiChecker<TCredentials, TResponseInfoType, TDownloadSize, TLinkChecker>
) {
    val openedTime = System.currentTimeMillis()

    val name = downloadUiChecker.name
    val folder = downloadUiChecker.folder
    val credentials = downloadUiChecker.credentials
    val downloadSize = downloadUiChecker.downloadSize
    abstract val downloadItem: StateFlow<TDownloadItem>
    abstract val downloadJobConfig: StateFlow<DownloadJobExtraConfig?>
    abstract val configurableList: List<Configurable<*>>

    abstract fun applyHostSettingsToExtraConfig(extraConfig: PerHostSettingsItem)

    fun setCredentials(credentials: TCredentials) {
        downloadUiChecker.credentials.update { credentials }
    }

    abstract fun downloadSizeToStringSource(downloadSize: TDownloadSize): StringSource?

    val lengthStringFlow: StateFlow<StringSource> = downloadSize.mapStateFlow {
        it
            ?.let(::downloadSizeToStringSource)
            ?: Res.string.unknown.asStringSource()
    }

    fun getLengthString(): StringSource {
        return lengthStringFlow.value
    }

    fun getUniqueId(): NewDownloadInputsUniqueIdType = hashCode()
}
typealias TANewDownloadInputs = NewDownloadInputs<IDownloadItem, IDownloadCredentials, IResponseInfo, DownloadSize, LinkChecker<IDownloadCredentials, IResponseInfo, DownloadSize>>
typealias NewDownloadInputsUniqueIdType = Int
