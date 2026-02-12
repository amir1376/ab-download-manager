package com.abdownloadmanager.shared.downloaderinui

import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputsFactory
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadCheckerFactory
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputsFactory
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.Downloader
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.DownloadItemStateFactory
import ir.amirab.downloader.monitor.ProcessingDownloadItemFactoryInputs
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.CoroutineScope

/**
 * This is a class that represent a downloader implementation details tight to the Application not just the downloader logic
 * including ui, component factories and every thing that app need work with
 */
abstract class DownloaderInUi<
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TDownloadSize : DownloadSize,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo, TDownloadSize>,
        TDownloadItem : IDownloadItem,
        TNewDownloadInputs : NewDownloadInputs<TDownloadItem, TCredentials, TResponseInfo, TDownloadSize, TLinkChecker>,
        TEditDownloadInputs : EditDownloadInputs<TDownloadItem, TCredentials, TResponseInfo, TDownloadSize, TLinkChecker, TCredentialAndItemMapper>,
        TCredentialAndItemMapper : CredentialAndItemMapper<TCredentials, TDownloadItem>,
        TDownloadJob : DownloadJob,
        TDownloader : Downloader<TDownloadItem, TDownloadJob, TCredentials>
        >(
    val downloader: TDownloader
) :
    LinkCheckerFactory<TCredentials, TResponseInfo, TDownloadSize, TLinkChecker>,
    EditDownloadCheckerFactory<TDownloadItem, TCredentials, TResponseInfo, TDownloadSize, TLinkChecker>,
    NewDownloadInputsFactory<TDownloadItem, TCredentials, TResponseInfo, TDownloadSize, TLinkChecker, TNewDownloadInputs>,
    EditDownloadInputsFactory<TDownloadItem, TCredentials, TResponseInfo, TDownloadSize, TLinkChecker, TCredentialAndItemMapper, TEditDownloadInputs>,
    DownloadItemStateFactory<TDownloadItem, TDownloadJob> {
    abstract fun newDownloadUiChecker(
        initialCredentials: TCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope,
    ): DownloadUiChecker<TCredentials, TResponseInfo, TDownloadSize, TLinkChecker>


    abstract fun acceptDownloadCredentials(item: IDownloadCredentials): Boolean
    abstract fun supportsThisLink(link: String): Boolean
    abstract fun createMinimumCredentials(link: String): TCredentials
    abstract fun createBareDownloadItem(
        credentials: TCredentials,
        basicDownloadItem: BasicDownloadItem
    ): TDownloadItem

    abstract override fun createProcessingDownloadItemState(
        props: ProcessingDownloadItemFactoryInputs<TDownloadJob>,
    ): ProcessingDownloadItemState

    override fun createCompletedDownloadItemState(
        downloadItem: TDownloadItem,
    ): CompletedDownloadItemState {
        return CompletedDownloadItemState.fromDownloadItem(downloadItem)
    }

    abstract val name: StringSource
}
