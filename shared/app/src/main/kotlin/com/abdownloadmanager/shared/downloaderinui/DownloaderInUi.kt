package com.abdownloadmanager.shared.downloaderinui

import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputsFactory
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadCheckerFactory
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputsFactory
import com.abdownloadmanager.shared.utils.DownloadSystem
import ir.amirab.downloader.Downloader
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.CoroutineScope

abstract class DownloaderInUi<
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo>,
        TDownloadItem : IDownloadItem,
        TNewDownloadInputs : NewDownloadInputs<TDownloadItem, TCredentials, TResponseInfo, TLinkChecker>,
        TEditDownloadInputs : EditDownloadInputs<TDownloadItem, TCredentials, TResponseInfo, TLinkChecker, TCredentialAndItemMapper>,
        TCredentialAndItemMapper : CredentialAndItemMapper<TCredentials, TDownloadItem>,
        TDownloadJob : DownloadJob,
        TDownloader : Downloader<TDownloadItem, TDownloadJob>
        >(
    val downloader: TDownloader
) :
    LinkCheckerFactory<TCredentials, TResponseInfo, TLinkChecker>,
    EditDownloadCheckerFactory<TDownloadItem, TCredentials, TResponseInfo, TLinkChecker>,
    NewDownloadInputsFactory<TDownloadItem, TCredentials, TResponseInfo, TLinkChecker, TNewDownloadInputs>,
    EditDownloadInputsFactory<TDownloadItem, TCredentials, TResponseInfo, TLinkChecker, TCredentialAndItemMapper, TEditDownloadInputs> {
    abstract fun newDownloadUiChecker(
        initialCredentials: TCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope,
    ): DownloadUiChecker<TCredentials, TResponseInfo, TLinkChecker>


    abstract fun acceptCredentials(credentials: IDownloadCredentials): Boolean
    abstract fun acceptDownloadItem(item: IDownloadItem): Boolean
    abstract fun acceptDownloadCredentials(item: IDownloadCredentials): Boolean
    abstract fun supportsThisLink(link: String): Boolean
    abstract fun createMinimumCredentials(link: String): TCredentials

    abstract fun createProcessingDownloadItemState(
        downloadJob: TDownloadJob,
        speed: Long,
    ): ProcessingDownloadItemState

    open fun createCompletedDownloadItemState(
        downloadItem: TDownloadItem,
    ): CompletedDownloadItemState {
        return CompletedDownloadItemState.fromDownloadItem(downloadItem)
    }

    abstract val name: StringSource
}
