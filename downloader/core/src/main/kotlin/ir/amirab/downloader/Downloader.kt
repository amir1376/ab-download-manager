package ir.amirab.downloader

import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

interface Downloader<TDownloadItem : IDownloadItem, TDownloadJob : DownloadJob> {
    fun createJob(
        item: TDownloadItem,
        downloadManager: DownloadManager,
    ): TDownloadJob

    /**
     * accept if and only if [IDownloadItem] is [TDownloadItem]
     * */
    fun accept(item: IDownloadItem): Boolean
    val downloadItemClass: KClass<TDownloadItem>
    val downloadJobClass: KClass<TDownloadJob>

    val downloadItemSerializer: KSerializer<TDownloadItem>
}

