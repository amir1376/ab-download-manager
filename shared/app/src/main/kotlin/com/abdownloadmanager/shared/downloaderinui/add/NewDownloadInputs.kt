package com.abdownloadmanager.shared.downloaderinui.add

import com.abdownloadmanager.shared.downloaderinui.DownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsItem
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

abstract class NewDownloadInputs<
        TDownloadItem : IDownloadItem,
        TCredentials : IDownloadCredentials,
        TResponseInfoType : IResponseInfo,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfoType>,
        >(
    val downloadUiChecker: DownloadUiChecker<TCredentials, TResponseInfoType, TLinkChecker>
) {
    val openedTime = System.currentTimeMillis()

    val name = downloadUiChecker.name
    val folder = downloadUiChecker.folder
    val credentials = downloadUiChecker.credentials

    abstract val downloadItem: StateFlow<TDownloadItem>
    abstract val downloadJobConfig: StateFlow<DownloadJobExtraConfig?>
    abstract val configurableList: List<Configurable<*>>

    abstract fun applyHostSettingsToExtraConfig(extraConfig: PerHostSettingsItem)

    fun setCredentials(credentials: TCredentials) {
        downloadUiChecker.credentials.update { credentials }
    }

    abstract val lengthStringFlow: StateFlow<StringSource>
    fun getLengthString(): StringSource {
        return lengthStringFlow.value
    }

    fun getUniqueId() = hashCode()
}
typealias TANewDownloadInputs = NewDownloadInputs<IDownloadItem, IDownloadCredentials, IResponseInfo, LinkChecker<IDownloadCredentials, IResponseInfo>>
