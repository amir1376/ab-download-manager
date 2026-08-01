package ir.amirab.downloader.downloaditem.http

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.Downloader
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

class HttpDownloader(
    httpDownloaderClient: Lazy<HttpDownloaderClient>
) : Downloader<HttpDownloadItem, DownloadJob, HttpDownloadCredentials> {
    val httpDownloaderClient by httpDownloaderClient
    override fun createJob(
        item: HttpDownloadItem,
        downloadManager: DownloadManager,
    ): DownloadJob {
        return if (item.bundle == null) {
            HttpDownloadJob(item, downloadManager, httpDownloaderClient)
        } else {
            HttpBundleDownloadJob(item, downloadManager, httpDownloaderClient)
        }
    }

    override fun accept(item: IDownloadItem): Boolean {
        return item is HttpDownloadItem
    }

    override val downloadItemClass: KClass<HttpDownloadItem> = HttpDownloadItem::class
    override val downloadCredentialsClass: KClass<HttpDownloadCredentials> = HttpDownloadCredentials::class
    override val downloadJobClass: KClass<DownloadJob> = DownloadJob::class
    override val downloadItemSerializer: KSerializer<HttpDownloadItem> = HttpDownloadItem.serializer()
    override val downloadCredentialsSerializer: KSerializer<HttpDownloadCredentials> =
        HttpDownloadCredentials.serializer()
}
