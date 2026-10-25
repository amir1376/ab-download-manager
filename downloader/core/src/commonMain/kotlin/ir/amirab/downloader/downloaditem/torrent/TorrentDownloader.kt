package ir.amirab.downloader.downloaditem.torrent

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.Downloader
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass


class TorrentDownloader(
    client: Lazy<HttpDownloaderClient>
) : Downloader<TorrentDownloadItem, TorrentDownloadJob, TorrentDownloadCredentials> {

    val client: HttpDownloaderClient by client

    override fun createJob(
        item: TorrentDownloadItem,
        downloadManager: DownloadManager
    ): TorrentDownloadJob {
        return TorrentDownloadJob(
            downloadItem = item,
            downloadManager = downloadManager,
            client = client,
        )
    }

    override fun accept(item: IDownloadItem): Boolean {
        return item is TorrentDownloadItem
    }

    override val downloadItemClass: KClass<TorrentDownloadItem> = TorrentDownloadItem::class
    override val downloadCredentialsClass: KClass<TorrentDownloadCredentials> = TorrentDownloadCredentials::class
    override val downloadJobClass: KClass<TorrentDownloadJob> = TorrentDownloadJob::class
    override val downloadItemSerializer: KSerializer<TorrentDownloadItem> = TorrentDownloadItem.serializer()
    override val downloadCredentialsSerializer: KSerializer<TorrentDownloadCredentials> =
        TorrentDownloadCredentials.serializer()
}
