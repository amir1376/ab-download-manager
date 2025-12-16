package ir.amirab.downloader.downloaditem.http

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.Downloader
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

class HttpDownloader(
    httpDownloaderClient: Lazy<HttpDownloaderClient>
) : Downloader<HttpDownloadItem, HttpDownloadJob, HttpDownloadCredentials> {
    val httpDownloaderClient by httpDownloaderClient
    override fun createJob(
        item: HttpDownloadItem,
        downloadManager: DownloadManager,
    ): HttpDownloadJob {
        return HttpDownloadJob(
            item,
            downloadManager,
            httpDownloaderClient,
        )
    }

    override fun accept(item: IDownloadItem): Boolean {
        return item is HttpDownloadItem
    }

    override val downloadItemClass: KClass<HttpDownloadItem> = HttpDownloadItem::class
    override val downloadCredentialsClass: KClass<HttpDownloadCredentials> = HttpDownloadCredentials::class
    override val downloadJobClass: KClass<HttpDownloadJob> = HttpDownloadJob::class
    override val downloadItemSerializer: KSerializer<HttpDownloadItem> = HttpDownloadItem.serializer()
    override val downloadCredentialsSerializer: KSerializer<HttpDownloadCredentials> =
        HttpDownloadCredentials.serializer()
}
