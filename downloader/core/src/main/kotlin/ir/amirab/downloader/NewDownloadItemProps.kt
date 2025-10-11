package ir.amirab.downloader

import ir.amirab.downloader.downloaditem.DownloadItemContext
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.utils.OnDuplicateStrategy

data class NewDownloadItemProps(
    val downloadItem: IDownloadItem,
    val extraConfig: DownloadJobExtraConfig?,
    val onDuplicateStrategy: OnDuplicateStrategy,
    val context: DownloadItemContext,
)
