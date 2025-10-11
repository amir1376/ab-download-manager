package ir.amirab.downloader.downloaditem.hls

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.Downloader
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass


class HLSDownloader(
    client: Lazy<HttpDownloaderClient>
) : Downloader<HLSDownloadItem, HLSDownloadJob> {

    val client: HttpDownloaderClient by client

    override fun createJob(
        item: HLSDownloadItem,
        downloadManager: DownloadManager
    ): HLSDownloadJob {
        return HLSDownloadJob(
            downloadItem = item,
            downloadManager = downloadManager,
            client = client,
        )
    }

    override fun accept(item: IDownloadItem): Boolean {
        return item is HLSDownloadItem
    }

    override val downloadItemClass: KClass<HLSDownloadItem> = HLSDownloadItem::class
    override val downloadJobClass: KClass<HLSDownloadJob> = HLSDownloadJob::class
    override val downloadItemSerializer: KSerializer<HLSDownloadItem> = HLSDownloadItem.serializer()
}
